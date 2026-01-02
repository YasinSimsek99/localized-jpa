package com.localizedjpa.compiler;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

public class JavacAstModifier {

    private final ProcessingEnvironment processingEnv;
    private final TreeMaker treeMaker;
    private final Names names;
    private final JavacTrees trees;

    /**
     * Creates a JavacAstModifier if the environment supports it.
     * Returns null if running in a non-javac compiler (e.g., Eclipse)
     * or if the JVM module system blocks access to internal APIs.
     * 
     * This method handles various wrapper patterns used by IDEs and build tools:
     * - IntelliJ IDEA 2020.3+ (Proxy pattern)
     * - Gradle incremental compilation (delegate field)
     * - Kotlin kapt (processingEnv field)
     */
    public static JavacAstModifier createIfSupported(ProcessingEnvironment processingEnv) {
        try {
            // Try to unwrap the environment to find real JavacProcessingEnvironment
            ProcessingEnvironment javacEnv = unwrapJavacProcessingEnvironment(processingEnv);
            
            if (javacEnv == null) {
                // Could not find javac environment
                logUnsupportedCompiler(processingEnv);
                return null;
            }
            
            // Log if we unwrapped something (helpful for debugging)
            if (javacEnv != processingEnv) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, 
                    "[LocalizedJPA] Unwrapped ProcessingEnvironment: " +
                    processingEnv.getClass().getSimpleName() + " -> " +
                    javacEnv.getClass().getSimpleName());
            }
            
            // Verify it's actually JavacProcessingEnvironment
            if (!(javacEnv instanceof JavacProcessingEnvironment)) {
                logUnsupportedCompiler(processingEnv);
                return null;
            }
            
            return new JavacAstModifier(javacEnv);
        } catch (Throwable e) {
            // Catch Throwable to handle both Exceptions and Errors (like IllegalAccessError)
            // This occurs when JVM module system blocks access to internal APIs
            // Catch Throwable to handle both Exceptions and Errors (like IllegalAccessError)
            // This occurs when JVM module system blocks access to internal APIs
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, 
                "[LocalizedJPA] Failed to create JavacAstModifier: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Unwraps ProcessingEnvironment to find the real JavacProcessingEnvironment.
     * Handles various wrapper patterns used by IDEs and build tools.
     * 
     * @param env The processing environment (possibly wrapped)
     * @return The unwrapped JavacProcessingEnvironment, or null if not found
     */
    private static ProcessingEnvironment unwrapJavacProcessingEnvironment(ProcessingEnvironment env) {
        // Direct javac?
        if (isJavacProcessingEnvironment(env)) {
            return env;
        }
        
        // Try recursive unwrapping
        return tryRecursiveUnwrap(env);
    }
    
    /**
     * Checks if the environment is a JavacProcessingEnvironment.
     */
    private static boolean isJavacProcessingEnvironment(ProcessingEnvironment env) {
        return env.getClass().getName().equals(
            "com.sun.tools.javac.processing.JavacProcessingEnvironment");
    }
    
    /**
     * Recursively unwraps wrapper environments to find JavacProcessingEnvironment.
     * Handles:
     * - Gradle incremental compilation (delegate field)
     * - Kotlin kapt (processingEnv field)
     * - IntelliJ IDEA 2020.3+ (Proxy pattern with val$delegateTo)
     */
    private static ProcessingEnvironment tryRecursiveUnwrap(ProcessingEnvironment env) {
        if (isJavacProcessingEnvironment(env)) {
            return env;
        }
        
        // Try common wrapper patterns
        for (Class<?> clazz = env.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            // 1. Gradle incremental compilation: delegate field
            ProcessingEnvironment delegate = tryGetField(env, clazz, "delegate");
            if (delegate != null) {
                ProcessingEnvironment unwrapped = tryRecursiveUnwrap(delegate);
                if (unwrapped != null) return unwrapped;
            }
            
            // 2. Kotlin kapt: processingEnv field
            delegate = tryGetField(env, clazz, "processingEnv");
            if (delegate != null) {
                ProcessingEnvironment unwrapped = tryRecursiveUnwrap(delegate);
                if (unwrapped != null) return unwrapped;
            }
            
            // 3. IntelliJ IDEA 2020.3+: Proxy pattern
            delegate = tryGetProxyDelegate(env);
            if (delegate != null) {
                ProcessingEnvironment unwrapped = tryRecursiveUnwrap(delegate);
                if (unwrapped != null) return unwrapped;
            }
        }
        
        return null;
    }
    
    /**
     * Attempts to get a field value from an object using reflection.
     * Returns null if field doesn't exist or can't be accessed.
     */
    private static ProcessingEnvironment tryGetField(Object instance, Class<?> clazz, String fieldName) {
        try {
            java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
            Permit.setAccessible(field);
            Object value = field.get(instance);
            if (value instanceof ProcessingEnvironment) {
                return (ProcessingEnvironment) value;
            }
        } catch (Exception e) {
            // Field doesn't exist or access denied - not an error, just try next pattern
        }
        return null;
    }
    
    /**
     * Attempts to unwrap IntelliJ IDEA's Proxy pattern.
     * IntelliJ wraps the ProcessingEnvironment in a dynamic proxy with val$delegateTo field.
     */
    private static ProcessingEnvironment tryGetProxyDelegate(Object instance) {
        try {
            if (!java.lang.reflect.Proxy.isProxyClass(instance.getClass())) {
                return null;
            }
            
            java.lang.reflect.InvocationHandler handler = 
                java.lang.reflect.Proxy.getInvocationHandler(instance);
            
            // IntelliJ pattern: val$delegateTo field in the handler
            java.lang.reflect.Field field = handler.getClass().getDeclaredField("val$delegateTo");
            Permit.setAccessible(field);
            Object value = field.get(handler);
            
            if (value instanceof ProcessingEnvironment) {
                return (ProcessingEnvironment) value;
            }
        } catch (Exception e) {
            // Not a proxy or field doesn't exist - not an error
        }
        return null;
    }
    
    /**
     * Logs a helpful error message when an unsupported compiler is detected.
     */
    private static void logUnsupportedCompiler(ProcessingEnvironment processingEnv) {
        String compilerClass = processingEnv.getClass().getName();
        String message;
        
        if (compilerClass.contains("eclipse")) {
            message = String.format(
                "[LocalizedJPA] Currently only supports javac compiler. " +
                "Detected Eclipse compiler: %s. " +
                "Please configure your IDE to use javac for compilation.",
                compilerClass
            );
        } else {
            message = String.format(
                "[LocalizedJPA] Could not detect a supported compiler. " +
                "Detected: %s. " +
                "Supported: javac (OpenJDK). " +
                "If using IntelliJ IDEA, enable 'Delegate IDE build/run actions to Maven' in Settings.",
                compilerClass
            );
        }
        
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message);
    }


    private JavacAstModifier(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;

        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
        this.trees = JavacTrees.instance(processingEnv);
    }


    /**
     * Ensures jakarta.persistence.Transient is imported in the compilation unit.
     */
    public void ensureTransientImport(TypeElement classElement) {
        JCTree tree = (JCTree) trees.getTree(classElement);
        if (!(tree instanceof JCClassDecl)) {
            return;
        }

        // Get the compilation unit (file level)
        JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) trees.getPath(classElement).getCompilationUnit();

        // Check if import already exists
        String importStr = "jakarta.persistence.Transient";
        for (JCTree def : compilationUnit.defs) {
            if (def instanceof JCTree.JCImport importDecl) {
                String importName = getImportQualifiedName(importDecl);
                if (importName != null && importName.equals(importStr)) {
                    // Already imported
                    return;
                }
            }
        }

        // Add import
        JCExpression importPath = createQualifiedName(importStr);
        JCTree.JCImport importDecl = createImport(importPath, false);

        // Insert import after the last import statement (or after package, before class)
        com.sun.tools.javac.util.List<JCTree> newDefs = com.sun.tools.javac.util.List.nil();
        JCTree.JCImport lastImport = null;

        // First pass: collect all defs and find last import
        for (JCTree def : compilationUnit.defs) {
            if (def instanceof JCTree.JCImport) {
                lastImport = (JCTree.JCImport) def;
            }
        }

        // Second pass: rebuild defs list, inserting our import after last import
        boolean inserted = false;
        for (JCTree def : compilationUnit.defs) {
            newDefs = newDefs.append(def);
            
            // If this is the last import, add our import right after it
            if (!inserted && lastImport != null && def == lastImport) {
                newDefs = newDefs.append(importDecl);
                inserted = true;
            }
            // If no imports exist and we hit a class def, add import before it
            else if (!inserted && lastImport == null && def instanceof JCClassDecl) {
                // Need to insert before this class, so remove it and re-add
                newDefs = newDefs.reverse().tail.reverse(); // Remove last added (the class)
                newDefs = newDefs.append(importDecl);
                newDefs = newDefs.append(def); // Re-add the class
                inserted = true;
            }
        }

        // Fallback: if still not inserted, add at end
        if (!inserted) {
            newDefs = newDefs.append(importDecl);
        }

        compilationUnit.defs = newDefs;

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                "[LocalizedJPA] Added import: jakarta.persistence.Transient");
    }

    /**
     * Adds @Transient annotation to a localized field.
     * This prevents JPA from creating a column for the field since it's only used as a placeholder.
     */
    public void markFieldAsTransient(TypeElement classElement, String fieldName) {
        JCTree tree = (JCTree) trees.getTree(classElement);
        if (!(tree instanceof JCClassDecl classDecl)) {
            return;
        }

        // Find the field
        for (JCTree member : classDecl.defs) {
            if (member instanceof JCVariableDecl fieldDecl) {
                if (fieldDecl.name.toString().equals(fieldName)) {
                    // Check if @Transient already exists
                    boolean hasTransient = false;
                    for (JCAnnotation annotation : fieldDecl.mods.annotations) {
                        String annotationName = annotation.annotationType.toString();
                        if (annotationName.contains("Transient")) {
                            hasTransient = true;
                            break;
                        }
                    }

                    if (!hasTransient) {
                        // Create @Transient annotation using simple Ident (works better with imports)
                        JCExpression transientType = treeMaker.Ident(names.fromString("Transient"));
                        JCAnnotation transientAnnotation = treeMaker.Annotation(
                                transientType,
                                com.sun.tools.javac.util.List.nil()
                        );

                        // Add to existing annotations
                        fieldDecl.mods.annotations = fieldDecl.mods.annotations.append(transientAnnotation);

                        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                                "[LocalizedJPA] Added @Transient to field: " + fieldName);
                    } else {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                                "[LocalizedJPA] Field already has @Transient: " + fieldName);
                    }
                    return;  // Found the field, exit early
                }
            }
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                "[LocalizedJPA] Field not found for @Transient: " + fieldName);
    }


    /**
     * Injects the translations map field into the class.
     */
    public void injectTranslationsField(TypeElement classElement, String translationClassName) {
        JCTree tree = (JCTree) trees.getTree(classElement);
        if (!(tree instanceof JCClassDecl classDecl)) {
            return;
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                "[LocalizedJPA] Injecting translations field into " + classElement.getSimpleName());

        // Create: private Map<String, ProductTranslation> translations = new HashMap<>();
        JCVariableDecl translationsField = createTranslationsField(translationClassName);
        classDecl.defs = classDecl.defs.prepend(translationsField);
    }

    /**
     * Injects getter method: public String getName() { ... }
     * If method exists, overrides its body with localization logic.
     */
    public void injectLocalizedGetter(TypeElement classElement, String fieldName, String translationClassName) {
        JCTree tree = (JCTree) trees.getTree(classElement);
        if (!(tree instanceof JCClassDecl classDecl)) {
            return;
        }

        String methodName = "get" + capitalize(fieldName);

        // Check if getter without locale already exists
        JCMethodDecl existingGetter = findMethod(classDecl, methodName, 0);
        if (existingGetter == null) {
            // Create new method
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "[LocalizedJPA] Injecting method: " + methodName + "() in " + classElement.getSimpleName());
            JCMethodDecl getter = createLocalizedGetter(fieldName, translationClassName);
            classDecl.defs = classDecl.defs.append(getter);
        } else {
            // Override existing method body
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "[LocalizedJPA] Overriding " + methodName + "() body in " + classElement.getSimpleName());
            JCMethodDecl newGetter = createLocalizedGetter(fieldName, translationClassName);
            existingGetter.body = newGetter.body;
        }

        // Check if getter with Locale parameter already exists
        JCMethodDecl existingGetterWithLocale = findMethod(classDecl, methodName, 1);
        if (existingGetterWithLocale == null) {
            // Create new method
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "[LocalizedJPA] Injecting method: " + methodName + "(Locale)");
            JCMethodDecl getterWithLocale = createLocalizedGetterWithLocale(fieldName, translationClassName);
            classDecl.defs = classDecl.defs.append(getterWithLocale);
        } else {
            // Override existing method body
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "[LocalizedJPA] Overriding " + methodName + "(Locale) with localization logic");
            JCMethodDecl newGetterWithLocale = createLocalizedGetterWithLocale(fieldName, translationClassName);
            existingGetterWithLocale.body = newGetterWithLocale.body;
        }
    }

    /**
     * Injects setter method: public void setName(String value) { ... }
     * If method exists, overrides its body with localization logic.
     */
    public void injectLocalizedSetter(TypeElement classElement, String fieldName, String translationClassName) {
        JCTree tree = (JCTree) trees.getTree(classElement);
        if (!(tree instanceof JCClassDecl classDecl)) {
            return;
        }

        String methodName = "set" + capitalize(fieldName);

        // Check if setter without locale already exists  
        JCMethodDecl existingSetter = findMethod(classDecl, methodName, 1);
        if (existingSetter == null) {
            // Create new method
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "[LocalizedJPA] Injecting method: " + methodName + "(String)");
            JCMethodDecl setter = createLocalizedSetter(fieldName, translationClassName);
            classDecl.defs = classDecl.defs.append(setter);
        } else {
            // Override existing method body AND parameters
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "[LocalizedJPA] Overriding " + methodName + "(String) with localization logic");
            JCMethodDecl newSetter = createLocalizedSetter(fieldName, translationClassName);
            existingSetter.body = newSetter.body;
            existingSetter.params = newSetter.params;  // Also replace parameters!
        }

        // Check if setter with Locale parameter already exists
        JCMethodDecl existingSetterWithLocale = findMethod(classDecl, methodName, 2);
        if (existingSetterWithLocale == null) {
            // Create new method
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "[LocalizedJPA] Injecting method: " + methodName + "(String, Locale)");
            JCMethodDecl setterWithLocale = createLocalizedSetterWithLocale(fieldName, translationClassName);
            classDecl.defs = classDecl.defs.append(setterWithLocale);
        } else {
            // Override existing method body AND parameters
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "[LocalizedJPA] Overriding " + methodName + "(String, Locale) with localization logic");
            JCMethodDecl newSetterWithLocale = createLocalizedSetterWithLocale(fieldName, translationClassName);
            existingSetterWithLocale.body = newSetterWithLocale.body;
            existingSetterWithLocale.params = newSetterWithLocale.params;  // Also replace parameters!
        }
    }

    /**
     * Injects getTranslations() and setTranslations() methods.
     */
    public void injectTranslationsAccessors(TypeElement classElement) {
        JCTree tree = (JCTree) trees.getTree(classElement);
        if (!(tree instanceof JCClassDecl classDecl)) {
            return;
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                "[LocalizedJPA] Injecting getTranslations() and setTranslations()");

        // getTranslations()
        JCMethodDecl getter = createTranslationsGetter();
        classDecl.defs = classDecl.defs.append(getter);

        // setTranslations(Map)
        JCMethodDecl setter = createTranslationsSetter();
        classDecl.defs = classDecl.defs.append(setter);
    }

    // ==================== Helper Methods ====================

    private JCVariableDecl createTranslationsField(String translationClassName) {
        // Create parametrized Map<String, ProductTranslation> type
        JCExpression rawMapType = treeMaker.Ident(names.fromString("java"));
        rawMapType = treeMaker.Select(rawMapType, names.fromString("util"));
        rawMapType = treeMaker.Select(rawMapType, names.fromString("Map"));

        // Type parameters: String and ProductTranslation
        JCExpression stringType = createQualifiedName("java.lang.String");
        JCExpression translationType = createQualifiedName(translationClassName);

        // Create Map<String, ProductTranslation>
        JCExpression parametrizedMapType = treeMaker.TypeApply(rawMapType, List.of(stringType, translationType));

        // Initialize: new HashMap<>()
        JCExpression hashMapType = treeMaker.Ident(names.fromString("java"));
        hashMapType = treeMaker.Select(hashMapType, names.fromString("util"));
        hashMapType = treeMaker.Select(hashMapType, names.fromString("HashMap"));
        JCNewClass init = treeMaker.NewClass(null, List.nil(), hashMapType, List.nil(), null);

        // Create @OneToMany annotation
        JCExpression oneToManyType = createQualifiedName("jakarta.persistence.OneToMany");
        List<JCExpression> oneToManyArgs = List.of(
                treeMaker.Assign(treeMaker.Ident(names.fromString("mappedBy")),
                        treeMaker.Literal("parent")),
                treeMaker.Assign(treeMaker.Ident(names.fromString("cascade")),
                        createCascadeAllArray()),
                treeMaker.Assign(treeMaker.Ident(names.fromString("orphanRemoval")),
                        treeMaker.Literal(true)),
                treeMaker.Assign(treeMaker.Ident(names.fromString("fetch")),
                        createFetchLazy())
        );
        JCAnnotation oneToMany = treeMaker.Annotation(oneToManyType, oneToManyArgs);

        // Create @MapKey(name = "locale")
        JCExpression mapKeyType = createQualifiedName("jakarta.persistence.MapKey");
        List<JCExpression> mapKeyArgs = List.of(
                treeMaker.Assign(treeMaker.Ident(names.fromString("name")),
                        treeMaker.Literal("locale"))
        );
        JCAnnotation mapKey = treeMaker.Annotation(mapKeyType, mapKeyArgs);

        // Create @JsonIgnore to hide translations from JSON response
        JCExpression jsonIgnoreType = createQualifiedName("com.fasterxml.jackson.annotation.JsonIgnore");
        JCAnnotation jsonIgnore = treeMaker.Annotation(jsonIgnoreType, List.nil());

        // Combine annotations: @OneToMany, @MapKey, @JsonIgnore
        List<JCAnnotation> annotations = List.of(oneToMany, mapKey, jsonIgnore);
        JCModifiers modifiers = treeMaker.Modifiers(Flags.PRIVATE, annotations);

        return treeMaker.VarDef(
                modifiers,
                names.fromString("translations"),
                parametrizedMapType,  // Use parametrized type!
                init
        );
    }

    private JCExpression createQualifiedName(String qualifiedName) {
        String[] parts = qualifiedName.split("\\.");
        JCExpression expr = treeMaker.Ident(names.fromString(parts[0]));
        for (int i = 1; i < parts.length; i++) {
            expr = treeMaker.Select(expr, names.fromString(parts[i]));
        }
        return expr;
    }

    private JCExpression createCascadeAllArray() {
        // CascadeType.ALL
        JCExpression cascadeType = createQualifiedName("jakarta.persistence.CascadeType");
        JCExpression all = treeMaker.Select(cascadeType, names.fromString("ALL"));
        return treeMaker.NewArray(null, List.nil(), List.of(all));
    }

    private JCExpression createFetchLazy() {
        JCExpression fetchType = createQualifiedName("jakarta.persistence.FetchType");
        return treeMaker.Select(fetchType, names.fromString("LAZY"));
    }

    private JCMethodDecl createLocalizedGetter(String fieldName, String translationClassName) {
        String methodName = "get" + capitalize(fieldName);
        String capitalizedFieldName = capitalize(fieldName);

        // Method body: Locale locale = LocaleContextHolder.getLocale(); return getName(locale);
        // Locale locale = LocaleContextHolder.getLocale();
        JCExpression localeContextHolder = createQualifiedName("org.springframework.context.i18n.LocaleContextHolder");
        JCExpression getLocaleCall = treeMaker.Apply(
                List.nil(),
                treeMaker.Select(localeContextHolder, names.fromString("getLocale")),
                List.nil()
        );
        JCVariableDecl localeVar = treeMaker.VarDef(
                treeMaker.Modifiers(0),
                names.fromString("locale"),
                createLocaleType(),
                getLocaleCall
        );

        // return getName(locale);
        JCExpression thisIdent = treeMaker.Ident(names.fromString("this"));
        JCExpression methodCall = treeMaker.Apply(
                List.nil(),
                treeMaker.Select(thisIdent, names.fromString(methodName)),
                List.of(treeMaker.Ident(names.fromString("locale")))
        );
        JCReturn returnStmt = treeMaker.Return(methodCall);

        JCBlock body = treeMaker.Block(0, List.of(localeVar, returnStmt));

        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                names.fromString(methodName),
                treeMaker.Ident(names.fromString("String")),
                List.nil(),
                List.nil(),
                List.nil(),
                body,
                null
        );
    }

    private JCMethodDecl createLocalizedGetterWithLocale(String fieldName, String translationClassName) {
        String methodName = "get" + capitalize(fieldName);
        String capitalizedFieldName = capitalize(fieldName);

        // Parameter: Locale locale
        JCVariableDecl localeParam = treeMaker.VarDef(
                treeMaker.Modifiers(Flags.PARAMETER),
                names.fromString("locale"),
                createLocaleType(),
                null
        );

        // Method body: if (translations == null) return null;
        JCExpression translationsField = treeMaker.Ident(names.fromString("translations"));
        JCExpression nullCheck = treeMaker.Binary(JCTree.Tag.EQ, translationsField, treeMaker.Literal(TypeTag.BOT, null));
        JCReturn returnNull1 = treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null));
        JCIf nullCheckIf = treeMaker.If(nullCheck, returnNull1, null);

        // String localeKey = locale.getLanguage();
        JCExpression localeIdent = treeMaker.Ident(names.fromString("locale"));
        JCExpression getLanguageCall = treeMaker.Apply(
                List.nil(),
                treeMaker.Select(localeIdent, names.fromString("getLanguage")),
                List.nil()
        );
        JCVariableDecl localeKeyVar = treeMaker.VarDef(
                treeMaker.Modifiers(0),
                names.fromString("localeKey"),
                treeMaker.Ident(names.fromString("String")),
                getLanguageCall
        );

        // ProductTranslation translation = translations.get(localeKey);
        JCExpression getCall = treeMaker.Apply(
                List.nil(),
                treeMaker.Select(translationsField, names.fromString("get")),
                List.of(treeMaker.Ident(names.fromString("localeKey")))
        );
        JCVariableDecl translationVar = treeMaker.VarDef(
                treeMaker.Modifiers(0),
                names.fromString("translation"),
                createQualifiedName(translationClassName),
                getCall
        );

        // if (translation == null) return null;
        JCExpression translationIdent = treeMaker.Ident(names.fromString("translation"));
        JCExpression translationNullCheck = treeMaker.Binary(JCTree.Tag.EQ, translationIdent, treeMaker.Literal(TypeTag.BOT, null));
        JCReturn returnNull2 = treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null));
        JCIf translationNullCheckIf = treeMaker.If(translationNullCheck, returnNull2, null);

        // return translation.getName();
        JCExpression getFieldCall = treeMaker.Apply(
                List.nil(),
                treeMaker.Select(translationIdent, names.fromString("get" + capitalizedFieldName)),
                List.nil()
        );
        JCReturn returnStmt = treeMaker.Return(getFieldCall);

        JCBlock body = treeMaker.Block(0, List.of(nullCheckIf, localeKeyVar, translationVar, translationNullCheckIf, returnStmt));

        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                names.fromString(methodName),
                treeMaker.Ident(names.fromString("String")),
                List.nil(),
                List.of(localeParam),
                List.nil(),
                body,
                null
        );
    }

    private JCMethodDecl createLocalizedSetter(String fieldName, String translationClassName) {
        String methodName = "set" + capitalize(fieldName);

        // Parameter: String value
        JCVariableDecl valueParam = treeMaker.VarDef(
                treeMaker.Modifiers(Flags.PARAMETER),
                names.fromString("value"),
                treeMaker.Ident(names.fromString("String")),
                null
        );

        // Method body: Locale locale = LocaleContextHolder.getLocale(); setName(value, locale);
        // Locale locale = LocaleContextHolder.getLocale();
        JCExpression localeContextHolder = createQualifiedName("org.springframework.context.i18n.LocaleContextHolder");
        JCExpression getLocaleCall = treeMaker.Apply(
                List.nil(),
                treeMaker.Select(localeContextHolder, names.fromString("getLocale")),
                List.nil()
        );
        JCVariableDecl localeVar = treeMaker.VarDef(
                treeMaker.Modifiers(0),
                names.fromString("locale"),
                createLocaleType(),
                getLocaleCall
        );

        // setName(value, locale);
        JCExpression thisIdent = treeMaker.Ident(names.fromString("this"));
        JCExpression methodCall = treeMaker.Apply(
                List.nil(),
                treeMaker.Select(thisIdent, names.fromString(methodName)),
                List.of(treeMaker.Ident(names.fromString("value")), treeMaker.Ident(names.fromString("locale")))
        );
        JCStatement callStmt = treeMaker.Exec(methodCall);

        JCBlock body = treeMaker.Block(0, List.of(localeVar, callStmt));

        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                names.fromString(methodName),
                treeMaker.TypeIdent(TypeTag.VOID),
                List.nil(),
                List.of(valueParam),
                List.nil(),
                body,
                null
        );
    }

    private JCMethodDecl createLocalizedSetterWithLocale(String fieldName, String translationClassName) {
        String methodName = "set" + capitalize(fieldName);
        String capitalizedFieldName = capitalize(fieldName);

        // Parameters
        JCVariableDecl valueParam = treeMaker.VarDef(
                treeMaker.Modifiers(Flags.PARAMETER),
                names.fromString("value"),
                treeMaker.Ident(names.fromString("String")),
                null
        );
        JCVariableDecl localeParam = treeMaker.VarDef(
                treeMaker.Modifiers(Flags.PARAMETER),
                names.fromString("locale"),
                createLocaleType(),
                null
        );

        // Method body: if (translations == null) translations = new HashMap<>();
        JCExpression translationsField = treeMaker.Ident(names.fromString("translations"));
        JCExpression nullCheck = treeMaker.Binary(JCTree.Tag.EQ, translationsField, treeMaker.Literal(TypeTag.BOT, null));

        JCExpression hashMapType = createQualifiedName("java.util.HashMap");
        JCNewClass newHashMap = treeMaker.NewClass(null, List.nil(), hashMapType, List.nil(), null);
        JCStatement initTranslations = treeMaker.Exec(treeMaker.Assign(translationsField, newHashMap));
        JCIf nullCheckIf = treeMaker.If(nullCheck, initTranslations, null);

        // String localeKey = locale.getLanguage();
        JCExpression localeIdent = treeMaker.Ident(names.fromString("locale"));
        JCExpression getLanguageCall = treeMaker.Apply(
                List.nil(),
                treeMaker.Select(localeIdent, names.fromString("getLanguage")),
                List.nil()
        );
        JCVariableDecl localeKeyVar = treeMaker.VarDef(
                treeMaker.Modifiers(0),
                names.fromString("localeKey"),
                treeMaker.Ident(names.fromString("String")),
                getLanguageCall
        );

        // ProductTranslation translation = translations.get(localeKey);
        JCExpression getCall = treeMaker.Apply(
                List.nil(),
                treeMaker.Select(translationsField, names.fromString("get")),
                List.of(treeMaker.Ident(names.fromString("localeKey")))
        );
        JCVariableDecl translationVar = treeMaker.VarDef(
                treeMaker.Modifiers(0),
                names.fromString("translation"),
                createQualifiedName(translationClassName),
                getCall
        );

        // if (translation == null) { ... }
        JCExpression translationIdent = treeMaker.Ident(names.fromString("translation"));
        JCExpression translationNullCheck = treeMaker.Binary(JCTree.Tag.EQ, translationIdent, treeMaker.Literal(TypeTag.BOT, null));

        // translation = new {EntityName}Translation();
        JCExpression translationType = createQualifiedName(translationClassName);
        JCNewClass newTranslation = treeMaker.NewClass(null, List.nil(), translationType, List.nil(), null);
        JCStatement assignTranslation = treeMaker.Exec(treeMaker.Assign(translationIdent, newTranslation));

        // translation.setParent(this);
        JCExpression setParentCall = treeMaker.Apply(
                List.nil(),
                treeMaker.Select(translationIdent, names.fromString("setParent")),
                List.of(treeMaker.Ident(names.fromString("this")))
        );
        JCStatement setParentStmt = treeMaker.Exec(setParentCall);

        // translation.setLocale(localeKey);
        JCExpression setLocaleCall = treeMaker.Apply(
                List.nil(),
                treeMaker.Select(translationIdent, names.fromString("setLocale")),
                List.of(treeMaker.Ident(names.fromString("localeKey")))
        );
        JCStatement setLocaleStmt = treeMaker.Exec(setLocaleCall);

        // translations.put(localeKey, translation);
        JCExpression putCall = treeMaker.Apply(
                List.nil(),
                treeMaker.Select(translationsField, names.fromString("put")),
                List.of(treeMaker.Ident(names.fromString("localeKey")), translationIdent)
        );
        JCStatement putStmt = treeMaker.Exec(putCall);

        JCBlock ifNullBlock = treeMaker.Block(0, List.of(assignTranslation, setParentStmt, setLocaleStmt, putStmt));
        JCIf translationNullCheckIf = treeMaker.If(translationNullCheck, ifNullBlock, null);

        // translation.setName(value);
        JCExpression setFieldCall = treeMaker.Apply(
                List.nil(),
                treeMaker.Select(translationIdent, names.fromString("set" + capitalizedFieldName)),
                List.of(treeMaker.Ident(names.fromString("value")))
        );
        JCStatement setFieldStmt = treeMaker.Exec(setFieldCall);

        // Complete method body
        JCBlock body = treeMaker.Block(0, List.of(
                nullCheckIf,
                localeKeyVar,
                translationVar,
                translationNullCheckIf,
                setFieldStmt
        ));

        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                names.fromString(methodName),
                treeMaker.TypeIdent(TypeTag.VOID),
                List.nil(),
                List.of(valueParam, localeParam),
                List.nil(),
                body,
                null
        );
    }

    private JCMethodDecl createTranslationsGetter() {
        JCExpression returnExpr = treeMaker.Ident(names.fromString("translations"));
        JCReturn returnStmt = treeMaker.Return(returnExpr);
        JCBlock body = treeMaker.Block(0, List.of(returnStmt));

        JCExpression mapType = treeMaker.Ident(names.fromString("java"));
        mapType = treeMaker.Select(mapType, names.fromString("util"));
        mapType = treeMaker.Select(mapType, names.fromString("Map"));

        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                names.fromString("getTranslations"),
                mapType,
                List.nil(),
                List.nil(),
                List.nil(),
                body,
                null
        );
    }

    private JCMethodDecl createTranslationsSetter() {
        JCVariableDecl param = treeMaker.VarDef(
                treeMaker.Modifiers(Flags.PARAMETER),
                names.fromString("translations"),
                createMapType(),
                null
        );

        // this.translations = translations;
        JCExpression lhs = treeMaker.Select(
                treeMaker.Ident(names.fromString("this")),
                names.fromString("translations")
        );
        JCExpression rhs = treeMaker.Ident(names.fromString("translations"));
        JCStatement assignStmt = treeMaker.Exec(treeMaker.Assign(lhs, rhs));
        JCBlock body = treeMaker.Block(0, List.of(assignStmt));

        return treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                names.fromString("setTranslations"),
                treeMaker.TypeIdent(TypeTag.VOID),
                List.nil(),
                List.of(param),
                List.nil(),
                body,
                null
        );
    }

    private JCExpression createLocaleType() {
        JCExpression type = treeMaker.Ident(names.fromString("java"));
        type = treeMaker.Select(type, names.fromString("util"));
        type = treeMaker.Select(type, names.fromString("Locale"));
        return type;
    }

    private JCExpression createMapType() {
        JCExpression type = treeMaker.Ident(names.fromString("java"));
        type = treeMaker.Select(type, names.fromString("util"));
        type = treeMaker.Select(type, names.fromString("Map"));
        return type;
    }

    /**
     * Finds an existing method with the given name and parameter count.
     *
     * @param classDecl  The class declaration to search
     * @param methodName The name of the method to look for
     * @param paramCount The number of parameters the method should have
     * @return The method if found, null otherwise
     */
    private JCMethodDecl findMethod(JCClassDecl classDecl, String methodName, int paramCount) {
        for (JCTree member : classDecl.defs) {
            if (member instanceof JCMethodDecl method) {
                if (method.name.toString().equals(methodName) &&
                        method.params.size() == paramCount) {
                    return method;
                }
            }
        }
        return null;
    }

    private String capitalize(String str) {
        return StringUtils.capitalize(str);
    }

    /**
     * Gets the qualified name from a JCImport in a JDK version-compatible way.
     * 
     * <p>In JDK 21, the 'qualid' field type changed from JCTree to JCFieldAccess,
     * causing NoSuchFieldError when accessing directly. This method uses reflection
     * to access the field safely across all JDK versions.
     * 
     * <p>Approach:
     * <ul>
     *   <li>Try getQualifiedIdentifier() method first (available in JDK 21+)</li>
     *   <li>Fall back to qualid field via reflection (JDK 17-20)</li>
     *   <li>Last resort: parse toString() output</li>
     * </ul>
     *
     * @param importDecl The import declaration
     * @return The fully qualified import name, or null if unable to determine
     */
    private String getImportQualifiedName(JCTree.JCImport importDecl) {
        if (importDecl == null) {
            return null;
        }

        // Try 1: Use getQualifiedIdentifier() method (JDK 21+)
        try {
            java.lang.reflect.Method method = JCTree.JCImport.class.getMethod("getQualifiedIdentifier");
            Object result = method.invoke(importDecl);
            if (result != null) {
                return result.toString();
            }
        } catch (NoSuchMethodException e) {
            // Method doesn't exist in this JDK version, try next approach
        } catch (Exception e) {
            // Other reflection error, try next approach
        }

        // Try 2: Access qualid field via reflection (JDK 17-20)
        try {
            java.lang.reflect.Field field = JCTree.JCImport.class.getDeclaredField("qualid");
            field.setAccessible(true);
            Object qualid = field.get(importDecl);
            if (qualid != null) {
                return qualid.toString();
            }
        } catch (NoSuchFieldException e) {
            // Field doesn't exist or has different name, try next approach
        } catch (Exception e) {
            // Other reflection error, try next approach
        }

        // Try 3: Parse the import declaration string
        // Format is typically "import package.Class;" or "import static package.Class.method;"
        try {
            String importStr = importDecl.toString().trim();
            if (importStr.startsWith("import ")) {
                importStr = importStr.substring(7).trim(); // Remove "import "
                if (importStr.startsWith("static ")) {
                    importStr = importStr.substring(7).trim(); // Remove "static " if present
                }
                if (importStr.endsWith(";")) {
                    importStr = importStr.substring(0, importStr.length() - 1).trim();
                }
                return importStr;
            }
        } catch (Exception e) {
            // String parsing failed
        }

        return null;
    }

    /**
     * Creates a JCImport in a JDK version-compatible way.
     * 
     * <p>In JDK 25, the TreeMaker.Import() method signature changed from
     * Import(JCTree, boolean) to Import(JCFieldAccess, boolean).
     * This method uses reflection to call the appropriate method.
     *
     * @param qualid The qualified identifier for the import
     * @param staticImport Whether this is a static import
     * @return The JCImport tree node, or null if creation failed
     */
    private JCTree.JCImport createImport(JCExpression qualid, boolean staticImport) {
        // Try 1: JDK 25+ signature - Import(JCFieldAccess, boolean)
        try {
            java.lang.reflect.Method method = TreeMaker.class.getMethod("Import", 
                JCTree.JCFieldAccess.class, boolean.class);
            if (qualid instanceof JCTree.JCFieldAccess) {
                return (JCTree.JCImport) method.invoke(treeMaker, qualid, staticImport);
            }
        } catch (NoSuchMethodException e) {
            // Method with JCFieldAccess signature doesn't exist, try next
        } catch (Exception e) {
            // Other reflection error, try next
        }

        // Try 2: JDK 17-24 signature - Import(JCTree, boolean)
        try {
            java.lang.reflect.Method method = TreeMaker.class.getMethod("Import", 
                JCTree.class, boolean.class);
            return (JCTree.JCImport) method.invoke(treeMaker, qualid, staticImport);
        } catch (NoSuchMethodException e) {
            // Method with JCTree signature doesn't exist
        } catch (Exception e) {
            // Other reflection error
        }

        // Try 3: Direct call as fallback (may fail on incompatible JDK)
        try {
            // This uses the method signature that was compiled against
            // It will work if running on the same JDK version as compiled
            return treeMaker.Import(qualid, staticImport);
        } catch (Throwable e) {
            System.err.println("[LocalizedJPA] Failed to create import: " + e.getMessage());
            return null;
        }
    }
}

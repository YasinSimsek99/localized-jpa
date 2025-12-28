package com.localizedjpa.compiler.processor;

import com.google.auto.service.AutoService;
import com.localizedjpa.annotations.Localized;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Annotation processor that generates repository implementation classes
 * for query methods targeting @Localized fields.
 * 
 * <p><strong>TRUE ZERO-BOILERPLATE:</strong> Directly scans repository interface
 * for method declarations - no Custom interface needed!
 */
@AutoService(Processor.class)
public class LocalizedRepositoryProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    // Thread-safe set for multi-threaded annotation processing
    private final Set<String> processedRepositories = 
        Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        
        messager.printMessage(Diagnostic.Kind.NOTE, 
            "LocalizedRepositoryProcessor initialized");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // Return the latest version to support both Java 17 and 21
        return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of("*");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        Set<? extends Element> allElements = roundEnv.getRootElements();

        for (Element element : allElements) {
            if (element.getKind() == ElementKind.INTERFACE) {
                TypeElement interfaceElement = (TypeElement) element;
                String className = interfaceElement.getQualifiedName().toString();
                
                if (isRepositoryInterface(interfaceElement) && !processedRepositories.contains(className)) {
                    processRepositoryInterface(interfaceElement);
                    processedRepositories.add(className);
                }
            }
        }

        return false;
    }

    private boolean isRepositoryInterface(TypeElement interfaceElement) {
        String className = interfaceElement.getQualifiedName().toString();
        
        if (className.startsWith("com.localizedjpa.runtime")) {
            return false;
        }
        
        for (TypeMirror superInterface : interfaceElement.getInterfaces()) {
            String superName = superInterface.toString();
            if (superName.contains("LocalizedRepository") || 
                superName.contains("JpaRepository") ||
                superName.contains("Repository")) {
                return true;
            }
        }
        
        return false;
    }

    private void processRepositoryInterface(TypeElement repositoryInterface) {
        String repositoryName = repositoryInterface.getSimpleName().toString();
        String packageName = processingEnv.getElementUtils()
            .getPackageOf(repositoryInterface).getQualifiedName().toString();

        List<ExecutableElement> methodsToImplement = new ArrayList<>();
        
        // Scan repository interface DIRECTLY for query methods
        for (Element member : repositoryInterface.getEnclosedElements()) {
            if (member.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) member;
                
                if (shouldImplementMethod(method, repositoryInterface)) {
                    methodsToImplement.add(method);
                    messager.printMessage(Diagnostic.Kind.NOTE,
                        "  - Will implement: " + method.getSimpleName());
                }
            }
        }

        if (!methodsToImplement.isEmpty()) {
            try {
                generateImplementation(packageName, repositoryName, methodsToImplement);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                    "Failed to generate implementation for " + repositoryName + ": " + e.getMessage());
            }
        }
    }

    private boolean shouldImplementMethod(ExecutableElement method, TypeElement repositoryInterface) {
        String methodName = method.getSimpleName().toString();
        
        if (!methodName.startsWith("findBy")) {
            return false;
        }

        String fieldName = extractFieldName(methodName);
        TypeElement entityType = getEntityType(repositoryInterface);
        
        if (entityType == null) {
            return false;
        }

        return isLocalizedField(entityType, fieldName);
    }

    private String extractFieldName(String methodName) {
        String withoutPrefix = methodName.substring(6); // Remove "findBy"
        
        // Remove query keywords (Containing, And, Or, etc.)
        withoutPrefix = removeQueryKeywords(withoutPrefix);
        
        return Character.toLowerCase(withoutPrefix.charAt(0)) + withoutPrefix.substring(1);
    }
    
    private String removeQueryKeywords(String fieldPart) {
        // Remove "Containing" keyword first
        if (fieldPart.endsWith("Containing")) {
            fieldPart = fieldPart.substring(0, fieldPart.length() - 10);
        }
        
        // Handle multiple fields (And/Or)
        int andIndex = fieldPart.indexOf("And");
        int orIndex = fieldPart.indexOf("Or");
        
        if (andIndex > 0) {
            fieldPart = fieldPart.substring(0, andIndex);
        } else if (orIndex > 0) {
            fieldPart = fieldPart.substring(0, orIndex);
        }
        
        return fieldPart;
    }
    
    private boolean isContainingQuery(String methodName) {
        return methodName.contains("Containing");
    }
    
    private boolean isPageableQuery(ExecutableElement method) {
        List<? extends VariableElement> params = method.getParameters();
        return !params.isEmpty() && 
               params.get(params.size() - 1).asType().toString().contains("Pageable");
    }

    private TypeElement getEntityType(TypeElement repositoryInterface) {
        for (TypeMirror superInterface : repositoryInterface.getInterfaces()) {
            if (superInterface instanceof DeclaredType) {
                DeclaredType declaredType = (DeclaredType) superInterface;
                TypeElement superElement = (TypeElement) declaredType.asElement();
                
                if (superElement.getQualifiedName().toString().contains("LocalizedRepository") ||
                    superElement.getQualifiedName().toString().contains("JpaRepository")) {
                    
                    List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
                    if (!typeArgs.isEmpty()) {
                        TypeMirror entityTypeMirror = typeArgs.get(0);
                        if (entityTypeMirror instanceof DeclaredType) {
                            return (TypeElement) ((DeclaredType) entityTypeMirror).asElement();
                        }
                    }
                }
            }
        }
        
        return null;
    }

    private boolean isLocalizedField(TypeElement entityType, String fieldName) {
        for (Element member : entityType.getEnclosedElements()) {
            if (member.getKind() == ElementKind.FIELD) {
                VariableElement field = (VariableElement) member;
                if (field.getSimpleName().toString().equals(fieldName)) {
                    return field.getAnnotation(Localized.class) != null;
                }
            }
        }
        
        return false;
    }

    private void generateImplementation(String packageName, String repositoryName,
                                       List<ExecutableElement> methods) throws IOException {
        
        String implClassName = repositoryName + "Impl";
        
        // Group methods by base name
        Map<String, List<ExecutableElement>> methodGroups = methods.stream()
            .collect(Collectors.groupingBy(m -> m.getSimpleName().toString()));
        
        Set<String> methodSignatures = new HashSet<>();
        
        javax.tools.JavaFileObject sourceFile = filer.createSourceFile(
            packageName + "." + implClassName);
        
        try (PrintWriter writer = new PrintWriter(sourceFile.openWriter())) {
            writer.println("package " + packageName + ";");
            writer.println();
            writer.println("import org.springframework.stereotype.Component;");
            writer.println("import org.springframework.beans.factory.annotation.Autowired;");
            writer.println("import jakarta.persistence.EntityManager;");
            writer.println("import jakarta.persistence.criteria.*;");
            writer.println("import org.springframework.context.i18n.LocaleContextHolder;");
            writer.println("import java.util.List;");
            writer.println("import java.util.Locale;");
            writer.println();
            writer.println("/**");
            writer.println(" * Generated implementation for " + repositoryName + ".");
            writer.println(" * DO NOT EDIT - This file is auto-generated by LocalizedRepositoryProcessor.");
            writer.println(" * ");
            writer.println(" * <p>Implements query methods for @Localized fields using JPA Criteria API.");
            writer.println(" */");
            writer.println("@Component");
            writer.println("public class " + implClassName + " {");
            writer.println();
            writer.println("    @Autowired");
            writer.println("    private EntityManager entityManager;");
            writer.println();
            
            // Process each method group
            for (Map.Entry<String, List<ExecutableElement>> entry : methodGroups.entrySet()) {
                String methodName = entry.getKey();
                List<ExecutableElement> methodOverloads = entry.getValue();
                
                ExecutableElement localeVersion = methodOverloads.stream()
                    .filter(m -> hasLocaleParameter(m))
                    .findFirst()
                    .orElse(null);
                
                ExecutableElement nonLocaleVersion = methodOverloads.stream()
                    .filter(m -> !hasLocaleParameter(m))
                    .findFirst()
                    .orElse(null);
                
                if (localeVersion != null) {
                    generateFullMethodImpl(writer, localeVersion, methodSignatures);
                }
                
                if (nonLocaleVersion != null && localeVersion != null) {
                    generateDelegatingMethod(writer, nonLocaleVersion, methodSignatures);
                }
                
                if (nonLocaleVersion != null && localeVersion == null) {
                    generateDelegatingMethod(writer, nonLocaleVersion, methodSignatures);
                    generateImpliedLocaleMethod(writer, nonLocaleVersion, methodSignatures);
                }
            }
            
            writer.println("}");
        }
        
        messager.printMessage(Diagnostic.Kind.NOTE,
            "Successfully generated: " + implClassName);
    }

    private boolean hasLocaleParameter(ExecutableElement method) {
        List<? extends VariableElement> params = method.getParameters();
        return params.size() > 1 && 
               params.get(params.size() - 1).asType().toString().contains("Locale");
    }

    private void generateFullMethodImpl(PrintWriter writer, ExecutableElement method,
                                       Set<String> methodSignatures) {
        String methodName = method.getSimpleName().toString();
        String fieldName = extractFieldName(methodName);
        List<? extends VariableElement> params = method.getParameters();
        
        String signature = getMethodSignature(method);
        if (methodSignatures.contains(signature)) {
            return;
        }
        methodSignatures.add(signature);
        
        writer.print("    public List");
        
        String returnType = method.getReturnType().toString();
        int genericStart = returnType.indexOf('<');
        if (genericStart > 0) {
            writer.print(returnType.substring(genericStart));
        }
        
        writer.print(" " + methodName + "(");
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) writer.print(", ");
            writer.print(params.get(i).asType() + " " + params.get(i).getSimpleName());
        }
        writer.println(") {");
        
        String localeParam = params.get(params.size() - 1).getSimpleName().toString();
        String valueParam = params.get(0).getSimpleName().toString();
        
        String entityClass = returnType.substring(returnType.indexOf('<') + 1, returnType.indexOf('>'));
        
        writer.println("        CriteriaBuilder cb = entityManager.getCriteriaBuilder();");
        writer.println("        CriteriaQuery<" + entityClass + "> query = cb.createQuery(" + entityClass + ".class);");
        writer.println("        Root<" + entityClass + "> root = query.from(" + entityClass + ".class);");
        writer.println("        Join translations = root.join(\"translations\", JoinType.INNER);");
        writer.println();
        writer.println("        Predicate localePredicate = cb.equal(translations.get(\"locale\"), " + localeParam + ".getLanguage());");
        
        // Use LIKE or EQUAL depending on method name
        boolean isContaining = methodName.contains("Containing");
        if (isContaining) {
            writer.println("        Predicate fieldPredicate = cb.like(translations.get(\"" + fieldName + "\"), \"%\" + " + valueParam + " + \"%\");");
        } else {
            writer.println("        Predicate fieldPredicate = cb.equal(translations.get(\"" + fieldName + "\"), " + valueParam + ");");
        }
        writer.println();
        writer.println("        query.where(cb.and(localePredicate, fieldPredicate));");
        writer.println();
        writer.println("        return entityManager.createQuery(query).getResultList();");
        writer.println("    }");
        writer.println();
    }

    private void generateDelegatingMethod(PrintWriter writer, ExecutableElement method,
                                         Set<String> methodSignatures) {
        String methodName = method.getSimpleName().toString();
        List<? extends VariableElement> params = method.getParameters();
        
        String signature = getMethodSignature(method);
        if (methodSignatures.contains(signature)) {
            return;
        }
        methodSignatures.add(signature);
        
        writer.print("    public List");
        
        String returnType = method.getReturnType().toString();
        int genericStart = returnType.indexOf('<');
        if (genericStart > 0) {
            writer.print(returnType.substring(genericStart));
        }
        
        writer.print(" " + methodName + "(");
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) writer.print(", ");
            writer.print(params.get(i).asType() + " " + params.get(i).getSimpleName());
        }
        writer.println(") {");
        writer.println("        return " + methodName + "(" + params.get(0).getSimpleName() + ", LocaleContextHolder.getLocale());");
        writer.println("    }");
        writer.println();
    }

    private void generateImpliedLocaleMethod(PrintWriter writer, ExecutableElement method,
                                            Set<String> methodSignatures) {
        String methodName = method.getSimpleName().toString();
        String fieldName = extractFieldName(methodName);
        List<? extends VariableElement> params = method.getParameters();
        
        String localeSignature = methodName + "(" + params.get(0).asType() + ",Locale)";
        if (methodSignatures.contains(localeSignature)) {
            return;
        }
        methodSignatures.add(localeSignature);
        
        String returnType = method.getReturnType().toString();
        int genericStart = returnType.indexOf('<');
        String entityClass = returnType.substring(returnType.indexOf('<') + 1, returnType.indexOf('>'));
        String valueParam = params.get(0).getSimpleName().toString();
        
        writer.print("    public List");
        if (genericStart > 0) {
            writer.print(returnType.substring(genericStart));
        }
        
        writer.println(" " + methodName + "(" + params.get(0).asType() + " " + valueParam + ", Locale locale) {");
        
        writer.println("        CriteriaBuilder cb = entityManager.getCriteriaBuilder();");
        writer.println("        CriteriaQuery<" + entityClass + "> query = cb.createQuery(" + entityClass + ".class);");
        writer.println("        Root<" + entityClass + "> root = query.from(" + entityClass + ".class);");
        writer.println("        Join translations = root.join(\"translations\", JoinType.INNER);");
        writer.println();
        writer.println("        Predicate localePredicate = cb.equal(translations.get(\"locale\"), locale.getLanguage());");
        
        // Use LIKE or EQUAL depending on method name
        boolean isContaining = methodName.contains("Containing");
        if (isContaining) {
            writer.println("        Predicate fieldPredicate = cb.like(translations.get(\"" + fieldName + "\"), \"%\" + " + valueParam + " + \"%\");");
        } else {
            writer.println("        Predicate fieldPredicate = cb.equal(translations.get(\"" + fieldName + "\"), " + valueParam + ");");
        }
        writer.println();
        writer.println("        query.where(cb.and(localePredicate, fieldPredicate));");
        writer.println();
        writer.println("        return entityManager.createQuery(query).getResultList();");
        writer.println("    }");
        writer.println();
    }

    private String getMethodSignature(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        String params = method.getParameters().stream()
            .map(p -> p.asType().toString())
            .collect(Collectors.joining(","));
        return methodName + "(" + params + ")";
    }
}

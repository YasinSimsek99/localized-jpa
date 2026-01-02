package com.localizedjpa.compiler;

import com.google.auto.service.AutoService;
import com.localizedjpa.annotations.Localized;
import com.localizedjpa.annotations.LocalizedEntity;
import com.localizedjpa.compiler.InterfaceGenerator.LocalizedFieldInfo;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

/**
 * Annotation processor for {@link Localized} annotations.
 * 
 * <p>This processor detects {@code @Localized} fields in JPA entities and automatically generates:
 * <ul>
 *   <li>Translation entity classes for storing localized values</li>
 *   <li>Interface definitions for type-safe method signatures</li>
 *   <li>Getter/setter methods injected via AST manipulation</li>
 * </ul>
 * 
 * <p>The processor uses a two-round approach for reliable compilation:
 * <ul>
 *   <li>Round 1: Generate Translation entities (new source files)</li>
 *   <li>Round 2: Inject methods into original classes via AST manipulation</li>
 * </ul>
 * 
 * <p>This ensures Translation classes are compiled before methods referencing them are injected.
 * 
 * <p><b>Note:</b> {@link LocalizedEntity} is deprecated and no longer required. Simply annotate
 * fields with {@code @Localized} in any {@code @Entity} class.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({
    "com.localizedjpa.annotations.LocalizedEntity",
    "com.localizedjpa.annotations.Localized"
})
public class LocalizedProcessor extends AbstractProcessor {

    private Messager messager;
    private Filer filer;
    private Elements elementUtils;
    private Types typeUtils;

    // Track processing state across rounds
    private final Set<String> generatedTranslations = new HashSet<>();
    private final Map<String, PendingInjection> pendingInjections = new HashMap<>();
    private int roundNumber = 0;

    /**
     * Holds information needed for deferred AST injection.
     */
    private record PendingInjection(
        String packageName,
        String className,
        List<LocalizedFieldInfo> fields
    ) {}

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        
        // Open jdk.compiler packages early to allow AST manipulation
        // This is the zero-config equivalent of --add-opens flags
        Permit.openJdkCompilerPackages();
        
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // Return the latest version to support both Java 17 and 21
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundNumber++;
        
        messager.printMessage(Diagnostic.Kind.NOTE, 
            "[LocalizedJPA] Processing round " + roundNumber + 
            ", processingOver=" + roundEnv.processingOver());

        if (roundEnv.processingOver()) {
            // Final round - perform any pending AST injections
            performPendingInjections();
            return false;
        }

        // NEW APPROACH: Process @Localized fields directly, grouped by entity class
        Map<TypeElement, List<LocalizedFieldInfo>> entitiesByClass = groupLocalizedFieldsByEntity(roundEnv);
        
        // LEGACY SUPPORT: Also process @LocalizedEntity for backward compatibility
        for (Element element : roundEnv.getElementsAnnotatedWith(LocalizedEntity.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, 
                    "@LocalizedEntity can only be applied to classes", element);
                continue;
            }

            TypeElement classElement = (TypeElement) element;
            
            // Only process if not already processed via @Localized fields
            if (!entitiesByClass.containsKey(classElement)) {
                List<LocalizedFieldInfo> fields = findLocalizedFields(classElement);
                if (!fields.isEmpty()) {
                    entitiesByClass.put(classElement, fields);
                }
            }
        }
        
        // Process all entities with localized fields
        for (var entry : entitiesByClass.entrySet()) {
            TypeElement classElement = entry.getKey();
            List<LocalizedFieldInfo> localizedFields = entry.getValue();
            processLocalizedEntity(classElement, localizedFields);
        }
        
        // Don't claim annotations - allow other processors to run
        return false;
    }

    /**
     * Groups @Localized fields by their parent entity class.
     * Validates that parent class is a JPA entity.
     */
    private Map<TypeElement, List<LocalizedFieldInfo>> groupLocalizedFieldsByEntity(RoundEnvironment roundEnv) {
        Map<TypeElement, List<LocalizedFieldInfo>> entitiesByClass = new HashMap<>();
        
        for (Element element : roundEnv.getElementsAnnotatedWith(Localized.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                messager.printMessage(Diagnostic.Kind.ERROR, 
                    "@Localized can only be applied to fields", element);
                continue;
            }
            
            VariableElement field = (VariableElement) element;
            TypeElement classElement = (TypeElement) field.getEnclosingElement();
            
            // Validate that parent class is a JPA entity
            if (!isJpaEntity(classElement)) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                    "@Localized field '" + field.getSimpleName() + 
                    "' must be in a class annotated with @Entity", field);
                continue;
            }
            
            // Extract field information
            LocalizedFieldInfo fieldInfo = extractFieldInfo(field);
            
            // Group by parent class
            entitiesByClass.computeIfAbsent(classElement, k -> new ArrayList<>())
                .add(fieldInfo);
            
            messager.printMessage(Diagnostic.Kind.NOTE, 
                "[LocalizedJPA] Found @Localized field: " + field.getSimpleName() + 
                " in entity " + classElement.getSimpleName());
        }
        
        return entitiesByClass;
    }

    /**
     * Checks if a class is annotated with JPA @Entity.
     */
    private boolean isJpaEntity(TypeElement classElement) {
        for (AnnotationMirror annotation : classElement.getAnnotationMirrors()) {
            String annotationName = annotation.getAnnotationType().toString();
            if (annotationName.equals("jakarta.persistence.Entity") || 
                annotationName.equals("javax.persistence.Entity")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts field information from a @Localized annotated field.
     */
    private LocalizedFieldInfo extractFieldInfo(VariableElement field) {
        Localized localizedAnnotation = field.getAnnotation(Localized.class);
        String fieldName = field.getSimpleName().toString();
        TypeMirror typeMirror = field.asType();
        TypeName typeName = TypeName.get(typeMirror);
        boolean fallback = localizedAnnotation.fallback();
        return new LocalizedFieldInfo(fieldName, typeName, fallback);
    }

    private void processLocalizedEntity(TypeElement classElement, List<LocalizedFieldInfo> localizedFields) {
        String className = classElement.getSimpleName().toString();
        String packageName = elementUtils.getPackageOf(classElement).getQualifiedName().toString();
        String qualifiedName = packageName + "." + className;

        messager.printMessage(Diagnostic.Kind.NOTE, 
            "[LocalizedJPA] Processing entity with localized fields: " + className);

        if (localizedFields.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.WARNING, 
                "Entity " + className + " has no @Localized fields", classElement);
            return;
        }

        // ROUND 1: Generate Translation entity and interface (source files)
        String translationKey = qualifiedName + "Translation";
        if (!generatedTranslations.contains(translationKey)) {
            generateSourceFiles(classElement, packageName, className, localizedFields);
            generatedTranslations.add(translationKey);
            
            // Queue AST injection for next round
            pendingInjections.put(qualifiedName, 
                new PendingInjection(packageName, className, localizedFields));
            
            messager.printMessage(Diagnostic.Kind.NOTE, 
                "[LocalizedJPA] Queued AST injection for " + className + " (will run after Translation is compiled)");
        }

        // ROUND 2+: Perform AST injection if Translation class is now available
        TypeElement translationElement = elementUtils.getTypeElement(translationKey);
        if (translationElement != null && pendingInjections.containsKey(qualifiedName)) {
            messager.printMessage(Diagnostic.Kind.NOTE, 
                "[LocalizedJPA] Translation class available, performing AST injection for " + className);
            performAstInjection(classElement, packageName, className, localizedFields);
            pendingInjections.remove(qualifiedName);
        }
    }

    private List<LocalizedFieldInfo> findLocalizedFields(TypeElement classElement) {
        List<LocalizedFieldInfo> fields = new ArrayList<>();
        
        for (Element enclosed : classElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD) {
                VariableElement field = (VariableElement) enclosed;
                Localized localizedAnnotation = field.getAnnotation(Localized.class);
                
                if (localizedAnnotation != null) {
                    String fieldName = field.getSimpleName().toString();
                    TypeMirror typeMirror = field.asType();
                    TypeName typeName = TypeName.get(typeMirror);
                    boolean fallback = localizedAnnotation.fallback();

                    fields.add(new LocalizedFieldInfo(fieldName, typeName, fallback));
                    
                    messager.printMessage(Diagnostic.Kind.NOTE, 
                        "[LocalizedJPA]   Found @Localized field: " + fieldName);
                }
            }
        }
        
        return fields;
    }

    private void generateSourceFiles(TypeElement classElement, String packageName, 
                                     String className, List<LocalizedFieldInfo> localizedFields) {
        // Generate interface
        try {
            InterfaceGenerator generator = new InterfaceGenerator(filer);
            generator.generateInterface(packageName, className, localizedFields);
            
            messager.printMessage(Diagnostic.Kind.NOTE, 
                "[LocalizedJPA] Generated interface: " + packageName + "." + className + "Localized");
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, 
                "Failed to generate interface: " + e.getMessage(), classElement);
        }

        // Generate translation entity
        try {
            TranslationEntityGenerator translationGenerator = new TranslationEntityGenerator(filer);
            String tableName = getTableName(classElement, className);
            translationGenerator.generateTranslationEntity(packageName, className, tableName, localizedFields);
            
            messager.printMessage(Diagnostic.Kind.NOTE, 
                "[LocalizedJPA] Generated translation entity: " + packageName + "." + className + "Translation");
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, 
                "Failed to generate translation entity: " + e.getMessage(), classElement);
        }
    }

    private void performAstInjection(TypeElement classElement, String packageName,
                                     String className, List<LocalizedFieldInfo> localizedFields) {
        JavacAstModifier astModifier = JavacAstModifier.createIfSupported(processingEnv);
        
        if (astModifier == null) {
            messager.printMessage(Diagnostic.Kind.NOTE, 
                "[LocalizedJPA] AST injection skipped for " + className + " (non-javac compiler)");
            return;
        }

        try {
            String translationClassName = packageName + "." + className + "Translation";
            
            // Ensure @Transient is imported
            astModifier.ensureTransientImport(classElement);
            
            // Mark localized fields as @Transient
            for (LocalizedFieldInfo field : localizedFields) {
                astModifier.markFieldAsTransient(classElement, field.name());
            }
            
            // Inject translations map field
            astModifier.injectTranslationsField(classElement, translationClassName);
            
            // Inject getter/setter methods for each @Localized field
            for (LocalizedFieldInfo field : localizedFields) {
                astModifier.injectLocalizedGetter(classElement, field.name(), translationClassName);
                astModifier.injectLocalizedSetter(classElement, field.name(), translationClassName);
            }
            
            // Inject getTranslations() and setTranslations()
            astModifier.injectTranslationsAccessors(classElement);
        
            messager.printMessage(Diagnostic.Kind.NOTE, 
                "[LocalizedJPA] Successfully injected methods into " + className);
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.WARNING, 
                "[LocalizedJPA] AST injection failed for " + className + ": " + e.getMessage());
        }
    }

    /**
     * Performs any pending AST injections at the end of processing.
     * This handles cases where Translation class wasn't found in subsequent rounds.
     */
    private void performPendingInjections() {
        for (var entry : pendingInjections.entrySet()) {
            String qualifiedName = entry.getKey();
            PendingInjection pending = entry.getValue();
            
            TypeElement classElement = elementUtils.getTypeElement(qualifiedName);
            if (classElement != null) {
                messager.printMessage(Diagnostic.Kind.NOTE, 
                    "[LocalizedJPA] Performing deferred AST injection for " + pending.className());
                performAstInjection(classElement, pending.packageName(), 
                    pending.className(), pending.fields());
            }
        }
        pendingInjections.clear();
    }

    private String getTableName(TypeElement classElement, String className) {
        for (AnnotationMirror annotation : classElement.getAnnotationMirrors()) {
            String annotationName = annotation.getAnnotationType().toString();
            if (annotationName.equals("jakarta.persistence.Table") || 
                annotationName.equals("javax.persistence.Table")) {
                for (var entry : annotation.getElementValues().entrySet()) {
                    if (entry.getKey().getSimpleName().toString().equals("name")) {
                        String value = entry.getValue().getValue().toString();
                        if (!value.isEmpty()) {
                            return value;
                        }
                    }
                }
            }
        }
        return StringUtils.toSnakeCase(className);
    }
}

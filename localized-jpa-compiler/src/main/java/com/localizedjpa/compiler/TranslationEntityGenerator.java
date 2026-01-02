package com.localizedjpa.compiler;

import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.List;

/**
 * Generates translation entity source files using JavaPoet.
 * 
 * <p>For each entity annotated with @LocalizedEntity, generates a translation
 * entity that extends BaseTranslation and contains all @Localized fields.
 * 
 * <p>Example generated entity:
 * <pre>
 * {@literal @}Entity
 * {@literal @}Table(name = "product_translations")
 * public class ProductTranslation extends BaseTranslation {
 *     {@literal @}Column(name = "name")
 *     private String name;
 *     
 *     // getters and setters
 * }
 * </pre>
 */
public class TranslationEntityGenerator {

    private static final ClassName BASE_TRANSLATION = 
        ClassName.get("com.localizedjpa.runtime", "BaseTranslation");
    private static final ClassName ENTITY_ANNOTATION = 
        ClassName.get("jakarta.persistence", "Entity");
    private static final ClassName TABLE_ANNOTATION = 
        ClassName.get("jakarta.persistence", "Table");
    private static final ClassName COLUMN_ANNOTATION = 
        ClassName.get("jakarta.persistence", "Column");
    private static final ClassName BATCH_SIZE_ANNOTATION = 
        ClassName.get("org.hibernate.annotations", "BatchSize");

    private final Filer filer;

    public TranslationEntityGenerator(Filer filer) {
        this.filer = filer;
    }

    /**
     * Generates a translation entity for the given entity.
     *
     * @param packageName Package name of the entity
     * @param entityName Simple name of the entity class
     * @param tableName Name of the main entity table (for generating translation table name)
     * @param localizedFields List of localized field info
     * @throws IOException If file cannot be written
     */
    public void generateTranslationEntity(String packageName, String entityName, 
                                           String tableName,
                                           List<InterfaceGenerator.LocalizedFieldInfo> localizedFields) 
                                           throws IOException {
        
        String translationClassName = entityName + "Translation";
        String translationTableName = tableName + "_translations";

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(translationClassName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(BASE_TRANSLATION)
                .addAnnotation(ENTITY_ANNOTATION)
                .addAnnotation(AnnotationSpec.builder(TABLE_ANNOTATION)
                        .addMember("name", "$S", translationTableName)
                        .build())
                .addJavadoc("Generated translation entity for {@code $L}.\n", entityName)
                .addJavadoc("\n<p>This entity stores localized field values for each locale.\n")
                .addJavadoc("Table: {@code $L}\n", translationTableName)
                .addAnnotation(AnnotationSpec.builder(BATCH_SIZE_ANNOTATION)
                        .addMember("size", "$L", 25)
                        .build());

        // Add parent entity reference field with @JsonIgnore to prevent circular serialization
        ClassName parentEntityClass = ClassName.get(packageName, entityName);
        ClassName jsonIgnoreAnnotation = ClassName.get("com.fasterxml.jackson.annotation", "JsonIgnore");
        
        classBuilder.addField(FieldSpec.builder(parentEntityClass, "parent", Modifier.PRIVATE)
                .addAnnotation(jsonIgnoreAnnotation)  // Prevent circular JSON serialization
                .addAnnotation(AnnotationSpec.builder(
                        ClassName.get("jakarta.persistence", "ManyToOne"))
                        .addMember("fetch", "$T.LAZY", 
                                ClassName.get("jakarta.persistence", "FetchType"))
                        .build())
                .addAnnotation(AnnotationSpec.builder(
                        ClassName.get("jakarta.persistence", "JoinColumn"))
                        .addMember("name", "$S", toSnakeCase(entityName) + "_id")
                        .addMember("nullable", "$L", false)
                        .build())
                .build());

        // Add getter/setter for parent
        classBuilder.addMethod(MethodSpec.methodBuilder("getParent")
                .addModifiers(Modifier.PUBLIC)
                .returns(parentEntityClass)
                .addStatement("return parent")
                .build());
        
        classBuilder.addMethod(MethodSpec.methodBuilder("setParent")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parentEntityClass, "parent")
                .addStatement("this.parent = parent")
                .build());

        // Add fields and getters/setters for each localized field
        for (InterfaceGenerator.LocalizedFieldInfo field : localizedFields) {
            addLocalizedField(classBuilder, field);
        }

        TypeSpec translationClass = classBuilder.build();
        
        JavaFile javaFile = JavaFile.builder(packageName, translationClass)
                .addFileComment("Generated by Localized JPA Compiler - do not modify")
                .indent("    ")
                .build();

        javaFile.writeTo(filer);
    }

    private void addLocalizedField(TypeSpec.Builder classBuilder, 
                                   InterfaceGenerator.LocalizedFieldInfo field) {
        String fieldName = field.name();
        TypeName fieldType = field.typeName();
        String capitalizedName = capitalize(fieldName);
        String columnName = toSnakeCase(fieldName);

        // Field with @Column annotation
        classBuilder.addField(FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE)
                .addAnnotation(AnnotationSpec.builder(COLUMN_ANNOTATION)
                        .addMember("name", "$S", columnName)
                        .build())
                .build());

        // Getter
        classBuilder.addMethod(MethodSpec.methodBuilder("get" + capitalizedName)
                .addModifiers(Modifier.PUBLIC)
                .returns(fieldType)
                .addStatement("return $N", fieldName)
                .build());

        // Setter
        classBuilder.addMethod(MethodSpec.methodBuilder("set" + capitalizedName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(fieldType, fieldName)
                .addStatement("this.$N = $N", fieldName, fieldName)
                .build());
    }

    private String capitalize(String str) {
        return StringUtils.capitalize(str);
    }

    private String toSnakeCase(String str) {
        return StringUtils.toSnakeCase(str);
    }
}

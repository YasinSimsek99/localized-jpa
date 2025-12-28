package com.localizedjpa.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as localized (multi-language support).
 *
 * <p>When applied to a field in a JPA entity (annotated with {@code @Entity}),
 * the compiler will automatically generate:
 * <ul>
 *   <li>A translation entity class (e.g., {@code ProductTranslation})</li>
 *   <li>A {@code translations} map field in the parent entity</li>
 *   <li>Getter/Setter for current locale: {@code getName()}, {@code setName(String)}</li>
 *   <li>Getter/Setter with explicit locale: {@code getName(Locale)}, {@code setName(String, Locale)}</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * {@literal @}Entity
 * public class Product {
 *     {@literal @}Localized(fallback = true)
 *     private String name;
 *     
 *     {@literal @}Localized(fallback = false)
 *     private String description;
 * }
 * </pre>
 * 
 * <p><b>Note:</b> The parent class must be annotated with {@code @Entity}.
 * The {@code @LocalizedEntity} annotation is no longer required.
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Localized {

    /**
     * If true, when the requested locale is not found,
     * the fallback language (configured in application.yml) will be used.
     *
     * @return true to enable fallback, false to return null if locale not found
     */
    boolean fallback() default true;
}

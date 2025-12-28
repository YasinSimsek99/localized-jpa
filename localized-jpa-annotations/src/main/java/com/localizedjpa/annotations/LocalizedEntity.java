package com.localizedjpa.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an entity class for localization support.
 *
 * <p><b>DEPRECATED:</b> As of version 0.2.0, this annotation is no longer required.
 * Simply use {@link Localized} on fields directly in any {@code @Entity} class.
 * The processor will automatically detect and process localized fields.
 * 
 * <p>This annotation is kept for backward compatibility and will be removed in version 1.0.0.
 *
 * <p><b>Migration:</b>
 * <pre>
 * // Old approach (still works but deprecated):
 * {@literal @}Entity
 * {@literal @}LocalizedEntity
 * public class Product {
 *     {@literal @}Localized
 *     private String name;
 * }
 *
 * // New approach (recommended):
 * {@literal @}Entity
 * public class Product {
 *     {@literal @}Localized
 *     private String name;
 * }
 * </pre>
 */
@Deprecated(since = "0.2.0", forRemoval = true)
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalizedEntity {

    /**
     * Custom name for the translations table.
     * If empty, defaults to {@code {entity_table}_translations}.
     *
     * @return custom translation table name or empty for default
     */
    String translationTable() default "";
}

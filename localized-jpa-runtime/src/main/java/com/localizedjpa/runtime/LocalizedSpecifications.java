package com.localizedjpa.runtime;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

/**
 * Utility class for creating JPA Specifications for localized entity queries.
 * 
 * <p>These specifications enable locale-aware querying by joining the translations
 * table and filtering by locale.
 * 
 * <p>Example usage:
 * <pre>
 * Specification&lt;Product&gt; spec = LocalizedSpecifications.byLocalizedField(
 *     "name", "Table", Locale.ENGLISH, ProductTranslation.class);
 * List&lt;Product&gt; products = productRepository.findAll(spec);
 * </pre>
 */
public final class LocalizedSpecifications {

    private LocalizedSpecifications() {
        // Utility class - no instantiation
    }

    /**
     * Creates a specification for exact match on a localized field.
     *
     * @param fieldName The name of the localized field
     * @param value The value to match
     * @param locale The locale to search in
     * @param translationClass The translation entity class
     * @param <T> The entity type
     * @return A specification for the query
     */
    public static <T> Specification<T> byLocalizedField(
            String fieldName, 
            String value, 
            Locale locale, 
            Class<?> translationClass) {
        
        return (root, query, cb) -> {
            Join<Object, Object> translations = root.join("translations", JoinType.LEFT);
            
            Predicate localePredicate = cb.equal(
                translations.get("locale"), 
                locale.getLanguage()
            );
            
            Predicate fieldPredicate = cb.equal(
                translations.get(fieldName), 
                value
            );
            
            return cb.and(localePredicate, fieldPredicate);
        };
    }

    /**
     * Creates a specification for partial (LIKE) match on a localized field.
     *
     * @param fieldName The name of the localized field
     * @param value The value to search for (will be wrapped in %)
     * @param locale The locale to search in
     * @param translationClass The translation entity class
     * @param <T> The entity type
     * @return A specification for the query
     */
    public static <T> Specification<T> byLocalizedFieldContaining(
            String fieldName, 
            String value, 
            Locale locale, 
            Class<?> translationClass) {
        
        return (root, query, cb) -> {
            Join<Object, Object> translations = root.join("translations", JoinType.LEFT);
            
            Predicate localePredicate = cb.equal(
                translations.get("locale"), 
                locale.getLanguage()
            );
            
            Predicate fieldPredicate = cb.like(
                cb.lower(translations.get(fieldName)), 
                "%" + value.toLowerCase() + "%"
            );
            
            return cb.and(localePredicate, fieldPredicate);
        };
    }

    /**
     * Creates a specification that eagerly fetches translations to avoid N+1 queries.
     *
     * @param <T> The entity type
     * @return A specification that fetches translations
     */
    public static <T> Specification<T> withTranslations() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("translations", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    /**
     * Creates a specification for querying by locale only (finds entities with translations in given locale).
     *
     * @param locale The locale to filter by
     * @param <T> The entity type
     * @return A specification for the query
     */
    public static <T> Specification<T> hasTranslationFor(Locale locale) {
        return (root, query, cb) -> {
            Join<Object, Object> translations = root.join("translations", JoinType.LEFT);
            return cb.equal(translations.get("locale"), locale.getLanguage());
        };
    }
}

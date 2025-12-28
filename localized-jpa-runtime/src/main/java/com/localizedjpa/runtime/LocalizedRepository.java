package com.localizedjpa.runtime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository interface for entities with localized fields.
 * 
 * <p>Extends both {@link JpaRepository} and {@link JpaSpecificationExecutor}
 * to provide standard CRUD operations plus specification-based querying.
 * 
 * <p>Example usage:
 * <pre>
 * public interface ProductRepository extends LocalizedRepository&lt;Product, Long&gt; {
 *     // Custom query methods can be added here
 * }
 * </pre>
 * 
 * <p>For locale-aware queries, use {@link LocalizedSpecifications}:
 * <pre>
 * Specification&lt;Product&gt; spec = LocalizedSpecifications.byLocalizedField(
 *     "name", "Table", Locale.ENGLISH, ProductTranslation.class);
 * List&lt;Product&gt; products = productRepository.findAll(spec);
 * </pre>
 *
 * @param <T> The entity type
 * @param <ID> The ID type
 */
@NoRepositoryBean
public interface LocalizedRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    
    // All query capabilities come from JpaSpecificationExecutor
    // Use LocalizedSpecifications to create locale-aware queries
}

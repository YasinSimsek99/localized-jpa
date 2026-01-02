package com.localizedjpa.demo.repository;

import com.localizedjpa.demo.entity.Product;
import com.localizedjpa.runtime.LocalizedRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Locale;

/**
 * Repository for Product entities with locale-aware query support.
 *
 * <p><strong>TRUE ZERO-BOILERPLATE:</strong> Just declare query methods here!
 * The annotation processor automatically generates implementations.
 *
 * <p>Supported query keywords:
 * <ul>
 *   <li><code>findBy{Field}</code> - Exact match</li>
 *   <li><code>findBy{Field}Containing</code> - LIKE query (%value%)</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * // Exact match with current locale
 * List&lt;Product&gt; products = repository.findByName("Table");
 *
 * // LIKE search - finds "Wooden Table", "Glass Table", etc.
 * List&lt;Product&gt; tables = repository.findByNameContaining("Table");
 *
 * // Pagination support
 * Page&lt;Product&gt; page = repository.findByNameContaining("Table", PageRequest.of(0, 10));
 * </pre>
 */
public interface ProductRepository extends LocalizedRepository<Product, Long> {

    // ========== EXACT MATCH QUERIES ==========

    /**
     * Find products by exact localized name match.
     */
    List<Product> findByName(String name);

    /**
     * Find products by exact localized name and locale param.
     */
    List<Product> findByName(String name, Locale locale);

    /**
     * Find products by exact localized description match.
     */
    List<Product> findByDescription(String description);

    // ========== LIKE QUERIES (Containing) ==========

    /**
     * Find products where localized name contains the search term.
     * Uses SQL LIKE '%searchTerm%' query.
     */
    List<Product> findByNameContaining(String searchTerm);

    /**
     * Find products where localized description contains the search term.
     */
    List<Product> findByDescriptionContaining(String searchTerm);

    // ========== PAGINATION SUPPORT ==========

    /**
     * Find products where localized name contains search term with pagination.
     */
    Page<Product> findByNameContaining(String searchTerm, Pageable pageable);

    /**
     * Find products where localized description contains search term with pagination.
     */
    Page<Product> findByDescriptionContaining(String searchTerm, Pageable pageable);

    List<Product> findByNameContaining(String keyword, Locale locale);

}

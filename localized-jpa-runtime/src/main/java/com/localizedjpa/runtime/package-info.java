/**
 * Core runtime components for LocalizedJPA.
 * 
 * <p>This package contains the runtime utilities and base classes needed
 * for localized JPA entity support:
 * <ul>
 *   <li>{@link com.localizedjpa.runtime.LocalizedSpecifications LocalizedSpecifications} - 
 *       Utility for creating locale-aware JPA Specifications</li>
 *   <li>{@link com.localizedjpa.runtime.BaseTranslation BaseTranslation} - 
 *       Abstract base class for translation entities</li>
 *   <li>{@link com.localizedjpa.runtime.LocalizedRepository LocalizedRepository} - 
 *       Repository interface with locale support</li>
 * </ul>
 * 
 * <p>Example: Querying localized entities:
 * <pre>{@code
 * // Find products by English name
 * Specification<Product> spec = LocalizedSpecifications.byLocalizedField(
 *     "name", "Table", Locale.ENGLISH, ProductTranslation.class);
 * List<Product> products = repository.findAll(spec);
 * 
 * // Search with partial match
 * Specification<Product> search = LocalizedSpecifications.byLocalizedFieldContaining(
 *     "description", "modern", Locale.GERMAN, ProductTranslation.class);
 * }</pre>
 * 
 * @see com.localizedjpa.annotations.LocalizedEntity
 * @see com.localizedjpa.annotations.Localized
 * @since 0.1.0
 */
package com.localizedjpa.runtime;

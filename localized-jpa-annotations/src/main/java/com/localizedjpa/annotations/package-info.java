/**
 * Annotation definitions for LocalizedJPA.
 * 
 * <p>This package contains the core annotations used to mark entities
 * and fields for localization support:
 * <ul>
 *   <li>{@link com.localizedjpa.annotations.LocalizedEntity LocalizedEntity} - Marks an entity class for localization</li>
 *   <li>{@link com.localizedjpa.annotations.Localized Localized} - Marks a field as localized (multi-language support)</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>{@code
 * @Entity
 * @LocalizedEntity
 * public class Product {
 *     @Id
 *     private Long id;
 *     
 *     @Localized
 *     private String name;
 * }
 * }</pre>
 * 
 * @since 0.1.0
 */
package com.localizedjpa.annotations;

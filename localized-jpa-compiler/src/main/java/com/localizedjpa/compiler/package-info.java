/**
 * Annotation processor and code generation for LocalizedJPA.
 * 
 * <p>This package contains the compile-time annotation processors that generate
 * localized interface and translation entity code:
 * <ul>
 *   <li>{@link com.localizedjpa.compiler.LocalizedProcessor LocalizedProcessor} - 
 *       Main annotation processor for {@code @LocalizedEntity}</li>
 *   <li>{@link com.localizedjpa.compiler.InterfaceGenerator InterfaceGenerator} - 
 *       Generates localized method interfaces</li>
 *   <li>{@link com.localizedjpa.compiler.TranslationEntityGenerator TranslationEntityGenerator} - 
 *       Generates JPA translation entities</li>
 * </ul>
 * 
 * <p>The processor automatically generates code at compile-time, no runtime overhead.
 * 
 * @since 0.1.0
 */
package com.localizedjpa.compiler;

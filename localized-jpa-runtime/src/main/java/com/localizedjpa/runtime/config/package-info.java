/**
 * Spring Boot auto-configuration for LocalizedJPA.
 * 
 * <p>This package contains the auto-configuration classes that enable
 * automatic setup of LocalizedJPA in Spring Boot applications:
 * <ul>
 *   <li>{@link com.localizedjpa.runtime.config.LocalizedJpaAutoConfiguration LocalizedJpaAutoConfiguration} - 
 *       Main auto-configuration class</li>
 *   <li>{@link com.localizedjpa.runtime.config.LocalizedJpaProperties LocalizedJpaProperties} - 
 *       Configuration properties</li>
 *   <li>{@link com.localizedjpa.runtime.config.LocaleValidationInterceptor LocaleValidationInterceptor} - 
 *       Request interceptor for locale validation</li>
 * </ul>
 * 
 * <p>Configuration example in {@code application.yml}:
 * <pre>
 * localized-jpa:
 *   supported-locales: en,tr,de
 *   default-locale: en
 *   exception-on-unsupported-languages: true
 * </pre>
 * 
 * @since 0.1.0
 */
package com.localizedjpa.runtime.config;

package com.localizedjpa.runtime.config;

import com.localizedjpa.runtime.exception.LocalizedJpaExceptionHandler;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * Auto-configuration for localized JPA support.
 * 
 * <p>Automatically configures:
 * <ul>
 *   <li>LocaleResolver - reads Accept-Language header</li>
 *   <li>LocaleChangeInterceptor - supports ?lang=tr query parameter</li>
 *   <li>Supported locales - configurable via application.yml</li>
 *   <li>Exception handler for UnsupportedLocaleException</li>
 * </ul>
 * 
 * <p>This makes LocaleContextHolder.getLocale() work automatically
 * for all AST-injected getName(), setName() methods.
 * 
 * <p>Configure in application.yml:
 * <pre>
 * localized-jpa:
 *   supported-locales: en,tr,de
 *   default-locale: tr
 * </pre>
 */
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(LocalizedJpaProperties.class)
public class LocalizedJpaAutoConfiguration implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(LocalizedJpaAutoConfiguration.class);

    private final LocalizedJpaProperties properties;

    public LocalizedJpaAutoConfiguration(LocalizedJpaProperties properties) {
        this.properties = properties;
    }
    
    @PostConstruct
    public void init() {
        String supportedLocales = properties.getSupportedLocales().isEmpty() 
            ? "all locales (validation disabled)" 
            : String.join(", ", properties.getSupportedLocales());
        
        log.info("Localized JPA initialized with default locale '{}' and supported locales: {}", 
            properties.getDefaultLocale(), supportedLocales);
    }

    /**
     * Resolves locale from Accept-Language header.
     * Validation happens in LocaleValidationInterceptor.
     * 
     * Only creates this bean if no other LocaleResolver is defined.
     */
    @Bean
    @ConditionalOnMissingBean(LocaleResolver.class)
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(properties.getDefaultLocaleAsLocale());
        
        // Note: We don't set supported locales here because validation
        // is done in LocaleValidationInterceptor for better control
        return resolver;
    }

    /**
     * Validates requested locale and either throws exception or falls back to default.
     * This is THE key component that makes locale validation work properly.
     */
    @Bean
    public LocaleValidationInterceptor localeValidationInterceptor(LocaleResolver localeResolver) {
        return new LocaleValidationInterceptor(properties, localeResolver);
    }

    /**
     * Allows changing locale via query parameter: ?lang=tr
     * Optional - enabled for convenience.
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    /**
     * Global exception handler for UnsupportedLocaleException.
     * Returns proper JSON error response when unsupported locale is requested.
     */
    @Bean
    public LocalizedJpaExceptionHandler localizedJpaExceptionHandler() {
        return new LocalizedJpaExceptionHandler();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // IMPORTANT: LocaleValidationInterceptor must run AFTER LocaleChangeInterceptor
        // so that ?lang parameter is processed first
        
        // NOTE: Calling @Bean methods here is safe - Spring ensures singleton behavior.
        // This avoids circular dependency that would occur with constructor injection.
        registry.addInterceptor(localeChangeInterceptor()).order(10);
        registry.addInterceptor(localeValidationInterceptor(localeResolver())).order(20);
    }
}

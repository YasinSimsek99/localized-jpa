package com.localizedjpa.runtime.config;

import com.localizedjpa.runtime.exception.UnsupportedLocaleException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * Interceptor that validates requested locale and either throws exception
 * or falls back to default locale based on configuration.
 * 
 * <p>This runs BEFORE the controller, ensuring LocaleContextHolder
 * always contains a valid, supported locale.
 */
public class LocaleValidationInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LocaleValidationInterceptor.class);

    private final LocalizedJpaProperties properties;
    private final LocaleResolver localeResolver;

    public LocaleValidationInterceptor(LocalizedJpaProperties properties, LocaleResolver localeResolver) {
        this.properties = properties;
        this.localeResolver = localeResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Check if Accept-Language header is present
        String acceptLanguageHeader = request.getHeader("Accept-Language");
        
        Locale localeToUse;
        
        if (acceptLanguageHeader == null || acceptLanguageHeader.isBlank()) {
            // No Accept-Language header → use configured default locale
            localeToUse = properties.getDefaultLocaleAsLocale();
            log.debug("No Accept-Language header, using default locale: '{}'", localeToUse.getLanguage());
            LocaleContextHolder.setLocale(localeToUse);
            return true;
        }
        
        // Get requested locale from Accept-Language header
        Locale requestedLocale = localeResolver.resolveLocale(request);
        
        log.debug("Accept-Language header = '{}', resolved locale = '{}'",
            acceptLanguageHeader, requestedLocale.getLanguage());
        
        // If no supported locales configured, allow all
        List<Locale> supportedLocales = properties.getSupportedLocalesAsLocale();
        if (supportedLocales.isEmpty()) {
            LocaleContextHolder.setLocale(requestedLocale);
            return true;
        }
        
        // Check if requested locale is supported
        boolean isSupported = false;
        for (Locale supportedLocale : supportedLocales) {
            if (isSameLocale(requestedLocale, supportedLocale)) {
                isSupported = true;
                break;
            }
        }
        
        if (isSupported) {
            // Set supported locale
            LocaleContextHolder.setLocale(requestedLocale);
            log.debug("Locale '{}' is supported, using it", requestedLocale.getLanguage());
            return true;
        }
        
        // Locale not supported - handle based on configuration
        if (properties.isExceptionOnUnsupportedLanguages()) {
            // Throw exception
            log.debug("Unsupported locale '{}' requested, throwing exception (supported: {})",
                requestedLocale.getLanguage(), supportedLocales);
            throw new UnsupportedLocaleException(
                requestedLocale.getLanguage(),
                properties.getDefaultLocale()
            );
        } else {
            // Silently fall back to default locale
            Locale defaultLocale = properties.getDefaultLocaleAsLocale();
            log.debug("Unsupported locale '{}' requested, falling back to default: '{}'",
                requestedLocale.getLanguage(), defaultLocale.getLanguage());
            LocaleContextHolder.setLocale(defaultLocale);
            return true;
        }
    }

    /**
     * Checks if two locales match (only compares language, ignores country/variant).
     */
    private boolean isSameLocale(Locale locale1, Locale locale2) {
        if (locale1 == null || locale2 == null) {
            return false;
        }
        return locale1.getLanguage().equalsIgnoreCase(locale2.getLanguage());
    }
}

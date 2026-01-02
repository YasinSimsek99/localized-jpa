package com.localizedjpa.runtime.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Configuration properties for Localized JPA.
 * 
 * <p>Configure in application.yml:
 * <pre>
 * localized-jpa:
 *   supported-locales: en,tr,de
 *   default-locale: tr
 * </pre>
 * 
 * <p>Locale conversions are lazily initialized and cached for performance.
 */
@ConfigurationProperties(prefix = "localized-jpa")
public class LocalizedJpaProperties {

    /**
     * List of supported locale codes (e.g., "en", "tr", "de").
     * If empty, all locales are supported.
     */
    private List<String> supportedLocales = new ArrayList<>();

    /**
     * Default locale to use when Accept-Language header is not present.
     * Defaults to "en" if not specified.
     */
    private String defaultLocale = "en";

    /**
     * Whether to throw UnsupportedLocaleException when an unsupported locale is requested.
     * If false, falls back to default locale silently.
     * Defaults to true.
     */
    private boolean exceptionOnUnsupportedLanguages = true;

    // Cached converted values (lazily initialized)
    private volatile List<Locale> cachedSupportedLocales;
    private volatile Locale cachedDefaultLocale;

    public List<String> getSupportedLocales() {
        return supportedLocales;
    }

    public void setSupportedLocales(List<String> supportedLocales) {
        this.supportedLocales = supportedLocales;
        // Invalidate cache when config changes
        this.cachedSupportedLocales = null;
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
        // Invalidate cache when config changes
        this.cachedDefaultLocale = null;
    }

    public boolean isExceptionOnUnsupportedLanguages() {
        return exceptionOnUnsupportedLanguages;
    }

    public void setExceptionOnUnsupportedLanguages(boolean exceptionOnUnsupportedLanguages) {
        this.exceptionOnUnsupportedLanguages = exceptionOnUnsupportedLanguages;
    }

    /**
     * Converts supported locale strings to Locale objects.
     * Result is cached for performance - no new objects created on subsequent calls.
     * 
     * @return Unmodifiable list of supported Locale objects
     */
    public List<Locale> getSupportedLocalesAsLocale() {
        List<Locale> result = cachedSupportedLocales;
        if (result == null) {
            synchronized (this) {
                result = cachedSupportedLocales;
                if (result == null) {
                    List<Locale> locales = new ArrayList<>(supportedLocales.size());
                    for (String localeStr : supportedLocales) {
                        locales.add(Locale.forLanguageTag(localeStr));
                    }
                    // Return unmodifiable to prevent external modification
                    result = Collections.unmodifiableList(locales);
                    cachedSupportedLocales = result;
                }
            }
        }
        return result;
    }

    /**
     * Converts default locale string to Locale object.
     * Result is cached for performance - no new objects created on subsequent calls.
     * 
     * @return Default Locale object
     */
    public Locale getDefaultLocaleAsLocale() {
        Locale result = cachedDefaultLocale;
        if (result == null) {
            synchronized (this) {
                result = cachedDefaultLocale;
                if (result == null) {
                    result = Locale.forLanguageTag(defaultLocale);
                    cachedDefaultLocale = result;
                }
            }
        }
        return result;
    }
}


package com.localizedjpa.runtime.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
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

    public List<String> getSupportedLocales() {
        return supportedLocales;
    }

    public void setSupportedLocales(List<String> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public boolean isExceptionOnUnsupportedLanguages() {
        return exceptionOnUnsupportedLanguages;
    }

    public void setExceptionOnUnsupportedLanguages(boolean exceptionOnUnsupportedLanguages) {
        this.exceptionOnUnsupportedLanguages = exceptionOnUnsupportedLanguages;
    }

    /**
     * Converts supported locale strings to Locale objects.
     */
    public List<Locale> getSupportedLocalesAsLocale() {
        List<Locale> locales = new ArrayList<>();
        for (String localeStr : supportedLocales) {
            locales.add(Locale.forLanguageTag(localeStr));
        }
        return locales;
    }

    /**
     * Converts default locale string to Locale object.
     */
    public Locale getDefaultLocaleAsLocale() {
        return Locale.forLanguageTag(defaultLocale);
    }
}

package com.localizedjpa.runtime.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an unsupported locale is requested
 * and exception-on-unsupported-languages is enabled.
 */
@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class UnsupportedLocaleException extends RuntimeException {

    private final String requestedLocale;
    private final String defaultLocale;

    public UnsupportedLocaleException(String requestedLocale, String defaultLocale) {
        super(String.format("Locale '%s' is not supported. Supported locales: see configuration. Default locale: '%s'", 
            requestedLocale, defaultLocale));
        this.requestedLocale = requestedLocale;
        this.defaultLocale = defaultLocale;
    }

    public String getRequestedLocale() {
        return requestedLocale;
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }
}

package com.localizedjpa.runtime.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link UnsupportedLocaleException}.
 */
class UnsupportedLocaleExceptionTest {

    @Test
    void shouldCreateExceptionWithLocaleStrings() {
        // Given
        String requestedLocale = "ja";
        String defaultLocale = "en";
        
        // When
        UnsupportedLocaleException exception = new UnsupportedLocaleException(requestedLocale, defaultLocale);
        
        // Then
        assertThat(exception.getMessage()).contains("ja", "en");
        assertThat(exception.getRequestedLocale()).isEqualTo(requestedLocale);
        assertThat(exception.getDefaultLocale()).isEqualTo(defaultLocale);
    }

    @Test
    void shouldIncludeLocaleDetailsInMessage() {
        // Given
        String requestedLocale = "zh";
        String defaultLocale = "tr";
        
        // When
        UnsupportedLocaleException exception = new UnsupportedLocaleException(requestedLocale, defaultLocale);
        
        // Then
        assertThat(exception.getMessage())
            .containsIgnoringCase("locale")
            .contains(requestedLocale)
            .contains(defaultLocale);
    }

    @Test
    void shouldProvideRequestedAndDefaultLocaleGetters() {
        // Given
        String requestedLocale = "fr";
        String defaultLocale = "en";
        
        // When
        UnsupportedLocaleException exception = new UnsupportedLocaleException(requestedLocale, defaultLocale);
        
        // Then
        assertThat(exception.getRequestedLocale()).isEqualTo("fr");
        assertThat(exception.getDefaultLocale()).isEqualTo("en");
    }
}

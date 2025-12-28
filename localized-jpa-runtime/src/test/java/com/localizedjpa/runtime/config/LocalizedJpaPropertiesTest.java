package com.localizedjpa.runtime.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LocalizedJpaProperties}.
 */
class LocalizedJpaPropertiesTest {

    @Test
    void shouldHaveDefaultValues() {
        // Given
        LocalizedJpaProperties properties = new LocalizedJpaProperties();
        
        // Then
        assertThat(properties.getDefaultLocale()).isEqualTo("en");
        assertThat(properties.getSupportedLocales()).isEmpty(); // Default is empty list
        assertThat(properties.isExceptionOnUnsupportedLanguages()).isTrue(); // Default is true
    }

    @Test
    void shouldSetAndGetSupportedLocales() {
        // Given
        LocalizedJpaProperties properties = new LocalizedJpaProperties();
        
        // When
        properties.setSupportedLocales(List.of("en", "tr", "de"));
        
        // Then
        assertThat(properties.getSupportedLocales()).containsExactly("en", "tr", "de");
    }

    @Test
    void shouldSetAndGetDefaultLocale() {
        // Given
        LocalizedJpaProperties properties = new LocalizedJpaProperties();
        
        // When
        properties.setDefaultLocale("tr");
        
        // Then
        assertThat(properties.getDefaultLocale()).isEqualTo("tr");
    }

    @Test
    void shouldSetAndGetExceptionOnUnsupportedLanguages() {
        // Given
        LocalizedJpaProperties properties = new LocalizedJpaProperties();
        
        // When
        properties.setExceptionOnUnsupportedLanguages(false);
        
        // Then
        assertThat(properties.isExceptionOnUnsupportedLanguages()).isFalse();
    }
}

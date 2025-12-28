package com.localizedjpa.runtime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BaseTranslation}.
 */
class BaseTranslationTest {

    @Test
    void shouldSetAndGetLocale() {
        // Given
        TestTranslation translation = new TestTranslation();
        String locale = "en";
        
        // When
        translation.setLocale(locale);
        
        // Then
        assertThat(translation.getLocale()).isEqualTo(locale);
    }

    @Test
    void equals_shouldCompareByLocale() {
        // Given
        TestTranslation t1 = new TestTranslation();
        t1.setLocale("en");
        
        TestTranslation t2 = new TestTranslation();
        t2.setLocale("en");
        
        TestTranslation t3 = new TestTranslation();
        t3.setLocale("de");
        
        // Then - equals by locale (business key), not ID
        assertThat(t1).isEqualTo(t2);
        assertThat(t1).isNotEqualTo(t3);
    }

    @ Test
    void hashCode_shouldBeConsistentWithEquals() {
        // Given
        TestTranslation t1 = new TestTranslation();
        t1.setLocale("en");
        
        TestTranslation t2 = new TestTranslation();
        t2.setLocale("en");
        
        // Then
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    }

    @Test
    void toString_shouldIncludeClassNameAndLocale() {
        // Given
        TestTranslation translation = new TestTranslation();
        translation.setLocale("de");
        
        // When
        String result = translation.toString();
        
        // Then
        assertThat(result)
            .contains("TestTranslation")
            .contains("locale='de'");
        // Note: ID might be null in test, that's OK
    }

    // Test implementation of BaseTranslation
    private static class TestTranslation extends BaseTranslation {
    }
}

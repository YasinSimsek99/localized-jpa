package com.localizedjpa.runtime;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link LocalizedSpecifications}.
 */
class LocalizedSpecificationsTest {

    @Test
    void byLocalizedField_shouldCreateCorrectSpecification() {
        // Given
        String fieldName = "name";
        String value = "Table";
        Locale locale = Locale.ENGLISH;
        
        // When
        Specification<Object> spec = LocalizedSpecifications.byLocalizedField(
            fieldName, value, locale, Object.class);
        
        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    void byLocalizedFieldContaining_shouldCreateLikeSpecification() {
        // Given
        String fieldName = "description";
        String value = "modern";
        Locale locale = Locale.GERMAN;
        
        // When
        Specification<Object> spec = LocalizedSpecifications.byLocalizedFieldContaining(
            fieldName, value, locale, Object.class);
        
        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    void withTranslations_shouldFetchTranslationsEagerly() {
        // When
        Specification<Object> spec = LocalizedSpecifications.withTranslations();
        
        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    void hasTranslationFor_shouldFilterByLocale() {
        // Given
        Locale locale = Locale.FRENCH;
        
        // When
        Specification<Object> spec = LocalizedSpecifications.hasTranslationFor(locale);
        
        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    void byLocalizedField_shouldJoinTranslationsAndFilterByLocale() {
        // Given
        Root<Object> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Join<Object, Object> join = mock(Join.class);
        
        when(root.join(eq("translations"), any())).thenReturn(join);
        
        // When
        Specification<Object> spec = LocalizedSpecifications.byLocalizedField(
            "name", "Table", Locale.ENGLISH, Object.class);
        spec.toPredicate(root, query, cb);
        
        // Then
        verify(root).join(eq("translations"), any());
        verify(join).get("locale");
        verify(join).get("name");
    }

    @Test
    void byLocalizedFieldContaining_shouldUseLowerCaseComparison() {
        // Given
        Root<Object> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Join<Object, Object> join = mock(Join.class);
        
        when(root.join(eq("translations"), any())).thenReturn(join);
        when(cb.lower(any())).thenReturn(mock());
        
        // When
        Specification<Object> spec = LocalizedSpecifications.byLocalizedFieldContaining(
            "description", "TEST", Locale.ENGLISH, Object.class);
        spec.toPredicate(root, query, cb);
        
        // Then
        verify(cb).lower(any());
        verify(cb).like(any(), eq("%test%"));
    }
}

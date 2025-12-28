package com.localizedjpa.annotations;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LocalizedEntity} annotation.
 */
class LocalizedEntityAnnotationTest {

    @Test
    void shouldHaveRuntimeRetention() {
        Retention retention = LocalizedEntity.class.getAnnotation(Retention.class);
        assertThat(retention).isNotNull();
        assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
    }

    @Test
    void shouldTargetTypes() {
        Target target = LocalizedEntity.class.getAnnotation(Target.class);
        assertThat(target).isNotNull();
        assertThat(target.value()).containsExactly(ElementType.TYPE);
    }

    @Test
    void shouldHaveTranslationTableDefaultValue() throws NoSuchMethodException {
        String defaultValue = (String) LocalizedEntity.class.getMethod("translationTable").getDefaultValue();
        assertThat(defaultValue).isEmpty();
    }
}

package com.localizedjpa.annotations;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Localized} annotation.
 */
class LocalizedAnnotationTest {

    @Test
    void shouldHaveRuntimeRetention() {
        Retention retention = Localized.class.getAnnotation(Retention.class);
        assertThat(retention).isNotNull();
        assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
    }

    @Test
    void shouldTargetFields() {
        Target target = Localized.class.getAnnotation(Target.class);
        assertThat(target).isNotNull();
        assertThat(target.value()).containsExactly(ElementType.FIELD);
    }

    @Test
    void shouldHaveFallbackDefaultValue() throws NoSuchMethodException {
        boolean defaultValue = (boolean) Localized.class.getMethod("fallback").getDefaultValue();
        assertThat(defaultValue).isTrue();
    }
}

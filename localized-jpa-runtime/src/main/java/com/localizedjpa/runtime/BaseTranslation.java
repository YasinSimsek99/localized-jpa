package com.localizedjpa.runtime;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Abstract base class for all generated translation entities.
 * 
 * <p>Each {@code @LocalizedEntity} will have a corresponding translation entity
 * that extends this class. The translation entity contains all {@code @Localized}
 * fields for a specific locale.
 * 
 * <p>Example generated entity:
 * <pre>
 * {@literal @}Entity
 * {@literal @}Table(name = "product_translations")
 * public class ProductTranslation extends BaseTranslation {
 *     private String name;
 *     private String description;
 *     // getters/setters
 * }
 * </pre>
 */
@MappedSuperclass
public abstract class BaseTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Two translations are equal if they have the same locale within the same parent.
     * Using locale as business key is safer than using mutable id.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseTranslation that = (BaseTranslation) o;
        return Objects.equals(locale, that.locale);
    }

    /**
     * Hash code based on locale only (business key).
     * This ensures stable hashCode even when id changes after persist.
     */
    @Override
    public int hashCode() {
        return Objects.hash(locale);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", locale='" + locale + '\'' +
                '}';
    }
}

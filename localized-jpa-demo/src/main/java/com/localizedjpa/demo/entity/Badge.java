package com.localizedjpa.demo.entity;

import com.localizedjpa.annotations.Localized;
import jakarta.persistence.*;

/**
 * Badge entity with localized fields.
 * 
 * <p>Demonstrates that the Localized JPA pattern works seamlessly with multiple
 * entity types - just add {@code @Localized} to fields and you're done!
 * No need for {@code @LocalizedEntity} annotation.
 * 
 * <p>Example usage:
 * <pre>
 * Badge badge = new Badge();
 * badge.setName("New Product", Locale.ENGLISH);
 * badge.setName("Yeni Ürün", new Locale("tr"));
 * badge.setColor("#00FF00");
 * </pre>
 */
@Entity
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Localized badge name (e.g., "New", "Best Seller", "Limited Edition")
     */
    @Localized
    private String name;

    /**
     * Localized badge description
     */
    @Localized
    private String description;

    /**
     * Badge color (hex code, not localized)
     */
    private String color;

    /**
     * Badge icon (emoji or icon name, not localized)
     */
    private String icon;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}

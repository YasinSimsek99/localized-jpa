package com.localizedjpa.demo.entity;

import com.localizedjpa.annotations.Localized;
import jakarta.persistence.*;

/**
 * Demo entity - TRUE ZERO BOILERPLATE!
 * 
 * <p>The annotation processor automatically injects:
 * <ul>
 *   <li>translations Map field</li>
 *   <li>getName(), setName(String), getName(Locale), setName(String, Locale)</li>
 *   <li>getDescription(), setDescription(String), ...</li>
 *   <li>getTranslations(), setTranslations(Map)</li>
 * </ul>
 * 
 * <p>No need for {@code @LocalizedEntity} - just use {@code @Localized} on fields!
 */
@Entity
@Table(name = "product")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Localized
    private String name;
    
    @Localized(fallback = false)
    private String description;
    
    @Column(name = "price")
    private Double price;
    
    /**
     * Badges associated with this product (e.g., "New", "Best Seller", etc.)
     */
    @ManyToMany
    @JoinTable(
        name = "product_badges",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "badge_id")
    )
    private java.util.List<Badge> badges = new java.util.ArrayList<>();

    // Only getter/setter for id and price - non-localized fields
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
    
    public java.util.List<Badge> getBadges() {
        return badges;
    }
    
    public void setBadges(java.util.List<Badge> badges) {
        this.badges = badges;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

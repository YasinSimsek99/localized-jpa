package com.localizedjpa.demo.entity;

import com.localizedjpa.annotations.Localized;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Demo entity
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
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Localized
    private String name;

    @Localized(fallback = false)
    @Column(length = 2000)
    @NotNull
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
    private List<Badge> badges = new java.util.ArrayList<>();
}

package com.localizedjpa.demo.repository;

import com.localizedjpa.demo.entity.Badge;
import com.localizedjpa.runtime.LocalizedRepository;

import java.util.List;

/**
 * Repository for Badge entities with locale-aware query support.
 * 
 * <p>Demonstrates the SAME zero-boilerplate pattern working for Badge entity!
 * Just declare methods - processor generates implementations automatically.
 */
public interface BadgeRepository extends LocalizedRepository<Badge, Long> {
    
    // ========== EXACT MATCH QUERIES ==========
    
    /**
     * Find badges by exact localized name match.
     */
    List<Badge> findByName(String name);
    
    /**
     * Find badges by exact localized description match.
     */
    List<Badge> findByDescription(String description);
    
    // ========== LIKE QUERIES (Containing) ==========
    
    /**
     * Find badges where localized name contains the search term.
     */
    List<Badge> findByNameContaining(String searchTerm);
    
    /**
     * Find badges where localized description contains the search term.
     */
    List<Badge> findByDescriptionContaining(String searchTerm);
}

package com.localizedjpa.demo.dto;

/**
 * DTO for Product with localized fields resolved.
 */
public record ProductDto(
    Long id, 
    Double price, 
    String name,         // Localized!
    String description,  // Localized!
    java.util.List<BadgeDto> badges
) {}

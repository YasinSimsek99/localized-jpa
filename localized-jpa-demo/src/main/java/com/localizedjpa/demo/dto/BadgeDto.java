package com.localizedjpa.demo.dto;

/**
 * DTO for Badge with localized fields resolved.
 */
public record BadgeDto(
    Long id,
    String name,         // Localized!
    String description,  // Localized!
    String color,
    String icon
) {}

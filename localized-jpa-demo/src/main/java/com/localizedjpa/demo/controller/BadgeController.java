package com.localizedjpa.demo.controller;

import com.localizedjpa.demo.dto.BadgeDto;
import com.localizedjpa.demo.entity.Badge;
import com.localizedjpa.demo.service.BadgeService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * REST controller for badges with localized fields.
 */
@RestController
@RequestMapping("/api/badges")
public class BadgeController {

    private final BadgeService badgeService;

    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    /**
     * Get all badges.
     */
    @GetMapping
    public List<BadgeDto> getAllBadges() {
        Locale locale = LocaleContextHolder.getLocale();
        return badgeService.findAll().stream()
            .map(b -> toDto(b, locale))
            .collect(Collectors.toList());
    }

    /**
     * Get badge by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BadgeDto> getBadgeById(@PathVariable Long id) {
        Locale locale = LocaleContextHolder.getLocale();
        return badgeService.findById(id)
            .map(b -> toDto(b, locale))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // ========== GENERATED QUERY METHOD ENDPOINTS ==========

    /**
     * Search badges by exact localized name match.
     */
    @GetMapping("/search/name")
    public List<BadgeDto> searchByName(@RequestParam String name) {
        Locale locale = LocaleContextHolder.getLocale();
        return badgeService.findByName(name).stream()
            .map(b -> toDto(b, locale))
            .collect(Collectors.toList());
    }

    /**
     * Search badges where localized name CONTAINS search term.
     */
    @GetMapping("/search/name/containing")
    public List<BadgeDto> searchByNameContaining(@RequestParam String searchTerm) {
        Locale locale = LocaleContextHolder.getLocale();
        return badgeService.findByNameContaining(searchTerm).stream()
            .map(b -> toDto(b, locale))
            .collect(Collectors.toList());
    }

    /**
     * Search badges by exact localized description match.
     */
    @GetMapping("/search/description")
    public List<BadgeDto> searchByDescription(@RequestParam String description) {
        Locale locale = LocaleContextHolder.getLocale();
        return badgeService.findByDescription(description).stream()
            .map(b -> toDto(b, locale))
            .collect(Collectors.toList());
    }

    /**
     * Search badges where localized description CONTAINS search term.
     */
    @GetMapping("/search/description/containing")
    public List<BadgeDto> searchByDescriptionContaining(@RequestParam String searchTerm) {
        Locale locale = LocaleContextHolder.getLocale();
        return badgeService.findByDescriptionContaining(searchTerm).stream()
            .map(b -> toDto(b, locale))
            .collect(Collectors.toList());
    }

    /**
     * Convert Badge entity to DTO with localized fields resolved.
     */
    private BadgeDto toDto(Badge badge, Locale locale) {
        return new BadgeDto(
            badge.getId(),
            badge.getName(locale),
            badge.getDescription(locale),
            badge.getColor(),
            badge.getIcon()
        );
    }
}

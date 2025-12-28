package com.localizedjpa.demo.service;

import com.localizedjpa.demo.entity.Badge;
import com.localizedjpa.demo.repository.BadgeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing badges with localized fields.
 */
@Service
@Transactional
public class BadgeService {

    private final BadgeRepository badgeRepository;

    public BadgeService(BadgeRepository badgeRepository) {
        this.badgeRepository = badgeRepository;
    }

    public Badge save(Badge badge) {
        return badgeRepository.save(badge);
    }

    public Optional<Badge> findById(Long id) {
        return badgeRepository.findById(id);
    }

    public List<Badge> findAll() {
        return badgeRepository.findAll();
    }

    // ========== GENERATED QUERY METHODS ==========

    public List<Badge> findByName(String name) {
        return badgeRepository.findByName(name);
    }

    public List<Badge> findByNameContaining(String searchTerm) {
        return badgeRepository.findByNameContaining(searchTerm);
    }

    public List<Badge> findByDescription(String description) {
        return badgeRepository.findByDescription(description);
    }

    public List<Badge> findByDescriptionContaining(String searchTerm) {
        return badgeRepository.findByDescriptionContaining(searchTerm);
    }

    public void deleteById(Long id) {
        badgeRepository.deleteById(id);
    }
}

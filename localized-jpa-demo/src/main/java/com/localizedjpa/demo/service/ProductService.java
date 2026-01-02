package com.localizedjpa.demo.service;

import com.localizedjpa.demo.entity.Product;
import com.localizedjpa.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Service for managing products with localized fields.
 * Delegates to generated repository methods.
 */
@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    // ========== GENERATED QUERY METHODS ==========

    /**
     * Find products by exact localized name match.
     */
    public List<Product> findByName(String name) {
        return productRepository.findByName(name);
    }

    /**
     * Find products by exact localized name and locale param.
     */
    public List<Product> findByName(String name, Locale locale) {
        return productRepository.findByName(name, locale);
    }
    
    /**
     * Find products by exact localized description match.
     */
    public List<Product> findByDescription(String description) {
        return productRepository.findByDescription(description);
    }

    /**
     * Find products where localized name contains search term.
     */
    public List<Product> findByNameContaining(String searchTerm) {
        return productRepository.findByNameContaining(searchTerm);
    }

    /**
     * Find products where localized description contains search term.
     */
    public List<Product> findByDescriptionContaining(String searchTerm) {
        return productRepository.findByDescriptionContaining(searchTerm);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }
}

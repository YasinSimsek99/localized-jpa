package com.localizedjpa.demo.controller;

import com.localizedjpa.demo.dto.BadgeDto;
import com.localizedjpa.demo.dto.ProductDto;
import com.localizedjpa.demo.entity.Product;
import com.localizedjpa.demo.service.ProductService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * REST controller for products with localized fields.
 * 
 * <p>Locale is automatically resolved from Accept-Language header
 * by LocalizedJpaAutoConfiguration. No manual header extraction needed!
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Get all products.
     * Locale automatically detected from Accept-Language header.
     */
    @GetMapping
    public List<ProductDto> getAllProducts() {
        // LocaleContextHolder.getLocale() automatically set by framework
        return productService.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Get product by ID.
     * Locale automatically detected from Accept-Language header.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        return productService.findById(id)
            .map(this::toDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // ========== GENERATED QUERY METHOD ENDPOINTS ==========
    
    /**
     * Search products by exact localized name match.
     * Uses automatically generated query method!
     */
    @GetMapping("/search/name")
    public List<ProductDto> searchByName(@RequestParam String name) {
        Locale locale = LocaleContextHolder.getLocale();
        return productService.findByName(name).stream()
            .map(p -> toDto(p, locale))
            .collect(Collectors.toList());
    }
    
    /**
     * Search products by exact localized description match.
     */
    @GetMapping("/search/description")
    public List<ProductDto> searchByDescription(@RequestParam String description) {
        Locale locale = LocaleContextHolder.getLocale();
        return productService.findByDescription(description).stream()
            .map(p -> toDto(p, locale))
            .collect(Collectors.toList());
    }
    
    /**
     * Search products where localized name CONTAINS search term.
     * Demonstrates LIKE query with generated method!
     */
    @GetMapping("/search/name/containing")
    public List<ProductDto> searchByNameContaining(@RequestParam String searchTerm) {
        Locale locale = LocaleContextHolder.getLocale();
        return productService.findByNameContaining(searchTerm).stream()
            .map(p -> toDto(p, locale))
            .collect(Collectors.toList());
    }
    
    /**
     * Search products where localized description CONTAINS search term.
     */
    @GetMapping("/search/description/containing")
    public List<ProductDto> searchByDescriptionContaining(@RequestParam String searchTerm) {
        Locale locale = LocaleContextHolder.getLocale();
        return productService.findByDescriptionContaining(searchTerm).stream()
            .map(p -> toDto(p, locale))
            .collect(Collectors.toList());
    }

    /**
     * Create a product with localized fields.
     */
    @PostMapping
    public ProductDto createProduct(@RequestBody CreateProductRequest request) {
        Product product = new Product();
        product.setPrice(request.price());
        
        for (var translation : request.translations()) {
            Locale locale = Locale.forLanguageTag(translation.locale());
            product.setName(translation.name(), locale);
            product.setDescription(translation.description(), locale);
        }
        
        Product saved = productService.save(product);
        return toDto(saved);
    }

    /**
     * Update product localized fields.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, 
                                                     @RequestBody UpdateProductRequest request) {
        return productService.findById(id)
            .map(product -> {
                if (request.price() != null) {
                    product.setPrice(request.price());
                }
                
                Locale locale = Locale.forLanguageTag(request.locale());
                if (request.name() != null) {
                    product.setName(request.name(), locale);
                }
                if (request.description() != null) {
                    product.setDescription(request.description(), locale);
                }
                
                Product saved = productService.save(product);
                return ResponseEntity.ok(toDto(saved, locale));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a product.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/searchByParams")
    public List<Product> searchByParams(@RequestParam(name = "searchTerm") String searchTerm) {
        return productService.findByName(searchTerm, LocaleContextHolder.getLocale());
    }

    /**
     * Converts Product entity to DTO with localized fields.
     * Uses current locale from LocaleContextHolder (set by framework).
     */
    private ProductDto toDto(Product product) {
        Locale locale = LocaleContextHolder.getLocale();
        return toDto(product, locale);
    }

    /**
     * Converts Product entity to DTO with explicit locale.
     */
    private ProductDto toDto(Product product, Locale locale) {
        String name = product.getName(locale);
        String description = product.getDescription(locale);
        
        // Convert badges to DTOs with localized names
        java.util.List<BadgeDto> badgeDtos = product.getBadges().stream()
            .map(badge -> new BadgeDto(
                badge.getId(),
                badge.getName(locale),  // Localized badge name!
                badge.getDescription(locale),
                badge.getColor(),
                badge.getIcon()
            ))
            .collect(java.util.stream.Collectors.toList());
        
        return new ProductDto(
            product.getId(),
            product.getPrice(),
            name,
            description,
            badgeDtos  // Include badges!
        );
    }

    // Request DTOs
    public record CreateProductRequest(
        Double price,
        List<TranslationRequest> translations
    ) {}
    
    public record TranslationRequest(
        String locale,
        String name,
        String description
    ) {}
    
    public record UpdateProductRequest(
        Double price,
        String locale,
        String name,
        String description
    ) {}
}

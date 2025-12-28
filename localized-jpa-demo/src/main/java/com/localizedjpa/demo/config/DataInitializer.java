package com.localizedjpa.demo.config;

import com.localizedjpa.demo.entity.Badge;
import com.localizedjpa.demo.entity.Product;
import com.localizedjpa.demo.repository.BadgeRepository;
import com.localizedjpa.demo.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Initializes sample data on application startup.
 *
 * <p>Enhanced with 10 products and 5 badges demonstrating Many-to-Many relationships!
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final BadgeRepository badgeRepository;

    public DataInitializer(ProductRepository productRepository, BadgeRepository badgeRepository) {
        this.productRepository = productRepository;
        this.badgeRepository = badgeRepository;
    }

    @Override
    public void run(String... args) {
        // ========== BADGES FIRST ===========
        java.util.List<Badge> badges = initializeBadges();
        
        // ========== PRODUCTS WITH BADGES ==========
        // Product 1: Wooden Table - Premium quality, eco-friendly
        Product table = new Product();
        table.setPrice(299.99);
        table.setName("Wooden Table", Locale.ENGLISH);
        table.setDescription("A beautiful handcrafted wooden table", Locale.ENGLISH);
        table.setName("Ahşap Masa", Locale.forLanguageTag("tr"));
        table.setDescription("El yapımı güzel bir ahşap masa", Locale.forLanguageTag("tr"));
        table.setBadges(java.util.Arrays.asList(badges.get(3), badges.get(4))); // Eco Friendly, Premium
        productRepository.save(table);

        // Product 2: Office Chair - Best seller!
        Product chair = new Product();
        chair.setPrice(149.99);
        chair.setName("Office Chair", Locale.ENGLISH);
        chair.setDescription("Ergonomic office chair with lumbar support", Locale.ENGLISH);
        chair.setName("Ofis Sandalyesi", Locale.forLanguageTag("tr"));
        chair.setDescription("Bel destekli ergonomik ofis sandalyesi", Locale.forLanguageTag("tr"));
        chair.setBadges(java.util.Arrays.asList(badges.get(1))); // Best Seller
        productRepository.save(chair);

        // Product 3: LED Desk Lamp - New product
        Product lamp = new Product();
        lamp.setPrice(79.99);
        lamp.setName("LED Desk Lamp", Locale.ENGLISH);
        lamp.setDescription("Modern LED desk lamp with adjustable brightness", Locale.ENGLISH);
        lamp.setName("LED Masa Lambası", Locale.forLanguageTag("tr"));
        lamp.setDescription("Ayarlanabilir parlaklıklı modern LED masa lambası", Locale.forLanguageTag("tr"));
        lamp.setBadges(java.util.Arrays.asList(badges.get(0))); // New
        productRepository.save(lamp);

        // Product 4: Wooden Bookshelf - Eco-friendly
        Product bookshelf = new Product();
        bookshelf.setPrice(189.99);
        bookshelf.setName("Wooden Bookshelf", Locale.ENGLISH);
        bookshelf.setDescription("Five-tier wooden bookshelf for storage", Locale.ENGLISH);
        bookshelf.setName("Ahşap Kitaplık", Locale.forLanguageTag("tr"));
        bookshelf.setDescription("Depolama için beş katlı ahşap kitaplık", Locale.forLanguageTag("tr"));
        bookshelf.setBadges(java.util.Arrays.asList(badges.get(3))); // Eco Friendly
        productRepository.save(bookshelf);

        // Product 5: Standing Desk - Premium, New
        Product standingDesk = new Product();
        standingDesk.setPrice(449.99);
        standingDesk.setName("Adjustable Standing Desk", Locale.ENGLISH);
        standingDesk.setDescription("Electric height-adjustable standing desk", Locale.ENGLISH);
        standingDesk.setName("Ayarlanabilir Ayakta Çalışma Masası", Locale.forLanguageTag("tr"));
        standingDesk.setDescription("Elektrikli yükseklik ayarlı ayakta çalışma masası", Locale.forLanguageTag("tr"));
        standingDesk.setBadges(java.util.Arrays.asList(badges.get(0), badges.get(4))); // New, Premium
        productRepository.save(standingDesk);

        // Product 6: Monitor Stand - Best Seller
        Product monitorStand = new Product();
        monitorStand.setPrice(39.99);
        monitorStand.setName("Monitor Stand", Locale.ENGLISH);
        monitorStand.setDescription("Ergonomic wooden monitor riser stand", Locale.ENGLISH);
        monitorStand.setName("Monitör Standı", Locale.forLanguageTag("tr"));
        monitorStand.setDescription("Ergonomik ahşap monitör yükseltici standı", Locale.forLanguageTag("tr"));
        monitorStand.setBadges(java.util.Arrays.asList(badges.get(1))); // Best Seller
        productRepository.save(monitorStand);

        // Product 7: Desk Organizer
        Product organizer = new Product();
        organizer.setPrice(24.99);
        organizer.setName("Desk Organizer", Locale.ENGLISH);
        organizer.setDescription("Wooden desk organizer with compartments", Locale.ENGLISH);
        organizer.setName("Masa Düzenleyici", Locale.forLanguageTag("tr"));
        organizer.setDescription("Bölmeli ahşap masa düzenleyici", Locale.forLanguageTag("tr"));
        productRepository.save(organizer);

        // Product 8: Table Clock - Limited Edition
        Product clock = new Product();
        clock.setPrice(34.99);
        clock.setName("Wooden Table Clock", Locale.ENGLISH);
        clock.setDescription("Vintage style wooden table clock", Locale.ENGLISH);
        clock.setName("Ahşap Masa Saati", Locale.forLanguageTag("tr"));
        clock.setDescription("Vintage tarzı ahşap masa saati", Locale.forLanguageTag("tr"));
        clock.setBadges(java.util.Arrays.asList(badges.get(2))); // Limited Edition
        productRepository.save(clock);

        // Product 9: Floor Lamp - New
        Product floorLamp = new Product();
        floorLamp.setPrice(119.99);
        floorLamp.setName("Modern Floor Lamp", Locale.ENGLISH);
        floorLamp.setDescription("Tall modern floor lamp with LED light", Locale.ENGLISH);
        floorLamp.setName("Modern Zemin Lambası", Locale.forLanguageTag("tr"));
        floorLamp.setDescription("LED ışıklı uzun modern zemin lambası", Locale.forLanguageTag("tr"));
        floorLamp.setBadges(java.util.Arrays.asList(badges.get(0))); // New
        productRepository.save(floorLamp);

        // Product 10: Coffee Table - Premium
        Product coffeeTable = new Product();
        coffeeTable.setPrice(179.99);
        coffeeTable.setName("Glass Coffee Table", Locale.ENGLISH);
        coffeeTable.setDescription("Modern glass top coffee table", Locale.ENGLISH);
        coffeeTable.setName("Cam Sehpa", Locale.forLanguageTag("tr"));
        coffeeTable.setDescription("Modern cam üstlü sehpa", Locale.forLanguageTag("tr"));
        coffeeTable.setBadges(java.util.Arrays.asList(badges.get(4))); // Premium
        productRepository.save(coffeeTable);

        System.out.println("✅ Initialized 10 sample products with badges!");
    }
    
    /**
     * Initialize sample badges with localized fields.
     * Returns list of saved badges for assigning to products.
     */
    private java.util.List<Badge> initializeBadges() {
        java.util.List<Badge> result = new java.util.ArrayList<>();
        
        // Badge 1: New
        Badge newBadge = new Badge();
        newBadge.setName("New", Locale.ENGLISH);
        newBadge.setDescription("Recently added product", Locale.ENGLISH);
        newBadge.setName("Yeni", Locale.forLanguageTag("tr"));
        newBadge.setDescription("Yakınlarda eklenen ürün", Locale.forLanguageTag("tr"));
        newBadge.setColor("#00FF00");
        newBadge.setIcon("🆕");
        result.add(badgeRepository.save(newBadge));

        // Badge 2: Best Seller
        Badge bestSeller = new Badge();
        bestSeller.setName("Best Seller", Locale.ENGLISH);
        bestSeller.setDescription("Top selling product", Locale.ENGLISH);
        bestSeller.setName("Çok Satan", Locale.forLanguageTag("tr"));
        bestSeller.setDescription("En çok satan ürün", Locale.forLanguageTag("tr"));
        bestSeller.setColor("#FFD700");
        bestSeller.setIcon("⭐");
        result.add(badgeRepository.save(bestSeller));

        // Badge 3: Limited Edition
        Badge limited = new Badge();
        limited.setName("Limited Edition", Locale.ENGLISH);
        limited.setDescription("Available in limited quantities", Locale.ENGLISH);
        limited.setName("Sınırlı Sayıda", Locale.forLanguageTag("tr"));
        limited.setDescription("Sınırlı miktarda mevcut", Locale.forLanguageTag("tr"));
        limited.setColor("#FF0000");
        limited.setIcon("💎");
        result.add(badgeRepository.save(limited));

        // Badge 4: Eco Friendly
        Badge eco = new Badge();
        eco.setName("Eco Friendly", Locale.ENGLISH);
        eco.setDescription("Environmentally friendly product", Locale.ENGLISH);
        eco.setName("Çevre Dostu", Locale.forLanguageTag("tr"));
        eco.setDescription("Çevre dostu ürün", Locale.forLanguageTag("tr"));
        eco.setColor("#228B22");
        eco.setIcon("🌱");
        result.add(badgeRepository.save(eco));

        // Badge 5: Premium Quality
        Badge premium = new Badge();
        premium.setName("Premium Quality", Locale.ENGLISH);
        premium.setDescription("High-quality premium product", Locale.ENGLISH);
        premium.setName("Premium Kalite", Locale.forLanguageTag("tr"));
        premium.setDescription("Yüksek kaliteli premium ürün", Locale.forLanguageTag("tr"));
        premium.setColor("#4B0082");
        premium.setIcon("👑");
        result.add(badgeRepository.save(premium));

        System.out.println("✅ Initialized 5 sample badges with localized data!");
        return result;
    }
}

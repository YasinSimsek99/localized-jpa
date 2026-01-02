# Localized JPA

[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.1.2-blue)](https://central.sonatype.com/artifact/com.localizedjpa/localized-jpa-starter)
[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-green)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue)](LICENSE)

Multi-language JPA entity support with compile-time code generation for Spring Boot applications.

## Installation

```xml
<dependency>
    <groupId>com.localizedjpa</groupId>
    <artifactId>localized-jpa-starter</artifactId>
    <version>0.1.2</version>
</dependency>
```

## Configuration

```yaml
localized-jpa:
  supported-locales:
    - en
    - tr
    - de
  default-locale: en
  exception-on-unsupported-languages: true  # 406 for unsupported locales
```

## Usage

### 1. Annotate Fields

```java
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Localized
    private String name;

    @Localized
    private String description;

    private Double price;
}
```

### 2. Use Generated Methods

```java
Product product = new Product();

// Current locale (from Accept-Language header)
product.setName("Laptop");
String name = product.getName();

// Explicit locale
product.setName("Dizüstü Bilgisayar", Locale.forLanguageTag("tr"));
String turkishName = product.getName(Locale.forLanguageTag("tr"));
```

### 3. Query by Localized Fields

```java
public interface ProductRepository extends LocalizedRepository<Product, Long> {
    
    // Current locale (from Accept-Language header)
    List<Product> findByName(String name);
    List<Product> findByNameContaining(String keyword);
    
    // Explicit locale parameter
    List<Product> findByName(String name, Locale locale);
    List<Product> findByNameContaining(String keyword, Locale locale);
}
```

**Usage:**

```java
@Service
public class ProductService {
    
    @Autowired
    private ProductRepository repository;
    
    public List<Product> search(String keyword) {
        // Uses current locale from request
        return repository.findByNameContaining(keyword);
    }
    
    public List<Product> searchInTurkish(String keyword) {
        // Explicit Turkish locale
        return repository.findByNameContaining(keyword, Locale.forLanguageTag("tr"));
    }
}
```

## Generated Code

The annotation processor generates:

| Generated | Description |
|-----------|-------------|
| `ProductTranslation` | Translation entity with locale-specific fields |
| `ProductLocalized` | Interface defining localized method signatures |
| Getter/Setter methods | `getName()`, `getName(Locale)`, `setName(String)`, `setName(String, Locale)` |

## Locale Resolution

Locale is resolved automatically from HTTP request:

```
Accept-Language: tr
```

Or via query parameter:

```
GET /products?lang=tr
```

## IntelliJ IDEA Plugin

For IDE support (autocomplete, no red squiggles), install the companion plugin:

[![IntelliJ Plugin](https://img.shields.io/badge/IntelliJ%20IDEA-Plugin-purple?logo=intellijidea)](https://github.com/YasinSimsek99/localized-jpa-plugin)

## Demo Project

See `localized-jpa-demo/` for a complete working example.

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history and release notes.

## License

Apache License 2.0

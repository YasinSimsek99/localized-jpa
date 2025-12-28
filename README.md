# Localized JPA

[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.1.1-blue)](https://central.sonatype.com/artifact/com.localizedjpa/localized-jpa-starter)
[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-green)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue)](LICENSE)

**Localized JPA** is a Spring Boot library that brings **first-class multi-language (i18n) support to JPA entities** with zero boilerplate and compile-time code generation.

Define localized fields using a single annotation — the library automatically generates translation entities, locale-aware getters/setters, and type-safe query specifications.

---

## Why Localized JPA?

Traditional JPA internationalization usually means:
- Manually designed translation tables
- Complex JOIN queries
- Repetitive getter/setter and mapping code

Localized JPA removes this overhead completely.

---

## Key Features

- Zero Boilerplate
- Compile-Time Generation
- Type-Safe Queries
- N+1 Prevention
- Automatic Locale Resolution
- Flexible Fallback Strategy

---

## 🧩 IntelliJ IDEA Plugin

For the best development experience, install the **Localized JPA Plugin** for IntelliJ IDEA:

[![IntelliJ Plugin](https://img.shields.io/badge/IntelliJ%20IDEA-Plugin-purple?logo=intellijidea)](https://github.com/YasinSimsek99/localized-jpa-plugin)

The plugin provides:
- ✅ **Full autocomplete** for generated locale-aware methods
- ✅ **Navigation support** to jump between entity and generated translation classes
- ✅ **No red squiggles** — IDE recognizes synthetic methods immediately

> **Tip**  
> Without the plugin, IntelliJ may show errors for generated methods until a full rebuild. The plugin eliminates this friction entirely.

👉 [**Get the Plugin on GitHub**](https://github.com/YasinSimsek99/localized-jpa-plugin)

---

## Installation

```xml
<dependency>
    <groupId>com.localizedjpa</groupId>
    <artifactId>localized-jpa-starter</artifactId>
    <version>0.1.1</version>
</dependency>
```

---

## Quick Start

```yaml
localized-jpa:
  supported-locales: en,tr,de
  default-locale: en
```

---

## Annotate Your Entity

Simply add `@Localized` to fields in any JPA entity — no class-level annotation needed.

```java
@Entity
public class Product {

    ...

    @Localized
    private String name;

    @Localized(fallback = false)
    private String description;

    ...
}
```

---

## Use Generated Methods

After compilation, the annotation processor injects locale-aware methods directly into your entity.

```java
Product product = new Product();

// Current locale (resolved automatically)
product.setName("Laptop");
String name = product.getName();

// Explicit locale - Turkish
product.setName("Dizüstü Bilgisayar", Locale.forLanguageTag("tr"));
String turkishName = product.getName(Locale.forLanguageTag("tr"));

// Explicit locale - German
product.setName("Tragbarer Computer", Locale.GERMAN);
String germanName = product.getName(Locale.GERMAN);
```

> **Note**  
> The current language is resolved via Spring’s `LocaleContextHolder`.  
> By default, Spring populates this value from the incoming HTTP request header:
>
> ```
> Accept-Language: en
> ```
>
> Localized JPA fully relies on Spring’s standard locale resolution mechanism and does not introduce custom request handling.

---

## Demo Project

A fully working example application is available in the repository under:

```
localized-jpa-demo/
```

The demo project showcases:
- Entity definitions with `@Localized`
- Generated translation entities
- Locale-aware getters and setters
- Request-based language resolution using `Accept-Language`

---

## License

Apache License 2.0

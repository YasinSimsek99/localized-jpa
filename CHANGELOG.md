# Changelog

All notable changes to LocalizedJPA will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [0.1.5] - 2026-02-23

### Added

- **Explicit `@Column(name)` Customization**: Added full support for customizing the generated database column names of `@Localized` fields. When `@Column(name = "custom_field_name")` is explicitly provided on a base entity's field, the annotation processor now strictly respects this name in the Translation entity instead of defaulting to the snake_case conversion of the Java field name.

### Changed

- **Smarter Snake Case Conversion**: Rewrote the internal `StringUtils.toSnakeCase(String)` method using an advanced Regex. It now handles consecutive uppercase abbreviations (e.g., `userID2Profile` -> `user_id2_profile` instead of `user_i_d2_profile`) and properly respects `Locale.ROOT` formatting to prevent Turkish standard locale bugs (like `I` to `ı`).

---

## [0.1.4] - 2026-02-21
### Added

- **AST Validation Cleanup**: Solved `ConstraintViolationException` that occurred during `persist`. The annotation processor now removes `jakarta.validation` and `javax.validation` annotations (e.g., `@NotNull`, `@Size`) from the base entity via AST manipulation at compile time, while safely propagating them to the generated `Translation` entity.
- **Schema & Catalog Propagation**: Automatically propagates `schema` and `catalog` attributes from the base entity's `@Table` annotation to the generated translation table.
- **README Updates**: Added detailed explanations for the `default-locale` configuration and locale fallback mechanism.

### Changed

- Version bumped to `0.1.4` preparing for public repository deployment.

---

## [0.1.3] - 2026-01-13

### Added

- **Annotation Propagation**: Now propagates JPA column definitions (e.g., `@Column(length=...)`) and Validation annotations (e.g., `@NotNull`, `@Size`) from the `@Localized` field to the generated Translation entity fields. This allows for precise database schema control and validation.
- **Spring Boot 3.x Compatibility**: Verified and documented native compatibility with Spring Boot 3.x (and 4.x) thanks to `jakarta.persistence` usage.

### Fixed

- **Maven Reactor Issue**: Fixed module parent referencing to ensure correct build order and reactor composition.

---

### Added

- **Zero-Configuration Module Bypass**: Introduced `Permit.java` utility class that uses `sun.misc.Unsafe` and `Module.implAddOpens` to programmatically open JDK compiler packages. Users no longer need to add `--add-exports` or `--add-opens` flags manually.

- **Startup Banner**: Added `LocalizedJpaStarterInfo` component that displays an ASCII art banner and version information when the application starts.

- **Configuration Validation**: Added startup validation to ensure `default-locale` is present in `supported-locales` list. Throws `IllegalStateException` with a clear error message if misconfigured.

- **Performance Optimization**: Implemented lazy initialization with double-checked locking for `LocalizedJpaProperties` locale conversions. Cached values are now reused across requests instead of creating new objects on every call.

- **Clean JSON Response**: Entity responses no longer include the internal `translations` map. Both the `translations` field on entities and the `parent` field on Translation entities are now annotated with `@JsonIgnore` for clean API responses.

### Fixed

- **Default Locale Bug**: Fixed an issue where the application would use JVM's system locale (e.g., `tr_TR`) instead of the configured `default-locale` when no `Accept-Language` header was present in the request. The `LocaleValidationInterceptor` now explicitly checks for missing headers and uses the configured default.

- **JSON Circular Reference**: Fixed infinite loop during JSON serialization when returning entities with `@Localized` fields. Generated Translation entities now have `@JsonIgnore` on the `parent` field to prevent circular references.

### Changed

- Updated `LocaleValidationInterceptor` to check for `Accept-Language` header presence before calling `localeResolver.resolveLocale()`.
- Added debug logging to trace locale resolution flow for easier troubleshooting.

---

## [0.1.1] - 2025-12-28

### Added

- Initial stable release of LocalizedJPA.
- Core annotation processing with `@Localized` field annotation.
- Compile-time AST manipulation for getter/setter injection.
- Translation entity generation via JavaPoet.
- Spring Boot auto-configuration with `LocalizedJpaAutoConfiguration`.
- `LocalizedSpecifications` utility for locale-aware JPA queries.
- `LocalizedRepository` base interface extending `JpaRepository` and `JpaSpecificationExecutor`.
- Exception handling with `UnsupportedLocaleException` and `LocalizedJpaExceptionHandler`.
- Locale resolution via `AcceptHeaderLocaleResolver` and `LocaleChangeInterceptor`.

### Modules

- `localized-jpa-annotations`: Core annotations (`@Localized`)
- `localized-jpa-compiler`: Annotation processor and AST modifier
- `localized-jpa-runtime`: Spring Boot auto-configuration and runtime utilities
- `localized-jpa-starter`: All-in-one starter dependency

---

## [0.1.0] - 2025-12-27

### Added

- Initial beta release.
- Basic annotation processing infrastructure.
- Proof of concept for AST injection.

---

## Version Comparison

| Version | Zero-Config | Startup Banner | Config Validation | Lazy Caching | Explicit `@Column(name)` |
|---------|-------------|----------------|-------------------|--------------|--------------------------|
| 0.1.0   | ❌          | ❌             | ❌                | ❌           | ❌                       |
| 0.1.1   | ❌          | ❌             | ❌                | ❌           | ❌                       |
| 0.1.2   | ✅          | ✅             | ✅                | ✅           | ❌                       |
| 0.1.3   | ✅          | ✅             | ✅                | ✅           | ❌                       |
| 0.1.4   | ✅          | ✅             | ✅                | ✅           | ❌                       |
| 0.1.5   | ✅          | ✅             | ✅                | ✅           | ✅                       |

---

## Upgrade Guide

### From 0.1.4 to 0.1.5

No breaking changes. Simply update the version in your `pom.xml`:

```xml
<dependency>
    <groupId>com.localizedjpa</groupId>
    <artifactId>localized-jpa-starter</artifactId>
    <version>0.1.5</version>
</dependency>
```

**Benefits of upgrading:**
- Ability to override auto-generated column names using `@Column(name = "your_name")`.
- Smart handling of abbreviations (like `URL`, `ID`, `XML`) during DB schema generation.

---

### From 0.1.3 to 0.1.4

No breaking changes. Simply update the version in your `pom.xml`:

```xml
<dependency>
    <groupId>com.localizedjpa</groupId>
    <artifactId>localized-jpa-starter</artifactId>
    <version>0.1.4</version>
</dependency>
```

**Benefits of upgrading:**
- Fixes `ConstraintViolationException` when using `@NotNull`, `@NotBlank`, etc., on `@Localized` fields.
- Full support for database schemas and catalogs.

---

### From 0.1.1 to 0.1.2

No breaking changes. Simply update the version in your `pom.xml`:

```xml
<dependency>
    <groupId>com.localizedjpa</groupId>
    <artifactId>localized-jpa-starter</artifactId>
    <version>0.1.2</version>
</dependency>
```

**Benefits of upgrading:**
- Remove any `--add-exports` or `--add-opens` compiler arguments from your build configuration.
- Enjoy automatic locale fallback when no `Accept-Language` header is present.
- Get clear error messages if `default-locale` is misconfigured.

---

## Links

- [GitHub Repository](https://github.com/YasinSimsek99/localized-jpa)
- [Maven Central](https://central.sonatype.com/artifact/com.localizedjpa/localized-jpa-starter)

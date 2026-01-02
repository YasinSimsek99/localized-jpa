# Changelog

All notable changes to LocalizedJPA will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.2] - 2026-01-03

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

| Version | Zero-Config | Startup Banner | Config Validation | Lazy Caching |
|---------|-------------|----------------|-------------------|--------------|
| 0.1.0   | ❌          | ❌             | ❌                | ❌           |
| 0.1.1   | ❌          | ❌             | ❌                | ❌           |
| 0.1.2   | ✅          | ✅             | ✅                | ✅           |

---

## Upgrade Guide

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

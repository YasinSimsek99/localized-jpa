package com.localizedjpa.compiler;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.Compiler.javac;
import static com.google.testing.compile.CompilationSubject.assertThat;

class ConstraintPropagationTest {

    @Test
    void shouldPropagateColumnAndValidationAnnotations() {
        JavaFileObject entity = JavaFileObjects.forSourceString(
            "com.example.TestEntity",
            """
            package com.example;
            
            import com.localizedjpa.annotations.Localized;
            import jakarta.persistence.*;
            import jakarta.validation.constraints.NotNull;
            import jakarta.validation.constraints.Size;
            
            @Entity
            @Table(name = "test_entity")
            public class TestEntity {
                
                @Id
                @GeneratedValue
                private Long id;
                
                @Localized
                @Column(length = 500, nullable = false, unique = true)
                @NotNull
                @Size(min = 5, max = 500)
                private String name;
                
                @Localized
                @Lob
                @Basic(fetch = FetchType.LAZY)
                private String description;
            }
            """
        );

        Compilation compilation = javac()
            .withProcessors(new LocalizedProcessor())
            .compile(entity);
            
        assertThat(compilation).succeeded();

        JavaFileObject expectedTranslation = JavaFileObjects.forSourceString(
            "com.example.TestEntityTranslation",
            """
            package com.example;
            
            import com.localizedjpa.runtime.BaseTranslation;
            import jakarta.persistence.Basic;
            import jakarta.persistence.Column;
            import jakarta.persistence.Entity;
            import jakarta.persistence.FetchType;
            import jakarta.persistence.Lob;
            import jakarta.persistence.Table;
            import jakarta.validation.constraints.NotNull;
            import jakarta.validation.constraints.Size;
            
            @Entity
            @Table(name = "test_entity_translations")
            public class TestEntityTranslation extends BaseTranslation {
                
                // ... parent field logic omitted for brevity in compiled check
                
                @Column(name = "name", length = 500, nullable = false, unique = true)
                @NotNull
                @Size(min = 5, max = 500)
                private String name;
                
                @Column(name = "description")
                @Lob
                @Basic(fetch = FetchType.LAZY)
                private String description;
                
                // ... getters setters
            }
            """
        );
        
        // Note: exact source comparison is tricky due to formatting, imports etc.
        // Instead we can inspect the generated file content for key strings
        assertThat(compilation)
            .generatedSourceFile("com.example.TestEntityTranslation")
            .hasSourceEquivalentTo(JavaFileObjects.forSourceString(
                 "com.example.TestEntityTranslation",
                    """
                    package com.example;
                    
                    import com.fasterxml.jackson.annotation.JsonIgnore;
                    import com.localizedjpa.runtime.BaseTranslation;
                    import jakarta.persistence.Basic;
                    import jakarta.persistence.Column;
                    import jakarta.persistence.Entity;
                    import jakarta.persistence.FetchType;
                    import jakarta.persistence.JoinColumn;
                    import jakarta.persistence.Lob;
                    import jakarta.persistence.ManyToOne;
                    import jakarta.persistence.Table;
                    import jakarta.validation.constraints.NotNull;
                    import jakarta.validation.constraints.Size;
                    import java.lang.String;
                    import org.hibernate.annotations.BatchSize;
                    
                    /**
                     * Generated translation entity for {@code TestEntity}.
                     *
                     * <p>This entity stores localized field values for each locale.
                     * Table: {@code test_entity_translations}
                     */
                    @Entity
                    @Table(
                        name = "test_entity_translations"
                    )
                    @BatchSize(
                        size = 25
                    )
                    public class TestEntityTranslation extends BaseTranslation {
                      @JsonIgnore
                      @ManyToOne(
                          fetch = FetchType.LAZY
                      )
                      @JoinColumn(
                          name = "test_entity_id",
                          nullable = false
                      )
                      private TestEntity parent;
                    
                      @Column(
                          name = "name",
                          length = 500,
                          nullable = false,
                          unique = true
                      )
                      @NotNull
                      @Size(
                          min = 5,
                          max = 500
                      )
                      private String name;
                    
                      @Column(
                          name = "description"
                      )
                      @Lob
                      @Basic(
                          fetch = FetchType.LAZY
                      )
                      private String description;
                    
                      public TestEntity getParent() {
                        return parent;
                      }
                    
                      public void setParent(TestEntity parent) {
                        this.parent = parent;
                      }
                    
                      public String getName() {
                        return name;
                      }
                    
                      public void setName(String name) {
                        this.name = name;
                      }
                    
                      public String getDescription() {
                        return description;
                      }
                    
                      public void setDescription(String description) {
                        this.description = description;
                      }
                    }
                    """
            ));
    }

    @Test
    void shouldPropagateSchemaAndCatalogToTranslationTable() {
        JavaFileObject entity = JavaFileObjects.forSourceString(
            "com.example.InventoryProduct",
            """
            package com.example;

            import com.localizedjpa.annotations.Localized;
            import jakarta.persistence.*;

            @Entity
            @Table(name = "inventory_product", schema = "app", catalog = "main")
            public class InventoryProduct {

                @Id
                @GeneratedValue
                private Long id;

                @Localized
                private String name;
            }
            """
        );

        Compilation compilation = javac()
            .withProcessors(new LocalizedProcessor())
            .compile(entity);

        assertThat(compilation).succeeded();

        assertThat(compilation)
            .generatedSourceFile("com.example.InventoryProductTranslation")
            .hasSourceEquivalentTo(JavaFileObjects.forSourceString(
                "com.example.InventoryProductTranslation",
                """
                // Generated by Localized JPA Compiler - do not modify
                package com.example;

                import com.fasterxml.jackson.annotation.JsonIgnore;
                import com.localizedjpa.runtime.BaseTranslation;
                import jakarta.persistence.Column;
                import jakarta.persistence.Entity;
                import jakarta.persistence.FetchType;
                import jakarta.persistence.JoinColumn;
                import jakarta.persistence.ManyToOne;
                import jakarta.persistence.Table;
                import java.lang.String;
                import org.hibernate.annotations.BatchSize;

                /**
                 * Generated translation entity for {@code InventoryProduct}.
                 *
                 * <p>This entity stores localized field values for each locale.
                 * Table: {@code inventory_product_translations}
                 */
                @Entity
                @Table(
                        name = "inventory_product_translations",
                        schema = "app",
                        catalog = "main"
                )
                @BatchSize(
                        size = 25
                )
                public class InventoryProductTranslation extends BaseTranslation {
                    @JsonIgnore
                    @ManyToOne(
                            fetch = FetchType.LAZY
                    )
                    @JoinColumn(
                            name = "inventory_product_id",
                            nullable = false
                    )
                    private InventoryProduct parent;

                    @Column(
                            name = "name"
                    )
                    private String name;

                    public InventoryProduct getParent() {
                        return parent;
                    }

                    public void setParent(InventoryProduct parent) {
                        this.parent = parent;
                    }

                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }
                }
                """
            ));
    }
    @Test
    void shouldUseExplicitColumnNameOverridesSnakeCase() {
        JavaFileObject entity = JavaFileObjects.forSourceString(
                "com.example.TestEntity",
                """
                package com.example;
                
                import com.localizedjpa.annotations.Localized;
                import jakarta.persistence.Entity;
                import jakarta.persistence.Id;
                import jakarta.persistence.Column;
                
                @Entity
                public class TestEntity {
                    @Id
                    private Long id;
                    
                    @Localized
                    @Column(name = "custom_address_field")
                    private String addressLine1;
                }
                """
        );

        Compilation compilation = javac()
                .withProcessors(new LocalizedProcessor())
                .compile(entity);

        assertThat(compilation).succeeded();

        JavaFileObject expectedTranslation = JavaFileObjects.forSourceString(
                "com.example.TestEntityTranslation",
                """
                package com.example;
                
                import com.fasterxml.jackson.annotation.JsonIgnore;
                import com.localizedjpa.runtime.BaseTranslation;
                import jakarta.persistence.Column;
                import jakarta.persistence.Entity;
                import jakarta.persistence.FetchType;
                import jakarta.persistence.JoinColumn;
                import jakarta.persistence.ManyToOne;
                import jakarta.persistence.Table;
                import java.lang.String;
                import org.hibernate.annotations.BatchSize;
                
                /**
                 * Generated translation entity for {@code TestEntity}.
                 *
                 * <p>This entity stores localized field values for each locale.
                 * Table: {@code test_entity_translations}
                 */
                @Entity
                @Table(
                    name = "test_entity_translations"
                )
                @BatchSize(
                    size = 25
                )
                public class TestEntityTranslation extends BaseTranslation {
                    @JsonIgnore
                    @ManyToOne(
                        fetch = FetchType.LAZY
                    )
                    @JoinColumn(
                        name = "test_entity_id",
                        nullable = false
                    )
                    private TestEntity parent;
                
                    @Column(
                        name = "custom_address_field"
                    )
                    private String addressLine1;
                
                    public TestEntity getParent() {
                        return parent;
                    }
                
                    public void setParent(TestEntity parent) {
                        this.parent = parent;
                    }
                
                    public String getAddressLine1() {
                        return addressLine1;
                    }
                
                    public void setAddressLine1(String addressLine1) {
                        this.addressLine1 = addressLine1;
                    }
                }
                """
        );

        assertThat(compilation)
                .generatedSourceFile("com.example.TestEntityTranslation")
                .hasSourceEquivalentTo(expectedTranslation);
    }
}

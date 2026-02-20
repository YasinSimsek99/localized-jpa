package com.localizedjpa.compiler;

/**
 * Holds table metadata extracted from entity @Table (name, schema, catalog).
 * Used to propagate schema/catalog to generated translation entities.
 */
public record TableInfo(String name, String schema, String catalog) {

    public static TableInfo of(String name, String schema, String catalog) {
        return new TableInfo(
            name != null ? name : "",
            schema != null ? schema : "",
            catalog != null ? catalog : ""
        );
    }
}

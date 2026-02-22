package com.localizedjpa.compiler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StringUtilsTest {

    @Test
    void toSnakeCase_NullOrEmpty_ReturnsSame() {
        assertNull(StringUtils.toSnakeCase(null));
        assertEquals("", StringUtils.toSnakeCase(""));
    }

    @Test
    void toSnakeCase_StandardCamelCase_ConvertsCorrectly() {
        assertEquals("address_line", StringUtils.toSnakeCase("addressLine"));
        assertEquals("first_name", StringUtils.toSnakeCase("firstName"));
        assertEquals("customer_billing_address", StringUtils.toSnakeCase("customerBillingAddress"));
    }

    @Test
    void toSnakeCase_WithNumbers_ConvertsCorrectly() {
        assertEquals("address_line1", StringUtils.toSnakeCase("addressLine1"));
        assertEquals("user2_profile", StringUtils.toSnakeCase("user2Profile"));
    }

    @Test
    void toSnakeCase_Abbreviations_ConvertsCorrectly() {
        assertEquals("user_id2_profile", StringUtils.toSnakeCase("userID2Profile"));
        assertEquals("xml_parser", StringUtils.toSnakeCase("XMLParser"));
        assertEquals("user_id", StringUtils.toSnakeCase("userID"));
        assertEquals("xml", StringUtils.toSnakeCase("XML"));
    }

    @Test
    void toSnakeCase_PascalCase_ConvertsCorrectly() {
        assertEquals("product_category", StringUtils.toSnakeCase("ProductCategory"));
        assertEquals("user", StringUtils.toSnakeCase("User"));
    }

    @Test
    void toSnakeCase_AlreadySnakeCase_ReturnsSame() {
        assertEquals("already_snake_case", StringUtils.toSnakeCase("already_snake_case"));
    }
}

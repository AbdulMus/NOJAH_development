package com.makeupbeauty;
import com.makeupbeauty.model.Product;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void testProductConstructorAndGetters() {
        // Create a product
        Product product = new Product(1, "Lipstick", "Maybelline", "Matte finish", "Makeup", "/uploads/lipstick.jpg");

        // Test constructor and getters
        assertEquals(1, product.getId());
        assertEquals("Lipstick", product.getName());
        assertEquals("Maybelline", product.getBrand());
        assertEquals("Matte finish", product.getDescription());
        assertEquals("Makeup", product.getCategory());
        assertEquals("/uploads/lipstick.jpg", product.getImage());
    }

    @Test
    void testSetters() {
        // Create a product
        Product product = new Product(1, "Lipstick", "Maybelline", "Matte finish", "Makeup", "/uploads/lipstick.jpg");

        // Use setters to update fields
        product.setId(2);
        product.setName("Mascara");
        product.setBrand("L'Oréal");
        product.setDescription("Volume boost");
        product.setCategory("Eyes");
        product.setImage("/uploads/mascara.jpg");

        // Test updated values
        assertEquals(2, product.getId());
        assertEquals("Mascara", product.getName());
        assertEquals("L'Oréal", product.getBrand());
        assertEquals("Volume boost", product.getDescription());
        assertEquals("Eyes", product.getCategory());
        assertEquals("/uploads/mascara.jpg", product.getImage());
    }
}
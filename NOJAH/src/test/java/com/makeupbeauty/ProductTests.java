package com.makeupbeauty;

import com.makeupbeauty.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductTests {
    // Private variable
    private Product product;

    // Function to create variables for each test case
    @BeforeEach
    void setUp() {
        ArrayList<String> labels = new ArrayList<>();
        labels.add("eco-friendly");
        labels.add("cruelty-free");
        product = new Product(999, "Lipstick", "Brand", "Description",
                "Category", "image.jpg", labels);
    }

    // Unit Tests ------------------------------------------------------------------------------------------------------
    @Test
    // Clear Box Test
    // Test Case for getLabelString function
    void testGetLabels() {
        String result = product.getLabelsString();
        assertTrue(result.contains("eco-friendly"));
        assertTrue(result.contains("cruelty-free"));
    }

    @Test
    // Clear Box Test
    // Test Case for product constructors
    void testProductInitialization() {
        assertEquals(999, product.getId());
        assertEquals("Lipstick", product.getName());
        assertEquals(2, product.getLabels().size());
    }

    // Integration Test ------------------------------------------------------------------------------------------------
    @Test
    // Translucent Box Test
    // Test Case for saveUpdateToCSV function
    public void testSaveUpdateToCSV() throws IOException {
        // Store previous catalog data
        Path path = Paths.get("src/main/resources/catalog.txt");
        List<String> originalLines = Files.readAllLines(path);

        try {
            // Save products to csv
            product.saveProductsToCSV();
            // Update test product
            List<String> updatedLabels = List.of("natural");
            product.setName("Updated Product");
            product.setBrand("Updated Brand");
            product.setDescription("Updated Description");
            product.setCategory("Updated Category");
            product.setImage("updatedImage.jpg");
            product.setLabels(updatedLabels);
            // Save update to csv
            product.saveUpdateToCSV();
            // Read all lines of the csv and find test product line
            List<String> updatedLines = Files.readAllLines(path);
            String updatedLine = updatedLines.stream()
                    .filter(line -> line.startsWith("999|,|")) // Find our product
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Product not found in CSV"));
            String[] values = updatedLine.split("\\|,\\|");
            // Test if product details were updated
            if (values[0].equalsIgnoreCase("999")){
                assertEquals(7, values.length, "CSV format incorrect");
                assertEquals("999", values[0]);
                assertEquals("Updated Product", values[1]);
                assertEquals("Updated Brand", values[2]);
                assertEquals("Updated Description", values[3]);
                assertEquals("Updated Category", values[4]);
                assertEquals("updatedImage.jpg", values[5]);
                assertTrue(values[6].contains("natural"));
            }

        } finally {
            // Rewrite catalog data
            Files.write(path, originalLines);
        }
    }

    // System Test -----------------------------------------------------------------------------------------------------
    @Test
    // Clear Box Test
    // Test Case for getName, setName, getDescription, setDescription and getLabels function
    void testCompleteProductLifecycle() {
        assertEquals("Lipstick", product.getName());

        product.setName("Updated Lipstick");
        assertEquals("Updated Lipstick", product.getName());

        product.setDescription("New description");
        assertEquals("New description", product.getDescription());

        assertTrue(product.getLabels().contains("eco-friendly"));
    }
}
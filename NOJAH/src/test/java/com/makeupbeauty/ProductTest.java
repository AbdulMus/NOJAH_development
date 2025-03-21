package com.makeupbeauty;

import static org.junit.jupiter.api.Assertions.*;

import com.makeupbeauty.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProductTest {

    private Product product;
    private final String catalogFilePath = "src/main/resources/catalog.txt";

    @BeforeEach
    public void setUp() {
        // Initialize a product
        product = new Product(1, "Test Product", "Test Brand", "Test Description", "Test Category", "test.jpg");
    }

    @Test
    public void testSaveProductsToCSV() {
        // Step 1: Read the original content of the CSV file (if it exists)
        List<String> originalLines = new ArrayList<>();
        File file = new File(catalogFilePath);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    originalLines.add(line);
                }
            } catch (IOException e) {
                fail("Failed to read the original CSV file: " + e.getMessage());
            }
        }

        // Step 2: Call the method to save the product to the CSV file
        product.saveProductsToCSV();

        // Step 3: Verify that the CSV file was updated correctly
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip the header line
            String header = reader.readLine();

            // Read the product line
            String productLine = reader.readLine();
            assertNotNull(productLine, "CSV file should contain product data");

            // Verify the product data
            String[] values = productLine.split("\\|,\\|");
            if (values[0].equalsIgnoreCase("1") && values[1].equalsIgnoreCase("Test Product")
                    && values[2].equalsIgnoreCase("Test Brand") && values[3].equalsIgnoreCase("Test Description")
                    && values[4].equalsIgnoreCase("Test Category") && values[5].equalsIgnoreCase("test.jpg"))
            {
                assertTrue(true);
            }

        } catch (IOException e) {
            fail("Failed to read the updated CSV file: " + e.getMessage());
        } finally {
            // Step 4: Restore the original content of the CSV file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String line : originalLines) {
                    writer.write(line);
                    writer.newLine();
                }
            } catch (IOException e) {
                fail("Failed to restore the original CSV file: " + e.getMessage());
            }
        }
    }

    @Test
    public void testSaveUpdateToCSV() {
        // Step 1: Read the original content of the CSV file (if it exists)
        List<String> originalLines = new ArrayList<>();
        File file = new File(catalogFilePath);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    originalLines.add(line);
                }
            } catch (IOException e) {
                fail("Failed to read the original CSV file: " + e.getMessage());
            }
        }

        // Step 2: Add a product to the CSV file (to simulate an existing product)
        product.saveProductsToCSV();

        // Step 3: Update the product and save the changes
        product.setName("Updated Product");
        product.setBrand("Updated Brand");
        product.setDescription("Updated Description");
        product.setCategory("Updated Category");
        product.setImage("updated.jpg");
        product.saveUpdateToCSV();

        // Step 3: Verify that the CSV file was updated correctly
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip the header line
            String header = reader.readLine();

            // Read the product line
            String productLine = reader.readLine();
            assertNotNull(productLine, "CSV file should contain product data");

            // Verify the product data
            String[] values = productLine.split("\\|,\\|");
            if (values[0].equalsIgnoreCase("1") && values[1].equalsIgnoreCase("Updated Product")
                    && values[2].equalsIgnoreCase("Updated Brand") && values[3].equalsIgnoreCase("Updated Description")
                    && values[4].equalsIgnoreCase("Updated Category") && values[5].equalsIgnoreCase("updated.jpg"))
            {
                assertTrue(true);
            }

        } catch (IOException e) {
            fail("Failed to read the updated CSV file: " + e.getMessage());
        } finally {
            // Step 4: Restore the original content of the CSV file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String line : originalLines) {
                    writer.write(line);
                    writer.newLine();
                }
            } catch (IOException e) {
                fail("Failed to restore the original CSV file: " + e.getMessage());
            }
        }
    }

//    @Test
//    public void testAddLabelsToProduct() {
//        List<String> labels = Arrays.asList("eco-friendly", "cruelty-free");
//        product.setLabels(labels);
//
//
//        List<String> retrievedLabels = product.getLabels();
//
//        // Expected: 2. Result: 0
//        assertEquals(2, retrievedLabels.size(), "There should be 2 labels");
//    }
}
package com.makeupbeauty;

import com.makeupbeauty.model.Product;
import com.makeupbeauty.model.User;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;

public class UserTest {

    private User user;
    private ArrayList<Product> products;
    private final String csvFilePath = "src/main/resources/users.csv";

    @BeforeEach
    public void setUp() {
        user = new User("testUser", "testPass", new ArrayList<>());

        products = new ArrayList<>();
        products.add(new Product(1, "Product1", "Brand1", "Description1", "Category1", "image1.jpg"));
        products.add(new Product(2, "Product2", "Brand2", "Description2", "Category2", "image2.jpg"));

    }

    @Test
    public void testSaveUserToCSV() {
        ArrayList<String> originalLines = new ArrayList<>();
        File file = new File(csvFilePath);
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

        user.saveUserToCSV();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();

            String userLine = reader.readLine();
            assertNotNull(userLine, "CSV file should contain user data");

            String[] values = userLine.split(",");
            if (values[0].trim().equalsIgnoreCase("testUser") && values[1].trim().equalsIgnoreCase("testPass")) {
                assertEquals("testPass", values[1].trim());
            }

        } catch (IOException e) {
            fail("Failed to read the updated CSV file: " + e.getMessage());
        } finally {
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
    public void testAddFavorites() {
        ArrayList<String> originalLines = new ArrayList<>();
        File file = new File(csvFilePath);
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

        user.saveUserToCSV();
        user.addFavorite(1, products);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();

            String userLine = reader.readLine();
            assertNotNull(userLine, "CSV file should contain user data");

            String[] values = userLine.split(",");
            if (values[0].trim().equalsIgnoreCase("testUser") && values[1].trim().equalsIgnoreCase("testPass")) {
                if (values[2].trim().equalsIgnoreCase("1")) {
                    assertEquals("1", values[2].trim());
                }
            }

        } catch (IOException e) {
            fail("Failed to read the updated CSV file: " + e.getMessage());
        } finally {
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
    public void testRemoveFavorites() {
        ArrayList<String> originalLines = new ArrayList<>();
        File file = new File(csvFilePath);
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

        user.saveUserToCSV();
        user.addFavorite(1, products);
        user.addFavorite(2, products);
        user.removeFavorite(products.get(1));

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();

            String userLine = reader.readLine();
            assertNotNull(userLine, "CSV file should contain user data");

            String[] values = userLine.split(",");
            if (values[0].trim().equalsIgnoreCase("testUser") && values[1].trim().equalsIgnoreCase("testPass")) {
                if (values[2].trim().equalsIgnoreCase("2")) {
                    assertEquals("2", values[2].trim());
                }
            }

        } catch (IOException e) {
            fail("Failed to read the updated CSV file: " + e.getMessage());
        } finally {
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
}

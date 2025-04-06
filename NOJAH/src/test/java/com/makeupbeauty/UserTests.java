package com.makeupbeauty;

import com.makeupbeauty.model.Product;
import com.makeupbeauty.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTests {
    // Private variables
    private User user;
    private Product testProduct;

    // Function to create variables for each test case
    @BeforeEach
    void setUp() {
        user = new User("testuser", "password", new ArrayList<>());
        testProduct = new Product(1, "Mascara", "Brand", "Desc",
                "Category", "image.jpg", new ArrayList<>());
    }

    // Unit Tests ------------------------------------------------------------------------------------------------------
    @Test
    // Clear Box Test
    // Test Case for addFavorite function
    void testAddFavorite() {
        // Add product to users favourites
        user.addFavorite(1, new ArrayList<>(List.of(testProduct)));
        // Test if users favourites list size increased by one
        assertEquals(1, user.getFavorites().size());
        // Test if users favourites list contains product
        assertEquals(testProduct, user.getFavorites().get(0));
    }

    @Test
    // Clear Box Test
    // Test Case for removeFavorite function
    void testRemoveFavorite() {
        // Add product to users favourites
        user.addFavorite(1, new ArrayList<>(List.of(testProduct)));
        // Remove product from users favourites
        user.removeFavorite(testProduct);
        // Test if users favourites list is now empty
        assertEquals(0, user.getFavorites().size());
    }

    @Test
    // Clear Box Test
    // Test Case for getFavorite function
    void testGetFavorite() {
        // Add product to users favourites
        user.addFavorite(1, new ArrayList<>(List.of(testProduct)));
        // Get list of users favourites
        Set<Integer> ids = user.getFavoriteProductIds();
        // Test if list contains product id
        assertTrue(ids.contains(1));
        // Test if list is the size of one
        assertEquals(1, ids.size());
    }

    // Integration Test ------------------------------------------------------------------------------------------------
    @Test
    // Translucent Box Test
    // Test Case for getStringFavorites function
    void testGetStringFavorites() {
        // Add product to users favorites
        user.addFavorite(1, new ArrayList<>(List.of(testProduct)));
        // Favourite String variable
        String favoritesString = user.getStringFavorites();
        // Test if the string contains the product id
        assertTrue(favoritesString.contains("1"));
    }

    // System Test -----------------------------------------------------------------------------------------------------
    @Test
    // Translucent Box Test
    // Test Case for addFavorite, removeFavorite and getPassword function
    void testCompleteUserLifecycle() {
        // Create user
        User newUser = new User("TestUser", "TestPass", new ArrayList<>());
        // Add product to users favourites
        newUser.addFavorite(1, new ArrayList<>(List.of(testProduct)));
        // Test if users favourites size is now one
        assertEquals(1, newUser.getFavorites().size());
        // Remove product from users favourites
        newUser.removeFavorite(testProduct);
        // Test if users favourites size is now 0
        assertEquals(0, newUser.getFavorites().size());
        // Test if users password is correct
        assertEquals("TestPass", newUser.getPassword());
    }
}
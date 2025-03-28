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
    private User user;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "password", new ArrayList<>());
        testProduct = new Product(1, "Mascara", "Brand", "Desc",
                "Category", "image.jpg", new ArrayList<>());
    }

    // Unit Tests ------------------------------------------------------------------------------------------------------
    @Test
    // Clear Box Test
    void testAddFavoriteAddsProduct() {
        user.addFavorite(1, new ArrayList<>(List.of(testProduct)));
        assertEquals(1, user.getFavorites().size());
        assertEquals(testProduct, user.getFavorites().get(0));
    }

    @Test
    // Clear Box Test
    void testRemoveFavoriteRemovesProduct() {
        user.addFavorite(1, new ArrayList<>(List.of(testProduct)));
        user.removeFavorite(testProduct);
        assertEquals(0, user.getFavorites().size());
    }

    @Test
    // Clear Box Test
    void testGetFavoriteProductIdsReturnsCorrectIds() {
        user.addFavorite(1, new ArrayList<>(List.of(testProduct)));
        Set<Integer> ids = user.getFavoriteProductIds();
        assertTrue(ids.contains(1));
        assertEquals(1, ids.size());
    }

    // Integration Test ------------------------------------------------------------------------------------------------
    @Test
    // Translucent Test Box
    void testAddFavoriteUpdatesStringRepresentation() {
        user.addFavorite(1, new ArrayList<>(List.of(testProduct)));
        String favoritesString = user.getStringFavorites();
        assertTrue(favoritesString.contains("1"));
    }

    // System Test -----------------------------------------------------------------------------------------------------
    @Test
    // Opaque Test Box
    void testCompleteUserLifecycle() {
        User newUser = new User("TestUser", "TestPass", new ArrayList<>());

        newUser.addFavorite(1, new ArrayList<>(List.of(testProduct)));
        assertEquals(1, newUser.getFavorites().size());

        newUser.removeFavorite(testProduct);
        assertEquals(0, newUser.getFavorites().size());

        assertEquals("TestPass", newUser.getPassword());
    }
}
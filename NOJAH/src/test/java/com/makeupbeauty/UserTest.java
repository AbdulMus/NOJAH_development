package com.makeupbeauty;

import com.makeupbeauty.model.Product;
import com.makeupbeauty.model.User;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserConstructorAndGetters() {
        ArrayList<Product> favorites = new ArrayList<>();
        User user = new User("testUser", "password123", favorites);

        assertEquals("testUser", user.getName());
        assertEquals("password123", user.getPassword());
        assertEquals(favorites, user.getFavorites());
    }

    @Test
    void testAddAndRemoveFavorite() {
        Product product = new Product(1, "Lipstick", "Maybelline", "Matte finish", "Makeup", "/uploads/lipstick.jpg");

        ArrayList<Product> favorites = new ArrayList<>();
        User user = new User("testUser", "password123", favorites);

        user.addFavorite(1, new ArrayList<>() {{
            add(product);
        }});

        assertTrue(user.getFavorites().contains(product));

        user.removeFavorite(product);

        assertFalse(user.getFavorites().contains(product));
    }

    @Test
    void testToString() {
        Product product = new Product(1, "Lipstick", "Maybelline", "Matte finish", "Makeup", "/uploads/lipstick.jpg");

        ArrayList<Product> favorites = new ArrayList<>() {{
            add(product);
        }};
        User user = new User("testUser", "password123", favorites);

        String expected = "Username: testUser, Password: password123, Favorites: \n" +
                "Product{id=1, name='Lipstick', brand='Maybelline', description='Matte finish', category='Makeup', image='/uploads/lipstick.jpg'}\n";
        assertEquals(expected, user.toString());
    }
}
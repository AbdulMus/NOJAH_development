package com.makeupbeauty;

import com.makeupbeauty.controller.HomeController;
import com.makeupbeauty.model.Product;
import com.makeupbeauty.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import java.io.IOException;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class HomeControllerTests {
    private HomeController controller;
    private MockHttpSession session;
    private Model model;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        controller = new HomeController();
        session = new MockHttpSession();
        model = new ExtendedModelMap();

        testProduct = new Product(999, "Test Product", "Test Brand",
                "Test Desc", "Test Cat", "test.jpg",
                new ArrayList<>());
        controller.getProducts().add(testProduct);

        controller.getUsers().put("TESTUSER",
                new User("TESTUSER", "password", new ArrayList<>()));
    }

    // Unit Tests ------------------------------------------------------------------------------------------------------
    @Test
    // Opaque Box Test
    void testFindProduct() {
        Product found = controller.findProduct(999);
        assertNotNull(found);
        assertEquals("Test Product", found.getName());
    }

    @Test
    // Opaque Box Test
    void testCheckUser() {
        session.setAttribute("user", "testuser");
        controller.checkUser(model, session);
        assertTrue((Boolean) model.getAttribute("isLoggedIn"));
    }

    // Integration Tests -----------------------------------------------------------------------------------------------
    @Test
    // Translucent Box Test
    void testAddFavourite() {
        session.setAttribute("user", "TESTUSER");
        controller.addFavorite(999, session);

        User user = controller.getUsers().get("TESTUSER");
        assertTrue(user.getFavorites().contains(testProduct));
    }

    @Test
    // Translucent Box Test
    void testAddProduct() throws IOException {
        session.setAttribute("user", "admin");
        MockMultipartFile mockImage = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());

        int initialProductSize = controller.getProducts().size();
        controller.addProduct("Name", "Brand", "Description", "Category", mockImage, "", session);

        assertEquals(initialProductSize + 1, controller.getProducts().size());
        int newId = controller.getProducts().getLast().getId();
        controller.deleteProduct(newId, session);
    }

    @Test
    // Opaque Box Test
    void testLogin() {
        String result = controller.login("admin", "123", session, model);
        assertEquals("redirect:/", result);
        assertEquals("admin", session.getAttribute("user"));
    }

    // System Tests ----------------------------------------------------------------------------------------------------
    @Test
    // Opaque Box Test
    void testWebsiteFlow() {
        String homePage = controller.home(null, null, model, session);
        assertEquals("index", homePage);

        String searchPage = controller.search("Test", null, null, null, model, session);
        assertEquals("search-results", searchPage);

        String productPage = controller.product(999, model, session);
        assertEquals("product", productPage);
    }

    @Test
    // Opaque Box Test
    void testAdminFlow() throws IOException {
        session.setAttribute("user", "admin");

        String adminPage = controller.adminPage(null, model, session);
        assertEquals("admin", adminPage);

        MockMultipartFile mockImage = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());
        controller.addProduct("AdminProduct", "Brand", "Desc", "Category", mockImage, "", session);

        int newId = controller.getProducts().getLast().getId();
        String updatePage = controller.showUpdateProductPage(newId, model, session);
        assertEquals("update-product", updatePage);
        controller.deleteProduct(newId, session);
    }
}
package com.makeupbeauty;

import com.makeupbeauty.controller.HomeController;
import com.makeupbeauty.model.Product;
import com.makeupbeauty.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HomeControllerTests {
    private HomeController controller;
    private MockHttpSession session;
    private Model model;
    private Product testProduct;
    private List<String> originalUserLines;
    private List<String> originalCatalogLines;
    private Path userPath;
    private Path catalogPath;


    @BeforeEach
    void setUp() {
        controller = new HomeController();
        session = new MockHttpSession();
        model = new ExtendedModelMap();

        userPath = Paths.get("src/main/resources/users.csv");
        catalogPath = Paths.get("src/main/resources/catalog.txt");

        try {
            originalUserLines = Files.readAllLines(userPath);
            originalCatalogLines = Files.readAllLines(catalogPath);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


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
    void testLoadUsers() {
        // loadUsers is already called upon when creating the controller
        assertFalse(controller.getUsers().isEmpty());

    }

    @Test
    // Opaque Box Test
    void testLoadProducts() {
        // loadProducts is already called upon when creating the controller
        assertFalse(controller.getProducts().isEmpty());
    }

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
    void testRemoveFavourite() {
        session.setAttribute("user", "TESTUSER");
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        User user = controller.getUsers().get("TESTUSER");

        controller.addFavorite(999, session);
        controller.removeFavorite(999, session, mockRequest);

        assertFalse(user.getFavorites().contains(testProduct));
    }

    @Test
    // Translucent Box Test
    void testShowFavourites() {
        session.setAttribute("user", "TESTUSER");
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        controller.addFavorite(999, session);

        Object favourites = model.getAttribute("favProducts");

        if (favourites != null){
            assertEquals("[Product{id=999, name='Test Product', brand='Test Brand'," +
                            " description='Test Desc', category='Test Cat', image='test.jpg', labels=[]}]",
                    favourites.toString());
        }
    }

    @Test
    // Translucent Box Test
    void testSaveImage() throws IOException {
        MockMultipartFile mockImage = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test.jpg".getBytes());
        controller.saveImage(mockImage);
        boolean foundImage = false;
        File uploadDir = new File("uploads/");
        if (uploadDir.exists()) {
            File[] files = uploadDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains("test.jpg")) {
                        foundImage = true;
                        break;
                    }
                }
            }
        }
        assertTrue(foundImage);
    }

    @Test
    // Translucent Box Test
    void testDeleteImage() throws IOException {
        MockMultipartFile mockImage = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test.jpg".getBytes());
        controller.saveImage(mockImage);
        StringBuilder fileName = new StringBuilder("/uploads/");
        boolean foundImage = false;
        File uploadDir = new File("uploads/");
        if (uploadDir.exists()) {
            File[] files = uploadDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains("test.jpg")) {
                        fileName.append(file.getName());
                        break;
                    }
                }
            }
        }
        controller.deleteImage(fileName.toString());
        if (uploadDir.exists()) {
            File[] files = uploadDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains("test.jpg")) {
                        foundImage = true;
                        break;
                    }
                }
            }
        }
        assertFalse(foundImage);
    }

    @Test
    // Opaque Box Test
    void testLoginForm() {
        String result = controller.loginForm();
        assertEquals("login", result);
    }

    @Test
    // Opaque Box Test
    void testLogin() {
        String result = controller.login("admin", "123", session, model);
        assertEquals("redirect:/", result);
        assertEquals("admin", session.getAttribute("user"));
    }

    @Test
    // Opaque Box Test
    void testLogout() {
        String result = controller.logout(session);
        assertEquals("redirect:/", result);
        assertTrue(session.isInvalid());
    }

    @Test
    // Opaque Box Text
    void testCreateAccountForm(){
        String result = controller.createAccountForm();
        assertEquals("create-account", result);
    }

    @Test
    // Opaque Box Test
    void testAddProduct(){
        session.setAttribute("user", "admin");
        MockMultipartFile mockImage = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());

        controller.addProduct("Name", "Brand", "Description", "Category", mockImage, "", session);
        assertEquals("Name", controller.getProducts().getLast().getName());
    }

    // Integration Tests -----------------------------------------------------------------------------------------------
    @Test
    // Translucent Box Test
    void testDeleteProduct() throws IOException {
        session.setAttribute("user", "admin");
        MockMultipartFile mockImage = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());

        int initialProductSize = controller.getProducts().size();
        controller.addProduct("Name", "Brand", "Description", "Category", mockImage, "", session);

        controller.deleteProduct(controller.getProducts().getLast().getId(), session);

        assertNotEquals("Name", controller.getProducts().getLast().getName());
    }

    @Test
    // Opaque Box Test
    void testFilterProducts() {
        controller.search("Test Product", null, null, null, model, session);
        ArrayList<Product> testProducts = new ArrayList<>();
        testProducts.add(testProduct);
        assertEquals(testProducts, model.getAttribute("searchResults"));

        HashSet<String> testCategories = new HashSet<>();
        testCategories.add(testProduct.getCategory());
        assertEquals(testCategories, model.getAttribute("categories"));
    }

    @Test
    // Translucent Box Test
    void testUpdateProduct() throws IOException {
        Path path = Paths.get("src/main/resources/catalog.txt");
        List<String> originalLines = Files.readAllLines(path);

        // When an admin updates a product
        session.setAttribute("user", "admin");

        // Creating Product
        MockMultipartFile mockImage = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());
        controller.addProduct("Name", "Brand", "Description", "Category", mockImage, "Label", session);
        Integer id = controller.getProducts().getLast().getId();

        // Updating Product
        MockMultipartFile updatedMockImage = new MockMultipartFile("new image", "updatedtest.jpg", "image/jpeg", "updatedTest".getBytes());
        controller.updateProduct(id, "Updated Name", "Updated Brand", "Updated Description", "Updated Category", updatedMockImage, "Updated Label", session);
        Product updatedProduct = controller.findProduct(id);

        // Checking if product details were updated
        assertEquals("Updated Name", updatedProduct.getName());
        assertEquals("Updated Brand", updatedProduct.getBrand());
        assertEquals("Updated Description", updatedProduct.getDescription());
        assertEquals("Updated Category", updatedProduct.getCategory());
        assertEquals("Updated Label;", updatedProduct.getLabelsString());
        assertTrue(updatedProduct.getImage().startsWith("/uploads/"));
        assertTrue(updatedProduct.getImage().contains("updatedtest.jpg"));
    }

    @Test
    // Translucent Box Test
    void testCreateAccount() throws IOException {
        Path path = Paths.get("src/main/resources/users.csv");
        List<String> originalLines = Files.readAllLines(path);
        String name = "Test Name", password = "T3sT Pass!";
        boolean foundUser = false;
        String result = controller.createAccount(name, password, password, model);
        for (User user: controller.getUsers().values()) {
            if (user.getName().equals(name) && user.getPassword().equals(password)) {
                foundUser = true;
                break;
            }
        }
        assertTrue(foundUser);
    }

    // System Tests ----------------------------------------------------------------------------------------------------
    @Test
    // Opaque Box Test
    void testWebsiteFlow() {
        String homePage = controller.home(null, null, null, model, session);
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
        controller.addProduct("AdminProduct", "Brand", "Desc", "Category", mockImage, "Eco-Friendly", session);

        int newId = controller.getProducts().getLast().getId();
        String updatePage = controller.showUpdateProductPage(newId, model, session);
        assertEquals("update-product", updatePage);
        controller.deleteProduct(newId, session);
    }

    @Test
    // Opaque Box Test
    void testUpdateProductFlow() throws IOException {
        // When an admin tries to update a false product
        session.setAttribute("user", "admin");
        String result3 = controller.updateProduct(125, "Updated Name", "Updated Brand", "Updated Desc", "Updated Cat", null, "", session);
        assertEquals("redirect:/admin", result3);

        // When a non-admin user tries to update a product
        session.setAttribute("user", "customer");
        String result4 = controller.updateProduct(999, "Updated Name", "Updated Brand", "Updated Desc", "Updated Cat", null, "", session);
        assertEquals("redirect:/login", result4);

        // When a guest user tries to update a product
        session.removeAttribute("user");
        String result5 = controller.updateProduct(999, "Updated Name", "Updated Brand", "Updated Desc", "Updated Cat", null, "", session);
        assertEquals("redirect:/login", result5);
    }

    @Test
    // Translucent Box Test
    void testCreateAccountFlow() throws IOException {
        // Creating an account
        String name1 = "Test name", password1 = "T3sT Pass!";
        controller.createAccount(name1, password1, password1, model);

        boolean foundUser = false;
        for (User user: controller.getUsers().values()) {
            if (user.getName().equals(name1) && user.getPassword().equals(password1)) {
                foundUser = true;
                break;
            }
        }
        assertTrue(foundUser);

        // Logging-in with account
        controller.login(name1, password1, session, model);
        assertEquals(name1, session.getAttribute("user"));

        // Favouriting a product
        controller.addFavorite(999, session);

        boolean foundFavourite = false;
        for (User user: controller.getUsers().values()) {
            if (user.getName().equals(name1) && user.getPassword().equals(password1)) {
                if (user.getFavorites().contains(testProduct)){
                    foundFavourite = true;
                    break;
                }
            }
        }
        assertTrue(foundFavourite);

        // Unfavouriting a product
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        controller.removeFavorite(999, session, mockRequest);

        boolean removedFavourite = true;
        for (User user: controller.getUsers().values()) {
            if (user.getName().equals(name1) && user.getPassword().equals(password1)) {
                if (user.getFavorites().contains(testProduct)){
                    removedFavourite = false;
                    break;
                }
            }
        }

        assertTrue(removedFavourite);
    }

    @Test
    // Translucent Box Test
    void testFalseCreateAccountFlow() throws IOException {
        // Creating an account with no username
        String name1 = "", password1 = "T3sT Pass!";
        String result1 = controller.createAccount(name1, password1, password1, model);
        assertEquals("create-account", result1);

        // Creating an account with different passwords
        String name2 = "", password2 = "T3sT Pass!";
        String result2 = controller.createAccount(name2, password2, "False Password", model);
        assertEquals("create-account", result2);

        // Creating an account with an already existing username
        Path path = Paths.get("src/main/resources/users.csv");
        List<String> originalLines = Files.readAllLines(path);
        String name3 = "Test Name", password3 = "T3sT Pass!";
        String result3 = controller.createAccount(name3, password3, password3, model);
        String result4 = controller.createAccount(name3, password3, password3, model);

        assertEquals("redirect:/login?success=Account+created+successfully", result3);
        assertEquals("create-account", result4);
    }

    // Clean Up --------------------------------------------------------------------------------------------------------
    @AfterEach
    void tearDown() throws IOException {
        File uploadDir = new File("uploads/");
        if (uploadDir.exists()) {
            File[] files = uploadDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains("test.jpg")){
                        file.delete();
                    }
                }
            }
        }

        Files.write(catalogPath, originalCatalogLines);
        Files.write(userPath, originalUserLines);
        controller.getProducts().removeIf(p -> p.getId() == 999);
    }
}
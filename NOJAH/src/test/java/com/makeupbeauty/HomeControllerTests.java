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
    // Private variables
    private HomeController controller;
    private MockHttpSession session;
    private Model model;
    private Product testProduct;
    private List<String> originalUserLines;
    private List<String> originalCatalogLines;
    private Path userPath;
    private Path catalogPath;


    // Function to create controllers, sessions and models for each test case, along with the current database data to use after each test case
    @BeforeEach
    void setUp() {
        // Create key instances
        controller = new HomeController();
        session = new MockHttpSession();
        model = new ExtendedModelMap();

        // Database paths
        userPath = Paths.get("src/main/resources/users.csv");
        catalogPath = Paths.get("src/main/resources/catalog.txt");

        try {
            // Read all lines in the current database for users and products
            originalUserLines = Files.readAllLines(userPath);
            originalCatalogLines = Files.readAllLines(catalogPath);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Create mock product
        testProduct = new Product(999, "Test Product", "Test Brand",
                "Test Desc", "Test Cat", "test.jpg",
                new ArrayList<>());
        // Add mock product to product list
        controller.getProducts().add(testProduct);

        // Add mock user to user list
        controller.getUsers().put("TESTUSER",
                new User("TESTUSER", "password", new ArrayList<>()));
    }

    // Unit Tests ------------------------------------------------------------------------------------------------------
    @Test
    // Opaque Box Test
    // Test Case for loadUser function
    void testLoadUsers() {
        // loadUsers is already called upon when creating the controller
        assertFalse(controller.getUsers().isEmpty());

    }

    @Test
    // Opaque Box Test
    // Test Case for loadProducts function
    void testLoadProducts() {
        // loadProducts is already called upon when creating the controller
        assertFalse(controller.getProducts().isEmpty());
    }

    @Test
    // Opaque Box Test
    // Test Case for findProduct function
    void testFindProduct() {
        // Product variable
        Product found = controller.findProduct(999);
        // Test its not null
        assertNotNull(found);
        // Test its the same product variable
        assertEquals("Test Product", found.getName());
    }

    @Test
    // Opaque Box Test
    // Test Case for checkUser function
    void testCheckUser() {
        // Set session attributes
        session.setAttribute("user", "testuser");
        // Check user credentials and add model attribute if logged in
        controller.checkUser(model, session);
        // Test the user is logged in
        assertTrue((Boolean) model.getAttribute("isLoggedIn"));
    }

    @Test
    // Translucent Box Test
    // Test Case for addFavourite function
    void testAddFavourite() {
        // Set session attributes
        session.setAttribute("user", "TESTUSER");
        // Add product to users favourites
        controller.addFavorite(999, session);
        // User variable
        User user = controller.getUsers().get("TESTUSER");
        // Test if product is in users favourites
        assertTrue(user.getFavorites().contains(testProduct));
    }

    @Test
    // Translucent Box Test
    // Test Case for removeFavourite function
    void testRemoveFavourite() {
        // Set session attributes
        session.setAttribute("user", "TESTUSER");
        // Mock HTTP request variable
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        // User variable
        User user = controller.getUsers().get("TESTUSER");
        // Add product to users favourites
        controller.addFavorite(999, session);
        // Remove product from users favourites
        controller.removeFavorite(999, session, mockRequest);
        // Test if product is in users favourites
        assertFalse(user.getFavorites().contains(testProduct));
    }

    @Test
    // Translucent Box Test
    // Test Case for showFavourites function
    void testShowFavourites() {
        // Set session attributes
        session.setAttribute("user", "TESTUSER");
        // Add product to users favourites
        controller.addFavorite(999, session);
        // Users favourite products variable
        Object favourites = model.getAttribute("favProducts");
        // Checks if variable is null
        if (favourites != null){
            // If not, test if the product matches the favourites
            assertEquals("[Product{id=999, name='Test Product', brand='Test Brand'," +
                            " description='Test Desc', category='Test Cat', image='test.jpg', labels=[]}]",
                    favourites.toString());
        }
    }

    @Test
    // Translucent Box Test
    // Test Case for saveImage function
    void testSaveImage() throws IOException {
        // Mock image variable
        MockMultipartFile mockImage = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test.jpg".getBytes());
        // Add image to directory using save image function
        controller.saveImage(mockImage);
        // Check if image was uploaded
        boolean foundImage = false;
        File uploadDir = new File("uploads/");
        if (uploadDir.exists()) {
            File[] files = uploadDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains("test.jpg")) {
                        // If it is, assign value to boolean and break loop
                        foundImage = true;
                        break;
                    }
                }
            }
        }
        // Test if boolean is true (image was saved)
        assertTrue(foundImage);
    }

    @Test
    // Translucent Box Test
    // Test Case for deleteImage function
    void testDeleteImage() throws IOException {
        // Mock image variable
        MockMultipartFile mockImage = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test.jpg".getBytes());
        // Add image to directory using saveImage function
        controller.saveImage(mockImage);
        // Check if image was uploaded
        StringBuilder fileName = new StringBuilder("/uploads/");
        boolean foundImage = false;
        File uploadDir = new File("uploads/");
        if (uploadDir.exists()) {
            File[] files = uploadDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains("test.jpg")) {
                        // If it was, save file name and break loop
                        fileName.append(file.getName());
                        break;
                    }
                }
            }
        }
        // Delete image from directory
        controller.deleteImage(fileName.toString());
        // Check if image is in directory
        if (uploadDir.exists()) {
            File[] files = uploadDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains("test.jpg")) {
                        // If it is, assign value to boolean and break loop
                        foundImage = true;
                        break;
                    }
                }
            }
        }
        // Test if boolean is false (image was not found)
        assertFalse(foundImage);
    }

    @Test
    // Test Case for loginForm function
    // Opaque Box Test
    void testLoginForm() {
        String result = controller.loginForm();
        assertEquals("login", result);
    }

    @Test
    // Opaque Box Test
    // Test Case for login function
    void testLogin() {
        String result = controller.login("admin", "123", session, model);
        assertEquals("redirect:/", result);
        assertEquals("admin", session.getAttribute("user"));
    }

    @Test
    // Opaque Box Test
    // Test Case for logout function
    void testLogout() {
        String result = controller.logout(session);
        assertEquals("redirect:/", result);
        assertTrue(session.isInvalid());
    }

    @Test
    // Opaque Box Text
    // Test Case for createAccountForm function
    void testCreateAccountForm(){
        String result = controller.createAccountForm();
        assertEquals("create-account", result);
    }

    @Test
    // Opaque Box Test
    // Test Case for addProduct function
    void testAddProduct(){
        // Set session attributes
        session.setAttribute("user", "admin");
        // Mock image variable
        MockMultipartFile mockImage = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());
        // Add product
        controller.addProduct("Name", "Brand", "Description", "Category", mockImage, "", session);
        // Test if product was added to list
        assertEquals("Name", controller.getProducts().getLast().getName());
    }

    // Integration Tests -----------------------------------------------------------------------------------------------
    @Test
    // Translucent Box Test
    // Test Case for deleteProduct function
    void testDeleteProduct() throws IOException {
        // Set session attributes
        session.setAttribute("user", "admin");
        // Mock image variable
        MockMultipartFile mockImage = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());
        // Add product
        controller.addProduct("Name", "Brand", "Description", "Category", mockImage, "", session);
        // Delete product
        controller.deleteProduct(controller.getProducts().getLast().getId(), session);
        // Test if product is not in list
        assertNotEquals("Name", controller.getProducts().getLast().getName());
    }

    @Test
    // Opaque Box Test
    // Test Case for filterProducts function
    void testFilterProducts() {
        // Search for a specific product
        controller.search("Test Product", null, null, null, model, session);
        ArrayList<Product> testProducts = new ArrayList<>();
        testProducts.add(testProduct);
        // Test if product was found in search result
        assertEquals(testProducts, model.getAttribute("searchResults"));

        HashSet<String> testCategories = new HashSet<>();
        testCategories.add(testProduct.getCategory());
        // Test if category was found in search result
        assertEquals(testCategories, model.getAttribute("categories"));
    }

    @Test
    // Translucent Box Test
    // Test Case for updateProduct function
    void testUpdateProduct() throws IOException {
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
    // Test Case for createAccount function
    void testCreateAccount() throws IOException {
        // Account name and password
        String name = "Test Name", password = "T3sT Pass!";
        boolean foundUser = false;
        // Create the account
        controller.createAccount(name, password, password, model);
        // Check if user is in controller users list
        for (User user: controller.getUsers().values()) {
            if (user.getName().equals(name) && user.getPassword().equals(password)) {
                foundUser = true;
                break;
            }
        }
        // Test if user was found
        assertTrue(foundUser);
    }

    // System Tests ----------------------------------------------------------------------------------------------------
    @Test
    // Opaque Box Test
    // Test Case for home, search and product function
    void testWebsiteFlow() {
        String homePage = controller.home(null, null, null, model, session);
        // Test if user was redirected properly
        assertEquals("index", homePage);

        String searchPage = controller.search("Test", null, null, null, model, session);
        // Test if user was redirected properly
        assertEquals("search-results", searchPage);

        String productPage = controller.product(999, model, session);
        // Test if user was redirected properly
        assertEquals("product", productPage);
    }

    @Test
    // Opaque Box Test
    // Test Case for adminPage, addProduct and showUpdateProductPage function
    void testAdminFlow() throws IOException {
        // Set session attributes
        session.setAttribute("user", "admin");
        String adminPage = controller.adminPage(null, model, session);
        // Test if user was redirected properly
        assertEquals("admin", adminPage);
        // Mock image file
        MockMultipartFile mockImage = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());
        // Add product
        controller.addProduct("AdminProduct", "Brand", "Desc", "Category", mockImage, "Eco-Friendly", session);
        int newId = controller.getProducts().getLast().getId();
        String updatePage = controller.showUpdateProductPage(newId, model, session);
        // Test if user was redirected properly
        assertEquals("update-product", updatePage);
        // Delete added product
        controller.deleteProduct(newId, session);
    }

    @Test
    // Opaque Box Test
    // Test Case for updateProduct function
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
    // Test Case for createAccount, login, addFavorite, and removeFavorite function
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
    // Test Case for createAccount function
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
        String name3 = "Test Name", password3 = "T3sT Pass!";
        String result3 = controller.createAccount(name3, password3, password3, model);
        String result4 = controller.createAccount(name3, password3, password3, model);

        assertEquals("redirect:/login?success=Account+created+successfully", result3);
        assertEquals("create-account", result4);
    }

    // Clean Up --------------------------------------------------------------------------------------------------------
    // Function to delete any test images and reset catalog and user files to how it was
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
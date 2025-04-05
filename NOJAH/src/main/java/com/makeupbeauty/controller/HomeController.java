package com.makeupbeauty.controller;

import com.makeupbeauty.model.Product;
import com.makeupbeauty.model.User;

import jakarta.servlet.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.*;
import java.nio.file.*;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    // In-memory storage for products and users
    private final ArrayList<Product> products = new ArrayList<>();
    private final HashMap<String, User> users = new HashMap<>();
    private final Map<String, Set<Integer>> userFavorites = new HashMap<>();
    
    // Logger for better error handling and debugging
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    // Constructor
    public HomeController() {
        loadProductsFromCSV("src/main/resources/catalog.txt");
        loadUsersFromCSV("src/main/resources/users.csv");
    }

    // Getter for products
    public List<Product> getProducts() {
        return this.products;
    }

    // Getter for users
    public HashMap<String, User> getUsers() {
        return users;
    }

    // Function to find product with product ID
    public Product findProduct(int productId) {
        Product foundProduct = null;

        // Search through product list
        for (Product p : products) {
            if (p.getId() == productId) {
                foundProduct = p;
                break;
            }
        }

        return foundProduct;
    }

    // Function to check if the user is logged in
    public void checkUser(Model model, HttpSession session) {
        // Checks if the user is logged in
        if (session.getAttribute("user") != null) {
            // If they are, add model attributes
            model.addAttribute("isLoggedIn", session.getAttribute("user") != null);
            model.addAttribute("username", session.getAttribute("user"));
        }

        // If user is admin and adds model attribute if they are
        model.addAttribute("isAdmin", "admin".equals(session.getAttribute("user")));
    }

    // USERS -----------------------------------------------------------------------------------------------------------

    // Function to load user data from CSV file
    public void loadUsersFromCSV(String filePath) {
        // Create BufferedReader to read CSV file
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Skip the header
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                String favorites = null;
                if (values.length >= 2) {
                    // Get user information
                    String name = values[0].trim().toUpperCase();
                    String password = values[1].trim();
                    if (values.length == 3) {
                        favorites = values[2].trim();
                    }

                    // Initialize empty list for favorite products
                    ArrayList<Product> favProducts = new ArrayList<>();

                    // Process favorites if they exist
                    if (favorites != null) {
                        String[] favoriteIds = favorites.split(";");
                        for (String favId : favoriteIds) {
                            int productId = Integer.parseInt(favId.trim());
                            Product foundProduct = findProduct(productId);
                            if (foundProduct != null) {
                                favProducts.add(foundProduct);
                            }
                        }
                    }

                    // Create and store user
                    User user = new User(name, password, favProducts);
                    users.put(name, user);

                    // Store favorite product IDs in userFavorites map
                    Set<Integer> favSet = new HashSet<>();
                    for (Product product : favProducts) {
                        favSet.add(product.getId());
                    }

                    // Add favourites list to users hashmap key
                    userFavorites.put(name, favSet);
                } else {
                    System.err.println("Failed to load" + line + " from CSV file");
                }
            }
        } catch (IOException e) {
            logger.error("Error reading CSV file: {}", e.getMessage(), e);
        }
    }


    // Function to add product to users favourites
    @PostMapping("/favorite")
    public String addFavorite(@RequestParam(required = false) Integer productId, HttpSession session) {
        // Checks if productID is empty
        if (productId == null) {
            logger.error("Product ID is null");
            return "redirect:/error?message=Product ID is missing";
        }

        // Get user instance
        User user = users.getOrDefault((((String) session.getAttribute("user")).toUpperCase()), null);

        // Checks if user is null
        if (user == null) {
            return "redirect:/login";
        }

        // Finds product using product id
        Product product = findProduct(productId);

        // Checks if Product is null
        if (product == null) {
            return "redirect:/error?message=Product not found";
        }

        // Adds product to users favourites
        user.addFavorite(productId, products);

        return "redirect:/product/" + productId + "?success=Added to favorites";
    }

    // Function to remove a favorite product
    @PostMapping("/unfavorite")
    public String removeFavorite(@RequestParam int productId, HttpSession session, HttpServletRequest request) {
        // Get username
        String username = (String) session.getAttribute("user");

        // Checks if username is null
        if (username == null) {
            return "redirect:/login";
        }

        // Get user instance
        User user = users.get(username.toUpperCase());

        // Variable to store the found product
        Product product = findProduct(productId);

        // Checks if product is null
        if (product == null) {
            return "redirect:/error?message=Product not found";
        }

        // Removes product from users favourites
        user.removeFavorite(product);

        // Get the referer header to determine where the request came from
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/my-favorites")) {
            return "redirect:/my-favorites?success=Removed+from+favorites";
        } else {
            return "redirect:/product/" + productId + "?success=Removed+from+favorites";
        }
    }


    // Function to show user's favorite products
    @GetMapping("/my-favorites")
    public String showFavorites(Model model, HttpSession session) {
        // Checks if the user is logged in
        if (session.getAttribute("user") != null) {
            checkUser(model, session);
        } else{
            return "redirect:/login";
        }

        // Gets user instance
        User user = users.get(Objects.requireNonNull(model.getAttribute("username")).toString().toUpperCase());

        // Adds users favourite products as a model attribute
        model.addAttribute("favProducts", user.getFavorites());
        return "favorites";
    }

    // PRODUCTS --------------------------------------------------------------------------------------------------------

    // Function to load product data from CSV or TXT
    public void loadProductsFromCSV(String filePath) {
        // Create BufferedReader to read file
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Skip header
            br.readLine();

            while ((line = br.readLine()) != null) {
                // Skip empty lines
                if (line.trim().isEmpty()) continue;
                String[] values = line.split("\\|,\\|");
                String labelsTxt = null;
                if (values.length >= 6) {
                    // Get product information
                    int id = Integer.parseInt(values[0].trim());
                    String name = values[1].trim();
                    String brand = values[2].trim();
                    String description = values[3].trim();
                    String category = values[4].trim();
                    String image = values[5].trim();

                    // Checks if products have labels
                    if (values.length == 7){
                        // Get label information
                        labelsTxt = values[6].trim();
                    }

                    // Create an arraylist to hold all labels
                    ArrayList<String> productLabels = new ArrayList<>();

                    // Process labels if they exist
                    if (labelsTxt != null) {
                        String[] labels = labelsTxt.split(";");
                        for (String l : labels) {
                            if (l != null) {
                                productLabels.add(l);
                            }
                        }
                    }

                    // Add product to list
                    products.add(new Product(id, name, brand, description, category, image, productLabels));
                } else {
                    System.err.println("Invalid Product Type: " + line);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            logger.error(String.valueOf(e));
        }
    }

    // Function to save an image to the directory
    public String saveImage(MultipartFile image) throws IOException {
        // Create variable for directory
        String uploadDir = "uploads/";
        // Create and initialize directory variable
        File directory = new File(uploadDir);
        // Check if directory exists
        if (!directory.exists()) {
            // If it doesn't, create one
            directory.mkdirs();
        }

        // Generate unique filename using timestamp
        String imageName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        String filePath = uploadDir + imageName;
        File file = new File(filePath);

        // Save the file
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(image.getBytes());
            fos.flush();
        }

        return "/uploads/" + imageName;
    }

    // Function to delete image from image path
    public void deleteImage(String imagePathToDelete) throws IOException {
        // Checks if the image path is empty
        if (imagePathToDelete != null) {
            // If It's not, use substring to remove any unnecessary slashes (/)
            imagePathToDelete = imagePathToDelete.substring(1);

            // Create image file variable
            File imageFile = new File(imagePathToDelete);
            // Check if image exists
            if (imageFile.exists() && imageFile.isFile()) {
                // Delete image and check if image was deleted
                if (imageFile.delete()) {
                    System.out.println("Image deleted successfully: " + imagePathToDelete);
                } else {
                    System.err.println("Failed to delete image: " + imagePathToDelete);
                }
            } else {
                System.err.println("Image not found: " + imagePathToDelete);
            }
        }
    }

    // Function to add a product
    @PostMapping("/add-product")
    public String addProduct(
            @RequestParam String name,
            @RequestParam String brand,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam("image") MultipartFile image,
            @RequestParam(name = "labels", required = false) String labelsString, // Changed to String
            HttpSession session) {

        // Check admin privileges
        if (session.getAttribute("user") == null || !"admin".equals(session.getAttribute("user"))) {
            return "redirect:/login";
        }

        // Validate form data
        if (name == null || name.isEmpty() || brand == null || brand.isEmpty() ||
                description == null || description.isEmpty() || category == null || category.isEmpty()) {
            return "redirect:/admin?error=Please fill in all fields";
        }

        // Check if the image doesn't exist
        if (image.isEmpty()) {
            return "redirect:/admin?error=Please upload an image";
        }

        try {
            // Save image and get image path
            String imagePath = saveImage(image);

            // Process labels - create empty list if none provided
            ArrayList<String> productLabels = new ArrayList<>();
            if (labelsString != null && !labelsString.trim().isEmpty()) {
                // Split by semicolon and filter out empty strings
                productLabels = Arrays.stream(labelsString.split(";"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toCollection(ArrayList::new));
            }

            // Get new product id from product list size + 1
            int newId = products.size() + 1;
            // Create new product
            Product newProduct = new Product(newId, name, brand, description, category, imagePath, productLabels);
            // Add new product to list
            products.add(newProduct);
            // Save product to TXT file
            newProduct.saveProductsToCSV();

        } catch (IOException e) {
            logger.error(String.valueOf(e));
            return "redirect:/admin?error=Failed to upload image. Please try again.";
        }

        return "redirect:/admin?success=Product added successfully";
    }

    // Function to delete product
    @PostMapping("/delete-product")
    public String deleteProduct(@RequestParam int id, HttpSession session) throws IOException {
        // Check admin privileges
        if (session.getAttribute("user") == null || !"admin".equals(session.getAttribute("user"))) {
            return "redirect:/login";
        }

        // Find product id of soon-to-be removed product
        Product removedProduct = findProduct(id);

        // Remove product from list if it matches with the product id
        products.removeIf(product -> product.getId() == id);

        // Create filepath variable
        String filepath = "src/main/resources/catalog.txt";
        // Create StringBuilder for updated file content
        StringBuilder updatedContent = new StringBuilder();
        // Store image path to delete later
        String imagePathToDelete = null;

        // Create new BufferedReader to read file
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            // Read and store the header
            String header = br.readLine();
            updatedContent.append(header).append("\n");

            String line;
            while ((line = br.readLine()) != null) {
                // Split product values and store them
                String[] values = line.split("\\|,\\|");
                String id1 = values[0].trim();
                String imagePath = values[5].trim();
                // If the product id matches
                if (id == Integer.parseInt(id1)) {
                    // Save image path for deletion
                    imagePathToDelete = imagePath;
                    // Skip writing this line
                    continue;
                }
                // Store the line
                updatedContent.append(line).append("\n");
            }
        } catch (Exception _) {
        }

        // Write the updated content back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            String trimmedContent = updatedContent.toString().trim();
            writer.write(trimmedContent);
        } catch (IOException e) {
            logger.error(String.valueOf(e));
        }

        // Delete the image file if it exists
        if (imagePathToDelete != null) {
            deleteImage(imagePathToDelete);
        }

        // Check each user in the user keyset
        for (String user : users.keySet()) {
            // Check if the user has favourited the removed product
            if (users.get(user).getFavorites().contains(removedProduct)) {
                // Remove product from users favourites
                users.get(user).removeFavorite(removedProduct);
            }
        }

        return "redirect:/admin";
    }

    // Function to show update product page
    @GetMapping("/update-product/{id}")
    public String showUpdateProductPage(@PathVariable int id, Model model, HttpSession session) {
        // Check if user is logged in
        checkUser(model, session);
        // Check admin privileges
        if (session.getAttribute("user") == null || !"admin".equals(session.getAttribute("user"))) {
            return "redirect:/login";
        }

        // Find product by ID
        Product foundProduct = null;
        for (Product product : products) {
            if (product.getId() == id) {
                foundProduct = product;
                break;
            }
        }

        // If product is not found, redirect back to admin
        if (foundProduct == null) {
            return "redirect:/admin";
        }

        // Add all the labels to a model attribute
        getLabels(model);
        // Add the found product to a model attribute
        model.addAttribute("product", foundProduct);

        return "update-product";
    }

    // Function to update product
    @PostMapping("/update-product/{id}")
    public String updateProduct(@PathVariable int id,
                                @RequestParam String name,
                                @RequestParam String brand,
                                @RequestParam String description,
                                @RequestParam String category,
                                @RequestParam(required = false) MultipartFile image,
                                @RequestParam(name = "labels", required = false) String labelsString,
                                HttpSession session) throws IOException {

        // Check admin privileges
        if (session.getAttribute("user") == null || !"admin".equals(session.getAttribute("user"))) {
            return "redirect:/login";
        }

        // Checks each product in list
        for (Product product : products) {
            // Checks if ids match
            if (product.getId() == id) {
                // Update basic product info
                product.setName(name);
                product.setBrand(brand);
                product.setDescription(description);
                product.setCategory(category);

                // Process and update labels
                ArrayList<String> productLabels = new ArrayList<>();
                if (labelsString != null && !labelsString.trim().isEmpty()) {
                    // Split by semicolon and filter out empty strings
                    productLabels = Arrays.stream(labelsString.split(";"))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toCollection(ArrayList::new));
                }
                product.setLabels(productLabels);

                // Update image if provided
                if (image != null && !image.isEmpty()) {
                    // Delete the old image
                    deleteImage(product.getImage());
                    String imagePath = saveImage(image);
                    product.setImage(imagePath);
                }

                // Save changes to TXT file
                product.saveUpdateToCSV();
                break;
            }
        }

        return "redirect:/admin";
    }

    // Function to display personal product page
    @GetMapping("/product/{id}")
    public String product(@PathVariable int id, Model model, HttpSession session) {
        // Empty product variable
        Product foundProduct = null;
        // Search through product list
        for (Product product : products) {
            // Checks if ids matches
            if (product.getId() == id) {
                // Assigns found product to previous product variable
                foundProduct = product;
                break;
            }
        }

        // Checks if foundProduct is not null
        if (foundProduct != null) {
            model.addAttribute("product", foundProduct);
            checkUser(model, session);

            // Get user's favorite product IDs
            String username = (String) session.getAttribute("user");
            if (username != null) {
                User user = users.get(username.toUpperCase());
                if (user != null) {
                    Set<Integer> userFavorites = user.getFavoriteProductIds();
                    model.addAttribute("userFavorites", userFavorites);
                }
            }

            return "product";
        } else {
            return "redirect:/";
        }
    }

    // LOGIN/LOGOUT/CREATE ACCOUNT -------------------------------------------------------------------------------------

    // Function to display login page
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    // Function to handle logins
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        // Get user from username
        User user = users.get(username.toUpperCase());

        // Check if user is not null and if the passwords match
        if (user != null && user.getPassword().equals(password)) {
            // Check if user is admin
            if (user.getName().equalsIgnoreCase("admin")){
                session.setAttribute("user", username);
                return "redirect:/";
            }

            // Set username to a session attribute
            session.setAttribute("user", username);

            // Check if user is contained within the userFavorites keyset
            if (!userFavorites.containsKey(username)) {
                // If not, add it
                userFavorites.put(username, new HashSet<>());
            }

            return "redirect:/";
        }

        // Add error to a model attribute
        model.addAttribute("error", "Invalid username or password.");
        return "login";
    }

    // Function to handle logouts
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // End session
        session.invalidate();
        return "redirect:/";
    }

    // Function to display account creation page
    @GetMapping("/create-account")
    public String createAccountForm() {
        return "create-account";
    }

    // Function to create an account
    @PostMapping("/create-account")
    public String createAccount(@RequestParam String username, @RequestParam String password, @RequestParam String confirmPassword,
                                Model model) {

        // Checks if username and password are empty
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            // If so, adds error to a model attribute
            model.addAttribute("error", "Username and password are required.");
            return "create-account";
        }

        // Checks if password matches the criteria
        if (!(password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[!@#$%^&*(),.?\":{}|<>].*"))){
            // If not, adds error to a model attribute
            model.addAttribute("error", "Password must contain: 8+ characters, An Uppercase Letter, A Lowercase Letter, A Number, A Special Character");
            return "create-account";
        }

        // Check if password and confirm password matches
        if (!password.equals(confirmPassword)) {
            // If not, adds error to a model attribute
            model.addAttribute("error", "Passwords do not match.");
            return "create-account";
        }

        // Check if the username already exists
        if (users.containsKey(username.toUpperCase())) {
            // If so, adds error to a model attribute
            model.addAttribute("error", "Username already exists.");
            return "create-account";
        }

        // Create a new user
        User newUser = new User(username, password, new ArrayList<>());
        // Add user to user hashmap
        users.put(username.toUpperCase(), newUser);

        // Save the new user to the CSV file
        newUser.saveUserToCSV();

        return "redirect:/login?success=Account+created+successfully";
    }

    // REST CONTROLLER -------------------------------------------------------------------------------------------------

    // Controller for handling file uploads
    @RestController
    public static class FileController {
        private static final String UPLOAD_DIR = "uploads/";

        // Servers uploaded files
        @GetMapping("/uploads/{filename:.+}")
        @ResponseBody
        public Resource serveFile(@PathVariable String filename) {
            try {
                Path filePath = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
                return new UrlResource(filePath.toUri());
            } catch (Exception e) {
                throw new RuntimeException("File not found: " + filename);
            }
        }
    }

    // HOME PAGE -------------------------------------------------------------------------------------------------------

    // Function to display homepage based on filters or searches
    @GetMapping("/")
    public String home(@RequestParam(required = false) String category,
                       @RequestParam(required = false) String brand,
                       @RequestParam(required = false) List<String> label,
                       Model model,
                       HttpSession session) {

        // Variables to hold filtered options/products
        ArrayList<Product> filteredResults = new ArrayList<>();
        Set<String> filteredCategories = new HashSet<>();
        Set<String> filteredBrands = new HashSet<>();
        Set<String> filteredLabels = new HashSet<>();

        // Filter products based on criteria
        for (Product product : products) {
            boolean matchesCategory = (category == null || category.isEmpty()) || product.getCategory().equalsIgnoreCase(category);
            boolean matchesBrand = (brand == null || brand.isEmpty()) || product.getBrand().equalsIgnoreCase(brand);
            boolean matchesLabel = true;
            
            // Check if product matches all selected labels
            if (!(label == null || label.isEmpty())) {
                for (String labelName : label) {
                    if (!product.getLabels().contains(labelName)) {
                        matchesLabel = false;
                        break;
                    }
                }
            }

            // Check if all filters match
            if (matchesCategory && matchesBrand && matchesLabel) {
                // If so, add it to the previously created variables
                filteredResults.add(product);
                filteredCategories.add(product.getCategory());
                filteredBrands.add(product.getBrand());
                filteredLabels.addAll(product.getLabels());
            }
        }

        // Check if user is logged in
        checkUser(model, session);
        // Add attributes to model
        model.addAttribute("filteredResults", filteredResults);
        model.addAttribute("categories", filteredCategories);
        model.addAttribute("brands", filteredBrands);
        model.addAttribute("labels", filteredLabels);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedBrand", brand);
        model.addAttribute("selectedLabels", label != null ? label : Collections.emptyList());

        return "index";
    }

    // SEARCH PAGE -----------------------------------------------------------------------------------------------------

    // Function to display products based on search or filters
    @GetMapping("/search")
    public String search(@RequestParam String query,
                         @RequestParam(required = false) String category,
                         @RequestParam(required = false) String brand,
                         @RequestParam(required = false) List<String> label,
                         Model model, HttpSession session) {

        // Variables to hold filtered options/products
        query = query.toLowerCase();
        ArrayList<Product> searchResults = new ArrayList<>();
        Set<String> filteredCategories = new HashSet<>();
        Set<String> filteredBrands = new HashSet<>();
        Set<String> filteredLabels = new HashSet<>();

        // Filter products based on criteria
        for (Product product : products) {
            boolean matchesQuery = product.getName().toLowerCase().contains(query) ||
                    product.getBrand().toLowerCase().contains(query) ||
                    product.getDescription().toLowerCase().contains(query) ||
                    product.getCategory().toLowerCase().contains(query);

            boolean matchesCategory = (category == null || category.isEmpty()) || product.getCategory().equalsIgnoreCase(category);
            boolean matchesBrand = (brand == null || brand.isEmpty()) || product.getBrand().equalsIgnoreCase(brand);
            boolean matchesLabel = true;

            // Check if product matches all selected labels
            if (!(label == null || label.isEmpty())) {
                for (String labelName : label) {
                    if (!product.getLabels().contains(labelName)) {
                        matchesLabel = false;
                        break;
                    }
                }
            }

            // Check if all filters match
            if (matchesQuery && matchesCategory && matchesBrand && matchesLabel) {
                // If so, add it to the previously created variables
                searchResults.add(product);
                filteredCategories.add(product.getCategory());
                filteredBrands.add(product.getBrand());
                filteredLabels.addAll(product.getLabels());
            }
        }

        // Check if user is logged in
        checkUser(model, session);
        // Add attributes to model
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("query", query);
        model.addAttribute("categories", filteredCategories);
        model.addAttribute("brands", filteredBrands);
        model.addAttribute("labels", filteredLabels);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedBrand", brand);
        model.addAttribute("selectedLabels", label != null ? label : Collections.emptyList());

        return "search-results";
    }

    // ADMIN PAGE ------------------------------------------------------------------------------------------------------

    // Function to display list of products based on search in admin dashboard
    @GetMapping("/admin")
    public String adminPage(@RequestParam(required = false) String query, Model model, HttpSession session) {

        // Check admin privileges
        if (session.getAttribute("user") == null || !"admin".equals(session.getAttribute("user"))) {
            return "redirect:/login";
        }

        // Check if user is logged in
        checkUser(model, session);

        // Handle null query properly
        if (query == null || query.trim().isEmpty()) {
            // Set to an empty string to avoid null pointer issues
            query = "";
        } else {
            query = query.toLowerCase();
        }

        // Variable to hold all searched products
        ArrayList<Product> searchResults = new ArrayList<>();
        // Check each product in list
        for (Product product : products) {
            // Boolean to see if product matches the query
            boolean matchesQuery = product.getName().toLowerCase().contains(query) ||
                    product.getBrand().toLowerCase().contains(query) ||
                    product.getDescription().toLowerCase().contains(query) ||
                    product.getCategory().toLowerCase().contains(query);
            // Check if product matches the query
            if (matchesQuery) {
                // If it does, add it to previously created variable
                searchResults.add(product);
            }
        }

        // Add search result product list as a model attribute
        model.addAttribute("products", searchResults);
        // Get all labels and add them to a model attribute
        getLabels(model);
        return "admin";
    }

    // Function to get all product labels and add them to a model attribute
    private void getLabels(Model model) {
        // Variable to hold all labels
        ArrayList<String> allLabels = new ArrayList<>();
        // Check each product in list
        for (Product product : products) {
            // Check if product has labels
            if (product.getLabels() != null && !product.getLabels().isEmpty()) {
                // Check each label in products label list
                for (String label : product.getLabels()) {
                    // Check if product label isn't already in the all-label variable
                    if (!allLabels.contains(label)) {
                        // If so, add it to it
                        allLabels.add(label);
                    }
                }
            }
        }
        // Add all labels as a model attribute
        model.addAttribute("labels", allLabels);
    }
}
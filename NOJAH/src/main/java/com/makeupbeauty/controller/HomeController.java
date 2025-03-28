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

    private final ArrayList<Product> products = new ArrayList<>();
    private final HashMap<String, User> users = new HashMap<>(); // Store User objects
    private final Map<String, Set<Integer>> userFavorites = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class); // <------- this is better than printStackTrace()

    // constructor
    public HomeController() {
        loadProductsFromCSV("src/main/resources/catalog.txt");
        loadUsersFromCSV("src/main/resources/users.csv");
    }

    private Product findProduct(int productId) {
        Product foundProduct = null;

        // Loop through each product in the products list
        for (Product p : products) {
            // Check if the current product's ID matches the productId
            if (p.getId() == productId) {
                // If a match is found, store the product and exit the loop
                foundProduct = p;
                break;
            }
        }

        // After the loop, foundProduct will either contain the matching product or remain null
        return foundProduct;
    }

    public void checkUser(Model model, HttpSession session) {
        if (session.getAttribute("user") != null) {
            model.addAttribute("isLoggedIn", session.getAttribute("user") != null);
            model.addAttribute("username", session.getAttribute("user"));
        }
        model.addAttribute("isAdmin", "admin".equals(session.getAttribute("user")));
    }

    // USERS -----------------------------------------------------------------------------------------------------------


    public void loadUsersFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                String favorites = null;
                if (values.length >= 2) {
                    String name = values[0].trim().toUpperCase();
                    String password = values[1].trim();
                    if (values.length == 3) {
                        favorites = values[2].trim();
                    }

                    // An empty list to store favorite products
                    ArrayList<Product> favProducts = new ArrayList<>();

                    // Check if the favorites string is not empty
                    if (favorites != null) {
                        // Split the favorites string into individual product IDs
                        String[] favoriteIds = favorites.split(";");

                        // Loop through each product ID
                        for (String favId : favoriteIds) {
                            // Convert the product ID from String to int
                            int productId = Integer.parseInt(favId.trim());

                            // Step 6: Search for the product in the products list
                            Product foundProduct = null;
                            for (Product p : products) {
                                if (p.getId() == productId) {
                                    // If found, store the product
                                    foundProduct = p;
                                    // Exit the loop once the product is found
                                    break;
                                }
                            }

                            // Step 8: If the product was found, add it to the favProducts list
                            if (foundProduct != null) {
                                favProducts.add(foundProduct);
                            }
                        }
                    }

                    User user = new User(name, password, favProducts);
                    users.put(name, user);

                    // Load favorites into userFavorites map
                    Set<Integer> favSet = new HashSet<>();
                    // Loop through each product in the favProducts list
                    for (Product product : favProducts) {
                        // Add the product ID to the Set
                        favSet.add(product.getId());
                    }
                    userFavorites.put(name, favSet);
                } else {
                    System.err.println("Failed to load" + line + " from CSV file");
                }
            }
        } catch (IOException e) {
            logger.error("Error reading CSV file: {}", e.getMessage(), e);
        }
    }

    // Add a favorite product
    @PostMapping("/favorite")
    public String addFavorite(@RequestParam(required = false) Integer productId, HttpSession session) {
        if (productId == null) {
            logger.error("Product ID is null");
            return "redirect:/error?message=Product ID is missing";
        }

        User user = users.getOrDefault((((String) session.getAttribute("user")).toUpperCase()), null);

        if (user == null) {
            return "redirect:/login";
        }

        // Variable to store the found product
        Product product = findProduct(productId);

        if (product == null) {
            return "redirect:/error?message=Product not found";
        }

        user.addFavorite(productId, products);

        return "redirect:/product/" + productId + "?success=Added to favorites";
    }

    // Remove a favorite product
    @PostMapping("/unfavorite")
    public String removeFavorite(@RequestParam int productId, HttpSession session, HttpServletRequest request) {
        String username = (String) session.getAttribute("user");

        if (username == null) {
            return "redirect:/login";
        }
        User user = users.get(username.toUpperCase());
        // Variable to store the found product
        Product product = findProduct(productId);

        if (product == null) {
            return "redirect:/error?message=Product not found";
        }

        user.removeFavorite(product);

        // Get the referer header to determine where the request came from
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/my-favorites")) {
            return "redirect:/my-favorites?success=Removed+from+favorites";
        } else {
            return "redirect:/product/" + productId + "?success=Removed+from+favorites";
        }    }


    // Show user's favorite products
    @GetMapping("/my-favorites")
    public String showFavorites(Model model, HttpSession session) {
        if (session.getAttribute("user") != null) {
            checkUser(model, session);
        }else{
            return "redirect:/login";
        }

        User user = users.get(Objects.requireNonNull(model.getAttribute("username")).toString().toUpperCase());

        model.addAttribute("favProducts", user.getFavorites());
        return "favorites";
    }

    // PRODUCTS --------------------------------------------------------------------------------------------------------

    public void loadProductsFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // skip empty lines
                String[] values = line.split("\\|,\\|");
                String labelsTxt = null;
                if (values.length >= 6) {
                    int id = Integer.parseInt(values[0].trim());
                    String name = values[1].trim();
                    String brand = values[2].trim();
                    String description = values[3].trim();
                    String category = values[4].trim();
                    String image = values[5].trim();

                    if (values.length == 7){
                        labelsTxt = values[6].trim();
                    }

                    ArrayList<String> productLabels = new ArrayList<>();

                    // Check if the favorites string is not empty
                    if (labelsTxt != null) {
                        // Split the favorites string into individual product IDs
                        String[] labels = labelsTxt.split(";");

                        // Loop through each product ID
                        for (String l : labels) {
                            // Step 8: If the product was found, add it to the favProducts list
                            if (l != null) {
                                productLabels.add(l);
                            }
                        }
                    }

                    products.add(new Product(id, name, brand, description, category, image, productLabels));
                } else {
                    System.err.println("Invalid Product Type: " + line);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            logger.error(String.valueOf(e));
        }

        System.out.println(products);
    }

    public String saveImage(MultipartFile image) throws IOException {
        String uploadDir = "uploads/";
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Generate a unique filename
        String imageName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        String filePath = uploadDir + imageName;
        File file = new File(filePath);

        // Save file
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(image.getBytes());
            fos.flush();
        }

        return "/uploads/" + imageName;  // Return relative path for web access
    }

    public void deleteImage(String imagePathToDelete) throws IOException {
        if (imagePathToDelete != null) {
            imagePathToDelete = imagePathToDelete.substring(1);

            File imageFile = new File(imagePathToDelete);
            if (imageFile.exists() && imageFile.isFile()) {
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

        if (image.isEmpty()) {
            return "redirect:/admin?error=Please upload an image";
        }

        try {
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

            int newId = products.size() + 1;
            Product newProduct = new Product(newId, name, brand, description, category, imagePath, productLabels);
            products.add(newProduct);
            newProduct.saveProductsToCSV();

        } catch (IOException e) {
            logger.error(String.valueOf(e));
            return "redirect:/admin?error=Failed to upload image. Please try again.";
        }

        return "redirect:/admin?success=Product added successfully";
    }

    @PostMapping("/delete-product")
    public String deleteProduct(@RequestParam int id, HttpSession session) throws IOException {
        if (session.getAttribute("user") == null || !"admin".equals(session.getAttribute("user"))) {
            return "redirect:/login";
        }

        Product removedProduct = findProduct(id);

        products.removeIf(product -> product.getId() == id);

        String filepath = "src/main/resources/catalog.txt";
        StringBuilder updatedContent = new StringBuilder();
        String imagePathToDelete = null; // Store image path to delete later

        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String header = br.readLine(); // Read and store the header
            updatedContent.append(header).append("\n"); // Ensure header is preserved

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split("\\|,\\|");
                String id1 = values[0].trim();
                String imagePath = values[5].trim(); // Assuming image path is stored in column 6
                if (id == Integer.parseInt(id1)) {
                    System.out.println(imagePath);
                    imagePathToDelete = imagePath; // Save image path for deletion
                    continue; // Skip writing this line
                }
                updatedContent.append(line).append("\n");
            }
        } catch (Exception e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            logger.error(String.valueOf(e));
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

        for (String user : users.keySet()) {
            if (users.get(user).getFavorites().contains(removedProduct)) {
                users.get(user).removeFavorite(removedProduct);
            }
        }

        return "redirect:/admin";
    }

    @GetMapping("/update-product/{id}")
    public String showUpdateProductPage(@PathVariable int id, Model model, HttpSession session) {
        checkUser(model, session);
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

        model.addAttribute("product", foundProduct);
        return "update-product"; // Renders update-product.html
    }


    @PostMapping("/update-product/{id}")
    public String updateProduct(@PathVariable int id,
                                @RequestParam String name,
                                @RequestParam String brand,
                                @RequestParam String description,
                                @RequestParam String category,
                                @RequestParam(required = false) MultipartFile image,
                                HttpSession session) throws IOException {

        if (session.getAttribute("user") == null || !"admin".equals(session.getAttribute("user"))) {
            return "redirect:/login";
        }

        for (Product product : products) {
            if (product.getId() == id) {
                product.setName(name);
                product.setBrand(brand);
                product.setDescription(description);
                product.setCategory(category);

                if (image != null && !image.isEmpty()) {
                    // Delete the old image
                    deleteImage(product.getImage());  // Pass the current image path from the product
                    String imagePath = saveImage(image);  // Save the new image and get the path
                    product.setImage(imagePath);  // Update the product's image path
                }
                product.saveUpdateToCSV();
                break;
            }
        }

        return "redirect:/admin";
    }

    @GetMapping("/product/{id}")
    public String product(@PathVariable int id, Model model, HttpSession session) {
        Product foundProduct = null;
        for (Product product : products) {
            if (product.getId() == id) {
                foundProduct = product;
                System.out.println("Product Labels: " + product.getLabels()); // Debug output
                break;
            }
        }

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

            return "product"; // Use the new template
        } else {
            return "redirect:/"; // Redirect if product not found
        }
    }

    // LOGIN/LOGOUT/CREATE ACCOUNT -------------------------------------------------------------------------------------

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    // Handle login
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
//        if (username.equals("admin") && password.equals("123")) {
//            session.setAttribute("user", username);
//            return "redirect:/";
//        }
        User user = users.get(username.toUpperCase());

        if (user != null && user.getPassword().equals(password)) {
            if (user.getName().equalsIgnoreCase("admin")){
                session.setAttribute("user", username);
                return "redirect:/";
            }

            session.setAttribute("user", username);

            if (!userFavorites.containsKey(username)) {
                userFavorites.put(username, new HashSet<>());
            }

            return "redirect:/";
        }

        model.addAttribute("error", "Invalid username or password.");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/create-account")
    public String createAccountForm() {
        return "create-account";
    }

    @PostMapping("/create-account")
    public String createAccount(@RequestParam String username, @RequestParam String password, @RequestParam String confirmPassword,
                                Model model) {

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            model.addAttribute("error", "Username and password are required.");
            return "create-account";
        }

        if (!(password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[!@#$%^&*(),.?\":{}|<>].*"))){
            model.addAttribute("error", "Password must contain: 8+ characters, An Uppercase Letter, A Lowercase Letter, A Number, A Special Character");
            return "create-account";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "create-account";
        }

        // Check if the username already exists
        if (users.containsKey(username.toUpperCase())) {
            model.addAttribute("error", "Username already exists.");
            return "create-account";
        }

        // Create a new user
        User newUser = new User(username, password, new ArrayList<>());
        users.put(username.toUpperCase(), newUser);

        // Save the new user to the CSV file
        newUser.saveUserToCSV();

        // Redirect to the login page with a success message
        return "redirect:/login?success=Account+created+successfully";
    }

    // REST CONTROLLER -------------------------------------------------------------------------------------------------

    @RestController
    public static class FileController {
        private static final String UPLOAD_DIR = "uploads/";

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

    @GetMapping("/")
    public String home(@RequestParam(required = false) String category,
                       @RequestParam(required = false) String brand,
                       Model model,
                       HttpSession session) {

        ArrayList<Product> filteredResults = new ArrayList<>();
        Set<String> filteredCategories = new HashSet<>();
        Set<String> filteredBrands = new HashSet<>();

        for (Product product : products) {

            boolean matchesCategory = (category == null || category.isEmpty()) || product.getCategory().equalsIgnoreCase(category);
            boolean matchesBrand = (brand == null || brand.isEmpty()) || product.getBrand().equalsIgnoreCase(brand);

            if (matchesCategory && matchesBrand) {
                filteredResults.add(product);
                filteredCategories.add(product.getCategory());
                filteredBrands.add(product.getBrand());
            }
        }

        checkUser(model, session);
        model.addAttribute("filteredResults", filteredResults);
        model.addAttribute("categories", filteredCategories);
        model.addAttribute("brands", filteredBrands);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedBrand", brand);

        return "index";
    }

    // SEARCH PAGE -----------------------------------------------------------------------------------------------------

    @GetMapping("/search")
    public String search(@RequestParam String query,
                         @RequestParam(required = false) String category,
                         @RequestParam(required = false) String brand,
                         @RequestParam(required = false) String reset,  // Added to handle reset button
                         Model model, HttpSession session) {

        query = query.toLowerCase();
        ArrayList<Product> searchResults = new ArrayList<>();
        Set<String> filteredCategories = new HashSet<>();
        Set<String> filteredBrands = new HashSet<>();

        for (Product product : products) {
            boolean matchesQuery = product.getName().toLowerCase().contains(query) ||
                    product.getBrand().toLowerCase().contains(query) ||
                    product.getDescription().toLowerCase().contains(query) ||
                    product.getCategory().toLowerCase().contains(query);

            boolean matchesCategory = (category == null || category.isEmpty()) || product.getCategory().equalsIgnoreCase(category);
            boolean matchesBrand = (brand == null || brand.isEmpty()) || product.getBrand().equalsIgnoreCase(brand);

            if (matchesQuery && matchesCategory && matchesBrand) {
                searchResults.add(product);
                filteredCategories.add(product.getCategory());
                filteredBrands.add(product.getBrand());
            }
        }

        checkUser(model, session);
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("query", query);
        model.addAttribute("categories", filteredCategories);
        model.addAttribute("brands", filteredBrands);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedBrand", brand);

        return "search-results";
    }

    // ADMIN PAGE ------------------------------------------------------------------------------------------------------

    @GetMapping("/admin")
    public String adminPage(@RequestParam(required = false) String query, Model model, HttpSession session) {
        if (session.getAttribute("user") == null || !"admin".equals(session.getAttribute("user"))) {
            return "redirect:/login";
        }

        checkUser(model, session);

        // Handle null query properly
        if (query == null || query.trim().isEmpty()) {
            query = ""; // Set to an empty string to avoid null pointer issues
        } else {
            query = query.toLowerCase();
        }

        ArrayList<Product> searchResults = new ArrayList<>();
        for (Product product : products) {
            boolean matchesQuery = product.getName().toLowerCase().contains(query) ||
                    product.getBrand().toLowerCase().contains(query) ||
                    product.getDescription().toLowerCase().contains(query) ||
                    product.getCategory().toLowerCase().contains(query);

            if (matchesQuery) {
                searchResults.add(product);
            }
        }

        model.addAttribute("products", searchResults);

        ArrayList<String> allLabels = new ArrayList<>();
        for (Product product : products) {
            if (product.getLabels() != null && !product.getLabels().isEmpty()) {
                for (String label : product.getLabels()) {
                    if (!allLabels.contains(label)) {
                        allLabels.add(label);
                    }
                }
            }
        }
        System.out.println(allLabels);
        model.addAttribute("labels", allLabels);
        return "admin";
    }
}
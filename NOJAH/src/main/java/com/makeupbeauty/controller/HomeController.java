package com.makeupbeauty.controller;

import com.makeupbeauty.model.Product;
import jakarta.servlet.http.HttpSession;
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

@Controller
public class HomeController {

    private final ArrayList<Product> products = new ArrayList<>();
    private final HashMap<String, String> users = new HashMap<>(); // Mocked user credentials
    private final Map<String, Set<Integer>> userFavorites = new HashMap<>(); //
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class); // <------- this is better than printStackTrace()

    // constructor init method
    public void loadFavoritesFromCSV(String filePath) {
        File favFile = new File(filePath);
        if (!favFile.exists()) {
            logger.warn("favorites.csv does not exist yet.");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] values = line.split(",");
                if (values.length == 2) {
                    String username = values[0].trim();
                    int productId = Integer.parseInt(values[1].trim());

                    // Add to the userFavorites map
                    userFavorites.putIfAbsent(username, new HashSet<>());
                    userFavorites.get(username).add(productId);
                }
            }
            logger.info("Favorites successfully loaded from {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to load favorites from {}: {}", filePath, e.getMessage(), e);
        }
    }

    // method is run everytime you save
    public void saveFavoriteToCSV(String username, int productId) {
        String fav_line = username + "," + productId;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/favorites.csv", true))) {
            writer.newLine();
            writer.write(fav_line);
            logger.info("User '{}' favorited product {}", username, productId);
        } catch (IOException e) {
            logger.error("Failed to save favorite: {}", e.getMessage(), e);
        }
    }

    // constructor
    public HomeController() {
        loadProductsFromCSV("src/main/resources/catalog.txt");
        loadUsersFromCSV("src/main/resources/users.csv");
        loadFavoritesFromCSV("src/main/resources/favorites.csv");
    }

    @PostMapping("/favorite")
    public String addFavorite(@RequestParam(required = false) Integer productId, HttpSession session) {
        if (productId == null) {
            System.err.println("OOPS! ERROR: productId is null!");
            return "redirect:/error?message=Product ID is missing";
        }

        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }

        System.out.println("OK: User " + username + " favorited product: " + productId);

        userFavorites.putIfAbsent(username, new HashSet<>());
        if (!userFavorites.get(username).contains(productId)) {
            userFavorites.get(username).add(productId);
            saveFavoriteToCSV(username, productId);
        }

        return "redirect:/product/" + productId + "?success=Added to favorites";
    }

    @PostMapping("/unfavorite")
    public String removeFavorite(@RequestParam int productId, HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }

        // Remove product from user's favorites
        Set<Integer> favSet = userFavorites.getOrDefault(username, new HashSet<>());
        if (favSet.contains(productId)) {
            favSet.remove(productId);
            rewriteFavoritesCSV("src/main/resources/favorites.csv");
        }

        return "redirect:/my-favorites?success=Removed+from+favorites";
    }

    private void rewriteFavoritesCSV(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("username,productId"); // Keep the header
            writer.newLine();

            for (String user : userFavorites.keySet()) {
                for (Integer pid : userFavorites.get(user)) {
                    writer.write(user + "," + pid);
                    writer.newLine();
                }
            }
            logger.info("Favorites file successfully updated.");
        } catch (IOException e) {
            logger.error("Failed to update favorites.csv: {}", e.getMessage(), e);
        }
    }



    @GetMapping("/my-favorites")
    public String showFavorites(Model model, HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }

        // Get the product IDs that the (current) user added to favs
        Set<Integer> favIds = userFavorites.getOrDefault(username, new HashSet<>());

        // Grab IDS of the product before turning them into the product objects of an ArrList
        List<Product> favProducts = new ArrayList<>();
        for (Product p : products) {
            if (favIds.contains(p.getId())) {
                favProducts.add(p);
            }
        }

        model.addAttribute("favProducts", favProducts);
        return "favorites"; // Loads the favorites.html endpoint thingy
    }


    public void loadProductsFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // skip empty lines
                String[] values = line.split("\\|,\\|");

                System.out.println("Reading line: " + line);

                if (values.length == 6) {
                    int id = Integer.parseInt(values[0].trim());
                    String name = values[1].trim();
                    String brand = values[2].trim();
                    String description = values[3].trim();
                    String category = values[4].trim();
                    String image = values[5].trim();

                    products.add(new Product(id, name, brand, description, category, image));
                } else {
                    System.err.println("Invalid Product Type: " + line);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            e.printStackTrace();
        }
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

    public void saveUpdateToCSV(Product product) {
        String inputFile = "src/main/resources/catalog.txt";
        StringBuilder updatedContent = new StringBuilder();
        String productIdStr = String.valueOf(product.getId());

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            boolean found = false;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|,\\|");  // Assuming "|,|" is the delimiter
                if (parts.length > 0 && parts[0].equals(productIdStr)) {
                    // Update the existing product entry
                    line = String.join("|,|",
                            productIdStr,
                            product.getName(),
                            product.getBrand(),
                            product.getDescription(),
                            product.getCategory(),
                            product.getImage()
                    );
                    found = true;
                }
                updatedContent.append(line); // Append the line without adding a newline yet
                updatedContent.append("\n");  // Add a newline after each line
            }

            if (!found) {
                System.out.println("Product ID not found in catalog.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Overwrite the file with the updated content, trimming the trailing newline if necessary
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile))) {
            // Remove the last newline (if it exists) before writing to the file
            String updatedContentString = updatedContent.toString();
            if (updatedContentString.endsWith("\n")) {
                updatedContentString = updatedContentString.substring(0, updatedContentString.length() - 1);
            }
            writer.write(updatedContentString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void saveProductsToCSV(Product product) {
        String productLine = String.join("|,|",
                String.valueOf(product.getId()),  // Product ID
                product.getName(),                // Product name
                product.getBrand(),               // Product brand
                product.getDescription(),         // Product description
                product.getCategory(),            // Product category
                product.getImage()                // Product image
        );
        // Append the new product to the CSV file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/catalog.txt", true))) {
            writer.newLine(); // Add a newline before appending the product
            writer.write(productLine);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void loadUsersFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 2) {
                    String name = values[0].trim();
                    String password = values[1].trim();
                    users.put(name, password);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void checkUser(Model model, HttpSession session) {
        if (session.getAttribute("user") != null) {
            model.addAttribute("isLoggedIn", session.getAttribute("user") != null);
            model.addAttribute("username", session.getAttribute("user"));
        }
        model.addAttribute("isAdmin", "admin".equals(session.getAttribute("user")));
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("products", products);
        checkUser(model, session);
        return "index";
    }

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

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        if (username.equals("admin") && password.equals("123")) {
            session.setAttribute("user", username);
            return "redirect:/";
        }

        for (String user : users.keySet()) {
            if (user.equalsIgnoreCase(username) && users.get(user).equals(password)) {
                session.setAttribute("user", username);
                return "redirect:/";
            }
        }

        model.addAttribute("error", "Invalid username or password.");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/admin")
    public String adminPage(Model model, HttpSession session) {
        if (session.getAttribute("user") == null || !"admin".equals(session.getAttribute("user"))) {
            return "redirect:/login";
        }
        checkUser(model, session);
        model.addAttribute("products", products);
        return "admin";
    }

    @PostMapping("/add-product")
    public String addProduct(
            @RequestParam String name,
            @RequestParam String brand,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam("image") MultipartFile image,
            HttpSession session) {

        // Check if the user is logged in and has 'admin' role
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
            String imagePath = saveImage(image);  // Use the new function

            int newId = products.size() + 1;
            Product newProduct = new Product(newId, name, brand, description, category, imagePath);
            products.add(newProduct);
            saveProductsToCSV(newProduct);

        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/admin?error=Failed to upload image. Please try again.";
        }

        // Redirect to the admin page with success message
        return "redirect:/admin?success=Product added successfully";
    }

    @PostMapping("/delete-product")
    public String deleteProduct(@RequestParam int id, HttpSession session) throws IOException {
        if (session.getAttribute("user") == null || !"admin".equals(session.getAttribute("user"))) {
            return "redirect:/login";
        }

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
            e.printStackTrace();
        }

        // Write the updated content back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            String trimmedContent = updatedContent.toString().trim();
            writer.write(trimmedContent);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Delete the image file if it exists
        if (imagePathToDelete != null) {
            deleteImage(imagePathToDelete);

        }

        return "redirect:/admin";
    }

    @GetMapping("/update-product/{id}")
    public String showUpdateProductPage(@PathVariable int id, Model model, HttpSession session) {
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
                saveUpdateToCSV(product);


                break;
            }
        }

        return "redirect:/admin";
    }

    @RestController
    public class FileController {
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


    @GetMapping("/product/{id}")
    public String product(@PathVariable int id, Model model, HttpSession session) {
        Product foundProduct = null;
        for (Product product : products) {
            if (product.getId() == id) {
                foundProduct = product;
                break;
            }
        }

        if (foundProduct != null) {
            model.addAttribute("product", foundProduct);
            checkUser(model, session);
            return "product"; // Use the new template
        } else {
            return "redirect:/"; // Redirect if product not found
        }
    }

    @GetMapping("/product1")
    public String product1(Model model, HttpSession session) {
        checkUser(model, session);
        return "product1";
    }

    @GetMapping("/product2")
    public String product2(Model model, HttpSession session) {
        checkUser(model, session);
        return "product2";
    }

    @GetMapping("/product3")
    public String product3(Model model, HttpSession session) {
        checkUser(model, session);
        return "product3";
    }

    @GetMapping("/product4")
    public String product4(Model model, HttpSession session) {
        checkUser(model, session);
        return "product4";
    }
}
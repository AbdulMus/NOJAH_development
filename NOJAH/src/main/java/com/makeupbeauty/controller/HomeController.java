package com.makeupbeauty.controller;

import com.makeupbeauty.model.Product;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;

import org.springframework.core.io.*;
import java.nio.file.*;

@Controller
public class HomeController {

    private final ArrayList<Product> products = new ArrayList<>();
    private final HashMap<String, String> users = new HashMap<>(); // Mocked user credentials

    public HomeController() {
        loadProductsFromCSV("src/main/resources/catalog.csv");
        loadUsersFromCSV("src/main/resources/users.csv");
    }

    public void loadProductsFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // skip empty lines
                String[] values = line.split(",");

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

    public void saveProductsToCSV(Product product) {
        String productLine = String.join(",",
                String.valueOf(product.getId()),  // Product ID
                product.getName(),               // Product name
                product.getBrand(),              // Product brand
                product.getDescription(),        // Product description
                product.getCategory(),           // Product category
                product.getImage()               // Product image
        );
        // Append the new product to the CSV file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/catalog.csv", true))) {
            writer.newLine(); // Add a newline before appending the product
            writer.write(productLine);
            System.out.println("Writing CSV file: " + productLine);
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
            // Define folder path (make sure it's a valid, writable directory)
            String uploadDir = "uploads/";

            // Create folder if it doesn't exist
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();  // Create the directory if it doesn't exist
            }

            // Handle image file naming (avoid collisions)
            String imageName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            String filePath = uploadDir + imageName;
            File file = new File(filePath);

            // Save the file using transferTo
//            image.transferTo(file);

            //  if transferTo doesn't work, you can use FileOutputStream like this:
            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] bytes = image.getBytes();
                fos.write(bytes);
                fos.flush();
            }

            // Save the relative image path for web access
            String imagePath = "/uploads/" + imageName;

            // Add the new product (assuming you have a list or a repository)
            int newId = products.size() + 1;  // Generating ID based on list size
            Product newProduct = new Product(newId, name, brand, description, category, imagePath);
            System.out.println(imagePath);
            products.add(newProduct);  // Add product to the in-memory list
            saveProductsToCSV(newProduct);


        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/admin?error=Failed to upload image. Please try again.";
        }

        // Redirect to the admin page with success message
        return "redirect:/admin?success=Product added successfully";
    }

    @PostMapping("/delete-product")
    public String deleteProduct(@RequestParam int id, HttpSession session) {
        if (session.getAttribute("user") == null || !"admin".equals(session.getAttribute("user"))) {
            return "redirect:/login";
        }

        products.removeIf(product -> product.getId() == id);

        return "redirect:/admin";
    }

    @PostMapping("/update-product")
    public String updateProduct(@RequestParam int id, @RequestParam String name,
                                @RequestParam String brand, @RequestParam String description,
                                @RequestParam String category, @RequestParam String image, HttpSession session) {
        if (session.getAttribute("user") == null || !"admin".equals(session.getAttribute("user"))) {
            return "redirect:/login";
        }

        Product foundProduct = null;

        for (Product product : products) {
            if (product.getId() == id) {
                foundProduct = product;
                break;
            }
        }

        if (foundProduct != null) {
            foundProduct.setName(name);
            foundProduct.setBrand(brand);
            foundProduct.setDescription(description);
            foundProduct.setCategory(category);
            foundProduct.setImage(image);
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
package com.makeupbeauty.controller;

import com.makeupbeauty.model.Product;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;

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
    public String search(@RequestParam String query, Model model, HttpSession session) {
        query = query.toLowerCase();
        ArrayList<Product> searchResults = new ArrayList<>();
        for (Product product : products) {
            if (product.getName().toLowerCase().contains(query) ||
                    product.getBrand().toLowerCase().contains(query) ||
                    product.getDescription().toLowerCase().contains(query) ||
                    product.getCategory().toLowerCase().contains(query)) {
                searchResults.add(product);
            }
        }

        checkUser(model, session);

        model.addAttribute("searchResults", searchResults);
        model.addAttribute("query", query);

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

        if (session.getAttribute("user") == null || !"admin".equals(session.getAttribute("user"))) {
            return "redirect:/login";
        }

        if (!image.isEmpty()) {
            try {
                // Define folder path
                String uploadDir = "src/main/resources/static/images/";

                // Create folder if not exists
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Create file path
                String filePath = uploadDir + image.getOriginalFilename();
                File file = new File(filePath);

                // Save file
                image.transferTo(file);

                // You can save just the relative path if you want
                String imagePath = "/images/" + image.getOriginalFilename();

                // Save product (assuming you have a list or a repository)
                int newId = products.size() + 1;
                products.add(new Product(newId, name, brand, description, category, imagePath));

            } catch (IOException e) {
                e.printStackTrace();
                return "redirect:/login";
            }
        }

        return "redirect:/admin";
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

    @GetMapping("/product1")
    public String product1() {
        return "product1";
    }

    @GetMapping("/product2")
    public String product2() {
        return "product2";
    }

    @GetMapping("/product3")
    public String product3() {
        return "product3";
    }

    @GetMapping("/product4")
    public String product4() {
        return "product4";
    }
}
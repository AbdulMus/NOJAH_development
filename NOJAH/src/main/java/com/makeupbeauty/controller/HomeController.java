package com.makeupbeauty.controller;
import com.makeupbeauty.model.Product;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;

@Controller
public class HomeController {

    protected ArrayList<Product> products = new ArrayList<>();

    public HomeController() {
        products.add(new Product("Matte Revolution Hydrating Lipstick",
                                "Charlotte Tilbury",
                                "A matte lipstick that features a long-lasting, buildable, and hydrating formula.",
                                "Lip",
                                "/images/lipstick.jpg"));

        products.add(new Product("Stay All Day Waterproof Liquid Eye Liner",
                                "Stila",
                            "An easy-application, waterproof liquid liner that stays on all day and night.",
                                "Eye",
                                "/images/eyeliner.jpg"));
    }


    // Homepage with search form
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("products", products);
        return "index";
    }

    // Search functionality
    @GetMapping("/search")
    public String search(@RequestParam String query, Model model) {
        ArrayList<Product> searchResults = new ArrayList<>();
        for (Product product : products) {
            query = query.toLowerCase().replaceAll("[^a-z0-9+\\-*/=]", "");
            if ((product.getName().toLowerCase().replaceAll("[^a-z0-9+\\-*/=]",  "").contains(query))
                || (product.getBrand().toLowerCase().replaceAll("[^a-z0-9+\\-*/=]",  "").contains(query))
                || (product.getDescription().toLowerCase().replaceAll("[^a-z0-9+\\-*/=]",  "").contains(query))
                || (product.getCategory().toLowerCase().replaceAll("[^a-z0-9+\\-*/=]",  "").contains(query)))
            {
                searchResults.add(product);
            }
        }
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("query", query);
        return "search-results";
    }

    // Product 1 page
    @GetMapping("/product1")
    public String product1() {
        return "product1";
    }

    // Product 2 page
    @GetMapping("/product2")
    public String product2() {
        return "product2";
    }

}
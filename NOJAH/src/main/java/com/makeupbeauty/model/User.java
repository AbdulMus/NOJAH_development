package com.makeupbeauty.model;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class User {
    private String name;
    private String password;
    private ArrayList<Product> favorites;
    private ArrayList<Product> cart;

    // Constructor
    public User(String name, String password, ArrayList<Product> favorites, ArrayList<Product> cart) {
        this.name = name;
        this.password = password;
        this.favorites = favorites;
        this.cart = cart;
    }

    public String getName(){ return this.name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }

    public ArrayList<Product> getFavorites() { return this.favorites; }
    public void setFavorites(ArrayList<Product> favorites) { this.favorites = favorites; }

    public ArrayList<Product> getCart() { return this.cart; }
    public void setCart(ArrayList<Product> cart) { this.cart = cart; }

    public void addCart(Product product) {
        this.cart.add(product);
    }
    public void removeCart(Product product) {
        this.cart.remove(product);
    }

    @Override
    public String toString() {
        StringBuilder x = new StringBuilder("Username: " + this.name + ", Password: " + this.password + ", Favorites: ");
        for (Product product : this.favorites) {
            x.append("\n").append(product.toString());
        }
        x.append("\nCart: ");
        if (this.cart != null) {
            for (Product product : this.cart) {
                x.append("\n").append(product.toString());
            }
        } else {
            x.append("null");
        }
        return x.toString();
    }

    public void addFavorite(int productId, ArrayList<Product> products) {
        // Initialize a variable to store the found product
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

        Product product = foundProduct;

        if (product == null) {
            return;
        }

        this.favorites.add(product);

        printFavorites();
    }

    public void removeFavorite(Product product) {
        if (product == null) {
            return;
        }

        this.favorites.remove(product);
        printFavorites();

    }

    private void printFavorites() {
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/users.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/users.csv"))) {
            writer.write("username,password,favorites"); // Write header
            writer.newLine();

            for (String line : lines) {
                if (line.startsWith("username,password,favorites")) {
                    continue; // Skip the header line
                }

                String[] values = line.split(",");
                if (values.length >= 2 && values[0].trim().equalsIgnoreCase(this.name)) {
                    String name = values[0].trim();
                    String password = values[1].trim();
                    String updatedFavorites = getStringFavorites();

                    // Write updated line
                    writer.write(name + "," + password + "," + updatedFavorites);
                } else {
                    writer.write(line);
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getStringFavorites() {
        StringBuilder updatedFavoritesBuilder = new StringBuilder();

        // Loop through each favorite in the favorites list
        for (Product p : this.favorites) {
            // Convert the favorite to a string
            String pID = String.valueOf(p.getId());
            // Append the favorite string to the StringBuilder
            updatedFavoritesBuilder.append(pID);
            // Add a semicolon separator after each favorite (except the last one)
            updatedFavoritesBuilder.append(";");
        }

        // Convert the StringBuilder to a string
        String updatedFavorites = updatedFavoritesBuilder.toString();

        // Remove the trailing semicolon (if any)
        if (updatedFavorites.endsWith(";")) {
            updatedFavorites = updatedFavorites.substring(0, updatedFavorites.length() - 1);
        }

        return updatedFavorites;
    }
}
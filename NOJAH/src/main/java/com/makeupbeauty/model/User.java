package com.makeupbeauty.model;

import java.io.*;
import java.util.*;

public class User {
    private String name;
    private String password;
    private ArrayList<Product> favorites;
    private final String userPath = "src/main/resources/users.csv";
    // Constructor
    public User(String name, String password, ArrayList<Product> favorites) {
        this.name = name;
        this.password = password;
        this.favorites = favorites;
    }

    public String getName(){ return this.name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }

    public ArrayList<Product> getFavorites() { return this.favorites; }
    public void setFavorites(ArrayList<Product> favorites) { this.favorites = favorites; }


    @Override
    public String toString() {
        StringBuilder x = new StringBuilder("Username: " + this.name + ", Password: " + this.password + ", Favorites: ");
        for (Product product : this.favorites) {
            x.append("\n").append(product.toString());
        }

        return x.toString();
    }

    public void saveUserToCSV() {
        String userLine = String.join(",", this.getName(), this.getPassword(), "");

        // Append the new user to the CSV file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userPath, true))) {
            writer.newLine(); // Add a newline before appending the user
            writer.write(userLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        if (favorites.contains(product)) {
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

        try (BufferedReader br = new BufferedReader(new FileReader(userPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userPath))) {
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

                    writer.newLine();
                    // Write updated line
                    writer.write(name + "," + password + "," + updatedFavorites);
                } else {
                    writer.write(line);
                }
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
    public Set<Integer> getFavoriteProductIds() {
        //return favourite item ids
        Set<Integer> favoriteIds = new HashSet<>();
        for (Product product : this.favorites) {
            favoriteIds.add(product.getId());
        }
        return favoriteIds;
    }
}
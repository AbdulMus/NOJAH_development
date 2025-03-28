package com.makeupbeauty.model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Product {
    private Integer id;
    private String name;
    private String brand;
    private String description;
    private String image;
    private String category;
    private ArrayList<String> labels;
    private final String catalogPath = "src/main/resources/catalog.txt";

    // Constructor
    public Product(Integer id, String name, String brand, String description,String category, String image, ArrayList<String> labels) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.category = category;
        this.image = image;
        this.labels = labels;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setLabels(List<String> labels) {
        this.labels = new ArrayList<>(labels);
    }

    public List<String> getLabels() {
        return labels;
    }

    public String getLabelsString() {
        StringBuilder sb = new StringBuilder();
        for (String label : labels) {
            sb.append(label);
            sb.append(";");
        }
        return sb.toString();
    }

    public void saveProductsToCSV() {
        String productLine = String.join("|,|",
                String.valueOf(this.getId()),  // Product ID
                this.getName(),                // Product name
                this.getBrand(),               // Product brand
                this.getDescription(),         // Product description
                this.getCategory(),            // Product category
                this.getImage(),               // Product image
                this.getLabelsString()
        );
        // Append the new product to the CSV file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(catalogPath, true))) {
            writer.newLine(); // Add a newline before appending the product
            writer.write(productLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveUpdateToCSV() {
        StringBuilder updatedContent = new StringBuilder();
        String productIdStr = String.valueOf(this.getId());

        try (BufferedReader br = new BufferedReader(new FileReader(catalogPath))) {
            String line;
            boolean found = false;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|,\\|");  // Assuming "|,|" is the delimiter
                if (parts.length > 0 && parts[0].equals(productIdStr)) {
                    // Update the existing product entry
                    line = String.join("|,|",
                            productIdStr,
                            this.getName(),
                            this.getBrand(),
                            this.getDescription(),
                            this.getCategory(),
                            this.getImage()
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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(catalogPath))) {
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

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", brand='" + brand + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", image='" + image + '\'' +
                ", labels=" + labels +
                '}';
    }
}
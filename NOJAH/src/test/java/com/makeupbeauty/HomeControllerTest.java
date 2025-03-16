package com.makeupbeauty;

import com.makeupbeauty.controller.HomeController;
import com.makeupbeauty.model.Product;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class HomeControllerTest {
    @Test
    void deleteImage() throws IOException {
        String pathname = "/uploads/1741231356446_lipstick.png";
        HomeController homeController = new HomeController();
        String imagePathToDelete = pathname.substring(1);

        String expected = "Image deleted successfully:" + pathname;

        String result = "";

        homeController.deleteImage(pathname);

        File imageFile = new File(imagePathToDelete);
        if (imageFile.exists() && imageFile.isFile()) {
            result = "Failed to delete image: " + imagePathToDelete;
        } else {
            result = "Image deleted successfully:" + pathname;
        }

        assertEquals(expected, result);


    }

//    @Test
//    void saveProductsToCSV(){
//
//        HomeController homeController = new HomeController();
//        Product product = new Product(6, "Lipstick", "Maybelline", "Matte finish", "Makeup", "/uploads/lipstick.jpg");
//        homeController.saveProductsToCSV(product);
//
//        boolean expected = true;
//        boolean result = false;
//
//        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/catalog.txt"))) {
//            String line;
//
//            while ((line = br.readLine()) != null) {
//                if (line.equals("6|,|Lipstick|,|Maybelline|,|Matte finish|,|Makeup|,|/uploads/lipstick.jpg")) {
//                    result = true;
//                    assertEquals(expected, result);
//                }
//
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return;
//        }
//        assertEquals(expected, result);
//    }
}

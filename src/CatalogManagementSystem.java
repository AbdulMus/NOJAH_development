import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CatalogManagementSystem {
    private final static String DATABASE_FILE = Paths.get("").toAbsolutePath() + "/src/catalog.csv";
    private final static String LOGIN_FILE = Paths.get("").toAbsolutePath() + "/src/login.csv";
    private static final List<Item> catalog = new ArrayList<>();
    private static final Map<String, String> loginCredentials = new HashMap<>();

    public static void main(String[] ignoredArgs) {
        loadLoginData();
        loadCatalog();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n1. Log In");
            System.out.println("2. Create Account");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice = getIntInput(scanner);
            scanner.nextLine();

            switch (choice) {
                case 1 -> {
                    if (loginUser(scanner)) {
                        showMainMenu(scanner);
                    }
                }
                case 2 -> createAccount(scanner);
                case 3 -> {
                    saveCatalog();
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void showMainMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            System.out.println("\nCatalog Management System");
            System.out.println("1. View Items");
            System.out.println("2. Add Item");
            System.out.println("3. Edit Item");
            System.out.println("4. Delete Item");
            System.out.println("5. Search Items");
            System.out.println("6. Filter Items");
            System.out.println("7. Save and Exit");
            System.out.print("Choose an option: ");

            int choice = getIntInput(scanner);
            scanner.nextLine();

            switch (choice) {
                case 1 -> viewItems();
                case 2 -> addItem(scanner);
                case 3 -> editItem(scanner);
                case 4 -> deleteItem(scanner);
                case 5 -> searchItems(scanner);
                case 6 -> filterItems(scanner);
                case 7 -> {
                    saveCatalog();
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static int getIntInput(Scanner scanner) {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.print("Invalid input. Enter a number: ");
                scanner.nextLine();
            }
        }
    }

    // --------------- Login System ---------------
    private static void loadLoginData() {
        try {
            Path path = Path.of(LOGIN_FILE);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            if (!Files.exists(path)) {
                Files.createFile(path);
            }

            try (BufferedReader br = new BufferedReader(new FileReader(LOGIN_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    List<String> parts = parseCsvLine(line);
                    if (parts.size() >= 2) {
                        loginCredentials.put(parts.get(0), parts.get(1));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading login data: " + e.getMessage());
        }
    }

    private static void createAccount(Scanner scanner) {
        System.out.print("Enter new username: ");
        String username = scanner.nextLine().trim();

        if (loginCredentials.containsKey(username)) {
            System.out.println("Username already exists!");
            return;
        }

        String password;
        while (true) {
            System.out.print("Enter new password: ");
            password = scanner.nextLine().trim();
            
            if (isValidPassword(password)) break;
            System.out.println("Password must contain:\n- 8+ characters\n- Uppercase letter\n- Lowercase letter\n- Number\n- Special character");
        }

        loginCredentials.put(username, password);
        saveLoginData();
        System.out.println("Account created successfully!");
    }

    private static boolean isValidPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
    }

    private static boolean loginUser(Scanner scanner) {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (loginCredentials.containsKey(username) && 
            loginCredentials.get(username).equals(password)) {
            System.out.println("Login successful!");
            return true;
        }
        System.out.println("Invalid credentials!");
        return false;
    }

    private static void saveLoginData() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LOGIN_FILE))) {
            for (Map.Entry<String, String> entry : loginCredentials.entrySet()) {
                bw.write(escapeCsvField(entry.getKey()) + "," + escapeCsvField(entry.getValue()));
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving login data: " + e.getMessage());
        }
    }

    // --------------- Catalog System ---------------
    private static void loadCatalog() {
        try {
            Path path = Path.of(DATABASE_FILE);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            if (!Files.exists(path)) {
                Files.createFile(path);
                return;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(DATABASE_FILE))) {
                String line;
                br.readLine(); // Skip header
                while ((line = br.readLine()) != null) {
                    List<String> parts = parseCsvLine(line);
                    if (parts.size() >= 5) {
                        catalog.add(new Item(
                            parts.get(0).trim(),
                            parts.get(1).trim(),
                            parts.get(2).trim(),
                            parts.get(3).trim(),
                            parts.get(4).trim()
                        ));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading catalog: " + e.getMessage());
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i < line.length() - 1 && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString());
        return fields;
    }

    private static void saveCatalog() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATABASE_FILE))) {
            bw.write(String.join(",",
                escapeCsvField("ID"),
                escapeCsvField("Name"),
                escapeCsvField("Description"),
                escapeCsvField("Category"),
                escapeCsvField("Brand")
            ));
            bw.newLine();

            for (Item item : catalog) {
                bw.write(String.join(",",
                    escapeCsvField(item.getId()),
                    escapeCsvField(item.getName()),
                    escapeCsvField(item.getDescription()),
                    escapeCsvField(item.getCategory()),
                    escapeCsvField(item.getBrand())
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving catalog: " + e.getMessage());
        }
    }

    private static String escapeCsvField(String field) {
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    // --------------- Catalog Operations ---------------
    private static void viewItems() {
        if (catalog.isEmpty()) {
            System.out.println("No items found.");
            return;
        }
        catalog.forEach(System.out::println);
    }

    private static void addItem(Scanner scanner) {
        System.out.print("Enter item ID: ");
        String id = scanner.nextLine().trim();
        
        if (!id.matches("\\d+")) {
            System.out.println("Invalid ID! Must be numeric.");
            return;
        }
        
        if (findItemById(id) != null) {
            System.out.println("Item ID already exists!");
            return;
        }

        System.out.print("Enter name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter description: ");
        String description = scanner.nextLine().trim();
        System.out.print("Enter category: ");
        String category = scanner.nextLine().trim();
        System.out.print("Enter brand: ");
        String brand = scanner.nextLine().trim();

        if (name.isEmpty() || description.isEmpty() || category.isEmpty() || brand.isEmpty()) {
            System.out.println("All fields are required!");
            return;
        }

        catalog.add(new Item(id, name, description, category, brand));
        System.out.println("Item added successfully.");
    }

    private static void editItem(Scanner scanner) {
        System.out.print("Enter item ID to edit: ");
        String id = scanner.nextLine().trim();
        Item item = findItemById(id);
        
        if (item == null) {
            System.out.println("Item not found.");
            return;
        }

        System.out.print("New name (" + item.getName() + "): ");
        String name = scanner.nextLine().trim();
        System.out.print("New description (" + item.getDescription() + "): ");
        String description = scanner.nextLine().trim();
        System.out.print("New category (" + item.getCategory() + "): ");
        String category = scanner.nextLine().trim();
        System.out.print("New brand (" + item.getBrand() + "): ");
        String brand = scanner.nextLine().trim();

        if (!name.isEmpty()) item.setName(name);
        if (!description.isEmpty()) item.setDescription(description);
        if (!category.isEmpty()) item.setCategory(category);
        if (!brand.isEmpty()) item.setBrand(brand);
        
        System.out.println("Item updated successfully.");
    }

    private static void deleteItem(Scanner scanner) {
        System.out.print("Enter item ID to delete: ");
        String id = scanner.nextLine().trim();
        Item item = findItemById(id);
        
        if (item == null) {
            System.out.println("Item not found.");
            return;
        }

        System.out.print("Confirm deletion (y/n)? ");
        if (scanner.nextLine().equalsIgnoreCase("y")) {
            catalog.remove(item);
            System.out.println("Item deleted.");
        } else {
            System.out.println("Deletion canceled.");
        }
    }

    private static void searchItems(Scanner scanner) {
        System.out.print("Search keyword: ");
        String keyword = scanner.nextLine().trim().toLowerCase();
        
        List<Item> results = catalog.stream()
            .filter(item -> item.toString().toLowerCase().contains(keyword))
            .collect(Collectors.toList());
        
        if (results.isEmpty()) {
            System.out.println("No matching items found.");
        } else {
            results.forEach(System.out::println);
        }
    }

    private static void filterItems(Scanner scanner) {
        System.out.println("Filter by:\n1. Category\n2. Brand");
        int choice = getIntInput(scanner);
        scanner.nextLine();
        
        if (choice < 1 || choice > 2) {
            System.out.println("Invalid choice!");
            return;
        }

        System.out.print("Enter filter value: ");
        String filter = scanner.nextLine().trim().toLowerCase();
        
        List<Item> results = catalog.stream()
            .filter(item -> (choice == 1 ? 
                item.getCategory().toLowerCase() : 
                item.getBrand().toLowerCase()).equals(filter))
            .collect(Collectors.toList());
        
        if (results.isEmpty()) {
            System.out.println("No items found.");
        } else {
            results.forEach(System.out::println);
        }
    }

    private static Item findItemById(String id) {
        return catalog.stream()
            .filter(item -> item.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    static class Item {
        private final String id;
        private String name;
        private String description;
        private String category;
        private String brand;

        public Item(String id, String name, String description, String category, String brand) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.category = category;
            this.brand = brand;
        }

        // Getters and Setters
        public String getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }

        @Override
        public String toString() {
            return String.format("ID: %s | Name: %s | Description: %s | Category: %s | Brand: %s",
                id, name, description, category, brand);
        }
    }
}

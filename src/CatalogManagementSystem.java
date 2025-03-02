import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class CatalogManagementSystem {
    private final static String DATABASE_FILE = Paths.get("").toAbsolutePath() + "/src/catalog.csv";
    private static final List<Item> catalog = new ArrayList<>();

    public static void main(String[] ignoredArgs) {
        loadCatalog();
        Scanner scanner = new Scanner(System.in);
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
        scanner.close();
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

    private static void loadCatalog() {
        System.out.println("Attempting to load catalog from: " + DATABASE_FILE);
        
        try {
            // Create parent directories if they don't exist
            Path path = Path.of(DATABASE_FILE);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            // Create file if it doesn't exist
            if (!Files.exists(path)) {
                Files.createFile(path);
                System.out.println("Created new catalog file");
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
                System.out.println("Successfully loaded " + catalog.size() + " items");
            }
        } catch (IOException e) {
            System.out.println("Error loading catalog: " + e.getMessage());
            e.printStackTrace();
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
        System.out.println("Saving catalog to: " + DATABASE_FILE);
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATABASE_FILE))) {
            // Write header
            bw.write(escapeCsvField("ID") + "," +
                    escapeCsvField("Name") + "," +
                    escapeCsvField("Description") + "," +
                    escapeCsvField("Category") + "," +
                    escapeCsvField("Brand"));
            bw.newLine();

            // Write items
            for (Item item : catalog) {
                String line = String.join(",",
                        escapeCsvField(item.getId()),
                        escapeCsvField(item.getName()),
                        escapeCsvField(item.getDescription()),
                        escapeCsvField(item.getCategory()),
                        escapeCsvField(item.getBrand()));
                bw.write(line);
                bw.newLine();
            }
            System.out.println("Successfully saved " + catalog.size() + " items");
        } catch (IOException e) {
            System.out.println("Error saving catalog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String escapeCsvField(String field) {
        if (field == null || field.isEmpty()) return "\"\"";
        String escaped = field.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private static void viewItems() {
        if (catalog.isEmpty()) {
            System.out.println("No items found in the catalog.");
            return;
        }
        catalog.forEach(System.out::println);
    }

    private static void addItem(Scanner scanner) {
        System.out.print("Enter item ID: ");
        String id = scanner.nextLine().trim();
        
        // Validate numeric ID
        try {
            Integer.parseInt(id);
        } catch (NumberFormatException e) {
            System.out.println("Error: ID must be a number. Item not added.");
            return;
        }
        
        // Check for duplicate ID
        if (findItemById(id) != null) {
            System.out.println("Error: An item with this ID already exists.");
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

        // Validate all fields
        if (name.isEmpty() || description.isEmpty() || category.isEmpty() || brand.isEmpty()) {
            System.out.println("Error: All fields are required. Item not added.");
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
            System.out.println("Error: Item not found.");
            return;
        }

        System.out.print("Enter new name (leave blank to keep current: " + item.getName() + "): ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) item.setName(name);

        System.out.print("Enter new description (leave blank to keep current: " + item.getDescription() + "): ");
        String description = scanner.nextLine().trim();
        if (!description.isEmpty()) item.setDescription(description);

        System.out.print("Enter new category (leave blank to keep current: " + item.getCategory() + "): ");
        String category = scanner.nextLine().trim();
        if (!category.isEmpty()) item.setCategory(category);

        System.out.print("Enter new brand (leave blank to keep current: " + item.getBrand() + "): ");
        String brand = scanner.nextLine().trim();
        if (!brand.isEmpty()) item.setBrand(brand);

        System.out.println("Item updated successfully.");
    }

    private static void deleteItem(Scanner scanner) {
        System.out.print("Enter item ID to delete: ");
        String id = scanner.nextLine().trim();
        Item item = findItemById(id);
        if (item == null) {
            System.out.println("Error: Item not found.");
            return;
        }

        System.out.print("Are you sure you want to delete this item? (yes/no): ");
        String confirmation = scanner.nextLine().trim();
        if (confirmation.equalsIgnoreCase("yes")) {
            catalog.remove(item);
            System.out.println("Item deleted successfully.");
        } else {
            System.out.println("Deletion canceled.");
        }
    }

    private static void searchItems(Scanner scanner) {
        System.out.print("Enter search keyword: ");
        String keyword = scanner.nextLine().trim().toLowerCase();
        List<Item> results = catalog.stream()
                .filter(item -> item.getName().toLowerCase().contains(keyword) ||
                        item.getDescription().toLowerCase().contains(keyword) ||
                        item.getCategory().toLowerCase().contains(keyword) ||
                        item.getBrand().toLowerCase().contains(keyword))
                .toList();
        if (results.isEmpty()) {
            System.out.println("No items found matching the keyword.");
        } else {
            results.forEach(System.out::println);
        }
    }

    private static void filterItems(Scanner scanner) {
        System.out.println("Filter by:\n1. Category\n2. Brand");
        int choice = getIntInput(scanner);
        scanner.nextLine();

        switch (choice) {
            case 1 -> {
                System.out.print("Enter category: ");
                String category = scanner.nextLine().trim();
                filterBy(category, "category");
            }
            case 2 -> {
                System.out.print("Enter brand: ");
                String brand = scanner.nextLine().trim();
                filterBy(brand, "brand");
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void filterBy(String value, String type) {
        List<Item> results = catalog.stream()
                .filter(item -> (type.equals("category") 
                        ? item.getCategory().equalsIgnoreCase(value) 
                        : item.getBrand().equalsIgnoreCase(value)))
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

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public String getBrand() { return brand; }

        public void setName(String name) { this.name = name; }
        public void setDescription(String description) { this.description = description; }
        public void setCategory(String category) { this.category = category; }
        public void setBrand(String brand) { this.brand = brand; }

        @Override
        public String toString() {
            return String.format("ID: %s, Name: %s, Description: %s, Category: %s, Brand: %s",
                    id, name, description, category, brand);
        }
    }
}

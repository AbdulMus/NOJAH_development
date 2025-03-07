import java.io.*;
import java.util.*;

public class CatalogManagementSystemLogin {
    private final static String DATABASE_FILE = "src/catalog.csv";
    private final static String LOGIN_FILE = "src/login.csv";
    private final static List<Item> catalog = new ArrayList<>();
    private final static HashMap<String, String> login = new HashMap<String, String>();

    public static void main(String[] args) {
        loadCatalog();
        loadLoginData();
        System.out.println("Loaded users: " + login);

        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        boolean loginSuccess = false;

        String name = "";
        String pass = "";
        while (running) {
            if (!loginSuccess) {
                System.out.println("Please enter your name: ");
                name = scanner.nextLine();
                System.out.println("Please enter your password: ");
                pass = scanner.nextLine();
            }

            if (isUser(name, pass)) {
                loginSuccess = true;
                System.out.println("\nCatalog Management System");
                System.out.println("1. View Items");
                System.out.println("2. Add Item");
                System.out.println("3. Edit Item");
                System.out.println("4. Save and Exit");

                int choice = -1; // Default invalid choice
                while (true) {
                    System.out.print("Choose an option: ");
                    try {
                        choice = scanner.nextInt();
                        scanner.nextLine(); // Consume newline
                        break; // Valid input, exit loop
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input. Please enter a number.");
                        scanner.nextLine(); // Clear invalid input
                    }
                }

                switch (choice) {
                    case 1 -> viewItems();
                    case 2 -> addItem(scanner);
                    case 3 -> editItem(scanner);
                    case 4 -> {
                        saveCatalog();
                        running = false;
                    }
                    default -> System.out.println("Invalid choice. Please select a valid option.");
                }
            } else if (!name.isEmpty() && !pass.isEmpty()) {
                System.out.println("Invalid username or password. Please try again or leave username/password blank to exit");
            } else {
                running = false;
            }
        }

    }

    private static void loadCatalog() {
        try (BufferedReader br = new BufferedReader(new FileReader(DATABASE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    catalog.add(new Item(parts[0], parts[1], parts[2]));
                }
            }
        } catch (IOException e) {
            System.out.println("No existing catalog found. Starting fresh.");
        }
    }

    private static void saveCatalog() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATABASE_FILE))) {
            for (Item item : catalog) {
                bw.write(item.getId() + "," + item.getName() + "," + item.getDescription());
                bw.newLine();
            }
            System.out.println("Catalog saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving catalog: " + e.getMessage());
        }
    }

    private static void viewItems() {
        if (catalog.isEmpty()) {
            System.out.println("No items in the catalog.");
            return;
        }
        System.out.println("\nCatalog Items:");
        for (Item item : catalog) {
            System.out.println(item);
        }
    }

    private static void addItem(Scanner scanner) {
        System.out.print("Enter item ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter item name: ");
        String name = scanner.nextLine();
        System.out.print("Enter item description: ");
        String description = scanner.nextLine();

        if (id.isEmpty() || name.isEmpty() || description.isEmpty()) {
            System.out.println("All fields are required. Item not added.");
            return;
        }

        catalog.add(new Item(id, name, description));
        System.out.println("Item added successfully.");
    }

    private static void editItem(Scanner scanner) {
        System.out.print("Enter the ID of the item to edit: ");
        String id = scanner.nextLine();
        Item item = findItemById(id);

        if (item == null) {
            System.out.println("Item not found.");
            return;
        }

        System.out.print("Enter new name (leave blank to keep current): ");
        String newName = scanner.nextLine();
        System.out.print("Enter new description (leave blank to keep current): ");
        String newDescription = scanner.nextLine();

        if (!newName.isEmpty()) item.setName(newName);
        if (!newDescription.isEmpty()) item.setDescription(newDescription);

        System.out.println("Item updated successfully.");
    }

    private static Item findItemById(String id) {
        for (Item item : catalog) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    private static void loadLoginData() {
        try (BufferedReader br = new BufferedReader(new FileReader(LOGIN_FILE))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length == 2) {
                    login.put(parts[0].trim(), parts[1].trim()); // Add valid login entries
                } else {
                    System.out.println("Invalid line format: " + line); // For Debugging purpose and listing invalid lines
                }
            }
        } catch (IOException e) {
            System.out.println("No existing login file found. Please ensure credentials are set.");
        }
    }


    private static boolean isUser(String username, String password) {
        return login.containsKey(username) && login.get(username).equals(password);
    }

}

class Item {
    private final String id;
    private String name;
    private String description;

    public Item(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Description: " + description;
    }
}

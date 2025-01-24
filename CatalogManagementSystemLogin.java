import java.io.*;
import java.util.*;

public static void main(String[] args) {
        loadCatalog();
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            boolean loginSuccess = false;

            while (running) {
                if (!loginSuccess) {
                    System.out.println("Please enter your name: ");
                    String name = scanner.nextLine().trim();
                    System.out.println("Please enter your password: ");
                    String pass = scanner.nextLine().trim();

                    if (isUser(name, pass)) {
                        loginSuccess = true;
                        System.out.println("Login successful! Welcome, " + name + "!");
                    } else {
                        System.out.println("Invalid username or password. Please try again.");
                        continue;
                    }
                }

                System.out.println("\nCatalog Management System");
                System.out.println("1. View Items");
                System.out.println("2. Add Item");
                System.out.println("3. Edit Item");
                System.out.println("4. Save and Exit");
                System.out.print("Choose an option: ");

                int choice = getValidIntInput(scanner);
                switch (choice) {
                    case 1 -> viewItems();
                    case 2 -> addItem(scanner);
                    case 3 -> editItem(scanner);
                    case 4 -> {
                        saveCatalog();
                        running = false;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
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

    private static boolean isUser(String username, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(LOGIN_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    login.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            System.out.println("No existing catalog found. Starting fresh.");
        }

        for (String user : login.keySet()) {
            if (user.equalsIgnoreCase(username) && login.get(user).equals(password)) {
                return true;
            }
        }
        return false;
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

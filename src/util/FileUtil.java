/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import model.User;
import model.RestaurantTable;
import model.MenuItem;
import model.Order;

/**
 *
 * @author hp
 */
public class FileUtil {

    public static final String DATA_FOLDER = "data";
    public static final String USERS_FILE = "data/users.txt";
    public static final String TABLES_FILE = "data/tables.txt";
    public static final String MENUITEMS_FILE = "data/menuitems.txt";
    public static final String ORDERS_FILE = "data/orders.txt";

    public static void ensureFiles() {
        try {
            Files.createDirectories(Paths.get(DATA_FOLDER));
            System.out.println("DATA FOLDER CREATED OR ALREADY EXISTS: " + Paths.get(DATA_FOLDER).toAbsolutePath());

            File usersFile = new File(USERS_FILE);
            if (!usersFile.exists()) {
                usersFile.createNewFile();
            }

            File tablesFile = new File(TABLES_FILE);
            if (!tablesFile.exists()) {
                tablesFile.createNewFile();
            }

            File menuItemsFile = new File(MENUITEMS_FILE);
            if (!menuItemsFile.exists()) {
                menuItemsFile.createNewFile();
            }

            File ordersFile = new File(ORDERS_FILE);
            if (!ordersFile.exists()) {
                ordersFile.createNewFile();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // User Methods
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();

        try {
            System.out.println("Reading from: " + Paths.get(USERS_FILE).toAbsolutePath());
            List<String> lines = Files.readAllLines(Paths.get(USERS_FILE));
            for (String line : lines) {
                System.out.println("LINE = [" + line + "]");
                if (!line.trim().isEmpty()) {
                    users.add(User.fromString(line));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return users;
    }

    public static void saveUsers(List<User> users) {
        ensureFiles();
        List<String> lines = new ArrayList<>();

        for (User user : users) {
            lines.add(user.toString());
        }

        try {
            Files.write(Paths.get(USERS_FILE), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getNextUserId(List<User> users) {
        if (users.isEmpty()) {
            return 1;
        }
        return users.get(users.size() - 1).getId() + 1;
    }

    //Tables Method
    public static List<RestaurantTable> loadTables() {
        List<RestaurantTable> tables = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(TABLES_FILE));
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    try {
                        tables.add(RestaurantTable.fromString(line));
                    } catch (Exception e) {
                        System.out.println("Invalid table line skipped: " + line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tables;
    }

    public static void saveTables(List<RestaurantTable> tables) {
        ensureFiles();

        List<String> lines = new ArrayList<>();
        for (RestaurantTable table : tables) {
            lines.add(table.toString());
        }

        try {
            Files.write(Paths.get(TABLES_FILE), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getNextTableId(List<RestaurantTable> tables) {
        if (tables.isEmpty()) {
            return 1;
        }
        return tables.get(tables.size() - 1).getId() + 1;
    }

    //MenuItem Method
    public static List<MenuItem> loadMenuItems() {
        List<MenuItem> items = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(MENUITEMS_FILE));
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    try {
                        items.add(MenuItem.fromString(line));
                    } catch (Exception e) {
                        System.out.println("Invalid menu item line skipped: " + line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return items;
    }

    public static void saveMenuItems(List<MenuItem> items) {
        ensureFiles();

        List<String> lines = new ArrayList<>();
        for (MenuItem item : items) {
            lines.add(item.toString());
        }

        try {
            Files.write(Paths.get(MENUITEMS_FILE), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getNextMenuItemId(List<MenuItem> items) {
        if (items.isEmpty()) {
            return 1;
        }
        return items.get(items.size() - 1).getId() + 1;
    }

    // Orders Method
    public static List<Order> loadOrders() {
        List<Order> orders = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(ORDERS_FILE));
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    try {
                        orders.add(Order.fromString(line));
                    } catch (Exception e) {
                        System.out.println("Invalid order line skipped: " + line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return orders;
    }

    public static void saveOrders(List<Order> orders) {
        ensureFiles();

        List<String> lines = new ArrayList<>();
        for (Order order : orders) {
            lines.add(order.toString());
        }

        try {
            Files.write(Paths.get(ORDERS_FILE), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getNextOrderId(List<Order> orders) {
        if (orders.isEmpty()) {
            return 1;
        }
        return orders.get(orders.size() - 1).getId() + 1;
    }
}

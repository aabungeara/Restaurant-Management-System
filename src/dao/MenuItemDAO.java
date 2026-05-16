package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.MenuItem;
import util.DBConnection;

public class MenuItemDAO {
    // Retrieve all menu items from database
    public static List<MenuItem> getAllMenuItems() throws SQLException {
        List<MenuItem> items = new ArrayList<>();

        String sql = "SELECT * FROM menuitems";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                items.add(new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("category")
                ));
            }
        }

        return items;
    }

    // Insert a new menu item
    public static void insertMenuItem(MenuItem item) throws SQLException {
        String sql = "INSERT INTO menuitems (name, price, category) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getName());
            ps.setDouble(2, item.getPrice());
            ps.setString(3, item.getCategory());

            ps.executeUpdate();
        }
    }

    // Update an existing menu item
    public static void updateMenuItem(MenuItem item) throws SQLException {
        String sql = "UPDATE menuitems SET name = ?, price = ?, category = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getName());
            ps.setDouble(2, item.getPrice());
            ps.setString(3, item.getCategory());
            ps.setInt(4, item.getId());

            ps.executeUpdate();
        }
    }

    // Delete a menu item
    public static void deleteMenuItem(int id) throws SQLException {
        String sql = "DELETE FROM menuitems WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Check duplicate menu item name
    public static boolean menuItemNameExists(String name, int currentId) throws SQLException {
        String sql = "SELECT id FROM menuitems WHERE name = ? AND id <> ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, currentId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}

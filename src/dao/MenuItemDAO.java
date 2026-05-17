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
    public static List<MenuItem> getAllMenuItems(int userId) throws SQLException {
        List<MenuItem> items = new ArrayList<>();

        String sql = "SELECT * FROM menuitems WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection(); 
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    items.add(new MenuItem(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getString("category"),
                            rs.getInt("user_id")
                    ));
                }
            }
        }

        return items;
    }

    // Insert a new menu item
    public static void insertMenuItem(MenuItem item, int userId) throws SQLException {
        String sql = "INSERT INTO menuitems (name, price, category, user_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection(); 
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getName());
            ps.setDouble(2, item.getPrice());
            ps.setString(3, item.getCategory());
            ps.setInt(4, userId);
            
            ps.executeUpdate();
        }
    }

    // Update an existing menu item
    public static void updateMenuItem(MenuItem item, int userId) throws SQLException {
        String sql = "UPDATE menuitems SET name = ?, price = ?, category = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection(); 
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getName());
            ps.setDouble(2, item.getPrice());
            ps.setString(3, item.getCategory());
            ps.setInt(4, item.getId());
            ps.setInt(5, userId);
            
            ps.executeUpdate();
        }
    }

    // Delete a menu item
    public static void deleteMenuItem(int id, int userId) throws SQLException {
        String sql = "DELETE FROM menuitems WHERE id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // Check duplicate menu item name
    public static boolean menuItemNameExists(String name, int currentId, int userId) throws SQLException {
        String sql = "SELECT id FROM menuitems WHERE name = ? AND id <> ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection(); 
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, currentId);
            ps.setInt(3, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}

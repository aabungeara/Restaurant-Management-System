package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.RestaurantTable;
import util.DBConnection;

public class TableDAO {

    // Retrieve all restaurant tables from database
    public static List<RestaurantTable> getAllTables(int userId) throws SQLException {
        List<RestaurantTable> tables = new ArrayList<>();

        String sql = "SELECT * FROM tables WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    tables.add(new RestaurantTable(
                            rs.getInt("id"),
                            rs.getInt("table_number"),
                            rs.getInt("capacity"),
                            rs.getInt("user_id")
                    ));
                }
            }
        }

        return tables;
    }

    // Insert new table into database
    public static void insertTable(RestaurantTable table, int userId) throws SQLException {
        String sql = "INSERT INTO tables (table_number, capacity, user_id) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection(); 
                PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, table.getTableNumber());
            ps.setInt(2, table.getCapacity());
            ps.setInt(3, userId);
            
            ps.executeUpdate();
        }
    }

    // Update selected table in database
    public static void updateTable(RestaurantTable table,  int userId) throws SQLException {
        String sql = "UPDATE tables SET table_number = ?, capacity = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection(); 
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, table.getTableNumber());
            ps.setInt(2, table.getCapacity());
            ps.setInt(3, table.getId());
            ps.setInt(4, userId);
            
            ps.executeUpdate();
        }
    }

    // Delete table from database
    public static void deleteTable(int id, int userId) throws SQLException {
        String sql = "DELETE FROM tables WHERE id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // Check duplicate table number
    public static boolean tableNumberExists(int tableNumber, int currentId, int userId) throws SQLException {
        String sql = "SELECT id FROM tables WHERE table_number = ? AND id <> ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tableNumber);
            ps.setInt(2, currentId);
            ps.setInt(3, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}

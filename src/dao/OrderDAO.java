package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Order;
import util.DBConnection;

public class OrderDAO {

    // Retrieve all orders with table number and item name
    public static List<Order> getAllOrders(int userId) throws SQLException {
        List<Order> orders = new ArrayList<>();

        String sql = """
            SELECT o.id, o.table_id, o.item_id, o.quantity, o.status,
                   o.user_id,
                   t.table_number,
                   m.name AS item_name
            FROM orders o
            JOIN tables t ON o.table_id = t.id
            JOIN menuitems m ON o.item_id = m.id
            WHERE o.user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection(); 
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order(
                            rs.getInt("id"),
                            rs.getInt("table_id"),
                            rs.getInt("item_id"),
                            rs.getInt("quantity"),
                            rs.getString("status"),
                            rs.getInt("user_id")
                    );

                    order.setTableNumber(rs.getInt("table_number"));
                    order.setItemName(rs.getString("item_name"));

                    orders.add(order);
                }
            }
        }

        return orders;
    }

    // Insert new order into database
    public static void insertOrder(Order order, int userId) throws SQLException {
        String sql = "INSERT INTO orders (table_id, item_id, quantity, status, user_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection(); 
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, order.getTableId());
            ps.setInt(2, order.getItemId());
            ps.setInt(3, order.getQuantity());
            ps.setString(4, order.getStatus());
            ps.setInt(5, userId);

            ps.executeUpdate();
        }
    }

    // Update existing order in database
    public static void updateOrder(Order order, int userId) throws SQLException {
        String sql = "UPDATE orders SET table_id = ?, item_id = ?, quantity = ?, status = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, order.getTableId());
            ps.setInt(2, order.getItemId());
            ps.setInt(3, order.getQuantity());
            ps.setString(4, order.getStatus());
            ps.setInt(5, order.getId());
            ps.setInt(6, userId);

            ps.executeUpdate();
        }
    }

    // Delete order from database
    public static void deleteOrder(int id, int userId) throws SQLException {
        String sql = "DELETE FROM orders WHERE id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }
}

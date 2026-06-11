package service;

import repositories.OrderRepo;
import java.sql.SQLException;
import java.util.List;
import model.Order;
import model.User;

public class OrderService {

    public List<Order> getOrders(User user) {
        return OrderRepo.getAllOrders(user.getId());
    }

    public void createOrder(Order order, User user) throws SQLException {
        OrderRepo.insertOrder(order, user.getId());
    }

    public void updateOrder(Order order, User user) throws SQLException {
        OrderRepo.updateOrder(order, user.getId());
    }

    public void updateOrderWithValidation(Order updatedOrder, User user) throws SQLException {

        Order currentOrder = OrderRepo.findById(
                updatedOrder.getId(),
                user.getId()
        );

        if (currentOrder == null) {
            throw new IllegalArgumentException("Order not found.");
        }

        String oldStatus = currentOrder.getStatus();
        String newStatus = updatedOrder.getStatus();

        if (!isValidStatusTransition(oldStatus, newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status transition. Allowed transitions are Pending -> Preparing and Preparing -> Served."
            );
        }

        OrderRepo.updateOrder(updatedOrder, user.getId());
    }

    private boolean isValidStatusTransition(String oldStatus, String newStatus) {

        if (oldStatus == null || newStatus == null) {
            return false;
        }

        if (oldStatus.equals(newStatus)) {
            return true;
        }

        return (oldStatus.equals("Pending") && newStatus.equals("Preparing"))
                || (oldStatus.equals("Preparing") && newStatus.equals("Served"));
    }

    public void deleteOrder(int id, User user) throws SQLException {
        OrderRepo.deleteOrder(id, user.getId());
    }

    public List<Order> getActiveOrders(User user) {
        return OrderRepo.findActiveOrders(user.getId());
    }

    public String generateKitchenSummary(User user) {

        List<Order> activeOrders = getActiveOrders(user);

        if (activeOrders == null || activeOrders.isEmpty()) {
            return "No active orders found.";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("KITCHEN SUMMARY\n");
        sb.append("============================\n\n");

        int currentTableNumber = -1;
        int totalActiveOrders = 0;

        for (Order order : activeOrders) {

            int tableNumber = order.getTable().getTableNumber();

            if (tableNumber != currentTableNumber) {
                currentTableNumber = tableNumber;
                sb.append("Table ")
                        .append(tableNumber)
                        .append("\n");
                sb.append("----------------------------\n");
            }

            sb.append("- ")
                    .append(order.getItem().getName())
                    .append(" x")
                    .append(order.getQuantity())
                    .append(" -- ")
                    .append(order.getStatus())
                    .append("\n");

            totalActiveOrders++;
        }

        sb.append("\nTotal Active Orders: ")
                .append(totalActiveOrders);

        return sb.toString();
    }

    public boolean hasActiveOrdersForTable(int tableId, User user) {
        return OrderRepo.hasActiveOrdersForTable(tableId, user.getId());
    }

    public boolean isTableAvailable(int tableId, User user) {
        return !hasActiveOrdersForTable(tableId, user);
    }

    public boolean hasOrdersForTable(int tableId, User user) {
        return OrderRepo.hasOrdersForTable(tableId, user.getId());
    }
}

package service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.Order;
import model.User;
import repositories.OrderRepo;

public class ReportService {

    public String generateActiveOrdersReport(User user) {

        List<Order> activeOrders = OrderRepo.findActiveOrders(user.getId());

        if (activeOrders == null || activeOrders.isEmpty()) {
            return "No data found for the selected report.";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("ACTIVE ORDERS REPORT\n");
        sb.append("============================\n\n");

        int count = 0;

        for (Order order : activeOrders) {
            sb.append("Table ")
                    .append(order.getTable().getTableNumber())
                    .append(" -- ")
                    .append(order.getItem().getName())
                    .append(" x")
                    .append(order.getQuantity())
                    .append(" -- ")
                    .append(order.getStatus())
                    .append("\n");

            count++;
        }

        sb.append("\nTotal Active Orders: ").append(count);

        return sb.toString();
    }

    public String generateServedOrdersReport(User user) {

        List<Order> servedOrders = OrderRepo.findServedOrders(user.getId());

        if (servedOrders == null || servedOrders.isEmpty()) {
            return "No data found for the selected report.";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("SERVED ORDERS REPORT\n");
        sb.append("============================\n\n");

        int count = 0;
        double totalRevenue = 0;

        for (Order order : servedOrders) {

            double lineTotal = order.getQuantity() * order.getItem().getPrice();

            sb.append("Table ")
                    .append(order.getTable().getTableNumber())
                    .append(" -- ")
                    .append(order.getItem().getName())
                    .append(" x")
                    .append(order.getQuantity())
                    .append(" -- $")
                    .append(String.format("%.2f", lineTotal))
                    .append("\n");

            totalRevenue += lineTotal;
            count++;
        }

        sb.append("\nTotal Served Orders: ").append(count);
        sb.append("\nTotal Revenue: $").append(String.format("%.2f", totalRevenue));

        return sb.toString();
    }

    public String generateMenuItemSalesSummary(User user) {

        List<Order> orders = OrderRepo.findAllOrdersForReports(user.getId());

        if (orders == null || orders.isEmpty()) {
            return "No data found for the selected report.";
        }

        Map<String, Integer> quantityByItem = new LinkedHashMap<>();
        Map<String, Double> revenueByItem = new LinkedHashMap<>();

        for (Order order : orders) {

            String itemName = order.getItem().getName();
            int quantity = order.getQuantity();
            double lineTotal = quantity * order.getItem().getPrice();

            quantityByItem.put(
                    itemName,
                    quantityByItem.getOrDefault(itemName, 0) + quantity
            );

            revenueByItem.put(
                    itemName,
                    revenueByItem.getOrDefault(itemName, 0.0) + lineTotal
            );
        }

        StringBuilder sb = new StringBuilder();

        sb.append("MENU ITEM SALES SUMMARY\n");
        sb.append("============================\n\n");

        for (String itemName : quantityByItem.keySet()) {
            sb.append(itemName)
                    .append(" -- Sold Qty: ")
                    .append(quantityByItem.get(itemName))
                    .append(" -- Revenue: $")
                    .append(String.format("%.2f", revenueByItem.get(itemName)))
                    .append("\n");
        }

        return sb.toString();
    }

    public String generateRevenueSummary(User user) {

    LocalDate today = LocalDate.now();

    double totalRevenue = OrderRepo.calculateServedRevenueByDate(
            user.getId(),
            today
    );

    if (totalRevenue <= 0) {
        return "No data found for the selected report.";
    }

    StringBuilder sb = new StringBuilder();

    sb.append("DAILY REVENUE SUMMARY\n");
    sb.append("============================\n\n");
    sb.append("Date: ").append(today).append("\n");
    sb.append("Revenue is calculated from Served orders for today only.\n\n");
    sb.append("Total Revenue: $")
            .append(String.format("%.2f", totalRevenue));

    return sb.toString();
}

    public String generateReport(String reportType, User user) {

        if (reportType == null || reportType.isBlank()) {
            throw new IllegalArgumentException("Please select a report type.");
        }

        switch (reportType) {

            case "Active Orders":
                return generateActiveOrdersReport(user);

            case "Served Orders":
                return generateServedOrdersReport(user);

            case "Menu Item Sales":
                return generateMenuItemSalesSummary(user);

            case "Revenue Summary":
                return generateRevenueSummary(user);

            default:
                throw new IllegalArgumentException("Invalid report type.");
        }
    }
}

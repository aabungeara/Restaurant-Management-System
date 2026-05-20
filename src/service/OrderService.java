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

    public void deleteOrder(int id, User user) throws SQLException {

        OrderRepo.deleteOrder(id, user.getId());
    }
    
}

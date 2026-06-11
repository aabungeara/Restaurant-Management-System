package repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.sql.SQLException;
import java.util.List;
import model.Order;
import model.RestaurantTable;
import util.JPAUtil;
import java.time.LocalDate;

public class OrderRepo {

    // Retrieve all orders with table number and item name
    public static List<Order> getAllOrders(int userId) {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            TypedQuery<Order> query = em.createQuery(
                    "SELECT o FROM Order o "
                    + "JOIN FETCH o.table "
                    + "JOIN FETCH o.item "
                    + "WHERE o.userId = :uid",
                    Order.class
            );
            query.setParameter("uid", userId);

            return query.getResultList();
        } finally {
            em.close();
        }

    }

    // Insert new order into database
    public static void insertOrder(Order order, int userId) throws SQLException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            order.setUserId(userId);

            em.getTransaction().begin();

            if (order.getOrderDate() == null) {
                order.setOrderDate(java.time.LocalDate.now());
            }

            em.persist(order);

            em.getTransaction().commit();
        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            e.printStackTrace();

        } finally {
            em.close();
        }
    }

    // Update existing order in database
    public static void updateOrder(Order order, int userId) throws SQLException {
        EntityManager em = JPAUtil.getEntityManager();

        try {

            em.getTransaction().begin();

            Order existing = em.find(Order.class, order.getId());

            if (existing != null
                    && existing.getUserId() == userId) {

                existing.setTable(order.getTable());
                existing.setItem(order.getItem());
                existing.setQuantity(order.getQuantity());
                existing.setStatus(order.getStatus());

                em.merge(existing);
            }

            em.getTransaction().commit();

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            e.printStackTrace();

        } finally {
            em.close();
        }
    }

    // Delete order from database
    public static void deleteOrder(int id, int userId) throws SQLException {
        EntityManager em = JPAUtil.getEntityManager();

        try {

            em.getTransaction().begin();

            Order order = em.find(Order.class, id);

            if (order != null
                    && order.getUserId() == userId) {

                em.remove(order);
            }

            em.getTransaction().commit();

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            e.printStackTrace();

        } finally {
            em.close();
        }

    }

    public double calculateTableTotal(RestaurantTable table, int userId) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            Double total = em.createQuery(
                    "SELECT SUM(o.quantity * o.item.price) "
                    + "FROM Order o "
                    + "WHERE o.table = :table "
                    + "AND o.userId = :uid",
                    Double.class
            )
                    .setParameter("table", table)
                    .setParameter("uid", userId)
                    .getSingleResult();

            return total == null ? 0 : total;

        } finally {
            em.close();
        }
    }

    public static List<Order> getOrdersByTable(int tableId, int userId) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT o FROM Order o "
                    + "JOIN FETCH o.item "
                    + "JOIN FETCH o.table "
                    + "WHERE o.table.id = :tid "
                    + "AND o.userId = :uid",
                    Order.class
            )
                    .setParameter("tid", tableId)
                    .setParameter("uid", userId)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public static List<Order> findActiveOrders(int userId) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            return em.createQuery(
                    "SELECT o FROM Order o "
                    + "JOIN FETCH o.table "
                    + "JOIN FETCH o.item "
                    + "WHERE o.userId = :uid "
                    + "AND o.status <> 'Served' "
                    + "ORDER BY o.table.tableNumber, o.status",
                    Order.class
            )
                    .setParameter("uid", userId)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public static Order findById(int orderId, int userId) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            List<Order> result = em.createQuery(
                    "SELECT o FROM Order o "
                    + "JOIN FETCH o.table "
                    + "JOIN FETCH o.item "
                    + "WHERE o.id = :orderId "
                    + "AND o.userId = :uid",
                    Order.class
            )
                    .setParameter("orderId", orderId)
                    .setParameter("uid", userId)
                    .getResultList();

            return result.isEmpty() ? null : result.get(0);

        } finally {
            em.close();
        }
    }

    public static boolean hasActiveOrdersForTable(int tableId, int userId) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            Long count = em.createQuery(
                    "SELECT COUNT(o) FROM Order o "
                    + "WHERE o.table.id = :tableId "
                    + "AND o.userId = :uid "
                    + "AND o.status <> 'Served'",
                    Long.class
            )
                    .setParameter("tableId", tableId)
                    .setParameter("uid", userId)
                    .getSingleResult();

            return count > 0;

        } finally {
            em.close();
        }
    }

    public static boolean hasOrdersForTable(int tableId, int userId) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            Long count = em.createQuery(
                    "SELECT COUNT(o) FROM Order o "
                    + "WHERE o.table.id = :tableId "
                    + "AND o.userId = :uid",
                    Long.class
            )
                    .setParameter("tableId", tableId)
                    .setParameter("uid", userId)
                    .getSingleResult();

            return count > 0;

        } finally {
            em.close();
        }
    }

    public static List<Order> findServedOrders(int userId) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT o FROM Order o "
                    + "JOIN FETCH o.table "
                    + "JOIN FETCH o.item "
                    + "WHERE o.userId = :uid "
                    + "AND o.status = 'Served' "
                    + "ORDER BY o.table.tableNumber",
                    Order.class
            )
                    .setParameter("uid", userId)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public static List<Order> findOrdersByStatus(String status, int userId) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT o FROM Order o "
                    + "JOIN FETCH o.table "
                    + "JOIN FETCH o.item "
                    + "WHERE o.userId = :uid "
                    + "AND o.status = :status "
                    + "ORDER BY o.table.tableNumber",
                    Order.class
            )
                    .setParameter("uid", userId)
                    .setParameter("status", status)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public static List<Order> findAllOrdersForReports(int userId) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT o FROM Order o "
                    + "JOIN FETCH o.table "
                    + "JOIN FETCH o.item "
                    + "WHERE o.userId = :uid "
                    + "ORDER BY o.table.tableNumber, o.status",
                    Order.class
            )
                    .setParameter("uid", userId)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public static double calculateServedRevenue(int userId) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            Double total = em.createQuery(
                    "SELECT SUM(o.quantity * o.item.price) "
                    + "FROM Order o "
                    + "WHERE o.userId = :uid "
                    + "AND o.status = 'Served'",
                    Double.class
            )
                    .setParameter("uid", userId)
                    .getSingleResult();

            return total == null ? 0 : total;

        } finally {
            em.close();
        }
    }
    
    public static List<Order> findOrdersByDate(int userId, LocalDate date) {

    EntityManager em = JPAUtil.getEntityManager();

    try {
        return em.createQuery(
                "SELECT o FROM Order o "
                + "JOIN FETCH o.table "
                + "JOIN FETCH o.item "
                + "WHERE o.userId = :uid "
                + "AND o.orderDate = :date "
                + "ORDER BY o.table.tableNumber, o.status",
                Order.class
        )
                .setParameter("uid", userId)
                .setParameter("date", date)
                .getResultList();

    } finally {
        em.close();
    }
}
    
    public static List<Order> findServedOrdersByDate(int userId, LocalDate date) {

    EntityManager em = JPAUtil.getEntityManager();

    try {
        return em.createQuery(
                "SELECT o FROM Order o "
                + "JOIN FETCH o.table "
                + "JOIN FETCH o.item "
                + "WHERE o.userId = :uid "
                + "AND o.status = 'Served' "
                + "AND o.orderDate = :date "
                + "ORDER BY o.table.tableNumber",
                Order.class
        )
                .setParameter("uid", userId)
                .setParameter("date", date)
                .getResultList();

    } finally {
        em.close();
    }
}
    
    public static double calculateServedRevenueByDate(int userId, LocalDate date) {

    EntityManager em = JPAUtil.getEntityManager();

    try {
        Double total = em.createQuery(
                "SELECT SUM(o.quantity * o.item.price) "
                + "FROM Order o "
                + "WHERE o.userId = :uid "
                + "AND o.status = 'Served' "
                + "AND o.orderDate = :date",
                Double.class
        )
                .setParameter("uid", userId)
                .setParameter("date", date)
                .getSingleResult();

        return total == null ? 0 : total;

    } finally {
        em.close();
    }
}

}

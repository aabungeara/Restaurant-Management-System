package repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.sql.SQLException;
import java.util.List;
import model.Order;
import util.JPAUtil;

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
}

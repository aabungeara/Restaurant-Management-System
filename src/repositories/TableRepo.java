package repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import model.RestaurantTable;
import util.JPAUtil;

public class TableRepo {

    // Retrieve all restaurant tables from database
    public static List<RestaurantTable> getAllTables(int userId) {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            TypedQuery<RestaurantTable> query = em.createQuery(
                    "SELECT t FROM RestaurantTable t "
                    + "JOIN FETCH t.user "
                    + "WHERE t.user.id = :uid",
                    RestaurantTable.class
            );

            query.setParameter("uid", userId);

            return query.getResultList();

        } finally {
            em.close();
        }
    }

    // Insert new table into database
    public static void insertTable(RestaurantTable table, int userId) {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            model.User user = em.find(model.User.class, userId);
            table.setUser(user);

            em.persist(table);

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

    // Update selected table in database
    public static void updateTable(RestaurantTable table, int userId) {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            RestaurantTable existing = em.find(RestaurantTable.class, table.getId());

            if (existing != null && existing.getUser().getId() == userId) {

                existing.setTableNumber(table.getTableNumber());
                existing.setCapacity(table.getCapacity());
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

    // Delete table from database
    public static void deleteTable(int id, int userId) {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            RestaurantTable table = em.find(RestaurantTable.class, id);

            if (table != null && table.getUser().getId() == userId) {
                em.remove(table);
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

    // Check duplicate table number
    public static boolean tableNumberExists(int tableNumber, int currentId, int userId) {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(t) FROM RestaurantTable t "
                    + "WHERE t.tableNumber = :num "
                    + "AND t.id <> :id "
                    + "AND t.user.id = :uid",
                    Long.class
            );

            query.setParameter("num", tableNumber);
            query.setParameter("id", currentId);
            query.setParameter("uid", userId);

            return query.getSingleResult() > 0;

        } finally {
            em.close();
        }
    }
}

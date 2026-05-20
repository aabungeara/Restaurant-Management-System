package repositories;

import java.sql.SQLException;
import java.util.List;
import jakarta.persistence.EntityManager;
import model.MenuItem;
import util.JPAUtil;

public class MenuItemRepo {

    // Retrieve all menu items from database
    public static List<MenuItem> getAllMenuItems(int userId) {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT m FROM MenuItem m WHERE m.user.id = :uid",
                    MenuItem.class
            )
                    .setParameter("uid", userId)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    // Insert a new menu item
    public static void insertMenuItem(MenuItem item, int userId) throws SQLException {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            item.getUser().setId(userId);

            em.persist(item);

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

    // Update an existing menu item
    public static void updateMenuItem(MenuItem item, int userId) throws SQLException {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            MenuItem existing = em.find(MenuItem.class, item.getId());

            if (existing != null && existing.getUser().getId() == userId) {

                existing.setName(item.getName());
                existing.setPrice(item.getPrice());
                existing.setCategory(item.getCategory());

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

    // Delete a menu item
    public static void deleteMenuItem(int id, int userId) {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            MenuItem item = em.find(MenuItem.class, id);

            if (item != null && item.getUser().getId() == userId) {
                em.remove(item);
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

    // Check duplicate menu item name
    public static boolean menuItemNameExists(String name, int currentId, int userId) throws SQLException {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            Long count = em.createQuery(
                    "SELECT COUNT(m) FROM MenuItem m "
                    + "WHERE m.name = :name "
                    + "AND m.id <> :id "
                    + "AND m.user.id = :uid",
                    Long.class
            )
                    .setParameter("name", name)
                    .setParameter("id", currentId)
                    .setParameter("uid", userId)
                    .getSingleResult();

            return count > 0;

        } finally {
            em.close();
        }
    }
}

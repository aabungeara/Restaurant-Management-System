package repositories;

import jakarta.persistence.EntityManager;
import java.util.List;
import model.Bill;
import util.JPAUtil;

public class BillRepo {

    public static List<Bill> getAllBills(int userId) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            return em.createQuery(
                    "SELECT b FROM Bill b "
                    + "JOIN FETCH b.table "
                    + "WHERE b.userId=:uid",
                    Bill.class)
                    .setParameter("uid", userId)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public static void insertBill(Bill bill) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            em.getTransaction().begin();

            em.persist(bill);

            em.getTransaction().commit();

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new RuntimeException("Failed to insert bill.", e);

        } finally {
            em.close();
        }
    }

    public static void updateBill(Bill bill) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            em.getTransaction().begin();

            em.merge(bill);

            em.getTransaction().commit();

        } finally {
            em.close();
        }
    }

    public static void deleteBill(int id) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            em.getTransaction().begin();

            Bill bill = em.find(Bill.class, id);

            if (bill != null) {
                em.remove(bill);
            }

            em.getTransaction().commit();

        } finally {
            em.close();
        }
    }

    public static boolean pendingBillExistsForTable(int tableId, int userId) {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            Long count = em.createQuery(
                    "SELECT COUNT(b) FROM Bill b "
                    + "WHERE b.table.id = :tableId "
                    + "AND b.userId = :uid "
                    + "AND b.paymentStatus <> 'Paid'",
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

    public static boolean billExistsForTable(int tableId, int userId) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            Long count = em.createQuery(
                    "SELECT COUNT(b) FROM Bill b "
                    + "WHERE b.table.id = :tableId "
                    + "AND b.userId = :uid",
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
}

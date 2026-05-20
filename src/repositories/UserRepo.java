package repositories;


import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import model.User;
import util.JPAUtil;

public class UserRepo {
    // Search for a user by email from the users table

    public static User findByEmail(String email){

        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email",
                    User.class
            );

            query.setParameter("email", email);

            return query.getResultStream()
                    .findFirst()
                    .orElse(null);

        } finally {
            em.close();

        }
    }

    public static boolean emailExists(String email) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email",
                    User.class
            );

            query.setParameter("email", email);

            return !query.getResultList().isEmpty();

        } finally {
            em.close();
        }
    }

    public static void insertUser(User user) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            em.persist(user);

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

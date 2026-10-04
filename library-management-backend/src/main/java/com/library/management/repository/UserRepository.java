package com.library.management.repository;

import com.library.management.config.HibernateUtil;
import com.library.management.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class UserRepository {

    private final SessionFactory sessionFactory;

    public UserRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public Optional<User> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.get(User.class, id);
            return Optional.ofNullable(user);
        }
    }

    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("FROM User", User.class)
                    .getResultList();
        }
    }

    public boolean existsByUsername(String username) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery(
                            """
                            SELECT COUNT(u)
                            FROM User u
                            WHERE u.username = :username
                            """,
                            Long.class
                    )
                    .setParameter("username", username)
                    .getSingleResult();

            return count > 0;
        }
    }

    public boolean existsByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery(
                            """
                            SELECT COUNT(u)
                            FROM User u
                            WHERE u.email = :email
                            """,
                            Long.class
                    )
                    .setParameter("email", email)
                    .getSingleResult();

            return count > 0;
        }
    }

    public boolean existsByEmailAndIdNot(String email, Long id) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery(
                            """
                            SELECT COUNT(u)
                            FROM User u
                            WHERE u.email = :email
                              AND u.id <> :id
                            """,
                            Long.class
                    )
                    .setParameter("email", email)
                    .setParameter("id", id)
                    .getSingleResult();

            return count > 0;
        }
    }

    public Optional<User> findByUsername(String username) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.createQuery(
                            """
                            FROM User u
                            WHERE u.username = :username
                            """,
                            User.class
                    )
                    .setParameter("username", username)
                    .setMaxResults(1)
                    .uniqueResult();

            return Optional.ofNullable(user);
        }
    }

    public long countByStatus(String status) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            """
                            SELECT COUNT(u)
                            FROM User u
                            WHERE u.status = :status
                            """,
                            Long.class
                    )
                    .setParameter("status", status)
                    .getSingleResult();
        }
    }

    public User save(User user) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            User result;

            if (user.getId() == null) {
                session.persist(user);
                result = user;
            } else {
                result = session.merge(user);
            }

            transaction.commit();
            return result;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể lưu User",
                    e
            );
        }
    }

    public void deleteById(Long id) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            User user = session.get(User.class, id);

            if (user != null) {
                session.remove(user);
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa User với id: " + id,
                    e
            );
        }
    }

    public boolean existsById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(User.class, id) != null;
        }
    }

    public long count() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "SELECT COUNT(u) FROM User u",
                    Long.class
            ).getSingleResult();
        }
    }
}
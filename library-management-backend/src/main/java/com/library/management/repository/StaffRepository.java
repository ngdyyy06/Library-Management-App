package com.library.management.repository;

import com.library.management.config.HibernateUtil;
import com.library.management.entity.Staff;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class StaffRepository {

    private final SessionFactory sessionFactory;

    public StaffRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public Optional<Staff> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Staff staff = session.get(Staff.class, id);
            return Optional.ofNullable(staff);
        }
    }

    public List<Staff> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("FROM Staff", Staff.class)
                    .getResultList();
        }
    }

    public Optional<Staff> findByUserUsername(String username) {
        try (Session session = sessionFactory.openSession()) {
            Staff staff = session.createQuery(
                            """
                            FROM Staff s
                            WHERE s.user.username = :username
                            """,
                            Staff.class
                    )
                    .setParameter("username", username)
                    .setMaxResults(1)
                    .uniqueResult();

            return Optional.ofNullable(staff);
        }
    }

    public Optional<Staff> findByUserId(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            Staff staff = session.createQuery(
                            """
                            FROM Staff s
                            WHERE s.user.id = :userId
                            """,
                            Staff.class
                    )
                    .setParameter("userId", userId)
                    .setMaxResults(1)
                    .uniqueResult();

            return Optional.ofNullable(staff);
        }
    }

    public boolean existsByUserId(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session.createQuery(
                            """
                            SELECT COUNT(s)
                            FROM Staff s
                            WHERE s.user.id = :userId
                            """,
                            Long.class
                    )
                    .setParameter("userId", userId)
                    .getSingleResult();

            return count > 0;
        }
    }

    public Staff save(Staff staff) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Staff result;

            if (staff.getId() == null) {
                session.persist(staff);
                result = staff;
            } else {
                result = session.merge(staff);
            }

            transaction.commit();
            return result;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể lưu Staff",
                    e
            );
        }
    }

    public void deleteById(Long id) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Staff staff = session.get(Staff.class, id);

            if (staff != null) {
                session.remove(staff);
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa Staff với id: " + id,
                    e
            );
        }
    }

    public boolean existsById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Staff.class, id) != null;
        }
    }

    public long count() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "SELECT COUNT(s) FROM Staff s",
                    Long.class
            ).getSingleResult();
        }
    }
}
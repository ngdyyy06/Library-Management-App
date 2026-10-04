package com.library.management.repository;

import com.library.management.config.HibernateUtil;
import com.library.management.entity.ReturnHistory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class ReturnHistoryRepository {

    private final SessionFactory sessionFactory;

    public ReturnHistoryRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public Optional<ReturnHistory> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            ReturnHistory history = session.get(ReturnHistory.class, id);
            return Optional.ofNullable(history);
        }
    }

    public List<ReturnHistory> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("FROM ReturnHistory", ReturnHistory.class)
                    .getResultList();
        }
    }

    public List<ReturnHistory> findAllByOrderByReturnedAtDesc() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            """
                            FROM ReturnHistory rh
                            ORDER BY rh.returnedAt DESC
                            """,
                            ReturnHistory.class
                    )
                    .getResultList();
        }
    }

    public ReturnHistory save(ReturnHistory history) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            ReturnHistory result;

            if (history.getId() == null) {
                session.persist(history);
                result = history;
            } else {
                result = session.merge(history);
            }

            transaction.commit();
            return result;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể lưu ReturnHistory",
                    e
            );
        }
    }

    public void deleteById(Long id) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            ReturnHistory history =
                    session.get(ReturnHistory.class, id);

            if (history != null) {
                session.remove(history);
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa ReturnHistory với id: " + id,
                    e
            );
        }
    }

    public boolean existsById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(ReturnHistory.class, id) != null;
        }
    }

    public long count() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            "SELECT COUNT(rh) FROM ReturnHistory rh",
                            Long.class
                    )
                    .getSingleResult();
        }
    }
}
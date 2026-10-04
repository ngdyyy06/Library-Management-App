package com.library.management.repository;

import com.library.management.config.HibernateUtil;
import com.library.management.entity.Borrowing;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class BorrowingRepository {

    private final SessionFactory sessionFactory;

    public BorrowingRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    // =========================
    // FIND
    // =========================

    public Optional<Borrowing> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Borrowing borrowing = session.get(Borrowing.class, id);
            return Optional.ofNullable(borrowing);
        }
    }

    public List<Borrowing> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("FROM Borrowing", Borrowing.class)
                    .getResultList();
        }
    }

    // =========================
    // FIND BY READER
    // =========================

    public List<Borrowing> findByReaderId(Long readerId) {
        try (Session session = sessionFactory.openSession()) {

            return session
                    .createQuery(
                            """
                            FROM Borrowing b
                            WHERE b.reader.id = :readerId
                            """,
                            Borrowing.class
                    )
                    .setParameter("readerId", readerId)
                    .getResultList();
        }
    }

    // =========================
    // COUNT BY STATUS
    // =========================

    public long countByStatus(String status) {
        try (Session session = sessionFactory.openSession()) {

            return session
                    .createQuery(
                            """
                            SELECT COUNT(b)
                            FROM Borrowing b
                            WHERE b.status = :status
                            """,
                            Long.class
                    )
                    .setParameter("status", status)
                    .getSingleResult();
        }
    }

    // =========================
    // SAVE
    // =========================

    public Borrowing save(Borrowing borrowing) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {

            transaction = session.beginTransaction();

            Borrowing result;

            if (borrowing.getId() == null) {
                session.persist(borrowing);
                result = borrowing;
            } else {
                result = session.merge(borrowing);
            }

            transaction.commit();

            return result;

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể lưu Borrowing",
                    e
            );
        }
    }

    // =========================
    // DELETE
    // =========================

    public void deleteById(Long id) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {

            transaction = session.beginTransaction();

            Borrowing borrowing = session.get(Borrowing.class, id);

            if (borrowing != null) {
                session.remove(borrowing);
            }

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa Borrowing với id: " + id,
                    e
            );
        }
    }

    // =========================
    // EXISTS
    // =========================

    public boolean existsById(Long id) {
        try (Session session = sessionFactory.openSession()) {

            Borrowing borrowing = session.get(Borrowing.class, id);

            return borrowing != null;
        }
    }

    // =========================
    // COUNT
    // =========================

    public long count() {
        try (Session session = sessionFactory.openSession()) {

            return session
                    .createQuery(
                            "SELECT COUNT(b) FROM Borrowing b",
                            Long.class
                    )
                    .getSingleResult();
        }
    }
}
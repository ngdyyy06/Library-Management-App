package com.library.management.repository;

import com.library.management.config.HibernateUtil;
import com.library.management.entity.BorrowingDetail;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class BorrowingDetailRepository {

    private final SessionFactory sessionFactory;

    public BorrowingDetailRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    // =========================
    // FIND
    // =========================

    public Optional<BorrowingDetail> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            BorrowingDetail detail =
                    session.get(BorrowingDetail.class, id);

            return Optional.ofNullable(detail);
        }
    }

    public List<BorrowingDetail> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            "FROM BorrowingDetail",
                            BorrowingDetail.class
                    )
                    .getResultList();
        }
    }

    // =========================
    // UNRETURNED BOOKS BY READER
    // =========================

    public long countUnreturnedBooksByReaderId(Long readerId) {
        try (Session session = sessionFactory.openSession()) {

            Long result = session
                    .createQuery(
                            """
                            SELECT COALESCE(SUM(bd.quantity), 0)
                            FROM BorrowingDetail bd
                            WHERE bd.borrowing.reader.id = :readerId
                              AND bd.returnedAt IS NULL
                            """,
                            Long.class
                    )
                    .setParameter("readerId", readerId)
                    .getSingleResult();

            return result != null ? result : 0L;
        }
    }

    // =========================
    // UNRETURNED BOOKS BY BORROWING
    // =========================

    public long countUnreturnedBooksByBorrowingId(Long borrowingId) {
        try (Session session = sessionFactory.openSession()) {

            Long result = session
                    .createQuery(
                            """
                            SELECT COALESCE(SUM(bd.quantity), 0)
                            FROM BorrowingDetail bd
                            WHERE bd.borrowing.id = :borrowingId
                              AND bd.returnedAt IS NULL
                            """,
                            Long.class
                    )
                    .setParameter("borrowingId", borrowingId)
                    .getSingleResult();

            return result != null ? result : 0L;
        }
    }

    // =========================
    // TODAY FINE REVENUE
    // =========================

    public long getTodayFineRevenue() {
        try (Session session = sessionFactory.openSession()) {

            Long result = session
                    .createQuery(
                            """
                            SELECT COALESCE(SUM(bd.fine), 0)
                                   + COALESCE(SUM(bd.damageFine), 0)
                            FROM BorrowingDetail bd
                            WHERE bd.returnedAt IS NOT NULL
                              AND FUNCTION('DATE', bd.returnedAt) = CURRENT_DATE
                            """,
                            Long.class
                    )
                    .getSingleResult();

            return result != null ? result : 0L;
        }
    }

    // =========================
    // MONTHLY FINE REVENUE
    // =========================

    public long getMonthlyFineRevenue() {
        try (Session session = sessionFactory.openSession()) {

            Long result = session
                    .createQuery(
                            """
                            SELECT COALESCE(SUM(bd.fine), 0)
                                   + COALESCE(SUM(bd.damageFine), 0)
                            FROM BorrowingDetail bd
                            WHERE bd.returnedAt IS NOT NULL
                              AND YEAR(bd.returnedAt) = YEAR(CURRENT_DATE)
                              AND MONTH(bd.returnedAt) = MONTH(CURRENT_DATE)
                            """,
                            Long.class
                    )
                    .getSingleResult();

            return result != null ? result : 0L;
        }
    }

    // =========================
    // TODAY RETURNED BOOKS
    // =========================

    public long getTodayReturnedBooks() {
        try (Session session = sessionFactory.openSession()) {

            Long result = session
                    .createQuery(
                            """
                            SELECT COALESCE(
                                SUM(
                                    bd.goodQuantity
                                    + bd.damagedQuantity
                                    + bd.lostQuantity
                                ), 0
                            )
                            FROM BorrowingDetail bd
                            WHERE bd.returnedAt IS NOT NULL
                              AND FUNCTION('DATE', bd.returnedAt) = CURRENT_DATE
                            """,
                            Long.class
                    )
                    .getSingleResult();

            return result != null ? result : 0L;
        }
    }

    // =========================
    // FIND BY BORROWING
    // =========================

    public List<BorrowingDetail> findByBorrowingId(Long borrowingId) {
        try (Session session = sessionFactory.openSession()) {

            return session
                    .createQuery(
                            """
                            FROM BorrowingDetail bd
                            WHERE bd.borrowing.id = :borrowingId
                            """,
                            BorrowingDetail.class
                    )
                    .setParameter("borrowingId", borrowingId)
                    .getResultList();
        }
    }

    // =========================
    // COUNT RETURNED
    // =========================

    public long countByReturnedAtIsNotNull() {
        try (Session session = sessionFactory.openSession()) {

            return session
                    .createQuery(
                            """
                            SELECT COUNT(bd)
                            FROM BorrowingDetail bd
                            WHERE bd.returnedAt IS NOT NULL
                            """,
                            Long.class
                    )
                    .getSingleResult();
        }
    }

    // =========================
    // SAVE
    // =========================

    public BorrowingDetail save(BorrowingDetail detail) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {

            transaction = session.beginTransaction();

            BorrowingDetail result;

            if (detail.getId() == null) {
                session.persist(detail);
                result = detail;
            } else {
                result = session.merge(detail);
            }

            transaction.commit();

            return result;

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể lưu BorrowingDetail",
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

            BorrowingDetail detail =
                    session.get(BorrowingDetail.class, id);

            if (detail != null) {
                session.remove(detail);
            }

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa BorrowingDetail với id: " + id,
                    e
            );
        }
    }

    // =========================
    // EXISTS
    // =========================

    public boolean existsById(Long id) {
        try (Session session = sessionFactory.openSession()) {

            BorrowingDetail detail =
                    session.get(BorrowingDetail.class, id);

            return detail != null;
        }
    }

    // =========================
    // COUNT
    // =========================

    public long count() {
        try (Session session = sessionFactory.openSession()) {

            return session
                    .createQuery(
                            "SELECT COUNT(bd) FROM BorrowingDetail bd",
                            Long.class
                    )
                    .getSingleResult();
        }
    }
}
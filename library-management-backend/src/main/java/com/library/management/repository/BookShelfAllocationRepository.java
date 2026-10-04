package com.library.management.repository;

import com.library.management.config.HibernateUtil;
import com.library.management.entity.BookShelfAllocation;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class BookShelfAllocationRepository {

    private final SessionFactory sessionFactory;

    public BookShelfAllocationRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    // =========================
    // FIND
    // =========================

    public Optional<BookShelfAllocation> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            BookShelfAllocation allocation =
                    session.get(BookShelfAllocation.class, id);

            return Optional.ofNullable(allocation);
        }
    }

    public List<BookShelfAllocation> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            "FROM BookShelfAllocation",
                            BookShelfAllocation.class
                    )
                    .getResultList();
        }
    }

    // =========================
    // FIND BY SHELF
    // =========================

    public List<BookShelfAllocation> findByShelfId(Long shelfId) {
        try (Session session = sessionFactory.openSession()) {

            return session
                    .createQuery(
                            """
                            FROM BookShelfAllocation a
                            WHERE a.shelf.id = :shelfId
                            """,
                            BookShelfAllocation.class
                    )
                    .setParameter("shelfId", shelfId)
                    .getResultList();
        }
    }

    // =========================
    // FIND BY BOOK
    // =========================

    public List<BookShelfAllocation> findByBookId(Long bookId) {
        try (Session session = sessionFactory.openSession()) {

            return session
                    .createQuery(
                            """
                            FROM BookShelfAllocation a
                            WHERE a.book.id = :bookId
                            """,
                            BookShelfAllocation.class
                    )
                    .setParameter("bookId", bookId)
                    .getResultList();
        }
    }

    // =========================
    // FIND BY BOOK + SHELF
    // =========================

    public Optional<BookShelfAllocation> findByBookIdAndShelfId(
            Long bookId,
            Long shelfId
    ) {
        try (Session session = sessionFactory.openSession()) {

            BookShelfAllocation allocation = session
                    .createQuery(
                            """
                            FROM BookShelfAllocation a
                            WHERE a.book.id = :bookId
                              AND a.shelf.id = :shelfId
                            """,
                            BookShelfAllocation.class
                    )
                    .setParameter("bookId", bookId)
                    .setParameter("shelfId", shelfId)
                    .setMaxResults(1)
                    .uniqueResult();

            return Optional.ofNullable(allocation);
        }
    }

    // =========================================================
    // TOTAL QUANTITY BY SHELF (ĐÃ FIX LỖI CLASS CAST EXCEPTION)
    // =========================================================

    public Integer getTotalQuantityByShelfId(Long shelfId) {
        try (Session session = sessionFactory.openSession()) {

            Number result = session
                    .createQuery(
                            """
                            SELECT COALESCE(SUM(a.quantity), 0)
                            FROM BookShelfAllocation a
                            WHERE a.shelf.id = :shelfId
                            """,
                            Number.class
                    )
                    .setParameter("shelfId", shelfId)
                    .getSingleResult();

            return result != null ? result.intValue() : 0;

        } catch (Exception e) {
            System.err.println("Error calculating total shelf quantity for shelf " + shelfId + ": " + e.getMessage());
            return 0;
        }
    }

    // =========================
    // SAVE
    // =========================

    public BookShelfAllocation save(
            BookShelfAllocation allocation
    ) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {

            transaction = session.beginTransaction();

            BookShelfAllocation result;

            if (allocation.getId() == null) {
                session.persist(allocation);
                result = allocation;
            } else {
                result = session.merge(allocation);
            }

            transaction.commit();

            return result;

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể lưu BookShelfAllocation",
                    e
            );
        }
    }

    // =========================
    // DELETE BY BOOK + SHELF
    // =========================

    public void deleteByBookIdAndShelfId(
            Long bookId,
            Long shelfId
    ) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {

            transaction = session.beginTransaction();

            session
                    .createMutationQuery(
                            """
                            DELETE FROM BookShelfAllocation a
                            WHERE a.book.id = :bookId
                              AND a.shelf.id = :shelfId
                            """
                    )
                    .setParameter("bookId", bookId)
                    .setParameter("shelfId", shelfId)
                    .executeUpdate();

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa phân bổ sách khỏi kệ",
                    e
            );
        }
    }

    // =========================
    // DELETE BY ID
    // =========================

    public void deleteById(Long id) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {

            transaction = session.beginTransaction();

            BookShelfAllocation allocation =
                    session.get(BookShelfAllocation.class, id);

            if (allocation != null) {
                session.remove(allocation);
            }

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa BookShelfAllocation với id: " + id,
                    e
            );
        }
    }

    // =========================
    // EXISTS
    // =========================

    public boolean existsById(Long id) {
        try (Session session = sessionFactory.openSession()) {

            BookShelfAllocation allocation =
                    session.get(BookShelfAllocation.class, id);

            return allocation != null;
        }
    }

    // =========================
    // COUNT
    // =========================

    public long count() {
        try (Session session = sessionFactory.openSession()) {

            return session
                    .createQuery(
                            "SELECT COUNT(a) FROM BookShelfAllocation a",
                            Long.class
                    )
                    .getSingleResult();
        }
    }
}
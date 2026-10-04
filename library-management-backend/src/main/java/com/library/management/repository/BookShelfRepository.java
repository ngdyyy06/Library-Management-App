package com.library.management.repository;

import com.library.management.config.HibernateUtil;
import com.library.management.entity.BookShelf;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class BookShelfRepository {

    private final SessionFactory sessionFactory;

    public BookShelfRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    // =========================
    // FIND
    // =========================

    public Optional<BookShelf> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            BookShelf shelf = session.createQuery(
                            """
                            SELECT DISTINCT s
                            FROM BookShelf s
                            LEFT JOIN FETCH s.categories
                            WHERE s.id = :id
                            """,
                            BookShelf.class
                    )
                    .setParameter("id", id)
                    .uniqueResult();

            return Optional.ofNullable(shelf);
        } catch (Exception e) {
            System.err.println("Error fetching shelf by id: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<BookShelf> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("FROM BookShelf", BookShelf.class)
                    .getResultList();
        }
    }

    // =========================
    // FIND BY SHELF CODE
    // =========================

    public Optional<BookShelf> findByShelfCode(String shelfCode) {
        try (Session session = sessionFactory.openSession()) {

            BookShelf shelf = session
                    .createQuery(
                            """
                            FROM BookShelf s
                            WHERE s.shelfCode = :shelfCode
                            """,
                            BookShelf.class
                    )
                    .setParameter("shelfCode", shelfCode)
                    .setMaxResults(1)
                    .uniqueResult();

            return Optional.ofNullable(shelf);
        }
    }

    // =========================
    // EXISTS BY SHELF CODE
    // =========================

    public boolean existsByShelfCode(String shelfCode) {
        try (Session session = sessionFactory.openSession()) {

            Long count = session
                    .createQuery(
                            """
                            SELECT COUNT(s)
                            FROM BookShelf s
                            WHERE s.shelfCode = :shelfCode
                            """,
                            Long.class
                    )
                    .setParameter("shelfCode", shelfCode)
                    .getSingleResult();

            return count > 0;
        }
    }

    // =========================
    // FIND BY CATEGORY + STATUS
    // =========================

    public List<BookShelf> findByCategoriesIdAndStatus(
            Long categoryId,
            String status
    ) {
        try (Session session = sessionFactory.openSession()) {

            return session
                    .createQuery(
                            """
                            SELECT DISTINCT s
                            FROM BookShelf s
                            JOIN s.categories c
                            WHERE c.id = :categoryId
                              AND s.status = :status
                            """,
                            BookShelf.class
                    )
                    .setParameter("categoryId", categoryId)
                    .setParameter("status", status)
                    .getResultList();
        }
    }

    // =========================
    // SAVE
    // =========================

    public BookShelf save(BookShelf shelf) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {

            transaction = session.beginTransaction();

            BookShelf result;

            if (shelf.getId() == null) {
                session.persist(shelf);
                result = shelf;
            } else {
                result = session.merge(shelf);
            }

            transaction.commit();

            return result;

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể lưu BookShelf",
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

            BookShelf shelf = session.get(BookShelf.class, id);

            if (shelf != null) {
                session.remove(shelf);
            }

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa BookShelf với id: " + id,
                    e
            );
        }
    }

    // =========================
    // EXISTS
    // =========================

    public boolean existsById(Long id) {
        try (Session session = sessionFactory.openSession()) {

            BookShelf shelf = session.get(BookShelf.class, id);

            return shelf != null;
        }
    }

    // =========================
    // COUNT
    // =========================

    public long count() {
        try (Session session = sessionFactory.openSession()) {

            return session
                    .createQuery(
                            "SELECT COUNT(s) FROM BookShelf s",
                            Long.class
                    )
                    .getSingleResult();
        }
    }
}
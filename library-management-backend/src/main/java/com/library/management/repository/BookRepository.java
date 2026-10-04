package com.library.management.repository;

import com.library.management.config.HibernateUtil;
import com.library.management.entity.Book;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class BookRepository {

    private final SessionFactory sessionFactory;

    public BookRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    // =========================
    // FIND
    // =========================

    public Optional<Book> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Book book = session.get(Book.class, id);
            return Optional.ofNullable(book);
        }
    }

    public List<Book> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            """
                            SELECT DISTINCT b
                            FROM Book b
                            LEFT JOIN FETCH b.authors
                            LEFT JOIN FETCH b.primaryCategory
                            LEFT JOIN FETCH b.publisher
                            LEFT JOIN FETCH b.shelf
                            ORDER BY b.id DESC
                            """,
                            Book.class
                    )
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Error fetching books: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // =========================
    // ISBN
    // =========================

    public boolean existsByIsbn(String isbn) {
        try (Session session = sessionFactory.openSession()) {

            Long count = session
                    .createQuery(
                            "SELECT COUNT(b) FROM Book b WHERE b.isbn = :isbn",
                            Long.class
                    )
                    .setParameter("isbn", isbn)
                    .getSingleResult();

            return count > 0;
        }
    }

    // =========================
    // AUTHOR
    // =========================

    public List<Book> findByAuthorsId(Long authorId) {
        try (Session session = sessionFactory.openSession()) {

            return session
                    .createQuery(
                            """
                            SELECT DISTINCT b
                            FROM Book b
                            JOIN b.authors a
                            WHERE a.id = :authorId
                            """,
                            Book.class
                    )
                    .setParameter("authorId", authorId)
                    .getResultList();
        }
    }

    public List<Book> findBooksByAuthorId(Long authorId) {
        try (Session session = sessionFactory.openSession()) {

            return session
                    .createQuery(
                            """
                            SELECT DISTINCT b
                            FROM Book b
                            JOIN b.authors a
                            WHERE a.id = :authorId
                            """,
                            Book.class
                    )
                    .setParameter("authorId", authorId)
                    .getResultList();
        }
    }

    // =========================
    // CATEGORY
    // =========================

    public List<Book> findByCategoriesId(Long categoryId) {
        try (Session session = sessionFactory.openSession()) {

            return session
                    .createQuery(
                            """
                            SELECT DISTINCT b
                            FROM Book b
                            JOIN b.categories c
                            WHERE c.id = :categoryId
                            """,
                            Book.class
                    )
                    .setParameter("categoryId", categoryId)
                    .getResultList();
        }
    }

    // =========================
    // TOTAL QUANTITY
    // =========================

    public long sumTotalQuantity() {
        try (Session session = sessionFactory.openSession()) {

            Long result = session
                    .createQuery(
                            """
                            SELECT COALESCE(SUM(b.totalQuantity), 0)
                            FROM Book b
                            """,
                            Long.class
                    )
                    .getSingleResult();

            return result != null ? result : 0L;
        }
    }

    // =========================
    // SAVE
    // =========================

    public Book save(Book book) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {

            transaction = session.beginTransaction();

            Book result;

            if (book.getId() == null) {
                session.persist(book);
                result = book;
            } else {
                result = session.merge(book);
            }

            transaction.commit();

            return result;

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể lưu Book",
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

            Book book = session.get(Book.class, id);

            if (book != null) {
                session.remove(book);
            }

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa Book với id: " + id,
                    e
            );
        }
    }

    // =========================
    // EXISTS
    // =========================

    public boolean existsById(Long id) {
        try (Session session = sessionFactory.openSession()) {

            Book book = session.get(Book.class, id);

            return book != null;
        }
    }

    // =========================
    // COUNT
    // =========================

    public long count() {
        try (Session session = sessionFactory.openSession()) {

            return session
                    .createQuery(
                            "SELECT COUNT(b) FROM Book b",
                            Long.class
                    )
                    .getSingleResult();
        }
    }

    public void saveAll(List<Book> books) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {

            transaction = session.beginTransaction();

            for (Book book : books) {
                if (book.getId() == null) {
                    session.persist(book);
                } else {
                    session.merge(book);
                }
            }

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể lưu danh sách Book",
                    e
            );
        }
    }
}
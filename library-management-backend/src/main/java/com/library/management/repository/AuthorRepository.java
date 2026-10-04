package com.library.management.repository;

import com.library.management.config.HibernateUtil;
import com.library.management.entity.Author;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class AuthorRepository {

    private final SessionFactory sessionFactory;

    public AuthorRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public Optional<Author> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Author author = session.get(Author.class, id);
            return Optional.ofNullable(author);
        }
    }

    public List<Author> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("FROM Author", Author.class)
                    .getResultList();
        }
    }

    public Optional<Author> findByNameIgnoreCase(String name) {
        try (Session session = sessionFactory.openSession()) {

            Author author = session
                    .createQuery(
                            "FROM Author a WHERE LOWER(a.name) = LOWER(:name)",
                            Author.class
                    )
                    .setParameter("name", name)
                    .setMaxResults(1)
                    .uniqueResult();

            return Optional.ofNullable(author);
        }
    }

    public Optional<Author> findFirstByNameIgnoreCase(String authorName) {
        return findByNameIgnoreCase(authorName);
    }

    public Author save(Author author) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {

            transaction = session.beginTransaction();

            Author result;

            if (author.getId() == null) {
                session.persist(author);
                result = author;
            } else {
                result = session.merge(author);
            }

            transaction.commit();

            return result;

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể lưu Author",
                    e
            );
        }
    }

    public void deleteById(Long id) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {

            transaction = session.beginTransaction();

            Author author = session.get(Author.class, id);

            if (author != null) {
                session.remove(author);
            }

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa Author với id: " + id,
                    e
            );
        }
    }

    public boolean existsById(Long id) {
        try (Session session = sessionFactory.openSession()) {

            Author author = session.get(Author.class, id);

            return author != null;
        }
    }

    public long count() {
        try (Session session = sessionFactory.openSession()) {

            return session
                    .createQuery(
                            "SELECT COUNT(a) FROM Author a",
                            Long.class
                    )
                    .getSingleResult();
        }
    }
}
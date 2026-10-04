package com.library.management.repository;

import com.library.management.config.HibernateUtil;
import com.library.management.entity.Category;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class CategoryRepository {

    private final SessionFactory sessionFactory;

    public CategoryRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public Optional<Category> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Category category = session.get(Category.class, id);
            return Optional.ofNullable(category);
        }
    }

    public List<Category> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("FROM Category", Category.class)
                    .getResultList();
        }
    }

    public boolean existsByNameIgnoreCase(String name) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session
                    .createQuery(
                            """
                            SELECT COUNT(c)
                            FROM Category c
                            WHERE LOWER(c.name) = LOWER(:name)
                            """,
                            Long.class
                    )
                    .setParameter("name", name)
                    .getSingleResult();

            return count > 0;
        }
    }

    public Optional<Category> findByNameIgnoreCase(String name) {
        try (Session session = sessionFactory.openSession()) {
            Category category = session
                    .createQuery(
                            """
                            FROM Category c
                            WHERE LOWER(c.name) = LOWER(:name)
                            """,
                            Category.class
                    )
                    .setParameter("name", name)
                    .setMaxResults(1)
                    .uniqueResult();

            return Optional.ofNullable(category);
        }
    }

    public Category save(Category category) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Category result;

            if (category.getId() == null) {
                session.persist(category);
                result = category;
            } else {
                result = session.merge(category);
            }

            transaction.commit();
            return result;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể lưu Category", e
            );
        }
    }

    public void deleteById(Long id) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Category category = session.get(Category.class, id);

            if (category != null) {
                session.remove(category);
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa Category với id: " + id, e
            );
        }
    }

    public boolean existsById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Category.class, id) != null;
        }
    }

    public long count() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            "SELECT COUNT(c) FROM Category c",
                            Long.class
                    )
                    .getSingleResult();
        }
    }
}
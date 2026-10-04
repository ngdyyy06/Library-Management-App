package com.library.management.repository;

import com.library.management.config.HibernateUtil;
import com.library.management.entity.Publisher;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class PublisherRepository {

    private final SessionFactory sessionFactory;

    public PublisherRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public Optional<Publisher> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Publisher publisher = session.get(Publisher.class, id);
            return Optional.ofNullable(publisher);
        }
    }

    public List<Publisher> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("FROM Publisher", Publisher.class)
                    .getResultList();
        }
    }

    public boolean existsByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session
                    .createQuery(
                            """
                            SELECT COUNT(p)
                            FROM Publisher p
                            WHERE p.name = :name
                            """,
                            Long.class
                    )
                    .setParameter("name", name)
                    .getSingleResult();

            return count > 0;
        }
    }

    public boolean existsByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session
                    .createQuery(
                            """
                            SELECT COUNT(p)
                            FROM Publisher p
                            WHERE p.email = :email
                            """,
                            Long.class
                    )
                    .setParameter("email", email)
                    .getSingleResult();

            return count > 0;
        }
    }

    public Publisher save(Publisher publisher) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Publisher result;

            if (publisher.getId() == null) {
                session.persist(publisher);
                result = publisher;
            } else {
                result = session.merge(publisher);
            }

            transaction.commit();
            return result;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể lưu Publisher", e
            );
        }
    }

    public void deleteById(Long id) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Publisher publisher = session.get(Publisher.class, id);

            if (publisher != null) {
                session.remove(publisher);
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa Publisher với id: " + id,
                    e
            );
        }
    }

    public boolean existsById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Publisher.class, id) != null;
        }
    }

    public long count() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            "SELECT COUNT(p) FROM Publisher p",
                            Long.class
                    )
                    .getSingleResult();
        }
    }
}
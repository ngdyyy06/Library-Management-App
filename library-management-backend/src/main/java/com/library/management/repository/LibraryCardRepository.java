package com.library.management.repository;

import com.library.management.config.HibernateUtil;
import com.library.management.entity.LibraryCard;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class LibraryCardRepository {

    private final SessionFactory sessionFactory;

    public LibraryCardRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public Optional<LibraryCard> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            LibraryCard card = session.get(LibraryCard.class, id);
            return Optional.ofNullable(card);
        }
    }

    public List<LibraryCard> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("FROM LibraryCard", LibraryCard.class)
                    .getResultList();
        }
    }

    public Optional<LibraryCard> findByCardNumber(String cardNumber) {
        try (Session session = sessionFactory.openSession()) {
            LibraryCard card = session
                    .createQuery(
                            """
                            FROM LibraryCard c
                            WHERE c.cardNumber = :cardNumber
                            """,
                            LibraryCard.class
                    )
                    .setParameter("cardNumber", cardNumber)
                    .setMaxResults(1)
                    .uniqueResult();

            return Optional.ofNullable(card);
        }
    }

    public Optional<LibraryCard> findByReaderId(Long readerId) {
        try (Session session = sessionFactory.openSession()) {
            LibraryCard card = session
                    .createQuery(
                            """
                            FROM LibraryCard c
                            WHERE c.reader.id = :readerId
                            """,
                            LibraryCard.class
                    )
                    .setParameter("readerId", readerId)
                    .setMaxResults(1)
                    .uniqueResult();

            return Optional.ofNullable(card);
        }
    }

    public boolean existsByCardNumber(String cardNumber) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session
                    .createQuery(
                            """
                            SELECT COUNT(c)
                            FROM LibraryCard c
                            WHERE c.cardNumber = :cardNumber
                            """,
                            Long.class
                    )
                    .setParameter("cardNumber", cardNumber)
                    .getSingleResult();

            return count > 0;
        }
    }

    public boolean existsByReaderId(Long readerId) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session
                    .createQuery(
                            """
                            SELECT COUNT(c)
                            FROM LibraryCard c
                            WHERE c.reader.id = :readerId
                            """,
                            Long.class
                    )
                    .setParameter("readerId", readerId)
                    .getSingleResult();

            return count > 0;
        }
    }

    public LibraryCard save(LibraryCard card) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            LibraryCard result;

            if (card.getId() == null) {
                session.persist(card);
                result = card;
            } else {
                result = session.merge(card);
            }

            transaction.commit();
            return result;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể lưu LibraryCard", e
            );
        }
    }

    public void deleteById(Long id) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            LibraryCard card = session.get(LibraryCard.class, id);

            if (card != null) {
                session.remove(card);
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa LibraryCard với id: " + id,
                    e
            );
        }
    }

    public boolean existsById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(LibraryCard.class, id) != null;
        }
    }

    public long count() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            "SELECT COUNT(c) FROM LibraryCard c",
                            Long.class
                    )
                    .getSingleResult();
        }
    }
}
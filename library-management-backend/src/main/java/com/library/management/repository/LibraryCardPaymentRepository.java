package com.library.management.repository;

import com.library.management.config.HibernateUtil;
import com.library.management.entity.LibraryCardPayment;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class LibraryCardPaymentRepository {

    private final SessionFactory sessionFactory;

    public LibraryCardPaymentRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public Optional<LibraryCardPayment> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            LibraryCardPayment payment =
                    session.get(LibraryCardPayment.class, id);

            return Optional.ofNullable(payment);
        }
    }

    public List<LibraryCardPayment> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            "FROM LibraryCardPayment",
                            LibraryCardPayment.class
                    )
                    .getResultList();
        }
    }

    public List<LibraryCardPayment> findByPaidAtBetween(
            LocalDateTime start,
            LocalDateTime end
    ) {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            """
                            FROM LibraryCardPayment p
                            WHERE p.paidAt BETWEEN :start AND :end
                            """,
                            LibraryCardPayment.class
                    )
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        }
    }

    public BigDecimal getRevenueBetween(
            LocalDateTime start,
            LocalDateTime end
    ) {
        try (Session session = sessionFactory.openSession()) {
            BigDecimal result = session
                    .createQuery(
                            """
                            SELECT COALESCE(SUM(p.amount), 0)
                            FROM LibraryCardPayment p
                            WHERE p.paidAt >= :start
                              AND p.paidAt < :end
                            """,
                            BigDecimal.class
                    )
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getSingleResult();

            return result != null ? result : BigDecimal.ZERO;
        }
    }

    public LibraryCardPayment save(LibraryCardPayment payment) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            LibraryCardPayment result;

            if (payment.getId() == null) {
                session.persist(payment);
                result = payment;
            } else {
                result = session.merge(payment);
            }

            transaction.commit();
            return result;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể lưu LibraryCardPayment", e
            );
        }
    }

    public void deleteById(Long id) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            LibraryCardPayment payment =
                    session.get(LibraryCardPayment.class, id);

            if (payment != null) {
                session.remove(payment);
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa LibraryCardPayment với id: " + id,
                    e
            );
        }
    }

    public boolean existsById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(LibraryCardPayment.class, id) != null;
        }
    }

    public long count() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            "SELECT COUNT(p) FROM LibraryCardPayment p",
                            Long.class
                    )
                    .getSingleResult();
        }
    }
}
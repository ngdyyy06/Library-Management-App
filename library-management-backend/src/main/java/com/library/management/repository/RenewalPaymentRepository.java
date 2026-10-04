package com.library.management.repository;

import com.library.management.config.HibernateUtil;
import com.library.management.entity.RenewalPayment;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class RenewalPaymentRepository {

    private final SessionFactory sessionFactory;

    public RenewalPaymentRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public Optional<RenewalPayment> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            RenewalPayment payment =
                    session.get(RenewalPayment.class, id);

            return Optional.ofNullable(payment);
        }
    }

    public List<RenewalPayment> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            "FROM RenewalPayment",
                            RenewalPayment.class
                    )
                    .getResultList();
        }
    }

    public BigDecimal getTodayRenewalRevenue() {
        try (Session session = sessionFactory.openSession()) {
            BigDecimal result = session
                    .createQuery(
                            """
                            SELECT COALESCE(SUM(rp.amount), 0)
                            FROM RenewalPayment rp
                            WHERE FUNCTION('DATE', rp.paidAt) = CURRENT_DATE
                            """,
                            BigDecimal.class
                    )
                    .getSingleResult();

            return result != null ? result : BigDecimal.ZERO;
        }
    }

    public BigDecimal getMonthlyRenewalRevenue() {
        try (Session session = sessionFactory.openSession()) {
            BigDecimal result = session
                    .createQuery(
                            """
                            SELECT COALESCE(SUM(rp.amount), 0)
                            FROM RenewalPayment rp
                            WHERE YEAR(rp.paidAt) = YEAR(CURRENT_DATE)
                              AND MONTH(rp.paidAt) = MONTH(CURRENT_DATE)
                            """,
                            BigDecimal.class
                    )
                    .getSingleResult();

            return result != null ? result : BigDecimal.ZERO;
        }
    }

    public RenewalPayment save(RenewalPayment payment) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            RenewalPayment result;

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
                    "Không thể lưu RenewalPayment",
                    e
            );
        }
    }

    public void deleteById(Long id) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            RenewalPayment payment =
                    session.get(RenewalPayment.class, id);

            if (payment != null) {
                session.remove(payment);
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa RenewalPayment với id: " + id,
                    e
            );
        }
    }

    public boolean existsById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(RenewalPayment.class, id) != null;
        }
    }

    public long count() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            "SELECT COUNT(rp) FROM RenewalPayment rp",
                            Long.class
                    )
                    .getSingleResult();
        }
    }
}
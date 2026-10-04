package com.library.management.repository;

import com.library.management.config.HibernateUtil;
import com.library.management.entity.ImportReceipt;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class ImportReceiptRepository {

    private final SessionFactory sessionFactory;

    public ImportReceiptRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public Optional<ImportReceipt> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            ImportReceipt receipt = session.get(ImportReceipt.class, id);
            return Optional.ofNullable(receipt);
        }
    }

    public List<ImportReceipt> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("FROM ImportReceipt", ImportReceipt.class)
                    .getResultList();
        }
    }

    public boolean existsByReceiptCode(String receiptCode) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session
                    .createQuery(
                            """
                            SELECT COUNT(r)
                            FROM ImportReceipt r
                            WHERE r.receiptCode = :receiptCode
                            """,
                            Long.class
                    )
                    .setParameter("receiptCode", receiptCode)
                    .getSingleResult();

            return count > 0;
        }
    }

    public Optional<ImportReceipt> findByReceiptCode(String receiptCode) {
        try (Session session = sessionFactory.openSession()) {
            ImportReceipt receipt = session
                    .createQuery(
                            """
                            FROM ImportReceipt r
                            WHERE r.receiptCode = :receiptCode
                            """,
                            ImportReceipt.class
                    )
                    .setParameter("receiptCode", receiptCode)
                    .setMaxResults(1)
                    .uniqueResult();

            return Optional.ofNullable(receipt);
        }
    }

    public ImportReceipt save(ImportReceipt receipt) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            ImportReceipt result;

            if (receipt.getId() == null) {
                session.persist(receipt);
                result = receipt;
            } else {
                result = session.merge(receipt);
            }

            transaction.commit();
            return result;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể lưu ImportReceipt", e
            );
        }
    }

    public void deleteById(Long id) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            ImportReceipt receipt = session.get(ImportReceipt.class, id);

            if (receipt != null) {
                session.remove(receipt);
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa ImportReceipt với id: " + id, e
            );
        }
    }

    public boolean existsById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(ImportReceipt.class, id) != null;
        }
    }

    public long count() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            "SELECT COUNT(r) FROM ImportReceipt r",
                            Long.class
                    )
                    .getSingleResult();
        }
    }
}
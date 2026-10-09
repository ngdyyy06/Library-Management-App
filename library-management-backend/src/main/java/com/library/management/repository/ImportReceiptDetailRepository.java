package com.library.management.repository;

import com.library.management.config.HibernateUtil;
import com.library.management.entity.ImportReceiptDetail;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import com.library.management.entity.Category;

import java.util.List;
import java.util.Optional;

public class ImportReceiptDetailRepository {

    private final SessionFactory sessionFactory;

    public ImportReceiptDetailRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public Optional<ImportReceiptDetail> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            ImportReceiptDetail detail =
                    session.get(ImportReceiptDetail.class, id);

            return Optional.ofNullable(detail);
        }
    }

    public List<ImportReceiptDetail> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            "FROM ImportReceiptDetail",
                            ImportReceiptDetail.class
                    )
                    .getResultList();
        }
    }

    public List<ImportReceiptDetail> findByImportReceiptId(
            Long importReceiptId
    ) {
        Session transactionSession = HibernateUtil.getTransactionSession();
        if (transactionSession != null) {
            return transactionSession
                    .createQuery(
                            "FROM ImportReceiptDetail d "
                                    + "WHERE d.importReceipt.id = :importReceiptId",
                            ImportReceiptDetail.class
                    )
                    .setParameter("importReceiptId", importReceiptId)
                    .getResultList();
        }
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            """
                            FROM ImportReceiptDetail d
                            WHERE d.importReceipt.id = :importReceiptId
                            """,
                            ImportReceiptDetail.class
                    )
                    .setParameter("importReceiptId", importReceiptId)
                    .getResultList();
        }
    }

    public boolean existsByImportReceiptIdAndBookId(
            Long importReceiptId,
            Long bookId
    ) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session
                    .createQuery(
                            """
                            SELECT COUNT(d)
                            FROM ImportReceiptDetail d
                            WHERE d.importReceipt.id = :importReceiptId
                              AND d.book.id = :bookId
                            """,
                            Long.class
                    )
                    .setParameter("importReceiptId", importReceiptId)
                    .setParameter("bookId", bookId)
                    .getSingleResult();

            return count > 0;
        }
    }

    public ImportReceiptDetail save(ImportReceiptDetail detail) {
        Session transactionSession = HibernateUtil.getTransactionSession();
        if (transactionSession != null) {
            if (detail.getId() == null) {
                transactionSession.persist(detail);
                return detail;
            }
            return transactionSession.merge(detail);
        }
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            ImportReceiptDetail result;

            if (detail.getId() == null) {
                session.persist(detail);
                result = detail;
            } else {
                result = session.merge(detail);
            }

            transaction.commit();
            return result;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể lưu ImportReceiptDetail", e
            );
        }
    }

    public void deleteById(Long id) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            ImportReceiptDetail detail =
                    session.get(ImportReceiptDetail.class, id);

            if (detail != null) {
                session.remove(detail);
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa ImportReceiptDetail với id: " + id,
                    e
            );
        }
    }

    public boolean existsById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(ImportReceiptDetail.class, id) != null;
        }
    }

    public long count() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            "SELECT COUNT(d) FROM ImportReceiptDetail d",
                            Long.class
                    )
                    .getSingleResult();
        }
    }

    public List<Category> findCategoriesUsedInCompletedImports() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            """
                            SELECT DISTINCT c
                            FROM ImportReceiptDetail d
                            JOIN d.importReceipt r
                            JOIN d.book b
                            JOIN b.categories c
                            WHERE r.status = :status
                            ORDER BY c.name
                            """,
                            Category.class
                    )
                    .setParameter("status", "COMPLETED")
                    .getResultList();
        }
    }
}

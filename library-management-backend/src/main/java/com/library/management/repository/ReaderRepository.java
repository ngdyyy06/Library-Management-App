package com.library.management.repository;

import com.library.management.config.HibernateUtil;
import com.library.management.entity.Reader;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class ReaderRepository {

    private final SessionFactory sessionFactory;

    public ReaderRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public Optional<Reader> findById(Long id) {
        Session transactionSession = HibernateUtil.getTransactionSession();
        if (transactionSession != null) {
            return Optional.ofNullable(transactionSession.get(Reader.class, id));
        }
        try (Session session = sessionFactory.openSession()) {
            Reader reader = session.get(Reader.class, id);
            return Optional.ofNullable(reader);
        }
    }

    public List<Reader> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("FROM Reader", Reader.class)
                    .getResultList();
        }
    }

    public boolean existsByReaderCode(String readerCode) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session
                    .createQuery(
                            """
                            SELECT COUNT(r)
                            FROM Reader r
                            WHERE r.readerCode = :readerCode
                            """,
                            Long.class
                    )
                    .setParameter("readerCode", readerCode)
                    .getSingleResult();

            return count > 0;
        }
    }

    public boolean existsByReaderCodeAndIdNot(
            String readerCode,
            Long id
    ) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session
                    .createQuery(
                            """
                            SELECT COUNT(r)
                            FROM Reader r
                            WHERE r.readerCode = :readerCode
                              AND r.id <> :id
                            """,
                            Long.class
                    )
                    .setParameter("readerCode", readerCode)
                    .setParameter("id", id)
                    .getSingleResult();

            return count > 0;
        }
    }

    public boolean existsByPhone(String phone) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session
                    .createQuery(
                            """
                            SELECT COUNT(r)
                            FROM Reader r
                            WHERE r.phone = :phone
                            """,
                            Long.class
                    )
                    .setParameter("phone", phone)
                    .getSingleResult();

            return count > 0;
        }
    }

    public boolean existsByPhoneAndIdNot(
            String phone,
            Long id
    ) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session
                    .createQuery(
                            """
                            SELECT COUNT(r)
                            FROM Reader r
                            WHERE r.phone = :phone
                              AND r.id <> :id
                            """,
                            Long.class
                    )
                    .setParameter("phone", phone)
                    .setParameter("id", id)
                    .getSingleResult();

            return count > 0;
        }
    }

    public boolean existsByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session
                    .createQuery(
                            """
                            SELECT COUNT(r)
                            FROM Reader r
                            WHERE r.email = :email
                            """,
                            Long.class
                    )
                    .setParameter("email", email)
                    .getSingleResult();

            return count > 0;
        }
    }

    public boolean existsByEmailAndIdNot(
            String email,
            Long id
    ) {
        try (Session session = sessionFactory.openSession()) {
            Long count = session
                    .createQuery(
                            """
                            SELECT COUNT(r)
                            FROM Reader r
                            WHERE r.email = :email
                              AND r.id <> :id
                            """,
                            Long.class
                    )
                    .setParameter("email", email)
                    .setParameter("id", id)
                    .getSingleResult();

            return count > 0;
        }
    }

    public Reader save(Reader reader) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Reader result;

            if (reader.getId() == null) {
                session.persist(reader);
                result = reader;
            } else {
                result = session.merge(reader);
            }

            transaction.commit();
            return result;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể lưu Reader",
                    e
            );
        }
    }

    public void deleteById(Long id) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Reader reader = session.get(Reader.class, id);

            if (reader != null) {
                session.remove(reader);
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa Reader với id: " + id,
                    e
            );
        }
    }

    public boolean existsById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Reader.class, id) != null;
        }
    }

    public long count() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            "SELECT COUNT(r) FROM Reader r",
                            Long.class
                    )
                    .getSingleResult();
        }
    }
}

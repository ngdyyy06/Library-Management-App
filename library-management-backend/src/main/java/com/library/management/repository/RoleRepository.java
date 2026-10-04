package com.library.management.repository;

import com.library.management.config.HibernateUtil;
import com.library.management.entity.Role;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class RoleRepository {

    private final SessionFactory sessionFactory;

    public RoleRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public Optional<Role> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Role role = session.get(Role.class, id);
            return Optional.ofNullable(role);
        }
    }

    public List<Role> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("FROM Role", Role.class)
                    .getResultList();
        }
    }

    public Role save(Role role) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Role result;

            if (role.getId() == null) {
                session.persist(role);
                result = role;
            } else {
                result = session.merge(role);
            }

            transaction.commit();
            return result;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể lưu Role",
                    e
            );
        }
    }

    public void deleteById(Long id) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Role role = session.get(Role.class, id);

            if (role != null) {
                session.remove(role);
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            throw new RuntimeException(
                    "Không thể xóa Role với id: " + id,
                    e
            );
        }
    }

    public boolean existsById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Role.class, id) != null;
        }
    }

    public long count() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery(
                            "SELECT COUNT(r) FROM Role r",
                            Long.class
                    )
                    .getSingleResult();
        }
    }
}
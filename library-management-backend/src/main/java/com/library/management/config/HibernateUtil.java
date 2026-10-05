package com.library.management.config;

import com.library.management.entity.Author;
import com.library.management.entity.Book;
import com.library.management.entity.BookShelf;
import com.library.management.entity.BookShelfAllocation;
import com.library.management.entity.Borrowing;
import com.library.management.entity.BorrowingDetail;
import com.library.management.entity.Category;
import com.library.management.entity.ImportReceipt;
import com.library.management.entity.ImportReceiptDetail;
import com.library.management.entity.LibraryCard;
import com.library.management.entity.LibraryCardPayment;
import com.library.management.entity.Publisher;
import com.library.management.entity.Reader;
import com.library.management.entity.RenewalPayment;
import com.library.management.entity.ReturnHistory;
import com.library.management.entity.Role;
import com.library.management.entity.Staff;
import com.library.management.entity.User;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Supplier;

public class HibernateUtil {

    private static final ThreadLocal<Session> TRANSACTION_SESSION =
            new ThreadLocal<>();

    private static final SessionFactory SESSION_FACTORY =
            buildSessionFactory();

    private static SessionFactory buildSessionFactory() {

        try {
            Properties properties = new Properties();

            try (InputStream input =
                         HibernateUtil.class
                                 .getClassLoader()
                                 .getResourceAsStream(
                                         "hibernate.properties")) {

                if (input == null) {
                    throw new RuntimeException(
                            "Không tìm thấy hibernate.properties"
                    );
                }

                properties.load(input);
            }

            Configuration configuration =
                    new Configuration();

            configuration.setProperties(properties);

            // Đăng ký toàn bộ Entity
            configuration.addAnnotatedClass(Author.class);
            configuration.addAnnotatedClass(Book.class);
            configuration.addAnnotatedClass(BookShelf.class);
            configuration.addAnnotatedClass(BookShelfAllocation.class);
            configuration.addAnnotatedClass(Borrowing.class);
            configuration.addAnnotatedClass(BorrowingDetail.class);
            configuration.addAnnotatedClass(Category.class);
            configuration.addAnnotatedClass(ImportReceipt.class);
            configuration.addAnnotatedClass(ImportReceiptDetail.class);
            configuration.addAnnotatedClass(LibraryCard.class);
            configuration.addAnnotatedClass(LibraryCardPayment.class);
            configuration.addAnnotatedClass(Publisher.class);
            configuration.addAnnotatedClass(Reader.class);
            configuration.addAnnotatedClass(RenewalPayment.class);
            configuration.addAnnotatedClass(ReturnHistory.class);
            configuration.addAnnotatedClass(Role.class);
            configuration.addAnnotatedClass(Staff.class);
            configuration.addAnnotatedClass(User.class);

            return configuration.buildSessionFactory();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Không thể đọc hibernate.properties",
                    e
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Can not create Hibernate SessionFactory",
                    e
            );
        }
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    /**
     * Runs a unit of work in one Hibernate session and one database transaction.
     * Repository methods may obtain this session through getTransactionSession()
     * instead of opening and committing their own transaction.
     */
    public static <T> T inTransaction(Supplier<T> work) {

        if (TRANSACTION_SESSION.get() != null) {
            return work.get();
        }

        try (Session session = SESSION_FACTORY.openSession()) {
            Transaction transaction = session.beginTransaction();
            TRANSACTION_SESSION.set(session);

            try {
                T result = work.get();
                transaction.commit();
                return result;
            } catch (RuntimeException | Error e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            } finally {
                TRANSACTION_SESSION.remove();
            }
        }
    }

    public static Session getTransactionSession() {
        return TRANSACTION_SESSION.get();
    }

    public static void shutdown() {
        if (SESSION_FACTORY != null
                && !SESSION_FACTORY.isClosed()) {

            SESSION_FACTORY.close();
        }
    }
}

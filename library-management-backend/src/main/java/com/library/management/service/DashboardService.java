package com.library.management.service;

import com.library.management.dto.DashboardResponse;
import com.library.management.repository.AuthorRepository;
import com.library.management.repository.BookRepository;
import com.library.management.repository.BorrowingDetailRepository;
import com.library.management.repository.BorrowingRepository;
import com.library.management.repository.CategoryRepository;
import com.library.management.repository.ImportReceiptRepository;
import com.library.management.repository.LibraryCardPaymentRepository;
import com.library.management.repository.PublisherRepository;
import com.library.management.repository.ReaderRepository;
import com.library.management.repository.RenewalPaymentRepository;
import com.library.management.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DashboardService {

    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final CategoryRepository categoryRepository;
    private final BorrowingRepository borrowingRepository;
    private final ImportReceiptRepository importReceiptRepository;
    private final UserRepository userRepository;
    private final BorrowingDetailRepository borrowingDetailRepository;
    private final RenewalPaymentRepository renewalPaymentRepository;
    private final LibraryCardPaymentRepository libraryCardPaymentRepository;

    public DashboardService(
            BookRepository bookRepository,
            ReaderRepository readerRepository,
            AuthorRepository authorRepository,
            PublisherRepository publisherRepository,
            CategoryRepository categoryRepository,
            BorrowingRepository borrowingRepository,
            ImportReceiptRepository importReceiptRepository,
            UserRepository userRepository,
            BorrowingDetailRepository borrowingDetailRepository,
            RenewalPaymentRepository renewalPaymentRepository,
            LibraryCardPaymentRepository libraryCardPaymentRepository) {

        this.bookRepository = bookRepository;
        this.readerRepository = readerRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
        this.categoryRepository = categoryRepository;
        this.borrowingRepository = borrowingRepository;
        this.importReceiptRepository = importReceiptRepository;
        this.userRepository = userRepository;
        this.borrowingDetailRepository = borrowingDetailRepository;
        this.renewalPaymentRepository = renewalPaymentRepository;
        this.libraryCardPaymentRepository = libraryCardPaymentRepository;
    }

    public DashboardResponse getDashboard() {

        // ==============================
        // GENERAL STATISTICS
        // ==============================

        long totalBooks =
                bookRepository.count();

        long totalBookQuantity =
                bookRepository.sumTotalQuantity();

        long totalReaders =
                readerRepository.count();

        long totalAuthors =
                authorRepository.count();

        long totalPublishers =
                publisherRepository.count();

        long totalCategories =
                categoryRepository.count();

        long activeBorrowings =
                borrowingRepository.countByStatus("BORROWING");

        long totalBorrowings =
                borrowingRepository.count();

        long totalImportReceipts =
                importReceiptRepository.count();

        long totalUsers =
                userRepository.count();

        long activeUsers =
                userRepository.countByStatus("ACTIVE");

        long inactiveUsers =
                userRepository.countByStatus("INACTIVE");

        long todayReturns =
                borrowingDetailRepository.getTodayReturnedBooks();


        // ==============================
        // DATE
        // ==============================

        LocalDate today =
                LocalDate.now();

        LocalDateTime startOfToday =
                today.atStartOfDay();

        LocalDateTime startOfTomorrow =
                today.plusDays(1).atStartOfDay();


        // ==============================
        // TODAY - FINE REVENUE
        // ==============================

        BigDecimal todayFineRevenueDecimal =
                borrowingDetailRepository.getTodayFineRevenue();

        long todayFineRevenue =
                todayFineRevenueDecimal != null
                        ? todayFineRevenueDecimal.longValue()
                        : 0L;


        // ==============================
        // TODAY - RENEWAL REVENUE
        // ==============================

        BigDecimal todayRenewalRevenueDecimal =
                renewalPaymentRepository.getTodayRenewalRevenue();

        long todayRenewalRevenue =
                todayRenewalRevenueDecimal != null
                        ? todayRenewalRevenueDecimal.longValue()
                        : 0L;


        // ==============================
        // TODAY - LIBRARY CARD REVENUE
        // ==============================

        BigDecimal todayCardRevenue =
                libraryCardPaymentRepository.getRevenueBetween(
                        startOfToday,
                        startOfTomorrow
                );

        long todayLibraryCardRevenue =
                todayCardRevenue != null
                        ? todayCardRevenue.longValue()
                        : 0L;


        // ==============================
        // TODAY - TOTAL BORROWING REVENUE
        // ==============================

        long todayBorrowingRevenue =
                todayFineRevenue
                        + todayRenewalRevenue;


        // ==============================
        // TODAY - TOTAL REVENUE
        // ==============================

        long todayRevenue =
                todayFineRevenue
                        + todayRenewalRevenue
                        + todayLibraryCardRevenue;


        // ==============================
        // MONTH
        // ==============================

        LocalDate firstDayOfMonth =
                today.withDayOfMonth(1);

        LocalDateTime startOfMonth =
                firstDayOfMonth.atStartOfDay();

        LocalDateTime startOfNextMonth =
                firstDayOfMonth
                        .plusMonths(1)
                        .atStartOfDay();


        // ==============================
        // MONTHLY - FINE REVENUE
        // ==============================

        BigDecimal monthlyFineRevenueDecimal =
                borrowingDetailRepository.getMonthlyFineRevenue();

        long monthlyFineRevenue =
                monthlyFineRevenueDecimal != null
                        ? monthlyFineRevenueDecimal.longValue()
                        : 0L;


        // ==============================
        // MONTHLY - RENEWAL REVENUE
        // ==============================

        BigDecimal monthlyRenewalRevenueDecimal =
                renewalPaymentRepository.getMonthlyRenewalRevenue();

        long monthlyRenewalRevenue =
                monthlyRenewalRevenueDecimal != null
                        ? monthlyRenewalRevenueDecimal.longValue()
                        : 0L;


        // ==============================
        // MONTHLY - LIBRARY CARD REVENUE
        // ==============================

        BigDecimal monthlyCardRevenue =
                libraryCardPaymentRepository.getRevenueBetween(
                        startOfMonth,
                        startOfNextMonth
                );

        long monthlyLibraryCardRevenue =
                monthlyCardRevenue != null
                        ? monthlyCardRevenue.longValue()
                        : 0L;


        // ==============================
        // MONTHLY - TOTAL BORROWING REVENUE
        // ==============================

        long monthlyBorrowingRevenue =
                monthlyFineRevenue
                        + monthlyRenewalRevenue;


        // ==============================
        // MONTHLY - TOTAL REVENUE
        // ==============================

        long monthlyRevenue =
                monthlyFineRevenue
                        + monthlyRenewalRevenue
                        + monthlyLibraryCardRevenue;


        // ==============================
        // RESPONSE
        // ==============================

        return new DashboardResponse(

                // General statistics
                totalBooks,
                totalBookQuantity,
                totalReaders,
                totalAuthors,
                totalPublishers,
                totalCategories,
                activeBorrowings,
                totalBorrowings,
                totalImportReceipts,

                // Users
                totalUsers,
                activeUsers,
                inactiveUsers,

                // Returns
                todayReturns,

                // Today revenue
                todayFineRevenue,
                todayRenewalRevenue,
                todayLibraryCardRevenue,
                todayRevenue,

                // Monthly revenue
                monthlyFineRevenue,
                monthlyRenewalRevenue,
                monthlyLibraryCardRevenue,
                monthlyRevenue
        );
    }
}
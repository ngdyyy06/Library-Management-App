package com.library.management.service;

import com.library.management.dto.StaffDashboardResponse;
import com.library.management.repository.BookRepository;
import com.library.management.repository.BorrowingDetailRepository;
import com.library.management.repository.BorrowingRepository;
import com.library.management.repository.LibraryCardPaymentRepository;
import com.library.management.repository.ReaderRepository;
import com.library.management.repository.RenewalPaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class StaffDashboardService {

    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final BorrowingRepository borrowingRepository;
    private final BorrowingDetailRepository borrowingDetailRepository;
    private final RenewalPaymentRepository renewalPaymentRepository;
    private final LibraryCardPaymentRepository libraryCardPaymentRepository;

    public StaffDashboardService(
            BookRepository bookRepository,
            ReaderRepository readerRepository,
            BorrowingRepository borrowingRepository,
            BorrowingDetailRepository borrowingDetailRepository,
            RenewalPaymentRepository renewalPaymentRepository,
            LibraryCardPaymentRepository libraryCardPaymentRepository) {

        this.bookRepository = bookRepository;
        this.readerRepository = readerRepository;
        this.borrowingRepository = borrowingRepository;
        this.borrowingDetailRepository = borrowingDetailRepository;
        this.renewalPaymentRepository = renewalPaymentRepository;
        this.libraryCardPaymentRepository = libraryCardPaymentRepository;
    }

    public StaffDashboardResponse getDashboard() {

        // =========================
        // GENERAL STATISTICS
        // =========================

        long totalBooks =
                bookRepository.count();

        long totalBookQuantity =
                bookRepository.sumTotalQuantity();

        long totalReaders =
                readerRepository.count();

        long totalBorrowings =
                borrowingRepository.count();

        long totalReturns =
                borrowingDetailRepository.countByReturnedAtIsNotNull();


        // =========================
        // DATE
        // =========================

        LocalDate today =
                LocalDate.now();

        LocalDateTime startOfToday =
                today.atStartOfDay();

        LocalDateTime startOfTomorrow =
                today.plusDays(1).atStartOfDay();


        // =========================
        // REVENUE - TODAY
        // =========================

        BigDecimal todayFineRevenue =
                borrowingDetailRepository.getTodayFineRevenue();

        long todayFineRevenueValue =
                todayFineRevenue != null
                        ? todayFineRevenue.longValue()
                        : 0L;


        BigDecimal todayRenewalRevenue =
                renewalPaymentRepository.getTodayRenewalRevenue();

        long todayRenewalRevenueValue =
                todayRenewalRevenue != null
                        ? todayRenewalRevenue.longValue()
                        : 0L;


        BigDecimal todayCardRevenue =
                libraryCardPaymentRepository.getRevenueBetween(
                        startOfToday,
                        startOfTomorrow
                );

        long todayCardRevenueValue =
                todayCardRevenue != null
                        ? todayCardRevenue.longValue()
                        : 0L;


        long todayRevenue =
                todayFineRevenueValue
                        + todayRenewalRevenueValue
                        + todayCardRevenueValue;


        // =========================
        // REVENUE - THIS MONTH
        // =========================

        BigDecimal monthlyFineRevenue =
                borrowingDetailRepository.getMonthlyFineRevenue();

        long monthlyFineRevenueValue =
                monthlyFineRevenue != null
                        ? monthlyFineRevenue.longValue()
                        : 0L;


        BigDecimal monthlyRenewalRevenue =
                renewalPaymentRepository.getMonthlyRenewalRevenue();

        long monthlyRenewalRevenueValue =
                monthlyRenewalRevenue != null
                        ? monthlyRenewalRevenue.longValue()
                        : 0L;


        LocalDate firstDayOfMonth =
                today.withDayOfMonth(1);

        LocalDateTime startOfMonth =
                firstDayOfMonth.atStartOfDay();

        LocalDateTime startOfNextMonth =
                firstDayOfMonth
                        .plusMonths(1)
                        .atStartOfDay();


        BigDecimal monthlyCardRevenue =
                libraryCardPaymentRepository.getRevenueBetween(
                        startOfMonth,
                        startOfNextMonth
                );

        long monthlyCardRevenueValue =
                monthlyCardRevenue != null
                        ? monthlyCardRevenue.longValue()
                        : 0L;


        long monthlyRevenue =
                monthlyFineRevenueValue
                        + monthlyRenewalRevenueValue
                        + monthlyCardRevenueValue;


        // =========================
        // RESPONSE
        // =========================

        return new StaffDashboardResponse(
                totalBooks,
                totalBookQuantity,
                totalReaders,
                totalBorrowings,
                totalReturns,
                todayRevenue,
                monthlyRevenue
        );
    }
}
package com.library.management.service;

import com.library.management.dto.BorrowingRevenueResponse;
import com.library.management.repository.BorrowingDetailRepository;
import com.library.management.repository.RenewalPaymentRepository;

import java.math.BigDecimal;

public class BorrowingRevenueService {

    private final BorrowingDetailRepository borrowingDetailRepository;
    private final RenewalPaymentRepository renewalPaymentRepository;

    public BorrowingRevenueService(
            BorrowingDetailRepository borrowingDetailRepository,
            RenewalPaymentRepository renewalPaymentRepository) {

        this.borrowingDetailRepository = borrowingDetailRepository;
        this.renewalPaymentRepository = renewalPaymentRepository;
    }

    public BorrowingRevenueResponse getRevenue() {

        // ==========================================
        // TODAY
        // ==========================================

        BigDecimal todayFineRevenueDecimal =
                borrowingDetailRepository.getTodayFineRevenue();

        long todayFineRevenue =
                todayFineRevenueDecimal != null
                        ? todayFineRevenueDecimal.longValue()
                        : 0L;

        BigDecimal todayRenewalRevenue =
                renewalPaymentRepository.getTodayRenewalRevenue();

        long todayRenewalRevenueValue =
                todayRenewalRevenue != null
                        ? todayRenewalRevenue.longValue()
                        : 0L;

        long todayRevenue =
                todayFineRevenue
                        + todayRenewalRevenueValue;

        // ==========================================
        // THIS MONTH
        // ==========================================

        BigDecimal monthlyFineRevenueDecimal =
                borrowingDetailRepository.getMonthlyFineRevenue();

        long monthlyFineRevenue =
                monthlyFineRevenueDecimal != null
                        ? monthlyFineRevenueDecimal.longValue()
                        : 0L;

        BigDecimal monthlyRenewalRevenue =
                renewalPaymentRepository.getMonthlyRenewalRevenue();

        long monthlyRenewalRevenueValue =
                monthlyRenewalRevenue != null
                        ? monthlyRenewalRevenue.longValue()
                        : 0L;

        long monthlyRevenue =
                monthlyFineRevenue
                        + monthlyRenewalRevenueValue;

        // ==========================================
        // RESPONSE
        // ==========================================

        return new BorrowingRevenueResponse(
                todayRevenue,
                monthlyRevenue
        );
    }
}

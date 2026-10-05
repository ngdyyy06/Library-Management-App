package com.library.management.view;

import com.library.management.dto.ReturnBookRequest;
import com.library.management.entity.Book;
import com.library.management.entity.Borrowing;
import com.library.management.entity.BorrowingDetail;
import com.library.management.service.BorrowingService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReturnBookDialog extends Dialog<Void> {

    private static final Logger LOGGER =
            Logger.getLogger(ReturnBookDialog.class.getName());

    // =========================================================
    // CONSTANTS
    // =========================================================

    private static final BigDecimal DAMAGE_FINE =
            new BigDecimal("50000");

    private static final BigDecimal LATE_FINE_PER_DAY =
            new BigDecimal("5000");

    private static final NumberFormat MONEY_FORMAT =
            NumberFormat.getNumberInstance(
                    Locale.forLanguageTag("vi-VN")
            );

    // =========================================================
    // RETURN STATUS
    // =========================================================

    private enum ReturnStatus {

        GOOD("GOOD"),
        DAMAGED("DAMAGED"),
        LOST("LOST");

        private final String displayName;

        ReturnStatus(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    // =========================================================
    // COPY ROW
    // =========================================================

    private static class CopyRow {

        private final BorrowingDetail detail;

        private final int copyNumber;

        private final ComboBox<ReturnStatus> statusComboBox;

        private final Label fineLabel;

        CopyRow(
                BorrowingDetail detail,
                int copyNumber) {

            this.detail = detail;
            this.copyNumber = copyNumber;

            this.statusComboBox =
                    new ComboBox<>();

            this.statusComboBox
                    .getItems()
                    .addAll(
                            ReturnStatus.GOOD,
                            ReturnStatus.DAMAGED,
                            ReturnStatus.LOST
                    );

            this.statusComboBox.setValue(
                    ReturnStatus.GOOD
            );

            this.statusComboBox.setPrefWidth(120);

            this.fineLabel =
                    new Label("0 ₫");

            this.fineLabel.setPrefWidth(110);

            this.fineLabel.setAlignment(
                    Pos.CENTER_RIGHT
            );
        }

        public BorrowingDetail getDetail() {
            return detail;
        }

        public int getCopyNumber() {
            return copyNumber;
        }

        public ComboBox<ReturnStatus> getStatusComboBox() {
            return statusComboBox;
        }

        public Label getFineLabel() {
            return fineLabel;
        }
    }

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ReturnBookDialog(
            Borrowing borrowing,
            List<BorrowingDetail> details,
            BorrowingService borrowingService) {

        setTitle(
                "Process Archival Return — Loan #"
                        + borrowing.getId()
        );

        setHeaderText(
                "Patron: "
                        + borrowing.getReader().getFullName()
                        + " • Due: "
                        + borrowing.getDueDate()
        );

        getDialogPane()
                .getButtonTypes()
                .add(ButtonType.CLOSE);

        // =====================================================
        // MAIN LAYOUT
        // =====================================================

        VBox mainLayout =
                new VBox(16);

        mainLayout.setPadding(
                new Insets(20, 25, 20, 25)
        );

        mainLayout.setPrefWidth(800);

        // =====================================================
        // NOTICE
        // =====================================================

        Label notice =
                new Label(
                        "Select the condition of each returned copy. "
                                + "Damaged: 50,000 VND/copy • "
                                + "Lost: Book value • "
                                + "Late: 5,000 VND/day"
                );

        notice.setWrapText(true);

        notice.setStyle(
                "-fx-font-size: 11.5px;"
                        + "-fx-text-fill: #7C6856;"
                        + "-fx-font-style: italic;"
        );

        mainLayout.getChildren()
                .add(notice);

        // =====================================================
        // COPY ROWS
        // =====================================================

        List<CopyRow> copyRows =
                new ArrayList<>();

        for (BorrowingDetail detail : details) {

            int alreadyReturned =
                    detail.getGoodQuantity()
                            + detail.getDamagedQuantity()
                            + detail.getLostQuantity();

            int remaining =
                    detail.getQuantity()
                            - alreadyReturned;

            if (remaining <= 0) {
                continue;
            }

            VBox bookCard =
                    createBookCard(
                            detail,
                            remaining,
                            copyRows
                    );

            mainLayout.getChildren()
                    .add(bookCard);
        }

        // =====================================================
        // SUMMARY BOX
        // =====================================================

        VBox summaryBox =
                createSummaryBox();

        mainLayout.getChildren()
                .add(summaryBox);

        // =====================================================
        // RETURN BUTTON
        // =====================================================

        Button returnAllButton =
                new Button("RETURN ALL");

        returnAllButton
                .getStyleClass()
                .add("btn-primary");

        returnAllButton.setPrefWidth(150);

        HBox buttonRow =
                new HBox();

        buttonRow.setAlignment(
                Pos.CENTER_RIGHT
        );

        buttonRow.getChildren()
                .add(returnAllButton);

        mainLayout.getChildren()
                .add(buttonRow);

        // =====================================================
        // STATUS CHANGE LISTENERS
        // =====================================================

        for (CopyRow row : copyRows) {

            row.getStatusComboBox()
                    .valueProperty()
                    .addListener(
                            (observable,
                             oldValue,
                             newValue) -> {

                                updateCopyFine(row);

                                updateSummary(
                                        borrowing,
                                        copyRows,
                                        summaryBox
                                );
                            }
                    );
        }

        // =====================================================
        // INITIAL VALUES
        // =====================================================

        for (CopyRow row : copyRows) {

            updateCopyFine(row);
        }

        updateSummary(
                borrowing,
                copyRows,
                summaryBox
        );

        // =====================================================
        // RETURN ALL
        // =====================================================

        returnAllButton.setOnAction(event -> {

            if (copyRows.isEmpty()) {

                showError(
                        "There are no books waiting for return."
                );

                return;
            }

            try {

                processReturn(
                        copyRows,
                        borrowingService
                );

                // Disable all status selectors

                for (CopyRow row : copyRows) {

                    row.getStatusComboBox()
                            .setDisable(true);
                }

                returnAllButton
                        .setDisable(true);

                returnAllButton.setText(
                        "RETURNED ✓"
                );

            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Unable to process book return", ex);

                showError(
                        ex.getMessage() != null
                                ? ex.getMessage()
                                : "Unable to process return."
                );
            }
        });

        // =====================================================
        // SCROLL
        // =====================================================

        ScrollPane scrollPane =
                new ScrollPane(
                        mainLayout
                );

        scrollPane.setFitToWidth(true);

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.setPrefHeight(600);

        scrollPane.setMinHeight(500);

        // =====================================================
        // SET CONTENT BEFORE ANY FUTURE UPDATE
        // =====================================================

        getDialogPane()
                .setContent(scrollPane);

        // =====================================================
        // CSS
        // =====================================================

        if (getClass().getResource(
                "/css/dashboard.css"
        ) != null) {

            getDialogPane()
                    .getStylesheets()
                    .add(
                            getClass()
                                    .getResource(
                                            "/css/dashboard.css"
                                    )
                                    .toExternalForm()
                    );
        }
    }

    // =========================================================
    // CREATE BOOK CARD
    // =========================================================

    private VBox createBookCard(
            BorrowingDetail detail,
            int remaining,
            List<CopyRow> copyRows) {

        VBox card =
                new VBox(8);

        card.getStyleClass()
                .add("stat-card");

        card.setPadding(
                new Insets(14, 16, 14, 16)
        );

        // =====================================================
        // BOOK HEADER
        // =====================================================

        HBox header =
                new HBox(10);

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        Label title =
                new Label(
                        detail.getBook().getTitle()
                );

        title.setWrapText(true);

        title.setStyle(
                "-fx-font-size: 13px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #27160E;"
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label quantity =
                new Label(
                        "Returning: "
                                + remaining
                                + " copy"
                                + (remaining > 1
                                ? "ies"
                                : "")
                );

        quantity.setStyle(
                "-fx-font-size: 11.5px;"
                        + "-fx-text-fill: #C5A059;"
        );

        header.getChildren()
                .addAll(
                        title,
                        spacer,
                        quantity
                );

        card.getChildren()
                .add(header);

        // =====================================================
        // BOOK PRICE
        // =====================================================

        Label price =
                new Label(
                        "Book price: "
                                + formatMoney(
                                getBookPrice(detail)
                        )
                                + " ₫"
                );

        price.setStyle(
                "-fx-font-size: 11px;"
                        + "-fx-text-fill: #7C6856;"
        );

        card.getChildren()
                .add(price);

        // =====================================================
        // TABLE
        // =====================================================

        GridPane table =
                new GridPane();

        table.setHgap(12);

        table.setVgap(7);

        table.setPadding(
                new Insets(7, 0, 0, 0)
        );

        // =====================================================
        // TABLE HEADER
        // =====================================================

        Label copyHeader =
                createColumnHeader("COPY");

        Label statusHeader =
                createColumnHeader("STATUS");

        Label fineHeader =
                createColumnHeader("FINE");

        table.add(
                copyHeader,
                0,
                0
        );

        table.add(
                statusHeader,
                1,
                0
        );

        table.add(
                fineHeader,
                2,
                0
        );

        // =====================================================
        // COLUMN WIDTHS
        // =====================================================

        ColumnConstraints copyColumn =
                new ColumnConstraints();

        copyColumn.setPrefWidth(100);

        ColumnConstraints statusColumn =
                new ColumnConstraints();

        statusColumn.setPrefWidth(180);

        statusColumn.setHgrow(
                Priority.ALWAYS
        );

        ColumnConstraints fineColumn =
                new ColumnConstraints();

        fineColumn.setPrefWidth(130);

        fineColumn.setHalignment(
                javafx.geometry.HPos.RIGHT
        );

        table.getColumnConstraints()
                .addAll(
                        copyColumn,
                        statusColumn,
                        fineColumn
                );

        // =====================================================
        // COPY ROWS
        // =====================================================

        for (int i = 1;
             i <= remaining;
             i++) {

            CopyRow copyRow =
                    new CopyRow(
                            detail,
                            i
                    );

            copyRows.add(copyRow);

            Label copyLabel =
                    new Label(
                            "Copy #" + i
                    );

            copyLabel.setStyle(
                    "-fx-font-size: 12px;"
                            + "-fx-text-fill: #3F2A1F;"
            );

            table.add(
                    copyLabel,
                    0,
                    i
            );

            table.add(
                    copyRow.getStatusComboBox(),
                    1,
                    i
            );

            table.add(
                    copyRow.getFineLabel(),
                    2,
                    i
            );
        }

        card.getChildren()
                .add(table);

        return card;
    }

    // =========================================================
    // CREATE SUMMARY BOX
    // =========================================================

    private VBox createSummaryBox() {

        VBox summary =
                new VBox(7);

        summary.setPadding(
                new Insets(16)
        );

        summary.setStyle(
                "-fx-background-color: #F7F0E4;"
                        + "-fx-border-color: #C5A059;"
                        + "-fx-border-width: 1.2;"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
        );

        // =====================================================
        // TITLE
        // =====================================================

        Label title =
                new Label(
                        "RETURN SUMMARY"
                );

        title.setStyle(
                "-fx-font-size: 13px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #6E432B;"
        );

        summary.getChildren()
                .add(title);

        // =====================================================
        // SELECTED BOOKS
        // =====================================================

        Label selectedBooks =
                new Label();

        selectedBooks.setWrapText(true);

        selectedBooks.setStyle(
                "-fx-font-size: 11.5px;"
                        + "-fx-text-fill: #5F4B3A;"
        );

        // =====================================================
        // CONDITION FINE
        // =====================================================

        Label conditionFine =
                new Label();

        conditionFine.setStyle(
                "-fx-font-size: 11.5px;"
                        + "-fx-text-fill: #5F4B3A;"
        );

        // =====================================================
        // LATE FINE
        // =====================================================

        Label lateFine =
                new Label();

        lateFine.setStyle(
                "-fx-font-size: 11.5px;"
                        + "-fx-text-fill: #5F4B3A;"
        );

        // =====================================================
        // TOTAL FINE
        // =====================================================

        Label totalFine =
                new Label();

        totalFine.setStyle(
                "-fx-font-size: 12px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #8A3D22;"
        );

        // =====================================================
        // DEPOSIT
        // =====================================================

        Label deposit =
                new Label();

        deposit.setStyle(
                "-fx-font-size: 11.5px;"
                        + "-fx-text-fill: #5F4B3A;"
        );

        // =====================================================
        // REFUND
        // =====================================================

        Label refund =
                new Label();

        refund.setStyle(
                "-fx-font-size: 14px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #2A541E;"
        );

        // =====================================================
        // ADDITIONAL PAYMENT
        // =====================================================

        Label additionalPayment =
                new Label();

        additionalPayment.setStyle(
                "-fx-font-size: 11.5px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #8A3D22;"
        );

        // =====================================================
        // ADD TO BOX
        // =====================================================

        summary.getChildren()
                .addAll(
                        selectedBooks,
                        new Separator(),
                        conditionFine,
                        lateFine,
                        totalFine,
                        new Separator(),
                        deposit,
                        refund,
                        additionalPayment
                );

        // =====================================================
        // STORE LABEL REFERENCES
        // =====================================================

        summary.setUserData(
                new Label[]{
                        selectedBooks,
                        conditionFine,
                        lateFine,
                        totalFine,
                        deposit,
                        refund,
                        additionalPayment
                }
        );

        return summary;
    }

    // =========================================================
    // UPDATE SUMMARY
    // =========================================================

    private void updateSummary(
            Borrowing borrowing,
            List<CopyRow> copyRows,
            VBox summaryBox) {

        if (summaryBox == null) {
            return;
        }

        Object userData =
                summaryBox.getUserData();

        if (!(userData instanceof Label[])) {
            return;
        }

        Label[] labels =
                (Label[]) userData;

        if (labels.length < 7) {
            return;
        }

        Label selectedBooks =
                labels[0];

        Label conditionFineLabel =
                labels[1];

        Label lateFineLabel =
                labels[2];

        Label totalFineLabel =
                labels[3];

        Label depositLabel =
                labels[4];

        Label refundLabel =
                labels[5];

        Label additionalPaymentLabel =
                labels[6];

        // =====================================================
        // CALCULATE CONDITION FINE
        // =====================================================

        BigDecimal conditionFine =
                BigDecimal.ZERO;

        StringBuilder selectedText =
                new StringBuilder();

        for (CopyRow row : copyRows) {

            ReturnStatus status =
                    row.getStatusComboBox()
                            .getValue();

            if (status == null) {
                continue;
            }

            Book book =
                    row.getDetail()
                            .getBook();

            BigDecimal fine =
                    getFineForStatus(
                            book,
                            status
                    );

            // -------------------------------------------------
            // Show every returned copy in summary
            // -------------------------------------------------

            selectedText.append(
                    "• "
            );

            selectedText.append(
                    row.getDetail()
                            .getBook()
                            .getTitle()
            );

            selectedText.append(
                    " — Copy #"
            );

            selectedText.append(
                    row.getCopyNumber()
            );

            selectedText.append(
                    " — "
            );

            selectedText.append(
                    status
            );

            selectedText.append(
                    " — "
            );

            selectedText.append(
                    formatMoney(fine)
            );

            selectedText.append(
                    " ₫\n"
            );

            conditionFine =
                    conditionFine.add(
                            fine
                    );
        }

        // =====================================================
        // LATE FINE
        // =====================================================

        BigDecimal lateFine =
                calculateLateFine(
                        borrowing
                );

        // =====================================================
        // TOTAL FINE
        // =====================================================

        BigDecimal totalFine =
                conditionFine.add(
                        lateFine
                );

        // =====================================================
        // DEPOSIT
        // =====================================================

        BigDecimal deposit =
                getDepositAmount(
                        borrowing
                );

        // =====================================================
        // REFUND
        // =====================================================

        BigDecimal refund =
                deposit.subtract(
                        totalFine
                );

        if (refund.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            refund =
                    BigDecimal.ZERO;
        }

        // =====================================================
        // ADDITIONAL PAYMENT
        // =====================================================

        BigDecimal additionalPayment =
                totalFine.subtract(
                        deposit
                );

        if (additionalPayment.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            additionalPayment =
                    BigDecimal.ZERO;
        }

        // =====================================================
        // SELECTED BOOKS
        // =====================================================

        if (selectedText.length() == 0) {

            selectedBooks.setText(
                    "No books selected."
            );

        } else {

            selectedBooks.setText(
                    selectedText.toString()
            );
        }

        // =====================================================
        // CONDITION FINE
        // =====================================================

        conditionFineLabel.setText(
                "Condition fine: "
                        + formatMoney(
                        conditionFine
                )
                        + " ₫"
        );

        // =====================================================
        // LATE FINE
        // =====================================================

        if (lateFine.compareTo(
                BigDecimal.ZERO
        ) > 0) {

            lateFineLabel.setText(
                    "Late fine: "
                            + formatMoney(
                            lateFine
                    )
                            + " ₫"
            );

        } else {

            lateFineLabel.setText(
                    "Late fine: 0 ₫"
            );
        }

        // =====================================================
        // TOTAL
        // =====================================================

        totalFineLabel.setText(
                "TOTAL FINE: "
                        + formatMoney(
                        totalFine
                )
                        + " ₫"
        );

        // =====================================================
        // DEPOSIT
        // =====================================================

        depositLabel.setText(
                "Deposit: "
                        + formatMoney(
                        deposit
                )
                        + " ₫"
        );

        // =====================================================
        // REFUND
        // =====================================================

        refundLabel.setText(
                "REFUND TO PATRON: "
                        + formatMoney(
                        refund
                )
                        + " ₫"
        );

        // =====================================================
        // ADDITIONAL PAYMENT
        // =====================================================

        additionalPaymentLabel.setText(
                "Additional payment required: "
                        + formatMoney(
                        additionalPayment
                )
                        + " ₫"
        );
    }

    // =========================================================
    // UPDATE INDIVIDUAL COPY FINE
    // =========================================================

    private void updateCopyFine(
            CopyRow row) {

        ReturnStatus status =
                row.getStatusComboBox()
                        .getValue();

        if (status == null) {

            row.getFineLabel()
                    .setText("0 ₫");

            return;
        }

        BigDecimal fine =
                getFineForStatus(
                        row.getDetail()
                                .getBook(),
                        status
                );

        row.getFineLabel()
                .setText(
                        formatMoney(fine)
                                + " ₫"
                );

        if (status ==
                ReturnStatus.GOOD) {

            row.getFineLabel()
                    .setStyle(
                            "-fx-font-size: 11.5px;"
                                    + "-fx-text-fill: #2A541E;"
                    );

        } else {

            row.getFineLabel()
                    .setStyle(
                            "-fx-font-size: 11.5px;"
                                    + "-fx-font-weight: bold;"
                                    + "-fx-text-fill: #8A3D22;"
                    );
        }
    }

    // =========================================================
    // GET FINE FOR STATUS
    // =========================================================

    private BigDecimal getFineForStatus(
            Book book,
            ReturnStatus status) {

        if (status == null) {

            return BigDecimal.ZERO;
        }

        switch (status) {

            case GOOD:

                return BigDecimal.ZERO;

            case DAMAGED:

                return DAMAGE_FINE;

            case LOST:

                if (book == null
                        || book.getPrice() == null) {

                    return BigDecimal.ZERO;
                }

                // LOST = exactly the book price

                return book.getPrice();

            default:

                return BigDecimal.ZERO;
        }
    }

    // =========================================================
    // PROCESS RETURN
    // =========================================================

    private void processReturn(
            List<CopyRow> copyRows,
            BorrowingService borrowingService) {

        /*
         * The backend API currently accepts quantities:
         *
         * GOOD
         * DAMAGED
         * LOST
         *
         * Therefore individual copy statuses are aggregated
         * before sending them to BorrowingService.
         *
         * The refund/fine preview shown in this dialog is
         * NOT saved as a refund transaction.
         */

        for (BorrowingDetail detail
                : getDetails(copyRows)) {

            int goodQuantity = 0;

            int damagedQuantity = 0;

            int lostQuantity = 0;

            for (CopyRow row : copyRows) {

                if (row.getDetail() != detail) {
                    continue;
                }

                ReturnStatus status =
                        row.getStatusComboBox()
                                .getValue();

                if (status == null) {
                    continue;
                }

                switch (status) {

                    case GOOD:

                        goodQuantity++;

                        break;

                    case DAMAGED:

                        damagedQuantity++;

                        break;

                    case LOST:

                        lostQuantity++;

                        break;
                }
            }

            int total =
                    goodQuantity
                            + damagedQuantity
                            + lostQuantity;

            if (total <= 0) {
                continue;
            }

            ReturnBookRequest request =
                    new ReturnBookRequest(
                            goodQuantity,
                            damagedQuantity,
                            lostQuantity
                    );

            borrowingService.returnBook(
                    detail.getId(),
                    request
            );
        }
    }

    // =========================================================
    // GET UNIQUE DETAILS
    // =========================================================

    private List<BorrowingDetail> getDetails(
            List<CopyRow> copyRows) {

        List<BorrowingDetail> result =
                new ArrayList<>();

        for (CopyRow row : copyRows) {

            if (!result.contains(
                    row.getDetail()
            )) {

                result.add(
                        row.getDetail()
                );
            }
        }

        return result;
    }

    // =========================================================
    // CALCULATE LATE FINE
    // =========================================================

    private BigDecimal calculateLateFine(
            Borrowing borrowing) {

        if (borrowing == null
                || borrowing.getDueDate() == null) {

            return BigDecimal.ZERO;
        }

        LocalDate dueDate =
                borrowing.getDueDate();

        LocalDate today =
                LocalDate.now();

        if (!today.isAfter(
                dueDate
        )) {

            return BigDecimal.ZERO;
        }

        long overdueDays =
                ChronoUnit.DAYS.between(
                        dueDate,
                        today
                );

        return LATE_FINE_PER_DAY.multiply(
                BigDecimal.valueOf(
                        overdueDays
                )
        );
    }

    // =========================================================
    // GET BOOK PRICE
    // =========================================================

    private BigDecimal getBookPrice(
            BorrowingDetail detail) {

        if (detail == null
                || detail.getBook() == null
                || detail.getBook().getPrice() == null) {

            return BigDecimal.ZERO;
        }

        return detail.getBook().getPrice();
    }

    // =========================================================
    // GET DEPOSIT
    // =========================================================

    private BigDecimal getDepositAmount(
            Borrowing borrowing) {

        if (borrowing == null
                || borrowing.getDepositAmount() == null) {

            return BigDecimal.ZERO;
        }

        return borrowing.getDepositAmount();
    }

    // =========================================================
    // FORMAT MONEY
    // =========================================================

    private String formatMoney(
            BigDecimal amount) {

        if (amount == null) {

            return "0";
        }

        return MONEY_FORMAT.format(
                amount
        );
    }

    // =========================================================
    // COLUMN HEADER
    // =========================================================

    private Label createColumnHeader(
            String text) {

        Label label =
                new Label(text);

        label.setStyle(
                "-fx-font-size: 10.5px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #8A6A45;"
        );

        return label;
    }

    // =========================================================
    // ERROR
    // =========================================================

    private void showError(
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR,
                        message,
                        ButtonType.OK
                );

        alert.setTitle(
                "Return Book"
        );

        alert.setHeaderText(
                "Unable to process return"
        );

        alert.showAndWait();
    }
}

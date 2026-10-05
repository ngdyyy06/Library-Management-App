package com.library.management.view;

import com.library.management.entity.Borrowing;
import com.library.management.entity.BorrowingDetail;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LoanDetailDialog extends Dialog<Void> {

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public LoanDetailDialog(
            Borrowing borrowing,
            List<BorrowingDetail> details) {

        setTitle(
                "Archival Loan Receipt #LOAN-"
                        + borrowing.getId()
        );

        setHeaderText(
                "Patron: "
                        + borrowing.getReader().getFullName()
                        + " ("
                        + borrowing.getReader().getReaderCode()
                        + ") • Due Date: "
                        + borrowing.getDueDate().format(dateFormatter)
                        + " • Status: "
                        + borrowing.getStatus()
        );

        getDialogPane()
                .getButtonTypes()
                .add(ButtonType.CLOSE);

        VBox root =
                new VBox(15);

        root.setPadding(
                new Insets(15, 20, 15, 20)
        );

        root.setPrefWidth(680);

        // =====================================================
        // CHECK PRINT PERMISSION
        // =====================================================

        boolean canPrint =
                !"RETURNED".equalsIgnoreCase(
                        borrowing.getStatus()
                );

        // =====================================================
        // 1. BẢNG CHI TIẾT SÁCH MƯỢN
        // =====================================================

        TableView<BorrowingDetail> table =
                new TableView<>();

        table.getStyleClass()
                .add("vintage-table");

        table.setPrefHeight(230);

        TableColumn<BorrowingDetail, String> colBook =
                new TableColumn<>(
                        "BOOK TITLE & VOLUME"
                );

        colBook.setPrefWidth(260);

        colBook.setCellValueFactory(
                d -> new SimpleStringProperty(
                        d.getValue()
                                .getBook()
                                .getTitle()
                )
        );

        TableColumn<BorrowingDetail, String> colQty =
                new TableColumn<>("QTY");

        colQty.setPrefWidth(60);

        colQty.setCellValueFactory(
                d -> new SimpleStringProperty(
                        String.valueOf(
                                d.getValue()
                                        .getQuantity()
                        )
                )
        );

        TableColumn<BorrowingDetail, String> colReturned =
                new TableColumn<>("RETURNED GOOD");

        colReturned.setPrefWidth(120);

        colReturned.setCellValueFactory(
                d -> new SimpleStringProperty(
                        String.valueOf(
                                d.getValue()
                                        .getGoodQuantity()
                        )
                )
        );

        TableColumn<BorrowingDetail, String> colPrice =
                new TableColumn<>("SECURITY PRICE");

        colPrice.setPrefWidth(120);

        colPrice.setCellValueFactory(
                d -> new SimpleStringProperty(
                        d.getValue()
                                .getBook()
                                .getPrice()
                                .toPlainString()
                                + " VND"
                )
        );

        table.getColumns()
                .addAll(
                        colBook,
                        colQty,
                        colReturned,
                        colPrice
                );

        table.setItems(
                FXCollections.observableArrayList(
                        details
                )
        );

        // =====================================================
        // 2. TỔNG HỢP VÀ NÚT IN
        // =====================================================

        HBox bottomBar =
                new HBox(15);

        bottomBar.setAlignment(
                Pos.CENTER_RIGHT
        );

        bottomBar.setPadding(
                new Insets(5, 0, 0, 0)
        );

        Label depositSummary =
                new Label(
                        "Held Deposit: "
                                + borrowing
                                .getDepositAmount()
                                .toPlainString()
                                + " VND"
                );

        depositSummary.setStyle(
                "-fx-font-family: 'Georgia', serif;"
                        + "-fx-font-weight: bold;"
                        + "-fx-font-size: 13px;"
                        + "-fx-text-fill: #C5A059;"
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        // =====================================================
        // PRINT BUTTON
        // =====================================================

        Button printReceiptBtn =
                new Button(
                        "PRINT ARCHIVAL RECEIPT"
                );

        printReceiptBtn
                .getStyleClass()
                .add("btn-primary");

        FontIcon printIcon =
                new FontIcon(
                        FontAwesomeSolid.PRINT
                );

        printIcon.setIconSize(12);

        printIcon.setIconColor(
                javafx.scene.paint.Color.web(
                        "#F8F3EB"
                )
        );

        printReceiptBtn.setGraphic(
                printIcon
        );

        printReceiptBtn.setGraphicTextGap(8);

        // =====================================================
        // KHÔNG CHO IN PHIẾU ĐÃ RETURNED
        // =====================================================

        if (!canPrint) {

            printReceiptBtn.setDisable(true);

            printReceiptBtn.setText(
                    "PRINT UNAVAILABLE"
            );

            printReceiptBtn.setTooltip(
                    new Tooltip(
                            "This loan has already been returned. "
                                    + "Returned loans cannot be printed."
                    )
            );
        }

        printReceiptBtn.setOnAction(e -> {

            if (!canPrint) {

                showPrintUnavailableAlert();

                return;
            }

            openPrintReceiptPreview(
                    borrowing,
                    details
            );
        });

        // =====================================================
        // STATUS NOTICE
        // =====================================================

        Label statusNotice =
                new Label();

        if (!canPrint) {

            statusNotice.setText(
                    "This loan has been RETURNED. "
                            + "Printing and exporting the loan receipt "
                            + "are no longer available."
            );

            statusNotice.setWrapText(true);

            statusNotice.setStyle(
                    "-fx-font-size: 11px;"
                            + "-fx-font-style: italic;"
                            + "-fx-text-fill: #8D2B1B;"
            );

        } else {

            statusNotice.setText(
                    "Loan receipt is available for printing."
            );

            statusNotice.setStyle(
                    "-fx-font-size: 11px;"
                            + "-fx-font-style: italic;"
                            + "-fx-text-fill: #7C6856;"
            );
        }

        bottomBar.getChildren()
                .addAll(
                        depositSummary,
                        spacer,
                        printReceiptBtn
                );

        root.getChildren()
                .addAll(
                        table,
                        statusNotice,
                        bottomBar
                );

        getDialogPane()
                .setContent(root);

        // =====================================================
        // CSS
        // =====================================================

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

    // =========================================================
    // HỘP THOẠI XEM TRƯỚC VÀ IN BIÊN LAI
    // =========================================================

    private void openPrintReceiptPreview(
            Borrowing borrowing,
            List<BorrowingDetail> details) {

        // -----------------------------------------------------
        // SAFETY CHECK
        // -----------------------------------------------------

        if ("RETURNED".equalsIgnoreCase(
                borrowing.getStatus())) {

            showPrintUnavailableAlert();

            return;
        }

        Dialog<Void> printDialog =
                new Dialog<>();

        printDialog.setTitle(
                "Archival Circulation Receipt Preview"
        );

        printDialog
                .getDialogPane()
                .getButtonTypes()
                .add(ButtonType.CLOSE);

        // =====================================================
        // RECEIPT SHEET
        // =====================================================

        VBox receiptSheet =
                new VBox(12);

        receiptSheet.setPrefWidth(480);

        receiptSheet.setPadding(
                new Insets(24)
        );

        receiptSheet.setStyle(
                "-fx-background-color: #FFFFFF; "
                        + "-fx-border-color: #27160E; "
                        + "-fx-border-width: 1.5; "
                        + "-fx-effect: dropshadow("
                        + "gaussian, rgba(0,0,0,0.15), "
                        + "10, 0, 0, 2);"
        );

        // =====================================================
        // HEADER
        // =====================================================

        Label headerLogo =
                new Label(
                        "ATELIER REPOSITORIUM"
                );

        headerLogo.setStyle(
                "-fx-font-family: 'Georgia', serif;"
                        + "-fx-font-size: 16px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #27160E;"
        );

        Label subLogo =
                new Label(
                        "OFFICIAL ARCHIVAL CIRCULATION VOUCHER"
                );

        subLogo.setStyle(
                "-fx-font-size: 9.5px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #7C6856;"
                        + "-fx-letter-spacing: 1.5px;"
        );

        VBox brandBox =
                new VBox(
                        2,
                        headerLogo,
                        subLogo
                );

        brandBox.setAlignment(
                Pos.CENTER
        );

        Separator div1 =
                new Separator();

        div1.setStyle(
                "-fx-background-color: #27160E;"
        );

        // =====================================================
        // THÔNG TIN PHIẾU
        // =====================================================

        GridPane metaGrid =
                new GridPane();

        metaGrid.setHgap(15);
        metaGrid.setVgap(6);

        metaGrid.add(
                createBoldLabel(
                        "VOUCHER NUMBER:"
                ),
                0,
                0
        );

        metaGrid.add(
                new Label(
                        "#LOAN-"
                                + borrowing.getId()
                ),
                1,
                0
        );

        metaGrid.add(
                createBoldLabel(
                        "PATRON NAME:"
                ),
                0,
                1
        );

        metaGrid.add(
                new Label(
                        borrowing.getReader()
                                .getFullName()
                                .toUpperCase()
                ),
                1,
                1
        );

        metaGrid.add(
                createBoldLabel(
                        "PATRON ID / PHONE:"
                ),
                0,
                2
        );

        metaGrid.add(
                new Label(
                        borrowing.getReader()
                                .getReaderCode()
                                + " • "
                                + borrowing.getReader()
                                .getPhone()
                ),
                1,
                2
        );

        metaGrid.add(
                createBoldLabel(
                        "CHECK OUT DATE:"
                ),
                0,
                3
        );

        metaGrid.add(
                new Label(
                        borrowing.getBorrowedAt()
                                .format(timeFormatter)
                ),
                1,
                3
        );

        metaGrid.add(
                createBoldLabel(
                        "DUE RETURN DEADLINE:"
                ),
                0,
                4
        );

        Label dueDateLabel =
                new Label(
                        borrowing.getDueDate()
                                .format(dateFormatter)
                );

        dueDateLabel.setStyle(
                "-fx-font-weight: bold;"
                        + "-fx-text-fill: #8D2B1B;"
        );

        metaGrid.add(
                dueDateLabel,
                1,
                4
        );

        Separator div2 =
                new Separator();

        // =====================================================
        // DANH SÁCH SÁCH
        // =====================================================

        VBox booksList =
                new VBox(6);

        Label listTitle =
                new Label(
                        "ALLOCATED VOLUMES:"
                );

        listTitle.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #5A4030;"
        );

        booksList.getChildren()
                .add(listTitle);

        int index = 1;

        for (BorrowingDetail d : details) {

            HBox itemRow =
                    new HBox(8);

            Label numLabel =
                    new Label(
                            index++ + "."
                    );

            numLabel.setPrefWidth(20);

            Label bookTitleLabel =
                    new Label(
                            d.getBook()
                                    .getTitle()
                    );

            bookTitleLabel.setStyle(
                    "-fx-font-weight: bold;"
                            + "-fx-text-fill: #27160E;"
            );

            Region sp =
                    new Region();

            HBox.setHgrow(
                    sp,
                    Priority.ALWAYS
            );

            Label qtyLabel =
                    new Label(
                            d.getQuantity()
                                    + " copy(s)"
                    );

            qtyLabel.setStyle(
                    "-fx-font-family: 'Consolas', monospace;"
            );

            itemRow.getChildren()
                    .addAll(
                            numLabel,
                            bookTitleLabel,
                            sp,
                            qtyLabel
                    );

            booksList.getChildren()
                    .add(itemRow);
        }

        Separator div3 =
                new Separator();

        // =====================================================
        // TIỀN CỌC
        // =====================================================

        HBox depositBox =
                new HBox();

        Label depositTitle =
                createBoldLabel(
                        "HELD SECURITY DEPOSIT:"
                );

        Region sp2 =
                new Region();

        HBox.setHgrow(
                sp2,
                Priority.ALWAYS
        );

        Label depositVal =
                new Label(
                        borrowing
                                .getDepositAmount()
                                .toPlainString()
                                + " VND"
                );

        depositVal.setStyle(
                "-fx-font-family: 'Consolas', monospace;"
                        + "-fx-font-weight: bold;"
                        + "-fx-font-size: 13px;"
        );

        depositBox.getChildren()
                .addAll(
                        depositTitle,
                        sp2,
                        depositVal
                );

        // =====================================================
        // NOTICE
        // =====================================================

        Label notice =
                new Label(
                        "Notice: Patron agrees to return volumes "
                                + "by the deadline in good standing. "
                                + "Damaged copies incur a 50,000 VND "
                                + "indemnity fee. Lost copies require "
                                + "full book reimbursement."
                );

        notice.setWrapText(true);

        notice.setStyle(
                "-fx-font-size: 9.5px;"
                        + "-fx-font-style: italic;"
                        + "-fx-text-fill: #7C6856;"
        );

        // =====================================================
        // CHỮ KÝ
        // =====================================================

        HBox signRow =
                new HBox(50);

        signRow.setPadding(
                new Insets(25, 10, 10, 10)
        );

        signRow.setAlignment(
                Pos.CENTER
        );

        VBox patronSign =
                new VBox(
                        40,
                        createBoldLabel(
                                "PATRON SIGNATURE"
                        ),
                        new Label("(Signed)")
                );

        patronSign.setAlignment(
                Pos.CENTER
        );

        Region signSpacer =
                new Region();

        HBox.setHgrow(
                signSpacer,
                Priority.ALWAYS
        );

        VBox curatorSign =
                new VBox(
                        40,
                        createBoldLabel(
                                "CURATOR IN CHARGE"
                        ),
                        new Label("(Verified)")
                );

        curatorSign.setAlignment(
                Pos.CENTER
        );

        signRow.getChildren()
                .addAll(
                        patronSign,
                        signSpacer,
                        curatorSign
                );

        receiptSheet.getChildren()
                .addAll(
                        brandBox,
                        div1,
                        metaGrid,
                        div2,
                        booksList,
                        div3,
                        depositBox,
                        notice,
                        signRow
                );

        // =====================================================
        // ACTION BUTTONS
        // =====================================================

        HBox actionsRow =
                new HBox(12);

        actionsRow.setAlignment(
                Pos.CENTER_RIGHT
        );

        actionsRow.setPadding(
                new Insets(10, 0, 0, 0)
        );

        // =====================================================
        // EXPORT TXT
        // =====================================================

        Button exportTxtBtn =
                new Button(
                        "EXPORT TEXT RECEIPT"
                );

        exportTxtBtn
                .getStyleClass()
                .add("btn-secondary");

        exportTxtBtn.setOnAction(e -> {

            if ("RETURNED".equalsIgnoreCase(
                    borrowing.getStatus())) {

                showPrintUnavailableAlert();

                return;
            }

            FileChooser chooser =
                    new FileChooser();

            chooser.setTitle(
                    "Save Receipt Document"
            );

            chooser.setInitialFileName(
                    "Receipt_Loan_"
                            + borrowing.getId()
                            + ".txt"
            );

            File file =
                    chooser.showSaveDialog(
                            printDialog
                                    .getDialogPane()
                                    .getScene()
                                    .getWindow()
                    );

            if (file != null) {

                try (
                        FileWriter writer =
                                new FileWriter(file)
                ) {

                    writer.write(
                            "====================================================\n"
                    );

                    writer.write(
                            "       ATELIER ARCHIVAL CIRCULATION VOUCHER         \n"
                    );

                    writer.write(
                            "====================================================\n"
                    );

                    writer.write(
                            "Voucher: #LOAN-"
                                    + borrowing.getId()
                                    + "\n"
                    );

                    writer.write(
                            "Status: "
                                    + borrowing.getStatus()
                                    + "\n"
                    );

                    writer.write(
                            "Patron: "
                                    + borrowing.getReader()
                                    .getFullName()
                                    + " ("
                                    + borrowing.getReader()
                                    .getReaderCode()
                                    + ")\n"
                    );

                    writer.write(
                            "Issued: "
                                    + borrowing.getBorrowedAt()
                                    .format(timeFormatter)
                                    + "\n"
                    );

                    writer.write(
                            "Due Date: "
                                    + borrowing.getDueDate()
                                    .format(dateFormatter)
                                    + "\n"
                    );

                    writer.write(
                            "Deposit: "
                                    + borrowing
                                    .getDepositAmount()
                                    .toPlainString()
                                    + " VND\n"
                    );

                    writer.write(
                            "----------------------------------------------------\n"
                    );

                    writer.write(
                            "VOLUMES:\n"
                    );

                    for (
                            BorrowingDetail d
                            : details
                    ) {

                        writer.write(
                                "- "
                                        + d.getBook()
                                        .getTitle()
                                        + " (Qty: "
                                        + d.getQuantity()
                                        + ")\n"
                        );
                    }

                    writer.write(
                            "====================================================\n"
                    );

                    writer.write(
                            "Signatures: Patron [       ]   "
                                    + "Curator [       ]\n"
                    );

                } catch (Exception ex) {

                    Alert alert =
                            new Alert(
                                    Alert.AlertType.ERROR,
                                    "Export failed: "
                                            + ex.getMessage(),
                                    ButtonType.OK
                            );

                    alert.showAndWait();
                }
            }
        });

        // =====================================================
        // PRINT NOW
        // =====================================================

        Button printNowBtn =
                new Button(
                        "EXECUTE HARDCOPY PRINT"
                );

        printNowBtn
                .getStyleClass()
                .add("btn-primary");

        FontIcon pIcon =
                new FontIcon(
                        FontAwesomeSolid.PRINT
                );

        pIcon.setIconSize(12);

        pIcon.setIconColor(
                javafx.scene.paint.Color.web(
                        "#F8F3EB"
                )
        );

        printNowBtn.setGraphic(
                pIcon
        );

        printNowBtn.setGraphicTextGap(8);

        printNowBtn.setOnAction(e -> {

            if ("RETURNED".equalsIgnoreCase(
                    borrowing.getStatus())) {

                showPrintUnavailableAlert();

                return;
            }

            PrinterJob job =
                    PrinterJob.createPrinterJob();

            if (
                    job != null
                            && job.showPrintDialog(
                            printDialog
                                    .getDialogPane()
                                    .getScene()
                                    .getWindow()
                    )
            ) {

                boolean success =
                        job.printPage(
                                receiptSheet
                        );

                if (success) {

                    job.endJob();

                    printDialog.close();
                }
            }
        });

        actionsRow.getChildren()
                .addAll(
                        exportTxtBtn,
                        printNowBtn
                );

        // =====================================================
        // SCROLL
        // =====================================================

        ScrollPane scroll =
                new ScrollPane(
                        new VBox(
                                14,
                                receiptSheet,
                                actionsRow
                        )
                );

        scroll.setFitToWidth(true);

        scroll.setPrefHeight(500);

        printDialog
                .getDialogPane()
                .setContent(scroll);

        printDialog
                .getDialogPane()
                .getStylesheets()
                .add(
                        getClass()
                                .getResource(
                                        "/css/dashboard.css"
                                )
                                .toExternalForm()
                );

        printDialog.showAndWait();
    }

    // =========================================================
    // PRINT UNAVAILABLE ALERT
    // =========================================================

    private void showPrintUnavailableAlert() {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING,
                        "This loan has already been returned. "
                                + "A returned loan cannot be printed "
                                + "or exported as a loan receipt.",
                        ButtonType.OK
                );

        alert.setTitle(
                "Printing Unavailable"
        );

        alert.setHeaderText(
                "Loan Already Returned"
        );

        alert.showAndWait();
    }

    // =========================================================
    // BOLD LABEL
    // =========================================================

    private Label createBoldLabel(
            String text) {

        Label label =
                new Label(text);

        label.setStyle(
                "-fx-font-family: 'Segoe UI', sans-serif;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #5A4030;"
        );

        return label;
    }
}
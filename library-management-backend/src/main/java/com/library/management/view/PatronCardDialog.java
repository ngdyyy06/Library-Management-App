package com.library.management.view;

import com.library.management.entity.Borrowing;
import com.library.management.entity.BorrowingDetail;
import com.library.management.entity.LibraryCard;
import com.library.management.entity.Reader;
import com.library.management.repository.BorrowingDetailRepository;
import com.library.management.repository.BorrowingRepository;
import com.library.management.repository.LibraryCardRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PatronCardDialog extends Dialog<Void> {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public PatronCardDialog(
            Reader reader,
            LibraryCardRepository libraryCardRepository,
            BorrowingRepository borrowingRepository,
            BorrowingDetailRepository borrowingDetailRepository) {

        setTitle("Library Fellowship Card • " + reader.getFullName());
        setHeaderText("Official Membership Certificate & Borrowing Privileges");

        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        LibraryCard card = libraryCardRepository.findByReaderId(reader.getId()).orElse(null);

        // =====================================================
        // TẤM THẺ THƯ VIỆN CỔ ĐIỂN
        // =====================================================
        VBox cardContainer = new VBox(16);
        cardContainer.setStyle(
                "-fx-background-color: #27160E; " +
                        "-fx-border-color: #C5A059; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-padding: 24 28; " +
                        "-fx-effect: dropshadow(gaussian, rgba(39, 22, 14, 0.35), 14, 0, 0, 4);"
        );
        cardContainer.setPrefWidth(540);

        // Header thẻ
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label orgName = new Label("ATELIER REPOSITORIUM");
        orgName.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #C5A059; -fx-letter-spacing: 2px;");

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Label statusBadge = new Label(card != null ? card.getStatus().name() : "INACTIVE");
        statusBadge.setStyle(
                "-fx-background-color: #4A2D1C; -fx-text-fill: #FFF7EC; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 12px; -fx-border-color: #C5A059; -fx-border-width: 0.8; -fx-border-radius: 12px;"
        );
        topRow.getChildren().addAll(orgName, topSpacer, statusBadge);

        // Tên độc giả
        Label patronName = new Label(reader.getFullName().toUpperCase());
        patronName.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 22px; -fx-text-fill: #FFF7EC; -fx-padding: 6 0 0 0;");

        Separator cardDivider = new Separator();
        cardDivider.setStyle("-fx-background-color: #4A2D1C;");

        // Thông tin thẻ
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);

        Label lblCardNum = new Label("CARD NUMBER:");
        lblCardNum.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #B89C82; -fx-font-weight: bold;");
        Label valCardNum = new Label(card != null ? card.getCardNumber() : "— Not Assigned —");
        valCardNum.setStyle("-fx-font-size: 13px; -fx-text-fill: #FFF7EC; -fx-font-family: 'Consolas', monospace; -fx-font-weight: bold;");

        Label lblCode = new Label("PATRON ID:");
        lblCode.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #B89C82; -fx-font-weight: bold;");
        Label valCode = new Label(reader.getReaderCode());
        valCode.setStyle("-fx-font-size: 13px; -fx-text-fill: #FFF7EC; -fx-font-family: 'Consolas', monospace;");

        Label lblIssued = new Label("ISSUED DATE:");
        lblIssued.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #B89C82; -fx-font-weight: bold;");
        Label valIssued = new Label(card != null ? card.getIssuedAt().format(dateFormatter) : "—");
        valIssued.setStyle("-fx-font-size: 12px; -fx-text-fill: #E8D5B7;");

        Label lblExpiry = new Label("VALID THRU:");
        lblExpiry.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #B89C82; -fx-font-weight: bold;");
        Label valExpiry = new Label(card != null ? card.getExpiredAt().format(dateFormatter) : "—");
        valExpiry.setStyle("-fx-font-size: 12px; -fx-text-fill: #C5A059; -fx-font-weight: bold;");

        grid.add(lblCardNum, 0, 0);
        grid.add(valCardNum, 1, 0);

        grid.add(lblCode, 2, 0);
        grid.add(valCode, 3, 0);

        grid.add(lblIssued, 0, 1);
        grid.add(valIssued, 1, 1);

        grid.add(lblExpiry, 2, 1);
        grid.add(valExpiry, 3, 1);

        // Nút mở lịch sử mượn
        HBox actionRow = new HBox();
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.setPadding(new Insets(10, 0, 0, 0));

        Button viewHistoryBtn = new Button("VIEW CIRCULATION & LOAN HISTORY");
        viewHistoryBtn.setStyle(
                "-fx-background-color: #4A2D1C; " +
                        "-fx-text-fill: #FFF7EC; " +
                        "-fx-border-color: #C5A059; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 4px; " +
                        "-fx-background-radius: 4px; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 7 14; " +
                        "-fx-cursor: hand;"
        );

        FontIcon histIcon = new FontIcon(FontAwesomeSolid.HISTORY);
        histIcon.setIconSize(11);
        histIcon.setIconColor(javafx.scene.paint.Color.web("#C5A059"));
        viewHistoryBtn.setGraphic(histIcon);

        viewHistoryBtn.setOnAction(e -> {
            showBorrowingHistoryDialog(reader, borrowingRepository, borrowingDetailRepository);
        });

        actionRow.getChildren().add(viewHistoryBtn);

        cardContainer.getChildren().addAll(topRow, patronName, cardDivider, grid, actionRow);

        VBox layout = new VBox(cardContainer);
        layout.setPadding(new Insets(15));
        getDialogPane().setContent(layout);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
    }

    // =========================================================
    // HỘP THOẠI XEM LỊCH SỬ MƯỢN TRẢ NỘI BỘ
    // =========================================================
    private void showBorrowingHistoryDialog(
            Reader reader,
            BorrowingRepository borrowingRepository,
            BorrowingDetailRepository borrowingDetailRepository) {

        Dialog<Void> histDialog = new Dialog<>();
        histDialog.setTitle("Circulation & Loan History • " + reader.getFullName());
        histDialog.setHeaderText("Patron Archival Dossier: Active Borrowings & Past Loans");
        histDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox root = new VBox(14);
        root.setPadding(new Insets(15, 20, 15, 20));
        root.setPrefWidth(700);

        List<Borrowing> readerBorrowings = borrowingRepository.findByReaderId(reader.getId());
        List<LoanHistoryItem> historyList = new ArrayList<>();

        for (Borrowing b : readerBorrowings) {
            List<BorrowingDetail> details = borrowingDetailRepository.findByBorrowingId(b.getId());
            for (BorrowingDetail d : details) {
                int outstanding = d.getQuantity() - (d.getGoodQuantity() + d.getDamagedQuantity() + d.getLostQuantity());
                String statusText = outstanding > 0 ? "HOLDING (" + outstanding + " COPIES)" : "RETURNED";
                if ("OVERDUE".equalsIgnoreCase(b.getStatus()) && outstanding > 0) {
                    statusText = "OVERDUE (" + outstanding + " COPIES)";
                }

                historyList.add(new LoanHistoryItem(
                        "#LOAN-" + b.getId(),
                        d.getBook() != null ? d.getBook().getTitle() : "null",
                        String.valueOf(d.getQuantity()),
                        b.getBorrowedAt() != null ? b.getBorrowedAt().format(dateFormatter) : "null",
                        b.getDueDate() != null ? b.getDueDate().format(dateFormatter) : "null",
                        d.getReturnedAt() != null ? d.getReturnedAt().format(dateFormatter) : "null",
                        statusText
                ));
            }
        }

        // Nếu độc giả chưa từng mượn bản ghi nào -> Thêm 1 dòng null rõ ràng
        if (historyList.isEmpty()) {
            historyList.add(new LoanHistoryItem(
                    "null",
                    "null",
                    "null",
                    "null",
                    "null",
                    "null",
                    "NO LOANS RECORDED"
            ));
        }

        TableView<LoanHistoryItem> table = new TableView<>();
        table.getStyleClass().add("vintage-table");
        table.setPrefHeight(260);

        TableColumn<LoanHistoryItem, String> colLoanId = new TableColumn<>("LOAN ID");
        colLoanId.setPrefWidth(90);
        colLoanId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLoanId()));

        TableColumn<LoanHistoryItem, String> colTitle = new TableColumn<>("BOOK VOLUME TITLE");
        colTitle.setPrefWidth(240);
        colTitle.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookTitle()));

        TableColumn<LoanHistoryItem, String> colQty = new TableColumn<>("QTY");
        colQty.setPrefWidth(60);
        colQty.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getQuantity()));

        TableColumn<LoanHistoryItem, String> colBorrowed = new TableColumn<>("BORROWED");
        colBorrowed.setPrefWidth(100);
        colBorrowed.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBorrowedDate()));

        TableColumn<LoanHistoryItem, String> colDue = new TableColumn<>("DUE DATE");
        colDue.setPrefWidth(100);
        colDue.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDueDate()));

        TableColumn<LoanHistoryItem, String> colReturned = new TableColumn<>("RETURNED ON");
        colReturned.setPrefWidth(105);
        colReturned.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReturnedDate()));

        TableColumn<LoanHistoryItem, String> colState = new TableColumn<>("CIRCULATION STATUS");
        colState.setPrefWidth(140);
        colState.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        table.getColumns().addAll(colLoanId, colTitle, colQty, colBorrowed, colDue, colReturned, colState);

        ObservableList<LoanHistoryItem> observableList = FXCollections.observableArrayList(historyList);
        table.setItems(observableList);

        root.getChildren().add(table);
        histDialog.getDialogPane().setContent(root);
        histDialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
        histDialog.showAndWait();
    }

    // =========================================================
    // MODEL CLASS WITH GETTERS
    // =========================================================
    public static class LoanHistoryItem {
        private final String loanId;
        private final String bookTitle;
        private final String quantity;
        private final String borrowedDate;
        private final String dueDate;
        private final String returnedDate;
        private final String status;

        public LoanHistoryItem(String loanId, String bookTitle, String quantity, String borrowedDate, String dueDate, String returnedDate, String status) {
            this.loanId = loanId;
            this.bookTitle = bookTitle;
            this.quantity = quantity;
            this.borrowedDate = borrowedDate;
            this.dueDate = dueDate;
            this.returnedDate = returnedDate;
            this.status = status;
        }

        public String getLoanId() {
            return loanId;
        }

        public String getBookTitle() {
            return bookTitle;
        }

        public String getQuantity() {
            return quantity;
        }

        public String getBorrowedDate() {
            return borrowedDate;
        }

        public String getDueDate() {
            return dueDate;
        }

        public String getReturnedDate() {
            return returnedDate;
        }

        public String getStatus() {
            return status;
        }
    }
}
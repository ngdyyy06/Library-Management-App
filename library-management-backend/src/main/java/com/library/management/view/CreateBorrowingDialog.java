package com.library.management.view;

import com.library.management.dto.CreateBorrowingRequest;
import com.library.management.entity.Book;
import com.library.management.entity.Reader;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CreateBorrowingDialog extends Dialog<CreateBorrowingRequest> {

    private final ObservableList<SelectedBookItem> selectedBooks = FXCollections.observableArrayList();
    private final Label depositTotalLabel = new Label("Total Security Deposit: 0 VND");
    private final Label totalBooksCountLabel = new Label("Selected: 0 / 5 volumes");

    public CreateBorrowingDialog(List<Reader> allReaders, List<Book> allBooks) {
        setTitle("Issue Archival Loan Receipt");
        setHeaderText("Select patron fellowship and allocate library volumes (Max 5 books • Due in 7 days).");

        ButtonType checkoutBtn = new ButtonType("EXECUTE CHECK OUT", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(checkoutBtn, ButtonType.CANCEL);

        VBox layout = new VBox(16);
        layout.setPadding(new Insets(20, 25, 20, 25));
        layout.setPrefWidth(650);

        // 1. Patron Selection
        HBox patronRow = new HBox(12);
        patronRow.setAlignment(Pos.CENTER_LEFT);
        Label patronLabel = new Label("PATRON FELLOWSHIP *");
        patronLabel.setPrefWidth(160);

        ComboBox<Reader> readerCombo = new ComboBox<>();
        readerCombo.getItems().addAll(allReaders.stream().filter(r -> "ACTIVE".equalsIgnoreCase(r.getStatus())).toList());
        readerCombo.setPrefWidth(420);
        readerCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Reader r) {
                return r != null ? r.getFullName() + " (" + r.getReaderCode() + ") — " + r.getPhone() : "Choose an active patron...";
            }

            @Override
            public Reader fromString(String string) {
                return null;
            }
        });
        patronRow.getChildren().addAll(patronLabel, readerCombo);

        // 2. Add Book to Loan Row
        HBox bookAddRow = new HBox(10);
        bookAddRow.setAlignment(Pos.CENTER_LEFT);

        ComboBox<Book> bookCombo = new ComboBox<>();
        bookCombo.getItems().addAll(allBooks.stream().filter(b -> "ACTIVE".equalsIgnoreCase(b.getStatus()) && b.getAvailableQuantity() > 0).toList());
        bookCombo.setPrefWidth(340);
        bookCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Book b) {
                return b != null ? b.getTitle() + " (Avail: " + b.getAvailableQuantity() + " • " + b.getPrice() + " VND)" : "Select available volume...";
            }

            @Override
            public Book fromString(String string) {
                return null;
            }
        });

        Spinner<Integer> qtySpinner = new Spinner<>(1, 5, 1);
        qtySpinner.setPrefWidth(80);

        Button addToListBtn = new Button("+ ADD VOLUME");
        addToListBtn.getStyleClass().add("btn-secondary");

        bookAddRow.getChildren().addAll(bookCombo, qtySpinner, addToListBtn);

        // 3. Table of Selected Books
        TableView<SelectedBookItem> table = new TableView<>();
        table.getStyleClass().add("vintage-table");
        table.setPrefHeight(200);

        TableColumn<SelectedBookItem, String> colTitle = new TableColumn<>("BOOK VOLUME");
        colTitle.setPrefWidth(280);
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().book.getTitle()));

        TableColumn<SelectedBookItem, String> colQty = new TableColumn<>("QTY");
        colQty.setPrefWidth(70);
        colQty.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().quantity)));

        TableColumn<SelectedBookItem, String> colPrice = new TableColumn<>("UNIT PRICE");
        colPrice.setPrefWidth(120);
        colPrice.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().book.getPrice().toPlainString() + " VND"));

        TableColumn<SelectedBookItem, Void> colRemove = new TableColumn<>("REMOVE");
        colRemove.setPrefWidth(80);
        colRemove.setCellFactory(param -> new TableCell<>() {
            private final Button removeBtn = new Button("✕");
            {
                removeBtn.getStyleClass().add("table-cell-btn");
                removeBtn.setOnAction(e -> {
                    SelectedBookItem item = getTableRow().getItem();
                    if (item != null) {
                        selectedBooks.remove(item);
                        updateSummary();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
                setAlignment(Pos.CENTER);
            }
        });

        table.getColumns().addAll(colTitle, colQty, colPrice, colRemove);
        table.setItems(selectedBooks);

        // Add to list action
        addToListBtn.setOnAction(e -> {
            Book book = bookCombo.getValue();
            int qty = qtySpinner.getValue();
            if (book == null) return;

            // Check if already in list
            boolean exists = selectedBooks.stream().anyMatch(item -> item.book.getId().equals(book.getId()));
            if (exists) {
                return;
            }

            int currentTotal = selectedBooks.stream().mapToInt(item -> item.quantity).sum();
            if (currentTotal + qty > 5) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Cannot exceed maximum 5 volumes per borrowing transaction.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            selectedBooks.add(new SelectedBookItem(book, qty));
            updateSummary();
        });

        // 4. Summary Row
        HBox summaryRow = new HBox(20, totalBooksCountLabel, depositTotalLabel);
        summaryRow.setAlignment(Pos.CENTER_LEFT);
        totalBooksCountLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #5A4030;");
        depositTotalLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #C5A059;");

        layout.getChildren().addAll(patronRow, new Separator(), bookAddRow, table, summaryRow);
        getDialogPane().setContent(layout);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());

        setResultConverter(dialogButton -> {
            if (dialogButton == checkoutBtn) {
                Reader r = readerCombo.getValue();
                if (r == null || selectedBooks.isEmpty()) {
                    return null;
                }

                CreateBorrowingRequest req = new CreateBorrowingRequest();
                req.setReaderId(r.getId());

                List<CreateBorrowingRequest.BookBorrowItem> items = new ArrayList<>();
                for (SelectedBookItem item : selectedBooks) {
                    CreateBorrowingRequest.BookBorrowItem bi = new CreateBorrowingRequest.BookBorrowItem();
                    bi.setBookId(item.book.getId());
                    bi.setQuantity(item.quantity);
                    items.add(bi);
                }
                req.setBooks(items);
                return req;
            }
            return null;
        });
    }

    private void updateSummary() {
        int totalQty = selectedBooks.stream().mapToInt(item -> item.quantity).sum();
        BigDecimal totalDeposit = BigDecimal.ZERO;
        for (SelectedBookItem item : selectedBooks) {
            BigDecimal subtotal = item.book.getPrice().multiply(BigDecimal.valueOf(item.quantity));
            totalDeposit = totalDeposit.add(subtotal);
        }

        totalBooksCountLabel.setText("Selected: " + totalQty + " / 5 volumes");
        depositTotalLabel.setText("Total Security Deposit: " + totalDeposit.toPlainString() + " VND");
    }

    private static class SelectedBookItem {
        Book book;
        int quantity;

        public SelectedBookItem(Book book, int quantity) {
            this.book = book;
            this.quantity = quantity;
        }
    }
}
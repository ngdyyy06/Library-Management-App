package com.library.management.view;

import com.library.management.dto.BookShelfDetailResponse;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class ShelfDetailDialog extends Dialog<Void> {

    public ShelfDetailDialog(BookShelfDetailResponse detail) {
        setTitle("Shelf Inventory Breakdown: " + detail.getShelfCode());
        setHeaderText(detail.getName() + " — Occupied: " + detail.getUsedCapacity()
                + " / " + detail.getMaxCapacity() + " volumes (" + detail.getAvailableCapacity() + " spots free)");

        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<BookShelfDetailResponse.BookAllocationResponse> table = new TableView<>();
        table.getStyleClass().add("vintage-table");
        table.setPrefHeight(300);
        table.setPrefWidth(550);

        TableColumn<BookShelfDetailResponse.BookAllocationResponse, String> colIsbn = new TableColumn<>("ISBN");
        colIsbn.setPrefWidth(140);
        colIsbn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIsbn()));

        TableColumn<BookShelfDetailResponse.BookAllocationResponse, String> colTitle = new TableColumn<>("BOOK TITLE");
        colTitle.setPrefWidth(280);
        colTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));

        TableColumn<BookShelfDetailResponse.BookAllocationResponse, String> colQty = new TableColumn<>("COPIES");
        colQty.setPrefWidth(100);
        colQty.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getQuantity())));

        table.getColumns().addAll(colIsbn, colTitle, colQty);

        if (detail.getBooks() != null) {
            table.setItems(FXCollections.observableArrayList(detail.getBooks()));
        }

        VBox content = new VBox(table);
        content.setPadding(new Insets(15));
        getDialogPane().setContent(content);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
    }
}
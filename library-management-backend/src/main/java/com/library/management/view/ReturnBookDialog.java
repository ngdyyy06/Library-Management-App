package com.library.management.view;

import com.library.management.dto.ReturnBookRequest;
import com.library.management.entity.Borrowing;
import com.library.management.entity.BorrowingDetail;
import com.library.management.service.BorrowingService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class ReturnBookDialog extends Dialog<Void> {

    public ReturnBookDialog(Borrowing borrowing, List<BorrowingDetail> details, BorrowingService borrowingService) {
        setTitle("Process Archival Return — Loan #" + borrowing.getId());
        setHeaderText("Patron: " + borrowing.getReader().getFullName() + " • Due: " + borrowing.getDueDate());

        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox layout = new VBox(14);
        layout.setPadding(new Insets(20, 25, 20, 25));
        layout.setPrefWidth(680);

        Label headerNotice = new Label("Inspect returned volumes condition (Damaged: 50,000 VND • Lost: Book value • Late: 5,000 VND/day)");
        headerNotice.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #7C6856; -fx-font-style: italic;");
        layout.getChildren().add(headerNotice);

        for (BorrowingDetail detail : details) {
            int alreadyReturned = detail.getGoodQuantity() + detail.getDamagedQuantity() + detail.getLostQuantity();
            int remaining = detail.getQuantity() - alreadyReturned;

            VBox itemCard = new VBox(8);
            itemCard.getStyleClass().add("stat-card");
            itemCard.setPadding(new Insets(12, 16, 12, 16));

            HBox titleRow = new HBox(10);
            titleRow.setAlignment(Pos.CENTER_LEFT);
            Label bookTitle = new Label(detail.getBook().getTitle());
            bookTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #27160E;");

            Label statusLabel = new Label("Outstanding: " + remaining + " / " + detail.getQuantity());
            statusLabel.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #C5A059;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            titleRow.getChildren().addAll(bookTitle, spacer, statusLabel);

            if (remaining > 0) {
                HBox controlsRow = new HBox(12);
                controlsRow.setAlignment(Pos.CENTER_LEFT);

                Spinner<Integer> goodSpin = new Spinner<>(0, remaining, remaining);
                goodSpin.setPrefWidth(70);

                Spinner<Integer> damagedSpin = new Spinner<>(0, remaining, 0);
                damagedSpin.setPrefWidth(70);

                Spinner<Integer> lostSpin = new Spinner<>(0, remaining, 0);
                lostSpin.setPrefWidth(70);

                Button submitItemBtn = new Button("RETURN ITEM");
                submitItemBtn.getStyleClass().add("btn-primary");

                submitItemBtn.setOnAction(e -> {
                    try {
                        ReturnBookRequest req = new ReturnBookRequest(goodSpin.getValue(), damagedSpin.getValue(), lostSpin.getValue());
                        borrowingService.returnBook(detail.getId(), req);
                        submitItemBtn.setDisable(true);
                        submitItemBtn.setText("PROCESSED ✓");
                    } catch (Exception ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
                        alert.showAndWait();
                    }
                });

                controlsRow.getChildren().addAll(
                        new Label("Good:"), goodSpin,
                        new Label("Damaged:"), damagedSpin,
                        new Label("Lost:"), lostSpin,
                        submitItemBtn
                );
                itemCard.getChildren().addAll(titleRow, controlsRow);
            } else {
                Label completedLabel = new Label("All copies of this volume have been completely returned.");
                completedLabel.setStyle("-fx-text-fill: #2A541E; -fx-font-size: 11.5px;");
                itemCard.getChildren().addAll(titleRow, completedLabel);
            }

            layout.getChildren().add(itemCard);
        }

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(380);
        getDialogPane().setContent(scrollPane);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
    }
}
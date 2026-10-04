package com.library.management.view;

import com.library.management.dto.RenewBorrowingRequest;
import com.library.management.entity.Borrowing;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class RenewBorrowingDialog extends Dialog<RenewBorrowingRequest> {

    public RenewBorrowingDialog(Borrowing borrowing) {
        setTitle("Extend Literary Loan Deadline");
        setHeaderText("Patron: " + borrowing.getReader().getFullName() + " • Current Due Date: " + borrowing.getDueDate()
                + "\nRate: 1,000 VND / extended day (3 to 30 days allowable).");

        ButtonType renewBtnType = new ButtonType("CONFIRM EXTENSION", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(renewBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 25, 20, 25));

        Spinner<Integer> daysSpinner = new Spinner<>(3, 30, 7);
        daysSpinner.setPrefWidth(260);

        Label feeLabel = new Label("Renewal Fee Due: 7,000 VND");
        feeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #C5A059; -fx-font-size: 12.5px;");

        daysSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            long fee = newVal * 1000L;
            feeLabel.setText("Renewal Fee Due: " + fee + " VND");
        });

        CheckBox paymentConfirmCheck = new CheckBox("Patron has remitted the renewal fee in full.");
        paymentConfirmCheck.setStyle("-fx-font-size: 12px; -fx-text-fill: #5A4030;");

        grid.add(new Label("EXTENSION DAYS *"), 0, 0);
        grid.add(daysSpinner, 1, 0);

        grid.add(new Label("TOTAL CHARGE"), 0, 1);
        grid.add(feeLabel, 1, 1);

        grid.add(new Label("REMITTANCE *"), 0, 2);
        grid.add(paymentConfirmCheck, 1, 2);

        getDialogPane().setContent(grid);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());

        setResultConverter(dialogButton -> {
            if (dialogButton == renewBtnType) {
                if (!paymentConfirmCheck.isSelected()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Payment must be confirmed to execute renewal.", ButtonType.OK);
                    alert.showAndWait();
                    return null;
                }

                RenewBorrowingRequest req = new RenewBorrowingRequest();
                req.setDays(daysSpinner.getValue());
                req.setPaymentConfirmed(true);
                return req;
            }
            return null;
        });
    }
}
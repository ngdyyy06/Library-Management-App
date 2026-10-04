package com.library.management.view;

import com.library.management.entity.Publisher;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class PublisherFormDialog extends Dialog<Publisher> {

    public PublisherFormDialog(Publisher existingPublisher) {
        boolean isEdit = existingPublisher != null;
        setTitle(isEdit ? "Edit Publisher Record" : "Enroll Publishing House");
        setHeaderText(isEdit ? "Update printing house credentials and dispatch contacts."
                : "Register a new imprint, press, or publishing company.");

        ButtonType saveBtnType = new ButtonType(isEdit ? "UPDATE RECORD" : "ENROLL PRESS", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 25, 20, 25));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Oxford University Press, Gallimard");
        nameField.setPrefWidth(320);

        TextField emailField = new TextField();
        emailField.setPromptText("e.g. contact@oxfordpress.org");
        emailField.setPrefWidth(320);

        TextField phoneField = new TextField();
        phoneField.setPromptText("e.g. +44 1865 556767");
        phoneField.setPrefWidth(320);

        TextField addressField = new TextField();
        addressField.setPromptText("e.g. Great Clarendon St, Oxford, United Kingdom");
        addressField.setPrefWidth(320);

        if (isEdit) {
            nameField.setText(existingPublisher.getName());
            emailField.setText(existingPublisher.getEmail());
            phoneField.setText(existingPublisher.getPhone());
            addressField.setText(existingPublisher.getAddress());
        }

        grid.add(new Label("PUBLISHER NAME *"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("DISPATCH EMAIL"), 0, 1);
        grid.add(emailField, 1, 1);

        grid.add(new Label("CONTACT PHONE"), 0, 2);
        grid.add(phoneField, 1, 2);

        grid.add(new Label("EDITORIAL ADDRESS"), 0, 3);
        grid.add(addressField, 1, 3);

        getDialogPane().setContent(grid);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());

        setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                String name = nameField.getText().trim();
                if (name.isBlank()) {
                    return null;
                }

                Publisher pub = isEdit ? existingPublisher : new Publisher();
                pub.setName(name);
                pub.setEmail(emailField.getText().trim().isBlank() ? null : emailField.getText().trim());
                pub.setPhone(phoneField.getText().trim().isBlank() ? null : phoneField.getText().trim());
                pub.setAddress(addressField.getText().trim().isBlank() ? null : addressField.getText().trim());
                return pub;
            }
            return null;
        });
    }
}
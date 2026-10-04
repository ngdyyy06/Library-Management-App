package com.library.management.view;

import com.library.management.dto.CreateReaderRequest;
import com.library.management.entity.Reader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class ReaderFormDialog extends Dialog<CreateReaderRequest> {

    public ReaderFormDialog(Reader existingReader) {
        boolean isEdit = existingReader != null;
        setTitle(isEdit ? "Update Patron Profile" : "Issue Archival Patron Card");
        setHeaderText(isEdit ? "Modify patron contact details and demographic records."
                : "Register a new fellowship patron (Card Fee: 50,000 VND • Valid for 1 year).");

        ButtonType saveBtnType = new ButtonType(isEdit ? "SAVE PROFILE" : "ENROLL & ISSUE CARD", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 25, 20, 25));

        TextField codeField = new TextField();
        codeField.setPromptText("e.g. RD-00109, PATRON-2026");
        codeField.setPrefWidth(300);

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Alexandre Dumas");
        nameField.setPrefWidth(300);

        TextField phoneField = new TextField();
        phoneField.setPromptText("10 digits (e.g. 0912345678)");
        phoneField.setPrefWidth(300);

        TextField emailField = new TextField();
        emailField.setPromptText("e.g. dumas@literature.org");
        emailField.setPrefWidth(300);

        TextField addressField = new TextField();
        addressField.setPromptText("e.g. 14 Rue de Rivoli, Paris");
        addressField.setPrefWidth(300);

        DatePicker dobPicker = new DatePicker();
        dobPicker.setPromptText("Select birthdate...");
        dobPicker.setPrefWidth(300);

        if (isEdit) {
            codeField.setText(existingReader.getReaderCode());
            nameField.setText(existingReader.getFullName());
            phoneField.setText(existingReader.getPhone());
            emailField.setText(existingReader.getEmail());
            addressField.setText(existingReader.getAddress());
            dobPicker.setValue(existingReader.getDateOfBirth());
        }

        grid.add(new Label("PATRON CODE *"), 0, 0);
        grid.add(codeField, 1, 0);

        grid.add(new Label("FULL LEGAL NAME *"), 0, 1);
        grid.add(nameField, 1, 1);

        grid.add(new Label("PHONE NUMBER *"), 0, 2);
        grid.add(phoneField, 1, 2);

        grid.add(new Label("DISPATCH EMAIL"), 0, 3);
        grid.add(emailField, 1, 3);

        grid.add(new Label("RESIDENTIAL ADDRESS"), 0, 4);
        grid.add(addressField, 1, 4);

        grid.add(new Label("DATE OF BIRTH"), 0, 5);
        grid.add(dobPicker, 1, 5);

        getDialogPane().setContent(grid);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());

        setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                String code = codeField.getText().trim();
                String name = nameField.getText().trim();
                String phone = phoneField.getText().trim();

                if (code.isBlank() || name.isBlank() || phone.isBlank()) {
                    return null;
                }

                CreateReaderRequest req = new CreateReaderRequest();
                req.setReaderCode(code);
                req.setFullName(name);
                req.setPhone(phone);
                req.setEmail(emailField.getText().trim().isBlank() ? null : emailField.getText().trim());
                req.setAddress(addressField.getText().trim().isBlank() ? null : addressField.getText().trim());
                req.setDateOfBirth(dobPicker.getValue());
                return req;
            }
            return null;
        });
    }
}
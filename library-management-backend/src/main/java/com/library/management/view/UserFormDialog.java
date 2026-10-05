package com.library.management.view;

import com.library.management.dto.CreateUserRequest;
import com.library.management.entity.Role;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.util.List;

public class UserFormDialog extends Dialog<CreateUserRequest> {

    public UserFormDialog(List<Role> availableRoles) {
        setTitle("Enroll Staff Account");
        setHeaderText("Register a new library curator or administrative officer.");

        ButtonType saveBtnType = new ButtonType("ENROLL CURATOR", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 25, 20, 25));

        TextField usernameField = new TextField();
        usernameField.setPromptText("e.g. curator_victor");
        usernameField.setPrefWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Minimum 6 characters");
        passwordField.setPrefWidth(300);

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("e.g. Victor Hugo");
        fullNameField.setPrefWidth(300);

        TextField emailField = new TextField();
        emailField.setPromptText("e.g. victor@atelier-library.org");
        emailField.setPrefWidth(300);

        ComboBox<Role> roleCombo = new ComboBox<>();

        availableRoles.stream()
                .filter(role -> !"READER".equalsIgnoreCase(role.getName()))
                .forEach(roleCombo.getItems()::add);

        roleCombo.setPrefWidth(300);

        roleCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Role r) {
                return r != null ? r.getName() : "Select Role & Authority...";
            }

            @Override
            public Role fromString(String string) {
                return null;
            }
        });

        if (!availableRoles.isEmpty()) {
            roleCombo.setValue(availableRoles.get(0));
        }

        grid.add(new Label("USERNAME *"), 0, 0);
        grid.add(usernameField, 1, 0);

        grid.add(new Label("PASSWORD *"), 0, 1);
        grid.add(passwordField, 1, 1);

        grid.add(new Label("FULL NAME *"), 0, 2);
        grid.add(fullNameField, 1, 2);

        grid.add(new Label("DISPATCH EMAIL"), 0, 3);
        grid.add(emailField, 1, 3);

        grid.add(new Label("ASSIGNED ROLE *"), 0, 4);
        grid.add(roleCombo, 1, 4);

        getDialogPane().setContent(grid);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());

        setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                String username = usernameField.getText().trim();
                String password = passwordField.getText();
                String fullName = fullNameField.getText().trim();
                Role role = roleCombo.getValue();

                if (username.isBlank() || password.isBlank() || fullName.isBlank() || role == null) {
                    return null;
                }

                CreateUserRequest req = new CreateUserRequest();
                req.setUsername(username);
                req.setPassword(password);
                req.setFullName(fullName);
                req.setEmail(emailField.getText().trim().isBlank() ? null : emailField.getText().trim());
                req.setRoleId(role.getId());
                return req;
            }
            return null;
        });
    }
}
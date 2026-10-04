package com.library.management.view;

import com.library.management.dto.UpdateUserRequest;
import com.library.management.dto.UserResponse;
import com.library.management.entity.Role;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.util.List;

public class UserEditDialog extends Dialog<UpdateUserRequest> {

    public UserEditDialog(UserResponse existingUser, List<Role> availableRoles) {
        setTitle("Modify Staff Profile: " + existingUser.getUsername());
        setHeaderText("Update personnel credentials and administrative authorities.");

        ButtonType saveBtnType = new ButtonType("APPLY CHANGES", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 25, 20, 25));

        TextField usernameField = new TextField(existingUser.getUsername());
        usernameField.setPrefWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Leave blank to retain current password");
        passwordField.setPrefWidth(300);

        TextField fullNameField = new TextField(existingUser.getFullName());
        fullNameField.setPrefWidth(300);

        TextField emailField = new TextField(existingUser.getEmail() != null ? existingUser.getEmail() : "");
        emailField.setPrefWidth(300);

        ComboBox<Role> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(availableRoles);
        roleCombo.setPrefWidth(300);
        roleCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Role r) {
                return r != null ? r.getName() : "Select Role...";
            }

            @Override
            public Role fromString(String string) {
                return null;
            }
        });

        // Pre-select role
        for (Role r : availableRoles) {
            if (r.getName().equalsIgnoreCase(existingUser.getRole())) {
                roleCombo.setValue(r);
                break;
            }
        }

        grid.add(new Label("USERNAME *"), 0, 0);
        grid.add(usernameField, 1, 0);

        grid.add(new Label("NEW PASSWORD"), 0, 1);
        grid.add(passwordField, 1, 1);

        grid.add(new Label("FULL NAME *"), 0, 2);
        grid.add(fullNameField, 1, 2);

        grid.add(new Label("DISPATCH EMAIL"), 0, 3);
        grid.add(emailField, 1, 3);

        grid.add(new Label("ROLE & AUTHORITY *"), 0, 4);
        grid.add(roleCombo, 1, 4);

        getDialogPane().setContent(grid);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());

        setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                String username = usernameField.getText().trim();
                String fullName = fullNameField.getText().trim();
                Role role = roleCombo.getValue();

                if (username.isBlank() || fullName.isBlank() || role == null) {
                    return null;
                }

                UpdateUserRequest req = new UpdateUserRequest();
                req.setUsername(username);
                req.setFullName(fullName);
                req.setEmail(emailField.getText().trim().isBlank() ? null : emailField.getText().trim());
                req.setRoleId(role.getId());

                String newPass = passwordField.getText();
                if (newPass != null && !newPass.isBlank()) {
                    req.setPassword(newPass);
                }

                return req;
            }
            return null;
        });
    }
}
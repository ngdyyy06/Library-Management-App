package com.library.management.view;

import com.library.management.dto.UpdateStaffProfileRequest;
import com.library.management.entity.Staff;
import com.library.management.entity.User;
import com.library.management.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

public class StaffProfileView {

    private final User currentUser;
    private final UserService userService;

    public StaffProfileView(User currentUser, UserService userService) {
        this.currentUser = currentUser;
        this.userService = userService;
    }

    public ScrollPane getView() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(30, 45, 35, 45));
        root.getStyleClass().add("catalog-content-pane");

        // =====================================================
        // 1. HEADER SECTION
        // =====================================================
        VBox titleBox = new VBox(4);
        Label title = new Label("Curator Dossier & Credentials");
        title.getStyleClass().add("view-main-title");

        Label subtitle = new Label("Manage personal staff identification, official dispatch coordinates, and security access key.");
        subtitle.getStyleClass().add("view-main-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // =====================================================
        // 2. MAIN DOSSIER CARDS (2 Columns)
        // =====================================================
        HBox columnsContainer = new HBox(28);
        columnsContainer.setAlignment(Pos.TOP_LEFT);

        // ─────────────────────────────────────────────────────
        // ── LEFT COLUMN: PERSONAL INFORMATION ──
        // ─────────────────────────────────────────────────────
        VBox personalCard = new VBox(14);
        personalCard.getStyleClass().add("stat-card");
        personalCard.setPadding(new Insets(24));
        personalCard.setPrefWidth(440);

        Label personalHeader = new Label("CURATORIAL IDENTIFICATION");
        personalHeader.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #27160E;");

        // 1. Username
        TextField usernameField = new TextField(currentUser.getUsername());
        usernameField.setDisable(true);
        usernameField.getStyleClass().add("custom-text-field");
        VBox usernameBox = createFormField("ACCOUNT IDENTIFIER (USERNAME)", usernameField);

        // 2. Full Name
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Enter legal full name...");
        fullNameField.getStyleClass().add("custom-text-field");
        VBox fullNameBox = createFormField("LEGAL FULL NAME *", fullNameField);

        // 3. Email
        TextField emailField = new TextField();
        emailField.setPromptText("e.g. curator@atelier-library.org");
        emailField.getStyleClass().add("custom-text-field");
        VBox emailBox = createFormField("OFFICIAL DISPATCH EMAIL", emailField);

        // 4. Phone
        TextField phoneField = new TextField();
        phoneField.setPromptText("e.g. 0912345678");
        phoneField.getStyleClass().add("custom-text-field");
        VBox phoneBox = createFormField("DIRECT TELEPHONE NUMBER", phoneField);

        // 5. Address
        TextField addressField = new TextField();
        addressField.setPromptText("e.g. 221B Baker Street, London");
        addressField.getStyleClass().add("custom-text-field");
        VBox addressBox = createFormField("RESIDENTIAL / OFFICIAL ADDRESS", addressField);

        // 6. Date of Birth
        DatePicker dobPicker = new DatePicker();
        dobPicker.setPromptText("Select birthdate...");
        dobPicker.setPrefWidth(Double.MAX_VALUE);
        VBox dobBox = createFormField("DATE OF BIRTH", dobPicker);

        personalCard.getChildren().addAll(
                personalHeader,
                new Separator(),
                usernameBox,
                fullNameBox,
                emailBox,
                phoneBox,
                addressBox,
                dobBox
        );

        // ─────────────────────────────────────────────────────
        // ── RIGHT COLUMN: SECURITY & PASSWORD VAULT ──
        // ─────────────────────────────────────────────────────
        VBox securityCard = new VBox(14);
        securityCard.getStyleClass().add("stat-card");
        securityCard.setPadding(new Insets(24));
        securityCard.setPrefWidth(440);

        Label securityHeader = new Label("SECURITY & ACCESS KEY VAULT");
        securityHeader.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #27160E;");

        Label securityDesc = new Label("Leave credential fields blank if you do not intend to modify your passkey.");
        securityDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #7C6856; -fx-font-style: italic;");

        // 1. Current Password
        PasswordField currentPassField = new PasswordField();
        currentPassField.setPromptText("Enter current passkey");
        currentPassField.getStyleClass().add("custom-text-field");
        VBox currentPassBox = createFormField("CURRENT ARCHIVAL PASSKEY", currentPassField);

        // 2. New Password
        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("Minimum 6 characters");
        newPassField.getStyleClass().add("custom-text-field");
        VBox newPassBox = createFormField("NEW ARCHIVAL PASSKEY", newPassField);

        // 3. Confirm Password
        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Re-type new passkey");
        confirmPassField.getStyleClass().add("custom-text-field");
        VBox confirmPassBox = createFormField("CONFIRM NEW PASSKEY", confirmPassField);

        securityCard.getChildren().addAll(
                securityHeader,
                securityDesc,
                new Separator(),
                currentPassBox,
                newPassBox,
                confirmPassBox
        );

        columnsContainer.getChildren().addAll(personalCard, securityCard);

        // =====================================================
        // 3. LOAD EXISTING DATA
        // =====================================================
        fullNameField.setText(currentUser.getFullName());
        emailField.setText(currentUser.getEmail());

        try {
            Staff staff = userService.getMyStaffProfile(currentUser.getUsername());
            if (staff != null) {
                if (staff.getPhone() != null && !"N/A".equals(staff.getPhone())) {
                    phoneField.setText(staff.getPhone());
                }
                addressField.setText(staff.getAddress());
                dobPicker.setValue(staff.getDateOfBirth());
            }
        } catch (Exception ignored) {
            // Trường hợp tài khoản Admin không có hồ sơ Staff riêng
        }

        // =====================================================
        // 4. ACTION BAR (SAVE BUTTON)
        // =====================================================
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Button saveBtn = new Button("COMMIT PROFILE REVISIONS");
        saveBtn.getStyleClass().add("btn-primary");
        FontIcon saveIcon = new FontIcon(FontAwesomeSolid.SAVE);
        saveIcon.setIconSize(12);
        saveIcon.setIconColor(javafx.scene.paint.Color.web("#F8F3EB"));
        saveBtn.setGraphic(saveIcon);

        Label statusMsg = new Label();
        statusMsg.setStyle("-fx-font-size: 12px;");

        saveBtn.setOnAction(e -> {
            String fullName = fullNameField.getText().trim();
            if (fullName.isBlank()) {
                showAlert(Alert.AlertType.WARNING, "Full name cannot be empty.");
                return;
            }

            UpdateStaffProfileRequest req = new UpdateStaffProfileRequest();
            req.setFullName(fullName);
            req.setEmail(emailField.getText().trim().isBlank() ? null : emailField.getText().trim());
            req.setPhone(phoneField.getText().trim().isBlank() ? null : phoneField.getText().trim());
            req.setAddress(addressField.getText().trim().isBlank() ? null : addressField.getText().trim());
            req.setDateOfBirth(dobPicker.getValue());

            req.setCurrentPassword(currentPassField.getText());
            req.setNewPassword(newPassField.getText());
            req.setConfirmPassword(confirmPassField.getText());

            try {
                userService.updateMyStaffProfile(currentUser.getUsername(), req);

                // Cập nhật lại session user hiện tại
                currentUser.setFullName(fullName);
                currentUser.setEmail(req.getEmail());

                currentPassField.clear();
                newPassField.clear();
                confirmPassField.clear();

                statusMsg.setStyle("-fx-text-fill: #2A541E; -fx-font-weight: bold;");
                statusMsg.setText("Curator profile successfully committed.");
            } catch (Exception ex) {
                statusMsg.setStyle("-fx-text-fill: #8D2B1B; -fx-font-weight: bold;");
                statusMsg.setText("Revision failed: " + ex.getMessage());
            }
        });

        actionBar.getChildren().addAll(saveBtn, statusMsg);

        VBox contentBox = new VBox(22, titleBox, columnsContainer, actionBar);
        contentBox.setPadding(new Insets(10));

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scroll;
    }

    // =========================================================
    // HELPER: TẠO KHỐI FORM GỒM TITLE + FIELD
    // =========================================================
    private VBox createFormField(String labelText, Control inputControl) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: #5A4030; -fx-letter-spacing: 0.8px;");

        VBox box = new VBox(5, label, inputControl);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void showAlert(Alert.AlertType type, String content) {
        Alert alert = new Alert(type, content, ButtonType.OK);
        alert.showAndWait();
    }
}
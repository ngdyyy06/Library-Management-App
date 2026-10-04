package com.library.management.view;

import com.library.management.entity.User;
import com.library.management.service.AuthService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginView {

    private final AuthService authService;

    public LoginView(AuthService authService) {
        this.authService = authService;
    }

    public void show(Stage stage) {

        // =====================================================
        // LEFT - BRANDING PANEL
        // =====================================================

        Label monogramText = new Label("A");
        monogramText.getStyleClass().add("logo-monogram");

        VBox logoSeal = new VBox(monogramText);
        logoSeal.getStyleClass().add("logo-seal");
        logoSeal.setAlignment(Pos.CENTER);

        Label brandTitle = new Label("ATELIER");
        brandTitle.getStyleClass().add("brand-title");

        Label brandSubtitle =
                new Label("ARCHIVAL & LIBRARY REPOSITORY");
        brandSubtitle.getStyleClass().add("brand-subtitle");

        Separator brandDivider = new Separator();
        brandDivider.getStyleClass().add("brand-divider");

        Label description = new Label(
                "Preserving literary heritage and managing modern collections with precision and discretion."
        );
        description.getStyleClass().add("brand-description");
        description.setWrapText(true);

        Label establishedLabel =
                new Label("EST. 2026 • REPOSITORIUM");
        establishedLabel.getStyleClass().add("brand-est");

        VBox brandContent = new VBox(
                16,
                logoSeal,
                brandTitle,
                brandSubtitle,
                brandDivider,
                description,
                establishedLabel
        );

        brandContent.setAlignment(Pos.CENTER_LEFT);
        brandContent.setMaxWidth(320);

        VBox brandPanel = new VBox(brandContent);
        brandPanel.getStyleClass().add("brand-panel");
        brandPanel.setAlignment(Pos.CENTER);
        brandPanel.setPrefWidth(440);
        brandPanel.setPadding(new Insets(50));

        // =====================================================
        // RIGHT - LOGIN FORM
        // =====================================================

        Label loginTitle =
                new Label("Curator Access");
        loginTitle.getStyleClass().add("login-title");

        Label loginSubtitle =
                new Label(
                        "Enter your credentials to access repository records."
                );
        loginSubtitle.getStyleClass().add("login-subtitle");

        VBox loginHeader = new VBox(
                6,
                loginTitle,
                loginSubtitle
        );

        loginHeader.setPadding(
                new Insets(0, 0, 25, 0)
        );

        // =====================================================
        // USERNAME
        // =====================================================

        Label usernameLabel =
                new Label("USERNAME OR IDENTIFIER");
        usernameLabel.getStyleClass().add("field-label");

        TextField usernameField =
                new TextField();

        usernameField.setPromptText(
                "e.g. curator_duy"
        );

        usernameField.getStyleClass().add(
                "custom-text-field"
        );

        VBox usernameBox = new VBox(
                6,
                usernameLabel,
                usernameField
        );

        // =====================================================
        // PASSWORD
        // =====================================================

        Label passwordLabel =
                new Label("PASSWORD");

        passwordLabel.getStyleClass().add(
                "field-label"
        );

        PasswordField passwordField =
                new PasswordField();

        passwordField.setPromptText(
                "Enter your password"
        );

        passwordField.getStyleClass().add(
                "custom-text-field"
        );

        VBox passwordBox = new VBox(
                6,
                passwordLabel,
                passwordField
        );

        // =====================================================
        // LOGIN BUTTON
        // =====================================================

        Button loginButton =
                new Button("SIGN IN");

        loginButton.getStyleClass().add(
                "login-button"
        );

        loginButton.setMaxWidth(
                Double.MAX_VALUE
        );

        // =====================================================
        // MESSAGE
        // =====================================================

        Label messageLabel =
                new Label();

        messageLabel.getStyleClass().add(
                "message-label"
        );

        messageLabel.setWrapText(true);

        // =====================================================
        // FOOTER
        // =====================================================

        Label footerText =
                new Label(
                        "Atelier Library System • v2.4.0"
                );

        footerText.getStyleClass().add(
                "footer-text"
        );

        // =====================================================
        // LOGIN ACTION
        // =====================================================

        Runnable loginAction = () -> {

            String username =
                    usernameField.getText().trim();

            String password =
                    passwordField.getText();

            messageLabel.setText("");

            if (
                    username.isBlank()
                            || password.isBlank()
            ) {

                messageLabel.setText(
                        "Please enter both username and password."
                );

                return;
            }

            try {

                User user =
                        authService.login(
                                username,
                                password
                        );

                DashboardView dashboardView =
                        new DashboardView(user);

                dashboardView.show(stage);

            } catch (Exception e) {

                messageLabel.setText(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Invalid credentials."
                );
            }
        };

        loginButton.setOnAction(
                event -> loginAction.run()
        );

        passwordField.setOnAction(
                event -> loginAction.run()
        );

        usernameField.setOnAction(
                event -> loginAction.run()
        );

        // =====================================================
        // FORM
        // =====================================================

        VBox form = new VBox(
                18,
                loginHeader,
                usernameBox,
                passwordBox,
                loginButton,
                messageLabel
        );

        Region spacer = new Region();

        VBox.setVgrow(
                spacer,
                Priority.ALWAYS
        );

        VBox loginPanel = new VBox(
                form,
                spacer,
                footerText
        );

        loginPanel.getStyleClass().add(
                "login-panel"
        );

        loginPanel.setMaxWidth(380);

        VBox rightContainer =
                new VBox(loginPanel);

        rightContainer.setAlignment(
                Pos.CENTER
        );

        rightContainer.setPadding(
                new Insets(
                        50,
                        60,
                        50,
                        60
                )
        );

        rightContainer.getStyleClass().add(
                "login-right-container"
        );

        // =====================================================
        // ROOT
        // =====================================================

        HBox root = new HBox(
                brandPanel,
                rightContainer
        );

        HBox.setHgrow(
                rightContainer,
                Priority.ALWAYS
        );

        // =====================================================
        // SCENE
        // =====================================================

        Scene scene = new Scene(root);

        scene.getStylesheets().add(
                getClass()
                        .getResource(
                                "/css/login.css"
                        )
                        .toExternalForm()
        );

        // =====================================================
        // STAGE
        // =====================================================

        stage.setTitle(
                "Atelier • Curator Login"
        );

        stage.setMinWidth(900);
        stage.setMinHeight(600);

        stage.setScene(scene);

        stage.show();

        forceMaximize(stage);
    }

    private void forceMaximize(Stage stage) {

        Platform.runLater(() -> {
            stage.setMaximized(true);
            stage.toFront();
        });
    }
}
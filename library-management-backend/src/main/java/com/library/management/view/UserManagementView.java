package com.library.management.view;

import com.library.management.dto.CreateUserRequest;
import com.library.management.dto.UpdateUserRequest;
import com.library.management.dto.UserResponse;
import com.library.management.repository.RoleRepository;
import com.library.management.service.UserService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Optional;

public class UserManagementView {

    private final UserService userService;
    private final RoleRepository roleRepository;

    private TableView<UserResponse> tableView;
    private ObservableList<UserResponse> masterData;
    private FilteredList<UserResponse> filteredData;

    public UserManagementView(
            UserService userService,
            RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    public Pane getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.getStyleClass().add("catalog-content-pane");

        // =====================================================
        // 1. HEADER SECTION
        // =====================================================
        VBox titleBox = new VBox(4);
        Label title = new Label("Staff Accounts & Permissions");
        title.getStyleClass().add("view-main-title");

        Label subtitle = new Label("Manage curator profiles, system administrators, and library personnel credentials.");
        subtitle.getStyleClass().add("view-main-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // =====================================================
        // 2. TOOLBAR
        // =====================================================
        HBox toolbar = new HBox(14);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search username, full name, or email...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(320);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("ALL STATUSES", "ACTIVE", "INACTIVE");
        statusFilter.setValue("ALL STATUSES");
        statusFilter.getStyleClass().add("filter-combo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = createActionButton("Refresh", FontAwesomeSolid.SYNC_ALT);
        refreshBtn.setOnAction(e -> reloadData());

        Button addBtn = new Button("NEW STAFF ACCOUNT");
        addBtn.getStyleClass().add("btn-primary");
        FontIcon plusIcon = new FontIcon(FontAwesomeSolid.USER_PLUS);
        plusIcon.setIconSize(12);
        plusIcon.setIconColor(javafx.scene.paint.Color.web("#F8F3EB"));
        addBtn.setGraphic(plusIcon);
        addBtn.setOnAction(e -> handleAddUser());

        toolbar.getChildren().addAll(searchField, statusFilter, spacer, refreshBtn, addBtn);

        // =====================================================
        // 3. TABLE SETUP
        // =====================================================
        tableView = new TableView<>();
        tableView.getStyleClass().add("vintage-table");
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<UserResponse, String> colId = new TableColumn<>("# ID");
        colId.setPrefWidth(70);
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));

        TableColumn<UserResponse, String> colUsername = new TableColumn<>("USERNAME");
        colUsername.setPrefWidth(160);
        colUsername.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));

        TableColumn<UserResponse, String> colFullName = new TableColumn<>("FULL NAME");
        colFullName.setPrefWidth(220);
        colFullName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));

        TableColumn<UserResponse, String> colEmail = new TableColumn<>("DISPATCH EMAIL");
        colEmail.setPrefWidth(240);
        colEmail.setCellValueFactory(data -> {
            String email = data.getValue().getEmail();
            return new SimpleStringProperty(email != null && !email.isBlank() ? email : "—");
        });

        TableColumn<UserResponse, String> colRole = new TableColumn<>("ROLE & AUTHORITY");
        colRole.setPrefWidth(160);
        colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));

        TableColumn<UserResponse, Void> colStatus = new TableColumn<>("STATUS");
        colStatus.setPrefWidth(110);
        colStatus.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    UserResponse u = getTableRow().getItem();
                    Label statusLabel = new Label(u.getStatus());
                    if ("ACTIVE".equalsIgnoreCase(u.getStatus())) {
                        statusLabel.getStyleClass().setAll("badge-status-active");
                    } else {
                        statusLabel.getStyleClass().setAll("badge-status-inactive");
                    }
                    setGraphic(statusLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<UserResponse, Void> colActions = new TableColumn<>("ACTIONS");
        colActions.setPrefWidth(120);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button detailBtn = createTableIconBtn(
                    FontAwesomeSolid.INFO_CIRCLE,
                    "View Staff Account Details"
            );
            private final Button editBtn = createTableIconBtn(FontAwesomeSolid.EDIT, "Modify Profile");
            private final Button toggleBtn = createTableIconBtn(FontAwesomeSolid.POWER_OFF, "Toggle Status");

            {
                detailBtn.setOnAction(e -> {
                    UserResponse u = getTableRow().getItem();
                    if (u != null) {
                        showUserDetail(u);
                    }
                });

                editBtn.setOnAction(e -> {
                    UserResponse u = getTableRow().getItem();
                    if (u != null) handleEditUser(u);
                });

                toggleBtn.setOnAction(e -> {
                    UserResponse u = getTableRow().getItem();
                    if (u != null) handleToggleStatus(u);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8, detailBtn, editBtn, toggleBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        tableView.getColumns().addAll(colId, colUsername, colFullName, colEmail, colRole, colStatus, colActions);

        // =====================================================
        // 4. FILTER & DATA BINDING
        // =====================================================
        masterData = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(masterData, u -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(searchField.getText(), statusFilter.getValue()));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(searchField.getText(), statusFilter.getValue()));

        tableView.setItems(filteredData);
        reloadData();

        root.getChildren().addAll(titleBox, toolbar, tableView);
        return root;
    }

    private void applyFilter(String query, String status) {
        filteredData.setPredicate(user -> {
            boolean matchesSearch = true;
            if (query != null && !query.isBlank()) {
                String q = query.toLowerCase().trim();
                boolean userMatch = user.getUsername() != null && user.getUsername().toLowerCase().contains(q);
                boolean nameMatch = user.getFullName() != null && user.getFullName().toLowerCase().contains(q);
                boolean emailMatch = user.getEmail() != null && user.getEmail().toLowerCase().contains(q);
                boolean roleMatch = user.getRole() != null && user.getRole().toLowerCase().contains(q);
                matchesSearch = userMatch || nameMatch || emailMatch || roleMatch;
            }

            boolean matchesStatus = true;
            if (status != null && !"ALL STATUSES".equals(status)) {
                matchesStatus = status.equalsIgnoreCase(user.getStatus());
            }

            return matchesSearch && matchesStatus;
        });
    }

    public void reloadData() {
        try {
            masterData.setAll(userService.getAllUsers());
        } catch (Exception e) {
            showError("Failed to load staff accounts: " + e.getMessage());
        }
    }

    private void handleAddUser() {
        UserFormDialog dialog = new UserFormDialog(roleRepository.findAll());
        Optional<CreateUserRequest> result = dialog.showAndWait();
        result.ifPresent(req -> {
            try {
                userService.createUser(req);
                reloadData();
            } catch (Exception e) {
                showError("Failed to register staff account: " + e.getMessage());
            }
        });
    }

    private void handleEditUser(UserResponse selected) {
        UserEditDialog dialog = new UserEditDialog(selected, roleRepository.findAll());
        Optional<UpdateUserRequest> result = dialog.showAndWait();
        result.ifPresent(req -> {
            try {
                userService.updateUser(selected.getId(), req);
                reloadData();
            } catch (Exception e) {
                showError("Failed to update staff account: " + e.getMessage());
            }
        });
    }

    private void handleToggleStatus(UserResponse user) {
        try {
            if ("ACTIVE".equalsIgnoreCase(user.getStatus())) {
                userService.deactivateUser(user.getId());
            } else {
                userService.activateUser(user.getId());
            }
            reloadData();
        } catch (Exception e) {
            showError("Failed to update status: " + e.getMessage());
        }
    }

    private void showUserDetail(UserResponse user) {

        // =====================================================
        // DETAIL WINDOW
        // =====================================================
        Stage stage = new Stage();

        stage.setTitle("Staff Account Details");

        Window owner = tableView.getScene().getWindow();

        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);

        // =====================================================
        // HEADER
        // =====================================================
        VBox header = new VBox(4);
        header.getStyleClass().add("user-detail-header");

        Label title = new Label("STAFF ACCOUNT");
        title.getStyleClass().add("user-detail-title");

        Label subtitle = new Label("ACCOUNT INFORMATION");
        subtitle.getStyleClass().add("user-detail-subtitle");

        header.getChildren().addAll(title, subtitle);

        // =====================================================
        // INFORMATION CARD
        // =====================================================
        VBox card = new VBox(10);
        card.getStyleClass().add("user-detail-card");

        Label sectionTitle = new Label("PERSONNEL DETAILS");
        sectionTitle.getStyleClass().add("user-detail-section-title");

        card.getChildren().add(sectionTitle);

        // -----------------------------------------------------
        // USER ID
        // -----------------------------------------------------
        card.getChildren().add(
                createUserDetailRow(
                        "USER ID",
                        String.valueOf(user.getId()),
                        true
                )
        );

        // -----------------------------------------------------
        // USERNAME
        // -----------------------------------------------------
        card.getChildren().add(
                createUserDetailRow(
                        "USERNAME",
                        user.getUsername(),
                        true
                )
        );

        // -----------------------------------------------------
        // FULL NAME
        // -----------------------------------------------------
        card.getChildren().add(
                createUserDetailRow(
                        "FULL NAME",
                        user.getFullName(),
                        false
                )
        );

        // -----------------------------------------------------
        // EMAIL
        // -----------------------------------------------------
        card.getChildren().add(
                createUserDetailRow(
                        "EMAIL",
                        user.getEmail() != null && !user.getEmail().isBlank()
                                ? user.getEmail()
                                : "—",
                        false
                )
        );

        // -----------------------------------------------------
        // ROLE
        // -----------------------------------------------------
        HBox roleRow = new HBox(15);
        roleRow.setAlignment(Pos.CENTER_LEFT);
        roleRow.getStyleClass().add("user-detail-row");

        Label roleLabel = new Label("ROLE");
        roleLabel.getStyleClass().add("user-detail-label");
        roleLabel.setPrefWidth(100);

        Label roleValue = new Label(
                user.getRole() != null
                        ? user.getRole()
                        : "—"
        );

        roleValue.getStyleClass().add("user-detail-role");

        roleRow.getChildren().addAll(
                roleLabel,
                roleValue
        );

        card.getChildren().add(roleRow);

        // -----------------------------------------------------
        // STATUS
        // -----------------------------------------------------
        HBox statusRow = new HBox(15);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.getStyleClass().add("user-detail-row");

        Label statusLabel = new Label("STATUS");
        statusLabel.getStyleClass().add("user-detail-label");
        statusLabel.setPrefWidth(100);

        Label statusValue = new Label(
                user.getStatus() != null
                        ? user.getStatus()
                        : "—"
        );

        if ("ACTIVE".equalsIgnoreCase(user.getStatus())) {
            statusValue.getStyleClass().add("user-detail-active");
        } else {
            statusValue.getStyleClass().add("user-detail-inactive");
        }

        statusRow.getChildren().addAll(
                statusLabel,
                statusValue
        );

        card.getChildren().add(statusRow);

        // =====================================================
        // CONTENT
        // =====================================================
        VBox content = new VBox(card);
        content.getStyleClass().add("user-detail-content");

        // =====================================================
        // CLOSE BUTTON
        // =====================================================
        Button closeBtn = new Button("CLOSE");
        closeBtn.getStyleClass().add("user-detail-close-button");

        closeBtn.setOnAction(e -> stage.close());

        HBox footer = new HBox(closeBtn);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.getStyleClass().add("user-detail-footer");

        // =====================================================
        // MAIN LAYOUT
        // =====================================================
        VBox main = new VBox(
                header,
                content,
                footer
        );

        main.getStyleClass().add("user-detail-main");

        // =====================================================
        // SCENE
        // =====================================================
        Scene scene = new Scene(main, 560, 470);

        // =====================================================
        // LOAD CSS
        // =====================================================
        String css = getClass()
                .getResource("/css/dashboard.css") != null
                ? getClass()
                .getResource("/css/dashboard.css")
                .toExternalForm()
                : null;

        if (css != null) {
            scene.getStylesheets().add(css);
        }

        // =====================================================
        // ESC TO CLOSE
        // =====================================================
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        });

        // =====================================================
        // WINDOW CLOSE EVENT
        // =====================================================
        stage.setOnCloseRequest(event -> {
            stage.close();
        });

        // =====================================================
        // STAGE CONFIG
        // =====================================================
        stage.setScene(scene);
        stage.setResizable(false);

        // Đặt cửa sổ ở giữa màn hình
        stage.centerOnScreen();

        // show() thay vì showAndWait()
        // => không khóa cửa sổ chính
        stage.show();
    }


    private HBox createUserDetailRow(
            String labelText,
            String valueText,
            boolean primary
    ) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("user-detail-row");

        Label label = new Label(labelText);
        label.getStyleClass().add("user-detail-label");
        label.setPrefWidth(100);

        Label value = new Label(valueText != null ? valueText : "—");
        value.setWrapText(true);

        if (primary) {
            value.getStyleClass().add("user-detail-value-primary");
        } else {
            value.getStyleClass().add("user-detail-value");
        }

        HBox.setHgrow(value, Priority.ALWAYS);

        row.getChildren().addAll(label, value);

        return row;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Account Operation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Button createActionButton(String text, FontAwesomeSolid iconType) {
        Button btn = new Button(text);
        btn.getStyleClass().add("btn-secondary");
        FontIcon icon = new FontIcon(iconType);
        icon.setIconSize(12);
        icon.setIconColor(javafx.scene.paint.Color.web("#5C3D22"));
        btn.setGraphic(icon);
        btn.setGraphicTextGap(8);
        return btn;
    }

    private Button createTableIconBtn(FontAwesomeSolid iconType, String tooltipText) {
        Button btn = new Button();
        btn.getStyleClass().add("table-cell-btn");
        FontIcon icon = new FontIcon(iconType);
        icon.setIconSize(12);
        icon.setIconColor(javafx.scene.paint.Color.web("#5C3D22"));
        btn.setGraphic(icon);
        btn.setTooltip(new Tooltip(tooltipText));
        return btn;
    }
}
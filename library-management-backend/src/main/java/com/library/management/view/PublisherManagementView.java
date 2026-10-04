package com.library.management.view;

import com.library.management.entity.Book;
import com.library.management.entity.Publisher;
import com.library.management.service.PublisherService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Optional;

public class PublisherManagementView {

    private final PublisherService publisherService;

    private TableView<Publisher> tableView;
    private ObservableList<Publisher> masterData;
    private FilteredList<Publisher> filteredData;

    public PublisherManagementView(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    public Pane getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.getStyleClass().add("catalog-content-pane");

        // =====================================================
        // 1. HEADER SECTION
        // =====================================================
        VBox titleBox = new VBox(4);
        Label title = new Label("Publishing Houses & Imprints");
        title.getStyleClass().add("view-main-title");

        Label subtitle = new Label("Register literary publishers, university presses, and print distribution houses.");
        subtitle.getStyleClass().add("view-main-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // =====================================================
        // 2. TOOLBAR
        // =====================================================
        HBox toolbar = new HBox(14);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search publisher, email, phone, or address...");
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

        Button addBtn = new Button("NEW PUBLISHER ENTRY");
        addBtn.getStyleClass().add("btn-primary");
        FontIcon plusIcon = new FontIcon(FontAwesomeSolid.PLUS);
        plusIcon.setIconSize(12);
        plusIcon.setIconColor(javafx.scene.paint.Color.web("#F8F3EB"));
        addBtn.setGraphic(plusIcon);
        addBtn.setOnAction(e -> handleAddPublisher());

        toolbar.getChildren().addAll(searchField, statusFilter, spacer, refreshBtn, addBtn);

        // =====================================================
        // 3. TABLE SETUP
        // =====================================================
        tableView = new TableView<>();
        tableView.getStyleClass().add("vintage-table");
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<Publisher, String> colId = new TableColumn<>("# ID");
        colId.setPrefWidth(70);
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));

        TableColumn<Publisher, String> colName = new TableColumn<>("PUBLISHER / PRESS NAME");
        colName.setPrefWidth(240);
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        TableColumn<Publisher, String> colEmail = new TableColumn<>("DISPATCH EMAIL");
        colEmail.setPrefWidth(200);
        colEmail.setCellValueFactory(data -> {
            String email = data.getValue().getEmail();
            return new SimpleStringProperty(email != null && !email.isBlank() ? email : "—");
        });

        TableColumn<Publisher, String> colPhone = new TableColumn<>("CONTACT PHONE");
        colPhone.setPrefWidth(140);
        colPhone.setCellValueFactory(data -> {
            String phone = data.getValue().getPhone();
            return new SimpleStringProperty(phone != null && !phone.isBlank() ? phone : "—");
        });

        TableColumn<Publisher, String> colAddress = new TableColumn<>("EDITORIAL HEADQUARTERS");
        colAddress.setPrefWidth(220);
        colAddress.setCellValueFactory(data -> {
            String addr = data.getValue().getAddress();
            return new SimpleStringProperty(addr != null && !addr.isBlank() ? addr : "—");
        });

        TableColumn<Publisher, Void> colStatus = new TableColumn<>("STATUS");
        colStatus.setPrefWidth(120);
        colStatus.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Publisher p = getTableRow().getItem();
                    Label statusLabel = new Label(p.getStatus());
                    if ("ACTIVE".equalsIgnoreCase(p.getStatus())) {
                        statusLabel.getStyleClass().setAll("badge-status-active");
                    } else {
                        statusLabel.getStyleClass().setAll("badge-status-inactive");
                    }
                    setGraphic(statusLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Publisher, Void> colActions = new TableColumn<>("ACTIONS");
        colActions.setPrefWidth(120);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button detailBtn = createTableIconBtn(FontAwesomeSolid.INFO_CIRCLE, "Inspect Volume Dossier");
            private final Button editBtn = createTableIconBtn(FontAwesomeSolid.EDIT, "Edit Publisher");
            private final Button toggleBtn = createTableIconBtn(FontAwesomeSolid.POWER_OFF, "Toggle Status");

            {
                detailBtn.setOnAction(e -> {
                    Publisher publisher = getTableRow().getItem();
                    if (publisher != null) ArchivalDetailDialogs.showPublisherDetail(publisher);
                });

                editBtn.setOnAction(e -> {
                    Publisher p = getTableRow().getItem();
                    if (p != null) handleEditPublisher(p);
                });

                toggleBtn.setOnAction(e -> {
                    Publisher p = getTableRow().getItem();
                    if (p != null) handleToggleStatus(p);
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

        tableView.getColumns().addAll(colId, colName, colEmail, colPhone, colAddress, colStatus, colActions);

        // =====================================================
        // 4. FILTER & DATA BINDING
        // =====================================================
        masterData = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(searchField.getText(), statusFilter.getValue()));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(searchField.getText(), statusFilter.getValue()));

        tableView.setItems(filteredData);
        reloadData();

        root.getChildren().addAll(titleBox, toolbar, tableView);
        return root;
    }

    private void applyFilter(String query, String status) {
        filteredData.setPredicate(pub -> {
            boolean matchesSearch = true;
            if (query != null && !query.isBlank()) {
                String q = query.toLowerCase().trim();
                boolean nameMatch = pub.getName() != null && pub.getName().toLowerCase().contains(q);
                boolean emailMatch = pub.getEmail() != null && pub.getEmail().toLowerCase().contains(q);
                boolean phoneMatch = pub.getPhone() != null && pub.getPhone().toLowerCase().contains(q);
                boolean addrMatch = pub.getAddress() != null && pub.getAddress().toLowerCase().contains(q);
                matchesSearch = nameMatch || emailMatch || phoneMatch || addrMatch;
            }

            boolean matchesStatus = true;
            if (status != null && !"ALL STATUSES".equals(status)) {
                matchesStatus = status.equalsIgnoreCase(pub.getStatus());
            }

            return matchesSearch && matchesStatus;
        });
    }

    public void reloadData() {
        try {
            masterData.setAll(publisherService.getAllPublishers());
        } catch (Exception e) {
            showError("Failed to load publishers: " + e.getMessage());
        }
    }

    private void handleAddPublisher() {
        PublisherFormDialog dialog = new PublisherFormDialog(null);
        Optional<Publisher> result = dialog.showAndWait();
        result.ifPresent(pub -> {
            try {
                publisherService.createPublisher(pub);
                reloadData();
            } catch (Exception e) {
                showError("Failed to save publisher: " + e.getMessage());
            }
        });
    }

    private void handleEditPublisher(Publisher existing) {
        PublisherFormDialog dialog = new PublisherFormDialog(existing);
        Optional<Publisher> result = dialog.showAndWait();
        result.ifPresent(pub -> {
            try {
                publisherService.updatePublisher(existing.getId(), pub);
                reloadData();
            } catch (Exception e) {
                showError("Failed to update publisher: " + e.getMessage());
            }
        });
    }

    private void handleToggleStatus(Publisher publisher) {
        try {
            if ("ACTIVE".equalsIgnoreCase(publisher.getStatus())) {
                publisherService.deactivatePublisher(publisher.getId());
            } else {
                publisherService.activatePublisher(publisher.getId());
            }
            reloadData();
        } catch (Exception e) {
            showError("Failed to update publisher status: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Publisher Operation Failed");
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
package com.library.management.view;

import com.library.management.dto.BookShelfDetailResponse;
import com.library.management.dto.BookShelfListResponse;
import com.library.management.entity.BookShelf;
import com.library.management.repository.CategoryRepository;
import com.library.management.service.BookShelfService;
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

public class BookShelfManagementView {

    private final BookShelfService bookShelfService;
    private final CategoryRepository categoryRepository;

    private TableView<BookShelfListResponse> tableView;
    private ObservableList<BookShelfListResponse> masterData;
    private FilteredList<BookShelfListResponse> filteredData;

    public BookShelfManagementView(
            BookShelfService bookShelfService,
            CategoryRepository categoryRepository) {
        this.bookShelfService = bookShelfService;
        this.categoryRepository = categoryRepository;
    }

    public Pane getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.getStyleClass().add("catalog-content-pane");

        // =====================================================
        // 1. HEADER SECTION
        // =====================================================
        VBox titleBox = new VBox(4);
        Label title = new Label("Physical Stacks & Shelf Locations");
        title.getStyleClass().add("view-main-title");

        Label subtitle = new Label("Monitor physical storage capacity, volume distributions, and archival stack assignments.");
        subtitle.getStyleClass().add("view-main-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // =====================================================
        // 2. TOOLBAR
        // =====================================================
        HBox toolbar = new HBox(14);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search stack code or shelf name...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(300);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("ALL STATUSES", "ACTIVE", "INACTIVE");
        statusFilter.setValue("ALL STATUSES");
        statusFilter.getStyleClass().add("filter-combo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = createActionButton("Refresh", FontAwesomeSolid.SYNC_ALT);
        refreshBtn.setOnAction(e -> reloadData());

        Button addBtn = new Button("NEW SHELF LOCATION");
        addBtn.getStyleClass().add("btn-primary");
        FontIcon plusIcon = new FontIcon(FontAwesomeSolid.PLUS);
        plusIcon.setIconSize(12);
        plusIcon.setIconColor(javafx.scene.paint.Color.web("#F8F3EB"));
        addBtn.setGraphic(plusIcon);
        addBtn.setOnAction(e -> handleAddShelf());

        toolbar.getChildren().addAll(searchField, statusFilter, spacer, refreshBtn, addBtn);

        // =====================================================
        // 3. TABLE SETUP
        // =====================================================
        tableView = new TableView<>();
        tableView.getStyleClass().add("vintage-table");
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<BookShelfListResponse, String> colCode = new TableColumn<>("STACK CODE");
        colCode.setPrefWidth(120);
        colCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getShelfCode()));

        TableColumn<BookShelfListResponse, String> colName = new TableColumn<>("SHELF DESIGNATION");
        colName.setPrefWidth(220);
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        TableColumn<BookShelfListResponse, Void> colCapacity = new TableColumn<>("CAPACITY STATUS");
        colCapacity.setPrefWidth(240);
        colCapacity.setCellFactory(param -> new TableCell<>() {
            private final ProgressBar bar = new ProgressBar(0);
            private final Label label = new Label();
            private final VBox box = new VBox(3, label, bar);

            {
                bar.setPrefWidth(180);
                bar.getStyleClass().add("shelf-progress-bar");
                label.getStyleClass().add("shelf-progress-label");
                box.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    BookShelfListResponse shelf = getTableRow().getItem();
                    double progress = (double) shelf.getUsedCapacity() / shelf.getMaxCapacity();
                    bar.setProgress(progress);
                    label.setText(shelf.getUsedCapacity() + " / " + shelf.getMaxCapacity() + " volumes (" + shelf.getAvailableCapacity() + " vacant)");
                    setGraphic(box);
                }
            }
        });

        TableColumn<BookShelfListResponse, Void> colStatus = new TableColumn<>("STATUS");
        colStatus.setPrefWidth(110);
        colStatus.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    BookShelfListResponse s = getTableRow().getItem();
                    Label statusLabel = new Label(s.getStatus());
                    if ("ACTIVE".equalsIgnoreCase(s.getStatus())) {
                        statusLabel.getStyleClass().setAll("badge-status-active");
                    } else {
                        statusLabel.getStyleClass().setAll("badge-status-inactive");
                    }
                    setGraphic(statusLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<BookShelfListResponse, Void> colActions = new TableColumn<>("ACTIONS");
        colActions.setPrefWidth(150);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button detailBtn = createTableIconBtn(FontAwesomeSolid.LIST_ALT, "Inspect Stored Volumes");
            private final Button editBtn = createTableIconBtn(FontAwesomeSolid.EDIT, "Edit Shelf Info");
            private final Button toggleBtn = createTableIconBtn(FontAwesomeSolid.POWER_OFF, "Toggle Status");

            {
                detailBtn.setOnAction(e -> {
                    BookShelfListResponse s = getTableRow().getItem();
                    if (s != null) handleViewDetail(s);
                });

                editBtn.setOnAction(e -> {
                    BookShelfListResponse s = getTableRow().getItem();
                    if (s != null) handleEditShelf(s);
                });

                toggleBtn.setOnAction(e -> {
                    BookShelfListResponse s = getTableRow().getItem();
                    if (s != null) handleToggleStatus(s);
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

        tableView.getColumns().addAll(colCode, colName, colCapacity, colStatus, colActions);

        // =====================================================
        // 4. FILTER & DATA BINDING
        // =====================================================
        masterData = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(masterData, s -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(searchField.getText(), statusFilter.getValue()));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(searchField.getText(), statusFilter.getValue()));

        tableView.setItems(filteredData);
        reloadData();

        root.getChildren().addAll(titleBox, toolbar, tableView);
        return root;
    }

    private void applyFilter(String query, String status) {
        filteredData.setPredicate(shelf -> {
            boolean matchesSearch = true;
            if (query != null && !query.isBlank()) {
                String q = query.toLowerCase().trim();
                boolean codeMatch = shelf.getShelfCode() != null && shelf.getShelfCode().toLowerCase().contains(q);
                boolean nameMatch = shelf.getName() != null && shelf.getName().toLowerCase().contains(q);
                matchesSearch = codeMatch || nameMatch;
            }

            boolean matchesStatus = true;
            if (status != null && !"ALL STATUSES".equals(status)) {
                matchesStatus = status.equalsIgnoreCase(shelf.getStatus());
            }

            return matchesSearch && matchesStatus;
        });
    }

    public void reloadData() {
        try {
            masterData.setAll(bookShelfService.getAllShelves());
        } catch (Exception e) {
            showError("Failed to load shelf records: " + e.getMessage());
        }
    }

    private void handleAddShelf() {
        BookShelfFormDialog dialog = new BookShelfFormDialog(null, categoryRepository.findAll());
        Optional<BookShelf> result = dialog.showAndWait();
        result.ifPresent(shelf -> {
            try {
                bookShelfService.createShelf(shelf);
                reloadData();
            } catch (Exception e) {
                showError("Failed to register shelf: " + e.getMessage());
            }
        });
    }

    private void handleEditShelf(BookShelfListResponse selected) {
        try {
            BookShelf shelf = bookShelfService.getShelfById(selected.getId());
            BookShelfFormDialog dialog = new BookShelfFormDialog(shelf, categoryRepository.findAll());
            Optional<BookShelf> result = dialog.showAndWait();
            result.ifPresent(updated -> {
                try {
                    bookShelfService.updateShelf(selected.getId(), updated);
                    reloadData();
                } catch (Exception ex) {
                    showError("Failed to update shelf: " + ex.getMessage());
                }
            });
        } catch (Exception e) {
            showError("Failed to retrieve shelf details: " + e.getMessage());
        }
    }

    private void handleViewDetail(BookShelfListResponse selected) {
        try {
            BookShelfDetailResponse detail = bookShelfService.getShelfDetail(selected.getId());
            ShelfDetailDialog dialog = new ShelfDetailDialog(detail);
            dialog.showAndWait();
        } catch (Exception e) {
            showError("Failed to fetch stack allocation detail: " + e.getMessage());
        }
    }

    private void handleToggleStatus(BookShelfListResponse shelf) {
        try {
            if ("ACTIVE".equalsIgnoreCase(shelf.getStatus())) {
                bookShelfService.deactivateShelf(shelf.getId());
            } else {
                bookShelfService.activateShelf(shelf.getId());
            }
            reloadData();
        } catch (Exception e) {
            showError("Failed to update shelf status: " + e.getMessage());
        }
    }

    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Atelier • Notice");

        DialogPane dialogPane = alert.getDialogPane();

        dialogPane.getStylesheets().clear();
        dialogPane.getStylesheets().add(
                getClass()
                        .getResource("/css/dashboard.css")
                        .toExternalForm()
        );

        dialogPane.getStyleClass().add("atelier-error-dialog");

        Label headerLabel =
                new Label("Operation Unsuccessful");

        headerLabel.getStyleClass()
                .add("error-dialog-header");

        Label contentLabel =
                new Label(message);

        contentLabel.setWrapText(true);
        contentLabel.getStyleClass()
                .add("error-dialog-content");

        VBox contentBox =
                new VBox(
                        10,
                        headerLabel,
                        contentLabel
                );

        contentBox.setPadding(
                new Insets(5)
        );

        contentBox.getStyleClass()
                .add("error-dialog-content-box");

        dialogPane.setContent(contentBox);

        ButtonType okType =
                new ButtonType(
                        "OK",
                        ButtonBar.ButtonData.OK_DONE
                );

        dialogPane.getButtonTypes().clear();
        dialogPane.getButtonTypes().add(okType);

        Button okButton =
                (Button) dialogPane.lookupButton(okType);

        okButton.getStyleClass()
                .add("error-dialog-ok-button");

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
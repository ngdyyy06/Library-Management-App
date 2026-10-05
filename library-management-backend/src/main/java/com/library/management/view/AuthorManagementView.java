package com.library.management.view;

import com.library.management.entity.Author;
import com.library.management.entity.Book;
import com.library.management.service.AuthorService;
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

public class AuthorManagementView {

    private final AuthorService authorService;

    private TableView<Author> tableView;
    private ObservableList<Author> masterData;
    private FilteredList<Author> filteredData;

    public AuthorManagementView(AuthorService authorService) {
        this.authorService = authorService;
    }

    public Pane getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.getStyleClass().add("catalog-content-pane");

        // =====================================================
        // 1. HEADER SECTION
        // =====================================================
        VBox titleBox = new VBox(4);
        Label title = new Label("Authors & Literary Figures");
        title.getStyleClass().add("view-main-title");

        Label subtitle = new Label("Maintain biographical profiles, creator records, and scholarly attributions.");
        subtitle.getStyleClass().add("view-main-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // =====================================================
        // 2. TOOLBAR
        // =====================================================
        HBox toolbar = new HBox(14);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search author name or biography...");
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

        Button addBtn = new Button("NEW AUTHOR ENTRY");
        addBtn.getStyleClass().add("btn-primary");
        FontIcon plusIcon = new FontIcon(FontAwesomeSolid.PLUS);
        plusIcon.setIconSize(12);
        plusIcon.setIconColor(javafx.scene.paint.Color.web("#F8F3EB"));
        addBtn.setGraphic(plusIcon);
        addBtn.setOnAction(e -> handleAddAuthor());

        toolbar.getChildren().addAll(searchField, statusFilter, spacer, refreshBtn, addBtn);

        // =====================================================
        // 3. TABLE SETUP
        // =====================================================
        tableView = new TableView<>();
        tableView.getStyleClass().add("vintage-table");
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<Author, String> colId = new TableColumn<>("# ID");
        colId.setPrefWidth(70);
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));

        TableColumn<Author, String> colName = new TableColumn<>("AUTHOR NAME");
        colName.setPrefWidth(240);
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        TableColumn<Author, String> colBio = new TableColumn<>("BIOGRAPHICAL NOTES & SUMMARY");
        colBio.setPrefWidth(420);
        colBio.setCellValueFactory(data -> {
            String bio = data.getValue().getBiography();
            if (bio == null || bio.isBlank()) {
                return new SimpleStringProperty("— No biography recorded —");
            }
            return new SimpleStringProperty(bio);
        });

        TableColumn<Author, Void> colStatus = new TableColumn<>("STATUS");
        colStatus.setPrefWidth(120);
        colStatus.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Author a = getTableRow().getItem();
                    Label statusLabel = new Label(a.getStatus());
                    if ("ACTIVE".equalsIgnoreCase(a.getStatus())) {
                        statusLabel.getStyleClass().setAll("badge-status-active");
                    } else {
                        statusLabel.getStyleClass().setAll("badge-status-inactive");
                    }
                    setGraphic(statusLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Author, Void> colActions = new TableColumn<>("ACTIONS");
        colActions.setPrefWidth(120);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button detailBtn = createTableIconBtn(FontAwesomeSolid.INFO_CIRCLE, "Inspect Volume Dossier");
            private final Button editBtn = createTableIconBtn(FontAwesomeSolid.EDIT, "Edit Author Profile");
            private final Button toggleBtn = createTableIconBtn(FontAwesomeSolid.POWER_OFF, "Toggle Status");

            {
                detailBtn.setOnAction(e -> {
                    Author author = getTableRow().getItem();
                    if (author != null) ArchivalDetailDialogs.showAuthorDetail(author);
                });

                editBtn.setOnAction(e -> {
                    Author a = getTableRow().getItem();
                    if (a != null) handleEditAuthor(a);
                });

                toggleBtn.setOnAction(e -> {
                    Author a = getTableRow().getItem();
                    if (a != null) handleToggleStatus(a);
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

        tableView.getColumns().addAll(colId, colName, colBio, colStatus, colActions);

        // =====================================================
        // 4. FILTER & DATA BINDING
        // =====================================================
        masterData = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(masterData, a -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(searchField.getText(), statusFilter.getValue()));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(searchField.getText(), statusFilter.getValue()));

        tableView.setItems(filteredData);
        reloadData();

        root.getChildren().addAll(titleBox, toolbar, tableView);
        return root;
    }

    private void applyFilter(String query, String status) {
        filteredData.setPredicate(author -> {
            boolean matchesSearch = true;
            if (query != null && !query.isBlank()) {
                String q = query.toLowerCase().trim();
                boolean nameMatch = author.getName() != null && author.getName().toLowerCase().contains(q);
                boolean bioMatch = author.getBiography() != null && author.getBiography().toLowerCase().contains(q);
                matchesSearch = nameMatch || bioMatch;
            }

            boolean matchesStatus = true;
            if (status != null && !"ALL STATUSES".equals(status)) {
                matchesStatus = status.equalsIgnoreCase(author.getStatus());
            }

            return matchesSearch && matchesStatus;
        });
    }

    public void reloadData() {
        try {
            masterData.setAll(authorService.getAllAuthors());
        } catch (Exception e) {
            showError("Failed to load authors: " + e.getMessage());
        }
    }

    private void handleAddAuthor() {
        AuthorFormDialog dialog = new AuthorFormDialog(null);
        Optional<Author> result = dialog.showAndWait();
        result.ifPresent(author -> {
            try {
                authorService.createAuthor(author);
                reloadData();
            } catch (Exception e) {
                showError("Failed to save author: " + e.getMessage());
            }
        });
    }

    private void handleEditAuthor(Author existing) {
        AuthorFormDialog dialog = new AuthorFormDialog(existing);
        Optional<Author> result = dialog.showAndWait();
        result.ifPresent(author -> {
            try {
                authorService.updateAuthor(existing.getId(), author);
                reloadData();
            } catch (Exception e) {
                showError("Failed to update author: " + e.getMessage());
            }
        });
    }

    private void handleToggleStatus(Author author) {
        try {
            if ("ACTIVE".equalsIgnoreCase(author.getStatus())) {
                authorService.deactivateAuthor(author.getId());
            } else {
                authorService.activateAuthor(author.getId());
            }
            reloadData();
        } catch (Exception e) {
            showError("Failed to update author status: " + e.getMessage());
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
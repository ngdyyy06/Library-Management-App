package com.library.management.view;

import com.library.management.entity.BookShelf;
import com.library.management.entity.Category;
import com.library.management.repository.BookShelfRepository;
import com.library.management.repository.CategoryRepository;
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

public class CategoryManagementView {

    private final CategoryRepository categoryRepository;
    private final BookShelfRepository bookShelfRepository;

    private TableView<Category> tableView;
    private ObservableList<Category> masterData;
    private FilteredList<Category> filteredData;

    public CategoryManagementView(
            CategoryRepository categoryRepository,
            BookShelfRepository bookShelfRepository) {
        this.categoryRepository = categoryRepository;
        this.bookShelfRepository = bookShelfRepository;
    }

    public Pane getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.getStyleClass().add("catalog-content-pane");

        // =====================================================
        // 1. HEADER SECTION
        // =====================================================
        VBox titleBox = new VBox(4);
        Label title = new Label("Genres & Topical Classification");
        title.getStyleClass().add("view-main-title");

        Label subtitle = new Label("Define subject areas, literary classifications, and their assigned physical default shelves.");
        subtitle.getStyleClass().add("view-main-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // =====================================================
        // 2. TOOLBAR
        // =====================================================
        HBox toolbar = new HBox(14);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search category or shelf...");
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

        Button addBtn = new Button("NEW GENRE CLASSIFICATION");
        addBtn.getStyleClass().add("btn-primary");
        FontIcon plusIcon = new FontIcon(FontAwesomeSolid.PLUS);
        plusIcon.setIconSize(12);
        plusIcon.setIconColor(javafx.scene.paint.Color.web("#F8F3EB"));
        addBtn.setGraphic(plusIcon);
        addBtn.setOnAction(e -> handleAddCategory());

        toolbar.getChildren().addAll(searchField, statusFilter, spacer, refreshBtn, addBtn);

        // =====================================================
        // 3. TABLE SETUP
        // =====================================================
        tableView = new TableView<>();
        tableView.getStyleClass().add("vintage-table");
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<Category, String> colId = new TableColumn<>("# ID");
        colId.setPrefWidth(70);
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));

        TableColumn<Category, String> colName = new TableColumn<>("GENRE / CATEGORY NAME");
        colName.setPrefWidth(280);
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        TableColumn<Category, String> colShelf = new TableColumn<>("ASSIGNED DEFAULT SHELF");
        colShelf.setPrefWidth(240);
        colShelf.setCellValueFactory(data -> {
            BookShelf shelf = data.getValue().getDefaultShelf();
            if (shelf != null) {
                return new SimpleStringProperty(shelf.getName() + " [" + shelf.getShelfCode() + "]");
            }
            return new SimpleStringProperty("— No shelf mapped —");
        });

        TableColumn<Category, Void> colStatus = new TableColumn<>("STATUS");
        colStatus.setPrefWidth(120);
        colStatus.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Category cat = getTableRow().getItem();
                    Label statusLabel = new Label(cat.getStatus());
                    if ("ACTIVE".equalsIgnoreCase(cat.getStatus())) {
                        statusLabel.getStyleClass().setAll("badge-status-active");
                    } else {
                        statusLabel.getStyleClass().setAll("badge-status-inactive");
                    }
                    setGraphic(statusLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Category, Void> colActions = new TableColumn<>("ACTIONS");
        colActions.setPrefWidth(140);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button detailBtn = createTableIconBtn(FontAwesomeSolid.INFO_CIRCLE, "Inspect Classification Dossier");
            private final Button editBtn = createTableIconBtn(FontAwesomeSolid.EDIT, "Edit Category");
            private final Button toggleBtn = createTableIconBtn(FontAwesomeSolid.POWER_OFF, "Toggle Status");
            private final Button deleteBtn = createTableIconBtn(FontAwesomeSolid.TRASH, "Delete Classification");

            {
                detailBtn.setOnAction(e -> {
                    Category cat = getTableRow().getItem();

                    if (cat != null) {
                        ArchivalDetailDialogs.showCategoryDetail(cat);
                    }
                });

                editBtn.setOnAction(e -> {
                    Category cat = getTableRow().getItem();
                    if (cat != null) handleEditCategory(cat);
                });

                toggleBtn.setOnAction(e -> {
                    Category cat = getTableRow().getItem();
                    if (cat != null) handleToggleStatus(cat);
                });

                deleteBtn.setOnAction(e -> {
                    Category cat = getTableRow().getItem();
                    if (cat != null) handleDeleteCategory(cat);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8, editBtn, toggleBtn, deleteBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        tableView.getColumns().addAll(colId, colName, colShelf, colStatus, colActions);

        // =====================================================
        // 4. FILTER & DATA BINDING
        // =====================================================
        masterData = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(masterData, c -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(searchField.getText(), statusFilter.getValue()));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(searchField.getText(), statusFilter.getValue()));

        tableView.setItems(filteredData);
        reloadData();

        root.getChildren().addAll(titleBox, toolbar, tableView);
        return root;

    }

    private void applyFilter(String query, String status) {
        filteredData.setPredicate(cat -> {
            boolean matchesSearch = true;
            if (query != null && !query.isBlank()) {
                String q = query.toLowerCase().trim();
                boolean nameMatch = cat.getName() != null && cat.getName().toLowerCase().contains(q);
                boolean shelfMatch = cat.getDefaultShelf() != null &&
                        (cat.getDefaultShelf().getName().toLowerCase().contains(q) ||
                                cat.getDefaultShelf().getShelfCode().toLowerCase().contains(q));
                matchesSearch = nameMatch || shelfMatch;
            }

            boolean matchesStatus = true;
            if (status != null && !"ALL STATUSES".equals(status)) {
                matchesStatus = status.equalsIgnoreCase(cat.getStatus());
            }

            return matchesSearch && matchesStatus;
        });
    }

    public void reloadData() {
        try {
            masterData.setAll(categoryRepository.findAll());
        } catch (Exception e) {
            showError("Failed to load classifications: " + e.getMessage());
        }
    }

    private void handleAddCategory() {
        CategoryFormDialog dialog = new CategoryFormDialog(null, bookShelfRepository.findAll());
        Optional<Category> result = dialog.showAndWait();
        result.ifPresent(cat -> {
            try {
                if (categoryRepository.existsByNameIgnoreCase(cat.getName())) {
                    showError("A classification with this title already exists.");
                    return;
                }
                cat.setStatus("ACTIVE");
                categoryRepository.save(cat);
                reloadData();
            } catch (Exception e) {
                showError("Failed to create classification: " + e.getMessage());
            }
        });
    }

    private void handleEditCategory(Category existing) {
        CategoryFormDialog dialog = new CategoryFormDialog(existing, bookShelfRepository.findAll());
        Optional<Category> result = dialog.showAndWait();
        result.ifPresent(cat -> {
            try {
                categoryRepository.save(cat);
                reloadData();
            } catch (Exception e) {
                showError("Failed to update classification: " + e.getMessage());
            }
        });
    }

    private void handleToggleStatus(Category cat) {
        try {
            String newStatus = "ACTIVE".equalsIgnoreCase(cat.getStatus()) ? "INACTIVE" : "ACTIVE";
            cat.setStatus(newStatus);
            categoryRepository.save(cat);
            reloadData();
        } catch (Exception e) {
            showError("Failed to update status: " + e.getMessage());
        }
    }

    private void handleDeleteCategory(Category cat) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Remove Archival Classification");
        confirm.setContentText("Are you sure you wish to delete the category: " + cat.getName() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                categoryRepository.deleteById(cat.getId());
                reloadData();
            } catch (Exception e) {
                showError("Cannot delete category (it may still be bound to catalog volumes): " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Classification Operation Failed");
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
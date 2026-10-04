package com.library.management.view;

import com.library.management.entity.Author;
import com.library.management.entity.Book;
import com.library.management.entity.Category;
import com.library.management.repository.CategoryRepository;
import com.library.management.repository.PublisherRepository;
import com.library.management.service.BookService;
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
import java.util.stream.Collectors;

public class BookManagementView {

    private final BookService bookService;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;

    private TableView<Book> tableView;
    private ObservableList<Book> masterData;
    private FilteredList<Book> filteredData;

    public BookManagementView(
            BookService bookService,
            CategoryRepository categoryRepository,
            PublisherRepository publisherRepository) {
        this.bookService = bookService;
        this.categoryRepository = categoryRepository;
        this.publisherRepository = publisherRepository;
    }

    public Pane getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.getStyleClass().add("catalog-content-pane");

        // 1. Header Section
        VBox titleBox = new VBox(4);
        Label title = new Label("Book Collection & Archive");
        title.getStyleClass().add("view-main-title");

        Label subtitle = new Label("Manage catalog entries, volume acquisitions, shelf allocation, and edition statuses.");
        subtitle.getStyleClass().add("view-main-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // 2. Toolbar (Đã gỡ nút Add Book)
        HBox toolbar = new HBox(14);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search title, ISBN, or author...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(350);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("ALL STATUSES", "ACTIVE", "INACTIVE");
        statusFilter.setValue("ALL STATUSES");
        statusFilter.getStyleClass().add("filter-combo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = createActionButton("Refresh", FontAwesomeSolid.SYNC_ALT);
        refreshBtn.setOnAction(e -> reloadData());

        toolbar.getChildren().addAll(searchField, statusFilter, spacer, refreshBtn);

        // 3. Table Setup
        tableView = new TableView<>();
        tableView.getStyleClass().add("vintage-table");
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<Book, String> colIsbn = new TableColumn<>("ISBN");
        colIsbn.setPrefWidth(125);
        colIsbn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIsbn() != null ? data.getValue().getIsbn() : "—"));

        TableColumn<Book, String> colTitle = new TableColumn<>("TITLE & VOLUME");
        colTitle.setPrefWidth(260);
        colTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle() != null ? data.getValue().getTitle() : "—"));

        TableColumn<Book, String> colAuthors = new TableColumn<>("AUTHORS");
        colAuthors.setPrefWidth(180);
        colAuthors.setCellValueFactory(data -> {
            try {
                if (data.getValue().getAuthors() == null || data.getValue().getAuthors().isEmpty()) {
                    return new SimpleStringProperty("—");
                }
                String authorsStr = data.getValue().getAuthors().stream()
                        .map(Author::getName)
                        .collect(Collectors.joining(", "));
                return new SimpleStringProperty(authorsStr);
            } catch (Exception e) {
                return new SimpleStringProperty("—");
            }
        });

        TableColumn<Book, String> colCategory = new TableColumn<>("PRIMARY GENRE");
        colCategory.setPrefWidth(140);
        colCategory.setCellValueFactory(data -> {
            try {
                Category cat = data.getValue().getPrimaryCategory();
                return new SimpleStringProperty(cat != null ? cat.getName() : "—");
            } catch (Exception e) {
                return new SimpleStringProperty("—");
            }
        });

        TableColumn<Book, String> colShelf = new TableColumn<>("SHELF");
        colShelf.setPrefWidth(120);
        colShelf.setCellValueFactory(data -> {
            try {
                if (data.getValue().getShelf() != null) {
                    return new SimpleStringProperty(data.getValue().getShelf().getName());
                }
            } catch (Exception ignored) {}
            return new SimpleStringProperty("Unassigned");
        });

        TableColumn<Book, String> colPrice = new TableColumn<>("PRICE");
        colPrice.setPrefWidth(100);
        colPrice.setCellValueFactory(data -> {
            if (data.getValue().getPrice() != null) {
                return new SimpleStringProperty(data.getValue().getPrice().toPlainString() + " VND");
            }
            return new SimpleStringProperty("0 VND");
        });

        TableColumn<Book, String> colStock = new TableColumn<>("HOLDINGS");
        colStock.setPrefWidth(110);
        colStock.setCellValueFactory(data -> {
            int avail = data.getValue().getAvailableQuantity() != null ? data.getValue().getAvailableQuantity() : 0;
            int total = data.getValue().getTotalQuantity() != null ? data.getValue().getTotalQuantity() : 0;
            return new SimpleStringProperty(avail + " / " + total);
        });

        TableColumn<Book, Void> colStatus = new TableColumn<>("STATUS");
        colStatus.setPrefWidth(110);
        colStatus.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Book book = getTableRow().getItem();
                    Label statusLabel = new Label(book.getStatus());
                    if ("ACTIVE".equalsIgnoreCase(book.getStatus())) {
                        statusLabel.getStyleClass().setAll("badge-status-active");
                    } else {
                        statusLabel.getStyleClass().setAll("badge-status-inactive");
                    }
                    setGraphic(statusLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // ACTIONS: Gồm 3 nút (Xem Chi Tiết, Chỉnh Sửa, Bật/Tắt Trạng Thái)
        TableColumn<Book, Void> colActions = new TableColumn<>("ACTIONS");
        colActions.setPrefWidth(150);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button detailBtn = createTableIconBtn(FontAwesomeSolid.INFO_CIRCLE, "Inspect Volume Dossier");
            private final Button editBtn = createTableIconBtn(FontAwesomeSolid.EDIT, "Edit Volume Details");
            private final Button toggleBtn = createTableIconBtn(FontAwesomeSolid.POWER_OFF, "Toggle Status");

            {
                detailBtn.setOnAction(e -> {
                    Book book = getTableRow().getItem();
                    if (book != null) ArchivalDetailDialogs.showBookDetail(book);
                });

                editBtn.setOnAction(e -> {
                    Book book = getTableRow().getItem();
                    if (book != null) handleEditBook(book);
                });

                toggleBtn.setOnAction(e -> {
                    Book book = getTableRow().getItem();
                    if (book != null) handleToggleStatus(book);
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

        tableView.getColumns().addAll(
                colIsbn, colTitle, colAuthors, colCategory,
                colShelf, colPrice, colStock, colStatus, colActions
        );

        // 4. Data Binding
        masterData = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(masterData, b -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(searchField.getText(), statusFilter.getValue()));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(searchField.getText(), statusFilter.getValue()));

        tableView.setItems(filteredData);
        reloadData();

        root.getChildren().addAll(titleBox, toolbar, tableView);
        return root;
    }

    private void applyFilter(String query, String status) {
        filteredData.setPredicate(book -> {
            boolean matchesSearch = true;
            if (query != null && !query.isBlank()) {
                String q = query.toLowerCase().trim();
                boolean titleMatch = book.getTitle() != null && book.getTitle().toLowerCase().contains(q);
                boolean isbnMatch = book.getIsbn() != null && book.getIsbn().toLowerCase().contains(q);
                matchesSearch = titleMatch || isbnMatch;
            }

            boolean matchesStatus = true;
            if (status != null && !"ALL STATUSES".equals(status)) {
                matchesStatus = status.equalsIgnoreCase(book.getStatus());
            }

            return matchesSearch && matchesStatus;
        });
    }

    public void reloadData() {
        try {
            masterData.setAll(bookService.getAllBooks());
        } catch (Exception e) {
            showError("Failed to fetch books: " + e.getMessage());
        }
    }

    private void handleEditBook(Book book) {
        BookFormDialog dialog = new BookFormDialog(
                book,
                categoryRepository.findAll(),
                publisherRepository.findAll()
        );
        Optional<com.library.management.dto.CreateBookRequest> result = dialog.showAndWait();
        result.ifPresent(req -> {
            try {
                bookService.updateBook(book.getId(), req);
                reloadData();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
    }

    private void handleToggleStatus(Book book) {
        try {
            if ("ACTIVE".equalsIgnoreCase(book.getStatus())) {
                bookService.deactivateBook(book.getId());
            } else {
                bookService.activateBook(book.getId());
            }
            reloadData();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Archival Action Failed");
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
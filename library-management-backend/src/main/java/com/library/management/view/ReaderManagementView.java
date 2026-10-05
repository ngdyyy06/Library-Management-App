package com.library.management.view;

import com.library.management.dto.CreateReaderRequest;
import com.library.management.entity.Reader;
import com.library.management.repository.BorrowingDetailRepository;
import com.library.management.repository.BorrowingRepository;
import com.library.management.repository.LibraryCardRepository;
import com.library.management.service.ReaderService;
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

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ReaderManagementView {

    private final ReaderService readerService;
    private final LibraryCardRepository libraryCardRepository;
    private final BorrowingRepository borrowingRepository;
    private final BorrowingDetailRepository borrowingDetailRepository;

    private TableView<Reader> tableView;
    private ObservableList<Reader> masterData;
    private FilteredList<Reader> filteredData;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public ReaderManagementView(
            ReaderService readerService,
            LibraryCardRepository libraryCardRepository,
            BorrowingRepository borrowingRepository,
            BorrowingDetailRepository borrowingDetailRepository) {
        this.readerService = readerService;
        this.libraryCardRepository = libraryCardRepository;
        this.borrowingRepository = borrowingRepository;
        this.borrowingDetailRepository = borrowingDetailRepository;
    }

    public Pane getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.getStyleClass().add("catalog-content-pane");

        // 1. Header
        VBox titleBox = new VBox(4);
        Label title = new Label("Registered Patrons & Fellowship");
        title.getStyleClass().add("view-main-title");

        Label subtitle = new Label("Manage active library membership cards, reader credentials, and membership dues.");
        subtitle.getStyleClass().add("view-main-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // 2. Toolbar
        HBox toolbar = new HBox(14);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search patron code, full name, phone, or email...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(340);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("ALL STATUSES", "ACTIVE", "INACTIVE");
        statusFilter.setValue("ALL STATUSES");
        statusFilter.getStyleClass().add("filter-combo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = createActionButton("Refresh", FontAwesomeSolid.SYNC_ALT);
        refreshBtn.setOnAction(e -> reloadData());

        Button addBtn = new Button("ISSUE PATRON CARD");
        addBtn.getStyleClass().add("btn-primary");
        FontIcon plusIcon = new FontIcon(FontAwesomeSolid.ID_CARD);
        plusIcon.setIconSize(12);
        plusIcon.setIconColor(javafx.scene.paint.Color.web("#F8F3EB"));
        addBtn.setGraphic(plusIcon);
        addBtn.setOnAction(e -> handleAddReader());

        toolbar.getChildren().addAll(searchField, statusFilter, spacer, refreshBtn, addBtn);

        // 3. Table
        tableView = new TableView<>();
        tableView.getStyleClass().add("vintage-table");
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<Reader, String> colCode = new TableColumn<>("PATRON CODE");
        colCode.setPrefWidth(120);
        colCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReaderCode()));

        TableColumn<Reader, String> colName = new TableColumn<>("FULL NAME");
        colName.setPrefWidth(190);
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));

        TableColumn<Reader, String> colPhone = new TableColumn<>("TELEPHONE");
        colPhone.setPrefWidth(120);
        colPhone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));

        TableColumn<Reader, String> colEmail = new TableColumn<>("DISPATCH EMAIL");
        colEmail.setPrefWidth(190);
        colEmail.setCellValueFactory(data -> {
            String email = data.getValue().getEmail();
            return new SimpleStringProperty(email != null && !email.isBlank() ? email : "—");
        });

        TableColumn<Reader, String> colDob = new TableColumn<>("BIRTHDATE");
        colDob.setPrefWidth(110);
        colDob.setCellValueFactory(data -> {
            if (data.getValue().getDateOfBirth() != null) {
                return new SimpleStringProperty(data.getValue().getDateOfBirth().format(dateFormatter));
            }
            return new SimpleStringProperty("—");
        });

        TableColumn<Reader, Void> colStatus = new TableColumn<>("MEMBERSHIP");
        colStatus.setPrefWidth(110);
        colStatus.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Reader r = getTableRow().getItem();
                    Label statusLabel = new Label(r.getStatus());
                    if ("ACTIVE".equalsIgnoreCase(r.getStatus())) {
                        statusLabel.getStyleClass().setAll("badge-status-active");
                    } else {
                        statusLabel.getStyleClass().setAll("badge-status-inactive");
                    }
                    setGraphic(statusLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // ACTIONS: 4 Nút (Xem Chi Tiết, Xem Thẻ, Chỉnh Sửa, Khóa/Mở)
        TableColumn<Reader, Void> colActions = new TableColumn<>("ACTIONS");
        colActions.setPrefWidth(180);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button detailBtn = createTableIconBtn(FontAwesomeSolid.INFO_CIRCLE, "Inspect Patron Dossier");
            private final Button cardBtn = createTableIconBtn(FontAwesomeSolid.ID_BADGE, "Inspect Fellowship Card & Loans");
            private final Button editBtn = createTableIconBtn(FontAwesomeSolid.EDIT, "Modify Patron Record");
            private final Button toggleBtn = createTableIconBtn(FontAwesomeSolid.POWER_OFF, "Suspend / Reactivate Card");

            {
                detailBtn.setOnAction(e -> {
                    Reader r = getTableRow().getItem();
                    if (r != null) ArchivalDetailDialogs.showReaderDetail(r);
                });

                cardBtn.setOnAction(e -> {
                    Reader r = getTableRow().getItem();
                    if (r != null) handleViewCard(r);
                });

                editBtn.setOnAction(e -> {
                    Reader r = getTableRow().getItem();
                    if (r != null) handleEditReader(r);
                });

                toggleBtn.setOnAction(e -> {
                    Reader r = getTableRow().getItem();
                    if (r != null) handleToggleStatus(r);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(6, detailBtn, cardBtn, editBtn, toggleBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        tableView.getColumns().addAll(colCode, colName, colPhone, colEmail, colDob, colStatus, colActions);

        // 4. Data Binding
        masterData = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(masterData, r -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(searchField.getText(), statusFilter.getValue()));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter(searchField.getText(), statusFilter.getValue()));

        tableView.setItems(filteredData);
        reloadData();

        root.getChildren().addAll(titleBox, toolbar, tableView);
        return root;
    }

    private void applyFilter(String query, String status) {
        filteredData.setPredicate(reader -> {
            boolean matchesSearch = true;
            if (query != null && !query.isBlank()) {
                String q = query.toLowerCase().trim();
                boolean codeMatch = reader.getReaderCode() != null && reader.getReaderCode().toLowerCase().contains(q);
                boolean nameMatch = reader.getFullName() != null && reader.getFullName().toLowerCase().contains(q);
                boolean phoneMatch = reader.getPhone() != null && reader.getPhone().toLowerCase().contains(q);
                boolean emailMatch = reader.getEmail() != null && reader.getEmail().toLowerCase().contains(q);
                matchesSearch = codeMatch || nameMatch || phoneMatch || emailMatch;
            }

            boolean matchesStatus = true;
            if (status != null && !"ALL STATUSES".equals(status)) {
                matchesStatus = status.equalsIgnoreCase(reader.getStatus());
            }

            return matchesSearch && matchesStatus;
        });
    }

    public void reloadData() {
        try {
            masterData.setAll(readerService.getAllReaders());
        } catch (Exception e) {
            showError("Failed to load patrons: " + e.getMessage());
        }
    }

    private void handleViewCard(Reader reader) {
        PatronCardDialog dialog = new PatronCardDialog(
                reader,
                libraryCardRepository,
                borrowingRepository,
                borrowingDetailRepository
        );
        dialog.showAndWait();
    }

    private void handleAddReader() {
        ReaderFormDialog dialog = new ReaderFormDialog(null);
        Optional<CreateReaderRequest> result = dialog.showAndWait();
        result.ifPresent(req -> {
            try {
                readerService.createReader(req);
                reloadData();
            } catch (Exception e) {
                showError("Failed to issue membership card: " + e.getMessage());
            }
        });
    }

    private void handleEditReader(Reader selected) {
        ReaderFormDialog dialog = new ReaderFormDialog(selected);
        Optional<CreateReaderRequest> result = dialog.showAndWait();
        result.ifPresent(req -> {
            try {
                readerService.updateReader(selected.getId(), req);
                reloadData();
            } catch (Exception e) {
                showError("Failed to update patron: " + e.getMessage());
            }
        });
    }

    private void handleToggleStatus(Reader reader) {
        try {
            if ("ACTIVE".equalsIgnoreCase(reader.getStatus())) {
                readerService.deactivateReader(reader.getId());
            } else {
                readerService.activateReader(reader.getId());
            }
            reloadData();
        } catch (Exception e) {
            showError("Failed to update membership card status: " + e.getMessage());
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
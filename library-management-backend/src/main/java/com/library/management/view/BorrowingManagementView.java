package com.library.management.view;

import com.library.management.dto.CreateBorrowingRequest;
import com.library.management.entity.Book;
import com.library.management.entity.Borrowing;
import com.library.management.entity.BorrowingDetail;
import com.library.management.entity.Reader;
import com.library.management.repository.BookRepository;
import com.library.management.repository.ReaderRepository;
import com.library.management.service.BorrowingService;
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
import java.util.List;
import java.util.Optional;

public class BorrowingManagementView {

    private final BorrowingService borrowingService;
    private final ReaderRepository readerRepository;
    private final BookRepository bookRepository;

    private TableView<Borrowing> tableView;
    private ObservableList<Borrowing> masterData;
    private FilteredList<Borrowing> filteredData;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public BorrowingManagementView(
            BorrowingService borrowingService,
            ReaderRepository readerRepository,
            BookRepository bookRepository) {
        this.borrowingService = borrowingService;
        this.readerRepository = readerRepository;
        this.bookRepository = bookRepository;
    }

    public Pane getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.getStyleClass().add("catalog-content-pane");

        // =====================================================
        // 1. HEADER SECTION
        // =====================================================
        VBox titleBox = new VBox(4);
        Label title = new Label("Circulation Desk & Loan Registry");
        title.getStyleClass().add("view-main-title");

        Label subtitle = new Label("Issue archival loans, track patron lending records, overdue deadlines, and security deposits.");
        subtitle.getStyleClass().add("view-main-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // =====================================================
        // 2. TOOLBAR
        // =====================================================
        HBox toolbar = new HBox(14);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search patron code, reader name, or loan ID...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(340);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("ALL LOANS", "BORROWING", "OVERDUE", "PARTIALLY_RETURNED", "RETURNED");
        statusFilter.setValue("ALL LOANS");
        statusFilter.getStyleClass().add("filter-combo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = createActionButton("Refresh", FontAwesomeSolid.SYNC_ALT);
        refreshBtn.setOnAction(e -> reloadData());

        Button issueLoanBtn = new Button("ISSUE NEW LOAN");
        issueLoanBtn.getStyleClass().add("btn-primary");
        FontIcon plusIcon = new FontIcon(FontAwesomeSolid.ARROW_CIRCLE_RIGHT);
        plusIcon.setIconSize(12);
        plusIcon.setIconColor(javafx.scene.paint.Color.web("#F8F3EB"));
        issueLoanBtn.setGraphic(plusIcon);
        issueLoanBtn.setOnAction(e -> handleCreateBorrowing());

        toolbar.getChildren().addAll(searchField, statusFilter, spacer, refreshBtn, issueLoanBtn);

        // =====================================================
        // 3. TABLE SETUP
        // =====================================================
        tableView = new TableView<>();
        tableView.getStyleClass().add("vintage-table");
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<Borrowing, String> colId = new TableColumn<>("LOAN ID");
        colId.setPrefWidth(80);
        colId.setCellValueFactory(data -> new SimpleStringProperty("#LOAN-" + data.getValue().getId()));

        TableColumn<Borrowing, String> colPatron = new TableColumn<>("BORROWING PATRON");
        colPatron.setPrefWidth(220);
        colPatron.setCellValueFactory(data -> {
            Reader r = data.getValue().getReader();
            if (r != null) {
                return new SimpleStringProperty(r.getFullName() + " (" + r.getReaderCode() + ")");
            }
            return new SimpleStringProperty("— Unknown Patron —");
        });

        TableColumn<Borrowing, String> colLoanedAt = new TableColumn<>("ISSUED ON");
        colLoanedAt.setPrefWidth(150);
        colLoanedAt.setCellValueFactory(data -> {
            if (data.getValue().getBorrowedAt() != null) {
                return new SimpleStringProperty(data.getValue().getBorrowedAt().format(timeFormatter));
            }
            return new SimpleStringProperty("—");
        });

        TableColumn<Borrowing, String> colDueDate = new TableColumn<>("DUE RETURN DATE");
        colDueDate.setPrefWidth(140);
        colDueDate.setCellValueFactory(data -> {
            if (data.getValue().getDueDate() != null) {
                return new SimpleStringProperty(data.getValue().getDueDate().format(dateFormatter));
            }
            return new SimpleStringProperty("—");
        });

        TableColumn<Borrowing, String> colDeposit = new TableColumn<>("SECURITY DEPOSIT");
        colDeposit.setPrefWidth(140);
        colDeposit.setCellValueFactory(data -> {
            if (data.getValue().getDepositAmount() != null) {
                return new SimpleStringProperty(data.getValue().getDepositAmount().toPlainString() + " VND");
            }
            return new SimpleStringProperty("0 VND");
        });

        TableColumn<Borrowing, String> colRenewals = new TableColumn<>("RENEWALS");
        colRenewals.setPrefWidth(90);
        colRenewals.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRenewalCount() + " / 2"));

        TableColumn<Borrowing, Void> colStatus = new TableColumn<>("STATUS");
        colStatus.setPrefWidth(140);
        colStatus.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Borrowing b = getTableRow().getItem();
                    Label statusLabel = new Label(b.getStatus());
                    String status = b.getStatus();
                    if ("BORROWING".equalsIgnoreCase(status)) {
                        statusLabel.getStyleClass().setAll("badge-status-borrowing");
                    } else if ("OVERDUE".equalsIgnoreCase(status)) {
                        statusLabel.getStyleClass().setAll("badge-status-overdue");
                    } else if ("RETURNED".equalsIgnoreCase(status)) {
                        statusLabel.getStyleClass().setAll("badge-status-active");
                    } else {
                        statusLabel.getStyleClass().setAll("badge-status-inactive");
                    }
                    setGraphic(statusLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Borrowing, Void> colActions = new TableColumn<>("ACTIONS");
        colActions.setPrefWidth(100);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button detailBtn = createTableIconBtn(FontAwesomeSolid.RECEIPT, "Inspect Loan Volumes");

            {
                detailBtn.setOnAction(e -> {
                    Borrowing b = getTableRow().getItem();
                    if (b != null) handleViewDetails(b);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(detailBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        tableView.getColumns().addAll(colId, colPatron, colLoanedAt, colDueDate, colDeposit, colRenewals, colStatus, colActions);

        // =====================================================
        // 4. FILTER & DATA BINDING
        // =====================================================
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
        filteredData.setPredicate(borrowing -> {
            boolean matchesSearch = true;
            if (query != null && !query.isBlank()) {
                String q = query.toLowerCase().trim();
                boolean idMatch = String.valueOf(borrowing.getId()).contains(q);
                boolean patronMatch = borrowing.getReader() != null &&
                        (borrowing.getReader().getFullName().toLowerCase().contains(q) ||
                                borrowing.getReader().getReaderCode().toLowerCase().contains(q));
                matchesSearch = idMatch || patronMatch;
            }

            boolean matchesStatus = true;
            if (status != null && !"ALL LOANS".equals(status)) {
                matchesStatus = status.equalsIgnoreCase(borrowing.getStatus());
            }

            return matchesSearch && matchesStatus;
        });
    }

    public void reloadData() {
        try {
            // Tự động quét và cập nhật trạng thái các khoản mượn quá hạn
            borrowingService.updateOverdueBorrowings();
            masterData.setAll(borrowingService.getAllBorrowings());
        } catch (Exception e) {
            showError("Failed to load loan registry: " + e.getMessage());
        }
    }

    private void handleCreateBorrowing() {
        CreateBorrowingDialog dialog = new CreateBorrowingDialog(
                readerRepository.findAll(),
                bookRepository.findAll()
        );
        Optional<CreateBorrowingRequest> result = dialog.showAndWait();
        result.ifPresent(req -> {
            try {
                borrowingService.borrowBooks(req);
                reloadData();
            } catch (Exception e) {
                showError("Check out transaction failed: " + e.getMessage());
            }
        });
    }

    private void handleViewDetails(Borrowing borrowing) {
        try {
            List<BorrowingDetail> details = borrowingService.getBorrowingDetails(borrowing.getId());
            LoanDetailDialog dialog = new LoanDetailDialog(borrowing, details);
            dialog.showAndWait();
        } catch (Exception e) {
            showError("Failed to fetch loan details: " + e.getMessage());
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
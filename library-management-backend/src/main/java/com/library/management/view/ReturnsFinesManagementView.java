package com.library.management.view;

import com.library.management.dto.RenewBorrowingRequest;
import com.library.management.entity.Borrowing;
import com.library.management.entity.BorrowingDetail;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReturnsFinesManagementView {

    private static final Logger LOGGER =
            Logger.getLogger(ReturnsFinesManagementView.class.getName());

    private final BorrowingService borrowingService;

    private TableView<Borrowing> tableView;
    private ObservableList<Borrowing> masterData;
    private FilteredList<Borrowing> filteredData;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public ReturnsFinesManagementView(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    public Pane getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.getStyleClass().add("catalog-content-pane");

        // =====================================================
        // 1. HEADER SECTION
        // =====================================================
        VBox titleBox = new VBox(4);
        Label title = new Label("Returns, Indemnities & Renewals");
        title.getStyleClass().add("view-main-title");

        Label subtitle = new Label("Process book returns, assess damaged or lost volumes, calculate late penalties, and extend loans.");
        subtitle.getStyleClass().add("view-main-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // =====================================================
        // 2. TOOLBAR
        // =====================================================
        HBox toolbar = new HBox(14);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search patron name, code, or loan ID...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(340);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("ACTIVE LOANS (ALL)", "BORROWING", "OVERDUE", "PARTIALLY_RETURNED", "RETURNED");
        statusFilter.setValue("ACTIVE LOANS (ALL)");
        statusFilter.getStyleClass().add("filter-combo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = createActionButton("Refresh", FontAwesomeSolid.SYNC_ALT);
        refreshBtn.setOnAction(e -> reloadData());

        toolbar.getChildren().addAll(searchField, statusFilter, spacer, refreshBtn);

        // =====================================================
        // 3. TABLE SETUP
        // =====================================================
        tableView = new TableView<>();
        tableView.getStyleClass().add("vintage-table");
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<Borrowing, String> colId = new TableColumn<>("LOAN ID");
        colId.setPrefWidth(90);
        colId.setCellValueFactory(d -> new SimpleStringProperty("#LOAN-" + d.getValue().getId()));

        TableColumn<Borrowing, String> colPatron = new TableColumn<>("PATRON FELLOWSHIP");
        colPatron.setPrefWidth(220);
        colPatron.setCellValueFactory(d -> {
            if (d.getValue().getReader() != null) {
                return new SimpleStringProperty(d.getValue().getReader().getFullName() + " (" + d.getValue().getReader().getReaderCode() + ")");
            }
            return new SimpleStringProperty("— Unknown —");
        });

        TableColumn<Borrowing, String> colDueDate = new TableColumn<>("DEADLINE");
        colDueDate.setPrefWidth(120);
        colDueDate.setCellValueFactory(d -> {
            if (d.getValue().getDueDate() != null) {
                return new SimpleStringProperty(d.getValue().getDueDate().format(dateFormatter));
            }
            return new SimpleStringProperty("—");
        });

        TableColumn<Borrowing, String> colDeposit = new TableColumn<>("HELD DEPOSIT");
        colDeposit.setPrefWidth(130);
        colDeposit.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDepositAmount().toPlainString() + " VND"));

        TableColumn<Borrowing, String> colRenewals = new TableColumn<>("RENEWED");
        colRenewals.setPrefWidth(80);
        colRenewals.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRenewalCount() + " / 2"));

        TableColumn<Borrowing, Void> colStatus = new TableColumn<>("STATUS");
        colStatus.setPrefWidth(130);
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
                    if ("OVERDUE".equalsIgnoreCase(status)) {
                        statusLabel.getStyleClass().setAll("badge-status-overdue");
                    } else if ("RETURNED".equalsIgnoreCase(status)) {
                        statusLabel.getStyleClass().setAll("badge-status-active");
                    } else if ("BORROWING".equalsIgnoreCase(status)) {
                        statusLabel.getStyleClass().setAll("badge-status-borrowing");
                    } else {
                        statusLabel.getStyleClass().setAll("badge-status-inactive");
                    }
                    setGraphic(statusLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Borrowing, Void> colActions = new TableColumn<>("DESK ACTIONS");
        colActions.setPrefWidth(180);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button returnBtn = createTableIconBtn(FontAwesomeSolid.CHECK, "Receive & Process Return");
            private final Button renewBtn = createTableIconBtn(FontAwesomeSolid.CALENDAR_PLUS, "Extend / Renew Loan");

            {
                returnBtn.setOnAction(e -> {
                    Borrowing b = getTableRow().getItem();
                    if (b != null) handleReturnBooks(b);
                });

                renewBtn.setOnAction(e -> {
                    Borrowing b = getTableRow().getItem();
                    if (b != null) handleRenewLoan(b);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Borrowing b = getTableRow().getItem();
                    boolean isClosed = "RETURNED".equalsIgnoreCase(b.getStatus());
                    returnBtn.setDisable(isClosed);
                    renewBtn.setDisable(isClosed || "OVERDUE".equalsIgnoreCase(b.getStatus()) || b.getRenewalCount() >= 2);

                    HBox box = new HBox(8, returnBtn, renewBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        tableView.getColumns().addAll(colId, colPatron, colDueDate, colDeposit, colRenewals, colStatus, colActions);

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
            if ("ACTIVE LOANS (ALL)".equals(status)) {
                matchesStatus = !"RETURNED".equalsIgnoreCase(borrowing.getStatus());
            } else if (status != null) {
                matchesStatus = status.equalsIgnoreCase(borrowing.getStatus());
            }

            return matchesSearch && matchesStatus;
        });
    }

    public void reloadData() {
        try {
            borrowingService.updateOverdueBorrowings();
            masterData.setAll(borrowingService.getAllBorrowings());
        } catch (Exception e) {
            showError("Failed to load return desk entries: " + e.getMessage());
        }
    }

    private void handleReturnBooks(Borrowing borrowing) {
        try {
            List<BorrowingDetail> details =
                    borrowingService.getBorrowingDetails(borrowing.getId());

            ReturnBookDialog dialog =
                    new ReturnBookDialog(
                            borrowing,
                            details,
                            borrowingService
                    );

            dialog.showAndWait();
            reloadData();

        } catch (Exception ex) {

            LOGGER.log(Level.SEVERE, "Unable to start return process", ex);

            Alert alert = new Alert(
                    Alert.AlertType.ERROR,
                    "Failed to initiate return process:\n\n"
                            + "Please try again or contact an administrator.",
                    ButtonType.OK
            );

            alert.setTitle("Circulation Operation Failed");
            alert.setHeaderText("Return process error");

            alert.showAndWait();
        }
    }

    private void handleRenewLoan(Borrowing borrowing) {
        RenewBorrowingDialog dialog = new RenewBorrowingDialog(borrowing);
        Optional<RenewBorrowingRequest> result = dialog.showAndWait();
        result.ifPresent(req -> {
            try {
                borrowingService.renewBorrowing(borrowing.getId(), req);
                reloadData();
            } catch (Exception e) {
                showError("Loan renewal failed: " + e.getMessage());
            }
        });
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

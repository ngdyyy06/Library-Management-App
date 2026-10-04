package com.library.management.view;

import com.library.management.dto.DashboardResponse;
import com.library.management.entity.User;
import com.library.management.repository.*;
import com.library.management.service.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.text.NumberFormat;
import java.util.Locale;

public class DashboardView {

    private final User user;

    // =========================================================
    // REPOSITORIES
    // =========================================================

    private final BookRepository bookRepository =
            new BookRepository();

    private final AuthorRepository authorRepository =
            new AuthorRepository();

    private final PublisherRepository publisherRepository =
            new PublisherRepository();

    private final CategoryRepository categoryRepository =
            new CategoryRepository();

    private final BookShelfRepository bookShelfRepository =
            new BookShelfRepository();

    private final BookShelfAllocationRepository bookShelfAllocationRepository =
            new BookShelfAllocationRepository();

    private final UserRepository userRepository =
            new UserRepository();

    private final RoleRepository roleRepository =
            new RoleRepository();

    private final StaffRepository staffRepository =
            new StaffRepository();

    private final ReaderRepository readerRepository =
            new ReaderRepository();

    private final LibraryCardRepository libraryCardRepository =
            new LibraryCardRepository();

    private final LibraryCardPaymentRepository libraryCardPaymentRepository =
            new LibraryCardPaymentRepository();

    private final BorrowingRepository borrowingRepository =
            new BorrowingRepository();

    private final BorrowingDetailRepository borrowingDetailRepository =
            new BorrowingDetailRepository();

    private final RenewalPaymentRepository renewalPaymentRepository =
            new RenewalPaymentRepository();

    private final ReturnHistoryRepository returnHistoryRepository =
            new ReturnHistoryRepository();

    // =========================================================
    // IMPORT RECEIPT REPOSITORIES
    // =========================================================

    private final ImportReceiptRepository importReceiptRepository =
            new ImportReceiptRepository();

    private final ImportReceiptDetailRepository importReceiptDetailRepository =
            new ImportReceiptDetailRepository();

    // =========================================================
    // SERVICES
    // =========================================================

    private final PasswordService passwordService =
            new PasswordService();

    private final BookShelfService bookShelfService =
            new BookShelfService(
                    bookShelfRepository,
                    bookShelfAllocationRepository,
                    categoryRepository
            );

    private final BookService bookService =
            new BookService(
                    bookRepository,
                    authorRepository,
                    publisherRepository,
                    categoryRepository,
                    bookShelfService
            );

    private final AuthorService authorService =
            new AuthorService(
                    authorRepository,
                    bookRepository
            );

    private final PublisherService publisherService =
            new PublisherService(
                    publisherRepository
            );

    private final UserService userService =
            new UserService(
                    userRepository,
                    roleRepository,
                    staffRepository,
                    passwordService
            );

    private final ReaderService readerService =
            new ReaderService(
                    readerRepository,
                    libraryCardRepository,
                    libraryCardPaymentRepository
            );

    private final BorrowingService borrowingService =
            new BorrowingService(
                    borrowingRepository,
                    borrowingDetailRepository,
                    readerRepository,
                    bookRepository,
                    renewalPaymentRepository,
                    returnHistoryRepository
            );

    // =========================================================
    // DASHBOARD SERVICE
    // =========================================================

    private final DashboardService dashboardService =
            new DashboardService(
                    bookRepository,
                    readerRepository,
                    authorRepository,
                    publisherRepository,
                    categoryRepository,
                    borrowingRepository,
                    importReceiptRepository,
                    userRepository,
                    borrowingDetailRepository,
                    renewalPaymentRepository,
                    libraryCardPaymentRepository
            );

    // =========================================================
    // IMPORT RECEIPT SERVICE
    // =========================================================

    private final ImportReceiptService importReceiptService =
            new ImportReceiptService(
                    importReceiptRepository,
                    importReceiptDetailRepository,
                    bookRepository,
                    publisherRepository,
                    userRepository,
                    bookShelfService,
                    bookService
            );

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public DashboardView(User user) {
        this.user = user;
    }

    // =========================================================
    // SHOW DASHBOARD
    // =========================================================

    public void show(Stage stage) {

        // =====================================================
        // CHECK ROLE
        // =====================================================

        boolean isAdmin =
                user.getRole() != null
                        && "ADMIN".equalsIgnoreCase(
                        user.getRole().getName()
                );

        // =====================================================
        // ROOT
        // =====================================================

        BorderPane root = new BorderPane();

        root.getStyleClass().add(
                "dashboard-root"
        );

        // =====================================================
        // HEADER
        // =====================================================

        HBox header = new HBox(15);

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        header.getStyleClass().add(
                "dashboard-header"
        );

        Label logo =
                new Label("ATELIER");

        logo.getStyleClass().add(
                "dashboard-logo-text"
        );

        Separator sep =
                new Separator();

        sep.setOrientation(
                javafx.geometry.Orientation.VERTICAL
        );

        sep.setPrefHeight(20);

        Label title =
                new Label(
                        "LIBRARY & ARCHIVAL REPOSITORY"
                );

        title.getStyleClass().add(
                "dashboard-title"
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        VBox userBox =
                new VBox(2);

        userBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        userBox.setCursor(
                javafx.scene.Cursor.HAND
        );

        Label fullName =
                new Label(
                        user.getFullName() != null
                                ? user.getFullName()
                                : user.getUsername()
                );

        fullName.getStyleClass().add(
                "dashboard-user"
        );

        Label role =
                new Label(
                        user.getRole() != null
                                ? user.getRole().getName()
                                : "CURATOR"
                );

        role.getStyleClass().add(
                "dashboard-role"
        );

        userBox.getChildren().addAll(
                fullName,
                role
        );

        header.getChildren().addAll(
                logo,
                sep,
                title,
                spacer,
                userBox
        );

        // Header không cuộn
        root.setTop(header);

        // =====================================================
        // SIDEBAR
        // =====================================================

        VBox sidebar =
                new VBox(4);

        sidebar.setPadding(
                new Insets(
                        18,
                        12,
                        18,
                        12
                )
        );

        sidebar.setPrefWidth(225);

        sidebar.getStyleClass().add(
                "dashboard-sidebar"
        );

        // =====================================================
        // GENERAL
        // =====================================================

        sidebar.getChildren().add(
                createSectionHeader("GENERAL")
        );

        Button dashboardButton =
                createMenuButton(
                        "Dashboard",
                        FontAwesomeSolid.COMPASS
                );

        sidebar.getChildren().add(
                dashboardButton
        );

        // =====================================================
        // CIRCULATION
        // =====================================================

        sidebar.getChildren().add(
                createSectionHeader("CIRCULATION")
        );

        Button borrowingButton =
                createMenuButton(
                        "Check Out",
                        FontAwesomeSolid.ARROW_CIRCLE_RIGHT
                );

        Button returningButton =
                createMenuButton(
                        "Returns & Fines",
                        FontAwesomeSolid.CHECK_CIRCLE
                );

        Button readersButton =
                createMenuButton(
                        "Patrons",
                        FontAwesomeSolid.ADDRESS_CARD
                );

        sidebar.getChildren().addAll(
                borrowingButton,
                returningButton,
                readersButton
        );

        // =====================================================
        // CATALOG & ARCHIVE
        // =====================================================

        sidebar.getChildren().add(
                createSectionHeader(
                        "CATALOG & ARCHIVE"
                )
        );

        Button booksButton =
                createMenuButton(
                        "Book Collection",
                        FontAwesomeSolid.BOOK
                );

        Button categoriesButton =
                createMenuButton(
                        "Genres & Topics",
                        FontAwesomeSolid.BOOKMARK
                );

        Button authorsButton =
                createMenuButton(
                        "Authors",
                        FontAwesomeSolid.FEATHER_ALT
                );

        Button publishersButton =
                createMenuButton(
                        "Publishers",
                        FontAwesomeSolid.LANDMARK
                );

        Button shelvesButton =
                createMenuButton(
                        "Shelf Locations",
                        FontAwesomeSolid.LAYER_GROUP
                );

        sidebar.getChildren().addAll(
                booksButton,
                categoriesButton,
                authorsButton,
                publishersButton,
                shelvesButton
        );

        // =====================================================
        // ADMIN ONLY - IMPORT RECEIPTS
        // =====================================================

        Button importReceiptButton = null;

        if (isAdmin) {

            importReceiptButton =
                    createMenuButton(
                            "Import Receipts",
                            FontAwesomeSolid.FILE_IMPORT
                    );

            sidebar.getChildren().add(
                    importReceiptButton
            );
        }

        // =====================================================
        // SYSTEM & FOOTER
        // =====================================================

        Region bottomSpacer =
                new Region();

        VBox.setVgrow(
                bottomSpacer,
                Priority.ALWAYS
        );

        Button usersButton =
                createMenuButton(
                        "Staff Accounts",
                        FontAwesomeSolid.USER_SHIELD
                );

        Button logoutButton =
                createMenuButton(
                        "Sign Out",
                        FontAwesomeSolid.SIGN_OUT_ALT
                );

        logoutButton.getStyleClass().add(
                "menu-logout-button"
        );

        sidebar.getChildren().addAll(
                bottomSpacer,
                usersButton,
                logoutButton
        );

        // Sidebar không cuộn
        root.setLeft(sidebar);

        // =====================================================
        // INITIAL DASHBOARD
        // =====================================================

        root.setCenter(
                createDashboardScrollPane(isAdmin)
        );

        // =====================================================
        // INITIALIZE ALL SUB-VIEWS
        // =====================================================

        BookManagementView bookManagementView =
                new BookManagementView(
                        bookService,
                        categoryRepository,
                        publisherRepository
                );

        CategoryManagementView categoryManagementView =
                new CategoryManagementView(
                        categoryRepository,
                        bookShelfRepository
                );

        AuthorManagementView authorManagementView =
                new AuthorManagementView(
                        authorService
                );

        PublisherManagementView publisherManagementView =
                new PublisherManagementView(
                        publisherService
                );

        BookShelfManagementView bookShelfManagementView =
                new BookShelfManagementView(
                        bookShelfService,
                        categoryRepository
                );

        ReaderManagementView readerManagementView =
                new ReaderManagementView(
                        readerService,
                        libraryCardRepository,
                        borrowingRepository,
                        borrowingDetailRepository
                );

        BorrowingManagementView borrowingManagementView =
                new BorrowingManagementView(
                        borrowingService,
                        readerRepository,
                        bookRepository
                );

        ReturnsFinesManagementView returnsFinesManagementView =
                new ReturnsFinesManagementView(
                        borrowingService
                );

        UserManagementView userManagementView =
                new UserManagementView(
                        userService,
                        roleRepository
                );

        StaffProfileView staffProfileView =
                new StaffProfileView(
                        user,
                        userService
                );

        // =====================================================
        // IMPORT RECEIPT VIEW
        // ADMIN ONLY
        // =====================================================

        ImportReceiptManagementView importReceiptManagementView =
                null;

        if (isAdmin) {

            importReceiptManagementView =
                    new ImportReceiptManagementView(
                            importReceiptService,
                            authorService,
                            categoryRepository,
                            authorRepository,
                            bookRepository,
                            publisherRepository,
                            user.getId()
                    );
        }

        // =====================================================
        // NAVIGATION EVENTS
        // =====================================================

        dashboardButton.setOnAction(e -> {

            // Dashboard luôn có ScrollPane
            root.setCenter(
                    createDashboardScrollPane(isAdmin)
            );
        });

        booksButton.setOnAction(e -> {

            root.setCenter(
                    bookManagementView.getView()
            );
        });

        categoriesButton.setOnAction(e -> {

            root.setCenter(
                    categoryManagementView.getView()
            );
        });

        authorsButton.setOnAction(e -> {

            root.setCenter(
                    authorManagementView.getView()
            );
        });

        publishersButton.setOnAction(e -> {

            root.setCenter(
                    publisherManagementView.getView()
            );
        });

        shelvesButton.setOnAction(e -> {

            root.setCenter(
                    bookShelfManagementView.getView()
            );
        });

        readersButton.setOnAction(e -> {

            root.setCenter(
                    readerManagementView.getView()
            );
        });

        borrowingButton.setOnAction(e -> {

            root.setCenter(
                    borrowingManagementView.getView()
            );
        });

        returningButton.setOnAction(e -> {

            root.setCenter(
                    returnsFinesManagementView.getView()
            );
        });

        usersButton.setOnAction(e -> {

            if (
                    "ADMIN".equalsIgnoreCase(
                            user.getRole() != null
                                    ? user.getRole().getName()
                                    : ""
                    )
            ) {

                root.setCenter(
                        userManagementView.getView()
                );

            } else {

                root.setCenter(
                        staffProfileView.getView()
                );
            }
        });

        if (
                isAdmin
                        && importReceiptButton != null
        ) {

            ImportReceiptManagementView finalImportReceiptManagementView =
                    importReceiptManagementView;

            importReceiptButton.setOnAction(e -> {

                root.setCenter(
                        finalImportReceiptManagementView.getView()
                );
            });
        }

        userBox.setOnMouseClicked(e -> {

            root.setCenter(
                    staffProfileView.getView()
            );
        });

        // =====================================================
        // LOGOUT
        // =====================================================

        logoutButton.setOnAction(event -> {

            // Ẩn cửa sổ Dashboard trước khi thay Scene
            stage.hide();

            LoginView loginView =
                    new LoginView(
                            new AuthService(
                                    new UserRepository(),
                                    new PasswordService()
                            )
                    );

            loginView.show(stage);

            Platform.runLater(() -> {
                stage.setMaximized(true);
                stage.toFront();
            });
        });

        // =====================================================
        // SCENE SETUP
        // =====================================================

        Scene scene = new Scene(root);

        scene.getStylesheets().add(
                getClass()
                        .getResource(
                                "/css/dashboard.css"
                        )
                        .toExternalForm()
        );

        stage.setTitle(
                "Atelier • Library Management System"
        );

        stage.setMinWidth(1000);
        stage.setMinHeight(650);

        stage.setScene(scene);

        stage.show();

        maximizeStage(stage);
    }

    private void maximizeStage(Stage stage) {

        Platform.runLater(() -> {

            stage.setMaximized(false);

            Platform.runLater(() -> {
                stage.setMaximized(true);
            });

        });
    }

    // =========================================================
    // CREATE DASHBOARD SCROLL PANE
    // =========================================================

    private ScrollPane createDashboardScrollPane(
            boolean isAdmin
    ) {

        VBox content =
                createDashboardContent(isAdmin);

        ScrollPane scrollPane =
                new ScrollPane();

        scrollPane.setContent(
                content
        );

        // Chiều rộng content theo vùng hiển thị
        scrollPane.setFitToWidth(true);

        // Chỉ cho phép cuộn dọc
        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        // Cho phép kéo nội dung bằng chuột
        scrollPane.setPannable(true);

        scrollPane.getStyleClass().add(
                "dashboard-scroll"
        );

        return scrollPane;
    }

    // =========================================================
    // CREATE DASHBOARD CONTENT
    // =========================================================

    private VBox createDashboardContent(
            boolean isAdmin
    ) {

        VBox content =
                new VBox(24);

        content.setPadding(
                new Insets(
                        35,
                        45,
                        35,
                        45
                )
        );

        content.getStyleClass().add(
                "dashboard-content"
        );

        // =====================================================
        // TITLE
        // =====================================================

        VBox titleArea =
                new VBox(6);

        Label welcomeTitle =
                new Label(
                        "Library Overview"
                );

        welcomeTitle.getStyleClass().add(
                "welcome-title"
        );

        Label welcomeSubtitle =
                new Label(
                        "Archive repository, catalog management, and circulation records."
                );

        welcomeSubtitle.getStyleClass().add(
                "welcome-subtitle"
        );

        titleArea.getChildren().addAll(
                welcomeTitle,
                welcomeSubtitle
        );

        // =====================================================
        // DATABASE METRICS
        // =====================================================

        long totalBooksCount =
                bookRepository.count();

        long totalPatronsCount =
                readerRepository.count();

        long activeLoansCount =
                borrowingRepository
                        .findAll()
                        .stream()
                        .filter(
                                b -> !"RETURNED"
                                        .equalsIgnoreCase(
                                                b.getStatus()
                                        )
                        )
                        .count();

        long overdueCount =
                borrowingRepository
                        .findAll()
                        .stream()
                        .filter(
                                b -> "OVERDUE"
                                        .equalsIgnoreCase(
                                                b.getStatus()
                                        )
                        )
                        .count();

        HBox statsRow =
                new HBox(20);

        statsRow.getChildren().addAll(

                createStatCard(
                        String.format(
                                "%,d",
                                totalBooksCount
                        ),
                        "Total Volumes"
                ),

                createStatCard(
                        String.format(
                                "%,d",
                                activeLoansCount
                        ),
                        "Active Loans"
                ),

                createStatCard(
                        String.format(
                                "%,d",
                                totalPatronsCount
                        ),
                        "Registered Patrons"
                ),

                createStatCard(
                        String.format(
                                "%,d",
                                overdueCount
                        ),
                        "Overdue Borrowings"
                )
        );

        content.getChildren().addAll(
                titleArea,
                statsRow
        );

        // =====================================================
        // ADMIN REVENUE
        // =====================================================

        if (isAdmin) {

            DashboardResponse dashboard =
                    dashboardService.getDashboard();

            content.getChildren().add(
                    createRevenueSection(
                            dashboard
                    )
            );
        }

        return content;
    }

    // =========================================================
    // REVENUE SECTION
    // =========================================================

    // =========================================================
// REVENUE SECTION
// =========================================================

    private VBox createRevenueSection(
            DashboardResponse dashboard
    ) {

        VBox section =
                new VBox(16);

        section.getStyleClass().add(
                "revenue-section"
        );

        // =====================================================
        // HEADER
        // =====================================================

        VBox header =
                new VBox(4);

        Label title =
                new Label(
                        "Revenue Overview"
                );

        title.getStyleClass().add(
                "revenue-section-title"
        );

        Label subtitle =
                new Label(
                        "Income generated from fines, reader creation fees, and renewals."
                );

        subtitle.getStyleClass().add(
                "revenue-section-subtitle"
        );

        header.getChildren().addAll(
                title,
                subtitle
        );

        // =====================================================
        // TODAY
        // =====================================================

        Label todayTitle =
                new Label(
                        "TODAY"
                );

        todayTitle.getStyleClass().add(
                "revenue-period-title"
        );

        HBox todayCards =
                new HBox(16);

        todayCards.getChildren().addAll(

                createRevenueCard(
                        "Total Revenue",
                        dashboard.getTodayRevenue(),
                        "revenue-card-total",
                        FontAwesomeSolid.MONEY_BILL_WAVE
                ),

                createRevenueCard(
                        "Fines",
                        dashboard.getTodayFineRevenue(),
                        "revenue-card-fine",
                        FontAwesomeSolid.EXCLAMATION_CIRCLE
                ),

                createRevenueCard(
                        "Reader Fees",
                        dashboard.getTodayLibraryCardRevenue(),
                        "revenue-card-reader",
                        FontAwesomeSolid.ID_CARD
                ),

                createRevenueCard(
                        "Renewal Fees",
                        dashboard.getTodayRenewalRevenue(),
                        "revenue-card-renewal",
                        FontAwesomeSolid.SYNC_ALT
                )
        );

        // =====================================================
        // THIS MONTH
        // =====================================================

        Label monthTitle =
                new Label(
                        "THIS MONTH"
                );

        monthTitle.getStyleClass().add(
                "revenue-period-title"
        );

        HBox monthCards =
                new HBox(16);

        monthCards.getChildren().addAll(

                createRevenueCard(
                        "Total Revenue",
                        dashboard.getMonthlyRevenue(),
                        "revenue-card-total",
                        FontAwesomeSolid.MONEY_BILL_WAVE
                ),

                createRevenueCard(
                        "Fines",
                        dashboard.getMonthlyFineRevenue(),
                        "revenue-card-fine",
                        FontAwesomeSolid.EXCLAMATION_CIRCLE
                ),

                createRevenueCard(
                        "Reader Fees",
                        dashboard.getMonthlyLibraryCardRevenue(),
                        "revenue-card-reader",
                        FontAwesomeSolid.ID_CARD
                ),

                createRevenueCard(
                        "Renewal Fees",
                        dashboard.getMonthlyRenewalRevenue(),
                        "revenue-card-renewal",
                        FontAwesomeSolid.SYNC_ALT
                )
        );

        // =====================================================
        // ADD TO SECTION
        // =====================================================

        section.getChildren().addAll(

                header,

                todayTitle,
                todayCards,

                monthTitle,
                monthCards
        );

        return section;
    }

    // =========================================================
    // REVENUE CARD
    // =========================================================

    private VBox createRevenueCard(
            String title,
            long amount,
            String styleClass,
            FontAwesomeSolid iconType
    ) {

        VBox card =
                new VBox(8);

        card.setPrefWidth(210);

        card.setMinHeight(105);

        card.getStyleClass().addAll(
                "revenue-card",
                styleClass
        );

        HBox top =
                new HBox(8);

        top.setAlignment(
                Pos.CENTER_LEFT
        );

        FontIcon icon =
                new FontIcon(
                        iconType
                );

        icon.setIconSize(14);

        icon.setIconColor(
                javafx.scene.paint.Color.web(
                        "#C5A059"
                )
        );

        Label titleLabel =
                new Label(
                        title
                );

        titleLabel.getStyleClass().add(
                "revenue-card-label"
        );

        top.getChildren().addAll(
                icon,
                titleLabel
        );

        Label amountLabel =
                new Label(
                        formatMoney(amount)
                );

        amountLabel.getStyleClass().add(
                "revenue-card-value"
        );

        card.getChildren().addAll(
                top,
                amountLabel
        );

        return card;
    }

    // =========================================================
    // REVENUE ROW
    // =========================================================

    private HBox createRevenueRow(
            String labelText,
            long amount
    ) {

        HBox row =
                new HBox();

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label label =
                new Label(
                        labelText
                );

        label.getStyleClass().add(
                "revenue-row-label"
        );

        Label value =
                new Label(
                        formatMoney(amount)
                );

        value.getStyleClass().add(
                "revenue-row-value"
        );

        row.getChildren().addAll(
                label,
                spacer,
                value
        );

        return row;
    }

    // =========================================================
    // REVENUE TOTAL ROW
    // =========================================================

    private HBox createRevenueTotalRow(
            String labelText,
            long amount
    ) {

        HBox row =
                new HBox();

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label label =
                new Label(
                        labelText
                );

        label.getStyleClass().add(
                "revenue-total-label"
        );

        Label value =
                new Label(
                        formatMoney(amount)
                );

        value.getStyleClass().add(
                "revenue-total-value"
        );

        row.getChildren().addAll(
                label,
                spacer,
                value
        );

        return row;
    }

    // =========================================================
    // FORMAT MONEY
    // =========================================================

    private String formatMoney(
            long amount
    ) {

        NumberFormat formatter =
                NumberFormat.getInstance(
                        new Locale(
                                "vi",
                                "VN"
                        )
                );

        return formatter.format(amount)
                + " ₫";
    }

    // =========================================================
    // HELPER - SECTION HEADER
    // =========================================================

    private Label createSectionHeader(
            String title
    ) {

        Label label =
                new Label(
                        title
                );

        label.getStyleClass().add(
                "sidebar-section-title"
        );

        return label;
    }

    // =========================================================
    // HELPER - MENU BUTTON
    // =========================================================

    private Button createMenuButton(
            String text,
            FontAwesomeSolid iconType
    ) {

        FontIcon icon =
                new FontIcon(
                        iconType
                );

        icon.setIconSize(13);

        icon.setIconColor(
                javafx.scene.paint.Color.web(
                        "#A88F78"
                )
        );

        Button button =
                new Button(
                        text
                );

        button.setGraphic(
                icon
        );

        button.setGraphicTextGap(
                12
        );

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.setAlignment(
                Pos.CENTER_LEFT
        );

        button.getStyleClass().add(
                "menu-button"
        );

        return button;
    }

    // =========================================================
    // HELPER - STAT CARD
    // =========================================================

    private VBox createStatCard(
            String value,
            String labelText
    ) {

        VBox card =
                new VBox(4);

        card.setPrefWidth(
                210
        );

        card.getStyleClass().add(
                "stat-card"
        );

        Label numLabel =
                new Label(
                        value
                );

        numLabel.getStyleClass().add(
                "stat-number"
        );

        Label descLabel =
                new Label(
                        labelText
                );

        descLabel.getStyleClass().add(
                "stat-label"
        );

        card.getChildren().addAll(
                numLabel,
                descLabel
        );

        return card;
    }
}
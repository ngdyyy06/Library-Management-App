package com.library.management.view;

import com.library.management.entity.User;
import com.library.management.repository.*;
import com.library.management.service.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

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
        // ROOT
        // =====================================================

        BorderPane root = new BorderPane();
        root.getStyleClass().add("dashboard-root");

        // =====================================================
        // HEADER
        // =====================================================

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("dashboard-header");

        Label logo = new Label("ATELIER");
        logo.getStyleClass().add("dashboard-logo-text");

        Separator sep = new Separator();
        sep.setOrientation(javafx.geometry.Orientation.VERTICAL);
        sep.setPrefHeight(20);

        Label title = new Label("LIBRARY & ARCHIVAL REPOSITORY");
        title.getStyleClass().add("dashboard-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox userBox = new VBox(2);
        userBox.setAlignment(Pos.CENTER_RIGHT);
        userBox.setCursor(javafx.scene.Cursor.HAND);

        Label fullName = new Label(
                user.getFullName() != null ? user.getFullName() : user.getUsername()
        );
        fullName.getStyleClass().add("dashboard-user");

        Label role = new Label(
                user.getRole() != null ? user.getRole().getName() : "CURATOR"
        );
        role.getStyleClass().add("dashboard-role");

        userBox.getChildren().addAll(fullName, role);

        header.getChildren().addAll(logo, sep, title, spacer, userBox);
        root.setTop(header);

        // =====================================================
        // SIDEBAR
        // =====================================================

        VBox sidebar = new VBox(4);
        sidebar.setPadding(new Insets(18, 12, 18, 12));
        sidebar.setPrefWidth(225);
        sidebar.getStyleClass().add("dashboard-sidebar");

        // ── GENERAL ──
        sidebar.getChildren().add(createSectionHeader("GENERAL"));
        Button dashboardButton = createMenuButton("Dashboard", FontAwesomeSolid.COMPASS);

        // ── CIRCULATION ──
        sidebar.getChildren().addAll(
                dashboardButton,
                createSectionHeader("CIRCULATION")
        );
        Button borrowingButton = createMenuButton("Check Out", FontAwesomeSolid.ARROW_CIRCLE_RIGHT);
        Button returningButton = createMenuButton("Returns & Fines", FontAwesomeSolid.CHECK_CIRCLE);
        Button readersButton   = createMenuButton("Patrons", FontAwesomeSolid.ADDRESS_CARD);

        // ── CATALOG & ARCHIVE ──
        sidebar.getChildren().addAll(
                borrowingButton,
                returningButton,
                readersButton,
                createSectionHeader("CATALOG & ARCHIVE")
        );
        Button booksButton      = createMenuButton("Book Collection", FontAwesomeSolid.BOOK);
        Button categoriesButton = createMenuButton("Genres & Topics", FontAwesomeSolid.BOOKMARK);
        Button authorsButton    = createMenuButton("Authors", FontAwesomeSolid.FEATHER_ALT);
        Button publishersButton = createMenuButton("Publishers", FontAwesomeSolid.LANDMARK);
        Button shelvesButton    = createMenuButton("Shelf Locations", FontAwesomeSolid.LAYER_GROUP);

        // ── SYSTEM & FOOTER ──
        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        Button usersButton = createMenuButton("Staff Accounts", FontAwesomeSolid.USER_SHIELD);
        Button logoutButton = createMenuButton("Sign Out", FontAwesomeSolid.SIGN_OUT_ALT);
        logoutButton.getStyleClass().add("menu-logout-button");

        sidebar.getChildren().addAll(
                booksButton,
                categoriesButton,
                authorsButton,
                publishersButton,
                shelvesButton,
                bottomSpacer,
                usersButton,
                logoutButton
        );

        root.setLeft(sidebar);

        // =====================================================
        // DEFAULT DASHBOARD CONTENT (Dynamic Metrics)
        // =====================================================

        VBox defaultContent = new VBox(24);
        defaultContent.setPadding(new Insets(35, 45, 35, 45));
        defaultContent.getStyleClass().add("dashboard-content");

        VBox titleArea = new VBox(6);
        Label welcomeTitle = new Label("Library Overview");
        welcomeTitle.getStyleClass().add("welcome-title");

        Label welcomeSubtitle = new Label("Archive repository, catalog management, and circulation records.");
        welcomeSubtitle.getStyleClass().add("welcome-subtitle");
        titleArea.getChildren().addAll(welcomeTitle, welcomeSubtitle);

        // Lấy số liệu thực tế từ Database
        long totalBooksCount = bookRepository.count();
        long totalPatronsCount = readerRepository.count();
        long activeLoansCount = borrowingRepository.findAll().stream()
                .filter(b -> !"RETURNED".equalsIgnoreCase(b.getStatus()))
                .count();
        long overdueCount = borrowingRepository.findAll().stream()
                .filter(b -> "OVERDUE".equalsIgnoreCase(b.getStatus()))
                .count();

        HBox statsRow = new HBox(20);
        statsRow.getChildren().addAll(
                createStatCard(String.format("%,d", totalBooksCount), "Total Volumes"),
                createStatCard(String.format("%,d", activeLoansCount), "Active Loans"),
                createStatCard(String.format("%,d", totalPatronsCount), "Registered Patrons"),
                createStatCard(String.format("%,d", overdueCount), "Overdue Borrowings")
        );

        defaultContent.getChildren().addAll(titleArea, statsRow);
        root.setCenter(defaultContent);

        // =====================================================
        // INITIALIZE ALL SUB-VIEWS
        // =====================================================

        BookManagementView bookManagementView =
                new BookManagementView(bookService, categoryRepository, publisherRepository);

        CategoryManagementView categoryManagementView =
                new CategoryManagementView(categoryRepository, bookShelfRepository);

        AuthorManagementView authorManagementView =
                new AuthorManagementView(authorService);

        PublisherManagementView publisherManagementView =
                new PublisherManagementView(publisherService);

        BookShelfManagementView bookShelfManagementView =
                new BookShelfManagementView(bookShelfService, categoryRepository);

        ReaderManagementView readerManagementView =
                new ReaderManagementView(
                        readerService,
                        libraryCardRepository,
                        borrowingRepository,
                        borrowingDetailRepository
                );

        BorrowingManagementView borrowingManagementView =
                new BorrowingManagementView(borrowingService, readerRepository, bookRepository);

        ReturnsFinesManagementView returnsFinesManagementView =
                new ReturnsFinesManagementView(borrowingService);

        UserManagementView userManagementView =
                new UserManagementView(userService, roleRepository);

        StaffProfileView staffProfileView =
                new StaffProfileView(user, userService);

        // =====================================================
        // ATTACH NAVIGATION EVENTS
        // =====================================================

        // 1. Dashboard
        dashboardButton.setOnAction(e -> {
            root.setCenter(defaultContent);
        });

        // 2. Book Collection
        booksButton.setOnAction(e -> {
            root.setCenter(bookManagementView.getView());
        });

        // 3. Genres & Topics
        categoriesButton.setOnAction(e -> {
            root.setCenter(categoryManagementView.getView());
        });

        // 4. Authors
        authorsButton.setOnAction(e -> {
            root.setCenter(authorManagementView.getView());
        });

        // 5. Publishers
        publishersButton.setOnAction(e -> {
            root.setCenter(publisherManagementView.getView());
        });

        // 6. Shelf Locations
        shelvesButton.setOnAction(e -> {
            root.setCenter(bookShelfManagementView.getView());
        });

        // 7. Patrons
        readersButton.setOnAction(e -> {
            root.setCenter(readerManagementView.getView());
        });

        // 8. Check Out
        borrowingButton.setOnAction(e -> {
            root.setCenter(borrowingManagementView.getView());
        });

        // 9. Returns & Fines
        returningButton.setOnAction(e -> {
            root.setCenter(returnsFinesManagementView.getView());
        });

        // 10. Staff Accounts (Admin xem danh sách, Librarian sửa hồ sơ cá nhân)
        usersButton.setOnAction(e -> {
            if ("ADMIN".equalsIgnoreCase(user.getRole() != null ? user.getRole().getName() : "")) {
                root.setCenter(userManagementView.getView());
            } else {
                root.setCenter(staffProfileView.getView());
            }
        });

        // 11. Click trực tiếp vào tên thủ thư ở góc Header để mở hồ sơ cá nhân
        userBox.setOnMouseClicked(e -> {
            root.setCenter(staffProfileView.getView());
        });

        // 12. Logout
        logoutButton.setOnAction(event -> {
            LoginView loginView = new LoginView(
                    new AuthService(
                            new UserRepository(),
                            new PasswordService()
                    )
            );
            loginView.show(stage);
        });

        // =====================================================
        // SCENE SETUP
        // =====================================================

        Scene scene = new Scene(root, 1200, 750);
        scene.getStylesheets().add(
                getClass().getResource("/css/dashboard.css").toExternalForm()
        );

        stage.setTitle("Atelier • Library Management System");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.show();
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private Label createSectionHeader(String title) {
        Label label = new Label(title);
        label.getStyleClass().add("sidebar-section-title");
        return label;
    }

    private Button createMenuButton(String text, FontAwesomeSolid iconType) {
        FontIcon icon = new FontIcon(iconType);
        icon.setIconSize(13);
        icon.setIconColor(javafx.scene.paint.Color.web("#A88F78"));

        Button button = new Button(text);
        button.setGraphic(icon);
        button.setGraphicTextGap(12);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.getStyleClass().add("menu-button");

        return button;
    }

    private VBox createStatCard(String value, String labelText) {
        VBox card = new VBox(4);
        card.setPrefWidth(210);
        card.getStyleClass().add("stat-card");

        Label numLabel = new Label(value);
        numLabel.getStyleClass().add("stat-number");

        Label descLabel = new Label(labelText);
        descLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(numLabel, descLabel);
        return card;
    }
}
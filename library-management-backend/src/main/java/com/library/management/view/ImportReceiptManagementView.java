package com.library.management.view;

import com.library.management.dto.CreateImportReceiptRequest;
import com.library.management.dto.ImportReceiptDetailRequest;
import com.library.management.dto.NewImportBookRequest;
import com.library.management.entity.Author;
import com.library.management.entity.Book;
import com.library.management.entity.Category;
import com.library.management.entity.Publisher;
import com.library.management.repository.AuthorRepository;
import com.library.management.repository.BookRepository;
import com.library.management.repository.CategoryRepository;
import com.library.management.repository.PublisherRepository;
import com.library.management.service.AuthorService;
import com.library.management.service.ImportReceiptService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;

public class ImportReceiptManagementView {

    private final BorderPane root = new BorderPane();

    private final ImportReceiptService importReceiptService;

    private final AuthorService authorService;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;

    private final Long currentUserId;

    // =========================================================
    // FORM
    // =========================================================

    private ComboBox<Publisher> publisherCombo;
    private DatePicker importDatePicker;

    private ToggleGroup bookTypeGroup;
    private RadioButton existingBookRadio;
    private RadioButton newBookRadio;

    private ComboBox<Book> existingBookCombo;
    private TextField existingBookPriceField;
    private TextField existingQuantityField;

    private TextField newTitleField;
    private TextField newIsbnField;
    private TextField newPublishYearField;
    private TextArea newDescriptionArea;
    private TextField newPriceField;
    private TextField newQuantityField;

    private ListView<Author> authorListView;
    private ListView<Category> categoryListView;
    private ComboBox<Category> primaryCategoryCombo;

    private VBox existingBookPane;
    private VBox newBookPane;

    // =========================================================
    // RECEIPT TABLE
    // =========================================================

    private final ObservableList<ImportItem> importItems =
            FXCollections.observableArrayList();

    private TableView<ImportItem> importTable;

    private Label totalAmountLabel;

    // =========================================================
    // CHECKBOX STATE
    // =========================================================

    private final Map<Long, SimpleBooleanProperty> authorSelection =
            new HashMap<>();

    private final Map<Long, SimpleBooleanProperty> categorySelection =
            new HashMap<>();

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ImportReceiptManagementView(
            ImportReceiptService importReceiptService,
            AuthorService authorService,
            CategoryRepository categoryRepository,
            AuthorRepository authorRepository,
            BookRepository bookRepository,
            PublisherRepository publisherRepository,
            Long currentUserId) {

        this.importReceiptService = importReceiptService;
        this.authorService = authorService;
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.publisherRepository = publisherRepository;
        this.currentUserId = currentUserId;

        buildView();
    }

    // =========================================================
    // PUBLIC
    // =========================================================

    public BorderPane getView() {
        return root;
    }

    // =========================================================
    // BUILD VIEW
    // =========================================================

    private void buildView() {

        root.getStyleClass().add("catalog-content-pane");

        VBox content = new VBox(22);

        content.setPadding(
                new Insets(28, 32, 32, 32)
        );

        content.getChildren().addAll(
                createHeader(),
                createMainForm(),
                createReceiptSection()
        );

        ScrollPane scrollPane =
                new ScrollPane(content);

        scrollPane.setFitToWidth(true);

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.getStyleClass()
                .add("catalog-scroll");

        root.setCenter(scrollPane);

        loadInitialData();
    }

    // =========================================================
    // HEADER
    // =========================================================

    private Node createHeader() {

        VBox box = new VBox(5);

        Label title =
                new Label("Import Receipts");

        title.getStyleClass()
                .add("view-main-title");

        Label subtitle =
                new Label(
                        "Create a new book import receipt and update library inventory"
                );

        subtitle.getStyleClass()
                .add("view-main-subtitle");

        box.getChildren().addAll(
                title,
                subtitle
        );

        return box;
    }

    // =========================================================
    // MAIN FORM
    // =========================================================

    private Node createMainForm() {

        VBox card = createCard();

        Label sectionTitle =
                new Label("IMPORT RECEIPT");

        sectionTitle.getStyleClass()
                .add("section-title");

        // -----------------------------------------------------
        // PUBLISHER
        // -----------------------------------------------------

        VBox publisherBox =
                new VBox(7);

        Label publisherLabel =
                new Label("Publisher");

        publisherLabel.getStyleClass()
                .add("field-label");

        publisherCombo =
                new ComboBox<>();

        publisherCombo.setPromptText(
                "Select publisher"
        );

        publisherCombo.getStyleClass()
                .add("filter-combo");

        publisherCombo.setMaxWidth(
                Double.MAX_VALUE
        );

        publisherCombo.setConverter(
                new StringConverter<Publisher>() {

                    @Override
                    public String toString(
                            Publisher publisher) {

                        return publisher == null
                                ? ""
                                : publisher.getName();
                    }

                    @Override
                    public Publisher fromString(
                            String string) {

                        return null;
                    }
                }
        );

        publisherBox.getChildren().addAll(
                publisherLabel,
                publisherCombo
        );

        // -----------------------------------------------------
        // DATE
        // -----------------------------------------------------

        VBox dateBox =
                new VBox(7);

        Label dateLabel =
                new Label("Import Date");

        dateLabel.getStyleClass()
                .add("field-label");

        importDatePicker =
                new DatePicker(
                        LocalDate.now()
                );

        importDatePicker.getStyleClass()
                .add("filter-combo");

        importDatePicker.setMaxWidth(
                Double.MAX_VALUE
        );

        dateBox.getChildren().addAll(
                dateLabel,
                importDatePicker
        );

        HBox topRow =
                new HBox(20);

        HBox.setHgrow(
                publisherBox,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                dateBox,
                Priority.ALWAYS
        );

        topRow.getChildren().addAll(
                publisherBox,
                dateBox
        );

        // -----------------------------------------------------
        // BOOK TYPE
        // -----------------------------------------------------

        Label bookSection =
                new Label("Book Information");

        bookSection.getStyleClass()
                .add("subsection-title");

        existingBookRadio =
                new RadioButton("Existing Book");

        newBookRadio =
                new RadioButton("New Book");

        existingBookRadio.getStyleClass()
                .add("type-radio");

        newBookRadio.getStyleClass()
                .add("type-radio");

        bookTypeGroup =
                new ToggleGroup();

        existingBookRadio.setToggleGroup(
                bookTypeGroup
        );

        newBookRadio.setToggleGroup(
                bookTypeGroup
        );

        existingBookRadio.setSelected(true);

        HBox typeBox =
                new HBox(18);

        typeBox.setAlignment(
                Pos.CENTER_LEFT
        );

        typeBox.getChildren().addAll(
                existingBookRadio,
                newBookRadio
        );

        // -----------------------------------------------------
        // BOOK PANES
        // -----------------------------------------------------

        existingBookPane =
                createExistingBookPane();

        newBookPane =
                createNewBookPane();

        newBookPane.setVisible(false);

        newBookPane.setManaged(false);

        bookTypeGroup.selectedToggleProperty()
                .addListener(
                        (obs, oldValue, newValue) -> {

                            boolean existing =
                                    newValue
                                            == existingBookRadio;

                            existingBookPane
                                    .setVisible(existing);

                            existingBookPane
                                    .setManaged(existing);

                            newBookPane
                                    .setVisible(!existing);

                            newBookPane
                                    .setManaged(!existing);
                        }
                );

        // -----------------------------------------------------
        // ADD BUTTON
        // -----------------------------------------------------

        Button addButton =
                createPrimaryButton(
                        "Add to Receipt"
                );

        addButton.setOnAction(
                e -> addItemToReceipt()
        );

        card.getChildren().addAll(
                sectionTitle,
                topRow,
                bookSection,
                typeBox,
                existingBookPane,
                newBookPane,
                createSeparator(),
                addButton
        );

        return card;
    }

    // =========================================================
    // EXISTING BOOK
    // =========================================================

    private VBox createExistingBookPane() {

        VBox container =
                new VBox(15);

        container.getStyleClass()
                .add("import-book-pane");

        Label title =
                new Label("Existing Book");

        title.getStyleClass()
                .add("subsection-title");

        existingBookCombo =
                new ComboBox<>();

        existingBookCombo.setPromptText(
                "Select an existing book"
        );

        existingBookCombo.getStyleClass()
                .add("filter-combo");

        existingBookCombo.setMaxWidth(
                Double.MAX_VALUE
        );

        existingBookCombo.setConverter(
                new StringConverter<Book>() {

                    @Override
                    public String toString(
                            Book book) {

                        if (book == null) {
                            return "";
                        }

                        String isbn =
                                book.getIsbn() == null
                                        ? ""
                                        : " • ISBN: "
                                          + book.getIsbn();

                        return book.getTitle()
                                + isbn;
                    }

                    @Override
                    public Book fromString(
                            String string) {

                        return null;
                    }
                }
        );

        VBox bookBox =
                fieldBox(
                        "Book",
                        existingBookCombo
                );

        existingBookPriceField =
                new TextField();

        existingBookPriceField.setEditable(false);

        existingBookPriceField
                .setFocusTraversable(false);

        existingBookPriceField
                .getStyleClass()
                .add("readonly-field");

        VBox priceBox =
                fieldBox(
                        "Current Price",
                        existingBookPriceField
                );

        existingQuantityField =
                new TextField();

        existingQuantityField.setPromptText(
                "Enter quantity"
        );

        VBox quantityBox =
                fieldBox(
                        "Import Quantity",
                        existingQuantityField
                );

        HBox row =
                new HBox(15);

        HBox.setHgrow(
                bookBox,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                priceBox,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                quantityBox,
                Priority.ALWAYS
        );

        row.getChildren().addAll(
                bookBox,
                priceBox,
                quantityBox
        );

        existingBookCombo.valueProperty()
                .addListener(
                        (obs, oldBook, newBook) -> {

                            if (newBook == null) {

                                existingBookPriceField
                                        .clear();

                                return;
                            }

                            BigDecimal price =
                                    newBook.getPrice();

                            existingBookPriceField
                                    .setText(
                                            formatMoney(price)
                                    );
                        }
                );

        container.getChildren().addAll(
                title,
                row
        );

        return container;
    }

    // =========================================================
    // NEW BOOK
    // =========================================================

    private VBox createNewBookPane() {

        VBox container =
                new VBox(16);

        container.getStyleClass()
                .add("import-book-pane");

        Label title =
                new Label("New Book");

        title.getStyleClass()
                .add("subsection-title");

        newTitleField =
                new TextField();

        newTitleField.setPromptText(
                "Book title"
        );

        newIsbnField =
                new TextField();

        newIsbnField.setPromptText(
                "ISBN"
        );

        newPublishYearField =
                new TextField();

        newPublishYearField.setPromptText(
                "Publish year"
        );

        newDescriptionArea =
                new TextArea();

        newDescriptionArea.setPromptText(
                "Book description"
        );

        newDescriptionArea.setPrefRowCount(3);

        newDescriptionArea.setWrapText(true);

        newPriceField =
                new TextField();

        newPriceField.setPromptText(
                "Import price"
        );

        newQuantityField =
                new TextField();

        newQuantityField.setPromptText(
                "Quantity"
        );

        // -----------------------------------------------------
        // BASIC INFO
        // -----------------------------------------------------

        HBox basicRow =
                new HBox(15);

        VBox titleBox =
                fieldBox(
                        "Title",
                        newTitleField
                );

        VBox isbnBox =
                fieldBox(
                        "ISBN",
                        newIsbnField
                );

        VBox yearBox =
                fieldBox(
                        "Publish Year",
                        newPublishYearField
                );

        HBox.setHgrow(
                titleBox,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                isbnBox,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                yearBox,
                Priority.ALWAYS
        );

        basicRow.getChildren().addAll(
                titleBox,
                isbnBox,
                yearBox
        );

        // -----------------------------------------------------
        // AUTHOR / CATEGORY
        // -----------------------------------------------------

        VBox authorSection =
                createAuthorSection();

        VBox categorySection =
                createCategorySection();

        // -----------------------------------------------------
        // PRICE / QUANTITY
        // -----------------------------------------------------

        HBox priceQuantityRow =
                new HBox(15);

        VBox priceBox =
                fieldBox(
                        "Import Price",
                        newPriceField
                );

        VBox quantityBox =
                fieldBox(
                        "Quantity",
                        newQuantityField
                );

        HBox.setHgrow(
                priceBox,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                quantityBox,
                Priority.ALWAYS
        );

        priceQuantityRow.getChildren().addAll(
                priceBox,
                quantityBox
        );

        VBox descriptionBox =
                fieldBox(
                        "Description",
                        newDescriptionArea
                );

        container.getChildren().addAll(
                title,
                basicRow,
                authorSection,
                categorySection,
                priceQuantityRow,
                descriptionBox
        );

        return container;
    }

    // =========================================================
    // AUTHOR SECTION
    // =========================================================

    private VBox createAuthorSection() {

        VBox section =
                new VBox(8);

        Label label =
                new Label("Authors");

        label.getStyleClass()
                .add("field-label");

        authorListView =
                new ListView<>();

        authorListView.setPrefHeight(135);

        authorListView.setMaxWidth(
                Double.MAX_VALUE
        );

        authorListView.getStyleClass()
                .add("selection-list");

        authorListView.setCellFactory(
                CheckBoxListCell.forListView(
                        author ->
                                authorSelection
                                        .computeIfAbsent(
                                                author.getId(),
                                                id -> {

                                                    SimpleBooleanProperty
                                                            property =
                                                            new SimpleBooleanProperty(
                                                                    false
                                                            );

                                                    property.addListener(
                                                            (obs,
                                                             oldValue,
                                                             newValue) -> {
                                                            }
                                                    );

                                                    return property;
                                                }
                                        ),
                        new StringConverter<Author>() {

                            @Override
                            public String toString(
                                    Author author) {

                                return author == null
                                        ? ""
                                        : author.getName();
                            }

                            @Override
                            public Author fromString(
                                    String string) {

                                return null;
                            }
                        }
                )
        );

        Button addAuthorButton =
                createSmallSecondaryButton(
                        "+ Add New Author"
                );

        addAuthorButton.setOnAction(
                e -> showAddAuthorDialog()
        );

        section.getChildren().addAll(
                label,
                authorListView,
                addAuthorButton
        );

        return section;
    }

    // =========================================================
    // CATEGORY SECTION
    // =========================================================

    private VBox createCategorySection() {

        VBox section =
                new VBox(8);

        Label label =
                new Label("Categories");

        label.getStyleClass()
                .add("field-label");

        categoryListView =
                new ListView<>();

        categoryListView.setPrefHeight(135);

        categoryListView.setMaxWidth(
                Double.MAX_VALUE
        );

        categoryListView.getStyleClass()
                .add("selection-list");

        categoryListView.setCellFactory(
                CheckBoxListCell.forListView(
                        category ->
                                categorySelection
                                        .computeIfAbsent(
                                                category.getId(),
                                                id -> {

                                                    SimpleBooleanProperty
                                                            property =
                                                            new SimpleBooleanProperty(
                                                                    false
                                                            );

                                                    property.addListener(
                                                            (obs,
                                                             oldValue,
                                                             newValue) ->
                                                                    refreshPrimaryCategoryChoices()
                                                    );

                                                    return property;
                                                }
                                        ),
                        new StringConverter<Category>() {

                            @Override
                            public String toString(
                                    Category category) {

                                return category == null
                                        ? ""
                                        : category.getName();
                            }

                            @Override
                            public Category fromString(
                                    String string) {

                                return null;
                            }
                        }
                )
        );

        Label primaryLabel =
                new Label(
                        "Primary Category"
                );

        primaryLabel.getStyleClass()
                .add("field-label");

        primaryCategoryCombo =
                new ComboBox<>();

        primaryCategoryCombo.setPromptText(
                "Select primary category"
        );

        primaryCategoryCombo.setMaxWidth(
                Double.MAX_VALUE
        );

        primaryCategoryCombo.getStyleClass()
                .add("filter-combo");

        primaryCategoryCombo.setConverter(
                new StringConverter<Category>() {

                    @Override
                    public String toString(
                            Category category) {

                        return category == null
                                ? ""
                                : category.getName();
                    }

                    @Override
                    public Category fromString(
                            String string) {

                        return null;
                    }
                }
        );

        Button addCategoryButton =
                createSmallSecondaryButton(
                        "+ Add New Category"
                );

        addCategoryButton.setOnAction(
                e -> showAddCategoryDialog()
        );

        section.getChildren().addAll(
                label,
                categoryListView,
                addCategoryButton,
                primaryLabel,
                primaryCategoryCombo
        );

        return section;
    }

    // =========================================================
    // RECEIPT SECTION
    // =========================================================

    private VBox createReceiptSection() {

        VBox card =
                createCard();

        Label title =
                new Label("RECEIPT ITEMS");

        title.getStyleClass()
                .add("section-title");

        importTable =
                new TableView<>();

        importTable.setItems(
                importItems
        );

        importTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
        );

        importTable.getStyleClass()
                .add("vintage-table");

        // -----------------------------------------------------
        // #
        // -----------------------------------------------------

        TableColumn<ImportItem, Number>
                numberColumn =
                new TableColumn<>("#");

        numberColumn.setCellValueFactory(
                cell ->
                        new SimpleObjectProperty<>(
                                importItems.indexOf(
                                        cell.getValue()
                                ) + 1
                        )
        );

        numberColumn.setMaxWidth(55);

        // -----------------------------------------------------
        // BOOK TITLE
        // -----------------------------------------------------

        TableColumn<ImportItem, String>
                titleColumn =
                new TableColumn<>("Book Title");

        titleColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                cell.getValue()
                                        .getTitle()
                        )
        );

        // -----------------------------------------------------
        // TYPE
        // -----------------------------------------------------

        TableColumn<ImportItem, String>
                typeColumn =
                new TableColumn<>("Type");

        typeColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                cell.getValue()
                                        .getType()
                        )
        );

        // -----------------------------------------------------
        // QUANTITY
        // -----------------------------------------------------

        TableColumn<ImportItem, Number>
                quantityColumn =
                new TableColumn<>("Quantity");

        quantityColumn.setCellValueFactory(
                cell ->
                        new SimpleObjectProperty<>(
                                cell.getValue()
                                        .getQuantity()
                        )
        );

        // -----------------------------------------------------
        // PRICE
        // -----------------------------------------------------

        TableColumn<ImportItem, String>
                priceColumn =
                new TableColumn<>("Unit Price");

        priceColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                formatMoney(
                                        cell.getValue()
                                                .getUnitPrice()
                                )
                        )
        );

        // -----------------------------------------------------
        // AMOUNT
        // -----------------------------------------------------

        TableColumn<ImportItem, String>
                amountColumn =
                new TableColumn<>("Amount");

        amountColumn.setCellValueFactory(
                cell ->
                        new SimpleStringProperty(
                                formatMoney(
                                        cell.getValue()
                                                .getAmount()
                                )
                        )
        );

        // -----------------------------------------------------
        // ACTION
        // -----------------------------------------------------

        TableColumn<ImportItem, Void>
                actionColumn =
                new TableColumn<>("Action");

        actionColumn.setCellFactory(
                column ->
                        new TableCell<>() {

                            private final Button
                                    removeButton =
                                    createSmallDangerButton(
                                            "Remove"
                                    );

                            {
                                removeButton
                                        .setOnAction(
                                                e -> {

                                                    ImportItem
                                                            item =
                                                            getTableView()
                                                                    .getItems()
                                                                    .get(
                                                                            getIndex()
                                                                    );

                                                    importItems
                                                            .remove(
                                                                    item
                                                            );

                                                    updateTotal();
                                                }
                                        );
                            }

                            @Override
                            protected void updateItem(
                                    Void item,
                                    boolean empty) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                setGraphic(
                                        empty
                                                ? null
                                                : removeButton
                                );
                            }
                        }
        );

        importTable.getColumns()
                .addAll(
                        numberColumn,
                        titleColumn,
                        typeColumn,
                        quantityColumn,
                        priceColumn,
                        amountColumn,
                        actionColumn
                );

        importTable.setPrefHeight(300);

        // -----------------------------------------------------
        // TOTAL
        // -----------------------------------------------------

        totalAmountLabel =
                new Label(
                        formatMoney(
                                BigDecimal.ZERO
                        )
                );

        totalAmountLabel
                .getStyleClass()
                .add("import-total-value");

        Label totalLabel =
                new Label("TOTAL");

        totalLabel
                .getStyleClass()
                .add("import-total-label");

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        HBox totalBox =
                new HBox(
                        15,
                        totalLabel,
                        spacer,
                        totalAmountLabel
                );

        totalBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        totalBox
                .getStyleClass()
                .add("import-total-box");

        // -----------------------------------------------------
        // CONFIRM
        // -----------------------------------------------------

        Button confirmButton =
                createPrimaryButton(
                        "Confirm Import"
                );

        confirmButton.setOnAction(
                e -> confirmImport()
        );

        HBox actionBox =
                new HBox(
                        confirmButton
                );

        actionBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        card.getChildren().addAll(
                title,
                importTable,
                totalBox,
                actionBox
        );

        return card;
    }

    // =========================================================
    // ADD ITEM
    // =========================================================

    private void addItemToReceipt() {

        if (publisherCombo.getValue() == null) {

            showWarning(
                    "Missing Publisher",
                    "Please select a publisher."
            );

            return;
        }

        if (existingBookRadio.isSelected()) {

            addExistingBook();

        } else {

            addNewBook();
        }
    }

    // =========================================================
    // ADD EXISTING BOOK
    // =========================================================

    private void addExistingBook() {

        Book book =
                existingBookCombo.getValue();

        if (book == null) {

            showWarning(
                    "Missing Book",
                    "Please select an existing book."
            );

            return;
        }

        int quantity;

        try {

            quantity =
                    parsePositiveInteger(
                            existingQuantityField
                                    .getText()
                    );

        } catch (Exception e) {

            showWarning(
                    "Invalid Quantity",
                    "Quantity must be greater than 0."
            );

            return;
        }

        BigDecimal price =
                book.getPrice();

        if (price == null) {

            showWarning(
                    "Missing Price",
                    "This book does not have a valid price."
            );

            return;
        }

        boolean duplicate =
                importItems.stream()
                        .anyMatch(
                                item ->
                                        item.getBookId() != null
                                                && item.getBookId()
                                                .equals(
                                                        book.getId()
                                                )
                        );

        if (duplicate) {

            showWarning(
                    "Duplicate Book",
                    "This book is already in the receipt."
            );

            return;
        }

        ImportItem item =
                new ImportItem(
                        book.getId(),
                        book.getTitle(),
                        "Existing",
                        quantity,
                        price,
                        null
                );

        importItems.add(item);

        existingBookCombo
                .getSelectionModel()
                .clearSelection();

        existingQuantityField.clear();

        existingBookPriceField.clear();

        updateTotal();
    }

    // =========================================================
    // ADD NEW BOOK
    // =========================================================

    private void addNewBook() {

        String title =
                newTitleField.getText()
                        .trim();

        String isbn =
                newIsbnField.getText()
                        .trim();

        if (title.isBlank()) {

            showWarning(
                    "Missing Title",
                    "Book title is required."
            );

            return;
        }

        if (isbn.isBlank()) {

            showWarning(
                    "Missing ISBN",
                    "ISBN is required."
            );

            return;
        }

        // -----------------------------------------------------
        // AUTHORS
        // -----------------------------------------------------

        List<Long> authorIds =
                authorSelection.entrySet()
                        .stream()
                        .filter(
                                entry ->
                                        entry.getValue().get()
                        )
                        .map(
                                Map.Entry::getKey
                        )
                        .filter(
                                Objects::nonNull
                        )
                        .distinct()
                        .toList();

        List<String> authorNames =
                authorListView.getItems()
                        .stream()
                        .filter(
                                author ->
                                        authorIds.contains(
                                                author.getId()
                                        )
                        )
                        .map(
                                Author::getName
                        )
                        .toList();

        // -----------------------------------------------------
        // CATEGORIES
        // -----------------------------------------------------

        List<Long> categoryIds =
                categorySelection.entrySet()
                        .stream()
                        .filter(
                                entry ->
                                        entry.getValue().get()
                        )
                        .map(
                                Map.Entry::getKey
                        )
                        .toList();

        if (categoryIds.isEmpty()) {

            showWarning(
                    "Missing Category",
                    "Please select at least one category."
            );

            return;
        }

        Category primaryCategory =
                primaryCategoryCombo.getValue();

        if (primaryCategory == null) {

            showWarning(
                    "Missing Primary Category",
                    "Please select a primary category."
            );

            return;
        }

        if (!categoryIds.contains(
                primaryCategory.getId()
        )) {

            showWarning(
                    "Invalid Primary Category",
                    "Primary category must be one of the selected categories."
            );

            return;
        }

        // -----------------------------------------------------
        // PRICE
        // -----------------------------------------------------

        BigDecimal price;

        try {

            price =
                    parseMoney(
                            newPriceField.getText()
                    );

        } catch (Exception e) {

            showWarning(
                    "Invalid Price",
                    "Please enter a valid import price."
            );

            return;
        }

        // -----------------------------------------------------
        // QUANTITY
        // -----------------------------------------------------

        int quantity;

        try {

            quantity =
                    parsePositiveInteger(
                            newQuantityField
                                    .getText()
                    );

        } catch (Exception e) {

            showWarning(
                    "Invalid Quantity",
                    "Quantity must be greater than 0."
            );

            return;
        }

        // -----------------------------------------------------
        // PUBLISH YEAR
        // -----------------------------------------------------

        Integer publishYear = null;

        if (!newPublishYearField
                .getText()
                .trim()
                .isBlank()) {

            try {

                publishYear =
                        Integer.parseInt(
                                newPublishYearField
                                        .getText()
                                        .trim()
                        );

            } catch (NumberFormatException e) {

                showWarning(
                        "Invalid Year",
                        "Publish year must be a number."
                );

                return;
            }
        }

        // -----------------------------------------------------
        // NEW BOOK DTO
        // -----------------------------------------------------

        NewImportBookRequest newBook =
                new NewImportBookRequest();

        newBook.setTitle(title);

        newBook.setIsbn(isbn);

        newBook.setPublishYear(
                publishYear
        );

        newBook.setDescription(
                newDescriptionArea
                        .getText()
        );

        newBook.setPrice(price);

        newBook.setAuthorIds(
                authorIds.isEmpty()
                        ? null
                        : authorIds
        );

        newBook.setAuthorNames(
                authorNames.isEmpty()
                        ? null
                        : authorNames
        );

        newBook.setCategoryIds(
                categoryIds
        );

        List<String> categoryNames =
                categoryListView.getItems()
                        .stream()
                        .filter(
                                category ->
                                        categoryIds.contains(
                                                category.getId()
                                        )
                        )
                        .map(
                                Category::getName
                        )
                        .toList();

        newBook.setCategoryNames(
                categoryNames
        );

        newBook.setPrimaryCategoryId(
                primaryCategory.getId()
        );

        // -----------------------------------------------------
        // DUPLICATE ISBN IN CURRENT RECEIPT
        // -----------------------------------------------------

        boolean duplicate =
                importItems.stream()
                        .anyMatch(
                                item ->
                                        item.getNewBook() != null
                                                && item.getNewBook()
                                                .getIsbn()
                                                .equalsIgnoreCase(
                                                        isbn
                                                )
                        );

        if (duplicate) {

            showWarning(
                    "Duplicate ISBN",
                    "This ISBN is already in the receipt."
            );

            return;
        }

        // -----------------------------------------------------
        // ADD ITEM
        // -----------------------------------------------------

        ImportItem item =
                new ImportItem(
                        null,
                        title,
                        "New",
                        quantity,
                        price,
                        newBook
                );

        importItems.add(item);

        clearNewBookForm();

        updateTotal();
    }

    // =========================================================
    // CONFIRM IMPORT
    // =========================================================

    private void confirmImport() {

        if (publisherCombo.getValue() == null) {

            showWarning(
                    "Missing Publisher",
                    "Please select a publisher."
            );

            return;
        }

        if (importItems.isEmpty()) {

            showWarning(
                    "Empty Receipt",
                    "Please add at least one book."
            );

            return;
        }

        if (importDatePicker.getValue() == null) {

            showWarning(
                    "Missing Date",
                    "Please select the import date."
            );

            return;
        }

        // =====================================================
        // CONFIRMATION DIALOG
        // =====================================================

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle(
                "Confirm Import"
        );

        confirmation.setHeaderText(
                "Create import receipt?"
        );

        confirmation.setContentText(
                "Total: "
                        + formatMoney(
                        calculateTotal()
                )
        );

        // =====================================================
        // APPLY CUSTOM CSS
        // =====================================================

        DialogPane confirmPane =
                confirmation.getDialogPane();

        confirmPane.getStyleClass()
                .add("import-confirm-dialog");

        if (root.getScene() != null) {

            confirmPane.getStylesheets()
                    .addAll(
                            root.getScene()
                                    .getStylesheets()
                    );
        }

        confirmPane.setPrefWidth(460);

        // =====================================================
        // BUTTON STYLE
        // =====================================================

        Button okButton =
                (Button) confirmPane.lookupButton(
                        ButtonType.OK
                );

        Button cancelButton =
                (Button) confirmPane.lookupButton(
                        ButtonType.CANCEL
                );

        if (okButton != null) {

            okButton.setDefaultButton(true);
        }

        if (cancelButton != null) {

            cancelButton.getStyleClass()
                    .add("dialog-cancel-button");
        }

        // =====================================================
        // SHOW
        // =====================================================

        Optional<ButtonType> result =
                confirmation.showAndWait();

        if (result.isEmpty()
                || result.get()
                != ButtonType.OK) {

            return;
        }

        // =====================================================
        // CREATE REQUEST
        // =====================================================

        try {

            CreateImportReceiptRequest request =
                    new CreateImportReceiptRequest();

            request.setPublisherId(
                    publisherCombo
                            .getValue()
                            .getId()
            );

            request.setImportDate(
                    importDatePicker
                            .getValue()
            );

            List<ImportReceiptDetailRequest>
                    details =
                    new ArrayList<>();

            for (ImportItem item :
                    importItems) {

                ImportReceiptDetailRequest detail =
                        new ImportReceiptDetailRequest();

                detail.setQuantity(
                        item.getQuantity()
                );

                detail.setUnitPrice(
                        item.getUnitPrice()
                );

                if (item.getBookId() != null) {

                    detail.setBookId(
                            item.getBookId()
                    );

                } else {

                    detail.setNewBook(
                            item.getNewBook()
                    );
                }

                details.add(detail);
            }

            request.setDetails(
                    details
            );

            importReceiptService
                    .createImportReceipt(
                            request,
                            currentUserId
                    );

            showSuccess(
                    "Import Completed",
                    "The import receipt was created successfully."
            );

            clearReceipt();

        } catch (Exception e) {

            showError(
                    "Import Failed",
                    extractErrorMessage(e)
            );
        }
    }

    // =========================================================
    // AUTHOR DIALOG
    // =========================================================

    private void showAddAuthorDialog() {

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "Add New Author"
        );

        ButtonType saveButton =
                new ButtonType(
                        "Add Author",
                        ButtonBar.ButtonData.OK_DONE
                );

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        saveButton,
                        ButtonType.CANCEL
                );

        DialogPane dialogPane =
                dialog.getDialogPane();

        // =====================================================
        // APPLY CSS
        // =====================================================

        dialogPane.getStyleClass()
                .add("custom-dialog-pane");

        dialogPane.setPrefWidth(520);

        if (root.getScene() != null) {

            dialogPane.getStylesheets()
                    .addAll(
                            root.getScene()
                                    .getStylesheets()
                    );
        }

        // =====================================================
        // HEADER
        // =====================================================

        VBox header =
                new VBox(4);

        header.getStyleClass()
                .add("dialog-header");

        Label title =
                new Label(
                        "ADD NEW AUTHOR"
                );

        title.getStyleClass()
                .add("dialog-title");

        Label subtitle =
                new Label(
                        "Add a new author to the library catalog"
                );

        subtitle.getStyleClass()
                .add("dialog-subtitle");

        header.getChildren().addAll(
                title,
                subtitle
        );

        // =====================================================
        // NAME
        // =====================================================

        TextField nameField =
                new TextField();

        nameField.setPromptText(
                "Enter author name..."
        );

        nameField.getStyleClass()
                .add("dialog-text-field");

        VBox nameBox =
                fieldBox(
                        "Author Name *",
                        nameField
                );

        // =====================================================
        // BIOGRAPHY
        // =====================================================

        TextArea biographyArea =
                new TextArea();

        biographyArea.setPromptText(
                "Write a short biography..."
        );

        biographyArea.setPrefRowCount(5);

        biographyArea.setWrapText(true);

        biographyArea.getStyleClass()
                .add("dialog-text-area");

        VBox biographyBox =
                fieldBox(
                        "Biography",
                        biographyArea
                );

        // =====================================================
        // FORM
        // =====================================================

        VBox form =
                new VBox(
                        16,
                        nameBox,
                        biographyBox
                );

        form.getStyleClass()
                .add("dialog-form");

        // =====================================================
        // CONTENT
        // =====================================================

        VBox content =
                new VBox(
                        header,
                        form
                );

        content.getStyleClass()
                .add("dialog-content");

        dialogPane.setContent(
                content
        );

        // =====================================================
        // BUTTON STYLE
        // =====================================================

        Node cancelButton =
                dialogPane.lookupButton(
                        ButtonType.CANCEL
                );

        Node addButton =
                dialogPane.lookupButton(
                        saveButton
                );

        if (cancelButton != null) {

            cancelButton.getStyleClass()
                    .add("dialog-cancel-button");
        }

        if (addButton != null) {

            addButton.getStyleClass()
                    .add("dialog-primary-button");
        }

        // =====================================================
        // RESULT
        // =====================================================

        dialog.setResultConverter(
                button -> {

                    if (button == saveButton) {

                        String name =
                                nameField.getText()
                                        .trim();

                        if (name.isBlank()) {

                            showWarning(
                                    "Invalid Author",
                                    "Author name is required."
                            );

                            return null;
                        }

                        try {

                            Author author =
                                    new Author();

                            author.setName(name);

                            author.setBiography(
                                    biographyArea
                                            .getText()
                            );

                            author.setStatus(
                                    "ACTIVE"
                            );

                            Author saved =
                                    authorService
                                            .createAuthor(
                                                    author
                                            );

                            refreshAuthors();

                            SimpleBooleanProperty property =
                                    authorSelection
                                            .computeIfAbsent(
                                                    saved.getId(),
                                                    id ->
                                                            new SimpleBooleanProperty(
                                                                    false
                                                            )
                                            );

                            property.set(true);

                            return saveButton;

                        } catch (Exception e) {

                            showError(
                                    "Cannot Create Author",
                                    extractErrorMessage(e)
                            );
                        }
                    }

                    return null;
                }
        );

        dialog.showAndWait();
    }

    // =========================================================
    // CATEGORY DIALOG
    // =========================================================

    private void showAddCategoryDialog() {

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                "Add New Category"
        );

        ButtonType saveButton =
                new ButtonType(
                        "Add Category",
                        ButtonBar.ButtonData.OK_DONE
                );

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        saveButton,
                        ButtonType.CANCEL
                );

        DialogPane dialogPane =
                dialog.getDialogPane();

        // =====================================================
        // APPLY CSS
        // =====================================================

        dialogPane.getStyleClass()
                .add("custom-dialog-pane");

        dialogPane.setPrefWidth(480);

        if (root.getScene() != null) {

            dialogPane.getStylesheets()
                    .addAll(
                            root.getScene()
                                    .getStylesheets()
                    );
        }

        // =====================================================
        // HEADER
        // =====================================================

        VBox header =
                new VBox(4);

        header.getStyleClass()
                .add("dialog-header");

        Label title =
                new Label(
                        "ADD NEW CATEGORY"
                );

        title.getStyleClass()
                .add("dialog-title");

        Label subtitle =
                new Label(
                        "Add a new category to organize your books"
                );

        subtitle.getStyleClass()
                .add("dialog-subtitle");

        header.getChildren().addAll(
                title,
                subtitle
        );

        // =====================================================
        // CATEGORY NAME
        // =====================================================

        TextField nameField =
                new TextField();

        nameField.setPromptText(
                "Enter category name..."
        );

        nameField.getStyleClass()
                .add("dialog-text-field");

        VBox nameBox =
                fieldBox(
                        "Category Name *",
                        nameField
                );

        // =====================================================
        // NOTE
        // =====================================================

        Label note =
                new Label(
                        "After creating the category, configure its default shelf in Genres & Topics before using it as the primary category of a new book."
                );

        note.setWrapText(true);

        note.getStyleClass()
                .add("dialog-note");

        // =====================================================
        // FORM
        // =====================================================

        VBox form =
                new VBox(
                        16,
                        nameBox,
                        note
                );

        form.getStyleClass()
                .add("dialog-form");

        // =====================================================
        // CONTENT
        // =====================================================

        VBox content =
                new VBox(
                        header,
                        form
                );

        content.getStyleClass()
                .add("dialog-content");

        dialogPane.setContent(
                content
        );

        // =====================================================
        // BUTTON STYLE
        // =====================================================

        Node cancelButton =
                dialogPane.lookupButton(
                        ButtonType.CANCEL
                );

        Node addButton =
                dialogPane.lookupButton(
                        saveButton
                );

        if (cancelButton != null) {

            cancelButton.getStyleClass()
                    .add("dialog-cancel-button");
        }

        if (addButton != null) {

            addButton.getStyleClass()
                    .add("dialog-primary-button");
        }

        // =====================================================
        // RESULT
        // =====================================================

        dialog.setResultConverter(
                button -> {

                    if (button == saveButton) {

                        String name =
                                nameField.getText()
                                        .trim();

                        if (name.isBlank()) {

                            showWarning(
                                    "Invalid Category",
                                    "Category name is required."
                            );

                            return null;
                        }

                        try {

                            if (categoryRepository
                                    .existsByNameIgnoreCase(
                                            name
                                    )) {

                                showWarning(
                                        "Duplicate Category",
                                        "This category already exists."
                                );

                                return null;
                            }

                            Category category =
                                    new Category();

                            category.setName(name);

                            category.setStatus(
                                    "ACTIVE"
                            );

                            Category saved =
                                    categoryRepository
                                            .save(
                                                    category
                                            );

                            refreshCategories();

                            SimpleBooleanProperty property =
                                    categorySelection
                                            .computeIfAbsent(
                                                    saved.getId(),
                                                    id ->
                                                            new SimpleBooleanProperty(
                                                                    false
                                                            )
                                            );

                            property.set(true);

                            refreshPrimaryCategoryChoices();

                            return saveButton;

                        } catch (Exception e) {

                            showError(
                                    "Cannot Create Category",
                                    extractErrorMessage(e)
                            );
                        }
                    }

                    return null;
                }
        );

        dialog.showAndWait();
    }

    // =========================================================
    // LOAD DATA
    // =========================================================

    private void loadInitialData() {

        try {

            publisherCombo.setItems(
                    FXCollections.observableArrayList(
                            publisherRepository
                                    .findAll()
                                    .stream()
                                    .filter(
                                            p ->
                                                    "ACTIVE"
                                                            .equalsIgnoreCase(
                                                                    p.getStatus()
                                                            )
                                    )
                                    .toList()
                    )
            );

            existingBookCombo.setItems(
                    FXCollections.observableArrayList(
                            bookRepository
                                    .findAll()
                                    .stream()
                                    .filter(
                                            b ->
                                                    "ACTIVE"
                                                            .equalsIgnoreCase(
                                                                    b.getStatus()
                                                            )
                                    )
                                    .toList()
                    )
            );

            refreshAuthors();

            refreshCategories();

        } catch (Exception e) {

            showError(
                    "Cannot Load Data",
                    extractErrorMessage(e)
            );
        }
    }

    // =========================================================
    // REFRESH AUTHORS
    // =========================================================

    private void refreshAuthors() {

        List<Author> authors =
                authorService
                        .getAllAuthors()
                        .stream()
                        .filter(
                                author ->
                                        "ACTIVE"
                                                .equalsIgnoreCase(
                                                        author.getStatus()
                                                )
                        )
                        .sorted(
                                Comparator.comparing(
                                        Author::getName,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                        .toList();

        authorListView.setItems(
                FXCollections.observableArrayList(
                        authors
                )
        );

        for (Author author :
                authors) {

            authorSelection.computeIfAbsent(
                    author.getId(),
                    id ->
                            new SimpleBooleanProperty(
                                    false
                            )
            );
        }
    }

    // =========================================================
    // REFRESH CATEGORIES
    // =========================================================

    private void refreshCategories() {

        List<Category> categories =
                categoryRepository
                        .findAll()
                        .stream()
                        .filter(
                                category ->
                                        "ACTIVE"
                                                .equalsIgnoreCase(
                                                        category.getStatus()
                                                )
                        )
                        .sorted(
                                Comparator.comparing(
                                        Category::getName,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                        .toList();

        categoryListView.setItems(
                FXCollections.observableArrayList(
                        categories
                )
        );

        for (Category category :
                categories) {

            categorySelection.computeIfAbsent(
                    category.getId(),
                    id -> {

                        SimpleBooleanProperty
                                property =
                                new SimpleBooleanProperty(
                                        false
                                );

                        property.addListener(
                                (obs,
                                 oldValue,
                                 newValue) ->
                                        refreshPrimaryCategoryChoices()
                        );

                        return property;
                    }
            );
        }

        refreshPrimaryCategoryChoices();
    }

    // =========================================================
    // PRIMARY CATEGORY
    // =========================================================

    private void refreshPrimaryCategoryChoices() {

        if (primaryCategoryCombo == null
                || categoryListView == null) {

            return;
        }

        List<Category> selected =
                categoryListView.getItems()
                        .stream()
                        .filter(
                                category -> {

                                    SimpleBooleanProperty
                                            property =
                                            categorySelection
                                                    .get(
                                                            category.getId()
                                                    );

                                    return property != null
                                            && property.get();
                                }
                        )
                        .toList();

        Category oldValue =
                primaryCategoryCombo.getValue();

        primaryCategoryCombo.setItems(
                FXCollections.observableArrayList(
                        selected
                )
        );

        if (oldValue != null
                && selected.stream()
                .anyMatch(
                        category ->
                                category.getId()
                                        .equals(
                                                oldValue.getId()
                                        )
                )) {

            primaryCategoryCombo.setValue(
                    selected.stream()
                            .filter(
                                    category ->
                                            category.getId()
                                                    .equals(
                                                            oldValue.getId()
                                                    )
                            )
                            .findFirst()
                            .orElse(null)
            );

        } else if (selected.size() == 1) {

            primaryCategoryCombo.setValue(
                    selected.get(0)
            );

        } else {

            primaryCategoryCombo.setValue(
                    null
            );
        }
    }

    // =========================================================
    // CLEAR NEW BOOK
    // =========================================================

    private void clearNewBookForm() {

        newTitleField.clear();

        newIsbnField.clear();

        newPublishYearField.clear();

        newDescriptionArea.clear();

        newPriceField.clear();

        newQuantityField.clear();

        authorSelection.values()
                .forEach(
                        property ->
                                property.set(false)
                );

        categorySelection.values()
                .forEach(
                        property ->
                                property.set(false)
                );

        primaryCategoryCombo.setValue(
                null
        );

        refreshPrimaryCategoryChoices();
    }

    // =========================================================
    // CLEAR RECEIPT
    // =========================================================

    private void clearReceipt() {

        importItems.clear();

        publisherCombo
                .getSelectionModel()
                .clearSelection();

        existingBookCombo
                .getSelectionModel()
                .clearSelection();

        existingBookPriceField.clear();

        existingQuantityField.clear();

        clearNewBookForm();

        importDatePicker.setValue(
                LocalDate.now()
        );

        updateTotal();
    }

    // =========================================================
    // TOTAL
    // =========================================================

    private void updateTotal() {

        if (totalAmountLabel != null) {

            totalAmountLabel.setText(
                    formatMoney(
                            calculateTotal()
                    )
            );
        }

        if (importTable != null) {

            importTable.refresh();
        }
    }

    private BigDecimal calculateTotal() {

        return importItems.stream()
                .map(
                        ImportItem::getAmount
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    // =========================================================
    // UI HELPERS
    // =========================================================

    private VBox createCard() {

        VBox card =
                new VBox(15);

        card.getStyleClass()
                .add("import-card");

        return card;
    }

    private VBox fieldBox(
            String labelText,
            Node field) {

        VBox box =
                new VBox(6);

        Label label =
                new Label(labelText);

        label.getStyleClass()
                .add("field-label");

        box.getChildren().addAll(
                label,
                field
        );

        VBox.setVgrow(
                field,
                Priority.NEVER
        );

        return box;
    }

    private Separator createSeparator() {

        Separator separator =
                new Separator();

        separator.getStyleClass()
                .add("vintage-separator");

        return separator;
    }

    private Button createPrimaryButton(
            String text) {

        Button button =
                new Button(text);

        button.getStyleClass()
                .add("btn-primary");

        button.setPrefHeight(40);

        return button;
    }

    private Button createSmallSecondaryButton(
            String text) {

        Button button =
                new Button(text);

        button.getStyleClass()
                .add("btn-secondary");

        button.setPrefHeight(34);

        return button;
    }

    private Button createSmallDangerButton(
            String text) {

        Button button =
                new Button(text);

        button.getStyleClass()
                .add("table-cell-btn");

        button.setPrefHeight(30);

        return button;
    }

    // =========================================================
    // PARSE
    // =========================================================

    private int parsePositiveInteger(
            String text) {

        int value =
                Integer.parseInt(
                        text.trim()
                );

        if (value <= 0) {

            throw new IllegalArgumentException();
        }

        return value;
    }

    private BigDecimal parseMoney(
            String text) {

        String value =
                text.trim()
                        .replace(",", "")
                        .replace("₫", "")
                        .trim();

        BigDecimal result =
                new BigDecimal(value);

        if (result.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new IllegalArgumentException();
        }

        return result;
    }

    private String formatMoney(
            BigDecimal value) {

        if (value == null) {

            return "0 ₫";
        }

        NumberFormat format =
                NumberFormat.getNumberInstance(
                        new Locale(
                                "vi",
                                "VN"
                        )
                );

        format.setMaximumFractionDigits(0);

        return format.format(value)
                + " ₫";
    }

    // =========================================================
    // ERROR
    // =========================================================

    private String extractErrorMessage(
            Throwable throwable) {

        Throwable current =
                throwable;

        while (current.getCause() != null
                && current.getCause()
                != current) {

            current =
                    current.getCause();
        }

        if (current.getMessage() != null
                && !current.getMessage()
                .isBlank()) {

            return current.getMessage();
        }

        return throwable.getMessage() != null
                ? throwable.getMessage()
                : "Unknown error";
    }

    // =========================================================
    // ALERTS
    // =========================================================

    private void showWarning(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }

    private void showError(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(title);

        DialogPane dialogPane =
                alert.getDialogPane();

        dialogPane.getStylesheets().clear();

        dialogPane.getStylesheets().add(
                getClass()
                        .getResource("/css/dashboard.css")
                        .toExternalForm()
        );

        dialogPane.getStyleClass()
                .add("atelier-error-dialog");

        Label headerLabel =
                new Label(title);

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

        dialogPane.getButtonTypes()
                .add(okType);

        Button okButton =
                (Button) dialogPane.lookupButton(okType);

        okButton.getStyleClass()
                .add("error-dialog-ok-button");

        alert.showAndWait();
    }

    private void showSuccess(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }

    // =========================================================
    // IMPORT ITEM
    // =========================================================

    public static class ImportItem {

        private final Long bookId;

        private final String title;

        private final String type;

        private final int quantity;

        private final BigDecimal unitPrice;

        private final NewImportBookRequest newBook;

        public ImportItem(
                Long bookId,
                String title,
                String type,
                int quantity,
                BigDecimal unitPrice,
                NewImportBookRequest newBook) {

            this.bookId = bookId;

            this.title = title;

            this.type = type;

            this.quantity = quantity;

            this.unitPrice = unitPrice;

            this.newBook = newBook;
        }

        public Long getBookId() {
            return bookId;
        }

        public String getTitle() {
            return title;
        }

        public String getType() {
            return type;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public BigDecimal getAmount() {

            return unitPrice.multiply(
                    BigDecimal.valueOf(
                            quantity
                    )
            );
        }

        public NewImportBookRequest getNewBook() {
            return newBook;
        }
    }
}
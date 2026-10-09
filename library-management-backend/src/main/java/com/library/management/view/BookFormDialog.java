package com.library.management.view;

import com.library.management.dto.CreateBookRequest;
import com.library.management.entity.Author;
import com.library.management.entity.Book;
import com.library.management.entity.Category;
import com.library.management.entity.Publisher;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BookFormDialog extends Dialog<CreateBookRequest> {

    public BookFormDialog(
            Book existingBook,
            List<Category> categories,
            List<Publisher> publishers
    ) {
        boolean isEdit = existingBook != null;

        setTitle(isEdit ? "Edit Archival Volume" : "Record New Book Volume");
        setHeaderText(
                isEdit
                        ? "Update volume details in the archive."
                        : "Add a new literary record to the repository."
        );

        ButtonType saveBtnType = new ButtonType(
                isEdit ? "UPDATE RECORD" : "PRESERVE ENTRY",
                ButtonBar.ButtonData.OK_DONE
        );

        getDialogPane().getButtonTypes().addAll(
                saveBtnType,
                ButtonType.CANCEL
        );

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 25, 20, 25));

        // Book title
        TextField titleField = new TextField();
        titleField.setPromptText("e.g. The History of Rome");

        // ISBN
        TextField isbnField = new TextField();
        isbnField.setPromptText("e.g. 978-0-123456-47-2");

        // Authors
        TextField authorsField = new TextField();
        authorsField.setPromptText(
                "Comma-separated author names (e.g. Theodor Mommsen, Livy)"
        );

        // Publisher
        ComboBox<Publisher> publisherCombo = new ComboBox<>();
        publisherCombo.getItems().addAll(publishers);

        publisherCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Publisher object) {
                return object != null
                        ? object.getName()
                        : "Select Publisher...";
            }

            @Override
            public Publisher fromString(String string) {
                return null;
            }
        });

        // Publish year
        TextField yearField = new TextField();
        yearField.setPromptText("e.g. 1856");

        // Price
        TextField priceField = new TextField();
        priceField.setPromptText("e.g. 24.50");

        // Primary Category
        ComboBox<Category> primaryCategoryCombo = new ComboBox<>();
        primaryCategoryCombo.getItems().setAll(categories);

        primaryCategoryCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category object) {
                return object != null
                        ? object.getName()
                        : "Select Primary Category...";
            }

            @Override
            public Category fromString(String string) {
                return null;
            }
        });

        // Description
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText(
                "Volume summary, historical annotations, or notes..."
        );
        descriptionArea.setPrefRowCount(3);

        /*
         * When editing:
         * - Preserve all existing category IDs.
         * - The dropdown contains only categories passed into this dialog.
         * - The current primary category is selected when it belongs
         *   to the supplied category list.
         */
        List<Long> existingCategoryIds = List.of();

        if (isEdit) {
            titleField.setText(existingBook.getTitle());
            isbnField.setText(existingBook.getIsbn());

            if (existingBook.getAuthors() != null) {
                authorsField.setText(
                        existingBook.getAuthors()
                                .stream()
                                .map(Author::getName)
                                .collect(Collectors.joining(", "))
                );
            }

            if (existingBook.getPublisher() != null) {
                publisherCombo.setValue(existingBook.getPublisher());
            }

            if (existingBook.getPublishYear() != null) {
                yearField.setText(
                        existingBook.getPublishYear().toString()
                );
            }

            if (existingBook.getPrice() != null) {
                priceField.setText(
                        existingBook.getPrice().toPlainString()
                );
            }

            if (existingBook.getDescription() != null) {
                descriptionArea.setText(existingBook.getDescription());
            }

            /*
             * Read category IDs from the supplied list.
             * BookManagementView must pass the categories associated
             * with this book, obtained through CategoryRepository.
             */
            existingCategoryIds = categories.stream()
                    .map(Category::getId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            if (existingBook.getPrimaryCategory() != null) {
                Long primaryCategoryId =
                        existingBook.getPrimaryCategory().getId();

                primaryCategoryCombo.getItems().stream()
                        .filter(category ->
                                Objects.equals(
                                        category.getId(),
                                        primaryCategoryId
                                )
                        )
                        .findFirst()
                        .ifPresent(primaryCategoryCombo::setValue);
            }
        }

        final List<Long> preservedCategoryIds = existingCategoryIds;

        // Layout
        int row = 0;

        grid.add(new Label("BOOK TITLE *"), 0, row);
        grid.add(titleField, 1, row++);

        grid.add(new Label("ISBN IDENTIFIER *"), 0, row);
        grid.add(isbnField, 1, row++);

        grid.add(new Label("AUTHOR(S) *"), 0, row);
        grid.add(authorsField, 1, row++);

        grid.add(new Label("PRIMARY CATEGORY *"), 0, row);
        grid.add(primaryCategoryCombo, 1, row++);

        grid.add(new Label("PUBLISHER"), 0, row);
        grid.add(publisherCombo, 1, row++);

        grid.add(new Label("PUBLISH YEAR"), 0, row);
        grid.add(yearField, 1, row++);

        grid.add(new Label("PURCHASE PRICE ($) *"), 0, row);
        grid.add(priceField, 1, row++);

        grid.add(new Label("ANNOTATION"), 0, row);
        grid.add(descriptionArea, 1, row);

        getDialogPane().setContent(grid);

        getDialogPane().getStylesheets().add(
                getClass()
                        .getResource("/css/dashboard.css")
                        .toExternalForm()
        );

        // Result converter
        setResultConverter(dialogButton -> {
            if (dialogButton != saveBtnType) {
                return null;
            }

            CreateBookRequest req = new CreateBookRequest();

            req.setTitle(titleField.getText().trim());
            req.setIsbn(isbnField.getText().trim());

            // Price
            if (!priceField.getText().isBlank()) {
                req.setPrice(
                        new BigDecimal(priceField.getText().trim())
                );
            } else {
                req.setPrice(BigDecimal.ZERO);
            }

            // Publish year
            if (!yearField.getText().isBlank()) {
                try {
                    req.setPublishYear(
                            Integer.parseInt(yearField.getText().trim())
                    );
                } catch (NumberFormatException ignored) {
                    // Leave publish year unset if input is invalid.
                }
            }

            // Description
            req.setDescription(descriptionArea.getText());

            // Publisher
            if (publisherCombo.getValue() != null) {
                req.setPublisherId(
                        publisherCombo.getValue().getId()
                );
            }

            // Authors
            if (!authorsField.getText().isBlank()) {
                List<String> names = Arrays.stream(
                                authorsField.getText().split(",")
                        )
                        .map(String::trim)
                        .filter(name -> !name.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());

                req.setAuthorNames(names);
            }

            // Primary Category and category IDs
            Category selectedCategory = primaryCategoryCombo.getValue();

            if (selectedCategory != null) {
                req.setPrimaryCategoryId(selectedCategory.getId());

                if (isEdit) {
                    /*
                     * Keep every category already associated with the book.
                     * Ensure the selected primary category is included.
                     */
                    List<Long> categoryIds = new java.util.ArrayList<>(
                            preservedCategoryIds
                    );

                    Long selectedCategoryId = selectedCategory.getId();

                    if (selectedCategoryId != null
                            && !categoryIds.contains(selectedCategoryId)) {
                        categoryIds.add(selectedCategoryId);
                    }

                    req.setCategoryIds(categoryIds);
                } else {
                    // Creating a new book: start with the selected category.
                    req.setCategoryIds(
                            List.of(selectedCategory.getId())
                    );
                }
            }

            return req;
        });
    }
}
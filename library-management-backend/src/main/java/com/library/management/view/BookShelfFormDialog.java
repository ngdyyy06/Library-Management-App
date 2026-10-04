package com.library.management.view;

import com.library.management.entity.BookShelf;
import com.library.management.entity.Category;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookShelfFormDialog extends Dialog<BookShelf> {

    public BookShelfFormDialog(BookShelf existingShelf, List<Category> allCategories) {
        boolean isEdit = existingShelf != null;
        setTitle(isEdit ? "Modify Shelf Location" : "Construct Shelf Stack");
        setHeaderText(isEdit ? "Update shelf designation, stack code, and associated topics."
                : "Register a new physical bookshelf in the repository (Max capacity: 50 volumes).");

        ButtonType saveBtnType = new ButtonType(isEdit ? "APPLY REVISIONS" : "INSTALL SHELF", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 25, 20, 25));

        TextField codeField = new TextField();
        codeField.setPromptText("e.g. STACK-A1, HIST-04");
        codeField.setPrefWidth(300);

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. West Wing Antiquities Shelf 1");
        nameField.setPrefWidth(300);

        // Category multi-selection list
        ListView<Category> categoryListView = new ListView<>();
        categoryListView.getItems().addAll(allCategories);
        categoryListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        categoryListView.setPrefHeight(140);
        categoryListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        // Điền dữ liệu nếu là Edit
        if (isEdit) {
            codeField.setText(existingShelf.getShelfCode());
            nameField.setText(existingShelf.getName());

            try {
                if (existingShelf.getCategories() != null) {
                    Set<Long> selectedCategoryIds = new HashSet<>();
                    for (Category c : existingShelf.getCategories()) {
                        if (c != null && c.getId() != null) {
                            selectedCategoryIds.add(c.getId());
                        }
                    }

                    for (Category item : categoryListView.getItems()) {
                        if (item != null && selectedCategoryIds.contains(item.getId())) {
                            categoryListView.getSelectionModel().select(item);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not pre-select categories due to session: " + e.getMessage());
            }
        }

        grid.add(new Label("SHELF / STACK CODE *"), 0, 0);
        grid.add(codeField, 1, 0);

        grid.add(new Label("SHELF DESIGNATION NAME *"), 0, 1);
        grid.add(nameField, 1, 1);

        grid.add(new Label("ASSOCIATED GENRES"), 0, 2);
        grid.add(categoryListView, 1, 2);

        getDialogPane().setContent(grid);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());

        setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                String code = codeField.getText().trim();
                String name = nameField.getText().trim();
                if (code.isBlank() || name.isBlank()) {
                    return null;
                }

                BookShelf shelf = isEdit ? existingShelf : new BookShelf();
                shelf.setShelfCode(code);
                shelf.setName(name);

                // Lấy các category được chọn
                List<Category> selected = new ArrayList<>(categoryListView.getSelectionModel().getSelectedItems());
                shelf.setCategories(selected);

                return shelf;
            }
            return null;
        });
    }
}
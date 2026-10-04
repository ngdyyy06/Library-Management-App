package com.library.management.view;

import com.library.management.entity.BookShelf;
import com.library.management.entity.Category;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.util.List;

public class CategoryFormDialog extends Dialog<Category> {

    public CategoryFormDialog(Category existingCategory, List<BookShelf> availableShelves) {
        boolean isEdit = existingCategory != null;
        setTitle(isEdit ? "Modify Genre Classification" : "New Archival Classification");
        setHeaderText(isEdit ? "Update classification title or physical shelf mapping."
                : "Register a new literary discipline or topic category.");

        ButtonType saveBtnType = new ButtonType(isEdit ? "APPLY CHANGES" : "REGISTER CLASSIFICATION", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 25, 20, 25));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Classical Antiquity, Epistemology, Poetry");
        nameField.setPrefWidth(300);

        ComboBox<BookShelf> shelfComboBox = new ComboBox<>();
        shelfComboBox.getItems().addAll(availableShelves);
        shelfComboBox.setPrefWidth(300);
        shelfComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(BookShelf shelf) {
                return shelf != null ? shelf.getName() + " (" + shelf.getShelfCode() + ")" : "Assign default shelf...";
            }

            @Override
            public BookShelf fromString(String string) {
                return null;
            }
        });

        if (isEdit) {
            nameField.setText(existingCategory.getName());
            if (existingCategory.getDefaultShelf() != null) {
                shelfComboBox.setValue(existingCategory.getDefaultShelf());
            }
        }

        grid.add(new Label("CLASSIFICATION NAME *"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("DEFAULT BOOKSHELF *"), 0, 1);
        grid.add(shelfComboBox, 1, 1);

        getDialogPane().setContent(grid);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());

        setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                String name = nameField.getText().trim();
                if (name.isBlank()) {
                    return null;
                }

                Category category = isEdit ? existingCategory : new Category();
                category.setName(name);
                category.setDefaultShelf(shelfComboBox.getValue());
                return category;
            }
            return null;
        });
    }
}
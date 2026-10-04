package com.library.management.view;

import com.library.management.entity.Author;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class AuthorFormDialog extends Dialog<Author> {

    public AuthorFormDialog(Author existingAuthor) {
        boolean isEdit = existingAuthor != null;
        setTitle(isEdit ? "Edit Author Profile" : "Register Author Entry");
        setHeaderText(isEdit ? "Update biographical summary and author credentials."
                : "Register a new literary creator to the archival directory.");

        ButtonType saveBtnType = new ButtonType(isEdit ? "SAVE PROFILE" : "ENROLL AUTHOR", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 25, 20, 25));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Victor Hugo, Marcus Aurelius");
        nameField.setPrefWidth(320);

        TextArea bioArea = new TextArea();
        bioArea.setPromptText("Historical background, notable eras, literary movements, and accolades...");
        bioArea.setPrefRowCount(4);
        bioArea.setWrapText(true);
        bioArea.setPrefWidth(320);

        if (isEdit) {
            nameField.setText(existingAuthor.getName());
            bioArea.setText(existingAuthor.getBiography());
        }

        grid.add(new Label("AUTHOR FULL NAME *"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("BIOGRAPHICAL NOTES"), 0, 1);
        grid.add(bioArea, 1, 1);

        getDialogPane().setContent(grid);
        getDialogPane().getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());

        setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                String name = nameField.getText().trim();
                if (name.isBlank()) {
                    return null;
                }

                Author author = isEdit ? existingAuthor : new Author();
                author.setName(name);
                author.setBiography(bioArea.getText());
                return author;
            }
            return null;
        });
    }
}
package com.library.management.view;

import com.library.management.entity.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class ArchivalDetailDialogs {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    // =========================================================
    // 1. DETAIL: BOOK VOLUME
    // =========================================================
    public static void showBookDetail(Book book) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Volume Dossier • " + book.getTitle());
        dialog.setHeaderText("Archival Record & Literary Metadata");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = createDossierGrid();

        int row = 0;
        addDossierRow(grid, "ISBN IDENTIFIER:", book.getIsbn(), row++);
        addDossierRow(grid, "FULL BOOK TITLE:", book.getTitle(), row++);

        String authors = (book.getAuthors() != null && !book.getAuthors().isEmpty())
                ? book.getAuthors().stream().map(Author::getName).collect(Collectors.joining(", "))
                : "— None Recorded —";
        addDossierRow(grid, "LITERARY AUTHORS:", authors, row++);

        String primaryCategory = book.getPrimaryCategory() != null ? book.getPrimaryCategory().getName() : "—";
        addDossierRow(grid, "PRIMARY GENRE:", primaryCategory, row++);

        String publisher = book.getPublisher() != null ? book.getPublisher().getName() : "— Independent Press —";
        addDossierRow(grid, "PUBLISHER / PRESS:", publisher, row++);

        String shelf = book.getShelf() != null ? book.getShelf().getName() + " [" + book.getShelf().getShelfCode() + "]" : "Unallocated";
        addDossierRow(grid, "PHYSICAL SHELF:", shelf, row++);

        String year = book.getPublishYear() != null ? String.valueOf(book.getPublishYear()) : "—";
        addDossierRow(grid, "PUBLICATION YEAR:", year, row++);

        String price = book.getPrice() != null ? book.getPrice().toPlainString() + " VND" : "0 VND";
        addDossierRow(grid, "VALUATION / PRICE:", price, row++);

        int avail = book.getAvailableQuantity() != null ? book.getAvailableQuantity() : 0;
        int total = book.getTotalQuantity() != null ? book.getTotalQuantity() : 0;
        addDossierRow(grid, "CIRCULATION HOLDINGS:", avail + " available / " + total + " total copies", row++);

        addDossierRow(grid, "CURATORIAL STATUS:", book.getStatus(), row++);

        TextArea desc = new TextArea(book.getDescription() != null && !book.getDescription().isBlank() ? book.getDescription() : "No archival annotations recorded.");
        desc.setEditable(false);
        desc.setWrapText(true);
        desc.setPrefRowCount(3);
        desc.getStyleClass().add("custom-text-field");

        VBox layout = new VBox(14, grid, new Label("HISTORICAL ANNOTATION / SYNOPSIS:"), desc);
        layout.setPadding(new Insets(15, 20, 15, 20));
        layout.setPrefWidth(550);

        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getStylesheets().add(ArchivalDetailDialogs.class.getResource("/css/dashboard.css").toExternalForm());
        dialog.showAndWait();
    }

    // =========================================================
    // 2. DETAIL: PATRON / READER
    // =========================================================
    public static void showReaderDetail(Reader reader) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Patron Demographic Dossier • " + reader.getFullName());
        dialog.setHeaderText("Fellowship Identification & Contact Credentials");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = createDossierGrid();

        int row = 0;
        addDossierRow(grid, "PATRON UNIQUE CODE:", reader.getReaderCode(), row++);
        addDossierRow(grid, "LEGAL FULL NAME:", reader.getFullName().toUpperCase(), row++);
        addDossierRow(grid, "CONTACT TELEPHONE:", reader.getPhone(), row++);
        addDossierRow(grid, "DISPATCH EMAIL:", reader.getEmail() != null ? reader.getEmail() : "— None —", row++);
        addDossierRow(grid, "DATE OF BIRTH:", reader.getDateOfBirth() != null ? reader.getDateOfBirth().format(dateFormatter) : "—", row++);
        addDossierRow(grid, "ENROLLED DATE:", reader.getCreatedAt() != null ? reader.getCreatedAt().toLocalDate().format(dateFormatter) : "—", row++);
        addDossierRow(grid, "MEMBERSHIP STATUS:", reader.getStatus(), row++);

        TextArea addr = new TextArea(reader.getAddress() != null && !reader.getAddress().isBlank() ? reader.getAddress() : "No residential address filed.");
        addr.setEditable(false);
        addr.setWrapText(true);
        addr.setPrefRowCount(2);
        addr.getStyleClass().add("custom-text-field");

        VBox layout = new VBox(14, grid, new Label("REGISTERED DOMICILE ADDRESS:"), addr);
        layout.setPadding(new Insets(15, 20, 15, 20));
        layout.setPrefWidth(520);

        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getStylesheets().add(ArchivalDetailDialogs.class.getResource("/css/dashboard.css").toExternalForm());
        dialog.showAndWait();
    }

    // =========================================================
    // 3. DETAIL: GENRE / CATEGORY
    // =========================================================
    public static void showCategoryDetail(Category category) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Classification Record • " + category.getName());
        dialog.setHeaderText("Subject Area & Stack Allocation");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = createDossierGrid();

        int row = 0;
        addDossierRow(grid, "CLASSIFICATION ID:", "#CAT-" + category.getId(), row++);
        addDossierRow(grid, "GENRE DISCIPLINE:", category.getName(), row++);

        String shelfInfo = category.getDefaultShelf() != null
                ? category.getDefaultShelf().getName() + " (" + category.getDefaultShelf().getShelfCode() + ")"
                : "— No Physical Shelf Assigned —";
        addDossierRow(grid, "DEFAULT ARCHIVAL STACK:", shelfInfo, row++);
        addDossierRow(grid, "CLASSIFICATION STATUS:", category.getStatus(), row++);

        VBox layout = new VBox(14, grid);
        layout.setPadding(new Insets(20, 25, 20, 25));
        layout.setPrefWidth(500);

        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getStylesheets().add(ArchivalDetailDialogs.class.getResource("/css/dashboard.css").toExternalForm());
        dialog.showAndWait();
    }

    // =========================================================
    // 4. DETAIL: AUTHOR
    // =========================================================
    public static void showAuthorDetail(Author author) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Author Profile • " + author.getName());
        dialog.setHeaderText("Literary Creator Dossier & Accolades");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = createDossierGrid();

        int row = 0;
        addDossierRow(grid, "ARCHIVAL ID:", "#AUTH-" + author.getId(), row++);
        addDossierRow(grid, "AUTHOR FULL NAME:", author.getName(), row++);
        addDossierRow(grid, "CREATOR STATUS:", author.getStatus(), row++);

        TextArea bio = new TextArea(author.getBiography() != null && !author.getBiography().isBlank() ? author.getBiography() : "No biography recorded in the archive.");
        bio.setEditable(false);
        bio.setWrapText(true);
        bio.setPrefRowCount(4);
        bio.getStyleClass().add("custom-text-field");

        VBox layout = new VBox(14, grid, new Label("BIOGRAPHICAL SUMMARY & HISTORICAL CONTEXT:"), bio);
        layout.setPadding(new Insets(15, 20, 15, 20));
        layout.setPrefWidth(520);

        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getStylesheets().add(ArchivalDetailDialogs.class.getResource("/css/dashboard.css").toExternalForm());
        dialog.showAndWait();
    }

    // =========================================================
    // 5. DETAIL: PUBLISHER
    // =========================================================
    public static void showPublisherDetail(Publisher publisher) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Publishing House Dossier • " + publisher.getName());
        dialog.setHeaderText("Imprint & Distribution House Credentials");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = createDossierGrid();

        int row = 0;
        addDossierRow(grid, "PUBLISHER ID:", "#PUB-" + publisher.getId(), row++);
        addDossierRow(grid, "PRESS / IMPRINT NAME:", publisher.getName(), row++);
        addDossierRow(grid, "DISPATCH EMAIL:", publisher.getEmail() != null ? publisher.getEmail() : "— None —", row++);
        addDossierRow(grid, "TELEPHONE DISPATCH:", publisher.getPhone() != null ? publisher.getPhone() : "— None —", row++);
        addDossierRow(grid, "PRESS STATUS:", publisher.getStatus(), row++);

        TextArea addr = new TextArea(publisher.getAddress() != null && !publisher.getAddress().isBlank() ? publisher.getAddress() : "No headquarters address recorded.");
        addr.setEditable(false);
        addr.setWrapText(true);
        addr.setPrefRowCount(2);
        addr.getStyleClass().add("custom-text-field");

        VBox layout = new VBox(14, grid, new Label("EDITORIAL HEADQUARTERS:"), addr);
        layout.setPadding(new Insets(15, 20, 15, 20));
        layout.setPrefWidth(520);

        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getStylesheets().add(ArchivalDetailDialogs.class.getResource("/css/dashboard.css").toExternalForm());
        dialog.showAndWait();
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================
    private static GridPane createDossierGrid() {
        GridPane g = new GridPane();
        g.setHgap(16);
        g.setVgap(8);
        return g;
    }

    private static void addDossierRow(GridPane g, String label, String value, int row) {
        Label l = new Label(label);
        l.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #5A4030;");
        l.setPrefWidth(160);

        Label v = new Label(value != null ? value : "—");
        v.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #27160E; -fx-font-weight: bold;");

        g.add(l, 0, row);
        g.add(v, 1, row);
    }
}
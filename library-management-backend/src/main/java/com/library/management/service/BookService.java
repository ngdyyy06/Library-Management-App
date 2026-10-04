package com.library.management.service;

import com.library.management.dto.CreateBookRequest;
import com.library.management.entity.Author;
import com.library.management.entity.Book;
import com.library.management.entity.BookShelf;
import com.library.management.entity.Category;
import com.library.management.entity.Publisher;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.AuthorRepository;
import com.library.management.repository.BookRepository;
import com.library.management.repository.CategoryRepository;
import com.library.management.repository.PublisherRepository;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final CategoryRepository categoryRepository;
    private final BookShelfService bookShelfService;

    public BookService(
            BookRepository bookRepository,
            AuthorRepository authorRepository,
            PublisherRepository publisherRepository,
            CategoryRepository categoryRepository,
            BookShelfService bookShelfService) {

        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
        this.categoryRepository = categoryRepository;
        this.bookShelfService = bookShelfService;
    }

    // =========================================================
    // CREATE BOOK
    // =========================================================

    public Book createBook(CreateBookRequest request) {

        if (request.getIsbn() != null
                && !request.getIsbn().isBlank()
                && bookRepository.existsByIsbn(request.getIsbn())) {

            throw new RuntimeException("ISBN already exists");
        }

        if (request.getPrice() == null
                || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException("Price cannot be negative");
        }

        Book book = new Book();

        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());

        // =====================================================
        // Publisher
        // =====================================================

        Publisher publisher = null;

        if (request.getPublisherId() != null) {

            publisher = publisherRepository
                    .findById(request.getPublisherId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Publisher not found"
                            )
                    );

            if (!"ACTIVE".equals(publisher.getStatus())) {

                throw new RuntimeException(
                        "Cannot create book from inactive publisher"
                );
            }
        }

        book.setPublisher(publisher);

        // =====================================================
        // Basic information
        // =====================================================

        book.setPublishYear(request.getPublishYear());
        book.setDescription(request.getDescription());
        book.setPrice(request.getPrice());

        // Quantity is managed through Import Receipt
        book.setTotalQuantity(0);
        book.setAvailableQuantity(0);

        book.setStatus("ACTIVE");

        // =====================================================
        // Authors
        // =====================================================

        Set<Author> authors = resolveAuthors(request);
        book.setAuthors(authors);

        // =====================================================
        // Categories
        // =====================================================

        Set<Category> categories = resolveCategories(request);
        book.setCategories(categories);

        // =====================================================
        // Primary Category + Shelf
        // =====================================================

        applyPrimaryCategoryAndShelf(
                book,
                request.getPrimaryCategoryId(),
                categories
        );

        return bookRepository.save(book);
    }

    // =========================================================
    // RESOLVE AUTHORS
    // =========================================================

    private Set<Author> resolveAuthors(CreateBookRequest request) {

        Set<Author> authors = new HashSet<>();

        if (request.getAuthorIds() != null) {

            for (Long authorId : request.getAuthorIds()) {

                if (authorId == null) {
                    continue;
                }

                Author author = authorRepository
                        .findById(authorId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Author not found: " + authorId
                                )
                        );

                authors.add(author);
            }
        }

        if (request.getAuthorNames() != null) {

            Set<String> processedNames = new HashSet<>();

            for (String rawName : request.getAuthorNames()) {

                if (rawName == null || rawName.isBlank()) {
                    continue;
                }

                String trimmedName = rawName.trim();

                String normalizedName =
                        trimmedName.toLowerCase(Locale.ROOT);

                if (!processedNames.add(normalizedName)) {
                    continue;
                }

                Author author = authorRepository
                        .findByNameIgnoreCase(trimmedName)
                        .orElse(null);

                if (author == null) {

                    author = new Author();
                    author.setName(trimmedName);
                    author.setStatus("ACTIVE");

                    author = authorRepository.save(author);
                }

                authors.add(author);
            }
        }

        return authors;
    }

    // =========================================================
    // GET ALL BOOKS
    // =========================================================

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // =========================================================
    // GET BOOK BY ID
    // =========================================================

    public Book getBookById(Long id) {

        return bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found"
                        )
                );
    }

    // =========================================================
    // UPDATE BOOK
    // =========================================================

    public Book updateBook(Long id, CreateBookRequest request) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found"
                        )
                );

        Category oldPrimaryCategory =
                book.getPrimaryCategory();

        // =====================================================
        // Check duplicate ISBN
        // =====================================================

        if (request.getIsbn() != null
                && bookRepository.existsByIsbn(request.getIsbn())
                && !request.getIsbn().equals(book.getIsbn())) {

            throw new RuntimeException("ISBN already exists");
        }

        // =====================================================
        // Validate price
        // =====================================================

        if (request.getPrice() == null
                || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException("Price cannot be negative");
        }

        // =====================================================
        // Update basic information
        // =====================================================

        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setPublishYear(request.getPublishYear());
        book.setPrice(request.getPrice());
        book.setDescription(request.getDescription());

        // =====================================================
        // Update Publisher
        // =====================================================

        if (request.getPublisherId() != null) {

            Publisher publisher = publisherRepository
                    .findById(request.getPublisherId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Publisher not found"
                            )
                    );

            if (!"ACTIVE".equals(publisher.getStatus())) {

                throw new RuntimeException(
                        "Cannot assign an inactive publisher to book"
                );
            }

            book.setPublisher(publisher);

        } else {

            book.setPublisher(null);
        }

        // =====================================================
        // Update Authors
        // =====================================================

        if (request.getAuthorIds() != null
                || request.getAuthorNames() != null) {

            Set<Author> authors = resolveAuthors(request);
            book.setAuthors(authors);
        }

        // =====================================================
        // Update Categories
        // =====================================================

        boolean categoriesProvided =
                request.getCategoryIds() != null
                        || request.getCategoryNames() != null;

        if (categoriesProvided) {

            Set<Category> categories =
                    resolveCategories(request);

            book.setCategories(categories);

            if (request.getPrimaryCategoryId() == null) {

                throw new RuntimeException(
                        "Primary category is required when updating book categories"
                );
            }

            updatePrimaryCategoryAndShelf(
                    book,
                    oldPrimaryCategory,
                    request.getPrimaryCategoryId(),
                    categories
            );

        } else if (request.getPrimaryCategoryId() != null) {

            updatePrimaryCategoryAndShelf(
                    book,
                    oldPrimaryCategory,
                    request.getPrimaryCategoryId(),
                    book.getCategories()
            );
        }

        // Quantity is managed by ImportReceipt
        return bookRepository.save(book);
    }

    // =========================================================
    // DEACTIVATE BOOK
    // =========================================================

    public Book deactivateBook(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found"
                        )
                );

        if ("INACTIVE".equals(book.getStatus())) {

            throw new RuntimeException(
                    "Book is already inactive"
            );
        }

        book.setStatus("INACTIVE");

        return bookRepository.save(book);
    }

    // =========================================================
    // ACTIVATE BOOK
    // =========================================================

    public Book activateBook(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found"
                        )
                );

        if ("ACTIVE".equals(book.getStatus())) {

            throw new RuntimeException(
                    "Book is already active"
            );
        }

        book.setStatus("ACTIVE");

        return bookRepository.save(book);
    }

    // =========================================================
    // RESOLVE CATEGORIES
    // =========================================================

    private Set<Category> resolveCategories(
            CreateBookRequest request) {

        Set<Category> categories = new HashSet<>();

        if (request.getCategoryIds() != null) {

            for (Long categoryId : request.getCategoryIds()) {

                if (categoryId == null) {
                    continue;
                }

                Category category = categoryRepository
                        .findById(categoryId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Category not found: "
                                                + categoryId
                                )
                        );

                categories.add(category);
            }
        }

        if (request.getCategoryNames() != null) {

            Set<String> processedNames = new HashSet<>();

            for (String rawName : request.getCategoryNames()) {

                if (rawName == null || rawName.isBlank()) {
                    continue;
                }

                String categoryName = rawName.trim();

                String normalizedName =
                        categoryName.toLowerCase(Locale.ROOT);

                if (!processedNames.add(normalizedName)) {
                    continue;
                }

                Category category = categoryRepository
                        .findByNameIgnoreCase(categoryName)
                        .orElse(null);

                if (category == null) {

                    category = new Category();
                    category.setName(categoryName);
                    category.setStatus("ACTIVE");

                    category =
                            categoryRepository.save(category);
                }

                categories.add(category);
            }
        }

        return categories;
    }

    // =========================================================
    // APPLY PRIMARY CATEGORY + SHELF
    // =========================================================

    private void applyPrimaryCategoryAndShelf(
            Book book,
            Long primaryCategoryId,
            Set<Category> categories) {

        if (primaryCategoryId == null) {

            throw new RuntimeException(
                    "Primary category is required"
            );
        }

        Category primaryCategory = categoryRepository
                .findById(primaryCategoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Primary category not found"
                        )
                );

        boolean belongsToBook =
                categories.stream()
                        .anyMatch(category ->
                                category.getId()
                                        .equals(primaryCategory.getId()));

        if (!belongsToBook) {

            throw new RuntimeException(
                    "Primary category must be one of the book categories"
            );
        }

        BookShelf defaultShelf =
                primaryCategory.getDefaultShelf();

        if (defaultShelf == null) {

            throw new RuntimeException(
                    "Primary category does not have a default shelf"
            );
        }

        book.setPrimaryCategory(primaryCategory);
        book.setShelf(defaultShelf);
    }

    // =========================================================
    // UPDATE PRIMARY CATEGORY + SHELF
    // =========================================================

    private void updatePrimaryCategoryAndShelf(
            Book book,
            Category oldPrimaryCategory,
            Long newPrimaryCategoryId,
            Set<Category> categories) {

        if (newPrimaryCategoryId == null) {

            throw new RuntimeException(
                    "Primary category is required"
            );
        }

        Category newPrimaryCategory =
                categoryRepository.findById(newPrimaryCategoryId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Primary category not found"
                                )
                        );

        boolean belongsToBook =
                categories.stream()
                        .anyMatch(category ->
                                category.getId()
                                        .equals(newPrimaryCategory.getId()));

        if (!belongsToBook) {

            throw new RuntimeException(
                    "Primary category must be one of the book categories"
            );
        }

        BookShelf newDefaultShelf =
                newPrimaryCategory.getDefaultShelf();

        if (newDefaultShelf == null) {

            throw new RuntimeException(
                    "Primary category does not have a default shelf"
            );
        }

        boolean primaryCategoryChanged =
                oldPrimaryCategory == null
                        || !oldPrimaryCategory.getId()
                        .equals(newPrimaryCategory.getId());

        if (primaryCategoryChanged) {

            int allocatedQuantity =
                    bookShelfService.getAllocatedQuantity(
                            book.getId()
                    );

            if (allocatedQuantity > 0) {

                bookShelfService.removeBookQuantity(
                        book,
                        allocatedQuantity
                );
            }

            book.setPrimaryCategory(
                    newPrimaryCategory
            );

            int quantityToAllocate;

            if (allocatedQuantity > 0) {

                quantityToAllocate = allocatedQuantity;

            } else {

                quantityToAllocate =
                        book.getAvailableQuantity() != null
                                ? book.getAvailableQuantity()
                                : 0;
            }

            if (quantityToAllocate > 0) {

                bookShelfService.allocateBook(
                        book,
                        quantityToAllocate
                );
            }

        } else {

            book.setPrimaryCategory(
                    newPrimaryCategory
            );
        }

        book.setShelf(newDefaultShelf);
    }
}
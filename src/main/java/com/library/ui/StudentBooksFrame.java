
package com.library.ui;

import com.library.database.DatabaseConnection;
import com.library.service.BorrowRequestService;
import com.library.service.UserSession;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentBooksFrame extends JFrame {

    // =====================================================
    // COLORS
    // =====================================================

    private static final Color BURGUNDY = new Color(75, 20, 36);
    private static final Color DARK_BURGUNDY = new Color(55, 13, 27);
    private static final Color WINE = new Color(112, 43, 59);
    private static final Color GOLD = new Color(176, 141, 87);
    private static final Color CREAM = new Color(243, 235, 221);
    private static final Color WHITE = new Color(255, 252, 245);
    private static final Color TEXT = new Color(53, 37, 31);
    private static final Color MUTED = new Color(130, 115, 103);
    private static final Color BORDER = new Color(222, 210, 191);
    private static final Color LIGHT_GOLD = new Color(244, 232, 207);
    private static final Color GREEN = new Color(48, 132, 91);
    private static final Color RED = new Color(178, 67, 67);

    // =====================================================
    // COMPONENTS
    // =====================================================

    private JPanel booksContainer;
    private JPanel categoryButtonsPanel;

    private JTextField searchField;

    private final UserSession session;

    private boolean loadingCategories = false;

    private String selectedCategory = "All Categories";

    private final List<String> categories = new ArrayList<>();
    private final List<BookData> allBooks = new ArrayList<>();

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public StudentBooksFrame(UserSession session) {

        if (session == null) {
            throw new SecurityException("Please log in first.");
        }

        session.requireStudent();
        this.session = session;

        setTitle("LibraryLink - Browse Books");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setMinimumSize(new Dimension(1000, 650));
        setSize(1350, 850);
        setLocationRelativeTo(null);

        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        loadCategories();
        loadBooks();
    }

    // =====================================================
    // HEADER
    // =====================================================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout(15, 0));

        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(17, 28, 17, 28));

        JPanel leftPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 14, 0)
        );

        leftPanel.setOpaque(false);

        JButton backButton = new JButton(new ArrowIcon(GOLD));

        backButton.setPreferredSize(new Dimension(42, 42));
        backButton.setToolTipText("Back to Student Dashboard");
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setOpaque(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        backButton.addActionListener(e -> dispose());

        JPanel titlePanel = new JPanel();

        titlePanel.setOpaque(false);
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("Browse Library Books");

        title.setFont(new Font("Georgia", Font.BOLD, 27));
        title.setForeground(GOLD);

        JLabel subtitle = new JLabel(
                "Discover your next favorite book"
        );

        subtitle.setFont(
                new Font("Book Antiqua", Font.ITALIC, 14)
        );

        subtitle.setForeground(WHITE);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        leftPanel.add(backButton);
        leftPanel.add(titlePanel);

        JLabel collectionLabel = new JLabel("LIBRARY COLLECTION");

        collectionLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 11)
        );

        collectionLabel.setForeground(
                new Color(235, 216, 185)
        );

        header.add(leftPanel, BorderLayout.WEST);
        header.add(collectionLabel, BorderLayout.EAST);

        return header;
    }

    // =====================================================
    // MAIN CONTENT
    // =====================================================

    private JPanel createMainContent() {

        JPanel main = new JPanel(new BorderLayout(0, 11));

        main.setBackground(CREAM);
        main.setBorder(new EmptyBorder(13, 25, 16, 25));

        main.add(createSearchPanel(), BorderLayout.NORTH);
        main.add(createCatalogPanel(), BorderLayout.CENTER);

        return main;
    }

    // =====================================================
    // COMPACT SEARCH AND CATEGORY PANEL
    // =====================================================

    private JPanel createSearchPanel() {

        RoundedPanel outerPanel = new RoundedPanel(WHITE, 17);

        outerPanel.setLayout(new BorderLayout(0, 12));
        outerPanel.setBorder(new EmptyBorder(14, 18, 14, 18));

        // SEARCH ROW

        JPanel searchRow = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );

        searchRow.setOpaque(false);

        // SHORTER SEARCH INPUT

        RoundedPanel searchInputPanel = new RoundedPanel(CREAM, 13);

        searchInputPanel.setLayout(new BorderLayout(10, 0));
        searchInputPanel.setBorder(new EmptyBorder(0, 14, 0, 14));
        searchInputPanel.setPreferredSize(new Dimension(390, 43));
        searchInputPanel.setMinimumSize(new Dimension(250, 43));

        JLabel searchIcon = new JLabel(new SearchIcon(MUTED));

        searchField = new JTextField();

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setForeground(TEXT);
        searchField.setCaretColor(BURGUNDY);
        searchField.setOpaque(false);
        searchField.setBorder(null);

        searchField.setToolTipText(
                "Enter a book title, author, category, or ISBN"
        );

        searchField.setPreferredSize(new Dimension(300, 35));

        searchInputPanel.add(searchIcon, BorderLayout.WEST);
        searchInputPanel.add(searchField, BorderLayout.CENTER);

        // SEARCH BUTTON WITH WORDS

        RoundedButton searchButton = new RoundedButton(
                "Search for Books",
                BURGUNDY,
                WHITE,
                13
        );

        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchButton.setPreferredSize(new Dimension(145, 43));
        searchButton.setMinimumSize(new Dimension(145, 43));
        searchButton.setMaximumSize(new Dimension(145, 43));
        searchButton.setToolTipText("Search books");

        searchButton.addActionListener(e -> loadBooks());

        // ENTER KEY SEARCH

        searchField.addActionListener(e -> loadBooks());

        searchRow.add(searchInputPanel);
        searchRow.add(searchButton);

        // CATEGORY SECTION

        JPanel categorySection = new JPanel();

        categorySection.setOpaque(false);
        categorySection.setLayout(
                new BoxLayout(categorySection, BoxLayout.Y_AXIS)
        );

        JPanel categoryHeading = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 7, 0)
        );

        categoryHeading.setOpaque(false);
        categoryHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel categoryIcon = new JLabel(new StackIcon(GOLD));

        JLabel categoryLabel = new JLabel("BROWSE BY CATEGORY");

        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        categoryLabel.setForeground(MUTED);

        categoryHeading.add(categoryIcon);
        categoryHeading.add(categoryLabel);

        categoryButtonsPanel = new JPanel(
                new WrapLayout(FlowLayout.LEFT, 8, 7)
        );

        categoryButtonsPanel.setOpaque(false);
        categoryButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        categorySection.add(categoryHeading);
        categorySection.add(Box.createVerticalStrut(8));
        categorySection.add(categoryButtonsPanel);

        outerPanel.add(searchRow, BorderLayout.NORTH);
        outerPanel.add(categorySection, BorderLayout.CENTER);

        outerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return outerPanel;
    }

    // =====================================================
    // LOAD CATEGORIES
    // =====================================================

    private void loadCategories() {

        if (categoryButtonsPanel == null) {
            return;
        }

        loadingCategories = true;

        categories.clear();
        categories.add("All Categories");

        String sql = """
                SELECT DISTINCT category
                FROM books
                WHERE category IS NOT NULL
                AND TRIM(category) <> ''
                ORDER BY category ASC
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                String category = result.getString("category");

                if (category != null && !category.isBlank()) {

                    String cleanCategory = category.trim();

                    if (!categories.contains(cleanCategory)) {
                        categories.add(cleanCategory);
                    }
                }
            }

        } catch (SQLException e) {

            showDatabaseError(
                    "Unable to load book categories.",
                    e
            );

        } finally {

            loadingCategories = false;
        }

        if (!categories.contains(selectedCategory)) {
            selectedCategory = "All Categories";
        }

        displayCategoryButtons();
    }

    // =====================================================
    // DISPLAY CATEGORY BUTTONS
    // =====================================================

    private void displayCategoryButtons() {

        if (categoryButtonsPanel == null) {
            return;
        }

        categoryButtonsPanel.removeAll();

        for (String category : categories) {

            boolean selected = category.equals(selectedCategory);

            RoundedButton categoryButton = new RoundedButton(
                    category,
                    selected ? BURGUNDY : CREAM,
                    selected ? WHITE : TEXT,
                    16
            );

            categoryButton.setFont(
                    new Font("Segoe UI", Font.BOLD, 10)
            );

            categoryButton.setBorder(
                    new EmptyBorder(6, 12, 6, 12)
            );

            categoryButton.setToolTipText(
                    "Show " + category + " books"
            );

            categoryButton.addActionListener(e -> {

                if (loadingCategories) {
                    return;
                }

                selectedCategory = category;

                displayCategoryButtons();
                loadBooks();
            });

            categoryButtonsPanel.add(categoryButton);
        }

        categoryButtonsPanel.revalidate();
        categoryButtonsPanel.repaint();
    }

    // =====================================================
    // BOOK CATALOG PANEL
    // =====================================================

    private JPanel createCatalogPanel() {

        RoundedPanel panel = new RoundedPanel(WHITE, 18);

        panel.setLayout(new BorderLayout(0, 12));

        panel.setBorder(
                new EmptyBorder(16, 19, 15, 19)
        );

        JPanel headingPanel = new JPanel(new BorderLayout());

        headingPanel.setOpaque(false);

        JPanel headingText = new JPanel();

        headingText.setOpaque(false);
        headingText.setLayout(
                new BoxLayout(headingText, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("Explore Our Collection");

        title.setFont(
                new Font("Georgia", Font.BOLD, 21)
        );

        title.setForeground(BURGUNDY);

        JLabel subtitle = new JLabel(
                "Select a book to view its complete information."
        );

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        subtitle.setForeground(MUTED);

        headingText.add(title);
        headingText.add(Box.createVerticalStrut(4));
        headingText.add(subtitle);

        JLabel countLabel = new JLabel("BOOK CATALOG");

        countLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 10)
        );

        countLabel.setForeground(GOLD);

        headingPanel.add(headingText, BorderLayout.WEST);
        headingPanel.add(countLabel, BorderLayout.EAST);

        // BOOK CARDS

        booksContainer = new JPanel(
                new WrapLayout(FlowLayout.LEFT, 17, 17)
        );

        booksContainer.setBackground(WHITE);

        booksContainer.setBorder(
                new EmptyBorder(5, 3, 10, 3)
        );

        JScrollPane scrollPane = new JScrollPane(booksContainer);

        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(WHITE);

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        panel.add(headingPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // =====================================================
    // LOAD BOOKS
    // =====================================================

    private void loadBooks() {

        if (booksContainer == null) {
            return;
        }

        booksContainer.removeAll();
        allBooks.clear();

        String search = searchField == null
                ? ""
                : searchField.getText().trim();

        String sql = """
                SELECT *
                FROM books
                WHERE (
                    title LIKE ?
                    OR author LIKE ?
                    OR category LIKE ?
                    OR isbn LIKE ?
                )
                AND (? = 'All Categories' OR category = ?)
                ORDER BY title ASC
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            String keyword = "%" + search + "%";

            statement.setString(1, keyword);
            statement.setString(2, keyword);
            statement.setString(3, keyword);
            statement.setString(4, keyword);
            statement.setString(5, selectedCategory);
            statement.setString(6, selectedCategory);

            try (ResultSet result = statement.executeQuery()) {

                ResultSetMetaData metadata = result.getMetaData();

                boolean hasCoverImage = hasColumn(
                        metadata,
                        "cover_image"
                );

                boolean hasDescription = hasColumn(
                        metadata,
                        "description"
                );

                boolean hasTotalCopies = hasColumn(
                        metadata,
                        "total_copies"
                );

                while (result.next()) {

                    String title = result.getString("title");

                    String coverImage = null;

                    if (hasCoverImage) {
                        coverImage = result.getString("cover_image");
                    }

                    if (coverImage == null || coverImage.isBlank()) {
                        coverImage = findCoverImage(title);
                    }

                    String description = hasDescription
                            ? result.getString("description")
                            : "";

                    int availableCopies = result.getInt(
                            "available_copies"
                    );

                    int totalCopies = hasTotalCopies
                            ? result.getInt("total_copies")
                            : availableCopies;

                    BookData book = new BookData(
                            result.getInt("book_id"),
                            safeValue(result.getString("isbn")),
                            safeValue(title),
                            safeValue(result.getString("author")),
                            safeValue(result.getString("category")),
                            safeValue(result.getString("publisher")),
                            safeValue(result.getObject("publication_year")),
                            totalCopies,
                            availableCopies,
                            safeValue(result.getString("shelf_location")),
                            safeValue(coverImage),
                            safeValue(description)
                    );

                    allBooks.add(book);
                }
            }

            displayBookCards();

        } catch (SQLException e) {

            showDatabaseError(
                    "Unable to load books.",
                    e
            );
        }
    }

    // =====================================================
    // CHECK DATABASE COLUMNS
    // =====================================================

    private boolean hasColumn(
            ResultSetMetaData metadata,
            String columnName
    ) throws SQLException {

        for (int i = 1; i <= metadata.getColumnCount(); i++) {

            String name = metadata.getColumnLabel(i);

            if (name != null
                    && name.equalsIgnoreCase(columnName)) {
                return true;
            }
        }

        return false;
    }

    // =====================================================
    // DISPLAY BOOK CARDS
    // =====================================================

    private void displayBookCards() {

        booksContainer.removeAll();

        if (allBooks.isEmpty()) {

            showEmptyBooksMessage();

        } else {

            for (BookData book : allBooks) {
                booksContainer.add(createBookCard(book));
            }
        }

        booksContainer.revalidate();
        booksContainer.repaint();
    }

    // =====================================================
    // CREATE BOOK CARD
    // =====================================================

    private JPanel createBookCard(BookData book) {

        RoundedPanel card = new RoundedPanel(WHITE, 16);

        card.setLayout(new BorderLayout(0, 0));

        card.setPreferredSize(new Dimension(210, 330));
        card.setMinimumSize(new Dimension(210, 330));
        card.setMaximumSize(new Dimension(210, 330));

        card.setBorder(new EmptyBorder(10, 10, 12, 10));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel coverPanel = new JPanel(new BorderLayout());

        coverPanel.setOpaque(false);
        coverPanel.setPreferredSize(new Dimension(190, 215));

        JLabel cover = createCoverLabel(
                book.coverImage,
                190,
                215
        );

        coverPanel.add(cover, BorderLayout.CENTER);

        JPanel information = new JPanel();

        information.setOpaque(false);
        information.setLayout(
                new BoxLayout(information, BoxLayout.Y_AXIS)
        );

        information.setBorder(
                new EmptyBorder(10, 3, 0, 3)
        );

        JLabel title = new JLabel(
                "<html><div style='width:175px;'>"
                        + escapeHtml(book.title)
                        + "</div></html>"
        );

        title.setFont(
                new Font("Georgia", Font.BOLD, 13)
        );

        title.setForeground(BURGUNDY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel author = new JLabel(
                "<html><div style='width:175px;'>"
                        + escapeHtml(book.author)
                        + "</div></html>"
        );

        author.setFont(
                new Font("Book Antiqua", Font.PLAIN, 11)
        );

        author.setForeground(MUTED);
        author.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel availability = new JLabel(
                book.availableCopies > 0
                        ? book.availableCopies + " copies available"
                        : "Currently unavailable"
        );

        availability.setFont(
                new Font("Segoe UI", Font.BOLD, 10)
        );

        availability.setForeground(
                book.availableCopies > 0 ? GREEN : RED
        );

        availability.setAlignmentX(Component.LEFT_ALIGNMENT);

        information.add(title);
        information.add(Box.createVerticalStrut(5));
        information.add(author);
        information.add(Box.createVerticalStrut(8));
        information.add(availability);

        card.add(coverPanel, BorderLayout.CENTER);
        card.add(information, BorderLayout.SOUTH);

        attachClickRecursively(
                card,
                () -> showBookDetails(book)
        );

        return card;
    }

    // =====================================================
    // BOOK DETAILS DIALOG
    // =====================================================

    private void showBookDetails(BookData book) {

        JDialog dialog = new JDialog(
                this,
                "Book Details",
                true
        );

        dialog.setDefaultCloseOperation(
                JDialog.DISPOSE_ON_CLOSE
        );

        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(CREAM);

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(17, 23, 17, 20));

        JLabel heading = new JLabel("BOOK DETAILS");

        heading.setFont(
                new Font("Georgia", Font.BOLD, 20)
        );

        heading.setForeground(GOLD);

        JButton closeButton = new JButton(new CloseIcon(WHITE));

        closeButton.setPreferredSize(new Dimension(35, 35));
        closeButton.setToolTipText("Close");
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setOpaque(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        closeButton.addActionListener(e -> dialog.dispose());

        header.add(heading, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(22, 0));

        content.setBackground(CREAM);
        content.setBorder(new EmptyBorder(22, 25, 22, 25));

        // LEFT COVER

        RoundedPanel coverPanel = new RoundedPanel(WHITE, 16);

        coverPanel.setLayout(new BorderLayout(0, 12));
        coverPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        coverPanel.setPreferredSize(new Dimension(285, 410));

        JLabel coverHeading = new JLabel(
                "BOOK COVER",
                SwingConstants.CENTER
        );

        coverHeading.setFont(
                new Font("Segoe UI", Font.BOLD, 11)
        );

        coverHeading.setForeground(GOLD);

        JLabel cover = createCoverLabel(
                book.coverImage,
                255,
                350
        );

        coverPanel.add(coverHeading, BorderLayout.NORTH);
        coverPanel.add(cover, BorderLayout.CENTER);

        // RIGHT DETAILS

        JPanel titlePanel = new JPanel();

        titlePanel.setOpaque(false);
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel(
                "<html><div style='width:420px;'>"
                        + escapeHtml(book.title)
                        + "</div></html>"
        );

        title.setFont(
                new Font("Georgia", Font.BOLD, 24)
        );

        title.setForeground(BURGUNDY);

        JLabel author = new JLabel(
                "Written by " + safeText(book.author)
        );

        author.setFont(
                new Font("Book Antiqua", Font.PLAIN, 15)
        );

        author.setForeground(MUTED);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(8));
        titlePanel.add(author);

        RoundedPanel informationPanel = new RoundedPanel(
                WHITE,
                14
        );

        informationPanel.setLayout(
                new GridLayout(0, 1, 0, 5)
        );

        informationPanel.setBorder(
                new EmptyBorder(17, 18, 17, 18)
        );

        addDetailRow(informationPanel, "BOOK ID",
                String.valueOf(book.bookId));

        addDetailRow(informationPanel, "ISBN", book.isbn);
        addDetailRow(informationPanel, "CATEGORY", book.category);
        addDetailRow(informationPanel, "PUBLISHER", book.publisher);

        addDetailRow(informationPanel, "PUBLICATION YEAR",
                book.publicationYear);

        addDetailRow(informationPanel, "TOTAL COPIES",
                String.valueOf(book.totalCopies));

        addDetailRow(informationPanel, "AVAILABLE COPIES",
                String.valueOf(book.availableCopies));

        addDetailRow(informationPanel, "SHELF LOCATION",
                book.shelfLocation);

        // DESCRIPTION

        RoundedPanel descriptionPanel = new RoundedPanel(
                WHITE,
                14
        );

        descriptionPanel.setLayout(
                new BorderLayout(0, 9)
        );

        descriptionPanel.setBorder(
                new EmptyBorder(14, 17, 14, 17)
        );

        JLabel descriptionHeading = new JLabel(
                "ABOUT THIS BOOK"
        );

        descriptionHeading.setFont(
                new Font("Georgia", Font.BOLD, 13)
        );

        descriptionHeading.setForeground(BURGUNDY);

        JTextArea description = new JTextArea(
                book.description.isBlank()
                        ? "No description has been added for this book."
                        : book.description
        );

        description.setFont(
                new Font("Book Antiqua", Font.PLAIN, 13)
        );

        description.setForeground(TEXT);
        description.setBackground(WHITE);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setEditable(false);
        description.setFocusable(false);
        description.setBorder(null);

        JScrollPane descriptionScroll = new JScrollPane(description);

        descriptionScroll.setBorder(null);
        descriptionScroll.setPreferredSize(new Dimension(400, 90));

        descriptionScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        descriptionPanel.add(
                descriptionHeading,
                BorderLayout.NORTH
        );

        descriptionPanel.add(
                descriptionScroll,
                BorderLayout.CENTER
        );

        JPanel rightContent = new JPanel(
                new BorderLayout(0, 13)
        );

        rightContent.setOpaque(false);

        rightContent.add(titlePanel, BorderLayout.NORTH);

        JPanel detailsAndDescription = new JPanel(
                new BorderLayout(0, 12)
        );

        detailsAndDescription.setOpaque(false);

        detailsAndDescription.add(
                informationPanel,
                BorderLayout.NORTH
        );

        detailsAndDescription.add(
                descriptionPanel,
                BorderLayout.CENTER
        );

        rightContent.add(
                detailsAndDescription,
                BorderLayout.CENTER
        );

        content.add(coverPanel, BorderLayout.WEST);
        content.add(rightContent, BorderLayout.CENTER);

        // FOOTER

        JPanel footer = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 12)
        );

        footer.setBackground(WHITE);
        footer.setBorder(new EmptyBorder(5, 20, 5, 20));

        RoundedButton closeFooterButton = new RoundedButton(
                "Close",
                LIGHT_GOLD,
                BURGUNDY,
                12
        );

        closeFooterButton.setPreferredSize(new Dimension(105, 40));
        closeFooterButton.addActionListener(e -> dialog.dispose());

        RoundedButton requestButton = new RoundedButton(
                "Request to Borrow",
                BURGUNDY,
                WHITE,
                12
        );

        requestButton.setPreferredSize(new Dimension(175, 40));

        requestButton.addActionListener(e -> {

            if (submitBorrowRequest(book)) {
                dialog.dispose();
            }
        });

        footer.add(closeFooterButton);
        footer.add(requestButton);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(content, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setSize(950, 680);
        dialog.setMinimumSize(new Dimension(800, 600));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // =====================================================
    // DETAILS ROW
    // =====================================================

    private void addDetailRow(
            JPanel panel,
            String labelText,
            String valueText
    ) {

        JPanel row = new JPanel(new BorderLayout(10, 0));

        row.setOpaque(false);

        JLabel label = new JLabel(labelText);

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 10)
        );

        label.setForeground(MUTED);
        label.setPreferredSize(new Dimension(145, 23));

        JLabel value = new JLabel(safeText(valueText));

        value.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        value.setForeground(TEXT);

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);

        panel.add(row);
    }

    // =====================================================
    // BORROW REQUEST
    // =====================================================

    private boolean submitBorrowRequest(BookData book) {

        session.requireStudent();

        if (book.availableCopies <= 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Sorry, this book is currently unavailable for borrowing.",
                    "Not Available",
                    JOptionPane.WARNING_MESSAGE
            );

            return false;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Do you want to submit a borrow request for:\n\n"
                        + book.title + "?",
                "Confirm Borrow Request",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return false;
        }

        try {

            BorrowRequestService service =
                    new BorrowRequestService();

            service.submitRequest(session, book.bookId);

            JOptionPane.showMessageDialog(
                    this,
                    "Your borrow request has been submitted successfully.",
                    "Request Submitted",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadBooks();

            return true;

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to submit your borrow request:\n"
                            + e.getMessage(),
                    "Request Failed",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();

            return false;
        }
    }

    // =====================================================
    // BOOK COVER
    // =====================================================

    private JLabel createCoverLabel(
            String imagePath,
            int width,
            int height
    ) {

        JLabel label = new JLabel();

        label.setOpaque(true);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBackground(LIGHT_GOLD);
        label.setForeground(BURGUNDY);

        label.setPreferredSize(new Dimension(width, height));

        label.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        if (imagePath != null && !imagePath.isBlank()) {

            try {

                File imageFile = new File(imagePath);

                if (!imageFile.isAbsolute()) {

                    File savedCover = new File(
                            System.getProperty("user.home")
                                    + File.separator
                                    + "LibraryLink"
                                    + File.separator
                                    + "book_covers",
                            imagePath
                    );

                    if (savedCover.exists()) {
                        imageFile = savedCover;
                    }
                }

                BufferedImage image = null;

                if (imageFile.exists()) {

                    image = ImageIO.read(imageFile);

                } else {

                    URL resource = getClass().getResource(
                            imagePath.startsWith("/")
                                    ? imagePath
                                    : "/" + imagePath
                    );

                    if (resource != null) {
                        image = ImageIO.read(resource);
                    }
                }

                if (image != null) {

                    Image scaled = getScaledImage(
                            image,
                            width - 12,
                            height - 12
                    );

                    label.setIcon(new ImageIcon(scaled));
                    label.setText("");

                    return label;
                }

            } catch (Exception e) {

                System.err.println(
                        "Unable to load book cover: "
                                + e.getMessage()
                );
            }
        }

        label.setLayout(new BorderLayout());

        JLabel placeholder = new JLabel(
                "<html><center>"
                        + "<span style='font-size:30px;'>&#128218;</span>"
                        + "<br><b>LIBRARYLINK</b>"
                        + "<br><small>No cover image</small>"
                        + "</center></html>",
                SwingConstants.CENTER
        );

        placeholder.setFont(
                new Font("Book Antiqua", Font.PLAIN, 11)
        );

        placeholder.setForeground(MUTED);

        label.add(placeholder, BorderLayout.CENTER);

        return label;
    }

    // =====================================================
    // IMAGE SCALING
    // =====================================================

    private Image getScaledImage(
            BufferedImage source,
            int maxWidth,
            int maxHeight
    ) {

        double widthScale =
                (double) maxWidth / source.getWidth();

        double heightScale =
                (double) maxHeight / source.getHeight();

        double scale = Math.min(widthScale, heightScale);

        int newWidth = Math.max(
                1,
                (int) (source.getWidth() * scale)
        );

        int newHeight = Math.max(
                1,
                (int) (source.getHeight() * scale)
        );

        return source.getScaledInstance(
                newWidth,
                newHeight,
                Image.SCALE_SMOOTH
        );
    }

    // =====================================================
    // FIND COVER BY BOOK TITLE
    // =====================================================

    private String findCoverImage(String bookTitle) {

        if (bookTitle == null || bookTitle.isBlank()) {
            return null;
        }

        File imagesFolder = new File(
                System.getProperty("user.dir"),
                "src/main/resources/images"
        );

        if (!imagesFolder.exists()
                || !imagesFolder.isDirectory()) {
            return null;
        }

        String targetName = bookTitle
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");

        File[] files = imagesFolder.listFiles();

        if (files == null) {
            return null;
        }

        for (File file : files) {

            if (!file.isFile()) {
                continue;
            }

            String fileName = file.getName();

            if (!fileName.matches(
                    "(?i).*\\.(jpg|jpeg|png|gif|bmp)$"
            )) {
                continue;
            }

            String imageName = fileName
                    .substring(0, fileName.lastIndexOf('.'))
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]", "");

            if (imageName.equals(targetName)) {
                return file.getAbsolutePath();
            }
        }

        return null;
    }

    // =====================================================
    // EMPTY BOOK MESSAGE
    // =====================================================

    private void showEmptyBooksMessage() {

        RoundedPanel emptyPanel = new RoundedPanel(CREAM, 16);

        emptyPanel.setLayout(
                new BoxLayout(emptyPanel, BoxLayout.Y_AXIS)
        );

        emptyPanel.setPreferredSize(new Dimension(500, 180));
        emptyPanel.setBorder(new EmptyBorder(30, 25, 30, 25));

        JLabel icon = new JLabel(new BookIcon(GOLD));

        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("No Books Found");

        title.setFont(
                new Font("Georgia", Font.BOLD, 19)
        );

        title.setForeground(BURGUNDY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel message = new JLabel(
                "Try another search term or select a different category."
        );

        message.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        message.setForeground(MUTED);
        message.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyPanel.add(icon);
        emptyPanel.add(Box.createVerticalStrut(10));
        emptyPanel.add(title);
        emptyPanel.add(Box.createVerticalStrut(7));
        emptyPanel.add(message);

        booksContainer.add(emptyPanel);
    }

    // =====================================================
    // RECURSIVE CLICK SUPPORT
    // =====================================================

    private void attachClickRecursively(
            Component component,
            Runnable action
    ) {

        component.setCursor(new Cursor(Cursor.HAND_CURSOR));

        component.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (SwingUtilities.isLeftMouseButton(e)) {
                    action.run();
                }
            }
        });

        if (component instanceof Container container) {

            for (Component child : container.getComponents()) {

                attachClickRecursively(child, action);
            }
        }
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private String safeValue(Object value) {

        return value == null
                ? ""
                : String.valueOf(value).trim();
    }

    private String safeText(String value) {

        return value == null || value.isBlank()
                ? "Not specified"
                : value;
    }

    private String escapeHtml(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void showDatabaseError(
            String message,
            SQLException exception
    ) {

        JOptionPane.showMessageDialog(
                this,
                message + "\n" + exception.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );

        exception.printStackTrace();
    }

    // =====================================================
    // BOOK DATA
    // =====================================================

    private static class BookData {

        private final int bookId;
        private final String isbn;
        private final String title;
        private final String author;
        private final String category;
        private final String publisher;
        private final String publicationYear;
        private final int totalCopies;
        private final int availableCopies;
        private final String shelfLocation;
        private final String coverImage;
        private final String description;

        private BookData(
                int bookId,
                String isbn,
                String title,
                String author,
                String category,
                String publisher,
                String publicationYear,
                int totalCopies,
                int availableCopies,
                String shelfLocation,
                String coverImage,
                String description
        ) {

            this.bookId = bookId;
            this.isbn = isbn;
            this.title = title;
            this.author = author;
            this.category = category;
            this.publisher = publisher;
            this.publicationYear = publicationYear;
            this.totalCopies = totalCopies;
            this.availableCopies = availableCopies;
            this.shelfLocation = shelfLocation;
            this.coverImage = coverImage;
            this.description = description;
        }
    }

    // =====================================================
    // ROUNDED PANEL
    // =====================================================

    private static class RoundedPanel extends JPanel {

        private final Color backgroundColor;
        private final int radius;

        public RoundedPanel(Color backgroundColor, int radius) {

            this.backgroundColor = backgroundColor;
            this.radius = radius;

            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(new Color(70, 45, 30, 17));

            g2.fillRoundRect(
                    3, 4,
                    getWidth() - 6,
                    getHeight() - 6,
                    radius,
                    radius
            );

            g2.setColor(backgroundColor);

            g2.fillRoundRect(
                    0, 0,
                    getWidth() - 4,
                    getHeight() - 5,
                    radius,
                    radius
            );

            g2.setColor(new Color(222, 210, 191, 180));

            g2.setStroke(new BasicStroke(1f));

            g2.drawRoundRect(
                    0, 0,
                    getWidth() - 4,
                    getHeight() - 5,
                    radius,
                    radius
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // =====================================================
    // ROUNDED BUTTON
    // =====================================================

    private static class RoundedButton extends JButton {

        private final Color buttonColor;
        private final Color textColor;
        private final int radius;

        public RoundedButton(
                String text,
                Color buttonColor,
                Color textColor,
                int radius
        ) {

            super(text);

            this.buttonColor = buttonColor;
            this.textColor = textColor;
            this.radius = radius;

            setFont(new Font("Segoe UI", Font.BOLD, 12));

            setForeground(textColor);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);

            setBorder(new EmptyBorder(8, 13, 8, 13));

            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color fill = buttonColor;

            if (getModel().isPressed()) {

                fill = buttonColor.darker();

            } else if (getModel().isRollover()) {

                fill = buttonColor.brighter();
            }

            g2.setColor(fill);

            g2.fillRoundRect(
                    0, 0,
                    getWidth(),
                    getHeight(),
                    radius,
                    radius
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // =====================================================
    // RESPONSIVE WRAP LAYOUT
    // =====================================================

    private static class WrapLayout extends FlowLayout {

        public WrapLayout(
                int alignment,
                int horizontalGap,
                int verticalGap
        ) {

            super(alignment, horizontalGap, verticalGap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {

            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {

            Dimension size = layoutSize(target, false);

            size.width -= getHgap() + 1;

            return size;
        }

        private Dimension layoutSize(
                Container target,
                boolean preferred
        ) {

            synchronized (target.getTreeLock()) {

                int targetWidth = target.getSize().width;

                if (targetWidth == 0) {

                    Container parent = target.getParent();

                    if (parent != null) {
                        targetWidth = parent.getSize().width;
                    }
                }

                if (targetWidth == 0) {
                    targetWidth = Integer.MAX_VALUE;
                }

                Insets insets = target.getInsets();

                int horizontalInsets =
                        insets.left + insets.right + getHgap() * 2;

                int maxWidth = targetWidth - horizontalInsets;

                Dimension dimension = new Dimension(0, 0);

                int rowWidth = 0;
                int rowHeight = 0;

                int componentCount = target.getComponentCount();

                for (int i = 0; i < componentCount; i++) {

                    Component component = target.getComponent(i);

                    if (!component.isVisible()) {
                        continue;
                    }

                    Dimension size = preferred
                            ? component.getPreferredSize()
                            : component.getMinimumSize();

                    if (rowWidth + size.width > maxWidth
                            && rowWidth > 0) {

                        dimension.width = Math.max(
                                dimension.width,
                                rowWidth
                        );

                        dimension.height += rowHeight + getVgap();

                        rowWidth = 0;
                        rowHeight = 0;
                    }

                    if (rowWidth > 0) {
                        rowWidth += getHgap();
                    }

                    rowWidth += size.width;

                    rowHeight = Math.max(rowHeight, size.height);
                }

                dimension.width = Math.max(
                        dimension.width,
                        rowWidth
                );

                dimension.height += rowHeight;

                dimension.width += horizontalInsets;

                dimension.height +=
                        insets.top + insets.bottom + getVgap() * 2;

                return dimension;
            }
        }
    }

    // =====================================================
    // SEARCH ICON
    // =====================================================

    private static class SearchIcon implements Icon {

        private final Color color;

        public SearchIcon(Color color) {
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return 18;
        }

        @Override
        public int getIconHeight() {
            return 18;
        }

        @Override
        public void paintIcon(
                Component component,
                Graphics graphics,
                int x,
                int y
        ) {

            Graphics2D g2 = (Graphics2D) graphics.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(color);

            g2.setStroke(
                    new BasicStroke(
                            2f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            // Magnifying glass

            g2.drawOval(
                    x + 2,
                    y + 2,
                    10,
                    10
            );

            // Handle

            g2.drawLine(
                    x + 11,
                    y + 11,
                    x + 16,
                    y + 16
            );

            g2.dispose();
        }
    }

    // =====================================================
    // BOOK ICON
    // =====================================================

    private static class BookIcon implements Icon {

        private final Color color;

        public BookIcon(Color color) {
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return 38;
        }

        @Override
        public int getIconHeight() {
            return 38;
        }

        @Override
        public void paintIcon(
                Component c,
                Graphics g,
                int x,
                int y
        ) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(color);

            g2.setStroke(
                    new BasicStroke(
                            2f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            g2.drawRoundRect(x + 3, y + 5, 15, 27, 3, 3);
            g2.drawRoundRect(x + 19, y + 5, 15, 27, 3, 3);
            g2.drawLine(x + 18, y + 7, x + 18, y + 32);
            g2.drawLine(x + 7, y + 12, x + 14, y + 12);
            g2.drawLine(x + 23, y + 12, x + 30, y + 12);

            g2.dispose();
        }
    }

    // =====================================================
    // CATEGORY / STACK ICON
    // =====================================================

    private static class StackIcon implements Icon {

        private final Color color;

        public StackIcon(Color color) {
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return 25;
        }

        @Override
        public int getIconHeight() {
            return 25;
        }

        @Override
        public void paintIcon(
                Component c,
                Graphics g,
                int x,
                int y
        ) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(color);

            g2.setStroke(
                    new BasicStroke(
                            1.8f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            g2.drawRoundRect(x + 3, y + 3, 18, 5, 3, 3);
            g2.drawRoundRect(x + 3, y + 10, 18, 5, 3, 3);
            g2.drawRoundRect(x + 3, y + 17, 18, 5, 3, 3);

            g2.dispose();
        }
    }

    // =====================================================
    // TRANSPARENT BACK ARROW ICON
    // =====================================================

    private static class ArrowIcon implements Icon {

        private final Color color;

        public ArrowIcon(Color color) {
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return 25;
        }

        @Override
        public int getIconHeight() {
            return 25;
        }

        @Override
        public void paintIcon(
                Component c,
                Graphics g,
                int x,
                int y
        ) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(color);

            g2.setStroke(
                    new BasicStroke(
                            2.3f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            g2.drawLine(x + 20, y + 12, x + 5, y + 12);
            g2.drawLine(x + 5, y + 12, x + 12, y + 5);
            g2.drawLine(x + 5, y + 12, x + 12, y + 19);

            g2.dispose();
        }
    }

    // =====================================================
    // CLOSE ICON
    // =====================================================

    private static class CloseIcon implements Icon {

        private final Color color;

        public CloseIcon(Color color) {
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }

        @Override
        public void paintIcon(
                Component c,
                Graphics g,
                int x,
                int y
        ) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(color);

            g2.setStroke(
                    new BasicStroke(
                            2f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            g2.drawLine(x + 4, y + 4, x + 16, y + 16);
            g2.drawLine(x + 16, y + 4, x + 4, y + 16);

            g2.dispose();
        }
    }
}

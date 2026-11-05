
package com.library.ui;

import com.library.service.BookService;
import com.library.service.BookService.Book;
import com.library.database.DatabaseConnection;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.sql.*;
import java.util.List;

public class ManageBooksFrame extends JFrame {

    private final Color BURGUNDY = new Color(75, 20, 36);
    private final Color DARK_BURGUNDY = new Color(55, 13, 27);
    private final Color GOLD = new Color(176, 141, 87);
    private final Color CREAM = new Color(243, 235, 221);
    private final Color WHITE = new Color(255, 252, 245);
    private final Color TEXT_DARK = new Color(53, 37, 31);
    private final Color TEXT_GRAY = new Color(130, 115, 103);
    private final Color BORDER = new Color(222, 210, 191);
    private final Color SOFT_GOLD = new Color(244, 232, 207);

    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    private JLabel titlesValue;
    private JLabel copiesValue;
    private JLabel availableValue;
    private JLabel borrowedValue;

    public ManageBooksFrame() {

        setTitle("LibraryLink - Manage Books");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        setSize(1350, 780);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        try {
            ensureBookDetailColumns();
        } catch (SQLException e) {
            showError("Unable to prepare the book details fields.", e);
        }

        loadBooks();
    }

    // HEADER

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout(15, 10));
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(22, 30, 22, 30));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Manage Books");
        title.setFont(new Font("Georgia", Font.BOLD, 27));
        title.setForeground(GOLD);

        JLabel subtitle = new JLabel(
                "Manage your library collection and inventory"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(243, 235, 221));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        RoundedButton closeButton = new RoundedButton(
                "Close", new Color(92, 28, 46), WHITE
        );

        closeButton.addActionListener(e -> dispose());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);

        return header;
    }

    // MAIN CONTENT

    private JPanel createContent() {

        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(CREAM);
        content.setBorder(new EmptyBorder(25, 30, 30, 30));

        // SUMMARY CARDS

        JPanel summaryPanel = new JPanel(
                new GridLayout(1, 4, 17, 17)
        );
        summaryPanel.setOpaque(false);

        summaryPanel.add(createSummaryCard("TOTAL TITLES", "0", 1));
        summaryPanel.add(createSummaryCard("TOTAL COPIES", "0", 2));
        summaryPanel.add(createSummaryCard("AVAILABLE COPIES", "0", 3));
        summaryPanel.add(createSummaryCard("BORROWED COPIES", "0", 4));

        // TOOLBAR

        JPanel toolbar = new JPanel(new BorderLayout(10, 12));
        toolbar.setOpaque(false);

        JPanel searchPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 5)
        );
        searchPanel.setOpaque(false);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(270, 42));
        searchField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(7, 12, 7, 12)
                )
        );
        searchField.setBackground(WHITE);
        searchField.setForeground(TEXT_DARK);

        searchField.putClientProperty(
                "JTextField.placeholderText",
                "Search title, author, ISBN..."
        );

        RoundedButton searchButton = new RoundedButton(
                "Search", BURGUNDY, WHITE
        );

        RoundedButton clearButton = new RoundedButton(
                "Clear", new Color(235, 230, 224), TEXT_DARK
        );

        RoundedButton refreshButton = new RoundedButton(
                "Refresh", WHITE, BURGUNDY
        );

        searchButton.addActionListener(e -> searchBooks());
        searchField.addActionListener(e -> searchBooks());

        clearButton.addActionListener(e -> {
            searchField.setText("");
            loadBooks();
        });

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadBooks();
        });

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);
        searchPanel.add(refreshButton);

        JPanel actionPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 5)
        );
        actionPanel.setOpaque(false);

        RoundedButton addButton = new RoundedButton(
                "+ Add Book", BURGUNDY, WHITE
        );

        RoundedButton editButton = new RoundedButton(
                "Edit", GOLD, DARK_BURGUNDY
        );

        RoundedButton detailsButton = new RoundedButton(
                "Details", WHITE, BURGUNDY
        );

        RoundedButton deleteButton = new RoundedButton(
                "Delete", new Color(170, 55, 65), WHITE
        );

        addButton.addActionListener(e -> showAddBookDialog());
        editButton.addActionListener(e -> editSelectedBook());
        detailsButton.addActionListener(e -> showBookDetails());
        deleteButton.addActionListener(e -> deleteSelectedBook());

        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(detailsButton);
        actionPanel.add(deleteButton);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(actionPanel, BorderLayout.EAST);

        JPanel topSection = new JPanel(new BorderLayout(0, 15));
        topSection.setOpaque(false);
        topSection.add(summaryPanel, BorderLayout.NORTH);
        topSection.add(toolbar, BorderLayout.CENTER);

        // BOOK TABLE

        String[] columns = {
                "ID", "ISBN", "Title", "Author", "Category",
                "Publisher", "Year", "Total Copies",
                "Available", "Shelf Location"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookTable = new JTable(tableModel);
        bookTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bookTable.setForeground(TEXT_DARK);
        bookTable.setBackground(WHITE);
        bookTable.setRowHeight(42);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.setAutoCreateRowSorter(true);
        bookTable.setShowVerticalLines(false);
        bookTable.setShowHorizontalLines(true);
        bookTable.setGridColor(BORDER);
        bookTable.setSelectionBackground(SOFT_GOLD);
        bookTable.setSelectionForeground(BURGUNDY);
        bookTable.setIntercellSpacing(new Dimension(0, 1));
        bookTable.setFillsViewportHeight(true);

        bookTable.getTableHeader().setFont(
                new Font("Georgia", Font.BOLD, 11)
        );
        bookTable.getTableHeader().setBackground(BURGUNDY);
        bookTable.getTableHeader().setForeground(WHITE);
        bookTable.getTableHeader().setPreferredSize(
                new Dimension(0, 45)
        );
        bookTable.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer cellRenderer =
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int column
                    ) {

                        Component component =
                                super.getTableCellRendererComponent(
                                        table,
                                        value,
                                        isSelected,
                                        hasFocus,
                                        row,
                                        column
                                );

                        setBorder(new EmptyBorder(0, 11, 0, 11));

                        if (!isSelected) {
                            component.setBackground(
                                    row % 2 == 0
                                            ? WHITE
                                            : new Color(250, 248, 245)
                            );
                            component.setForeground(TEXT_DARK);
                        }

                        return component;
                    }
                };

        for (int i = 0; i < bookTable.getColumnCount(); i++) {
            bookTable.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(cellRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        RoundedPanel tablePanel = new RoundedPanel(22, WHITE);
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(9, 9, 9, 9));

        JLabel tableTitle = new JLabel("BOOK INVENTORY");
        tableTitle.setFont(new Font("Georgia", Font.BOLD, 17));
        tableTitle.setForeground(BURGUNDY);
        tableTitle.setBorder(new EmptyBorder(8, 8, 15, 8));

        tablePanel.add(tableTitle, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JLabel tableFooter = new JLabel(
                "Select a book to edit, view details, or delete."
        );

        tableFooter.setFont(new Font("Georgia", Font.PLAIN, 11));
        tableFooter.setForeground(TEXT_GRAY);
        tableFooter.setBorder(new EmptyBorder(12, 8, 5, 8));

        tablePanel.add(tableFooter, BorderLayout.SOUTH);

        content.add(topSection, BorderLayout.NORTH);
        content.add(tablePanel, BorderLayout.CENTER);

        return content;
    }

    // SUMMARY CARD

    private JPanel createSummaryCard(
            String title,
            String value,
            int cardNumber
    ) {

        RoundedPanel card = new RoundedPanel(20, WHITE);

        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 11));
        titleLabel.setForeground(TEXT_GRAY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Georgia", Font.BOLD, 29));
        valueLabel.setForeground(BURGUNDY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        switch (cardNumber) {
            case 1 -> titlesValue = valueLabel;
            case 2 -> copiesValue = valueLabel;
            case 3 -> availableValue = valueLabel;
            case 4 -> borrowedValue = valueLabel;
        }

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(valueLabel);

        JLabel decoration = new JLabel("L");
        decoration.setFont(new Font("Georgia", Font.BOLD, 48));
        decoration.setForeground(new Color(225, 211, 185));

        JPanel cardContent = new JPanel(new BorderLayout());
        cardContent.setOpaque(false);
        cardContent.add(textPanel, BorderLayout.CENTER);
        cardContent.add(decoration, BorderLayout.EAST);

        card.add(cardContent, BorderLayout.CENTER);

        return card;
    }

    // ROUNDED BUTTON

    private class RoundedButton extends JButton {

        private final Color normalColor;
        private final Color foregroundColor;

        RoundedButton(
                String text,
                Color background,
                Color foreground
        ) {

            super(text);

            this.normalColor = background;
            this.foregroundColor = foreground;

            setFont(new Font("Georgia", Font.BOLD, 11));
            setForeground(foreground);
            setBackground(background);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(11, 16, 11, 16));
            setPreferredSize(new Dimension(
                    Math.max(85, getPreferredSize().width),
                    40
            ));
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color paintColor = normalColor;

            if (getModel().isPressed()) {
                paintColor = DARK_BURGUNDY;
            } else if (getModel().isRollover()) {
                paintColor = normalColor.darker();
            }

            g2.setColor(paintColor);
            g2.fillRoundRect(
                    0, 0,
                    getWidth(),
                    getHeight(),
                    15, 15
            );

            g2.setColor(GOLD);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(
                    0, 0,
                    getWidth() - 1,
                    getHeight() - 1,
                    15, 15
            );

            g2.dispose();

            setForeground(foregroundColor);
            super.paintComponent(g);
        }
    }

    // ROUNDED PANEL WITH SOFT SHADOW

    private class RoundedPanel extends JPanel {

        private final int radius;
        private final Color backgroundColor;

        RoundedPanel(int radius, Color backgroundColor) {

            this.radius = radius;
            this.backgroundColor = backgroundColor;

            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(new Color(75, 20, 36, 18));
            g2.fillRoundRect(
                    3, 5,
                    getWidth() - 7,
                    getHeight() - 7,
                    radius, radius
            );

            g2.setColor(backgroundColor);
            g2.fillRoundRect(
                    0, 0,
                    getWidth() - 5,
                    getHeight() - 5,
                    radius, radius
            );

            g2.setColor(BORDER);
            g2.drawRoundRect(
                    0, 0,
                    getWidth() - 6,
                    getHeight() - 6,
                    radius, radius
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // LOAD BOOKS

    private void loadBooks() {

        try {
            displayBooks(BookService.getAllBooks());
        } catch (SQLException e) {
            showError("Unable to load books.", e);
        }
    }

    // SEARCH BOOKS

    private void searchBooks() {

        String keyword = searchField.getText().trim();

        try {
            displayBooks(BookService.searchBooks(keyword));
        } catch (SQLException e) {
            showError("Unable to search books.", e);
        }
    }

    // DISPLAY BOOKS AND UPDATE SUMMARY


// DISPLAY BOOKS AND UPDATE SUMMARY

    // DISPLAY BOOKS AND UPDATE SUMMARY
    private void displayBooks(List<Book> books) {

        // Clear the existing table rows
        tableModel.setRowCount(0);

        int totalTitles = 0;
        int totalCopies = 0;
        int totalAvailable = 0;
        int totalBorrowed = 0;

        // Add each book to the inventory table
        for (Book book : books) {

            int borrowedCopies =
                    book.getTotalCopies() - book.getAvailableCopies();

            tableModel.addRow(new Object[]{
                    book.getBookId(),
                    book.getIsbn() == null ? "" : book.getIsbn(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCategory() == null ? "" : book.getCategory(),
                    book.getPublisher() == null ? "" : book.getPublisher(),
                    book.getPublicationYear() == 0
                            ? ""
                            : book.getPublicationYear(),
                    book.getTotalCopies(),
                    book.getAvailableCopies(),
                    book.getShelfLocation() == null
                            ? ""
                            : book.getShelfLocation()
            });

            totalTitles++;
            totalCopies += book.getTotalCopies();
            totalAvailable += book.getAvailableCopies();
            totalBorrowed += borrowedCopies;
        }

        // Update summary cards
        titlesValue.setText(String.valueOf(totalTitles));
        copiesValue.setText(String.valueOf(totalCopies));
        availableValue.setText(String.valueOf(totalAvailable));
        borrowedValue.setText(String.valueOf(totalBorrowed));

        // Refresh the table display
        tableModel.fireTableDataChanged();
    }

// BOOK COVER IMAGE PICKER

    // BOOK COVER IMAGE PICKER

    private class CoverImagePicker {

        private final JPanel panel;
        private final JLabel previewLabel;
        private final JLabel fileNameLabel;

        private String existingImagePath;
        private File selectedFile;

        CoverImagePicker(String existingImagePath) {

            this.existingImagePath = existingImagePath == null
                    ? ""
                    : existingImagePath;

            panel = new JPanel(new BorderLayout(12, 5));
            panel.setOpaque(false);

            JPanel leftPanel = new JPanel();
            leftPanel.setOpaque(false);
            leftPanel.setLayout(
                    new BoxLayout(leftPanel, BoxLayout.Y_AXIS)
            );

            RoundedButton chooseButton = new RoundedButton(
                    "Choose Image", BURGUNDY, WHITE
            );

            chooseButton.addActionListener(e -> chooseImage());

            fileNameLabel = new JLabel("No image selected");
            fileNameLabel.setFont(
                    new Font("Segoe UI", Font.PLAIN, 11)
            );
            fileNameLabel.setForeground(TEXT_GRAY);

            leftPanel.add(chooseButton);
            leftPanel.add(Box.createVerticalStrut(7));
            leftPanel.add(fileNameLabel);

            previewLabel = new JLabel(
                    "No Cover",
                    SwingConstants.CENTER
            );

            previewLabel.setPreferredSize(new Dimension(100, 130));
            previewLabel.setOpaque(true);
            previewLabel.setBackground(new Color(247, 243, 235));
            previewLabel.setBorder(
                    BorderFactory.createLineBorder(BORDER)
            );

            panel.add(leftPanel, BorderLayout.CENTER);
            panel.add(previewLabel, BorderLayout.EAST);

            if (!this.existingImagePath.isBlank()) {

                File existingFile = new File(this.existingImagePath);

                if (existingFile.exists()) {
                    displayPreview(existingFile);
                    fileNameLabel.setText(existingFile.getName());
                } else {
                    fileNameLabel.setText("Existing image not found");
                }
            }
        }

        private void chooseImage() {

            JFileChooser chooser = new JFileChooser();

            chooser.setDialogTitle("Select Book Cover Image");

            chooser.setFileFilter(
                    new FileNameExtensionFilter(
                            "Image Files (*.jpg, *.jpeg, *.png)",
                            "jpg", "jpeg", "png"
                    )
            );

            chooser.setAcceptAllFileFilterUsed(false);

            int result = chooser.showOpenDialog(ManageBooksFrame.this);

            if (result == JFileChooser.APPROVE_OPTION) {

                File file = chooser.getSelectedFile();

                if (displayPreview(file)) {
                    selectedFile = file;
                    fileNameLabel.setText(file.getName());
                }
            }
        }

        private boolean displayPreview(File file) {

            try {

                BufferedImage image = ImageIO.read(file);

                if (image == null) {
                    throw new IllegalArgumentException(
                            "Invalid image file."
                    );
                }

                Image scaledImage = image.getScaledInstance(
                        100,
                        130,
                        Image.SCALE_SMOOTH
                );

                previewLabel.setIcon(new ImageIcon(scaledImage));
                previewLabel.setText("");

                return true;

            } catch (Exception e) {

                previewLabel.setIcon(null);
                previewLabel.setText("Preview unavailable");

                showWarning(
                        "The selected file is not a valid image.",
                        "Image Error"
                );

                return false;
            }
        }

        JPanel getPanel() {
            return panel;
        }

        String saveImage() throws java.io.IOException {

            if (selectedFile == null) {
                return existingImagePath;
            }

            String fileName = selectedFile.getName();

            String extension = fileName.substring(
                    fileName.lastIndexOf(".")
            ).toLowerCase();

            Path directory = Path.of(
                    System.getProperty("user.home"),
                    "LibraryLink",
                    "book_covers"
            );

            Files.createDirectories(directory);

            String uniqueName = UUID.randomUUID() + extension;

            Path destination = directory.resolve(uniqueName);

            Files.copy(
                    selectedFile.toPath(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return destination.toAbsolutePath().toString();
        }
    }

    // ADD BOOK

    private void showAddBookDialog() {

        JTextField isbnField = createFormField();
        JTextField titleField = createFormField();
        JTextField authorField = createFormField();
        JTextField categoryField = createFormField();
        JTextField publisherField = createFormField();
        JTextField yearField = createFormField();
        JTextField copiesField = createFormField();
        JTextField shelfField = createFormField();
        CoverImagePicker coverPicker = new CoverImagePicker("");
        JPanel form = createFormPanel();

        addFormRow(form, "ISBN", isbnField);
        addFormRow(form, "Book Title *", titleField);
        addFormRow(form, "Author *", authorField);
        addFormRow(form, "Category", categoryField);
        addFormRow(form, "Publisher", publisherField);
        addFormRow(form, "Publication Year", yearField);
        addFormRow(form, "Total Copies *", copiesField);
        addFormRow(form, "Shelf Location", shelfField);
        addFormRow(form, "Book Cover", coverPicker.getPanel());
        if (showFormDialog(form, "Add New Book") != JOptionPane.OK_OPTION) {
            return;
        }

        String title = titleField.getText().trim();
        String author = authorField.getText().trim();

        if (title.isEmpty() || author.isEmpty()) {
            showWarning(
                    "Book title and author are required.",
                    "Missing Information"
            );
            return;
        }

        try {

            int year = parseYear(yearField.getText());
            int copies = Integer.parseInt(copiesField.getText().trim());

            if (copies < 1) {
                throw new IllegalArgumentException(
                        "Total copies must be at least 1."
                );
            }

            Book book = new Book(
                    0,
                    emptyToNull(isbnField.getText()),
                    title,
                    author,
                    emptyToNull(categoryField.getText()),
                    emptyToNull(publisherField.getText()),
                    year,
                    copies,
                    copies,
                    emptyToNull(shelfField.getText())
            );

            if (BookService.addBook(book)) {

                String coverPath = coverPicker.saveImage();

                if (!coverPath.isBlank()) {

                    int newBookId = findNewBookId(book);

                    if (newBookId > 0) {
                        updateOptionalBookDetails(
                                newBookId,
                                coverPath,
                                ""
                        );
                    }
                }

                showSuccess("Book added successfully!");
                searchField.setText("");
                loadBooks();

            } else {

                showWarning(
                        "The book could not be added.",
                        "Add Book Failed"
                );
            }

        } catch (NumberFormatException e) {
            showWarning(
                    "Enter a valid publication year and copy count.",
                    "Invalid Input"
            );
        } catch (IllegalArgumentException e) {
            showWarning(e.getMessage(), "Invalid Input");
        } catch (SQLException e) {
            showError(
                    "Unable to add book. Check whether its ISBN already exists.",
                    e
            );
        } catch (java.io.IOException e) {
            showError(
                    "The book was added, but the cover image could not be saved.",
                    e
            );
        }
    }
// FIND NEWLY ADDED BOOK ID

    private int findNewBookId(Book book) throws SQLException {

        String sql = """
            SELECT book_id
            FROM books
            WHERE title = ?
              AND author = ?
              AND (
                    isbn = ?
                    OR (isbn IS NULL AND ? IS NULL)
              )
            ORDER BY book_id DESC
            LIMIT 1
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());
            statement.setString(3, book.getIsbn());
            statement.setString(4, book.getIsbn());

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("book_id");
                }
            }
        }

        return -1;
    }
    // EDIT BOOK

    private void editSelectedBook() {

        int selectedRow = bookTable.getSelectedRow();

        if (selectedRow == -1) {
            showWarning("Please select a book first.", "No Book Selected");
            return;
        }

        int row = bookTable.convertRowIndexToModel(selectedRow);

        int bookId = getIntValue(row, 0);
        String isbn = getStringValue(row, 1);
        String title = getStringValue(row, 2);
        String author = getStringValue(row, 3);
        String category = getStringValue(row, 4);
        String publisher = getStringValue(row, 5);
        int year = getIntValue(row, 6);
        int totalCopies = getIntValue(row, 7);
        int availableCopies = getIntValue(row, 8);
        String shelf = getStringValue(row, 9);

        JTextField isbnField = createFormField(isbn);
        JTextField titleField = createFormField(title);
        JTextField authorField = createFormField(author);
        JTextField categoryField = createFormField(category);
        JTextField publisherField = createFormField(publisher);
        JTextField yearField = createFormField(
                year == 0 ? "" : String.valueOf(year)
        );
        JTextField totalCopiesField =
                createFormField(String.valueOf(totalCopies));
        JTextField shelfField = createFormField(shelf);

        int borrowedCopies = totalCopies - availableCopies;

        JPanel form = createFormPanel();

        addFormRow(form, "ISBN", isbnField);
        addFormRow(form, "Book Title *", titleField);
        addFormRow(form, "Author *", authorField);
        addFormRow(form, "Category", categoryField);
        addFormRow(form, "Publisher", publisherField);
        addFormRow(form, "Publication Year", yearField);
        addFormRow(form, "Total Copies *", totalCopiesField);
        addFormRow(form, "Shelf Location", shelfField);

        JLabel borrowedInfo = new JLabel(
                "Currently borrowed copies: " + borrowedCopies
        );
        borrowedInfo.setForeground(BURGUNDY);
        borrowedInfo.setFont(new Font("Georgia", Font.BOLD, 12));

        form.add(new JLabel("Inventory Note"));
        form.add(borrowedInfo);

        if (showFormDialog(form, "Edit Book Information")
                != JOptionPane.OK_OPTION) {
            return;
        }

        String newTitle = titleField.getText().trim();
        String newAuthor = authorField.getText().trim();

        if (newTitle.isEmpty() || newAuthor.isEmpty()) {
            showWarning(
                    "Book title and author are required.",
                    "Missing Information"
            );
            return;
        }

        try {

            int newYear = parseYear(yearField.getText());
            int newTotalCopies = Integer.parseInt(
                    totalCopiesField.getText().trim()
            );

            int newAvailableCopies = newTotalCopies - borrowedCopies;

            if (newTotalCopies < 1) {
                throw new IllegalArgumentException(
                        "Total copies must be at least 1."
                );
            }

            if (newAvailableCopies < 0) {
                throw new IllegalArgumentException(
                        "Total copies cannot be less than the number currently borrowed."
                );
            }

            Book updatedBook = new Book(
                    bookId,
                    emptyToNull(isbnField.getText()),
                    newTitle,
                    newAuthor,
                    emptyToNull(categoryField.getText()),
                    emptyToNull(publisherField.getText()),
                    newYear,
                    newTotalCopies,
                    newAvailableCopies,
                    emptyToNull(shelfField.getText())
            );

            if (BookService.updateBook(updatedBook)) {
                showSuccess("Book information updated successfully!");
                loadBooks();
            } else {
                showWarning(
                        "The book information could not be updated.",
                        "Update Failed"
                );
            }

        } catch (NumberFormatException e) {
            showWarning(
                    "Enter valid numbers for publication year and total copies.",
                    "Invalid Input"
            );
        } catch (IllegalArgumentException e) {
            showWarning(e.getMessage(), "Invalid Copy Count");
        } catch (SQLException e) {
            showError(
                    "Unable to update book. Check whether its ISBN already exists.",
                    e
            );
        }
    }

    // BOOK DETAILS WINDOW

    private void showBookDetails() {

        int selectedRow = bookTable.getSelectedRow();

        if (selectedRow == -1) {
            showWarning("Please select a book first.", "No Book Selected");
            return;
        }

        int row = bookTable.convertRowIndexToModel(selectedRow);
        int bookId = getIntValue(row, 0);

        try {

            BookExtraDetails extra = getBookExtraDetails(bookId);

            String title = getStringValue(row, 2);
            String author = getStringValue(row, 3);
            String category = getStringValue(row, 4);
            String publisher = getStringValue(row, 5);
            String year = getStringValue(row, 6);
            String available = getStringValue(row, 8);

            JDialog dialog = new JDialog(this, "BOOK DETAILS", true);

            dialog.setSize(900, 650);
            dialog.setMinimumSize(new Dimension(760, 580));
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());
            dialog.getContentPane().setBackground(CREAM);

            // HEADER

            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(BURGUNDY);
            header.setBorder(new EmptyBorder(20, 28, 20, 28));

            JLabel headerTitle = new JLabel("BOOK DETAILS");
            headerTitle.setFont(new Font("Georgia", Font.BOLD, 22));
            headerTitle.setForeground(GOLD);

            RoundedButton closeX = new RoundedButton(
                    "X", BURGUNDY, WHITE
            );
            closeX.addActionListener(e -> dialog.dispose());

            header.add(headerTitle, BorderLayout.WEST);
            header.add(closeX, BorderLayout.EAST);

            // MAIN DETAILS

            JPanel main = new JPanel(new BorderLayout(25, 15));
            main.setBackground(CREAM);
            main.setBorder(new EmptyBorder(25, 28, 15, 28));

            // BOOK COVER

            RoundedPanel coverPanel = new RoundedPanel(18, WHITE);
            coverPanel.setLayout(new BorderLayout());
            coverPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
            coverPanel.setPreferredSize(new Dimension(270, 330));

            coverPanel.add(
                    createCoverLabel(extra.coverImage),
                    BorderLayout.CENTER
            );

            // BOOK INFORMATION

            JPanel informationPanel = new JPanel();
            informationPanel.setBackground(CREAM);
            informationPanel.setLayout(
                    new BoxLayout(informationPanel, BoxLayout.Y_AXIS)
            );

            JLabel bookTitle = new JLabel(title);
            bookTitle.setFont(new Font("Georgia", Font.BOLD, 23));
            bookTitle.setForeground(BURGUNDY);
            bookTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

            informationPanel.add(bookTitle);
            informationPanel.add(Box.createVerticalStrut(20));

            addDetailLine(informationPanel, "Book ID", String.valueOf(bookId));
            addDetailLine(informationPanel, "Author", author);
            addDetailLine(informationPanel, "Genre", category);
            addDetailLine(informationPanel, "Publisher", publisher);
            addDetailLine(informationPanel, "Year Published", year);
            addDetailLine(
                    informationPanel,
                    "Availability Status",
                    getAvailabilityStatus(available)
            );

            JPanel topDetails = new JPanel(new BorderLayout(25, 0));
            topDetails.setOpaque(false);
            topDetails.add(coverPanel, BorderLayout.WEST);
            topDetails.add(informationPanel, BorderLayout.CENTER);

            // DESCRIPTION SECTION

            RoundedPanel descriptionPanel = new RoundedPanel(18, WHITE);
            descriptionPanel.setLayout(new BorderLayout(0, 10));
            descriptionPanel.setBorder(
                    new EmptyBorder(15, 18, 15, 18)
            );

            JLabel descriptionTitle = new JLabel("DESCRIPTION");
            descriptionTitle.setFont(new Font("Georgia", Font.BOLD, 14));
            descriptionTitle.setForeground(BURGUNDY);

            JTextArea descriptionArea = new JTextArea(
                    extra.description.isBlank()
                            ? "No description available."
                            : extra.description
            );

            descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            descriptionArea.setForeground(TEXT_DARK);
            descriptionArea.setBackground(WHITE);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setEditable(false);
            descriptionArea.setRows(3);

            JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
            descriptionScroll.setBorder(null);
            descriptionScroll.setPreferredSize(new Dimension(500, 85));

            RoundedButton editDescriptionButton = new RoundedButton(
                    "EDIT DESCRIPTION", GOLD, DARK_BURGUNDY
            );

            editDescriptionButton.addActionListener(e ->
                    editDescription(bookId, dialog)
            );

            JPanel descriptionHeader = new JPanel(new BorderLayout());
            descriptionHeader.setOpaque(false);
            descriptionHeader.add(descriptionTitle, BorderLayout.WEST);
            descriptionHeader.add(editDescriptionButton, BorderLayout.EAST);

            descriptionPanel.add(descriptionHeader, BorderLayout.NORTH);
            descriptionPanel.add(descriptionScroll, BorderLayout.CENTER);

            JPanel detailsContent = new JPanel(new BorderLayout(0, 18));
            detailsContent.setOpaque(false);
            detailsContent.add(topDetails, BorderLayout.CENTER);
            detailsContent.add(descriptionPanel, BorderLayout.SOUTH);

            main.add(detailsContent, BorderLayout.CENTER);

            // FOOTER BUTTONS

            JPanel footer = new JPanel(
                    new FlowLayout(FlowLayout.RIGHT, 12, 15)
            );
            footer.setBackground(WHITE);
            footer.setBorder(new EmptyBorder(5, 20, 5, 25));

            RoundedButton closeButton = new RoundedButton(
                    "CLOSE", new Color(235, 230, 224), TEXT_DARK
            );

            RoundedButton editDetailsButton = new RoundedButton(
                    "EDIT DETAILS", GOLD, DARK_BURGUNDY
            );

            RoundedButton borrowButton = new RoundedButton(
                    "BORROW BOOK", BURGUNDY, WHITE
            );

            closeButton.addActionListener(e -> dialog.dispose());

            editDetailsButton.addActionListener(e -> {
                dialog.dispose();
                editBookDetails(bookId);
            });

            borrowButton.addActionListener(e -> {
                dialog.dispose();
                openBorrowBooks();
            });

            footer.add(closeButton);
            footer.add(editDetailsButton);
            footer.add(borrowButton);

            dialog.add(header, BorderLayout.NORTH);
            dialog.add(main, BorderLayout.CENTER);
            dialog.add(footer, BorderLayout.SOUTH);

            dialog.setVisible(true);

        } catch (SQLException e) {
            showError("Unable to load book details.", e);
        }
    }

    // EDIT ALL BOOK DETAILS

    private void editBookDetails(int bookId) {

        int row = findBookModelRow(bookId);

        if (row == -1) {
            showWarning(
                    "Book not found. Please refresh the list.",
                    "Book Not Found"
            );
            return;
        }

        String isbn = getStringValue(row, 1);
        String title = getStringValue(row, 2);
        String author = getStringValue(row, 3);
        String category = getStringValue(row, 4);
        String publisher = getStringValue(row, 5);
        String year = getStringValue(row, 6);
        String total = getStringValue(row, 7);
        String available = getStringValue(row, 8);
        String shelf = getStringValue(row, 9);

        try {

            BookExtraDetails extra = getBookExtraDetails(bookId);

            JTextField isbnField = createFormField(isbn);
            JTextField titleField = createFormField(title);
            JTextField authorField = createFormField(author);
            JTextField categoryField = createFormField(category);
            JTextField publisherField = createFormField(publisher);
            JTextField yearField = createFormField(year);
            JTextField totalField = createFormField(total);
            JTextField shelfField = createFormField(shelf);
            CoverImagePicker coverPicker =
                    new CoverImagePicker(extra.coverImage);

            JTextArea descriptionField = new JTextArea(
                    extra.description, 5, 25
            );

            descriptionField.setFont(
                    new Font("Segoe UI", Font.PLAIN, 13)
            );
            descriptionField.setLineWrap(true);
            descriptionField.setWrapStyleWord(true);
            descriptionField.setBorder(
                    new EmptyBorder(8, 9, 8, 9)
            );

            JPanel form = createFormPanel();

            addFormRow(form, "ISBN", isbnField);
            addFormRow(form, "Book Title *", titleField);
            addFormRow(form, "Author *", authorField);
            addFormRow(form, "Genre", categoryField);
            addFormRow(form, "Publisher", publisherField);
            addFormRow(form, "Year Published", yearField);
            addFormRow(form, "Total Copies *", totalField);
            addFormRow(form, "Shelf Location", shelfField);
            addFormRow(form, "Book Cover", coverPicker.getPanel());

            form.add(createFormLabel("Description"));
            form.add(new JScrollPane(descriptionField));

            int result = JOptionPane.showConfirmDialog(
                    this,
                    new JScrollPane(form),
                    "Edit Book Details",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String newTitle = titleField.getText().trim();
            String newAuthor = authorField.getText().trim();

            if (newTitle.isEmpty() || newAuthor.isEmpty()) {
                showWarning(
                        "Book title and author are required.",
                        "Missing Information"
                );
                return;
            }

            int newYear = parseYear(yearField.getText());
            int newTotal = Integer.parseInt(totalField.getText().trim());
            int oldTotal = Integer.parseInt(total);
            int oldAvailable = Integer.parseInt(available);
            int borrowed = oldTotal - oldAvailable;
            int newAvailable = newTotal - borrowed;

            if (newTotal < 1 || newAvailable < 0) {
                showWarning(
                        "Total copies cannot be less than the number currently borrowed.",
                        "Invalid Copy Count"
                );
                return;
            }

            Book updatedBook = new Book(
                    bookId,
                    emptyToNull(isbnField.getText()),
                    newTitle,
                    newAuthor,
                    emptyToNull(categoryField.getText()),
                    emptyToNull(publisherField.getText()),
                    newYear,
                    newTotal,
                    newAvailable,
                    emptyToNull(shelfField.getText())
            );

            if (!BookService.updateBook(updatedBook)) {
                showWarning(
                        "The book information could not be updated.",
                        "Update Failed"
                );
                return;
            }

            String updatedCoverPath = coverPicker.saveImage();

            updateOptionalBookDetails(
                    bookId,
                    updatedCoverPath,
                    descriptionField.getText().trim()
            );

            showSuccess("Book details updated successfully!");
            loadBooks();

        } catch (NumberFormatException e) {
            showWarning(
                    "Enter valid numbers for the publication year and total copies.",
                    "Invalid Input"
            );
        } catch (IllegalArgumentException e) {
            showWarning(e.getMessage(), "Invalid Input");
        } catch (SQLException e) {
            showError("Unable to update book details.", e);
        } catch (java.io.IOException e) {
            showError(
                    "The book details were updated, but the cover image could not be saved.",
                    e
            );
        }
    }

    // EDIT DESCRIPTION ONLY

    private void editDescription(int bookId, JDialog detailsDialog) {

        try {

            BookExtraDetails extra = getBookExtraDetails(bookId);

            JTextArea descriptionField = new JTextArea(
                    extra.description, 8, 35
            );

            descriptionField.setFont(
                    new Font("Segoe UI", Font.PLAIN, 13)
            );
            descriptionField.setLineWrap(true);
            descriptionField.setWrapStyleWord(true);
            descriptionField.setBorder(
                    new EmptyBorder(8, 9, 8, 9)
            );

            JScrollPane scrollPane = new JScrollPane(descriptionField);

            int result = JOptionPane.showConfirmDialog(
                    detailsDialog,
                    scrollPane,
                    "Edit Book Description",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {

                updateOptionalBookDetails(
                        bookId,
                        extra.coverImage,
                        descriptionField.getText().trim()
                );

                showSuccess("Book description saved successfully!");

                detailsDialog.dispose();
                showBookDetailsForId(bookId);
            }

        } catch (SQLException e) {
            showError("Unable to save the book description.", e);
        }
    }

    // REOPEN DETAILS AFTER DESCRIPTION EDIT

    private void showBookDetailsForId(int bookId) {

        int viewRow = -1;

        for (int i = 0; i < bookTable.getRowCount(); i++) {

            int modelRow = bookTable.convertRowIndexToModel(i);

            if (getIntValue(modelRow, 0) == bookId) {
                viewRow = i;
                break;
            }
        }

        if (viewRow >= 0) {
            bookTable.setRowSelectionInterval(viewRow, viewRow);
            showBookDetails();
        }
    }

    // ENSURE OPTIONAL DETAIL COLUMNS EXIST

    private void ensureBookDetailColumns() throws SQLException {

        ensureColumnExists("cover_image");
        ensureColumnExists("description");
    }

    private void ensureColumnExists(String columnName)
            throws SQLException {

        boolean exists = false;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement("PRAGMA table_info(books)");
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    exists = true;
                    break;
                }
            }
        }

        if (!exists) {

            String sql = "ALTER TABLE books ADD COLUMN "
                    + columnName + " TEXT";

            try (Connection connection = DatabaseConnection.getConnection();
                 Statement statement = connection.createStatement()) {

                statement.executeUpdate(sql);
            }
        }
    }

    // READ COVER IMAGE AND DESCRIPTION

    private BookExtraDetails getBookExtraDetails(int bookId)
            throws SQLException {

        String coverImage = "";
        String description = "";

        String sql = """
                SELECT cover_image, description
                FROM books
                WHERE book_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, bookId);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    coverImage = valueOrEmpty(rs.getString("cover_image"));
                    description = valueOrEmpty(rs.getString("description"));
                }
            }
        }

        return new BookExtraDetails(coverImage, description);
    }

    // SAVE COVER IMAGE AND DESCRIPTION

    private void updateOptionalBookDetails(
            int bookId,
            String coverImage,
            String description
    ) throws SQLException {

        String sql = """
                UPDATE books
                SET cover_image = ?, description = ?
                WHERE book_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, emptyToNull(coverImage));
            statement.setString(2, emptyToNull(description));
            statement.setInt(3, bookId);

            statement.executeUpdate();
        }
    }

    private static class BookExtraDetails {

        private final String coverImage;
        private final String description;

        private BookExtraDetails(String coverImage, String description) {
            this.coverImage = coverImage;
            this.description = description;
        }
    }

    // DISPLAY COVER IMAGE

    // DISPLAY COVER IMAGE - RESPONSIVE SCALING
    private JLabel createCoverLabel(String imagePath) {

        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(240, 300));
        label.setOpaque(true);
        label.setBackground(new Color(247, 243, 235));

        if (imagePath != null && !imagePath.isBlank()) {

            try {

                BufferedImage image;

                if (imagePath.startsWith("http://")
                        || imagePath.startsWith("https://")) {

                    image = ImageIO.read(new URL(imagePath));

                } else {

                    image = ImageIO.read(new File(imagePath));
                }

                if (image != null) {

                    // Resize the image whenever the label changes size
                    label.addComponentListener(
                            new java.awt.event.ComponentAdapter() {

                                @Override
                                public void componentResized(
                                        java.awt.event.ComponentEvent e) {

                                    int availableWidth = label.getWidth() - 10;
                                    int availableHeight = label.getHeight() - 10;

                                    if (availableWidth <= 0 || availableHeight <= 0) {
                                        return;
                                    }

                                    double widthRatio =
                                            (double) availableWidth / image.getWidth();

                                    double heightRatio =
                                            (double) availableHeight / image.getHeight();

                                    double scale = Math.min(
                                            widthRatio,
                                            heightRatio
                                    );

                                    int newWidth = (int) (image.getWidth() * scale);
                                    int newHeight = (int) (image.getHeight() * scale);

                                    Image scaledImage = image.getScaledInstance(
                                            newWidth,
                                            newHeight,
                                            Image.SCALE_SMOOTH
                                    );

                                    label.setIcon(new ImageIcon(scaledImage));
                                }
                            });

                    return label;
                }

            } catch (Exception e) {
                // Keep the placeholder when the image cannot be loaded.
            }
        }

        label.setLayout(new BorderLayout());

        JLabel placeholder = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "<span style='font-size:48px;'>BOOK</span><br>"
                        + "<span style='font-size:13px;'>"
                        + "Cover image unavailable"
                        + "</span></div></html>",
                SwingConstants.CENTER
        );

        placeholder.setForeground(TEXT_GRAY);
        label.add(placeholder, BorderLayout.CENTER);

        return label;
    }

    // BOOK INFORMATION LINE

    private void addDetailLine(
            JPanel panel,
            String label,
            String value
    ) {

        JPanel line = new JPanel(new BorderLayout(10, 0));
        line.setOpaque(false);
        line.setBorder(new EmptyBorder(5, 0, 5, 0));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(label + ":");
        nameLabel.setFont(new Font("Georgia", Font.BOLD, 12));
        nameLabel.setForeground(TEXT_GRAY);
        nameLabel.setPreferredSize(new Dimension(145, 25));

        JLabel valueLabel = new JLabel(
                value == null || value.isBlank()
                        ? "Not provided"
                        : value
        );

        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueLabel.setForeground(TEXT_DARK);

        line.add(nameLabel, BorderLayout.WEST);
        line.add(valueLabel, BorderLayout.CENTER);

        panel.add(line);
    }

    private String getAvailabilityStatus(String availableText) {

        try {

            int available = Integer.parseInt(availableText);

            return available > 0
                    ? "Available (" + available + " copies)"
                    : "Currently Unavailable";

        } catch (NumberFormatException e) {
            return "Status unavailable";
        }
    }

    // BORROW BOOK WINDOW

    private void openBorrowBooks() {

        BorrowBooksFrame frame = new BorrowBooksFrame();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    private int findBookModelRow(int bookId) {

        for (int row = 0; row < tableModel.getRowCount(); row++) {

            if (getIntValue(row, 0) == bookId) {
                return row;
            }
        }

        return -1;
    }

    // DELETE BOOK

    private void deleteSelectedBook() {

        int selectedRow = bookTable.getSelectedRow();

        if (selectedRow == -1) {
            showWarning("Please select a book first.", "No Book Selected");
            return;
        }

        int row = bookTable.convertRowIndexToModel(selectedRow);

        int bookId = getIntValue(row, 0);
        String title = getStringValue(row, 2);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete this book?\n\n"
                        + "Title: " + title
                        + "\nBook ID: " + bookId
                        + "\n\nDeletion may be blocked if borrowing records reference this book.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            if (BookService.deleteBook(bookId)) {
                showSuccess("Book deleted successfully!");
                searchField.setText("");
                loadBooks();
            } else {
                showWarning(
                        "The book could not be deleted.",
                        "Delete Failed"
                );
            }

        } catch (SQLException e) {
            showError(
                    "Unable to delete book. It may have borrowing records.",
                    e
            );
        }
    }

    // FORM HELPERS

    private JPanel createFormPanel() {

        JPanel form = new JPanel(new GridLayout(0, 2, 12, 12));
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));

        return form;
    }

    private JTextField createFormField() {
        JTextField field = new JTextField();

        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(180, 32));
        field.setMinimumSize(new Dimension(150, 32));

        field.setBackground(WHITE);
        field.setForeground(TEXT_DARK);
        field.setCaretColor(BURGUNDY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(5, 8, 5, 8)
        ));

        return field;
    }

    private JTextField createFormField(String value) {

        JTextField field = new JTextField(value);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(250, 36));
        field.setBackground(WHITE);
        field.setForeground(TEXT_DARK);

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(GOLD),
                        new EmptyBorder(7, 10, 7, 10)
                )
        );

        return field;
    }

    private JLabel createFormLabel(String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("Georgia", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);

        return label;
    }

    private void addFormRow(
            JPanel form,
            String label,
            JComponent field
    ) {

        form.add(createFormLabel(label));
        form.add(field);
    }

    private int showFormDialog(JPanel form, String title) {

        JScrollPane scrollPane = new JScrollPane(form);

        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(650, 550));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        return JOptionPane.showConfirmDialog(
                this,
                scrollPane,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
    }

    // VALIDATE YEAR

    private int parseYear(String value) {

        if (value == null || value.isBlank()) {
            return 0;
        }

        int year = Integer.parseInt(value.trim());

        if (year < 0 || year > 9999) {
            throw new IllegalArgumentException(
                    "Publication year must be between 0 and 9999."
            );
        }

        return year;
    }

    // TABLE HELPERS

    private int getIntValue(int row, int column) {

        Object value = tableModel.getValueAt(row, column);

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        return Integer.parseInt(String.valueOf(value));
    }

    private String getStringValue(int row, int column) {

        Object value = tableModel.getValueAt(row, column);

        return value == null ? "" : String.valueOf(value);
    }

    private String emptyToNull(String value) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    // MESSAGES

    private void showWarning(String message, String title) {

        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void showSuccess(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String message, Exception e) {

        JOptionPane.showMessageDialog(
                this,
                message + "\n" + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );

        e.printStackTrace();
    }
}

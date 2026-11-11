
package com.library.ui;

import com.library.service.BookService;
import com.library.service.BookService.Book;
import com.library.database.DatabaseConnection;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ManageBooksFrame extends JFrame {

    private final Color BURGUNDY = new Color(75, 20, 36);
    private final Color DARK_BURGUNDY = new Color(55, 13, 27);
    private final Color GOLD = new Color(176, 141, 87);
    private final Color CREAM = new Color(243, 235, 221);
    private final Color WHITE = new Color(255, 252, 245);
    private final Color TEXT_DARK = new Color(53, 37, 31);
    private final Color TEXT_GRAY = new Color(130, 115, 103);
    private final Color BORDER = new Color(222, 210, 191);

    private JTable bookTable;
    private DefaultTableModel tableModel;

    private JTextField searchField;
    private JComboBox<String> shelfFilter;

    private JLabel titlesValue;
    private JLabel copiesValue;
    private JLabel availableValue;
    private JLabel borrowedValue;

    private JPanel bookCardsPanel;
    private JScrollPane bookCardsScrollPane;

    private int selectedBookId = -1;

    private List<Book> allBooks = new ArrayList<>();
    private List<Book> searchResults = new ArrayList<>();

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
            showError("Unable to prepare book details fields.", e);
        }

        loadBooks();
    }
    private JPanel createRoundedSearchPanel() {
        return new JPanel(new BorderLayout(8, 0)) {
            {
                setOpaque(false);
                setBorder(new EmptyBorder(5, 10, 5, 10));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(
                        0, 0,
                        getWidth() - 1,
                        getHeight() - 1,
                        18, 18
                );

                g2.dispose();
                super.paintComponent(g);
            }
        };
    }
    // =========================================================
    // HEADER
    // =========================================================

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
        subtitle.setForeground(CREAM);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JPanel headerActions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        headerActions.setOpaque(false);

        JButton refreshButton = createHeaderIconButton("↻");
        refreshButton.setToolTipText("Refresh book inventory");

        JButton backButton = createHeaderIconButton("←");
        backButton.setToolTipText("Back");

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            shelfFilter.setSelectedIndex(0);
            loadBooks();
        });

        backButton.addActionListener(e -> dispose());

        headerActions.add(refreshButton);
        headerActions.add(backButton);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(headerActions, BorderLayout.EAST);

        return header;
    }

    private JButton createHeaderIconButton(String iconText) {

        JButton button = new JButton(iconText);

        button.setFont(new Font("Segoe UI Symbol", Font.BOLD, 25));
        button.setForeground(WHITE);
        button.setBackground(BURGUNDY);
        button.setPreferredSize(new Dimension(42, 42));

        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(GOLD);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(WHITE);
            }
        });

        return button;
    }

    // =========================================================
    // MAIN CONTENT
    // =========================================================

    private JPanel createContent() {

        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(CREAM);
        content.setBorder(new EmptyBorder(25, 30, 30, 30));

        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 17, 17));
        summaryPanel.setOpaque(false);

        summaryPanel.add(createSummaryCard("TOTAL TITLES", "0", 1));
        summaryPanel.add(createSummaryCard("TOTAL COPIES", "0", 2));
        summaryPanel.add(createSummaryCard("AVAILABLE COPIES", "0", 3));
        summaryPanel.add(createSummaryCard("BORROWED COPIES", "0", 4));

        // SEARCH AND INVENTORY FILTER

        JPanel toolbar = new JPanel(new BorderLayout(12, 10));
        toolbar.setOpaque(false);

        JPanel leftToolbar = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 12, 5)
        );
        leftToolbar.setOpaque(false);

        // SEARCH FIELD

        JPanel searchBox = new JPanel(new BorderLayout());
        searchBox.setOpaque(false);
        searchBox.setPreferredSize(new Dimension(300, 42));

        JPanel searchInputPanel = createRoundedSearchPanel();

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setForeground(TEXT_DARK);
        searchField.setCaretColor(BURGUNDY);
        searchField.setBackground(WHITE);
        searchField.setOpaque(false);
        searchField.setBorder(new EmptyBorder(8, 5, 8, 5));

        JLabel searchIcon = new JLabel("⌕");
        searchIcon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 23));
        searchIcon.setForeground(TEXT_GRAY);
        searchIcon.setBorder(new EmptyBorder(0, 8, 0, 3));

        searchInputPanel.add(searchField, BorderLayout.CENTER);
        searchInputPanel.add(searchIcon, BorderLayout.EAST);

        searchBox.add(searchInputPanel, BorderLayout.CENTER);

        // SHELF FILTER

        JPanel shelfPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 7, 0)
        );
        shelfPanel.setOpaque(false);

        JPanel shelfFilterPanel = new JPanel(new BorderLayout(8, 0));
        shelfFilterPanel.setBackground(WHITE);
        shelfFilterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));

        shelfFilterPanel.setPreferredSize(new Dimension(165, 42));

        JLabel shelfLabel = new JLabel("Shelf:");
        shelfLabel.setFont(new Font("Georgia", Font.BOLD, 12));
        shelfLabel.setForeground(TEXT_DARK);

        shelfFilter = new JComboBox<>();
        shelfFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        shelfFilter.setBackground(WHITE);
        shelfFilter.setForeground(TEXT_DARK);
        shelfFilter.setPreferredSize(new Dimension(165, 38));
        shelfFilter.setFocusable(false);

        shelfFilterPanel.add(shelfFilter, BorderLayout.CENTER);

        shelfPanel.add(shelfLabel);
        shelfPanel.add(shelfFilter);

        leftToolbar.add(searchBox);
        leftToolbar.add(shelfPanel);

        // ADD BOOK BUTTON

        JPanel actionPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 5)
        );
        actionPanel.setOpaque(false);

        RoundedButton addButton = new RoundedButton(
                "+ Add Book", BURGUNDY, WHITE
        );

        addButton.setPreferredSize(new Dimension(125, 42));
        addButton.addActionListener(e -> showAddBookDialog());

        actionPanel.add(addButton);

        toolbar.add(leftToolbar, BorderLayout.WEST);
        toolbar.add(actionPanel, BorderLayout.EAST);

        // AUTOMATIC SEARCH

        searchField.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        searchBooks();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        searchBooks();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        searchBooks();
                    }
                }
        );

        searchField.addActionListener(e -> searchBooks());

        shelfFilter.addActionListener(e -> applyInventoryFilters());

        JPanel topSection = new JPanel(new BorderLayout(0, 15));
        topSection.setOpaque(false);
        topSection.add(summaryPanel, BorderLayout.NORTH);
        topSection.add(toolbar, BorderLayout.CENTER);

        // INTERNAL MODEL

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

        // BOOK CARDS

        bookCardsPanel = new JPanel(new GridBagLayout());
        bookCardsPanel.setBackground(CREAM);
        bookCardsPanel.setBorder(new EmptyBorder(8, 5, 15, 5));

        bookCardsScrollPane = new JScrollPane(bookCardsPanel);
        bookCardsScrollPane.setBorder(null);
        bookCardsScrollPane.setBackground(CREAM);
        bookCardsScrollPane.getViewport().setBackground(CREAM);
        bookCardsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        bookCardsScrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        RoundedPanel inventoryPanel = new RoundedPanel(22, CREAM);
        inventoryPanel.setLayout(new BorderLayout(0, 10));
        inventoryPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel inventoryTitle = new JLabel("BOOK INVENTORY");
        inventoryTitle.setFont(new Font("Georgia", Font.BOLD, 17));
        inventoryTitle.setForeground(BURGUNDY);
        inventoryTitle.setBorder(new EmptyBorder(8, 8, 5, 8));

        JLabel inventoryFooter = new JLabel(
                "Select a book card to view its details and available actions."
        );
        inventoryFooter.setFont(new Font("Georgia", Font.PLAIN, 11));
        inventoryFooter.setForeground(TEXT_GRAY);
        inventoryFooter.setBorder(new EmptyBorder(8, 8, 5, 8));

        inventoryPanel.add(inventoryTitle, BorderLayout.NORTH);
        inventoryPanel.add(bookCardsScrollPane, BorderLayout.CENTER);
        inventoryPanel.add(inventoryFooter, BorderLayout.SOUTH);

        content.add(topSection, BorderLayout.NORTH);
        content.add(inventoryPanel, BorderLayout.CENTER);

        bookCardsPanel.addComponentListener(
                new java.awt.event.ComponentAdapter() {
                    @Override
                    public void componentResized(
                            java.awt.event.ComponentEvent e
                    ) {
                        if (bookCardsPanel.getComponentCount() > 0) {
                            arrangeBookCards();
                        }
                    }
                }
        );

        return content;
    }

    // =========================================================
    // SUMMARY CARDS
    // =========================================================

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

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Georgia", Font.BOLD, 29));
        valueLabel.setForeground(BURGUNDY);

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

    // =========================================================
    // ROUNDED BUTTON
    // =========================================================

    private class RoundedButton extends JButton {

        private final Color normalColor;
        private final Color foregroundColor;

        RoundedButton(String text, Color background, Color foreground) {

            super(text);

            normalColor = background;
            foregroundColor = foreground;

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
                    Math.max(85, getPreferredSize().width), 40
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
                    0, 0, getWidth(), getHeight(), 20, 20
            );

            g2.setColor(GOLD);
            g2.drawRoundRect(
                    0, 0, getWidth() - 1, getHeight() - 1, 20, 20
            );

            g2.dispose();

            setForeground(foregroundColor);
            super.paintComponent(g);
        }
    }

    // =========================================================
    // ROUNDED PANEL
    // =========================================================

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

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(new Color(75, 20, 36, 18));
            g2.fillRoundRect(
                    3, 5, getWidth() - 7, getHeight() - 7,
                    radius, radius
            );

            g2.setColor(backgroundColor);
            g2.fillRoundRect(
                    0, 0, getWidth() - 5, getHeight() - 5,
                    radius, radius
            );

            g2.setColor(BORDER);
            g2.drawRoundRect(
                    0, 0, getWidth() - 6, getHeight() - 6,
                    radius, radius
            );

            g2.dispose();
        }
    }

    // =========================================================
    // LOAD BOOKS
    // =========================================================

    private void loadBooks() {

        try {

            allBooks = new ArrayList<>(BookService.getAllBooks());
            updateShelfFilter();
            searchResults = new ArrayList<>(allBooks);

            applyInventoryFilters();

        } catch (SQLException e) {
            showError("Unable to load books.", e);
        }
    }

    // =========================================================
    // SEARCH BOOKS
    // =========================================================

    private void searchBooks() {

        String keyword = searchField.getText().trim();

        try {

            if (keyword.isEmpty()) {
                searchResults = new ArrayList<>(allBooks);
            } else {
                searchResults = new ArrayList<>(
                        BookService.searchBooks(keyword)
                );
            }

            applyInventoryFilters();

        } catch (SQLException e) {
            showError("Unable to search books.", e);
        }
    }

    // =========================================================
    // SHELF FILTER
    // =========================================================

    private void updateShelfFilter() {

        if (shelfFilter == null) {
            return;
        }

        String previousSelection =
                (String) shelfFilter.getSelectedItem();

        Set<String> shelves = new LinkedHashSet<>();

        for (Book book : allBooks) {

            String shelf = valueOrEmpty(book.getShelfLocation()).trim();

            if (!shelf.isEmpty()) {
                shelves.add(shelf);
            }
        }

        shelfFilter.removeAllItems();
        shelfFilter.addItem("All Books");

        for (String shelf : shelves) {
            shelfFilter.addItem(shelf);
        }

        if (previousSelection != null) {
            shelfFilter.setSelectedItem(previousSelection);

            if (shelfFilter.getSelectedIndex() == -1) {
                shelfFilter.setSelectedIndex(0);
            }
        } else {
            shelfFilter.setSelectedIndex(0);
        }
    }

    private void applyInventoryFilters() {

        if (bookCardsPanel == null || shelfFilter == null) {
            return;
        }

        String selectedShelf =
                (String) shelfFilter.getSelectedItem();

        List<Book> filteredBooks = new ArrayList<>();

        for (Book book : searchResults) {

            String bookShelf =
                    valueOrEmpty(book.getShelfLocation()).trim();

            boolean matchesShelf =
                    selectedShelf == null
                            || selectedShelf.equals("All Books")
                            || selectedShelf.equals(bookShelf);

            if (matchesShelf) {
                filteredBooks.add(book);
            }
        }

        displayBooks(filteredBooks);
    }

    // =========================================================
    // DISPLAY BOOKS
    // =========================================================

    private void displayBooks(List<Book> books) {

        tableModel.setRowCount(0);
        bookCardsPanel.removeAll();
        selectedBookId = -1;

        int totalTitles = 0;
        int totalCopies = 0;
        int totalAvailable = 0;
        int totalBorrowed = 0;

        for (Book book : books) {

            int borrowedCopies =
                    book.getTotalCopies() - book.getAvailableCopies();

            tableModel.addRow(new Object[]{
                    book.getBookId(),
                    valueOrEmpty(book.getIsbn()),
                    book.getTitle(),
                    book.getAuthor(),
                    valueOrEmpty(book.getCategory()),
                    valueOrEmpty(book.getPublisher()),
                    book.getPublicationYear() == 0
                            ? "" : book.getPublicationYear(),
                    book.getTotalCopies(),
                    book.getAvailableCopies(),
                    valueOrEmpty(book.getShelfLocation())
            });

            bookCardsPanel.add(createBookCard(book));

            totalTitles++;
            totalCopies += book.getTotalCopies();
            totalAvailable += book.getAvailableCopies();
            totalBorrowed += borrowedCopies;
        }

        titlesValue.setText(String.valueOf(totalTitles));
        copiesValue.setText(String.valueOf(totalCopies));
        availableValue.setText(String.valueOf(totalAvailable));
        borrowedValue.setText(String.valueOf(totalBorrowed));

        arrangeBookCards();

        bookCardsPanel.revalidate();
        bookCardsPanel.repaint();
    }

    // =========================================================
    // RESPONSIVE CARD ARRANGEMENT
    // =========================================================

    private void arrangeBookCards() {

        if (bookCardsPanel == null) {
            return;
        }

        Component[] cards = bookCardsPanel.getComponents();

        int panelWidth = bookCardsPanel.getWidth();

        if (panelWidth <= 0) {
            panelWidth = 1100;
        }

        int columns;

        if (panelWidth >= 1100) {
            columns = 4;
        } else if (panelWidth >= 800) {
            columns = 3;
        } else if (panelWidth >= 520) {
            columns = 2;
        } else {
            columns = 1;
        }

        bookCardsPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        for (int i = 0; i < cards.length; i++) {

            gbc.gridx = i % columns;
            gbc.gridy = i / columns;
            gbc.weightx = 1;

            bookCardsPanel.add(cards[i], gbc);
        }

        gbc.gridx = 0;
        gbc.gridy = (cards.length + columns - 1) / columns;
        gbc.gridwidth = columns;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        bookCardsPanel.add(Box.createVerticalGlue(), gbc);

        bookCardsPanel.revalidate();
        bookCardsPanel.repaint();
    }

    // =========================================================
    // CREATE BOOK CARD
    // =========================================================

    private JPanel createBookCard(Book book) {

        RoundedPanel card = new RoundedPanel(18, WHITE);

        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(13, 13, 13, 13));
        card.setPreferredSize(new Dimension(260, 390));
        card.setMinimumSize(new Dimension(210, 370));

        JPanel topPanel = new JPanel(new BorderLayout(9, 5));
        topPanel.setOpaque(false);

        JPanel coverThumbnail = createCardCover(book.getBookId());

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(
                escapeHtml(valueOrEmpty(book.getTitle()))
        );
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 14));
        titleLabel.setForeground(BURGUNDY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setToolTipText(valueOrEmpty(book.getTitle()));

        JLabel idLabel = new JLabel("BOOK ID  #" + book.getBookId());
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        idLabel.setForeground(TEXT_GRAY);
        idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        titlePanel.add(Box.createVerticalStrut(8));
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(7));
        titlePanel.add(idLabel);

        topPanel.add(coverThumbnail, BorderLayout.WEST);
        topPanel.add(titlePanel, BorderLayout.CENTER);

        int available = book.getAvailableCopies();
        int total = book.getTotalCopies();

        boolean isAvailable = available > 0;

        JLabel availabilityLabel = new JLabel(
                isAvailable ? "AVAILABLE" : "OUT OF STOCK"
        );

        availabilityLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 10)
        );

        availabilityLabel.setOpaque(true);
        availabilityLabel.setBorder(
                new EmptyBorder(5, 9, 5, 9)
        );

        if (isAvailable) {
            availabilityLabel.setBackground(new Color(224, 241, 226));
            availabilityLabel.setForeground(new Color(39, 112, 59));
        } else {
            availabilityLabel.setBackground(new Color(250, 226, 222));
            availabilityLabel.setForeground(new Color(164, 55, 48));
        }

        JPanel availabilityPanel = new JPanel(new BorderLayout());
        availabilityPanel.setOpaque(false);
        availabilityPanel.add(availabilityLabel, BorderLayout.WEST);

        JLabel copiesLabel = new JLabel(
                available + " / " + total + " copies available"
        );

        copiesLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        copiesLabel.setForeground(TEXT_DARK);

        availabilityPanel.add(copiesLabel, BorderLayout.EAST);

        JPanel metadataPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        metadataPanel.setOpaque(false);
        metadataPanel.setBorder(new EmptyBorder(7, 0, 5, 0));

        addMetadata(metadataPanel, "ISBN", book.getIsbn());
        addMetadata(metadataPanel, "Author", book.getAuthor());
        addMetadata(metadataPanel, "Category", book.getCategory());
        addMetadata(metadataPanel, "Publisher", book.getPublisher());

        addMetadata(
                metadataPanel,
                "Publication Year",
                book.getPublicationYear() == 0
                        ? "—" : String.valueOf(book.getPublicationYear())
        );

        addMetadata(
                metadataPanel,
                "Total Copies",
                String.valueOf(book.getTotalCopies())
        );

        addMetadata(
                metadataPanel,
                "Available Copies",
                String.valueOf(book.getAvailableCopies())
        );

        addMetadata(
                metadataPanel,
                "Borrowed Copies",
                String.valueOf(
                        book.getTotalCopies() - book.getAvailableCopies()
                )
        );

        addMetadata(
                metadataPanel,
                "Shelf Location",
                book.getShelfLocation()
        );

        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setOpaque(false);

        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(metadataPanel, BorderLayout.CENTER);
        contentPanel.add(availabilityPanel, BorderLayout.SOUTH);

        card.add(contentPanel, BorderLayout.CENTER);

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Clicking anywhere inside the card opens the details window.
        addBookCardClickListener(card, book);

        return card;
    }

    private void addBookCardClickListener(Component component, Book book) {

        component.setCursor(new Cursor(Cursor.HAND_CURSOR));

        component.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (SwingUtilities.isRightMouseButton(e)) {
                    return;
                }

                selectedBookId = book.getBookId();
                updateCardSelection();
                showBookDetails(book);
            }
        });

        if (component instanceof Container container) {

            for (Component child : container.getComponents()) {
                addBookCardClickListener(child, book);
            }
        }
    }

    // =========================================================
    // BOOK DETAILS WINDOW
    // =========================================================

    private void showBookDetails(Book book) {

        try {

            BookExtraDetails extra =
                    getBookExtraDetails(book.getBookId());

            JDialog dialog = new JDialog(
                    this,
                    "Book Details",
                    true
            );

            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setLayout(new BorderLayout());
            dialog.getContentPane().setBackground(CREAM);

            // HEADER

            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(BURGUNDY);
            header.setBorder(new EmptyBorder(20, 25, 20, 25));

            JPanel headingPanel = new JPanel();
            headingPanel.setOpaque(false);
            headingPanel.setLayout(
                    new BoxLayout(headingPanel, BoxLayout.Y_AXIS)
            );

            JLabel heading = new JLabel("BOOK DETAILS");
            heading.setFont(new Font("Georgia", Font.BOLD, 20));
            heading.setForeground(GOLD);

            JLabel subtitle = new JLabel(
                    "Complete information about this library book"
            );
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            subtitle.setForeground(CREAM);

            headingPanel.add(heading);
            headingPanel.add(Box.createVerticalStrut(5));
            headingPanel.add(subtitle);

            JLabel recordId = new JLabel(
                    "BOOK ID  #" + book.getBookId()
            );
            recordId.setFont(new Font("Segoe UI", Font.BOLD, 12));
            recordId.setForeground(WHITE);

            header.add(headingPanel, BorderLayout.WEST);
            header.add(recordId, BorderLayout.EAST);

            // DETAILS BODY

            JPanel body = new JPanel(new BorderLayout(20, 0));
            body.setBackground(CREAM);
            body.setBorder(new EmptyBorder(25, 25, 25, 25));

            RoundedPanel coverPanel = new RoundedPanel(18, WHITE);
            coverPanel.setLayout(new BorderLayout(0, 12));
            coverPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
            coverPanel.setPreferredSize(new Dimension(230, 340));

            JLabel coverHeading = new JLabel("BOOK COVER");
            coverHeading.setFont(new Font("Georgia", Font.BOLD, 12));
            coverHeading.setForeground(BURGUNDY);

            JLabel coverImage = new JLabel(
                    "▤",
                    SwingConstants.CENTER
            );
            coverImage.setFont(new Font("Georgia", Font.BOLD, 65));
            coverImage.setForeground(GOLD);
            coverImage.setOpaque(true);
            coverImage.setBackground(new Color(247, 243, 235));
            coverImage.setBorder(BorderFactory.createLineBorder(BORDER));

            if (!extra.coverImage.isBlank()) {

                File imageFile = new File(extra.coverImage);

                if (imageFile.exists()) {

                    BufferedImage image = ImageIO.read(imageFile);

                    if (image != null) {

                        int width = 190;
                        int height = 270;

                        double scale = Math.min(
                                (double) width / image.getWidth(),
                                (double) height / image.getHeight()
                        );

                        int scaledWidth = Math.max(
                                1, (int) (image.getWidth() * scale)
                        );

                        int scaledHeight = Math.max(
                                1, (int) (image.getHeight() * scale)
                        );

                        Image scaled = image.getScaledInstance(
                                scaledWidth,
                                scaledHeight,
                                Image.SCALE_SMOOTH
                        );

                        coverImage.setText("");
                        coverImage.setIcon(new ImageIcon(scaled));
                    }
                }
            }

            coverPanel.add(coverHeading, BorderLayout.NORTH);
            coverPanel.add(coverImage, BorderLayout.CENTER);

            // BOOK INFORMATION

            RoundedPanel informationPanel =
                    new RoundedPanel(18, WHITE);

            informationPanel.setLayout(
                    new BorderLayout(0, 15)
            );
            informationPanel.setBorder(
                    new EmptyBorder(20, 22, 20, 22)
            );

            JLabel bookTitle = new JLabel(
                    escapeHtml(valueOrEmpty(book.getTitle()))
            );
            bookTitle.setFont(new Font("Georgia", Font.BOLD, 21));
            bookTitle.setForeground(BURGUNDY);

            JLabel authorLabel = new JLabel(
                    "by " + valueOrEmpty(book.getAuthor())
            );
            authorLabel.setFont(
                    new Font("Segoe UI", Font.PLAIN, 13)
            );
            authorLabel.setForeground(TEXT_GRAY);

            JPanel titleArea = new JPanel();
            titleArea.setOpaque(false);
            titleArea.setLayout(
                    new BoxLayout(titleArea, BoxLayout.Y_AXIS)
            );

            titleArea.add(bookTitle);
            titleArea.add(Box.createVerticalStrut(5));
            titleArea.add(authorLabel);

            JPanel detailsGrid = new JPanel(new GridLayout(0, 2, 15, 16));
            detailsGrid.setOpaque(false);

            addMetadata(detailsGrid, "ISBN", book.getIsbn());
            addMetadata(detailsGrid, "Category", book.getCategory());
            addMetadata(detailsGrid, "Publisher", book.getPublisher());

            addMetadata(
                    detailsGrid,
                    "Publication Year",
                    book.getPublicationYear() == 0
                            ? "—" : String.valueOf(book.getPublicationYear())
            );

            addMetadata(
                    detailsGrid,
                    "Total Copies",
                    String.valueOf(book.getTotalCopies())
            );

            addMetadata(
                    detailsGrid,
                    "Available Copies",
                    String.valueOf(book.getAvailableCopies())
            );

            addMetadata(
                    detailsGrid,
                    "Borrowed Copies",
                    String.valueOf(
                            book.getTotalCopies() - book.getAvailableCopies()
                    )
            );

            addMetadata(
                    detailsGrid,
                    "Shelf Location",
                    book.getShelfLocation()
            );

            JPanel descriptionPanel = new JPanel(new BorderLayout(0, 6));
            descriptionPanel.setOpaque(false);

            JLabel descriptionHeading = new JLabel("DESCRIPTION");
            descriptionHeading.setFont(
                    new Font("Segoe UI", Font.BOLD, 10)
            );
            descriptionHeading.setForeground(TEXT_GRAY);

            JTextArea description = new JTextArea(
                    extra.description.isBlank()
                            ? "No description provided."
                            : extra.description
            );

            description.setFont(
                    new Font("Segoe UI", Font.PLAIN, 12)
            );
            description.setForeground(TEXT_DARK);
            description.setBackground(new Color(249, 246, 240));
            description.setLineWrap(true);
            description.setWrapStyleWord(true);
            description.setEditable(false);
            description.setFocusable(false);
            description.setBorder(
                    new EmptyBorder(10, 10, 10, 10)
            );
            description.setRows(4);

            JScrollPane descriptionScroll = new JScrollPane(description);
            descriptionScroll.setBorder(
                    BorderFactory.createLineBorder(BORDER)
            );
            descriptionScroll.setPreferredSize(
                    new Dimension(450, 100)
            );

            descriptionPanel.add(
                    descriptionHeading, BorderLayout.NORTH
            );
            descriptionPanel.add(
                    descriptionScroll, BorderLayout.CENTER
            );

            JPanel informationContent = new JPanel(
                    new BorderLayout(0, 18)
            );
            informationContent.setOpaque(false);

            informationContent.add(titleArea, BorderLayout.NORTH);
            informationContent.add(detailsGrid, BorderLayout.CENTER);
            informationContent.add(descriptionPanel, BorderLayout.SOUTH);

            informationPanel.add(
                    informationContent, BorderLayout.CENTER
            );

            body.add(coverPanel, BorderLayout.WEST);
            body.add(informationPanel, BorderLayout.CENTER);

            // FOOTER ACTIONS

            JPanel footer = new JPanel(
                    new BorderLayout()
            );
            footer.setBackground(WHITE);
            footer.setBorder(new EmptyBorder(13, 22, 13, 22));

            JLabel hint = new JLabel("Book ID: " + book.getBookId());
            hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            hint.setForeground(TEXT_GRAY);

            JPanel actionButtons = new JPanel(
                    new FlowLayout(FlowLayout.RIGHT, 10, 0)
            );
            actionButtons.setOpaque(false);

            RoundedButton closeButton = new RoundedButton(
                    "Close", new Color(235, 230, 224), TEXT_DARK
            );

            RoundedButton editButton = new RoundedButton(
                    "Edit Book", GOLD, DARK_BURGUNDY
            );

            RoundedButton deleteButton = new RoundedButton(
                    "Delete Book", new Color(170, 55, 65), WHITE
            );

            closeButton.addActionListener(e -> dialog.dispose());

            editButton.addActionListener(e -> {
                dialog.dispose();
                editBookDetails(book.getBookId());
            });

            deleteButton.addActionListener(e -> {
                dialog.dispose();
                deleteBookById(book.getBookId());
            });

            actionButtons.add(closeButton);
            actionButtons.add(editButton);
            actionButtons.add(deleteButton);

            footer.add(hint, BorderLayout.WEST);
            footer.add(actionButtons, BorderLayout.EAST);

            dialog.add(header, BorderLayout.NORTH);
            dialog.add(body, BorderLayout.CENTER);
            dialog.add(footer, BorderLayout.SOUTH);

            dialog.setMinimumSize(new Dimension(850, 570));
            dialog.setSize(1000, 650);
            dialog.setResizable(true);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

        } catch (SQLException e) {
            showError("Unable to load book details.", e);
        } catch (Exception e) {
            showError("Unable to display the book details.", e);
        }
    }

    // =========================================================
    // CARD COVER IMAGE
    // =========================================================

    private JPanel createCardCover(int bookId) {

        JPanel coverPanel = new JPanel(new BorderLayout());
        coverPanel.setPreferredSize(new Dimension(76, 105));
        coverPanel.setMinimumSize(new Dimension(76, 105));
        coverPanel.setBackground(new Color(247, 243, 235));
        coverPanel.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        JLabel imageLabel = new JLabel("▤", SwingConstants.CENTER);
        imageLabel.setFont(new Font("Georgia", Font.BOLD, 35));
        imageLabel.setForeground(GOLD);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        try {

            BookExtraDetails details = getBookExtraDetails(bookId);

            if (!details.coverImage.isBlank()) {

                File imageFile = new File(details.coverImage);

                if (imageFile.exists()) {

                    BufferedImage image = ImageIO.read(imageFile);

                    if (image != null) {

                        int width = 72;
                        int height = 101;

                        double scale = Math.min(
                                (double) width / image.getWidth(),
                                (double) height / image.getHeight()
                        );

                        int scaledWidth = Math.max(
                                1, (int) (image.getWidth() * scale)
                        );

                        int scaledHeight = Math.max(
                                1, (int) (image.getHeight() * scale)
                        );

                        Image scaled = image.getScaledInstance(
                                scaledWidth,
                                scaledHeight,
                                Image.SCALE_SMOOTH
                        );

                        imageLabel.setText("");
                        imageLabel.setIcon(new ImageIcon(scaled));
                    }
                }
            }

        } catch (Exception e) {
            System.err.println(
                    "Unable to display cover image for book "
                            + bookId + ": " + e.getMessage()
            );
        }

        coverPanel.add(imageLabel, BorderLayout.CENTER);

        return coverPanel;
    }

    // =========================================================
    // CARD METADATA
    // =========================================================

    private void addMetadata(
            JPanel panel,
            String label,
            String value
    ) {

        JPanel item = new JPanel();
        item.setOpaque(false);
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));

        JLabel labelText = new JLabel(label.toUpperCase());
        labelText.setFont(new Font("Segoe UI", Font.BOLD, 9));
        labelText.setForeground(TEXT_GRAY);

        String displayValue = value == null || value.isBlank()
                ? "—" : value;

        JLabel valueText = new JLabel(
                "<html><body style='width:105px'>"
                        + escapeHtml(displayValue)
                        + "</body></html>"
        );

        valueText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        valueText.setForeground(TEXT_DARK);

        item.add(labelText);
        item.add(Box.createVerticalStrut(3));
        item.add(valueText);

        panel.add(item);
    }

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    // =========================================================
    // CARD SELECTION
    // =========================================================

    private void updateCardSelection() {

        for (Component component : bookCardsPanel.getComponents()) {
            if (component instanceof RoundedPanel) {
                component.repaint();
            }
        }

        bookCardsPanel.repaint();
    }

    // =========================================================
    // BOOK COVER IMAGE PICKER
    // =========================================================

    private class CoverImagePicker {

        private final JPanel panel;
        private final JLabel previewLabel;
        private final JLabel fileNameLabel;

        private String existingImagePath;
        private File selectedFile;

        CoverImagePicker(String existingImagePath) {

            this.existingImagePath = existingImagePath == null
                    ? "" : existingImagePath;

            panel = new JPanel(new BorderLayout(0, 10));
            panel.setOpaque(false);
            panel.setBorder(new EmptyBorder(4, 2, 2, 2));

            previewLabel = new JLabel(
                    "No Cover Image", SwingConstants.CENTER
            );

            previewLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            previewLabel.setForeground(TEXT_GRAY);
            previewLabel.setPreferredSize(new Dimension(220, 245));
            previewLabel.setMinimumSize(new Dimension(180, 200));
            previewLabel.setOpaque(true);
            previewLabel.setBackground(new Color(247, 243, 235));
            previewLabel.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(BORDER),
                            new EmptyBorder(6, 6, 6, 6)
                    )
            );

            RoundedButton chooseButton = new RoundedButton(
                    "Choose Image", BURGUNDY, WHITE
            );

            chooseButton.setPreferredSize(new Dimension(200, 42));
            chooseButton.addActionListener(e -> chooseImage());

            fileNameLabel = new JLabel(
                    "No image selected", SwingConstants.CENTER
            );

            fileNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            fileNameLabel.setForeground(TEXT_GRAY);

            JPanel bottomPanel = new JPanel();
            bottomPanel.setOpaque(false);
            bottomPanel.setLayout(
                    new BoxLayout(bottomPanel, BoxLayout.Y_AXIS)
            );

            chooseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            fileNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            bottomPanel.add(chooseButton);
            bottomPanel.add(Box.createVerticalStrut(6));
            bottomPanel.add(fileNameLabel);

            panel.add(previewLabel, BorderLayout.CENTER);
            panel.add(bottomPanel, BorderLayout.SOUTH);

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

            int result = chooser.showOpenDialog(
                    ManageBooksFrame.this
            );

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

                int frameWidth = 205;
                int frameHeight = 230;

                double scale = Math.min(
                        (double) frameWidth / image.getWidth(),
                        (double) frameHeight / image.getHeight()
                );

                int scaledWidth = Math.max(
                        1, (int) (image.getWidth() * scale)
                );

                int scaledHeight = Math.max(
                        1, (int) (image.getHeight() * scale)
                );

                Image scaledImage = image.getScaledInstance(
                        scaledWidth,
                        scaledHeight,
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

    // =========================================================
    // ADD BOOK
    // =========================================================

    private void showAddBookDialog() {

        JTextField isbnField = createFormField("");
        JTextField titleField = createFormField("");
        JTextField authorField = createFormField("");
        JTextField categoryField = createFormField("");
        JTextField publisherField = createFormField("");
        JTextField yearField = createFormField("");
        JTextField copiesField = createFormField("");
        JTextField shelfField = createFormField("");

        CoverImagePicker coverPicker = new CoverImagePicker("");

        JTextArea descriptionField = new JTextArea(7, 30);
        descriptionField.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );
        descriptionField.setForeground(TEXT_DARK);
        descriptionField.setBackground(WHITE);
        descriptionField.setCaretColor(BURGUNDY);
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        descriptionField.setMargin(new Insets(10, 12, 10, 12));
        descriptionField.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );
        descriptionField.setPreferredSize(
                new Dimension(560, 125)
        );

        JDialog dialog = new JDialog(
                this, "Add New Book", true
        );

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(CREAM);

        JPanel header = new JPanel(new BorderLayout(10, 5));
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(20, 28, 20, 28));

        JPanel headerText = new JPanel();
        headerText.setOpaque(false);
        headerText.setLayout(
                new BoxLayout(headerText, BoxLayout.Y_AXIS)
        );

        JLabel heading = new JLabel("ADD NEW BOOK");
        heading.setFont(new Font("Georgia", Font.BOLD, 21));
        heading.setForeground(GOLD);

        JLabel subheading = new JLabel(
                "Enter the book information, publication details, and inventory."
        );
        subheading.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subheading.setForeground(CREAM);

        headerText.add(heading);
        headerText.add(Box.createVerticalStrut(5));
        headerText.add(subheading);

        JLabel recordId = new JLabel("NEW RECORD");
        recordId.setFont(new Font("Segoe UI", Font.BOLD, 12));
        recordId.setForeground(WHITE);

        header.add(headerText, BorderLayout.WEST);
        header.add(recordId, BorderLayout.EAST);

        JPanel formContent = new JPanel();
        formContent.setOpaque(false);
        formContent.setLayout(
                new BoxLayout(formContent, BoxLayout.Y_AXIS)
        );
        formContent.setBorder(new EmptyBorder(3, 3, 3, 12));

        JPanel informationSection =
                createEditSection("BOOK INFORMATION");

        JPanel informationGrid = createEditGrid();

        addEditField(informationGrid, "ISBN", isbnField);
        addEditField(informationGrid, "BOOK TITLE *", titleField);
        addEditField(informationGrid, "AUTHOR *", authorField);
        addEditField(
                informationGrid, "CATEGORY / GENRE", categoryField
        );

        informationSection.add(
                informationGrid, BorderLayout.CENTER
        );

        JPanel publicationSection =
                createEditSection("PUBLICATION & INVENTORY");

        JPanel publicationGrid = createEditGrid();

        addEditField(publicationGrid, "PUBLISHER", publisherField);
        addEditField(
                publicationGrid, "PUBLICATION YEAR", yearField
        );
        addEditField(
                publicationGrid, "TOTAL COPIES *", copiesField
        );
        addEditField(
                publicationGrid, "SHELF LOCATION", shelfField
        );

        publicationSection.add(
                publicationGrid, BorderLayout.CENTER
        );

        JPanel descriptionSection =
                createEditSection("BOOK DESCRIPTION");

        descriptionSection.add(
                descriptionField, BorderLayout.CENTER
        );

        formContent.add(informationSection);
        formContent.add(Box.createVerticalStrut(14));
        formContent.add(publicationSection);
        formContent.add(Box.createVerticalStrut(14));
        formContent.add(descriptionSection);

        RoundedPanel coverSection =
                new RoundedPanel(18, WHITE);

        coverSection.setLayout(new BorderLayout(0, 12));
        coverSection.setBorder(
                new EmptyBorder(16, 14, 16, 14)
        );
        coverSection.setPreferredSize(
                new Dimension(270, 390)
        );
        coverSection.setMinimumSize(
                new Dimension(250, 370)
        );

        JLabel coverHeading = new JLabel("BOOK COVER");
        coverHeading.setFont(
                new Font("Georgia", Font.BOLD, 13)
        );
        coverHeading.setForeground(BURGUNDY);

        JLabel coverHint = new JLabel("JPG or PNG image");
        coverHint.setFont(
                new Font("Segoe UI", Font.PLAIN, 11)
        );
        coverHint.setForeground(TEXT_GRAY);

        JPanel coverTitlePanel = new JPanel();
        coverTitlePanel.setOpaque(false);
        coverTitlePanel.setLayout(
                new BoxLayout(coverTitlePanel, BoxLayout.Y_AXIS)
        );

        coverTitlePanel.add(coverHeading);
        coverTitlePanel.add(Box.createVerticalStrut(5));
        coverTitlePanel.add(coverHint);

        coverSection.add(coverTitlePanel, BorderLayout.NORTH);
        coverSection.add(
                coverPicker.getPanel(), BorderLayout.CENTER
        );

        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(
                new BoxLayout(rightPanel, BoxLayout.Y_AXIS)
        );

        rightPanel.setPreferredSize(new Dimension(320, 430));
        rightPanel.setMinimumSize(new Dimension(300, 430));

        coverSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(coverSection);

        JPanel mainContent = new JPanel(
                new BorderLayout(18, 0)
        );

        mainContent.setBackground(CREAM);
        mainContent.setBorder(
                new EmptyBorder(20, 25, 18, 25)
        );

        formContent.setMinimumSize(new Dimension(500, 580));
        formContent.setPreferredSize(new Dimension(600, 620));

        mainContent.add(formContent, BorderLayout.CENTER);
        mainContent.add(rightPanel, BorderLayout.EAST);

        JScrollPane bodyScroll = new JScrollPane(mainContent);
        bodyScroll.setBorder(null);
        bodyScroll.setOpaque(false);
        bodyScroll.getViewport().setOpaque(false);

        bodyScroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        bodyScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        bodyScroll.getVerticalScrollBar().setUnitIncrement(18);

        mainContent.setPreferredSize(new Dimension(950, 700));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(WHITE);
        footer.setOpaque(true);
        footer.setBorder(
                new EmptyBorder(12, 25, 12, 25)
        );

        footer.setPreferredSize(new Dimension(0, 76));
        footer.setMinimumSize(new Dimension(0, 76));

        JLabel requiredNote = new JLabel("* Required fields");
        requiredNote.setFont(
                new Font("Segoe UI", Font.ITALIC, 11)
        );
        requiredNote.setForeground(TEXT_GRAY);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        buttons.setOpaque(false);

        JButton cancelButton = new JButton("Cancel");
        styleDialogButton(
                cancelButton,
                new Color(235, 230, 224),
                TEXT_DARK
        );
        cancelButton.setPreferredSize(new Dimension(115, 42));

        JButton addBookButton = new JButton("Add Book");
        styleDialogButton(addBookButton, BURGUNDY, WHITE);
        addBookButton.setPreferredSize(new Dimension(155, 42));
        addBookButton.setMinimumSize(new Dimension(155, 42));

        cancelButton.addActionListener(e -> dialog.dispose());

        addBookButton.addActionListener(e -> {

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

                int copies = Integer.parseInt(
                        copiesField.getText().trim()
                );

                if (copies < 1) {
                    showWarning(
                            "Total copies must be at least 1.",
                            "Invalid Copy Count"
                    );
                    return;
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

                if (!BookService.addBook(book)) {
                    showWarning(
                            "The book could not be added.",
                            "Add Book Failed"
                    );
                    return;
                }

                int newBookId = findNewBookId(book);

                if (newBookId <= 0) {
                    dialog.dispose();
                    loadBooks();

                    showWarning(
                            "The book was added, but its new ID could not be retrieved. "
                                    + "The saved book record has not been deleted.",
                            "Book Added"
                    );
                    return;
                }

                String coverPath = coverPicker.saveImage();

                updateOptionalBookDetails(
                        newBookId,
                        coverPath,
                        descriptionField.getText().trim()
                );

                dialog.dispose();

                showSuccess("Book added successfully!");

                searchField.setText("");
                shelfFilter.setSelectedIndex(0);
                loadBooks();

            } catch (NumberFormatException ex) {

                showWarning(
                        "Enter a valid publication year and total copies.",
                        "Invalid Input"
                );

            } catch (IllegalArgumentException ex) {

                showWarning(ex.getMessage(), "Invalid Input");

            } catch (SQLException ex) {

                showError(
                        "Unable to save the book information.",
                        ex
                );

            } catch (java.io.IOException ex) {

                showError(
                        "The book record was saved, but its cover image could not be saved.",
                        ex
                );
            }
        });

        buttons.add(cancelButton);
        buttons.add(addBookButton);

        footer.add(requiredNote, BorderLayout.WEST);
        footer.add(buttons, BorderLayout.EAST);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(bodyScroll, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setMinimumSize(new Dimension(900, 620));
        dialog.setSize(1000, 700);
        dialog.setResizable(true);

        dialog.getRootPane().setDefaultButton(addBookButton);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // =========================================================
    // FIND NEW BOOK ID
    // =========================================================

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
             PreparedStatement statement = connection.prepareStatement(sql)) {

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

    // =========================================================
    // EDIT BOOK WINDOW
    // =========================================================

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
                    extra.description, 7, 30
            );

            descriptionField.setFont(
                    new Font("Segoe UI", Font.PLAIN, 13)
            );
            descriptionField.setForeground(TEXT_DARK);
            descriptionField.setBackground(WHITE);
            descriptionField.setCaretColor(BURGUNDY);
            descriptionField.setLineWrap(true);
            descriptionField.setWrapStyleWord(true);
            descriptionField.setMargin(
                    new Insets(10, 12, 10, 12)
            );
            descriptionField.setBorder(
                    BorderFactory.createLineBorder(BORDER)
            );
            descriptionField.setPreferredSize(
                    new Dimension(560, 125)
            );

            JDialog dialog = new JDialog(
                    this, "Edit Book Record", true
            );

            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setLayout(new BorderLayout());
            dialog.getContentPane().setBackground(CREAM);

            JPanel header = new JPanel(new BorderLayout(10, 5));
            header.setBackground(BURGUNDY);
            header.setBorder(new EmptyBorder(20, 28, 20, 28));

            JPanel headerText = new JPanel();
            headerText.setOpaque(false);
            headerText.setLayout(
                    new BoxLayout(headerText, BoxLayout.Y_AXIS)
            );

            JLabel heading = new JLabel("EDIT BOOK RECORD");
            heading.setFont(new Font("Georgia", Font.BOLD, 21));
            heading.setForeground(GOLD);

            JLabel subheading = new JLabel(
                    "Update book information, publication details, and inventory."
            );
            subheading.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            subheading.setForeground(CREAM);

            headerText.add(heading);
            headerText.add(Box.createVerticalStrut(5));
            headerText.add(subheading);

            JLabel recordId = new JLabel("BOOK ID  #" + bookId);
            recordId.setFont(new Font("Segoe UI", Font.BOLD, 12));
            recordId.setForeground(WHITE);

            header.add(headerText, BorderLayout.WEST);
            header.add(recordId, BorderLayout.EAST);

            JPanel formContent = new JPanel();
            formContent.setOpaque(false);
            formContent.setLayout(
                    new BoxLayout(formContent, BoxLayout.Y_AXIS)
            );
            formContent.setBorder(new EmptyBorder(3, 3, 3, 12));

            JPanel informationSection =
                    createEditSection("BOOK INFORMATION");

            JPanel informationGrid = createEditGrid();

            addEditField(informationGrid, "ISBN", isbnField);
            addEditField(informationGrid, "BOOK TITLE *", titleField);
            addEditField(informationGrid, "AUTHOR *", authorField);
            addEditField(
                    informationGrid, "CATEGORY / GENRE", categoryField
            );

            informationSection.add(
                    informationGrid, BorderLayout.CENTER
            );

            JPanel publicationSection =
                    createEditSection("PUBLICATION & INVENTORY");

            JPanel publicationGrid = createEditGrid();

            addEditField(publicationGrid, "PUBLISHER", publisherField);
            addEditField(
                    publicationGrid, "PUBLICATION YEAR", yearField
            );
            addEditField(
                    publicationGrid, "TOTAL COPIES *", totalField
            );
            addEditField(
                    publicationGrid, "SHELF LOCATION", shelfField
            );

            publicationSection.add(
                    publicationGrid, BorderLayout.CENTER
            );

            JPanel descriptionSection =
                    createEditSection("BOOK DESCRIPTION");

            descriptionSection.add(
                    descriptionField, BorderLayout.CENTER
            );

            formContent.add(informationSection);
            formContent.add(Box.createVerticalStrut(14));
            formContent.add(publicationSection);
            formContent.add(Box.createVerticalStrut(14));
            formContent.add(descriptionSection);

            RoundedPanel coverSection =
                    new RoundedPanel(18, WHITE);

            coverSection.setLayout(new BorderLayout(0, 12));
            coverSection.setBorder(
                    new EmptyBorder(16, 14, 16, 14)
            );
            coverSection.setPreferredSize(
                    new Dimension(270, 390)
            );
            coverSection.setMinimumSize(
                    new Dimension(250, 370)
            );

            JLabel coverHeading = new JLabel("BOOK COVER");
            coverHeading.setFont(
                    new Font("Georgia", Font.BOLD, 13)
            );
            coverHeading.setForeground(BURGUNDY);

            JLabel coverHint = new JLabel("JPG or PNG image");
            coverHint.setFont(
                    new Font("Segoe UI", Font.PLAIN, 11)
            );
            coverHint.setForeground(TEXT_GRAY);

            JPanel coverTitlePanel = new JPanel();
            coverTitlePanel.setOpaque(false);
            coverTitlePanel.setLayout(
                    new BoxLayout(coverTitlePanel, BoxLayout.Y_AXIS)
            );

            coverTitlePanel.add(coverHeading);
            coverTitlePanel.add(Box.createVerticalStrut(5));
            coverTitlePanel.add(coverHint);

            coverSection.add(coverTitlePanel, BorderLayout.NORTH);
            coverSection.add(
                    coverPicker.getPanel(), BorderLayout.CENTER
            );

            RoundedPanel inventoryNote =
                    new RoundedPanel(16, WHITE);

            inventoryNote.setLayout(
                    new BoxLayout(inventoryNote, BoxLayout.Y_AXIS)
            );

            inventoryNote.setBorder(
                    new EmptyBorder(17, 19, 20, 19)
            );

            int borrowedCopies =
                    Integer.parseInt(total)
                            - Integer.parseInt(available);

            JLabel inventoryTitle =
                    new JLabel("CURRENT INVENTORY");

            inventoryTitle.setFont(
                    new Font("Georgia", Font.BOLD, 12)
            );
            inventoryTitle.setForeground(BURGUNDY);

            JLabel availableLabel =
                    new JLabel("Available Copies: " + available);

            availableLabel.setFont(
                    new Font("Segoe UI", Font.PLAIN, 12)
            );
            availableLabel.setForeground(TEXT_DARK);

            JLabel borrowedLabel =
                    new JLabel("Borrowed Copies: " + borrowedCopies);

            borrowedLabel.setFont(
                    new Font("Segoe UI", Font.PLAIN, 12)
            );
            borrowedLabel.setForeground(TEXT_DARK);

            JTextArea inventoryHint = new JTextArea(
                    "Borrowed copies are protected. Total copies cannot be reduced below the number currently borrowed."
            );

            inventoryHint.setFont(
                    new Font("Segoe UI", Font.ITALIC, 11)
            );
            inventoryHint.setForeground(TEXT_GRAY);
            inventoryHint.setOpaque(false);
            inventoryHint.setLineWrap(true);
            inventoryHint.setWrapStyleWord(true);
            inventoryHint.setEditable(false);
            inventoryHint.setFocusable(false);
            inventoryHint.setBorder(null);
            inventoryHint.setPreferredSize(
                    new Dimension(270, 65)
            );
            inventoryHint.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 80)
            );

            inventoryNote.add(inventoryTitle);
            inventoryNote.add(Box.createVerticalStrut(15));
            inventoryNote.add(availableLabel);
            inventoryNote.add(Box.createVerticalStrut(9));
            inventoryNote.add(borrowedLabel);
            inventoryNote.add(Box.createVerticalStrut(15));
            inventoryNote.add(inventoryHint);

            JPanel rightPanel = new JPanel();
            rightPanel.setOpaque(false);
            rightPanel.setLayout(
                    new BoxLayout(rightPanel, BoxLayout.Y_AXIS)
            );

            rightPanel.setPreferredSize(
                    new Dimension(320, 740)
            );
            rightPanel.setMinimumSize(
                    new Dimension(300, 740)
            );

            coverSection.setAlignmentX(Component.LEFT_ALIGNMENT);
            inventoryNote.setAlignmentX(Component.LEFT_ALIGNMENT);

            rightPanel.add(coverSection);
            rightPanel.add(Box.createVerticalStrut(14));
            rightPanel.add(inventoryNote);

            JPanel mainContent = new JPanel(
                    new BorderLayout(18, 0)
            );

            mainContent.setBackground(CREAM);
            mainContent.setBorder(
                    new EmptyBorder(20, 25, 18, 25)
            );

            formContent.setMinimumSize(new Dimension(500, 700));
            formContent.setPreferredSize(new Dimension(600, 740));

            mainContent.add(formContent, BorderLayout.CENTER);
            mainContent.add(rightPanel, BorderLayout.EAST);

            JScrollPane bodyScroll = new JScrollPane(mainContent);

            bodyScroll.setBorder(null);
            bodyScroll.setOpaque(false);
            bodyScroll.getViewport().setOpaque(false);

            bodyScroll.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            );

            bodyScroll.setHorizontalScrollBarPolicy(
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            );

            bodyScroll.getVerticalScrollBar().setUnitIncrement(18);

            mainContent.setPreferredSize(
                    new Dimension(950, 780)
            );

            JPanel footer = new JPanel(new BorderLayout());

            footer.setBackground(WHITE);
            footer.setOpaque(true);
            footer.setBorder(
                    new EmptyBorder(12, 25, 12, 25)
            );

            footer.setPreferredSize(new Dimension(0, 76));
            footer.setMinimumSize(new Dimension(0, 76));

            JLabel requiredNote = new JLabel("* Required fields");

            requiredNote.setFont(
                    new Font("Segoe UI", Font.ITALIC, 11)
            );
            requiredNote.setForeground(TEXT_GRAY);

            JPanel buttons = new JPanel(
                    new FlowLayout(FlowLayout.RIGHT, 10, 0)
            );
            buttons.setOpaque(false);

            JButton cancelButton = new JButton("Cancel");

            styleDialogButton(
                    cancelButton,
                    new Color(235, 230, 224),
                    TEXT_DARK
            );

            cancelButton.setPreferredSize(new Dimension(115, 42));

            JButton saveButton = new JButton("Save Changes");

            styleDialogButton(saveButton, BURGUNDY, WHITE);

            saveButton.setPreferredSize(new Dimension(155, 42));
            saveButton.setMinimumSize(new Dimension(155, 42));

            cancelButton.addActionListener(e -> dialog.dispose());

            saveButton.addActionListener(e -> {

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

                    int newTotal = Integer.parseInt(
                            totalField.getText().trim()
                    );

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

                    dialog.dispose();

                    showSuccess(
                            "Book details updated successfully!"
                    );

                    loadBooks();

                } catch (NumberFormatException ex) {

                    showWarning(
                            "Enter valid numbers for the publication year and total copies.",
                            "Invalid Input"
                    );

                } catch (IllegalArgumentException ex) {

                    showWarning(ex.getMessage(), "Invalid Input");

                } catch (SQLException ex) {

                    showError(
                            "Unable to update book details.",
                            ex
                    );

                } catch (java.io.IOException ex) {

                    showError(
                            "The book details were updated, but the cover image could not be saved.",
                            ex
                    );
                }
            });

            buttons.add(cancelButton);
            buttons.add(saveButton);

            footer.add(requiredNote, BorderLayout.WEST);
            footer.add(buttons, BorderLayout.EAST);

            dialog.add(header, BorderLayout.NORTH);
            dialog.add(bodyScroll, BorderLayout.CENTER);
            dialog.add(footer, BorderLayout.SOUTH);

            dialog.setMinimumSize(new Dimension(900, 620));
            dialog.setSize(1000, 700);
            dialog.setResizable(true);

            dialog.getRootPane().setDefaultButton(saveButton);

            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

        } catch (NumberFormatException e) {

            showWarning(
                    "The existing copy information is invalid.",
                    "Book Data Error"
            );

        } catch (SQLException e) {

            showError(
                    "Unable to load book details for editing.",
                    e
            );
        }
    }

    // =========================================================
    // EDIT DIALOG DESIGN HELPERS
    // =========================================================

    private void styleDialogButton(
            JButton button,
            Color background,
            Color foreground
    ) {

        button.setFont(new Font("Georgia", Font.BOLD, 12));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
    }

    private JPanel createEditSection(String title) {

        RoundedPanel section = new RoundedPanel(18, WHITE);

        section.setLayout(new BorderLayout(0, 13));
        section.setBorder(new EmptyBorder(16, 18, 18, 18));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Georgia", Font.BOLD, 12));
        heading.setForeground(BURGUNDY);

        JPanel headingPanel = new JPanel(new BorderLayout());
        headingPanel.setOpaque(false);
        headingPanel.add(heading, BorderLayout.WEST);

        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER);

        headingPanel.add(separator, BorderLayout.SOUTH);
        section.add(headingPanel, BorderLayout.NORTH);

        return section;
    }

    private JPanel createEditGrid() {

        JPanel grid = new JPanel(new GridLayout(0, 2, 14, 12));
        grid.setOpaque(false);

        return grid;
    }

    private void addEditField(
            JPanel grid,
            String label,
            JComponent field
    ) {

        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setOpaque(false);

        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        fieldLabel.setForeground(TEXT_GRAY);

        field.setPreferredSize(new Dimension(240, 38));
        field.setMinimumSize(new Dimension(120, 36));

        wrapper.add(fieldLabel, BorderLayout.NORTH);
        wrapper.add(field, BorderLayout.CENTER);

        grid.add(wrapper);
    }

    // =========================================================
    // OPTIONAL DATABASE COLUMNS
    // =========================================================

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

            String sql =
                    "ALTER TABLE books ADD COLUMN " + columnName + " TEXT";

            try (Connection connection = DatabaseConnection.getConnection();
                 Statement statement = connection.createStatement()) {

                statement.executeUpdate(sql);
            }
        }
    }

    // =========================================================
    // READ COVER IMAGE AND DESCRIPTION
    // =========================================================

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
             PreparedStatement statement = connection.prepareStatement(sql)) {

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

    // =========================================================
    // UPDATE COVER IMAGE AND DESCRIPTION
    // =========================================================

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
             PreparedStatement statement = connection.prepareStatement(sql)) {

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

    // =========================================================
    // FIND BOOK IN MODEL
    // =========================================================

    private int findBookModelRow(int bookId) {

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if (getIntValue(row, 0) == bookId) {
                return row;
            }
        }

        return -1;
    }

    // =========================================================
    // DELETE BOOK BY ID
    // =========================================================

    private void deleteBookById(int bookId) {

        int row = findBookModelRow(bookId);

        if (row == -1) {
            showWarning(
                    "Book not found. Please refresh the list.",
                    "Book Not Found"
            );
            return;
        }

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

                selectedBookId = -1;

                showSuccess("Book deleted successfully!");

                searchField.setText("");
                shelfFilter.setSelectedIndex(0);
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

    // =========================================================
    // FORM HELPERS
    // =========================================================

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

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(5, 8, 5, 8)
                )
        );

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

    // =========================================================
    // VALIDATE PUBLICATION YEAR
    // =========================================================

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

    // =========================================================
    // MODEL HELPERS
    // =========================================================

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

    // =========================================================
    // MESSAGES
    // =========================================================

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

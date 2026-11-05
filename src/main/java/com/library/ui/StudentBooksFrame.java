package com.library.ui;

import com.library.database.DatabaseConnection;
import com.library.service.BorrowRequestService;
import com.library.service.UserSession;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class StudentBooksFrame extends JFrame {

    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;

    private JLabel resultsLabel;
    private JLabel availableLabel;

    private JLabel detailCoverLabel;
    private JLabel detailTitleLabel;
    private JLabel detailAuthorLabel;
    private JLabel detailYearLabel;
    private JLabel detailCategoryLabel;
    private JLabel detailAvailabilityLabel;
    private JLabel detailShelfLabel;

    private final UserSession session;
    private boolean loadingCategories = false;

    // COLORS
    private static final Color BURGUNDY = new Color(75, 20, 36);
    private static final Color DARK_BURGUNDY = new Color(55, 13, 27);
    private static final Color GOLD = new Color(176, 141, 87);
    private static final Color CREAM = new Color(243, 235, 221);
    private static final Color WHITE = new Color(255, 252, 245);
    private static final Color TEXT = new Color(53, 37, 31);
    private static final Color MUTED = new Color(130, 115, 103);
    private static final Color BORDER = new Color(222, 210, 191);
    private static final Color LIGHT_GOLD = new Color(244, 232, 207);

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    public StudentBooksFrame(UserSession session) {

        if (session == null) {
            throw new SecurityException("Please log in first.");
        }

        session.requireStudent();
        this.session = session;

        setTitle("LibraryLink - Browse Books");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 650));
        setSize(1300, 800);
        setLocationRelativeTo(null);

        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        loadCategories();
        loadBooks("");
    }

    // =========================
    // HEADER
    // =========================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(22, 30, 22, 30));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Browse Library Books");
        title.setFont(new Font("Georgia", Font.BOLD, 27));
        title.setForeground(GOLD);

        JLabel subtitle = new JLabel("Discover and Borrow");
        subtitle.setFont(new Font("Georgia", Font.ITALIC, 14));
        subtitle.setForeground(WHITE);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitle);

        JButton closeButton = createButton(
                "CLOSE", DARK_BURGUNDY, WHITE
        );
        closeButton.setPreferredSize(new Dimension(105, 40));
        closeButton.addActionListener(e -> dispose());

        header.add(titlePanel, BorderLayout.CENTER);
        header.add(closeButton, BorderLayout.EAST);

        return header;
    }

    // =========================
    // MAIN CONTENT
    // =========================

    private JPanel createMainContent() {

        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBackground(CREAM);
        main.setBorder(new EmptyBorder(20, 25, 20, 25));

        main.add(createSummaryPanel(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                createBookTablePanel(),
                createBookDetailsPanel()
        );

        splitPane.setResizeWeight(0.73);
        splitPane.setDividerLocation(0.73);
        splitPane.setEnabled(false); // Lock pane divider
        splitPane.setDividerSize(0);  // Remove divider line
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        centerPanel.add(splitPane, BorderLayout.CENTER);

        main.add(centerPanel, BorderLayout.CENTER);
        main.add(createActionPanel(), BorderLayout.SOUTH);

        return main;
    }

    // =========================
    // SUMMARY CARDS
    // =========================

    private JPanel createSummaryPanel() {

        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 0));
        panel.setOpaque(false);

        JPanel resultsCard = createSummaryCard(
                "BOOKS DISPLAYED",
                "0",
                "Books matching your search"
        );

        JPanel availableCard = createSummaryCard(
                "AVAILABLE COPIES",
                "0",
                "Copies currently available"
        );

        resultsLabel = (JLabel) resultsCard.getClientProperty("valueLabel");
        availableLabel = (JLabel) availableCard.getClientProperty("valueLabel");

        panel.add(resultsCard);
        panel.add(availableCard);

        return panel;
    }

    private JPanel createSummaryCard(
            String heading,
            String value,
            String description
    ) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(14, 20, 14, 20)
        ));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        headingLabel.setForeground(MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Georgia", Font.BOLD, 28));
        valueLabel.setForeground(BURGUNDY);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descriptionLabel.setForeground(MUTED);

        textPanel.add(headingLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(descriptionLabel);

        JLabel decoration = new JLabel("L");
        decoration.setFont(new Font("Georgia", Font.BOLD, 48));
        decoration.setForeground(new Color(225, 211, 185));
        decoration.setBorder(new EmptyBorder(0, 15, 0, 10));

        card.add(textPanel, BorderLayout.CENTER);
        card.add(decoration, BorderLayout.EAST);

        card.putClientProperty("valueLabel", valueLabel);

        return card;
    }

    // =========================
    // BOOK TABLE
    // =========================

    private JPanel createBookTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(17, 17, 17, 17)
        ));

        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));

        JLabel sectionTitle = new JLabel("Library Collection");
        sectionTitle.setFont(new Font("Georgia", Font.BOLD, 20));
        sectionTitle.setForeground(BURGUNDY);

        JLabel sectionSubtitle = new JLabel(
                "Browse the available books in the library collection."
        );
        sectionSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sectionSubtitle.setForeground(MUTED);

        headingPanel.add(sectionTitle);
        headingPanel.add(Box.createVerticalStrut(5));
        headingPanel.add(sectionSubtitle);

        String[] columns = {
                "Book ID",
                "ISBN",
                "Title",
                "Author",
                "Category",
                "Publisher",
                "Year",
                "Availability",
                "Shelf Location",
                "Cover Image",
                "Description"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookTable = new JTable(tableModel);
        bookTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bookTable.setForeground(TEXT);
        bookTable.setBackground(WHITE);
        bookTable.setRowHeight(58);
        bookTable.setSelectionBackground(LIGHT_GOLD);
        bookTable.setSelectionForeground(BURGUNDY);
        bookTable.setGridColor(BORDER);
        bookTable.setShowVerticalLines(false);
        bookTable.setShowHorizontalLines(true);
        bookTable.setFillsViewportHeight(true);
        bookTable.setAutoCreateRowSorter(true);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader tableHeader = bookTable.getTableHeader();
        tableHeader.setFont(new Font("Georgia", Font.BOLD, 11));
        tableHeader.setBackground(BURGUNDY);
        tableHeader.setForeground(WHITE);
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setReorderingAllowed(false);

        // Hide description column
        bookTable.removeColumn(
                bookTable.getColumnModel().getColumn(10)
        );

        // Cover image thumbnail renderer
        bookTable.getColumnModel()
                .getColumn(9)
                .setCellRenderer(new CoverImageRenderer());

        bookTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(170);
        bookTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        bookTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        bookTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        bookTable.getColumnModel().getColumn(6).setPreferredWidth(55);
        bookTable.getColumnModel().getColumn(7).setPreferredWidth(85);
        bookTable.getColumnModel().getColumn(8).setPreferredWidth(95);
        bookTable.getColumnModel().getColumn(9).setPreferredWidth(75);

        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectedBookDetails();
            }
        });

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(WHITE);

        panel.add(headingPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // =========================
    // BOOK DETAILS SIDE PANEL
    // =========================

    private JPanel createBookDetailsPanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(WHITE);
        panel.setPreferredSize(new Dimension(285, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // 1. HEADER & COVER CONTAINER
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Selected Book");
        heading.setFont(new Font("Georgia", Font.BOLD, 18));
        heading.setForeground(BURGUNDY);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        top.add(heading);
        top.add(Box.createVerticalStrut(10));

        detailCoverLabel = new JLabel();
        detailCoverLabel.setOpaque(true);
        detailCoverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        detailCoverLabel.setVerticalAlignment(SwingConstants.CENTER);
        detailCoverLabel.setPreferredSize(new Dimension(150, 190));
        detailCoverLabel.setMaximumSize(new Dimension(150, 190));
        detailCoverLabel.setMinimumSize(new Dimension(150, 190));
        detailCoverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        showCoverPlaceholder(detailCoverLabel);

        top.add(detailCoverLabel);
        top.add(Box.createVerticalStrut(10));

        // 2. INFORMATION CONTENT CONTAINER
        JPanel information = new JPanel();
        information.setOpaque(false);
        information.setLayout(new BoxLayout(information, BoxLayout.Y_AXIS));
        information.setBorder(new EmptyBorder(5, 5, 5, 5));

        detailTitleLabel = new JLabel("Select a book");
        detailTitleLabel.setFont(new Font("Georgia", Font.BOLD, 15));
        detailTitleLabel.setForeground(BURGUNDY);
        detailTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailAuthorLabel = createDetailValue("Author: —");
        detailYearLabel = createDetailValue("Year: —");
        detailCategoryLabel = createDetailValue("Category: —");
        detailAvailabilityLabel = createDetailValue("Availability: —");
        detailShelfLabel = createDetailValue("Shelf: —");

        detailAuthorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailYearLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailCategoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailAvailabilityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailShelfLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        information.add(detailTitleLabel);
        information.add(Box.createVerticalStrut(8));
        information.add(detailAuthorLabel);
        information.add(Box.createVerticalStrut(5));
        information.add(detailYearLabel);
        information.add(Box.createVerticalStrut(5));
        information.add(detailCategoryLabel);
        information.add(Box.createVerticalStrut(5));
        information.add(detailAvailabilityLabel);
        information.add(Box.createVerticalStrut(5));
        information.add(detailShelfLabel);

        JScrollPane infoScrollPane = new JScrollPane(information);
        infoScrollPane.setBorder(null);
        infoScrollPane.setOpaque(false);
        infoScrollPane.getViewport().setOpaque(false);

        // 3. BOTTOM BUTTON CONTAINER
        JButton requestButton = createButton(
                "REQUEST TO BORROW", BURGUNDY, WHITE
        );
        requestButton.setPreferredSize(new Dimension(220, 40));
        requestButton.addActionListener(e -> submitBorrowRequest());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        buttonPanel.add(requestButton);

        panel.add(top, BorderLayout.NORTH);
        panel.add(infoScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel createDetailValue(String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT);
        return label;
    }

    private void updateSelectedBookDetails() {

        int selectedRow = bookTable.getSelectedRow();

        if (selectedRow == -1) {
            detailTitleLabel.setText("Select a book");
            detailAuthorLabel.setText("Author: —");
            detailYearLabel.setText("Year: —");
            detailCategoryLabel.setText("Category: —");
            detailAvailabilityLabel.setText("Availability: —");
            detailShelfLabel.setText("Shelf: —");

            showCoverPlaceholder(detailCoverLabel);
            return;
        }

        int modelRow = bookTable.convertRowIndexToModel(selectedRow);

        String title = safeValue(tableModel.getValueAt(modelRow, 2));
        String author = safeValue(tableModel.getValueAt(modelRow, 3));
        String category = safeValue(tableModel.getValueAt(modelRow, 4));
        String year = safeValue(tableModel.getValueAt(modelRow, 6));
        String available = safeValue(tableModel.getValueAt(modelRow, 7));
        String shelf = safeValue(tableModel.getValueAt(modelRow, 8));
        String coverImage = safeValue(tableModel.getValueAt(modelRow, 9));

        detailTitleLabel.setText(
                "<html><div style='width:235px;'>"
                        + escapeHtml(title)
                        + "</div></html>"
        );

        detailAuthorLabel.setText("Author: " + author);
        detailYearLabel.setText("Year: " + year);
        detailCategoryLabel.setText("Category: " + category);
        detailShelfLabel.setText("Shelf: " + shelf);

        int copies = 0;

        try {
            copies = Integer.parseInt(available);
        } catch (NumberFormatException ignored) {
        }

        detailAvailabilityLabel.setText(
                copies > 0
                        ? "Availability: " + copies + " cop(y/ies)"
                        : "Availability: Not Available"
        );

        JLabel cover = createCoverLabel(coverImage);
        detailCoverLabel.setIcon(cover.getIcon());
        detailCoverLabel.setText(cover.getText());
        detailCoverLabel.setForeground(BURGUNDY);
        detailCoverLabel.setBackground(LIGHT_GOLD);
    }

    // =========================
    // COVER IMAGE SCALING FIX
    // =========================

    private JLabel createCoverLabel(String coverImagePath) {
        JLabel label = new JLabel();
        label.setOpaque(true);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBackground(LIGHT_GOLD);
        label.setForeground(BURGUNDY);
        label.setFont(new Font("Georgia", Font.BOLD, 12));

        if (coverImagePath != null && !coverImagePath.isBlank()) {
            try {
                File imgFile = new File(coverImagePath);
                BufferedImage img = null;
                if (imgFile.exists()) {
                    img = ImageIO.read(imgFile);
                } else {
                    URL url = getClass().getResource(coverImagePath);
                    if (url != null) {
                        img = ImageIO.read(url);
                    }
                }

                if (img != null) {
                    // Smooth scaling to fit inside 150x190 without cropping or distortion
                    Image scaled = getScaledImage(img, 150, 190);
                    label.setIcon(new ImageIcon(scaled));
                    label.setText("");
                    return label;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        showCoverPlaceholder(label);
        return label;
    }

    private Image getScaledImage(BufferedImage src, int maxWidth, int maxHeight) {
        double widthScale = (double) maxWidth / src.getWidth();
        double heightScale = (double) maxHeight / src.getHeight();
        double scale = Math.min(widthScale, heightScale);

        int newWidth = (int) (src.getWidth() * scale);
        int newHeight = (int) (src.getHeight() * scale);

        return src.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    }

    private void showCoverPlaceholder(JLabel label) {
        label.setIcon(null);
        label.setText("<html><center><b>BOOK COVER</b><br><small>No cover image available</small></center></html>");
        label.setBackground(LIGHT_GOLD);
        label.setForeground(BURGUNDY);
    }

    // =========================
    // SEARCH AND FILTER PANEL
    // =========================

    private JPanel createActionPanel() {

        JPanel outerPanel = new JPanel(new BorderLayout(0, 10));
        outerPanel.setOpaque(false);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setOpaque(false);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(TEXT);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setPreferredSize(new Dimension(190, 37));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(5, 9, 5, 9)
        ));

        JButton searchButton = createButton(
                "Search", BURGUNDY, WHITE
        );

        JLabel categoryLabel = new JLabel("Browse by Category:");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        categoryLabel.setForeground(TEXT);

        categoryFilter = new JComboBox<>();
        categoryFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryFilter.setPreferredSize(new Dimension(155, 37));
        categoryFilter.setBackground(WHITE);

        JButton refreshButton = createButton(
                "Refresh", LIGHT_GOLD, BURGUNDY
        );

        searchButton.addActionListener(e ->
                loadBooks(searchField.getText().trim())
        );

        searchField.addActionListener(e ->
                loadBooks(searchField.getText().trim())
        );

        categoryFilter.addActionListener(e -> {
            if (!loadingCategories) {
                loadBooks(searchField.getText().trim());
            }
        });

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            categoryFilter.setSelectedIndex(0);
            loadBooks("");
        });

        controls.add(searchLabel);
        controls.add(searchField);
        controls.add(searchButton);
        controls.add(Box.createHorizontalStrut(8));
        controls.add(categoryLabel);
        controls.add(categoryFilter);
        controls.add(refreshButton);

        JButton detailsButton = createButton(
                "View Details", LIGHT_GOLD, BURGUNDY
        );

        JButton requestButton = createButton(
                "Request to Borrow", BURGUNDY, WHITE
        );

        detailsButton.addActionListener(e -> showBookDetails());
        requestButton.addActionListener(e -> submitBorrowRequest());

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        actions.setOpaque(false);
        actions.add(detailsButton);
        actions.add(requestButton);

        outerPanel.add(controls, BorderLayout.CENTER);
        outerPanel.add(actions, BorderLayout.SOUTH);

        return outerPanel;
    }

    // =========================
    // CATEGORY LOADING
    // =========================

    private void loadCategories() {

        if (categoryFilter == null) {
            return;
        }

        loadingCategories = true;

        categoryFilter.removeAllItems();
        categoryFilter.addItem("All Categories");

        String sql = """
                SELECT DISTINCT category
                FROM books
                WHERE category IS NOT NULL
                AND TRIM(category) <> ''
                ORDER BY category ASC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                categoryFilter.addItem(result.getString("category"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        categoryFilter.setSelectedIndex(0);
        loadingCategories = false;
    }

    // =========================
    // BUTTON STYLE
    // =========================

    private JButton createButton(
            String text,
            Color background,
            Color foreground
    ) {

        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        background.equals(LIGHT_GOLD) ? BORDER : background
                ),
                new EmptyBorder(9, 15, 9, 15)
        ));

        return button;
    }

    private String findCoverImage(String bookTitle) {
        if (bookTitle == null || bookTitle.isBlank()) return null;
        File imagesFolder = new File(
                System.getProperty("user.dir"), "src/main/resources/images"
        );

        if (!imagesFolder.exists() || !imagesFolder.isDirectory()) {
            return null;
        }

        String targetName = bookTitle.toLowerCase()
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

            if (!fileName.matches("(?i).*\\.(jpg|jpeg|png|gif|bmp)$")) {
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

    // =========================
    // LOAD BOOKS
    // =========================

    private void loadBooks(String search) {

        tableModel.setRowCount(0);

        String selectedCategory = categoryFilter == null
                || categoryFilter.getSelectedItem() == null
                ? "All Categories"
                : String.valueOf(categoryFilter.getSelectedItem());

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

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String keyword = "%" + search + "%";

            statement.setString(1, keyword);
            statement.setString(2, keyword);
            statement.setString(3, keyword);
            statement.setString(4, keyword);
            statement.setString(5, selectedCategory);
            statement.setString(6, selectedCategory);

            try (ResultSet result = statement.executeQuery()) {

                ResultSetMetaData metadata = result.getMetaData();
                int columnCount = metadata.getColumnCount();

                boolean hasCoverImage = false;
                boolean hasDescription = false;

                for (int i = 1; i <= columnCount; i++) {

                    String columnName = metadata.getColumnLabel(i);

                    if (columnName != null) {

                        if (columnName.equalsIgnoreCase("cover_image")) {
                            hasCoverImage = true;
                        }

                        if (columnName.equalsIgnoreCase("description")) {
                            hasDescription = true;
                        }
                    }
                }

                while (result.next()) {

                    String title = result.getString("title");

                    String coverImage = findCoverImage(title);

                    if (coverImage == null && hasCoverImage) {
                        coverImage = result.getString("cover_image");
                    }

                    String description = hasDescription
                            ? result.getString("description")
                            : null;

                    tableModel.addRow(new Object[]{
                            result.getInt("book_id"),
                            result.getString("isbn"),
                            result.getString("title"),
                            result.getString("author"),
                            result.getString("category"),
                            result.getString("publisher"),
                            result.getObject("publication_year"),
                            result.getInt("available_copies"),
                            result.getString("shelf_location"),
                            coverImage,
                            description
                    });
                }
            }

            updateSummary();

            if (tableModel.getRowCount() > 0) {
                bookTable.setRowSelectionInterval(0, 0);
            } else {
                updateSelectedBookDetails();
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load books:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    // =========================
    // UPDATE SUMMARY
    // =========================

    private void updateSummary() {

        int totalRows = tableModel.getRowCount();
        int totalAvailable = 0;

        for (int i = 0; i < totalRows; i++) {

            totalAvailable += Integer.parseInt(
                    String.valueOf(tableModel.getValueAt(i, 7))
            );
        }

        resultsLabel.setText(String.valueOf(totalRows));
        availableLabel.setText(String.valueOf(totalAvailable));
    }

    // =========================
    // SHOW BOOK DETAILS WINDOW
    // =========================

    private void showBookDetails() {

        int selectedRow = bookTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a book first.",
                    "No Book Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow = bookTable.convertRowIndexToModel(selectedRow);

        int bookId = Integer.parseInt(
                String.valueOf(tableModel.getValueAt(modelRow, 0))
        );

        String isbn = safeValue(tableModel.getValueAt(modelRow, 1));
        String title = safeValue(tableModel.getValueAt(modelRow, 2));
        String author = safeValue(tableModel.getValueAt(modelRow, 3));
        String category = safeValue(tableModel.getValueAt(modelRow, 4));
        String publisher = safeValue(tableModel.getValueAt(modelRow, 5));
        String year = safeValue(tableModel.getValueAt(modelRow, 6));
        String available = safeValue(tableModel.getValueAt(modelRow, 7));
        String shelf = safeValue(tableModel.getValueAt(modelRow, 8));
        String coverImage = safeValue(tableModel.getValueAt(modelRow, 9));
        String description = safeValue(tableModel.getValueAt(modelRow, 10));

        if (description.isBlank()) {
            description = "No description is available for this book yet.";
        }

        JDialog dialog = new JDialog(this, "Book Details", true);

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(850, 590);
        dialog.setMinimumSize(new Dimension(760, 540));
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(CREAM);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(18, 25, 18, 20));

        JLabel headerTitle = new JLabel("BOOK DETAILS");
        headerTitle.setFont(new Font("Georgia", Font.BOLD, 19));
        headerTitle.setForeground(GOLD);

        JButton closeXButton = new JButton("X");
        closeXButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        closeXButton.setForeground(WHITE);
        closeXButton.setBackground(BURGUNDY);
        closeXButton.setBorderPainted(false);
        closeXButton.setFocusPainted(false);
        closeXButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeXButton.addActionListener(e -> dialog.dispose());

        header.add(headerTitle, BorderLayout.CENTER);
        header.add(closeXButton, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(22, 18));
        content.setBackground(CREAM);
        content.setBorder(new EmptyBorder(15, 25, 10, 25));

        // LEFT: BOOK COVER
        JPanel coverPanel = new JPanel(new BorderLayout());
        coverPanel.setBackground(WHITE);
        coverPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 12, 12, 12)
        ));

        coverPanel.setPreferredSize(new Dimension(280, 300));
        coverPanel.setMinimumSize(new Dimension(220, 250));

        JLabel coverHeading = new JLabel("SELECTED BOOK", SwingConstants.CENTER);
        coverHeading.setFont(new Font("Georgia", Font.BOLD, 15));
        coverHeading.setForeground(BURGUNDY);
        coverHeading.setBorder(new EmptyBorder(5, 0, 12, 0));

        JLabel coverLabel = createCoverLabel(coverImage);
        coverLabel.setPreferredSize(new Dimension(250, 250));

        coverPanel.add(coverHeading, BorderLayout.NORTH);
        coverPanel.add(coverLabel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setOpaque(false);

        JLabel bookTitle = new JLabel(
                "<html><div style='width:420px;'>"
                        + escapeHtml(title)
                        + "</div></html>"
        );
        bookTitle.setFont(new Font("Georgia", Font.BOLD, 23));
        bookTitle.setForeground(BURGUNDY);

        JPanel informationPanel = new JPanel(new GridLayout(0, 1, 5, 8));
        informationPanel.setBackground(WHITE);
        informationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(15, 17, 15, 17)
        ));

        addDetail(informationPanel, "Book ID", String.valueOf(bookId));
        addDetail(informationPanel, "ISBN", isbn);
        addDetail(informationPanel, "Author", author);
        addDetail(informationPanel, "Genre", category);
        addDetail(informationPanel, "Publisher", publisher);
        addDetail(informationPanel, "Year Published", year);
        addDetail(
                informationPanel,
                "Availability Status",
                Integer.parseInt(available) > 0
                        ? "AVAILABLE (" + available + " COPIES)"
                        : "NOT AVAILABLE"
        );
        addDetail(informationPanel, "Shelf Location", shelf);

        rightPanel.add(bookTitle, BorderLayout.NORTH);
        rightPanel.add(informationPanel, BorderLayout.CENTER);

        JPanel topDetails = new JPanel(new BorderLayout(22, 0));
        topDetails.setOpaque(false);
        topDetails.add(coverPanel, BorderLayout.WEST);
        topDetails.add(rightPanel, BorderLayout.CENTER);

        JPanel descriptionPanel = new JPanel(new BorderLayout(0, 8));
        descriptionPanel.setBackground(WHITE);
        descriptionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 15, 12, 15)
        ));

        JLabel descHeader = new JLabel("DESCRIPTION");
        descHeader.setFont(new Font("Georgia", Font.BOLD, 13));
        descHeader.setForeground(BURGUNDY);

        JTextArea descArea = new JTextArea(description);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descArea.setForeground(TEXT);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setOpaque(false);

        descriptionPanel.add(descHeader, BorderLayout.NORTH);
        descriptionPanel.add(descArea, BorderLayout.CENTER);

        content.add(topDetails, BorderLayout.CENTER);
        content.add(descriptionPanel, BorderLayout.SOUTH);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(content, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void addDetail(JPanel panel, String labelText, String valueText) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel label = new JLabel(labelText + ":");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(MUTED);

        JLabel value = new JLabel(valueText);
        value.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        value.setForeground(TEXT);

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);

        panel.add(row);
    }

    private void submitBorrowRequest() {
        int selectedRow = bookTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a book to request.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow = bookTable.convertRowIndexToModel(selectedRow);
        int bookId = Integer.parseInt(
                String.valueOf(tableModel.getValueAt(modelRow, 0))
        );
        String title = safeValue(tableModel.getValueAt(modelRow, 2));
        int available = Integer.parseInt(
                String.valueOf(tableModel.getValueAt(modelRow, 7))
        );

        if (available <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Sorry, this book is currently unavailable for borrowing.",
                    "Not Available",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Do you want to submit a borrow request for:\n\"" + title + "\"?",
                "Confirm Borrow Request",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                BorrowRequestService service = new BorrowRequestService();

                // Call the void method directly without assigning it to a boolean
                service.submitRequest(session, bookId);

                // If execution completes without throwing an exception, the request succeeded
                JOptionPane.showMessageDialog(
                        this,
                        "Borrow request submitted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                loadBooks(searchField.getText().trim());

            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to submit borrow request: " + e.getMessage(),
                        "Request Failed",
                        JOptionPane.ERROR_MESSAGE
                );
                e.printStackTrace();
            }
        }
    }

    // =========================
    // HELPER METHODS
    // =========================

    private String safeValue(Object obj) {
        return obj == null ? "" : String.valueOf(obj).trim();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    // =========================
    // TABLE CELL RENDERER FOR COVER IMAGES
    // =========================

    // =========================
    // TABLE CELL RENDERER FOR COVER IMAGES
    // =========================

    private class CoverImageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            JLabel label = new JLabel();
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setVerticalAlignment(SwingConstants.CENTER);

            if (isSelected) {
                label.setBackground(LIGHT_GOLD);
            } else {
                label.setBackground(WHITE);
            }

            String imagePath = safeValue(value);

            if (!imagePath.isEmpty()) {
                try {
                    File imgFile = new File(imagePath);
                    BufferedImage img = null;
                    if (imgFile.exists()) {
                        img = ImageIO.read(imgFile);
                    } else {
                        URL url = getClass().getResource(imagePath);
                        if (url != null) {
                            img = ImageIO.read(url);
                        }
                    }

                    if (img != null) {
                        // Smooth scaling to fit inside 40x50 table cell bounds without cropping
                        Image scaled = getScaledImage(img, 40, 50);
                        label.setIcon(new ImageIcon(scaled));
                        label.setText("");
                        return label;
                    }
                } catch (Exception ignored) {
                }
            }

            // Fallback placeholder text if image path is empty or unreadable
            label.setFont(new Font("Segoe UI", Font.BOLD, 10));
            label.setForeground(MUTED);
            label.setText("BOOK");
            return label;
        }
    }
    }

package com.library.ui;

import com.library.database.DatabaseConnection;
import com.library.service.BorrowRequestService;
import com.library.service.UserSession;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
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

    private JLabel resultsLabel;
    private JLabel availableLabel;

    private final UserSession session;

    private static final Color BURGUNDY = new Color(105, 25, 43);
    private static final Color DARK_BURGUNDY = new Color(78, 17, 32);
    private static final Color GOLD = new Color(205, 166, 85);
    private static final Color CREAM = new Color(250, 247, 240);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(55, 48, 48);
    private static final Color MUTED = new Color(125, 116, 112);
    private static final Color BORDER = new Color(230, 222, 211);
    private static final Color LIGHT_GOLD = new Color(248, 241, 226);

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
        setMinimumSize(new Dimension(950, 600));
        setSize(1250, 750);
        setLocationRelativeTo(null);

        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        loadBooks("");
    }

    // HEADER
    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(22, 30, 22, 30));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Browse Library Books");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setForeground(WHITE);

        JLabel subtitle = new JLabel(
                "Discover books and submit your borrowing requests."
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(239, 222, 211));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JButton closeButton = createButton("Close", DARK_BURGUNDY, WHITE);
        closeButton.setPreferredSize(new Dimension(100, 38));
        closeButton.addActionListener(e -> dispose());

        header.add(titlePanel, BorderLayout.CENTER);
        header.add(closeButton, BorderLayout.EAST);

        return header;
    }

    // MAIN CONTENT
    private JPanel createMainContent() {

        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(CREAM);
        main.setBorder(new EmptyBorder(22, 28, 25, 28));

        main.add(createSummaryPanel(), BorderLayout.NORTH);
        main.add(createBookTablePanel(), BorderLayout.CENTER);
        main.add(createActionPanel(), BorderLayout.SOUTH);

        return main;
    }

    // SUMMARY CARDS
    private JPanel createSummaryPanel() {

        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setOpaque(false);

        JPanel resultsCard = createSummaryCard(
                "BOOKS DISPLAYED",
                "0",
                "Matching your search"
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

        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        headingLabel.setForeground(MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 27));
        valueLabel.setForeground(BURGUNDY);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descriptionLabel.setForeground(MUTED);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        textPanel.add(headingLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(descriptionLabel);

        card.add(textPanel, BorderLayout.CENTER);
        card.putClientProperty("valueLabel", valueLabel);

        return card;
    }

    // BOOK TABLE PANEL
    private JPanel createBookTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel sectionTitle = new JLabel("Library Collection");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sectionTitle.setForeground(BURGUNDY);

        JLabel sectionSubtitle = new JLabel(
                "Select a book to view its information or request to borrow it."
        );
        sectionSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sectionSubtitle.setForeground(MUTED);

        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));

        headingPanel.add(sectionTitle);
        headingPanel.add(Box.createVerticalStrut(4));
        headingPanel.add(sectionSubtitle);

        String[] columns = {
                "Book ID",
                "ISBN",
                "Title",
                "Author",
                "Category",
                "Publisher",
                "Year",
                "Available Copies",
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
        bookTable.setRowHeight(34);
        bookTable.setSelectionBackground(LIGHT_GOLD);
        bookTable.setSelectionForeground(BURGUNDY);
        bookTable.setGridColor(new Color(239, 234, 226));
        bookTable.setShowVerticalLines(false);
        bookTable.setShowHorizontalLines(true);
        bookTable.setFillsViewportHeight(true);
        bookTable.setAutoCreateRowSorter(true);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader tableHeader = bookTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableHeader.setBackground(BURGUNDY);
        tableHeader.setForeground(WHITE);
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setReorderingAllowed(false);

        // Keep cover image and description in the model but hide them
        // from the visible table.
        bookTable.removeColumn(
                bookTable.getColumnModel().getColumn(10)
        );
        bookTable.removeColumn(
                bookTable.getColumnModel().getColumn(9)
        );

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(WHITE);

        panel.add(headingPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // SEARCH AND ACTION PANEL
    private JPanel createActionPanel() {

        JPanel outerPanel = new JPanel(new BorderLayout(0, 12));
        outerPanel.setOpaque(false);

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("Search books");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(TEXT);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(250, 39));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(5, 10, 5, 10)
        ));

        JPanel searchInputPanel = new JPanel(new BorderLayout(8, 0));
        searchInputPanel.setOpaque(false);
        searchInputPanel.add(searchLabel, BorderLayout.WEST);
        searchInputPanel.add(searchField, BorderLayout.CENTER);

        JButton searchButton = createButton("Search", BURGUNDY, WHITE);
        JButton refreshButton = createButton("Refresh", LIGHT_GOLD, BURGUNDY);

        searchButton.addActionListener(e ->
                loadBooks(searchField.getText().trim())
        );

        searchField.addActionListener(e ->
                loadBooks(searchField.getText().trim())
        );

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadBooks("");
        });

        JPanel searchButtons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        searchButtons.setOpaque(false);
        searchButtons.add(searchButton);
        searchButtons.add(refreshButton);

        searchPanel.add(searchInputPanel, BorderLayout.CENTER);
        searchPanel.add(searchButtons, BorderLayout.EAST);

        JButton detailsButton = createButton(
                "View Details", LIGHT_GOLD, BURGUNDY
        );

        JButton requestButton = createButton(
                "Request to Borrow", BURGUNDY, WHITE
        );

        detailsButton.addActionListener(e -> showBookDetails());
        requestButton.addActionListener(e -> submitBorrowRequest());

        JPanel actionButtons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        actionButtons.setOpaque(false);
        actionButtons.add(detailsButton);
        actionButtons.add(requestButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(actionButtons, BorderLayout.EAST);

        outerPanel.add(searchPanel, BorderLayout.NORTH);
        outerPanel.add(bottomPanel, BorderLayout.SOUTH);

        return outerPanel;
    }

    // BUTTON STYLE
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

    // LOAD BOOKS
    private void loadBooks(String search) {

        tableModel.setRowCount(0);

        // SELECT * allows the program to check optional columns
        // without requiring a database structure change.
        String sql = """
                SELECT *
                FROM books
                WHERE (
                    title LIKE ?
                    OR author LIKE ?
                    OR category LIKE ?
                    OR isbn LIKE ?
                )
                ORDER BY title ASC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String keyword = "%" + search + "%";

            statement.setString(1, keyword);
            statement.setString(2, keyword);
            statement.setString(3, keyword);
            statement.setString(4, keyword);

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

                    String coverImage = hasCoverImage
                            ? result.getString("cover_image")
                            : null;

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

    // UPDATE SUMMARY CARDS
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

    // SHOW CUSTOM BOOK DETAILS WINDOW
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

        JDialog dialog = new JDialog(
                this,
                "Book Details",
                true
        );

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(850, 590);
        dialog.setMinimumSize(new Dimension(760, 540));
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(CREAM);

        // DIALOG HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(18, 25, 18, 20));

        JLabel headerTitle = new JLabel("BOOK DETAILS");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        headerTitle.setForeground(WHITE);

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

        // MAIN DETAILS AREA
        JPanel content = new JPanel(new BorderLayout(22, 18));
        content.setBackground(CREAM);
        content.setBorder(new EmptyBorder(22, 25, 18, 25));

        // LEFT: BOOK COVER
        JPanel coverPanel = new JPanel(new BorderLayout());
        coverPanel.setBackground(WHITE);
        coverPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10, 10, 10, 10)
        ));
        coverPanel.setPreferredSize(new Dimension(235, 330));

        JLabel coverLabel = createCoverLabel(coverImage);
        coverPanel.add(coverLabel, BorderLayout.CENTER);

        // RIGHT: BOOK INFORMATION
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setOpaque(false);

        JLabel bookTitle = new JLabel(
                "<html><div style='width:420px;'>"
                        + escapeHtml(title)
                        + "</div></html>"
        );
        bookTitle.setFont(new Font("Segoe UI", Font.BOLD, 23));
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

        // DESCRIPTION SECTION
        JPanel descriptionPanel = new JPanel(new BorderLayout(0, 8));
        descriptionPanel.setBackground(WHITE);
        descriptionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(14, 17, 14, 17)
        ));

        JLabel descriptionTitle = new JLabel("DESCRIPTION");
        descriptionTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        descriptionTitle.setForeground(BURGUNDY);

        JTextArea descriptionText = new JTextArea(description);
        descriptionText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descriptionText.setForeground(TEXT);
        descriptionText.setBackground(WHITE);
        descriptionText.setLineWrap(true);
        descriptionText.setWrapStyleWord(true);
        descriptionText.setEditable(false);
        descriptionText.setFocusable(false);

        JScrollPane descriptionScroll = new JScrollPane(descriptionText);
        descriptionScroll.setBorder(null);
        descriptionScroll.setPreferredSize(new Dimension(500, 85));
        descriptionScroll.getViewport().setBackground(WHITE);

        descriptionPanel.add(descriptionTitle, BorderLayout.NORTH);
        descriptionPanel.add(descriptionScroll, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(topDetails, BorderLayout.CENTER);
        centerPanel.add(descriptionPanel, BorderLayout.SOUTH);

        content.add(centerPanel, BorderLayout.CENTER);

        // BOTTOM BUTTONS
        JPanel footer = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 15)
        );
        footer.setBackground(WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, BORDER
        ));

        JButton closeButton = createButton(
                "CLOSE", LIGHT_GOLD, BURGUNDY
        );

        JButton borrowButton = createButton(
                "BORROW BOOK", BURGUNDY, WHITE
        );

        closeButton.setPreferredSize(new Dimension(115, 40));
        borrowButton.setPreferredSize(new Dimension(155, 40));

        closeButton.addActionListener(e -> dialog.dispose());

        borrowButton.addActionListener(e -> {
            dialog.dispose();
            submitBorrowRequest();
        });

        footer.add(closeButton);
        footer.add(borrowButton);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(content, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // BOOK COVER IMAGE OR PLACEHOLDER
    private JLabel createCoverLabel(String imagePath) {

        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(210, 300));
        label.setOpaque(true);
        label.setBackground(LIGHT_GOLD);

        if (imagePath == null || imagePath.isBlank()) {
            showCoverPlaceholder(label);
            return label;
        }

        try {
            Image image;

            if (imagePath.startsWith("http://")
                    || imagePath.startsWith("https://")) {

                image = ImageIO.read(new URL(imagePath));

            } else {

                File imageFile = new File(imagePath);

                if (!imageFile.isAbsolute()) {
                    imageFile = new File(
                            System.getProperty("user.dir"),
                            imagePath
                    );
                }

                image = ImageIO.read(imageFile);
            }

            if (image == null) {
                showCoverPlaceholder(label);
                return label;
            }

            Image scaledImage = image.getScaledInstance(
                    210,
                    300,
                    Image.SCALE_SMOOTH
            );

            label.setIcon(new ImageIcon(scaledImage));

        } catch (Exception e) {
            showCoverPlaceholder(label);
        }

        return label;
    }

    private void showCoverPlaceholder(JLabel label) {

        label.setIcon(null);
        label.setText(
                "<html><div style='text-align:center;'>"
                        + "<span style='font-size:42px;'>📚</span><br><br>"
                        + "<b>BOOK COVER</b><br>"
                        + "<span style='font-size:11px;'>"
                        + "No cover image available"
                        + "</span></div></html>"
        );
        label.setForeground(BURGUNDY);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    // BOOK INFORMATION ROW
    private void addDetail(
            JPanel panel,
            String label,
            String value
    ) {

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel nameLabel = new JLabel(label + ":");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(MUTED);

        JLabel valueLabel = new JLabel(
                "<html><div style='width:220px;'>"
                        + escapeHtml(value)
                        + "</div></html>"
        );
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        valueLabel.setForeground(TEXT);

        row.add(nameLabel, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.CENTER);

        panel.add(row);
    }

    private String safeValue(Object value) {

        if (value == null) {
            return "";
        }

        return String.valueOf(value);
    }

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // SUBMIT BORROWING REQUEST WITH PREFERRED RETURN DATE
    private void submitBorrowRequest() {

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

        int bookId = (int) tableModel.getValueAt(modelRow, 0);

        String bookTitle = String.valueOf(
                tableModel.getValueAt(modelRow, 2)
        );

        int availableCopies = Integer.parseInt(
                String.valueOf(tableModel.getValueAt(modelRow, 7))
        );

        if (availableCopies <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "This book currently has no available copies.",
                    "Book Unavailable",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        LocalDate today = LocalDate.now();

        LocalDate defaultReturnDate = today.plusDays(7);
        LocalDate maximumReturnDate = today.plusDays(365);

        ZoneId zone = ZoneId.systemDefault();

        Date defaultDate = Date.from(
                defaultReturnDate.atStartOfDay(zone).toInstant()
        );

        Date minimumDate = Date.from(
                today.plusDays(1).atStartOfDay(zone).toInstant()
        );

        Date maximumDate = Date.from(
                maximumReturnDate.atStartOfDay(zone).toInstant()
        );

        SpinnerDateModel dateModel = new SpinnerDateModel(
                defaultDate,
                minimumDate,
                maximumDate,
                Calendar.DAY_OF_MONTH
        );

        JSpinner returnDateSpinner = new JSpinner(dateModel);

        returnDateSpinner.setEditor(
                new JSpinner.DateEditor(returnDateSpinner, "MMMM dd, yyyy")
        );

        JPanel datePanel = new JPanel(new GridLayout(0, 1, 5, 8));
        datePanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel bookLabel = new JLabel("Book: " + bookTitle);
        JLabel instructionLabel = new JLabel(
                "Select your preferred return date:"
        );

        JLabel noteLabel = new JLabel(
                "The librarian will review your requested date."
        );

        bookLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bookLabel.setForeground(BURGUNDY);

        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        noteLabel.setForeground(MUTED);

        returnDateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        datePanel.add(bookLabel);
        datePanel.add(instructionLabel);
        datePanel.add(returnDateSpinner);
        datePanel.add(noteLabel);

        int choice = JOptionPane.showConfirmDialog(
                this,
                datePanel,
                "Choose Preferred Return Date",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        Date selectedDate = (Date) returnDateSpinner.getValue();

        LocalDate requestedReturnDate = selectedDate.toInstant()
                .atZone(zone)
                .toLocalDate();

        if (!requestedReturnDate.isAfter(today)) {
            JOptionPane.showMessageDialog(
                    this,
                    "The return date must be at least tomorrow.",
                    "Invalid Return Date",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (requestedReturnDate.isAfter(maximumReturnDate)) {
            JOptionPane.showMessageDialog(
                    this,
                    "The return date cannot be more than 365 days from today.",
                    "Invalid Return Date",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int borrowingDays = (int) ChronoUnit.DAYS.between(
                today,
                requestedReturnDate
        );

        String formattedReturnDate =
                requestedReturnDate.format(DATE_FORMAT);

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Submit a borrowing request for:\n\n"
                        + bookTitle
                        + "\n\nPreferred Return Date: "
                        + formattedReturnDate
                        + "\nBorrowing Duration: "
                        + borrowingDays
                        + " day(s)\n\n"
                        + "The librarian must approve your request "
                        + "before the book is borrowed.",
                "Confirm Borrowing Request",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            BorrowRequestService.submitRequest(
                    session,
                    bookId,
                    requestedReturnDate
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Your borrowing request has been submitted!\n\n"
                            + "Book: " + bookTitle
                            + "\nPreferred Return Date: "
                            + formattedReturnDate
                            + "\n\nYou can check its status in "
                            + "My Borrowing Requests.",
                    "Request Submitted",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadBooks(searchField.getText().trim());

        } catch (SQLException
                 | IllegalArgumentException
                 | SecurityException
                 | IllegalStateException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage() == null
                            ? "An unexpected error occurred while submitting your request."
                            : e.getMessage(),
                    "Unable to Submit Request",
                    JOptionPane.WARNING_MESSAGE
            );

            e.printStackTrace();
        }
    }
}

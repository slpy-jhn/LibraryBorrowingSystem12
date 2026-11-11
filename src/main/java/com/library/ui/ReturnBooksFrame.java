
package com.library.ui;

import com.library.database.DatabaseConnection;
import com.library.service.ReturnService;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class ReturnBooksFrame extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    private JLabel borrowedValue;
    private JLabel overdueValue;
    private JLabel displayedValue;

    private static final Color BURGUNDY = new Color(75, 20, 36);
    private static final Color DARK_BURGUNDY = new Color(55, 13, 27);
    private static final Color GOLD = new Color(176, 141, 87);
    private static final Color CREAM = new Color(243, 235, 221);
    private static final Color WHITE = new Color(255, 252, 245);
    private static final Color TEXT = new Color(53, 37, 31);
    private static final Color MUTED = new Color(130, 115, 103);
    private static final Color BORDER = new Color(222, 210, 191);
    private static final Color LIGHT_GOLD = new Color(244, 232, 207);

    private static final Color GREEN = new Color(38, 125, 82);
    private static final Color DARK_GREEN = new Color(28, 99, 64);
    private static final Color LIGHT_GREEN = new Color(231, 243, 235);
    private static final Color RED = new Color(175, 55, 55);

    public ReturnBooksFrame() {

        setTitle("Return Books");
        setSize(1200, 720);
        setMinimumSize(new Dimension(950, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());

        buildHeader();
        buildMainContent();

        loadBorrowedBooks("");
    }

    // =====================================================
    // HEADER
    // =====================================================

    private void buildHeader() {

        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(20, 25, 20, 25));

        JButton backButton = new JButton("←");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 27));
        backButton.setForeground(WHITE);
        backButton.setBackground(BURGUNDY);
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setOpaque(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.setToolTipText("Back");

        backButton.addActionListener(e -> dispose());

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Return Borrowed Books");
        title.setFont(new Font("Georgia", Font.BOLD, 27));
        title.setForeground(GOLD);

        JLabel subtitle = new JLabel(
                "Review active loans and process returned library books"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(235, 220, 215));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        header.add(backButton, BorderLayout.WEST);
        header.add(titlePanel, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
    }

    // =====================================================
    // MAIN CONTENT
    // =====================================================

    private void buildMainContent() {

        JPanel main = new JPanel(new BorderLayout(0, 20));
        main.setBackground(CREAM);
        main.setBorder(new EmptyBorder(22, 25, 22, 25));

        // SUMMARY CARDS
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryPanel.setOpaque(false);

        borrowedValue = createSummaryValue();
        overdueValue = createSummaryValue();
        displayedValue = createSummaryValue();

        summaryPanel.add(createSummaryCard(
                "ACTIVE BORROWINGS",
                borrowedValue,
                "Books not yet returned"
        ));

        summaryPanel.add(createSummaryCard(
                "OVERDUE BOOKS",
                overdueValue,
                "Past their due date"
        ));

        summaryPanel.add(createSummaryCard(
                "DISPLAYED RECORDS",
                displayedValue,
                "Based on current search"
        ));

        // CENTER CONTENT
        JPanel centerPanel = new JPanel(new BorderLayout(0, 14));
        centerPanel.setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout(15, 0));
        toolbar.setOpaque(false);

        JLabel sectionTitle = new JLabel("Borrowed Books Directory");
        sectionTitle.setFont(new Font("Georgia", Font.BOLD, 18));
        sectionTitle.setForeground(BURGUNDY);

        // SEARCH PANEL
        RoundedPanel searchPanel = new RoundedPanel(
                WHITE,
                BORDER,
                22
        );

        searchPanel.setLayout(new BorderLayout(10, 0));
        searchPanel.setBorder(new EmptyBorder(4, 13, 4, 13));
        searchPanel.setPreferredSize(new Dimension(320, 43));

        JLabel searchIcon = new JLabel("⌕");
        searchIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 25));
        searchIcon.setForeground(MUTED);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setForeground(TEXT);
        searchField.setCaretColor(BURGUNDY);
        searchField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        searchField.setOpaque(false);
        searchField.setToolTipText("Search by member or book title");

        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        toolbar.add(sectionTitle, BorderLayout.WEST);
        toolbar.add(searchPanel, BorderLayout.EAST);

        // TABLE
        String[] columns = {
                "Transaction ID",
                "Member Name",
                "Book Title",
                "Borrowing Date",
                "Due Date",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Integer.class : String.class;
            }
        };

        table = new JTable(tableModel);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        styleTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setBackground(WHITE);
        scrollPane.getViewport().setOpaque(true);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setPreferredSize(
                new Dimension(9, 0)
        );

        RoundedPanel tablePanel = new RoundedPanel(
                WHITE,
                BORDER,
                24
        );

        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(toolbar, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        main.add(summaryPanel, BorderLayout.NORTH);
        main.add(centerPanel, BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);

        // LIVE SEARCH
        searchField.getDocument().addDocumentListener(
                new DocumentListener() {

                    private void filter() {

                        String keyword = searchField.getText().trim();

                        if (keyword.isEmpty()) {
                            sorter.setRowFilter(null);
                        } else {
                            sorter.setRowFilter(
                                    RowFilter.regexFilter(
                                            "(?i)" +
                                                    java.util.regex.Pattern.quote(keyword)
                                    )
                            );
                        }

                        updateDisplayedCount();
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        filter();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        filter();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        filter();
                    }
                }
        );

        // CLICK A ROW TO OPEN THE ACTION POPUP
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                int clickedRow = table.rowAtPoint(e.getPoint());

                if (clickedRow >= 0) {

                    table.setRowSelectionInterval(
                            clickedRow,
                            clickedRow
                    );

                    showTransactionActions();
                }
            }
        });
    }

    // =====================================================
    // SUMMARY CARDS
    // =====================================================

    private JLabel createSummaryValue() {

        JLabel label = new JLabel("0");
        label.setFont(new Font("Georgia", Font.BOLD, 29));
        label.setForeground(BURGUNDY);

        return label;
    }

    private JPanel createSummaryCard(
            String heading,
            JLabel value,
            String description
    ) {

        RoundedPanel card = new RoundedPanel(
                WHITE,
                BORDER,
                23
        );

        card.setLayout(new BorderLayout(10, 0));
        card.setBorder(new EmptyBorder(15, 18, 15, 18));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(new Font("Georgia", Font.BOLD, 11));
        headingLabel.setForeground(MUTED);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descriptionLabel.setForeground(MUTED);

        content.add(headingLabel);
        content.add(Box.createVerticalStrut(8));
        content.add(value);
        content.add(Box.createVerticalStrut(4));
        content.add(descriptionLabel);

        JLabel watermark = new JLabel("L");
        watermark.setFont(new Font("Georgia", Font.BOLD, 48));
        watermark.setForeground(new Color(225, 211, 185));

        card.add(content, BorderLayout.CENTER);
        card.add(watermark, BorderLayout.EAST);

        return card;
    }

    // =====================================================
    // TABLE DESIGN
    // =====================================================

    private void styleTable() {

        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setBackground(WHITE);
        table.setRowHeight(43);

        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(240, 235, 230));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(LIGHT_GOLD);
        table.setSelectionForeground(BURGUNDY);
        table.setFillsViewportHeight(true);

        table.setIntercellSpacing(new Dimension(0, 1));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Georgia", Font.BOLD, 11));
        header.setBackground(BURGUNDY);
        header.setForeground(WHITE);
        header.setPreferredSize(new Dimension(0, 45));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createEmptyBorder());

        DefaultTableCellRenderer renderer =
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

                        if (isSelected) {
                            component.setBackground(LIGHT_GOLD);
                            component.setForeground(BURGUNDY);
                        } else {
                            component.setBackground(
                                    row % 2 == 0
                                            ? WHITE
                                            : new Color(250, 247, 240)
                            );
                            component.setForeground(TEXT);
                        }

                        setBorder(new EmptyBorder(0, 10, 0, 10));

                        return component;
                    }
                };

        renderer.setVerticalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(renderer);
        }

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
    }

    // =====================================================
    // LOAD BORROWED BOOKS
    // =====================================================

    private void loadBorrowedBooks(String search) {

        tableModel.setRowCount(0);

        String sql = """
                SELECT
                    t.transaction_id,
                    m.full_name,
                    b.title,
                    t.borrowing_date,
                    t.due_date,
                    CASE
                        WHEN t.due_date < date('now', 'localtime')
                        THEN 'OVERDUE'
                        ELSE t.status
                    END AS display_status
                FROM transactions t
                JOIN members m ON t.member_id = m.member_id
                JOIN books b ON t.book_id = b.book_id
                WHERE t.status IN ('BORROWED', 'OVERDUE')
                  AND t.return_date IS NULL
                  AND (
                      m.full_name LIKE ?
                      OR b.title LIKE ?
                  )
                ORDER BY t.due_date ASC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String keyword = "%" + search + "%";

            statement.setString(1, keyword);
            statement.setString(2, keyword);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    tableModel.addRow(new Object[]{
                            result.getInt("transaction_id"),
                            result.getString("full_name"),
                            result.getString("title"),
                            result.getString("borrowing_date"),
                            result.getString("due_date"),
                            result.getString("display_status")
                    });
                }
            }

            updateSummaryCards();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load borrowed books:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // SUMMARY UPDATE
    // =====================================================

    private void updateSummaryCards() {

        int borrowed = tableModel.getRowCount();
        int overdue = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {

            String status = String.valueOf(
                    tableModel.getValueAt(i, 5)
            );

            if ("OVERDUE".equalsIgnoreCase(status)) {
                overdue++;
            }
        }

        borrowedValue.setText(String.valueOf(borrowed));
        overdueValue.setText(String.valueOf(overdue));

        updateDisplayedCount();
    }

    private void updateDisplayedCount() {

        if (displayedValue != null && table != null) {
            displayedValue.setText(
                    String.valueOf(table.getRowCount())
            );
        }
    }

    // =====================================================
    // SELECTED TRANSACTION
    // =====================================================

    private Integer getSelectedTransactionId() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            return null;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);

        return Integer.parseInt(
                String.valueOf(tableModel.getValueAt(modelRow, 0))
        );
    }

    private int getSelectedModelRow() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            return -1;
        }

        return table.convertRowIndexToModel(selectedRow);
    }

    // =====================================================
    // ROW ACTION POPUP
    // =====================================================

    private void showTransactionActions() {

        int modelRow = getSelectedModelRow();

        if (modelRow == -1) {
            return;
        }

        Integer transactionId = getSelectedTransactionId();

        if (transactionId == null) {
            return;
        }

        String memberName = String.valueOf(
                tableModel.getValueAt(modelRow, 1)
        );

        String bookTitle = String.valueOf(
                tableModel.getValueAt(modelRow, 2)
        );

        String status = String.valueOf(
                tableModel.getValueAt(modelRow, 5)
        );

        JDialog dialog = new JDialog(
                this,
                "Borrowing Record Actions",
                true
        );

        dialog.setSize(470, 330);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CREAM);
        root.setBorder(new EmptyBorder(0, 0, 18, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(20, 23, 20, 23));

        JPanel headingContent = new JPanel();
        headingContent.setOpaque(false);
        headingContent.setLayout(new BoxLayout(headingContent, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Borrowing Record");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(GOLD);

        JLabel subtitle = new JLabel("Choose an action for this transaction");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(WHITE);

        headingContent.add(title);
        headingContent.add(Box.createVerticalStrut(5));
        headingContent.add(subtitle);

        header.add(headingContent, BorderLayout.CENTER);

        JPanel information = new RoundedPanel(WHITE, BORDER, 20);
        information.setLayout(new GridLayout(3, 1, 5, 5));
        information.setBorder(new EmptyBorder(15, 18, 15, 18));

        information.add(createInfoRow("TRANSACTION ID", "#" + transactionId));
        information.add(createInfoRow("MEMBER", memberName));
        information.add(createInfoRow("BOOK TITLE", bookTitle));

        JPanel infoHolder = new JPanel(new BorderLayout());
        infoHolder.setOpaque(false);
        infoHolder.setBorder(new EmptyBorder(18, 20, 12, 20));
        infoHolder.add(information, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(0, 20, 0, 20));

        JButton detailsButton = createActionButton(
                "VIEW DETAILS",
                BURGUNDY,
                WHITE
        );

        JButton returnButton = createActionButton(
                "PROCESS RETURN",
                GREEN,
                WHITE
        );

        detailsButton.addActionListener(e -> {
            dialog.dispose();
            viewSelectedDetails();
        });

        returnButton.addActionListener(e -> {
            dialog.dispose();
            processReturn();
        });

        buttons.add(detailsButton);
        buttons.add(returnButton);

        root.add(header, BorderLayout.NORTH);
        root.add(infoHolder, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private JPanel createInfoRow(String label, String value) {

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel name = new JLabel(label);
        name.setFont(new Font("Georgia", Font.BOLD, 10));
        name.setForeground(MUTED);

        JLabel data = new JLabel(value);
        data.setFont(new Font("Segoe UI", Font.BOLD, 12));
        data.setForeground(TEXT);
        data.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(name, BorderLayout.WEST);
        row.add(data, BorderLayout.CENTER);

        return row;
    }

    // =====================================================
    // VIEW DETAILS
    // =====================================================

    private void viewSelectedDetails() {

        int modelRow = getSelectedModelRow();

        if (modelRow == -1) {
            return;
        }

        Integer transactionId = getSelectedTransactionId();

        String[] labels = {
                "Transaction ID",
                "Member Name",
                "Book Title",
                "Borrowing Date",
                "Due Date",
                "Status"
        };

        JDialog dialog = new JDialog(
                this,
                "Borrowing Details",
                true
        );

        dialog.setSize(500, 440);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CREAM);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(20, 23, 20, 23));

        JLabel title = new JLabel("Borrowing Details");
        title.setFont(new Font("Georgia", Font.BOLD, 21));
        title.setForeground(GOLD);

        JLabel subtitle = new JLabel(
                "Complete information for transaction #" + transactionId
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(WHITE);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);

        header.add(heading, BorderLayout.CENTER);

        RoundedPanel detailsPanel = new RoundedPanel(
                WHITE,
                BORDER,
                22
        );

        detailsPanel.setLayout(new GridLayout(6, 1, 8, 8));
        detailsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        for (int i = 0; i < labels.length; i++) {

            JPanel row = new JPanel(new BorderLayout(12, 0));
            row.setOpaque(false);

            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Georgia", Font.BOLD, 11));
            label.setForeground(MUTED);

            JLabel value = new JLabel(
                    String.valueOf(tableModel.getValueAt(modelRow, i))
            );
            value.setFont(new Font("Segoe UI", Font.BOLD, 13));
            value.setForeground(TEXT);
            value.setHorizontalAlignment(SwingConstants.RIGHT);

            row.add(label, BorderLayout.WEST);
            row.add(value, BorderLayout.CENTER);

            detailsPanel.add(row);
        }

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 20, 15, 20));
        content.add(detailsPanel, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 20, 18, 20));

        JButton closeButton = createActionButton(
                "CLOSE",
                BURGUNDY,
                WHITE
        );

        closeButton.addActionListener(e -> dialog.dispose());
        footer.add(closeButton);

        root.add(header, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    // =====================================================
    // PROCESS RETURN
    // =====================================================

    private void processReturn() {

        int modelRow = getSelectedModelRow();

        if (modelRow == -1) {
            return;
        }

        Integer transactionId = getSelectedTransactionId();

        if (transactionId == null) {
            return;
        }

        String memberName = String.valueOf(
                tableModel.getValueAt(modelRow, 1)
        );

        String bookTitle = String.valueOf(
                tableModel.getValueAt(modelRow, 2)
        );

        String borrowingDate = String.valueOf(
                tableModel.getValueAt(modelRow, 3)
        );

        String dueDate = String.valueOf(
                tableModel.getValueAt(modelRow, 4)
        );

        String status = String.valueOf(
                tableModel.getValueAt(modelRow, 5)
        );

        showReturnConfirmation(
                transactionId,
                memberName,
                bookTitle,
                borrowingDate,
                dueDate,
                status
        );
    }

    // =====================================================
    // RETURN CONFIRMATION DIALOG
    // =====================================================

    private void showReturnConfirmation(
            Integer transactionId,
            String memberName,
            String bookTitle,
            String borrowingDate,
            String dueDate,
            String status
    ) {

        JDialog dialog = new JDialog(
                this,
                "Process Book Return",
                true
        );

        dialog.setSize(540, 590);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CREAM);

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(22, 25, 22, 25));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Process Book Return");
        title.setFont(new Font("Georgia", Font.BOLD, 23));
        title.setForeground(GOLD);

        JLabel subtitle = new JLabel(
                "Review the borrowing information before confirming"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(WHITE);

        heading.add(title);
        heading.add(Box.createVerticalStrut(6));
        heading.add(subtitle);

        header.add(heading, BorderLayout.CENTER);

        // MAIN CONTENT
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 22, 15, 22));

        // BOOK INFORMATION CARD
        RoundedPanel bookCard = new RoundedPanel(
                WHITE,
                BORDER,
                22
        );

        bookCard.setLayout(new BorderLayout(12, 0));
        bookCard.setBorder(new EmptyBorder(18, 18, 18, 18));
        bookCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));

        JPanel bookText = new JPanel();
        bookText.setOpaque(false);
        bookText.setLayout(new BoxLayout(bookText, BoxLayout.Y_AXIS));

        JLabel bookLabel = new JLabel("BOOK TO BE RETURNED");
        bookLabel.setFont(new Font("Georgia", Font.BOLD, 10));
        bookLabel.setForeground(MUTED);

        JLabel bookName = new JLabel(bookTitle);
        bookName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bookName.setForeground(BURGUNDY);

        JLabel memberLabel = new JLabel("Borrowed by: " + memberName);
        memberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        memberLabel.setForeground(MUTED);

        bookText.add(bookLabel);
        bookText.add(Box.createVerticalStrut(7));
        bookText.add(bookName);
        bookText.add(Box.createVerticalStrut(5));
        bookText.add(memberLabel);

        JLabel bookIcon = new JLabel("▤");
        bookIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 36));
        bookIcon.setForeground(GOLD);

        bookCard.add(bookText, BorderLayout.CENTER);
        bookCard.add(bookIcon, BorderLayout.EAST);

        // TRANSACTION DETAILS
        RoundedPanel detailsCard = new RoundedPanel(
                WHITE,
                BORDER,
                22
        );

        detailsCard.setLayout(new GridLayout(5, 1, 8, 8));
        detailsCard.setBorder(new EmptyBorder(17, 18, 17, 18));
        detailsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        detailsCard.add(createInfoRow(
                "TRANSACTION ID",
                "#" + transactionId
        ));

        detailsCard.add(createInfoRow(
                "BORROWING DATE",
                borrowingDate
        ));

        detailsCard.add(createInfoRow(
                "DUE DATE",
                dueDate
        ));

        detailsCard.add(createInfoRow(
                "CURRENT STATUS",
                status
        ));

        detailsCard.add(createInfoRow(
                "RETURN DATE",
                LocalDate.now().toString()
        ));

        // INSPECTION REMINDER
        RoundedPanel reminder = new RoundedPanel(
                LIGHT_GREEN,
                new Color(190, 220, 198),
                18
        );

        reminder.setLayout(new BorderLayout(10, 0));
        reminder.setBorder(new EmptyBorder(13, 15, 13, 15));
        reminder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        JLabel reminderIcon = new JLabel("✓");
        reminderIcon.setFont(new Font("Segoe UI", Font.BOLD, 21));
        reminderIcon.setForeground(GREEN);

        JLabel reminderText = new JLabel(
                "<html><b>Before confirming</b><br>" +
                        "Make sure the physical book has been received and inspected.</html>"
        );
        reminderText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        reminderText.setForeground(TEXT);

        reminder.add(reminderIcon, BorderLayout.WEST);
        reminder.add(reminderText, BorderLayout.CENTER);

        content.add(bookCard);
        content.add(Box.createVerticalStrut(13));
        content.add(detailsCard);
        content.add(Box.createVerticalStrut(13));
        content.add(reminder);

        // FOOTER BUTTONS
        JPanel footer = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 22, 20, 22));

        JButton cancelButton = createActionButton(
                "CANCEL",
                MUTED,
                WHITE
        );

        JButton confirmButton = createActionButton(
                "CONFIRM RETURN",
                GREEN,
                WHITE
        );

        confirmButton.setPreferredSize(new Dimension(175, 42));
        cancelButton.setPreferredSize(new Dimension(105, 42));

        cancelButton.addActionListener(e -> dialog.dispose());

        confirmButton.addActionListener(e -> {

            confirmButton.setEnabled(false);
            confirmButton.setText("PROCESSING...");

            try {

                boolean success = ReturnService.returnBook(
                        transactionId,
                        LocalDate.now()
                );

                dialog.dispose();

                if (success) {

                    JOptionPane.showMessageDialog(
                            this,
                            "The book has been returned successfully.",
                            "Return Successful",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    loadBorrowedBooks(searchField.getText().trim());

                } else {

                    JOptionPane.showMessageDialog(
                            this,
                            "The return could not be completed. " +
                                    "Please check the transaction.",
                            "Return Not Completed",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

            } catch (Exception ex) {

                dialog.dispose();

                JOptionPane.showMessageDialog(
                        this,
                        "Unable to process return:\n" + ex.getMessage(),
                        "Return Error",
                        JOptionPane.ERROR_MESSAGE
                );

                ex.printStackTrace();
            }
        });

        footer.add(cancelButton);
        footer.add(confirmButton);

        root.add(header, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    // =====================================================
    // BUTTON DESIGN
    // =====================================================

    private JButton createActionButton(
            String text,
            Color background,
            Color foreground
    ) {

        RoundedButton button = new RoundedButton(
                text,
                background,
                foreground
        );

        return button;
    }

    // =====================================================
    // ROUNDED PANEL
    // =====================================================

    private static class RoundedPanel extends JPanel {

        private final Color backgroundColor;
        private final Color borderColor;
        private final int radius;

        public RoundedPanel(
                Color backgroundColor,
                Color borderColor,
                int radius
        ) {

            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
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

            int width = getWidth() - 1;
            int height = getHeight() - 1;

            g2.setColor(backgroundColor);
            g2.fillRoundRect(
                    0,
                    0,
                    width,
                    height,
                    radius,
                    radius
            );

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(
                        0,
                        0,
                        width,
                        height,
                        radius,
                        radius
                );
            }

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // =====================================================
    // ROUNDED BUTTON
    // =====================================================

    private static class RoundedButton extends JButton {

        private final Color normalColor;
        private final Color foregroundColor;

        public RoundedButton(
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

            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(12, 17, 12, 17));
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color fill = normalColor;

            if (!isEnabled()) {
                fill = new Color(170, 170, 170);
            } else if (getModel().isPressed()) {
                fill = normalColor.darker();
            } else if (getModel().isRollover()) {
                fill = normalColor.brighter();
            }

            g2.setColor(fill);
            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    18,
                    18
            );

            g2.dispose();

            setForeground(foregroundColor);
            super.paintComponent(g);
        }
    }
}

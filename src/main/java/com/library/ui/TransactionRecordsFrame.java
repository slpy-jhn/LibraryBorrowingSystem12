
package com.library.ui;

import com.library.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class TransactionRecordsFrame extends JFrame {

    // =====================================================
    // COMPONENTS
    // =====================================================

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private TableRowSorter<DefaultTableModel> sorter;

    private JLabel totalValue;
    private JLabel borrowedValue;
    private JLabel overdueValue;
    private JLabel returnedValue;
    private JLabel recordCountLabel;

    // =====================================================
    // THEME
    // =====================================================

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
    private static final Color RED = new Color(175, 55, 55);
    private static final Color BLUE = new Color(60, 105, 155);

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public TransactionRecordsFrame() {

        setTitle("LibraryLink - Transaction Records");
        setSize(1350, 760);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());

        buildHeader();
        buildMainContent();

        loadTransactions("", "ALL");
    }

    // =====================================================
    // HEADER
    // =====================================================

    private void buildHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(BURGUNDY);
        header.setBorder(
                new EmptyBorder(22, 28, 22, 28)
        );

        JPanel titlePanel = new JPanel();

        titlePanel.setOpaque(false);
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("Transaction Records");

        title.setFont(
                new Font("Georgia", Font.BOLD, 27)
        );

        title.setForeground(GOLD);

        JLabel subtitle = new JLabel(
                "View and monitor the complete borrowing and return history"
        );

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        subtitle.setForeground(
                new Color(235, 220, 215)
        );

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        // TRANSPARENT ICON-ONLY BACK BUTTON

        JButton backButton = new JButton("\u2190");

        backButton.setFont(
                new Font("Segoe UI Symbol", Font.PLAIN, 27)
        );

        backButton.setForeground(GOLD);
        backButton.setBackground(new Color(0, 0, 0, 0));

        backButton.setPreferredSize(
                new Dimension(46, 46)
        );

        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.setOpaque(false);

        backButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        backButton.setToolTipText("Back");

        backButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                backButton.setForeground(WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                backButton.setForeground(GOLD);
            }
        });

        backButton.addActionListener(e -> dispose());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    // =====================================================
    // MAIN CONTENT
    // =====================================================

    private void buildMainContent() {

        JPanel main = new JPanel(new BorderLayout(0, 20));

        main.setBackground(CREAM);
        main.setBorder(
                new EmptyBorder(22, 25, 22, 25)
        );

        // SUMMARY CARDS

        JPanel summaryPanel = new JPanel(
                new GridLayout(1, 4, 15, 0)
        );

        summaryPanel.setOpaque(false);

        totalValue = createSummaryValue(BURGUNDY);
        borrowedValue = createSummaryValue(BLUE);
        overdueValue = createSummaryValue(RED);
        returnedValue = createSummaryValue(GREEN);

        summaryPanel.add(
                createSummaryCard(
                        "TOTAL TRANSACTIONS",
                        totalValue,
                        "All recorded transactions",
                        "01"
                )
        );

        summaryPanel.add(
                createSummaryCard(
                        "BORROWED",
                        borrowedValue,
                        "Currently active loans",
                        "02"
                )
        );

        summaryPanel.add(
                createSummaryCard(
                        "OVERDUE",
                        overdueValue,
                        "Past their due dates",
                        "03"
                )
        );

        summaryPanel.add(
                createSummaryCard(
                        "RETURNED",
                        returnedValue,
                        "Completed returns",
                        "04"
                )
        );

        // CENTER PANEL

        JPanel centerPanel = new JPanel(
                new BorderLayout(0, 15)
        );

        centerPanel.setOpaque(false);

        // TOOLBAR

        JPanel toolbar = new JPanel(
                new BorderLayout(12, 0)
        );

        toolbar.setOpaque(false);

        JPanel headingPanel = new JPanel(
                new BorderLayout(0, 4)
        );

        headingPanel.setOpaque(false);

        JLabel sectionTitle = new JLabel(
                "Transaction History"
        );

        sectionTitle.setFont(
                new Font("Georgia", Font.BOLD, 18)
        );

        sectionTitle.setForeground(BURGUNDY);

        recordCountLabel = new JLabel(
                "Showing 0 records"
        );

        recordCountLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 11)
        );

        recordCountLabel.setForeground(MUTED);

        headingPanel.add(
                sectionTitle,
                BorderLayout.NORTH
        );

        headingPanel.add(
                recordCountLabel,
                BorderLayout.SOUTH
        );

        // SEARCH AND FILTER PANEL

        RoundedPanel filterPanel = new RoundedPanel(
                18,
                WHITE,
                BORDER
        );

        filterPanel.setLayout(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        12,
                        9
                )
        );

        filterPanel.setBorder(
                new EmptyBorder(3, 10, 3, 10)
        );

        JLabel searchIcon = new JLabel("\u2315");

        searchIcon.setFont(
                new Font("Segoe UI Symbol", Font.BOLD, 22)
        );

        searchIcon.setForeground(GOLD);

        searchField = new JTextField(17);

        searchField.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        searchField.setPreferredSize(
                new Dimension(190, 34)
        );

        searchField.setForeground(TEXT);
        searchField.setBackground(WHITE);
        searchField.setCaretColor(BURGUNDY);

        searchField.setToolTipText(
                "Search member, Student/Employee ID, or book title"
        );

        searchField.setBorder(
                BorderFactory.createEmptyBorder(
                        5, 5, 5, 5
                )
        );

        JLabel divider = new JLabel("|");

        divider.setForeground(BORDER);

        divider.setFont(
                new Font("Segoe UI", Font.PLAIN, 20)
        );

        // STATUS LABEL REMOVED

        statusFilter = new JComboBox<>(
                new String[]{
                        "ALL",
                        "BORROWED",
                        "OVERDUE",
                        "RETURNED"
                }
        );

        statusFilter.setFont(
                new Font("Segoe UI", Font.BOLD, 11)
        );

        statusFilter.setPreferredSize(
                new Dimension(125, 34)
        );

        statusFilter.setBackground(WHITE);
        statusFilter.setForeground(BURGUNDY);
        statusFilter.setFocusable(false);

        statusFilter.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        filterPanel.add(searchIcon);
        filterPanel.add(searchField);
        filterPanel.add(divider);
        filterPanel.add(statusFilter);

        toolbar.add(
                headingPanel,
                BorderLayout.WEST
        );

        toolbar.add(
                filterPanel,
                BorderLayout.EAST
        );

        // TABLE MODEL

        String[] columns = {
                "Transaction ID",
                "Member Name",
                "Student/Employee ID",
                "Book Title",
                "Borrowing Date",
                "Due Date",
                "Return Date",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {

                return columnIndex == 0
                        ? Integer.class
                        : String.class;
            }
        };

        table = new JTable(tableModel);

        sorter = new TableRowSorter<>(tableModel);

        table.setRowSorter(sorter);

        styleTable();

        // SCROLL PANE - NO HORIZONTAL SCROLLBAR

        JScrollPane scrollPane = new JScrollPane(
                table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder()
        );

        scrollPane.getViewport().setBackground(WHITE);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        scrollPane.getVerticalScrollBar().setPreferredSize(
                new Dimension(10, 0)
        );

        // ROUNDED TABLE PANEL

        RoundedPanel tablePanel = new RoundedPanel(
                20,
                WHITE,
                BORDER
        );

        tablePanel.setLayout(new BorderLayout());

        tablePanel.setBorder(
                new EmptyBorder(9, 9, 9, 9)
        );

        tablePanel.add(
                scrollPane,
                BorderLayout.CENTER
        );

        centerPanel.add(
                toolbar,
                BorderLayout.NORTH
        );

        centerPanel.add(
                tablePanel,
                BorderLayout.CENTER
        );

        main.add(
                summaryPanel,
                BorderLayout.NORTH
        );

        main.add(
                centerPanel,
                BorderLayout.CENTER
        );

        add(main, BorderLayout.CENTER);

        // AUTOMATIC COLUMN RESIZING

        tablePanel.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                fitTableColumns();
            }
        });

        // SEARCH LISTENER

        searchField.getDocument().addDocumentListener(
                new DocumentListener() {

                    private void filter() {

                        String text = searchField
                                .getText()
                                .trim();

                        if (text.isEmpty()) {

                            sorter.setRowFilter(null);

                        } else {

                            sorter.setRowFilter(
                                    RowFilter.regexFilter(
                                            "(?i)" +
                                                    Pattern.quote(text)
                                    )
                            );
                        }

                        updateSummaryCards();
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

        // STATUS FILTER

        statusFilter.addActionListener(e -> {

            loadTransactions(
                    searchField.getText().trim(),
                    statusFilter.getSelectedItem().toString()
            );
        });

        // CLICK TABLE ROW TO VIEW DETAILS

        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                int clickedRow = table.rowAtPoint(e.getPoint());

                if (clickedRow < 0) {
                    return;
                }

                table.setRowSelectionInterval(
                        clickedRow,
                        clickedRow
                );

                if (e.getClickCount() == 1) {
                    viewSelectedDetails();
                }
            }
        });
    }

    // =====================================================
    // FIT TABLE COLUMNS
    // =====================================================

    private void fitTableColumns() {

        if (table == null || table.getColumnCount() == 0) {
            return;
        }

        int availableWidth = table.getParent().getWidth();

        if (availableWidth <= 0) {
            return;
        }

        int[] preferredWidths = {
                105, 165, 150, 205, 125, 125, 125, 105
        };

        int totalPreferred = 0;

        for (int width : preferredWidths) {
            totalPreferred += width;
        }

        int remainingWidth = availableWidth;

        for (int i = 0; i < table.getColumnCount(); i++) {

            int columnWidth;

            if (i == table.getColumnCount() - 1) {

                columnWidth = remainingWidth;

            } else {

                columnWidth = (int) (
                        (double) preferredWidths[i]
                                / totalPreferred
                                * availableWidth
                );

                remainingWidth -= columnWidth;
            }

            table.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(columnWidth);

            table.getColumnModel()
                    .getColumn(i)
                    .setMinWidth(65);
        }

        table.setAutoResizeMode(
                JTable.AUTO_RESIZE_ALL_COLUMNS
        );

        table.revalidate();
        table.repaint();
    }

    // =====================================================
    // SUMMARY VALUE
    // =====================================================

    private JLabel createSummaryValue(Color color) {

        JLabel label = new JLabel("0");

        label.setFont(
                new Font("Georgia", Font.BOLD, 29)
        );

        label.setForeground(color);

        return label;
    }

    // =====================================================
    // SUMMARY CARD
    // =====================================================

    private JPanel createSummaryCard(
            String heading,
            JLabel value,
            String description,
            String number
    ) {

        RoundedPanel card = new RoundedPanel(
                20,
                WHITE,
                BORDER
        );

        card.setLayout(new BorderLayout());

        card.setBorder(
                new EmptyBorder(16, 17, 16, 17)
        );

        JPanel textPanel = new JPanel();

        textPanel.setOpaque(false);

        textPanel.setLayout(
                new BoxLayout(textPanel, BoxLayout.Y_AXIS)
        );

        JLabel headingLabel = new JLabel(heading);

        headingLabel.setFont(
                new Font("Georgia", Font.BOLD, 10)
        );

        headingLabel.setForeground(MUTED);

        JLabel descriptionLabel = new JLabel(description);

        descriptionLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 10)
        );

        descriptionLabel.setForeground(MUTED);

        textPanel.add(headingLabel);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(value);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(descriptionLabel);

        JLabel watermark = new JLabel(number);

        watermark.setFont(
                new Font("Georgia", Font.BOLD, 29)
        );

        watermark.setForeground(
                new Color(225, 211, 185)
        );

        card.add(
                textPanel,
                BorderLayout.CENTER
        );

        card.add(
                watermark,
                BorderLayout.EAST
        );

        return card;
    }

    // =====================================================
    // TABLE DESIGN
    // =====================================================

    private void styleTable() {

        table.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        table.setForeground(TEXT);
        table.setBackground(WHITE);

        table.setRowHeight(43);

        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);

        table.setGridColor(
                new Color(235, 226, 212)
        );

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        table.setSelectionBackground(LIGHT_GOLD);
        table.setSelectionForeground(BURGUNDY);

        table.setFillsViewportHeight(true);

        table.setAutoResizeMode(
                JTable.AUTO_RESIZE_ALL_COLUMNS
        );

        table.setIntercellSpacing(
                new Dimension(0, 1)
        );

        // TABLE HEADER

        JTableHeader header = table.getTableHeader();

        header.setFont(
                new Font("Georgia", Font.BOLD, 10)
        );

        header.setBackground(BURGUNDY);
        header.setForeground(WHITE);

        header.setPreferredSize(
                new Dimension(0, 45)
        );

        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);

        // GENERAL CELL RENDERER

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
                                    row % 2 == 0 ? WHITE : CREAM
                            );

                            component.setForeground(TEXT);
                        }

                        setBorder(
                                new EmptyBorder(0, 7, 0, 7)
                        );

                        return component;
                    }
                };

        for (int i = 0; i < table.getColumnCount(); i++) {

            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(renderer);
        }

        // STATUS RENDERER

        DefaultTableCellRenderer statusRenderer =
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

                        String status = String.valueOf(value);

                        setHorizontalAlignment(
                                SwingConstants.CENTER
                        );

                        setFont(
                                new Font("Georgia", Font.BOLD, 10)
                        );

                        if (isSelected) {

                            component.setBackground(LIGHT_GOLD);
                            component.setForeground(BURGUNDY);

                        } else {

                            component.setBackground(
                                    row % 2 == 0 ? WHITE : CREAM
                            );

                            if ("RETURNED".equalsIgnoreCase(status)) {

                                component.setForeground(GREEN);

                            } else if ("OVERDUE".equalsIgnoreCase(status)) {

                                component.setForeground(RED);

                            } else {

                                component.setForeground(BLUE);
                            }
                        }

                        setBorder(
                                new EmptyBorder(0, 4, 0, 4)
                        );

                        return component;
                    }
                };

        table.getColumnModel()
                .getColumn(7)
                .setCellRenderer(statusRenderer);
    }

    // =====================================================
    // LOAD TRANSACTIONS
    // =====================================================

    private void loadTransactions(
            String search,
            String status
    ) {

        tableModel.setRowCount(0);

        String sql = """
                SELECT
                    t.transaction_id,
                    m.full_name,
                    m.student_employee_id,
                    b.title,
                    t.borrowing_date,
                    t.due_date,
                    t.return_date,
                    CASE
                        WHEN t.return_date IS NOT NULL
                            THEN 'RETURNED'
                        WHEN t.due_date < date('now', 'localtime')
                            THEN 'OVERDUE'
                        ELSE 'BORROWED'
                    END AS display_status
                FROM transactions t
                JOIN members m
                    ON t.member_id = m.member_id
                JOIN books b
                    ON t.book_id = b.book_id
                WHERE (
                    m.full_name LIKE ?
                    OR m.student_employee_id LIKE ?
                    OR b.title LIKE ?
                )
                AND (
                    ? = 'ALL'
                    OR (
                        ? = 'OVERDUE'
                        AND t.return_date IS NULL
                        AND t.status IN ('BORROWED', 'OVERDUE')
                        AND t.due_date < date('now', 'localtime')
                    )
                    OR (
                        ? = 'BORROWED'
                        AND t.return_date IS NULL
                        AND t.status = 'BORROWED'
                        AND t.due_date >= date('now', 'localtime')
                    )
                    OR (
                        ? = 'RETURNED'
                        AND t.return_date IS NOT NULL
                    )
                )
                ORDER BY t.transaction_id DESC
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            String keyword = "%" + search + "%";

            statement.setString(1, keyword);
            statement.setString(2, keyword);
            statement.setString(3, keyword);

            statement.setString(4, status);
            statement.setString(5, status);
            statement.setString(6, status);
            statement.setString(7, status);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    tableModel.addRow(new Object[]{
                            result.getInt("transaction_id"),
                            result.getString("full_name"),
                            result.getString("student_employee_id"),
                            result.getString("title"),
                            result.getString("borrowing_date"),
                            result.getString("due_date"),
                            result.getString("return_date") == null
                                    ? "-"
                                    : result.getString("return_date"),
                            result.getString("display_status")
                    });
                }
            }

            updateSummaryCards();
            fitTableColumns();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load transaction records:\n"
                            + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // UPDATE SUMMARY CARDS
    // =====================================================

    private void updateSummaryCards() {

        if (table == null || tableModel == null) {
            return;
        }

        int total = table.getRowCount();

        int borrowed = 0;
        int overdue = 0;
        int returned = 0;

        for (int i = 0; i < table.getRowCount(); i++) {

            int modelRow = table.convertRowIndexToModel(i);

            String status = String.valueOf(
                    tableModel.getValueAt(modelRow, 7)
            );

            if ("BORROWED".equalsIgnoreCase(status)) {

                borrowed++;

            } else if ("OVERDUE".equalsIgnoreCase(status)) {

                overdue++;

            } else if ("RETURNED".equalsIgnoreCase(status)) {

                returned++;
            }
        }

        totalValue.setText(String.valueOf(total));
        borrowedValue.setText(String.valueOf(borrowed));
        overdueValue.setText(String.valueOf(overdue));
        returnedValue.setText(String.valueOf(returned));

        if (recordCountLabel != null) {

            recordCountLabel.setText(
                    "Showing " + total + " records"
            );
        }
    }

    // =====================================================
    // VIEW DETAILS WHEN A TABLE ROW IS CLICKED
    // =====================================================

    private void viewSelectedDetails() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);

        String[] labels = {
                "Transaction ID",
                "Member Name",
                "Student/Employee ID",
                "Book Title",
                "Borrowing Date",
                "Due Date",
                "Return Date",
                "Status"
        };

        // DIALOG

        JDialog dialog = new JDialog(
                this,
                "Transaction Details",
                true
        );

        dialog.setUndecorated(true);

        dialog.setSize(600, 510);
        dialog.setMinimumSize(new Dimension(540, 460));
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());

        root.setBackground(WHITE);

        root.setBorder(
                BorderFactory.createLineBorder(BORDER, 1)
        );

        // DIALOG HEADER

        JPanel dialogHeader = new JPanel(
                new BorderLayout()
        );

        dialogHeader.setBackground(BURGUNDY);

        dialogHeader.setBorder(
                new EmptyBorder(17, 22, 17, 18)
        );

        JPanel heading = new JPanel();

        heading.setOpaque(false);

        heading.setLayout(
                new BoxLayout(heading, BoxLayout.Y_AXIS)
        );

        JLabel dialogTitle = new JLabel(
                "Transaction Details"
        );

        dialogTitle.setFont(
                new Font("Georgia", Font.BOLD, 20)
        );

        dialogTitle.setForeground(GOLD);

        JLabel dialogSubtitle = new JLabel(
                "Complete information about this transaction"
        );

        dialogSubtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 11)
        );

        dialogSubtitle.setForeground(
                new Color(235, 220, 215)
        );

        heading.add(dialogTitle);
        heading.add(Box.createVerticalStrut(5));
        heading.add(dialogSubtitle);

        // TRANSPARENT ICON-ONLY BACK BUTTON

        JButton backButton = new JButton("\u2190");

        backButton.setFont(
                new Font("Segoe UI Symbol", Font.PLAIN, 25)
        );

        backButton.setForeground(GOLD);
        backButton.setBackground(new Color(0, 0, 0, 0));

        backButton.setPreferredSize(
                new Dimension(42, 42)
        );

        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.setOpaque(false);

        backButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        backButton.setToolTipText("Back");

        backButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                backButton.setForeground(WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                backButton.setForeground(GOLD);
            }
        });

        backButton.addActionListener(
                e -> dialog.dispose()
        );

        dialogHeader.add(
                heading,
                BorderLayout.WEST
        );

        dialogHeader.add(
                backButton,
                BorderLayout.EAST
        );

        // DETAILS CONTENT

        JPanel content = new JPanel(new BorderLayout());

        content.setBackground(CREAM);

        content.setBorder(
                new EmptyBorder(20, 22, 20, 22)
        );

        RoundedPanel detailsPanel = new RoundedPanel(
                18,
                WHITE,
                BORDER
        );

        detailsPanel.setLayout(
                new GridLayout(8, 1, 0, 0)
        );

        detailsPanel.setBorder(
                new EmptyBorder(12, 22, 12, 22)
        );

        // ADD EIGHT DETAIL ROWS

        for (int i = 0; i < labels.length; i++) {

            JPanel row = new JPanel(
                    new BorderLayout(12, 0)
            );

            row.setOpaque(false);

            row.setBorder(
                    new EmptyBorder(5, 0, 5, 0)
            );

            JLabel nameLabel = new JLabel(
                    labels[i]
            );

            nameLabel.setFont(
                    new Font("Georgia", Font.BOLD, 11)
            );

            nameLabel.setForeground(BURGUNDY);

            String value = String.valueOf(
                    tableModel.getValueAt(modelRow, i)
            );

            JLabel valueLabel = new JLabel(value);

            valueLabel.setFont(
                    new Font("Segoe UI", Font.PLAIN, 12)
            );

            valueLabel.setForeground(TEXT);

            if (i == 7) {

                valueLabel.setFont(
                        new Font("Georgia", Font.BOLD, 11)
                );

                if ("RETURNED".equalsIgnoreCase(value)) {

                    valueLabel.setForeground(GREEN);

                } else if ("OVERDUE".equalsIgnoreCase(value)) {

                    valueLabel.setForeground(RED);

                } else {

                    valueLabel.setForeground(BLUE);
                }
            }

            row.add(nameLabel, BorderLayout.WEST);
            row.add(valueLabel, BorderLayout.EAST);

            detailsPanel.add(row);
        }

        // DETAILS PANEL FILLS AVAILABLE SPACE

        content.add(
                detailsPanel,
                BorderLayout.CENTER
        );

        root.add(
                dialogHeader,
                BorderLayout.NORTH
        );

        root.add(
                content,
                BorderLayout.CENTER
        );

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    // =====================================================
    // ROUNDED PANEL
    // =====================================================

    private static class RoundedPanel extends JPanel {

        private final int radius;
        private final Color backgroundColor;
        private final Color borderColor;

        public RoundedPanel(
                int radius,
                Color backgroundColor,
                Color borderColor
        ) {

            this.radius = radius;
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;

            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(backgroundColor);

            g2.fill(
                    new RoundRectangle2D.Float(
                            0,
                            0,
                            getWidth() - 1,
                            getHeight() - 1,
                            radius,
                            radius
                    )
            );

            g2.setColor(borderColor);

            g2.setStroke(
                    new BasicStroke(1f)
            );

            g2.draw(
                    new RoundRectangle2D.Float(
                            0.5f,
                            0.5f,
                            getWidth() - 2,
                            getHeight() - 2,
                            radius,
                            radius
                    )
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // =====================================================
    // ROUNDED BORDER
    // =====================================================

    private static class RoundedBorder extends AbstractBorder {

        private final int radius;
        private final Color color;

        public RoundedBorder(
                int radius,
                Color color
        ) {

            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(
                Component c,
                Graphics g,
                int x,
                int y,
                int width,
                int height
        ) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(color);

            g2.drawRoundRect(
                    x,
                    y,
                    width - 1,
                    height - 1,
                    radius,
                    radius
            );

            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {

            return new Insets(5, 8, 5, 8);
        }

        @Override
        public Insets getBorderInsets(
                Component c,
                Insets insets
        ) {

            insets.set(5, 8, 5, 8);

            return insets;
        }
    }
}

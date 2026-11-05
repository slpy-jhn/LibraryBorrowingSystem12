
package com.library.ui;

import com.library.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionRecordsFrame extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private TableRowSorter<DefaultTableModel> sorter;

    private JLabel totalValue;
    private JLabel borrowedValue;
    private JLabel overdueValue;
    private JLabel returnedValue;

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

    public TransactionRecordsFrame() {

        setTitle("Transaction Records");
        setSize(1350, 760);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());

        buildHeader();
        buildMainContent();
        buildBottomPanel();

        loadTransactions("", "ALL");
    }

    private void buildHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(
                BorderFactory.createEmptyBorder(22, 28, 22, 28)
        );

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("Transaction Records");
        title.setFont(new Font("Georgia", Font.BOLD, 27));
        title.setForeground(GOLD);

        JLabel subtitle = new JLabel(
                "View and monitor the complete borrowing and return history"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(235, 220, 215));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JButton closeButton = new JButton("CLOSE");
        styleButton(closeButton, BURGUNDY, WHITE);

        closeButton.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(GOLD, 1),
                        new EmptyBorder(9, 18, 9, 18)
                )
        );

        closeButton.addActionListener(e -> dispose());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    private void buildMainContent() {

        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(CREAM);
        main.setBorder(
                BorderFactory.createEmptyBorder(20, 25, 10, 25)
        );

        JPanel summaryPanel = new JPanel(
                new GridLayout(1, 4, 12, 0)
        );
        summaryPanel.setOpaque(false);

        totalValue = createSummaryValue(BURGUNDY);
        borrowedValue = createSummaryValue(BLUE);
        overdueValue = createSummaryValue(RED);
        returnedValue = createSummaryValue(GREEN);

        summaryPanel.add(createSummaryCard(
                "TOTAL TRANSACTIONS",
                totalValue,
                "All recorded transactions"
        ));

        summaryPanel.add(createSummaryCard(
                "BORROWED",
                borrowedValue,
                "Currently active loans"
        ));

        summaryPanel.add(createSummaryCard(
                "OVERDUE",
                overdueValue,
                "Past their due dates"
        ));

        summaryPanel.add(createSummaryCard(
                "RETURNED",
                returnedValue,
                "Completed returns"
        ));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JLabel sectionTitle = new JLabel("Transaction History");
        sectionTitle.setFont(new Font("Georgia", Font.BOLD, 17));
        sectionTitle.setForeground(BURGUNDY);

        JPanel filterPanel = new JPanel(new FlowLayout(
                FlowLayout.RIGHT, 8, 0
        ));
        filterPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(TEXT);

        searchField = new JTextField(18);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setPreferredSize(new Dimension(190, 36));
        searchField.setBackground(WHITE);
        searchField.setForeground(TEXT);
        searchField.setCaretColor(BURGUNDY);
        searchField.setToolTipText(
                "Search member, Student/Employee ID, or book title"
        );

        searchField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(7, 10, 7, 10)
                )
        );

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(TEXT);

        statusFilter = new JComboBox<>(new String[]{
                "ALL",
                "BORROWED",
                "OVERDUE",
                "RETURNED"
        });

        statusFilter.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusFilter.setPreferredSize(new Dimension(125, 36));
        statusFilter.setBackground(WHITE);
        statusFilter.setForeground(BURGUNDY);

        filterPanel.add(searchLabel);
        filterPanel.add(searchField);
        filterPanel.add(statusLabel);
        filterPanel.add(statusFilter);

        toolbar.add(sectionTitle, BorderLayout.WEST);
        toolbar.add(filterPanel, BorderLayout.EAST);

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

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(WHITE);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(WHITE);
        tablePanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(8, 8, 8, 8)
                )
        );

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(toolbar, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        main.add(summaryPanel, BorderLayout.NORTH);
        main.add(centerPanel, BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {

                    private void filter() {

                        String text = searchField.getText().trim();

                        if (text.isEmpty()) {
                            sorter.setRowFilter(null);
                        } else {
                            sorter.setRowFilter(
                                    RowFilter.regexFilter(
                                            "(?i)" +
                                                    java.util.regex.Pattern.quote(text)
                                    )
                            );
                        }

                        updateSummaryCards();
                    }

                    @Override
                    public void insertUpdate(
                            javax.swing.event.DocumentEvent e
                    ) {
                        filter();
                    }

                    @Override
                    public void removeUpdate(
                            javax.swing.event.DocumentEvent e
                    ) {
                        filter();
                    }

                    @Override
                    public void changedUpdate(
                            javax.swing.event.DocumentEvent e
                    ) {
                        filter();
                    }
                }
        );

        statusFilter.addActionListener(e ->
                loadTransactions(
                        searchField.getText().trim(),
                        statusFilter.getSelectedItem().toString()
                )
        );
    }

    private void buildBottomPanel() {

        JPanel bottom = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 12)
        );

        bottom.setBackground(CREAM);
        bottom.setBorder(
                BorderFactory.createEmptyBorder(5, 25, 12, 25)
        );

        JButton detailsButton = createButton(
                "VIEW DETAILS",
                WHITE,
                BURGUNDY
        );

        JButton refreshButton = createButton(
                "REFRESH",
                BURGUNDY,
                WHITE
        );

        detailsButton.addActionListener(e -> viewSelectedDetails());

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedItem("ALL");
            loadTransactions("", "ALL");
        });

        bottom.add(detailsButton);
        bottom.add(refreshButton);

        add(bottom, BorderLayout.SOUTH);
    }

    private JLabel createSummaryValue(Color color) {

        JLabel label = new JLabel("0");
        label.setFont(new Font("Georgia", Font.BOLD, 27));
        label.setForeground(color);

        return label;
    }

    private JPanel createSummaryCard(
            String heading,
            JLabel value,
            String description
    ) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(14, 15, 14, 15)
                )
        );

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(
                new BoxLayout(textPanel, BoxLayout.Y_AXIS)
        );

        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(new Font("Georgia", Font.BOLD, 11));
        headingLabel.setForeground(MUTED);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        descriptionLabel.setForeground(MUTED);

        textPanel.add(headingLabel);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(value);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(descriptionLabel);

        JLabel watermark = new JLabel("L");
        watermark.setFont(new Font("Georgia", Font.BOLD, 48));
        watermark.setForeground(new Color(225, 211, 185));

        card.add(textPanel, BorderLayout.CENTER);
        card.add(watermark, BorderLayout.EAST);

        return card;
    }

    private JButton createButton(
            String text,
            Color background,
            Color foreground
    ) {

        JButton button = new JButton(text);
        styleButton(button, background, foreground);

        return button;
    }

    private void styleButton(
            JButton button,
            Color background,
            Color foreground
    ) {

        button.setFont(new Font("Georgia", Font.BOLD, 11));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(GOLD, 1),
                        new EmptyBorder(9, 15, 9, 15)
                )
        );
    }

    private void styleTable() {

        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setBackground(WHITE);
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(LIGHT_GOLD);
        table.setSelectionForeground(BURGUNDY);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Georgia", Font.BOLD, 11));
        header.setBackground(BURGUNDY);
        header.setForeground(WHITE);
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);

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
                                BorderFactory.createEmptyBorder(0, 8, 0, 8)
                        );

                        return component;
                    }
                };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(renderer);
        }

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

                        setHorizontalAlignment(SwingConstants.CENTER);
                        setFont(new Font("Georgia", Font.BOLD, 10));
                        setBorder(
                                BorderFactory.createEmptyBorder(0, 5, 0, 5)
                        );

                        return component;
                    }
                };

        table.getColumnModel()
                .getColumn(7)
                .setCellRenderer(statusRenderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(105);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(155);
        table.getColumnModel().getColumn(3).setPreferredWidth(210);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(130);
        table.getColumnModel().getColumn(6).setPreferredWidth(130);
        table.getColumnModel().getColumn(7).setPreferredWidth(110);
    }

    private void loadTransactions(String search, String status) {

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

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

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

    private void updateSummaryCards() {

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
    }

    private void viewSelectedDetails() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a transaction record first.",
                    "No Record Selected",
                    JOptionPane.WARNING_MESSAGE
            );

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

        JPanel detailsPanel = new JPanel(
                new GridLayout(0, 2, 12, 12)
        );

        detailsPanel.setBackground(WHITE);
        detailsPanel.setBorder(
                new EmptyBorder(15, 15, 15, 15)
        );

        for (int i = 0; i < labels.length; i++) {

            JLabel nameLabel = new JLabel(labels[i] + ":");
            nameLabel.setFont(new Font("Georgia", Font.BOLD, 12));
            nameLabel.setForeground(BURGUNDY);

            JLabel valueLabel = new JLabel(
                    String.valueOf(tableModel.getValueAt(modelRow, i))
            );
            valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            valueLabel.setForeground(TEXT);

            detailsPanel.add(nameLabel);
            detailsPanel.add(valueLabel);
        }

        JOptionPane.showMessageDialog(
                this,
                detailsPanel,
                "Transaction Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}


package com.library.ui;

import com.library.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OverdueBooksFrame extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    private JLabel totalOverdueValue;
    private JLabel severeOverdueValue;
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

    private static final Color RED = new Color(175, 55, 55);
    private static final Color DARK_RED = new Color(135, 35, 35);

    public OverdueBooksFrame() {

        setTitle("Overdue Books");
        setSize(1250, 730);
        setMinimumSize(new Dimension(950, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());

        buildHeader();
        buildMainContent();
        buildBottomPanel();

        loadOverdueBooks("");
    }

    private void buildHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Overdue Books");
        title.setFont(new Font("Georgia", Font.BOLD, 27));
        title.setForeground(GOLD);

        JLabel subtitle = new JLabel(
                "Monitor unreturned books that have passed their due dates"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(235, 220, 215));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JButton closeButton = new JButton("CLOSE");
        styleButton(closeButton, DARK_BURGUNDY, WHITE);
        closeButton.addActionListener(e -> dispose());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    private void buildMainContent() {

        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(CREAM);
        main.setBorder(new EmptyBorder(20, 25, 10, 25));

        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryPanel.setOpaque(false);

        totalOverdueValue = createSummaryValue();
        severeOverdueValue = createSummaryValue();
        displayedValue = createSummaryValue();

        summaryPanel.add(createSummaryCard(
                "TOTAL OVERDUE BOOKS",
                totalOverdueValue,
                "All currently overdue loans"
        ));

        summaryPanel.add(createSummaryCard(
                "SEVERELY OVERDUE",
                severeOverdueValue,
                "Overdue for more than 30 days"
        ));

        summaryPanel.add(createSummaryCard(
                "DISPLAYED RECORDS",
                displayedValue,
                "Based on current search"
        ));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JLabel sectionTitle = new JLabel("Overdue Books Directory");
        sectionTitle.setFont(new Font("Georgia", Font.BOLD, 17));
        sectionTitle.setForeground(BURGUNDY);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(280, 38));
        searchField.setToolTipText(
                "Search by member, Student/Employee ID, or book title"
        );

        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 10, 7, 10)
        ));

        JButton clearButton = createButton(
                "CLEAR",
                MUTED,
                WHITE
        );

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(clearButton, BorderLayout.EAST);

        toolbar.add(sectionTitle, BorderLayout.WEST);
        toolbar.add(searchPanel, BorderLayout.EAST);

        String[] columns = {
                "Transaction ID",
                "Member Name",
                "Student/Employee ID",
                "Book Title",
                "Borrowing Date",
                "Due Date",
                "Days Overdue"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 || columnIndex == 6
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
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 8, 8, 8)
        ));

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

                        updateDisplayedCount();
                    }

                    @Override
                    public void insertUpdate(javax.swing.event.DocumentEvent e) {
                        filter();
                    }

                    @Override
                    public void removeUpdate(javax.swing.event.DocumentEvent e) {
                        filter();
                    }

                    @Override
                    public void changedUpdate(javax.swing.event.DocumentEvent e) {
                        filter();
                    }
                }
        );

        clearButton.addActionListener(e -> searchField.setText(""));
    }

    private void buildBottomPanel() {

        JPanel bottom = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 12)
        );

        bottom.setBackground(CREAM);
        bottom.setBorder(new EmptyBorder(5, 25, 12, 25));

        JButton detailsButton = createButton(
                "VIEW DETAILS",
                MUTED,
                WHITE
        );

        JButton refreshButton = createButton(
                "REFRESH",
                BURGUNDY,
                WHITE
        );

        detailsButton.addActionListener(e -> viewSelectedDetails());

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadOverdueBooks("");
        });

        bottom.add(detailsButton);
        bottom.add(refreshButton);

        add(bottom, BorderLayout.SOUTH);
    }

    private JLabel createSummaryValue() {

        JLabel label = new JLabel("0");
        label.setFont(new Font("Georgia", Font.BOLD, 27));
        label.setForeground(BURGUNDY);

        return label;
    }

    private JPanel createSummaryCard(
            String heading,
            JLabel value,
            String description
    ) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(14, 17, 14, 17)
        ));

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
        watermark.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(content, BorderLayout.CENTER);
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

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD, 1),
                new EmptyBorder(9, 15, 9, 15)
        ));
    }

    private void styleTable() {

        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setBackground(WHITE);
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(240, 235, 230));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(LIGHT_GOLD);
        table.setSelectionForeground(BURGUNDY);
        table.setFillsViewportHeight(true);

        table.getTableHeader().setFont(
                new Font("Georgia", Font.BOLD, 11)
        );
        table.getTableHeader().setBackground(BURGUNDY);
        table.getTableHeader().setForeground(WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        table.getTableHeader().setReorderingAllowed(false);

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

                        setBorder(new EmptyBorder(0, 8, 0, 8));

                        return component;
                    }
                };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(renderer);
        }

        DefaultTableCellRenderer daysRenderer =
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

                            int days = Integer.parseInt(
                                    String.valueOf(value)
                            );

                            component.setForeground(
                                    days > 30 ? DARK_RED : RED
                            );
                        }

                        setHorizontalAlignment(SwingConstants.CENTER);
                        setFont(new Font("Georgia", Font.BOLD, 12));

                        setBorder(new EmptyBorder(0, 8, 0, 8));

                        return component;
                    }
                };

        table.getColumnModel()
                .getColumn(6)
                .setCellRenderer(daysRenderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(155);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        table.getColumnModel().getColumn(4).setPreferredWidth(135);
        table.getColumnModel().getColumn(5).setPreferredWidth(135);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
    }

    private void loadOverdueBooks(String search) {

        tableModel.setRowCount(0);

        String sql = """
                SELECT
                    t.transaction_id,
                    m.full_name,
                    m.student_employee_id,
                    b.title,
                    t.borrowing_date,
                    t.due_date,
                    CAST(
                        julianday(date('now', 'localtime'))
                        - julianday(t.due_date)
                        AS INTEGER
                    ) AS days_overdue
                FROM transactions t
                JOIN members m
                    ON t.member_id = m.member_id
                JOIN books b
                    ON t.book_id = b.book_id
                WHERE t.return_date IS NULL
                  AND t.status IN ('BORROWED', 'OVERDUE')
                  AND t.due_date < date('now', 'localtime')
                  AND (
                      m.full_name LIKE ?
                      OR m.student_employee_id LIKE ?
                      OR b.title LIKE ?
                  )
                ORDER BY t.due_date ASC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String keyword = "%" + search + "%";

            statement.setString(1, keyword);
            statement.setString(2, keyword);
            statement.setString(3, keyword);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    tableModel.addRow(new Object[]{
                            result.getInt("transaction_id"),
                            result.getString("full_name"),
                            result.getString("student_employee_id"),
                            result.getString("title"),
                            result.getString("borrowing_date"),
                            result.getString("due_date"),
                            result.getInt("days_overdue")
                    });
                }
            }

            updateSummaryCards();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load overdue books:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    private void updateSummaryCards() {

        int total = tableModel.getRowCount();
        int severe = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {

            int days = Integer.parseInt(
                    String.valueOf(tableModel.getValueAt(i, 6))
            );

            if (days > 30) {
                severe++;
            }
        }

        totalOverdueValue.setText(String.valueOf(total));
        severeOverdueValue.setText(String.valueOf(severe));

        updateDisplayedCount();
    }

    private void updateDisplayedCount() {

        if (displayedValue != null && table != null) {
            displayedValue.setText(
                    String.valueOf(table.getRowCount())
            );
        }
    }

    private void viewSelectedDetails() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an overdue book first.",
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
                "Days Overdue"
        };

        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        detailsPanel.setBackground(WHITE);
        detailsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        for (int i = 0; i < labels.length; i++) {

            JLabel nameLabel = new JLabel(labels[i] + ":");
            nameLabel.setFont(new Font("Georgia", Font.BOLD, 12));
            nameLabel.setForeground(BURGUNDY);

            String value = String.valueOf(
                    tableModel.getValueAt(modelRow, i)
            );

            if (i == 6) {
                value += " days";
            }

            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            valueLabel.setForeground(TEXT);

            detailsPanel.add(nameLabel);
            detailsPanel.add(valueLabel);
        }

        JOptionPane.showMessageDialog(
                this,
                detailsPanel,
                "Overdue Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}

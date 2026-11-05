
package com.library.ui;

import com.library.database.DatabaseConnection;
import com.library.service.ReturnService;

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
import java.time.LocalDate;

public class ReturnBooksFrame extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    private JLabel borrowedValue;
    private JLabel overdueValue;
    private JLabel displayedValue;

    private static final Color BURGUNDY = new Color(91, 28, 43);
    private static final Color DARK_BURGUNDY = new Color(65, 18, 31);
    private static final Color GOLD = new Color(205, 164, 82);
    private static final Color CREAM = new Color(250, 247, 240);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(55, 45, 45);
    private static final Color MUTED = new Color(125, 115, 110);
    private static final Color GREEN = new Color(43, 125, 82);
    private static final Color RED = new Color(174, 55, 55);

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
        buildBottomPanel();

        loadBorrowedBooks("");
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

        JLabel title = new JLabel("Return Borrowed Books");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(WHITE);

        JLabel subtitle = new JLabel(
                "Review active loans and process returned library books"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(235, 220, 215));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JButton closeButton = new JButton("CLOSE");
        styleButton(closeButton, WHITE, BURGUNDY);
        closeButton.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(GOLD, 1),
                        BorderFactory.createEmptyBorder(9, 18, 9, 18)
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
                new GridLayout(1, 3, 15, 0)
        );
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

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JLabel sectionTitle = new JLabel("Borrowed Books Directory");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        sectionTitle.setForeground(BURGUNDY);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(260, 38));
        searchField.setToolTipText("Search by member or book title");

        searchField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(220, 210, 200)
                        ),
                        BorderFactory.createEmptyBorder(7, 10, 7, 10)
                )
        );

        JButton clearButton = createButton(
                "CLEAR",
                new Color(115, 105, 100),
                WHITE
        );

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(clearButton, BorderLayout.EAST);

        toolbar.add(sectionTitle, BorderLayout.WEST);
        toolbar.add(searchPanel, BorderLayout.EAST);

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
        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(225, 215, 205)
                )
        );
        scrollPane.getViewport().setBackground(WHITE);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(WHITE);
        tablePanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 215, 205)
                        ),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)
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
                                            "(?i)" + java.util.regex.Pattern.quote(text)
                                    )
                            );
                        }

                        updateDisplayedCount();
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

        clearButton.addActionListener(e -> searchField.setText(""));
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
                new Color(100, 90, 85),
                WHITE
        );

        JButton returnButton = createButton(
                "PROCESS RETURN",
                GREEN,
                WHITE
        );

        JButton refreshButton = createButton(
                "REFRESH",
                BURGUNDY,
                WHITE
        );

        detailsButton.addActionListener(e -> viewSelectedDetails());
        returnButton.addActionListener(e -> processReturn());

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadBorrowedBooks("");
        });

        bottom.add(detailsButton);
        bottom.add(returnButton);
        bottom.add(refreshButton);

        add(bottom, BorderLayout.SOUTH);
    }

    private JLabel createSummaryValue() {

        JLabel label = new JLabel("0");
        label.setFont(new Font("Segoe UI", Font.BOLD, 27));
        label.setForeground(BURGUNDY);

        return label;
    }

    private JPanel createSummaryCard(
            String heading,
            JLabel value,
            String description
    ) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(WHITE);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(230, 220, 210)
                        ),
                        BorderFactory.createEmptyBorder(15, 17, 15, 17)
                )
        );

        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        headingLabel.setForeground(MUTED);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descriptionLabel.setForeground(MUTED);

        card.add(headingLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(value);
        card.add(Box.createVerticalStrut(4));
        card.add(descriptionLabel);

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

        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.setBorder(
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        );
    }

    private void styleTable() {

        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setBackground(WHITE);
        table.setRowHeight(35);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(240, 235, 230));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(245, 231, 207));
        table.setSelectionForeground(BURGUNDY);
        table.setFillsViewportHeight(true);

        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        table.getTableHeader().setBackground(BURGUNDY);
        table.getTableHeader().setForeground(WHITE);
        table.getTableHeader().setPreferredSize(
                new Dimension(0, 40)
        );
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
                            component.setBackground(
                                    new Color(245, 231, 207)
                            );
                            component.setForeground(BURGUNDY);
                        } else {
                            component.setBackground(
                                    row % 2 == 0
                                            ? WHITE
                                            : new Color(250, 248, 244)
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

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
    }

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

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

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

    private Integer getSelectedTransactionId() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a borrowed book first.",
                    "No Book Selected",
                    JOptionPane.WARNING_MESSAGE
            );

            return null;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);

        return Integer.parseInt(
                String.valueOf(
                        tableModel.getValueAt(modelRow, 0)
                )
        );
    }

    private void viewSelectedDetails() {

        Integer transactionId = getSelectedTransactionId();

        if (transactionId == null) {
            return;
        }

        int modelRow = table.convertRowIndexToModel(
                table.getSelectedRow()
        );

        String[] labels = {
                "Transaction ID",
                "Member Name",
                "Book Title",
                "Borrowing Date",
                "Due Date",
                "Status"
        };

        JPanel detailsPanel = new JPanel(
                new GridLayout(0, 2, 12, 12)
        );

        detailsPanel.setBackground(WHITE);
        detailsPanel.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        for (int i = 0; i < labels.length; i++) {

            JLabel nameLabel = new JLabel(labels[i] + ":");
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
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
                "Borrowing Details - #" + transactionId,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void processReturn() {

        Integer transactionId = getSelectedTransactionId();

        if (transactionId == null) {
            return;
        }

        int modelRow = table.convertRowIndexToModel(
                table.getSelectedRow()
        );

        String memberName = String.valueOf(
                tableModel.getValueAt(modelRow, 1)
        );

        String bookTitle = String.valueOf(
                tableModel.getValueAt(modelRow, 2)
        );

        String status = String.valueOf(
                tableModel.getValueAt(modelRow, 5)
        );

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Have you physically received and inspected this book?\n\n"
                        + "Transaction ID: " + transactionId
                        + "\nBook: " + bookTitle
                        + "\nMember: " + memberName
                        + "\nCurrent Status: " + status
                        + "\nReturn Date: " + LocalDate.now()
                        + "\n\nContinue with the return?",
                "Confirm Book Return",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            boolean success = ReturnService.returnBook(
                    transactionId,
                    LocalDate.now()
            );

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Book returned successfully!",
                        "Return Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );

                loadBorrowedBooks(searchField.getText().trim());

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "The return could not be completed. Please check the transaction.",
                        "Return Not Completed",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to process return:\n" + e.getMessage(),
                    "Return Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }
}


package com.library.ui;

import com.library.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MyBorrowingHistoryFrame extends JFrame {
    private static final Color BURGUNDY = new Color(75, 20, 36);
    private static final Color DARK_BURGUNDY = new Color(55, 13, 27);
    private static final Color GOLD = new Color(176, 141, 87);
    private static final Color CREAM = new Color(243, 235, 221);
    private static final Color WHITE = new Color(255, 252, 245);
    private static final Color TEXT = new Color(53, 37, 31);
    private static final Color MUTED = new Color(130, 115, 103);
    private static final Color BORDER = new Color(222, 210, 191);
    private static final Color LIGHT_GOLD = new Color(244, 232, 207);

    private JTable table;
    private DefaultTableModel tableModel;

    private final int memberId;

    private JLabel totalLabel;
    private JLabel returnedLabel;
    private JLabel activeLabel;

    public MyBorrowingHistoryFrame(int memberId) {

        this.memberId = memberId;

        setTitle("LibraryLink - My Borrowing History");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setSize(1200, 750);
        setLocationRelativeTo(null);
        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("My Borrowing History");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 27));
        titleLabel.setForeground(GOLD);

        JLabel subtitleLabel = new JLabel(
                "Review your previous and current book transactions"
        );
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(235, 218, 211));

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitleLabel);

        JButton closeButton = createButton("Close", BURGUNDY, WHITE);
        closeButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD, 1),
                new EmptyBorder(8, 18, 8, 18)
        ));
        closeButton.addActionListener(e -> dispose());

        header.add(titlePanel, BorderLayout.CENTER);
        header.add(closeButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // MAIN CONTENT
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(CREAM);
        content.setBorder(new EmptyBorder(22, 25, 20, 25));

        // SUMMARY CARDS
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryPanel.setOpaque(false);

        totalLabel = new JLabel("0");
        returnedLabel = new JLabel("0");
        activeLabel = new JLabel("0");

        summaryPanel.add(createSummaryCard(
                "TOTAL TRANSACTIONS", totalLabel
        ));

        summaryPanel.add(createSummaryCard(
                "BOOKS RETURNED", returnedLabel
        ));

        summaryPanel.add(createSummaryCard(
                "ACTIVE BORROWINGS", activeLabel
        ));

        content.add(summaryPanel, BorderLayout.NORTH);

        // TABLE
        String[] columns = {
                "Transaction ID",
                "Book Title",
                "Author",
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
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setBackground(WHITE);
        table.setRowHeight(38);
        table.setSelectionBackground(LIGHT_GOLD);
        table.setSelectionForeground(BURGUNDY);
        table.setGridColor(new Color(239, 234, 226));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Georgia", Font.BOLD, 11));
        tableHeader.setBackground(BURGUNDY);
        tableHeader.setForeground(WHITE);
        tableHeader.setPreferredSize(new Dimension(0, 42));
        tableHeader.setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer =
                new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        table.getColumnModel().getColumn(0)
                .setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3)
                .setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4)
                .setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5)
                .setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6)
                .setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(WHITE);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setBackground(CREAM);

        JLabel tableTitle = new JLabel("Transaction Records");
        tableTitle.setFont(new Font("Georgia", Font.BOLD, 17));
        tableTitle.setForeground(BURGUNDY);

        tablePanel.add(tableTitle, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        content.add(tablePanel, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        // FOOTER
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(CREAM);
        footer.setBorder(new EmptyBorder(0, 25, 20, 25));

        JLabel footerLabel = new JLabel(
                "LibraryLink  |  Your personal borrowing records"
        );
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(MUTED);

        JButton refreshButton = createButton(
                "Refresh History", BURGUNDY, WHITE
        );

        refreshButton.addActionListener(e -> loadBorrowingHistory());

        footer.add(footerLabel, BorderLayout.WEST);
        footer.add(refreshButton, BorderLayout.EAST);

        add(footer, BorderLayout.SOUTH);

        loadBorrowingHistory();
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 27));
        valueLabel.setForeground(BURGUNDY);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(valueLabel);

        JLabel decoration = new JLabel("L");
        decoration.setFont(new Font("Georgia", Font.BOLD, 48));
        decoration.setForeground(new Color(225, 211, 185));

        JPanel cardContent = new JPanel(new BorderLayout());
        cardContent.setOpaque(false);
        cardContent.add(textPanel, BorderLayout.CENTER);
        cardContent.add(decoration, BorderLayout.EAST);

        card.setLayout(new BorderLayout());
        card.add(cardContent, BorderLayout.CENTER);

        return card;
    }

    private JButton createButton(
            String text, Color background, Color foreground) {

        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        button.setOpaque(true);

        return button;
    }

    private void loadBorrowingHistory() {

        tableModel.setRowCount(0);

        String sql = """
                SELECT
                    t.transaction_id,
                    b.title,
                    b.author,
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

                JOIN books b
                    ON t.book_id = b.book_id

                WHERE t.member_id = ?

                ORDER BY t.borrowing_date DESC,
                         t.transaction_id DESC
                """;

        int total = 0;
        int returned = 0;
        int active = 0;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, memberId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    String returnDate = result.getString("return_date");
                    String status = result.getString("display_status");

                    total++;

                    if ("RETURNED".equals(status)) {
                        returned++;
                    } else {
                        active++;
                    }

                    tableModel.addRow(new Object[]{
                            result.getInt("transaction_id"),
                            result.getString("title"),
                            result.getString("author"),
                            result.getString("borrowing_date"),
                            result.getString("due_date"),
                            returnDate == null || returnDate.isBlank()
                                    ? "Not yet returned"
                                    : returnDate,
                            status
                    });
                }
            }

            totalLabel.setText(String.valueOf(total));
            returnedLabel.setText(String.valueOf(returned));
            activeLabel.setText(String.valueOf(active));

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load your borrowing history:\n"
                            + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }
}

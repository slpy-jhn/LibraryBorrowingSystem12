
package com.library.ui;

import com.library.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MyBorrowingHistoryFrame extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;

    private final int memberId;

    public MyBorrowingHistoryFrame(int memberId) {

        this.memberId = memberId;

        setTitle("My Borrowing History");
        setSize(1050, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));

        getContentPane().setBackground(
                new Color(245, 247, 250)
        );

        // HEADER
        JLabel titleLabel = new JLabel("My Borrowing History");

        titleLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 25)
        );

        titleLabel.setForeground(
                new Color(23, 43, 77)
        );

        titleLabel.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 5, 20)
        );

        add(titleLabel, BorderLayout.NORTH);

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

        table.setRowHeight(30);

        table.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder(0, 20, 0, 20)
        );

        add(scrollPane, BorderLayout.CENTER);

        // BOTTOM PANEL
        JPanel bottomPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 10)
        );

        bottomPanel.setBackground(
                new Color(245, 247, 250)
        );

        bottomPanel.setBorder(
                BorderFactory.createEmptyBorder(5, 20, 15, 20)
        );

        JButton refreshButton = new JButton("Refresh");

        bottomPanel.add(refreshButton);

        add(bottomPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e ->
                loadBorrowingHistory()
        );

        loadBorrowingHistory();
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

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, memberId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    tableModel.addRow(new Object[]{
                            result.getInt("transaction_id"),
                            result.getString("title"),
                            result.getString("author"),
                            result.getString("borrowing_date"),
                            result.getString("due_date"),
                            result.getString("return_date") == null
                                    ? "Not yet returned"
                                    : result.getString("return_date"),
                            result.getString("display_status")
                    });
                }
            }

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

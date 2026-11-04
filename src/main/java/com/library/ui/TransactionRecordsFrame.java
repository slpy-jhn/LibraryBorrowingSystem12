
package com.library.ui;

import com.library.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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

    public TransactionRecordsFrame() {

        setTitle("Transaction Records");
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));

        getContentPane().setBackground(
                new Color(245, 247, 250)
        );

        // HEADER
        JLabel titleLabel = new JLabel("Transaction Records");

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
                new FlowLayout(FlowLayout.LEFT, 10, 10)
        );

        bottomPanel.setBackground(
                new Color(245, 247, 250)
        );

        bottomPanel.setBorder(
                BorderFactory.createEmptyBorder(5, 20, 15, 20)
        );

        searchField = new JTextField(20);

        statusFilter = new JComboBox<>(new String[]{
                "ALL",
                "BORROWED",
                "OVERDUE",
                "RETURNED"
        });

        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Refresh");

        bottomPanel.add(new JLabel("Search:"));
        bottomPanel.add(searchField);

        bottomPanel.add(new JLabel("Status:"));
        bottomPanel.add(statusFilter);

        bottomPanel.add(searchButton);
        bottomPanel.add(refreshButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // BUTTON ACTIONS
        searchButton.addActionListener(e ->
                loadTransactions(
                        searchField.getText().trim(),
                        statusFilter.getSelectedItem().toString()
                )
        );

        searchField.addActionListener(e ->
                loadTransactions(
                        searchField.getText().trim(),
                        statusFilter.getSelectedItem().toString()
                )
        );

        statusFilter.addActionListener(e ->
                loadTransactions(
                        searchField.getText().trim(),
                        statusFilter.getSelectedItem().toString()
                )
        );

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedItem("ALL");
            loadTransactions("", "ALL");
        });

        loadTransactions("", "ALL");
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
}


package com.library.ui;

import com.library.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OverdueBooksFrame extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public OverdueBooksFrame() {
        setTitle("Overdue Books");
        setSize(1000, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));

        getContentPane().setBackground(
                new Color(245, 247, 250)
        );

        // HEADER
        JLabel titleLabel = new JLabel("Overdue Books");
        titleLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 25)
        );
        titleLabel.setForeground(new Color(23, 43, 77));
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
                "Days Overdue"
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
                new Font("Segoe UI", Font.PLAIN, 13)
        );
        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(
                BorderFactory.createEmptyBorder(0, 20, 0, 20)
        );

        add(scrollPane, BorderLayout.CENTER);

        // BOTTOM PANEL
        JPanel bottomPanel = new JPanel(
                new BorderLayout(10, 10)
        );
        bottomPanel.setBackground(
                new Color(245, 247, 250)
        );
        bottomPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 20, 20, 20)
        );

        JPanel searchPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );
        searchPanel.setBackground(
                new Color(245, 247, 250)
        );

        searchField = new JTextField(22);

        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Refresh");

        searchPanel.add(new JLabel("Search member or book:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        bottomPanel.add(searchPanel, BorderLayout.WEST);

        add(bottomPanel, BorderLayout.SOUTH);

        // BUTTON ACTIONS
        searchButton.addActionListener(e ->
                loadOverdueBooks(searchField.getText().trim())
        );

        searchField.addActionListener(e ->
                loadOverdueBooks(searchField.getText().trim())
        );

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadOverdueBooks("");
        });

        loadOverdueBooks("");
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

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

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

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load overdue books:\n"
                            + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }
}

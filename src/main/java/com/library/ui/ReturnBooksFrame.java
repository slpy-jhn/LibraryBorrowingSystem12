
package com.library.ui;

import com.library.database.DatabaseConnection;
import com.library.service.ReturnService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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

    public ReturnBooksFrame() {
        setTitle("Return Books");
        setSize(950, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));

        getContentPane().setBackground(new Color(245, 247, 250));

        // HEADER
        JLabel titleLabel = new JLabel("Return Borrowed Books");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 5, 20)
        );

        add(titleLabel, BorderLayout.NORTH);

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
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(
                BorderFactory.createEmptyBorder(0, 20, 0, 20)
        );

        add(scrollPane, BorderLayout.CENTER);

        // BOTTOM PANEL
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 20, 20, 20)
        );
        bottomPanel.setBackground(new Color(245, 247, 250));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(new Color(245, 247, 250));

        searchField = new JTextField(22);

        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Refresh");
        JButton returnButton = new JButton("Process Return");

        searchPanel.add(new JLabel("Search member or book:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(new Color(245, 247, 250));
        actionPanel.add(returnButton);

        bottomPanel.add(searchPanel, BorderLayout.WEST);
        bottomPanel.add(actionPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // BUTTON ACTIONS
        searchButton.addActionListener(e -> loadBorrowedBooks(
                searchField.getText().trim()
        ));

        searchField.addActionListener(e -> loadBorrowedBooks(
                searchField.getText().trim()
        ));

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadBorrowedBooks("");
        });

        returnButton.addActionListener(e -> processReturn());

        loadBorrowedBooks("");
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

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load borrowed books:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void processReturn() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a borrowed book first."
            );
            return;
        }

        int transactionId = (int) tableModel.getValueAt(
                selectedRow, 0
        );

        String memberName = tableModel.getValueAt(
                selectedRow, 1
        ).toString();

        String bookTitle = tableModel.getValueAt(
                selectedRow, 2
        ).toString();

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Process the return of:\n\n" +
                        "Book: " + bookTitle + "\n" +
                        "Member: " + memberName + "\n\n" +
                        "Continue?",
                "Confirm Book Return",
                JOptionPane.YES_NO_OPTION
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
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to process return:\n" + e.getMessage(),
                    "Return Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}

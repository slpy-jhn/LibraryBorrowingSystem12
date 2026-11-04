
package com.library.ui;

import com.library.database.DatabaseConnection;
import com.library.service.ReturnRequestService;
import com.library.service.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MyBorrowedBooksFrame extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;

    private final UserSession session;
    private final int memberId;

    public MyBorrowedBooksFrame(UserSession session) {

        if (session == null) {
            throw new IllegalArgumentException(
                    "A valid user session is required."
            );
        }

        // Confirm that this is a student session.
        this.memberId = session.requireStudent();
        this.session = session;

        setTitle("My Borrowed Books");
        setSize(1000, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));

        getContentPane().setBackground(
                new Color(245, 247, 250)
        );

        // HEADER
        JLabel titleLabel = new JLabel("My Borrowed Books");

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
                "Days Remaining",
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

        JButton requestReturnButton =
                new JButton("Request to Return");

        JButton refreshButton =
                new JButton("Refresh");

        bottomPanel.add(requestReturnButton);
        bottomPanel.add(refreshButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // BUTTON ACTIONS
        refreshButton.addActionListener(e ->
                loadBorrowedBooks()
        );

        requestReturnButton.addActionListener(e ->
                submitReturnRequest()
        );

        loadBorrowedBooks();
    }

    // LOAD STUDENT'S BORROWED BOOKS
    private void loadBorrowedBooks() {

        tableModel.setRowCount(0);

        String sql = """
                SELECT
                    t.transaction_id,
                    b.title,
                    b.author,
                    t.borrowing_date,
                    t.due_date,

                    CAST(
                        julianday(t.due_date)
                        - julianday(date('now', 'localtime'))
                        AS INTEGER
                    ) AS days_remaining,

                    CASE
                        WHEN t.due_date < date('now', 'localtime')
                            THEN 'OVERDUE'
                        ELSE 'BORROWED'
                    END AS display_status

                FROM transactions t

                JOIN books b
                    ON t.book_id = b.book_id

                WHERE t.member_id = ?
                  AND t.return_date IS NULL
                  AND t.status IN ('BORROWED', 'OVERDUE')

                ORDER BY t.due_date ASC
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, memberId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    int daysRemaining =
                            result.getInt("days_remaining");

                    String daysText;

                    if (daysRemaining < 0) {

                        daysText = Math.abs(daysRemaining)
                                + " day(s) overdue";

                    } else if (daysRemaining == 0) {

                        daysText = "Due today";

                    } else {

                        daysText = daysRemaining + " day(s)";
                    }

                    tableModel.addRow(new Object[]{
                            result.getInt("transaction_id"),
                            result.getString("title"),
                            result.getString("author"),
                            result.getString("borrowing_date"),
                            result.getString("due_date"),
                            daysText,
                            result.getString("display_status")
                    });
                }
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load your borrowed books:\n"
                            + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    // STUDENT: SUBMIT RETURN REQUEST
    private void submitReturnRequest() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a borrowed book first.",
                    "No Book Selected",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int modelRow =
                table.convertRowIndexToModel(selectedRow);

        int transactionId = Integer.parseInt(
                String.valueOf(
                        tableModel.getValueAt(modelRow, 0)
                )
        );

        String bookTitle = String.valueOf(
                tableModel.getValueAt(modelRow, 1)
        );

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Submit a return request for:\n\n"
                        + bookTitle
                        + "\n\nThe librarian must inspect the physical book "
                        + "and approve your return request.",
                "Confirm Return Request",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            // Submit return request using the authenticated student session.
            ReturnRequestService.submitReturnRequest(
                    session,
                    transactionId
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Your return request has been submitted!\n"
                            + "Please bring the book to the library for inspection.",
                    "Return Request Submitted",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadBorrowedBooks();

        } catch (SQLException
                 | IllegalArgumentException
                 | SecurityException
                 | IllegalStateException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Unable to Submit Return Request",
                    JOptionPane.WARNING_MESSAGE
            );

            e.printStackTrace();
        }
    }
}

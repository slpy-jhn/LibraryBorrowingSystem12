
package com.library.ui;

import com.library.database.DatabaseConnection;
import com.library.service.BorrowRequestService;
import com.library.service.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentBooksFrame extends JFrame {

    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    private final UserSession session;

    public StudentBooksFrame(UserSession session) {

        if (session == null) {
            throw new SecurityException("Please log in first.");
        }

        session.requireStudent();
        this.session = session;

        setTitle("Browse Library Books");
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));

        getContentPane().setBackground(
                new Color(245, 247, 250)
        );

        // HEADER
        JLabel titleLabel = new JLabel("Browse Library Books");

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
                "Book ID",
                "ISBN",
                "Title",
                "Author",
                "Category",
                "Publisher",
                "Year",
                "Available Copies",
                "Shelf Location"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookTable = new JTable(tableModel);
        bookTable.setRowHeight(30);

        bookTable.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        bookTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        JScrollPane scrollPane = new JScrollPane(bookTable);

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder(0, 20, 0, 20)
        );

        add(scrollPane, BorderLayout.CENTER);

        // SEARCH AND ACTION PANEL
        JPanel searchPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 10)
        );

        searchPanel.setBackground(
                new Color(245, 247, 250)
        );

        searchPanel.setBorder(
                BorderFactory.createEmptyBorder(5, 20, 15, 20)
        );

        searchField = new JTextField(20);

        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Refresh");
        JButton detailsButton = new JButton("View Details");
        JButton requestButton = new JButton("Request to Borrow");

        searchPanel.add(new JLabel("Search Books:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);
        searchPanel.add(detailsButton);
        searchPanel.add(requestButton);

        add(searchPanel, BorderLayout.SOUTH);

        // BUTTON ACTIONS
        searchButton.addActionListener(e ->
                loadBooks(searchField.getText().trim())
        );

        searchField.addActionListener(e ->
                loadBooks(searchField.getText().trim())
        );

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadBooks("");
        });

        detailsButton.addActionListener(e ->
                showBookDetails()
        );

        requestButton.addActionListener(e ->
                submitBorrowRequest()
        );

        loadBooks("");
    }

    private void loadBooks(String search) {

        tableModel.setRowCount(0);

        String sql = """
                SELECT
                    book_id,
                    isbn,
                    title,
                    author,
                    category,
                    publisher,
                    publication_year,
                    available_copies,
                    shelf_location
                FROM books
                WHERE (
                    title LIKE ?
                    OR author LIKE ?
                    OR category LIKE ?
                    OR isbn LIKE ?
                )
                ORDER BY title ASC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String keyword = "%" + search + "%";

            statement.setString(1, keyword);
            statement.setString(2, keyword);
            statement.setString(3, keyword);
            statement.setString(4, keyword);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    tableModel.addRow(new Object[]{
                            result.getInt("book_id"),
                            result.getString("isbn"),
                            result.getString("title"),
                            result.getString("author"),
                            result.getString("category"),
                            result.getString("publisher"),
                            result.getObject("publication_year"),
                            result.getInt("available_copies"),
                            result.getString("shelf_location")
                    });
                }
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load books:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    private void showBookDetails() {

        int selectedRow = bookTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a book first.",
                    "No Book Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow = bookTable.convertRowIndexToModel(selectedRow);

        String title = String.valueOf(tableModel.getValueAt(modelRow, 2));
        String author = String.valueOf(tableModel.getValueAt(modelRow, 3));
        String category = String.valueOf(tableModel.getValueAt(modelRow, 4));
        String publisher = String.valueOf(tableModel.getValueAt(modelRow, 5));
        String year = String.valueOf(tableModel.getValueAt(modelRow, 6));
        String available = String.valueOf(tableModel.getValueAt(modelRow, 7));
        String shelf = String.valueOf(tableModel.getValueAt(modelRow, 8));

        String details = """
                Book Title: %s

                Author: %s

                Category: %s

                Publisher: %s

                Publication Year: %s

                Available Copies: %s

                Shelf Location: %s
                """.formatted(
                title, author, category, publisher,
                year, available, shelf
        );

        JOptionPane.showMessageDialog(
                this,
                details,
                "Book Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void submitBorrowRequest() {

        int selectedRow = bookTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a book first.",
                    "No Book Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow = bookTable.convertRowIndexToModel(selectedRow);

        int bookId = (int) tableModel.getValueAt(modelRow, 0);

        String bookTitle = String.valueOf(
                tableModel.getValueAt(modelRow, 2)
        );

        int availableCopies = Integer.parseInt(
                String.valueOf(tableModel.getValueAt(modelRow, 7))
        );

        if (availableCopies <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "This book currently has no available copies.",
                    "Book Unavailable",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Submit a borrowing request for:\n\n"
                        + bookTitle
                        + "\n\nA librarian must approve your request "
                        + "before the book is borrowed.",
                "Confirm Borrowing Request",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            BorrowRequestService.submitRequest(session, bookId);

            JOptionPane.showMessageDialog(
                    this,
                    "Your borrowing request has been submitted!\n"
                            + "You can check its status in My Borrowing Requests.",
                    "Request Submitted",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadBooks(searchField.getText().trim());

        } catch (SQLException
                 | IllegalArgumentException
                 | SecurityException
                 | IllegalStateException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage() == null
                            ? "An unexpected error occurred while submitting your request."
                            : e.getMessage(),
                    "Unable to Submit Request",
                    JOptionPane.WARNING_MESSAGE
            );

            e.printStackTrace();
        }
    }
}


package com.library.ui;

import com.library.database.DatabaseConnection;
import com.library.service.BorrowService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class BorrowBooksFrame extends JFrame {

    private final JComboBox<MemberItem> memberBox;
    private final JComboBox<BookItem> bookBox;
    private final JTextField borrowingDateField;
    private final JTextField dueDateField;

    private final Color NAVY = new Color(23, 43, 77);
    private final Color LIGHT_BG = new Color(245, 247, 250);

    public BorrowBooksFrame() {
        setTitle("Borrow Books");
        setSize(650, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(LIGHT_BG);
        main.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel heading = new JLabel("Process Book Borrowing");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 25));
        heading.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "Select a member, book, and borrowing dates."
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);

        JPanel header = new JPanel();
        header.setBackground(LIGHT_BG);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(heading);
        header.add(Box.createVerticalStrut(8));
        header.add(subtitle);

        main.add(header, BorderLayout.NORTH);

        memberBox = new JComboBox<>();
        bookBox = new JComboBox<>();

        borrowingDateField = new JTextField(
                LocalDate.now().toString()
        );

        dueDateField = new JTextField(
                LocalDate.now().plusDays(7).toString()
        );

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(25, 25, 25, 25));

        form.add(new JLabel("Select Member:"));
        form.add(memberBox);

        form.add(new JLabel("Select Available Book:"));
        form.add(bookBox);

        form.add(new JLabel("Borrowing Date (YYYY-MM-DD):"));
        form.add(borrowingDateField);

        form.add(new JLabel("Due Date (YYYY-MM-DD):"));
        form.add(dueDateField);

        main.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );
        buttons.setBackground(LIGHT_BG);

        JButton refreshButton = new JButton("Refresh");
        JButton borrowButton = new JButton("Confirm Borrowing");
        JButton cancelButton = new JButton("Cancel");

        borrowButton.setBackground(NAVY);
        borrowButton.setForeground(Color.WHITE);

        buttons.add(refreshButton);
        buttons.add(cancelButton);
        buttons.add(borrowButton);

        main.add(buttons, BorderLayout.SOUTH);

        add(main);

        refreshButton.addActionListener(e -> loadData());

        cancelButton.addActionListener(e -> dispose());

        borrowButton.addActionListener(e -> processBorrowing());

        loadData();
    }

    private void loadData() {
        loadMembers();
        loadBooks();
    }

    private void loadMembers() {
        memberBox.removeAllItems();

        String sql = """
                SELECT member_id, full_name, student_employee_id
                FROM members
                WHERE membership_status = 'ACTIVE'
                ORDER BY full_name
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                memberBox.addItem(new MemberItem(
                        result.getInt("member_id"),
                        result.getString("full_name"),
                        result.getString("student_employee_id")
                ));
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void loadBooks() {
        bookBox.removeAllItems();

        String sql = """
                SELECT book_id, title, author, available_copies
                FROM books
                WHERE available_copies > 0
                ORDER BY title
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                bookBox.addItem(new BookItem(
                        result.getInt("book_id"),
                        result.getString("title"),
                        result.getString("author"),
                        result.getInt("available_copies")
                ));
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void processBorrowing() {
        MemberItem member =
                (MemberItem) memberBox.getSelectedItem();

        BookItem book =
                (BookItem) bookBox.getSelectedItem();

        if (member == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please register or select an active member first.",
                    "No Member Available",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (book == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "There are no available books to borrow.",
                    "No Book Available",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            LocalDate borrowingDate =
                    LocalDate.parse(borrowingDateField.getText().trim());

            LocalDate dueDate =
                    LocalDate.parse(dueDateField.getText().trim());

            if (borrowingDate.isAfter(LocalDate.now())) {
                JOptionPane.showMessageDialog(
                        this,
                        "Borrowing date cannot be in the future.",
                        "Invalid Date",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (dueDate.isBefore(borrowingDate)
                    || dueDate.isEqual(borrowingDate)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Due date must be after the borrowing date.",
                        "Invalid Due Date",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            int confirmation = JOptionPane.showConfirmDialog(
                    this,
                    "Member: " + member.name
                            + "\nBook: " + book.title
                            + "\nBorrowing Date: " + borrowingDate
                            + "\nDue Date: " + dueDate
                            + "\n\nConfirm this transaction?",
                    "Confirm Borrowing",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirmation != JOptionPane.YES_OPTION) {
                return;
            }

            boolean success = BorrowService.borrowBook(
                    member.id,
                    book.id,
                    borrowingDate,
                    dueDate
            );

            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Book borrowed successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                loadBooks();

                dueDateField.setText(
                        LocalDate.now().plusDays(7).toString()
                );
            }

        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid dates using YYYY-MM-DD.",
                    "Invalid Date Format",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Borrowing Failed",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void showError(SQLException e) {
        JOptionPane.showMessageDialog(
                this,
                "Database error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
        e.printStackTrace();
    }

    private static class MemberItem {
        private final int id;
        private final String name;
        private final String studentId;

        MemberItem(int id, String name, String studentId) {
            this.id = id;
            this.name = name;
            this.studentId = studentId;
        }

        @Override
        public String toString() {
            return name + " (" + studentId + ")";
        }
    }

    private static class BookItem {
        private final int id;
        private final String title;
        private final String author;
        private final int availableCopies;

        BookItem(int id, String title, String author, int availableCopies) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.availableCopies = availableCopies;
        }

        @Override
        public String toString() {
            return title + " - " + author
                    + " (" + availableCopies + " available)";
        }
    }
}

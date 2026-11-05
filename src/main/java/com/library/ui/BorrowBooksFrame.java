
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
import java.time.format.DateTimeParseException;

public class BorrowBooksFrame extends JFrame {

    private final JComboBox<MemberItem> memberBox;
    private final JComboBox<BookItem> bookBox;
    private final JTextField borrowingDateField;
    private final JTextField dueDateField;

    private JLabel memberCountLabel;
    private JLabel bookCountLabel;

    // LIBRARYLINK THEME

    private static final Color BURGUNDY = new Color(75, 20, 36);
    private static final Color DARK_BURGUNDY = new Color(55, 13, 27);
    private static final Color GOLD = new Color(176, 141, 87);
    private static final Color CREAM = new Color(243, 235, 221);
    private static final Color WHITE = new Color(255, 252, 245);
    private static final Color TEXT = new Color(53, 37, 31);
    private static final Color MUTED = new Color(130, 115, 103);
    private static final Color BORDER = new Color(222, 210, 191);
    private static final Color LIGHT_GOLD = new Color(244, 232, 207);
    private static final Color GREEN = new Color(38, 125, 82);

    public BorrowBooksFrame() {

        setTitle("LibraryLink - Borrow Book");
        setSize(850, 720);
        setMinimumSize(new Dimension(700, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());

        memberBox = new JComboBox<>();
        bookBox = new JComboBox<>();

        borrowingDateField = new JTextField(
                LocalDate.now().toString()
        );

        dueDateField = new JTextField(
                LocalDate.now().plusDays(7).toString()
        );

        buildHeader();
        buildMainContent();
        buildBottomPanel();

        loadData();
    }

    // ==========================================
    // HEADER
    // ==========================================

    private void buildHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(
                new EmptyBorder(22, 28, 22, 28)
        );

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("Borrow Book");
        title.setFont(new Font("Georgia", Font.BOLD, 27));
        title.setForeground(GOLD);

        JLabel subtitle = new JLabel(
                "Process a book borrowing transaction for a library member."
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(235, 220, 207));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitle);

        JButton closeButton = createButton(
                "CLOSE",
                BURGUNDY,
                WHITE
        );

        closeButton.addActionListener(e -> dispose());

        JPanel rightPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 5)
        );
        rightPanel.setOpaque(false);
        rightPanel.add(closeButton);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    // ==========================================
    // MAIN CONTENT
    // ==========================================

    private void buildMainContent() {

        JPanel main = new JPanel(new BorderLayout(0, 20));
        main.setBackground(CREAM);
        main.setBorder(
                new EmptyBorder(20, 25, 12, 25)
        );

        // SUMMARY CARDS

        JPanel summaryPanel = new JPanel(
                new GridLayout(1, 2, 15, 0)
        );
        summaryPanel.setOpaque(false);

        memberCountLabel = createSummaryValue();
        bookCountLabel = createSummaryValue();

        summaryPanel.add(createSummaryCard(
                "ACTIVE MEMBERS",
                memberCountLabel,
                "Members eligible to borrow"
        ));

        summaryPanel.add(createSummaryCard(
                "AVAILABLE BOOKS",
                bookCountLabel,
                "Books currently available"
        ));

        // BORROWING INFORMATION PANEL

        JPanel formPanel = new JPanel(
                new BorderLayout(0, 22)
        );

        formPanel.setBackground(WHITE);
        formPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(25, 30, 25, 30)
                )
        );

        // FORM HEADER

        JPanel formHeader = new JPanel();
        formHeader.setOpaque(false);
        formHeader.setLayout(
                new BoxLayout(formHeader, BoxLayout.Y_AXIS)
        );

        JLabel formTitle = new JLabel(
                "BORROWING INFORMATION"
        );
        formTitle.setFont(
                new Font("Georgia", Font.BOLD, 18)
        );
        formTitle.setForeground(BURGUNDY);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel formSubtitle = new JLabel(
                "Complete the information below to process a borrowing transaction."
        );
        formSubtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        formSubtitle.setForeground(MUTED);
        formSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        formHeader.add(formTitle);
        formHeader.add(Box.createVerticalStrut(7));
        formHeader.add(formSubtitle);

        // FORM FIELDS

        configureComboBox(memberBox);
        configureComboBox(bookBox);

        configureDateField(borrowingDateField);
        configureDateField(dueDateField);

        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(
                new BoxLayout(fields, BoxLayout.Y_AXIS)
        );

        fields.add(createFieldLabel("SELECT MEMBER"));
        fields.add(Box.createVerticalStrut(7));
        fields.add(memberBox);
        fields.add(Box.createVerticalStrut(15));

        fields.add(createFieldLabel("SELECT AVAILABLE BOOK"));
        fields.add(Box.createVerticalStrut(7));
        fields.add(bookBox);
        fields.add(Box.createVerticalStrut(15));

        fields.add(createFieldLabel("BORROWING DATE (YYYY-MM-DD)"));
        fields.add(Box.createVerticalStrut(7));
        fields.add(borrowingDateField);
        fields.add(Box.createVerticalStrut(15));

        fields.add(createFieldLabel("DUE DATE (YYYY-MM-DD)"));
        fields.add(Box.createVerticalStrut(7));
        fields.add(dueDateField);
        fields.add(Box.createVerticalStrut(13));

        JLabel note = new JLabel(
                "<html><b>Note:</b> The due date must be later than the borrowing date.</html>"
        );
        note.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        note.setForeground(MUTED);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel formCenter = new JPanel();
        formCenter.setOpaque(false);
        formCenter.setLayout(
                new BoxLayout(formCenter, BoxLayout.Y_AXIS)
        );

        formCenter.add(fields);
        formCenter.add(note);

        formPanel.add(formHeader, BorderLayout.NORTH);
        formPanel.add(formCenter, BorderLayout.CENTER);

        main.add(summaryPanel, BorderLayout.NORTH);
        main.add(formPanel, BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);
    }

    // ==========================================
    // BOTTOM ACTION PANEL
    // ==========================================

    private void buildBottomPanel() {

        JPanel bottom = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 12)
        );

        bottom.setBackground(CREAM);
        bottom.setBorder(
                new EmptyBorder(3, 25, 10, 25)
        );

        JButton refreshButton = createButton(
                "REFRESH",
                BURGUNDY,
                WHITE
        );

        JButton cancelButton = createButton(
                "CANCEL",
                CREAM,
                BURGUNDY
        );

        JButton borrowButton = createButton(
                "CONFIRM BORROWING",
                GREEN,
                WHITE
        );

        refreshButton.addActionListener(e -> loadData());
        cancelButton.addActionListener(e -> dispose());
        borrowButton.addActionListener(e -> processBorrowing());

        bottom.add(refreshButton);
        bottom.add(cancelButton);
        bottom.add(borrowButton);

        add(bottom, BorderLayout.SOUTH);
    }

    // ==========================================
    // SUMMARY CARDS
    // ==========================================

    private JLabel createSummaryValue() {

        JLabel label = new JLabel("0");
        label.setFont(
                new Font("Georgia", Font.BOLD, 27)
        );
        label.setForeground(BURGUNDY);

        return label;
    }

    private JPanel createSummaryCard(
            String heading,
            JLabel value,
            String description
    ) {

        JPanel textPanel = new JPanel();
        textPanel.setLayout(
                new BoxLayout(textPanel, BoxLayout.Y_AXIS)
        );
        textPanel.setOpaque(false);

        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(
                new Font("Georgia", Font.BOLD, 11)
        );
        headingLabel.setForeground(MUTED);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 11)
        );
        descriptionLabel.setForeground(MUTED);

        textPanel.add(headingLabel);
        textPanel.add(Box.createVerticalStrut(7));
        textPanel.add(value);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(descriptionLabel);

        JLabel decoration = new JLabel("L");
        decoration.setFont(
                new Font("Georgia", Font.BOLD, 48)
        );
        decoration.setForeground(
                new Color(225, 211, 185)
        );

        JPanel cardContent = new JPanel(new BorderLayout());
        cardContent.setOpaque(false);
        cardContent.add(textPanel, BorderLayout.CENTER);
        cardContent.add(decoration, BorderLayout.EAST);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(14, 18, 14, 18)
                )
        );

        card.add(cardContent, BorderLayout.CENTER);

        return card;
    }

    // ==========================================
    // FIELD LABELS
    // ==========================================

    private JLabel createFieldLabel(String text) {

        JLabel label = new JLabel(text);
        label.setFont(
                new Font("Georgia", Font.BOLD, 11)
        );
        label.setForeground(BURGUNDY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    // ==========================================
    // COMBO BOX DESIGN
    // ==========================================

    private void configureComboBox(JComboBox<?> comboBox) {

        comboBox.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        comboBox.setBackground(WHITE);
        comboBox.setForeground(TEXT);

        comboBox.setPreferredSize(
                new Dimension(500, 42)
        );

        comboBox.setMinimumSize(
                new Dimension(200, 42)
        );

        comboBox.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42)
        );

        comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        comboBox.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );
    }

    // ==========================================
    // DATE FIELD DESIGN
    // ==========================================

    private void configureDateField(JTextField field) {

        field.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        field.setForeground(TEXT);
        field.setBackground(WHITE);

        field.setPreferredSize(
                new Dimension(500, 42)
        );

        field.setMinimumSize(
                new Dimension(200, 42)
        );

        field.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42)
        );

        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(9, 12, 9, 12)
                )
        );
    }

    // ==========================================
    // BUTTON DESIGN
    // ==========================================

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

        button.setFont(
                new Font("Georgia", Font.BOLD, 11)
        );

        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setOpaque(true);

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(GOLD, 1),
                        new EmptyBorder(9, 15, 9, 15)
                )
        );
    }

    // ==========================================
    // LOAD DATA
    // ==========================================

    private void loadData() {

        loadMembers();
        loadBooks();

        memberCountLabel.setText(
                String.valueOf(memberBox.getItemCount())
        );

        bookCountLabel.setText(
                String.valueOf(bookBox.getItemCount())
        );
    }

    // ==========================================
    // LOAD ACTIVE MEMBERS
    // ==========================================

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

    // ==========================================
    // LOAD AVAILABLE BOOKS
    // ==========================================

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

    // ==========================================
    // PROCESS BORROWING
    // ==========================================

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

            LocalDate borrowingDate = LocalDate.parse(
                    borrowingDateField.getText().trim()
            );

            LocalDate dueDate = LocalDate.parse(
                    dueDateField.getText().trim()
            );

            if (borrowingDate.isAfter(LocalDate.now())) {

                JOptionPane.showMessageDialog(
                        this,
                        "Borrowing date cannot be in the future.",
                        "Invalid Date",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            if (!dueDate.isAfter(borrowingDate)) {

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
                    "Please review the borrowing information:\n\n"
                            + "Member: " + member.name
                            + "\nStudent ID: " + member.studentId
                            + "\nBook: " + book.title
                            + "\nAuthor: " + book.author
                            + "\nAvailable Copies: " + book.availableCopies
                            + "\nBorrowing Date: " + borrowingDate
                            + "\nDue Date: " + dueDate
                            + "\n\nConfirm this transaction?",
                    "Confirm Borrowing",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
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
                        "Borrowing Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );

                loadBooks();

                dueDateField.setText(
                        borrowingDate.plusDays(7).toString()
                );
            }

        } catch (DateTimeParseException e) {

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

            e.printStackTrace();
        }
    }

    // ==========================================
    // DATABASE ERROR
    // ==========================================

    private void showError(SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Database error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );

        e.printStackTrace();
    }

    // ==========================================
    // MEMBER ITEM
    // ==========================================

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

    // ==========================================
    // BOOK ITEM
    // ==========================================

    private static class BookItem {

        private final int id;
        private final String title;
        private final String author;
        private final int availableCopies;

        BookItem(
                int id,
                String title,
                String author,
                int availableCopies
        ) {
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

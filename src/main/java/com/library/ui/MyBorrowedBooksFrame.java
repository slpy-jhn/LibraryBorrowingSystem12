
package com.library.ui;

import com.library.database.DatabaseConnection;
import com.library.service.ReturnRequestService;
import com.library.service.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class MyBorrowedBooksFrame extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;

    private final UserSession session;
    private final int memberId;

    private JLabel totalLabel;
    private JLabel overdueLabel;
    private JLabel dueSoonLabel;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    private static final Color BURGUNDY = new Color(105, 25, 43);
    private static final Color DARK_BURGUNDY = new Color(78, 17, 32);
    private static final Color GOLD = new Color(205, 166, 85);
    private static final Color CREAM = new Color(250, 247, 240);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(55, 48, 48);
    private static final Color MUTED = new Color(125, 116, 112);
    private static final Color BORDER = new Color(230, 222, 211);
    private static final Color LIGHT_GOLD = new Color(248, 241, 226);

    public MyBorrowedBooksFrame(UserSession session) {

        if (session == null) {
            throw new IllegalArgumentException(
                    "A valid user session is required."
            );
        }

        this.memberId = session.requireStudent();
        this.session = session;

        setTitle("LibraryLink - My Borrowed Books");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(950, 600));
        setSize(1250, 750);
        setLocationRelativeTo(null);

        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        loadBorrowedBooks();
    }

    // HEADER
    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(22, 30, 22, 30));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("My Borrowed Books");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 27));
        titleLabel.setForeground(WHITE);

        JLabel subtitleLabel = new JLabel(
                "Track your active loans, due dates, and return requests."
        );
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(239, 222, 211));

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitleLabel);

        JButton closeButton = createButton("Close", DARK_BURGUNDY, WHITE);
        closeButton.setPreferredSize(new Dimension(100, 38));
        closeButton.addActionListener(e -> dispose());

        header.add(titlePanel, BorderLayout.CENTER);
        header.add(closeButton, BorderLayout.EAST);

        return header;
    }

    // MAIN CONTENT
    private JPanel createMainContent() {

        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(CREAM);
        main.setBorder(new EmptyBorder(22, 28, 25, 28));

        main.add(createSummaryPanel(), BorderLayout.NORTH);
        main.add(createTablePanel(), BorderLayout.CENTER);
        main.add(createActionPanel(), BorderLayout.SOUTH);

        return main;
    }

    // SUMMARY CARDS
    private JPanel createSummaryPanel() {

        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));
        panel.setOpaque(false);

        JPanel totalCard = createSummaryCard(
                "ACTIVE LOANS", "0", "Books currently borrowed"
        );

        JPanel overdueCard = createSummaryCard(
                "OVERDUE BOOKS", "0", "Loans past their due date"
        );

        JPanel dueSoonCard = createSummaryCard(
                "DUE SOON", "0", "Due within the next 3 days"
        );

        totalLabel = (JLabel) totalCard.getClientProperty("valueLabel");
        overdueLabel = (JLabel) overdueCard.getClientProperty("valueLabel");
        dueSoonLabel = (JLabel) dueSoonCard.getClientProperty("valueLabel");

        panel.add(totalCard);
        panel.add(overdueCard);
        panel.add(dueSoonCard);

        return panel;
    }

    private JPanel createSummaryCard(
            String heading,
            String value,
            String description
    ) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        headingLabel.setForeground(MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 27));
        valueLabel.setForeground(BURGUNDY);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descriptionLabel.setForeground(MUTED);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        textPanel.add(headingLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(descriptionLabel);

        card.add(textPanel, BorderLayout.CENTER);
        card.putClientProperty("valueLabel", valueLabel);

        return card;
    }

    // TABLE PANEL
    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel sectionTitle = new JLabel("Your Current Loans");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sectionTitle.setForeground(BURGUNDY);

        JLabel sectionSubtitle = new JLabel(
                "Select a borrowed book to view details or request its return."
        );
        sectionSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sectionSubtitle.setForeground(MUTED);

        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));

        headingPanel.add(sectionTitle);
        headingPanel.add(Box.createVerticalStrut(4));
        headingPanel.add(sectionSubtitle);

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

        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setBackground(WHITE);
        table.setRowHeight(36);
        table.setSelectionBackground(LIGHT_GOLD);
        table.setSelectionForeground(BURGUNDY);
        table.setGridColor(new Color(239, 234, 226));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableHeader.setBackground(BURGUNDY);
        tableHeader.setForeground(WHITE);
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setReorderingAllowed(false);

        table.getColumnModel().getColumn(1).setPreferredWidth(210);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);
        table.getColumnModel().getColumn(6).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(WHITE);

        panel.add(headingPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ACTION PANEL
    private JPanel createActionPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel noteLabel = new JLabel(
                "Remember to return borrowed books by their due dates."
        );
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        noteLabel.setForeground(MUTED);

        JButton detailsButton = createButton(
                "View Details", LIGHT_GOLD, BURGUNDY
        );

        JButton requestReturnButton = createButton(
                "Request to Return", BURGUNDY, WHITE
        );

        JButton refreshButton = createButton(
                "Refresh", LIGHT_GOLD, BURGUNDY
        );

        detailsButton.addActionListener(e -> showBookDetails());

        requestReturnButton.addActionListener(e ->
                submitReturnRequest()
        );

        refreshButton.addActionListener(e ->
                loadBorrowedBooks()
        );

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        buttons.add(detailsButton);
        buttons.add(requestReturnButton);
        buttons.add(refreshButton);

        panel.add(noteLabel, BorderLayout.WEST);
        panel.add(buttons, BorderLayout.EAST);

        return panel;
    }

    // BUTTON STYLE
    private JButton createButton(
            String text,
            Color background,
            Color foreground
    ) {

        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        background.equals(LIGHT_GOLD) ? BORDER : background
                ),
                new EmptyBorder(9, 15, 9, 15)
        ));

        return button;
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
                        julianday(date(t.due_date))
                        - julianday(date('now', 'localtime'))
                        AS INTEGER
                    ) AS days_remaining

                FROM transactions t

                JOIN books b
                    ON t.book_id = b.book_id

                WHERE t.member_id = ?
                  AND t.return_date IS NULL
                  AND t.status IN ('BORROWED', 'OVERDUE')

                ORDER BY t.due_date ASC
                """;

        int totalLoans = 0;
        int overdueCount = 0;
        int dueSoonCount = 0;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, memberId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    totalLoans++;

                    int daysRemaining =
                            result.getInt("days_remaining");

                    String daysText;
                    String displayStatus;

                    if (daysRemaining < 0) {

                        daysText = Math.abs(daysRemaining)
                                + " day(s) overdue";

                        displayStatus = "OVERDUE";
                        overdueCount++;

                    } else if (daysRemaining == 0) {

                        daysText = "Due today";
                        displayStatus = "DUE TODAY";

                    } else if (daysRemaining <= 3) {

                        daysText = daysRemaining + " day(s) left";
                        displayStatus = "DUE SOON";
                        dueSoonCount++;

                    } else {

                        daysText = daysRemaining + " day(s) left";
                        displayStatus = "BORROWED";
                    }

                    tableModel.addRow(new Object[]{
                            result.getInt("transaction_id"),
                            result.getString("title"),
                            result.getString("author"),
                            formatDate(result.getString("borrowing_date")),
                            formatDate(result.getString("due_date")),
                            daysText,
                            displayStatus
                    });
                }
            }

            updateSummary(
                    totalLoans,
                    overdueCount,
                    dueSoonCount
            );

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

    // UPDATE SUMMARY CARDS
    private void updateSummary(
            int totalLoans,
            int overdueCount,
            int dueSoonCount
    ) {

        totalLabel.setText(String.valueOf(totalLoans));
        overdueLabel.setText(String.valueOf(overdueCount));
        dueSoonLabel.setText(String.valueOf(dueSoonCount));
    }

    // FORMAT DATE
    private String formatDate(String date) {

        if (date == null || date.isBlank()) {
            return "-";
        }

        try {
            return LocalDate.parse(date).format(dateFormatter);

        } catch (DateTimeParseException e) {
            return date;
        }
    }

    // VIEW SELECTED BOOK DETAILS
    private void showBookDetails() {

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

        int modelRow = table.convertRowIndexToModel(selectedRow);

        String transactionId = String.valueOf(
                tableModel.getValueAt(modelRow, 0)
        );

        String title = String.valueOf(
                tableModel.getValueAt(modelRow, 1)
        );

        String author = String.valueOf(
                tableModel.getValueAt(modelRow, 2)
        );

        String borrowingDate = String.valueOf(
                tableModel.getValueAt(modelRow, 3)
        );

        String dueDate = String.valueOf(
                tableModel.getValueAt(modelRow, 4)
        );

        String remaining = String.valueOf(
                tableModel.getValueAt(modelRow, 5)
        );

        String status = String.valueOf(
                tableModel.getValueAt(modelRow, 6)
        );

        JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 5, 8));
        detailsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        detailsPanel.setPreferredSize(new Dimension(350, 240));

        addDetail(detailsPanel, "Transaction ID", transactionId);
        addDetail(detailsPanel, "Book Title", title);
        addDetail(detailsPanel, "Author", author);
        addDetail(detailsPanel, "Borrowing Date", borrowingDate);
        addDetail(detailsPanel, "Due Date", dueDate);
        addDetail(detailsPanel, "Days Remaining", remaining);
        addDetail(detailsPanel, "Status", status);

        JOptionPane.showMessageDialog(
                this,
                detailsPanel,
                "Borrowed Book Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void addDetail(JPanel panel, String label, String value) {

        JLabel detail = new JLabel(label + ": " + value);
        detail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        detail.setForeground(TEXT);

        panel.add(detail);
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
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

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
                    e.getMessage() == null
                            ? "Unable to submit your return request."
                            : e.getMessage(),
                    "Unable to Submit Return Request",
                    JOptionPane.WARNING_MESSAGE
            );

            e.printStackTrace();
        }
    }
}

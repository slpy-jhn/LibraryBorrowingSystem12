
package com.library.ui;

import com.library.service.BorrowRequestService;
import com.library.service.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ManageBorrowRequestsFrame extends JFrame {

    private final UserSession session;

    private JTable requestTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;

    private JLabel totalPendingValue;
    private JLabel todayValue;
    private JLabel booksValue;
    private JLabel durationValue;

    private final Color BURGUNDY = new Color(91, 28, 43);
    private final Color DARK_BURGUNDY = new Color(65, 18, 31);
    private final Color GOLD = new Color(205, 164, 82);
    private final Color CREAM = new Color(250, 247, 240);
    private final Color WHITE = Color.WHITE;
    private final Color TEXT_DARK = new Color(55, 45, 43);
    private final Color TEXT_GRAY = new Color(125, 116, 110);
    private final Color GREEN = new Color(48, 125, 85);
    private final Color RED = new Color(175, 57, 57);

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    public ManageBorrowRequestsFrame(UserSession session) {

        if (session == null) {
            throw new SecurityException("Admin session is required.");
        }

        session.requireAdmin();
        this.session = session;

        setTitle("LibraryLink - Manage Borrowing Requests");
        setSize(1350, 780);
        setMinimumSize(new Dimension(1050, 650));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadRequests();
    }

    // ==========================================
    // HEADER
    // ==========================================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(24, 32, 24, 32));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("Manage Borrowing Requests");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setForeground(WHITE);

        JLabel subtitle = new JLabel(
                "Review, approve, and manage student borrowing requests."
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(235, 218, 207));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitle);

        JPanel rightPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 8)
        );
        rightPanel.setOpaque(false);

        JLabel sectionLabel = new JLabel("ADMINISTRATION");
        sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sectionLabel.setForeground(GOLD);

        rightPanel.add(sectionLabel);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    // ==========================================
    // MAIN PANEL
    // ==========================================

    private JPanel createMainPanel() {

        JPanel main = new JPanel(new BorderLayout(0, 20));
        main.setBackground(CREAM);
        main.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel topContent = new JPanel();
        topContent.setOpaque(false);
        topContent.setLayout(
                new BoxLayout(topContent, BoxLayout.Y_AXIS)
        );

        JLabel overviewTitle = new JLabel("Request Overview");
        overviewTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        overviewTitle.setForeground(BURGUNDY);
        overviewTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel statsPanel = new JPanel(
                new GridLayout(1, 4, 15, 15)
        );
        statsPanel.setOpaque(false);
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statsPanel.add(createStatCard(
                "Pending Requests", "0", "Awaiting admin decision", 1
        ));

        statsPanel.add(createStatCard(
                "Requests Today", "0", "Submitted today", 2
        ));

        statsPanel.add(createStatCard(
                "Books Requested", "0", "Total requested copies", 3
        ));

        statsPanel.add(createStatCard(
                "Average Duration", "0", "Requested days", 4
        ));

        topContent.add(overviewTitle);
        topContent.add(Box.createVerticalStrut(14));
        topContent.add(statsPanel);

        JPanel toolbar = createToolbar();

        JPanel tableCard = new JPanel(new BorderLayout(0, 12));
        tableCard.setBackground(WHITE);
        tableCard.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(232, 225, 216)
                        ),
                        new EmptyBorder(18, 18, 18, 18)
                )
        );

        JPanel tableHeading = new JPanel(new BorderLayout());
        tableHeading.setOpaque(false);

        JLabel tableTitle = new JLabel("Pending Request Directory");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        tableTitle.setForeground(BURGUNDY);

        JLabel tableSubtitle = new JLabel(
                "Select a request to view details or take action."
        );
        tableSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableSubtitle.setForeground(TEXT_GRAY);

        JPanel headingText = new JPanel();
        headingText.setOpaque(false);
        headingText.setLayout(
                new BoxLayout(headingText, BoxLayout.Y_AXIS)
        );

        headingText.add(tableTitle);
        headingText.add(Box.createVerticalStrut(4));
        headingText.add(tableSubtitle);

        tableHeading.add(headingText, BorderLayout.WEST);

        createRequestTable();

        JScrollPane scrollPane = new JScrollPane(requestTable);
        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(230, 225, 218)
                )
        );
        scrollPane.getViewport().setBackground(WHITE);

        JPanel actionPanel = createActionPanel();

        tableCard.add(tableHeading, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);
        tableCard.add(actionPanel, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 15));
        center.setOpaque(false);
        center.add(toolbar, BorderLayout.NORTH);
        center.add(tableCard, BorderLayout.CENTER);

        main.add(topContent, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);

        return main;
    }

    // ==========================================
    // STATISTIC CARDS
    // ==========================================

    private JPanel createStatCard(
            String title,
            String value,
            String description,
            int cardNumber
    ) {

        JPanel card = new JPanel();
        card.setBackground(WHITE);
        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(235, 229, 221)
                        ),
                        new EmptyBorder(17, 19, 17, 19)
                )
        );

        JPanel accent = new JPanel();
        accent.setBackground(GOLD);
        accent.setMaximumSize(new Dimension(42, 4));
        accent.setPreferredSize(new Dimension(42, 4));
        accent.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_GRAY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 29));
        valueLabel.setForeground(BURGUNDY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descriptionLabel.setForeground(TEXT_GRAY);
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        switch (cardNumber) {
            case 1 -> totalPendingValue = valueLabel;
            case 2 -> todayValue = valueLabel;
            case 3 -> booksValue = valueLabel;
            case 4 -> durationValue = valueLabel;
        }

        card.add(accent);
        card.add(Box.createVerticalStrut(11));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(7));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(descriptionLabel);

        return card;
    }

    // ==========================================
    // TOOLBAR
    // ==========================================

    private JPanel createToolbar() {

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setBackground(WHITE);
        searchPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 218, 209)
                        ),
                        new EmptyBorder(5, 10, 5, 10)
                )
        );

        JLabel searchIcon = new JLabel("SEARCH");
        searchIcon.setFont(new Font("Segoe UI", Font.BOLD, 10));
        searchIcon.setForeground(TEXT_GRAY);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createEmptyBorder(7, 5, 7, 5));
        searchField.setToolTipText(
                "Search request ID, student name, student ID, or book title"
        );

        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
                    @Override
                    public void insertUpdate(
                            javax.swing.event.DocumentEvent e
                    ) {
                        filterRequests();
                    }

                    @Override
                    public void removeUpdate(
                            javax.swing.event.DocumentEvent e
                    ) {
                        filterRequests();
                    }

                    @Override
                    public void changedUpdate(
                            javax.swing.event.DocumentEvent e
                    ) {
                        filterRequests();
                    }
                }
        );

        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.setPreferredSize(new Dimension(430, 42));

        JButton refreshButton = createButton(
                "REFRESH",
                BURGUNDY
        );

        refreshButton.addActionListener(e -> loadRequests());

        JPanel right = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 0)
        );
        right.setOpaque(false);
        right.add(refreshButton);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);

        return toolbar;
    }

    // ==========================================
    // REQUEST TABLE
    // ==========================================

    private void createRequestTable() {

        String[] columns = {
                "Request ID",
                "Student ID",
                "Student Name",
                "Book ID",
                "Book Title",
                "Request Date",
                "Preferred Return Date",
                "Duration (Days)"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestTable = new JTable(tableModel);

        requestTable.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        requestTable.setForeground(TEXT_DARK);
        requestTable.setBackground(WHITE);
        requestTable.setRowHeight(38);
        requestTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        requestTable.setShowVerticalLines(false);
        requestTable.setShowHorizontalLines(true);
        requestTable.setGridColor(new Color(238, 234, 228));
        requestTable.setFillsViewportHeight(true);
        requestTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        requestTable.setSelectionBackground(
                new Color(246, 235, 216)
        );
        requestTable.setSelectionForeground(BURGUNDY);

        JTableHeader header = requestTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(BURGUNDY);
        header.setForeground(WHITE);
        header.setPreferredSize(new Dimension(100, 42));
        header.setReorderingAllowed(false);

        int[] widths = {
                85, 90, 175, 80, 220, 155, 195, 115
        };

        for (int i = 0; i < widths.length; i++) {
            requestTable.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        sorter = new TableRowSorter<>(tableModel);
        requestTable.setRowSorter(sorter);

        requestTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showSelectedRequestDetails();
                }
            }
        });
    }

    // ==========================================
    // ACTION BUTTONS
    // ==========================================

    private JPanel createActionPanel() {

        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 12)
        );
        panel.setOpaque(false);

        JButton detailsButton = createButton(
                "VIEW DETAILS",
                new Color(105, 91, 75)
        );

        JButton rejectButton = createButton(
                "REJECT REQUEST",
                RED
        );

        JButton approveButton = createButton(
                "APPROVE REQUEST",
                GREEN
        );

        detailsButton.addActionListener(
                e -> showSelectedRequestDetails()
        );

        rejectButton.addActionListener(
                e -> rejectSelectedRequest()
        );

        approveButton.addActionListener(
                e -> approveSelectedRequest()
        );

        panel.add(detailsButton);
        panel.add(rejectButton);
        panel.add(approveButton);

        return panel;
    }

    // ==========================================
    // BUTTON DESIGN
    // ==========================================

    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);

        button.setFont(
                new Font("Segoe UI", Font.BOLD, 11)
        );
        button.setBackground(color);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );
        button.setBorder(
                new EmptyBorder(11, 17, 11, 17)
        );

        return button;
    }

    // ==========================================
    // LOAD PENDING REQUESTS
    // ==========================================

    private void loadRequests() {

        tableModel.setRowCount(0);

        try {

            session.requireAdmin();

            List<BorrowRequestService.BorrowRequest> requests =
                    BorrowRequestService.getPendingRequests(session);

            int requestsToday = 0;
            int totalDuration = 0;
            int totalBooks = 0;

            LocalDate today = LocalDate.now();

            for (BorrowRequestService.BorrowRequest request : requests) {

                String requestedDate = formatDate(
                        request.requestedReturnDate()
                );

                tableModel.addRow(new Object[]{
                        request.requestId(),
                        request.memberId(),
                        request.memberName(),
                        request.bookId(),
                        request.bookTitle(),
                        formatDateTime(request.requestDate()),
                        requestedDate,
                        request.borrowingDuration()
                });

                totalBooks++;
                totalDuration += request.borrowingDuration();

                if (isToday(request.requestDate(), today)) {
                    requestsToday++;
                }
            }

            totalPendingValue.setText(
                    String.valueOf(requests.size())
            );

            todayValue.setText(
                    String.valueOf(requestsToday)
            );

            booksValue.setText(
                    String.valueOf(totalBooks)
            );

            int average = requests.isEmpty()
                    ? 0
                    : Math.round(
                    (float) totalDuration / requests.size()
            );

            durationValue.setText(average + " days");

            if (requests.isEmpty()) {
                // Keep the empty table visible without interrupting the admin.
                requestTable.clearSelection();
            }

            filterRequests();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load borrowing requests.\n"
                            + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    // ==========================================
    // SEARCH FILTER
    // ==========================================

    private void filterRequests() {

        if (sorter == null || searchField == null) {
            return;
        }

        String search = searchField.getText().trim();

        if (search.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(
                    RowFilter.regexFilter(
                            "(?i)" + java.util.regex.Pattern.quote(search)
                    )
            );
        }
    }

    // ==========================================
    // DATE FORMATTING
    // ==========================================

    private String formatDate(String date) {

        if (date == null || date.isBlank()) {
            return "Not specified";
        }

        try {
            return LocalDate.parse(date.trim()).format(DATE_FORMAT);
        } catch (Exception e) {
            return date;
        }
    }

    private String formatDateTime(String dateTime) {

        if (dateTime == null || dateTime.isBlank()) {
            return "Not available";
        }

        String datePart = dateTime.trim();

        if (datePart.length() >= 10) {
            datePart = datePart.substring(0, 10);
        }

        try {
            return LocalDate.parse(datePart).format(DATE_FORMAT);
        } catch (Exception e) {
            return dateTime;
        }
    }

    private boolean isToday(String dateTime, LocalDate today) {

        if (dateTime == null || dateTime.isBlank()) {
            return false;
        }

        String datePart = dateTime.trim();

        if (datePart.length() >= 10) {
            datePart = datePart.substring(0, 10);
        }

        try {
            return LocalDate.parse(datePart).equals(today);
        } catch (Exception e) {
            return false;
        }
    }

    // ==========================================
    // SELECTED REQUEST
    // ==========================================

    private int getSelectedRequestId() {

        int selectedRow = requestTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a borrowing request first.",
                    "No Request Selected",
                    JOptionPane.WARNING_MESSAGE
            );

            return -1;
        }

        int modelRow =
                requestTable.convertRowIndexToModel(selectedRow);

        return (int) tableModel.getValueAt(modelRow, 0);
    }

    // ==========================================
    // VIEW REQUEST DETAILS
    // ==========================================

    private void showSelectedRequestDetails() {

        int requestId = getSelectedRequestId();

        if (requestId == -1) {
            return;
        }

        int selectedRow = requestTable.getSelectedRow();
        int modelRow =
                requestTable.convertRowIndexToModel(selectedRow);

        String studentId = String.valueOf(
                tableModel.getValueAt(modelRow, 1)
        );

        String studentName = String.valueOf(
                tableModel.getValueAt(modelRow, 2)
        );

        String bookId = String.valueOf(
                tableModel.getValueAt(modelRow, 3)
        );

        String bookTitle = String.valueOf(
                tableModel.getValueAt(modelRow, 4)
        );

        String requestDate = String.valueOf(
                tableModel.getValueAt(modelRow, 5)
        );

        String returnDate = String.valueOf(
                tableModel.getValueAt(modelRow, 6)
        );

        String duration = String.valueOf(
                tableModel.getValueAt(modelRow, 7)
        );

        JDialog dialog = new JDialog(
                this,
                "Borrowing Request Details",
                true
        );

        dialog.setSize(560, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(CREAM);

        JPanel heading = new JPanel();
        heading.setBackground(BURGUNDY);
        heading.setBorder(new EmptyBorder(22, 25, 22, 25));
        heading.setLayout(
                new BoxLayout(heading, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("BORROWING REQUEST DETAILS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(WHITE);

        JLabel subtitle = new JLabel(
                "Review the student's borrowing information."
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(235, 218, 207));

        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);

        JPanel details = new JPanel(
                new GridLayout(0, 2, 12, 14)
        );
        details.setBackground(WHITE);
        details.setBorder(new EmptyBorder(25, 28, 25, 28));

        addDetailRow(details, "Request ID", String.valueOf(requestId));
        addDetailRow(details, "Current Status", "PENDING");
        addDetailRow(details, "Student ID", studentId);
        addDetailRow(details, "Student Name", studentName);
        addDetailRow(details, "Book ID", bookId);
        addDetailRow(details, "Book Title", bookTitle);
        addDetailRow(details, "Request Date", requestDate);
        addDetailRow(details, "Preferred Return Date", returnDate);
        addDetailRow(details, "Duration", duration + " day(s)");

        JButton closeButton = createButton("CLOSE", BURGUNDY);
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel footer = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 15, 15)
        );
        footer.setBackground(CREAM);
        footer.add(closeButton);

        dialog.add(heading, BorderLayout.NORTH);
        dialog.add(details, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void addDetailRow(
            JPanel panel,
            String label,
            String value
    ) {

        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelText.setForeground(TEXT_GRAY);

        JLabel valueText = new JLabel(
                "<html><body style='width:190px'>"
                        + escapeHtml(value)
                        + "</body></html>"
        );
        valueText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        valueText.setForeground(TEXT_DARK);

        panel.add(labelText);
        panel.add(valueText);
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    // ==========================================
    // APPROVE REQUEST
    // ==========================================

    private void approveSelectedRequest() {

        int requestId = getSelectedRequestId();

        if (requestId == -1) {
            return;
        }

        int selectedRow = requestTable.getSelectedRow();
        int modelRow =
                requestTable.convertRowIndexToModel(selectedRow);

        String bookTitle = String.valueOf(
                tableModel.getValueAt(modelRow, 4)
        );

        String studentName = String.valueOf(
                tableModel.getValueAt(modelRow, 2)
        );

        String requestedReturnDate = String.valueOf(
                tableModel.getValueAt(modelRow, 6)
        );

        String duration = String.valueOf(
                tableModel.getValueAt(modelRow, 7)
        );

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Approve this borrowing request?\n\n"
                        + "Student: " + studentName
                        + "\nBook: " + bookTitle
                        + "\nPreferred Return Date: "
                        + requestedReturnDate
                        + "\nBorrowing Duration: "
                        + duration + " day(s)\n\n"
                        + "The preferred return date will be used "
                        + "as the transaction due date.",
                "Confirm Approval",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            session.requireAdmin();

            // Uses the exact date saved by the student.
            BorrowRequestService.approveRequest(
                    session,
                    requestId
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Borrowing request approved successfully!\n\n"
                            + "Student: " + studentName
                            + "\nBook: " + bookTitle
                            + "\nApproved Return Date: "
                            + requestedReturnDate,
                    "Request Approved",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadRequests();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to approve request.\n"
                            + e.getMessage(),
                    "Approval Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    // ==========================================
    // REJECT REQUEST
    // ==========================================

    private void rejectSelectedRequest() {

        int requestId = getSelectedRequestId();

        if (requestId == -1) {
            return;
        }

        int selectedRow = requestTable.getSelectedRow();
        int modelRow =
                requestTable.convertRowIndexToModel(selectedRow);

        String studentName = String.valueOf(
                tableModel.getValueAt(modelRow, 2)
        );

        String bookTitle = String.valueOf(
                tableModel.getValueAt(modelRow, 4)
        );

        JTextArea reasonArea = new JTextArea(5, 30);
        reasonArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(220, 210, 200)
                        ),
                        new EmptyBorder(8, 8, 8, 8)
                )
        );

        JPanel reasonPanel = new JPanel(new BorderLayout(0, 10));
        reasonPanel.setBackground(CREAM);

        JLabel prompt = new JLabel(
                "<html>Reject request from <b>"
                        + escapeHtml(studentName)
                        + "</b> for <b>"
                        + escapeHtml(bookTitle)
                        + "</b>.<br><br>"
                        + "Please provide a reason:</html>"
        );
        prompt.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        reasonPanel.add(prompt, BorderLayout.NORTH);
        reasonPanel.add(new JScrollPane(reasonArea), BorderLayout.CENTER);

        int choice = JOptionPane.showConfirmDialog(
                this,
                reasonPanel,
                "Reject Borrowing Request",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String reason = reasonArea.getText().trim();

        if (reason.isBlank()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please provide a rejection reason.",
                    "Reason Required",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            session.requireAdmin();

            BorrowRequestService.rejectRequest(
                    session,
                    requestId,
                    reason
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Borrowing request rejected successfully.",
                    "Request Rejected",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadRequests();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to reject request.\n"
                            + e.getMessage(),
                    "Rejection Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }
}

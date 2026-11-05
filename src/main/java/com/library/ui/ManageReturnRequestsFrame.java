
package com.library.ui;

import com.library.service.ReturnRequestService;
import com.library.service.UserSession;

import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ManageReturnRequestsFrame extends JFrame {

    private final UserSession session;

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JLabel pendingValue;
    private JLabel todayValue;
    private JLabel displayedValue;

    private JTextField searchField;

    private static final Color BURGUNDY = new Color(91, 28, 43);
    private static final Color DARK_BURGUNDY = new Color(65, 18, 31);
    private static final Color GOLD = new Color(205, 164, 82);
    private static final Color CREAM = new Color(250, 247, 240);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(55, 45, 45);
    private static final Color MUTED = new Color(125, 115, 110);
    private static final Color GREEN = new Color(43, 125, 82);
    private static final Color RED = new Color(174, 55, 55);

    public ManageReturnRequestsFrame(UserSession session) {

        if (session == null) {
            throw new IllegalArgumentException(
                    "A valid admin session is required."
            );
        }

        session.requireAdmin();
        this.session = session;

        setTitle("Manage Return Requests");
        setSize(1250, 750);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());

        buildHeader();
        buildMainContent();
        buildBottomPanel();

        loadPendingRequests();
    }

    private void buildHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(
                BorderFactory.createEmptyBorder(22, 28, 22, 28)
        );

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Manage Return Requests");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(WHITE);

        JLabel subtitle = new JLabel(
                "Review, approve, and manage student book return requests"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(235, 220, 215));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JButton closeButton = new JButton("CLOSE");
        styleButton(closeButton, WHITE, BURGUNDY);
        closeButton.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(GOLD, 1),
                        BorderFactory.createEmptyBorder(9, 18, 9, 18)
                )
        );
        closeButton.addActionListener(e -> dispose());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    private void buildMainContent() {

        JPanel mainPanel = new JPanel(new BorderLayout(0, 18));
        mainPanel.setBackground(CREAM);
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(20, 25, 10, 25)
        );

        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryPanel.setOpaque(false);

        pendingValue = createSummaryValue();
        todayValue = createSummaryValue();
        displayedValue = createSummaryValue();

        summaryPanel.add(createSummaryCard(
                "PENDING REQUESTS",
                pendingValue,
                "Awaiting admin review"
        ));

        summaryPanel.add(createSummaryCard(
                "REQUESTS TODAY",
                todayValue,
                "Submitted today"
        ));

        summaryPanel.add(createSummaryCard(
                "DISPLAYED REQUESTS",
                displayedValue,
                "Based on current search"
        ));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JLabel sectionTitle = new JLabel("Return Request Directory");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        sectionTitle.setForeground(BURGUNDY);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(270, 38));
        searchField.setToolTipText(
                "Search by student, book, request ID, or transaction ID"
        );

        searchField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(220, 210, 200)
                        ),
                        BorderFactory.createEmptyBorder(7, 10, 7, 10)
                )
        );

        JButton clearButton = createButton(
                "CLEAR",
                new Color(115, 105, 100),
                WHITE
        );

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(clearButton, BorderLayout.EAST);

        toolbar.add(sectionTitle, BorderLayout.WEST);
        toolbar.add(searchPanel, BorderLayout.EAST);

        String[] columns = {
                "Request ID",
                "Transaction ID",
                "Student ID",
                "Student Name",
                "Book ID",
                "Book Title",
                "Borrowing Date",
                "Due Date",
                "Request Date"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0
                        || columnIndex == 1
                        || columnIndex == 2
                        || columnIndex == 4) {
                    return Integer.class;
                }
                return String.class;
            }
        };

        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        styleTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(225, 215, 205)
                )
        );
        scrollPane.getViewport().setBackground(WHITE);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(WHITE);
        tablePanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 215, 205)
                        ),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)
                )
        );
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(toolbar, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        mainPanel.add(summaryPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
                    private void filter() {
                        String text = searchField.getText().trim();

                        if (text.isEmpty()) {
                            sorter.setRowFilter(null);
                        } else {
                            sorter.setRowFilter(
                                    RowFilter.regexFilter(
                                            "(?i)" + java.util.regex.Pattern.quote(text)
                                    )
                            );
                        }

                        updateDisplayedCount();
                    }

                    @Override
                    public void insertUpdate(
                            javax.swing.event.DocumentEvent e
                    ) {
                        filter();
                    }

                    @Override
                    public void removeUpdate(
                            javax.swing.event.DocumentEvent e
                    ) {
                        filter();
                    }

                    @Override
                    public void changedUpdate(
                            javax.swing.event.DocumentEvent e
                    ) {
                        filter();
                    }
                }
        );

        clearButton.addActionListener(e -> searchField.setText(""));
    }

    private void buildBottomPanel() {

        JPanel bottomPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 12)
        );

        bottomPanel.setBackground(CREAM);
        bottomPanel.setBorder(
                BorderFactory.createEmptyBorder(5, 25, 12, 25)
        );

        JButton detailsButton = createButton(
                "VIEW DETAILS",
                new Color(100, 90, 85),
                WHITE
        );

        JButton approveButton = createButton(
                "APPROVE RETURN",
                GREEN,
                WHITE
        );

        JButton rejectButton = createButton(
                "REJECT REQUEST",
                RED,
                WHITE
        );

        JButton refreshButton = createButton(
                "REFRESH",
                BURGUNDY,
                WHITE
        );

        detailsButton.addActionListener(e -> viewSelectedRequest());
        approveButton.addActionListener(e -> approveSelectedRequest());
        rejectButton.addActionListener(e -> rejectSelectedRequest());
        refreshButton.addActionListener(e -> loadPendingRequests());

        bottomPanel.add(detailsButton);
        bottomPanel.add(approveButton);
        bottomPanel.add(rejectButton);
        bottomPanel.add(refreshButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JLabel createSummaryValue() {

        JLabel label = new JLabel("0");
        label.setFont(new Font("Segoe UI", Font.BOLD, 27));
        label.setForeground(BURGUNDY);

        return label;
    }

    private JPanel createSummaryCard(
            String heading,
            JLabel valueLabel,
            String description
    ) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(WHITE);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(230, 220, 210)
                        ),
                        BorderFactory.createEmptyBorder(15, 17, 15, 17)
                )
        );

        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 11)
        );
        headingLabel.setForeground(MUTED);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 11)
        );
        descriptionLabel.setForeground(MUTED);

        card.add(headingLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(descriptionLabel);

        return card;
    }

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

        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setBorder(
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        );
    }

    private void styleTable() {

        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setBackground(WHITE);
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(240, 235, 230));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(245, 231, 207));
        table.setSelectionForeground(BURGUNDY);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        table.getTableHeader().setBackground(BURGUNDY);
        table.getTableHeader().setForeground(WHITE);
        table.getTableHeader().setPreferredSize(
                new Dimension(0, 40)
        );
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int column
                    ) {

                        Component component =
                                super.getTableCellRendererComponent(
                                        table,
                                        value,
                                        isSelected,
                                        hasFocus,
                                        row,
                                        column
                                );

                        if (isSelected) {
                            component.setBackground(
                                    new Color(245, 231, 207)
                            );
                            component.setForeground(BURGUNDY);
                        } else {
                            component.setBackground(
                                    row % 2 == 0
                                            ? WHITE
                                            : new Color(250, 248, 244)
                            );
                            component.setForeground(TEXT);
                        }

                        setBorder(
                                BorderFactory.createEmptyBorder(0, 8, 0, 8)
                        );

                        return component;
                    }
                };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(renderer);
        }

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(180);
        table.getColumnModel().getColumn(6).setPreferredWidth(115);
        table.getColumnModel().getColumn(7).setPreferredWidth(115);
        table.getColumnModel().getColumn(8).setPreferredWidth(115);
    }

    // LOAD ALL PENDING RETURN REQUESTS
    private void loadPendingRequests() {

        tableModel.setRowCount(0);

        try {

            session.requireAdmin();

            List<ReturnRequestService.ReturnRequest> requests =
                    ReturnRequestService.getPendingRequests(session);

            LocalDate today = LocalDate.now();
            int requestsToday = 0;

            for (ReturnRequestService.ReturnRequest request : requests) {

                tableModel.addRow(new Object[]{
                        request.returnRequestId(),
                        request.transactionId(),
                        request.memberId(),
                        request.memberName(),
                        request.bookId(),
                        request.bookTitle(),
                        request.borrowingDate(),
                        request.dueDate(),
                        request.requestDate()
                });

                if (isToday(request.requestDate(), today)) {
                    requestsToday++;
                }
            }

            pendingValue.setText(String.valueOf(requests.size()));
            todayValue.setText(String.valueOf(requestsToday));

            updateDisplayedCount();

        } catch (SQLException
                 | IllegalArgumentException
                 | SecurityException
                 | IllegalStateException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load return requests:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    private boolean isToday(String dateText, LocalDate today) {

        if (dateText == null || dateText.trim().isEmpty()) {
            return false;
        }

        try {
            LocalDate requestDate = LocalDate.parse(
                    dateText.substring(0, 10),
                    DateTimeFormatter.ISO_LOCAL_DATE
            );

            return requestDate.equals(today);

        } catch (DateTimeParseException | IndexOutOfBoundsException e) {
            return false;
        }
    }

    private void updateDisplayedCount() {

        if (displayedValue != null && table != null) {
            displayedValue.setText(
                    String.valueOf(table.getRowCount())
            );
        }
    }

    // GET SELECTED REQUEST ID
    private Integer getSelectedRequestId() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a return request first.",
                    "No Request Selected",
                    JOptionPane.WARNING_MESSAGE
            );

            return null;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);

        return Integer.parseInt(
                String.valueOf(
                        tableModel.getValueAt(modelRow, 0)
                )
        );
    }

    // VIEW SELECTED REQUEST DETAILS
    private void viewSelectedRequest() {

        Integer requestId = getSelectedRequestId();

        if (requestId == null) {
            return;
        }

        int modelRow = table.convertRowIndexToModel(
                table.getSelectedRow()
        );

        String[] labels = {
                "Request ID",
                "Transaction ID",
                "Student ID",
                "Student Name",
                "Book ID",
                "Book Title",
                "Borrowing Date",
                "Due Date",
                "Request Date"
        };

        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        detailsPanel.setBackground(WHITE);
        detailsPanel.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        for (int i = 0; i < labels.length; i++) {

            JLabel nameLabel = new JLabel(labels[i] + ":");
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nameLabel.setForeground(BURGUNDY);

            JLabel valueLabel = new JLabel(
                    String.valueOf(tableModel.getValueAt(modelRow, i))
            );
            valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            valueLabel.setForeground(TEXT);

            detailsPanel.add(nameLabel);
            detailsPanel.add(valueLabel);
        }

        JOptionPane.showMessageDialog(
                this,
                detailsPanel,
                "Return Request Details - #" + requestId,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // APPROVE RETURN REQUEST
    private void approveSelectedRequest() {

        Integer requestId = getSelectedRequestId();

        if (requestId == null) {
            return;
        }

        int modelRow = table.convertRowIndexToModel(
                table.getSelectedRow()
        );

        String bookTitle = String.valueOf(
                tableModel.getValueAt(modelRow, 5)
        );

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Have you physically inspected the returned book?\n\n"
                        + "Book: " + bookTitle
                        + "\n\nApproving this request will:\n"
                        + "- Mark the transaction as RETURNED.\n"
                        + "- Increase the book's available copies.\n"
                        + "- Complete the return request.",
                "Confirm Return Approval",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            session.requireAdmin();

            ReturnRequestService.approveReturnRequest(
                    session,
                    requestId
            );

            JOptionPane.showMessageDialog(
                    this,
                    "The return request has been approved!\n"
                            + "The book has been marked as returned.",
                    "Return Approved",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadPendingRequests();

        } catch (SQLException
                 | IllegalArgumentException
                 | SecurityException
                 | IllegalStateException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Unable to Approve Return",
                    JOptionPane.WARNING_MESSAGE
            );

            e.printStackTrace();
        }
    }

    // REJECT RETURN REQUEST
    private void rejectSelectedRequest() {

        Integer requestId = getSelectedRequestId();

        if (requestId == null) {
            return;
        }

        String reason = JOptionPane.showInputDialog(
                this,
                "Enter the reason for rejecting this return request:",
                "Reject Return Request",
                JOptionPane.QUESTION_MESSAGE
        );

        if (reason == null) {
            return;
        }

        reason = reason.trim();

        if (reason.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please provide a rejection reason.",
                    "Reason Required",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Reject this return request?\n\n"
                        + "Reason: " + reason,
                "Confirm Rejection",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            session.requireAdmin();

            ReturnRequestService.rejectReturnRequest(
                    session,
                    requestId,
                    reason
            );

            JOptionPane.showMessageDialog(
                    this,
                    "The return request has been rejected.",
                    "Request Rejected",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadPendingRequests();

        } catch (SQLException
                 | IllegalArgumentException
                 | SecurityException
                 | IllegalStateException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Unable to Reject Request",
                    JOptionPane.WARNING_MESSAGE
            );

            e.printStackTrace();
        }
    }
}

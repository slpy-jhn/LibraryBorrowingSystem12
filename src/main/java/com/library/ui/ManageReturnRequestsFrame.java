
package com.library.ui;

import com.library.service.ReturnRequestService;
import com.library.service.UserSession;

import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
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
    private static final Color RED = new Color(175, 55, 55);

    public ManageReturnRequestsFrame(UserSession session) {

        if (session == null) {
            throw new IllegalArgumentException(
                    "A valid admin session is required."
            );
        }

        session.requireAdmin();
        this.session = session;

        setTitle("LibraryLink - Manage Return Requests");
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

    // ==========================================
    // HEADER
    // ==========================================

    private void buildHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Manage Return Requests");
        title.setFont(new Font("Georgia", Font.BOLD, 27));
        title.setForeground(GOLD);

        JLabel subtitle = new JLabel(
                "Review, approve, and manage student book return requests."
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

        JPanel mainPanel = new JPanel(new BorderLayout(0, 18));
        mainPanel.setBackground(CREAM);
        mainPanel.setBorder(new EmptyBorder(20, 25, 10, 25));

        // SUMMARY CARDS

        JPanel summaryPanel = new JPanel(
                new GridLayout(1, 3, 15, 0)
        );
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

        // CENTER CONTENT

        JPanel centerPanel = new JPanel(
                new BorderLayout(0, 12)
        );
        centerPanel.setOpaque(false);

        // TOOLBAR

        JPanel toolbar = new JPanel(
                new BorderLayout(10, 0)
        );
        toolbar.setOpaque(false);

        JLabel sectionTitle = new JLabel(
                "Return Request Directory"
        );
        sectionTitle.setFont(
                new Font("Georgia", Font.BOLD, 17)
        );
        sectionTitle.setForeground(BURGUNDY);

        JPanel searchPanel = new JPanel(
                new BorderLayout(8, 0)
        );
        searchPanel.setBackground(WHITE);
        searchPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(4, 10, 4, 10)
                )
        );

        JLabel searchLabel = new JLabel("SEARCH");
        searchLabel.setFont(
                new Font("Georgia", Font.BOLD, 10)
        );
        searchLabel.setForeground(MUTED);

        searchField = new JTextField();
        searchField.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );
        searchField.setForeground(TEXT);
        searchField.setBackground(WHITE);
        searchField.setPreferredSize(
                new Dimension(250, 36)
        );
        searchField.setBorder(
                BorderFactory.createEmptyBorder(7, 5, 7, 5)
        );
        searchField.setToolTipText(
                "Search by student, book, request ID, or transaction ID"
        );

        JButton clearButton = createButton(
                "CLEAR",
                CREAM,
                BURGUNDY
        );

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(clearButton, BorderLayout.EAST);

        toolbar.add(sectionTitle, BorderLayout.WEST);
        toolbar.add(searchPanel, BorderLayout.EAST);

        // TABLE

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
                BorderFactory.createLineBorder(BORDER)
        );
        scrollPane.getViewport().setBackground(WHITE);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(WHITE);
        tablePanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(8, 8, 8, 8)
                )
        );

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(toolbar, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        mainPanel.add(summaryPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // SEARCH FILTER

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

        clearButton.addActionListener(
                e -> searchField.setText("")
        );
    }

    // ==========================================
    // BOTTOM ACTION PANEL
    // ==========================================

    private void buildBottomPanel() {

        JPanel bottomPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 12)
        );

        bottomPanel.setBackground(CREAM);
        bottomPanel.setBorder(
                new EmptyBorder(5, 25, 12, 25)
        );

        JButton detailsButton = createButton(
                "VIEW DETAILS",
                BURGUNDY,
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

        detailsButton.addActionListener(
                e -> viewSelectedRequest()
        );

        approveButton.addActionListener(
                e -> approveSelectedRequest()
        );

        rejectButton.addActionListener(
                e -> rejectSelectedRequest()
        );

        refreshButton.addActionListener(
                e -> loadPendingRequests()
        );

        bottomPanel.add(detailsButton);
        bottomPanel.add(approveButton);
        bottomPanel.add(rejectButton);
        bottomPanel.add(refreshButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ==========================================
    // SUMMARY CARDS
    // ==========================================

    private JLabel createSummaryValue() {

        JLabel label = new JLabel("0");
        label.setFont(new Font("Georgia", Font.BOLD, 27));
        label.setForeground(BURGUNDY);

        return label;
    }

    private JPanel createSummaryCard(
            String heading,
            JLabel valueLabel,
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
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(valueLabel);
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
                        new EmptyBorder(15, 17, 15, 17)
                )
        );

        card.add(cardContent, BorderLayout.CENTER);

        return card;
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

        button.setFont(new Font("Georgia", Font.BOLD, 11));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.setOpaque(true);

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(GOLD, 1),
                        new EmptyBorder(9, 15, 9, 15)
                )
        );
    }

    // ==========================================
    // TABLE DESIGN
    // ==========================================

    private void styleTable() {

        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setBackground(WHITE);
        table.setRowHeight(38);

        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER);

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        table.setSelectionBackground(LIGHT_GOLD);
        table.setSelectionForeground(BURGUNDY);

        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(false);

        // TABLE HEADER
        // These methods must be called on JTableHeader.

        JTableHeader header = table.getTableHeader();

        header.setFont(new Font("Georgia", Font.BOLD, 11));
        header.setBackground(BURGUNDY);
        header.setForeground(WHITE);
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);

        // TABLE CELL RENDERER

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
                            component.setBackground(LIGHT_GOLD);
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
                                BorderFactory.createEmptyBorder(
                                        0, 8, 0, 8
                                )
                        );

                        return component;
                    }
                };

        for (int i = 0; i < table.getColumnCount(); i++) {

            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(renderer);
        }

        // COLUMN WIDTHS

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

    // ==========================================
    // LOAD ALL PENDING RETURN REQUESTS
    // ==========================================

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

    // ==========================================
    // CHECK TODAY'S REQUESTS
    // ==========================================

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

        } catch (DateTimeParseException
                 | IndexOutOfBoundsException e) {

            return false;
        }
    }

    // ==========================================
    // UPDATE DISPLAYED COUNT
    // ==========================================

    private void updateDisplayedCount() {

        if (displayedValue != null && table != null) {

            displayedValue.setText(
                    String.valueOf(table.getRowCount())
            );
        }
    }

    // ==========================================
    // GET SELECTED REQUEST ID
    // ==========================================

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

    // ==========================================
    // VIEW SELECTED REQUEST DETAILS
    // ==========================================

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

        JPanel detailsPanel = new JPanel(
                new GridLayout(0, 2, 12, 14)
        );

        detailsPanel.setBackground(WHITE);
        detailsPanel.setBorder(
                new EmptyBorder(22, 25, 22, 25)
        );

        for (int i = 0; i < labels.length; i++) {

            JLabel nameLabel = new JLabel(labels[i] + ":");
            nameLabel.setFont(
                    new Font("Georgia", Font.BOLD, 12)
            );
            nameLabel.setForeground(BURGUNDY);

            JLabel valueLabel = new JLabel(
                    String.valueOf(
                            tableModel.getValueAt(modelRow, i)
                    )
            );

            valueLabel.setFont(
                    new Font("Segoe UI", Font.PLAIN, 13)
            );
            valueLabel.setForeground(TEXT);

            detailsPanel.add(nameLabel);
            detailsPanel.add(valueLabel);
        }

        JPanel heading = new JPanel();
        heading.setLayout(
                new BoxLayout(heading, BoxLayout.Y_AXIS)
        );
        heading.setBackground(BURGUNDY);
        heading.setBorder(
                new EmptyBorder(18, 22, 18, 22)
        );

        JLabel title = new JLabel("RETURN REQUEST DETAILS");
        title.setFont(
                new Font("Georgia", Font.BOLD, 18)
        );
        title.setForeground(GOLD);

        JLabel subtitle = new JLabel(
                "Review the student's return request information."
        );
        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        subtitle.setForeground(CREAM);

        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);

        JDialog dialog = new JDialog(
                this,
                "Return Request Details - #" + requestId,
                true
        );

        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(CREAM);

        JButton closeButton = createButton(
                "CLOSE",
                BURGUNDY,
                WHITE
        );

        closeButton.addActionListener(
                e -> dialog.dispose()
        );

        JPanel footer = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 15, 12)
        );
        footer.setBackground(CREAM);
        footer.add(closeButton);

        dialog.add(heading, BorderLayout.NORTH);
        dialog.add(detailsPanel, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setSize(580, 540);
        dialog.setMinimumSize(new Dimension(520, 480));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ==========================================
    // APPROVE RETURN REQUEST
    // ==========================================

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

    // ==========================================
    // REJECT RETURN REQUEST
    // ==========================================

    private void rejectSelectedRequest() {

        Integer requestId = getSelectedRequestId();

        if (requestId == null) {
            return;
        }

        JTextArea reasonArea = new JTextArea(5, 30);
        reasonArea.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );
        reasonArea.setForeground(TEXT);
        reasonArea.setBackground(WHITE);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(8, 8, 8, 8)
                )
        );

        JPanel reasonPanel = new JPanel(
                new BorderLayout(0, 10)
        );
        reasonPanel.setBackground(CREAM);
        reasonPanel.setBorder(
                new EmptyBorder(5, 5, 5, 5)
        );

        JLabel prompt = new JLabel(
                "Enter the reason for rejecting this return request:"
        );
        prompt.setFont(
                new Font("Georgia", Font.BOLD, 12)
        );
        prompt.setForeground(BURGUNDY);

        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        reasonPanel.add(prompt, BorderLayout.NORTH);
        reasonPanel.add(reasonScroll, BorderLayout.CENTER);

        int choice = JOptionPane.showConfirmDialog(
                this,
                reasonPanel,
                "Reject Return Request",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String reason = reasonArea.getText().trim();

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

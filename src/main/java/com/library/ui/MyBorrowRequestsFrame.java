
package com.library.ui;

import com.library.service.BorrowRequestService;
import com.library.service.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MyBorrowRequestsFrame extends JFrame {

    private static final Color BURGUNDY = new Color(75, 20, 36);
    private static final Color DARK_BURGUNDY = new Color(55, 13, 27);
    private static final Color GOLD = new Color(176, 141, 87);
    private static final Color CREAM = new Color(243, 235, 221);
    private static final Color WHITE = new Color(255, 252, 245);
    private static final Color TEXT = new Color(53, 37, 31);
    private static final Color MUTED = new Color(130, 115, 103);
    private static final Color GREEN = new Color(38, 125, 82);
    private static final Color RED = new Color(175, 55, 55);
    private static final Color ORANGE = new Color(190, 125, 35);
    private static final Color BORDER = new Color(222, 210, 191);
    private static final Color LIGHT_GOLD = new Color(244, 232, 207);

    private final UserSession session;
    private final DefaultTableModel tableModel;
    private final JTable requestTable;

    private JLabel totalValue;
    private JLabel pendingValue;
    private JLabel approvedValue;
    private JLabel rejectedValue;
    private JLabel resultLabel;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    public MyBorrowRequestsFrame(UserSession session) {
        if (session == null) {
            throw new SecurityException("Please log in first.");
        }

        session.requireStudent();
        this.session = session;

        setTitle("My Borrowing Requests - LibraryLink");
        setSize(1400, 760);
        setMinimumSize(new Dimension(1050, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CREAM);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(22, 30, 22, 30));

        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("My Borrowing Requests");
        title.setFont(new Font("Georgia", Font.BOLD, 27));
        title.setForeground(GOLD);

        JLabel subtitle = new JLabel(
                "Track your requests, approval status, and preferred return dates."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(240, 225, 220));

        headingPanel.add(title);
        headingPanel.add(Box.createVerticalStrut(6));
        headingPanel.add(subtitle);

        header.add(headingPanel, BorderLayout.WEST);

        JLabel logo = new JLabel("LIBRARYLINK");
        logo.setFont(new Font("Georgia", Font.BOLD, 18));
        logo.setForeground(GOLD);
        header.add(logo, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Main content
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(CREAM);
        content.setBorder(new EmptyBorder(22, 25, 22, 25));

        // Summary cards
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        summaryPanel.setOpaque(false);

        totalValue = new JLabel("0");
        pendingValue = new JLabel("0");
        approvedValue = new JLabel("0");
        rejectedValue = new JLabel("0");

        summaryPanel.add(createSummaryCard(
                "TOTAL REQUESTS", totalValue, BURGUNDY
        ));
        summaryPanel.add(createSummaryCard(
                "PENDING", pendingValue, ORANGE
        ));
        summaryPanel.add(createSummaryCard(
                "APPROVED", approvedValue, GREEN
        ));
        summaryPanel.add(createSummaryCard(
                "REJECTED", rejectedValue, RED
        ));

        // Table
        String[] columns = {
                "Request ID",
                "Book ID",
                "Book Title",
                "Request Date",
                "Preferred Return Date",
                "Duration (Days)",
                "Status",
                "Decision Date",
                "Rejection Reason"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestTable = new JTable(tableModel);
        requestTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        requestTable.setForeground(TEXT);
        requestTable.setBackground(WHITE);
        requestTable.setSelectionBackground(LIGHT_GOLD);
        requestTable.setSelectionForeground(TEXT);
        requestTable.setSelectionForeground(TEXT);
        requestTable.setRowHeight(38);
        requestTable.setShowGrid(true);
        requestTable.setGridColor(new Color(235, 228, 218));
        requestTable.setIntercellSpacing(new Dimension(1, 1));
        requestTable.setAutoCreateRowSorter(true);
        requestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader tableHeader = requestTable.getTableHeader();
        tableHeader.setFont(new Font("Georgia", Font.BOLD, 11));
        tableHeader.setBackground(BURGUNDY);
        tableHeader.setForeground(WHITE);
        tableHeader.setPreferredSize(new Dimension(0, 43));
        tableHeader.setReorderingAllowed(false);

        // Keep columns wide enough to display their contents.
        requestTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {90, 80, 230, 145, 175, 110, 115, 145, 330};

        for (int i = 0; i < widths.length; i++) {
            requestTable.getColumnModel()
                    .getColumn(i).setPreferredWidth(widths[i]);
        }

        // General cell styling
        DefaultTableCellRenderer centerRenderer =
                new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i : new int[]{0, 1, 3, 4, 5, 6, 7}) {
            requestTable.getColumnModel()
                    .getColumn(i).setCellRenderer(centerRenderer);
        }

        // Status colors
        requestTable.getColumnModel().getColumn(6)
                .setCellRenderer(new StatusRenderer());

        // Rejection reason: wrap long text and increase row height.
        requestTable.getColumnModel().getColumn(8)
                .setCellRenderer(new WrappingRenderer());

        JScrollPane tableScroll = new JScrollPane(requestTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(
                new Color(225, 215, 205)
        ));
        tableScroll.getViewport().setBackground(WHITE);
        tableScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        tableScroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        JPanel tableCard = new JPanel(new BorderLayout(0, 12));
        tableCard.setBackground(WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(230, 222, 212)
                ),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JPanel tableHeading = new JPanel(new BorderLayout());
        tableHeading.setOpaque(false);

        JLabel tableTitle = new JLabel("Borrowing Request History");
        tableTitle.setFont(new Font("Georgia", Font.BOLD, 17));
        tableTitle.setForeground(BURGUNDY);

        resultLabel = new JLabel("Your submitted requests");
        resultLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        resultLabel.setForeground(MUTED);

        tableHeading.add(tableTitle, BorderLayout.WEST);
        tableHeading.add(resultLabel, BorderLayout.EAST);

        tableCard.add(tableHeading, BorderLayout.NORTH);
        tableCard.add(tableScroll, BorderLayout.CENTER);

        // Buttons
        JButton detailsButton = createButton("VIEW DETAILS", BURGUNDY);
        JButton refreshButton = createButton("REFRESH", DARK_BURGUNDY);

        detailsButton.addActionListener(e -> showRequestDetails());
        refreshButton.addActionListener(e -> loadRequests());

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        buttonPanel.setOpaque(false);
        buttonPanel.add(detailsButton);
        buttonPanel.add(refreshButton);

        JPanel lowerPanel = new JPanel(new BorderLayout());
        lowerPanel.setOpaque(false);
        lowerPanel.add(buttonPanel, BorderLayout.EAST);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 18));
        centerPanel.setOpaque(false);
        centerPanel.add(summaryPanel, BorderLayout.NORTH);
        centerPanel.add(tableCard, BorderLayout.CENTER);
        centerPanel.add(lowerPanel, BorderLayout.SOUTH);

        content.add(centerPanel, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        loadRequests();
    }

    private JPanel createSummaryCard(
            String heading,
            JLabel valueLabel,
            Color accent
    ) {
        JPanel card = new JPanel(new BorderLayout(0, 9));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        0, 4, 0, 0, accent
                ),
                new EmptyBorder(15, 17, 15, 17)
        ));

        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(new Font("Georgia", Font.BOLD, 11));
        headingLabel.setForeground(MUTED);

        valueLabel.setFont(new Font("Georgia", Font.BOLD, 27));
        valueLabel.setForeground(accent);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        textPanel.add(headingLabel);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(valueLabel);

        JLabel decoration = new JLabel("L");
        decoration.setFont(new Font("Georgia", Font.BOLD, 48));
        decoration.setForeground(new Color(225, 211, 185));

        card.add(textPanel, BorderLayout.CENTER);
        card.add(decoration, BorderLayout.EAST);

        return card;
    }

    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font("Georgia", Font.BOLD, 11));
        button.setForeground(WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD, 1),
                new EmptyBorder(10, 18, 10, 18)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void loadRequests() {
        tableModel.setRowCount(0);

        int pending = 0;
        int approved = 0;
        int rejected = 0;

        try {
            List<BorrowRequestService.BorrowRequest> requests =
                    BorrowRequestService.getMyRequests(session);

            for (BorrowRequestService.BorrowRequest request : requests) {

                String requestedReturnDate =
                        formatDate(request.requestedReturnDate());

                String decisionDate =
                        request.decisionDate() == null
                                || request.decisionDate().isBlank()
                                ? "-"
                                : formatDate(request.decisionDate());

                String rejectionReason =
                        request.rejectionReason() == null
                                || request.rejectionReason().isBlank()
                                ? "-"
                                : request.rejectionReason();

                String status = request.status() == null
                        ? "UNKNOWN"
                        : request.status().trim().toUpperCase();

                if (status.contains("PENDING")) {
                    pending++;
                } else if (status.contains("APPROVED")) {
                    approved++;
                } else if (status.contains("REJECTED")) {
                    rejected++;
                }

                tableModel.addRow(new Object[]{
                        request.requestId(),
                        request.bookId(),
                        request.bookTitle(),
                        formatDate(request.requestDate()),
                        requestedReturnDate,
                        request.borrowingDuration(),
                        status,
                        decisionDate,
                        rejectionReason
                });
            }

            totalValue.setText(String.valueOf(requests.size()));
            pendingValue.setText(String.valueOf(pending));
            approvedValue.setText(String.valueOf(approved));
            rejectedValue.setText(String.valueOf(rejected));

            resultLabel.setText(
                    requests.size() + " request(s) found"
            );

            updateRejectionRowHeights();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load your borrowing requests:\n"
                            + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

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

    private void updateRejectionRowHeights() {
        int reasonColumnWidth = requestTable.getColumnModel()
                .getColumn(8).getWidth();

        FontMetrics metrics = requestTable.getFontMetrics(
                requestTable.getFont()
        );

        for (int row = 0; row < requestTable.getRowCount(); row++) {
            Object value = requestTable.getValueAt(row, 8);
            String reason = value == null ? "" : value.toString();

            int availableWidth = Math.max(80, reasonColumnWidth - 16);
            int lines = 1;
            int currentLineWidth = 0;

            for (String word : reason.split("\\s+")) {
                int wordWidth = metrics.stringWidth(word + " ");

                if (currentLineWidth + wordWidth > availableWidth) {
                    lines++;
                    currentLineWidth = wordWidth;
                } else {
                    currentLineWidth += wordWidth;
                }
            }

            int rowHeight = Math.max(38, Math.min(120, lines * 18 + 16));
            requestTable.setRowHeight(row, rowHeight);
        }
    }

    private void showRequestDetails() {
        int selectedRow = requestTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a borrowing request first.",
                    "No Request Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow = requestTable.convertRowIndexToModel(selectedRow);

        String[] labels = {
                "Request ID",
                "Book ID",
                "Book Title",
                "Request Date",
                "Preferred Return Date",
                "Borrowing Duration",
                "Status",
                "Decision Date",
                "Rejection Reason"
        };

        int[] columns = {0, 1, 2, 3, 4, 5, 6, 7, 8};

        JPanel detailsPanel = new JPanel(new BorderLayout(0, 12));
        detailsPanel.setBackground(WHITE);
        detailsPanel.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel informationPanel = new JPanel(new GridBagLayout());
        informationPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 5, 7, 12);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < labels.length; i++) {
            Object value = tableModel.getValueAt(modelRow, columns[i]);
            String displayValue = value == null ? "-" : value.toString();

            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;

            JLabel label = new JLabel(labels[i] + ":");
            label.setFont(new Font("SansSerif", Font.BOLD, 13));
            label.setForeground(BURGUNDY);
            informationPanel.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;

            if (i == 8) {
                JTextArea reasonArea = new JTextArea(displayValue);
                reasonArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
                reasonArea.setForeground(TEXT);
                reasonArea.setBackground(CREAM);
                reasonArea.setLineWrap(true);
                reasonArea.setWrapStyleWord(true);
                reasonArea.setEditable(false);
                reasonArea.setRows(5);
                reasonArea.setBorder(new EmptyBorder(9, 9, 9, 9));

                JScrollPane reasonScroll = new JScrollPane(reasonArea);
                reasonScroll.setPreferredSize(new Dimension(360, 115));
                informationPanel.add(reasonScroll, gbc);
            } else {
                JLabel valueLabel = new JLabel(displayValue);
                valueLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
                valueLabel.setForeground(TEXT);
                informationPanel.add(valueLabel, gbc);
            }
        }

        detailsPanel.add(informationPanel, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(
                this,
                detailsPanel,
                "Borrowing Request Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private static class StatusRenderer
            extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
            );

            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 12));

            if (!isSelected) {
                String status = value == null
                        ? ""
                        : value.toString().toUpperCase();

                if (status.contains("APPROVED")) {
                    label.setForeground(GREEN);
                } else if (status.contains("REJECTED")) {
                    label.setForeground(RED);
                } else if (status.contains("PENDING")) {
                    label.setForeground(ORANGE);
                } else {
                    label.setForeground(MUTED);
                }
            }

            return label;
        }
    }

    private static class WrappingRenderer
            extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JTextArea area = new JTextArea(
                    value == null ? "" : value.toString()
            );

            area.setFont(new Font("SansSerif", Font.PLAIN, 12));
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setEditable(false);
            area.setBorder(new EmptyBorder(5, 7, 5, 7));

            if (isSelected) {
                area.setBackground(table.getSelectionBackground());
                area.setForeground(table.getSelectionForeground());
            } else {
                area.setBackground(table.getBackground());
                area.setForeground(TEXT);
            }

            return area;
        }
    }
}

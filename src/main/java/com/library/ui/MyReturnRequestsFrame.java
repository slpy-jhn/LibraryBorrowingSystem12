
package com.library.ui;

import com.library.service.ReturnRequestService;
import com.library.service.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MyReturnRequestsFrame extends JFrame {

    private static final Color BURGUNDY = new Color(91, 28, 43);
    private static final Color DARK_BURGUNDY = new Color(65, 18, 31);
    private static final Color GOLD = new Color(205, 164, 82);
    private static final Color CREAM = new Color(250, 247, 240);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(55, 45, 45);
    private static final Color MUTED = new Color(125, 115, 110);
    private static final Color BORDER = new Color(229, 222, 211);
    private static final Color GREEN = new Color(38, 125, 82);
    private static final Color RED = new Color(175, 55, 55);
    private static final Color ORANGE = new Color(190, 125, 35);

    private final UserSession session;

    private JTable requestTable;
    private DefaultTableModel tableModel;

    private JLabel totalLabel;
    private JLabel pendingLabel;
    private JLabel approvedLabel;
    private JLabel rejectedLabel;
    private JLabel resultLabel;

    public MyReturnRequestsFrame(UserSession session) {

        if (session == null) {
            throw new SecurityException("Please log in first.");
        }

        session.requireStudent();
        this.session = session;

        setTitle("LibraryLink - My Return Requests");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 650));
        setSize(1400, 760);
        setLocationRelativeTo(null);

        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());

        createHeader();
        createMainContent();
        createFooter();

        loadRequests();
    }

    private void createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(23, 30, 23, 30));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("My Return Requests");
        title.setFont(new Font("Serif", Font.BOLD, 27));
        title.setForeground(WHITE);

        JLabel subtitle = new JLabel(
                "Track your submitted return requests and their approval status."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(240, 225, 220));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitle);

        JLabel logo = new JLabel("LIBRARYLINK");
        logo.setFont(new Font("Serif", Font.BOLD, 18));
        logo.setForeground(GOLD);

        JButton closeButton = createButton("CLOSE", DARK_BURGUNDY, WHITE);
        closeButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD, 1),
                new EmptyBorder(9, 18, 9, 18)
        ));
        closeButton.addActionListener(e -> dispose());

        JPanel rightPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 0)
        );
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(logo);
        rightPanel.add(Box.createVerticalStrut(12));
        rightPanel.add(closeButton);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    private void createMainContent() {

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(CREAM);
        content.setBorder(new EmptyBorder(22, 25, 20, 25));

        // SUMMARY CARDS
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        summaryPanel.setOpaque(false);

        totalLabel = new JLabel("0");
        pendingLabel = new JLabel("0");
        approvedLabel = new JLabel("0");
        rejectedLabel = new JLabel("0");

        summaryPanel.add(createSummaryCard(
                "TOTAL REQUESTS", totalLabel, BURGUNDY
        ));

        summaryPanel.add(createSummaryCard(
                "PENDING REQUESTS", pendingLabel, ORANGE
        ));

        summaryPanel.add(createSummaryCard(
                "APPROVED REQUESTS", approvedLabel, GREEN
        ));

        summaryPanel.add(createSummaryCard(
                "REJECTED REQUESTS", rejectedLabel, RED
        ));

        content.add(summaryPanel, BorderLayout.NORTH);

        // TABLE
        String[] columns = {
                "Request ID",
                "Transaction ID",
                "Book Title",
                "Borrowing Date",
                "Due Date",
                "Request Date",
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
        requestTable.setRowHeight(40);
        requestTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        requestTable.setSelectionBackground(new Color(245, 231, 205));
        requestTable.setSelectionForeground(TEXT);
        requestTable.setGridColor(new Color(235, 228, 218));
        requestTable.setShowVerticalLines(false);
        requestTable.setShowHorizontalLines(true);
        requestTable.setAutoCreateRowSorter(true);
        requestTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader tableHeader = requestTable.getTableHeader();
        tableHeader.setFont(new Font("SansSerif", Font.BOLD, 12));
        tableHeader.setBackground(BURGUNDY);
        tableHeader.setForeground(WHITE);
        tableHeader.setPreferredSize(new Dimension(0, 44));
        tableHeader.setReorderingAllowed(false);

        // Column widths
        int[] widths = {
                90, 115, 240, 150, 150, 155, 120, 155, 350
        };

        for (int i = 0; i < widths.length; i++) {
            requestTable.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        // Center selected columns
        DefaultTableCellRenderer centerRenderer =
                new DefaultTableCellRenderer();

        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i : new int[]{0, 1, 3, 4, 5, 6, 7}) {
            requestTable.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(centerRenderer);
        }

        // Status colors
        requestTable.getColumnModel()
                .getColumn(6)
                .setCellRenderer(new StatusRenderer());

        // Rejection reason wrapping
        requestTable.getColumnModel()
                .getColumn(8)
                .setCellRenderer(new WrappingRenderer());

        JScrollPane scrollPane = new JScrollPane(requestTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(WHITE);
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        JPanel tablePanel = new JPanel(new BorderLayout(0, 12));
        tablePanel.setBackground(WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(17, 18, 17, 18)
        ));

        JPanel tableHeading = new JPanel(new BorderLayout());
        tableHeading.setOpaque(false);

        JLabel tableTitle = new JLabel("Return Request Records");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        tableTitle.setForeground(BURGUNDY);

        resultLabel = new JLabel("Your submitted requests");
        resultLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        resultLabel.setForeground(MUTED);

        tableHeading.add(tableTitle, BorderLayout.WEST);
        tableHeading.add(resultLabel, BorderLayout.EAST);

        tablePanel.add(tableHeading, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        content.add(tablePanel, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    private void createFooter() {

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(CREAM);
        footer.setBorder(new EmptyBorder(0, 25, 20, 25));

        JLabel footerLabel = new JLabel(
                "LibraryLink  |  Your return request records"
        );
        footerLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footerLabel.setForeground(MUTED);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        buttons.setOpaque(false);

        JButton refreshButton = createButton(
                "REFRESH REQUESTS", BURGUNDY, WHITE
        );

        JButton detailsButton = createButton(
                "VIEW REQUEST DETAILS", GOLD, BURGUNDY
        );

        refreshButton.addActionListener(e -> loadRequests());
        detailsButton.addActionListener(e -> showRequestDetails());

        buttons.add(refreshButton);
        buttons.add(detailsButton);

        footer.add(footerLabel, BorderLayout.WEST);
        footer.add(buttons, BorderLayout.EAST);

        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createSummaryCard(
            String title,
            JLabel valueLabel,
            Color accent
    ) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        titleLabel.setForeground(MUTED);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 27));
        valueLabel.setForeground(accent);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        return card;
    }

    private JButton createButton(
            String text,
            Color background,
            Color foreground
    ) {

        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(11, 18, 11, 18));
        button.setOpaque(true);

        return button;
    }

    private void loadRequests() {

        tableModel.setRowCount(0);

        int pending = 0;
        int approved = 0;
        int rejected = 0;

        try {

            List<ReturnRequestService.ReturnRequest> requests =
                    ReturnRequestService.getMyReturnRequests(session);

            for (ReturnRequestService.ReturnRequest request : requests) {

                String status = request.status() == null
                        ? "UNKNOWN"
                        : request.status().trim().toUpperCase();

                if ("PENDING".equals(status)) {
                    pending++;
                } else if ("APPROVED".equals(status)) {
                    approved++;
                } else if ("REJECTED".equals(status)) {
                    rejected++;
                }

                String decisionDate =
                        request.decisionDate() == null
                                || request.decisionDate().isBlank()
                                ? "-"
                                : request.decisionDate();

                String rejectionReason =
                        request.rejectionReason() == null
                                || request.rejectionReason().isBlank()
                                ? "-"
                                : request.rejectionReason();

                tableModel.addRow(new Object[]{
                        request.returnRequestId(),
                        request.transactionId(),
                        request.bookTitle(),
                        request.borrowingDate(),
                        request.dueDate(),
                        request.requestDate(),
                        status,
                        decisionDate,
                        rejectionReason
                });
            }

            totalLabel.setText(String.valueOf(requests.size()));
            pendingLabel.setText(String.valueOf(pending));
            approvedLabel.setText(String.valueOf(approved));
            rejectedLabel.setText(String.valueOf(rejected));

            resultLabel.setText(
                    requests.size() + " request(s) found"
            );

            updateRejectionRowHeights();

            if (requests.isEmpty()) {
                resultLabel.setText("No return requests submitted yet");
            }

        } catch (SQLException | SecurityException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load return requests:\n" + e.getMessage(),
                    "System Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
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

            int rowHeight = Math.max(40, Math.min(120, lines * 18 + 16));
            requestTable.setRowHeight(row, rowHeight);
        }
    }

    private void showRequestDetails() {

        int selectedRow = requestTable.getSelectedRow();

        if (selectedRow < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a return request first.",
                    "No Request Selected",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int modelRow = requestTable.convertRowIndexToModel(selectedRow);

        String[] labels = {
                "Request ID",
                "Transaction ID",
                "Book Title",
                "Borrowing Date",
                "Due Date",
                "Request Date",
                "Status",
                "Decision Date",
                "Rejection Reason"
        };

        String[] values = new String[9];

        for (int i = 0; i < values.length; i++) {
            Object value = tableModel.getValueAt(modelRow, i);
            values[i] = value == null ? "-" : value.toString();
        }

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

                JTextArea reasonArea = new JTextArea(values[i]);
                reasonArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
                reasonArea.setForeground(TEXT);
                reasonArea.setBackground(CREAM);
                reasonArea.setLineWrap(true);
                reasonArea.setWrapStyleWord(true);
                reasonArea.setEditable(false);
                reasonArea.setRows(5);
                reasonArea.setBorder(new EmptyBorder(9, 9, 9, 9));

                JScrollPane reasonScroll = new JScrollPane(reasonArea);
                reasonScroll.setPreferredSize(new Dimension(380, 125));

                informationPanel.add(reasonScroll, gbc);

            } else {

                JLabel valueLabel = new JLabel(values[i]);
                valueLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
                valueLabel.setForeground(TEXT);

                informationPanel.add(valueLabel, gbc);
            }
        }

        detailsPanel.add(informationPanel, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(
                this,
                detailsPanel,
                "Return Request Details",
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

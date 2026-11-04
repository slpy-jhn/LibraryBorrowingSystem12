
package com.library.ui;

import com.library.service.ReturnRequestService;
import com.library.service.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MyReturnRequestsFrame extends JFrame {

    private final UserSession session;
    private JTable requestTable;
    private DefaultTableModel tableModel;
    private JLabel countLabel;

    public MyReturnRequestsFrame(UserSession session) {

        if (session == null) {
            throw new SecurityException("Please log in first.");
        }

        session.requireStudent();

        this.session = session;

        setTitle("My Return Requests");
        setSize(1100, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(new Color(245, 247, 250));
        setLayout(new BorderLayout(15, 15));

        createHeader();
        createTable();
        createButtons();

        loadRequests();
    }

    private void createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(245, 247, 250));
        header.setBorder(new EmptyBorder(20, 25, 10, 25));

        JLabel title = new JLabel("My Return Requests");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(new Color(23, 43, 77));

        countLabel = new JLabel("Loading requests...");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        countLabel.setForeground(Color.GRAY);

        header.add(title, BorderLayout.NORTH);
        header.add(countLabel, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
    }

    private void createTable() {

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

        requestTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        requestTable.setRowHeight(30);
        requestTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        requestTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        requestTable.getTableHeader().setBackground(
                new Color(23, 43, 77)
        );

        requestTable.getTableHeader().setForeground(Color.WHITE);

        requestTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {
                80, 100, 200, 120, 120, 150, 100, 150, 250
        };

        for (int i = 0; i < widths.length; i++) {
            requestTable.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        JScrollPane scrollPane = new JScrollPane(requestTable);
        scrollPane.setBorder(
                BorderFactory.createEmptyBorder(0, 20, 0, 20)
        );

        add(scrollPane, BorderLayout.CENTER);
    }

    private void createButtons() {

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 15)
        );

        buttonPanel.setBackground(new Color(245, 247, 250));
        buttonPanel.setBorder(
                new EmptyBorder(0, 20, 10, 20)
        );

        JButton refreshButton = new JButton("Refresh");
        JButton detailsButton = new JButton("View Request Details");
        JButton closeButton = new JButton("Close");

        refreshButton.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        refreshButton.setBackground(new Color(23, 43, 77));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);

        detailsButton.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        closeButton.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        refreshButton.addActionListener(e -> loadRequests());
        detailsButton.addActionListener(e -> showRequestDetails());
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(refreshButton);
        buttonPanel.add(detailsButton);
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadRequests() {

        tableModel.setRowCount(0);

        try {

            List<ReturnRequestService.ReturnRequest> requests =
                    ReturnRequestService.getMyReturnRequests(session);

            for (ReturnRequestService.ReturnRequest request : requests) {

                tableModel.addRow(new Object[]{
                        request.returnRequestId(),
                        request.transactionId(),
                        request.bookTitle(),
                        request.borrowingDate(),
                        request.dueDate(),
                        request.requestDate(),
                        request.status(),
                        request.decisionDate() == null
                                ? "-"
                                : request.decisionDate(),
                        request.rejectionReason() == null
                                || request.rejectionReason().isBlank()
                                ? "-"
                                : request.rejectionReason()
                });
            }

            countLabel.setText(
                    "Total return requests: " + requests.size()
            );

            if (requests.isEmpty()) {
                countLabel.setText(
                        "You have not submitted any return requests yet."
                );
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

        String bookTitle = String.valueOf(
                tableModel.getValueAt(selectedRow, 2)
        );

        String status = String.valueOf(
                tableModel.getValueAt(selectedRow, 6)
        );

        String decisionDate = String.valueOf(
                tableModel.getValueAt(selectedRow, 7)
        );

        String rejectionReason = String.valueOf(
                tableModel.getValueAt(selectedRow, 8)
        );

        String message = """
                Book: %s

                Request Status: %s

                Decision Date: %s

                Rejection Reason: %s
                """.formatted(
                bookTitle,
                status,
                decisionDate,
                rejectionReason
        );

        JOptionPane.showMessageDialog(
                this,
                message,
                "Return Request Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}

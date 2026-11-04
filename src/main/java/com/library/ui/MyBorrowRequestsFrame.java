
package com.library.ui;

import com.library.service.BorrowRequestService;
import com.library.service.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MyBorrowRequestsFrame extends JFrame {

    private final UserSession session;
    private final DefaultTableModel tableModel;
    private final JTable requestTable;

    public MyBorrowRequestsFrame(UserSession session) {
        if (session == null) {
            throw new SecurityException("Please log in first.");
        }

        session.requireStudent();
        this.session = session;

        setTitle("My Borrowing Requests");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columns = {
                "Request ID", "Book ID", "Book Title",
                "Request Date", "Status", "Decision Date",
                "Rejection Reason"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestTable = new JTable(tableModel);
        requestTable.setRowHeight(28);
        requestTable.setAutoCreateRowSorter(true);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadRequests());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(requestTable), BorderLayout.CENTER);

        loadRequests();
    }

    private void loadRequests() {
        tableModel.setRowCount(0);

        try {
            List<BorrowRequestService.BorrowRequest> requests =
                    BorrowRequestService.getMyRequests(session);

            for (BorrowRequestService.BorrowRequest request : requests) {
                tableModel.addRow(new Object[]{
                        request.requestId(),
                        request.bookId(),
                        request.bookTitle(),
                        request.requestDate(),
                        request.status(),
                        request.decisionDate() == null
                                ? "-"
                                : request.decisionDate(),
                        request.rejectionReason() == null
                                ? "-"
                                : request.rejectionReason()
                });
            }

            if (requests.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "You have not submitted any borrowing requests yet."
                );
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load your borrowing requests:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }
}

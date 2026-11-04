
package com.library.ui;

import com.library.service.ReturnRequestService;
import com.library.service.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ManageReturnRequestsFrame extends JFrame {

    private final UserSession session;

    private JTable table;
    private DefaultTableModel tableModel;

    public ManageReturnRequestsFrame(UserSession session) {

        if (session == null) {
            throw new IllegalArgumentException(
                    "A valid admin session is required."
            );
        }

        session.requireAdmin();
        this.session = session;

        setTitle("Manage Return Requests");
        setSize(1150, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));

        getContentPane().setBackground(
                new Color(245, 247, 250)
        );

        // HEADER
        JLabel titleLabel = new JLabel(
                "Manage Student Return Requests"
        );

        titleLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 25)
        );

        titleLabel.setForeground(
                new Color(23, 43, 77)
        );

        titleLabel.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 5, 20)
        );

        add(titleLabel, BorderLayout.NORTH);

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
        };

        table = new JTable(tableModel);

        table.setRowHeight(30);

        table.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder(0, 20, 0, 20)
        );

        add(scrollPane, BorderLayout.CENTER);

        // BOTTOM PANEL
        JPanel bottomPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 10)
        );

        bottomPanel.setBackground(
                new Color(245, 247, 250)
        );

        bottomPanel.setBorder(
                BorderFactory.createEmptyBorder(5, 20, 15, 20)
        );

        JButton approveButton =
                new JButton("Approve Return");

        JButton rejectButton =
                new JButton("Reject Request");

        JButton refreshButton =
                new JButton("Refresh");

        approveButton.setBackground(
                new Color(40, 140, 85)
        );

        approveButton.setForeground(Color.WHITE);

        rejectButton.setBackground(
                new Color(180, 60, 60)
        );

        rejectButton.setForeground(Color.WHITE);

        bottomPanel.add(approveButton);
        bottomPanel.add(rejectButton);
        bottomPanel.add(refreshButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // BUTTON ACTIONS
        approveButton.addActionListener(e ->
                approveSelectedRequest()
        );

        rejectButton.addActionListener(e ->
                rejectSelectedRequest()
        );

        refreshButton.addActionListener(e ->
                loadPendingRequests()
        );

        loadPendingRequests();
    }

    // LOAD ALL PENDING RETURN REQUESTS
    private void loadPendingRequests() {

        tableModel.setRowCount(0);

        try {

            session.requireAdmin();

            List<ReturnRequestService.ReturnRequest> requests =
                    ReturnRequestService.getPendingRequests(session);

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
            }

            if (requests.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "There are currently no pending return requests.",
                        "No Pending Requests",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (SQLException
                 | IllegalArgumentException
                 | SecurityException
                 | IllegalStateException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load return requests:\n"
                            + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
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

        int modelRow =
                table.convertRowIndexToModel(selectedRow);

        return Integer.parseInt(
                String.valueOf(
                        tableModel.getValueAt(modelRow, 0)
                )
        );
    }

    // APPROVE RETURN REQUEST
    private void approveSelectedRequest() {

        Integer requestId = getSelectedRequestId();

        if (requestId == null) {
            return;
        }

        int modelRow =
                table.convertRowIndexToModel(
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
                JOptionPane.YES_NO_OPTION
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
                JOptionPane.YES_NO_OPTION
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

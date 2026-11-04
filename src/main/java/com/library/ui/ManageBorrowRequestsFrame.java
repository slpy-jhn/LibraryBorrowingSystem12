
package com.library.ui;

import com.library.service.BorrowRequestService;
import com.library.service.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ManageBorrowRequestsFrame extends JFrame {

    private final UserSession session;

    private JTable requestTable;
    private DefaultTableModel tableModel;

    private final Color NAVY = new Color(23, 43, 77);
    private final Color WHITE = Color.WHITE;
    private final Color BACKGROUND = new Color(245, 247, 250);

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    public ManageBorrowRequestsFrame(UserSession session) {

        if (session == null) {
            throw new SecurityException("Admin session is required.");
        }

        session.requireAdmin();
        this.session = session;

        setTitle("Manage Borrowing Requests");
        setSize(1200, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadRequests();
    }

    // ==========================================
    // HEADER
    // ==========================================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(22, 30, 22, 30));

        JLabel title = new JLabel("Manage Borrowing Requests");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(WHITE);

        JLabel subtitle = new JLabel(
                "Review, approve, or reject student requests."
        );

        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(210, 220, 235));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(
                new BoxLayout(textPanel, BoxLayout.Y_AXIS)
        );

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);

        return header;
    }

    // ==========================================
    // MAIN PANEL
    // ==========================================

    private JPanel createMainPanel() {

        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(BACKGROUND);
        main.setBorder(new EmptyBorder(25, 25, 25, 25));

        String[] columns = {
                "Request ID",
                "Student ID",
                "Student Name",
                "Book ID",
                "Book Title",
                "Request Date",
                "Borrowing Duration (Days)"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestTable = new JTable(tableModel);

        requestTable.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        requestTable.setRowHeight(32);

        requestTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        requestTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        requestTable.getTableHeader().setBackground(NAVY);
        requestTable.getTableHeader().setForeground(WHITE);

        requestTable.setFillsViewportHeight(true);

        requestTable.setAutoResizeMode(
                JTable.AUTO_RESIZE_ALL_COLUMNS
        );

        JScrollPane scrollPane = new JScrollPane(requestTable);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(220, 225, 232)
                )
        );

        // ==========================================
        // BUTTONS
        // ==========================================

        JButton approveButton = createButton(
                "APPROVE REQUEST",
                new Color(35, 130, 85)
        );

        JButton rejectButton = createButton(
                "REJECT REQUEST",
                new Color(190, 60, 60)
        );

        JButton refreshButton = createButton(
                "REFRESH",
                NAVY
        );

        approveButton.addActionListener(
                e -> approveSelectedRequest()
        );

        rejectButton.addActionListener(
                e -> rejectSelectedRequest()
        );

        refreshButton.addActionListener(
                e -> loadRequests()
        );

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 5)
        );

        buttonPanel.setOpaque(false);

        buttonPanel.add(refreshButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(approveButton);

        main.add(scrollPane, BorderLayout.CENTER);
        main.add(buttonPanel, BorderLayout.SOUTH);

        return main;
    }

    // ==========================================
    // BUTTON DESIGN
    // ==========================================

    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);

        button.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        button.setBackground(color);
        button.setForeground(WHITE);

        button.setFocusPainted(false);
        button.setBorderPainted(false);

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        button.setPreferredSize(
                new Dimension(165, 40)
        );

        return button;
    }

    // ==========================================
    // LOAD PENDING REQUESTS
    // ==========================================

    private void loadRequests() {

        tableModel.setRowCount(0);

        try {

            List<BorrowRequestService.BorrowRequest> requests =
                    BorrowRequestService.getPendingRequests(session);

            for (BorrowRequestService.BorrowRequest request : requests) {

                tableModel.addRow(new Object[]{

                        request.requestId(),
                        request.memberId(),
                        request.memberName(),
                        request.bookId(),
                        request.bookTitle(),
                        request.requestDate(),
                        request.borrowingDuration() + " days"

                });
            }

            if (requests.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "There are currently no pending borrowing requests.",
                        "No Pending Requests",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

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
    // GET SELECTED REQUEST ID
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
    // GET SELECTED REQUEST DETAILS
    // ==========================================

    private BorrowRequestService.BorrowRequest
    getSelectedRequestDetails(int requestId) throws SQLException {

        List<BorrowRequestService.BorrowRequest> requests =
                BorrowRequestService.getPendingRequests(session);

        for (BorrowRequestService.BorrowRequest request : requests) {

            if (request.requestId() == requestId) {
                return request;
            }
        }

        return null;
    }

    // ==========================================
    // ADMIN: APPROVE REQUEST
    // ==========================================

    private void approveSelectedRequest() {

        int requestId = getSelectedRequestId();

        if (requestId == -1) {
            return;
        }

        BorrowRequestService.BorrowRequest selectedRequest;

        try {

            selectedRequest = getSelectedRequestDetails(requestId);

            if (selectedRequest == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "This request is no longer pending. Please refresh.",
                        "Request Not Found",
                        JOptionPane.WARNING_MESSAGE
                );

                loadRequests();
                return;
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to retrieve request details.\n"
                            + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
            return;
        }

        int studentId = selectedRequest.memberId();
        String studentName = selectedRequest.memberName();
        String bookTitle = selectedRequest.bookTitle();

        // Use the borrowing duration saved by the student.
        int borrowingDays = selectedRequest.borrowingDuration();

        LocalDate borrowingDate = LocalDate.now();

        LocalDate dueDate =
                borrowingDate.plusDays(borrowingDays);

        String confirmationMessage = """
                Are you sure you want to approve this borrowing request?

                Student ID: %d
                Student Name: %s

                Book: %s

                Borrowing Duration: %d days

                Borrowing Date: %s
                Due Date: %s
                """.formatted(
                studentId,
                studentName,
                bookTitle,
                borrowingDays,
                borrowingDate.format(dateFormatter),
                dueDate.format(dateFormatter)
        );

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                confirmationMessage,
                "Confirm Borrowing Approval",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        // ==========================================
        // APPROVE USING SAVED REQUESTED DURATION
        // ==========================================

        try {

            BorrowRequestService.approveRequest(
                    session,
                    requestId
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Borrowing request approved successfully!\n\n"
                            + "Student: " + studentName + "\n"
                            + "Book: " + bookTitle + "\n"
                            + "Borrowing Duration: " + borrowingDays + " days\n"
                            + "Due Date: " + dueDate.format(dateFormatter),
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
    // ADMIN: REJECT REQUEST
    // ==========================================

    private void rejectSelectedRequest() {

        int requestId = getSelectedRequestId();

        if (requestId == -1) {
            return;
        }

        String reason = JOptionPane.showInputDialog(
                this,
                "Enter the reason for rejecting this request:",
                "Reject Borrowing Request",
                JOptionPane.QUESTION_MESSAGE
        );

        if (reason == null) {
            return;
        }

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

            BorrowRequestService.rejectRequest(
                    session,
                    requestId,
                    reason.trim()
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

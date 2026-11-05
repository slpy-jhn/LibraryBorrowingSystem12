
package com.library.ui;

import com.library.service.MemberService;
import com.library.service.MemberBorrowingHistoryService;
import com.library.service.MemberBorrowingHistoryService.BorrowingRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ManageMembersFrame extends JFrame {

    private final DefaultTableModel tableModel;
    private final JTable memberTable;
    private final JTextField searchField;

    private final JLabel totalLabel;
    private final JLabel activeLabel;
    private final JLabel inactiveLabel;

    private final Color burgundy = new Color(105, 25, 43);
    private final Color darkBurgundy = new Color(75, 15, 30);
    private final Color gold = new Color(205, 164, 85);
    private final Color cream = new Color(250, 247, 240);
    private final Color white = Color.WHITE;
    private final Color textColor = new Color(55, 48, 48);
    private final Color muted = new Color(125, 115, 112);
    private final Color borderColor = new Color(232, 224, 215);

    public ManageMembersFrame() {

        setTitle("Manage Library Members");
        setSize(1350, 780);
        setMinimumSize(new Dimension(1050, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(cream);
        setLayout(new BorderLayout());

        // HEADER
        JPanel header = new JPanel(new BorderLayout(15, 10));
        header.setBackground(burgundy);
        header.setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Manage Library Members");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(white);

        JLabel subtitle = new JLabel(
                "Register, search and manage member accounts"
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(239, 222, 211));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JButton closeButton = createButton("Close", darkBurgundy, white);
        closeButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(gold, 1),
                new EmptyBorder(9, 18, 9, 18)
        ));
        closeButton.addActionListener(e -> dispose());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // MAIN CONTENT
        JPanel centerPanel = new JPanel(new BorderLayout(15, 18));
        centerPanel.setBackground(cream);
        centerPanel.setBorder(new EmptyBorder(20, 24, 10, 24));

        // STATISTICS
        JPanel statisticsPanel = new JPanel(
                new GridLayout(1, 3, 15, 0)
        );
        statisticsPanel.setBackground(cream);

        totalLabel = new JLabel("0", SwingConstants.CENTER);
        activeLabel = new JLabel("0", SwingConstants.CENTER);
        inactiveLabel = new JLabel("0", SwingConstants.CENTER);

        statisticsPanel.add(
                createStatCard("TOTAL MEMBERS", totalLabel)
        );
        statisticsPanel.add(
                createStatCard("ACTIVE MEMBERS", activeLabel)
        );
        statisticsPanel.add(
                createStatCard("INACTIVE MEMBERS", inactiveLabel)
        );

        // TABLE
        String[] columns = {
                "Member ID",
                "Full Name",
                "Student/Employee ID",
                "Course / Department",
                "Contact Number",
                "Email",
                "Registration Date",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        memberTable = new JTable(tableModel);
        memberTable.setRowHeight(36);
        memberTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        memberTable.setForeground(textColor);
        memberTable.setBackground(white);
        memberTable.setSelectionBackground(new Color(245, 230, 210));
        memberTable.setSelectionForeground(darkBurgundy);
        memberTable.setGridColor(new Color(239, 233, 226));
        memberTable.setShowVerticalLines(false);
        memberTable.setIntercellSpacing(new Dimension(0, 1));
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.setAutoCreateRowSorter(true);
        memberTable.setFillsViewportHeight(true);

        memberTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );
        memberTable.getTableHeader().setBackground(burgundy);
        memberTable.getTableHeader().setForeground(white);
        memberTable.getTableHeader().setPreferredSize(
                new Dimension(0, 42)
        );
        memberTable.getTableHeader().setReorderingAllowed(false);

        // Alternating table rows
        DefaultTableCellRenderer cellRenderer =
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
                                        table, value, isSelected,
                                        hasFocus, row, column
                                );

                        if (isSelected) {
                            component.setBackground(
                                    new Color(245, 230, 210)
                            );
                            component.setForeground(darkBurgundy);
                        } else {
                            component.setBackground(
                                    row % 2 == 0
                                            ? white
                                            : new Color(250, 248, 245)
                            );
                            component.setForeground(textColor);
                        }

                        setBorder(new EmptyBorder(0, 10, 0, 10));
                        return component;
                    }
                };

        for (int i = 0; i < memberTable.getColumnCount(); i++) {
            memberTable.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(cellRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(memberTable);
        scrollPane.setBorder(
                BorderFactory.createLineBorder(borderColor)
        );
        scrollPane.getViewport().setBackground(white);

        JPanel tableCard = new JPanel(new BorderLayout(0, 12));
        tableCard.setBackground(white);
        tableCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel tableTitle = new JLabel("Member Directory");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        tableTitle.setForeground(burgundy);

        JLabel tableSubtitle = new JLabel(
                "Select a member to view details or manage their account."
        );
        tableSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tableSubtitle.setForeground(muted);

        JPanel tableHeading = new JPanel();
        tableHeading.setOpaque(false);
        tableHeading.setLayout(new BoxLayout(tableHeading, BoxLayout.Y_AXIS));
        tableHeading.add(tableTitle);
        tableHeading.add(Box.createVerticalStrut(4));
        tableHeading.add(tableSubtitle);

        tableCard.add(tableHeading, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        JPanel topSection = new JPanel(new BorderLayout(0, 18));
        topSection.setBackground(cream);
        topSection.add(statisticsPanel, BorderLayout.NORTH);
        topSection.add(tableCard, BorderLayout.CENTER);

        centerPanel.add(topSection, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // BOTTOM TOOLBAR
        JPanel bottom = new JPanel(new BorderLayout(15, 10));
        bottom.setBackground(white);
        bottom.setBorder(new EmptyBorder(15, 24, 15, 24));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        searchPanel.setBackground(white);

        JLabel searchLabel = new JLabel("Search Members");
        searchLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        searchLabel.setForeground(textColor);

        searchField = new JTextField(17);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(210, 38));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                new EmptyBorder(5, 10, 5, 10)
        ));
        searchField.putClientProperty(
                "JTextField.placeholderText",
                "Name, ID, course or email..."
        );

        JButton searchButton = createButton(
                "Search", burgundy, white
        );
        JButton clearButton = createButton(
                "Clear", new Color(235, 229, 220), textColor
        );
        JButton refreshButton = createButton(
                "Refresh", new Color(235, 229, 220), textColor
        );

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);
        searchPanel.add(refreshButton);

        JPanel actionPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 7, 3)
        );
        actionPanel.setBackground(white);

        JButton historyButton = createButton(
                "Borrowing History", new Color(75, 95, 120), white
        );
        JButton detailsButton = createButton(
                "View Details", new Color(235, 229, 220), textColor
        );
        JButton statusButton = createButton(
                "Activate / Deactivate", new Color(235, 229, 220), textColor
        );
        JButton editButton = createButton(
                "Edit Member", new Color(235, 229, 220), textColor
        );
        JButton addButton = createButton(
                "+ Register Member", burgundy, white
        );

        actionPanel.add(historyButton);
        actionPanel.add(detailsButton);
        actionPanel.add(statusButton);
        actionPanel.add(editButton);
        actionPanel.add(addButton);

        bottom.add(searchPanel, BorderLayout.WEST);
        bottom.add(actionPanel, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        // BUTTON ACTIONS
        searchButton.addActionListener(e -> searchMembers());
        searchField.addActionListener(e -> searchMembers());

        clearButton.addActionListener(e -> {
            searchField.setText("");
            loadMembers();
        });

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadMembers();
        });

        addButton.addActionListener(e -> showMemberDialog(null));
        editButton.addActionListener(e -> editSelectedMember());
        detailsButton.addActionListener(e -> viewSelectedMember());
        statusButton.addActionListener(e -> toggleMemberStatus());
        historyButton.addActionListener(e -> viewBorrowingHistory());

        loadMembers();
    }

    // BUTTON DESIGN
    private JButton createButton(
            String text,
            Color backgroundColor,
            Color foregroundColor
    ) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(foregroundColor);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 13, 10, 13));
        button.setOpaque(true);
        return button;
    }

    // STATISTICS CARD
    private JPanel createStatCard(String heading, JLabel valueLabel) {

        JPanel card = new JPanel(new BorderLayout(5, 10));
        card.setBackground(white);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                new EmptyBorder(16, 12, 16, 12)
        ));

        JLabel headingLabel = new JLabel(
                heading,
                SwingConstants.CENTER
        );
        headingLabel.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );
        headingLabel.setForeground(muted);

        valueLabel.setFont(
                new Font("SansSerif", Font.BOLD, 27)
        );
        valueLabel.setForeground(burgundy);

        card.add(headingLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // LOAD ALL MEMBERS
    private void loadMembers() {
        try {
            displayMembers(MemberService.getAllMembers());
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // SEARCH MEMBERS
    private void searchMembers() {

        try {
            String keyword = searchField.getText().trim();

            if (keyword.isEmpty()) {
                loadMembers();
                return;
            }

            displayMembers(MemberService.searchMembers(keyword));

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // DISPLAY MEMBERS AND UPDATE STATISTICS
    private void displayMembers(List<MemberService.Member> members) {

        tableModel.setRowCount(0);

        int activeCount = 0;
        int inactiveCount = 0;

        for (MemberService.Member member : members) {

            String status = member.getMembershipStatus();

            if (status != null &&
                    status.equalsIgnoreCase("ACTIVE")) {
                activeCount++;
            } else {
                inactiveCount++;
            }

            tableModel.addRow(new Object[]{
                    member.getMemberId(),
                    member.getFullName(),
                    member.getStudentEmployeeId(),
                    member.getCourseDepartment(),
                    member.getContactNumber(),
                    member.getEmail(),
                    member.getRegistrationDate(),
                    status
            });
        }

        totalLabel.setText(String.valueOf(members.size()));
        activeLabel.setText(String.valueOf(activeCount));
        inactiveLabel.setText(String.valueOf(inactiveCount));
    }

    // GET SELECTED MEMBER
    private MemberService.Member getSelectedMember() {

        int selectedRow = memberTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a member first.",
                    "No Member Selected",
                    JOptionPane.WARNING_MESSAGE
            );

            return null;
        }

        int modelRow = memberTable.convertRowIndexToModel(selectedRow);
        int memberId = (int) tableModel.getValueAt(modelRow, 0);

        try {

            List<MemberService.Member> members =
                    MemberService.getAllMembers();

            for (MemberService.Member member : members) {

                if (member.getMemberId() == memberId) {
                    return member;
                }
            }

            JOptionPane.showMessageDialog(
                    this,
                    "The selected member could not be found. Refresh the list."
            );

        } catch (SQLException ex) {
            showError(ex);
        }

        return null;
    }

    // EDIT SELECTED MEMBER
    private void editSelectedMember() {

        MemberService.Member member = getSelectedMember();

        if (member != null) {
            showMemberDialog(member);
        }
    }

    // VIEW MEMBER DETAILS
    private void viewSelectedMember() {

        MemberService.Member member = getSelectedMember();

        if (member == null) {
            return;
        }

        String details =
                "Member ID: " + member.getMemberId() +
                        "\nFull Name: " + member.getFullName() +
                        "\nStudent/Employee ID: " + member.getStudentEmployeeId() +
                        "\nCourse / Department: " + member.getCourseDepartment() +
                        "\nContact Number: " + member.getContactNumber() +
                        "\nEmail: " + member.getEmail() +
                        "\nRegistration Date: " + member.getRegistrationDate() +
                        "\nMembership Status: " + member.getMembershipStatus();

        JOptionPane.showMessageDialog(
                this,
                details,
                "Member Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // VIEW MEMBER BORROWING HISTORY
    private void viewBorrowingHistory() {

        MemberService.Member member = getSelectedMember();

        if (member == null) {
            return;
        }

        try {

            List<BorrowingRecord> history =
                    MemberBorrowingHistoryService.getMemberHistory(
                            member.getMemberId()
                    );

            showBorrowingHistoryDialog(member, history);

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // DISPLAY BORROWING HISTORY
    private void showBorrowingHistoryDialog(
            MemberService.Member member,
            List<BorrowingRecord> history
    ) {

        String[] columns = {
                "Transaction ID",
                "Book ID",
                "Book Title",
                "Borrowing Date",
                "Due Date",
                "Return Date",
                "Status"
        };

        DefaultTableModel historyModel =
                new DefaultTableModel(columns, 0) {

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

        int borrowedCount = 0;
        int returnedCount = 0;
        int overdueCount = 0;

        for (BorrowingRecord record : history) {

            String status = record.status();

            if ("RETURNED".equalsIgnoreCase(status)) {
                returnedCount++;
            } else if ("OVERDUE".equalsIgnoreCase(status)) {
                overdueCount++;
            } else {
                borrowedCount++;
            }

            historyModel.addRow(new Object[]{
                    record.transactionId(),
                    record.bookId(),
                    record.bookTitle(),
                    record.borrowingDate(),
                    record.dueDate(),
                    record.returnDate() == null ||
                            record.returnDate().isBlank()
                            ? "Not Returned"
                            : record.returnDate(),
                    status
            });
        }

        JTable historyTable = new JTable(historyModel);
        historyTable.setRowHeight(32);
        historyTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        historyTable.setAutoCreateRowSorter(true);
        historyTable.setFillsViewportHeight(true);
        historyTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );
        historyTable.getTableHeader().setBackground(burgundy);
        historyTable.getTableHeader().setForeground(white);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setPreferredSize(new Dimension(900, 350));

        JLabel memberLabel = new JLabel(
                "Member: " + member.getFullName() +
                        "  |  Member ID: " + member.getMemberId()
        );
        memberLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        memberLabel.setForeground(burgundy);

        JLabel summaryLabel = new JLabel(
                "Total Transactions: " + history.size() +
                        "     Currently Borrowed: " + borrowedCount +
                        "     Returned: " + returnedCount +
                        "     Overdue: " + overdueCount
        );
        summaryLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        summaryLabel.setForeground(textColor);

        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setBackground(cream);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topPanel = new JPanel(
                new GridLayout(2, 1, 5, 5)
        );
        topPanel.setOpaque(false);
        topPanel.add(memberLabel);
        topPanel.add(summaryLabel);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        if (history.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "This member has no borrowing history yet.",
                    "Borrowing History",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        JOptionPane.showMessageDialog(
                this,
                panel,
                "Borrowing History",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    // ACTIVATE OR DEACTIVATE MEMBERSHIP
    private void toggleMemberStatus() {

        MemberService.Member existingMember = getSelectedMember();

        if (existingMember == null) {
            return;
        }

        String currentStatus = existingMember.getMembershipStatus();

        boolean currentlyActive =
                currentStatus != null &&
                        currentStatus.equalsIgnoreCase("ACTIVE");

        String newStatus = currentlyActive
                ? "INACTIVE"
                : "ACTIVE";

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Change " + existingMember.getFullName() +
                        "'s membership status to " + newStatus + "?",
                "Confirm Membership Status",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        MemberService.Member updatedMember =
                new MemberService.Member(
                        existingMember.getMemberId(),
                        existingMember.getFullName(),
                        existingMember.getStudentEmployeeId(),
                        existingMember.getCourseDepartment(),
                        existingMember.getContactNumber(),
                        existingMember.getEmail(),
                        existingMember.getRegistrationDate(),
                        newStatus
                );

        try {

            boolean success = MemberService.updateMember(updatedMember);

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Membership status updated to " + newStatus + ".",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                reloadCurrentSearch();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "The membership status could not be updated.",
                        "Update Failed",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // REGISTER OR EDIT MEMBER
    private void showMemberDialog(MemberService.Member existingMember) {

        boolean isEditing = existingMember != null;

        JTextField nameField = createFormField(
                isEditing ? existingMember.getFullName() : ""
        );

        JTextField idField = createFormField(
                isEditing ? existingMember.getStudentEmployeeId() : ""
        );

        JTextField courseField = createFormField(
                isEditing ? existingMember.getCourseDepartment() : ""
        );

        JTextField contactField = createFormField(
                isEditing ? existingMember.getContactNumber() : ""
        );

        JTextField emailField = createFormField(
                isEditing ? existingMember.getEmail() : ""
        );

        String[] statuses = {"ACTIVE", "INACTIVE"};
        JComboBox<String> statusBox = new JComboBox<>(statuses);

        statusBox.setSelectedItem(
                isEditing
                        ? existingMember.getMembershipStatus()
                        : "ACTIVE"
        );

        JPanel panel = new JPanel(new GridLayout(0, 2, 12, 12));
        panel.setBackground(white);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        addFormRow(panel, "Full Name:", nameField);
        addFormRow(panel, "Student/Employee ID:", idField);
        addFormRow(panel, "Course / Department:", courseField);
        addFormRow(panel, "Contact Number:", contactField);
        addFormRow(panel, "Email:", emailField);
        addFormRow(panel, "Membership Status:", statusBox);

        int choice = JOptionPane.showConfirmDialog(
                this,
                panel,
                isEditing ? "Edit Member Information" : "Register New Member",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();
        String memberNumber = idField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || memberNumber.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Full Name and Student/Employee ID are required.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (!email.isEmpty() && !email.contains("@")) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid email address.",
                    "Invalid Email",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String registrationDate = isEditing
                ? existingMember.getRegistrationDate()
                : LocalDate.now().toString();

        int memberId = isEditing
                ? existingMember.getMemberId()
                : 0;

        MemberService.Member member =
                new MemberService.Member(
                        memberId,
                        name,
                        memberNumber,
                        courseField.getText().trim(),
                        contactField.getText().trim(),
                        email,
                        registrationDate,
                        (String) statusBox.getSelectedItem()
                );

        try {

            boolean success;

            if (isEditing) {
                success = MemberService.updateMember(member);
            } else {
                success = MemberService.addMember(member);
            }

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        isEditing
                                ? "Member updated successfully!"
                                : "Member registered successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                reloadCurrentSearch();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "The member information could not be saved.",
                        "Save Failed",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (SQLException ex) {

            if (ex.getMessage() != null &&
                    ex.getMessage().toLowerCase().contains("unique")) {

                JOptionPane.showMessageDialog(
                        this,
                        "That Student/Employee ID is already registered.",
                        "Duplicate ID",
                        JOptionPane.WARNING_MESSAGE
                );

            } else {
                showError(ex);
            }
        }
    }

    // FORM FIELD DESIGN
    private JTextField createFormField(String value) {

        JTextField field = new JTextField(value);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(230, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                new EmptyBorder(6, 9, 6, 9)
        ));

        return field;
    }

    private void addFormRow(
            JPanel panel,
            String labelText,
            JComponent field
    ) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(textColor);

        panel.add(label);
        panel.add(field);
    }

    // RELOAD CURRENT SEARCH RESULTS
    private void reloadCurrentSearch() {

        if (searchField.getText().trim().isEmpty()) {
            loadMembers();
        } else {
            searchMembers();
        }
    }

    // DATABASE ERROR
    private void showError(Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                "A database error occurred:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );

        ex.printStackTrace();
    }
}

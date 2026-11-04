package com.library.ui;

import com.library.service.MemberService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ManageMembersFrame extends JFrame {

    private final DefaultTableModel tableModel;
    private final JTable memberTable;
    private final JTextField searchField;

    public ManageMembersFrame() {
        setTitle("Manage Members");
        setSize(1050, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));

        Color navy = new Color(25, 42, 86);
        Color background = new Color(245, 247, 250);

        getContentPane().setBackground(background);

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(navy);
        header.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Manage Library Members");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // TABLE
        String[] columns = {
                "Member ID", "Full Name", "Student/Employee ID",
                "Course / Department", "Contact Number",
                "Email", "Registration Date", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        memberTable = new JTable(tableModel);
        memberTable.setRowHeight(28);
        memberTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        memberTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );
        memberTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scrollPane = new JScrollPane(memberTable);
        scrollPane.setBorder(
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        );
        add(scrollPane, BorderLayout.CENTER);

        // BOTTOM PANEL
        JPanel bottom = new JPanel(new BorderLayout(10, 10));
        bottom.setBackground(background);
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(background);

        searchField = new JTextField(22);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Refresh");

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(background);

        JButton addButton = new JButton("Register Member");
        JButton editButton = new JButton("Edit Selected");

        addButton.setBackground(navy);
        addButton.setForeground(Color.WHITE);

        actionPanel.add(addButton);
        actionPanel.add(editButton);

        bottom.add(searchPanel, BorderLayout.WEST);
        bottom.add(actionPanel, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        // BUTTON ACTIONS
        searchButton.addActionListener(e -> searchMembers());
        searchField.addActionListener(e -> searchMembers());

        refreshButton.addActionListener(e -> loadMembers());

        addButton.addActionListener(e -> showMemberDialog(null));

        editButton.addActionListener(e -> {
            int selectedRow = memberTable.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select a member first.",
                        "No Member Selected",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            int modelRow = memberTable.convertRowIndexToModel(selectedRow);
            int memberId = (int) tableModel.getValueAt(modelRow, 0);

            try {
                List<MemberService.Member> members =
                        MemberService.getAllMembers();

                for (MemberService.Member member : members) {
                    if (member.getMemberId() == memberId) {
                        showMemberDialog(member);
                        break;
                    }
                }
            } catch (SQLException ex) {
                showError(ex);
            }
        });

        loadMembers();
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
            displayMembers(MemberService.searchMembers(keyword));
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // DISPLAY MEMBERS IN TABLE
    private void displayMembers(List<MemberService.Member> members) {
        tableModel.setRowCount(0);

        for (MemberService.Member member : members) {
            tableModel.addRow(new Object[]{
                    member.getMemberId(),
                    member.getFullName(),
                    member.getStudentEmployeeId(),
                    member.getCourseDepartment(),
                    member.getContactNumber(),
                    member.getEmail(),
                    member.getRegistrationDate(),
                    member.getMembershipStatus()
            });
        }
    }

    // REGISTER OR EDIT MEMBER
    private void showMemberDialog(MemberService.Member existingMember) {
        boolean isEditing = existingMember != null;

        JTextField nameField = new JTextField(
                isEditing ? existingMember.getFullName() : ""
        );
        JTextField idField = new JTextField(
                isEditing ? existingMember.getStudentEmployeeId() : ""
        );
        JTextField courseField = new JTextField(
                isEditing ? existingMember.getCourseDepartment() : ""
        );
        JTextField contactField = new JTextField(
                isEditing ? existingMember.getContactNumber() : ""
        );
        JTextField emailField = new JTextField(
                isEditing ? existingMember.getEmail() : ""
        );

        String[] statuses = {"ACTIVE", "INACTIVE"};

        JComboBox<String> statusBox = new JComboBox<>(statuses);
        statusBox.setSelectedItem(
                isEditing ? existingMember.getMembershipStatus() : "ACTIVE"
        );

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));

        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);

        panel.add(new JLabel("Student/Employee ID:"));
        panel.add(idField);

        panel.add(new JLabel("Course / Department:"));
        panel.add(courseField);

        panel.add(new JLabel("Contact Number:"));
        panel.add(contactField);

        panel.add(new JLabel("Email:"));
        panel.add(emailField);

        panel.add(new JLabel("Membership Status:"));
        panel.add(statusBox);

        int choice = JOptionPane.showConfirmDialog(
                this,
                panel,
                isEditing ? "Edit Member" : "Register New Member",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();
        String memberNumber = idField.getText().trim();

        if (name.isEmpty() || memberNumber.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Full Name and Student/Employee ID are required.",
                    "Missing Information",
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

        MemberService.Member member = new MemberService.Member(
                memberId,
                name,
                memberNumber,
                courseField.getText().trim(),
                contactField.getText().trim(),
                emailField.getText().trim(),
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
                                : "Member registered successfully!"
                );
                loadMembers();
            }

        } catch (SQLException ex) {
            if (ex.getMessage() != null
                    && ex.getMessage().toLowerCase().contains("unique")) {
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
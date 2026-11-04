
package com.library.ui;

import com.library.database.DatabaseConnection;
import com.library.service.StudentAccountService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.Arrays;

public class ManageStudentAccountsFrame extends JFrame {

    private JComboBox<MemberItem> memberComboBox;
    private JTextField createUsernameField;
    private JPasswordField createPasswordField;
    private JPasswordField confirmCreatePasswordField;

    private JComboBox<StudentAccountService.StudentAccount> accountComboBox;
    private JTextField updateUsernameField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmNewPasswordField;

    public ManageStudentAccountsFrame() {

        setTitle("Manage Student Accounts");
        setSize(750, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(new Color(245, 247, 250));
        setLayout(new BorderLayout(15, 15));

        JLabel titleLabel = new JLabel("Manage Student Accounts");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 25));
        titleLabel.setForeground(new Color(23, 43, 77));
        titleLabel.setBorder(new EmptyBorder(20, 25, 5, 25));

        add(titleLabel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabs.addTab("Create Account", createCreatePanel());
        tabs.addTab("Update Account", createUpdatePanel());

        add(tabs, BorderLayout.CENTER);

        loadActiveMembers();
        loadStudentAccounts();
    }

    private JPanel createCreatePanel() {

        JPanel panel = createFormPanel();

        memberComboBox = new JComboBox<>();
        createUsernameField = new JTextField(20);
        createPasswordField = new JPasswordField(20);
        confirmCreatePasswordField = new JPasswordField(20);

        addRow(panel, "Select Member:", memberComboBox, 0);
        addRow(panel, "Username:", createUsernameField, 1);
        addRow(panel, "Password:", createPasswordField, 2);
        addRow(panel, "Confirm Password:", confirmCreatePasswordField, 3);

        JLabel note = new JLabel("Password must contain at least 8 characters.");
        note.setForeground(Color.GRAY);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        GridBagConstraints gbc = createConstraints(1, 4);
        panel.add(note, gbc);

        JButton createButton = new JButton("Create Account");
        JButton refreshButton = new JButton("Refresh Members");
        JButton clearButton = new JButton("Clear");

        stylePrimaryButton(createButton);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setBackground(Color.WHITE);

        buttons.add(refreshButton);
        buttons.add(clearButton);
        buttons.add(createButton);

        GridBagConstraints buttonGbc = createConstraints(0, 5);
        buttonGbc.gridwidth = 2;
        buttonGbc.anchor = GridBagConstraints.EAST;
        panel.add(buttons, buttonGbc);

        createButton.addActionListener(e -> createAccount());
        refreshButton.addActionListener(e -> loadActiveMembers());
        clearButton.addActionListener(e -> clearCreateFields());

        return panel;
    }

    private JPanel createUpdatePanel() {

        JPanel panel = createFormPanel();

        accountComboBox = new JComboBox<>();
        updateUsernameField = new JTextField(20);
        newPasswordField = new JPasswordField(20);
        confirmNewPasswordField = new JPasswordField(20);

        addRow(panel, "Select Student Account:", accountComboBox, 0);
        addRow(panel, "New Username:", updateUsernameField, 1);
        addRow(panel, "New Password:", newPasswordField, 2);
        addRow(panel, "Confirm New Password:", confirmNewPasswordField, 3);

        JLabel note = new JLabel(
                "Leave both password fields blank to keep the current password."
        );
        note.setForeground(Color.GRAY);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        GridBagConstraints gbc = createConstraints(1, 4);
        panel.add(note, gbc);

        JButton updateButton = new JButton("Update Account");
        JButton refreshButton = new JButton("Refresh Accounts");
        JButton clearButton = new JButton("Clear");

        stylePrimaryButton(updateButton);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setBackground(Color.WHITE);

        buttons.add(refreshButton);
        buttons.add(clearButton);
        buttons.add(updateButton);

        GridBagConstraints buttonGbc = createConstraints(0, 5);
        buttonGbc.gridwidth = 2;
        buttonGbc.anchor = GridBagConstraints.EAST;
        panel.add(buttons, buttonGbc);

        accountComboBox.addActionListener(e -> populateSelectedAccount());
        updateButton.addActionListener(e -> updateAccount());
        refreshButton.addActionListener(e -> loadStudentAccounts());
        clearButton.addActionListener(e -> clearUpdateFields());

        return panel;
    }

    private JPanel createFormPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        return panel;
    }

    private void addRow(
            JPanel panel,
            String labelText,
            JComponent component,
            int row
    ) {

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));

        component.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        GridBagConstraints labelGbc = createConstraints(0, row);
        labelGbc.weightx = 0;

        GridBagConstraints fieldGbc = createConstraints(1, row);
        fieldGbc.weightx = 1;

        panel.add(label, labelGbc);
        panel.add(component, fieldGbc);
    }

    private GridBagConstraints createConstraints(int column, int row) {

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = column;
        gbc.gridy = row;
        gbc.insets = new Insets(12, 8, 12, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        return gbc;
    }

    private void stylePrimaryButton(JButton button) {

        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(new Color(23, 43, 77));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }

    private void loadActiveMembers() {

        memberComboBox.removeAllItems();

        String sql = """
                SELECT member_id, full_name, student_employee_id
                FROM members
                WHERE membership_status = 'ACTIVE'
                  AND member_id NOT IN (
                      SELECT member_id
                      FROM users
                      WHERE member_id IS NOT NULL
                  )
                ORDER BY full_name
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                memberComboBox.addItem(new MemberItem(
                        result.getInt("member_id"),
                        result.getString("full_name"),
                        result.getString("student_employee_id")
                ));
            }

            if (memberComboBox.getItemCount() == 0) {
                memberComboBox.addItem(
                        new MemberItem(-1, "No eligible members available", "")
                );
            }

        } catch (SQLException e) {

            showError("Unable to load members:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadStudentAccounts() {

        accountComboBox.removeAllItems();

        try {

            for (StudentAccountService.StudentAccount account :
                    StudentAccountService.getStudentAccounts()) {

                accountComboBox.addItem(account);
            }

            if (accountComboBox.getItemCount() == 0) {
                updateUsernameField.setText("");
                JOptionPane.showMessageDialog(
                        this,
                        "No student accounts found.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                populateSelectedAccount();
            }

        } catch (SQLException e) {

            showError("Unable to load student accounts:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateSelectedAccount() {

        StudentAccountService.StudentAccount selected =
                (StudentAccountService.StudentAccount)
                        accountComboBox.getSelectedItem();

        if (selected == null) {
            return;
        }

        updateUsernameField.setText(selected.username());

        newPasswordField.setText("");
        confirmNewPasswordField.setText("");
    }

    private void createAccount() {

        MemberItem selected =
                (MemberItem) memberComboBox.getSelectedItem();

        if (selected == null || selected.memberId <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an eligible member.",
                    "No Member Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String username = createUsernameField.getText().trim();

        char[] password = createPasswordField.getPassword();
        char[] confirmation = confirmCreatePasswordField.getPassword();

        try {

            if (username.isBlank()) {
                throw new IllegalArgumentException(
                        "Please enter a username."
                );
            }

            if (password.length < 8) {
                throw new IllegalArgumentException(
                        "Password must contain at least 8 characters."
                );
            }

            if (!Arrays.equals(password, confirmation)) {
                throw new IllegalArgumentException(
                        "Passwords do not match."
                );
            }

            StudentAccountService.createStudentAccount(
                    selected.memberId,
                    username,
                    new String(password)
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Student account created successfully!\n"
                            + "Student: " + selected.fullName
                            + "\nUsername: " + username,
                    "Account Created",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearCreateFields();
            loadActiveMembers();
            loadStudentAccounts();

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Unable to Create Account",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException | GeneralSecurityException e) {

            showError("Unable to create account:\n" + e.getMessage());
            e.printStackTrace();

        } finally {

            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');
            clearCreatePasswords();
        }
    }

    private void updateAccount() {

        StudentAccountService.StudentAccount selected =
                (StudentAccountService.StudentAccount)
                        accountComboBox.getSelectedItem();

        if (selected == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a student account.",
                    "No Account Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String username = updateUsernameField.getText().trim();

        char[] password = newPasswordField.getPassword();
        char[] confirmation = confirmNewPasswordField.getPassword();

        try {

            if (username.isBlank()) {
                throw new IllegalArgumentException(
                        "Username is required."
                );
            }

            if (password.length > 0 || confirmation.length > 0) {

                if (password.length < 8) {
                    throw new IllegalArgumentException(
                            "New password must contain at least 8 characters."
                    );
                }

                if (!Arrays.equals(password, confirmation)) {
                    throw new IllegalArgumentException(
                            "New passwords do not match."
                    );
                }
            }

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Save changes to " + selected.fullName() + "'s account?",
                    "Confirm Account Update",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }

            StudentAccountService.updateStudentAccount(
                    selected.userId(),
                    username,
                    password.length == 0 ? null : new String(password)
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Student account updated successfully!",
                    "Update Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadStudentAccounts();

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Unable to Update Account",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException | GeneralSecurityException e) {

            showError("Unable to update account:\n" + e.getMessage());
            e.printStackTrace();

        } finally {

            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');

            newPasswordField.setText("");
            confirmNewPasswordField.setText("");
        }
    }

    private void clearCreatePasswords() {
        createPasswordField.setText("");
        confirmCreatePasswordField.setText("");
    }

    private void clearCreateFields() {
        createUsernameField.setText("");
        clearCreatePasswords();
    }

    private void clearUpdateFields() {
        updateUsernameField.setText("");
        newPasswordField.setText("");
        confirmNewPasswordField.setText("");
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "System Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static class MemberItem {

        private final int memberId;
        private final String fullName;
        private final String studentId;

        private MemberItem(
                int memberId,
                String fullName,
                String studentId
        ) {
            this.memberId = memberId;
            this.fullName = fullName;
            this.studentId = studentId;
        }

        @Override
        public String toString() {
            if (memberId <= 0) {
                return fullName;
            }

            return fullName + " (" + studentId + ")";
        }
    }
}

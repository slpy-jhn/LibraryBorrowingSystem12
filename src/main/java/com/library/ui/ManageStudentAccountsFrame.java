package com.library.ui;

import com.library.database.DatabaseConnection;
import com.library.service.StudentAccountService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class ManageStudentAccountsFrame extends JFrame {

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

    private JComboBox<MemberItem> memberComboBox;
    private JTextField createUsernameField;
    private JPasswordField createPasswordField;
    private JPasswordField confirmCreatePasswordField;

    private JComboBox<StudentAccountService.StudentAccount> accountComboBox;
    private JTextField updateUsernameField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmNewPasswordField;

    private JLabel memberCountLabel;
    private JLabel accountCountLabel;

    public ManageStudentAccountsFrame() {

        setTitle("LibraryLink - Manage Student Accounts");
        setSize(1050, 760);
        setMinimumSize(new Dimension(850, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());

        createHeader();
        createMainContent();
        createFooter();

        loadActiveMembers();
        loadStudentAccounts();
    }

    // HEADER

    private void createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(23, 30, 23, 30));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Manage Student Accounts");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 27));
        titleLabel.setForeground(GOLD);

        JLabel subtitleLabel = new JLabel(
                "Create student login accounts and update existing credentials."
        );
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(240, 225, 220));

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitleLabel);

        JLabel logo = new JLabel("LIBRARYLINK");
        logo.setFont(new Font("Georgia", Font.BOLD, 18));
        logo.setForeground(GOLD);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(logo, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    // MAIN CONTENT

    private void createMainContent() {

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(CREAM);
        content.setBorder(new EmptyBorder(22, 25, 20, 25));

        // SUMMARY CARDS

        JPanel summaryPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        summaryPanel.setOpaque(false);

        memberCountLabel = new JLabel("0");
        accountCountLabel = new JLabel("0");

        summaryPanel.add(createSummaryCard(
                "ELIGIBLE MEMBERS",
                memberCountLabel,
                GOLD
        ));

        summaryPanel.add(createSummaryCard(
                "STUDENT ACCOUNTS",
                accountCountLabel,
                BURGUNDY
        ));

        content.add(summaryPanel, BorderLayout.NORTH);

        // TABS

        JTabbedPane tabs = new JTabbedPane();

        tabs.setFont(new Font("Georgia", Font.BOLD, 12));
        tabs.setBackground(WHITE);
        tabs.setForeground(BURGUNDY);
        tabs.setBorder(BorderFactory.createEmptyBorder());

        tabs.addTab("  Create Account  ", createCreatePanel());
        tabs.addTab("  Update Account  ", createUpdatePanel());

        content.add(tabs, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    // FOOTER

    private void createFooter() {

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(CREAM);
        footer.setBorder(new EmptyBorder(0, 25, 18, 25));

        JLabel footerLabel = new JLabel(
                "LibraryLink  |  Student Account Administration"
        );
        footerLabel.setFont(new Font("Georgia", Font.PLAIN, 11));
        footerLabel.setForeground(MUTED);

        JButton closeButton = createButton(
                "CLOSE",
                DARK_BURGUNDY,
                WHITE
        );

        closeButton.addActionListener(e -> dispose());

        footer.add(footerLabel, BorderLayout.WEST);
        footer.add(closeButton, BorderLayout.EAST);

        add(footer, BorderLayout.SOUTH);
    }

    // CREATE ACCOUNT TAB

    private JPanel createCreatePanel() {

        JPanel panel = createFormPanel();

        memberComboBox = new JComboBox<>();
        createUsernameField = createTextField();
        createPasswordField = createPasswordField();
        confirmCreatePasswordField = createPasswordField();

        styleComboBox(memberComboBox);

        addRow(panel, "Select Member:", memberComboBox, 0);
        addRow(panel, "Create Username:", createUsernameField, 1);
        addRow(panel, "Create Password:", createPasswordField, 2);
        addRow(panel, "Confirm Password:", confirmCreatePasswordField, 3);

        JLabel note = createNote(
                "Password must contain at least 8 characters."
        );

        addNote(panel, note, 4);

        JButton refreshButton = createButton(
                "REFRESH MEMBERS",
                CREAM,
                BURGUNDY
        );

        JButton clearButton = createButton(
                "CLEAR",
                CREAM,
                BURGUNDY
        );

        JButton createButton = createButton(
                "CREATE ACCOUNT",
                BURGUNDY,
                WHITE
        );

        JPanel buttons = createButtonPanel(
                refreshButton,
                clearButton,
                createButton
        );

        addButtonRow(panel, buttons, 5);

        createButton.addActionListener(e -> createAccount());
        refreshButton.addActionListener(e -> loadActiveMembers());
        clearButton.addActionListener(e -> clearCreateFields());

        return panel;
    }

    // UPDATE ACCOUNT TAB

    private JPanel createUpdatePanel() {

        JPanel panel = createFormPanel();

        accountComboBox = new JComboBox<>();
        updateUsernameField = createTextField();
        newPasswordField = createPasswordField();
        confirmNewPasswordField = createPasswordField();

        styleComboBox(accountComboBox);

        addRow(panel, "Select Student Account:", accountComboBox, 0);
        addRow(panel, "Update Username:", updateUsernameField, 1);
        addRow(panel, "New Password:", newPasswordField, 2);
        addRow(panel, "Confirm New Password:", confirmNewPasswordField, 3);

        addNote(panel, createNote(
                "Edit the username above to change the student's login username."
        ), 4);

        addNote(panel, createNote(
                "Leave both password fields blank to keep the current password."
        ), 5);

        JButton refreshButton = createButton(
                "REFRESH ACCOUNTS",
                CREAM,
                BURGUNDY
        );

        JButton clearButton = createButton(
                "CLEAR FIELDS",
                CREAM,
                BURGUNDY
        );

        JButton updateButton = createButton(
                "SAVE ACCOUNT CHANGES",
                BURGUNDY,
                WHITE
        );

        JPanel buttons = createButtonPanel(
                refreshButton,
                clearButton,
                updateButton
        );

        addButtonRow(panel, buttons, 6);

        accountComboBox.addActionListener(
                e -> populateSelectedAccount()
        );

        updateButton.addActionListener(e -> updateAccount());
        refreshButton.addActionListener(e -> loadStudentAccounts());
        clearButton.addActionListener(e -> clearUpdateFields());

        return panel;
    }

    // SHARED FORM LAYOUT

    private JPanel createFormPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(WHITE);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(30, 40, 30, 40)
        ));

        return panel;
    }

    private void addRow(
            JPanel panel,
            String labelText,
            JComponent component,
            int row
    ) {

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Georgia", Font.BOLD, 12));
        label.setForeground(TEXT);

        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = 0;
        labelGbc.gridy = row;
        labelGbc.weightx = 0;
        labelGbc.fill = GridBagConstraints.NONE;
        labelGbc.anchor = GridBagConstraints.WEST;
        labelGbc.insets = new Insets(10, 8, 10, 20);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1;
        fieldGbc.gridy = row;
        fieldGbc.weightx = 1;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.anchor = GridBagConstraints.WEST;
        fieldGbc.insets = new Insets(10, 8, 10, 8);

        panel.add(label, labelGbc);
        panel.add(component, fieldGbc);
    }

    private void addNote(JPanel panel, JLabel note, int row) {

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 8, 5, 8);

        panel.add(note, gbc);
    }

    // BUTTON ROW POSITIONING

    private void addButtonRow(
            JPanel panel,
            JPanel buttons,
            int row
    ) {

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(12, 8, 5, 8);

        panel.add(buttons, gbc);

        GridBagConstraints filler = new GridBagConstraints();

        filler.gridx = 0;
        filler.gridy = row + 1;
        filler.gridwidth = 2;
        filler.weightx = 1;
        filler.weighty = 1;
        filler.fill = GridBagConstraints.BOTH;

        panel.add(Box.createGlue(), filler);
    }

    private JPanel createButtonPanel(
            JButton first,
            JButton second,
            JButton third
    ) {

        JPanel buttons = new JPanel(new GridLayout(1, 3, 10, 0));
        buttons.setOpaque(false);

        Dimension buttonSize = new Dimension(170, 40);

        first.setPreferredSize(buttonSize);
        second.setPreferredSize(buttonSize);
        third.setPreferredSize(buttonSize);

        buttons.add(first);
        buttons.add(second);
        buttons.add(third);

        return buttons;
    }

    // SUMMARY CARDS

    private JPanel createSummaryCard(
            String title,
            JLabel valueLabel,
            Color accent
    ) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(13, 18, 13, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 11));
        titleLabel.setForeground(MUTED);

        valueLabel.setFont(new Font("Georgia", Font.BOLD, 27));
        valueLabel.setForeground(accent);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(7));
        textPanel.add(valueLabel);

        JLabel decoration = new JLabel("L");
        decoration.setFont(new Font("Georgia", Font.BOLD, 48));
        decoration.setForeground(new Color(225, 211, 185));

        card.add(textPanel, BorderLayout.CENTER);
        card.add(decoration, BorderLayout.EAST);

        return card;
    }

    // INPUT STYLING

    private JTextField createTextField() {

        JTextField field = new JTextField();

        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setBackground(WHITE);
        field.setCaretColor(BURGUNDY);

        field.setPreferredSize(new Dimension(300, 42));
        field.setMinimumSize(new Dimension(180, 42));

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        return field;
    }

    private JPasswordField createPasswordField() {

        JPasswordField field = new JPasswordField();

        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setBackground(WHITE);
        field.setCaretColor(BURGUNDY);

        field.setPreferredSize(new Dimension(300, 42));
        field.setMinimumSize(new Dimension(180, 42));

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        return field;
    }

    private void styleComboBox(JComboBox<?> comboBox) {

        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(WHITE);
        comboBox.setForeground(TEXT);

        comboBox.setPreferredSize(new Dimension(300, 42));
        comboBox.setMinimumSize(new Dimension(180, 42));

        comboBox.setBorder(BorderFactory.createLineBorder(BORDER, 1));
    }

    private JLabel createNote(String text) {

        JLabel note = new JLabel(text);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setForeground(MUTED);

        return note;
    }

    private JButton createButton(
            String text,
            Color background,
            Color foreground
    ) {

        JButton button = new JButton(text);

        button.setFont(new Font("Georgia", Font.BOLD, 11));
        button.setBackground(background);
        button.setForeground(foreground);

        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD, 1),
                new EmptyBorder(10, 14, 10, 14)
        ));

        return button;
    }

    // LOAD ELIGIBLE MEMBERS

    private void loadActiveMembers() {

        if (memberComboBox == null) {
            return;
        }

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

            int count = 0;

            while (result.next()) {

                memberComboBox.addItem(new MemberItem(
                        result.getInt("member_id"),
                        result.getString("full_name"),
                        result.getString("student_employee_id")
                ));

                count++;
            }

            memberCountLabel.setText(String.valueOf(count));

            if (memberComboBox.getItemCount() == 0) {
                memberComboBox.addItem(
                        new MemberItem(
                                -1,
                                "No eligible members available",
                                ""
                        )
                );
            }

        } catch (SQLException e) {

            showError("Unable to load members:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    // LOAD STUDENT ACCOUNTS

    private void loadStudentAccounts() {

        if (accountComboBox == null) {
            return;
        }

        accountComboBox.removeAllItems();

        try {

            for (StudentAccountService.StudentAccount account :
                    StudentAccountService.getStudentAccounts()) {

                accountComboBox.addItem(account);
            }

            accountCountLabel.setText(
                    String.valueOf(accountComboBox.getItemCount())
            );

            if (accountComboBox.getItemCount() == 0) {

                updateUsernameField.setText("");
                clearUpdatePasswords();

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

        if (accountComboBox == null) {
            return;
        }

        StudentAccountService.StudentAccount selected =
                (StudentAccountService.StudentAccount)
                        accountComboBox.getSelectedItem();

        if (selected == null) {
            return;
        }

        updateUsernameField.setText(selected.username());

        clearUpdatePasswords();
    }

    // CREATE ACCOUNT

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

    // UPDATE ACCOUNT

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

            boolean usernameChanged =
                    !username.equals(selected.username());

            boolean passwordChanged = password.length > 0;

            if (!usernameChanged && !passwordChanged) {

                JOptionPane.showMessageDialog(
                        this,
                        "No changes were entered.",
                        "Nothing to Update",
                        JOptionPane.INFORMATION_MESSAGE
                );

                return;
            }

            StringBuilder changes = new StringBuilder();

            if (usernameChanged) {
                changes.append("Username");
            }

            if (passwordChanged) {

                if (changes.length() > 0) {
                    changes.append(" and ");
                }

                changes.append("Password");
            }

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Save " + changes + " changes for "
                            + selected.fullName() + "?",
                    "Confirm Account Update",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
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
                    "Student account updated successfully!\n\n"
                            + "Student: " + selected.fullName()
                            + "\nUsername: " + username
                            + "\nUpdated: " + changes,
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

            clearUpdatePasswords();
        }
    }

    // CLEAR FIELDS

    private void clearCreatePasswords() {

        if (createPasswordField != null) {
            createPasswordField.setText("");
        }

        if (confirmCreatePasswordField != null) {
            confirmCreatePasswordField.setText("");
        }
    }

    private void clearCreateFields() {

        createUsernameField.setText("");
        clearCreatePasswords();

        if (memberComboBox.getItemCount() > 0) {
            memberComboBox.setSelectedIndex(0);
        }
    }

    private void clearUpdatePasswords() {

        if (newPasswordField != null) {
            newPasswordField.setText("");
        }

        if (confirmNewPasswordField != null) {
            confirmNewPasswordField.setText("");
        }
    }

    private void clearUpdateFields() {

        StudentAccountService.StudentAccount selected =
                (StudentAccountService.StudentAccount)
                        accountComboBox.getSelectedItem();

        if (selected != null) {
            updateUsernameField.setText(selected.username());
        } else {
            updateUsernameField.setText("");
        }

        clearUpdatePasswords();
    }

    // ERROR MESSAGE

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "System Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // MEMBER COMBO BOX ITEM

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
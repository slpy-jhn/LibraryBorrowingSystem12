
package com.library.ui;

import com.library.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MyAccountFrame extends JFrame {

    private static final Color BURGUNDY = new Color(92, 20, 38);
    private static final Color GOLD = new Color(205, 166, 82);
    private static final Color CREAM = new Color(248, 245, 238);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(55, 48, 48);
    private static final Color MUTED = new Color(125, 116, 112);
    private static final Color BORDER = new Color(229, 222, 211);

    private final int memberId;

    private JLabel nameValue;
    private JLabel studentIdValue;
    private JLabel departmentValue;
    private JLabel contactValue;
    private JLabel emailValue;
    private JLabel registrationValue;
    private JLabel statusValue;

    public MyAccountFrame(int memberId) {

        this.memberId = memberId;

        setTitle("LibraryLink - My Account");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(650, 650));
        setSize(850, 750);
        setLocationRelativeTo(null);

        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());

        createHeader();
        createAccountInformation();
        createFooter();

        loadAccountInformation();
    }

    private void createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("My Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 27));
        titleLabel.setForeground(WHITE);

        JLabel subtitleLabel = new JLabel(
                "View your registered library membership information"
        );
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(235, 218, 211));

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitleLabel);

        JButton closeButton = createButton("Close", BURGUNDY, WHITE);
        closeButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD, 1),
                new EmptyBorder(8, 18, 8, 18)
        ));
        closeButton.addActionListener(e -> dispose());

        header.add(titlePanel, BorderLayout.CENTER);
        header.add(closeButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    private void createAccountInformation() {

        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBackground(CREAM);
        outerPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel informationPanel = new JPanel(new GridBagLayout());
        informationPanel.setBackground(WHITE);
        informationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(25, 30, 25, 30)
        ));

        JLabel sectionTitle = new JLabel("Membership Information");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        sectionTitle.setForeground(BURGUNDY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(13, 10, 13, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameValue = createValueLabel();
        studentIdValue = createValueLabel();
        departmentValue = createValueLabel();
        contactValue = createValueLabel();
        emailValue = createValueLabel();
        registrationValue = createValueLabel();
        statusValue = createValueLabel();

        addInformationRow(
                informationPanel, gbc, 1,
                "Full Name", nameValue
        );

        addInformationRow(
                informationPanel, gbc, 2,
                "Student / Employee ID", studentIdValue
        );

        addInformationRow(
                informationPanel, gbc, 3,
                "Course / Department", departmentValue
        );

        addInformationRow(
                informationPanel, gbc, 4,
                "Contact Number", contactValue
        );

        addInformationRow(
                informationPanel, gbc, 5,
                "Email Address", emailValue
        );

        addInformationRow(
                informationPanel, gbc, 6,
                "Registration Date", registrationValue
        );

        addInformationRow(
                informationPanel, gbc, 7,
                "Membership Status", statusValue
        );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 10, 18, 10);
        informationPanel.add(sectionTitle, gbc);

        outerPanel.add(informationPanel, BorderLayout.NORTH);

        add(outerPanel, BorderLayout.CENTER);
    }

    private void createFooter() {

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(CREAM);
        footer.setBorder(new EmptyBorder(0, 30, 22, 30));

        JLabel footerLabel = new JLabel(
                "LibraryLink  |  Student Membership Profile"
        );
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(MUTED);

        JButton refreshButton = createButton(
                "Refresh Information", BURGUNDY, WHITE
        );

        refreshButton.addActionListener(e -> loadAccountInformation());

        footer.add(footerLabel, BorderLayout.WEST);
        footer.add(refreshButton, BorderLayout.EAST);

        add(footer, BorderLayout.SOUTH);
    }

    private JLabel createValueLabel() {

        JLabel label = new JLabel("-");

        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(TEXT);

        return label;
    }

    private void addInformationRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String labelText,
            JLabel valueLabel) {

        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(13, 10, 13, 10);

        gbc.gridx = 0;
        gbc.weightx = 0.4;

        JLabel fieldLabel = new JLabel(labelText);
        fieldLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        fieldLabel.setForeground(MUTED);

        panel.add(fieldLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;

        panel.add(valueLabel, gbc);
    }

    private JButton createButton(
            String text,
            Color background,
            Color foreground) {

        JButton button = new JButton(text);

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        button.setOpaque(true);

        return button;
    }

    private void loadAccountInformation() {

        if (memberId <= 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Invalid student account.",
                    "Account Error",
                    JOptionPane.ERROR_MESSAGE
            );

            dispose();
            return;
        }

        String sql = """
                SELECT
                    full_name,
                    student_employee_id,
                    course_department,
                    contact_number,
                    email,
                    registration_date,
                    membership_status
                FROM members
                WHERE member_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, memberId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    nameValue.setText(
                            valueOrDefault(result.getString("full_name"))
                    );

                    studentIdValue.setText(
                            valueOrDefault(
                                    result.getString("student_employee_id")
                            )
                    );

                    departmentValue.setText(
                            valueOrDefault(
                                    result.getString("course_department")
                            )
                    );

                    contactValue.setText(
                            valueOrDefault(
                                    result.getString("contact_number")
                            )
                    );

                    emailValue.setText(
                            valueOrDefault(result.getString("email"))
                    );

                    registrationValue.setText(
                            valueOrDefault(
                                    result.getString("registration_date")
                            )
                    );

                    statusValue.setText(
                            valueOrDefault(
                                    result.getString("membership_status")
                            )
                    );

                } else {

                    JOptionPane.showMessageDialog(
                            this,
                            "Student account information was not found.",
                            "Account Not Found",
                            JOptionPane.WARNING_MESSAGE
                    );

                    dispose();
                }
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load account information:\n"
                            + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    private String valueOrDefault(String value) {

        if (value == null || value.isBlank()) {
            return "Not provided";
        }

        return value;
    }
}

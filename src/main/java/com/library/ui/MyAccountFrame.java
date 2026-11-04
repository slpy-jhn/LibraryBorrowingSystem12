
package com.library.ui;

import com.library.database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MyAccountFrame extends JFrame {

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

        setTitle("My Account");
        setSize(650, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(
                new Color(245, 247, 250)
        );

        setLayout(new BorderLayout(15, 15));

        // HEADER
        JLabel titleLabel = new JLabel("My Account");

        titleLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 26)
        );

        titleLabel.setForeground(
                new Color(23, 43, 77)
        );

        titleLabel.setBorder(
                BorderFactory.createEmptyBorder(20, 25, 5, 25)
        );

        add(titleLabel, BorderLayout.NORTH);

        // ACCOUNT INFORMATION PANEL
        JPanel informationPanel = new JPanel(
                new GridBagLayout()
        );

        informationPanel.setBackground(Color.WHITE);

        informationPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(220, 225, 232)
                        ),
                        BorderFactory.createEmptyBorder(
                                25, 30, 25, 30
                        )
                )
        );

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(12, 10, 12, 10);
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
                informationPanel, gbc, 0,
                "Full Name:", nameValue
        );

        addInformationRow(
                informationPanel, gbc, 1,
                "Student/Employee ID:", studentIdValue
        );

        addInformationRow(
                informationPanel, gbc, 2,
                "Course/Department:", departmentValue
        );

        addInformationRow(
                informationPanel, gbc, 3,
                "Contact Number:", contactValue
        );

        addInformationRow(
                informationPanel, gbc, 4,
                "Email Address:", emailValue
        );

        addInformationRow(
                informationPanel, gbc, 5,
                "Registration Date:", registrationValue
        );

        addInformationRow(
                informationPanel, gbc, 6,
                "Membership Status:", statusValue
        );

        JPanel centerPanel = new JPanel(
                new BorderLayout()
        );

        centerPanel.setBackground(
                new Color(245, 247, 250)
        );

        centerPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        );

        centerPanel.add(
                informationPanel,
                BorderLayout.CENTER
        );

        add(centerPanel, BorderLayout.CENTER);

        // BOTTOM PANEL
        JPanel bottomPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 15, 15)
        );

        bottomPanel.setBackground(
                new Color(245, 247, 250)
        );

        JButton refreshButton = new JButton("Refresh");

        bottomPanel.add(refreshButton);

        add(bottomPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e ->
                loadAccountInformation()
        );

        loadAccountInformation();
    }

    private JLabel createValueLabel() {

        JLabel label = new JLabel("-");

        label.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        label.setForeground(
                new Color(50, 60, 75)
        );

        return label;
    }

    private void addInformationRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String labelText,
            JLabel valueLabel
    ) {

        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0.4;

        JLabel fieldLabel = new JLabel(labelText);

        fieldLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 14)
        );

        fieldLabel.setForeground(
                new Color(70, 80, 95)
        );

        panel.add(fieldLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;

        panel.add(valueLabel, gbc);
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
                            valueOrDefault(
                                    result.getString("full_name")
                            )
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
                            valueOrDefault(
                                    result.getString("email")
                            )
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

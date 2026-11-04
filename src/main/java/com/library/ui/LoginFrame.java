
package com.library.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.library.service.AuthService;
import com.library.service.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPassword;

    private final Color NAVY = new Color(23, 43, 77);
    private final Color WHITE = Color.WHITE;

    public LoginFrame() {

        setTitle("Library Borrowing System");
        setSize(900, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        setLayout(new GridLayout(1, 2));

        add(createLeftPanel());
        add(createRightPanel());
    }

    // LEFT PANEL
    private JPanel createLeftPanel() {

        JPanel panel = new JPanel();
        panel.setBackground(NAVY);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(70, 45, 50, 45));

        JLabel icon = new JLabel("📚");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 65));
        icon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("LIBRARY");
        title.setFont(new Font("Segoe UI", Font.BOLD, 35));
        title.setForeground(WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("BORROWING SYSTEM");
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        subtitle.setForeground(new Color(190, 205, 225));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "<html>Manage books, members, and<br>" +
                        "borrowing transactions in one place.</html>"
        );
        description.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        description.setForeground(WHITE);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel footer = new JLabel(
                "A SMARTER WAY TO MANAGE YOUR LIBRARY"
        );
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footer.setForeground(new Color(170, 185, 210));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(icon);
        panel.add(Box.createVerticalStrut(20));
        panel.add(title);
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(25));
        panel.add(description);
        panel.add(Box.createVerticalGlue());
        panel.add(footer);

        return panel;
    }

    // RIGHT PANEL
    private JPanel createRightPanel() {

        JPanel panel = new JPanel();
        panel.setBackground(WHITE);
        panel.setLayout(new GridBagLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(25, 45, 25, 45));
        form.setPreferredSize(new Dimension(390, 420));

        JLabel heading = new JLabel("Welcome Back!");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        heading.setForeground(NAVY);

        JLabel description = new JLabel(
                "Sign in to access your library account."
        );
        description.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        description.setForeground(Color.GRAY);

        JLabel usernameLabel = createLabel("Username");

        usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setMaximumSize(new Dimension(320, 42));

        JLabel passwordLabel = createLabel("Password");

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setMaximumSize(new Dimension(320, 42));

        showPassword = new JCheckBox("Show password");
        showPassword.setBackground(WHITE);
        showPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        showPassword.addActionListener(e -> {
            passwordField.setEchoChar(
                    showPassword.isSelected() ? (char) 0 : '•'
            );
        });

        JButton loginButton = new JButton("SIGN IN");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(NAVY);
        loginButton.setForeground(WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setMaximumSize(new Dimension(320, 43));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton clearButton = new JButton("CLEAR");
        clearButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        clearButton.setMaximumSize(new Dimension(320, 38));
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel footer = new JLabel(
                "Library Borrowing System © 2026"
        );
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setForeground(Color.GRAY);

        loginButton.addActionListener(e -> login());
        clearButton.addActionListener(e -> clearFields());
        passwordField.addActionListener(e -> login());

        form.add(heading);
        form.add(Box.createVerticalStrut(8));
        form.add(description);
        form.add(Box.createVerticalStrut(35));

        form.add(usernameLabel);
        form.add(Box.createVerticalStrut(7));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(20));

        form.add(passwordLabel);
        form.add(Box.createVerticalStrut(7));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(5));

        form.add(showPassword);
        form.add(Box.createVerticalStrut(25));

        form.add(loginButton);
        form.add(Box.createVerticalStrut(10));
        form.add(clearButton);

        form.add(Box.createVerticalGlue());
        form.add(footer);

        panel.add(form);

        return panel;
    }

    // LABEL DESIGN
    private JLabel createLabel(String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(NAVY);

        return label;
    }

    // LOGIN FUNCTION
    private void login() {

        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty() || password.length == 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter your username and password.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );

            Arrays.fill(password, '\0');
            return;
        }

        try {

            AuthService.AuthResult result =
                    AuthService.authenticate(username, password);

            if (result == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Invalid username or password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );

                passwordField.requestFocus();
                return;
            }

            // CREATE AUTHENTICATED USER SESSION
            UserSession session = new UserSession(
                    result.username(),
                    result.role(),
                    result.memberId()
            );

            // ADMIN LOGIN
            if (session.isAdmin()) {

                session.requireAdmin();

                AdminDashboard dashboard =
                        new AdminDashboard(session);

                dashboard.setVisible(true);
                dispose();
            }

            // STUDENT LOGIN
            else if (session.isStudent()) {

                // Validate and retrieve the authenticated student ID.
                session.requireStudent();

                StudentDashboard dashboard =
                        new StudentDashboard(session);

                dashboard.setVisible(true);
                dispose();
            }

            // UNKNOWN ROLE
            else {

                JOptionPane.showMessageDialog(
                        this,
                        "Your account has an unrecognized role.",
                        "Access Denied",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to process login.\n" + e.getMessage(),
                    "System Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();

        } finally {

            passwordField.setText("");
        }
    }

    // CLEAR FUNCTION
    private void clearFields() {

        usernameField.setText("");
        passwordField.setText("");
        showPassword.setSelected(false);
        passwordField.setEchoChar('•');

        usernameField.requestFocus();
    }

    // MAIN METHOD FOR TESTING
    public static void main(String[] args) {

        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}

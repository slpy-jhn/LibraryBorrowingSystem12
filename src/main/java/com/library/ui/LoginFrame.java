
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

    private final Color BURGUNDY = new Color(105, 12, 43);
    private final Color DARK_BURGUNDY = new Color(75, 8, 31);
    private final Color GOLD = new Color(207, 173, 91);
    private final Color CREAM = new Color(250, 246, 235);
    private final Color DARK_TEXT = new Color(65, 42, 43);
    private final Color MUTED = new Color(135, 125, 115);
    private final Color FIELD_BORDER = new Color(205, 188, 151);

    private static final int FIELD_WIDTH = 420;
    private static final int FIELD_HEIGHT = 55;

    public LoginFrame() {

        setTitle("LibraryLink - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setMinimumSize(new Dimension(850, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        setLayout(new GridLayout(1, 2, 0, 0));

        add(createLibraryPanel());
        add(createLoginPanel());
    }

    // LEFT SIDE: BOOKSHELF WITHOUT TEXT

    private JPanel createLibraryPanel() {

        ShelfPanel panel = new ShelfPanel();
        panel.setLayout(new BorderLayout());

        return panel;
    }

    // RIGHT SIDE: LOGIN DESIGN

    private JPanel createLoginPanel() {

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(CREAM);
        background.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(460, 590));

        // LOGO: SINGLE GOLD L

        JLabel logo = new JLabel("L", SwingConstants.CENTER);
        logo.setFont(new Font("Georgia", Font.BOLD, 82));
        logo.setForeground(GOLD);
        logo.setPreferredSize(new Dimension(420, 92));
        logo.setMinimumSize(new Dimension(420, 92));
        logo.setMaximumSize(new Dimension(420, 92));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // LIBRARYLINK BRAND NAME

        JLabel brand = new JLabel("LibraryLink", SwingConstants.CENTER);
        brand.setFont(new Font("Georgia", Font.BOLD, 31));
        brand.setForeground(BURGUNDY);
        brand.setPreferredSize(new Dimension(420, 43));
        brand.setMaximumSize(new Dimension(420, 43));
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);

        // HEADING

        JLabel heading = new JLabel("Welcome Back!", SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 25));
        heading.setForeground(DARK_TEXT);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel description = new JLabel(
                "Sign in to access your library account.",
                SwingConstants.CENTER
        );
        description.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        description.setForeground(MUTED);
        description.setAlignmentX(Component.CENTER_ALIGNMENT);

        // USERNAME LABEL AND FIELD

        JLabel usernameLabel = createLabel("Username");

        usernameField = new RoundedTextField(
                "username123"
        );

        // PASSWORD LABEL AND FIELD

        JLabel passwordLabel = createLabel("Password");

        passwordField = new RoundedPasswordField(
                "Enter your password"
        );

        // SHOW PASSWORD CHECKBOX
        // This panel keeps the checkbox aligned with the fields.

        JPanel showPasswordPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0)
        );

        showPasswordPanel.setOpaque(false);
        showPasswordPanel.setPreferredSize(
                new Dimension(FIELD_WIDTH, 30)
        );
        showPasswordPanel.setMaximumSize(
                new Dimension(FIELD_WIDTH, 30)
        );
        showPasswordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        showPassword = new JCheckBox("Show password");
        showPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showPassword.setForeground(MUTED);
        showPassword.setOpaque(false);
        showPassword.setFocusPainted(false);
        showPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));

        showPassword.addActionListener(e -> {
            passwordField.setEchoChar(
                    showPassword.isSelected() ? (char) 0 : '•'
            );
        });

        showPasswordPanel.add(showPassword);

        // LOGIN BUTTON

        RoundedButton loginButton = new RoundedButton(
                "LOGIN", BURGUNDY, GOLD
        );

        loginButton.setPreferredSize(
                new Dimension(FIELD_WIDTH, 55)
        );
        loginButton.setMinimumSize(
                new Dimension(FIELD_WIDTH, 55)
        );
        loginButton.setMaximumSize(
                new Dimension(FIELD_WIDTH, 55)
        );
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginButton.addActionListener(e -> login());
        passwordField.addActionListener(e -> login());

        // CLEAR BUTTON

        JButton clearButton = new JButton("Clear fields");
        clearButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        clearButton.setForeground(BURGUNDY);
        clearButton.setBorderPainted(false);
        clearButton.setContentAreaFilled(false);
        clearButton.setFocusPainted(false);
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        clearButton.addActionListener(e -> clearFields());

        // FOOTER

        JLabel footer = new JLabel(
                "Library Borrowing System © 2026",
                SwingConstants.CENTER
        );

        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setForeground(MUTED);
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ADD COMPONENTS IN ORDER

        form.add(Box.createVerticalGlue());

        form.add(logo);
        form.add(Box.createVerticalStrut(0));
        form.add(brand);

        form.add(Box.createVerticalStrut(28));

        form.add(heading);
        form.add(Box.createVerticalStrut(7));
        form.add(description);

        form.add(Box.createVerticalStrut(32));

        form.add(usernameLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(usernameField);

        form.add(Box.createVerticalStrut(18));

        form.add(passwordLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(passwordField);

        form.add(Box.createVerticalStrut(7));
        form.add(showPasswordPanel);

        form.add(Box.createVerticalStrut(20));
        form.add(loginButton);

        form.add(Box.createVerticalStrut(8));
        form.add(clearButton);

        form.add(Box.createVerticalStrut(25));
        form.add(footer);

        form.add(Box.createVerticalGlue());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        background.add(form, gbc);

        return background;
    }

    // LABEL DESIGN

    private JLabel createLabel(String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(DARK_TEXT);
        label.setPreferredSize(new Dimension(FIELD_WIDTH, 22));
        label.setMaximumSize(new Dimension(FIELD_WIDTH, 22));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

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

                dashboard.setExtendedState(JFrame.MAXIMIZED_BOTH);
                dashboard.setVisible(true);

                dispose();
            }

            // STUDENT LOGIN

            else if (session.isStudent()) {

                session.requireStudent();

                StudentDashboard dashboard =
                        new StudentDashboard(session);

                dashboard.setExtendedState(JFrame.MAXIMIZED_BOTH);
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

            Arrays.fill(password, '\0');
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

    // ROUNDED USERNAME FIELD

    private class RoundedTextField extends JTextField {

        private final String placeholder;

        RoundedTextField(String placeholder) {

            this.placeholder = placeholder;

            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(DARK_TEXT);
            setCaretColor(BURGUNDY);
            setOpaque(false);

            setBorder(new EmptyBorder(5, 17, 5, 17));

            setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
            setMinimumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
            setMaximumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));

            setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(
                    0, 0, getWidth(), getHeight(), 18, 18
            );

            g2.dispose();

            super.paintComponent(g);

            if (getText().isEmpty() && !hasFocus()) {

                Graphics2D p = (Graphics2D) g.create();
                p.setColor(new Color(160, 150, 140));
                p.setFont(getFont());

                p.drawString(
                        placeholder,
                        18,
                        getHeight() / 2 + 5
                );

                p.dispose();
            }
        }

        @Override
        protected void paintBorder(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(
                    hasFocus() ? GOLD : FIELD_BORDER
            );

            g2.setStroke(new BasicStroke(hasFocus() ? 2f : 1.3f));

            g2.drawRoundRect(
                    1, 1,
                    getWidth() - 3,
                    getHeight() - 3,
                    18, 18
            );

            g2.dispose();
        }
    }

    // ROUNDED PASSWORD FIELD

    private class RoundedPasswordField extends JPasswordField {

        private final String placeholder;

        RoundedPasswordField(String placeholder) {

            this.placeholder = placeholder;

            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(DARK_TEXT);
            setCaretColor(BURGUNDY);
            setOpaque(false);

            setBorder(new EmptyBorder(5, 17, 5, 17));

            setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
            setMinimumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
            setMaximumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));

            setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(
                    0, 0, getWidth(), getHeight(), 18, 18
            );

            g2.dispose();

            super.paintComponent(g);

            if (getPassword().length == 0 && !hasFocus()) {

                Graphics2D p = (Graphics2D) g.create();
                p.setColor(new Color(160, 150, 140));
                p.setFont(getFont());

                p.drawString(
                        placeholder,
                        18,
                        getHeight() / 2 + 5
                );

                p.dispose();
            }
        }

        @Override
        protected void paintBorder(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(
                    hasFocus() ? GOLD : FIELD_BORDER
            );

            g2.setStroke(new BasicStroke(hasFocus() ? 2f : 1.3f));

            g2.drawRoundRect(
                    1, 1,
                    getWidth() - 3,
                    getHeight() - 3,
                    18, 18
            );

            g2.dispose();
        }
    }

    // ROUNDED LOGIN BUTTON

    private class RoundedButton extends JButton {

        private final Color buttonColor;
        private final Color textColor;

        RoundedButton(
                String text,
                Color buttonColor,
                Color textColor
        ) {

            super(text);

            this.buttonColor = buttonColor;
            this.textColor = textColor;

            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setForeground(textColor);

            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);

            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(0, 0, 0, 0));
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color currentColor;

            if (getModel().isPressed()) {
                currentColor = DARK_BURGUNDY;
            } else if (getModel().isRollover()) {
                currentColor = buttonColor.brighter();
            } else {
                currentColor = buttonColor;
            }

            g2.setColor(currentColor);

            g2.fillRoundRect(
                    0, 0,
                    getWidth(),
                    getHeight(),
                    18, 18
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // BOOKSHELF BACKGROUND

    private class ShelfPanel extends JPanel {

        ShelfPanel() {
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int w = getWidth();
            int h = getHeight();

            GradientPaint background = new GradientPaint(
                    0, 0, new Color(65, 34, 21),
                    w, h, new Color(29, 18, 15)
            );

            g2.setPaint(background);
            g2.fillRect(0, 0, w, h);

            g2.setColor(new Color(93, 48, 26));
            g2.fillRect(25, 0, w - 50, h);

            int shelfHeight = Math.max(125, h / 4);

            Color[] bookColors = {
                    new Color(115, 38, 29),
                    new Color(165, 107, 48),
                    new Color(54, 67, 45),
                    new Color(82, 45, 34),
                    new Color(142, 91, 52),
                    new Color(72, 35, 47),
                    new Color(190, 145, 76)
            };

            for (int row = 0; row < 4; row++) {

                int y = row * shelfHeight + 35;

                if (y > h - 45) {
                    break;
                }

                g2.setColor(new Color(48, 25, 17));
                g2.fillRect(
                        35, y,
                        w - 70,
                        shelfHeight - 15
                );

                int x = 45;

                while (x < w - 65) {

                    int bookWidth = 17 + (x * 7 % 19);
                    int bookHeight = 65 + (x * 11 % 55);

                    int bookY = y + shelfHeight - bookHeight - 22;

                    g2.setColor(
                            bookColors[(x / 17) % bookColors.length]
                    );

                    g2.fillRoundRect(
                            x, bookY,
                            bookWidth, bookHeight,
                            4, 4
                    );

                    g2.setColor(
                            new Color(220, 180, 105, 120)
                    );

                    g2.drawLine(
                            x + 4, bookY + 10,
                            x + 4, bookY + bookHeight - 10
                    );

                    x += bookWidth + 4;
                }

                g2.setColor(new Color(135, 78, 39));
                g2.fillRect(
                        25,
                        y + shelfHeight - 20,
                        w - 50,
                        15
                );

                g2.setColor(new Color(185, 120, 60));
                g2.fillRect(
                        25,
                        y + shelfHeight - 20,
                        w - 50,
                        3
                );
            }

            GradientPaint overlay = new GradientPaint(
                    0, 0, new Color(15, 10, 8, 100),
                    0, h, new Color(15, 10, 8, 190)
            );

            g2.setPaint(overlay);
            g2.fillRect(0, 0, w, h);

            g2.dispose();
        }
    }

    // MAIN METHOD

    public static void main(String[] args) {

        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {

            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);

        });
    }
}

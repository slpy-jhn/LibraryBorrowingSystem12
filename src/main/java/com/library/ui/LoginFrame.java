package com.library.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.library.service.AuthService;
import com.library.service.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton showPasswordButton;

    private JPanel formContainer;
    private RoundedButton loginButton;
    private String selectedRole = "Student";
    private boolean passwordVisible = false;

    private static final int FIELD_WIDTH = 380;
    private static final int FIELD_HEIGHT = 50;

    private static final Color BURGUNDY = new Color(105, 24, 43);
    private static final Color DARK_BURGUNDY = new Color(65, 15, 29);
    private static final Color GOLD = new Color(204, 164, 87);
    private static final Color LIGHT_GOLD = new Color(235, 207, 151);
    private static final Color WHITE = new Color(255, 255, 255);
    private static final Color MUTED = new Color(190, 185, 185);
    private static final Color GLASS = new Color(32, 23, 27, 225);

    public LoginFrame() {
        setTitle("LibraryLink - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        setContentPane(createMainPanel());
    }

    private JPanel createMainPanel() {
        LibraryBackground background = new LibraryBackground();
        background.setLayout(new GridBagLayout());

        background.add(createGlassCard());

        return background;
    }

    private JPanel createGlassCard() {
        RoundedPanel card = new RoundedPanel(24, GLASS);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 55, 25, 55));
        card.setPreferredSize(new Dimension(500, 730));
        card.setMaximumSize(new Dimension(500, 730));

        // Logo badge
        JPanel logoBadge = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(BURGUNDY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        logoBadge.setOpaque(false);
        logoBadge.setPreferredSize(new Dimension(62, 62));
        logoBadge.setMaximumSize(new Dimension(62, 62));
        logoBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel logoIcon = new JLabel(new LibraryLogoIcon(WHITE));
        logoBadge.add(logoIcon);

        card.add(logoBadge);
        card.add(Box.createVerticalStrut(12));

        JLabel title = new JLabel("LibraryLink");
        title.setFont(new Font("Serif", Font.BOLD, 29));
        title.setForeground(WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);

        JLabel tagline = new JLabel("Your gateway to knowledge");
        tagline.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tagline.setForeground(MUTED);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(tagline);

        card.add(Box.createVerticalStrut(25));

        // Role selector
        JPanel rolePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        rolePanel.setOpaque(false);
        rolePanel.setMaximumSize(new Dimension(FIELD_WIDTH, 60));
        rolePanel.setPreferredSize(new Dimension(FIELD_WIDTH, 60));
        rolePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        RoundedButton memberButton = new RoundedButton(
                "MEMBER", new RoleIcon(RoleIcon.GRADUATION, WHITE),
                selectedRole.equals("Student")
        );

        RoundedButton staffButton = new RoundedButton(
                "STAFF", new RoleIcon(RoleIcon.STAFF, WHITE),
                selectedRole.equals("Admin")
        );

        memberButton.addActionListener(e -> {
            selectedRole = "Student";
            updateRoleButtons(memberButton, staffButton);
        });

        staffButton.addActionListener(e -> {
            selectedRole = "Admin";
            updateRoleButtons(memberButton, staffButton);
        });

        rolePanel.add(memberButton);
        rolePanel.add(staffButton);
        card.add(rolePanel);

        card.add(Box.createVerticalStrut(17));

        // Contextual information
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 7, 0));
        infoPanel.setOpaque(false);
        infoPanel.setMaximumSize(new Dimension(FIELD_WIDTH, 25));

        JLabel infoIcon = new JLabel(new SmallIcon(SmallIcon.INFO, LIGHT_GOLD));
        JLabel infoText = new JLabel(
                "Please enter your registered email address"
        );
        infoText.setFont(new Font("SansSerif", Font.PLAIN, 10));
        infoText.setForeground(MUTED);

        infoPanel.add(infoIcon);
        infoPanel.add(infoText);
        card.add(Box.createVerticalStrut(20));

        formContainer = new JPanel();
        formContainer.setOpaque(false);
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setPreferredSize(new Dimension(FIELD_WIDTH, 300));
        formContainer.setMinimumSize(new Dimension(FIELD_WIDTH, 300));
        formContainer.setMaximumSize(new Dimension(FIELD_WIDTH, 300));
        formContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(formContainer);

        buildLoginForm();

        card.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("© 2026 LibraryLink | Knowledge starts here.");
        footer.setFont(new Font("SansSerif", Font.PLAIN, 10));
        footer.setForeground(MUTED);
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(footer);

        return card;
    }

    private void updateRoleButtons(
            RoundedButton memberButton,
            RoundedButton staffButton
    ) {
        memberButton.setSelectedRole(selectedRole.equals("Student"));
        staffButton.setSelectedRole(selectedRole.equals("Admin"));

        memberButton.repaint();
        staffButton.repaint();
    }


    private void buildLoginForm() {
        formContainer.removeAll();

        JLabel emailLabel = createFieldLabel("Username");
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(emailLabel);

        usernameField = new JTextField();
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        usernameField.setForeground(WHITE);
        usernameField.setCaretColor(WHITE);
        usernameField.setOpaque(false);
        usernameField.setBorder(null);
        usernameField.setText("member@librarylink.edu");
        usernameField.setForeground(MUTED);

        usernameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (usernameField.getText().equals("member@librarylink.edu")) {
                    usernameField.setText("");
                    usernameField.setForeground(WHITE);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (usernameField.getText().isEmpty()) {
                    usernameField.setText("member@librarylink.edu");
                    usernameField.setForeground(MUTED);
                }
            }
        });
        usernameField.setForeground(MUTED);

        JPanel emailInput = createInputPanel(
                new SmallIcon(SmallIcon.ENVELOPE, MUTED),
                usernameField,
                null
        );

        formContainer.add(emailInput);
        formContainer.add(Box.createVerticalStrut(18));

        JLabel passwordLabel = createFieldLabel("Password");
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField.setForeground(WHITE);
        passwordField.setCaretColor(WHITE);
        passwordField.setOpaque(false);
        passwordField.setBorder(null);
        passwordField.setToolTipText("Enter your password");

        showPasswordButton = new JButton(
                new SmallIcon(SmallIcon.EYE, MUTED)
        );
        showPasswordButton.setBorder(null);
        showPasswordButton.setContentAreaFilled(false);
        showPasswordButton.setFocusPainted(false);
        showPasswordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPasswordButton.setPreferredSize(new Dimension(28, 28));

        showPasswordButton.addActionListener(e -> togglePasswordVisibility());

        JPanel passwordInput = createInputPanel(
                new SmallIcon(SmallIcon.LOCK, MUTED),
                passwordField,
                showPasswordButton
        );

        formContainer.add(passwordInput);
        formContainer.add(Box.createVerticalStrut(25));

        loginButton = new RoundedButton(
                "LOG IN",
                null,
                true
        );
        loginButton.setPreferredSize(new Dimension(FIELD_WIDTH, 70));
        loginButton.setMaximumSize(new Dimension(FIELD_WIDTH, 56));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginButton.addActionListener(e -> login());

        formContainer.add(loginButton);
        formContainer.add(Box.createVerticalStrut(12));

        formContainer.revalidate();
        formContainer.repaint();
    }


    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(WHITE);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setPreferredSize(new Dimension(FIELD_WIDTH, 20));
        label.setMaximumSize(new Dimension(FIELD_WIDTH, 20));
        return label;
    }

    private JPanel createInputPanel(
            Icon leftIcon,
            JComponent inputField,
            JButton rightButton
    ) {
        RoundedPanel panel = new RoundedPanel(
                12,
                new Color(255, 255, 255, 15)
        );

        panel.setLayout(new BorderLayout(12, 0));
        panel.setBorder(new EmptyBorder(0, 16, 0, 16));
        panel.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        panel.setMaximumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        panel.setMinimumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLabel = new JLabel(leftIcon);
        panel.add(iconLabel, BorderLayout.WEST);

        panel.add(inputField, BorderLayout.CENTER);

        if (rightButton != null) {
            panel.add(rightButton, BorderLayout.EAST);
        }

        return panel;
    }

    private void togglePasswordVisibility() {
        if (passwordField == null) {
            return;
        }

        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            passwordField.setEchoChar((char) 0);
            showPasswordButton.setIcon(
                    new SmallIcon(SmallIcon.EYE_OFF, MUTED)
            );
        } else {
            passwordField.setEchoChar('•');
            showPasswordButton.setIcon(
                    new SmallIcon(SmallIcon.EYE, MUTED)
            );
        }
    }

    private void login() {
        String username = usernameField.getText().trim();

        if (username.equals("member@librarylink.edu")) {
            username = "";
        }
        char[] password = passwordField.getPassword();

        if (username.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter your username and password.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            var result = AuthService.authenticate(username, password);

            if (result != null) {

                // Check whether the account matches the selected login type
                boolean roleMatches;

                if (selectedRole.equals("Student")) {
                    roleMatches = "STUDENT".equalsIgnoreCase(result.role());
                } else {
                    roleMatches = "ADMIN".equalsIgnoreCase(result.role());
                }

                // Reject accounts using the wrong login option
                if (!roleMatches) {

                    JOptionPane.showMessageDialog(
                            this,
                            selectedRole.equals("Student")
                                    ? "This account is not registered as a Member. Please use Staff Login."
                                    : "This account is not authorized for Staff Login. Please use Member Login.",
                            "Access Denied",
                            JOptionPane.WARNING_MESSAGE
                    );

                    return;
                }

                UserSession session = new UserSession(
                        result.username(),
                        result.role(),
                        result.memberId()
                );

                if (session.isAdmin()) {
                    new AdminDashboard(session).setVisible(true);
                } else if (session.isStudent()) {
                    new StudentDashboard(session).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Your account has an unrecognized role.",
                            "Login Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                dispose();

            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid username or password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "An error occurred while logging in:\n" + ex.getMessage(),
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();

        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    // Rounded panel
    private static class RoundedPanel extends JPanel {

        private final int radius;
        private final Color backgroundColor;

        public RoundedPanel(int radius, Color backgroundColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(backgroundColor);
            g2.fillRoundRect(
                    0, 0,
                    getWidth(), getHeight(),
                    radius, radius
            );

            if (backgroundColor.equals(GLASS)) {
                g2.setColor(new Color(255, 255, 255, 35));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(
                        0, 0,
                        getWidth() - 1, getHeight() - 1,
                        radius, radius
                );
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Rounded buttons
    private static class RoundedButton extends JButton {

        private boolean selectedRole;
        private final Icon buttonIcon;

        public RoundedButton(String text, Icon icon, boolean selected) {
            super(text);

            this.selectedRole = selected;
            this.buttonIcon = icon;

            setFont(new Font("SansSerif", Font.BOLD, 12));
            setForeground(WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(FIELD_WIDTH, 60));
            setMaximumSize(new Dimension(FIELD_WIDTH, 60));

            if (icon != null) {
                setIcon(icon);
                setIconTextGap(8);
            }
        }

        public void setSelectedRole(boolean selected) {
            this.selectedRole = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            if (selectedRole) {
                g2.setColor(BURGUNDY);
            } else {
                g2.setColor(new Color(255, 255, 255, 15));
            }

            g2.fillRoundRect(
                    0, 0,
                    getWidth(), getHeight(),
                    12, 12
            );

            g2.setColor(selectedRole ? GOLD : new Color(255, 255, 255, 45));
            g2.setStroke(new BasicStroke(1f));

            g2.drawRoundRect(
                    0, 0,
                    getWidth() - 1, getHeight() - 1,
                    12, 12
            );

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Background image with dark overlay
    private static class LibraryBackground extends JPanel {

        private Image backgroundImage;

        public LibraryBackground() {
            URL imageURL = getClass().getResource(
                    "/images/library-background.jpg"
            );

            if (imageURL != null) {
                backgroundImage = new ImageIcon(imageURL).getImage();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );

            int width = getWidth();
            int height = getHeight();

            if (backgroundImage != null) {
                double imageWidth = backgroundImage.getWidth(this);
                double imageHeight = backgroundImage.getHeight(this);

                double scale = Math.max(
                        width / imageWidth,
                        height / imageHeight
                );

                int scaledWidth = (int) (imageWidth * scale);
                int scaledHeight = (int) (imageHeight * scale);

                int x = (width - scaledWidth) / 2;
                int y = (height - scaledHeight) / 2;

                g2.drawImage(
                        backgroundImage,
                        x, y,
                        scaledWidth, scaledHeight,
                        this
                );
            } else {
                g2.setColor(DARK_BURGUNDY);
                g2.fillRect(0, 0, width, height);
            }

            // Dark overlay
            g2.setColor(new Color(15, 8, 12, 165));
            g2.fillRect(0, 0, width, height);

            // Vignette effect
            g2.setPaint(new RadialGradientPaint(
                    new Point(width / 2, height / 2),
                    Math.max(width, height) * 0.75f,
                    new float[]{0f, 1f},
                    new Color[]{
                            new Color(0, 0, 0, 0),
                            new Color(0, 0, 0, 150)
                    }
            ));

            g2.fillRect(0, 0, width, height);
            g2.dispose();
        }
    }

    // Logo icon
    private static class LibraryLogoIcon implements Icon {

        private final Color color;

        public LibraryLogoIcon(Color color) {
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return 38;
        }

        @Override
        public int getIconHeight() {
            return 38;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(color);
            g2.setStroke(new BasicStroke(
                    2f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            // Open book
            g2.drawLine(x + 5, y + 13, x + 18, y + 17);
            g2.drawLine(x + 18, y + 17, x + 18, y + 31);
            g2.drawLine(x + 18, y + 17, x + 32, y + 13);
            g2.drawLine(x + 32, y + 13, x + 32, y + 29);
            g2.drawLine(x + 5, y + 13, x + 5, y + 29);
            g2.drawLine(x + 5, y + 29, x + 18, y + 33);
            g2.drawLine(x + 18, y + 33, x + 32, y + 29);

            // Graduation cap
            Polygon cap = new Polygon();
            cap.addPoint(x + 7, y + 8);
            cap.addPoint(x + 18, y + 3);
            cap.addPoint(x + 30, y + 8);
            cap.addPoint(x + 18, y + 13);

            g2.fillPolygon(cap);
            g2.drawLine(x + 29, y + 9, x + 29, y + 15);

            g2.dispose();
        }
    }

    // Small outline icons
    private static class SmallIcon implements Icon {

        static final int INFO = 0;
        static final int ENVELOPE = 1;
        static final int LOCK = 2;
        static final int EYE = 3;
        static final int EYE_OFF = 4;

        private final int type;
        private final Color color;

        public SmallIcon(int type, Color color) {
            this.type = type;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return 19;
        }

        @Override
        public int getIconHeight() {
            return 19;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(color);
            g2.setStroke(new BasicStroke(
                    1.6f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            switch (type) {
                case INFO:
                    g2.drawOval(x + 2, y + 2, 14, 14);
                    g2.drawLine(x + 9, y + 8, x + 9, y + 13);
                    g2.fillOval(x + 8, y + 5, 2, 2);
                    break;

                case ENVELOPE:
                    g2.drawRoundRect(x + 1, y + 3, 17, 13, 2, 2);
                    g2.drawLine(x + 2, y + 4, x + 9, y + 10);
                    g2.drawLine(x + 17, y + 4, x + 9, y + 10);
                    break;

                case LOCK:
                    g2.drawRoundRect(x + 3, y + 8, 13, 9, 2, 2);
                    g2.drawArc(x + 5, y + 1, 9, 14, 0, 180);
                    g2.drawLine(x + 9, y + 11, x + 9, y + 14);
                    break;

                case EYE:
                case EYE_OFF:
                    g2.drawArc(x + 1, y + 4, 17, 11, 0, 180);
                    g2.drawArc(x + 1, y + 4, 17, 11, 180, 180);
                    g2.drawOval(x + 7, y + 6, 5, 5);

                    if (type == EYE_OFF) {
                        g2.drawLine(x + 2, y + 2, x + 17, y + 17);
                    }
                    break;
            }

            g2.dispose();
        }
    }

    // Role selector icons
    private static class RoleIcon implements Icon {

        static final int GRADUATION = 0;
        static final int STAFF = 1;

        private final int type;
        private final Color color;

        public RoleIcon(int type, Color color) {
            this.type = type;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(color);
            g2.setStroke(new BasicStroke(
                    1.5f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            if (type == GRADUATION) {
                Polygon cap = new Polygon();
                cap.addPoint(x + 1, y + 7);
                cap.addPoint(x + 10, y + 2);
                cap.addPoint(x + 19, y + 7);
                cap.addPoint(x + 10, y + 12);

                g2.drawPolygon(cap);
                g2.drawLine(x + 5, y + 10, x + 5, y + 15);
                g2.drawArc(x + 5, y + 9, 10, 8, 180, 180);
                g2.drawLine(x + 18, y + 8, x + 18, y + 15);
            } else {
                // User outline
                g2.drawOval(x + 7, y + 1, 7, 7);
                g2.drawArc(x + 4, y + 8, 13, 10, 0, 180);

                // Shield
                Polygon shield = new Polygon();
                shield.addPoint(x + 15, y + 8);
                shield.addPoint(x + 19, y + 10);
                shield.addPoint(x + 18, y + 16);
                shield.addPoint(x + 15, y + 19);
                shield.addPoint(x + 12, y + 16);
                shield.addPoint(x + 11, y + 10);

                g2.drawPolygon(shield);
            }

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();

            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}
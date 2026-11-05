
package com.library.ui;

import com.library.service.StudentDashboardService;
import com.library.service.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

public class StudentDashboard extends JFrame {

    // Vintage color palette
    private final Color BURGUNDY = new Color(75, 20, 36);
    private final Color DARK_BURGUNDY = new Color(55, 13, 27);
    private final Color WINE = new Color(112, 43, 59);
    private final Color GOLD = new Color(176, 141, 87);
    private final Color CREAM = new Color(243, 235, 221);
    private final Color WHITE = new Color(255, 252, 245);
    private final Color TEXT_DARK = new Color(53, 37, 31);
    private final Color TEXT_GRAY = new Color(130, 115, 103);

    private final UserSession session;
    private final int memberId;

    private JLabel borrowedValue;
    private JLabel overdueValue;
    private JLabel returnedValue;

    public StudentDashboard(UserSession session) {

        if (session == null) {
            throw new IllegalArgumentException(
                    "A valid user session is required."
            );
        }

        this.session = session;
        this.memberId = session.requireStudent();

        setTitle("LibraryLink - Student Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(1100, 760);
        setMinimumSize(new Dimension(1100, 760));
        setResizable(true);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(session.getUsername()), BorderLayout.CENTER);

        loadStudentStats();
    }

    // =========================
    // SIDEBAR
    // =========================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel(new BorderLayout()) {

            @Override
            protected void paintComponent(Graphics g) {

                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                GradientPaint gradient = new GradientPaint(
                        0, 0, BURGUNDY,
                        getWidth(), getHeight(), WINE
                );

                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.dispose();
            }
        };

        sidebar.setPreferredSize(new Dimension(260, 760));
        sidebar.setOpaque(false);

        // Top section
        JPanel topSection = new JPanel();
        topSection.setOpaque(false);
        topSection.setLayout(
                new BoxLayout(topSection, BoxLayout.Y_AXIS)
        );

        topSection.setBorder(
                new EmptyBorder(30, 20, 15, 20)
        );

        // Logo and brand
        JPanel logoRow = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0)
        );
        logoRow.setOpaque(false);
        logoRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logo = new JLabel("L");
        logo.setFont(new Font("Georgia", Font.BOLD, 34));
        logo.setForeground(GOLD);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setPreferredSize(new Dimension(48, 48));
        logo.setBorder(
                BorderFactory.createLineBorder(GOLD, 2)
        );

        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(
                new BoxLayout(brandText, BoxLayout.Y_AXIS)
        );
        brandText.setBorder(
                new EmptyBorder(3, 12, 0, 0)
        );

        JLabel brand = new JLabel("LibraryLink");
        brand.setFont(new Font("Georgia", Font.BOLD, 23));
        brand.setForeground(WHITE);

        JLabel subtitle = new JLabel("STUDENT PORTAL");
        subtitle.setFont(new Font("Palatino Linotype", Font.BOLD, 10));
        subtitle.setForeground(GOLD);

        brandText.add(brand);
        brandText.add(Box.createVerticalStrut(5));
        brandText.add(subtitle);

        logoRow.add(logo);
        logoRow.add(brandText);

        topSection.add(logoRow);
        topSection.add(Box.createVerticalStrut(42));

        // Menu heading
        JLabel menuHeading = new JLabel("STUDENT MENU");
        menuHeading.setFont(
                new Font("Palatino Linotype", Font.BOLD, 11)
        );
        menuHeading.setForeground(new Color(224, 190, 164));
        menuHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        topSection.add(menuHeading);
        topSection.add(Box.createVerticalStrut(15));

        // Navigation items
        String[] menuItems = {
                "Dashboard",
                "Browse Books",
                "My Borrowed Books",
                "My Borrowing History",
                "My Borrowing Requests",
                "My Return Requests",
                "My Account"
        };

        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(
                new BoxLayout(menuPanel, BoxLayout.Y_AXIS)
        );

        for (String item : menuItems) {

            boolean isActive = item.equals("Dashboard");

            RoundedMenuButton button =
                    new RoundedMenuButton(item, isActive);

            button.addActionListener(e -> {

                int authenticatedMemberId =
                        session.requireStudent();

                switch (item) {

                    case "Dashboard" ->
                            loadStudentStats();

                    case "Browse Books" ->
                            openFrame(new StudentBooksFrame(session));

                    case "My Borrowed Books" ->
                            openFrame(new MyBorrowedBooksFrame(session));

                    case "My Borrowing History" ->
                            openFrame(
                                    new MyBorrowingHistoryFrame(
                                            authenticatedMemberId
                                    )
                            );

                    case "My Borrowing Requests" ->
                            openFrame(new MyBorrowRequestsFrame(session));

                    case "My Return Requests" ->
                            openFrame(new MyReturnRequestsFrame(session));

                    case "My Account" ->
                            openFrame(
                                    new MyAccountFrame(
                                            authenticatedMemberId
                                    )
                            );

                    default -> JOptionPane.showMessageDialog(
                            this,
                            "Section is not available yet.",
                            "Information",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            });

            menuPanel.add(button);
            menuPanel.add(Box.createVerticalStrut(7));
        }

        topSection.add(menuPanel);

        sidebar.add(topSection, BorderLayout.NORTH);

        // Logout section
        JPanel bottomSection = new JPanel();
        bottomSection.setOpaque(false);
        bottomSection.setLayout(
                new BoxLayout(bottomSection, BoxLayout.Y_AXIS)
        );
        bottomSection.setBorder(
                new EmptyBorder(10, 20, 28, 20)
        );

        JSeparator separator = new JSeparator();
        separator.setForeground(GOLD);
        separator.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 1)
        );

        JButton logoutButton = new JButton("Log Out");
        logoutButton.setFont(
                new Font("Palatino Linotype", Font.BOLD, 14)
        );
        logoutButton.setForeground(WHITE);
        logoutButton.setBackground(DARK_BURGUNDY);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(
                new EmptyBorder(13, 15, 13, 15)
        );
        logoutButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );
        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 48)
        );

        logoutButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                logoutButton.setBackground(WINE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutButton.setBackground(DARK_BURGUNDY);
            }
        });

        logoutButton.addActionListener(e -> {

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        bottomSection.add(separator);
        bottomSection.add(Box.createVerticalStrut(18));
        bottomSection.add(logoutButton);

        sidebar.add(bottomSection, BorderLayout.SOUTH);

        return sidebar;
    }

    // =========================
    // MAIN DASHBOARD CONTENT
    // =========================

    private JPanel createMainContent(String username) {

        JPanel main = new JPanel(new BorderLayout(0, 28));
        main.setBackground(CREAM);
        main.setBorder(
                new EmptyBorder(38, 38, 35, 38)
        );

        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(
                new BoxLayout(header, BoxLayout.Y_AXIS)
        );

        JLabel heading = new JLabel("Student Dashboard");
        heading.setFont(
                new Font("Georgia", Font.BOLD, 39)
        );
        heading.setForeground(BURGUNDY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel welcome = new JLabel(
                "Welcome back, " + username + "!"
        );
        welcome.setFont(
                new Font("Book Antiqua", Font.PLAIN, 17)
        );
        welcome.setForeground(TEXT_GRAY);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel goldLine = new JPanel();
        goldLine.setBackground(GOLD);
        goldLine.setPreferredSize(new Dimension(65, 4));
        goldLine.setMaximumSize(new Dimension(65, 4));
        goldLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(heading);
        header.add(Box.createVerticalStrut(9));
        header.add(welcome);
        header.add(Box.createVerticalStrut(18));
        header.add(goldLine);

        // Statistics cards
        JPanel statsPanel = new JPanel(
                new GridLayout(1, 3, 18, 0)
        );
        statsPanel.setOpaque(false);

        statsPanel.add(createStatCard(
                "CURRENTLY BORROWED",
                "0",
                "Books in your possession",
                1
        ));

        statsPanel.add(createStatCard(
                "OVERDUE BOOKS",
                "0",
                "Books past their due date",
                2
        ));

        statsPanel.add(createStatCard(
                "RETURNED BOOKS",
                "0",
                "Previously returned books",
                3
        ));

        // Welcome information card
        RoundedPanel infoPanel = new RoundedPanel(
                WHITE,
                18
        );

        infoPanel.setLayout(
                new BorderLayout(0, 18)
        );

        infoPanel.setBorder(
                new EmptyBorder(28, 30, 28, 30)
        );

        JLabel infoTitle = new JLabel(
                "Welcome to LibraryLink"
        );
        infoTitle.setFont(
                new Font("Georgia", Font.BOLD, 24)
        );
        infoTitle.setForeground(BURGUNDY);

        JLabel infoDescription = new JLabel(
                "Your personal library portal makes it easier to "
                        + "access and manage your library activities."
        );

        infoDescription.setFont(
                new Font("Book Antiqua", Font.PLAIN, 15)
        );
        infoDescription.setForeground(TEXT_GRAY);

        JLabel features = new JLabel(
                "<html>"
                        + "<div style='line-height:1.8;'>"
                        + "&#8226;&nbsp; Browse books available in the library.<br>"
                        + "&#8226;&nbsp; Check your borrowed and overdue books.<br>"
                        + "&#8226;&nbsp; Submit and monitor borrowing requests.<br>"
                        + "&#8226;&nbsp; Track your return requests and borrowing history.<br>"
                        + "&#8226;&nbsp; View and manage your account information."
                        + "</div>"
                        + "</html>"
        );

        features.setFont(
                new Font("Book Antiqua", Font.PLAIN, 15)
        );
        features.setForeground(TEXT_DARK);

        JPanel infoContent = new JPanel();
        infoContent.setOpaque(false);
        infoContent.setLayout(
                new BoxLayout(infoContent, BoxLayout.Y_AXIS)
        );

        infoContent.add(infoDescription);
        infoContent.add(Box.createVerticalStrut(15));
        infoContent.add(features);

        infoPanel.add(infoTitle, BorderLayout.NORTH);
        infoPanel.add(infoContent, BorderLayout.CENTER);

        // Center content
        JPanel center = new JPanel(
                new BorderLayout(0, 25)
        );
        center.setOpaque(false);

        center.add(statsPanel, BorderLayout.NORTH);
        center.add(infoPanel, BorderLayout.CENTER);

        main.add(header, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);

        return main;
    }

    // =========================
    // STATISTICS CARDS
    // =========================

    private JPanel createStatCard(
            String title,
            String value,
            String description,
            int cardNumber
    ) {

        RoundedPanel card = new RoundedPanel(
                WHITE,
                16
        );

        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );

        card.setBorder(
                new EmptyBorder(22, 22, 22, 22)
        );

        // Gold accent
        JPanel accent = new JPanel();
        accent.setBackground(GOLD);
        accent.setPreferredSize(new Dimension(42, 4));
        accent.setMaximumSize(new Dimension(42, 4));
        accent.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(
                new Font("Book Antiqua", Font.BOLD, 13)
        );
        titleLabel.setForeground(TEXT_GRAY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(
                new Font("Georgia", Font.BOLD, 42)
        );
        valueLabel.setForeground(BURGUNDY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(
                new Font("Book Antiqua", Font.PLAIN, 13)
        );
        descriptionLabel.setForeground(TEXT_GRAY);
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        switch (cardNumber) {
            case 1 -> borrowedValue = valueLabel;
            case 2 -> overdueValue = valueLabel;
            case 3 -> returnedValue = valueLabel;
        }

        card.add(accent);
        card.add(Box.createVerticalStrut(18));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(descriptionLabel);

        return card;
    }

    // =========================
    // LOAD STUDENT STATISTICS
    // =========================

    private void loadStudentStats() {

        try {

            int authenticatedMemberId =
                    session.requireStudent();

            StudentDashboardService.StudentStats stats =
                    StudentDashboardService.getStudentStats(
                            authenticatedMemberId
                    );

            borrowedValue.setText(
                    String.valueOf(stats.currentlyBorrowed())
            );

            overdueValue.setText(
                    String.valueOf(stats.overdueBooks())
            );

            returnedValue.setText(
                    String.valueOf(stats.returnedBooks())
            );

        } catch (SQLException |
                 IllegalArgumentException |
                 SecurityException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load student dashboard:\n"
                            + e.getMessage(),
                    "Dashboard Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    // =========================
    // OPEN OTHER WINDOWS
    // =========================

    private void openFrame(JFrame frame) {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    // =========================
    // ROUNDED PANEL
    // =========================

    private class RoundedPanel extends JPanel {

        private final Color backgroundColor;
        private final int cornerRadius;

        public RoundedPanel(
                Color backgroundColor,
                int cornerRadius
        ) {
            this.backgroundColor = backgroundColor;
            this.cornerRadius = cornerRadius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            // Soft shadow
            g2.setColor(new Color(70, 45, 30, 18));

            g2.fillRoundRect(
                    3,
                    4,
                    getWidth() - 6,
                    getHeight() - 6,
                    cornerRadius,
                    cornerRadius
            );

            // Ivory card background
            g2.setColor(backgroundColor);

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth() - 4,
                    getHeight() - 5,
                    cornerRadius,
                    cornerRadius
            );

            // Antique gold border
            g2.setColor(new Color(176, 141, 87, 130));
            g2.setStroke(new BasicStroke(1f));

            g2.drawRoundRect(
                    0,
                    0,
                    getWidth() - 4,
                    getHeight() - 5,
                    cornerRadius,
                    cornerRadius
            );

            g2.dispose();
        }
    }

    // =========================
    // SIDEBAR MENU BUTTON
    // =========================

    private class RoundedMenuButton extends JButton {

        private boolean hovered = false;
        private final boolean active;

        public RoundedMenuButton(
                String text,
                boolean active
        ) {

            super(text);
            this.active = active;

            setFont(
                    new Font("Palatino Linotype", Font.PLAIN, 14)
            );

            setForeground(WHITE);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);

            setCursor(
                    new Cursor(Cursor.HAND_CURSOR)
            );

            setBorder(
                    new EmptyBorder(12, 18, 12, 10)
            );

            setAlignmentX(Component.LEFT_ALIGNMENT);

            setPreferredSize(new Dimension(220, 44));
            setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 44)
            );

            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            // Active and hover background
            if (active) {
                g2.setColor(new Color(255, 252, 245, 35));

                g2.fillRoundRect(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        8,
                        8
                );

            } else if (hovered) {
                g2.setColor(new Color(255, 252, 245, 20));

                g2.fillRoundRect(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        8,
                        8
                );
            }

            // Gold accent indicator
            if (active) {
                g2.setColor(GOLD);

                g2.fillRoundRect(
                        0,
                        7,
                        4,
                        getHeight() - 14,
                        4,
                        4
                );
            }

            g2.dispose();
        }
    }
}

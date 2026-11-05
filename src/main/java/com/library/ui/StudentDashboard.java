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

    private final Color BURGUNDY = new Color(91, 28, 43);
    private final Color DARK_BURGUNDY = new Color(65, 18, 31);
    private final Color GOLD = new Color(205, 164, 82);
    private final Color CREAM = new Color(250, 247, 240);
    private final Color WHITE = Color.WHITE;
    private final Color TEXT_DARK = new Color(55, 45, 43);
    private final Color TEXT_GRAY = new Color(125, 116, 110);

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

        this.memberId = session.requireStudent();
        this.session = session;

        setTitle("LibraryLink - Student Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(session.getUsername()), BorderLayout.CENTER);

        loadStudentStats();
    }

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setBackground(BURGUNDY);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setLayout(new BorderLayout());

        JPanel topSection = new JPanel();
        topSection.setOpaque(false);
        topSection.setLayout(
                new BoxLayout(topSection, BoxLayout.Y_AXIS)
        );
        topSection.setBorder(
                new EmptyBorder(28, 20, 15, 20)
        );

        // LibraryLink logo
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        logoRow.setOpaque(false);
        logoRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logo = new JLabel("L");
        logo.setFont(new Font("Serif", Font.BOLD, 35));
        logo.setForeground(GOLD);
        logo.setPreferredSize(new Dimension(48, 48));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setBorder(
                BorderFactory.createLineBorder(GOLD, 2)
        );

        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(
                new BoxLayout(brandText, BoxLayout.Y_AXIS)
        );
        brandText.setBorder(new EmptyBorder(2, 12, 0, 0));

        JLabel brand = new JLabel("LibraryLink");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 21));
        brand.setForeground(WHITE);

        JLabel subtitle = new JLabel("STUDENT PORTAL");
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        subtitle.setForeground(GOLD);

        brandText.add(brand);
        brandText.add(Box.createVerticalStrut(4));
        brandText.add(subtitle);

        logoRow.add(logo);
        logoRow.add(brandText);

        topSection.add(logoRow);
        topSection.add(Box.createVerticalStrut(35));

        JLabel menuHeading = new JLabel("STUDENT MENU");
        menuHeading.setFont(
                new Font("Segoe UI", Font.BOLD, 10)
        );
        menuHeading.setForeground(new Color(220, 190, 155));
        menuHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        topSection.add(menuHeading);
        topSection.add(Box.createVerticalStrut(14));

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

            RoundedMenuButton button =
                    new RoundedMenuButton(item);

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
                new EmptyBorder(10, 18, 25, 18)
        );

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(130, 75, 82));
        separator.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 1)
        );

        JButton logoutButton = new JButton("Log Out");
        logoutButton.setFont(
                new Font("Bookman Old Style", Font.BOLD, 14)
        );
        logoutButton.setForeground(WHITE);
        logoutButton.setBackground(DARK_BURGUNDY);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(
                new EmptyBorder(12, 15, 12, 15)
        );
        logoutButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );
        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 46)
        );

        logoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutButton.setBackground(
                        new Color(115, 37, 52)
                );
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

    private JPanel createMainContent(String username) {

        JPanel main = new JPanel(new BorderLayout(20, 25));
        main.setBackground(CREAM);
        main.setBorder(
                new EmptyBorder(35, 40, 35, 40)
        );

        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(
                new BoxLayout(header, BoxLayout.Y_AXIS)
        );

        JLabel heading = new JLabel("Student Dashboard");
        heading.setFont(
                new Font("Segoe UI", Font.BOLD, 30)
        );
        heading.setForeground(BURGUNDY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel welcome = new JLabel(
                "Welcome back, " + username + "!"
        );
        welcome.setFont(
                new Font("Segoe UI", Font.PLAIN, 15)
        );
        welcome.setForeground(TEXT_GRAY);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(heading);
        header.add(Box.createVerticalStrut(7));
        header.add(welcome);

        // Gold divider
        JPanel goldLine = new JPanel();
        goldLine.setBackground(GOLD);
        goldLine.setPreferredSize(new Dimension(65, 3));
        goldLine.setMaximumSize(new Dimension(65, 3));
        goldLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(Box.createVerticalStrut(16));
        header.add(goldLine);

        // Statistics cards
        JPanel statsPanel = new JPanel(
                new GridLayout(1, 3, 20, 20)
        );
        statsPanel.setOpaque(false);

        statsPanel.add(createStatCard(
                "Currently Borrowed",
                "0",
                "Books in your possession",
                1
        ));

        statsPanel.add(createStatCard(
                "Overdue Books",
                "0",
                "Books past their due date",
                2
        ));

        statsPanel.add(createStatCard(
                "Returned Books",
                "0",
                "Previously returned books",
                3
        ));

        // Library information card
        RoundedPanel infoPanel = new RoundedPanel(
                WHITE,
                20
        );
        infoPanel.setLayout(
                new BorderLayout(10, 18)
        );
        infoPanel.setBorder(
                new EmptyBorder(30, 32, 30, 32)
        );

        JLabel infoTitle = new JLabel(
                "Welcome to LibraryLink"
        );
        infoTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 21)
        );
        infoTitle.setForeground(BURGUNDY);

        JLabel infoText = new JLabel(
                "<html>"
                        + "Your personal library portal gives you easy access "
                        + "to your library activities.<br><br>"
                        + "• Browse books available in the library.<br><br>"
                        + "• Check your currently borrowed and overdue books.<br><br>"
                        + "• Submit and monitor your borrowing requests.<br><br>"
                        + "• Track your return requests and borrowing history.<br><br>"
                        + "• View and manage your account information."
                        + "</html>"
        );

        infoText.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );
        infoText.setForeground(TEXT_DARK);

        infoPanel.add(infoTitle, BorderLayout.NORTH);
        infoPanel.add(infoText, BorderLayout.CENTER);

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

    private JPanel createStatCard(
            String title,
            String value,
            String description,
            int cardNumber
    ) {

        RoundedPanel card = new RoundedPanel(
                WHITE,
                18
        );

        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );

        card.setBorder(
                new EmptyBorder(23, 23, 23, 23)
        );

        // Gold accent line
        JPanel accent = new JPanel();
        accent.setBackground(GOLD);
        accent.setPreferredSize(new Dimension(45, 4));
        accent.setMaximumSize(new Dimension(45, 4));
        accent.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );
        titleLabel.setForeground(TEXT_GRAY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 36)
        );
        valueLabel.setForeground(BURGUNDY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
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
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(7));
        card.add(descriptionLabel);

        return card;
    }

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

    private void openFrame(JFrame frame) {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    // Rounded background panel
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

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(backgroundColor);

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    cornerRadius,
                    cornerRadius
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // Custom sidebar navigation button
    private class RoundedMenuButton extends JButton {

        private boolean hovered = false;
        private final String buttonText;

        public RoundedMenuButton(String text) {

            super(text);
            this.buttonText = text;

            setFont(
                    new Font("Bookman Old Style", Font.PLAIN, 13)
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

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            if (hovered) {
                g2.setColor(new Color(115, 43, 58));

                g2.fillRoundRect(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        12,
                        12
                );

                g2.setColor(GOLD);
                g2.fillRoundRect(
                        0,
                        8,
                        4,
                        getHeight() - 16,
                        4,
                        4
                );
            }

            g2.dispose();

            super.paintComponent(g);
        }
    }
}
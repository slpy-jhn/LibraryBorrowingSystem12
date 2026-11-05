package com.library.ui;

import com.library.service.DashboardService;
import com.library.service.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class AdminDashboard extends JFrame {

    // ==========================================
    // MODERN LIGHT THEME
    // ==========================================

    private final Color PRIMARY = new Color(75, 20, 36);
    private final Color PRIMARY_DARK = new Color(55, 13, 27);
    private final Color GOLD = new Color(176, 141, 87);

    private final Color BACKGROUND = new Color(243, 235, 221);
    private final Color WHITE = new Color(255, 252, 245);
    private final Color CARD_BACKGROUND = new Color(255, 252, 245);

    private final Color DARK_TEXT = new Color(53, 37, 31);
    private final Color MUTED = new Color(130, 115, 103);
    private final Color SOFT_BORDER = new Color(222, 210, 191);

    private final UserSession session;

    private JLabel totalBooksValue;
    private JLabel registeredMembersValue;
    private JLabel booksBorrowedValue;
    private JLabel overdueBooksValue;

    public AdminDashboard(UserSession session) {

        if (session == null) {
            throw new SecurityException("Admin session is required.");
        }

        session.requireAdmin();
        this.session = session;

        setTitle("LibraryLink - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(850, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(session.getUsername()), BorderLayout.CENTER);

        loadDashboardStats();
    }

    // ==========================================
    // SIDEBAR
    // ==========================================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(PRIMARY);
        sidebar.setPreferredSize(new Dimension(245, 0));

        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 1, 0, GOLD
                ),
                new EmptyBorder(25, 15, 20, 15)
        ));

        // BRAND

        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("L");
        logo.setFont(new Font("Georgia", Font.BOLD, 48));
        logo.setForeground(new Color(205, 170, 105));
        logo.setOpaque(false);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("LibraryLink");
        title.setFont(new Font("Georgia", Font.BOLD, 23));
        title.setForeground(WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("ADMINISTRATION");
        subtitle.setFont(new Font("Georgia", Font.BOLD, 10));
        subtitle.setForeground(GOLD);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandPanel.add(logo);
        brandPanel.add(Box.createVerticalStrut(2));
        brandPanel.add(title);
        brandPanel.add(Box.createVerticalStrut(5));
        brandPanel.add(subtitle);
        brandPanel.add(Box.createVerticalStrut(30));

        // NAVIGATION

        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        String[] menuItems = {
                "Dashboard",
                "Manage Books",
                "Manage Members",
                "Manage Student Accounts",
                "Manage Borrowing Requests",
                "Manage Return Requests",
                "Borrow Books",
                "Return Books",
                "Overdue Books",
                "Transaction Records"
        };

        for (String item : menuItems) {

            RoundedMenuButton button = new RoundedMenuButton(item);

            button.addActionListener(e -> {

                session.requireAdmin();

                switch (item) {

                    case "Manage Books" ->
                            openFrame(new ManageBooksFrame());

                    case "Manage Members" ->
                            openFrame(new ManageMembersFrame());

                    case "Manage Student Accounts" ->
                            openFrame(new ManageStudentAccountsFrame());

                    case "Manage Borrowing Requests" ->
                            openFrame(new ManageBorrowRequestsFrame(session));

                    case "Manage Return Requests" ->
                            openFrame(new ManageReturnRequestsFrame(session));

                    case "Borrow Books" ->
                            openFrame(new BorrowBooksFrame());

                    case "Return Books" ->
                            openFrame(new ReturnBooksFrame());

                    case "Overdue Books" ->
                            openFrame(new OverdueBooksFrame());

                    case "Transaction Records" ->
                            openFrame(new TransactionRecordsFrame());

                    case "Dashboard" ->
                            loadDashboardStats();

                    default -> JOptionPane.showMessageDialog(
                            this,
                            item + " section will be added in a later step.",
                            item,
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            });

            menuPanel.add(button);
            menuPanel.add(Box.createVerticalStrut(7));
        }

        JScrollPane menuScroll = new JScrollPane(menuPanel);

        menuScroll.setBorder(null);
        menuScroll.setOpaque(false);
        menuScroll.getViewport().setOpaque(false);

        menuScroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        menuScroll.getVerticalScrollBar().setUnitIncrement(16);
        menuScroll.getVerticalScrollBar().setPreferredSize(
                new Dimension(5, 0)
        );

        menuScroll.getVerticalScrollBar().setUI(
                new javax.swing.plaf.basic.BasicScrollBarUI() {

                    @Override
                    protected void configureScrollBarColors() {
                        thumbColor = new Color(210, 190, 165);
                        trackColor = WHITE;
                    }

                    @Override
                    protected JButton createDecreaseButton(int orientation) {
                        return createZeroButton();
                    }

                    @Override
                    protected JButton createIncreaseButton(int orientation) {
                        return createZeroButton();
                    }

                    private JButton createZeroButton() {
                        JButton button = new JButton();
                        button.setPreferredSize(new Dimension(0, 0));
                        return button;
                    }
                }
        );

        // LOGOUT

        RoundedMenuButton logoutButton = new RoundedMenuButton("Log Out");
        logoutButton.setLogoutStyle(true);

        logoutButton.addActionListener(e -> {

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        JPanel logoutPanel = new JPanel(new BorderLayout());
        logoutPanel.setOpaque(false);
        logoutPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        logoutPanel.add(logoutButton, BorderLayout.CENTER);

        sidebar.add(brandPanel, BorderLayout.NORTH);
        sidebar.add(menuScroll, BorderLayout.CENTER);
        sidebar.add(logoutPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    // ==========================================
    // OPEN MANAGEMENT WINDOWS
    // ==========================================

    private void openFrame(JFrame frame) {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ==========================================
    // MAIN CONTENT
    // ==========================================

    private JPanel createMainContent(String username) {

        JPanel main = new JPanel(new BorderLayout(0, 28));
        main.setBackground(BACKGROUND);
        main.setBorder(new EmptyBorder(35, 38, 35, 38));

        // HEADER

        JPanel header = new JPanel(new BorderLayout(15, 10));
        header.setOpaque(false);

        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Dashboard");
        heading.setFont(new Font("Georgia", Font.BOLD, 30));
        heading.setForeground(PRIMARY);

        JLabel welcome = new JLabel(
                "Welcome back, " + username + "! Here's your library overview."
        );
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcome.setForeground(MUTED);

        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(7));
        headingPanel.add(welcome);

        // ADMIN BADGE

        RoundedPanel adminBadge = new RoundedPanel(18, WHITE);
        adminBadge.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 7));
        adminBadge.setBorder(new EmptyBorder(3, 12, 3, 12));

        JLabel badgeLogo = new JLabel("L");
        badgeLogo.setFont(new Font("Georgia", Font.BOLD, 19));
        badgeLogo.setForeground(GOLD);

        JLabel badgeText = new JLabel("ADMIN");
        badgeText.setFont(new Font("Georgia", Font.BOLD, 12));
        badgeText.setForeground(PRIMARY);

        adminBadge.add(badgeLogo);
        adminBadge.add(badgeText);

        header.add(headingPanel, BorderLayout.WEST);
        header.add(adminBadge, BorderLayout.EAST);

        // STATISTICS CARDS

        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setOpaque(false);

        statsPanel.add(createStatCard(
                "TOTAL BOOKS",
                "0",
                "Books in library inventory",
                "01"
        ));

        statsPanel.add(createStatCard(
                "REGISTERED MEMBERS",
                "0",
                "Registered library members",
                "02"
        ));

        statsPanel.add(createStatCard(
                "BOOKS BORROWED",
                "0",
                "Currently borrowed books",
                "03"
        ));

        statsPanel.add(createStatCard(
                "OVERDUE BOOKS",
                "0",
                "Books past their due date",
                "04"
        ));

        // RECENT TRANSACTIONS SECTION

        RoundedPanel bottomPanel = new RoundedPanel(22, CARD_BACKGROUND);
        bottomPanel.setLayout(new BorderLayout(0, 14));
        bottomPanel.setBorder(new EmptyBorder(22, 25, 22, 25));

        JLabel recentTitle = new JLabel("Recent Transactions");
        recentTitle.setFont(new Font("Georgia", Font.BOLD, 18));
        recentTitle.setForeground(PRIMARY);

        JLabel placeholder = new JLabel(
                "Transaction records will appear here."
        );
        placeholder.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        placeholder.setForeground(MUTED);

        bottomPanel.add(recentTitle, BorderLayout.NORTH);
        bottomPanel.add(placeholder, BorderLayout.CENTER);

        // CENTER LAYOUT

        JPanel center = new JPanel(new BorderLayout(0, 22));
        center.setOpaque(false);

        center.add(statsPanel, BorderLayout.CENTER);
        center.add(bottomPanel, BorderLayout.SOUTH);

        main.add(header, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);

        return main;
    }

    // ==========================================
    // STATISTIC CARD
    // ==========================================

    private JPanel createStatCard(
            String title,
            String value,
            String description,
            String number
    ) {

        RoundedPanel card = new RoundedPanel(23, CARD_BACKGROUND);

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(23, 25, 23, 25));

        JLabel numberLabel = new JLabel(number);
        numberLabel.setFont(new Font("Georgia", Font.BOLD, 17));
        numberLabel.setForeground(GOLD);
        numberLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 12));
        titleLabel.setForeground(MUTED);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Georgia", Font.BOLD, 38));
        valueLabel.setForeground(PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descriptionLabel.setForeground(MUTED);
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        switch (number) {
            case "01" -> totalBooksValue = valueLabel;
            case "02" -> registeredMembersValue = valueLabel;
            case "03" -> booksBorrowedValue = valueLabel;
            case "04" -> overdueBooksValue = valueLabel;
        }

        card.add(numberLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(descriptionLabel);

        return card;
    }

    // ==========================================
    // ROUNDED PANEL WITH SOFT SHADOW
    // ==========================================

    private class RoundedPanel extends JPanel {

        private final int radius;
        private final Color backgroundColor;

        RoundedPanel(int radius, Color backgroundColor) {
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

            // Soft shadow

            g2.setColor(new Color(75, 20, 36, 18));
            g2.fillRoundRect(
                    3, 5,
                    getWidth() - 7,
                    getHeight() - 7,
                    radius, radius
            );

            // Main card surface

            g2.setColor(backgroundColor);
            g2.fillRoundRect(
                    0, 0,
                    getWidth() - 5,
                    getHeight() - 5,
                    radius, radius
            );

            // Subtle border

            g2.setColor(SOFT_BORDER);
            g2.drawRoundRect(
                    0, 0,
                    getWidth() - 6,
                    getHeight() - 6,
                    radius, radius
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // ==========================================
    // ROUNDED SIDEBAR BUTTON
    // ==========================================

    private class RoundedMenuButton extends JButton {

        private boolean logoutStyle = false;

        RoundedMenuButton(String text) {

            super(text);

            setFont(new Font("Georgia", Font.BOLD, 12));
            setForeground(new Color(243, 235, 221));
            setHorizontalAlignment(SwingConstants.LEFT);

            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            setPreferredSize(new Dimension(215, 46));

            setBorder(new EmptyBorder(10, 16, 10, 10));

            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);

            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        void setLogoutStyle(boolean value) {
            logoutStyle = value;
            setFont(new Font("Georgia", Font.BOLD, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            if (logoutStyle) {
                g2.setColor(new Color(55, 13, 27));

            } else if (getModel().isPressed()) {
                g2.setColor(new Color(105, 35, 53));

            } else if (getModel().isRollover()) {
                g2.setColor(new Color(92, 28, 46));

            } else {
                g2.setColor(PRIMARY);
            }

            g2.fillRoundRect(
                    0, 0,
                    getWidth(),
                    getHeight(),
                    14, 14
            );

            if (logoutStyle) {
                setForeground(GOLD);

            } else if (getModel().isRollover() || getModel().isPressed()) {
                setForeground(GOLD);

            } else {
                setForeground(new Color(243, 235, 221));
            }

            if (getModel().isRollover() || logoutStyle) {

                g2.setColor(GOLD);

                g2.fillRoundRect(
                        0, 10,
                        4,
                        getHeight() - 20,
                        4, 4
                );
            }

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // ==========================================
    // LOAD DASHBOARD STATISTICS
    // ==========================================

    private void loadDashboardStats() {

        try {

            session.requireAdmin();

            DashboardService.DashboardStats stats =
                    DashboardService.getDashboardStats();

            totalBooksValue.setText(
                    String.valueOf(stats.getTotalBooks())
            );

            registeredMembersValue.setText(
                    String.valueOf(stats.getRegisteredMembers())
            );

            booksBorrowedValue.setText(
                    String.valueOf(stats.getBooksBorrowed())
            );

            overdueBooksValue.setText(
                    String.valueOf(stats.getOverdueBooks())
            );

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load dashboard statistics:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }
}
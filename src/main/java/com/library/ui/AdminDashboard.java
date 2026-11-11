
package com.library.ui;

import com.library.service.DashboardService;
import com.library.service.UserSession;
import com.library.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class AdminDashboard extends JFrame {

    // =====================================================
    // LIBRARYLINK MODERN LIGHT THEME
    // =====================================================

    private final Color PRIMARY = new Color(92, 22, 35);
    private final Color PRIMARY_DARK = new Color(68, 15, 28);
    private final Color GOLD = new Color(197, 160, 89);

    private final Color BACKGROUND = new Color(251, 249, 245);
    private final Color WHITE = Color.WHITE;
    private final Color DARK_TEXT = new Color(31, 25, 26);
    private final Color MUTED = new Color(122, 106, 108);
    private final Color SOFT_BORDER = new Color(232, 225, 216);

    private final Color GREEN = new Color(48, 132, 91);
    private final Color RED = new Color(183, 66, 66);
    private final Color BLUE = new Color(60, 105, 155);

    private final UserSession session;

    private JLabel totalBooksValue;
    private JLabel registeredMembersValue;
    private JLabel booksBorrowedValue;
    private JLabel overdueBooksValue;

    // DATE AND TIME
    private JLabel dateTimeLabel;
    private Timer dateTimeTimer;

    // NOTIFICATIONS
    private NotificationEnvelopeButton notificationButton;
    private int notificationCount = 0;

    // RECENT TRANSACTIONS
    private JTable recentTransactionsTable;
    private DefaultTableModel recentTransactionsModel;

    // ACTIVE SIDEBAR BUTTON
    private RoundedMenuButton activeMenuButton;

    private final Map<String, RoundedMenuButton> menuButtons =
            new HashMap<>();

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public AdminDashboard(UserSession session) {

        if (session == null) {
            throw new SecurityException("Admin session is required.");
        }

        session.requireAdmin();
        this.session = session;

        setTitle("LibraryLink | Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setMinimumSize(new Dimension(1100, 700));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(session.getUsername()), BorderLayout.CENTER);

        loadDashboardStats();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (dateTimeTimer != null) {
                    dateTimeTimer.stop();
                }
            }
        });
    }

    // =====================================================
    // SIDEBAR
    // =====================================================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(PRIMARY);
        sidebar.setPreferredSize(new Dimension(255, 0));
        sidebar.setBorder(new EmptyBorder(28, 17, 20, 17));

        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(
                brandPanel,
                BoxLayout.Y_AXIS
        ));

        JLabel title = new JLabel("LibraryLink");
        title.setFont(new Font("Georgia", Font.BOLD, 23));
        title.setForeground(WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("ADMINISTRATION");
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        subtitle.setForeground(GOLD);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandPanel.add(Box.createVerticalStrut(2));
        brandPanel.add(title);
        brandPanel.add(Box.createVerticalStrut(5));
        brandPanel.add(subtitle);
        brandPanel.add(Box.createVerticalStrut(35));

        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(
                menuPanel,
                BoxLayout.Y_AXIS
        ));

        addSectionLabel(menuPanel, "CORE");
        addMenuItem(menuPanel, "Dashboard", true);

        addSectionLabel(menuPanel, "MANAGEMENT");
        addMenuItem(menuPanel, "Manage Books", false);
        addMenuItem(menuPanel, "Manage Members", false);
        addMenuItem(menuPanel, "Manage Student Accounts", false);

        addSectionLabel(menuPanel, "CIRCULATION");
        addMenuItem(menuPanel, "Request", false);
        addMenuItem(menuPanel, "Book Transactions", false);
        addMenuItem(menuPanel, "Transaction Records", false);

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
                        thumbColor = new Color(197, 160, 89);
                        trackColor = PRIMARY;
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

        RoundedMenuButton logoutButton =
                new RoundedMenuButton("Log Out");

        logoutButton.setLogoutStyle(true);
        logoutButton.addActionListener(e -> confirmLogout());

        JPanel logoutPanel = new JPanel(new BorderLayout());
        logoutPanel.setOpaque(false);
        logoutPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        logoutPanel.add(logoutButton, BorderLayout.CENTER);

        sidebar.add(brandPanel, BorderLayout.NORTH);
        sidebar.add(menuScroll, BorderLayout.CENTER);
        sidebar.add(logoutPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    // =====================================================
    // SIDEBAR SECTION LABEL
    // =====================================================

    private void addSectionLabel(JPanel panel, String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setForeground(new Color(197, 160, 89, 190));
        label.setBorder(new EmptyBorder(17, 12, 9, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
    }

    // =====================================================
    // SIDEBAR DISPLAY LABELS
    // =====================================================

    private String getMenuDisplayName(String item) {

        return switch (item) {
            case "Manage Books" -> "Books";
            case "Manage Members" -> "Members";
            case "Manage Student Accounts" -> "Student Accounts";
            default -> item;
        };
    }

    // =====================================================
    // SIDEBAR MENU ITEM
    // =====================================================

    private void addMenuItem(
            JPanel panel,
            String item,
            boolean active
    ) {

        RoundedMenuButton button =
                new RoundedMenuButton(getMenuDisplayName(item));

        menuButtons.put(item, button);

        if (active) {
            button.setActive(true);
            activeMenuButton = button;
        }

        button.addActionListener(e -> {

            session.requireAdmin();

            setActiveMenuItem(item);

            switch (item) {

                case "Manage Books" ->
                        openFrame(new ManageBooksFrame());

                case "Manage Members" ->
                        openFrame(new ManageMembersFrame());

                case "Manage Student Accounts" ->
                        openFrame(new ManageStudentAccountsFrame());

                case "Request" ->
                        openFrame(new ManageRequestsFrame(session));

                case "Book Transactions" ->
                        openFrame(new BookTransactionsFrame());

                case "Transaction Records" ->
                        openFrame(new TransactionRecordsFrame());

                case "Dashboard" -> {
                    loadDashboardStats();
                    loadRecentTransactions();
                }

                default -> JOptionPane.showMessageDialog(
                        this,
                        item + " section will be added in a later step.",
                        item,
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        panel.add(button);
        panel.add(Box.createVerticalStrut(5));
    }

    // =====================================================
    // UPDATE ACTIVE SIDEBAR BUTTON
    // =====================================================

    private void setActiveMenuItem(String item) {

        if (activeMenuButton != null) {
            activeMenuButton.setActive(false);
        }

        RoundedMenuButton selectedButton = menuButtons.get(item);

        if (selectedButton != null) {
            selectedButton.setActive(true);
            activeMenuButton = selectedButton;
        }
    }

    // =====================================================
    // LOGOUT
    // =====================================================

    private void confirmLogout() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {

            if (dateTimeTimer != null) {
                dateTimeTimer.stop();
            }

            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    // =====================================================
    // OPEN MANAGEMENT WINDOWS
    // =====================================================

    private void openFrame(JFrame frame) {

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // =====================================================
    // MAIN CONTENT
    // =====================================================

    private JPanel createMainContent(String username) {

        JPanel main = new JPanel(new BorderLayout(0, 25));
        main.setBackground(BACKGROUND);
        main.setBorder(new EmptyBorder(30, 35, 30, 35));

        JPanel header = createHeader(username);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(
                content,
                BoxLayout.Y_AXIS
        ));

        JPanel statsPanel = new JPanel(
                new GridLayout(1, 4, 17, 0)
        );

        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 175)
        );
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statsPanel.add(createStatCard(
                "TOTAL BOOKS",
                "0",
                "Books in library inventory",
                "01",
                "▤",
                GOLD
        ));

        statsPanel.add(createStatCard(
                "REGISTERED MEMBERS",
                "0",
                "Registered library members",
                "02",
                "",
                new Color(93, 127, 153)
        ));

        statsPanel.add(createStatCard(
                "BOOKS BORROWED",
                "0",
                "Currently borrowed books",
                "03",
                "↗",
                new Color(74, 139, 109)
        ));

        statsPanel.add(createStatCard(
                "OVERDUE BOOKS",
                "0",
                "Books past their due date",
                "04",
                "!",
                RED
        ));

        content.add(statsPanel);
        content.add(Box.createVerticalStrut(25));

        JPanel lowerContent = new JPanel(new GridBagLayout());
        lowerContent.setOpaque(false);
        lowerContent.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, 0, 18);

        gbc.gridx = 0;
        gbc.weightx = 0.72;

        lowerContent.add(
                createRecentTransactionsPanel(),
                gbc
        );

        gbc.gridx = 1;
        gbc.weightx = 0.28;
        gbc.insets = new Insets(0, 0, 0, 0);

        lowerContent.add(
                createQuickActionsPanel(),
                gbc
        );

        content.add(lowerContent);

        main.add(header, BorderLayout.NORTH);
        main.add(content, BorderLayout.CENTER);

        return main;
    }

    // =====================================================
    // HEADER - DATE, TIME AND ENVELOPE ICON
    // =====================================================

    private JPanel createHeader(String username) {

        JPanel header = new JPanel(new BorderLayout(20, 10));
        header.setOpaque(false);

        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(new BoxLayout(
                headingPanel,
                BoxLayout.Y_AXIS
        ));

        JLabel heading = new JLabel("Dashboard");
        heading.setFont(new Font("Georgia", Font.BOLD, 30));
        heading.setForeground(PRIMARY);

        JLabel welcome = new JLabel(
                "Welcome back, " + username
                        + "! Here's your library overview."
        );

        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        welcome.setForeground(MUTED);

        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(6));
        headingPanel.add(welcome);

        JPanel controls = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 13, 5)
        );

        controls.setOpaque(false);

        JPanel dateTimePanel = new JPanel();
        dateTimePanel.setOpaque(false);
        dateTimePanel.setLayout(new BoxLayout(
                dateTimePanel,
                BoxLayout.Y_AXIS
        ));

        JLabel dateLabel = new JLabel(
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(
                                "EEEE, MMMM d, yyyy",
                                Locale.ENGLISH
                        )
                )
        );

        dateLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 11)
        );

        dateLabel.setForeground(MUTED);
        dateLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 14)
        );

        dateTimeLabel.setForeground(PRIMARY);
        dateTimeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        dateTimePanel.add(dateLabel);
        dateTimePanel.add(Box.createVerticalStrut(4));
        dateTimePanel.add(dateTimeLabel);

        updateDateTime();

        dateTimeTimer = new Timer(1000, e -> updateDateTime());
        dateTimeTimer.start();

        // GMAIL-STYLE ENVELOPE BUTTON
        notificationButton = new NotificationEnvelopeButton();

        notificationButton.setToolTipText("Notifications");

        notificationButton.addActionListener(
                e -> showNotifications()
        );

        controls.add(dateTimePanel);
        controls.add(notificationButton);

        header.add(headingPanel, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);

        return header;
    }

    // =====================================================
    // LIVE DATE AND TIME
    // =====================================================

    private void updateDateTime() {

        if (dateTimeLabel == null) {
            return;
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "h:mm:ss a",
                        Locale.ENGLISH
                );

        dateTimeLabel.setText(
                LocalDateTime.now().format(formatter)
        );
    }

    // =====================================================
    // NOTIFICATIONS
    // =====================================================

    private void showNotifications() {

        session.requireAdmin();

        if (notificationCount <= 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "There are no new notifications.",
                    "Notifications",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "You have " + notificationCount
                            + " notification(s).",
                    "Notifications",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void setNotificationCount(int count) {

        notificationCount = Math.max(0, count);

        if (notificationButton != null) {
            notificationButton.setNotificationCount(
                    notificationCount
            );
        }
    }

    // =====================================================
    // STATISTIC CARD
    // =====================================================

    private JPanel createStatCard(
            String title,
            String value,
            String description,
            String number,
            String icon,
            Color iconColor
    ) {

        RoundedPanel card = new RoundedPanel(19, WHITE);

        card.setLayout(new BorderLayout(8, 12));
        card.setBorder(new EmptyBorder(19, 19, 19, 19));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLabel.setForeground(MUTED);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 20));
        iconLabel.setForeground(iconColor);
        iconLabel.setOpaque(true);

        iconLabel.setBackground(new Color(
                iconColor.getRed(),
                iconColor.getGreen(),
                iconColor.getBlue(),
                22
        ));

        iconLabel.setPreferredSize(new Dimension(40, 40));

        if ("02".equals(number)) {
            iconLabel.setText("");
            iconLabel.setIcon(new MembersGroupIcon(iconColor));
        }

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Georgia", Font.BOLD, 34));
        valueLabel.setForeground(PRIMARY);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descriptionLabel.setForeground(MUTED);

        switch (number) {
            case "01" -> totalBooksValue = valueLabel;
            case "02" -> registeredMembersValue = valueLabel;
            case "03" -> booksBorrowedValue = valueLabel;
            case "04" -> overdueBooksValue = valueLabel;
        }

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(
                bottom,
                BoxLayout.Y_AXIS
        ));

        bottom.add(valueLabel);
        bottom.add(Box.createVerticalStrut(5));
        bottom.add(descriptionLabel);

        top.add(titleLabel, BorderLayout.WEST);
        top.add(iconLabel, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.CENTER);

        return card;
    }

    // =====================================================
    // MEMBERS GROUP ICON
    // =====================================================

    private class MembersGroupIcon implements Icon {

        private final Color iconColor;

        MembersGroupIcon(Color iconColor) {
            this.iconColor = iconColor;
        }

        @Override
        public int getIconWidth() {
            return 25;
        }

        @Override
        public int getIconHeight() {
            return 25;
        }

        @Override
        public void paintIcon(
                Component component,
                Graphics graphics,
                int x,
                int y
        ) {

            Graphics2D g2 = (Graphics2D) graphics.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(iconColor);
            g2.setStroke(new BasicStroke(
                    1.8f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            g2.drawOval(x + 3, y + 3, 6, 6);

            g2.drawArc(
                    x + 1,
                    y + 12,
                    11,
                    10,
                    0,
                    180
            );

            g2.drawOval(x + 16, y + 3, 6, 6);

            g2.drawArc(
                    x + 13,
                    y + 12,
                    11,
                    10,
                    0,
                    180
            );

            g2.setStroke(new BasicStroke(
                    2f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            g2.drawOval(x + 9, y, 7, 7);

            g2.drawArc(
                    x + 6,
                    y + 10,
                    13,
                    13,
                    0,
                    180
            );

            g2.dispose();
        }
    }

    // =====================================================
    // RECENT TRANSACTIONS PANEL
    // =====================================================

    private JPanel createRecentTransactionsPanel() {

        RoundedPanel panel = new RoundedPanel(19, WHITE);
        panel.setLayout(new BorderLayout(0, 18));
        panel.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel headingPanel = new JPanel(new BorderLayout());
        headingPanel.setOpaque(false);

        JLabel title = new JLabel("Recent Transactions");
        title.setFont(new Font("Georgia", Font.BOLD, 18));
        title.setForeground(PRIMARY);

        JLabel subtitle = new JLabel(
                "Latest library circulation activity"
        );

        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(MUTED);

        JPanel titleGroup = new JPanel();
        titleGroup.setOpaque(false);
        titleGroup.setLayout(new BoxLayout(
                titleGroup,
                BoxLayout.Y_AXIS
        ));

        titleGroup.add(title);
        titleGroup.add(Box.createVerticalStrut(5));
        titleGroup.add(subtitle);

        JButton viewAll = new JButton("View Records  →");
        viewAll.setFont(new Font("Segoe UI", Font.BOLD, 11));
        viewAll.setForeground(GOLD);
        viewAll.setBorderPainted(false);
        viewAll.setContentAreaFilled(false);
        viewAll.setFocusPainted(false);
        viewAll.setCursor(new Cursor(Cursor.HAND_CURSOR));

        viewAll.addActionListener(e -> {
            session.requireAdmin();
            setActiveMenuItem("Transaction Records");
            openFrame(new TransactionRecordsFrame());
        });

        headingPanel.add(titleGroup, BorderLayout.WEST);
        headingPanel.add(viewAll, BorderLayout.EAST);

        String[] columns = {
                "TRANSACTION",
                "MEMBER",
                "BOOK",
                "STATUS"
        };

        recentTransactionsModel = new DefaultTableModel(
                new Object[][]{},
                columns
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recentTransactionsTable = new JTable(
                recentTransactionsModel
        );

        styleRecentTransactionsTable();

        JScrollPane tableScroll = new JScrollPane(
                recentTransactionsTable,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        tableScroll.setBorder(
                BorderFactory.createLineBorder(SOFT_BORDER)
        );

        tableScroll.setPreferredSize(new Dimension(500, 190));

        tableScroll.getVerticalScrollBar().setUnitIncrement(12);
        tableScroll.getVerticalScrollBar().setPreferredSize(
                new Dimension(9, 0)
        );

        tableScroll.getVerticalScrollBar().setBackground(
                new Color(249, 246, 240)
        );

        tableScroll.getVerticalScrollBar().setUI(
                new javax.swing.plaf.basic.BasicScrollBarUI() {

                    @Override
                    protected void configureScrollBarColors() {
                        thumbColor = GOLD;
                        trackColor = new Color(249, 246, 240);
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
                        button.setMinimumSize(new Dimension(0, 0));
                        button.setMaximumSize(new Dimension(0, 0));
                        return button;
                    }
                }
        );

        panel.add(headingPanel, BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);

        loadRecentTransactions();

        return panel;
    }

    // =====================================================
    // RECENT TRANSACTIONS TABLE STYLE
    // =====================================================

    private void styleRecentTransactionsTable() {

        recentTransactionsTable.setFont(
                new Font("Segoe UI", Font.PLAIN, 11)
        );

        recentTransactionsTable.setRowHeight(36);
        recentTransactionsTable.setForeground(DARK_TEXT);
        recentTransactionsTable.setBackground(WHITE);
        recentTransactionsTable.setGridColor(SOFT_BORDER);
        recentTransactionsTable.setShowVerticalLines(false);
        recentTransactionsTable.setShowHorizontalLines(true);
        recentTransactionsTable.setFillsViewportHeight(true);

        recentTransactionsTable.setSelectionBackground(
                new Color(248, 242, 232)
        );

        recentTransactionsTable.setSelectionForeground(PRIMARY);

        recentTransactionsTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        recentTransactionsTable.setAutoResizeMode(
                JTable.AUTO_RESIZE_ALL_COLUMNS
        );

        recentTransactionsTable.setIntercellSpacing(
                new Dimension(0, 1)
        );

        recentTransactionsTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 10)
        );

        recentTransactionsTable.getTableHeader().setForeground(MUTED);
        recentTransactionsTable.getTableHeader().setBackground(
                new Color(249, 246, 240)
        );

        recentTransactionsTable.getTableHeader().setPreferredSize(
                new Dimension(0, 36)
        );

        recentTransactionsTable.getTableHeader().setReorderingAllowed(
                false
        );

        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int column
                    ) {

                        Component component =
                                super.getTableCellRendererComponent(
                                        table,
                                        value,
                                        isSelected,
                                        hasFocus,
                                        row,
                                        column
                                );

                        if (isSelected) {
                            component.setBackground(
                                    new Color(248, 242, 232)
                            );
                            component.setForeground(PRIMARY);

                        } else {
                            component.setBackground(
                                    row % 2 == 0
                                            ? WHITE
                                            : new Color(251, 249, 245)
                            );

                            component.setForeground(DARK_TEXT);
                        }

                        setBorder(
                                BorderFactory.createEmptyBorder(
                                        0, 8, 0, 8
                                )
                        );

                        return component;
                    }
                };

        for (int i = 0;
             i < recentTransactionsTable.getColumnCount();
             i++) {

            recentTransactionsTable.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(renderer);
        }

        DefaultTableCellRenderer statusRenderer =
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int column
                    ) {

                        Component component =
                                super.getTableCellRendererComponent(
                                        table,
                                        value,
                                        isSelected,
                                        hasFocus,
                                        row,
                                        column
                                );

                        String status = String.valueOf(value);

                        if (isSelected) {
                            component.setBackground(
                                    new Color(248, 242, 232)
                            );
                            component.setForeground(PRIMARY);

                        } else {

                            component.setBackground(
                                    row % 2 == 0
                                            ? WHITE
                                            : new Color(251, 249, 245)
                            );

                            if ("RETURNED".equalsIgnoreCase(status)) {
                                component.setForeground(GREEN);

                            } else if ("OVERDUE".equalsIgnoreCase(status)) {
                                component.setForeground(RED);

                            } else {
                                component.setForeground(BLUE);
                            }
                        }

                        setHorizontalAlignment(SwingConstants.CENTER);
                        setFont(
                                new Font("Segoe UI", Font.BOLD, 10)
                        );

                        return component;
                    }
                };

        recentTransactionsTable.getColumnModel()
                .getColumn(3)
                .setCellRenderer(statusRenderer);
    }

    // =====================================================
    // LOAD RECENT TRANSACTIONS
    // =====================================================

    private void loadRecentTransactions() {

        if (recentTransactionsModel == null) {
            return;
        }

        recentTransactionsModel.setRowCount(0);

        String sql = """
                SELECT
                    t.transaction_id,
                    m.full_name,
                    b.title,
                    CASE
                        WHEN t.return_date IS NOT NULL
                            THEN 'RETURNED'
                        WHEN t.due_date < date('now', 'localtime')
                            THEN 'OVERDUE'
                        ELSE 'BORROWED'
                    END AS display_status
                FROM transactions t
                JOIN members m
                    ON t.member_id = m.member_id
                JOIN books b
                    ON t.book_id = b.book_id
                ORDER BY t.transaction_id DESC
                LIMIT 5
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                recentTransactionsModel.addRow(new Object[]{
                        "#" + result.getInt("transaction_id"),
                        result.getString("full_name"),
                        result.getString("title"),
                        result.getString("display_status")
                });
            }

            if (recentTransactionsModel.getRowCount() == 0) {

                recentTransactionsModel.addRow(new Object[]{
                        "-",
                        "No transactions yet",
                        "-",
                        "-"
                });
            }

        } catch (SQLException e) {

            recentTransactionsModel.addRow(new Object[]{
                    "-",
                    "Unable to load records",
                    "-",
                    "-"
            });

            System.err.println(
                    "Unable to load recent transactions: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // QUICK ACTIONS
    // =====================================================

    private JPanel createQuickActionsPanel() {

        RoundedPanel panel = new RoundedPanel(19, WHITE);

        panel.setLayout(new BoxLayout(
                panel,
                BoxLayout.Y_AXIS
        ));

        panel.setBorder(new EmptyBorder(22, 19, 22, 19));

        JLabel title = new JLabel("Quick Actions");
        title.setFont(new Font("Georgia", Font.BOLD, 18));
        title.setForeground(PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "Common administration tasks"
        );

        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(22));

        addQuickAction(
                panel,
                "Issue Book",
                "Process a book borrowing",
                "↗",
                () -> openFrame(new BookTransactionsFrame())
        );

        addQuickAction(
                panel,
                "Manage Members",
                "View and manage members",
                "",
                () -> openFrame(new ManageMembersFrame())
        );

        addQuickAction(
                panel,
                "Generate Report",
                "View transaction records",
                "▤",
                () -> openFrame(new TransactionRecordsFrame())
        );

        return panel;
    }

    // =====================================================
    // QUICK ACTION BUTTON
    // =====================================================

    private void addQuickAction(
            JPanel panel,
            String title,
            String description,
            String icon,
            Runnable action
    ) {

        JButton button = new JButton();
        button.setLayout(new BorderLayout(10, 0));

        button.setBackground(new Color(251, 249, 245));

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(SOFT_BORDER),
                        new EmptyBorder(11, 10, 11, 10)
                )
        );

        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 67)
        );

        button.setPreferredSize(new Dimension(230, 67));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(
                new Font("Segoe UI Symbol", Font.BOLD, 20)
        );
        iconLabel.setForeground(GOLD);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(30, 30));

        if ("Manage Members".equals(title)) {
            iconLabel.setText("");
            iconLabel.setIcon(new MembersGroupIcon(GOLD));
        }

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(
                textPanel,
                BoxLayout.Y_AXIS
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(PRIMARY);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        descriptionLabel.setForeground(MUTED);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(descriptionLabel);

        JLabel arrow = new JLabel("›");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 23));
        arrow.setForeground(GOLD);

        button.add(iconLabel, BorderLayout.WEST);
        button.add(textPanel, BorderLayout.CENTER);
        button.add(arrow, BorderLayout.EAST);

        button.addActionListener(e -> {
            session.requireAdmin();
            action.run();
        });

        panel.add(button);
        panel.add(Box.createVerticalStrut(10));
    }

    // =====================================================
    // LOAD DASHBOARD STATISTICS
    // =====================================================

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
                    "Unable to load dashboard statistics:\n"
                            + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // GMAIL-STYLE ENVELOPE NOTIFICATION BUTTON
    // =====================================================

    private class NotificationEnvelopeButton extends JButton {

        private int count = 0;

        NotificationEnvelopeButton() {

            setPreferredSize(new Dimension(36, 36));
            setMinimumSize(new Dimension(36, 36));
            setMaximumSize(new Dimension(36, 36));

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setBorder(null);

            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText("Notifications");
        }

        void setNotificationCount(int count) {

            this.count = Math.max(0, count);

            setToolTipText(
                    this.count > 0
                            ? this.count + " new notification(s)"
                            : "No new notifications"
            );

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            // TRANSPARENT HOVER CIRCLE
            if (getModel().isRollover()) {
                g2.setColor(new Color(92, 22, 35, 15));
                g2.fillOval(1, 1, 34, 34);
            }

            // ENVELOPE OUTLINE
            int x = 7;
            int y = 10;
            int width = 22;
            int height = 16;

            g2.setColor(PRIMARY);

            g2.setStroke(new BasicStroke(
                    1.8f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            // Envelope body
            g2.drawRoundRect(
                    x,
                    y,
                    width,
                    height,
                    3,
                    3
            );

            // Envelope top flap
            g2.drawLine(
                    x + 1,
                    y + 2,
                    x + width / 2,
                    y + 10
            );

            g2.drawLine(
                    x + width / 2,
                    y + 10,
                    x + width - 1,
                    y + 2
            );

            // Lower envelope folds
            g2.drawLine(
                    x + 1,
                    y + height - 2,
                    x + 8,
                    y + 9
            );

            g2.drawLine(
                    x + width - 1,
                    y + height - 2,
                    x + width - 8,
                    y + 9
            );

            // RED NOTIFICATION INDICATOR
            if (count > 0) {

                g2.setColor(RED);
                g2.fillOval(25, 4, 7, 7);

                // Small white center highlight
                g2.setColor(WHITE);
                g2.fillOval(27, 6, 3, 3);
            }

            g2.dispose();
        }
    }

    // =====================================================
    // ROUNDED PANEL
    // =====================================================

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

            g2.setColor(new Color(50, 25, 30, 16));

            g2.fillRoundRect(
                    3,
                    5,
                    getWidth() - 7,
                    getHeight() - 7,
                    radius,
                    radius
            );

            g2.setColor(backgroundColor);

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth() - 5,
                    getHeight() - 5,
                    radius,
                    radius
            );

            g2.setColor(SOFT_BORDER);

            g2.drawRoundRect(
                    0,
                    0,
                    getWidth() - 6,
                    getHeight() - 6,
                    radius,
                    radius
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // =====================================================
    // SIDEBAR MENU BUTTON
    // =====================================================

    private class RoundedMenuButton extends JButton {

        private boolean logoutStyle = false;
        private boolean active = false;

        RoundedMenuButton(String text) {

            super(text);

            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(new Color(245, 237, 228));
            setHorizontalAlignment(SwingConstants.LEFT);

            setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 43)
            );

            setPreferredSize(new Dimension(220, 43));
            setBorder(new EmptyBorder(10, 15, 10, 10));

            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        void setActive(boolean value) {
            active = value;
            repaint();
        }

        void setLogoutStyle(boolean value) {
            logoutStyle = value;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            if (logoutStyle) {
                g2.setColor(new Color(75, 18, 32));

            } else if (active) {
                g2.setColor(new Color(115, 42, 58));

            } else if (getModel().isPressed()) {
                g2.setColor(new Color(105, 35, 53));

            } else if (getModel().isRollover()) {
                g2.setColor(new Color(105, 35, 53));

            } else {
                g2.setColor(PRIMARY);
            }

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    12,
                    12
            );

            if (logoutStyle) {
                setForeground(new Color(235, 174, 174));

            } else if (active || getModel().isRollover()) {
                setForeground(GOLD);

            } else {
                setForeground(new Color(245, 237, 228));
            }

            if (active || logoutStyle) {

                g2.setColor(logoutStyle ? RED : GOLD);

                g2.fillRoundRect(
                        0,
                        9,
                        4,
                        getHeight() - 18,
                        4,
                        4
                );
            }

            g2.dispose();

            super.paintComponent(g);
        }
    }
}

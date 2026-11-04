
package com.library.ui;

import com.library.service.DashboardService;
import com.library.service.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class AdminDashboard extends JFrame {

    private final Color NAVY = new Color(23, 43, 77);
    private final Color LIGHT_BG = new Color(245, 247, 250);
    private final Color WHITE = Color.WHITE;

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

        setTitle("Library Borrowing System - Admin Dashboard");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(session.getUsername()), BorderLayout.CENTER);

        loadDashboardStats();
    }

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(25, 15, 25, 15));

        JLabel title = new JLabel("📚 LIBRARY");
        title.setFont(new Font("Segoe UI", Font.BOLD, 23));
        title.setForeground(WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("ADMIN PANEL");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(190, 205, 225));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(title);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(subtitle);
        sidebar.add(Box.createVerticalStrut(40));

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

            JButton button = new JButton(item);

            button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            button.setForeground(WHITE);
            button.setBackground(NAVY);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setMaximumSize(new Dimension(200, 45));
            button.setBorder(new EmptyBorder(10, 15, 10, 10));
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            button.addActionListener(e -> {

                session.requireAdmin();

                switch (item) {

                    case "Manage Books" ->
                            new ManageBooksFrame().setVisible(true);

                    case "Manage Members" ->
                            new ManageMembersFrame().setVisible(true);

                    case "Manage Student Accounts" ->
                            new ManageStudentAccountsFrame().setVisible(true);

                    case "Manage Borrowing Requests" ->
                            new ManageBorrowRequestsFrame(session).setVisible(true);

                    case "Manage Return Requests" ->
                            new ManageReturnRequestsFrame(session).setVisible(true);

                    case "Borrow Books" ->
                            new BorrowBooksFrame().setVisible(true);

                    case "Return Books" ->
                            new ReturnBooksFrame().setVisible(true);

                    case "Overdue Books" ->
                            new OverdueBooksFrame().setVisible(true);

                    case "Transaction Records" ->
                            new TransactionRecordsFrame().setVisible(true);

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

            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(8));
        }

        sidebar.add(Box.createVerticalGlue());

        JButton logoutButton = new JButton("Log Out");

        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutButton.setForeground(WHITE);
        logoutButton.setBackground(new Color(170, 55, 55));
        logoutButton.setMaximumSize(new Dimension(200, 42));
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

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

        sidebar.add(logoutButton);

        return sidebar;
    }

    private JPanel createMainContent(String username) {

        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBackground(LIGHT_BG);
        main.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel header = new JPanel();
        header.setBackground(LIGHT_BG);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Dashboard");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 30));
        heading.setForeground(NAVY);

        JLabel welcome = new JLabel("Welcome back, " + username + "!");
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        welcome.setForeground(Color.GRAY);

        header.add(heading);
        header.add(Box.createVerticalStrut(7));
        header.add(welcome);

        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 18, 18));
        statsPanel.setBackground(LIGHT_BG);

        statsPanel.add(createStatCard(
                "Total Books", "0", "Books in inventory", 1
        ));

        statsPanel.add(createStatCard(
                "Registered Members", "0", "Active library members", 2
        ));

        statsPanel.add(createStatCard(
                "Books Borrowed", "0", "Currently borrowed", 3
        ));

        statsPanel.add(createStatCard(
                "Overdue Books", "0", "Past their due date", 4
        ));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(WHITE);
        bottomPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel recentTitle = new JLabel("Recent Transactions");
        recentTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        recentTitle.setForeground(NAVY);

        JLabel placeholder = new JLabel(
                "Transaction records will appear here."
        );

        placeholder.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        placeholder.setForeground(Color.GRAY);

        bottomPanel.add(recentTitle, BorderLayout.NORTH);
        bottomPanel.add(placeholder, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setBackground(LIGHT_BG);

        center.add(statsPanel, BorderLayout.CENTER);
        center.add(bottomPanel, BorderLayout.SOUTH);

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

        JPanel card = new JPanel();
        card.setBackground(WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(NAVY);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descriptionLabel.setForeground(Color.GRAY);

        switch (cardNumber) {
            case 1 -> totalBooksValue = valueLabel;
            case 2 -> registeredMembersValue = valueLabel;
            case 3 -> booksBorrowedValue = valueLabel;
            case 4 -> overdueBooksValue = valueLabel;
        }

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(descriptionLabel);

        return card;
    }

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

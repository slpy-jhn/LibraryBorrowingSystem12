
package com.library.ui;

import com.library.service.StudentDashboardService;
import com.library.service.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class StudentDashboard extends JFrame {

    private final Color NAVY = new Color(23, 43, 77);
    private final Color LIGHT_BG = new Color(245, 247, 250);
    private final Color WHITE = Color.WHITE;

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

        // Confirm that this is a student session.
        this.memberId = session.requireStudent();
        this.session = session;

        setTitle("Library Borrowing System - Student Dashboard");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(session.getUsername()), BorderLayout.CENTER);

        loadStudentStats();
    }

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();

        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(230, 0));

        sidebar.setLayout(
                new BoxLayout(sidebar, BoxLayout.Y_AXIS)
        );

        sidebar.setBorder(
                new EmptyBorder(25, 15, 25, 15)
        );

        JLabel title = new JLabel("📚 LIBRARY");

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 23)
        );

        title.setForeground(WHITE);

        JLabel subtitle = new JLabel("STUDENT PORTAL");

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        subtitle.setForeground(
                new Color(190, 205, 225)
        );

        sidebar.add(title);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(subtitle);
        sidebar.add(Box.createVerticalStrut(40));

        String[] menuItems = {
                "Dashboard",
                "Browse Books",
                "My Borrowed Books",
                "My Borrowing History",
                "My Borrowing Requests",
                "My Return Requests",
                "My Account"
        };

        for (String item : menuItems) {

            JButton button = new JButton(item);

            button.setFont(
                    new Font("Segoe UI", Font.PLAIN, 14)
            );

            button.setForeground(WHITE);
            button.setBackground(NAVY);

            button.setHorizontalAlignment(
                    SwingConstants.LEFT
            );

            button.setMaximumSize(
                    new Dimension(200, 45)
            );

            button.setBorder(
                    new EmptyBorder(10, 15, 10, 10)
            );

            button.setFocusPainted(false);

            button.setCursor(
                    new Cursor(Cursor.HAND_CURSOR)
            );

            button.addActionListener(e -> {

                // Validate the student session before opening a section.
                int authenticatedMemberId = session.requireStudent();

                switch (item) {

                    case "Dashboard" ->
                            loadStudentStats();

                    case "Browse Books" ->
                            new StudentBooksFrame(session)
                                    .setVisible(true);

                    case "My Borrowed Books" ->
                            new MyBorrowedBooksFrame(session)
                                    .setVisible(true);

                    case "My Borrowing History" ->
                            new MyBorrowingHistoryFrame(authenticatedMemberId)
                                    .setVisible(true);

                    case "My Borrowing Requests" ->
                            new MyBorrowRequestsFrame(session)
                                    .setVisible(true);

                    case "My Return Requests" ->
                            new MyReturnRequestsFrame(session)
                                    .setVisible(true);

                    case "My Account" ->
                            new MyAccountFrame(authenticatedMemberId)
                                    .setVisible(true);

                    default -> JOptionPane.showMessageDialog(
                            this,
                            "Section is not available yet.",
                            "Information",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            });

            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(8));
        }

        sidebar.add(Box.createVerticalGlue());

        JButton logoutButton = new JButton("Log Out");

        logoutButton.setFont(
                new Font("Segoe UI", Font.BOLD, 14)
        );

        logoutButton.setForeground(WHITE);

        logoutButton.setBackground(
                new Color(170, 55, 55)
        );

        logoutButton.setMaximumSize(
                new Dimension(200, 42)
        );

        logoutButton.setFocusPainted(false);

        logoutButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

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

        JPanel main = new JPanel(
                new BorderLayout(20, 20)
        );

        main.setBackground(LIGHT_BG);

        main.setBorder(
                new EmptyBorder(30, 30, 30, 30)
        );

        // HEADER
        JPanel header = new JPanel();

        header.setBackground(LIGHT_BG);

        header.setLayout(
                new BoxLayout(header, BoxLayout.Y_AXIS)
        );

        JLabel heading = new JLabel("Student Dashboard");

        heading.setFont(
                new Font("Segoe UI", Font.BOLD, 30)
        );

        heading.setForeground(NAVY);

        JLabel welcome = new JLabel(
                "Welcome back, " + username + "!"
        );

        welcome.setFont(
                new Font("Segoe UI", Font.PLAIN, 15)
        );

        welcome.setForeground(Color.GRAY);

        header.add(heading);
        header.add(Box.createVerticalStrut(7));
        header.add(welcome);

        // STATISTICS
        JPanel statsPanel = new JPanel(
                new GridLayout(1, 3, 18, 18)
        );

        statsPanel.setBackground(LIGHT_BG);

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

        // INFORMATION PANEL
        JPanel infoPanel = new JPanel(
                new BorderLayout(10, 15)
        );

        infoPanel.setBackground(WHITE);

        infoPanel.setBorder(
                new EmptyBorder(25, 25, 25, 25)
        );

        JLabel infoTitle = new JLabel(
                "Library Information"
        );

        infoTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 19)
        );

        infoTitle.setForeground(NAVY);

        JLabel infoText = new JLabel(
                "<html>"
                        + "Welcome to your Library Student Portal.<br><br>"
                        + "You can use this dashboard to browse available "
                        + "books,<br>"
                        + "check your borrowed books, monitor due dates, "
                        + "and view<br>"
                        + "your personal borrowing history."
                        + "</html>"
        );

        infoText.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        infoText.setForeground(Color.DARK_GRAY);

        infoPanel.add(infoTitle, BorderLayout.NORTH);
        infoPanel.add(infoText, BorderLayout.CENTER);

        JPanel center = new JPanel(
                new BorderLayout(0, 25)
        );

        center.setBackground(LIGHT_BG);

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

        JPanel card = new JPanel();

        card.setBackground(WHITE);

        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );

        card.setBorder(
                new EmptyBorder(20, 20, 20, 20)
        );

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(value);

        valueLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 32)
        );

        valueLabel.setForeground(NAVY);

        JLabel descriptionLabel = new JLabel(description);

        descriptionLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        descriptionLabel.setForeground(Color.GRAY);

        switch (cardNumber) {

            case 1 -> borrowedValue = valueLabel;
            case 2 -> overdueValue = valueLabel;
            case 3 -> returnedValue = valueLabel;
        }

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(descriptionLabel);

        return card;
    }

    private void loadStudentStats() {

        try {

            // Always obtain the ID from the authenticated session.
            int authenticatedMemberId = session.requireStudent();

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

        } catch (SQLException | IllegalArgumentException
                 | SecurityException e) {

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
}


package com.library.ui;

import com.library.service.BookService;
import com.library.service.BookService.Book;
import com.library.service.StudentDashboardService;
import com.library.service.UserSession;
import com.library.database.DatabaseConnection;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StudentDashboard extends JFrame {

    // =====================================================
    // COLORS
    // =====================================================

    private final Color BURGUNDY = new Color(75, 20, 36);
    private final Color DARK_BURGUNDY = new Color(55, 13, 27);
    private final Color WINE = new Color(112, 43, 59);
    private final Color GOLD = new Color(176, 141, 87);
    private final Color CREAM = new Color(243, 235, 221);
    private final Color WHITE = new Color(255, 252, 245);
    private final Color TEXT_DARK = new Color(53, 37, 31);
    private final Color TEXT_GRAY = new Color(130, 115, 103);
    private final Color BORDER = new Color(222, 210, 191);
    private final Color GREEN = new Color(48, 132, 91);

    // =====================================================
    // SESSION
    // =====================================================

    private final UserSession session;
    private final int memberId;

    // =====================================================
    // DASHBOARD COMPONENTS
    // =====================================================

    private JLabel borrowedValue;
    private JLabel overdueValue;
    private JLabel returnedValue;

    private JLabel dateLabel;
    private JLabel timeLabel;

    private NotificationEnvelopeButton notificationButton;

    private Timer clockTimer;

    private JPanel recommendationContainer;

    private List<Book> allBooks = new ArrayList<>();

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

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
        setMinimumSize(new Dimension(1100, 700));
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(session.getUsername()), BorderLayout.CENTER);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        loadStudentStats();
        loadRecommendedBooks();

        startClock();
    }

    // =====================================================
    // SIDEBAR
    // =====================================================

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

        sidebar.setPreferredSize(new Dimension(255, 760));
        sidebar.setOpaque(false);

        JPanel topSection = new JPanel();

        topSection.setOpaque(false);
        topSection.setLayout(
                new BoxLayout(topSection, BoxLayout.Y_AXIS)
        );

        topSection.setBorder(
                new EmptyBorder(30, 20, 15, 20)
        );

        // LOGO

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

        subtitle.setFont(
                new Font("Palatino Linotype", Font.BOLD, 10)
        );

        subtitle.setForeground(GOLD);

        brandText.add(brand);
        brandText.add(Box.createVerticalStrut(5));
        brandText.add(subtitle);

        logoRow.add(logo);
        logoRow.add(brandText);

        topSection.add(logoRow);
        topSection.add(Box.createVerticalStrut(42));

        // MENU HEADING

        JLabel menuHeading = new JLabel("STUDENT MENU");

        menuHeading.setFont(
                new Font("Palatino Linotype", Font.BOLD, 11)
        );

        menuHeading.setForeground(
                new Color(224, 190, 164)
        );

        menuHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        topSection.add(menuHeading);
        topSection.add(Box.createVerticalStrut(15));

        // MENU ITEMS

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

                    case "Dashboard" -> {
                        loadStudentStats();
                        loadRecommendedBooks();
                    }

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

        // LOGOUT SECTION

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

                if (clockTimer != null) {
                    clockTimer.stop();
                }

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

    // =====================================================
    // MAIN CONTENT
    // =====================================================

    private JPanel createMainContent(String username) {

        JPanel main = new JPanel(new BorderLayout());

        main.setBackground(CREAM);

        main.add(createHeader(username), BorderLayout.NORTH);

        JPanel content = new JPanel();

        content.setOpaque(false);
        content.setLayout(
                new BoxLayout(content, BoxLayout.Y_AXIS)
        );

        content.setBorder(
                new EmptyBorder(10, 35, 35, 35)
        );

        // STATISTICS

        JPanel statsPanel = new JPanel(
                new GridLayout(1, 3, 18, 0)
        );

        statsPanel.setOpaque(false);

        statsPanel.add(
                createStatCard(
                        "CURRENTLY BORROWED",
                        "0",
                        "Books in your possession",
                        1
                )
        );

        statsPanel.add(
                createStatCard(
                        "OVERDUE BOOKS",
                        "0",
                        "Books past their due date",
                        2
                )
        );

        statsPanel.add(
                createStatCard(
                        "RETURNED BOOKS",
                        "0",
                        "Previously returned books",
                        3
                )
        );

        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statsPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 155)
        );

        content.add(statsPanel);
        content.add(Box.createVerticalStrut(25));

        // WELCOME CARD

        RoundedPanel welcomePanel = new RoundedPanel(WHITE, 18);

        welcomePanel.setLayout(new BorderLayout(15, 0));

        welcomePanel.setBorder(
                new EmptyBorder(20, 25, 20, 25)
        );

        JLabel welcomeTitle = new JLabel(
                "Welcome to LibraryLink"
        );

        welcomeTitle.setFont(
                new Font("Georgia", Font.BOLD, 23)
        );

        welcomeTitle.setForeground(BURGUNDY);

        JLabel welcomeDescription = new JLabel(
                "Explore your library, discover new books, and keep track of your borrowing activities."
        );

        welcomeDescription.setFont(
                new Font("Book Antiqua", Font.PLAIN, 14)
        );

        welcomeDescription.setForeground(TEXT_GRAY);

        JPanel welcomeText = new JPanel();

        welcomeText.setOpaque(false);
        welcomeText.setLayout(
                new BoxLayout(welcomeText, BoxLayout.Y_AXIS)
        );

        welcomeText.add(welcomeTitle);
        welcomeText.add(Box.createVerticalStrut(8));
        welcomeText.add(welcomeDescription);

        welcomePanel.add(welcomeText, BorderLayout.CENTER);

        welcomePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        welcomePanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 105)
        );

        content.add(welcomePanel);
        content.add(Box.createVerticalStrut(30));

        // RECOMMENDED BOOKS HEADING

        JPanel recommendationHeading = new JPanel(
                new BorderLayout()
        );

        recommendationHeading.setOpaque(false);
        recommendationHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel recommendationTitle = new JLabel(
                "Recommended Books"
        );

        recommendationTitle.setFont(
                new Font("Georgia", Font.BOLD, 25)
        );

        recommendationTitle.setForeground(BURGUNDY);

        JLabel recommendationSubtitle = new JLabel(
                "Discover books available in your library collection"
        );

        recommendationSubtitle.setFont(
                new Font("Book Antiqua", Font.PLAIN, 13)
        );

        recommendationSubtitle.setForeground(TEXT_GRAY);

        JPanel headingText = new JPanel();

        headingText.setOpaque(false);
        headingText.setLayout(
                new BoxLayout(headingText, BoxLayout.Y_AXIS)
        );

        headingText.add(recommendationTitle);
        headingText.add(Box.createVerticalStrut(5));
        headingText.add(recommendationSubtitle);

        recommendationHeading.add(
                headingText,
                BorderLayout.WEST
        );

        content.add(recommendationHeading);
        content.add(Box.createVerticalStrut(18));

        // BOOK RECOMMENDATIONS

        recommendationContainer = new JPanel();

        recommendationContainer.setOpaque(false);
        recommendationContainer.setLayout(
                new BoxLayout(
                        recommendationContainer,
                        BoxLayout.Y_AXIS
                )
        );

        recommendationContainer.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        content.add(recommendationContainer);

        JScrollPane scrollPane = new JScrollPane(content);

        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);

        scrollPane.getViewport().setOpaque(false);

        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        main.add(scrollPane, BorderLayout.CENTER);

        return main;
    }

    // =====================================================
    // HEADER WITH DATE, TIME AND NOTIFICATIONS
    // =====================================================

    private JPanel createHeader(String username) {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(WHITE);

        header.setBorder(
                new EmptyBorder(22, 35, 20, 35)
        );

        JPanel leftPanel = new JPanel();

        leftPanel.setOpaque(false);
        leftPanel.setLayout(
                new BoxLayout(leftPanel, BoxLayout.Y_AXIS)
        );

        JLabel heading = new JLabel("Student Dashboard");

        heading.setFont(
                new Font("Georgia", Font.BOLD, 32)
        );

        heading.setForeground(BURGUNDY);

        JLabel welcome = new JLabel(
                "Welcome back, " + username + "!"
        );

        welcome.setFont(
                new Font("Book Antiqua", Font.PLAIN, 15)
        );

        welcome.setForeground(TEXT_GRAY);

        leftPanel.add(heading);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(welcome);

        JPanel rightPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 16, 5)
        );

        rightPanel.setOpaque(false);

        // DATE AND TIME

        JPanel dateTimePanel = new JPanel();

        dateTimePanel.setOpaque(false);
        dateTimePanel.setLayout(
                new BoxLayout(dateTimePanel, BoxLayout.Y_AXIS)
        );

        dateLabel = new JLabel();

        dateLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        dateLabel.setForeground(TEXT_GRAY);

        dateLabel.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        dateLabel.setAlignmentX(
                Component.RIGHT_ALIGNMENT
        );

        timeLabel = new JLabel();

        timeLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 23)
        );

        timeLabel.setForeground(BURGUNDY);

        timeLabel.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        timeLabel.setAlignmentX(
                Component.RIGHT_ALIGNMENT
        );

        dateTimePanel.add(dateLabel);
        dateTimePanel.add(Box.createVerticalStrut(2));
        dateTimePanel.add(timeLabel);

        // NOTIFICATION BUTTON

        notificationButton = new NotificationEnvelopeButton();

        notificationButton.addActionListener(
                e -> showNotifications()
        );

        rightPanel.add(dateTimePanel);
        rightPanel.add(Box.createHorizontalStrut(8));
        rightPanel.add(notificationButton);

        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    // =====================================================
    // LIVE CLOCK
    // =====================================================

    private void startClock() {

        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern(
                        "EEEE, MMMM d, yyyy"
                );

        DateTimeFormatter timeFormatter =
                DateTimeFormatter.ofPattern(
                        "h:mm:ss a"
                );

        Runnable updateClock = () -> {

            LocalDateTime now = LocalDateTime.now();

            dateLabel.setText(
                    now.format(dateFormatter)
            );

            timeLabel.setText(
                    now.format(timeFormatter)
            );
        };

        updateClock.run();

        clockTimer = new Timer(
                1000,
                e -> updateClock.run()
        );

        clockTimer.start();
    }

    // =====================================================
    // NOTIFICATIONS
    // =====================================================

    private void showNotifications() {

        session.requireStudent();

        JOptionPane.showMessageDialog(
                this,
                "You currently have no new notifications.",
                "Notifications",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // =====================================================
    // STATISTICS CARDS
    // =====================================================

    private JPanel createStatCard(
            String title,
            String value,
            String description,
            int cardNumber
    ) {

        RoundedPanel card = new RoundedPanel(WHITE, 16);

        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );

        card.setBorder(
                new EmptyBorder(19, 22, 19, 22)
        );

        JPanel accent = new JPanel();

        accent.setBackground(GOLD);

        accent.setPreferredSize(new Dimension(42, 4));
        accent.setMaximumSize(new Dimension(42, 4));

        accent.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(
                new Font("Book Antiqua", Font.BOLD, 12)
        );

        titleLabel.setForeground(TEXT_GRAY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);

        valueLabel.setFont(
                new Font("Georgia", Font.BOLD, 38)
        );

        valueLabel.setForeground(BURGUNDY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descriptionLabel = new JLabel(description);

        descriptionLabel.setFont(
                new Font("Book Antiqua", Font.PLAIN, 12)
        );

        descriptionLabel.setForeground(TEXT_GRAY);
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        switch (cardNumber) {

            case 1 -> borrowedValue = valueLabel;
            case 2 -> overdueValue = valueLabel;
            case 3 -> returnedValue = valueLabel;
        }

        card.add(accent);
        card.add(Box.createVerticalStrut(12));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(7));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(descriptionLabel);

        return card;
    }

    // =====================================================
    // LOAD STUDENT STATISTICS
    // =====================================================

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

    // =====================================================
    // LOAD BOOKS AND OPTIONAL DETAILS
    // =====================================================

    private void loadRecommendedBooks() {

        recommendationContainer.removeAll();

        try {

            allBooks = BookService.getAllBooks();

            Map<Integer, BookExtraDetails> extraDetails =
                    loadAllBookExtraDetails();

            if (allBooks.isEmpty()) {

                showEmptyRecommendations(
                        "No books are currently available in the library collection."
                );

                return;
            }

            Map<String, List<Book>> categories =
                    new TreeMap<>(
                            String.CASE_INSENSITIVE_ORDER
                    );

            for (Book book : allBooks) {

                String category = book.getCategory();

                if (category == null || category.isBlank()) {
                    category = "Uncategorized";
                }

                categories.computeIfAbsent(
                        category.trim(),
                        key -> new ArrayList<>()
                ).add(book);
            }

            for (Map.Entry<String, List<Book>> entry :
                    categories.entrySet()) {

                String categoryName = entry.getKey();

                List<Book> categoryBooks = entry.getValue();

                categoryBooks.sort(
                        Comparator
                                .comparingInt(Book::getAvailableCopies)
                                .reversed()
                                .thenComparing(
                                        Book::getTitle,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                );

                addCategoryRow(
                        categoryName,
                        categoryBooks,
                        extraDetails
                );
            }

        } catch (SQLException e) {

            showEmptyRecommendations(
                    "Unable to load recommended books. Please try again."
            );

            e.printStackTrace();
        }

        recommendationContainer.revalidate();
        recommendationContainer.repaint();
    }

    // =====================================================
    // LOAD COVER AND DESCRIPTION DATA
    // =====================================================

    private Map<Integer, BookExtraDetails>
    loadAllBookExtraDetails() {

        Map<Integer, BookExtraDetails> details =
                new LinkedHashMap<>();

        String sql = """
                SELECT book_id, cover_image, description
                FROM books
                ORDER BY title
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {

                int bookId = rs.getInt("book_id");

                String cover = rs.getString("cover_image");

                String description = rs.getString("description");

                details.put(
                        bookId,
                        new BookExtraDetails(cover, description)
                );
            }

        } catch (SQLException e) {

            System.err.println(
                    "Unable to load optional book details: "
                            + e.getMessage()
            );
        }

        return details;
    }

    // =====================================================
    // CATEGORY ROW - IMPROVED ALIGNMENT
    // =====================================================

    private void addCategoryRow(
            String categoryName,
            List<Book> books,
            Map<Integer, BookExtraDetails> extraDetails
    ) {

        JPanel categorySection = new JPanel();

        categorySection.setOpaque(false);

        categorySection.setLayout(
                new BoxLayout(
                        categorySection,
                        BoxLayout.Y_AXIS
                )
        );

        categorySection.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        categorySection.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 380)
        );

        // CATEGORY HEADER

        JPanel heading = new JPanel(new BorderLayout());

        heading.setOpaque(false);

        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        heading.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 28)
        );

        JLabel categoryLabel = new JLabel(categoryName);

        categoryLabel.setFont(
                new Font("Georgia", Font.BOLD, 19)
        );

        categoryLabel.setForeground(BURGUNDY);

        JLabel bookCount = new JLabel(
                books.size() + " books"
        );

        bookCount.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        bookCount.setForeground(TEXT_GRAY);

        heading.add(categoryLabel, BorderLayout.WEST);
        heading.add(bookCount, BorderLayout.EAST);

        categorySection.add(heading);

        categorySection.add(
                Box.createVerticalStrut(12)
        );

        // BOOK CARDS

        JPanel cardsPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 18, 5)
        );

        cardsPanel.setOpaque(false);

        cardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (Book book : books) {

            BookExtraDetails extra =
                    extraDetails.getOrDefault(
                            book.getBookId(),
                            new BookExtraDetails("", "")
                    );

            cardsPanel.add(
                    createBookCard(book, extra)
            );
        }

        // HORIZONTAL SCROLL

        JScrollPane horizontalScroll = new JScrollPane(
                cardsPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        horizontalScroll.setBorder(null);
        horizontalScroll.setOpaque(false);

        horizontalScroll.getViewport().setOpaque(false);
        horizontalScroll.getViewport().setBackground(CREAM);

        horizontalScroll.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        horizontalScroll.setPreferredSize(
                new Dimension(800, 335)
        );

        horizontalScroll.setMinimumSize(
                new Dimension(0, 335)
        );

        horizontalScroll.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 335)
        );

        horizontalScroll.getHorizontalScrollBar()
                .setUnitIncrement(20);

        categorySection.add(horizontalScroll);

        categorySection.add(
                Box.createVerticalStrut(24)
        );

        recommendationContainer.add(categorySection);
    }

    // =====================================================
    // BOOK CARD
    // =====================================================

    private JPanel createBookCard(
            Book book,
            BookExtraDetails extra
    ) {

        RoundedPanel card = new RoundedPanel(WHITE, 17);

        card.setLayout(
                new BorderLayout(0, 0)
        );

        card.setPreferredSize(
                new Dimension(205, 315)
        );

        card.setMinimumSize(
                new Dimension(205, 315)
        );

        card.setMaximumSize(
                new Dimension(205, 315)
        );

        card.setBorder(
                new EmptyBorder(10, 10, 12, 10)
        );

        // COVER IMAGE

        JLabel cover = createCoverLabel(
                extra.coverImage,
                183,
                190
        );

        JPanel coverWrapper = new JPanel(
                new BorderLayout()
        );

        coverWrapper.setOpaque(false);

        coverWrapper.add(cover, BorderLayout.CENTER);

        // BOOK INFORMATION

        JPanel information = new JPanel();

        information.setOpaque(false);

        information.setLayout(
                new BoxLayout(
                        information,
                        BoxLayout.Y_AXIS
                )
        );

        information.setBorder(
                new EmptyBorder(10, 3, 0, 3)
        );

        JLabel title = new JLabel(
                shortenText(book.getTitle(), 25)
        );

        title.setFont(
                new Font("Georgia", Font.BOLD, 13)
        );

        title.setForeground(BURGUNDY);
        title.setToolTipText(book.getTitle());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel author = new JLabel(
                shortenText(book.getAuthor(), 27)
        );

        author.setFont(
                new Font("Book Antiqua", Font.PLAIN, 11)
        );

        author.setForeground(TEXT_GRAY);
        author.setToolTipText(book.getAuthor());
        author.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel availability = new JLabel(
                book.getAvailableCopies() > 0
                        ? book.getAvailableCopies() + " copies available"
                        : "Currently unavailable"
        );

        availability.setFont(
                new Font("Segoe UI", Font.BOLD, 10)
        );

        availability.setForeground(
                book.getAvailableCopies() > 0
                        ? GREEN
                        : new Color(183, 66, 66)
        );

        availability.setAlignmentX(Component.LEFT_ALIGNMENT);

        information.add(title);
        information.add(Box.createVerticalStrut(5));
        information.add(author);
        information.add(Box.createVerticalStrut(7));
        information.add(availability);

        card.add(coverWrapper, BorderLayout.CENTER);
        card.add(information, BorderLayout.SOUTH);

        attachClickRecursively(
                card,
                () -> showBookDetails(book, extra)
        );

        card.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        return card;
    }

    // =====================================================
    // COVER IMAGE
    // =====================================================

    private JLabel createCoverLabel(
            String imagePath,
            int width,
            int height
    ) {

        JLabel label = new JLabel();

        label.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        label.setVerticalAlignment(
                SwingConstants.CENTER
        );

        label.setOpaque(true);

        label.setBackground(
                new Color(244, 237, 226)
        );

        label.setPreferredSize(
                new Dimension(width, height)
        );

        label.setBorder(
                BorderFactory.createLineBorder(BORDER, 1)
        );

        if (imagePath != null && !imagePath.isBlank()) {

            try {

                File imageFile = new File(imagePath);

                if (!imageFile.isAbsolute()) {

                    imageFile = new File(
                            System.getProperty("user.home")
                                    + File.separator
                                    + "LibraryLink"
                                    + File.separator
                                    + "book_covers",
                            imagePath
                    );
                }

                if (imageFile.exists()) {

                    BufferedImage image = ImageIO.read(imageFile);

                    if (image != null) {

                        Image scaled = image.getScaledInstance(
                                width - 8,
                                height - 8,
                                Image.SCALE_SMOOTH
                        );

                        label.setIcon(
                                new ImageIcon(scaled)
                        );

                        label.setText("");

                        return label;
                    }
                }

            } catch (Exception e) {

                System.err.println(
                        "Unable to display book cover: "
                                + e.getMessage()
                );
            }
        }

        label.setLayout(new BorderLayout());

        JLabel placeholder = new JLabel(
                "<html><center>"
                        + "<span style='font-size:28px;'>&#128218;</span>"
                        + "<br>LibraryLink"
                        + "<br><small>No cover image</small>"
                        + "</center></html>",
                SwingConstants.CENTER
        );

        placeholder.setFont(
                new Font("Book Antiqua", Font.PLAIN, 12)
        );

        placeholder.setForeground(TEXT_GRAY);

        label.add(placeholder, BorderLayout.CENTER);

        return label;
    }

    // =====================================================
    // BOOK DETAILS WINDOW
    // =====================================================

    private void showBookDetails(
            Book book,
            BookExtraDetails extra
    ) {

        JDialog dialog = new JDialog(
                this,
                "Book Details",
                true
        );

        dialog.setDefaultCloseOperation(
                JDialog.DISPOSE_ON_CLOSE
        );

        dialog.setLayout(new BorderLayout());

        dialog.getContentPane().setBackground(CREAM);

        // HEADER

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(BURGUNDY);

        header.setBorder(
                new EmptyBorder(20, 25, 20, 25)
        );

        JLabel heading = new JLabel("BOOK DETAILS");

        heading.setFont(
                new Font("Georgia", Font.BOLD, 21)
        );

        heading.setForeground(GOLD);

        JLabel bookIdLabel = new JLabel(
                "BOOK ID  #" + book.getBookId()
        );

        bookIdLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        bookIdLabel.setForeground(WHITE);

        header.add(heading, BorderLayout.WEST);
        header.add(bookIdLabel, BorderLayout.EAST);

        // BODY

        JPanel body = new JPanel(
                new BorderLayout(22, 0)
        );

        body.setBackground(CREAM);

        body.setBorder(
                new EmptyBorder(24, 25, 24, 25)
        );

        // LEFT COVER

        JPanel coverPanel = new JPanel(new BorderLayout());

        coverPanel.setBackground(WHITE);

        coverPanel.setBorder(
                new EmptyBorder(12, 12, 12, 12)
        );

        JLabel cover = createCoverLabel(
                extra.coverImage,
                235,
                315
        );

        coverPanel.add(cover, BorderLayout.CENTER);

        coverPanel.setPreferredSize(
                new Dimension(260, 345)
        );

        // RIGHT INFORMATION

        JPanel details = new JPanel();

        details.setBackground(WHITE);

        details.setLayout(
                new BoxLayout(details, BoxLayout.Y_AXIS)
        );

        details.setBorder(
                new EmptyBorder(20, 22, 20, 22)
        );

        JLabel title = new JLabel(
                "<html><div style='width:390px;'>"
                        + escapeHtml(book.getTitle())
                        + "</div></html>"
        );

        title.setFont(
                new Font("Georgia", Font.BOLD, 22)
        );

        title.setForeground(BURGUNDY);

        JLabel author = new JLabel(
                "By " + safeText(book.getAuthor())
        );

        author.setFont(
                new Font("Book Antiqua", Font.PLAIN, 15)
        );

        author.setForeground(TEXT_GRAY);

        details.add(title);
        details.add(Box.createVerticalStrut(8));
        details.add(author);
        details.add(Box.createVerticalStrut(20));

        addDetailLine(details, "ISBN", book.getIsbn());
        addDetailLine(details, "CATEGORY", book.getCategory());
        addDetailLine(details, "PUBLISHER", book.getPublisher());

        addDetailLine(
                details,
                "PUBLICATION YEAR",
                String.valueOf(book.getPublicationYear())
        );

        addDetailLine(
                details,
                "SHELF LOCATION",
                book.getShelfLocation()
        );

        addDetailLine(
                details,
                "TOTAL COPIES",
                String.valueOf(book.getTotalCopies())
        );

        addDetailLine(
                details,
                "AVAILABLE COPIES",
                String.valueOf(book.getAvailableCopies())
        );

        // DESCRIPTION

        JLabel descriptionHeading = new JLabel(
                "BOOK DESCRIPTION"
        );

        descriptionHeading.setFont(
                new Font("Georgia", Font.BOLD, 12)
        );

        descriptionHeading.setForeground(BURGUNDY);

        descriptionHeading.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JTextArea description = new JTextArea(
                extra.description == null
                        || extra.description.isBlank()
                        ? "No description has been added for this book."
                        : extra.description
        );

        description.setFont(
                new Font("Book Antiqua", Font.PLAIN, 13)
        );

        description.setForeground(TEXT_DARK);
        description.setBackground(WHITE);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setEditable(false);
        description.setFocusable(false);
        description.setBorder(null);
        description.setRows(5);

        JScrollPane descriptionScroll = new JScrollPane(
                description
        );

        descriptionScroll.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        descriptionScroll.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        descriptionScroll.setPreferredSize(
                new Dimension(400, 110)
        );

        descriptionScroll.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 110)
        );

        details.add(Box.createVerticalStrut(14));
        details.add(descriptionHeading);
        details.add(Box.createVerticalStrut(8));
        details.add(descriptionScroll);

        JScrollPane detailsScroll = new JScrollPane(details);

        detailsScroll.setBorder(null);

        detailsScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        detailsScroll.getVerticalScrollBar()
                .setUnitIncrement(15);

        body.add(coverPanel, BorderLayout.WEST);
        body.add(detailsScroll, BorderLayout.CENTER);

        // FOOTER

        JPanel footer = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 12)
        );

        footer.setBackground(WHITE);

        footer.setBorder(
                new EmptyBorder(5, 20, 5, 20)
        );

        JButton closeButton = new JButton("Close");

        styleButton(closeButton, BURGUNDY, WHITE);

        closeButton.setPreferredSize(
                new Dimension(115, 40)
        );

        closeButton.addActionListener(
                e -> dialog.dispose()
        );

        footer.add(closeButton);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(body, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setSize(850, 650);

        dialog.setMinimumSize(
                new Dimension(750, 550)
        );

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // =====================================================
    // BOOK DETAIL ROW
    // =====================================================

    private void addDetailLine(
            JPanel panel,
            String label,
            String value
    ) {

        JPanel row = new JPanel(
                new BorderLayout(12, 0)
        );

        row.setOpaque(false);

        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 29)
        );

        JLabel name = new JLabel(label);

        name.setFont(
                new Font("Segoe UI", Font.BOLD, 10)
        );

        name.setForeground(TEXT_GRAY);

        name.setPreferredSize(
                new Dimension(135, 25)
        );

        JLabel content = new JLabel(safeText(value));

        content.setFont(
                new Font("Book Antiqua", Font.PLAIN, 12)
        );

        content.setForeground(TEXT_DARK);

        row.add(name, BorderLayout.WEST);
        row.add(content, BorderLayout.CENTER);

        panel.add(row);
        panel.add(Box.createVerticalStrut(5));
    }

    // =====================================================
    // CLICK SUPPORT
    // =====================================================

    private void attachClickRecursively(
            Component component,
            Runnable action
    ) {

        component.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        component.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (SwingUtilities.isLeftMouseButton(e)) {
                    action.run();
                }
            }
        });

        if (component instanceof Container container) {

            for (Component child : container.getComponents()) {

                attachClickRecursively(child, action);
            }
        }
    }

    // =====================================================
    // EMPTY RECOMMENDATION MESSAGE
    // =====================================================

    private void showEmptyRecommendations(String message) {

        recommendationContainer.removeAll();

        RoundedPanel emptyPanel = new RoundedPanel(
                WHITE,
                18
        );

        emptyPanel.setLayout(new BorderLayout());

        emptyPanel.setBorder(
                new EmptyBorder(35, 25, 35, 25)
        );

        JLabel messageLabel = new JLabel(
                message,
                SwingConstants.CENTER
        );

        messageLabel.setFont(
                new Font("Book Antiqua", Font.PLAIN, 14)
        );

        messageLabel.setForeground(TEXT_GRAY);

        emptyPanel.add(messageLabel, BorderLayout.CENTER);

        emptyPanel.setPreferredSize(
                new Dimension(600, 110)
        );

        emptyPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 110)
        );

        recommendationContainer.add(emptyPanel);

        recommendationContainer.revalidate();
        recommendationContainer.repaint();
    }

    // =====================================================
    // TEXT HELPERS
    // =====================================================

    private String shortenText(
            String value,
            int maximumLength
    ) {

        if (value == null || value.isBlank()) {
            return "Unknown";
        }

        if (value.length() <= maximumLength) {
            return value;
        }

        return value.substring(0, maximumLength - 3) + "...";
    }

    private String safeText(String value) {

        return value == null || value.isBlank()
                ? "Not specified"
                : value;
    }

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    // =====================================================
    // BUTTON STYLE
    // =====================================================

    private void styleButton(
            JButton button,
            Color background,
            Color foreground
    ) {

        button.setFont(
                new Font("Georgia", Font.BOLD, 12)
        );

        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        button.setBorder(
                new EmptyBorder(10, 16, 10, 16)
        );
    }

    // =====================================================
    // OPEN OTHER WINDOWS
    // =====================================================

    private void openFrame(JFrame frame) {

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    // =====================================================
    // OPTIONAL BOOK DETAILS
    // =====================================================

    private static class BookExtraDetails {

        private final String coverImage;
        private final String description;

        private BookExtraDetails(
                String coverImage,
                String description
        ) {

            this.coverImage = coverImage == null
                    ? ""
                    : coverImage;

            this.description = description == null
                    ? ""
                    : description;
        }
    }

    // =====================================================
    // NOTIFICATION ENVELOPE BUTTON
    // =====================================================

    private class NotificationEnvelopeButton extends JButton {

        public NotificationEnvelopeButton() {

            setPreferredSize(new Dimension(42, 42));
            setMinimumSize(new Dimension(42, 42));
            setMaximumSize(new Dimension(42, 42));

            setToolTipText("Notifications");
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);

            setCursor(
                    new Cursor(Cursor.HAND_CURSOR)
            );
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            if (getModel().isRollover()) {

                g2.setColor(
                        new Color(176, 141, 87, 35)
                );

                g2.fillRoundRect(
                        0, 0,
                        getWidth(), getHeight(),
                        12, 12
                );
            }

            g2.setColor(BURGUNDY);

            g2.setStroke(
                    new BasicStroke(
                            1.8f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            int x = 9;
            int y = 12;
            int width = 24;
            int height = 18;

            g2.drawRoundRect(
                    x, y,
                    width, height,
                    4, 4
            );

            g2.drawLine(
                    x + 1,
                    y + 2,
                    x + width / 2,
                    y + 11
            );

            g2.drawLine(
                    x + width / 2,
                    y + 11,
                    x + width - 1,
                    y + 2
            );

            g2.dispose();
        }
    }

    // =====================================================
    // ROUNDED PANEL
    // =====================================================

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

            // SOFT SHADOW

            g2.setColor(
                    new Color(70, 45, 30, 18)
            );

            g2.fillRoundRect(
                    3, 4,
                    getWidth() - 6,
                    getHeight() - 6,
                    cornerRadius,
                    cornerRadius
            );

            // BACKGROUND

            g2.setColor(backgroundColor);

            g2.fillRoundRect(
                    0, 0,
                    getWidth() - 4,
                    getHeight() - 5,
                    cornerRadius,
                    cornerRadius
            );

            // GOLD BORDER

            g2.setColor(
                    new Color(176, 141, 87, 130)
            );

            g2.setStroke(new BasicStroke(1f));

            g2.drawRoundRect(
                    0, 0,
                    getWidth() - 4,
                    getHeight() - 5,
                    cornerRadius,
                    cornerRadius
            );

            g2.dispose();
        }
    }

    // =====================================================
    // SIDEBAR MENU BUTTON
    // =====================================================

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

            if (active) {

                g2.setColor(
                        new Color(255, 252, 245, 35)
                );

                g2.fillRoundRect(
                        0, 0,
                        getWidth(), getHeight(),
                        8, 8
                );

            } else if (hovered) {

                g2.setColor(
                        new Color(255, 252, 245, 20)
                );

                g2.fillRoundRect(
                        0, 0,
                        getWidth(), getHeight(),
                        8, 8
                );
            }

            if (active) {

                g2.setColor(GOLD);

                g2.fillRoundRect(
                        0, 7,
                        4,
                        getHeight() - 14,
                        4, 4
                );
            }

            g2.dispose();
        }
    }
}

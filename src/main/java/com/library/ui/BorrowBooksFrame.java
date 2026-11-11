
package com.library.ui;

import com.library.database.DatabaseConnection;
import com.library.service.BorrowService;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;

public class BorrowBooksFrame extends JFrame {

    // =====================================================
    // THEME
    // =====================================================

    private static final Color BURGUNDY = new Color(75, 20, 36);
    private static final Color DARK_BURGUNDY = new Color(55, 13, 27);
    private static final Color GOLD = new Color(176, 141, 87);
    private static final Color CREAM = new Color(243, 235, 221);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(53, 37, 31);
    private static final Color MUTED = new Color(130, 115, 103);
    private static final Color BORDER = new Color(222, 210, 191);
    private static final Color LIGHT_GOLD = new Color(244, 232, 207);
    private static final Color GREEN = new Color(38, 125, 82);

    private static final int BORROW_PERIOD_DAYS = 7;

    private final JComboBox<MemberItem> memberBox;
    private final JComboBox<BookItem> bookBox;
    private final JTextField borrowingDateField;

    private JLabel memberCountLabel;
    private JLabel bookCountLabel;

    private JLabel summaryMemberLabel;
    private JLabel summaryBookLabel;
    private JLabel summaryPeriodLabel;
    private JLabel summaryDueDateLabel;

    private JButton confirmButton;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public BorrowBooksFrame() {

        setTitle("LibraryLink - Borrow Book");
        setSize(1100, 720);
        setMinimumSize(new Dimension(850, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());

        memberBox = new JComboBox<>();
        bookBox = new JComboBox<>();

        borrowingDateField = new RoundedTextField(
                LocalDate.now().toString(),
                12,
                BORDER,
                14
        );

        buildHeader();
        buildMainContent();

        loadData();
        updateBorrowingSummary();
    }

    // =====================================================
    // HEADER
    // =====================================================

    private void buildHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel leftPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 15, 0)
        );

        leftPanel.setOpaque(false);

        JButton backButton = new JButton(
                new ArrowIcon(GOLD)
        );

        backButton.setPreferredSize(new Dimension(38, 38));
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.setOpaque(false);
        backButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        backButton.setToolTipText("Back");

        backButton.addActionListener(e -> dispose());

        JPanel titlePanel = new JPanel();

        titlePanel.setOpaque(false);

        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("Borrow Book");

        title.setFont(
                new Font("Georgia", Font.BOLD, 26)
        );

        title.setForeground(GOLD);

        JLabel subtitle = new JLabel(
                "Process a book borrowing transaction for a library member."
        );

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        subtitle.setForeground(new Color(235, 220, 207));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        leftPanel.add(backButton);
        leftPanel.add(titlePanel);

        header.add(leftPanel, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);
    }

    // =====================================================
    // MAIN CONTENT
    // =====================================================

    private void buildMainContent() {

        JPanel root = new JPanel(new BorderLayout(0, 12));

        root.setBackground(CREAM);

        root.setBorder(
                new EmptyBorder(18, 24, 22, 24)
        );

        // =================================================
        // COUNT INFORMATION
        // =================================================

        JPanel countPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 20, 0)
        );

        countPanel.setOpaque(false);

        memberCountLabel = new JLabel("Active Members: 0");
        bookCountLabel = new JLabel("Available Books: 0");

        memberCountLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 11)
        );

        bookCountLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 11)
        );

        memberCountLabel.setForeground(MUTED);
        bookCountLabel.setForeground(MUTED);

        countPanel.add(memberCountLabel);
        countPanel.add(bookCountLabel);

        // =================================================
        // TWO-COLUMN LAYOUT
        // =================================================

        JPanel columns = new JPanel(new GridBagLayout());

        columns.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;

        // =================================================
        // LEFT PANEL
        // =================================================

        JPanel formPanel = new RoundedPanel(
                WHITE,
                BORDER,
                22
        );

        formPanel.setLayout(
                new BorderLayout(0, 20)
        );

        formPanel.setBorder(
                new EmptyBorder(25, 25, 25, 25)
        );

        JLabel formTitle = new JLabel(
                "BORROWING INFORMATION"
        );

        formTitle.setFont(
                new Font("Georgia", Font.BOLD, 18)
        );

        formTitle.setForeground(BURGUNDY);

        JLabel formSubtitle = new JLabel(
                "Select a member, book, and borrowing issue date."
        );

        formSubtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        formSubtitle.setForeground(MUTED);

        JPanel formHeader = new JPanel();

        formHeader.setOpaque(false);

        formHeader.setLayout(
                new BoxLayout(formHeader, BoxLayout.Y_AXIS)
        );

        formHeader.add(formTitle);
        formHeader.add(Box.createVerticalStrut(7));
        formHeader.add(formSubtitle);

        configureComboBox(memberBox);
        configureComboBox(bookBox);

        JPanel fields = new JPanel();

        fields.setOpaque(false);

        fields.setLayout(
                new BoxLayout(fields, BoxLayout.Y_AXIS)
        );

        // MEMBER

        fields.add(createFieldLabel("SELECT MEMBER"));
        fields.add(Box.createVerticalStrut(8));
        fields.add(memberBox);
        fields.add(Box.createVerticalStrut(20));

        // BOOK

        fields.add(createFieldLabel("SELECT AVAILABLE BOOK"));
        fields.add(Box.createVerticalStrut(8));
        fields.add(bookBox);
        fields.add(Box.createVerticalStrut(20));

        // BORROWING DATE

        fields.add(createFieldLabel("BORROWING ISSUE DATE"));
        fields.add(Box.createVerticalStrut(8));

        JPanel datePanel = new JPanel(
                new BorderLayout(8, 0)
        );

        datePanel.setOpaque(false);

        datePanel.add(
                borrowingDateField,
                BorderLayout.CENTER
        );

        JButton calendarButton = new RoundedButton(
                "",
                LIGHT_GOLD,
                BURGUNDY,
                14
        );

        calendarButton.setIcon(
                new CalendarIcon(BURGUNDY)
        );

        calendarButton.setToolTipText("Open calendar");

        calendarButton.setPreferredSize(
                new Dimension(48, 43)
        );

        calendarButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        calendarButton.addActionListener(
                e -> showCalendar()
        );

        datePanel.add(
                calendarButton,
                BorderLayout.EAST
        );

        datePanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 43)
        );

        datePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        fields.add(datePanel);

        // The unnecessary standard-period note has been removed.

        // =================================================
        // CONFIRM BUTTON - RIGHT ALIGNED
        // =================================================

        confirmButton = new RoundedButton(
                "CONFIRM BORROWING",
                GREEN,
                WHITE,
                14
        );

        confirmButton.setFont(
                new Font("Georgia", Font.BOLD, 11)
        );

        confirmButton.setPreferredSize(
                new Dimension(215, 45)
        );

        confirmButton.setMaximumSize(
                new Dimension(215, 45)
        );

        confirmButton.setMinimumSize(
                new Dimension(215, 45)
        );

        confirmButton.setAlignmentX(Component.RIGHT_ALIGNMENT);

        confirmButton.addActionListener(
                e -> processBorrowing()
        );

        JPanel formBottom = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 0)
        );

        formBottom.setOpaque(false);

        formBottom.add(confirmButton);

        formPanel.add(formHeader, BorderLayout.NORTH);
        formPanel.add(fields, BorderLayout.CENTER);
        formPanel.add(formBottom, BorderLayout.SOUTH);

        // =================================================
        // RIGHT PANEL
        // =================================================

        JPanel summaryPanel = createSummaryPanel();

        gbc.gridx = 0;
        gbc.weightx = 0.58;
        gbc.insets = new Insets(0, 0, 0, 10);

        columns.add(formPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.42;
        gbc.insets = new Insets(0, 10, 0, 0);

        columns.add(summaryPanel, gbc);

        root.add(countPanel, BorderLayout.NORTH);
        root.add(columns, BorderLayout.CENTER);

        add(root, BorderLayout.CENTER);

        // =================================================
        // LIVE SUMMARY
        // =================================================

        memberBox.addActionListener(
                e -> updateBorrowingSummary()
        );

        bookBox.addActionListener(
                e -> updateBorrowingSummary()
        );

        borrowingDateField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {

                    @Override
                    public void insertUpdate(
                            javax.swing.event.DocumentEvent e
                    ) {
                        updateBorrowingSummary();
                    }

                    @Override
                    public void removeUpdate(
                            javax.swing.event.DocumentEvent e
                    ) {
                        updateBorrowingSummary();
                    }

                    @Override
                    public void changedUpdate(
                            javax.swing.event.DocumentEvent e
                    ) {
                        updateBorrowingSummary();
                    }
                }
        );
    }

    // =====================================================
    // BORROWING SUMMARY
    // =====================================================

    private JPanel createSummaryPanel() {

        JPanel panel = new RoundedPanel(
                WHITE,
                BORDER,
                22
        );

        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        panel.setBorder(
                new EmptyBorder(25, 22, 25, 22)
        );

        JLabel heading = new JLabel(
                "BORROWING SUMMARY"
        );

        heading.setFont(
                new Font("Georgia", Font.BOLD, 17)
        );

        heading.setForeground(BURGUNDY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "Transaction details"
        );

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(22));

        summaryMemberLabel = new JLabel("Not selected");
        summaryBookLabel = new JLabel("Not selected");
        summaryPeriodLabel = new JLabel(BORROW_PERIOD_DAYS + " days");
        summaryDueDateLabel = new JLabel("—");

        panel.add(createSummaryRow(
                "Member Name",
                summaryMemberLabel
        ));

        panel.add(createSummaryDivider());

        panel.add(createSummaryRow(
                "Selected Book",
                summaryBookLabel
        ));

        panel.add(createSummaryDivider());

        panel.add(createSummaryRow(
                "Borrow Period",
                summaryPeriodLabel
        ));

        panel.add(createSummaryDivider());

        panel.add(createSummaryRow(
                "Due Date",
                summaryDueDateLabel
        ));

        return panel;
    }

    // =====================================================
    // SUMMARY ROW
    // =====================================================

    private JPanel createSummaryRow(
            String heading,
            JLabel value
    ) {

        JPanel row = new JPanel(new GridBagLayout());

        row.setOpaque(false);

        row.setBorder(
                new EmptyBorder(10, 0, 10, 0)
        );

        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel(heading);

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 11)
        );

        title.setForeground(MUTED);

        value.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        value.setForeground(BURGUNDY);
        value.setHorizontalAlignment(SwingConstants.RIGHT);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridx = 0;
        gbc.weightx = 0.45;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 8);

        row.add(title, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.55;
        gbc.insets = new Insets(0, 0, 0, 0);

        row.add(value, gbc);

        row.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 65)
        );

        return row;
    }

    private JPanel createSummaryDivider() {

        JPanel divider = new JPanel();

        divider.setBackground(BORDER);

        divider.setPreferredSize(
                new Dimension(200, 1)
        );

        divider.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 1)
        );

        divider.setAlignmentX(Component.LEFT_ALIGNMENT);

        return divider;
    }

    // =====================================================
    // UPDATE SUMMARY
    // =====================================================

    private void updateBorrowingSummary() {

        if (summaryMemberLabel == null) {
            return;
        }

        MemberItem member =
                (MemberItem) memberBox.getSelectedItem();

        BookItem book =
                (BookItem) bookBox.getSelectedItem();

        summaryMemberLabel.setText(
                member == null ? "Not selected" : member.name
        );

        summaryBookLabel.setText(
                book == null ? "Not selected" : book.title
        );

        summaryPeriodLabel.setText(
                BORROW_PERIOD_DAYS + " days"
        );

        try {

            LocalDate borrowingDate = LocalDate.parse(
                    borrowingDateField.getText().trim()
            );

            summaryDueDateLabel.setText(
                    borrowingDate.plusDays(BORROW_PERIOD_DAYS).toString()
            );

        } catch (Exception e) {

            summaryDueDateLabel.setText("—");
        }
    }

    // =====================================================
    // CALENDAR
    // =====================================================

    private void showCalendar() {

        LocalDate initialSelectedDate;

        try {

            initialSelectedDate = LocalDate.parse(
                    borrowingDateField.getText().trim()
            );

        } catch (Exception e) {

            initialSelectedDate = LocalDate.now();
        }

        final LocalDate selectedDate = initialSelectedDate;

        JDialog dialog = new JDialog(
                this,
                "Select Borrowing Date",
                true
        );

        dialog.setDefaultCloseOperation(
                JDialog.DISPOSE_ON_CLOSE
        );

        JPanel calendarPanel = new JPanel(
                new BorderLayout(10, 12)
        );

        calendarPanel.setBackground(WHITE);

        calendarPanel.setBorder(
                new EmptyBorder(18, 18, 18, 18)
        );

        final YearMonth[] displayedMonth = {
                YearMonth.from(selectedDate)
        };

        JPanel monthHeader = new JPanel(
                new BorderLayout()
        );

        monthHeader.setOpaque(false);

        JLabel monthLabel = new JLabel(
                "",
                SwingConstants.CENTER
        );

        monthLabel.setFont(
                new Font("Georgia", Font.BOLD, 16)
        );

        monthLabel.setForeground(BURGUNDY);

        JButton previousButton =
                createCalendarNavigationButton("‹");

        JButton nextButton =
                createCalendarNavigationButton("›");

        monthHeader.add(
                previousButton,
                BorderLayout.WEST
        );

        monthHeader.add(
                monthLabel,
                BorderLayout.CENTER
        );

        monthHeader.add(
                nextButton,
                BorderLayout.EAST
        );

        JPanel daysPanel = new JPanel(
                new GridLayout(0, 7, 5, 5)
        );

        daysPanel.setOpaque(false);

        JPanel calendarBody = new JPanel(
                new BorderLayout(0, 8)
        );

        calendarBody.setOpaque(false);

        calendarBody.add(
                monthHeader,
                BorderLayout.NORTH
        );

        calendarBody.add(
                daysPanel,
                BorderLayout.CENTER
        );

        JLabel instruction = new JLabel(
                "Choose a borrowing issue date"
        );

        instruction.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        instruction.setForeground(MUTED);

        calendarPanel.add(
                instruction,
                BorderLayout.NORTH
        );

        calendarPanel.add(
                calendarBody,
                BorderLayout.CENTER
        );

        Runnable renderCalendar = () -> {

            daysPanel.removeAll();

            monthLabel.setText(
                    displayedMonth[0].getMonth().getDisplayName(
                            TextStyle.FULL,
                            Locale.ENGLISH
                    ) + " " + displayedMonth[0].getYear()
            );

            String[] weekdays = {
                    "MON", "TUE", "WED",
                    "THU", "FRI", "SAT", "SUN"
            };

            for (String day : weekdays) {

                JLabel dayLabel = new JLabel(
                        day,
                        SwingConstants.CENTER
                );

                dayLabel.setFont(
                        new Font("Segoe UI", Font.BOLD, 10)
                );

                dayLabel.setForeground(MUTED);

                daysPanel.add(dayLabel);
            }

            LocalDate firstDay =
                    displayedMonth[0].atDay(1);

            int offset =
                    firstDay.getDayOfWeek().getValue() - 1;

            for (int i = 0; i < offset; i++) {
                daysPanel.add(new JLabel(""));
            }

            int daysInMonth =
                    displayedMonth[0].lengthOfMonth();

            for (int day = 1; day <= daysInMonth; day++) {

                final LocalDate date =
                        displayedMonth[0].atDay(day);

                JButton dayButton = new RoundedButton(
                        String.valueOf(day),
                        WHITE,
                        TEXT,
                        10
                );

                dayButton.setFont(
                        new Font("Segoe UI", Font.PLAIN, 12)
                );

                boolean isFuture =
                        date.isAfter(LocalDate.now());

                boolean isSelected =
                        date.equals(selectedDate);

                if (isSelected) {

                    dayButton.setBackground(BURGUNDY);
                    dayButton.setForeground(WHITE);

                } else {

                    dayButton.setBackground(WHITE);
                    dayButton.setForeground(TEXT);
                }

                if (isFuture) {

                    dayButton.setEnabled(false);

                    dayButton.setForeground(
                            new Color(190, 190, 190)
                    );
                }

                dayButton.addActionListener(e -> {

                    borrowingDateField.setText(
                            date.toString()
                    );

                    dialog.dispose();
                });

                daysPanel.add(dayButton);
            }

            int totalCells = 7 + offset + daysInMonth;

            while (totalCells % 7 != 0) {

                daysPanel.add(new JLabel(""));
                totalCells++;
            }

            daysPanel.revalidate();
            daysPanel.repaint();
        };

        previousButton.addActionListener(e -> {

            displayedMonth[0] =
                    displayedMonth[0].minusMonths(1);

            renderCalendar.run();
        });

        nextButton.addActionListener(e -> {

            YearMonth nextMonth =
                    displayedMonth[0].plusMonths(1);

            if (!nextMonth.atDay(1).isAfter(LocalDate.now())) {

                displayedMonth[0] = nextMonth;

                renderCalendar.run();
            }
        });

        renderCalendar.run();

        dialog.add(calendarPanel);

        dialog.setSize(340, 370);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // =====================================================
    // CALENDAR NAVIGATION BUTTON
    // =====================================================

    private JButton createCalendarNavigationButton(
            String symbol
    ) {

        JButton button = new RoundedButton(
                symbol,
                LIGHT_GOLD,
                BURGUNDY,
                10
        );

        button.setFont(
                new Font("Segoe UI", Font.BOLD, 23)
        );

        button.setPreferredSize(
                new Dimension(38, 34)
        );

        return button;
    }

    // =====================================================
    // COMBO BOX
    // =====================================================

    private void configureComboBox(
            JComboBox<?> comboBox
    ) {

        comboBox.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        comboBox.setBackground(WHITE);
        comboBox.setForeground(TEXT);

        comboBox.setPreferredSize(
                new Dimension(300, 43)
        );

        comboBox.setMinimumSize(
                new Dimension(150, 43)
        );

        comboBox.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 43)
        );

        comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        comboBox.setBorder(
                new RoundedLineBorder(BORDER, 14, 1)
        );
    }

    // =====================================================
    // FIELD LABEL
    // =====================================================

    private JLabel createFieldLabel(String text) {

        JLabel label = new JLabel(text);

        label.setFont(
                new Font("Georgia", Font.BOLD, 11)
        );

        label.setForeground(BURGUNDY);

        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    // =====================================================
    // ROUNDED TEXT FIELD
    // =====================================================

    private static class RoundedTextField extends JTextField {

        private final Color borderColor;
        private final int radius;

        RoundedTextField(
                String text,
                int columns,
                Color borderColor,
                int radius
        ) {

            super(text, columns);

            this.borderColor = borderColor;
            this.radius = radius;

            setFont(
                    new Font("Segoe UI", Font.PLAIN, 13)
            );

            setForeground(TEXT);
            setBackground(WHITE);
            setOpaque(false);

            setBorder(
                    new EmptyBorder(9, 12, 9, 12)
            );
        }

        @Override
        protected void paintComponent(Graphics graphics) {

            Graphics2D g = (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setColor(getBackground());

            g.fillRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius,
                    radius
            );

            g.dispose();

            super.paintComponent(graphics);
        }

        @Override
        protected void paintBorder(Graphics graphics) {

            Graphics2D g = (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setColor(borderColor);
            g.setStroke(new BasicStroke(1f));

            g.drawRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius,
                    radius
            );

            g.dispose();
        }
    }

    // =====================================================
    // ROUNDED BUTTON
    // =====================================================

    private static class RoundedButton extends JButton {

        private final int radius;

        RoundedButton(
                String text,
                Color background,
                Color foreground,
                int radius
        ) {

            super(text);

            this.radius = radius;

            setBackground(background);
            setForeground(foreground);

            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);

            setBorder(
                    new EmptyBorder(8, 14, 8, 14)
            );

            setCursor(
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            );
        }

        @Override
        protected void paintComponent(Graphics graphics) {

            Graphics2D g = (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color background = getBackground();

            if (!isEnabled()) {
                background = new Color(190, 190, 190);
            } else if (getModel().isPressed()) {
                background = background.darker();
            } else if (getModel().isRollover()) {
                background = background.brighter();
            }

            g.setColor(background);

            g.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    radius,
                    radius
            );

            g.dispose();

            super.paintComponent(graphics);
        }
    }

    // =====================================================
    // ROUNDED BORDER
    // =====================================================

    private static class RoundedLineBorder extends AbstractBorder {

        private final Color color;
        private final int radius;
        private final int thickness;

        RoundedLineBorder(
                Color color,
                int radius,
                int thickness
        ) {

            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public Insets getBorderInsets(Component component) {

            return new Insets(8, 12, 8, 12);
        }

        @Override
        public Insets getBorderInsets(
                Component component,
                Insets insets
        ) {

            insets.set(8, 12, 8, 12);

            return insets;
        }

        @Override
        public void paintBorder(
                Component component,
                Graphics graphics,
                int x,
                int y,
                int width,
                int height
        ) {

            Graphics2D g = (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setColor(color);

            g.setStroke(
                    new BasicStroke(thickness)
            );

            g.drawRoundRect(
                    x,
                    y,
                    width - 1,
                    height - 1,
                    radius,
                    radius
            );

            g.dispose();
        }
    }

    // =====================================================
    // ROUNDED PANEL
    // =====================================================

    private static class RoundedPanel extends JPanel {

        private final Color backgroundColor;
        private final Color borderColor;
        private final int radius;

        RoundedPanel(
                Color backgroundColor,
                Color borderColor,
                int radius
        ) {

            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
            this.radius = radius;

            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {

            Graphics2D g = (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setColor(backgroundColor);

            g.fillRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius,
                    radius
            );

            g.setColor(borderColor);

            g.setStroke(
                    new BasicStroke(1f)
            );

            g.drawRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius,
                    radius
            );

            g.dispose();

            super.paintComponent(graphics);
        }
    }

    // =====================================================
    // CALENDAR ICON
    // =====================================================

    private static class CalendarIcon implements Icon {

        private final Color color;

        CalendarIcon(Color color) {
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
        public void paintIcon(
                Component component,
                Graphics graphics,
                int x,
                int y
        ) {

            Graphics2D g = (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setColor(color);
            g.setStroke(new BasicStroke(1.8f));

            g.drawRoundRect(
                    x + 2, y + 4, 16, 14, 3, 3
            );

            g.drawLine(
                    x + 2, y + 8, x + 18, y + 8
            );

            g.drawLine(
                    x + 6, y + 2, x + 6, y + 6
            );

            g.drawLine(
                    x + 14, y + 2, x + 14, y + 6
            );

            g.fillRect(x + 6, y + 11, 2, 2);
            g.fillRect(x + 11, y + 11, 2, 2);
            g.fillRect(x + 6, y + 15, 2, 2);
            g.fillRect(x + 11, y + 15, 2, 2);

            g.dispose();
        }
    }

    // =====================================================
    // BACK ARROW ICON
    // =====================================================

    private static class ArrowIcon implements Icon {

        private final Color color;

        ArrowIcon(Color color) {
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return 24;
        }

        @Override
        public int getIconHeight() {
            return 24;
        }

        @Override
        public void paintIcon(
                Component component,
                Graphics graphics,
                int x,
                int y
        ) {

            Graphics2D g = (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setColor(color);

            g.setStroke(
                    new BasicStroke(
                            2.2f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            g.drawLine(x + 19, y + 12, x + 5, y + 12);
            g.drawLine(x + 5, y + 12, x + 12, y + 5);
            g.drawLine(x + 5, y + 12, x + 12, y + 19);

            g.dispose();
        }
    }

    // =====================================================
    // LOAD DATA
    // =====================================================

    private void loadData() {

        loadMembers();
        loadBooks();

        memberBox.setSelectedIndex(-1);
        bookBox.setSelectedIndex(-1);

        memberCountLabel.setText(
                "Active Members: " + memberBox.getItemCount()
        );

        bookCountLabel.setText(
                "Available Books: " + bookBox.getItemCount()
        );

        updateBorrowingSummary();
    }

    // =====================================================
    // LOAD MEMBERS
    // =====================================================

    private void loadMembers() {

        memberBox.removeAllItems();

        String sql = """
                SELECT member_id, full_name, student_employee_id
                FROM members
                WHERE membership_status = 'ACTIVE'
                ORDER BY full_name
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet result = statement.executeQuery()
        ) {

            while (result.next()) {

                memberBox.addItem(
                        new MemberItem(
                                result.getInt("member_id"),
                                result.getString("full_name"),
                                result.getString("student_employee_id")
                        )
                );
            }

        } catch (SQLException e) {

            showError(e);
        }
    }

    // =====================================================
    // LOAD BOOKS
    // =====================================================

    private void loadBooks() {

        bookBox.removeAllItems();

        String sql = """
                SELECT book_id, title, author, available_copies
                FROM books
                WHERE available_copies > 0
                ORDER BY title
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet result = statement.executeQuery()
        ) {

            while (result.next()) {

                bookBox.addItem(
                        new BookItem(
                                result.getInt("book_id"),
                                result.getString("title"),
                                result.getString("author"),
                                result.getInt("available_copies")
                        )
                );
            }

        } catch (SQLException e) {

            showError(e);
        }
    }

    // =====================================================
    // PROCESS BORROWING
    // =====================================================

    private void processBorrowing() {

        MemberItem member =
                (MemberItem) memberBox.getSelectedItem();

        BookItem book =
                (BookItem) bookBox.getSelectedItem();

        if (member == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an active member first.",
                    "No Member Selected",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (book == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an available book.",
                    "No Book Selected",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            LocalDate borrowingDate = LocalDate.parse(
                    borrowingDateField.getText().trim()
            );

            LocalDate dueDate = borrowingDate.plusDays(
                    BORROW_PERIOD_DAYS
            );

            if (borrowingDate.isAfter(LocalDate.now())) {

                JOptionPane.showMessageDialog(
                        this,
                        "Borrowing date cannot be in the future.",
                        "Invalid Date",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            int confirmation = JOptionPane.showConfirmDialog(
                    this,

                    "Please review the borrowing information:\n\n"
                            + "Member: " + member.name
                            + "\nStudent ID: " + member.studentId
                            + "\nBook: " + book.title
                            + "\nAuthor: " + book.author
                            + "\nAvailable Copies: " + book.availableCopies
                            + "\nBorrowing Date: " + borrowingDate
                            + "\nBorrow Period: " + BORROW_PERIOD_DAYS + " days"
                            + "\nDue Date: " + dueDate
                            + "\n\nConfirm this transaction?",

                    "Confirm Borrowing",

                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirmation != JOptionPane.YES_OPTION) {
                return;
            }

            boolean success = BorrowService.borrowBook(
                    member.id,
                    book.id,
                    borrowingDate,
                    dueDate
            );

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Book borrowed successfully!",
                        "Borrowing Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );

                loadData();

                borrowingDateField.setText(
                        LocalDate.now().toString()
                );

                memberBox.setSelectedIndex(-1);
                bookBox.setSelectedIndex(-1);

                updateBorrowingSummary();
            }

        } catch (DateTimeParseException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a valid date using the calendar.",
                    "Invalid Date",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException | IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Borrowing Failed",
                    JOptionPane.WARNING_MESSAGE
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // DATABASE ERROR
    // =====================================================

    private void showError(SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Database error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );

        e.printStackTrace();
    }

    // =====================================================
    // MEMBER ITEM
    // =====================================================

    private static class MemberItem {

        private final int id;
        private final String name;
        private final String studentId;

        MemberItem(
                int id,
                String name,
                String studentId
        ) {

            this.id = id;
            this.name = name;
            this.studentId = studentId;
        }

        @Override
        public String toString() {

            return name + " (" + studentId + ")";
        }
    }

    // =====================================================
    // BOOK ITEM
    // =====================================================

    private static class BookItem {

        private final int id;
        private final String title;
        private final String author;
        private final int availableCopies;

        BookItem(
                int id,
                String title,
                String author,
                int availableCopies
        ) {

            this.id = id;
            this.title = title;
            this.author = author;
            this.availableCopies = availableCopies;
        }

        @Override
        public String toString() {

            return title + " - " + author
                    + " (" + availableCopies + " available)";
        }
    }
}

package com.library.ui;

import com.library.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OverdueBooksFrame extends JFrame {

    // =====================================================
    // COMPONENTS
    // =====================================================

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    private JLabel totalOverdueValue;
    private JLabel severeOverdueValue;
    private JLabel displayedValue;

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
    private static final Color BORDER = new Color(226, 217, 204);
    private static final Color LIGHT_GOLD = new Color(249, 241, 224);
    private static final Color RED = new Color(175, 55, 55);
    private static final Color DARK_RED = new Color(135, 35, 35);
    private static final Color LIGHT_RED = new Color(255, 235, 235);
    private static final Color LIGHT_BACKGROUND = new Color(250, 247, 242);

    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font HEADING_FONT = new Font("Georgia", Font.BOLD, 17);

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public OverdueBooksFrame() {

        setTitle("Overdue Books");
        setSize(1280, 760);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());
        getContentPane().setBackground(CREAM);

        buildHeader();
        buildMainContent();
        loadOverdueBooks("");
    }

    // =====================================================
    // HEADER
    // =====================================================

    private void buildHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(22, 30, 22, 30));

        JPanel titlePanel = new JPanel();

        titlePanel.setOpaque(false);
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("Overdue Books");

        title.setFont(
                new Font("Georgia", Font.BOLD, 27)
        );

        title.setForeground(GOLD);

        JLabel subtitle = new JLabel(
                "Monitor unreturned books that have passed their due dates"
        );

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        subtitle.setForeground(
                new Color(235, 220, 215)
        );

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        // Transparent icon-only back button
        JButton backButton = new JButton("←");

        backButton.setFont(
                new Font("Segoe UI", Font.PLAIN, 25)
        );

        backButton.setForeground(WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setOpaque(false);

        backButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        backButton.setToolTipText("Back");

        backButton.setPreferredSize(
                new Dimension(45, 42)
        );

        backButton.addActionListener(e -> dispose());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    // =====================================================
    // MAIN CONTENT
    // =====================================================

    private void buildMainContent() {

        JPanel main = new JPanel(
                new BorderLayout(0, 20)
        );

        main.setBackground(CREAM);

        main.setBorder(
                new EmptyBorder(22, 28, 22, 28)
        );

        // =================================================
        // SUMMARY CARDS
        // =================================================

        JPanel summaryPanel = new JPanel(
                new GridLayout(1, 3, 16, 0)
        );

        summaryPanel.setOpaque(false);

        totalOverdueValue = createSummaryValue();
        severeOverdueValue = createSummaryValue();
        displayedValue = createSummaryValue();

        summaryPanel.add(
                createSummaryCard(
                        "TOTAL OVERDUE BOOKS",
                        totalOverdueValue,
                        "All currently overdue loans",
                        GOLD
                )
        );

        summaryPanel.add(
                createSummaryCard(
                        "SEVERELY OVERDUE",
                        severeOverdueValue,
                        "Overdue for more than 30 days",
                        RED
                )
        );

        summaryPanel.add(
                createSummaryCard(
                        "DISPLAYED RECORDS",
                        displayedValue,
                        "Based on current search",
                        BURGUNDY
                )
        );

        // =================================================
        // DIRECTORY PANEL
        // =================================================

        JPanel centerPanel = new JPanel(
                new BorderLayout(0, 16)
        );

        centerPanel.setOpaque(false);

        JPanel toolbar = new JPanel(
                new BorderLayout(15, 0)
        );

        toolbar.setOpaque(false);

        JLabel sectionTitle = new JLabel(
                "Overdue Books Directory"
        );

        sectionTitle.setFont(HEADING_FONT);
        sectionTitle.setForeground(BURGUNDY);

        // Upgraded search panel
        JPanel searchPanel = buildSearchPanel();

        toolbar.add(
                sectionTitle,
                BorderLayout.WEST
        );

        toolbar.add(
                searchPanel,
                BorderLayout.EAST
        );

        // =================================================
        // TABLE
        // =================================================

        String[] columns = {
                "Transaction ID",
                "Member Name",
                "Student/Employee ID",
                "Book Title",
                "Borrowing Date",
                "Due Date",
                "Days Overdue"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column
            ) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {

                return columnIndex == 0 || columnIndex == 6
                        ? Integer.class
                        : String.class;
            }
        };

        table = new JTable(tableModel);

        sorter = new TableRowSorter<>(tableModel);

        table.setRowSorter(sorter);

        styleTable();

        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder()
        );

        scrollPane.getViewport().setBackground(WHITE);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel tablePanel = new RoundedPanel(
                18,
                WHITE,
                BORDER
        );

        tablePanel.setLayout(new BorderLayout());

        tablePanel.setBorder(
                new EmptyBorder(10, 10, 10, 10)
        );

        tablePanel.add(
                scrollPane,
                BorderLayout.CENTER
        );

        centerPanel.add(
                toolbar,
                BorderLayout.NORTH
        );

        centerPanel.add(
                tablePanel,
                BorderLayout.CENTER
        );

        main.add(
                summaryPanel,
                BorderLayout.NORTH
        );

        main.add(
                centerPanel,
                BorderLayout.CENTER
        );

        add(main, BorderLayout.CENTER);

        installSearchFilter();
        installRowActions();
    }

    // =====================================================
    // SEARCH PANEL
    // =====================================================

    private JPanel buildSearchPanel() {

        JPanel outerPanel = new RoundedPanel(
                16,
                WHITE,
                BORDER
        );

        outerPanel.setLayout(
                new BorderLayout(10, 0)
        );

        outerPanel.setBorder(
                new EmptyBorder(5, 14, 5, 14)
        );

        outerPanel.setPreferredSize(
                new Dimension(365, 46)
        );

        outerPanel.setMinimumSize(
                new Dimension(250, 46)
        );

        JLabel searchIcon = new JLabel("⌕");

        searchIcon.setFont(
                new Font("Segoe UI Symbol", Font.PLAIN, 25)
        );

        searchIcon.setForeground(GOLD);

        searchField = new JTextField();

        searchField.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        searchField.setForeground(TEXT);
        searchField.setCaretColor(BURGUNDY);
        searchField.setBackground(WHITE);

        searchField.setBorder(
                BorderFactory.createEmptyBorder(5, 2, 5, 2)
        );

        searchField.setToolTipText(
                "Search by member name, Student/Employee ID, or book title"
        );

        outerPanel.add(
                searchIcon,
                BorderLayout.WEST
        );

        outerPanel.add(
                searchField,
                BorderLayout.CENTER
        );

        return outerPanel;
    }

    // =====================================================
    // SEARCH FILTER
    // =====================================================

    private void installSearchFilter() {

        searchField.getDocument().addDocumentListener(
                new DocumentListener() {

                    private void filter() {

                        String keyword =
                                searchField.getText().trim();

                        if (keyword.isEmpty()) {

                            sorter.setRowFilter(null);

                        } else {

                            sorter.setRowFilter(
                                    RowFilter.regexFilter(
                                            "(?i)" +
                                                    java.util.regex.Pattern.quote(
                                                            keyword
                                                    )
                                    )
                            );
                        }

                        updateDisplayedCount();
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        filter();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        filter();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        filter();
                    }
                }
        );
    }

    // =====================================================
    // TABLE ROW ACTIONS
    // =====================================================

    private void installRowActions() {

        table.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {

                        if (e.getClickCount() == 1
                                && SwingUtilities.isLeftMouseButton(e)) {

                            int viewRow = table.rowAtPoint(e.getPoint());

                            if (viewRow >= 0) {

                                table.setRowSelectionInterval(
                                        viewRow,
                                        viewRow
                                );

                                SwingUtilities.invokeLater(
                                        OverdueBooksFrame.this::viewSelectedDetails
                                );
                            }
                        }
                    }
                }
        );

        // Press Enter to open selected record
        table.getInputMap(
                JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        ).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "viewOverdueDetails"
        );

        table.getActionMap().put(
                "viewOverdueDetails",
                new AbstractAction() {

                    @Override
                    public void actionPerformed(
                            java.awt.event.ActionEvent e
                    ) {
                        viewSelectedDetails();
                    }
                }
        );
    }

    // =====================================================
    // SUMMARY VALUE
    // =====================================================

    private JLabel createSummaryValue() {

        JLabel label = new JLabel("0");

        label.setFont(
                new Font("Georgia", Font.BOLD, 29)
        );

        label.setForeground(BURGUNDY);

        return label;
    }

    // =====================================================
    // SUMMARY CARD
    // =====================================================

    private JPanel createSummaryCard(
            String heading,
            JLabel value,
            String description,
            Color accent
    ) {

        JPanel card = new RoundedPanel(
                18,
                WHITE,
                BORDER
        );

        card.setLayout(new BorderLayout(12, 0));

        card.setBorder(
                new EmptyBorder(17, 19, 17, 19)
        );

        JPanel content = new JPanel();

        content.setOpaque(false);

        content.setLayout(
                new BoxLayout(content, BoxLayout.Y_AXIS)
        );

        JLabel headingLabel = new JLabel(heading);

        headingLabel.setFont(
                new Font("Georgia", Font.BOLD, 11)
        );

        headingLabel.setForeground(MUTED);

        JLabel descriptionLabel = new JLabel(description);

        descriptionLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 11)
        );

        descriptionLabel.setForeground(MUTED);

        content.add(headingLabel);
        content.add(Box.createVerticalStrut(9));
        content.add(value);
        content.add(Box.createVerticalStrut(5));
        content.add(descriptionLabel);

        JPanel accentPanel = new JPanel();

        accentPanel.setBackground(accent);

        accentPanel.setPreferredSize(
                new Dimension(5, 55)
        );

        card.add(
                accentPanel,
                BorderLayout.WEST
        );

        card.add(
                content,
                BorderLayout.CENTER
        );

        return card;
    }

    // =====================================================
    // TABLE DESIGN
    // =====================================================

    private void styleTable() {

        table.setFont(BODY_FONT);
        table.setForeground(TEXT);
        table.setBackground(WHITE);

        table.setRowHeight(45);

        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);

        table.setGridColor(
                new Color(240, 235, 230)
        );

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        table.setSelectionBackground(LIGHT_GOLD);
        table.setSelectionForeground(BURGUNDY);

        table.setFillsViewportHeight(true);

        table.setIntercellSpacing(
                new Dimension(0, 1)
        );

        table.setAutoResizeMode(
                JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS
        );

        JTableHeader header = table.getTableHeader();

        header.setFont(
                new Font("Georgia", Font.BOLD, 11)
        );

        header.setBackground(BURGUNDY);
        header.setForeground(WHITE);

        header.setPreferredSize(
                new Dimension(0, 44)
        );

        header.setReorderingAllowed(false);

        header.setBorder(
                BorderFactory.createEmptyBorder()
        );

        // General cell renderer
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

                            component.setBackground(LIGHT_GOLD);
                            component.setForeground(BURGUNDY);

                        } else {

                            component.setBackground(
                                    row % 2 == 0
                                            ? WHITE
                                            : LIGHT_BACKGROUND
                            );

                            component.setForeground(TEXT);
                        }

                        setBorder(
                                new EmptyBorder(0, 10, 0, 10)
                        );

                        setVerticalAlignment(SwingConstants.CENTER);

                        return component;
                    }
                };

        for (int i = 0; i < table.getColumnCount(); i++) {

            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(renderer);
        }

        // Days overdue renderer
        DefaultTableCellRenderer daysRenderer =
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

                        super.getTableCellRendererComponent(
                                table,
                                value,
                                isSelected,
                                hasFocus,
                                row,
                                column
                        );

                        int days = 0;

                        try {
                            days = Integer.parseInt(
                                    String.valueOf(value)
                            );
                        } catch (NumberFormatException ignored) {
                        }

                        setHorizontalAlignment(
                                SwingConstants.CENTER
                        );

                        setFont(
                                new Font("Georgia", Font.BOLD, 12)
                        );

                        setText(days + " days");

                        if (isSelected) {

                            setBackground(LIGHT_GOLD);
                            setForeground(BURGUNDY);

                        } else if (days > 30) {

                            setBackground(LIGHT_RED);
                            setForeground(DARK_RED);

                        } else {

                            setBackground(
                                    row % 2 == 0
                                            ? WHITE
                                            : LIGHT_BACKGROUND
                            );

                            setForeground(RED);
                        }

                        setOpaque(true);

                        setBorder(
                                new EmptyBorder(0, 5, 0, 5)
                        );

                        return this;
                    }
                };

        table.getColumnModel()
                .getColumn(6)
                .setCellRenderer(daysRenderer);

        // Column widths
        table.getColumnModel().getColumn(0)
                .setPreferredWidth(105);

        table.getColumnModel().getColumn(1)
                .setPreferredWidth(175);

        table.getColumnModel().getColumn(2)
                .setPreferredWidth(165);

        table.getColumnModel().getColumn(3)
                .setPreferredWidth(210);

        table.getColumnModel().getColumn(4)
                .setPreferredWidth(140);

        table.getColumnModel().getColumn(5)
                .setPreferredWidth(140);

        table.getColumnModel().getColumn(6)
                .setPreferredWidth(115);
    }

    // =====================================================
    // LOAD OVERDUE BOOKS
    // =====================================================

    private void loadOverdueBooks(String search) {

        tableModel.setRowCount(0);

        String sql = """
                SELECT
                    t.transaction_id,
                    m.full_name,
                    m.student_employee_id,
                    b.title,
                    t.borrowing_date,
                    t.due_date,
                    CAST(
                        julianday(date('now', 'localtime'))
                        - julianday(t.due_date)
                        AS INTEGER
                    ) AS days_overdue
                FROM transactions t
                JOIN members m
                    ON t.member_id = m.member_id
                JOIN books b
                    ON t.book_id = b.book_id
                WHERE t.return_date IS NULL
                  AND t.status IN ('BORROWED', 'OVERDUE')
                  AND t.due_date < date('now', 'localtime')
                  AND (
                      m.full_name LIKE ?
                      OR m.student_employee_id LIKE ?
                      OR b.title LIKE ?
                  )
                ORDER BY t.due_date ASC
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            String keyword = "%" + search + "%";

            statement.setString(1, keyword);
            statement.setString(2, keyword);
            statement.setString(3, keyword);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    tableModel.addRow(
                            new Object[]{
                                    result.getInt("transaction_id"),
                                    result.getString("full_name"),
                                    result.getString("student_employee_id"),
                                    result.getString("title"),
                                    result.getString("borrowing_date"),
                                    result.getString("due_date"),
                                    result.getInt("days_overdue")
                            }
                    );
                }
            }

            updateSummaryCards();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load overdue books:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // UPDATE SUMMARY CARDS
    // =====================================================

    private void updateSummaryCards() {

        int total = tableModel.getRowCount();
        int severe = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {

            int days = Integer.parseInt(
                    String.valueOf(
                            tableModel.getValueAt(i, 6)
                    )
            );

            if (days > 30) {
                severe++;
            }
        }

        totalOverdueValue.setText(
                String.valueOf(total)
        );

        severeOverdueValue.setText(
                String.valueOf(severe)
        );

        updateDisplayedCount();
    }

    // =====================================================
    // DISPLAYED RECORD COUNT
    // =====================================================

    private void updateDisplayedCount() {

        if (displayedValue != null && table != null) {

            displayedValue.setText(
                    String.valueOf(table.getRowCount())
            );
        }
    }

    // =====================================================
    // OVERDUE DETAILS POPUP
    // =====================================================

    private void viewSelectedDetails() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow =
                table.convertRowIndexToModel(selectedRow);

        String[] labels = {
                "Transaction ID",
                "Member Name",
                "Student/Employee ID",
                "Book Title",
                "Borrowing Date",
                "Due Date",
                "Days Overdue"
        };

        int daysOverdue = Integer.parseInt(
                String.valueOf(
                        tableModel.getValueAt(modelRow, 6)
                )
        );

        JDialog dialog = new JDialog(
                this,
                "Overdue Book Details",
                true
        );

        dialog.setSize(560, 520);

        dialog.setMinimumSize(
                new Dimension(500, 470)
        );

        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(CREAM);

        // =================================================
        // POPUP HEADER
        // =================================================

        JPanel header = new JPanel(
                new BorderLayout()
        );

        header.setBackground(BURGUNDY);

        header.setBorder(
                new EmptyBorder(20, 25, 20, 25)
        );

        JPanel headerText = new JPanel();

        headerText.setOpaque(false);

        headerText.setLayout(
                new BoxLayout(headerText, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("Overdue Record");

        title.setFont(
                new Font("Georgia", Font.BOLD, 21)
        );

        title.setForeground(GOLD);

        JLabel subtitle = new JLabel(
                "Borrowing and member information"
        );

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        subtitle.setForeground(WHITE);

        headerText.add(title);
        headerText.add(Box.createVerticalStrut(5));
        headerText.add(subtitle);

        JButton closeIcon = new JButton("×");

        closeIcon.setFont(
                new Font("Segoe UI", Font.PLAIN, 25)
        );

        closeIcon.setForeground(WHITE);

        closeIcon.setFocusPainted(false);
        closeIcon.setBorderPainted(false);
        closeIcon.setContentAreaFilled(false);

        closeIcon.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        closeIcon.addActionListener(e -> dialog.dispose());

        header.add(
                headerText,
                BorderLayout.WEST
        );

        header.add(
                closeIcon,
                BorderLayout.EAST
        );

        // =================================================
        // DETAILS CONTENT
        // =================================================

        JPanel detailsCard = new RoundedPanel(
                18,
                WHITE,
                BORDER
        );

        detailsCard.setLayout(
                new GridLayout(0, 2, 12, 16)
        );

        detailsCard.setBorder(
                new EmptyBorder(22, 22, 22, 22)
        );

        for (int i = 0; i < labels.length; i++) {

            JLabel nameLabel = new JLabel(
                    labels[i]
            );

            nameLabel.setFont(
                    new Font("Georgia", Font.BOLD, 11)
            );

            nameLabel.setForeground(BURGUNDY);

            String value = String.valueOf(
                    tableModel.getValueAt(modelRow, i)
            );

            if (i == 6) {
                value += " days";
            }

            JLabel valueLabel = new JLabel(value);

            valueLabel.setFont(
                    new Font("Segoe UI", Font.PLAIN, 13)
            );

            valueLabel.setForeground(TEXT);

            if (i == 6) {

                valueLabel.setFont(
                        new Font("Georgia", Font.BOLD, 13)
                );

                valueLabel.setForeground(
                        daysOverdue > 30 ? DARK_RED : RED
                );
            }

            detailsCard.add(nameLabel);
            detailsCard.add(valueLabel);
        }

        JPanel content = new JPanel(
                new BorderLayout(0, 15)
        );

        content.setBackground(CREAM);

        content.setBorder(
                new EmptyBorder(22, 22, 15, 22)
        );

        JLabel warningLabel = new JLabel(
                daysOverdue > 30
                        ? "Severely overdue — more than 30 days"
                        : "This book has passed its due date"
        );

        warningLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        warningLabel.setForeground(
                daysOverdue > 30 ? DARK_RED : RED
        );

        warningLabel.setBorder(
                new EmptyBorder(0, 3, 0, 0)
        );

        content.add(
                warningLabel,
                BorderLayout.NORTH
        );

        content.add(
                detailsCard,
                BorderLayout.CENTER
        );

        // =================================================
        // POPUP FOOTER
        // =================================================

        JPanel footer = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 0)
        );

        footer.setBackground(CREAM);

        footer.setBorder(
                new EmptyBorder(0, 22, 20, 22)
        );

        JButton closeButton = new JButton("CLOSE");

        styleButton(
                closeButton,
                BURGUNDY,
                WHITE
        );

        closeButton.addActionListener(
                e -> dialog.dispose()
        );

        footer.add(closeButton);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(content, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
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
                new Font("Georgia", Font.BOLD, 11)
        );

        button.setBackground(background);
        button.setForeground(foreground);

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.setBorder(
                new EmptyBorder(11, 21, 11, 21)
        );
    }

    // =====================================================
    // ROUNDED PANEL
    // =====================================================

    private static class RoundedPanel extends JPanel {

        private final int radius;
        private final Color backgroundColor;
        private final Color borderColor;

        public RoundedPanel(
                int radius,
                Color backgroundColor,
                Color borderColor
        ) {

            this.radius = radius;
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;

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

            int width = getWidth();
            int height = getHeight();

            g2.setColor(backgroundColor);

            g2.fillRoundRect(
                    0,
                    0,
                    width - 1,
                    height - 1,
                    radius,
                    radius
            );

            g2.setColor(borderColor);

            g2.setStroke(
                    new BasicStroke(1f)
            );

            g2.drawRoundRect(
                    0,
                    0,
                    width - 1,
                    height - 1,
                    radius,
                    radius
            );

            g2.dispose();
        }
    }
}
package com.library.ui;

import com.library.service.BorrowRequestService;
import com.library.service.ReturnRequestService;
import com.library.service.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ManageRequestsFrame extends JFrame {

    private final UserSession session;

    // =====================================================
    // THEME
    // =====================================================

    private static final Color MAROON = new Color(74, 14, 23);
    private static final Color DARK_MAROON = new Color(55, 13, 27);
    private static final Color CANVAS = new Color(249, 248, 243);
    private static final Color WHITE = Color.WHITE;
    private static final Color CHARCOAL = new Color(31, 41, 55);
    private static final Color MUTED = new Color(107, 114, 128);
    private static final Color GOLD = new Color(176, 141, 87);
    private static final Color RED = new Color(190, 18, 60);
    private static final Color GREEN = new Color(6, 120, 85);
    private static final Color ORANGE = new Color(245, 158, 11);
    private static final Color BLUE = new Color(29, 78, 216);
    private static final Color BORDER = new Color(229, 231, 235);
    private static final Color ROW_ALT = new Color(249, 250, 251);
    private static final Color SELECTED = new Color(255, 247, 237);

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    // =====================================================
    // BORROW COMPONENTS
    // =====================================================

    private JTable borrowTable;
    private DefaultTableModel borrowModel;
    private TableRowSorter<DefaultTableModel> borrowSorter;
    private JTextField borrowSearch;

    private JLabel borrowPendingValue;
    private JLabel borrowTodayValue;
    private JLabel borrowBooksValue;
    private JLabel borrowDurationValue;

    // =====================================================
    // RETURN COMPONENTS
    // =====================================================

    private JTable returnTable;
    private DefaultTableModel returnModel;
    private TableRowSorter<DefaultTableModel> returnSorter;
    private JTextField returnSearch;

    private JLabel returnPendingValue;
    private JLabel returnTodayValue;
    private JLabel returnDisplayedValue;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public ManageRequestsFrame(UserSession session) {

        if (session == null) {
            throw new SecurityException("Admin session is required.");
        }

        session.requireAdmin();
        this.session = session;

        setTitle("LibraryLink - Manage Requests");
        setSize(1450, 850);
        setMinimumSize(new Dimension(1050, 680));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setBackground(CANVAS);
        setLayout(new BorderLayout());

        add(createTopBar(), BorderLayout.NORTH);
        add(createWorkspace(), BorderLayout.CENTER);

        loadBorrowRequests();
        loadReturnRequests();
    }

    // =====================================================
    // TOP BAR
    // =====================================================

    private JPanel createTopBar() {

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(MAROON);
        topBar.setPreferredSize(new Dimension(0, 65));
        topBar.setBorder(new EmptyBorder(8, 25, 8, 25));

        JButton backButton = new JButton("←");

        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        backButton.setForeground(WHITE);
        backButton.setBackground(MAROON);
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setOpaque(false);
        backButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        backButton.setPreferredSize(new Dimension(45, 45));
        backButton.setToolTipText("Back");

        backButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                backButton.setForeground(GOLD);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                backButton.setForeground(WHITE);
            }
        });

        backButton.addActionListener(e -> dispose());

        topBar.add(backButton, BorderLayout.WEST);

        return topBar;
    }

    // =====================================================
    // WORKSPACE
    // =====================================================

    private JPanel createWorkspace() {

        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(CANVAS);
        workspace.setBorder(new EmptyBorder(15, 25, 25, 25));

        JTabbedPane tabs = new JTabbedPane();

        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(WHITE);
        tabs.setForeground(MAROON);
        tabs.setFocusable(false);

        tabs.addTab("  Borrowing Requests  ", createBorrowPanel());
        tabs.addTab("  Return Requests  ", createReturnPanel());

        workspace.add(tabs, BorderLayout.CENTER);

        return workspace;
    }

    // =====================================================
    // BORROW PANEL
    // =====================================================

    private JPanel createBorrowPanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(CANVAS);
        panel.setBorder(new EmptyBorder(15, 5, 5, 5));

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);

        JPanel searchRow = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 0)
        );
        searchRow.setOpaque(false);

        searchRow.add(createSearchPanel(
                "Search borrowing requests...",
                true
        ));

        top.add(searchRow, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1, 4, 13, 0));
        stats.setOpaque(false);

        borrowPendingValue = createStatValue();
        borrowTodayValue = createStatValue();
        borrowBooksValue = createStatValue();
        borrowDurationValue = createStatValue();

        stats.add(createStatCard(
                "PENDING REQUESTS",
                borrowPendingValue,
                "Awaiting admin review",
                ORANGE
        ));

        stats.add(createStatCard(
                "REQUESTS TODAY",
                borrowTodayValue,
                "Submitted today",
                RED
        ));

        stats.add(createStatCard(
                "BOOKS REQUESTED",
                borrowBooksValue,
                "Pending book requests",
                BLUE
        ));

        stats.add(createStatCard(
                "AVG DURATION",
                borrowDurationValue,
                "Requested borrowing days",
                GREEN
        ));

        top.add(stats, BorderLayout.CENTER);

        createBorrowTable();

        JPanel tableCard = createTableCard(
                "BORROWING REQUEST DIRECTORY",
                "Click a request row to view its complete details.",
                borrowTable
        );

        panel.add(top, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);

        addSearchListener(borrowSearch, borrowSorter, null);

        return panel;
    }

    // =====================================================
    // BORROW TABLE
    // =====================================================

    private void createBorrowTable() {

        String[] columns = {
                "REQUEST ID",
                "BOOK ID",
                "STUDENT NAME",
                "BOOK TITLE",
                "REQUEST DATE",
                "DURATION / PREFERRED RETURN",
                "STATUS",
                "ACTION"
        };

        borrowModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };

        borrowTable = new JTable(borrowModel);

        styleTable(borrowTable);
        borrowTable.setRowHeight(52);

        // Preferred widths are proportional when AUTO_RESIZE_ALL_COLUMNS
        // distributes the available table width.
        int[] widths = {
                75, 75, 165, 185, 155, 205, 100, 190
        };

        setColumnWidths(borrowTable, widths);

        borrowTable.getColumnModel()
                .getColumn(7)
                .setCellRenderer(new ActionCellRenderer());

        borrowTable.getColumnModel()
                .getColumn(7)
                .setCellEditor(new ActionCellEditor(true));

        borrowSorter = new TableRowSorter<>(borrowModel);
        borrowTable.setRowSorter(borrowSorter);

        // Click a row to open its details.
        borrowTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                int viewRow = borrowTable.rowAtPoint(e.getPoint());
                int viewColumn = borrowTable.columnAtPoint(e.getPoint());

                if (viewRow >= 0 && viewColumn != 7) {

                    borrowTable.setRowSelectionInterval(viewRow, viewRow);

                    int modelRow =
                            borrowTable.convertRowIndexToModel(viewRow);

                    showBorrowDetails(modelRow);
                }
            }
        });
    }

    // =====================================================
    // RETURN PANEL
    // =====================================================

    private JPanel createReturnPanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(CANVAS);
        panel.setBorder(new EmptyBorder(15, 5, 5, 5));

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);

        JPanel searchRow = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 0)
        );
        searchRow.setOpaque(false);

        searchRow.add(createSearchPanel(
                "Search return requests...",
                false
        ));

        top.add(searchRow, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1, 3, 13, 0));
        stats.setOpaque(false);

        returnPendingValue = createStatValue();
        returnTodayValue = createStatValue();
        returnDisplayedValue = createStatValue();

        stats.add(createStatCard(
                "PENDING REQUESTS",
                returnPendingValue,
                "Awaiting admin review",
                ORANGE
        ));

        stats.add(createStatCard(
                "REQUESTS TODAY",
                returnTodayValue,
                "Submitted today",
                RED
        ));

        stats.add(createStatCard(
                "DISPLAYED REQUESTS",
                returnDisplayedValue,
                "Based on current search",
                GREEN
        ));

        top.add(stats, BorderLayout.CENTER);

        createReturnTable();

        JPanel tableCard = createTableCard(
                "RETURN REQUEST DIRECTORY",
                "Click a request row to view its complete details.",
                returnTable
        );

        panel.add(top, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);

        addSearchListener(
                returnSearch,
                returnSorter,
                returnDisplayedValue
        );

        return panel;
    }

    // =====================================================
    // RETURN TABLE
    // =====================================================

    private void createReturnTable() {

        String[] columns = {
                "REQUEST ID",
                "STUDENT NAME",
                "BOOK TITLE",
                "BORROW DATE",
                "DUE DATE",
                "STATUS",
                "ACTION"
        };

        returnModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        returnTable = new JTable(returnModel);

        styleTable(returnTable);
        returnTable.setRowHeight(52);

        int[] widths = {
                95, 190, 245, 170, 170, 110, 200
        };

        setColumnWidths(returnTable, widths);

        returnTable.getColumnModel()
                .getColumn(6)
                .setCellRenderer(new ActionCellRenderer());

        returnTable.getColumnModel()
                .getColumn(6)
                .setCellEditor(new ActionCellEditor(false));

        returnSorter = new TableRowSorter<>(returnModel);
        returnTable.setRowSorter(returnSorter);

        // Click a row to open its details.
        returnTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                int viewRow = returnTable.rowAtPoint(e.getPoint());
                int viewColumn = returnTable.columnAtPoint(e.getPoint());

                if (viewRow >= 0 && viewColumn != 6) {

                    returnTable.setRowSelectionInterval(viewRow, viewRow);

                    int modelRow =
                            returnTable.convertRowIndexToModel(viewRow);

                    showReturnDetails(modelRow);
                }
            }
        });
    }

    // =====================================================
    // COLUMN WIDTHS
    // =====================================================

    private void setColumnWidths(JTable table, int[] widths) {

        for (int i = 0; i < widths.length; i++) {

            TableColumn column = table.getColumnModel().getColumn(i);

            column.setPreferredWidth(widths[i]);

            if (i == widths.length - 1) {
                column.setMinWidth(175);
            } else {
                column.setMinWidth(65);
            }
        }

        // Distribute the table width across all columns.
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    // =====================================================
    // TABLE CARD
    // =====================================================

    private JPanel createTableCard(
            String headingText,
            String description,
            JTable table
    ) {

        RoundedPanel card = new RoundedPanel(20, WHITE, BORDER);

        card.setLayout(new BorderLayout(0, 13));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(
                new BoxLayout(headingPanel, BoxLayout.Y_AXIS)
        );

        JLabel heading = new JLabel(headingText);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heading.setForeground(MAROON);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(description);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(4));
        headingPanel.add(subtitle);

        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scroll.getViewport().setBackground(WHITE);
        scroll.getViewport().setOpaque(true);

        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        scroll.getVerticalScrollBar().setUnitIncrement(16);

        table.setFillsViewportHeight(true);

        card.add(headingPanel, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // =====================================================
    // SEARCH PANEL
    // =====================================================

    private JPanel createSearchPanel(
            String placeholder,
            boolean borrowing
    ) {

        RoundedPanel searchPanel = new RoundedPanel(
                14,
                WHITE,
                BORDER
        );

        searchPanel.setLayout(new BorderLayout(9, 0));
        searchPanel.setBorder(new EmptyBorder(0, 12, 0, 12));
        searchPanel.setPreferredSize(new Dimension(340, 42));
        searchPanel.setMinimumSize(new Dimension(260, 42));
        searchPanel.setMaximumSize(new Dimension(340, 42));

        JLabel searchIcon = new JLabel("⌕");
        searchIcon.setFont(
                new Font("Segoe UI Symbol", Font.PLAIN, 24)
        );
        searchIcon.setForeground(MUTED);

        JTextField field = new JTextField();

        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setForeground(CHARCOAL);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        field.setToolTipText(placeholder);
        field.setCaretColor(MAROON);

        if (borrowing) {
            borrowSearch = field;
        } else {
            returnSearch = field;
        }

        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(field, BorderLayout.CENTER);

        return searchPanel;
    }

    // =====================================================
    // STATISTIC VALUE
    // =====================================================

    private JLabel createStatValue() {

        JLabel value = new JLabel("0");

        value.setFont(new Font("Segoe UI", Font.BOLD, 25));
        value.setForeground(CHARCOAL);

        return value;
    }

    // =====================================================
    // STATISTIC CARD
    // =====================================================

    private JPanel createStatCard(
            String headingText,
            JLabel value,
            String description,
            Color accentColor
    ) {

        RoundedPanel card = new RoundedPanel(17, WHITE, BORDER);

        card.setLayout(new BorderLayout(10, 0));
        card.setBorder(new EmptyBorder(12, 14, 12, 14));
        card.setPreferredSize(new Dimension(200, 100));

        JPanel accent = new JPanel();
        accent.setBackground(accentColor);
        accent.setPreferredSize(new Dimension(4, 60));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(
                new BoxLayout(textPanel, BoxLayout.Y_AXIS)
        );

        JLabel heading = new JLabel(headingText);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 10));
        heading.setForeground(MUTED);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        value.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 10)
        );
        descriptionLabel.setForeground(MUTED);
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(heading);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(value);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(descriptionLabel);

        card.add(accent, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    // =====================================================
    // TABLE DESIGN
    // =====================================================

    private void styleTable(JTable table) {

        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(CHARCOAL);
        table.setBackground(WHITE);
        table.setRowHeight(48);

        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(243, 244, 246));

        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        table.setSelectionBackground(SELECTED);
        table.setSelectionForeground(CHARCOAL);
        table.setIntercellSpacing(new Dimension(0, 1));

        // MAROON TABLE HEADER

        JTableHeader header = table.getTableHeader();

        header.setFont(new Font("Segoe UI", Font.BOLD, 10));
        header.setBackground(MAROON);
        header.setForeground(WHITE);
        header.setOpaque(true);
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean selected,
                            boolean focus,
                            int row,
                            int column
                    ) {

                        JLabel label = (JLabel)
                                super.getTableCellRendererComponent(
                                        table,
                                        value,
                                        selected,
                                        focus,
                                        row,
                                        column
                                );

                        label.setBackground(MAROON);
                        label.setForeground(WHITE);
                        label.setFont(
                                new Font("Segoe UI", Font.BOLD, 10)
                        );
                        label.setOpaque(true);
                        label.setHorizontalAlignment(
                                SwingConstants.CENTER
                        );
                        label.setBorder(
                                new EmptyBorder(0, 7, 0, 7)
                        );

                        return label;
                    }
                }
        );

        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean selected,
                            boolean focus,
                            int row,
                            int column
                    ) {

                        JLabel label = (JLabel)
                                super.getTableCellRendererComponent(
                                        table,
                                        value,
                                        selected,
                                        focus,
                                        row,
                                        column
                                );

                        label.setBorder(
                                new EmptyBorder(0, 9, 0, 9)
                        );

                        if (selected) {
                            label.setBackground(SELECTED);
                            label.setForeground(CHARCOAL);
                        } else {
                            label.setBackground(
                                    row % 2 == 0 ? WHITE : ROW_ALT
                            );
                            label.setForeground(CHARCOAL);
                        }

                        label.setOpaque(true);

                        return label;
                    }
                };

        for (int i = 0; i < table.getColumnCount(); i++) {

            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(renderer);
        }
    }

    // =====================================================
    // ACTION BUTTON RENDERER
    // =====================================================

    private class ActionCellRenderer implements TableCellRenderer {

        private final JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 5, 8)
        );

        private final JButton approveButton =
                createSmallButton("Approve", GREEN);

        private final JButton rejectButton =
                createSmallButton("Reject", RED);

        ActionCellRenderer() {

            panel.setOpaque(true);
            panel.add(approveButton);
            panel.add(rejectButton);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean selected,
                boolean focus,
                int row,
                int column
        ) {

            panel.setBackground(
                    selected ? SELECTED :
                            (row % 2 == 0 ? WHITE : ROW_ALT)
            );

            return panel;
        }
    }

    // =====================================================
    // ACTION BUTTON EDITOR
    // =====================================================

    private class ActionCellEditor extends AbstractCellEditor
            implements TableCellEditor {

        private final JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 5, 8)
        );

        private final JButton approveButton =
                createSmallButton("Approve", GREEN);

        private final JButton rejectButton =
                createSmallButton("Reject", RED);

        private final boolean borrowing;

        private int modelRow = -1;

        ActionCellEditor(boolean borrowing) {

            this.borrowing = borrowing;

            panel.setBackground(WHITE);
            panel.add(approveButton);
            panel.add(rejectButton);

            approveButton.addActionListener(e -> {

                int selectedRow = modelRow;

                fireEditingStopped();

                SwingUtilities.invokeLater(() -> {

                    if (borrowing) {
                        approveBorrowRequest(selectedRow);
                    } else {
                        approveReturnRequest(selectedRow);
                    }
                });
            });

            rejectButton.addActionListener(e -> {

                int selectedRow = modelRow;

                fireEditingStopped();

                SwingUtilities.invokeLater(() -> {

                    if (borrowing) {
                        rejectBorrowRequest(selectedRow);
                    } else {
                        rejectReturnRequest(selectedRow);
                    }
                });
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean selected,
                int row,
                int column
        ) {

            modelRow = table.convertRowIndexToModel(row);
            panel.setBackground(WHITE);

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "ACTION";
        }
    }

    // =====================================================
    // ROUNDED SMALL ACTION BUTTON
    // =====================================================

    private JButton createSmallButton(
            String text,
            Color background
    ) {

        RoundedButton button = new RoundedButton(text, 10);

        button.setFont(new Font("Segoe UI", Font.BOLD, 10));
        button.setForeground(WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.setPreferredSize(new Dimension(78, 30));
        button.setToolTipText(text);

        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(background.brighter());
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(background);
                button.repaint();
            }
        });

        return button;
    }

    // =====================================================
    // SEARCH FILTER
    // =====================================================

    private void addSearchListener(
            JTextField field,
            TableRowSorter<DefaultTableModel> sorter,
            JLabel displayedLabel
    ) {

        field.getDocument().addDocumentListener(
                new DocumentListener() {

                    private void filter() {

                        String search = field.getText().trim();

                        if (search.isEmpty()) {
                            sorter.setRowFilter(null);
                        } else {
                            sorter.setRowFilter(
                                    RowFilter.regexFilter(
                                            "(?i)" +
                                                    java.util.regex.Pattern.quote(search)
                                    )
                            );
                        }

                        if (displayedLabel != null) {
                            displayedLabel.setText(
                                    String.valueOf(sorter.getViewRowCount())
                            );
                        }
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
    // LOAD BORROW REQUESTS
    // =====================================================

    private void loadBorrowRequests() {

        if (borrowModel == null) {
            return;
        }

        borrowModel.setRowCount(0);

        try {

            session.requireAdmin();

            List<BorrowRequestService.BorrowRequest> requests =
                    BorrowRequestService.getPendingRequests(session);

            int todayCount = 0;
            long totalDuration = 0;

            for (BorrowRequestService.BorrowRequest request : requests) {

                String preferredReturn =
                        formatDate(request.requestedReturnDate());

                String durationText =
                        request.borrowingDuration() + " days / "
                                + preferredReturn;

                borrowModel.addRow(new Object[]{
                        request.requestId(),
                        request.bookId(),
                        request.memberName(),
                        request.bookTitle(),
                        formatDateTime(request.requestDate()),
                        durationText,
                        "PENDING",
                        "ACTION"
                });

                totalDuration += request.borrowingDuration();

                if (isToday(request.requestDate())) {
                    todayCount++;
                }
            }

            borrowPendingValue.setText(
                    String.valueOf(requests.size())
            );

            borrowTodayValue.setText(
                    String.valueOf(todayCount)
            );

            borrowBooksValue.setText(
                    String.valueOf(requests.size())
            );

            long average = requests.isEmpty()
                    ? 0
                    : Math.round(
                    (double) totalDuration / requests.size()
            );

            borrowDurationValue.setText(average + " days");

            applyFilter(borrowSearch, borrowSorter);

        } catch (Exception e) {

            showError(
                    "Unable to load borrowing requests.\n" + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // LOAD RETURN REQUESTS
    // =====================================================

    private void loadReturnRequests() {

        if (returnModel == null) {
            return;
        }

        returnModel.setRowCount(0);

        try {

            session.requireAdmin();

            List<ReturnRequestService.ReturnRequest> requests =
                    ReturnRequestService.getPendingRequests(session);

            int todayCount = 0;

            for (ReturnRequestService.ReturnRequest request : requests) {

                returnModel.addRow(new Object[]{
                        request.returnRequestId(),
                        request.memberName(),
                        request.bookTitle(),
                        formatDate(request.borrowingDate()),
                        formatDate(request.dueDate()),
                        "PENDING",
                        "ACTION"
                });

                if (isToday(request.requestDate())) {
                    todayCount++;
                }
            }

            returnPendingValue.setText(
                    String.valueOf(requests.size())
            );

            returnTodayValue.setText(
                    String.valueOf(todayCount)
            );

            applyFilter(returnSearch, returnSorter);

        } catch (Exception e) {

            showError(
                    "Unable to load return requests.\n" + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // APPLY FILTER
    // =====================================================

    private void applyFilter(
            JTextField field,
            TableRowSorter<DefaultTableModel> sorter
    ) {

        if (field == null || sorter == null) {
            return;
        }

        String search = field.getText().trim();

        if (search.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(
                    RowFilter.regexFilter(
                            "(?i)" +
                                    java.util.regex.Pattern.quote(search)
                    )
            );
        }

        if (sorter == returnSorter
                && returnDisplayedValue != null) {

            returnDisplayedValue.setText(
                    String.valueOf(sorter.getViewRowCount())
            );
        }
    }

    // =====================================================
    // BORROW DETAILS
    // =====================================================

    private void showBorrowDetails(int row) {

        if (row < 0 || row >= borrowModel.getRowCount()) {
            return;
        }

        showDetailsDialog(
                "Borrowing Request Details",
                new String[]{
                        "Request ID",
                        "Book ID",
                        "Student Name",
                        "Book Title",
                        "Request Date",
                        "Duration / Preferred Return",
                        "Status"
                },
                new Object[]{
                        borrowModel.getValueAt(row, 0),
                        borrowModel.getValueAt(row, 1),
                        borrowModel.getValueAt(row, 2),
                        borrowModel.getValueAt(row, 3),
                        borrowModel.getValueAt(row, 4),
                        borrowModel.getValueAt(row, 5),
                        borrowModel.getValueAt(row, 6)
                }
        );
    }

    // =====================================================
    // RETURN DETAILS
    // =====================================================

    private void showReturnDetails(int row) {

        if (row < 0 || row >= returnModel.getRowCount()) {
            return;
        }

        showDetailsDialog(
                "Return Request Details",
                new String[]{
                        "Request ID",
                        "Student Name",
                        "Book Title",
                        "Borrow Date",
                        "Due Date",
                        "Status"
                },
                new Object[]{
                        returnModel.getValueAt(row, 0),
                        returnModel.getValueAt(row, 1),
                        returnModel.getValueAt(row, 2),
                        returnModel.getValueAt(row, 3),
                        returnModel.getValueAt(row, 4),
                        returnModel.getValueAt(row, 5)
                }
        );
    }

    // =====================================================
    // DETAILS DIALOG
    // =====================================================

    private void showDetailsDialog(
            String title,
            String[] labels,
            Object[] values
    ) {

        JDialog dialog = new JDialog(this, title, true);

        dialog.setSize(520, 470);
        dialog.setMinimumSize(new Dimension(420, 350));
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(WHITE);
        content.setBorder(new EmptyBorder(24, 26, 20, 26));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 19));
        heading.setForeground(MAROON);

        JPanel details = new JPanel(new GridLayout(0, 2, 12, 17));
        details.setBackground(WHITE);
        details.setBorder(new EmptyBorder(8, 2, 8, 2));

        for (int i = 0; i < labels.length; i++) {

            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setForeground(MUTED);

            JLabel value = new JLabel(
                    values[i] == null ? "N/A" : String.valueOf(values[i])
            );

            value.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            value.setForeground(CHARCOAL);

            details.add(label);
            details.add(value);
        }

        JScrollPane scroll = new JScrollPane(details);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(WHITE);

        JButton close = createSmallButton("Close", MAROON);
        close.setPreferredSize(new Dimension(100, 36));
        close.addActionListener(e -> dialog.dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(WHITE);
        footer.add(close);

        content.add(heading, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);

        dialog.add(content);
        dialog.setVisible(true);
    }

    // =====================================================
    // APPROVE BORROW REQUEST
    // =====================================================

    private void approveBorrowRequest(int row) {

        if (row < 0 || row >= borrowModel.getRowCount()) {
            return;
        }

        Object requestId = borrowModel.getValueAt(row, 0);
        Object studentName = borrowModel.getValueAt(row, 2);
        Object bookTitle = borrowModel.getValueAt(row, 3);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Approve this borrowing request?\n\n"
                        + "Student: " + studentName + "\n"
                        + "Book: " + bookTitle,
                "Confirm Borrowing Approval",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            session.requireAdmin();

            BorrowRequestService.approveRequest(
                    session,
                    Integer.parseInt(String.valueOf(requestId))
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Borrowing request approved successfully.",
                    "Request Approved",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadBorrowRequests();

        } catch (Exception e) {

            showError(
                    "Unable to approve borrowing request.\n" + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // REJECT BORROW REQUEST
    // =====================================================

    private void rejectBorrowRequest(int row) {

        if (row < 0 || row >= borrowModel.getRowCount()) {
            return;
        }

        Object requestId = borrowModel.getValueAt(row, 0);

        String reason = askForRejectionReason();

        if (reason == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reject this borrowing request?",
                "Confirm Rejection",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            session.requireAdmin();

            BorrowRequestService.rejectRequest(
                    session,
                    Integer.parseInt(String.valueOf(requestId)),
                    reason
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Borrowing request rejected successfully.",
                    "Request Rejected",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadBorrowRequests();

        } catch (Exception e) {

            showError(
                    "Unable to reject borrowing request.\n" + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // APPROVE RETURN REQUEST
    // =====================================================

    private void approveReturnRequest(int row) {

        if (row < 0 || row >= returnModel.getRowCount()) {
            return;
        }

        Object requestId = returnModel.getValueAt(row, 0);
        Object studentName = returnModel.getValueAt(row, 1);
        Object bookTitle = returnModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Approve this return request?\n\n"
                        + "Student: " + studentName + "\n"
                        + "Book: " + bookTitle
                        + "\n\nPlease confirm that the returned book has been inspected.",
                "Confirm Return Approval",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            session.requireAdmin();

            ReturnRequestService.approveReturnRequest(
                    session,
                    Integer.parseInt(String.valueOf(requestId))
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Return request approved successfully.",
                    "Return Approved",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadReturnRequests();

        } catch (Exception e) {

            showError(
                    "Unable to approve return request.\n" + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // REJECT RETURN REQUEST
    // =====================================================

    private void rejectReturnRequest(int row) {

        if (row < 0 || row >= returnModel.getRowCount()) {
            return;
        }

        Object requestId = returnModel.getValueAt(row, 0);

        String reason = askForRejectionReason();

        if (reason == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reject this return request?",
                "Confirm Rejection",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            session.requireAdmin();

            ReturnRequestService.rejectReturnRequest(
                    session,
                    Integer.parseInt(String.valueOf(requestId)),
                    reason
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Return request rejected successfully.",
                    "Return Rejected",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadReturnRequests();

        } catch (Exception e) {

            showError(
                    "Unable to reject return request.\n" + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // REJECTION REASON
    // =====================================================

    private String askForRejectionReason() {

        JTextArea reasonArea = new JTextArea(5, 30);

        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reasonArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(reasonArea);

        int option = JOptionPane.showConfirmDialog(
                this,
                scroll,
                "Enter Reason for Rejection",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return null;
        }

        String reason = reasonArea.getText().trim();

        if (reason.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please provide a reason for rejecting this request.",
                    "Reason Required",
                    JOptionPane.WARNING_MESSAGE
            );

            return null;
        }

        return reason;
    }

    // =====================================================
    // DATE HELPERS
    // =====================================================

    private String formatDate(Object value) {

        if (value == null) {
            return "N/A";
        }

        try {

            if (value instanceof LocalDate date) {
                return date.format(DATE_FORMAT);
            }

            String text = value.toString();

            if (text.length() >= 10) {

                LocalDate date = LocalDate.parse(
                        text.substring(0, 10)
                );

                return date.format(DATE_FORMAT);
            }

            return text;

        } catch (Exception e) {
            return value.toString();
        }
    }

    private String formatDateTime(Object value) {

        if (value == null) {
            return "N/A";
        }

        String text = value.toString().replace("T", " ");

        if (text.length() > 16) {
            return text.substring(0, 16);
        }

        return text;
    }

    private boolean isToday(Object value) {

        if (value == null) {
            return false;
        }

        try {

            String text = value.toString();

            if (text.length() < 10) {
                return false;
            }

            LocalDate date = LocalDate.parse(
                    text.substring(0, 10)
            );

            return date.equals(LocalDate.now());

        } catch (Exception e) {
            return false;
        }
    }

    // =====================================================
    // ERROR MESSAGE
    // =====================================================

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "LibraryLink Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // =====================================================
    // ROUNDED BUTTON
    // =====================================================

    private static class RoundedButton extends JButton {

        private final int radius;

        public RoundedButton(String text, int radius) {
            super(text);
            this.radius = radius;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {

            Graphics2D g2 = (Graphics2D) graphics.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(getBackground());

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    radius,
                    radius
            );

            g2.dispose();

            super.paintComponent(graphics);
        }
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
        protected void paintComponent(Graphics graphics) {

            Graphics2D g2 = (Graphics2D) graphics.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(backgroundColor);

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius,
                    radius
            );

            if (borderColor != null) {

                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));

                g2.drawRoundRect(
                        0,
                        0,
                        getWidth() - 1,
                        getHeight() - 1,
                        radius,
                        radius
                );
            }

            g2.dispose();

            super.paintComponent(graphics);
        }
    }
}
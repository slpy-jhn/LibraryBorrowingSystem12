
package com.library.ui;

import com.library.service.MemberService;
import com.library.service.MemberBorrowingHistoryService;
import com.library.service.MemberBorrowingHistoryService.BorrowingRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ManageMembersFrame extends JFrame {

    private final DefaultTableModel tableModel;
    private final JTable memberTable;

    private final JTextField searchField = new JTextField(20);
    private final JLabel paginationLabel = new JLabel();

    private final JLabel totalLabel;
    private final JLabel activeLabel;
    private final JLabel inactiveLabel;
    private final JLabel selectionLabel;

    private final Color BURGUNDY = new Color(92, 22, 35);
    private final Color DARK_BURGUNDY = new Color(65, 15, 26);
    private final Color GOLD = new Color(197, 160, 89);
    private final Color CREAM = new Color(251, 249, 245);
    private final Color WHITE = Color.WHITE;
    private final Color TEXT = new Color(31, 25, 26);
    private final Color MUTED = new Color(122, 106, 108);
    private final Color BORDER = new Color(229, 222, 214);
    private final Color LIGHT_BURGUNDY = new Color(250, 240, 241);
    private final Color LIGHT_GOLD = new Color(249, 243, 230);

    public ManageMembersFrame() {

        setTitle("LibraryLink - Manage Members");
        setSize(1400, 850);
        setMinimumSize(new Dimension(1050, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());

        // HEADER

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Manage Library Members");
        title.setFont(new Font("Georgia", Font.BOLD, 27));
        title.setForeground(GOLD);

        JLabel subtitle = new JLabel(
                "Register, search and manage member accounts"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(245, 231, 220));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        // TRANSPARENT HEADER ICON BUTTONS

        JButton refreshButton = createHeaderIconButton(
                "refresh",
                "Refresh members"
        );

        JButton backButton = createHeaderIconButton(
                "back",
                "Back"
        );

        refreshButton.addActionListener(e -> {

            if (searchField.getText().isEmpty()) {
                loadMembers();
            } else {
                searchField.setText("");
            }
        });

        backButton.addActionListener(e -> dispose());

        JPanel headerButtons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        headerButtons.setOpaque(false);

        headerButtons.add(refreshButton);
        headerButtons.add(backButton);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(headerButtons, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // STATISTICS CARDS

        JPanel statisticsPanel = new JPanel(
                new GridLayout(1, 3, 18, 0)
        );
        statisticsPanel.setOpaque(false);

        totalLabel = createNumberLabel();
        activeLabel = createNumberLabel();
        inactiveLabel = createNumberLabel();

        statisticsPanel.add(createStatCard(
                "TOTAL REGISTERED MEMBERS",
                totalLabel,
                "users",
                LIGHT_BURGUNDY,
                BURGUNDY
        ));

        statisticsPanel.add(createStatCard(
                "CURRENTLY ACTIVE MEMBERS",
                activeLabel,
                "user-check",
                new Color(232, 245, 233),
                new Color(36, 120, 72)
        ));

        statisticsPanel.add(createStatCard(
                "CURRENTLY INACTIVE MEMBERS",
                inactiveLabel,
                "user-x",
                new Color(240, 238, 236),
                MUTED
        ));

        JPanel statisticsContainer = new JPanel(new BorderLayout());
        statisticsContainer.setOpaque(false);
        statisticsContainer.setBorder(
                new EmptyBorder(20, 25, 18, 25)
        );
        statisticsContainer.add(statisticsPanel, BorderLayout.CENTER);

        // MEMBER TABLE

        String[] columns = {
                "Member ID",
                "Full Name",
                "Student/Employee ID",
                "Course / Department",
                "Contact Number",
                "Email",
                "Registration Date",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        memberTable = new JTable(tableModel);

        memberTable.setRowHeight(43);
        memberTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        memberTable.setForeground(TEXT);
        memberTable.setBackground(WHITE);
        memberTable.setSelectionBackground(LIGHT_BURGUNDY);
        memberTable.setSelectionForeground(BURGUNDY);
        memberTable.setGridColor(BORDER);
        memberTable.setShowVerticalLines(false);
        memberTable.setShowHorizontalLines(true);
        memberTable.setIntercellSpacing(new Dimension(0, 1));
        memberTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        memberTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        memberTable.setAutoCreateRowSorter(true);
        memberTable.setFillsViewportHeight(true);

        memberTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 11)
        );
        memberTable.getTableHeader().setBackground(BURGUNDY);
        memberTable.getTableHeader().setForeground(WHITE);
        memberTable.getTableHeader().setPreferredSize(
                new Dimension(0, 46)
        );
        memberTable.getTableHeader().setReorderingAllowed(false);

        setColumnWidths();

        DefaultTableCellRenderer cellRenderer =
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

                        setBorder(new EmptyBorder(0, 9, 0, 9));

                        if (isSelected) {
                            component.setBackground(LIGHT_BURGUNDY);
                            component.setForeground(BURGUNDY);
                        } else {
                            component.setBackground(
                                    row % 2 == 0
                                            ? WHITE
                                            : new Color(250, 248, 244)
                            );
                            component.setForeground(TEXT);
                        }

                        setFont(new Font(
                                "Segoe UI",
                                column == 7 ? Font.BOLD : Font.PLAIN,
                                column == 7 ? 11 : 12
                        ));

                        return component;
                    }
                };

        for (int i = 0; i < memberTable.getColumnCount(); i++) {
            memberTable.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(cellRenderer);
        }

        memberTable.getColumnModel()
                .getColumn(7)
                .setCellRenderer(new StatusRenderer());

        // TABLE SCROLL PANE

        JScrollPane tableScrollPane = new JScrollPane(memberTable);

        tableScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_NEVER
        );

        tableScrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        // MEMBER DIRECTORY CARD

        JPanel tableCard = new JPanel(new BorderLayout(0, 13));
        tableCard.setBackground(WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel directoryTitle = new JLabel("Member Directory");
        directoryTitle.setFont(new Font("Georgia", Font.BOLD, 20));
        directoryTitle.setForeground(BURGUNDY);

        JLabel directorySubtitle = new JLabel(
                "Select a member to view details or manage their account."
        );
        directorySubtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        directorySubtitle.setForeground(MUTED);

        JPanel directoryHeading = new JPanel(new BorderLayout());
        directoryHeading.setOpaque(false);

        JPanel directoryText = new JPanel();
        directoryText.setOpaque(false);
        directoryText.setLayout(
                new BoxLayout(directoryText, BoxLayout.Y_AXIS)
        );

        directoryText.add(directoryTitle);
        directoryText.add(Box.createVerticalStrut(5));
        directoryText.add(directorySubtitle);

        selectionLabel = new JLabel("No member selected");
        selectionLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        selectionLabel.setForeground(BURGUNDY);

        directoryHeading.add(directoryText, BorderLayout.WEST);
        directoryHeading.add(selectionLabel, BorderLayout.EAST);

        // DIRECTORY TOOLBAR

        JPanel directoryToolbar = createDirectoryToolbar();

        JPanel directoryHeader = new JPanel();
        directoryHeader.setOpaque(false);
        directoryHeader.setLayout(
                new BoxLayout(directoryHeader, BoxLayout.Y_AXIS)
        );

        directoryHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        directoryToolbar.setAlignmentX(Component.LEFT_ALIGNMENT);

        directoryHeader.add(directoryHeading);
        directoryHeader.add(Box.createVerticalStrut(16));
        directoryHeader.add(directoryToolbar);

        tableCard.add(directoryHeader, BorderLayout.NORTH);
        tableCard.add(tableScrollPane, BorderLayout.CENTER);

        // MEMBER SELECTION

        memberTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionLabel();
            }
        });

        // SINGLE-CLICK MEMBER ACTION POPUP

        memberTable.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {

                if (e.getClickCount() == 1 &&
                        SwingUtilities.isLeftMouseButton(e)) {

                    int row = memberTable.rowAtPoint(e.getPoint());

                    if (row >= 0) {
                        memberTable.setRowSelectionInterval(row, row);
                        showMemberActionsPopup(e);
                    }
                }
            }
        });

        // SCROLLABLE LOWER CONTENT

        JPanel scrollContent = new JPanel();
        scrollContent.setBackground(CREAM);
        scrollContent.setLayout(
                new BoxLayout(scrollContent, BoxLayout.Y_AXIS)
        );
        scrollContent.setBorder(
                new EmptyBorder(0, 25, 20, 25)
        );

        tableCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        scrollContent.add(tableCard);

        JScrollPane pageScrollPane = new JScrollPane(
                scrollContent,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        pageScrollPane.setBorder(BorderFactory.createEmptyBorder());
        pageScrollPane.setBackground(CREAM);
        pageScrollPane.getViewport().setBackground(CREAM);
        pageScrollPane.setWheelScrollingEnabled(true);
        pageScrollPane.getVerticalScrollBar().setUnitIncrement(20);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(CREAM);
        mainPanel.add(statisticsContainer, BorderLayout.NORTH);
        mainPanel.add(pageScrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        paginationLabel.setText(
                "Showing 0 members listed | All members"
        );

        loadMembers();
    }

    // COLUMN WIDTHS

    private void setColumnWidths() {

        int[] widths = {
                65, 150, 125, 145, 115, 175, 115, 90
        };

        for (int i = 0; i < widths.length; i++) {
            TableColumn column =
                    memberTable.getColumnModel().getColumn(i);

            column.setPreferredWidth(widths[i]);
            column.setMinWidth(45);
        }
    }

    // STATISTICS

    private JLabel createNumberLabel() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("Georgia", Font.BOLD, 29));
        label.setForeground(BURGUNDY);
        return label;
    }

    private JPanel createStatCard(
            String heading,
            JLabel valueLabel,
            String iconType,
            Color iconBackground,
            Color iconColor
    ) {

        JPanel card = new RoundedPanel(
                20,
                WHITE,
                BORDER
        );

        card.setLayout(new BorderLayout(12, 5));
        card.setBorder(new EmptyBorder(17, 18, 17, 18));

        JPanel iconTile = new JPanel(new GridBagLayout());
        iconTile.setBackground(iconBackground);
        iconTile.setPreferredSize(new Dimension(42, 42));

        JLabel iconLabel = new JLabel(
                new VectorIcon(iconType, iconColor, 23)
        );

        iconTile.add(iconLabel);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(
                new BoxLayout(textPanel, BoxLayout.Y_AXIS)
        );

        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 10)
        );
        headingLabel.setForeground(MUTED);

        textPanel.add(headingLabel);
        textPanel.add(Box.createVerticalStrut(7));
        textPanel.add(valueLabel);

        JLabel watermark = new JLabel("L");
        watermark.setFont(new Font("Georgia", Font.BOLD, 48));
        watermark.setForeground(new Color(240, 233, 222));

        card.add(iconTile, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        card.add(watermark, BorderLayout.EAST);

        return card;
    }

    // DIRECTORY TOOLBAR

    private JPanel createDirectoryToolbar() {

        JPanel toolbar = new JPanel(new BorderLayout(12, 8));
        toolbar.setBackground(WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 13, 10, 13)
        ));

        JPanel searchPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 5)
        );
        searchPanel.setOpaque(false);

        searchField.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );
        searchField.setPreferredSize(new Dimension(260, 35));
        searchField.setBackground(WHITE);
        searchField.setForeground(TEXT);
        searchField.setBorder(
                BorderFactory.createEmptyBorder(5, 10, 5, 5)
        );

        searchField.setToolTipText(
                "Search by name, ID, course or email"
        );

        searchField.putClientProperty(
                "JTextField.placeholderText",
                "Name, ID, course or email..."
        );

        // SEARCH FIELD WITH EMBEDDED SEARCH ICON

        JPanel searchContainer = new JPanel(new BorderLayout(5, 0));
        searchContainer.setBackground(WHITE);
        searchContainer.setPreferredSize(new Dimension(300, 41));

        searchContainer.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER, 24),
                new EmptyBorder(2, 8, 2, 9)
        ));

        JButton searchIconButton = new JButton(
                new VectorIcon("search", BURGUNDY, 18)
        );

        searchIconButton.setToolTipText("Search members");
        searchIconButton.setPreferredSize(new Dimension(30, 30));
        searchIconButton.setBorderPainted(false);
        searchIconButton.setContentAreaFilled(false);
        searchIconButton.setFocusPainted(false);
        searchIconButton.setOpaque(false);
        searchIconButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        searchContainer.add(searchField, BorderLayout.CENTER);
        searchContainer.add(searchIconButton, BorderLayout.EAST);

        // REGISTER MEMBER BUTTON

        JButton addButton = createButton(
                "Register Member",
                new VectorIcon("user-plus", TEXT, 17),
                GOLD,
                TEXT
        );

        addButton.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(GOLD, 24),
                new EmptyBorder(10, 17, 10, 17)
        ));

        searchPanel.add(searchContainer);

        JPanel registerPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 5, 5)
        );
        registerPanel.setOpaque(false);
        registerPanel.add(addButton);

        JPanel controlsPanel = new JPanel(new BorderLayout());
        controlsPanel.setOpaque(false);
        controlsPanel.add(searchPanel, BorderLayout.WEST);
        controlsPanel.add(registerPanel, BorderLayout.EAST);

        paginationLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 11)
        );
        paginationLabel.setForeground(MUTED);

        JLabel pageLabel = new JLabel("All Members");
        pageLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 11)
        );
        pageLabel.setForeground(BURGUNDY);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(7, 5, 0, 5));

        footer.add(paginationLabel, BorderLayout.WEST);
        footer.add(pageLabel, BorderLayout.EAST);

        toolbar.add(controlsPanel, BorderLayout.CENTER);
        toolbar.add(footer, BorderLayout.SOUTH);

        // SEARCH ICON ACTION

        searchIconButton.addActionListener(e -> searchMembers());

        // ENTER KEY SEARCH

        searchField.addActionListener(e -> searchMembers());

        // AUTOMATIC SEARCH WHILE TYPING

        searchField.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        searchMembers();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        searchMembers();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        searchMembers();
                    }
                }
        );

        addButton.addActionListener(e -> showMemberDialog(null));

        return toolbar;
    }

    // TRANSPARENT HEADER ICON BUTTONS

    private JButton createHeaderIconButton(
            String iconType,
            String tooltip
    ) {

        JButton button = new JButton(
                new VectorIcon(iconType, GOLD, 21)
        );

        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(42, 42));

        button.setBackground(BURGUNDY);
        button.setForeground(GOLD);

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    // SINGLE-CLICK MEMBER ACTION POPUP

    private void showMemberActionsPopup(
            java.awt.event.MouseEvent event
    ) {

        JPopupMenu popupMenu = new JPopupMenu();

        popupMenu.setBackground(WHITE);
        popupMenu.setBorder(BorderFactory.createLineBorder(BORDER));

        JMenuItem detailsItem = new JMenuItem(
                "View Details",
                new VectorIcon("eye", BURGUNDY, 17)
        );

        JMenuItem editItem = new JMenuItem(
                "Edit Member",
                new VectorIcon("edit", BURGUNDY, 17)
        );

        JMenuItem historyItem = new JMenuItem(
                "Borrowing History",
                new VectorIcon("history", BURGUNDY, 17)
        );

        JMenuItem statusItem = new JMenuItem(
                "Activate / Deactivate",
                new VectorIcon("user-minus", BURGUNDY, 17)
        );

        JMenuItem[] menuItems = {
                detailsItem,
                editItem,
                historyItem,
                statusItem
        };

        for (JMenuItem item : menuItems) {
            item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            item.setForeground(TEXT);
            item.setBackground(WHITE);
            item.setBorder(new EmptyBorder(9, 12, 9, 12));
            item.setIconTextGap(10);
            item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        detailsItem.addActionListener(e -> viewSelectedMember());
        editItem.addActionListener(e -> editSelectedMember());
        historyItem.addActionListener(e -> viewBorrowingHistory());
        statusItem.addActionListener(e -> toggleMemberStatus());

        popupMenu.add(detailsItem);
        popupMenu.add(editItem);
        popupMenu.addSeparator();
        popupMenu.add(historyItem);
        popupMenu.addSeparator();
        popupMenu.add(statusItem);

        popupMenu.show(
                memberTable,
                event.getX(),
                event.getY()
        );
    }

    // BUTTON DESIGN

    private JButton createButton(
            String text,
            Icon icon,
            Color background,
            Color foreground
    ) {

        JButton button = new JButton(text);

        button.setIcon(icon);
        button.setIconTextGap(8);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(
                        background.equals(WHITE) ? BORDER : background,
                        24
                ),
                new EmptyBorder(10, 17, 10, 17)
        ));

        button.setOpaque(true);
        button.setContentAreaFilled(true);

        button.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(background.darker());
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(background);
            }
        });

        return button;
    }

    // LOAD MEMBERS

    private void loadMembers() {
        try {
            displayMembers(MemberService.getAllMembers());
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // SEARCH MEMBERS

    private void searchMembers() {

        try {
            String keyword = searchField.getText().trim();

            if (keyword.isEmpty()) {
                loadMembers();
                return;
            }

            displayMembers(MemberService.searchMembers(keyword));

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // DISPLAY MEMBERS

    private void displayMembers(List<MemberService.Member> members) {

        tableModel.setRowCount(0);

        int activeCount = 0;
        int inactiveCount = 0;

        for (MemberService.Member member : members) {

            String status = member.getMembershipStatus();

            if (status != null &&
                    status.equalsIgnoreCase("ACTIVE")) {
                activeCount++;
            } else {
                inactiveCount++;
            }

            tableModel.addRow(new Object[]{
                    member.getMemberId(),
                    member.getFullName(),
                    member.getStudentEmployeeId(),
                    member.getCourseDepartment(),
                    member.getContactNumber(),
                    member.getEmail(),
                    member.getRegistrationDate(),
                    status
            });
        }

        totalLabel.setText(String.valueOf(members.size()));
        activeLabel.setText(String.valueOf(activeCount));
        inactiveLabel.setText(String.valueOf(inactiveCount));

        paginationLabel.setText(
                "Showing " + members.size() +
                        " members listed | All members"
        );

        int tableHeight = Math.max(
                320,
                memberTable.getRowHeight() * members.size() + 48
        );

        memberTable.setPreferredScrollableViewportSize(
                new Dimension(1100, tableHeight)
        );

        memberTable.revalidate();
        memberTable.repaint();

        updateSelectionLabel();
    }

    // SELECTED MEMBER INDICATOR

    private void updateSelectionLabel() {

        int selectedRow = memberTable.getSelectedRow();

        if (selectedRow < 0) {
            selectionLabel.setText("No member selected");
            return;
        }

        int modelRow =
                memberTable.convertRowIndexToModel(selectedRow);

        String name = String.valueOf(
                tableModel.getValueAt(modelRow, 1)
        );

        selectionLabel.setText(
                "1 member selected (" + name + ")"
        );
    }

    // GET SELECTED MEMBER

    private MemberService.Member getSelectedMember() {

        int selectedRow = memberTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a member first.",
                    "No Member Selected",
                    JOptionPane.WARNING_MESSAGE
            );

            return null;
        }

        int modelRow =
                memberTable.convertRowIndexToModel(selectedRow);

        int memberId = Integer.parseInt(
                String.valueOf(tableModel.getValueAt(modelRow, 0))
        );

        try {

            List<MemberService.Member> members =
                    MemberService.getAllMembers();

            for (MemberService.Member member : members) {

                if (member.getMemberId() == memberId) {
                    return member;
                }
            }

            JOptionPane.showMessageDialog(
                    this,
                    "The selected member could not be found. Refresh the list."
            );

        } catch (SQLException ex) {
            showError(ex);
        }

        return null;
    }

    // EDIT MEMBER

    private void editSelectedMember() {

        MemberService.Member member = getSelectedMember();

        if (member != null) {
            showMemberDialog(member);
        }
    }

    // VIEW MEMBER DETAILS

    private void viewSelectedMember() {

        MemberService.Member member = getSelectedMember();

        if (member == null) {
            return;
        }

        String details =
                "Member ID: " + member.getMemberId() +
                        "\nFull Name: " + member.getFullName() +
                        "\nStudent/Employee ID: " + member.getStudentEmployeeId() +
                        "\nCourse / Department: " + member.getCourseDepartment() +
                        "\nContact Number: " + member.getContactNumber() +
                        "\nEmail: " + member.getEmail() +
                        "\nRegistration Date: " + member.getRegistrationDate() +
                        "\nMembership Status: " + member.getMembershipStatus();

        JOptionPane.showMessageDialog(
                this,
                details,
                "Member Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // BORROWING HISTORY

    private void viewBorrowingHistory() {

        MemberService.Member member = getSelectedMember();

        if (member == null) {
            return;
        }

        try {

            List<BorrowingRecord> history =
                    MemberBorrowingHistoryService.getMemberHistory(
                            member.getMemberId()
                    );

            showBorrowingHistoryDialog(member, history);

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void showBorrowingHistoryDialog(
            MemberService.Member member,
            List<BorrowingRecord> history
    ) {

        String[] columns = {
                "Transaction ID",
                "Book ID",
                "Book Title",
                "Borrowing Date",
                "Due Date",
                "Return Date",
                "Status"
        };

        DefaultTableModel historyModel =
                new DefaultTableModel(columns, 0) {

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

        int borrowedCount = 0;
        int returnedCount = 0;
        int overdueCount = 0;

        for (BorrowingRecord record : history) {

            String status = record.status();

            if ("RETURNED".equalsIgnoreCase(status)) {
                returnedCount++;
            } else if ("OVERDUE".equalsIgnoreCase(status)) {
                overdueCount++;
            } else {
                borrowedCount++;
            }

            historyModel.addRow(new Object[]{
                    record.transactionId(),
                    record.bookId(),
                    record.bookTitle(),
                    record.borrowingDate(),
                    record.dueDate(),
                    record.returnDate() == null ||
                            record.returnDate().isBlank()
                            ? "Not Returned"
                            : record.returnDate(),
                    status
            });
        }

        if (history.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "This member has no borrowing history yet.",
                    "Borrowing History",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        JTable historyTable = new JTable(historyModel);
        historyTable.setRowHeight(35);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyTable.setForeground(TEXT);
        historyTable.setBackground(WHITE);
        historyTable.setSelectionBackground(LIGHT_GOLD);
        historyTable.setSelectionForeground(BURGUNDY);
        historyTable.setGridColor(BORDER);
        historyTable.setShowVerticalLines(false);
        historyTable.setAutoCreateRowSorter(true);
        historyTable.setFillsViewportHeight(true);

        historyTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 11)
        );
        historyTable.getTableHeader().setBackground(BURGUNDY);
        historyTable.getTableHeader().setForeground(WHITE);
        historyTable.getTableHeader().setPreferredSize(
                new Dimension(0, 40)
        );

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setPreferredSize(new Dimension(900, 350));
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(WHITE);

        JLabel memberLabel = new JLabel(
                "Member: " + member.getFullName() +
                        "  |  Member ID: " + member.getMemberId()
        );
        memberLabel.setFont(new Font("Georgia", Font.BOLD, 14));
        memberLabel.setForeground(BURGUNDY);

        JLabel summaryLabel = new JLabel(
                "Total Transactions: " + history.size() +
                        "     Currently Borrowed: " + borrowedCount +
                        "     Returned: " + returnedCount +
                        "     Overdue: " + overdueCount
        );
        summaryLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        summaryLabel.setForeground(TEXT);

        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setBackground(CREAM);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        topPanel.setOpaque(false);
        topPanel.add(memberLabel);
        topPanel.add(summaryLabel);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(
                this,
                panel,
                "Borrowing History",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    // ACTIVATE / DEACTIVATE MEMBER

    private void toggleMemberStatus() {

        MemberService.Member existingMember = getSelectedMember();

        if (existingMember == null) {
            return;
        }

        String currentStatus = existingMember.getMembershipStatus();

        boolean currentlyActive =
                currentStatus != null &&
                        currentStatus.equalsIgnoreCase("ACTIVE");

        String newStatus = currentlyActive ? "INACTIVE" : "ACTIVE";

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Change " + existingMember.getFullName() +
                        "'s membership status to " + newStatus + "?",
                "Confirm Membership Status",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        MemberService.Member updatedMember =
                new MemberService.Member(
                        existingMember.getMemberId(),
                        existingMember.getFullName(),
                        existingMember.getStudentEmployeeId(),
                        existingMember.getCourseDepartment(),
                        existingMember.getContactNumber(),
                        existingMember.getEmail(),
                        existingMember.getRegistrationDate(),
                        newStatus
                );

        try {

            boolean success = MemberService.updateMember(updatedMember);

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Membership status updated to " + newStatus + ".",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                reloadCurrentSearch();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "The membership status could not be updated.",
                        "Update Failed",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // REGISTER / EDIT MEMBER

    private void showMemberDialog(MemberService.Member existingMember) {

        boolean isEditing = existingMember != null;

        JTextField nameField = createFormField(
                isEditing ? existingMember.getFullName() : ""
        );

        JTextField idField = createFormField(
                isEditing ? existingMember.getStudentEmployeeId() : ""
        );

        JTextField courseField = createFormField(
                isEditing ? existingMember.getCourseDepartment() : ""
        );

        JTextField contactField = createFormField(
                isEditing ? existingMember.getContactNumber() : ""
        );

        JTextField emailField = createFormField(
                isEditing ? existingMember.getEmail() : ""
        );

        String[] statuses = {"ACTIVE", "INACTIVE"};

        JComboBox<String> statusBox = new JComboBox<>(statuses);

        statusBox.setSelectedItem(
                isEditing
                        ? existingMember.getMembershipStatus()
                        : "ACTIVE"
        );

        JPanel panel = new JPanel(new GridLayout(0, 2, 12, 12));
        panel.setBackground(WHITE);
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        addFormRow(panel, "Full Name:", nameField);
        addFormRow(panel, "Student/Employee ID:", idField);
        addFormRow(panel, "Course / Department:", courseField);
        addFormRow(panel, "Contact Number:", contactField);
        addFormRow(panel, "Email:", emailField);
        addFormRow(panel, "Membership Status:", statusBox);

        int choice = JOptionPane.showConfirmDialog(
                this,
                panel,
                isEditing
                        ? "Edit Member Information"
                        : "Register New Member",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();
        String memberNumber = idField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || memberNumber.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Full Name and Student/Employee ID are required.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (!email.isEmpty() && !email.contains("@")) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid email address.",
                    "Invalid Email",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String registrationDate = isEditing
                ? existingMember.getRegistrationDate()
                : LocalDate.now().toString();

        int memberId = isEditing
                ? existingMember.getMemberId()
                : 0;

        MemberService.Member member = new MemberService.Member(
                memberId,
                name,
                memberNumber,
                courseField.getText().trim(),
                contactField.getText().trim(),
                email,
                registrationDate,
                (String) statusBox.getSelectedItem()
        );

        try {

            boolean success;

            if (isEditing) {
                success = MemberService.updateMember(member);
            } else {
                success = MemberService.addMember(member);
            }

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        isEditing
                                ? "Member updated successfully!"
                                : "Member registered successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                reloadCurrentSearch();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "The member information could not be saved.",
                        "Save Failed",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (SQLException ex) {

            if (ex.getMessage() != null &&
                    ex.getMessage().toLowerCase().contains("unique")) {

                JOptionPane.showMessageDialog(
                        this,
                        "That Student/Employee ID is already registered.",
                        "Duplicate ID",
                        JOptionPane.WARNING_MESSAGE
                );

            } else {
                showError(ex);
            }
        }
    }

    // FORM DESIGN

    private JTextField createFormField(String value) {

        JTextField field = new JTextField(value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(230, 36));
        field.setBackground(WHITE);
        field.setForeground(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(6, 9, 6, 9)
        ));

        return field;
    }

    private void addFormRow(
            JPanel panel,
            String labelText,
            JComponent field
    ) {

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Georgia", Font.BOLD, 12));
        label.setForeground(TEXT);

        if (field instanceof JComboBox<?>) {
            field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            field.setBackground(WHITE);
            field.setForeground(TEXT);
        }

        panel.add(label);
        panel.add(field);
    }

    // RELOAD CURRENT SEARCH

    private void reloadCurrentSearch() {

        if (searchField.getText().trim().isEmpty()) {
            loadMembers();
        } else {
            searchMembers();
        }
    }

    // STATUS RENDERER

    private class StatusRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {

            JLabel label = (JLabel)
                    super.getTableCellRendererComponent(
                            table,
                            value,
                            isSelected,
                            hasFocus,
                            row,
                            column
                    );

            String status = value == null
                    ? "UNKNOWN"
                    : value.toString().toUpperCase();

            label.setText("●  " + status);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));

            if ("ACTIVE".equals(status)) {
                label.setForeground(
                        isSelected ? BURGUNDY : new Color(27, 130, 72)
                );
            } else {
                label.setForeground(MUTED);
            }

            return label;
        }
    }

    // SMOOTH ROUNDED STATISTICS PANEL

    private static class RoundedPanel extends JPanel {

        private final int radius;
        private final Color backgroundColor;
        private final Color borderColor;

        RoundedPanel(
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

            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.2f));

            g2.drawRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius,
                    radius
            );

            g2.dispose();

            super.paintComponent(graphics);
        }
    }

    // ROUNDED BORDER

    private static class RoundedBorder
            extends javax.swing.border.AbstractBorder {

        private final Color color;
        private final int radius;

        RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
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

            Graphics2D g2 = (Graphics2D) graphics.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.2f));

            g2.drawRoundRect(
                    x,
                    y,
                    width - 1,
                    height - 1,
                    radius,
                    radius
            );

            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component component) {
            return new Insets(5, 8, 5, 8);
        }
    }

    // VECTOR ICONS
    // Existing icon designs kept unchanged.
    // Only the "users" icon now displays three people.
    // The "back" icon is an additional icon.

    private static class VectorIcon implements Icon {

        private final String type;
        private final Color color;
        private final int size;

        VectorIcon(String type, Color color, int size) {
            this.type = type;
            this.color = color;
            this.size = size;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        private void drawLine(Graphics2D g, double x1, double y1,
                              double x2, double y2) {
            g.draw(new java.awt.geom.Line2D.Double(x1, y1, x2, y2));
        }

        private void drawArrowHead(
                Graphics2D g,
                double tipX,
                double tipY,
                double angle,
                double length
        ) {

            double spread = Math.toRadians(28);

            double x1 = tipX - length * Math.cos(angle - spread);
            double y1 = tipY - length * Math.sin(angle - spread);

            double x2 = tipX - length * Math.cos(angle + spread);
            double y2 = tipY - length * Math.sin(angle + spread);

            drawLine(g, tipX, tipY, x1, y1);
            drawLine(g, tipX, tipY, x2, y2);
        }

        @Override
        public void paintIcon(
                Component component,
                Graphics graphics,
                int x,
                int y
        ) {

            Graphics2D g = (Graphics2D) graphics.create();

            g.translate(x, y);

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setRenderingHint(
                    RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE
            );

            g.setColor(color);
            g.setStroke(new BasicStroke(
                    Math.max(1.6f, size / 9.0f),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            double scale = size / 24.0;

            g.scale(scale, scale);

            switch (type) {

                case "search" -> {
                    g.drawOval(3, 3, 13, 13);
                    drawLine(g, 15, 15, 21, 21);
                }

                case "clear" -> {
                    g.drawOval(3, 3, 18, 18);

                    drawLine(g, 9, 9, 15, 15);
                    drawLine(g, 15, 9, 9, 15);
                }

                case "refresh" -> {
                    g.drawArc(5, 5, 14, 14, 45, 135);

                    Path2D topArrow = new Path2D.Double();
                    topArrow.moveTo(5, 5);
                    topArrow.lineTo(5, 10);
                    topArrow.lineTo(10, 8);
                    g.draw(topArrow);

                    g.drawArc(5, 5, 14, 14, 225, 135);

                    Path2D bottomArrow = new Path2D.Double();
                    bottomArrow.moveTo(19, 19);
                    bottomArrow.lineTo(19, 14);
                    bottomArrow.lineTo(14, 16);
                    g.draw(bottomArrow);
                }

                case "history" -> {
                    g.drawOval(5, 5, 15, 15);

                    drawLine(g, 12, 8, 12, 13);
                    drawLine(g, 12, 13, 16, 15);

                    drawLine(g, 3, 9, 3, 4);
                    drawLine(g, 3, 4, 8, 4);
                    drawLine(g, 3, 4, 7, 8);
                }

                case "eye" -> {

                    Path2D eye = new Path2D.Double();

                    eye.moveTo(2, 12);
                    eye.quadTo(12, 2, 22, 12);
                    eye.quadTo(12, 22, 2, 12);

                    g.draw(eye);
                    g.drawOval(9, 9, 6, 6);
                }

                case "edit" -> {

                    Path2D pencil = new Path2D.Double();

                    pencil.moveTo(4, 17);
                    pencil.lineTo(16, 5);
                    pencil.lineTo(20, 9);
                    pencil.lineTo(8, 21);
                    pencil.lineTo(3, 22);
                    pencil.closePath();

                    g.draw(pencil);

                    drawLine(g, 14, 7, 18, 11);
                    drawLine(g, 3, 22, 4, 17);
                }

                // THREE-PERSON USERS ICON

                case "users" -> {

                    // LEFT PERSON
                    g.drawOval(2, 4, 6, 6);

                    Arc2D leftPerson = new Arc2D.Double(
                            0, 12, 10, 9, 0, 180, Arc2D.OPEN
                    );

                    // CENTER PERSON
                    g.drawOval(9, 2, 7, 7);

                    Arc2D centerPerson = new Arc2D.Double(
                            7, 11, 11, 11, 0, 180, Arc2D.OPEN
                    );

                    // RIGHT PERSON
                    g.drawOval(17, 4, 6, 6);

                    Arc2D rightPerson = new Arc2D.Double(
                            14, 12, 10, 9, 0, 180, Arc2D.OPEN
                    );

                    g.draw(leftPerson);
                    g.draw(centerPerson);
                    g.draw(rightPerson);
                }

                case "user-check" -> {

                    g.drawOval(4, 2, 9, 9);

                    Arc2D person = new Arc2D.Double(
                            1, 12, 15, 10, 0, 180, Arc2D.OPEN
                    );

                    g.draw(person);

                    drawLine(g, 14, 15, 17, 18);
                    drawLine(g, 17, 18, 22, 12);
                }

                case "user-x" -> {

                    g.drawOval(3, 2, 9, 9);

                    Arc2D person = new Arc2D.Double(
                            1, 12, 14, 10, 0, 180, Arc2D.OPEN
                    );

                    g.draw(person);

                    drawLine(g, 16, 14, 22, 20);
                    drawLine(g, 22, 14, 16, 20);
                }

                case "user-plus" -> {

                    g.drawOval(2, 3, 9, 9);

                    Arc2D person = new Arc2D.Double(
                            0, 13, 14, 9, 0, 180, Arc2D.OPEN
                    );

                    g.draw(person);

                    drawLine(g, 18, 8, 18, 18);
                    drawLine(g, 13, 13, 23, 13);
                }

                case "user-minus" -> {

                    g.drawOval(2, 3, 9, 9);

                    Arc2D person = new Arc2D.Double(
                            0, 13, 14, 9, 0, 180, Arc2D.OPEN
                    );

                    g.draw(person);

                    drawLine(g, 15, 14, 23, 14);
                }

                // BACK ICON

                case "back" -> {
                    drawLine(g, 19, 12, 5, 12);
                    drawLine(g, 5, 12, 12, 5);
                    drawLine(g, 5, 12, 12, 19);
                }

                default -> g.drawOval(4, 4, 16, 16);
            }

            g.dispose();
        }
    }

    // DATABASE ERROR

    private void showError(Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                "A database error occurred:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );

        ex.printStackTrace();
    }
}

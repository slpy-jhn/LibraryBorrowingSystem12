
package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BookTransactionsFrame extends JFrame {

    // =====================================================
    // LIBRARYLINK THEME
    // =====================================================

    private static final Color BURGUNDY = new Color(75, 20, 36);
    private static final Color GOLD = new Color(176, 141, 87);
    private static final Color CREAM = new Color(243, 235, 221);
    private static final Color WHITE = new Color(255, 252, 245);
    private static final Color MUTED = new Color(130, 115, 103);
    private static final Color BORDER = new Color(222, 210, 191);

    // Existing screens are retained and reused.
    private BorrowBooksFrame borrowFrame;
    private ReturnBooksFrame returnFrame;
    private OverdueBooksFrame overdueFrame;

    private JTabbedPane tabs;

    public BookTransactionsFrame() {

        setTitle("LibraryLink - Book Transactions");
        setSize(1250, 780);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(CREAM);
        setLayout(new BorderLayout());

        // Create the existing screens.
        borrowFrame = new BorrowBooksFrame();
        returnFrame = new ReturnBooksFrame();
        overdueFrame = new OverdueBooksFrame();

        buildHeader();
        buildTabs();
    }

    // =====================================================
    // SHARED HEADER
    // =====================================================

    private void buildHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("Book Transactions");
        title.setFont(new Font("Georgia", Font.BOLD, 27));
        title.setForeground(GOLD);

        JLabel subtitle = new JLabel(
                "Manage borrowing, book returns, and overdue records."
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(235, 220, 215));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        // =================================================
        // TRANSPARENT ICON-ONLY BACK BUTTON
        // =================================================

        JButton backButton = new JButton("\u2190");

        backButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 27));
        backButton.setForeground(GOLD);
        backButton.setBackground(new Color(0, 0, 0, 0));

        backButton.setPreferredSize(new Dimension(46, 46));
        backButton.setMinimumSize(new Dimension(46, 46));
        backButton.setMaximumSize(new Dimension(46, 46));

        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.setOpaque(false);

        backButton.setMargin(new Insets(0, 0, 0, 0));

        backButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        backButton.setToolTipText("Back");

        // Subtle hover effect.
        backButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                backButton.setForeground(WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                backButton.setForeground(GOLD);
            }
        });

        // Close the Book Transactions window.
        backButton.addActionListener(e -> dispose());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    // =====================================================
    // TABS
    // =====================================================

    private void buildTabs() {

        tabs = new JTabbedPane(
                JTabbedPane.TOP,
                JTabbedPane.SCROLL_TAB_LAYOUT
        );

        tabs.setFont(new Font("Georgia", Font.BOLD, 13));
        tabs.setBackground(CREAM);
        tabs.setForeground(BURGUNDY);
        tabs.setBorder(new EmptyBorder(12, 18, 12, 18));

        // Extract the existing screens' content.
        JPanel borrowingPanel = extractContent(borrowFrame);
        JPanel returningPanel = extractContent(returnFrame);
        JPanel overduePanel = extractContent(overdueFrame);

        // Redirect the old borrowing screen CANCEL button.
        redirectCancelButton(borrowingPanel);

        tabs.addTab("  Borrow Book  ", borrowingPanel);
        tabs.addTab("  Return Book  ", returningPanel);
        tabs.addTab("  Overdue Book  ", overduePanel);

        add(tabs, BorderLayout.CENTER);
    }

    // =====================================================
    // REUSE EXISTING SCREEN CONTENT
    // =====================================================

    private JPanel extractContent(JFrame oldFrame) {

        Container oldContent = oldFrame.getContentPane();

        // Detach the original content from its JFrame.
        oldFrame.setContentPane(
                new JPanel(new BorderLayout())
        );

        // Remove the old individual header.
        if (oldContent.getLayout() instanceof BorderLayout) {

            BorderLayout layout =
                    (BorderLayout) oldContent.getLayout();

            Component oldHeader =
                    layout.getLayoutComponent(BorderLayout.NORTH);

            if (oldHeader != null) {
                oldContent.remove(oldHeader);
            }
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CREAM);
        panel.add(oldContent, BorderLayout.CENTER);

        panel.revalidate();
        panel.repaint();

        return panel;
    }

    // =====================================================
    // REDIRECT OLD CANCEL BUTTON
    // =====================================================

    private void redirectCancelButton(Container container) {

        for (Component component : container.getComponents()) {

            if (component instanceof JButton button) {

                if ("CANCEL".equalsIgnoreCase(
                        button.getText().trim()
                )) {

                    for (var listener :
                            button.getActionListeners()) {
                        button.removeActionListener(listener);
                    }

                    button.addActionListener(e -> dispose());
                }
            }

            if (component instanceof Container child) {
                redirectCancelButton(child);
            }
        }
    }

    // =====================================================
    // DISPOSE
    // =====================================================

    @Override
    public void dispose() {

        if (borrowFrame != null) {
            borrowFrame.dispose();
        }

        if (returnFrame != null) {
            returnFrame.dispose();
        }

        if (overdueFrame != null) {
            overdueFrame.dispose();
        }

        super.dispose();
    }
}

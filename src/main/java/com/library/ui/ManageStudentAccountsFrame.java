package com.library.ui;

import com.library.database.DatabaseConnection;
import com.library.service.StudentAccountService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.Arrays;

public class ManageStudentAccountsFrame extends JFrame {

    // =====================================================
    // THEME
    // =====================================================

    private static final Color BURGUNDY = new Color(74, 14, 23);
    private static final Color DARK_BURGUNDY = new Color(55, 10, 18);
    private static final Color BACKGROUND = new Color(249, 248, 243);
    private static final Color CREAM = new Color(247, 244, 238);
    private static final Color WHITE = Color.WHITE;
    private static final Color DARK_TEXT = new Color(31, 41, 55);
    private static final Color MUTED = new Color(107, 114, 128);
    private static final Color BORDER = new Color(229, 231, 235);
    private static final Color GOLD = new Color(197, 160, 89);
    private static final Color SOFT_GOLD = new Color(255, 247, 237);
    private static final Color GREEN = new Color(21, 128, 61);
    private static final Color SOFT_GREEN = new Color(220, 252, 231);
    private static final Color RED = new Color(183, 66, 66);

    private static final Font HEADING_FONT =
            new Font("Georgia", Font.BOLD, 22);

    private static final Font BODY_FONT =
            new Font("Segoe UI", Font.PLAIN, 13);

    private static final Font LABEL_FONT =
            new Font("Segoe UI", Font.BOLD, 12);

    // =====================================================
    // FORM COMPONENTS
    // =====================================================

    private JComboBox<MemberItem> memberComboBox;

    private JTextField createUsernameField;
    private JPasswordField createPasswordField;
    private JPasswordField confirmCreatePasswordField;

    private JComboBox<StudentAccountService.StudentAccount> accountComboBox;

    private JTextField updateUsernameField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmNewPasswordField;

    // =====================================================
    // PREVIEW
    // =====================================================

    private JLabel previewNameLabel;
    private JLabel previewIdLabel;
    private JLabel previewUsernameLabel;
    private JLabel previewCourseLabel;
    private JLabel previewRoleLabel;
    private JLabel previewPasswordLabel;
    private JLabel previewStatusLabel;
    private JLabel previewAvatarLabel;
    private JLabel previewModeLabel;

    private JLabel strengthLabel;
    private JProgressBar strengthBar;

    // =====================================================
    // TABS
    // =====================================================

    private CardLayout formCardLayout;
    private JPanel formCardPanel;

    private JButton createTabButton;
    private JButton updateTabButton;

    private boolean showingCreateTab = true;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public ManageStudentAccountsFrame() {

        setTitle("Manage Student Accounts");
        setSize(1250, 850);
        setMinimumSize(new Dimension(950, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout());

        createHeader();
        createMainContent();

        loadActiveMembers();
        loadStudentAccounts();
    }

    // =====================================================
    // HEADER WITH TRANSPARENT BACK BUTTON
    // =====================================================

    private void createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BURGUNDY);
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        JButton backButton = new JButton(new DrawIcon("BACK", GOLD, 22));

        backButton.setToolTipText("Back");
        backButton.setPreferredSize(new Dimension(38, 38));
        backButton.setContentAreaFilled(false);
        backButton.setOpaque(false);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        backButton.addActionListener(e -> dispose());

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(backButton);

        header.add(leftPanel, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);
    }

    // =====================================================
    // MAIN CONTENT
    // =====================================================

    private void createMainContent() {

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(22, 25, 22, 25));

        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Configure Login Credentials");
        heading.setFont(HEADING_FONT);
        heading.setForeground(BURGUNDY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "Create new student access or manage existing login information."
        );
        description.setFont(BODY_FONT);
        description.setForeground(MUTED);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(5));
        headingPanel.add(description);

        content.add(headingPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;

        gbc.gridx = 0;
        gbc.weightx = 0.64;
        gbc.insets = new Insets(0, 0, 0, 12);

        mainPanel.add(createLeftPanel(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.36;
        gbc.insets = new Insets(0, 12, 0, 0);

        mainPanel.add(createPreviewPanel(), gbc);

        content.add(mainPanel, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    // =====================================================
    // LEFT PANEL
    // =====================================================

    private JPanel createLeftPanel() {

        RoundedPanel container = new RoundedPanel(WHITE, 18);
        container.setLayout(new BorderLayout());
        container.setBorder(new EmptyBorder(17, 20, 18, 20));

        JPanel tabPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        tabPanel.setOpaque(false);

        createTabButton = createTabButton(
                "Create Account",
                new DrawIcon("PROFILE_PLUS", GOLD, 19),
                true
        );

        updateTabButton = createTabButton(
                "Update Account",
                new DrawIcon("PROFILE_EDIT", MUTED, 19),
                false
        );

        tabPanel.add(createTabButton);
        tabPanel.add(updateTabButton);

        container.add(tabPanel, BorderLayout.NORTH);

        formCardLayout = new CardLayout();
        formCardPanel = new JPanel(formCardLayout);
        formCardPanel.setOpaque(false);

        formCardPanel.add(createCreatePanel(), "CREATE");
        formCardPanel.add(createUpdatePanel(), "UPDATE");

        JScrollPane scrollPane = new JScrollPane(formCardPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setPreferredSize(
                new Dimension(8, 0)
        );

        container.add(scrollPane, BorderLayout.CENTER);

        createTabButton.addActionListener(e -> showCreateTab());
        updateTabButton.addActionListener(e -> showUpdateTab());

        return container;
    }

    private JButton createTabButton(
            String text,
            Icon icon,
            boolean active
    ) {

        JButton button = new JButton(text, icon);

        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(active ? BURGUNDY : MUTED);
        button.setBackground(WHITE);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setIconTextGap(9);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        0, 0, active ? 3 : 1, 0,
                        active ? BURGUNDY : BORDER
                ),
                new EmptyBorder(12, 5, 12, 5)
        ));

        return button;
    }

    private void showCreateTab() {

        showingCreateTab = true;
        formCardLayout.show(formCardPanel, "CREATE");

        styleActiveTab(createTabButton, updateTabButton);
        updatePreviewForCreate();
    }

    private void showUpdateTab() {

        showingCreateTab = false;
        formCardLayout.show(formCardPanel, "UPDATE");

        styleActiveTab(updateTabButton, createTabButton);
        updatePreviewForUpdate();
    }

    private void styleActiveTab(JButton active, JButton inactive) {

        active.setForeground(BURGUNDY);
        active.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, BURGUNDY),
                new EmptyBorder(12, 5, 12, 5)
        ));

        inactive.setForeground(MUTED);
        inactive.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(12, 5, 12, 5)
        ));
    }

    // =====================================================
    // CREATE ACCOUNT PANEL
    // =====================================================

    private JPanel createCreatePanel() {

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(25, 5, 15, 5));

        JLabel title = new JLabel("Create Student Login");
        title.setFont(new Font("Georgia", Font.BOLD, 19));
        title.setForeground(BURGUNDY);
        alignLeft(title);

        JLabel description = new JLabel(
                "Select an eligible member and set up their portal access account."
        );
        description.setFont(BODY_FONT);
        description.setForeground(MUTED);
        alignLeft(description);

        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(description);
        panel.add(Box.createVerticalStrut(24));

        memberComboBox = new JComboBox<>();
        styleComboBox(memberComboBox);

        addFieldSection(panel, "SELECT MEMBER", memberComboBox);

        createUsernameField = createTextField();

        addFieldSection(panel, "USERNAME", createUsernameField);

        JPanel passwordRow = new JPanel(new GridLayout(1, 2, 14, 0));
        passwordRow.setOpaque(false);
        passwordRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        createPasswordField = createPasswordField();
        confirmCreatePasswordField = createPasswordField();

        passwordRow.add(createPasswordSection(
                "PASSWORD",
                createPasswordField
        ));

        passwordRow.add(createPasswordSection(
                "CONFIRM PASSWORD",
                confirmCreatePasswordField
        ));

        panel.add(passwordRow);
        panel.add(Box.createVerticalStrut(17));

        strengthBar = new JProgressBar(0, 100);
        strengthBar.setValue(0);
        strengthBar.setBorderPainted(false);
        strengthBar.setBackground(CREAM);
        strengthBar.setForeground(GOLD);
        strengthBar.setPreferredSize(new Dimension(100, 7));

        strengthLabel = new JLabel("Not entered");
        strengthLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        strengthLabel.setForeground(MUTED);

        JPanel strengthTop = new JPanel(new BorderLayout());
        strengthTop.setOpaque(false);

        JLabel strengthTitle = new JLabel("PASSWORD STRENGTH");
        strengthTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        strengthTitle.setForeground(MUTED);

        strengthTop.add(strengthTitle, BorderLayout.WEST);
        strengthTop.add(strengthLabel, BorderLayout.EAST);

        JPanel strengthPanel = new JPanel(new BorderLayout(0, 7));
        strengthPanel.setOpaque(false);
        strengthPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        strengthPanel.add(strengthTop, BorderLayout.NORTH);
        strengthPanel.add(strengthBar, BorderLayout.CENTER);
        strengthPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        panel.add(strengthPanel);
        panel.add(Box.createVerticalStrut(10));

        JLabel passwordNote = new JLabel(
                "Password must contain at least 8 characters."
        );
        passwordNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        passwordNote.setForeground(MUTED);
        alignLeft(passwordNote);

        panel.add(passwordNote);
        panel.add(Box.createVerticalStrut(25));

        JButton createButton = createPrimaryButton(
                "Create Account",
                new DrawIcon("PLUS", WHITE, 18)
        );

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        buttonPanel.add(createButton);

        panel.add(buttonPanel);

        createButton.addActionListener(e -> createAccount());

        memberComboBox.addActionListener(e -> updatePreviewForCreate());

        createUsernameField.getDocument().addDocumentListener(
                createDocumentListener(this::updatePreviewForCreate)
        );

        createPasswordField.getDocument().addDocumentListener(
                createDocumentListener(() -> {
                    updatePasswordStrength(createPasswordField);
                    updatePreviewForCreate();
                })
        );

        confirmCreatePasswordField.getDocument().addDocumentListener(
                createDocumentListener(this::updatePreviewForCreate)
        );

        return panel;
    }

    // =====================================================
    // UPDATE ACCOUNT PANEL
    // =====================================================

    private JPanel createUpdatePanel() {

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(25, 5, 15, 5));

        JLabel title = new JLabel("Update Existing Student Account");
        title.setFont(new Font("Georgia", Font.BOLD, 19));
        title.setForeground(BURGUNDY);
        alignLeft(title);

        JLabel description = new JLabel(
                "Select an active student account to modify login credentials or perform a secure password reset."
        );
        description.setFont(BODY_FONT);
        description.setForeground(MUTED);
        alignLeft(description);

        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(description);
        panel.add(Box.createVerticalStrut(24));

        accountComboBox = new JComboBox<>();
        styleComboBox(accountComboBox);

        addFieldSection(panel, "SELECT STUDENT ACCOUNT", accountComboBox);

        updateUsernameField = createTextField();

        addFieldSection(panel, "UPDATE USERNAME", updateUsernameField);

        JPanel passwordRow = new JPanel(new GridLayout(1, 2, 14, 0));
        passwordRow.setOpaque(false);
        passwordRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        newPasswordField = createPasswordField();
        confirmNewPasswordField = createPasswordField();

        passwordRow.add(createPasswordSection(
                "NEW PASSWORD",
                newPasswordField
        ));

        passwordRow.add(createPasswordSection(
                "CONFIRM PASSWORD",
                confirmNewPasswordField
        ));

        panel.add(passwordRow);
        panel.add(Box.createVerticalStrut(17));

        JPanel notePanel = new RoundedPanel(SOFT_GOLD, 10);
        notePanel.setLayout(new BorderLayout(9, 0));
        notePanel.setBorder(new EmptyBorder(12, 13, 12, 13));
        notePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        notePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JLabel warningIcon = new JLabel(
                new DrawIcon("SHIELD", new Color(180, 83, 9), 19)
        );

        JLabel noteText = new JLabel(
                "Leave both password fields blank to retain the student's existing password."
        );
        noteText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        noteText.setForeground(new Color(120, 75, 25));

        notePanel.add(warningIcon, BorderLayout.WEST);
        notePanel.add(noteText, BorderLayout.CENTER);

        panel.add(notePanel);
        panel.add(Box.createVerticalStrut(25));

        JButton updateButton = createPrimaryButton(
                "Save Account Changes",
                new DrawIcon("SAVE", WHITE, 18)
        );

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        buttonPanel.add(updateButton);

        panel.add(buttonPanel);

        accountComboBox.addActionListener(e -> {
            populateSelectedAccount();
            updatePreviewForUpdate();
        });

        updateUsernameField.getDocument().addDocumentListener(
                createDocumentListener(this::updatePreviewForUpdate)
        );

        newPasswordField.getDocument().addDocumentListener(
                createDocumentListener(this::updatePreviewForUpdate)
        );

        confirmNewPasswordField.getDocument().addDocumentListener(
                createDocumentListener(this::updatePreviewForUpdate)
        );

        updateButton.addActionListener(e -> updateAccount());

        return panel;
    }

    // =====================================================
    // FIELD HELPERS
    // =====================================================

    private void alignLeft(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void addFieldSection(
            JPanel parent,
            String labelText,
            JComponent component
    ) {

        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        JLabel label = new JLabel(labelText, SwingConstants.LEFT);
        label.setFont(LABEL_FONT);
        label.setForeground(DARK_TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setAlignmentX(Component.LEFT_ALIGNMENT);

        section.add(label);
        section.add(Box.createVerticalStrut(7));
        section.add(component);

        parent.add(section);
        parent.add(Box.createVerticalStrut(17));
    }

    private JPanel createPasswordSection(
            String labelText,
            JPasswordField passwordField
    ) {

        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        label.setForeground(DARK_TEXT);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel inputPanel = new RoundedPanel(CREAM, 10);
        inputPanel.setLayout(new BorderLayout(5, 0));
        inputPanel.setBorder(new EmptyBorder(0, 10, 0, 7));
        inputPanel.setPreferredSize(new Dimension(100, 43));
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        inputPanel.setMinimumSize(new Dimension(70, 43));

        passwordField.setBorder(BorderFactory.createEmptyBorder(8, 3, 8, 3));
        passwordField.setBackground(CREAM);
        passwordField.setOpaque(false);
        passwordField.setPreferredSize(new Dimension(100, 40));

        JToggleButton visibilityButton = new JToggleButton(
                new DrawIcon("EYE", MUTED, 19)
        );

        visibilityButton.setPreferredSize(new Dimension(32, 32));
        visibilityButton.setMinimumSize(new Dimension(32, 32));
        visibilityButton.setMaximumSize(new Dimension(32, 32));
        visibilityButton.setToolTipText("Show password");
        visibilityButton.setFocusPainted(false);
        visibilityButton.setBorderPainted(false);
        visibilityButton.setContentAreaFilled(false);
        visibilityButton.setOpaque(false);
        visibilityButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        char defaultEcho = passwordField.getEchoChar();

        visibilityButton.addActionListener(e -> {

            if (visibilityButton.isSelected()) {

                passwordField.setEchoChar((char) 0);

                visibilityButton.setIcon(
                        new DrawIcon("EYE_OFF", BURGUNDY, 19)
                );

                visibilityButton.setToolTipText("Hide password");

            } else {

                passwordField.setEchoChar(defaultEcho);

                visibilityButton.setIcon(
                        new DrawIcon("EYE", MUTED, 19)
                );

                visibilityButton.setToolTipText("Show password");
            }
        });

        inputPanel.add(passwordField, BorderLayout.CENTER);
        inputPanel.add(visibilityButton, BorderLayout.EAST);

        section.add(label);
        section.add(Box.createVerticalStrut(7));
        section.add(inputPanel);

        return section;
    }

    // =====================================================
    // LIVE ACCOUNT PREVIEW
    // =====================================================

    private JPanel createPreviewPanel() {

        RoundedPanel card = new RoundedPanel(WHITE, 18);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 19, 18, 19));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel headerLabel = new JLabel(
                new DrawIcon("CARD", GOLD, 19)
        );

        JLabel headerText = new JLabel("LIVE ACCOUNT PREVIEW");
        headerText.setFont(new Font("Segoe UI", Font.BOLD, 11));
        headerText.setForeground(MUTED);

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        headerLeft.setOpaque(false);
        headerLeft.add(headerLabel);
        headerLeft.add(headerText);

        previewStatusLabel = new JLabel("PENDING CREATION");
        previewStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 9));
        previewStatusLabel.setForeground(new Color(180, 83, 9));
        previewStatusLabel.setBackground(SOFT_GOLD);
        previewStatusLabel.setOpaque(true);
        previewStatusLabel.setBorder(new EmptyBorder(6, 9, 6, 9));

        header.add(headerLeft, BorderLayout.WEST);
        header.add(previewStatusLabel, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        profile.setBorder(new EmptyBorder(28, 0, 10, 0));

        previewAvatarLabel = new JLabel("S");
        previewAvatarLabel.setFont(new Font("Georgia", Font.BOLD, 28));
        previewAvatarLabel.setForeground(WHITE);
        previewAvatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewAvatarLabel.setVerticalAlignment(SwingConstants.CENTER);
        previewAvatarLabel.setOpaque(true);
        previewAvatarLabel.setBackground(BURGUNDY);
        previewAvatarLabel.setPreferredSize(new Dimension(76, 76));
        previewAvatarLabel.setMaximumSize(new Dimension(76, 76));
        previewAvatarLabel.setMinimumSize(new Dimension(76, 76));
        previewAvatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel avatarHolder = new JPanel(new GridBagLayout());
        avatarHolder.setOpaque(false);
        avatarHolder.setMaximumSize(new Dimension(100, 90));
        avatarHolder.add(previewAvatarLabel);

        previewNameLabel = new JLabel("Select a Student");
        previewNameLabel.setFont(new Font("Georgia", Font.BOLD, 17));
        previewNameLabel.setForeground(BURGUNDY);
        previewNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        previewIdLabel = new JLabel("STUDENT ID: —");
        previewIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        previewIdLabel.setForeground(MUTED);
        previewIdLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        profile.add(avatarHolder);
        profile.add(Box.createVerticalStrut(15));
        profile.add(previewNameLabel);
        profile.add(Box.createVerticalStrut(7));
        profile.add(previewIdLabel);
        profile.add(Box.createVerticalStrut(25));

        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER);
        profile.add(separator);
        profile.add(Box.createVerticalStrut(18));

        JLabel detailsTitle = new JLabel("ACCOUNT DETAILS");
        detailsTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        detailsTitle.setForeground(MUTED);
        alignLeft(detailsTitle);

        profile.add(detailsTitle);
        profile.add(Box.createVerticalStrut(15));

        previewUsernameLabel = createPreviewValue();
        previewCourseLabel = createPreviewValue();
        previewRoleLabel = createPreviewValue();
        previewPasswordLabel = createPreviewValue();

        profile.add(createPreviewRow("Assigned Username", previewUsernameLabel));
        profile.add(Box.createVerticalStrut(16));

        profile.add(createPreviewRow("Course / Department", previewCourseLabel));
        profile.add(Box.createVerticalStrut(16));

        profile.add(createPreviewRow("Access Role", previewRoleLabel));
        profile.add(Box.createVerticalStrut(16));

        profile.add(createPreviewRow("Password Status", previewPasswordLabel));

        card.add(profile, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setOpaque(true);
        footer.setBackground(CREAM);
        footer.setBorder(new EmptyBorder(11, 10, 11, 10));
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));

        previewModeLabel = new JLabel("Waiting for account information");
        previewModeLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        previewModeLabel.setForeground(BURGUNDY);
        previewModeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel syncedLabel = new JLabel(
                "●  Synced live with LibraryLink Directory"
        );
        syncedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        syncedLabel.setForeground(MUTED);
        syncedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        footer.add(previewModeLabel);
        footer.add(Box.createVerticalStrut(5));
        footer.add(syncedLabel);

        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private JLabel createPreviewValue() {

        JLabel label = new JLabel("—");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(DARK_TEXT);
        label.setHorizontalAlignment(SwingConstants.RIGHT);

        return label;
    }

    private JPanel createPreviewRow(String title, JLabel valueLabel) {

        JPanel row = new JPanel(new BorderLayout(5, 5));
        row.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLabel.setForeground(MUTED);

        row.add(titleLabel, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);

        return row;
    }

    // =====================================================
    // PREVIEW UPDATE
    // =====================================================

    private void updatePreviewForCreate() {

        if (previewNameLabel == null) {
            return;
        }

        MemberItem selected = memberComboBox == null
                ? null
                : (MemberItem) memberComboBox.getSelectedItem();

        if (selected == null || selected.memberId <= 0) {

            setPreviewStudent("Select a Student", "STUDENT ID: —", "S");
            previewCourseLabel.setText("—");

        } else {

            setPreviewStudent(
                    selected.fullName,
                    "STUDENT ID: " + safeText(selected.studentId),
                    getInitial(selected.fullName)
            );

            previewCourseLabel.setText(safeText(selected.courseDepartment));
        }

        String username = createUsernameField == null
                ? ""
                : createUsernameField.getText().trim();

        previewUsernameLabel.setText(
                username.isBlank() ? "Not assigned" : username
        );

        char[] password = createPasswordField == null
                ? new char[0]
                : createPasswordField.getPassword();

        char[] confirmation = confirmCreatePasswordField == null
                ? new char[0]
                : confirmCreatePasswordField.getPassword();

        if (password.length == 0) {
            previewPasswordLabel.setText("Not set");
        } else if (!Arrays.equals(password, confirmation)
                && confirmation.length > 0) {
            previewPasswordLabel.setText("Does not match");
        } else if (password.length < 8) {
            previewPasswordLabel.setText("Too short");
        } else {
            previewPasswordLabel.setText("Ready to create");
        }

        Arrays.fill(password, '\0');
        Arrays.fill(confirmation, '\0');

        previewRoleLabel.setText("Student Member");

        setPreviewStatus(
                "PENDING CREATION",
                SOFT_GOLD,
                new Color(180, 83, 9)
        );

        previewModeLabel.setText("New account preview");
    }

    private void updatePreviewForUpdate() {

        if (previewNameLabel == null) {
            return;
        }

        StudentAccountService.StudentAccount selected =
                accountComboBox == null
                        ? null
                        : (StudentAccountService.StudentAccount)
                        accountComboBox.getSelectedItem();

        if (selected == null) {

            setPreviewStudent(
                    "Select an Account",
                    "STUDENT ID: —",
                    "S"
            );

            previewUsernameLabel.setText("Not assigned");
            previewCourseLabel.setText("—");
            previewPasswordLabel.setText("Current password retained");

            setPreviewStatus("NO ACCOUNT SELECTED", CREAM, MUTED);
            previewModeLabel.setText("Waiting for account selection");

            return;
        }

        setPreviewStudent(
                selected.fullName(),
                "ACCOUNT ID: " + selected.userId(),
                getInitial(selected.fullName())
        );

        String username = updateUsernameField == null
                ? selected.username()
                : updateUsernameField.getText().trim();

        previewUsernameLabel.setText(
                username.isBlank() ? "Not assigned" : username
        );

        previewCourseLabel.setText(
                getCourseDepartmentForAccount(selected.fullName())
        );

        char[] password = newPasswordField == null
                ? new char[0]
                : newPasswordField.getPassword();

        char[] confirmation = confirmNewPasswordField == null
                ? new char[0]
                : confirmNewPasswordField.getPassword();

        if (password.length == 0 && confirmation.length == 0) {
            previewPasswordLabel.setText("Unchanged");
        } else if (!Arrays.equals(password, confirmation)
                && confirmation.length > 0) {
            previewPasswordLabel.setText("Does not match");
        } else if (password.length < 8) {
            previewPasswordLabel.setText("Too short");
        } else {
            previewPasswordLabel.setText("New password ready");
        }

        Arrays.fill(password, '\0');
        Arrays.fill(confirmation, '\0');

        previewRoleLabel.setText("Student Member");

        setPreviewStatus("ACTIVE ACCOUNT", SOFT_GREEN, GREEN);
        previewModeLabel.setText("Existing account preview");
    }

    private void setPreviewStudent(
            String name,
            String id,
            String initial
    ) {

        previewNameLabel.setText(name);
        previewIdLabel.setText(id);
        previewAvatarLabel.setText(initial);
    }

    private void setPreviewStatus(
            String text,
            Color background,
            Color foreground
    ) {

        previewStatusLabel.setText(text);
        previewStatusLabel.setBackground(background);
        previewStatusLabel.setForeground(foreground);
    }

    private String getInitial(String name) {

        if (name == null || name.isBlank()) {
            return "S";
        }

        return name.trim().substring(0, 1).toUpperCase();
    }

    private String safeText(String value) {

        return value == null || value.isBlank()
                ? "Not provided"
                : value;
    }

    // =====================================================
    // COURSE / DEPARTMENT LOOKUP
    // =====================================================

    private String getCourseDepartmentForAccount(String fullName) {

        String sql = """
                SELECT *
                FROM members
                WHERE full_name = ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, fullName);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    String[] possibleColumns = {
                            "course_department",
                            "courseDepartment",
                            "department_course",
                            "department",
                            "course",
                            "program",
                            "major"
                    };

                    ResultSetMetaData metadata = result.getMetaData();

                    for (int i = 1; i <= metadata.getColumnCount(); i++) {

                        String actualColumn = metadata.getColumnName(i);

                        for (String possible : possibleColumns) {

                            if (actualColumn.equalsIgnoreCase(possible)) {

                                String value = result.getString(i);

                                if (value != null && !value.isBlank()) {
                                    return value;
                                }
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Library Member";
    }

    // =====================================================
    // PASSWORD STRENGTH
    // =====================================================

    private void updatePasswordStrength(JPasswordField field) {

        if (strengthBar == null || field == null) {
            return;
        }

        char[] password = field.getPassword();

        int score = 0;

        if (password.length >= 8) score += 40;
        if (password.length >= 12) score += 15;

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;

        for (char character : password) {

            if (Character.isUpperCase(character)) hasUpper = true;
            if (Character.isLowerCase(character)) hasLower = true;
            if (Character.isDigit(character)) hasNumber = true;
            if (!Character.isLetterOrDigit(character)) hasSpecial = true;
        }

        if (hasUpper) score += 15;
        if (hasLower) score += 10;
        if (hasNumber) score += 10;
        if (hasSpecial) score += 10;

        score = Math.min(score, 100);
        strengthBar.setValue(score);

        if (password.length == 0) {

            strengthLabel.setText("Not entered");
            strengthLabel.setForeground(MUTED);
            strengthBar.setForeground(GOLD);

        } else if (score < 40) {

            strengthLabel.setText("Weak");
            strengthLabel.setForeground(RED);
            strengthBar.setForeground(RED);

        } else if (score < 70) {

            strengthLabel.setText("Fair");
            strengthLabel.setForeground(new Color(180, 125, 45));
            strengthBar.setForeground(new Color(180, 125, 45));

        } else {

            strengthLabel.setText("Strong");
            strengthLabel.setForeground(GREEN);
            strengthBar.setForeground(GREEN);
        }

        Arrays.fill(password, '\0');
    }

    // =====================================================
    // INPUT STYLING
    // =====================================================

    private JTextField createTextField() {

        JTextField field = new JTextField();

        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(DARK_TEXT);
        field.setBackground(CREAM);
        field.setCaretColor(BURGUNDY);

        field.setPreferredSize(new Dimension(200, 43));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        return field;
    }

    private JPasswordField createPasswordField() {

        JPasswordField field = new JPasswordField();

        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(DARK_TEXT);
        field.setBackground(CREAM);
        field.setCaretColor(BURGUNDY);
        field.setBorder(BorderFactory.createEmptyBorder());

        return field;
    }

    private void styleComboBox(JComboBox<?> comboBox) {

        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setBackground(CREAM);
        comboBox.setForeground(DARK_TEXT);

        comboBox.setPreferredSize(new Dimension(200, 43));
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));

        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(5, 9, 5, 9)
        ));
    }

    // =====================================================
    // BUTTON STYLING
    // =====================================================

    private JButton createPrimaryButton(String text, Icon icon) {

        JButton button = new JButton(text, icon);

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(BURGUNDY);
        button.setForeground(WHITE);
        button.setIconTextGap(9);

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.setPreferredSize(new Dimension(190, 43));
        button.setMinimumSize(new Dimension(125, 43));

        button.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(DARK_BURGUNDY);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(BURGUNDY);
            }
        });

        return button;
    }

    // =====================================================
    // DOCUMENT LISTENER
    // =====================================================

    private DocumentListener createDocumentListener(Runnable action) {

        return new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        };
    }

    // =====================================================
    // LOAD ELIGIBLE MEMBERS
    // =====================================================

    private void loadActiveMembers() {

        if (memberComboBox == null) {
            return;
        }

        memberComboBox.removeAllItems();

        String sql = """
                SELECT *
                FROM members
                WHERE membership_status = 'ACTIVE'
                  AND member_id NOT IN (
                      SELECT member_id
                      FROM users
                      WHERE member_id IS NOT NULL
                  )
                ORDER BY full_name
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                String courseDepartment = getOptionalColumn(
                        result,
                        "course_department",
                        "courseDepartment",
                        "department_course",
                        "department",
                        "course",
                        "program",
                        "major"
                );

                memberComboBox.addItem(new MemberItem(
                        result.getInt("member_id"),
                        result.getString("full_name"),
                        result.getString("student_employee_id"),
                        courseDepartment
                ));
            }

            if (memberComboBox.getItemCount() == 0) {

                memberComboBox.addItem(
                        new MemberItem(
                                -1,
                                "No eligible members available",
                                "",
                                ""
                        )
                );
            }

        } catch (SQLException e) {

            showError("Unable to load members:\n" + e.getMessage());
            e.printStackTrace();
        }

        updatePreviewForCreate();
    }

    private String getOptionalColumn(
            ResultSet result,
            String... possibleNames
    ) throws SQLException {

        ResultSetMetaData metadata = result.getMetaData();

        for (int i = 1; i <= metadata.getColumnCount(); i++) {

            String actualName = metadata.getColumnName(i);

            for (String possibleName : possibleNames) {

                if (actualName.equalsIgnoreCase(possibleName)) {

                    String value = result.getString(i);

                    return value == null ? "" : value;
                }
            }
        }

        return "";
    }

    // =====================================================
    // LOAD STUDENT ACCOUNTS
    // =====================================================

    private void loadStudentAccounts() {

        if (accountComboBox == null) {
            return;
        }

        Integer selectedUserId = null;

        StudentAccountService.StudentAccount previous =
                (StudentAccountService.StudentAccount)
                        accountComboBox.getSelectedItem();

        if (previous != null) {
            selectedUserId = previous.userId();
        }

        accountComboBox.removeAllItems();

        try {

            for (StudentAccountService.StudentAccount account :
                    StudentAccountService.getStudentAccounts()) {

                accountComboBox.addItem(account);
            }

            if (selectedUserId != null) {

                for (int i = 0; i < accountComboBox.getItemCount(); i++) {

                    StudentAccountService.StudentAccount account =
                            accountComboBox.getItemAt(i);

                    if (account.userId() == selectedUserId) {
                        accountComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }

            if (accountComboBox.getItemCount() > 0) {

                if (accountComboBox.getSelectedIndex() < 0) {
                    accountComboBox.setSelectedIndex(0);
                }

                populateSelectedAccount();

            } else {

                updateUsernameField.setText("");
                clearUpdatePasswords();
            }

            updatePreviewForUpdate();

        } catch (SQLException e) {

            showError("Unable to load student accounts:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateSelectedAccount() {

        if (accountComboBox == null || updateUsernameField == null) {
            return;
        }

        StudentAccountService.StudentAccount selected =
                (StudentAccountService.StudentAccount)
                        accountComboBox.getSelectedItem();

        if (selected == null) {
            updateUsernameField.setText("");
            clearUpdatePasswords();
            return;
        }

        updateUsernameField.setText(selected.username());
        clearUpdatePasswords();
    }

    // =====================================================
    // CREATE ACCOUNT
    // =====================================================

    private void createAccount() {

        MemberItem selected =
                (MemberItem) memberComboBox.getSelectedItem();

        if (selected == null || selected.memberId <= 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an eligible member.",
                    "No Member Selected",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String username = createUsernameField.getText().trim();

        char[] password = createPasswordField.getPassword();
        char[] confirmation = confirmCreatePasswordField.getPassword();

        boolean created = false;

        try {

            if (username.isBlank()) {
                throw new IllegalArgumentException(
                        "Please enter a username."
                );
            }

            if (password.length < 8) {
                throw new IllegalArgumentException(
                        "Password must contain at least 8 characters."
                );
            }

            if (!Arrays.equals(password, confirmation)) {
                throw new IllegalArgumentException(
                        "Passwords do not match."
                );
            }

            StudentAccountService.createStudentAccount(
                    selected.memberId,
                    username,
                    new String(password)
            );

            created = true;

            JOptionPane.showMessageDialog(
                    this,
                    "Student account created successfully!\n"
                            + "Student: " + selected.fullName
                            + "\nUsername: " + username,
                    "Account Created",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearCreateFields();

            loadActiveMembers();
            loadStudentAccounts();

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Unable to Create Account",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException | GeneralSecurityException e) {

            showError("Unable to create account:\n" + e.getMessage());
            e.printStackTrace();

        } finally {

            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');

            if (created) {
                clearCreatePasswords();
            }
        }
    }

    // =====================================================
    // UPDATE ACCOUNT
    // =====================================================

    private void updateAccount() {

        StudentAccountService.StudentAccount selected =
                (StudentAccountService.StudentAccount)
                        accountComboBox.getSelectedItem();

        if (selected == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a student account.",
                    "No Account Selected",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String username = updateUsernameField.getText().trim();

        char[] password = newPasswordField.getPassword();
        char[] confirmation = confirmNewPasswordField.getPassword();

        boolean updated = false;

        try {

            if (username.isBlank()) {
                throw new IllegalArgumentException(
                        "Username is required."
                );
            }

            if (password.length > 0 || confirmation.length > 0) {

                if (password.length < 8) {
                    throw new IllegalArgumentException(
                            "New password must contain at least 8 characters."
                    );
                }

                if (!Arrays.equals(password, confirmation)) {
                    throw new IllegalArgumentException(
                            "New passwords do not match."
                    );
                }
            }

            boolean usernameChanged =
                    !username.equals(selected.username());

            boolean passwordChanged = password.length > 0;

            if (!usernameChanged && !passwordChanged) {

                JOptionPane.showMessageDialog(
                        this,
                        "No changes were entered.",
                        "Nothing to Update",
                        JOptionPane.INFORMATION_MESSAGE
                );

                return;
            }

            StringBuilder changes = new StringBuilder();

            if (usernameChanged) {
                changes.append("Username");
            }

            if (passwordChanged) {

                if (changes.length() > 0) {
                    changes.append(" and ");
                }

                changes.append("Password");
            }

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Save " + changes + " changes for "
                            + selected.fullName() + "?",
                    "Confirm Account Update",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }

            StudentAccountService.updateStudentAccount(
                    selected.userId(),
                    username,
                    password.length == 0 ? null : new String(password)
            );

            updated = true;

            JOptionPane.showMessageDialog(
                    this,
                    "Student account updated successfully!\n\n"
                            + "Student: " + selected.fullName()
                            + "\nUsername: " + username
                            + "\nUpdated: " + changes,
                    "Update Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadStudentAccounts();

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Unable to Update Account",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException | GeneralSecurityException e) {

            showError("Unable to update account:\n" + e.getMessage());
            e.printStackTrace();

        } finally {

            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');

            if (updated) {
                clearUpdatePasswords();
            }
        }
    }

    // =====================================================
    // CLEAR FORM FIELDS
    // =====================================================

    private void clearCreatePasswords() {

        if (createPasswordField != null) {
            createPasswordField.setText("");
        }

        if (confirmCreatePasswordField != null) {
            confirmCreatePasswordField.setText("");
        }

        if (strengthBar != null && showingCreateTab) {

            strengthBar.setValue(0);

            if (strengthLabel != null) {
                strengthLabel.setText("Not entered");
                strengthLabel.setForeground(MUTED);
            }
        }
    }

    private void clearCreateFields() {

        if (createUsernameField != null) {
            createUsernameField.setText("");
        }

        clearCreatePasswords();

        if (memberComboBox != null && memberComboBox.getItemCount() > 0) {
            memberComboBox.setSelectedIndex(0);
        }

        updatePreviewForCreate();
    }

    private void clearUpdatePasswords() {

        if (newPasswordField != null) {
            newPasswordField.setText("");
        }

        if (confirmNewPasswordField != null) {
            confirmNewPasswordField.setText("");
        }
    }

    // =====================================================
    // ERROR MESSAGE
    // =====================================================

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "System Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // =====================================================
    // ROUNDED PANEL
    // =====================================================

    private static class RoundedPanel extends JPanel {

        private final Color backgroundColor;
        private final int radius;

        RoundedPanel(Color backgroundColor, int radius) {

            this.backgroundColor = backgroundColor;
            this.radius = radius;

            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {

            Graphics2D g2 = (Graphics2D) graphics.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            g2.setColor(new Color(0, 0, 0, 12));

            g2.fill(new RoundRectangle2D.Double(
                    2, 3,
                    getWidth() - 4,
                    getHeight() - 5,
                    radius, radius
            ));

            g2.setColor(backgroundColor);

            g2.fill(new RoundRectangle2D.Double(
                    0, 0,
                    getWidth() - 4,
                    getHeight() - 5,
                    radius, radius
            ));

            g2.setColor(BORDER);

            g2.draw(new RoundRectangle2D.Double(
                    0.5, 0.5,
                    getWidth() - 5,
                    getHeight() - 6,
                    radius, radius
            ));

            g2.dispose();

            super.paintComponent(graphics);
        }
    }

    // =====================================================
    // IMPROVED CUSTOM DRAWN ICONS
    // =====================================================

    private static class DrawIcon implements Icon {

        private final String type;
        private final Color color;
        private final int size;

        DrawIcon(String type, Color color, int size) {
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
                    1.7f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            int w = size;
            int h = size;

            switch (type) {

                case "BACK" -> {
                    g.drawLine(w - 4, h / 2, 4, h / 2);
                    g.drawLine(4, h / 2, 10, 5);
                    g.drawLine(4, h / 2, 10, h - 5);
                }

                case "PROFILE" -> {
                    g.drawOval(w / 2 - 4, 2, 8, 8);
                    g.drawArc(
                            w / 2 - 7,
                            h / 2,
                            14,
                            11,
                            0,
                            180
                    );
                }

                case "PROFILE_PLUS" -> {

                    // Profile symbol on the left
                    g.drawOval(2, 2, 7, 7);
                    g.drawArc(0, 11, 11, 7, 0, 180);

                    // Separate plus symbol on the right
                    int centerX = w - 5;
                    int centerY = h / 2 + 1;

                    g.setStroke(new BasicStroke(
                            2.0f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    ));

                    g.drawLine(centerX, centerY - 4,
                            centerX, centerY + 4);

                    g.drawLine(centerX - 4, centerY,
                            centerX + 4, centerY);
                }

                case "PROFILE_EDIT" -> {

                    // Profile symbol on the left
                    g.drawOval(2, 2, 7, 7);
                    g.drawArc(0, 11, 11, 7, 0, 180);

                    // Separate edit pencil on the right
                    g.setStroke(new BasicStroke(
                            2.0f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    ));

                    g.drawLine(
                            w - 8, h - 5,
                            w - 3, h - 10
                    );

                    // Pencil tip
                    g.drawLine(
                            w - 4, h - 11,
                            w - 2, h - 9
                    );
                }

                case "PLUS" -> {
                    g.drawLine(w / 2, 3, w / 2, h - 3);
                    g.drawLine(3, h / 2, w - 3, h / 2);
                }

                case "EYE" -> {
                    g.drawArc(2, h / 4, w - 4, h / 2, 0, 180);
                    g.drawArc(2, h / 4, w - 4, h / 2, 180, 180);
                    g.drawOval(w / 2 - 2, h / 2 - 2, 4, 4);
                }

                case "EYE_OFF" -> {
                    g.drawArc(2, h / 4, w - 4, h / 2, 0, 180);
                    g.drawArc(2, h / 4, w - 4, h / 2, 180, 180);
                    g.drawOval(w / 2 - 2, h / 2 - 2, 4, 4);
                    g.drawLine(3, h - 3, w - 3, 3);
                }

                case "SHIELD" -> {
                    Polygon shield = new Polygon();
                    shield.addPoint(w / 2, 2);
                    shield.addPoint(w - 3, 5);
                    shield.addPoint(w - 4, h / 2 + 2);
                    shield.addPoint(w / 2, h - 2);
                    shield.addPoint(4, h / 2 + 2);
                    shield.addPoint(3, 5);

                    g.drawPolygon(shield);
                    g.drawLine(w / 2, 6, w / 2, h / 2 + 2);
                    g.fillOval(w / 2 - 1, h / 2 + 4, 3, 3);
                }

                case "SAVE" -> {
                    g.drawRoundRect(3, 3, w - 6, h - 6, 2, 2);
                    g.drawRect(6, 3, w - 12, 6);
                    g.drawRect(6, h / 2 + 1, w - 12, h / 2 - 5);
                }

                case "CARD" -> {
                    g.drawRoundRect(2, 4, w - 4, h - 8, 2, 2);
                    g.drawLine(2, h / 2 - 1, w - 2, h / 2 - 1);
                    g.drawLine(5, h - 6, w / 2 - 1, h - 6);
                }

                default -> g.drawOval(3, 3, w - 6, h - 6);
            }

            g.dispose();
        }
    }

    // =====================================================
    // MEMBER COMBO BOX ITEM
    // =====================================================

    private static class MemberItem {

        private final int memberId;
        private final String fullName;
        private final String studentId;
        private final String courseDepartment;

        public MemberItem(
                int memberId,
                String fullName,
                String studentId,
                String courseDepartment
        ) {

            this.memberId = memberId;
            this.fullName = fullName;
            this.studentId = studentId;
            this.courseDepartment = courseDepartment;
        }

        @Override
        public String toString() {
            return fullName;
        }
    }
}
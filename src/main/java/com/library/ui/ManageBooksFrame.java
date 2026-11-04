
package com.library.ui;

import com.library.service.BookService;
import com.library.service.BookService.Book;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ManageBooksFrame extends JFrame {

    private final Color NAVY = new Color(23, 43, 77);
    private final Color LIGHT_BG = new Color(245, 247, 250);
    private final Color WHITE = Color.WHITE;

    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public ManageBooksFrame() {
        setTitle("Manage Books");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        loadBooks();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(18, 25, 18, 25));

        JLabel title = new JLabel("Manage Books");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(WHITE);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        header.add(title, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBackground(LIGHT_BG);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(LIGHT_BG);

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(300, 38));

        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Refresh");
        JButton addButton = new JButton("Add Book");
        JButton editButton = new JButton("Edit Selected");
        JButton deleteButton = new JButton("Delete Selected");

        searchButton.addActionListener(e -> searchBooks());
        refreshButton.addActionListener(e -> loadBooks());
        addButton.addActionListener(e -> showAddBookDialog());
        editButton.addActionListener(e -> editSelectedBook());
        deleteButton.addActionListener(e -> deleteSelectedBook());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(LIGHT_BG);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(LIGHT_BG);
        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);

        String[] columns = {
                "ID", "ISBN", "Title", "Author", "Category",
                "Publisher", "Year", "Total Copies",
                "Available", "Shelf Location"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookTable = new JTable(tableModel);
        bookTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bookTable.setRowHeight(30);
        bookTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        bookTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        bookTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232))
        );

        content.add(topPanel, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);

        return content;
    }

    private void loadBooks() {
        try {
            displayBooks(BookService.getAllBooks());
        } catch (SQLException e) {
            showError("Unable to load books.", e);
        }
    }

    private void searchBooks() {
        try {
            displayBooks(
                    BookService.searchBooks(searchField.getText().trim())
            );
        } catch (SQLException e) {
            showError("Unable to search books.", e);
        }
    }

    private void displayBooks(List<Book> books) {
        tableModel.setRowCount(0);

        for (Book book : books) {
            tableModel.addRow(new Object[]{
                    book.getBookId(),
                    book.getIsbn(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCategory(),
                    book.getPublisher(),
                    book.getPublicationYear(),
                    book.getTotalCopies(),
                    book.getAvailableCopies(),
                    book.getShelfLocation()
            });
        }
    }

    private void showAddBookDialog() {
        JTextField isbnField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField publisherField = new JTextField();
        JTextField yearField = new JTextField();
        JTextField copiesField = new JTextField();
        JTextField shelfField = new JTextField();

        JPanel form = new JPanel(new GridLayout(8, 2, 8, 8));

        form.add(new JLabel("ISBN:"));
        form.add(isbnField);
        form.add(new JLabel("Title:"));
        form.add(titleField);
        form.add(new JLabel("Author:"));
        form.add(authorField);
        form.add(new JLabel("Category:"));
        form.add(categoryField);
        form.add(new JLabel("Publisher:"));
        form.add(publisherField);
        form.add(new JLabel("Publication Year:"));
        form.add(yearField);
        form.add(new JLabel("Total Copies:"));
        form.add(copiesField);
        form.add(new JLabel("Shelf Location:"));
        form.add(shelfField);

        int option = JOptionPane.showConfirmDialog(
                this,
                form,
                "Add New Book",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) return;

        String title = titleField.getText().trim();
        String author = authorField.getText().trim();

        if (title.isEmpty() || author.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Title and Author are required.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            int year = yearField.getText().isBlank()
                    ? 0 : Integer.parseInt(yearField.getText().trim());

            int copies = Integer.parseInt(copiesField.getText().trim());

            if (copies < 1 || year < 0 || year > 9999) {
                throw new NumberFormatException();
            }

            Book book = new Book(
                    0,
                    emptyToNull(isbnField.getText()),
                    title,
                    author,
                    emptyToNull(categoryField.getText()),
                    emptyToNull(publisherField.getText()),
                    year,
                    copies,
                    copies,
                    emptyToNull(shelfField.getText())
            );

            if (BookService.addBook(book)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Book added successfully!"
                );
                loadBooks();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid publication year and copy count.",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (SQLException e) {
            showError(
                    "Unable to add book. Check whether its ISBN already exists.",
                    e
            );
        }
    }

    private void editSelectedBook() {
        int selectedRow = bookTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a book first.",
                    "No Book Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow = bookTable.convertRowIndexToModel(selectedRow);

        int bookId = (int) tableModel.getValueAt(modelRow, 0);
        String isbn = (String) tableModel.getValueAt(modelRow, 1);
        String title = (String) tableModel.getValueAt(modelRow, 2);
        String author = (String) tableModel.getValueAt(modelRow, 3);
        String category = (String) tableModel.getValueAt(modelRow, 4);
        String publisher = (String) tableModel.getValueAt(modelRow, 5);
        int year = (int) tableModel.getValueAt(modelRow, 6);
        int totalCopies = (int) tableModel.getValueAt(modelRow, 7);
        int availableCopies = (int) tableModel.getValueAt(modelRow, 8);
        String shelf = (String) tableModel.getValueAt(modelRow, 9);

        JTextField isbnField = new JTextField(valueOrEmpty(isbn));
        JTextField titleField = new JTextField(title);
        JTextField authorField = new JTextField(author);
        JTextField categoryField = new JTextField(valueOrEmpty(category));
        JTextField publisherField = new JTextField(valueOrEmpty(publisher));
        JTextField yearField = new JTextField(String.valueOf(year));
        JTextField totalCopiesField =
                new JTextField(String.valueOf(totalCopies));
        JTextField shelfField = new JTextField(valueOrEmpty(shelf));

        JPanel form = new JPanel(new GridLayout(8, 2, 8, 8));

        form.add(new JLabel("ISBN:"));
        form.add(isbnField);
        form.add(new JLabel("Title:"));
        form.add(titleField);
        form.add(new JLabel("Author:"));
        form.add(authorField);
        form.add(new JLabel("Category:"));
        form.add(categoryField);
        form.add(new JLabel("Publisher:"));
        form.add(publisherField);
        form.add(new JLabel("Publication Year:"));
        form.add(yearField);
        form.add(new JLabel("Total Copies:"));
        form.add(totalCopiesField);
        form.add(new JLabel("Shelf Location:"));
        form.add(shelfField);

        int option = JOptionPane.showConfirmDialog(
                this,
                form,
                "Edit Book Information",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) return;

        String newTitle = titleField.getText().trim();
        String newAuthor = authorField.getText().trim();

        if (newTitle.isEmpty() || newAuthor.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Title and Author are required.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            int newYear = Integer.parseInt(yearField.getText().trim());
            int newTotalCopies =
                    Integer.parseInt(totalCopiesField.getText().trim());

            int borrowedCopies = totalCopies - availableCopies;
            int newAvailableCopies = newTotalCopies - borrowedCopies;

            if (newYear < 0 || newYear > 9999
                    || newTotalCopies < 1
                    || newAvailableCopies < 0) {
                throw new IllegalArgumentException(
                        "Total copies cannot be less than the number currently borrowed."
                );
            }

            Book updatedBook = new Book(
                    bookId,
                    emptyToNull(isbnField.getText()),
                    newTitle,
                    newAuthor,
                    emptyToNull(categoryField.getText()),
                    emptyToNull(publisherField.getText()),
                    newYear,
                    newTotalCopies,
                    newAvailableCopies,
                    emptyToNull(shelfField.getText())
            );

            if (BookService.updateBook(updatedBook)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Book information updated successfully!"
                );
                loadBooks();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter valid numbers for publication year and total copies.",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Invalid Copy Count",
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (SQLException e) {
            showError(
                    "Unable to update book. Check whether its ISBN already exists.",
                    e
            );
        }
    }

    private void deleteSelectedBook() {
        int selectedRow = bookTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a book first."
            );
            return;
        }

        int modelRow = bookTable.convertRowIndexToModel(selectedRow);

        int bookId = (int) tableModel.getValueAt(modelRow, 0);
        String title = (String) tableModel.getValueAt(modelRow, 2);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete \"" + title + "\"?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            if (BookService.deleteBook(bookId)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Book deleted successfully!"
                );
                loadBooks();
            }
        } catch (SQLException e) {
            showError(
                    "Unable to delete book. It may have borrowing records.",
                    e
            );
        }
    }

    private String emptyToNull(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private void showError(String message, Exception e) {
        JOptionPane.showMessageDialog(
                this,
                message + "\n" + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
        e.printStackTrace();
    }
}

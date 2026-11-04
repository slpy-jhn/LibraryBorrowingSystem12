
package com.library.service;

import com.library.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookService {

    public static class Book {
        private final int bookId;
        private final String isbn;
        private final String title;
        private final String author;
        private final String category;
        private final String publisher;
        private final int publicationYear;
        private final int totalCopies;
        private final int availableCopies;
        private final String shelfLocation;

        public Book(int bookId, String isbn, String title,
                    String author, String category, String publisher,
                    int publicationYear, int totalCopies,
                    int availableCopies, String shelfLocation) {
            this.bookId = bookId;
            this.isbn = isbn;
            this.title = title;
            this.author = author;
            this.category = category;
            this.publisher = publisher;
            this.publicationYear = publicationYear;
            this.totalCopies = totalCopies;
            this.availableCopies = availableCopies;
            this.shelfLocation = shelfLocation;
        }

        public int getBookId() { return bookId; }
        public String getIsbn() { return isbn; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getCategory() { return category; }
        public String getPublisher() { return publisher; }
        public int getPublicationYear() { return publicationYear; }
        public int getTotalCopies() { return totalCopies; }
        public int getAvailableCopies() { return availableCopies; }
        public String getShelfLocation() { return shelfLocation; }
    }

    // ADD BOOK
    public static boolean addBook(Book book) throws SQLException {
        String sql = """
                INSERT INTO books (
                    isbn, title, author, category, publisher,
                    publication_year, total_copies,
                    available_copies, shelf_location
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, book.getIsbn());
            statement.setString(2, book.getTitle());
            statement.setString(3, book.getAuthor());
            statement.setString(4, book.getCategory());
            statement.setString(5, book.getPublisher());
            statement.setInt(6, book.getPublicationYear());
            statement.setInt(7, book.getTotalCopies());
            statement.setInt(8, book.getTotalCopies());
            statement.setString(9, book.getShelfLocation());

            return statement.executeUpdate() > 0;
        }
    }

    // GET ALL BOOKS
    public static List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();

        String sql = "SELECT * FROM books ORDER BY title";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                books.add(new Book(
                        result.getInt("book_id"),
                        result.getString("isbn"),
                        result.getString("title"),
                        result.getString("author"),
                        result.getString("category"),
                        result.getString("publisher"),
                        result.getInt("publication_year"),
                        result.getInt("total_copies"),
                        result.getInt("available_copies"),
                        result.getString("shelf_location")
                ));
            }
        }

        return books;
    }

    // SEARCH BOOKS
    public static List<Book> searchBooks(String keyword) throws SQLException {
        List<Book> books = new ArrayList<>();

        String sql = """
                SELECT * FROM books
                WHERE title LIKE ?
                   OR author LIKE ?
                   OR isbn LIKE ?
                   OR category LIKE ?
                ORDER BY title
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String search = "%" + keyword + "%";

            statement.setString(1, search);
            statement.setString(2, search);
            statement.setString(3, search);
            statement.setString(4, search);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    books.add(new Book(
                            result.getInt("book_id"),
                            result.getString("isbn"),
                            result.getString("title"),
                            result.getString("author"),
                            result.getString("category"),
                            result.getString("publisher"),
                            result.getInt("publication_year"),
                            result.getInt("total_copies"),
                            result.getInt("available_copies"),
                            result.getString("shelf_location")
                    ));
                }
            }
        }

        return books;
    }

    // DELETE BOOK
    public static boolean deleteBook(int bookId) throws SQLException {
        String sql = "DELETE FROM books WHERE book_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bookId);
            return statement.executeUpdate() > 0;
        }
    }

    // UPDATE BOOK
    public static boolean updateBook(Book book) throws SQLException {

        String sql = """
                UPDATE books
                SET isbn = ?,
                    title = ?,
                    author = ?,
                    category = ?,
                    publisher = ?,
                    publication_year = ?,
                    total_copies = ?,
                    available_copies = ?,
                    shelf_location = ?
                WHERE book_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, book.getIsbn());
            statement.setString(2, book.getTitle());
            statement.setString(3, book.getAuthor());
            statement.setString(4, book.getCategory());
            statement.setString(5, book.getPublisher());
            statement.setInt(6, book.getPublicationYear());
            statement.setInt(7, book.getTotalCopies());
            statement.setInt(8, book.getAvailableCopies());
            statement.setString(9, book.getShelfLocation());
            statement.setInt(10, book.getBookId());

            return statement.executeUpdate() > 0;
        }
    }
}

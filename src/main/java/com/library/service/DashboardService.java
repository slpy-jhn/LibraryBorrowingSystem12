
package com.library.service;

import com.library.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardService {

    public static class DashboardStats {
        private final int totalBooks;
        private final int registeredMembers;
        private final int booksBorrowed;
        private final int overdueBooks;

        public DashboardStats(
                int totalBooks,
                int registeredMembers,
                int booksBorrowed,
                int overdueBooks
        ) {
            this.totalBooks = totalBooks;
            this.registeredMembers = registeredMembers;
            this.booksBorrowed = booksBorrowed;
            this.overdueBooks = overdueBooks;
        }

        public int getTotalBooks() {
            return totalBooks;
        }

        public int getRegisteredMembers() {
            return registeredMembers;
        }

        public int getBooksBorrowed() {
            return booksBorrowed;
        }

        public int getOverdueBooks() {
            return overdueBooks;
        }
    }

    public static DashboardStats getDashboardStats()
            throws SQLException {

        String sql = """
                SELECT
                    (SELECT COALESCE(SUM(total_copies), 0)
                     FROM books) AS total_books,

                    (SELECT COUNT(*)
                     FROM members) AS registered_members,

                    (SELECT COUNT(*)
                     FROM transactions
                     WHERE status = 'BORROWED'
                       AND return_date IS NULL) AS books_borrowed,

                    (SELECT COUNT(*)
                     FROM transactions
                     WHERE status IN ('BORROWED', 'OVERDUE')
                       AND return_date IS NULL
                       AND due_date < date('now', 'localtime'))
                     AS overdue_books
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet result = statement.executeQuery()) {

            if (result.next()) {
                return new DashboardStats(
                        result.getInt("total_books"),
                        result.getInt("registered_members"),
                        result.getInt("books_borrowed"),
                        result.getInt("overdue_books")
                );
            }
        }

        return new DashboardStats(0, 0, 0, 0);
    }
}


package com.library.service;

import com.library.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class BorrowService {

    public static boolean borrowBook(
            int memberId,
            int bookId,
            LocalDate borrowingDate,
            LocalDate dueDate
    ) throws SQLException {

        if (borrowingDate == null || dueDate == null) {
            throw new IllegalArgumentException(
                    "Borrowing date and due date are required."
            );
        }

        if (dueDate.isBefore(borrowingDate)) {
            throw new IllegalArgumentException(
                    "Due date cannot be before borrowing date."
            );
        }

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // CHECK MEMBER STATUS
                String memberSql = """
                        SELECT membership_status
                        FROM members
                        WHERE member_id = ?
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(memberSql)) {

                    statement.setInt(1, memberId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException(
                                    "Member does not exist."
                            );
                        }

                        if (!"ACTIVE".equalsIgnoreCase(
                                result.getString("membership_status"))) {
                            throw new SQLException(
                                    "This member is inactive."
                            );
                        }
                    }
                }

                // CHECK BOOK AVAILABILITY
                String bookSql = """
                        SELECT available_copies
                        FROM books
                        WHERE book_id = ?
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(bookSql)) {

                    statement.setInt(1, bookId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException(
                                    "Book does not exist."
                            );
                        }

                        if (result.getInt("available_copies") <= 0) {
                            throw new SQLException(
                                    "This book is currently unavailable."
                            );
                        }
                    }
                }

                // DECREASE AVAILABLE COPIES
                String updateBookSql = """
                        UPDATE books
                        SET available_copies = available_copies - 1
                        WHERE book_id = ?
                          AND available_copies > 0
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(updateBookSql)) {

                    statement.setInt(1, bookId);

                    if (statement.executeUpdate() == 0) {
                        throw new SQLException(
                                "Book is no longer available."
                        );
                    }
                }

                // SAVE BORROWING TRANSACTION
                String transactionSql = """
                        INSERT INTO transactions (
                            member_id,
                            book_id,
                            borrowing_date,
                            due_date,
                            status
                        )
                        VALUES (?, ?, ?, ?, 'BORROWED')
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(transactionSql)) {

                    statement.setInt(1, memberId);
                    statement.setInt(2, bookId);
                    statement.setString(
                            3, borrowingDate.toString()
                    );
                    statement.setString(
                            4, dueDate.toString()
                    );

                    statement.executeUpdate();
                }

                connection.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}

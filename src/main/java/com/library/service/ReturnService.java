
package com.library.service;

import com.library.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class ReturnService {

    public static boolean returnBook(
            int transactionId,
            LocalDate returnDate
    ) throws SQLException {

        if (returnDate == null) {
            throw new IllegalArgumentException(
                    "Return date is required."
            );
        }

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // FIND THE ACTIVE BORROWING TRANSACTION
                String checkSql = """
                        SELECT book_id, borrowing_date
                        FROM transactions
                        WHERE transaction_id = ?
                          AND status IN ('BORROWED', 'OVERDUE')
                          AND return_date IS NULL
                        """;

                int bookId;
                LocalDate borrowingDate;

                try (PreparedStatement statement =
                             connection.prepareStatement(checkSql)) {

                    statement.setInt(1, transactionId);

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            throw new SQLException(
                                    "Transaction not found or book already returned."
                            );
                        }

                        bookId = result.getInt("book_id");

                        borrowingDate = LocalDate.parse(
                                result.getString("borrowing_date")
                        );
                    }
                }

                if (returnDate.isBefore(borrowingDate)) {
                    throw new IllegalArgumentException(
                            "Return date cannot be before borrowing date."
                    );
                }

                // UPDATE TRANSACTION
                String updateTransactionSql = """
                        UPDATE transactions
                        SET return_date = ?,
                            status = 'RETURNED'
                        WHERE transaction_id = ?
                          AND return_date IS NULL
                          AND status IN ('BORROWED', 'OVERDUE')
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     updateTransactionSql)) {

                    statement.setString(1, returnDate.toString());
                    statement.setInt(2, transactionId);

                    if (statement.executeUpdate() == 0) {
                        throw new SQLException(
                                "Unable to process this return."
                        );
                    }
                }

                // INCREASE AVAILABLE BOOK COPIES
                String updateBookSql = """
                        UPDATE books
                        SET available_copies = available_copies + 1
                        WHERE book_id = ?
                          AND available_copies < total_copies
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(updateBookSql)) {

                    statement.setInt(1, bookId);

                    if (statement.executeUpdate() == 0) {
                        throw new SQLException(
                                "Unable to update book availability."
                        );
                    }
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


package com.library.service;

import com.library.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReturnRequestService {

    public record ReturnRequest(
            int returnRequestId,
            int transactionId,
            int memberId,
            String memberName,
            int bookId,
            String bookTitle,
            String borrowingDate,
            String dueDate,
            String requestDate,
            String status,
            String decisionDate,
            String rejectionReason
    ) {}

    // STUDENT: Submit a return request
    public static void submitReturnRequest(
            UserSession session,
            int transactionId
    ) throws SQLException {

        if (session == null) {
            throw new SecurityException("Please log in first.");
        }

        session.requireStudent();

        String checkSql = """
                SELECT transaction_id
                FROM transactions
                WHERE transaction_id = ?
                  AND member_id = ?
                  AND return_date IS NULL
                  AND status IN ('BORROWED', 'OVERDUE')
                """;

        String duplicateSql = """
                SELECT return_request_id
                FROM return_requests
                WHERE transaction_id = ?
                  AND status = 'PENDING'
                """;

        // Explicitly save PENDING status and request date.
        String insertSql = """
                INSERT INTO return_requests (
                    transaction_id,
                    status,
                    request_date
                )
                VALUES (
                    ?,
                    'PENDING',
                    datetime('now', 'localtime')
                )
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                // Verify that the transaction belongs to the student.
                try (PreparedStatement statement =
                             connection.prepareStatement(checkSql)) {

                    statement.setInt(1, transactionId);
                    statement.setInt(2, session.getMemberId());

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            throw new IllegalStateException(
                                    "This borrowed book cannot be returned."
                            );
                        }
                    }
                }

                // Prevent duplicate pending requests.
                try (PreparedStatement statement =
                             connection.prepareStatement(duplicateSql)) {

                    statement.setInt(1, transactionId);

                    try (ResultSet result = statement.executeQuery()) {

                        if (result.next()) {
                            throw new IllegalStateException(
                                    "You already have a pending return request for this book."
                            );
                        }
                    }
                }

                // Save the return request with PENDING status.
                try (PreparedStatement statement =
                             connection.prepareStatement(insertSql)) {

                    statement.setInt(1, transactionId);

                    int rowsInserted = statement.executeUpdate();

                    if (rowsInserted != 1) {
                        throw new SQLException(
                                "The return request could not be saved."
                        );
                    }
                }

                connection.commit();

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    // STUDENT: Get the logged-in student's complete return request history.
    public static List<ReturnRequest> getMyReturnRequests(
            UserSession session
    ) throws SQLException {

        if (session == null) {
            throw new SecurityException("Please log in first.");
        }

        session.requireStudent();

        List<ReturnRequest> requests = new ArrayList<>();

        String sql = """
                SELECT
                    rr.return_request_id,
                    t.transaction_id,
                    m.member_id,
                    m.full_name,
                    b.book_id,
                    b.title,
                    t.borrowing_date,
                    t.due_date,
                    rr.request_date,
                    rr.status,
                    rr.decision_date,
                    rr.rejection_reason

                FROM return_requests rr

                JOIN transactions t
                    ON rr.transaction_id = t.transaction_id

                JOIN members m
                    ON t.member_id = m.member_id

                JOIN books b
                    ON t.book_id = b.book_id

                WHERE t.member_id = ?

                ORDER BY rr.request_date DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, session.getMemberId());

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    requests.add(new ReturnRequest(
                            result.getInt("return_request_id"),
                            result.getInt("transaction_id"),
                            result.getInt("member_id"),
                            result.getString("full_name"),
                            result.getInt("book_id"),
                            result.getString("title"),
                            result.getString("borrowing_date"),
                            result.getString("due_date"),
                            result.getString("request_date"),
                            result.getString("status"),
                            result.getString("decision_date"),
                            result.getString("rejection_reason")
                    ));
                }
            }
        }

        return requests;
    }

    // ADMIN: Get all pending return requests
    public static List<ReturnRequest> getPendingRequests(
            UserSession session
    ) throws SQLException {

        if (session == null) {
            throw new SecurityException("Please log in first.");
        }

        session.requireAdmin();

        List<ReturnRequest> requests = new ArrayList<>();

        String sql = """
                SELECT
                    rr.return_request_id,
                    t.transaction_id,
                    m.member_id,
                    m.full_name,
                    b.book_id,
                    b.title,
                    t.borrowing_date,
                    t.due_date,
                    rr.request_date,
                    rr.status,
                    rr.decision_date,
                    rr.rejection_reason

                FROM return_requests rr

                JOIN transactions t
                    ON rr.transaction_id = t.transaction_id

                JOIN members m
                    ON t.member_id = m.member_id

                JOIN books b
                    ON t.book_id = b.book_id

                WHERE rr.status = 'PENDING'

                ORDER BY rr.request_date ASC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                requests.add(new ReturnRequest(
                        result.getInt("return_request_id"),
                        result.getInt("transaction_id"),
                        result.getInt("member_id"),
                        result.getString("full_name"),
                        result.getInt("book_id"),
                        result.getString("title"),
                        result.getString("borrowing_date"),
                        result.getString("due_date"),
                        result.getString("request_date"),
                        result.getString("status"),
                        result.getString("decision_date"),
                        result.getString("rejection_reason")
                ));
            }
        }

        return requests;
    }

    // ADMIN: Approve a return request
    public static void approveReturnRequest(
            UserSession session,
            int returnRequestId
    ) throws SQLException {

        if (session == null) {
            throw new SecurityException("Please log in first.");
        }

        session.requireAdmin();

        String findSql = """
                SELECT
                    rr.transaction_id,
                    t.book_id
                FROM return_requests rr

                JOIN transactions t
                    ON rr.transaction_id = t.transaction_id

                WHERE rr.return_request_id = ?
                  AND rr.status = 'PENDING'
                  AND t.return_date IS NULL
                """;

        String updateTransactionSql = """
                UPDATE transactions
                SET return_date = ?,
                    status = 'RETURNED'
                WHERE transaction_id = ?
                  AND return_date IS NULL
                """;

        String updateBookSql = """
                UPDATE books
                SET available_copies = available_copies + 1
                WHERE book_id = ?
                  AND available_copies < total_copies
                """;

        String updateRequestSql = """
                UPDATE return_requests
                SET status = 'APPROVED',
                    decision_date = datetime('now', 'localtime')
                WHERE return_request_id = ?
                  AND status = 'PENDING'
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                int transactionId;
                int bookId;

                // Find the pending request.
                try (PreparedStatement statement =
                             connection.prepareStatement(findSql)) {

                    statement.setInt(1, returnRequestId);

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            throw new IllegalStateException(
                                    "Return request not found or already processed."
                            );
                        }

                        transactionId = result.getInt("transaction_id");
                        bookId = result.getInt("book_id");
                    }
                }

                // Mark the transaction as returned.
                try (PreparedStatement statement =
                             connection.prepareStatement(updateTransactionSql)) {

                    statement.setString(
                            1,
                            LocalDate.now().toString()
                    );

                    statement.setInt(2, transactionId);

                    if (statement.executeUpdate() != 1) {
                        throw new IllegalStateException(
                                "Unable to update the borrowing transaction."
                        );
                    }
                }

                // Increase available book copies.
                try (PreparedStatement statement =
                             connection.prepareStatement(updateBookSql)) {

                    statement.setInt(1, bookId);

                    if (statement.executeUpdate() != 1) {
                        throw new IllegalStateException(
                                "Unable to update book availability."
                        );
                    }
                }

                // Approve the return request.
                try (PreparedStatement statement =
                             connection.prepareStatement(updateRequestSql)) {

                    statement.setInt(1, returnRequestId);

                    if (statement.executeUpdate() != 1) {
                        throw new IllegalStateException(
                                "Unable to approve the return request."
                        );
                    }
                }

                connection.commit();

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    // ADMIN: Reject a return request
    public static void rejectReturnRequest(
            UserSession session,
            int returnRequestId,
            String reason
    ) throws SQLException {

        if (session == null) {
            throw new SecurityException("Please log in first.");
        }

        session.requireAdmin();

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Please provide a rejection reason."
            );
        }

        String sql = """
                UPDATE return_requests
                SET status = 'REJECTED',
                    decision_date = datetime('now', 'localtime'),
                    rejection_reason = ?
                WHERE return_request_id = ?
                  AND status = 'PENDING'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, reason.trim());
            statement.setInt(2, returnRequestId);

            if (statement.executeUpdate() != 1) {
                throw new IllegalStateException(
                        "Return request not found or already processed."
                );
            }
        }
    }
}

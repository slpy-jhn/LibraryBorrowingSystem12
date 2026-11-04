
package com.library.service;

import com.library.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowRequestService {

    public record BorrowRequest(
            int requestId,
            int memberId,
            String memberName,
            int bookId,
            String bookTitle,
            String requestDate,
            String status,
            String decisionDate,
            String rejectionReason
    ) {}

    // ==========================================
    // STUDENT: SUBMIT BORROWING REQUEST
    // ==========================================

    public static void submitRequest(
            UserSession session,
            int bookId
    ) throws SQLException {

        if (session == null) {
            throw new SecurityException("Login is required.");
        }

        int memberId = session.requireStudent();

        if (bookId <= 0) {
            throw new IllegalArgumentException("Invalid book selection.");
        }

        String checkSql = """
                SELECT
                    m.membership_status,
                    b.available_copies
                FROM members m
                CROSS JOIN books b
                WHERE m.member_id = ?
                  AND b.book_id = ?
                """;

        String pendingCheckSql = """
                SELECT request_id
                FROM borrow_requests
                WHERE member_id = ?
                  AND book_id = ?
                  AND status = 'PENDING'
                """;

        String insertSql = """
                INSERT INTO borrow_requests (
                    member_id,
                    book_id,
                    status,
                    request_date
                )
                VALUES (
                    ?,
                    ?,
                    'PENDING',
                    datetime('now', 'localtime')
                )
                """;

        String loanCheckSql = """
                SELECT COUNT(*)
                FROM transactions
                WHERE member_id = ?
                  AND book_id = ?
                  AND return_date IS NULL
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                // Check student and book.
                try (PreparedStatement check =
                             connection.prepareStatement(checkSql)) {

                    check.setInt(1, memberId);
                    check.setInt(2, bookId);

                    try (ResultSet result = check.executeQuery()) {

                        if (!result.next()) {
                            throw new IllegalArgumentException(
                                    "Student or book record was not found."
                            );
                        }

                        if (!"ACTIVE".equalsIgnoreCase(
                                result.getString("membership_status"))) {

                            throw new IllegalStateException(
                                    "Your membership is not active."
                            );
                        }

                        if (result.getInt("available_copies") <= 0) {
                            throw new IllegalStateException(
                                    "This book is currently unavailable."
                            );
                        }
                    }
                }

                // Prevent duplicate active loans.
                try (PreparedStatement loanCheck =
                             connection.prepareStatement(loanCheckSql)) {

                    loanCheck.setInt(1, memberId);
                    loanCheck.setInt(2, bookId);

                    try (ResultSet result = loanCheck.executeQuery()) {

                        if (result.next() && result.getInt(1) > 0) {
                            throw new IllegalStateException(
                                    "You already have this book borrowed."
                            );
                        }
                    }
                }

                // Check whether a pending request already exists.
                try (PreparedStatement pendingCheck =
                             connection.prepareStatement(pendingCheckSql)) {

                    pendingCheck.setInt(1, memberId);
                    pendingCheck.setInt(2, bookId);

                    try (ResultSet result = pendingCheck.executeQuery()) {

                        if (result.next()) {
                            throw new IllegalStateException(
                                    "You already have a pending borrowing request for this book."
                            );
                        }
                    }
                }

                // Insert pending request.
                try (PreparedStatement insert =
                             connection.prepareStatement(insertSql)) {

                    insert.setInt(1, memberId);
                    insert.setInt(2, bookId);

                    if (insert.executeUpdate() != 1) {
                        throw new SQLException(
                                "Unable to save borrowing request."
                        );
                    }
                }

                connection.commit();

            } catch (SQLException e) {

                connection.rollback();

                // Handle duplicate pending request safely.
                if (e.getErrorCode() == 19 ||
                        (e.getMessage() != null &&
                                e.getMessage().contains("UNIQUE constraint failed"))) {

                    throw new IllegalStateException(
                            "You already have a pending borrowing request for this book.",
                            e
                    );
                }

                throw e;

            } catch (RuntimeException e) {

                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    // ==========================================
    // STUDENT: VIEW OWN REQUESTS
    // ==========================================

    public static List<BorrowRequest> getMyRequests(
            UserSession session
    ) throws SQLException {

        if (session == null) {
            throw new SecurityException("Login is required.");
        }

        int memberId = session.requireStudent();

        String sql = """
                SELECT
                    r.request_id,
                    r.member_id,
                    m.full_name,
                    r.book_id,
                    b.title,
                    r.request_date,
                    r.status,
                    r.decision_date,
                    r.rejection_reason
                FROM borrow_requests r
                JOIN members m ON r.member_id = m.member_id
                JOIN books b ON r.book_id = b.book_id
                WHERE r.member_id = ?
                ORDER BY r.request_id DESC
                """;

        List<BorrowRequest> requests = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, memberId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    requests.add(new BorrowRequest(
                            result.getInt("request_id"),
                            result.getInt("member_id"),
                            result.getString("full_name"),
                            result.getInt("book_id"),
                            result.getString("title"),
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

    // ==========================================
    // ADMIN: VIEW ALL PENDING REQUESTS
    // ==========================================

    public static List<BorrowRequest> getPendingRequests(
            UserSession session
    ) throws SQLException {

        requireAdmin(session);

        String sql = """
                SELECT
                    r.request_id,
                    r.member_id,
                    m.full_name,
                    r.book_id,
                    b.title,
                    r.request_date,
                    r.status,
                    r.decision_date,
                    r.rejection_reason
                FROM borrow_requests r
                JOIN members m ON r.member_id = m.member_id
                JOIN books b ON r.book_id = b.book_id
                WHERE r.status = 'PENDING'
                ORDER BY r.request_date ASC
                """;

        List<BorrowRequest> requests = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                requests.add(new BorrowRequest(
                        result.getInt("request_id"),
                        result.getInt("member_id"),
                        result.getString("full_name"),
                        result.getInt("book_id"),
                        result.getString("title"),
                        result.getString("request_date"),
                        result.getString("status"),
                        result.getString("decision_date"),
                        result.getString("rejection_reason")
                ));
            }
        }

        return requests;
    }

    // ==========================================
    // ADMIN: APPROVE REQUEST
    // ==========================================

    public static void approveRequest(
            UserSession session,
            int requestId,
            int borrowingDays
    ) throws SQLException {

        requireAdmin(session);

        if (requestId <= 0) {
            throw new IllegalArgumentException("Invalid request ID.");
        }

        if (borrowingDays <= 0 || borrowingDays > 365) {
            throw new IllegalArgumentException(
                    "Borrowing period must be between 1 and 365 days."
            );
        }

        String requestSql = """
                SELECT member_id, book_id
                FROM borrow_requests
                WHERE request_id = ?
                  AND status = 'PENDING'
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                int memberId;
                int bookId;

                // Retrieve pending request.
                try (PreparedStatement statement =
                             connection.prepareStatement(requestSql)) {

                    statement.setInt(1, requestId);

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            throw new IllegalStateException(
                                    "Request was not found or has already been processed."
                            );
                        }

                        memberId = result.getInt("member_id");
                        bookId = result.getInt("book_id");
                    }
                }

                // Check membership and available copies.
                String validationSql = """
                        SELECT m.membership_status, b.available_copies
                        FROM members m
                        CROSS JOIN books b
                        WHERE m.member_id = ?
                          AND b.book_id = ?
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(validationSql)) {

                    statement.setInt(1, memberId);
                    statement.setInt(2, bookId);

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            throw new IllegalStateException(
                                    "Student or book record no longer exists."
                            );
                        }

                        if (!"ACTIVE".equalsIgnoreCase(
                                result.getString("membership_status"))) {

                            throw new IllegalStateException(
                                    "The student's membership is inactive."
                            );
                        }

                        if (result.getInt("available_copies") <= 0) {
                            throw new IllegalStateException(
                                    "No copies are currently available."
                            );
                        }
                    }
                }

                // Prevent duplicate active loans.
                String activeLoanSql = """
                        SELECT COUNT(*)
                        FROM transactions
                        WHERE member_id = ?
                          AND book_id = ?
                          AND return_date IS NULL
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(activeLoanSql)) {

                    statement.setInt(1, memberId);
                    statement.setInt(2, bookId);

                    try (ResultSet result = statement.executeQuery()) {

                        if (result.next() && result.getInt(1) > 0) {
                            throw new IllegalStateException(
                                    "Student already has this book borrowed."
                            );
                        }
                    }
                }

                // Reduce available copies safely.
                String updateBookSql = """
                        UPDATE books
                        SET available_copies = available_copies - 1
                        WHERE book_id = ?
                          AND available_copies > 0
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(updateBookSql)) {

                    statement.setInt(1, bookId);

                    if (statement.executeUpdate() != 1) {
                        throw new IllegalStateException(
                                "Book availability changed. Approval cancelled."
                        );
                    }
                }

                // Create the actual borrowing transaction.
                LocalDate borrowingDate = LocalDate.now();
                LocalDate dueDate = borrowingDate.plusDays(borrowingDays);

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
                    statement.setString(3, borrowingDate.toString());
                    statement.setString(4, dueDate.toString());

                    statement.executeUpdate();
                }

                // Mark request as approved.
                String updateRequestSql = """
                        UPDATE borrow_requests
                        SET status = 'APPROVED',
                            decision_date = datetime('now', 'localtime')
                        WHERE request_id = ?
                          AND status = 'PENDING'
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(updateRequestSql)) {

                    statement.setInt(1, requestId);

                    if (statement.executeUpdate() != 1) {
                        throw new IllegalStateException(
                                "Request could not be approved."
                        );
                    }
                }

                connection.commit();

            } catch (SQLException e) {

                connection.rollback();
                throw e;

            } catch (RuntimeException e) {

                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    // ==========================================
    // ADMIN: REJECT REQUEST
    // ==========================================

    public static void rejectRequest(
            UserSession session,
            int requestId,
            String reason
    ) throws SQLException {

        requireAdmin(session);

        if (requestId <= 0) {
            throw new IllegalArgumentException("Invalid request ID.");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Please provide a rejection reason."
            );
        }

        String sql = """
                UPDATE borrow_requests
                SET status = 'REJECTED',
                    decision_date = datetime('now', 'localtime'),
                    rejection_reason = ?
                WHERE request_id = ?
                  AND status = 'PENDING'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, reason.trim());
            statement.setInt(2, requestId);

            if (statement.executeUpdate() != 1) {
                throw new IllegalStateException(
                        "Request was not found or has already been processed."
                );
            }
        }
    }

    // ==========================================
    // ADMIN ACCESS VALIDATION
    // ==========================================

    private static void requireAdmin(UserSession session) {

        if (session == null) {
            throw new SecurityException("Login is required.");
        }

        session.requireAdmin();
    }
}

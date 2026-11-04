
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
            int borrowingDuration,
            String requestDate,
            String status,
            String decisionDate,
            String rejectionReason
    ) {}

    // ==========================================
    // STUDENT: SUBMIT BORROWING REQUEST
    // ==========================================

    // Backward-compatible method.
    // Default borrowing period is 7 days.
    public static void submitRequest(
            UserSession session,
            int bookId
    ) throws SQLException {

        submitRequest(session, bookId, 7);
    }

    // Submit request with selected borrowing duration.
    public static void submitRequest(
            UserSession session,
            int bookId,
            int borrowingDays
    ) throws SQLException {

        if (session == null) {
            throw new SecurityException("Login is required.");
        }

        int memberId = session.requireStudent();

        if (bookId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid book selection."
            );
        }

        validateBorrowingDays(borrowingDays);

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

        String loanCheckSql = """
                SELECT COUNT(*)
                FROM transactions
                WHERE member_id = ?
                  AND book_id = ?
                  AND return_date IS NULL
                """;

        String insertSql = """
                INSERT INTO borrow_requests (
                    member_id,
                    book_id,
                    borrowing_duration,
                    status,
                    request_date
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    'PENDING',
                    datetime('now', 'localtime')
                )
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                // Check student and book records.
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
                try (PreparedStatement statement =
                             connection.prepareStatement(loanCheckSql)) {

                    statement.setInt(1, memberId);
                    statement.setInt(2, bookId);

                    try (ResultSet result = statement.executeQuery()) {

                        if (result.next() && result.getInt(1) > 0) {
                            throw new IllegalStateException(
                                    "You already have this book borrowed."
                            );
                        }
                    }
                }

                // Prevent duplicate pending requests.
                try (PreparedStatement statement =
                             connection.prepareStatement(pendingCheckSql)) {

                    statement.setInt(1, memberId);
                    statement.setInt(2, bookId);

                    try (ResultSet result = statement.executeQuery()) {

                        if (result.next()) {
                            throw new IllegalStateException(
                                    "You already have a pending borrowing request for this book."
                            );
                        }
                    }
                }

                // Insert borrowing request.
                try (PreparedStatement statement =
                             connection.prepareStatement(insertSql)) {

                    statement.setInt(1, memberId);
                    statement.setInt(2, bookId);
                    statement.setInt(3, borrowingDays);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Unable to save borrowing request."
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

    // ==========================================
    // STUDENT: VIEW OWN BORROWING REQUESTS
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
                    r.borrowing_duration,
                    r.request_date,
                    r.status,
                    r.decision_date,
                    r.rejection_reason
                FROM borrow_requests r
                JOIN members m
                    ON r.member_id = m.member_id
                JOIN books b
                    ON r.book_id = b.book_id
                WHERE r.member_id = ?
                ORDER BY r.request_id DESC
                """;

        List<BorrowRequest> requests = new ArrayList<>();

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, memberId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    requests.add(new BorrowRequest(
                            result.getInt("request_id"),
                            result.getInt("member_id"),
                            result.getString("full_name"),
                            result.getInt("book_id"),
                            result.getString("title"),
                            result.getInt("borrowing_duration"),
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
                    r.borrowing_duration,
                    r.request_date,
                    r.status,
                    r.decision_date,
                    r.rejection_reason
                FROM borrow_requests r
                JOIN members m
                    ON r.member_id = m.member_id
                JOIN books b
                    ON r.book_id = b.book_id
                WHERE r.status = 'PENDING'
                ORDER BY r.request_date ASC
                """;

        List<BorrowRequest> requests = new ArrayList<>();

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                requests.add(new BorrowRequest(
                        result.getInt("request_id"),
                        result.getInt("member_id"),
                        result.getString("full_name"),
                        result.getInt("book_id"),
                        result.getString("title"),
                        result.getInt("borrowing_duration"),
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
    // ADMIN: APPROVE BORROWING REQUEST
    // ==========================================

    // Uses the borrowing duration selected by the student.
    public static void approveRequest(
            UserSession session,
            int requestId
    ) throws SQLException {

        requireAdmin(session);

        if (requestId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid request ID."
            );
        }

        String requestSql = """
                SELECT
                    member_id,
                    book_id,
                    borrowing_duration
                FROM borrow_requests
                WHERE request_id = ?
                  AND status = 'PENDING'
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                int memberId;
                int bookId;
                int borrowingDays;

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
                        borrowingDays = result.getInt("borrowing_duration");
                    }
                }

                validateBorrowingDays(borrowingDays);

                // Validate membership and book availability.
                String validationSql = """
                        SELECT
                            m.membership_status,
                            b.available_copies
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

                // Reduce available copies.
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

                // Calculate borrowing and due dates.
                LocalDate borrowingDate = LocalDate.now();

                LocalDate dueDate =
                        borrowingDate.plusDays(borrowingDays);

                // Create borrowing transaction.
                String transactionSql = """
                        INSERT INTO transactions (
                            member_id,
                            book_id,
                            borrowing_date,
                            borrowing_duration,
                            due_date,
                            status
                        )
                        VALUES (?, ?, ?, ?, ?, 'BORROWED')
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(transactionSql)) {

                    statement.setInt(1, memberId);
                    statement.setInt(2, bookId);
                    statement.setString(3, borrowingDate.toString());
                    statement.setInt(4, borrowingDays);
                    statement.setString(5, dueDate.toString());

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Unable to create borrowing transaction."
                        );
                    }
                }

                // Update request status.
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

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    // Backward-compatible approval method.
    // The saved student duration takes priority.
    @Deprecated
    public static void approveRequest(
            UserSession session,
            int requestId,
            int borrowingDays
    ) throws SQLException {

        approveRequest(session, requestId);
    }

    // ==========================================
    // ADMIN: REJECT BORROWING REQUEST
    // ==========================================

    public static void rejectRequest(
            UserSession session,
            int requestId,
            String reason
    ) throws SQLException {

        requireAdmin(session);

        if (requestId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid request ID."
            );
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

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

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
    // BORROWING DURATION VALIDATION
    // ==========================================

    private static void validateBorrowingDays(
            int borrowingDays
    ) {

        if (borrowingDays <= 0 || borrowingDays > 365) {
            throw new IllegalArgumentException(
                    "Borrowing period must be between 1 and 365 days."
            );
        }
    }

    // ==========================================
    // ADMIN ACCESS VALIDATION
    // ==========================================

    private static void requireAdmin(UserSession session) {

        if (session == null) {
            throw new SecurityException(
                    "Login is required."
            );
        }

        session.requireAdmin();
    }
}

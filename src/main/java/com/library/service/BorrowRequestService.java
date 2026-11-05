
package com.library.service;

import com.library.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
            String rejectionReason,
            String requestedReturnDate,
            int borrowingDuration
    ) {}

    // ==========================================
    // DATABASE UPGRADE
    // ==========================================

    /**
     * Adds the new columns only when they do not already exist.
     * Existing borrowing requests and transactions are preserved.
     */
    private static void ensureRequestColumns(Connection connection)
            throws SQLException {

        boolean hasReturnDate = false;
        boolean hasDuration = false;

        String pragmaSql = "PRAGMA table_info(borrow_requests)";

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(pragmaSql)) {

            while (result.next()) {
                String columnName = result.getString("name");

                if ("requested_return_date".equalsIgnoreCase(columnName)) {
                    hasReturnDate = true;
                }

                if ("borrowing_duration".equalsIgnoreCase(columnName)) {
                    hasDuration = true;
                }
            }
        }

        if (!hasReturnDate) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(
                        "ALTER TABLE borrow_requests " +
                                "ADD COLUMN requested_return_date TEXT"
                );
            }
        }

        if (!hasDuration) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(
                        "ALTER TABLE borrow_requests " +
                                "ADD COLUMN borrowing_duration INTEGER DEFAULT 7"
                );
            }
        }
    }

    // ==========================================
    // STUDENT: SUBMIT BORROWING REQUEST
    // ==========================================

    /**
     * Compatibility method for existing screens.
     * Uses seven days as the default borrowing period.
     */
    public static void submitRequest(
            UserSession session,
            int bookId
    ) throws SQLException {

        submitRequest(session, bookId, 7);
    }

    /**
     * Compatibility method for screens that still submit a duration.
     */
    public static void submitRequest(
            UserSession session,
            int bookId,
            int borrowingDays
    ) throws SQLException {

        validateBorrowingDays(borrowingDays);

        LocalDate requestedReturnDate =
                LocalDate.now().plusDays(borrowingDays);

        submitRequest(session, bookId, requestedReturnDate);
    }

    /**
     * New method: saves the exact return date selected by the student.
     */
    public static void submitRequest(
            UserSession session,
            int bookId,
            LocalDate requestedReturnDate
    ) throws SQLException {

        if (session == null) {
            throw new SecurityException("Login is required.");
        }

        int memberId = session.requireStudent();

        if (bookId <= 0) {
            throw new IllegalArgumentException("Invalid book selection.");
        }

        if (requestedReturnDate == null) {
            throw new IllegalArgumentException(
                    "Please select a preferred return date."
            );
        }

        LocalDate today = LocalDate.now();

        if (!requestedReturnDate.isAfter(today)) {
            throw new IllegalArgumentException(
                    "The requested return date must be after today."
            );
        }

        long days = ChronoUnit.DAYS.between(
                today,
                requestedReturnDate
        );

        if (days > 365) {
            throw new IllegalArgumentException(
                    "The requested return date cannot be more than 365 days away."
            );
        }

        int borrowingDays = (int) days;

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
                    status,
                    request_date,
                    requested_return_date,
                    borrowing_duration
                )
                VALUES (
                    ?,
                    ?,
                    'PENDING',
                    datetime('now', 'localtime'),
                    ?,
                    ?
                )
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            ensureRequestColumns(connection);
            connection.setAutoCommit(false);

            try {

                // Check student membership and book availability.
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

                // Prevent duplicate pending requests.
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

                // Save the exact requested date.
                try (PreparedStatement insert =
                             connection.prepareStatement(insertSql)) {

                    insert.setInt(1, memberId);
                    insert.setInt(2, bookId);
                    insert.setString(3, requestedReturnDate.toString());
                    insert.setInt(4, borrowingDays);

                    if (insert.executeUpdate() != 1) {
                        throw new SQLException(
                                "Unable to save borrowing request."
                        );
                    }
                }

                connection.commit();

            } catch (SQLException e) {

                connection.rollback();

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
                    r.rejection_reason,
                    r.requested_return_date,
                    r.borrowing_duration
                FROM borrow_requests r
                JOIN members m ON r.member_id = m.member_id
                JOIN books b ON r.book_id = b.book_id
                WHERE r.member_id = ?
                ORDER BY r.request_id DESC
                """;

        List<BorrowRequest> requests = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection()) {

            ensureRequestColumns(connection);

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, memberId);

                try (ResultSet result = statement.executeQuery()) {

                    while (result.next()) {
                        requests.add(mapRequest(result));
                    }
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
                    r.rejection_reason,
                    r.requested_return_date,
                    r.borrowing_duration
                FROM borrow_requests r
                JOIN members m ON r.member_id = m.member_id
                JOIN books b ON r.book_id = b.book_id
                WHERE r.status = 'PENDING'
                ORDER BY r.request_date ASC
                """;

        List<BorrowRequest> requests = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection()) {

            ensureRequestColumns(connection);

            try (PreparedStatement statement =
                         connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    requests.add(mapRequest(result));
                }
            }
        }

        return requests;
    }

    // ==========================================
    // MAP DATABASE ROW TO BORROW REQUEST
    // ==========================================

    private static BorrowRequest mapRequest(ResultSet result)
            throws SQLException {

        return new BorrowRequest(
                result.getInt("request_id"),
                result.getInt("member_id"),
                result.getString("full_name"),
                result.getInt("book_id"),
                result.getString("title"),
                result.getString("request_date"),
                result.getString("status"),
                result.getString("decision_date"),
                result.getString("rejection_reason"),
                result.getString("requested_return_date"),
                result.getInt("borrowing_duration")
        );
    }

    // ==========================================
    // ADMIN: APPROVE REQUEST
    // ==========================================

    /**
     * New approval method.
     * Uses the exact return date saved by the student.
     */
    public static void approveRequest(
            UserSession session,
            int requestId
    ) throws SQLException {

        approveRequestInternal(session, requestId, null);
    }

    /**
     * Compatibility method for existing admin screens.
     * If a requested date exists, that date takes priority.
     * Otherwise, the supplied borrowing duration is used.
     */
    public static void approveRequest(
            UserSession session,
            int requestId,
            int borrowingDays
    ) throws SQLException {

        validateBorrowingDays(borrowingDays);

        approveRequestInternal(session, requestId, borrowingDays);
    }

    private static void approveRequestInternal(
            UserSession session,
            int requestId,
            Integer fallbackBorrowingDays
    ) throws SQLException {

        requireAdmin(session);

        if (requestId <= 0) {
            throw new IllegalArgumentException("Invalid request ID.");
        }

        String requestSql = """
                SELECT
                    member_id,
                    book_id,
                    requested_return_date,
                    borrowing_duration
                FROM borrow_requests
                WHERE request_id = ?
                  AND status = 'PENDING'
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            ensureRequestColumns(connection);
            connection.setAutoCommit(false);

            try {

                int memberId;
                int bookId;
                String savedReturnDate;
                int savedDuration;

                // Retrieve the pending request and its requested date.
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
                        savedReturnDate =
                                result.getString("requested_return_date");
                        savedDuration =
                                result.getInt("borrowing_duration");
                    }
                }

                LocalDate borrowingDate = LocalDate.now();
                LocalDate dueDate;

                if (savedReturnDate != null &&
                        !savedReturnDate.isBlank()) {

                    dueDate = LocalDate.parse(savedReturnDate.trim());

                } else {

                    int days;

                    if (fallbackBorrowingDays != null) {
                        days = fallbackBorrowingDays;
                    } else if (savedDuration > 0) {
                        days = savedDuration;
                    } else {
                        // Older requests created before this upgrade.
                        days = 7;
                    }

                    dueDate = borrowingDate.plusDays(days);
                }

                if (!dueDate.isAfter(borrowingDate)) {
                    throw new IllegalStateException(
                            "The student's requested return date has already passed. " +
                                    "The request cannot be approved with an expired date. " +
                                    "Please reject the request and ask the student to submit a new one."
                    );
                }

                // Check membership and available copies.
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

                // Create transaction using the requested return date.
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

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Unable to create borrowing transaction."
                        );
                    }
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

        try (Connection connection = DatabaseConnection.getConnection()) {

            ensureRequestColumns(connection);

            try (PreparedStatement statement =
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
    }

    // ==========================================
    // VALIDATION HELPERS
    // ==========================================

    private static void validateBorrowingDays(int borrowingDays) {

        if (borrowingDays <= 0 || borrowingDays > 365) {
            throw new IllegalArgumentException(
                    "Borrowing period must be between 1 and 365 days."
            );
        }
    }

    private static void requireAdmin(UserSession session) {

        if (session == null) {
            throw new SecurityException("Login is required.");
        }

        session.requireAdmin();
    }
}

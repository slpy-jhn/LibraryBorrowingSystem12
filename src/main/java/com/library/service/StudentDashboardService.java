
package com.library.service;

import com.library.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentDashboardService {

    public record StudentStats(
            int currentlyBorrowed,
            int overdueBooks,
            int returnedBooks
    ) {}

    public static StudentStats getStudentStats(int memberId)
            throws SQLException {

        validateMemberId(memberId);

        String sql = """
                SELECT
                    (SELECT COUNT(*)
                     FROM transactions
                     WHERE member_id = ?
                       AND return_date IS NULL
                       AND status IN ('BORROWED', 'OVERDUE')
                    ) AS currently_borrowed,

                    (SELECT COUNT(*)
                     FROM transactions
                     WHERE member_id = ?
                       AND return_date IS NULL
                       AND status IN ('BORROWED', 'OVERDUE')
                       AND due_date < date('now', 'localtime')
                    ) AS overdue_books,

                    (SELECT COUNT(*)
                     FROM transactions
                     WHERE member_id = ?
                       AND return_date IS NOT NULL
                    ) AS returned_books
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, memberId);
            statement.setInt(2, memberId);
            statement.setInt(3, memberId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return new StudentStats(
                            result.getInt("currently_borrowed"),
                            result.getInt("overdue_books"),
                            result.getInt("returned_books")
                    );
                }
            }
        }

        return new StudentStats(0, 0, 0);
    }

    private static void validateMemberId(int memberId) {
        if (memberId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid student account."
            );
        }
    }
}

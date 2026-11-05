
package com.library.service;

import com.library.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MemberBorrowingHistoryService {

    public record BorrowingRecord(
            int transactionId,
            int bookId,
            String bookTitle,
            String borrowingDate,
            String dueDate,
            String returnDate,
            String status
    ) {}

    public static List<BorrowingRecord> getMemberHistory(
            int memberId
    ) throws SQLException {

        List<BorrowingRecord> history = new ArrayList<>();

        String sql = """
                SELECT
                    t.transaction_id,
                    t.book_id,
                    b.title,
                    t.borrowing_date,
                    t.due_date,
                    t.return_date,
                    t.status
                FROM transactions t
                INNER JOIN books b
                    ON t.book_id = b.book_id
                WHERE t.member_id = ?
                ORDER BY t.borrowing_date DESC
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, memberId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    history.add(new BorrowingRecord(
                            result.getInt("transaction_id"),
                            result.getInt("book_id"),
                            result.getString("title"),
                            result.getString("borrowing_date"),
                            result.getString("due_date"),
                            result.getString("return_date"),
                            result.getString("status")
                    ));
                }
            }
        }

        return history;
    }
}


package com.library.service;

import com.library.database.DatabaseConnection;
import com.library.util.PasswordUtil;

import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentAccountService {

    public record StudentAccount(
            int userId,
            int memberId,
            String username,
            String fullName,
            String studentId
    ) {
        @Override
        public String toString() {
            return fullName + " (" + studentId + ") - " + username;
        }
    }

    public static void createStudentAccount(
            int memberId,
            String username,
            String password
    ) throws SQLException, GeneralSecurityException {

        if (memberId <= 0) {
            throw new IllegalArgumentException("Invalid member selected.");
        }

        validateUsername(username);
        validatePassword(password);

        String checkMemberSql = """
                SELECT member_id
                FROM members
                WHERE member_id = ?
                  AND membership_status = 'ACTIVE'
                """;

        String checkUsernameSql = """
                SELECT user_id
                FROM users
                WHERE LOWER(username) = LOWER(?)
                """;

        String checkAccountSql = """
                SELECT user_id
                FROM users
                WHERE member_id = ?
                """;

        String insertSql = """
                INSERT INTO users (
                    username, password_hash, role, member_id
                )
                VALUES (?, ?, 'STUDENT', ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            try (PreparedStatement statement =
                         connection.prepareStatement(checkMemberSql)) {

                statement.setInt(1, memberId);

                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new IllegalArgumentException(
                                "Member does not exist or is inactive."
                        );
                    }
                }
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(checkUsernameSql)) {

                statement.setString(1, username.trim());

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        throw new IllegalArgumentException(
                                "Username is already taken."
                        );
                    }
                }
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(checkAccountSql)) {

                statement.setInt(1, memberId);

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        throw new IllegalArgumentException(
                                "This member already has a login account."
                        );
                    }
                }
            }

            String passwordHash = PasswordUtil.hashPassword(password);

            try (PreparedStatement statement =
                         connection.prepareStatement(insertSql)) {

                statement.setString(1, username.trim());
                statement.setString(2, passwordHash);
                statement.setInt(3, memberId);

                statement.executeUpdate();
            }
        }
    }

    // Get all student accounts.
    public static List<StudentAccount> getStudentAccounts()
            throws SQLException {

        List<StudentAccount> accounts = new ArrayList<>();

        String sql = """
                SELECT
                    u.user_id,
                    u.member_id,
                    u.username,
                    m.full_name,
                    m.student_employee_id
                FROM users u
                JOIN members m ON u.member_id = m.member_id
                WHERE u.role = 'STUDENT'
                ORDER BY m.full_name
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                accounts.add(new StudentAccount(
                        result.getInt("user_id"),
                        result.getInt("member_id"),
                        result.getString("username"),
                        result.getString("full_name"),
                        result.getString("student_employee_id")
                ));
            }
        }

        return accounts;
    }

    // Update username and optionally update password.
    public static void updateStudentAccount(
            int userId,
            String username,
            String newPassword
    ) throws SQLException, GeneralSecurityException {

        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid student account.");
        }

        validateUsername(username);

        boolean changePassword =
                newPassword != null && !newPassword.isBlank();

        if (changePassword) {
            validatePassword(newPassword);
        }

        String checkAccountSql = """
                SELECT user_id
                FROM users
                WHERE user_id = ?
                  AND role = 'STUDENT'
                """;

        String checkUsernameSql = """
                SELECT user_id
                FROM users
                WHERE LOWER(username) = LOWER(?)
                  AND user_id <> ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            try (PreparedStatement statement =
                         connection.prepareStatement(checkAccountSql)) {

                statement.setInt(1, userId);

                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new IllegalArgumentException(
                                "Student account was not found."
                        );
                    }
                }
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(checkUsernameSql)) {

                statement.setString(1, username.trim());
                statement.setInt(2, userId);

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        throw new IllegalArgumentException(
                                "That username is already being used."
                        );
                    }
                }
            }

            if (changePassword) {

                String passwordHash =
                        PasswordUtil.hashPassword(newPassword);

                String sql = """
                        UPDATE users
                        SET username = ?, password_hash = ?
                        WHERE user_id = ?
                          AND role = 'STUDENT'
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    statement.setString(1, username.trim());
                    statement.setString(2, passwordHash);
                    statement.setInt(3, userId);

                    statement.executeUpdate();
                }

            } else {

                String sql = """
                        UPDATE users
                        SET username = ?
                        WHERE user_id = ?
                          AND role = 'STUDENT'
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    statement.setString(1, username.trim());
                    statement.setInt(2, userId);

                    statement.executeUpdate();
                }
            }
        }
    }

    private static void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username is required."
            );
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }
    }
}

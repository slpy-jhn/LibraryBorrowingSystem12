
package com.library.service;

import com.library.database.DatabaseConnection;
import com.library.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.GeneralSecurityException;

public class AuthService {

    // Authenticate user using SQLite database
    public static AuthResult authenticate(
            String username, char[] password)
            throws SQLException, GeneralSecurityException {

        if (username == null || username.isBlank()
                || password == null || password.length == 0) {
            return null;
        }

        String sql = """
                SELECT username, password_hash, role, member_id
                FROM users
                WHERE username = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    return null;
                }

                String storedHash = result.getString("password_hash");

                boolean valid = PasswordUtil.verifyPassword(
                        new String(password), storedHash);

                if (!valid) {
                    return null;
                }

                String role = result.getString("role");

                int memberIdValue = result.getInt("member_id");

                Integer memberId = result.wasNull()
                        ? null : memberIdValue;

                return new AuthResult(
                        result.getString("username"),
                        role,
                        memberId
                );
            }

        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    // Stores authenticated user information
    public record AuthResult(
            String username,
            String role,
            Integer memberId
    ) {}
}


package com.library.service;

import com.library.database.DatabaseConnection;
import com.library.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.security.GeneralSecurityException;

public class UserService {

    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "lib123";

    // Create default admin account if it does not exist
    public static void createDefaultAdmin()
            throws SQLException, GeneralSecurityException {

        String checkSql = """
                SELECT user_id FROM users
                WHERE username = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement check = connection.prepareStatement(checkSql)) {

            check.setString(1, DEFAULT_USERNAME);

            try (var result = check.executeQuery()) {

                if (result.next()) {
                    System.out.println("Admin account already exists.");
                    return;
                }
            }

            String hashedPassword =
                    PasswordUtil.hashPassword(DEFAULT_PASSWORD);

            String insertSql = """
                    INSERT INTO users (username, password_hash, role)
                    VALUES (?, ?, 'ADMIN')
                    """;

            try (PreparedStatement insert =
                         connection.prepareStatement(insertSql)) {

                insert.setString(1, DEFAULT_USERNAME);
                insert.setString(2, hashedPassword);

                insert.executeUpdate();

                System.out.println("Default Admin account created!");
            }
        }
    }
}

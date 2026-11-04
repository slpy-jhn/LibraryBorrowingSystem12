
package com.library;

import com.formdev.flatlaf.FlatLightLaf;
import com.library.database.DatabaseConnection;
import com.library.service.UserService;
import com.library.ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

public class Main {

    public static void main(String[] args) {

        System.out.println("Starting Library Borrowing System...");

        try {

            // Step 1: Initialize database
            DatabaseConnection.initializeDatabase();

            // Step 2: Verify borrowing requests table
            try (var connection = DatabaseConnection.getConnection();
                 var statement = connection.createStatement();
                 var result = statement.executeQuery("""
                         SELECT name
                         FROM sqlite_master
                         WHERE type = 'table'
                         AND name = 'borrow_requests'
                         """)) {

                if (!result.next()) {
                    throw new IllegalStateException(
                            "The borrow_requests table is missing."
                    );
                }

                System.out.println(
                        "Borrow requests table verified successfully."
                );
            }

            // Step 3: Create default Admin account
            UserService.createDefaultAdmin();

            System.out.println(
                    "Library Borrowing System is ready!"
            );

            // Step 4: Apply modern FlatLaf appearance
            FlatLightLaf.setup();

            // Step 5: Open Login Screen
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });

        } catch (Exception e) {

            System.err.println(
                    "Application startup failed!"
            );

            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    null,
                    "Unable to start the Library Borrowing System.\n"
                            + e.getMessage(),
                    "Startup Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}

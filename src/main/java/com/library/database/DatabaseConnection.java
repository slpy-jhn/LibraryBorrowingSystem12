
package com.library.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    // SQLite database location
    private static final String URL = "jdbc:sqlite:library.db";

    // Connect to SQLite database
    public static Connection getConnection() throws SQLException {

        Connection connection = DriverManager.getConnection(URL);

        // Enable foreign key constraints
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        return connection;
    }

    // Automatically create database tables
    public static void initializeDatabase() {

        String membersTable = """
                CREATE TABLE IF NOT EXISTS members (
                    member_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    student_employee_id TEXT NOT NULL UNIQUE,
                    course_department TEXT,
                    contact_number TEXT,
                    email TEXT,
                    registration_date TEXT NOT NULL,
                    membership_status TEXT NOT NULL DEFAULT 'ACTIVE'
                )
                """;

        String booksTable = """
                CREATE TABLE IF NOT EXISTS books (
                    book_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    isbn TEXT UNIQUE,
                    title TEXT NOT NULL,
                    author TEXT NOT NULL,
                    category TEXT,
                    publisher TEXT,
                    publication_year INTEGER,
                    total_copies INTEGER NOT NULL DEFAULT 1,
                    available_copies INTEGER NOT NULL DEFAULT 1,
                    shelf_location TEXT,
                    cover_image TEXT,
                    CHECK(total_copies >= 0),
                    CHECK(available_copies >= 0),
                    CHECK(available_copies <= total_copies)
                )
                """;

        String usersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL CHECK(role IN ('ADMIN', 'STUDENT')),
                    member_id INTEGER UNIQUE,
                    FOREIGN KEY (member_id)
                        REFERENCES members(member_id)
                )
                """;

        String transactionsTable = """
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    member_id INTEGER NOT NULL,
                    book_id INTEGER NOT NULL,
                    borrowing_date TEXT NOT NULL,
                    due_date TEXT NOT NULL,
                    return_date TEXT,
                    status TEXT NOT NULL DEFAULT 'BORROWED'
                        CHECK(status IN ('BORROWED', 'RETURNED', 'OVERDUE')),
                    FOREIGN KEY (member_id)
                        REFERENCES members(member_id),
                    FOREIGN KEY (book_id)
                        REFERENCES books(book_id)
                )
                """;

        // Student borrowing requests table
        String borrowRequestsTable = """
                CREATE TABLE IF NOT EXISTS borrow_requests (
                    request_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    member_id INTEGER NOT NULL,
                    book_id INTEGER NOT NULL,
                    request_date TEXT NOT NULL DEFAULT
                        (datetime('now', 'localtime')),
                    status TEXT NOT NULL DEFAULT 'PENDING'
                        CHECK(status IN (
                            'PENDING',
                            'APPROVED',
                            'REJECTED'
                        )),
                    decision_date TEXT,
                    rejection_reason TEXT,
                    FOREIGN KEY (member_id)
                        REFERENCES members(member_id),
                    FOREIGN KEY (book_id)
                        REFERENCES books(book_id)
                )
                """;

        // Prevent duplicate pending borrowing requests
        String pendingRequestIndex = """
                CREATE UNIQUE INDEX IF NOT EXISTS
                idx_one_pending_request_per_student_book
                ON borrow_requests(member_id, book_id)
                WHERE status = 'PENDING'
                """;

        // Student return requests table
        String returnRequestsTable = """
                CREATE TABLE IF NOT EXISTS return_requests (
                    return_request_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    transaction_id INTEGER NOT NULL,
                    request_date TEXT NOT NULL DEFAULT
                        (datetime('now', 'localtime')),
                    status TEXT NOT NULL DEFAULT 'PENDING'
                        CHECK(status IN (
                            'PENDING',
                            'APPROVED',
                            'REJECTED'
                        )),
                    decision_date TEXT,
                    rejection_reason TEXT,
                    FOREIGN KEY (transaction_id)
                        REFERENCES transactions(transaction_id)
                )
                """;

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            // Create the main tables
            statement.executeUpdate(membersTable);
            statement.executeUpdate(booksTable);
            statement.executeUpdate(usersTable);
            statement.executeUpdate(transactionsTable);

            // Create borrowing request table
            statement.executeUpdate(borrowRequestsTable);

            // Create return request table
            statement.executeUpdate(returnRequestsTable);

            // Repair older return_requests table structures
            migrateReturnRequestsTable(connection);

            // Create borrowing request index
            statement.executeUpdate(pendingRequestIndex);

            // Create return request index
            statement.executeUpdate("""
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    idx_one_pending_return_per_transaction
                    ON return_requests(transaction_id)
                    WHERE status = 'PENDING'
                    """);

            System.out.println("Database tables created successfully!");
            System.out.println("Borrow requests table verified successfully.");
            System.out.println("Return requests table verified successfully.");

        } catch (SQLException e) {

            System.out.println("Database initialization failed!");
            e.printStackTrace();
        }
    }

    // Check whether a column exists in a table
    private static boolean columnExists(
            Connection connection,
            String tableName,
            String columnName
    ) throws SQLException {

        String sql = "PRAGMA table_info(" + tableName + ")";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {

                String existingColumn = resultSet.getString("name");

                if (columnName.equalsIgnoreCase(existingColumn)) {
                    return true;
                }
            }
        }

        return false;
    }

    // Repair and update older return_requests tables
    private static void migrateReturnRequestsTable(
            Connection connection
    ) throws SQLException {

        try (Statement statement = connection.createStatement()) {

            // Add status column if an older table does not have it
            if (!columnExists(connection, "return_requests", "status")) {

                statement.executeUpdate("""
                        ALTER TABLE return_requests
                        ADD COLUMN status TEXT NOT NULL DEFAULT 'PENDING'
                        """);

                System.out.println("Added missing return request status column.");
            }

            // Add request_date column if missing
            if (!columnExists(connection, "return_requests", "request_date")) {

                statement.executeUpdate("""
                        ALTER TABLE return_requests
                        ADD COLUMN request_date TEXT
                        """);

                System.out.println("Added missing return request date column.");
            }

            // Add decision_date column if missing
            if (!columnExists(connection, "return_requests", "decision_date")) {

                statement.executeUpdate("""
                        ALTER TABLE return_requests
                        ADD COLUMN decision_date TEXT
                        """);

                System.out.println("Added missing return decision date column.");
            }

            // Add rejection_reason column if missing
            if (!columnExists(connection, "return_requests", "rejection_reason")) {

                statement.executeUpdate("""
                        ALTER TABLE return_requests
                        ADD COLUMN rejection_reason TEXT
                        """);

                System.out.println("Added missing rejection reason column.");
            }

            // Repair existing requests with an empty or missing status
            statement.executeUpdate("""
                    UPDATE return_requests
                    SET status = 'PENDING'
                    WHERE status IS NULL
                       OR TRIM(status) = ''
                    """);

            // Fill missing request dates
            statement.executeUpdate("""
                    UPDATE return_requests
                    SET request_date = datetime('now', 'localtime')
                    WHERE request_date IS NULL
                       OR TRIM(request_date) = ''
                    """);

            System.out.println("Return requests table migration completed.");
        }
    }
}

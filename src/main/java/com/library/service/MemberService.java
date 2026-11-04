
package com.library.service;

import com.library.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberService {

    public static class Member {
        private final int memberId;
        private final String fullName;
        private final String studentEmployeeId;
        private final String courseDepartment;
        private final String contactNumber;
        private final String email;
        private final String registrationDate;
        private final String membershipStatus;

        public Member(int memberId, String fullName,
                      String studentEmployeeId, String courseDepartment,
                      String contactNumber, String email,
                      String registrationDate, String membershipStatus) {
            this.memberId = memberId;
            this.fullName = fullName;
            this.studentEmployeeId = studentEmployeeId;
            this.courseDepartment = courseDepartment;
            this.contactNumber = contactNumber;
            this.email = email;
            this.registrationDate = registrationDate;
            this.membershipStatus = membershipStatus;
        }

        public int getMemberId() { return memberId; }
        public String getFullName() { return fullName; }
        public String getStudentEmployeeId() { return studentEmployeeId; }
        public String getCourseDepartment() { return courseDepartment; }
        public String getContactNumber() { return contactNumber; }
        public String getEmail() { return email; }
        public String getRegistrationDate() { return registrationDate; }
        public String getMembershipStatus() { return membershipStatus; }
    }

    // REGISTER MEMBER
    public static boolean addMember(Member member) throws SQLException {
        String sql = """
                INSERT INTO members (
                    full_name, student_employee_id, course_department,
                    contact_number, email, registration_date,
                    membership_status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, member.getFullName());
            statement.setString(2, member.getStudentEmployeeId());
            statement.setString(3, member.getCourseDepartment());
            statement.setString(4, member.getContactNumber());
            statement.setString(5, member.getEmail());
            statement.setString(6, member.getRegistrationDate());
            statement.setString(7, member.getMembershipStatus());

            return statement.executeUpdate() > 0;
        }
    }

    // GET ALL MEMBERS
    public static List<Member> getAllMembers() throws SQLException {
        List<Member> members = new ArrayList<>();

        String sql = "SELECT * FROM members ORDER BY full_name";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                members.add(new Member(
                        result.getInt("member_id"),
                        result.getString("full_name"),
                        result.getString("student_employee_id"),
                        result.getString("course_department"),
                        result.getString("contact_number"),
                        result.getString("email"),
                        result.getString("registration_date"),
                        result.getString("membership_status")
                ));
            }
        }

        return members;
    }

    // SEARCH MEMBERS
    public static List<Member> searchMembers(String keyword)
            throws SQLException {

        List<Member> members = new ArrayList<>();

        String sql = """
                SELECT * FROM members
                WHERE full_name LIKE ?
                   OR student_employee_id LIKE ?
                   OR course_department LIKE ?
                ORDER BY full_name
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String search = "%" + keyword + "%";

            statement.setString(1, search);
            statement.setString(2, search);
            statement.setString(3, search);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    members.add(new Member(
                            result.getInt("member_id"),
                            result.getString("full_name"),
                            result.getString("student_employee_id"),
                            result.getString("course_department"),
                            result.getString("contact_number"),
                            result.getString("email"),
                            result.getString("registration_date"),
                            result.getString("membership_status")
                    ));
                }
            }
        }

        return members;
    }

    // UPDATE MEMBER
    public static boolean updateMember(Member member)
            throws SQLException {

        String sql = """
                UPDATE members
                SET full_name = ?,
                    student_employee_id = ?,
                    course_department = ?,
                    contact_number = ?,
                    email = ?,
                    membership_status = ?
                WHERE member_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, member.getFullName());
            statement.setString(2, member.getStudentEmployeeId());
            statement.setString(3, member.getCourseDepartment());
            statement.setString(4, member.getContactNumber());
            statement.setString(5, member.getEmail());
            statement.setString(6, member.getMembershipStatus());
            statement.setInt(7, member.getMemberId());

            return statement.executeUpdate() > 0;
        }
    }
}

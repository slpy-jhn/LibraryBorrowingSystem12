
package com.library.service;

public final class UserSession {

    private final String username;
    private final String role;
    private final Integer memberId;

    public UserSession(
            String username,
            String role,
            Integer memberId
    ) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username is required."
            );
        }

        if (!"ADMIN".equals(role) && !"STUDENT".equals(role)) {
            throw new IllegalArgumentException(
                    "Invalid user role."
            );
        }

        if ("STUDENT".equals(role)
                && (memberId == null || memberId <= 0)) {
            throw new IllegalArgumentException(
                    "A valid member ID is required for student accounts."
            );
        }

        if ("ADMIN".equals(role) && memberId != null) {
            throw new IllegalArgumentException(
                    "Administrator accounts cannot have a student member ID."
            );
        }

        this.username = username;
        this.role = role;
        this.memberId = memberId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isStudent() {
        return "STUDENT".equals(role);
    }

    public void requireAdmin() {

        if (!isAdmin()) {
            throw new SecurityException(
                    "Administrator access is required."
            );
        }
    }

    public int requireStudent() {

        if (!isStudent() || memberId == null || memberId <= 0) {
            throw new SecurityException(
                    "Student access is required."
            );
        }

        return memberId;
    }
}

package com.mohid.masu.student.session;

public class StudentSession {

    private static String studentId;
    private static String username;
    private static String fullName;
    private static String gender;
    private static String status;

    public static String getStudentId() {
        return studentId;
    }

    public static void setStudentId(String studentId) {
        StudentSession.studentId = studentId;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        StudentSession.username = username;
    }

    public static String getFullName() {
        return fullName;
    }

    public static void setFullName(String fullName) {
        StudentSession.fullName = fullName;
    }

    public static String getGender() {
        return gender;
    }

    public static void setGender(String gender) {
        StudentSession.gender = gender;
    }

    public static String getStatus() {
        return status;
    }

    public static void setStatus(String status) {
        StudentSession.status = status;
    }

    public static void clearSession() {
        studentId = null;
        username = null;
        fullName = null;
        gender = null;
        status = null;
    }
}
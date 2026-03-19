package com.mohid.masu.student.session;

public class SessionManager {

    private static StudentSession currentStudent;

    private SessionManager() {
    }

    public static void setCurrentStudent(StudentSession student) {
        currentStudent = student;
    }

    public static StudentSession getCurrentStudent() {
        return currentStudent;
    }

    public static boolean isLoggedIn() {
        return currentStudent != null;
    }

    public static void clearSession() {
        currentStudent = null;
    }
}
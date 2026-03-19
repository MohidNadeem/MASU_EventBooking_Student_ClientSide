package com.mohid.masu.student.dto;

public class StudentLoginResponse {

    private String message;
    private String id;
    private String username;
    private String fullName;
    private String gender;
    private String status;

    public StudentLoginResponse() {
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getGender() {
        return gender;
    }

    public String getStatus() {
        return status;
    }
}
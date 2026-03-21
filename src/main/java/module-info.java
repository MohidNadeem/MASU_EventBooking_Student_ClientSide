module com.mohid.masu.student {

    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires java.desktop;

    // For HTTP Client (Java 11+)
    requires java.net.http;

    // For JSON (Jackson)
    requires com.fasterxml.jackson.databind;

    opens com.mohid.masu.student to javafx.fxml;
    opens com.mohid.masu.student.controller to javafx.fxml;

    exports com.mohid.masu.student;
    exports com.mohid.masu.student.controller;
    exports com.mohid.masu.student.service;
    exports com.mohid.masu.student.dto;
    exports com.mohid.masu.student.session;
    exports com.mohid.masu.student.model;
    exports com.mohid.masu.student.util;
}
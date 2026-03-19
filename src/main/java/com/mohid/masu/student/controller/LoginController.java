package com.mohid.masu.student.controller;

import com.mohid.masu.student.App;
import com.mohid.masu.student.dto.LoginRequest;
import com.mohid.masu.student.service.ApiClient;
import com.mohid.masu.student.session.StudentSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.json.JSONObject;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin() {
        try {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter username and password.");
                return;
            }

            LoginRequest request = new LoginRequest(username, password);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", request.getUsername());
            jsonBody.put("password", request.getPassword());

            String response = ApiClient.post("/students/login", jsonBody.toString());

            JSONObject obj = new JSONObject(response);

            if (response.contains("Invalid username or password")) {
                messageLabel.setText("Invalid username or password.");
                return;
            }

            if (response.contains("Username and password are required")) {
                messageLabel.setText("Please enter username and password.");
                return;
            }

            StudentSession.setStudentId(obj.optString("id", ""));
            StudentSession.setUsername(obj.optString("username", ""));
            StudentSession.setFullName(obj.optString("fullName", ""));
            StudentSession.setGender(obj.optString("gender", ""));
            StudentSession.setStatus(obj.optString("status", ""));

            messageLabel.setText("");
            App.setRoot("/com/mohid/masu/student/view/dashboard");

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Login failed. Check API/server connection.");
        }
    }
}
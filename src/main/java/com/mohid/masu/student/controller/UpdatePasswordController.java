package com.mohid.masu.student.controller;

import com.mohid.masu.student.dto.UpdatePasswordRequest;
import com.mohid.masu.student.service.ApiClient;
import com.mohid.masu.student.session.StudentSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import org.json.JSONObject;

public class UpdatePasswordController {

    @FXML
    private PasswordField oldPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleUpdatePassword() {
        try {
            String oldPassword = oldPasswordField.getText().trim();
            String newPassword = newPasswordField.getText().trim();
            String confirmPassword = confirmPasswordField.getText().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                messageLabel.setText("All password fields are required.");
                return;
            }

            UpdatePasswordRequest request =
                    new UpdatePasswordRequest(oldPassword, newPassword, confirmPassword);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("oldPassword", request.getOldPassword());
            jsonBody.put("newPassword", request.getNewPassword());
            jsonBody.put("confirmPassword", request.getConfirmPassword());

            String endpoint = "/students/" + StudentSession.getStudentId() + "/password";
            String response = ApiClient.put(endpoint, jsonBody.toString());

            JSONObject obj = new JSONObject(response);
            String message = obj.optString("message", "Password update failed.");

            messageLabel.setText(message);

            if (response.contains("successfully")) {
                oldPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
            }

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Failed to update password. Check API/server connection.");
        }
    }
}
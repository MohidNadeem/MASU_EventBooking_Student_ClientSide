package com.mohid.masu.student.controller;

import com.mohid.masu.student.App;
import com.mohid.masu.student.session.StudentSession;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DashboardController {

    @FXML
    private Label studentNameLabel;

    @FXML
    private Label studentMetaLabel;

    @FXML
    private Label initialsLabel;

    @FXML
    private Label pageTitleLabel;

    @FXML
    private Label pageSubtitleLabel;

    @FXML
    private VBox contentArea;

    @FXML
    public void initialize() {
        if (StudentSession.getStudentId() == null || StudentSession.getStudentId().isBlank()) {
            try {
                App.setRoot("/com/mohid/masu/student/view/login");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        String fullName = StudentSession.getFullName();
        String gender = StudentSession.getGender();
        String status = StudentSession.getStatus();

        studentNameLabel.setText(fullName);
        studentMetaLabel.setText(status + " • " + gender);
        initialsLabel.setText(getInitials(fullName));

        showUpcomingEvents();
    }

    @FXML
    private void showUpcomingEvents() {
        pageTitleLabel.setText("Upcoming Events");
        pageSubtitleLabel.setText("Discover, book, and explore student events from here.");

        contentArea.getChildren().clear();
        Label placeholder = new Label("Upcoming Events content will appear here.");
        placeholder.getStyleClass().add("page-subtitle");
        contentArea.getChildren().add(placeholder);
    }

    @FXML
    private void showMyBookings() {
        pageTitleLabel.setText("My Bookings");
        pageSubtitleLabel.setText("View all events you have booked and manage them from here.");

        contentArea.getChildren().clear();
        Label placeholder = new Label("My Bookings content will appear here.");
        placeholder.getStyleClass().add("page-subtitle");
        contentArea.getChildren().add(placeholder);
    }

    @FXML
    private void showPublishedEvents() {
        pageTitleLabel.setText("Events I Published");
        pageSubtitleLabel.setText("Manage the events you created and published.");

        contentArea.getChildren().clear();
        Label placeholder = new Label("Published Events content will appear here.");
        placeholder.getStyleClass().add("page-subtitle");
        contentArea.getChildren().add(placeholder);
    }

    @FXML
    private void showCreateEvent() {
        pageTitleLabel.setText("Create Event");
        pageSubtitleLabel.setText("Publish a new event with details, location, and map support.");

        contentArea.getChildren().clear();
        Label placeholder = new Label("Create Event content will appear here.");
        placeholder.getStyleClass().add("page-subtitle");
        contentArea.getChildren().add(placeholder);
    }

    @FXML
    private void showUpdatePassword() {
        pageTitleLabel.setText("Update Password");
        pageSubtitleLabel.setText("Change your current password securely.");

        contentArea.getChildren().clear();
        Label placeholder = new Label("Update Password content will appear here.");
        placeholder.getStyleClass().add("page-subtitle");
        contentArea.getChildren().add(placeholder);
    }

    @FXML
    private void handleLogout() {
        try {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Logout");

            VBox root = new VBox(15);
            root.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15;");
            root.setAlignment(Pos.CENTER);

            Label title = new Label("Are you sure you want to logout?");
            title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Label desc = new Label("Your current student session will be cleared.");

            Button okBtn = new Button("OK");
            Button cancelBtn = new Button("Cancel");

            okBtn.setOnAction(e -> {
                StudentSession.clearSession();
                dialog.close();
                try {
                    App.setRoot("/com/mohid/masu/student/view/login");
                } catch (IOException ex) {
                    System.getLogger(DashboardController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            });

            cancelBtn.setOnAction(e -> dialog.close());

            HBox buttons = new HBox(10, okBtn, cancelBtn);
            buttons.setAlignment(Pos.CENTER);

            root.getChildren().addAll(title, desc, buttons);

            Scene scene = new Scene(root);
            dialog.setScene(scene);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String getInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "S";
        }

        String[] parts = fullName.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }

        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }
}

package com.mohid.masu.student.controller;

import com.mohid.masu.student.service.ApiClient;
import java.time.LocalDate;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONObject;

public class UpdateEventController {

    @FXML private TextField titleField;
    @FXML private TextField typeField;
    @FXML private DatePicker datePicker;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private TextField venueField;
    @FXML private TextField locationField;
    @FXML private TextField countryField;
    @FXML private TextField postalCodeField;
    @FXML private TextField genderField;
    @FXML private TextField currencyField;
    @FXML private TextField durationField;
    @FXML private TextField costField;
    @FXML private TextField maxParticipantsField;
    @FXML private TextField alumniSlotsField;
    @FXML private TextArea descriptionArea;
    @FXML private Label messageLabel;

    private String eventId;

    public void setEventData(JSONObject eventObj) {
        eventId = eventObj.optString("id", "");

        titleField.setText(eventObj.optString("title", ""));
        typeField.setText(eventObj.optString("type", ""));
        venueField.setText(eventObj.optString("venueName", ""));
        locationField.setText(eventObj.optString("location", ""));
        countryField.setText(eventObj.optString("country", ""));
        postalCodeField.setText(eventObj.optString("postalCode", ""));
        genderField.setText(eventObj.optString("gender", ""));
        currencyField.setText(eventObj.optString("currency", ""));
        durationField.setText(eventObj.optString("duration", ""));

        String rawDate = eventObj.optString("date", "");
        if (!rawDate.isBlank()) {
            try {
                datePicker.setValue(LocalDate.parse(rawDate));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        startTimeField.setText(eventObj.optString("startTime", ""));
        endTimeField.setText(eventObj.optString("endTime", ""));
        
        startTimeField.textProperty().addListener((obs, oldVal, newVal) -> updateDurationFromTime());
        endTimeField.textProperty().addListener((obs, oldVal, newVal) -> updateDurationFromTime());

        updateDurationFromTime();
                
        costField.setText(String.valueOf(eventObj.optDouble("cost", 0.0)));
        maxParticipantsField.setText(String.valueOf(eventObj.optInt("maxParticipants", 0)));
        alumniSlotsField.setText(String.valueOf(eventObj.optInt("alumniReservedSlots", 0)));
        descriptionArea.setText(eventObj.optString("description", ""));
    }
    
    private void updateDurationFromTime() {
        try {
            String start = startTimeField.getText().trim();
            String end = endTimeField.getText().trim();

            if (start.isEmpty() || end.isEmpty()) {
                durationField.setText("");
                return;
            }

            java.time.LocalTime startTime = java.time.LocalTime.parse(start);
            java.time.LocalTime endTime = java.time.LocalTime.parse(end);

            long minutes = java.time.Duration.between(startTime, endTime).toMinutes();

            if (minutes <= 0) {
                durationField.setText("Invalid duration");
                return;
            }

            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;

            if (hours > 0 && remainingMinutes > 0) {
                durationField.setText(hours + " hr " + remainingMinutes + " min");
            } else if (hours > 0) {
                durationField.setText(hours + " hr");
            } else {
                durationField.setText(remainingMinutes + " min");
            }

        } catch (Exception e) {
            durationField.setText("");
        }
    }

    @FXML
    private void handleUpdateEvent() {
        try {
            if ("Invalid duration".equals(durationField.getText().trim())) {
                messageLabel.setText("Please enter a valid start and end time.");
                return;
            }
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("date", datePicker.getValue() != null ? datePicker.getValue().toString() : "");
            requestBody.put("startTime", startTimeField.getText().trim());
            requestBody.put("endTime", endTimeField.getText().trim());
            requestBody.put("description", descriptionArea.getText().trim());
            requestBody.put("duration", durationField.getText().trim());
            requestBody.put("cost", Double.parseDouble(costField.getText().trim()));
            requestBody.put("maxParticipants", Integer.parseInt(maxParticipantsField.getText().trim()));
            requestBody.put("alumniReservedSlots", Integer.parseInt(alumniSlotsField.getText().trim()));

            String response = ApiClient.put("/events/" + eventId, requestBody.toString());
            messageLabel.setText(new JSONObject(response).optString("message", "Event updated."));

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Failed to update event.");
        }
    }

    @FXML
    private void handleCancelEvent() {
        try {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Cancel Event");

            VBox root = new VBox(15);
            root.setAlignment(Pos.CENTER);
            root.setPadding(new Insets(22));
            root.getStyleClass().add("custom-dialog-root");

            Label title = new Label("Cancel this event?");
            title.getStyleClass().add("dialog-title");

            Label desc = new Label("This will mark the event as cancelled.");
            desc.getStyleClass().add("dialog-text");
            desc.setWrapText(true);

            Button yesBtn = new Button("Yes, Cancel Event");
            yesBtn.getStyleClass().add("primary-btn");

            Button noBtn = new Button("Keep Event");
            noBtn.getStyleClass().add("secondary-btn");

            yesBtn.setOnAction(e -> {
                try {
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("status", "CANCELLED");

                    String response = ApiClient.put("/events/" + eventId + "/status", requestBody.toString());
                    messageLabel.setText(new JSONObject(response).optString("message", "Event cancelled."));
                    dialog.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    messageLabel.setText("Failed to cancel event.");
                    dialog.close();
                }
            });

            noBtn.setOnAction(e -> dialog.close());

            HBox buttons = new HBox(10, yesBtn, noBtn);
            buttons.setAlignment(Pos.CENTER);

            root.getChildren().addAll(title, desc, buttons);

            Scene scene = new Scene(root, 390, 190);
            scene.getStylesheets().add(getClass().getResource("/com/mohid/masu/student/assets/styles.css").toExternalForm());

            dialog.setScene(scene);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Failed to cancel event.");
        }
    }
}
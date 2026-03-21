package com.mohid.masu.student.controller;

import com.mohid.masu.student.service.ApiClient;
import com.mohid.masu.student.session.StudentSession;
import java.time.Duration;
import java.time.LocalTime;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.json.JSONObject;


public class CreateEventController {

    @FXML
    private TextField titleField;

    @FXML
    private ComboBox<String> typeCombo;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField startTimeField;

    @FXML
    private TextField endTimeField;

    @FXML
    private TextField durationField;

    @FXML
    private TextField venueField;

    @FXML
    private TextField locationField;

    @FXML
    private TextField countryField;

    @FXML
    private TextField postalCodeField;

    @FXML
    private ComboBox<String> genderCombo;

    @FXML
    private ComboBox<String> currencyCombo;

    @FXML
    private TextField costField;

    @FXML
    private TextField maxParticipantsField;

    @FXML
    private TextField alumniSlotsField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Label messageLabel;

    @FXML
    private TextField latitudeField;

    @FXML
    private TextField longitudeField;
    
    @FXML
    private TextField locationSearchField;

    @FXML
    private ListView<String> locationSuggestionsList;

    @FXML
    private ImageView mapPreviewImage;

    private final ObservableList<String> suggestionItems = FXCollections.observableArrayList();
    private JSONArray latestSuggestionResults = new JSONArray();

    @FXML
    public void initialize() {
        typeCombo.getItems().addAll(
                "Seminar", "Workshop", "Sports", "Competition",
                "Networking", "Entertainment", "Academic", "General"
        );

        genderCombo.getItems().addAll("BOYS", "GIRLS", "BOTH");
        currencyCombo.getItems().addAll("PKR", "GBP", "USD", "EUR");

        startTimeField.textProperty().addListener((obs, oldVal, newVal) -> updateDurationFromTime());
        endTimeField.textProperty().addListener((obs, oldVal, newVal) -> updateDurationFromTime());

        locationSuggestionsList.setItems(suggestionItems);

        locationSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.trim().length() >= 3) {
                loadLocationSuggestions(newVal.trim());
            } else {
                suggestionItems.clear();
                latestSuggestionResults = new JSONArray();
            }
        });

        locationSuggestionsList.setOnMouseClicked(e -> handleSuggestionSelection());
    }

    @FXML
    private void handleCreateEvent() {
        try {
            String title = titleField.getText().trim();
            String type = typeCombo.getValue();
            String startTime = startTimeField.getText().trim();
            String endTime = endTimeField.getText().trim();
            String duration = durationField.getText().trim();
            String venue = venueField.getText().trim();
            String location = locationField.getText().trim();
            String country = countryField.getText().trim();
            String postalCode = postalCodeField.getText().trim();
            String gender = genderCombo.getValue();
            String currency = currencyCombo.getValue();
            String costText = costField.getText().trim();
            String maxParticipantsText = maxParticipantsField.getText().trim();
            String alumniSlotsText = alumniSlotsField.getText().trim();
            String description = descriptionArea.getText().trim();

            if (title.isEmpty() || type == null || datePicker.getValue() == null
                    || startTime.isEmpty() || endTime.isEmpty() || duration.isEmpty()
                    || venue.isEmpty() || location.isEmpty() || country.isEmpty()
                    || postalCode.isEmpty() || gender == null || currency == null
                    || costText.isEmpty() || maxParticipantsText.isEmpty()
                    || alumniSlotsText.isEmpty() || description.isEmpty()) {
                messageLabel.setText("Please fill all required fields.");
                return;
            }

            if ("Invalid duration".equals(duration)) {
                messageLabel.setText("Please enter a valid start and end time.");
                return;
            }

            double cost = Double.parseDouble(costText);
            int maxParticipants = Integer.parseInt(maxParticipantsText);
            int alumniSlots = Integer.parseInt(alumniSlotsText);
            
            String latitudeText = latitudeField.getText().trim();
            String longitudeText = longitudeField.getText().trim();

            if (latitudeText.isEmpty() || longitudeText.isEmpty()) {
                messageLabel.setText("Please select a location from the map.");
                return;
            }

            JSONObject requestBody = new JSONObject();
            requestBody.put("publisherId", StudentSession.getStudentId());
            requestBody.put("title", title);
            requestBody.put("type", type);
            requestBody.put("date", datePicker.getValue().toString());
            requestBody.put("startTime", startTime);
            requestBody.put("endTime", endTime);
            requestBody.put("venueName", venue);
            requestBody.put("location", location);
            requestBody.put("country", country);
            requestBody.put("latitude", Double.parseDouble(latitudeText));
            requestBody.put("longitude", Double.parseDouble(longitudeText));
            requestBody.put("postalCode", postalCode);
            requestBody.put("description", description);
            requestBody.put("gender", gender);
            requestBody.put("currency", currency);
            requestBody.put("duration", duration);
            requestBody.put("cost", cost);
            requestBody.put("maxParticipants", maxParticipants);
            requestBody.put("alumniReservedSlots", alumniSlots);
            requestBody.put("status", "ACTIVE");

            String response = ApiClient.post("/events", requestBody.toString());
            JSONObject obj = new JSONObject(response);

            String message = obj.optString("message", "Event created successfully.");
            messageLabel.setText(message);

            if (message.toLowerCase().contains("successfully")) {
                clearForm();
            }

        } catch (NumberFormatException e) {
            messageLabel.setText("Please enter valid numeric values for cost and slots.");
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Failed to create event. Check API/server connection.");
        }
    }

    private void updateDurationFromTime() {
        try {
            String start = startTimeField.getText().trim();
            String end = endTimeField.getText().trim();

            if (start.isEmpty() || end.isEmpty()) {
                durationField.setText("");
                return;
            }

            LocalTime startTime = LocalTime.parse(start);
            LocalTime endTime = LocalTime.parse(end);

            long minutes = Duration.between(startTime, endTime).toMinutes();

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
    private void clearForm() {
        titleField.clear();
        typeCombo.setValue(null);
        datePicker.setValue(null);
        startTimeField.clear();
        endTimeField.clear();
        durationField.clear();
        venueField.clear();
        locationField.clear();
        countryField.clear();
        postalCodeField.clear();
        genderCombo.setValue(null);
        currencyCombo.setValue(null);
        costField.clear();
        maxParticipantsField.clear();
        alumniSlotsField.clear();
        descriptionArea.clear();
        latitudeField.clear();
        longitudeField.clear();
        locationSearchField.clear();
        locationSuggestionsList.getItems().clear();
        mapPreviewImage.setImage(null);
    }
    
    private void loadLocationSuggestions(String query) {
        try {
            String encodedQuery = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
            String response = ApiClient.get("/external/location-search?q=" + encodedQuery);
            System.out.println("Suggestions Response: " + response);
            latestSuggestionResults = new JSONArray(response);
            suggestionItems.clear();

            for (int i = 0; i < latestSuggestionResults.length(); i++) {
                JSONObject item = latestSuggestionResults.getJSONObject(i);
                suggestionItems.add(item.optString("display_name", "Unknown location"));
            }

        } catch (Exception e) {
            e.printStackTrace();    
            suggestionItems.clear();
        }
    }
    
    private void handleSuggestionSelection() {
        try {
            int index = locationSuggestionsList.getSelectionModel().getSelectedIndex();
            if (index < 0 || index >= latestSuggestionResults.length()) {
                return;
            }

            JSONObject selected = latestSuggestionResults.getJSONObject(index);

            String lat = selected.optString("lat", "");
            String lon = selected.optString("lon", "");
            String displayName = selected.optString("display_name", "");

            latitudeField.setText(lat);
            longitudeField.setText(lon);

            if (!displayName.isBlank()) {
                locationField.setText(displayName);
            }

            JSONObject address = selected.optJSONObject("address");
            if (address != null) {
                String countryCode = address.optString("country_code", "").toUpperCase();
                String postcode = address.optString("postcode", "");

                if (!countryCode.isBlank()) {
                    countryField.setText(countryCode);
                }

                if (!postcode.isBlank()) {
                    postalCodeField.setText(postcode);
                }
            }

            updateStaticMapPreview(lat, lon);
            messageLabel.setText("Location selected successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Failed to use selected location.");
        }
    }
    
    private void updateStaticMapPreview(String lat, String lon) {
        try {
            String response = ApiClient.get("/external/static-map?lat=" + lat + "&lng=" + lon);
            JSONObject obj = new JSONObject(response);

            String imageUrl = obj.optString("imageUrl", "");
            if (!imageUrl.isBlank()) {
                mapPreviewImage.setImage(new Image(imageUrl, true));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
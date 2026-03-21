package com.mohid.masu.student.controller;

import com.mohid.masu.student.service.ApiClient;
import java.awt.Desktop;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.json.JSONObject;
import org.json.JSONArray;

public class EventDetailsController {

    @FXML private Label typeBadgeLabel;
    @FXML private Label genderBadgeLabel;
    @FXML private Label headerRatingLabel;

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;

    @FXML private Label dateTimeLabel;
    @FXML private Label durationBadgeLabel;
    @FXML private Label costLabel;
    @FXML private Label capacityLabel;
    @FXML private Label alumniSlotsLabel;
    @FXML private Label statusBadgeLabel;
    @FXML private Label capacityRemainingBadgeLabel;
    @FXML private Label alumniRemainingBadgeLabel;

    @FXML private Label weatherMainLabel;
    @FXML private Label weatherTempLabel;
    @FXML private Label weatherFeelsLabel;
    @FXML private Label weatherHumidityLabel;
    @FXML private Label weatherWindLabel;

    @FXML private Label venueLabel;
    @FXML private Label countryLabel;
    @FXML private Label postalCodeLabel;

    @FXML private Label publisherUsernameLabel;
    @FXML private Label publisherNameLabel;
    @FXML private Label publisherStatusBadgeLabel;

    @FXML private Label descriptionLabel;

    @FXML private ImageView mapPreviewImage;
    @FXML private Button openMapBtn;
    
    @FXML private VBox nearbyLocationsBox;

    private double eventLatitude;
    private double eventLongitude;

    public void setEventId(String eventId) {
        loadEventDetails(eventId);
    }

    private void loadEventDetails(String eventId) {
        try {
            String response = ApiClient.get("/events/" + eventId + "/full-details");
            JSONObject root = new JSONObject(response);

            JSONObject event = root.getJSONObject("event");

            String title = event.optString("title", "-");
            String type = event.optString("type", "-");
            String gender = event.optString("gender", "BOTH");
            String venue = event.optString("venueName", "-");
            String location = event.optString("location", "-");
            String country = event.optString("country", "-");
            String postalCode = event.optString("postalCode", "-");
            String date = formatEventDate(event.optString("date", "-"));
            String startTime = event.optString("startTime", "-");
            String endTime = event.optString("endTime", "-");
            String duration = event.optString("duration", "-");
            String currency = event.optString("currency", "");
            double cost = event.optDouble("cost", 0.0);
            int maxParticipants = event.optInt("maxParticipants", 0);
            int alumniSlots = event.optInt("alumniReservedSlots", 0);
            int remainingSeats = event.optInt("remainingSeats", 0);
            int remainingAlumniSlots = event.optInt("remainingAlumniSlots", 0);
            String status = event.optString("status", "-");
            String publisherId = event.optString("publisherId", "-");
            String description = event.optString("description", "-");

            eventLatitude = event.optDouble("latitude", 0.0);
            eventLongitude = event.optDouble("longitude", 0.0);

            double avgRating = root.optDouble("averageRating", 0.0);

            typeBadgeLabel.setText(type);
            genderBadgeLabel.setText(getGenderIcon(gender) + " " + gender);

            if (avgRating <= 0.0) {
                headerRatingLabel.setText("No Ratings Marked");
            } else {
                headerRatingLabel.setText("★ " + String.format("%.1f", avgRating) + " / 5");
            }

            titleLabel.setText(title);
            subtitleLabel.setText(venue + ", " + location);

            dateTimeLabel.setText(date + "  •  " + startTime + " - " + endTime);
            durationBadgeLabel.setText(duration);
            costLabel.setText(makeInfoLine("Cost", cost <= 0 ? "Free" : currency + " " + cost));

            capacityLabel.setText(makeInfoLine("Capacity", String.valueOf(maxParticipants)));
            capacityRemainingBadgeLabel.setText("Remaining: " + remainingSeats);

            alumniSlotsLabel.setText(makeInfoLine("Reserved Alumni Slots", String.valueOf(alumniSlots)));
            alumniRemainingBadgeLabel.setText("Remaining: " + remainingAlumniSlots);

            statusBadgeLabel.setText(status);

            venueLabel.setText("Venue: " + venue + ", " + location);
            postalCodeLabel.setText(makeInfoLine("Postal Code", postalCode));
            countryLabel.setText(makeInfoLine("Country Code", country));

            descriptionLabel.setText(description);

            loadWeather(root.optString("weather", ""));
            loadPublisherInfo(publisherId);
            updateStaticMapPreview(eventLatitude, eventLongitude);
            loadNearbyLocations(root.optString("nearbyLocations", ""));

            if (openMapBtn != null) {
                openMapBtn.setDisable(eventLatitude == 0.0 && eventLongitude == 0.0);
            }
            
            javafx.application.Platform.runLater(() -> titleLabel.requestFocus());

        } catch (Exception e) {
            e.printStackTrace();
            titleLabel.setText("Failed to load event details");
            if (openMapBtn != null) {
                openMapBtn.setDisable(true);
            }
        }
    }

    private void updateStaticMapPreview(double lat, double lng) {
        try {
            if (lat == 0.0 && lng == 0.0) {
                return;
            }

            String response = ApiClient.get("/external/static-map?lat=" + lat + "&lng=" + lng);
            JSONObject obj = new JSONObject(response);

            String imageUrl = obj.optString("imageUrl", "");
            if (!imageUrl.isBlank()) {
                mapPreviewImage.setImage(new Image(imageUrl, true));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenInMap() {
        try {
            if (eventLatitude == 0.0 && eventLongitude == 0.0) {
                return;
            }

            String url = "https://www.google.com/maps?q=" + eventLatitude + "," + eventLongitude;
            Desktop.getDesktop().browse(new URI(url));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadWeather(String weatherRaw) {
        try {
            if (weatherRaw != null && !weatherRaw.isBlank() && weatherRaw.trim().startsWith("{")) {
                JSONObject weatherObj = new JSONObject(weatherRaw);

                String description = weatherObj.optString("description", "-");
                String temp = weatherObj.optString("temperature", "-");
                String feelsLike = weatherObj.optString("feelsLike", "-");
                String humidity = weatherObj.optString("humidity", "-");
                String wind = weatherObj.optString("windSpeed", "-");

                weatherMainLabel.setText(getWeatherEmoji(description) + " " + capitalizeWords(description));
                weatherTempLabel.setText(makeInfoLine("Temperature", temp + "°C"));
                weatherFeelsLabel.setText(makeInfoLine("Feels Like", feelsLike + "°C"));
                weatherHumidityLabel.setText(makeInfoLine("Humidity", humidity + "%"));
                weatherWindLabel.setText(makeInfoLine("Wind Speed", wind));
            } else {
                weatherMainLabel.setText("🌤 Weather unavailable");
                weatherTempLabel.setText(makeInfoLine("Temperature", "-"));
                weatherFeelsLabel.setText(makeInfoLine("Feels Like", "-"));
                weatherHumidityLabel.setText(makeInfoLine("Humidity", "-"));
                weatherWindLabel.setText(makeInfoLine("Wind Speed", "-"));
            }
        } catch (Exception e) {
            weatherMainLabel.setText("🌤 Weather unavailable");
        }
    }

    private void loadPublisherInfo(String publisherId) {
        try {
            String response = ApiClient.get("/students/" + publisherId);
            JSONObject publisher = new JSONObject(response);

            publisherUsernameLabel.setText(makeInfoLine("Username", publisher.optString("username", "-")));
            publisherNameLabel.setText(makeInfoLine("Full Name", publisher.optString("fullName", "-")));
            publisherStatusBadgeLabel.setText(publisher.optString("status", "-"));

        } catch (Exception e) {
            publisherUsernameLabel.setText(makeInfoLine("Username", "-"));
            publisherNameLabel.setText(makeInfoLine("Full Name", "-"));
            publisherStatusBadgeLabel.setText("-");
        }
    }
    
    private void loadNearbyLocations(String nearbyRaw) {
        try {
            nearbyLocationsBox.getChildren().clear();

            if (nearbyRaw == null || nearbyRaw.isBlank()) {
                Label emptyLabel = new Label("No nearby locations available.");
                emptyLabel.getStyleClass().add("page-subtitle");
                nearbyLocationsBox.getChildren().add(emptyLabel);
                return;
            }

            JSONObject nearbyRoot = new JSONObject(nearbyRaw);
            JSONArray geonamesArray = nearbyRoot.optJSONArray("geonames");

            if (geonamesArray == null || geonamesArray.isEmpty()) {
                Label emptyLabel = new Label("No nearby locations available.");
                emptyLabel.getStyleClass().add("page-subtitle");
                nearbyLocationsBox.getChildren().add(emptyLabel);
                return;
            }

            int limit = Math.min(2, geonamesArray.length());

            for (int i = 0; i < limit; i++) {
                JSONObject place = geonamesArray.getJSONObject(i);

                String title = place.optString("title", "Nearby Location");
                String feature = place.optString("feature", "Place");
                String distance = place.optString("distance", "");
                String summary = place.optString("summary", "");
                String wikiUrl = place.optString("wikipediaUrl", "");

                VBox itemCard = new VBox(6);
                itemCard.getStyleClass().add("nearby-location-item");

                Label nameLabel = new Label(title);
                nameLabel.getStyleClass().add("nearby-location-title");
                nameLabel.setWrapText(true);

                String metaText = capitalizeWords(feature);
                if (!distance.isBlank()) {
                    metaText += " • " + formatDistance(distance);
                }

                Label metaLabel = new Label(metaText);
                metaLabel.getStyleClass().add("nearby-location-meta");
                metaLabel.setWrapText(true);

                String shortSummary = summary;
                if (shortSummary != null && shortSummary.length() > 480) {
                    shortSummary = shortSummary.substring(0, 180).trim() + "...";
                }

                Label summaryLabel = new Label(
                        (shortSummary == null || shortSummary.isBlank())
                                ? "No summary available."
                                : shortSummary
                );
                summaryLabel.getStyleClass().add("page-subtitle");
                summaryLabel.setWrapText(true);

                itemCard.getChildren().addAll(nameLabel, metaLabel, summaryLabel);

                if (wikiUrl != null && !wikiUrl.isBlank()) {

                    String fullUrl = wikiUrl.startsWith("http")
                            ? wikiUrl
                            : "https://" + wikiUrl;

                    Label wikiLabel = new Label("🔗 View on Wikipedia");
                    wikiLabel.getStyleClass().add("nearby-location-link");
                    wikiLabel.setCursor(javafx.scene.Cursor.HAND);

                    wikiLabel.setOnMouseClicked(e -> {
                        try {
                            java.awt.Desktop.getDesktop().browse(new java.net.URI(fullUrl));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });

                    itemCard.getChildren().add(wikiLabel);
                }

                nearbyLocationsBox.getChildren().add(itemCard);
            }

        } catch (Exception e) {
            e.printStackTrace();
            nearbyLocationsBox.getChildren().clear();

            Label errorLabel = new Label("Failed to load nearby locations.");
            errorLabel.getStyleClass().add("page-subtitle");
            nearbyLocationsBox.getChildren().add(errorLabel);
        }
    }
    
    private String makeInfoLine(String label, String value) {
        return label + ": " + value;
    }

    private String formatEventDate(String rawDate) {
        try {
            LocalDate parsedDate = LocalDate.parse(rawDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            return parsedDate.format(formatter);
        } catch (Exception e) {
            return rawDate;
        }
    }

    private String getGenderIcon(String gender) {
        if ("BOYS".equalsIgnoreCase(gender)) return "♂";
        if ("GIRLS".equalsIgnoreCase(gender)) return "♀";
        return "⚥";
    }

    private String getWeatherEmoji(String description) {
        String d = description == null ? "" : description.toLowerCase();

        if (d.contains("clear")) return "☀";
        if (d.contains("cloud")) return "☁";
        if (d.contains("rain")) return "🌧";
        if (d.contains("storm") || d.contains("thunder")) return "⛈";
        if (d.contains("snow")) return "❄";
        if (d.contains("mist") || d.contains("fog") || d.contains("haze")) return "🌫";
        return "🌤";
    }

    private String capitalizeWords(String text) {
        if (text == null || text.isBlank()) return "-";

        String[] parts = text.split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(part.substring(0, 1).toUpperCase())
                  .append(part.substring(1).toLowerCase())
                  .append(" ");
            }
        }

        return sb.toString().trim();
    }
    
    private String formatDistance(String rawDistanceKm) {
        try {
            double km = Double.parseDouble(rawDistanceKm);

            if (km < 1) {
                int meters = (int) Math.round(km * 1000);
                return meters + " m away";
            }

            return String.format("%.2f km away", km);

        } catch (Exception e) {
            return rawDistanceKm + " km away";
        }
    }
}
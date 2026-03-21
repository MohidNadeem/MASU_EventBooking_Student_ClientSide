package com.mohid.masu.student.controller;

import com.mohid.masu.student.App;
import com.mohid.masu.student.service.ApiClient;
import com.mohid.masu.student.session.StudentSession;
import com.mohid.masu.student.util.EventCardFactory;
import java.io.IOException;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

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
    private Button createEventBtn;
    
    @FXML private VBox filtersContainer;

    @FXML private ComboBox<String> filterTypeCombo;
    @FXML private DatePicker filterDatePicker;
    @FXML private ComboBox<String> filterCostCombo;
    @FXML private ComboBox<String> filterGenderCombo;
    @FXML private TextField filterKeywordField;
    @FXML private Label appliedFiltersLabel;

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
        
        if ("ALUMNI".equalsIgnoreCase(status) && createEventBtn != null) {
            createEventBtn.setManaged(false);
            createEventBtn.setVisible(false);
        }
        
        filterTypeCombo.getItems().addAll(
        "Seminar", "Workshop", "Sports", "Competition",
        "Networking", "Entertainment", "Academic", "General"
        );

        filterCostCombo.getItems().addAll("FREE", "PRICED");
        filterGenderCombo.getItems().addAll("BOYS", "GIRLS", "BOTH");
        appliedFiltersLabel.setText("No Filters Applied.");
        
        handleClearFilters();
        showUpcomingEvents();
    }
    
    @FXML
    public void showUpcomingEvents() {
        pageTitleLabel.setText("Upcoming Events");
        pageSubtitleLabel.setText("Discover, book, and explore student events from here.");

        filtersContainer.setManaged(true);
        filtersContainer.setVisible(true);

        loadUpcomingEvents(null, null, null, null, null);
    }
    
    @FXML
    private void handleApplyFilters() {
        String type = filterTypeCombo.getValue();
        String date = filterDatePicker.getValue() != null ? filterDatePicker.getValue().toString() : null;
        String gender = filterGenderCombo.getValue();
        String costType = filterCostCombo.getValue();
        String keyword = filterKeywordField.getText() != null ? filterKeywordField.getText().trim() : null;

        loadUpcomingEvents(type, date, gender, costType, keyword);
    }

    @FXML
    private void handleClearFilters() {
        resetCombo(filterTypeCombo, "Event Type");
        resetCombo(filterCostCombo, "Cost");
        resetCombo(filterGenderCombo, "Gender");
        
        filterDatePicker.setValue(null);
        filterKeywordField.clear();
        filterKeywordField.setPromptText("Keyword");
        
        appliedFiltersLabel.setText("No Filters Applied.");
        loadUpcomingEvents(null, null, null, null, null);
    }
    
    private void resetCombo(ComboBox<String> combo, String placeholder) {
        combo.setValue(null);
        combo.setPromptText(placeholder);
        combo.setButtonCell(null);
    }

    private void loadUpcomingEvents(String type, String date, String gender, String costType, String keyword) {
        contentArea.getChildren().clear();

        JSONObject popularEvent = null;
        double bestRatio = -1.0;

        try {
            String endpoint;

            boolean noFilters =
                    (type == null || type.isBlank()) &&
                    (date == null || date.isBlank()) &&
                    (gender == null || gender.isBlank()) &&
                    (costType == null || costType.isBlank()) &&
                    (keyword == null || keyword.isBlank());

            if (noFilters) {
                endpoint = "/events";
                appliedFiltersLabel.setText("No Filters Applied.");
            } else {
                StringBuilder sb = new StringBuilder("/events/search?");
                boolean first = true;

                if (type != null && !type.isBlank()) {
                    sb.append("type=").append(java.net.URLEncoder.encode(type, java.nio.charset.StandardCharsets.UTF_8));
                    first = false;
                }
                if (date != null && !date.isBlank()) {
                    if (!first) sb.append("&");
                    sb.append("date=").append(java.net.URLEncoder.encode(date, java.nio.charset.StandardCharsets.UTF_8));
                    first = false;
                }
                if (gender != null && !gender.isBlank()) {
                    if (!first) sb.append("&");
                    sb.append("gender=").append(java.net.URLEncoder.encode(gender, java.nio.charset.StandardCharsets.UTF_8));
                    first = false;
                }
                if (costType != null && !costType.isBlank()) {
                    if (!first) sb.append("&");
                    sb.append("costType=").append(java.net.URLEncoder.encode(costType, java.nio.charset.StandardCharsets.UTF_8));
                    first = false;
                }
                if (keyword != null && !keyword.isBlank()) {
                    if (!first) sb.append("&");
                    sb.append("keyword=").append(java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8));
                }

                endpoint = sb.toString();
                appliedFiltersLabel.setText(buildAppliedFiltersText(type, date, gender, costType, keyword));
            }

            String response = ApiClient.get(endpoint);
            JSONArray eventsArray = new JSONArray(response);

            TilePane cardsPane = new TilePane();
            cardsPane.setHgap(18);
            cardsPane.setVgap(18);
            cardsPane.setPrefColumns(3);
            cardsPane.setTileAlignment(Pos.TOP_LEFT);

            int activeCount = 0;

            // Finding most popular active event
            for (int i = 0; i < eventsArray.length(); i++) {
                JSONObject obj = eventsArray.getJSONObject(i);

                String status = obj.optString("status", "");
                if (!"ACTIVE".equalsIgnoreCase(status)) {
                    continue;
                }

                int maxParticipants = obj.optInt("maxParticipants", 0);
                int remainingSeats = obj.optInt("remainingSeats", 0);

                if (maxParticipants > 10) {
                    int booked = maxParticipants - remainingSeats;
                    double ratio = (double) booked / maxParticipants;

                    if (ratio > bestRatio) {
                        bestRatio = ratio;
                        popularEvent = obj;
                    }
                }
            }

            // Adding the popular event first
            if (popularEvent != null) {
                JSONObject popularCopy = new JSONObject(popularEvent.toString());
                popularCopy.put("isPopular", true);

                VBox popularCard = EventCardFactory.createCard(
                        popularCopy,
                        "UPCOMING",
                        this::openEventDetails,
                        eventObj -> {
                            String eventId = eventObj.optString("id", "");
                            String title = eventObj.optString("title", "Untitled Event");
                            String currency = eventObj.optString("currency", "");
                            double cost = eventObj.optDouble("cost", 0.0);

                            handleBookEvent(eventId, title, currency, cost);
                        },
                        null
                );

                cardsPane.getChildren().add(popularCard);
                activeCount++;
            }

            // Adding remaining active events in the normal order
            for (int i = 0; i < eventsArray.length(); i++) {
                JSONObject obj = eventsArray.getJSONObject(i);

                String status = obj.optString("status", "");
                if (!"ACTIVE".equalsIgnoreCase(status)) {
                    continue;
                }

                if (popularEvent != null
                        && obj.optString("id", "").equals(popularEvent.optString("id", ""))) {
                    continue;
                }

                VBox card = EventCardFactory.createCard(
                        obj,
                        "UPCOMING",
                        this::openEventDetails,
                        eventObj -> {
                            String eventId = eventObj.optString("id", "");
                            String title = eventObj.optString("title", "Untitled Event");
                            String currency = eventObj.optString("currency", "");
                            double cost = eventObj.optDouble("cost", 0.0);

                            handleBookEvent(eventId, title, currency, cost);
                        },
                        null
                );

                cardsPane.getChildren().add(card);
                activeCount++;
            }

            if (activeCount == 0) {
                contentArea.getChildren().add(createEmptyUpcomingState());
                return;
            }

            ScrollPane scrollPane = new ScrollPane(cardsPane);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
            scrollPane.getStyleClass().add("transparent-scroll");

            contentArea.getChildren().add(scrollPane);

        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Failed to load upcoming events.");
            errorLabel.getStyleClass().add("page-subtitle");
            contentArea.getChildren().add(errorLabel);
        }
    }

    private VBox createEmptyUpcomingState() {
        VBox wrapper = new VBox(18);
        wrapper.setAlignment(Pos.CENTER_LEFT);

        Label emptyLabel = new Label("No upcoming events");
        emptyLabel.getStyleClass().add("section-title");

        VBox addCard = new VBox(10);
        addCard.getStyleClass().add("empty-state-card");
        addCard.setAlignment(Pos.CENTER);
        addCard.setPadding(new Insets(24));
        addCard.setPrefWidth(260);

        Label plusLabel = new Label("+");
        plusLabel.getStyleClass().add("empty-state-plus");

        Label addText = new Label("Add an Event");
        addText.getStyleClass().add("empty-state-title");

        addCard.getChildren().addAll(plusLabel, addText);

        addCard.setOnMouseClicked(e -> showCreateEvent());

        wrapper.getChildren().addAll(emptyLabel, addCard);
        return wrapper;
    }

    private void handleBookEvent(String eventId, String eventTitle, String currency, double cost) {
        if (cost <= 0) {
            showBookingConfirmation(eventId, eventTitle);
        } else {
            showPaymentDialog(eventId, eventTitle, currency, cost);
        }
    }

    private void showBookingConfirmation(String eventId, String eventTitle) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Book Event");

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(22));
        root.getStyleClass().add("custom-dialog-root");

        Label title = new Label("Book this event?");
        title.getStyleClass().add("dialog-title");

        Label desc = new Label("You are about to book: " + eventTitle);
        desc.getStyleClass().add("dialog-text");
        desc.setWrapText(true);

        Button confirmBtn = new Button("Confirm Booking");
        confirmBtn.getStyleClass().add("primary-btn");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-btn");

        confirmBtn.setOnAction(e -> {
            dialog.close();
            performBooking(eventId);
        });

        cancelBtn.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, confirmBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, desc, buttons);

        Scene scene = new Scene(root, 380, 190);
        scene.getStylesheets().add(getClass().getResource("/com/mohid/masu/student/assets/styles.css").toExternalForm());

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // I am adding
    // A fake payment process (Just for better user flow)
    private void showPaymentDialog(String eventId, String eventTitle, String currency, double cost) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Make a Payment");

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(22));
        root.getStyleClass().add("custom-dialog-root");

        Label title = new Label("Payment Required");
        title.getStyleClass().add("dialog-title");

        Label costLabel = new Label("Event: " + eventTitle + "\nCost: " + currency + " " + cost);
        costLabel.getStyleClass().add("dialog-text");
        costLabel.setWrapText(true);
        costLabel.setAlignment(Pos.CENTER);

        Label statusLabel = new Label("");
        statusLabel.getStyleClass().add("dialog-text");

        Button payBtn = new Button("Make a Payment");
        payBtn.getStyleClass().add("primary-btn");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-btn");

        payBtn.setOnAction(e -> {
            payBtn.setDisable(true);
            cancelBtn.setDisable(true);
            statusLabel.setText("Wait a Moment ...");

            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(ev -> {
                statusLabel.setText("Payment successful. Event booked successfully.");
                performBooking(eventId);
                dialog.close();
            });
            pause.play();
        });

        cancelBtn.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, payBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, costLabel, statusLabel, buttons);

        Scene scene = new Scene(root, 420, 240);
        scene.getStylesheets().add(getClass().getResource("/com/mohid/masu/student/assets/styles.css").toExternalForm());

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void performBooking(String eventId) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("studentId", StudentSession.getStudentId());

            String response = ApiClient.post("/events/" + eventId + "/book", requestBody.toString());
            JSONObject obj = new JSONObject(response);

            String message = obj.optString("message", "Booking completed.");

            showInfoDialog("Booking Status", message);

            if (message.toLowerCase().contains("successfully")) {
                showUpcomingEvents();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showInfoDialog("Booking Status", "Failed to book event. Check API/server connection.");
        }
    }

    private void openEventDetails(String eventId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mohid/masu/student/view/eventDetails.fxml"));
            Parent root = loader.load();

            EventDetailsController controller = loader.getController();
            controller.setEventId(eventId);

            Stage stage = new Stage();
            stage.setTitle("Event Details");
            Scene scene = new Scene(root, 1050, 760);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showInfoDialog("Error", "Failed to open event details.");
        }
    }

    private void showInfoDialog(String titleText, String messageText) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(titleText);

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(22));
        root.getStyleClass().add("custom-dialog-root");

        Label title = new Label(titleText);
        title.getStyleClass().add("dialog-title");

        Label message = new Label(messageText);
        message.getStyleClass().add("dialog-text");
        message.setWrapText(true);

        Button okBtn = new Button("OK");
        okBtn.getStyleClass().add("primary-btn");
        okBtn.setOnAction(e -> dialog.close());

        root.getChildren().addAll(title, message, okBtn);

        Scene scene = new Scene(root, 390, 190);
        scene.getStylesheets().add(getClass().getResource("/com/mohid/masu/student/assets/styles.css").toExternalForm());

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    @FXML
    private void showMyBookings() {
        filtersContainer.setManaged(false);
        filtersContainer.setVisible(false);
    
        pageTitleLabel.setText("My Bookings");
        pageSubtitleLabel.setText("View all events you have booked and manage them from here.");
        loadContent("/com/mohid/masu/student/view/myBookings.fxml");
    }

    @FXML
    private void showPublishedEvents() {
        filtersContainer.setManaged(false);
        filtersContainer.setVisible(false);
    
        pageTitleLabel.setText("Published Events");
        pageSubtitleLabel.setText("Manage the events you created and published.");
        loadContent("/com/mohid/masu/student/view/publishedEvents.fxml");
    }

    @FXML
    private void showCreateEvent() {
        filtersContainer.setManaged(false);
        filtersContainer.setVisible(false);
        
        pageTitleLabel.setText("Create Event");
        pageSubtitleLabel.setText("Publish a new event with details, location, and map support.");
        loadContent("/com/mohid/masu/student/view/createEvent.fxml");
    }

    @FXML
    private void showUpdatePassword() {
        filtersContainer.setManaged(false);
        filtersContainer.setVisible(false);
        
        pageTitleLabel.setText("Update Password");
        pageSubtitleLabel.setText("Change your current password securely.");
        loadContent("/com/mohid/masu/student/view/updatePassword.fxml");
    }
    
    @FXML
    private void showRecentCancelledEvents() {
        filtersContainer.setManaged(false);
        filtersContainer.setVisible(false);
    
        pageTitleLabel.setText("Recent Cancelled Events");
        pageSubtitleLabel.setText("View cancelled events from the last 7 days.");
        loadContent("/com/mohid/masu/student/view/cancelledEvents.fxml");
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (Exception e) {
            e.printStackTrace();
            contentArea.getChildren().clear();
            Label errorLabel = new Label("Failed to load content.");
            errorLabel.getStyleClass().add("page-subtitle");
            contentArea.getChildren().add(errorLabel);
        }
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
                    System.getLogger(DashboardController.class.getName())
                            .log(System.Logger.Level.ERROR, (String) null, ex);
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
    
    private String buildAppliedFiltersText(String type, String date, String gender, String costType, String keyword) {
        StringBuilder sb = new StringBuilder();

        if (gender != null && !gender.isBlank()) {
            sb.append(formatPrettyValue(gender));
        }
        if (costType != null && !costType.isBlank()) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(formatPrettyValue(costType));
        }
        if (type != null && !type.isBlank()) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(type);
        }
        if (date != null && !date.isBlank()) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(formatPrettyDate(date));
        }
        if (keyword != null && !keyword.isBlank()) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append("Keyword: ").append(keyword);
        }

        return sb.length() == 0 ? "No Filters Applied." : sb.toString();
    }
    
    private String formatPrettyValue(String value) {
        if ("BOYS".equalsIgnoreCase(value)) return "Boys";
        if ("GIRLS".equalsIgnoreCase(value)) return "Girls";
        if ("BOTH".equalsIgnoreCase(value)) return "Both";
        if ("FREE".equalsIgnoreCase(value)) return "Free";
        if ("PRICED".equalsIgnoreCase(value)) return "Priced";
        return value;
    }

    private String formatPrettyDate(String rawDate) {
        try {
            java.time.LocalDate parsedDate = java.time.LocalDate.parse(rawDate);
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy");
            return parsedDate.format(formatter);
        } catch (Exception e) {
            return rawDate;
        }
    }
}
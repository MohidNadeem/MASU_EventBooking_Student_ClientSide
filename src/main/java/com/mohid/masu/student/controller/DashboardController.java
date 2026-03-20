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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

        try {
            String response = ApiClient.get("/events");
            JSONArray eventsArray = new JSONArray(response);

            TilePane cardsPane = new TilePane();
            cardsPane.setHgap(18);
            cardsPane.setVgap(18);
            cardsPane.setPrefColumns(3);
            cardsPane.setTileAlignment(Pos.TOP_LEFT);

            int activeCount = 0;

            for (int i = 0; i < eventsArray.length(); i++) {
                JSONObject obj = eventsArray.getJSONObject(i);

                String status = obj.optString("status", "");
                if (!"ACTIVE".equalsIgnoreCase(status)) {
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
        pageTitleLabel.setText("My Bookings");
        pageSubtitleLabel.setText("View all events you have booked and manage them from here.");
        loadContent("/com/mohid/masu/student/view/myBookings.fxml");
    }

    @FXML
    private void showPublishedEvents() {
        pageTitleLabel.setText("Published Events");
        pageSubtitleLabel.setText("Manage the events you created and published.");
        loadContent("/com/mohid/masu/student/view/publishedEvents.fxml");
    }

    @FXML
    private void showCreateEvent() {
        pageTitleLabel.setText("Create Event");
        pageSubtitleLabel.setText("Publish a new event with details, location, and map support.");
        loadContent("/com/mohid/masu/student/view/createEvent.fxml");
    }

    @FXML
    private void showUpdatePassword() {
        pageTitleLabel.setText("Update Password");
        pageSubtitleLabel.setText("Change your current password securely.");
        loadContent("/com/mohid/masu/student/view/updatePassword.fxml");
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
}
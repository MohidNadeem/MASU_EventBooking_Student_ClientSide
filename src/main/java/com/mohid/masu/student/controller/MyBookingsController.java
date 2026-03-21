package com.mohid.masu.student.controller;

import com.mohid.masu.student.service.ApiClient;
import com.mohid.masu.student.session.StudentSession;
import com.mohid.masu.student.util.EventCardFactory;
import javafx.fxml.FXML;
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
import org.json.JSONArray;
import org.json.JSONObject;

public class MyBookingsController {

    @FXML
    private VBox rootBox;

    @FXML
    public void initialize() {
        loadBookings();
    }

    private void loadBookings() {
        rootBox.getChildren().clear();

        try {
            String bookingsResponse = ApiClient.get("/students/" + StudentSession.getStudentId() + "/bookings");
            JSONArray bookingsArray = new JSONArray(bookingsResponse);

            if (bookingsArray.isEmpty()) {
                rootBox.getChildren().add(createEmptyState());
                return;
            }

            TilePane cardsPane = new TilePane();
            cardsPane.setHgap(22);
            cardsPane.setVgap(22);
            cardsPane.setPrefColumns(2);
            cardsPane.setTileAlignment(Pos.TOP_LEFT);

            int count = 0;

            for (int i = 0; i < bookingsArray.length(); i++) {
                JSONObject booking = bookingsArray.getJSONObject(i);
                String eventId = booking.optString("eventId", "");

                if (eventId.isBlank()) continue;

                String eventResponse = ApiClient.get("/events/" + eventId);
                JSONObject eventObj = new JSONObject(eventResponse);

                boolean hasRated = hasStudentRatedEvent(eventId, StudentSession.getStudentId());
                eventObj.put("userHasRated", hasRated);

                VBox card = EventCardFactory.createCard(
                        eventObj,
                        "BOOKINGS",
                        this::openEventDetails,
                        this::handleUnbookEvent,
                        this::handleRateEvent
                );

                cardsPane.getChildren().add(card);
                count++;
            }

            if (count == 0) {
                rootBox.getChildren().add(createEmptyState());
                return;
            }

            ScrollPane scrollPane = new ScrollPane(cardsPane);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.getStyleClass().add("transparent-scroll");

            rootBox.getChildren().add(scrollPane);

        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Failed to load bookings.");
            errorLabel.getStyleClass().add("page-subtitle");
            rootBox.getChildren().add(errorLabel);
        }
    }

    private void handleUnbookEvent(JSONObject eventObj) {
        try {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Unbook Event");

            VBox root = new VBox(15);
            root.setAlignment(Pos.CENTER);
            root.setPadding(new Insets(22));
            root.getStyleClass().add("custom-dialog-root");

            Label title = new Label("Unbook this event?");
            title.getStyleClass().add("dialog-title");

            Label desc = new Label("This booking will be removed from your account.");
            desc.getStyleClass().add("dialog-text");
            desc.setWrapText(true);

            Button yesBtn = new Button("Yes, Unbook");
            yesBtn.getStyleClass().add("primary-btn");

            Button cancelBtn = new Button("Cancel");
            cancelBtn.getStyleClass().add("secondary-btn");

            yesBtn.setOnAction(e -> {
                try {
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("studentId", StudentSession.getStudentId());

                    String eventId = eventObj.optString("id", "");
                    String response = ApiClient.deleteWithBody("/events/" + eventId + "/unbook", requestBody.toString());

                    dialog.close();
                    showInfoDialog("Unbook Status", new JSONObject(response).optString("message", "Event unbooked."));
                    loadBookings();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    dialog.close();
                    showInfoDialog("Unbook Status", "Failed to unbook event.");
                }
            });

            cancelBtn.setOnAction(e -> dialog.close());

            root.getChildren().addAll(title, desc, new javafx.scene.layout.HBox(10, yesBtn, cancelBtn));
            ((javafx.scene.layout.HBox) root.getChildren().get(2)).setAlignment(Pos.CENTER);

            Scene scene = new Scene(root, 390, 190);
            scene.getStylesheets().add(getClass().getResource("/com/mohid/masu/student/assets/styles.css").toExternalForm());

            dialog.setScene(scene);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEventDetails(String eventId) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/mohid/masu/student/view/eventDetails.fxml")
            );
            Parent root = loader.load();

            EventDetailsController controller = loader.getController();
            controller.setEventId(eventId);

            Stage stage = new Stage();
            stage.setTitle("Event Details");
            stage.setScene(new Scene(root, 1050, 760));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showInfoDialog("Error", "Failed to open event details.");
        }
    }

    private VBox createEmptyState() {
        VBox wrapper = new VBox(18);
        wrapper.setAlignment(Pos.CENTER_LEFT);

        Label emptyLabel = new Label("No booked events");
        emptyLabel.getStyleClass().add("section-title");

        wrapper.getChildren().add(emptyLabel);
        return wrapper;
    }

    private void showInfoDialog(String titleText, String messageText) {
        try {
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean hasStudentRatedEvent(String eventId, String studentId) {
        try {
            String response = ApiClient.get("/events/" + eventId + "/ratings");
            org.json.JSONArray ratingsArray = new org.json.JSONArray(response);

            for (int i = 0; i < ratingsArray.length(); i++) {
                org.json.JSONObject rating = ratingsArray.getJSONObject(i);
                if (studentId.equals(rating.optString("studentId", ""))) {
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    private void handleRateEvent(JSONObject eventObj) {
        try {
            String eventId = eventObj.optString("id", "");
            String eventTitle = eventObj.optString("title", "this event");

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Rate Event");

            VBox root = new VBox(16);
            root.setAlignment(Pos.CENTER);
            root.setPadding(new Insets(22));
            root.getStyleClass().add("custom-dialog-root");

            Label title = new Label("Rate this event");
            title.getStyleClass().add("dialog-title");

            Label desc = new Label("How would you rate \"" + eventTitle + "\"?");
            desc.getStyleClass().add("dialog-text");
            desc.setWrapText(true);

            HBox starRow = new HBox(10);
            starRow.setAlignment(Pos.CENTER);

            final int[] selectedStars = {0};
            Button[] starButtons = new Button[5];

            for (int i = 0; i < 5; i++) {
                int stars = i + 1;
                Button starBtn = new Button("★");
                starBtn.getStyleClass().add("star-btn");

                starBtn.setOnAction(e -> {
                    selectedStars[0] = stars;
                    for (int j = 0; j < 5; j++) {
                        if (j < stars) {
                            starButtons[j].getStyleClass().remove("star-btn");
                            if (!starButtons[j].getStyleClass().contains("star-btn-selected")) {
                                starButtons[j].getStyleClass().add("star-btn-selected");
                            }
                        } else {
                            starButtons[j].getStyleClass().remove("star-btn-selected");
                            if (!starButtons[j].getStyleClass().contains("star-btn")) {
                                starButtons[j].getStyleClass().add("star-btn");
                            }
                        }
                    }
                });

                starButtons[i] = starBtn;
                starRow.getChildren().add(starBtn);
            }

            Label messageLabel = new Label();
            messageLabel.getStyleClass().add("dialog-text");
            messageLabel.setWrapText(true);

            Button submitBtn = new Button("Submit Rating");
            submitBtn.getStyleClass().add("primary-btn");

            Button cancelBtn = new Button("Cancel");
            cancelBtn.getStyleClass().add("secondary-btn");

            submitBtn.setOnAction(e -> {
                try {
                    if (selectedStars[0] == 0) {
                        messageLabel.setText("Please select a rating first.");
                        return;
                    }

                    JSONObject requestBody = new JSONObject();
                    requestBody.put("studentId", StudentSession.getStudentId());
                    requestBody.put("stars", selectedStars[0]);

                    String response = ApiClient.post("/events/" + eventId + "/rate", requestBody.toString());
                    String msg = new JSONObject(response).optString("message", "Rating submitted.");

                    dialog.close();
                    showInfoDialog("Rating Status", msg);
                    loadBookings();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    messageLabel.setText("Failed to submit rating.");
                }
            });

            cancelBtn.setOnAction(e -> dialog.close());

            HBox buttons = new HBox(10, submitBtn, cancelBtn);
            buttons.setAlignment(Pos.CENTER);

            root.getChildren().addAll(title, desc, starRow, messageLabel, buttons);

            Scene scene = new Scene(root, 430, 240);
            scene.getStylesheets().add(getClass().getResource("/com/mohid/masu/student/assets/styles.css").toExternalForm());

            dialog.setScene(scene);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showInfoDialog("Rating Status", "Failed to open rating dialog.");
        }
    }
}
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

                VBox card = EventCardFactory.createCard(
                        eventObj,
                        "BOOKINGS",
                        this::openEventDetails,
                        this::handleUnbookEvent,
                        null
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
}
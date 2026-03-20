package com.mohid.masu.student.controller;

import com.mohid.masu.student.service.ApiClient;
import com.mohid.masu.student.session.StudentSession;
import com.mohid.masu.student.util.EventCardFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

public class PublishedEventsController {

    @FXML
    private VBox rootBox;

    @FXML
    public void initialize() {
        loadPublishedEvents();
    }

    private void loadPublishedEvents() {
        rootBox.getChildren().clear();

        try {
            String response = ApiClient.get("/events");
            JSONArray eventsArray = new JSONArray(response);

            TilePane cardsPane = new TilePane();
            cardsPane.setHgap(22);
            cardsPane.setVgap(22);
            cardsPane.setPrefColumns(2);
            cardsPane.setTileAlignment(Pos.TOP_LEFT);

            int count = 0;

            for (int i = 0; i < eventsArray.length(); i++) {
                JSONObject eventObj = eventsArray.getJSONObject(i);

                if (!StudentSession.getStudentId().equals(eventObj.optString("publisherId", ""))) {
                    continue;
                }

                VBox card = EventCardFactory.createCard(
                        eventObj,
                        "PUBLISHED",
                        this::openEventDetails,
                        this::openUpdateEvent,
                        null
                );

                cardsPane.getChildren().add(card);
                count++;
            }

            if (count == 0) {
                VBox wrapper = new VBox(18);
                wrapper.setAlignment(Pos.CENTER_LEFT);

                Label emptyLabel = new Label("No published events");
                emptyLabel.getStyleClass().add("section-title");

                VBox addCard = new VBox(10);
                addCard.getStyleClass().add("empty-state-card");
                addCard.setAlignment(Pos.CENTER);
                addCard.setPadding(new javafx.geometry.Insets(24));
                addCard.setPrefWidth(260);

                Label plusLabel = new Label("+");
                plusLabel.getStyleClass().add("empty-state-plus");

                Label addText = new Label("Add an Event");
                addText.getStyleClass().add("empty-state-title");

                addCard.getChildren().addAll(plusLabel, addText);

                wrapper.getChildren().addAll(emptyLabel, addCard);
                rootBox.getChildren().add(wrapper);
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
            Label errorLabel = new Label("Failed to load published events.");
            errorLabel.getStyleClass().add("page-subtitle");
            rootBox.getChildren().add(errorLabel);
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
            stage.setScene(new Scene(root, 1050, 760));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openUpdateEvent(JSONObject eventObj) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mohid/masu/student/view/updateEvent.fxml"));
            Parent root = loader.load();

            UpdateEventController controller = loader.getController();
            controller.setEventData(eventObj);

            Stage stage = new Stage();
            stage.setTitle("Update Event");
            stage.setScene(new Scene(root, 900, 760));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
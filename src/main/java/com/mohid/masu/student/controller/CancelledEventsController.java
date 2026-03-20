package com.mohid.masu.student.controller;

import com.mohid.masu.student.service.ApiClient;
import com.mohid.masu.student.util.EventCardFactory;
import java.time.LocalDate;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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

public class CancelledEventsController {

    @FXML
    private VBox rootBox;

    @FXML
    public void initialize() {
        loadCancelledEvents();
    }

    private void loadCancelledEvents() {
        rootBox.getChildren().clear();

        try {
            String response = ApiClient.get("/events");
            JSONArray eventsArray = new JSONArray(response);

            TilePane cardsPane = new TilePane();
            cardsPane.setHgap(22);
            cardsPane.setVgap(22);
            cardsPane.setPrefColumns(2);
            cardsPane.setTileAlignment(Pos.TOP_LEFT);

            LocalDate today = LocalDate.now();
            LocalDate cutoffDate = today.minusDays(2);

            int count = 0;

            for (int i = 0; i < eventsArray.length(); i++) {
                JSONObject eventObj = eventsArray.getJSONObject(i);

                String status = eventObj.optString("status", "");
                String rawDate = eventObj.optString("date", "");

                if (!"CANCELLED".equalsIgnoreCase(status)) {
                    continue;
                }

                if (rawDate.isBlank()) {
                    continue;
                }

                LocalDate eventDate;
                try {
                    eventDate = LocalDate.parse(rawDate);
                } catch (Exception e) {
                    continue;
                }

                if (eventDate.isBefore(cutoffDate)) {
                    continue;
                }

                VBox card = EventCardFactory.createCard(
                        eventObj,
                        "CANCELLED",
                        this::openEventDetails,
                        null,
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
            Label errorLabel = new Label("Failed to load cancelled events.");
            errorLabel.getStyleClass().add("page-subtitle");
            rootBox.getChildren().add(errorLabel);
        }
    }

    private VBox createEmptyState() {
        VBox wrapper = new VBox(14);
        wrapper.setAlignment(Pos.CENTER_LEFT);

        VBox emptyCard = new VBox(10);
        emptyCard.getStyleClass().add("content-card");
        emptyCard.setPadding(new Insets(24));
        emptyCard.setMaxWidth(420);

        Label title = new Label("No recent cancelled events");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label("There are no recent cancelled events to show right now.");
        subtitle.getStyleClass().add("page-subtitle");
        subtitle.setWrapText(true);

        emptyCard.getChildren().addAll(title, subtitle);
        wrapper.getChildren().add(emptyCard);

        return wrapper;
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
}
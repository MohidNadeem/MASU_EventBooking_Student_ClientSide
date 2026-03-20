package com.mohid.masu.student.util;

import com.mohid.masu.student.session.StudentSession;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.json.JSONObject;

// Extracted Logic for Event Card Creation
// For reusability (to be used in multiple screens)
public class EventCardFactory {

    // Returning Event Card (VBox)
    public static VBox createCard(
            JSONObject obj,
            String mode,
            Consumer<String> onViewDetails,
            Consumer<JSONObject> onPrimaryAction,
            Consumer<JSONObject> onSecondaryAction
    ) {
        VBox card = new VBox(16);
        card.getStyleClass().add("event-card");
        card.setPadding(new Insets(20));
        card.setPrefWidth(370);
        card.setMaxWidth(370);

        String eventId = obj.optString("id", "");
        String title = obj.optString("title", "Untitled Event");
        String type = obj.optString("type", "General");
        String date = formatEventDate(obj.optString("date", "-"));
        String startTime = obj.optString("startTime", "-");
        String endTime = obj.optString("endTime", "-");
        String venue = obj.optString("venueName", "-");
        String location = obj.optString("location", "-");
        String gender = obj.optString("gender", "BOTH");
        String currency = obj.optString("currency", "");
        double cost = obj.optDouble("cost", 0.0);

        int remainingSeats = obj.optInt("remainingSeats", 0);
        int remainingAlumniSlots = obj.optInt("remainingAlumniSlots", 0);

        String currentStatus = StudentSession.getStatus();
        String remainingText = "ALUMNI".equalsIgnoreCase(currentStatus)
                ? "Remaining Alumni Slots: " + remainingAlumniSlots
                : "Remaining Seats: " + remainingSeats;
        
        String status = obj.optString("status", "");
        boolean isCancelled = "CANCELLED".equalsIgnoreCase(status);
        
        HBox row1 = new HBox();
        row1.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("event-card-title");
        titleLabel.setWrapText(true);

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Label typeBadge = new Label(type);
        typeBadge.getStyleClass().add("event-type-badge");

        row1.getChildren().addAll(titleLabel, spacer1, typeBadge);

        HBox row2 = new HBox();
        row2.setAlignment(Pos.CENTER_LEFT);

        Label dateTimeLabel = new Label(date + "   •   " + startTime + " - " + endTime);
        dateTimeLabel.getStyleClass().add("event-card-subtext");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Label genderBadge = new Label(getGenderIcon(gender) + " " + gender);
        genderBadge.getStyleClass().add("event-gender-badge");

        row2.getChildren().addAll(dateTimeLabel, spacer2, genderBadge);

        Label venueLocationLabel = new Label(venue + ", " + location);
        venueLocationLabel.getStyleClass().add("event-card-meta");
        venueLocationLabel.setWrapText(true);

        HBox row4 = new HBox();
        row4.setAlignment(Pos.CENTER_LEFT);

        String costText = cost <= 0 ? "Free" : currency + " " + cost;
        Label costLabel = new Label("Cost: " + costText);
        costLabel.getStyleClass().add("event-card-cost");

        Region spacer4 = new Region();
        HBox.setHgrow(spacer4, Priority.ALWAYS);

        Label remainingLabel = new Label(remainingText);
        remainingLabel.getStyleClass().add("event-remaining-inline");

        row4.getChildren().addAll(costLabel, spacer4, remainingLabel);

        Button viewBtn = new Button("View Details");
        viewBtn.getStyleClass().add("secondary-btn");
        viewBtn.setOnAction(e -> onViewDetails.accept(eventId));

        HBox row5 = new HBox(12);
        row5.setAlignment(Pos.CENTER_LEFT);
        row5.getChildren().add(viewBtn);

        if ("BOOKINGS".equalsIgnoreCase(mode)) {
            Button unbookBtn = new Button("Unbook Event");
            unbookBtn.getStyleClass().add("primary-btn");
            unbookBtn.setDisable(cost > 0);
            unbookBtn.setOnAction(e -> onPrimaryAction.accept(obj));
            row5.getChildren().add(unbookBtn);

        } else if ("PUBLISHED".equalsIgnoreCase(mode)) {
            Button updateBtn = new Button("Update Event");
            updateBtn.getStyleClass().add("primary-btn");
            updateBtn.setDisable(isCancelled);
            updateBtn.setOnAction(e -> onPrimaryAction.accept(obj));
            row5.getChildren().add(updateBtn);
            
        } else if ("UPCOMING".equalsIgnoreCase(mode)) {
            Button bookBtn = new Button("Book Event");
            bookBtn.getStyleClass().add("primary-btn");
            bookBtn.setOnAction(e -> onPrimaryAction.accept(obj));
            row5.getChildren().add(bookBtn);
            
        } else if ("CANCELLED".equalsIgnoreCase(mode)) {
            // only View Details button, so no more addition for now
        }
        
        if (isCancelled) {
            Region spacer5 = new Region();
            HBox.setHgrow(spacer5, Priority.ALWAYS);

            Label cancelledBadge = new Label("Cancelled");
            cancelledBadge.getStyleClass().add("cancelled-status-badge");

            row5.getChildren().addAll(spacer5, cancelledBadge);
        }

        card.getChildren().addAll(
                row1,
                row2,
                venueLocationLabel,
                row4,
                row5
        );

        return card;
    }

    // Gender Icons
    private static String getGenderIcon(String gender) {
        if ("BOYS".equalsIgnoreCase(gender)) return "♂";
        if ("GIRLS".equalsIgnoreCase(gender)) return "♀";
        return "⚥";
    }

    // Formatting to dd Month yyyy (Month -> Feb, Apr, Jun .. etc)
    private static String formatEventDate(String rawDate) {
        try {
            LocalDate parsedDate = LocalDate.parse(rawDate);
            return parsedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        } catch (Exception e) {
            return rawDate;
        }
    }
}
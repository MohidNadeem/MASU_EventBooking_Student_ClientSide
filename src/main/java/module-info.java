module com.mohid.masu_studentapp {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mohid.masu.student to javafx.fxml;
    exports com.mohid.masu.student;
}

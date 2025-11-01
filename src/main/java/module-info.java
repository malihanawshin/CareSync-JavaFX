module com.example.healthcareapp {
        requires javafx.controls;
        requires javafx.fxml;
        requires java.sql;
        requires jbcrypt;
        requires javafx.base;

    // Open packages for FXML controller instantiation
        opens com.example.healthcareapp to javafx.graphics, javafx.fxml;
        opens com.example.healthcareapp.controller to javafx.fxml;

        // IMPORTANT: Open model package to javafx.base for PropertyValueFactory reflection
        opens com.example.healthcareapp.model to javafx.base, javafx.controls;

        // Open DAO package if needed
        opens com.example.healthcareapp.dao to javafx.fxml;
        opens com.example.healthcareapp.util to javafx.fxml;
}

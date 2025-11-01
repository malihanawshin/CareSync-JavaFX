package com.example.healthcareapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HealthcareApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Load login view instead of main view
        FXMLLoader fxmlLoader = new FXMLLoader(
                HealthcareApplication.class.getResource("/LoginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 500);

        stage.setTitle("Healthcare Management System - Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

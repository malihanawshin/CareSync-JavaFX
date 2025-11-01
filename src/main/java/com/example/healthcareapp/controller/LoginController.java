package com.example.healthcareapp.controller;

import com.example.healthcareapp.dao.UserDAO;
import com.example.healthcareapp.model.User;
import com.example.healthcareapp.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private UserDAO userDAO;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        errorLabel.setText("");

        // Allow Enter key to submit
        passwordField.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password");
            return;
        }

        // Authenticate
        User user = userDAO.authenticate(username, password);

        if (user != null) {
            // Store logged-in user in session
            SessionManager.getInstance().setCurrentUser(user);

            // Show success and load main application
            errorLabel.setStyle("-fx-text-fill: green;");
            errorLabel.setText("Login successful! Welcome " + user.getFullName());

            // Load main view after short delay
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    javafx.application.Platform.runLater(this::loadMainView);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } else {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Invalid username or password");
            passwordField.clear();
        }
    }

    private void loadMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Healthcare Management System - " +
                    SessionManager.getInstance().getCurrentUser().getFullName());

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Error loading main application");
        }
    }
}

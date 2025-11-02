package com.example.healthcareapp.controller;

import com.example.healthcareapp.Refreshable;
import com.example.healthcareapp.dao.UserDAO;
import com.example.healthcareapp.model.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class UserRegistrationController {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private Label usernameStatusLabel;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordStrengthLabel;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Button registerButton;
    @FXML private Button clearButton;
    @FXML private Button closeButton;
    @FXML private Button checkUsernameButton;

    private UserDAO userDAO;
    private boolean usernameAvailable = false;
    private Refreshable refreshListener;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        errorLabel.setText("");

        // Populate role ComboBox with options
        roleComboBox.setItems(FXCollections.observableArrayList(
                "Admin", "Doctor", "Receptionist"
        ));

        // Add listeners for real-time validation
        passwordField.textProperty().addListener((obs, oldVal, newVal) ->
                updatePasswordStrength(newVal));

        // Set default role
        roleComboBox.setValue("Doctor");
    }

    public void setRefreshListener(Refreshable listener) {
        this.refreshListener = listener;
    }

    @FXML
    private void checkUsernameAvailability() {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            usernameStatusLabel.setStyle("-fx-text-fill: red;");
            usernameStatusLabel.setText("Please enter a username");
            usernameAvailable = false;
            return;
        }

        // Check if username exists in database
        if (userDAO.usernameExists(username)) {
            usernameStatusLabel.setStyle("-fx-text-fill: red;");
            usernameStatusLabel.setText("❌ Username already taken");
            usernameAvailable = false;
        } else {
            usernameStatusLabel.setStyle("-fx-text-fill: green;");
            usernameStatusLabel.setText("✓ Username available");
            usernameAvailable = true;
        }
    }

    private void updatePasswordStrength(String password) {
        if (password.isEmpty()) {
            passwordStrengthLabel.setText("");
            return;
        }

        int strength = calculatePasswordStrength(password);
        String[] strengthLabels = {"Very Weak", "Weak", "Fair", "Good", "Strong"};
        String[] colors = {"-fx-text-fill: red;", "-fx-text-fill: orange;",
                "-fx-text-fill: yellow;", "-fx-text-fill: lightgreen;",
                "-fx-text-fill: green;"};

        passwordStrengthLabel.setStyle(colors[strength]);
        passwordStrengthLabel.setText("Strength: " + strengthLabels[strength]);
    }

    private int calculatePasswordStrength(String password) {
        int strength = 0;

        if (password.length() >= 8) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) strength++;

        return Math.min(strength - 1, 4);
    }

    @FXML
    private void handleRegister() {
        // Clear previous error
        errorLabel.setText("");

        // Validation
        if (!validateForm()) {
            return;
        }

        try {
            // Create new user
            User newUser = new User();
            newUser.setFullName(fullNameField.getText().trim());
            newUser.setUsername(usernameField.getText().trim());
            newUser.setEmail(emailField.getText().trim());
            newUser.setRole(roleComboBox.getValue());

            String password = passwordField.getText();

            // Register user
            if (userDAO.createUser(newUser, password)) {
                showAlert("Success",
                        "User registered successfully!\n\n" +
                                "Username: " + newUser.getUsername() + "\n" +
                                "Role: " + newUser.getRole(),
                        Alert.AlertType.INFORMATION);
                if (refreshListener != null) {
                    refreshListener.refreshDoctors();
                }
                handleClear();
                usernameAvailable = false;
                usernameStatusLabel.setText("");

            } else {
                errorLabel.setStyle("-fx-text-fill: red;");
                errorLabel.setText("Failed to register user. Please try again.");
            }
        } catch (Exception e) {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear() {
        fullNameField.clear();
        usernameField.clear();
        emailField.clear();
        roleComboBox.setValue("Doctor");
        passwordField.clear();
        confirmPasswordField.clear();
        errorLabel.setText("");
        usernameStatusLabel.setText("");
        passwordStrengthLabel.setText("");
        usernameAvailable = false;
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private boolean validateForm() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String role = roleComboBox.getValue();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (fullName.isEmpty()) {
            errorLabel.setText("Full name is required");
            return false;
        }

        if (username.isEmpty()) {
            errorLabel.setText("Username is required");
            return false;
        }

        if (!usernameAvailable) {
            errorLabel.setText("Please check username availability first");
            return false;
        }

        if (email.isEmpty()) {
            errorLabel.setText("Email is required");
            return false;
        }

        if (!isValidEmail(email)) {
            errorLabel.setText("Invalid email format");
            return false;
        }

        if (role == null || role.isEmpty()) {
            errorLabel.setText("Please select a role");
            return false;
        }

        if (password.isEmpty()) {
            errorLabel.setText("Password is required");
            return false;
        }

        if (password.length() < 8) {
            errorLabel.setText("Password must be at least 8 characters long");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

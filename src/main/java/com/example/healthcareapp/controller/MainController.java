package com.example.healthcareapp.controller;

import com.example.healthcareapp.dao.PatientDAO;
import com.example.healthcareapp.dao.UserDAO;
import com.example.healthcareapp.model.Patient;
import com.example.healthcareapp.model.User;
import com.example.healthcareapp.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // Text Fields and Input Controls
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextArea addressArea;
    @FXML private TextArea medicalHistoryArea;
    @FXML private TabPane mainTabPane;
    @FXML private Tab adminTab;
    @FXML private TableView<User> usersTable;

    // Buttons
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    @FXML private Button logoutButton;

    // Top section
    @FXML private Label userInfoLabel;

    // Table View and Columns
    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, Integer> idColumn;
    @FXML private TableColumn<Patient, String> nameColumn;
    @FXML private TableColumn<Patient, LocalDate> dobColumn;
    @FXML private TableColumn<Patient, String> genderColumn;
    @FXML private TableColumn<Patient, String> phoneColumn;
    @FXML private TableColumn<Patient, String> emailColumn;

    @FXML private TableColumn<User, Integer> userIdColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, Boolean> activeColumn;
    @FXML private TableColumn<User, String> lastLoginColumn;


    // Instance variables
    private PatientDAO patientDAO;
    private ObservableList<Patient> patientList;
    private Patient selectedPatient;
    private User currentUser;
    private UserDAO userDAO;
    private ObservableList<User> userList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get logged-in user from session
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            showAlert("Error", "Please login first", Alert.AlertType.ERROR);
            return;
        }

        // Initialize DAOs and data structures
        patientDAO = new PatientDAO();
        patientList = FXCollections.observableArrayList();

        userDAO = new UserDAO();
        userList = FXCollections.observableArrayList();

        setupUsersTable();

        // Display current user info
        displayUserInfo();

        // Populate ComboBox with gender options
        genderComboBox.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));

        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        nameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()));
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        patientTable.setItems(patientList);

        // Add selection listener
        patientTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    selectedPatient = newSelection;
                    if (selectedPatient != null) {
                        populateForm(selectedPatient);
                    }
                });

        // Apply role-based access control
        applyAccessControl();

        // Load patients from database
        loadPatients();

        if (!currentUser.hasRole("Admin")) {
            mainTabPane.getTabs().remove(adminTab);
        } else {
            loadUsersList();
        }
    }

    // Display current logged-in user info
    private void displayUserInfo() {
        String userInfo = "Logged in as: " + currentUser.getFullName() +
                " (" + currentUser.getRole() + ")";
        userInfoLabel.setText(userInfo);
    }

    // to set up the users table
    private void setupUsersTable() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        lastLoginColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String lastLogin = user.getLastLogin() != null ?
                    user.getLastLogin().toString() : "Never";
            return new javafx.beans.property.SimpleStringProperty(lastLogin);
        });

        usersTable.setItems(userList);
    }

    // Apply role-based access control restrictions
    private void applyAccessControl() {
        // Receptionists can only view and add patients
        if (currentUser.hasRole("Receptionist")) {
            updateButton.setDisable(true);
            deleteButton.setDisable(true);
        }

        // Doctors can add and update but not delete
        if (currentUser.hasRole("Doctor")) {
            deleteButton.setDisable(true);
        }

        if (!currentUser.hasRole("Admin")) {
            mainTabPane.getTabs().remove(adminTab);
        }else {
            // Load users list for admin
            loadUsersList();
        }
        // Only admins can perform all operations (no restrictions)
    }

    // Logout handler
    @FXML
    private void handleLogout() {
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Logout");
        confirmAlert.setHeaderText("Logout Confirmation");
        confirmAlert.setContentText("Are you sure you want to logout?");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            performLogout();
        }
    }

    // Perform actual logout
    private void performLogout() {
        try {
            // Clear session
            SessionManager.getInstance().logout();

            // Load login view
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/LoginView.fxml"));
            Scene scene = new Scene(loader.load(), 500, 500);

            // Get current stage and switch to login
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Healthcare Management System - Login");
            stage.setResizable(false);

            showAlert("Success", "Logged out successfully", Alert.AlertType.INFORMATION);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error during logout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Existing methods for patient management
    @FXML
    private void addPatient() {
        if (validateInput()) {
            Patient patient = createPatientFromForm();
            if (patientDAO.addPatient(patient)) {
                showAlert("Success", "Patient added successfully!", Alert.AlertType.INFORMATION);
                clearForm();
                loadPatients();
            } else {
                showAlert("Error", "Failed to add patient!", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void updatePatient() {
        if (selectedPatient != null && validateInput()) {
            Patient patient = createPatientFromForm();
            patient.setPatientId(selectedPatient.getPatientId());

            if (patientDAO.updatePatient(patient)) {
                showAlert("Success", "Patient updated successfully!", Alert.AlertType.INFORMATION);
                clearForm();
                loadPatients();
            } else {
                showAlert("Error", "Failed to update patient!", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Warning", "Please select a patient to update!", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void deletePatient() {
        if (selectedPatient != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText("Delete Patient");
            confirmation.setContentText("Are you sure you want to delete this patient?");

            if (confirmation.showAndWait().get() == ButtonType.OK) {
                if (patientDAO.deletePatient(selectedPatient.getPatientId())) {
                    showAlert("Success", "Patient deleted successfully!", Alert.AlertType.INFORMATION);
                    clearForm();
                    loadPatients();
                } else {
                    showAlert("Error", "Failed to delete patient!", Alert.AlertType.ERROR);
                }
            }
        } else {
            showAlert("Warning", "Please select a patient to delete!", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        dobPicker.setValue(null);
        genderComboBox.setValue(null);
        phoneField.clear();
        emailField.clear();
        addressArea.clear();
        medicalHistoryArea.clear();
        selectedPatient = null;
        patientTable.getSelectionModel().clearSelection();
    }

    private void loadPatients() {
        patientList.clear();
        patientList.addAll(patientDAO.getAllPatients());
    }

    private Patient createPatientFromForm() {
        Patient patient = new Patient();
        patient.setFirstName(firstNameField.getText().trim());
        patient.setLastName(lastNameField.getText().trim());
        patient.setDateOfBirth(dobPicker.getValue());
        patient.setGender(genderComboBox.getValue());
        patient.setPhone(phoneField.getText().trim());
        patient.setEmail(emailField.getText().trim());
        patient.setAddress(addressArea.getText().trim());
        patient.setMedicalHistory(medicalHistoryArea.getText().trim());
        return patient;
    }

    private void populateForm(Patient patient) {
        firstNameField.setText(patient.getFirstName());
        lastNameField.setText(patient.getLastName());
        dobPicker.setValue(patient.getDateOfBirth());
        genderComboBox.setValue(patient.getGender());
        phoneField.setText(patient.getPhone());
        emailField.setText(patient.getEmail());
        addressArea.setText(patient.getAddress());
        medicalHistoryArea.setText(patient.getMedicalHistory());
    }

    private boolean validateInput() {
        if (firstNameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "First name is required!", Alert.AlertType.WARNING);
            return false;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Last name is required!", Alert.AlertType.WARNING);
            return false;
        }
        if (dobPicker.getValue() == null) {
            showAlert("Validation Error", "Date of birth is required!", Alert.AlertType.WARNING);
            return false;
        }
        if (genderComboBox.getValue() == null) {
            showAlert("Validation Error", "Gender is required!", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void openUserRegistration() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/UserRegistrationView.fxml"));
            VBox registrationPane = loader.load();

            Scene scene = new Scene(registrationPane, 600, 600);
            Stage stage = new Stage();
            stage.setTitle("Register New User");
            stage.setScene(scene);

            // Refresh users list when registration window closes
            stage.setOnHidden(event -> {
                System.out.println("Registration window closed, reloading users list...");
                loadUsersList();
            });

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open registration form: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void manageUsers() {
        // Load and display users in table
        loadUsersList();
        showAlert("Info", "Users list refreshed", Alert.AlertType.INFORMATION);
    }

    private void loadUsersList() {
        try {
            userList.clear();
            userList.addAll(userDAO.getActiveUsers());
            System.out.println("Loaded " + userList.size() + " users");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load users list: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}

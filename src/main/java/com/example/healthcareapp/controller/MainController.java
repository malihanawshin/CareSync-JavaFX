package com.example.healthcareapp.controller;

import com.example.healthcareapp.dao.PatientDAO;
import com.example.healthcareapp.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextArea addressArea;
    @FXML private TextArea medicalHistoryArea;
    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, Integer> idColumn;
    @FXML private TableColumn<Patient, String> nameColumn;
    @FXML private TableColumn<Patient, LocalDate> dobColumn;
    @FXML private TableColumn<Patient, String> genderColumn;
    @FXML private TableColumn<Patient, String> phoneColumn;
    @FXML private TableColumn<Patient, String> emailColumn;

    private PatientDAO patientDAO;
    private ObservableList<Patient> patientList;
    private Patient selectedPatient;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        patientDAO = new PatientDAO();
        patientList = FXCollections.observableArrayList();

        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        nameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()));
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        genderComboBox.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));

        patientTable.setItems(patientList);

        // Add selection listener
        patientTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    selectedPatient = newSelection;
                    if (selectedPatient != null) {
                        populateForm(selectedPatient);
                    }
                });

        loadPatients();
    }

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
}

package com.example.healthcareapp.controller;

import com.example.healthcareapp.Refreshable;
import com.example.healthcareapp.dao.AppointmentDAO;
import com.example.healthcareapp.dao.PatientDAO;
import com.example.healthcareapp.dao.UserDAO;
import com.example.healthcareapp.model.*;
import com.example.healthcareapp.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AppointmentController implements Initializable, Refreshable {

    // Book Appointment Tab Controls
    @FXML private ComboBox<Patient> patientComboBox;
    @FXML private ComboBox<User> doctorComboBox;
    @FXML private DatePicker appointmentDatePicker;
    @FXML private Button checkAvailabilityButton;
    @FXML private ComboBox<LocalTime> timeComboBox;
    @FXML private TextArea reasonTextArea;
    @FXML private TextArea notesTextArea;
    @FXML private Button bookButton;
    @FXML private Button clearFormButton;
    @FXML private Label statusLabel;

    // My Appointments Tab
    @FXML private Button refreshAppointmentsButton;
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, Integer> appointmentIdColumn;
    @FXML private TableColumn<Appointment, String> patientNameColumn;
    @FXML private TableColumn<Appointment, String> doctorNameColumn;
    @FXML private TableColumn<Appointment, String> dateTimeColumn;
    @FXML private TableColumn<Appointment, String> reasonColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;
    @FXML private TableColumn<Appointment, Void> actionColumn;

    // All Appointments Tab
    @FXML private Tab allAppointmentsTab;
    @FXML private ComboBox<String> filterStatusComboBox;
    @FXML private DatePicker filterDatePicker;
    @FXML private Button filterButton;
    @FXML private Button refreshAllButton;
    @FXML private TableView<Appointment> allAppointmentsTable;
    @FXML private TableColumn<Appointment, Integer> allIdColumn;
    @FXML private TableColumn<Appointment, String> allPatientColumn;
    @FXML private TableColumn<Appointment, String> allDoctorColumn;
    @FXML private TableColumn<Appointment, String> allDateTimeColumn;
    @FXML private TableColumn<Appointment, String> allReasonColumn;
    @FXML private TableColumn<Appointment, String> allStatusColumn;
    @FXML private TableColumn<Appointment, Void> allActionColumn;

    private AppointmentDAO appointmentDAO;
    private PatientDAO patientDAO;
    private UserDAO userDAO;
    private ObservableList<Patient> patientList;
    private ObservableList<User> doctorList;
    private ObservableList<Appointment> myAppointments;
    private ObservableList<Appointment> allAppointmentsList;
    private User currentUser;

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        appointmentDAO = new AppointmentDAO();
        patientDAO = new PatientDAO();
        userDAO = new UserDAO();
        currentUser = SessionManager.getInstance().getCurrentUser();

        patientList = FXCollections.observableArrayList();
        doctorList = FXCollections.observableArrayList();
        myAppointments = FXCollections.observableArrayList();
        allAppointmentsList = FXCollections.observableArrayList();

        // Populate filter status ComboBox
        filterStatusComboBox.setItems(FXCollections.observableArrayList(
                "All", "Scheduled", "Completed", "Cancelled", "No Show"
        ));
        filterStatusComboBox.setValue("All");

        // Hide admin tab if not admin (this is safe in controller context)
        if (allAppointmentsTab != null && !currentUser.hasRole("Admin")) {
            allAppointmentsTab.setDisable(true);
            //allAppointmentsTab.setVisible(false);
            // Or remove the tab entirely if access to the TabPane:
            TabPane tabPane = (TabPane) allAppointmentsTab.getTabPane();
            if (tabPane != null) tabPane.getTabs().remove(allAppointmentsTab);
        }

        // Setup book appointment controls
        loadPatientsForBooking();
        loadDoctors();

        // Set up table columns
        setupMyAppointmentsTable();
        setupAllAppointmentsTable();

        // Load initial data
        loadMyAppointments();
        loadAllAppointments();

        // Setup event handlers
        appointmentDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null && doctorComboBox.getValue() != null) {
                loadAvailableTimeSlots();
            }
        });

        doctorComboBox.valueProperty().addListener((obs, oldDoctor, newDoctor) -> {
            if (newDoctor != null && appointmentDatePicker.getValue() != null) {
                loadAvailableTimeSlots();
            }
        });
    }

    private void loadPatientsForBooking() {
        refreshPatients();

        // Add "Add New Patient" option
        Patient newPatientOption = new Patient();
        newPatientOption.setPatientId(-1);
        newPatientOption.setFirstName("Add New");
        newPatientOption.setLastName("Patient...");
        patientList.add(0, newPatientOption);

        patientComboBox.setCellFactory(lv -> new ListCell<Patient>() {
            @Override
            protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                setText(empty ? "" : patient.getFirstName() + " " + patient.getLastName());
            }
        });
        patientComboBox.setButtonCell(new ListCell<Patient>() {
            @Override
            protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);
                setText(empty ? "" : patient.getFirstName() + " " + patient.getLastName());
            }
        });

        // Handle "Add New Patient" selection
        patientComboBox.valueProperty().addListener((obs, oldPatient, newPatient) -> {
            if (newPatient != null && newPatient.getPatientId() == -1) {
                // Open patient creation dialog
                openPatientCreationDialog();
            }
        });
    }

    protected void refreshPatients() {
        System.out.println("here ");
        patientList.clear();
        patientList.addAll(patientDAO.getAllPatients());
        patientComboBox.setItems(patientList);
    }

    private void openPatientCreationDialog() {
        try {
            // Create a simple dialog for new patient
            VBox dialogContent = new VBox(10);
            dialogContent.setStyle("-fx-padding: 20px;");

            TextField firstNameField = new TextField();
            firstNameField.setPromptText("First Name");
            TextField lastNameField = new TextField();
            lastNameField.setPromptText("Last Name");
            DatePicker dobPicker = new DatePicker();
            ComboBox<String> genderCombo = new ComboBox<>();
            genderCombo.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));

            Button createButton = new Button("Create Patient");
            createButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

            dialogContent.getChildren().addAll(
                    new Label("Create New Patient:"),
                    new Label("First Name:"), firstNameField,
                    new Label("Last Name:"), lastNameField,
                    new Label("Date of Birth:"), dobPicker,
                    new Label("Gender:"), genderCombo,
                    createButton
            );

            Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
            dialog.getDialogPane().setContent(dialogContent);
            dialog.setHeaderText("New Patient Registration");
            dialog.setTitle("Add Patient");

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.get() == ButtonType.OK && !firstNameField.getText().trim().isEmpty()) {
                Patient newPatient = new Patient();
                newPatient.setFirstName(firstNameField.getText().trim());
                newPatient.setLastName(lastNameField.getText().trim());
                newPatient.setDateOfBirth(dobPicker.getValue());
                newPatient.setGender(genderCombo.getValue());

                if (patientDAO.addPatient(newPatient)) {
                    // Reload patient list
                    loadPatientsForBooking();
                    showAlert("Success", "Patient created and added to booking", Alert.AlertType.INFORMATION);

                    // Auto-select the new patient
                    for (Patient patient : patientList) {
                        if (patient.getFirstName().equals(newPatient.getFirstName()) &&
                                patient.getLastName().equals(newPatient.getLastName())) {
                            patientComboBox.setValue(patient);
                            break;
                        }
                    }
                } else {
                    showAlert("Error", "Failed to create patient", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open patient creation dialog", Alert.AlertType.ERROR);
        }
    }


    private void loadDoctors() {
        refreshDoctors();
        doctorComboBox.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User doctor, boolean empty) {
                super.updateItem(doctor, empty);
                setText(empty ? "" : doctor.getFullName());
            }
        });
        doctorComboBox.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User doctor, boolean empty) {
                super.updateItem(doctor, empty);
                setText(empty ? "" : doctor.getFullName());
            }
        });
    }

    @Override public void refreshDoctors() {
        doctorList.clear();
        List<User> allUsers = userDAO.getActiveUsers();
        for (User user : allUsers) {
            if ("Doctor".equals(user.getRole())) {
                doctorList.add(user);
            }
        }
        doctorComboBox.setItems(doctorList);
    }


    @FXML
    private void checkAvailability() {
        loadAvailableTimeSlots();
    }

    private void loadAvailableTimeSlots() {
        User selectedDoctor = doctorComboBox.getValue();
        LocalDate selectedDate = appointmentDatePicker.getValue();

        if (selectedDoctor != null && selectedDate != null) {
            List<LocalTime> availableSlots = appointmentDAO.getAvailableTimeSlots(
                    selectedDoctor.getUserId(), selectedDate);

            ObservableList<LocalTime> timeList = FXCollections.observableArrayList(availableSlots);
            timeComboBox.setItems(timeList);
            timeComboBox.setCellFactory(lv -> new ListCell<LocalTime>() {
                @Override
                protected void updateItem(LocalTime time, boolean empty) {
                    super.updateItem(time, empty);
                    setText(empty ? "" : time.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")));
                }
            });
            timeComboBox.setButtonCell(new ListCell<LocalTime>() {
                @Override
                protected void updateItem(LocalTime time, boolean empty) {
                    super.updateItem(time, empty);
                    setText(empty ? "" : time != null ? time.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")) : "");
                }
            });

            if (!availableSlots.isEmpty()) {
                statusLabel.setStyle("-fx-text-fill: green;");
                statusLabel.setText("Available slots found: " + availableSlots.size());
            } else {
                statusLabel.setStyle("-fx-text-fill: orange;");
                statusLabel.setText("No available slots for selected date and doctor");
            }
        } else {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Please select both doctor and date");
        }
    }

    @FXML
    private void bookAppointment() {
        Patient selectedPatient = patientComboBox.getValue();
        User selectedDoctor = doctorComboBox.getValue();
        LocalDate date = appointmentDatePicker.getValue();
        LocalTime time = timeComboBox.getValue();
        String reason = reasonTextArea.getText().trim();

        if (selectedPatient == null || selectedDoctor == null || date == null || time == null || reason.isEmpty()) {
            showAlert("Validation Error", "Please fill all required fields", Alert.AlertType.WARNING);
            return;
        }

        Appointment appointment = new Appointment();
        appointment.setPatientId(selectedPatient.getPatientId());
        appointment.setDoctorId(selectedDoctor.getUserId());
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(Time.valueOf(time));
        appointment.setReason(reason);
        appointment.setCreatedBy(currentUser.getUserId());

        if (appointmentDAO.createAppointment(appointment)) {
            showAlert("Success", "Appointment booked successfully!\n\n" +
                            "Patient: " + selectedPatient.getFirstName() + " " + selectedPatient.getLastName() + "\n" +
                            "Doctor: " + selectedDoctor.getFullName() + "\n" +
                            "Date: " + date + "\n" +
                            "Time: " + time.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")),
                    Alert.AlertType.INFORMATION);

            clearForm();
            loadMyAppointments();
            loadAllAppointments();
        } else {
            showAlert("Error", "Failed to book appointment. The time slot might be taken.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void clearForm() {
        patientComboBox.setValue(null);
        doctorComboBox.setValue(null);
        appointmentDatePicker.setValue(null);
        timeComboBox.setItems(null);
        reasonTextArea.clear();
        notesTextArea.clear();
        statusLabel.setText("");
    }

    @FXML
    private void loadMyAppointments() {
        myAppointments.clear();

        if (currentUser.hasRole("Doctor")) {
            // Doctors see their own appointments
            myAppointments.addAll(appointmentDAO.getAppointmentsByDoctor(currentUser.getUserId()));
            statusLabel.setText("Your schedule: " + myAppointments.size() + " appointments");

        } else if (currentUser.hasRole("Admin") || currentUser.hasRole("Receptionist")) {
            // Admins/Receptionists see appointments they created
            List<Appointment> createdAppointments = new ArrayList<>();
            for (Appointment appointment : appointmentDAO.getAllAppointments()) {
                if (appointment.getCreatedBy() == currentUser.getUserId()) {
                    createdAppointments.add(appointment);
                }
            }
            myAppointments.addAll(createdAppointments);
            statusLabel.setText("Your bookings: " + myAppointments.size() + " appointments");

        } else {
            // Patients would see their own appointments (not implemented yet)
            statusLabel.setText("Please contact reception to view your appointments");
        }

        appointmentsTable.setItems(myAppointments);

        // Clear selection to avoid issues
        appointmentsTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void loadAllAppointments() {
        if (currentUser.hasRole("Admin")) {
            allAppointmentsList.clear();
            allAppointmentsList.addAll(appointmentDAO.getAllAppointments());
            allAppointmentsTable.setItems(allAppointmentsList);
            // Update status label if you have one for admin view
        } else {
            // Non-admins shouldn't see this tab, but just in case
            allAppointmentsTable.setItems(FXCollections.emptyObservableList());
        }
    }


    @FXML
    private void filterAppointments() {
        String statusFilter = filterStatusComboBox.getValue();
        LocalDate dateFilter = filterDatePicker.getValue();

        ObservableList<Appointment> filteredList = FXCollections.observableArrayList();

        for (Appointment appointment : allAppointmentsList) {
            boolean matches = true;

            // Filter by status
            if (!"All".equals(statusFilter)) {
                if (!statusFilter.equals(appointment.getStatus())) {
                    matches = false;
                }
            }

            // Filter by date
            if (dateFilter != null) {
                if (appointment.getAppointmentDate() == null ||
                        !appointment.getAppointmentDate().equals(dateFilter)) {
                    matches = false;
                }
            }

            if (matches) {
                filteredList.add(appointment);
            }
        }

        allAppointmentsTable.setItems(filteredList);
    }

    private void setupMyAppointmentsTable() {
        appointmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        patientNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPatientName()));
        doctorNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDoctorName()));
        dateTimeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDisplayDateTime()));
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        actionColumn.setCellFactory(col -> new TableCell<Appointment, Void>() {
            private final Button viewButton = new Button("View");
            private final Button cancelButton = new Button("Cancel");

            {
                viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                viewButton.setOnAction(e -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    // Implement view details
                    System.out.println("Viewing appointment: " + appointment.getAppointmentId());
                });

                cancelButton.setOnAction(e -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    // Show confirmation and cancel
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Cancel Appointment");
                    alert.setHeaderText("Cancel appointment for " + appointment.getPatientName() + "?");

                    if (alert.showAndWait().get() == ButtonType.OK) {
                        if (appointmentDAO.cancelAppointment(appointment.getAppointmentId(), "Cancelled by user")) {
                            loadMyAppointments();
                            showAlert("Success", "Appointment cancelled", Alert.AlertType.INFORMATION);
                        } else {
                            showAlert("Error", "Failed to cancel appointment", Alert.AlertType.ERROR);
                        }
                    }
                });

                setGraphic(viewButton);
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    // Show/hide buttons based on status
                    if ("Scheduled".equals(appointment.getStatus())) {
                        setGraphic(new HBox(5, viewButton, cancelButton));
                    } else {
                        setGraphic(viewButton);
                    }
                }
            }
        });
    }

    private void setupAllAppointmentsTable() {
        // Similar setup for all appointments table
        allIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        allPatientColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPatientName()));
        allDoctorColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDoctorName()));
        allDateTimeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDisplayDateTime()));
        allReasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        allStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        allActionColumn.setCellFactory(col -> new TableCell<Appointment, Void>() {
            private final Button completeButton = new Button("Complete");
            private final Button cancelButton = new Button("Cancel");
            private final Button viewButton = new Button("View");

            {
                completeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

                completeButton.setOnAction(e -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    if (appointmentDAO.updateAppointmentStatus(appointment.getAppointmentId(), "Completed")) {
                        loadAllAppointments();
                        showAlert("Success", "Appointment marked as completed", Alert.AlertType.INFORMATION);
                    }
                });

                cancelButton.setOnAction(e -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    if (appointmentDAO.updateAppointmentStatus(appointment.getAppointmentId(), "Cancelled")) {
                        loadAllAppointments();
                        showAlert("Success", "Appointment cancelled", Alert.AlertType.INFORMATION);
                    }
                });

                viewButton.setOnAction(e -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    // Show details
                    showAppointmentDetails(appointment);
                });

                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    HBox actions = new HBox(5);

                    if ("Scheduled".equals(appointment.getStatus())) {
                        actions.getChildren().addAll(completeButton, cancelButton, viewButton);
                    } else if ("Completed".equals(appointment.getStatus()) || "Cancelled".equals(appointment.getStatus())) {
                        actions.getChildren().add(viewButton);
                    }

                    setGraphic(actions);
                }
            }
        });
    }

    private void showAppointmentDetails(Appointment appointment) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Appointment Details");
        alert.setHeaderText("Appointment #" + appointment.getAppointmentId());

        String details = "Patient: " + appointment.getPatientName() + "\n" +
                "Doctor: " + appointment.getDoctorName() + "\n" +
                "Date & Time: " + appointment.getDisplayDateTime() + "\n" +
                "Status: " + appointment.getStatus() + "\n" +
                "Reason: " + appointment.getReason() + "\n" +
                "Notes: " + (appointment.getNotes() != null ? appointment.getNotes() : "None") + "\n" +
                "Created by: " + appointment.getCreator().getFullName() + "\n" +
                "Created: " + appointment.getCreatedDate();

        alert.setContentText(details);
        alert.showAndWait();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

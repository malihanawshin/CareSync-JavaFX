package com.example.healthcareapp.model;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Appointment {
    private int appointmentId;
    private int patientId;
    private int doctorId;
    private LocalDate appointmentDate;
    private Time appointmentTime;
    private String reason;
    private String status;
    private String notes;
    private int createdBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // Helper objects for display
    private Patient patient;
    private User doctor;
    private User creator;

    // Constructors
    public Appointment() {}

    public Appointment(int patientId, int doctorId, LocalDate appointmentDate,
                       Time appointmentTime, String reason) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        this.status = "Scheduled";
        this.notes = "";
    }

    // Getters and Setters
    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public Time getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(Time appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    // Helper getters
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public User getDoctor() { return doctor; }
    public void setDoctor(User doctor) { this.doctor = doctor; }

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }

    // Utility methods
    public String getDisplayDateTime() {
        return (appointmentDate != null ? appointmentDate.toString() : "") +
                " " +
                (appointmentTime != null ? appointmentTime.toString() : "");
    }

    public String getPatientName() {
        return patient != null ?
                patient.getFirstName() + " " + patient.getLastName() : "Unknown";
    }

    public String getDoctorName() {
        return doctor != null ? doctor.getFullName() : "Unknown Doctor";
    }

    @Override
    public String toString() {
        return getPatientName() + " - " + getDisplayDateTime() + " (" + status + ")";
    }
}

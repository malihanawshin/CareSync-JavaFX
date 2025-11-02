package com.example.healthcareapp.dao;

import com.example.healthcareapp.model.Appointment;
import com.example.healthcareapp.model.Patient;
import com.example.healthcareapp.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    // Create new appointment
    public boolean createAppointment(Appointment appointment) {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, reason, status, notes, created_by) " +
                "VALUES (?, ?, ?, ?, ?, 'Scheduled', ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, appointment.getPatientId());
            pstmt.setInt(2, appointment.getDoctorId());
            pstmt.setDate(3, java.sql.Date.valueOf(appointment.getAppointmentDate()));
            pstmt.setTime(4, appointment.getAppointmentTime());
            pstmt.setString(5, appointment.getReason());
            pstmt.setString(6, appointment.getNotes());
            pstmt.setInt(7, appointment.getCreatedBy());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Get generated appointment ID
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    appointment.setAppointmentId(generatedKeys.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error creating appointment: " + e.getMessage());
            // Check if it was a duplicate key constraint
            if (e.getSQLState().equals("23505")) {
                System.err.println("Duplicate appointment slot - doctor already booked at this time");
            }
        }

        return false;
    }

    // Get all appointments
    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, " +
                "p.first_name as p_first, p.last_name as p_last, " +
                "d.full_name as d_name, d.email as d_email, " +
                "u.full_name as creator_name " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN users d ON a.doctor_id = d.user_id " +
                "JOIN users u ON a.created_by = u.user_id " +
                "ORDER BY a.appointment_date, a.appointment_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Appointment appointment = extractAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return appointments;
    }

    // Get appointments by doctor ID (for doctor's schedule)
    public List<Appointment> getAppointmentsByDoctor(int doctorId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, " +
                "p.first_name as p_first, p.last_name as p_last, " +
                "d.full_name as d_name, d.email as d_email, " +
                "u.full_name as creator_name " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN users d ON a.doctor_id = d.user_id " +
                "JOIN users u ON a.created_by = u.user_id " +
                "WHERE a.doctor_id = ? " +
                "ORDER BY a.appointment_date, a.appointment_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, doctorId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Appointment appointment = extractAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return appointments;
    }

    // Get appointments for a specific date
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, " +
                "p.first_name as p_first, p.last_name as p_last, " +
                "d.full_name as d_name, d.email as d_email, " +
                "u.full_name as creator_name " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "JOIN users d ON a.doctor_id = d.user_id " +
                "JOIN users u ON a.created_by = u.user_id " +
                "WHERE a.appointment_date = ? " +
                "ORDER BY a.appointment_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Appointment appointment = extractAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return appointments;
    }

    // Get available time slots for a doctor on a specific date
    public List<LocalTime> getAvailableTimeSlots(int doctorId, LocalDate date) {
        List<LocalTime> availableSlots = new ArrayList<>();

        // Clinic working hours: 9 AM to 6 PM, 15-minute intervals
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(18, 0);
        LocalTime slotDuration = LocalTime.of(0, 15); // 15 minutes

        // Generate all possible slots
        LocalTime currentTime = startTime;
        while (currentTime.isBefore(endTime)) {
            availableSlots.add(currentTime);
            currentTime = currentTime.plusMinutes(15);
        }

        // Get booked slots
        String sql = "SELECT appointment_time FROM appointments WHERE doctor_id = ? AND appointment_date = ? AND status != 'Cancelled'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, doctorId);
            pstmt.setDate(2, java.sql.Date.valueOf(date));

            ResultSet rs = pstmt.executeQuery();
            List<Time> bookedSlots = new ArrayList<>();

            while (rs.next()) {
                bookedSlots.add(rs.getTime("appointment_time"));
            }

            // Remove booked slots
            availableSlots.removeIf(slot -> {
                for (Time booked : bookedSlots) {
                    if (slot.equals(Time.valueOf(booked.toLocalTime()))) {
                        return true;
                    }
                }
                return false;
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return availableSlots;
    }

    // Update appointment status
    public boolean updateAppointmentStatus(int appointmentId, String status) {
        String sql = "UPDATE appointments SET status = ?, updated_date = NOW() WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, appointmentId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cancel appointment
    public boolean cancelAppointment(int appointmentId, String cancelReason) {
        String sql = "UPDATE appointments SET status = 'Cancelled', notes = CONCAT(IFNULL(notes, ''), ?), updated_date = NOW() WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "\n[CANCELLED: " + cancelReason + "]");
            pstmt.setInt(2, appointmentId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete appointment
    public boolean deleteAppointment(int appointmentId) {
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appointmentId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to extract appointment from ResultSet
    private Appointment extractAppointmentFromResultSet(ResultSet rs) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(rs.getInt("appointment_id"));
        appointment.setPatientId(rs.getInt("patient_id"));
        appointment.setDoctorId(rs.getInt("doctor_id"));

        Date date = rs.getDate("appointment_date");
        appointment.setAppointmentDate(date != null ? date.toLocalDate() : null);

        Time time = rs.getTime("appointment_time");
        appointment.setAppointmentTime(time);

        appointment.setReason(rs.getString("reason"));
        appointment.setStatus(rs.getString("status"));
        appointment.setNotes(rs.getString("notes"));
        appointment.setCreatedBy(rs.getInt("created_by"));

        Timestamp created = rs.getTimestamp("created_date");
        appointment.setCreatedDate(created != null ? created.toLocalDateTime() : null);

        Timestamp updated = rs.getTimestamp("updated_date");
        appointment.setUpdatedDate(updated != null ? updated.toLocalDateTime() : null);

        // Set helper objects
        Patient patient = new Patient();
        patient.setFirstName(rs.getString("p_first"));
        patient.setLastName(rs.getString("p_last"));
        appointment.setPatient(patient);

        User doctor = new User();
        doctor.setFullName(rs.getString("d_name"));
        doctor.setEmail(rs.getString("d_email"));
        appointment.setDoctor(doctor);

        User creator = new User();
        creator.setFullName(rs.getString("creator_name"));
        appointment.setCreator(creator);

        return appointment;
    }
}

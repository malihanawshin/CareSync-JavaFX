package com.example.healthcareapp.dao;

import com.example.healthcareapp.model.AnalyticsReport;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportingService {

    // Generate comprehensive analytics report
    public AnalyticsReport generateAnalyticsReport(LocalDate startDate, LocalDate endDate) {
        AnalyticsReport report = new AnalyticsReport();
        report.setReportType("Comprehensive Analytics");
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get patient demographics
            reportPatientDemographics(conn, report);

            // Get appointment statistics
            reportAppointmentStats(conn, report, startDate, endDate);

            // Get doctor performance (last 6 months)
            reportDoctorPerformance(conn, report);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return report;
    }

    private void reportPatientDemographics(Connection conn, AnalyticsReport report) throws SQLException {
        String sql = "SELECT COUNT(*) as total, AVG(TIMESTAMPDIFF(YEAR, date_of_birth, CURDATE())) as avg_age FROM patients";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                report.setTotalPatients(rs.getInt("total"));
                report.setAvgPatientAge(rs.getDouble("avg_age"));
            }
        }

        // Gender distribution
        String genderSql = "SELECT gender, COUNT(*) as count FROM patients GROUP BY gender";
        try (PreparedStatement pstmt = conn.prepareStatement(genderSql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                report.updateGenderDistribution(rs.getString("gender"), rs.getInt("count"));
            }
        }
    }

    private void reportAppointmentStats(Connection conn, AnalyticsReport report, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT COUNT(*) as total, " +
                "SUM(CASE WHEN status = 'Completed' THEN 1 ELSE 0 END) as completed, " +
                "COUNT(DISTINCT patient_id) as unique_patients " +
                "FROM appointments " +
                "WHERE appointment_date BETWEEN ? AND ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                report.setTotalAppointments(rs.getInt("total"));
                report.setCompletedAppointments(rs.getInt("completed"));
                double rate = rs.getInt("total") > 0 ?
                        (double) rs.getInt("completed") / rs.getInt("total") * 100 : 0;
                report.setCompletionRate(rate);
            }
        }

        // Status distribution
        String statusSql = "SELECT status, COUNT(*) as count " +
                "FROM appointments " +
                "WHERE appointment_date BETWEEN ? AND ? " +
                "GROUP BY status";
        try (PreparedStatement pstmt = conn.prepareStatement(statusSql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                report.updateStatusDistribution(rs.getString("status"), rs.getInt("count"));
            }
        }
    }

    private void reportDoctorPerformance(Connection conn, AnalyticsReport report) throws SQLException {
        String sql = "SELECT COUNT(a.appointment_id) as total_doctor_appts, " +
                "AVG(doctor_completion_rate) as avg_doctor_rate " +
                "FROM (SELECT u.user_id, " +
                "      ROUND(COUNT(CASE WHEN a.status = 'Completed' THEN 1 END) / " +
                "      NULLIF(COUNT(a.appointment_id), 0) * 100, 2) as doctor_completion_rate " +
                "      FROM users u " +
                "      LEFT JOIN appointments a ON u.user_id = a.doctor_id " +
                "      WHERE u.role = 'Doctor' AND u.is_active = TRUE " +
                "      GROUP BY u.user_id) doctor_stats";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // You could store this as additional metrics
                System.out.println("Average Doctor Completion Rate: " + rs.getDouble("avg_doctor_rate") + "%");
                System.out.println("Total Doctor Appointments: " + rs.getInt("total_doctor_appts"));
            }
        }
    }

    // Generate specific report types
    public List<AnalyticsReport> generatePatientAgeReport(int ageGroups) {
        List<AnalyticsReport> reports = new ArrayList<>();
        String sql = "SELECT " +
                "FLOOR(TIMESTAMPDIFF(YEAR, date_of_birth, CURDATE()) / ?) * ? as age_group, " +
                "COUNT(*) as count, " +
                "gender " +
                "FROM patients " +
                "GROUP BY age_group, gender " +
                "ORDER BY age_group";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ageGroups);
            pstmt.setInt(2, ageGroups);
            ResultSet rs = pstmt.executeQuery();

            AnalyticsReport currentReport = null;
            while (rs.next()) {
                int ageGroup = rs.getInt("age_group");
                String gender = rs.getString("gender");
                int count = rs.getInt("count");

                if (currentReport == null || currentReport.getReportType().isEmpty()) {
                    currentReport = new AnalyticsReport();
                    currentReport.setReportType("Patient Age Distribution (" + ageGroups + " year groups)");
                    reports.add(currentReport);
                }

                currentReport.updateGenderDistribution(
                        ageGroup + "s (" + gender + ")", count);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reports;
    }

    // Monthly appointment trend
    public List<AnalyticsReport> generateMonthlyAppointmentTrend() {
        List<AnalyticsReport> reports = new ArrayList<>();
        String sql = "SELECT " +
                "YEAR(appointment_date) as year, " +
                "MONTH(appointment_date) as month, " +
                "COUNT(*) as appointments, " +
                "SUM(CASE WHEN status = 'Completed' THEN 1 ELSE 0 END) as completed " +
                "FROM appointments " +
                "WHERE appointment_date >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
                "GROUP BY YEAR(appointment_date), MONTH(appointment_date) " +
                "ORDER BY year DESC, month DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            AnalyticsReport report = new AnalyticsReport();
            report.setReportType("Monthly Appointment Trends (Last 12 Months)");

            while (rs.next()) {
                String monthKey = rs.getInt("year") + "-" + String.format("%02d", rs.getInt("month"));
                int total = rs.getInt("appointments");
                int completed = rs.getInt("completed");

                report.updateStatusDistribution(monthKey + " Total", total);
                report.updateStatusDistribution(monthKey + " Completed", completed);
            }

            reports.add(report);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reports;
    }
}

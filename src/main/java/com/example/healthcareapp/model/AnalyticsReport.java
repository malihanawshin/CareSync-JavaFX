package com.example.healthcareapp.model;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class AnalyticsReport {
    private final StringProperty reportType;
    private final ObjectProperty<LocalDate> startDate;
    private final ObjectProperty<LocalDate> endDate;
    private final IntegerProperty totalPatients;
    private final DoubleProperty avgPatientAge;
    private final Map<String, Integer> genderDistribution;
    private final IntegerProperty totalAppointments;
    private final IntegerProperty completedAppointments;
    private final DoubleProperty completionRate;
    private final Map<String, Integer> statusDistribution;

    public AnalyticsReport() {
        this.reportType = new SimpleStringProperty();
        this.startDate = new SimpleObjectProperty<>();
        this.endDate = new SimpleObjectProperty<>();
        this.totalPatients = new SimpleIntegerProperty();
        this.avgPatientAge = new SimpleDoubleProperty();
        this.genderDistribution = new HashMap<>();
        this.totalAppointments = new SimpleIntegerProperty();
        this.completedAppointments = new SimpleIntegerProperty();
        this.completionRate = new SimpleDoubleProperty();
        this.statusDistribution = new HashMap<>();
    }

    // Getters
    public String getReportType() { return reportType.get(); }
    public void setReportType(String value) { reportType.set(value); }

    public LocalDate getStartDate() { return startDate.get(); }
    public void setStartDate(LocalDate value) { startDate.set(value); }

    public LocalDate getEndDate() { return endDate.get(); }
    public void setEndDate(LocalDate value) { endDate.set(value); }

    public int getTotalPatients() { return totalPatients.get(); }
    public void setTotalPatients(int value) { totalPatients.set(value); }

    public double getAvgPatientAge() { return avgPatientAge.get(); }
    public void setAvgPatientAge(double value) { avgPatientAge.set(value); }

    public Map<String, Integer> getGenderDistribution() { return genderDistribution; }

    public int getTotalAppointments() { return totalAppointments.get(); }
    public void setTotalAppointments(int value) { totalAppointments.set(value); }

    public int getCompletedAppointments() { return completedAppointments.get(); }
    public void setCompletedAppointments(int value) { completedAppointments.set(value); }

    public double getCompletionRate() { return completionRate.get(); }
    public void setCompletionRate(double value) { completionRate.set(value); }

    public Map<String, Integer> getStatusDistribution() { return statusDistribution; }

    // Utility methods
    public void updateGenderDistribution(String gender, int count) {
        genderDistribution.put(gender, count);
    }

    public void updateStatusDistribution(String status, int count) {
        statusDistribution.put(status, count);
    }

    public double getGenderPercentage(String gender) {
        int total = genderDistribution.values().stream().mapToInt(Integer::intValue).sum();
        return total > 0 ? (double) genderDistribution.getOrDefault(gender, 0) / total * 100 : 0;
    }

    public double getStatusPercentage(String status) {
        int total = statusDistribution.values().stream().mapToInt(Integer::intValue).sum();
        return total > 0 ? (double) statusDistribution.getOrDefault(status, 0) / total * 100 : 0;
    }

    @Override
    public String toString() {
        return String.format("Report: %s (%s to %s) | Patients: %d | Avg Age: %.1f | Appointments: %d (%.1f%%)",
                reportType.get(), startDate.get(), endDate.get(), totalPatients.get(),
                avgPatientAge.get(), totalAppointments.get(), completionRate.get());
    }
}

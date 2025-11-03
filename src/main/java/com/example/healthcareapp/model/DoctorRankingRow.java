package com.example.healthcareapp.model;

import javafx.beans.property.*;

public class DoctorRankingRow {
    private final SimpleStringProperty doctorName;
    private final SimpleIntegerProperty doctorId;
    private final SimpleDoubleProperty completionRate;
    private final SimpleIntegerProperty patientCount;
    private final SimpleIntegerProperty appointmentCount;
    private final SimpleIntegerProperty rankingPosition;

    public DoctorRankingRow(int doctorId, String doctorName, double completionRate,
                            int patientCount, int appointmentCount, int rankingPosition) {
        this.doctorId = new SimpleIntegerProperty(doctorId);
        this.doctorName = new SimpleStringProperty(doctorName);
        this.completionRate = new SimpleDoubleProperty(completionRate);
        this.patientCount = new SimpleIntegerProperty(patientCount);
        this.appointmentCount = new SimpleIntegerProperty(appointmentCount);
        this.rankingPosition = new SimpleIntegerProperty(rankingPosition);
    }

    // Getters
    public String getDoctorName() { return doctorName.get(); }
    public SimpleStringProperty doctorNameProperty() { return doctorName; }

    public int getDoctorId() { return doctorId.get(); }
    public SimpleIntegerProperty doctorIdProperty() { return doctorId; }

    public double getCompletionRate() { return completionRate.get(); }
    public SimpleDoubleProperty completionRateProperty() { return completionRate; }

    public int getPatientCount() { return patientCount.get(); }
    public SimpleIntegerProperty patientCountProperty() { return patientCount; }

    public int getAppointmentCount() { return appointmentCount.get(); }
    public SimpleIntegerProperty appointmentCountProperty() { return appointmentCount; }

    public int getRankingPosition() { return rankingPosition.get(); }
    public SimpleIntegerProperty rankingPositionProperty() { return rankingPosition; }

    // Static factory methods for database data
    public static DoctorRankingRow createFromDatabase(int doctorId, String doctorName,
                                                      double completionRate, int patientCount,
                                                      int appointmentCount) {
        // Calculate ranking based on completion rate and appointment volume
        int ranking = calculateRanking(completionRate, appointmentCount);
        return new DoctorRankingRow(doctorId, doctorName, completionRate, patientCount,
                appointmentCount, ranking);
    }

    private static int calculateRanking(double completionRate, int appointmentCount) {
        // Simple ranking algorithm: prioritize completion rate, then volume
        int rank = (int)(completionRate * 10); // 0-1000 base score
        rank += appointmentCount / 10; // Add volume bonus
        return Math.min(100, rank); // Cap at 100
    }

    @Override
    public String toString() {
        return String.format("%s: %.1f%% completion, %d patients, %d appointments",
                doctorName.get(), completionRate.get(), patientCount.get(), appointmentCount.get());
    }

    // Compare for sorting
    public int compareTo(DoctorRankingRow other) {
        // Sort by completion rate descending, then by appointment count descending
        int rateCompare = Double.compare(other.getCompletionRate(), this.getCompletionRate());
        if (rateCompare != 0) {
            return rateCompare;
        }
        return Integer.compare(other.getAppointmentCount(), this.getAppointmentCount());
    }
}

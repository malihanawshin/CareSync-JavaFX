package com.example.healthcareapp.model;

import javafx.beans.property.*;

public class SimpleDemographicsRow {
    private final SimpleStringProperty category;
    private final SimpleIntegerProperty count;
    private final SimpleDoubleProperty percentage;

    public SimpleDemographicsRow(String category, int count, double percentage) {
        this.category = new SimpleStringProperty(category);
        this.count = new SimpleIntegerProperty(count);
        this.percentage = new SimpleDoubleProperty(percentage);
    }

    // Getters
    public String getCategory() { return category.get(); }
    public SimpleStringProperty categoryProperty() { return category; }

    public int getCount() { return count.get(); }
    public SimpleIntegerProperty countProperty() { return count; }

    public double getPercentage() { return percentage.get(); }
    public SimpleDoubleProperty percentageProperty() { return percentage; }

    // Static factory methods
    public static SimpleDemographicsRow createFromMap(String category, int totalPatients,
                                                      java.util.Map<String, Integer> distribution) {
        int count = distribution.getOrDefault(category, 0);
        double percentage = totalPatients > 0 ? (double) count / totalPatients * 100 : 0;

        return new SimpleDemographicsRow(category, count, percentage);
    }

    @Override
    public String toString() {
        return String.format("%s: %d (%.1f%%)", category.get(), count.get(), percentage.get());
    }
}

package com.example.healthcareapp.model;

import javafx.beans.property.*;

public class DemographicsRow {
    private final SimpleStringProperty ageGroup;
    private final SimpleIntegerProperty maleCount;
    private final SimpleIntegerProperty femaleCount;
    private final SimpleIntegerProperty otherCount;
    private final SimpleIntegerProperty total;

    public DemographicsRow(String ageGroup, int male, int female, int other, int total) {
        this.ageGroup = new SimpleStringProperty(ageGroup);
        this.maleCount = new SimpleIntegerProperty(male);
        this.femaleCount = new SimpleIntegerProperty(female);
        this.otherCount = new SimpleIntegerProperty(other);
        this.total = new SimpleIntegerProperty(total);
    }

    // Getters
    public String getAgeGroup() { return ageGroup.get(); }
    public SimpleStringProperty ageGroupProperty() { return ageGroup; }

    public int getMaleCount() { return maleCount.get(); }
    public SimpleIntegerProperty maleCountProperty() { return maleCount; }

    public int getFemaleCount() { return femaleCount.get(); }
    public SimpleIntegerProperty femaleCountProperty() { return femaleCount; }

    public int getOtherCount() { return otherCount.get(); }
    public SimpleIntegerProperty otherCountProperty() { return otherCount; }

    public int getTotal() { return total.get(); }
    public SimpleIntegerProperty totalProperty() { return total; }

    // Static factory methods for easy creation
    public static DemographicsRow createFromMap(String ageGroup, java.util.Map<String, Integer> genderData) {
        int male = genderData.getOrDefault("Male", 0);
        int female = genderData.getOrDefault("Female", 0);
        int other = genderData.getOrDefault("Other", 0);
        int total = male + female + other;

        return new DemographicsRow(ageGroup, male, female, other, total);
    }

    @Override
    public String toString() {
        return String.format("%s: M:%d F:%d O:%d Total:%d",
                ageGroup.get(), maleCount.get(), femaleCount.get(),
                otherCount.get(), total.get());
    }
}

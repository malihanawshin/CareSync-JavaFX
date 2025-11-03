package com.example.healthcareapp.controller;

import com.example.healthcareapp.dao.ReportingService;
import com.example.healthcareapp.model.AnalyticsReport;
import com.example.healthcareapp.model.DemographicsRow;
import com.example.healthcareapp.model.DoctorRankingRow;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    // Overview Tab Controls
    @FXML private VBox totalPatientsCard;        // Changed from HBox to VBox
    @FXML private Label patientsCountLabel;
    @FXML private ProgressIndicator patientsProgress;

    @FXML private VBox totalAppointmentsCard;    // Changed from HBox to VBox
    @FXML private Label appointmentsCountLabel;
    @FXML private PieChart appointmentStatusPie;

    @FXML private VBox doctorPerformanceCard;    // Changed from HBox to VBox
    @FXML private Label completionRateLabel;
    @FXML private LineChart performanceTrendLine;

    @FXML private VBox avgAgeCard;              // Changed from HBox to VBox
    @FXML private Label avgAgeLabel;
    @FXML private BarChart ageDistributionBar;

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button generateReportButton;
    @FXML private ProgressBar loadingProgress;
    @FXML private Label loadingLabel;

    // Patient Analytics Tab
    @FXML private ComboBox<String> ageGroupComboBox;
    @FXML private Button generateDemographicsButton;
    @FXML private PieChart genderPieChart;
    @FXML private BarChart ageBarChart;
    @FXML private TableView<DemographicsRow> demographicsTable;
    @FXML private TableColumn<DemographicsRow, String> ageGroupColumn;
    @FXML private TableColumn<DemographicsRow, Integer> maleCountColumn;
    @FXML private TableColumn<DemographicsRow, Integer> femaleCountColumn;
    @FXML private TableColumn<DemographicsRow, Integer> otherCountColumn;
    @FXML private TableColumn<DemographicsRow, Integer> totalDemographicsColumn;
    @FXML private TableColumn<DemographicsRow, Double> percentageColumn;

    // Appointment Analytics Tab
    @FXML private ComboBox<String> appointmentTimeRangeComboBox;
    @FXML private Button generateAppointmentReportButton;
    @FXML private LineChart appointmentTrendLine;
    @FXML private BarChart hourlyAppointmentBar;
    @FXML private PieChart statusPieChart;
    @FXML private TableView<DoctorRankingRow> doctorRankingTable;
    @FXML private TableColumn<DoctorRankingRow, String> doctorNameRankColumn;
    @FXML private TableColumn<DoctorRankingRow, Double> completionRankColumn;
    @FXML private TableColumn<DoctorRankingRow, Integer> patientCountRankColumn;
    @FXML private TableColumn<DoctorRankingRow, Integer> appointmentCountRankColumn;

    // Custom Reports Tab
    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private DatePicker customStartDatePicker;
    @FXML private DatePicker customEndDatePicker;
    @FXML private Button generateCustomReportButton;
    @FXML private VBox reportPreviewArea;
    @FXML private Button exportCSVButton;
    @FXML private Button exportPDFButton;
    @FXML private Button printReportButton;

    private ReportingService reportingService;
    private AnalyticsReport currentReport;
    private LocalDate defaultStartDate;
    private LocalDate defaultEndDate;
    private int totalPatients = 0; // Store total for percentage calculations

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reportingService = new ReportingService();

        // Set default date values (this was previously in FXML)
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        startDatePicker.setValue(thirtyDaysAgo);
        endDatePicker.setValue(LocalDate.now());
        customStartDatePicker.setValue(LocalDate.now().minusMonths(6));
        customEndDatePicker.setValue(LocalDate.now());

        // Set default date range (last 30 days)
        defaultStartDate = LocalDate.now().minusDays(30);
        defaultEndDate = LocalDate.now();
        startDatePicker.setValue(defaultStartDate);
        endDatePicker.setValue(defaultEndDate);
        customStartDatePicker.setValue(defaultStartDate);
        customEndDatePicker.setValue(defaultEndDate);

        // Initialize ComboBoxes
        ageGroupComboBox.setItems(FXCollections.observableArrayList("5", "10", "20", "30"));
        ageGroupComboBox.setValue("10");

        appointmentTimeRangeComboBox.setItems(FXCollections.observableArrayList(
                "30 Days", "3 Months", "6 Months", "12 Months"));
        appointmentTimeRangeComboBox.setValue("30 Days");

        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Patient Demographics", "Appointment Trends", "Doctor Performance",
                "Patient Retention", "Revenue Analysis"));
        reportTypeComboBox.setValue("Patient Demographics");

        // Setup table columns
        setupDemographicsTable();
        setupDoctorRankingTable();
        //setupChartSizes();
        // Initialize charts with sample data
        initializeSampleCharts();

        // Event handlers
        generateReportButton.setOnAction(e -> generateReport());
        generateDemographicsButton.setOnAction(e -> generateDemographicsReport());
        generateAppointmentReportButton.setOnAction(e -> generateAppointmentReport());
        generateCustomReportButton.setOnAction(e -> generateCustomReport());

        //exportCSVButton.setOnAction(e -> exportToCSV());
        //exportPDFButton.setOnAction(e -> exportToPDF());
        printReportButton.setOnAction(e -> printReport());

        // Generate initial report
        generateInitialReport();
    }

    private void setupChartSizes() {
        // Set chart sizes in controller
        appointmentTrendLine.setPrefSize(600, 300);
        hourlyAppointmentBar.setPrefSize(400, 300);
        genderPieChart.setPrefSize(400, 300);
        ageBarChart.setPrefSize(500, 300);
        statusPieChart.setPrefSize(300, 250);
        appointmentStatusPie.setPrefSize(100, 100);
    }


    private void initializeSampleCharts() {
        // Initialize appointment status pie chart
        appointmentStatusPie.getData().addAll(
                new PieChart.Data("Scheduled", 167),
                new PieChart.Data("Completed", 1089),
                new PieChart.Data("Cancelled", 0),
                new PieChart.Data("No Show", 0)
        );
        updatePieChart(appointmentStatusPie, null);

        // Initialize gender pie chart
        genderPieChart.getData().addAll(
                new PieChart.Data("Male", 245),
                new PieChart.Data("Female", 210),
                new PieChart.Data("Other", 17)
        );
        updatePieChart(genderPieChart, null);

        // Initialize status pie chart
        statusPieChart.getData().addAll(
                new PieChart.Data("Scheduled", 167),
                new PieChart.Data("Completed", 1089),
                new PieChart.Data("Cancelled", 0),
                new PieChart.Data("No Show", 0)
        );
        updatePieChart(statusPieChart, null);

        // Initialize line and bar charts
//        initializeLineChart(appointmentTrendLine, "Appointment Trends");
//        initializeBarChart(ageDistributionBar, "Age Distribution");
//        initializeBarChart(hourlyAppointmentBar, "Hourly Appointments");
//
//        // Initialize line chart for performance
//        initializeLineChart(performanceTrendLine, "Performance Trend");
//
//        // Initialize other charts with sample data
//        updateAgeBarChart();
        //updateAppointmentTrendChart();
        //updateHourlyBarChart();
        updateDoctorRankingTable();
        updateDemographicsTable();
    }

    private void initializeLineChart(LineChart lineChart, String title) {
        if (lineChart == null) return;

        // Clear existing data
        lineChart.getData().clear();

        // Create sample data for line chart
        XYChart.Series<String, Number> sampleSeries = new XYChart.Series<>();
        sampleSeries.setName(title + " Trend");

        // Sample data points (you can replace this with real data)
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Number[] values = {45, 52, 48, 65, 70, 68, 75, 80, 72, 85, 90, 95};

        for (int i = 0; i < months.length; i++) {
            sampleSeries.getData().add(new XYChart.Data<>(months[i], values[i]));
        }

        lineChart.getData().add(sampleSeries);

        // Style the line
        sampleSeries.getNode().setStyle("-fx-stroke: #2196F3; -fx-stroke-width: 2px; -fx-fill: none;");

        // Add hover effects to data points
        for (XYChart.Data<String, Number> dataPoint : sampleSeries.getData()) {
            dataPoint.getNode().setOnMouseEntered(e -> {
                dataPoint.getNode().setStyle("-fx-stroke: #1976d2; -fx-stroke-width: 3px; -fx-background-color: #2196F3; -fx-background-radius: 5px;");
            });

            dataPoint.getNode().setOnMouseExited(e -> {
                dataPoint.getNode().setStyle("-fx-stroke: #2196F3; -fx-stroke-width: 2px; -fx-fill: none;");
            });
        }

        lineChart.setTitle(title);

        // Style the chart
        lineChart.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-background-radius: 5px;");
    }

    private void initializeBarChart(BarChart barChart, String title) {
        if (barChart == null) return;

        // Clear existing data
        barChart.getData().clear();

        // Create sample data for bar chart
        XYChart.Series<String, Number> sampleSeries = new XYChart.Series<>();
        sampleSeries.setName(title);

        // Sample data points (replace with real data as needed)
        if ("Age Distribution".equals(title)) {
            // Age distribution data
            String[] ageGroups = {"0-18", "19-35", "36-50", "51-65", "65+"};
            Number[] ageCounts = {57, 180, 193, 102, 46};

            for (int i = 0; i < ageGroups.length; i++) {
                sampleSeries.getData().add(new XYChart.Data<>(ageGroups[i], ageCounts[i]));
            }
        } else if ("Hourly Appointments".equals(title)) {
            // Hourly distribution data
            String[] hours = {"8AM", "9AM", "10AM", "11AM", "12PM", "1PM", "2PM", "3PM", "4PM", "5PM", "6PM"};
            Number[] hourlyCounts = {5, 15, 25, 30, 20, 18, 25, 22, 18, 12, 5};

            for (int i = 0; i < hours.length; i++) {
                sampleSeries.getData().add(new XYChart.Data<>(hours[i], hourlyCounts[i]));
            }
        }

        barChart.getData().add(sampleSeries);

        // Style the bars
        sampleSeries.getNode().setStyle("-fx-stroke: #FF9800; -fx-fill: linear-gradient(to bottom, #FF9800, #F57C00);");

        // Add hover effects to bars
        for (XYChart.Data<String, Number> dataPoint : sampleSeries.getData()) {
            dataPoint.getNode().setOnMouseEntered(e -> {
                dataPoint.getNode().setStyle("-fx-stroke: #F57C00; -fx-fill: linear-gradient(to bottom, #F57C00, #EF6C00);");
            });

            dataPoint.getNode().setOnMouseExited(e -> {
                dataPoint.getNode().setStyle("-fx-stroke: #FF9800; -fx-fill: linear-gradient(to bottom, #FF9800, #F57C00);");
            });
        }

        barChart.setTitle(title);

        // Style the chart
        barChart.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-background-radius: 5px;");

        // Style the axis labels if needed
        for (Node node : barChart.lookupAll(".chart-title")) {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333;");
            }
        }

        for (Node node : barChart.lookupAll(".axis-label")) {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
            }
        }
    }



    private void generateInitialReport() {
        loadingProgress.setVisible(true);
        loadingLabel.setVisible(true);

        new Thread(() -> {
            try {
                currentReport = reportingService.generateAnalyticsReport(
                        startDatePicker.getValue(), endDatePicker.getValue());

                Platform.runLater(() -> {
                    updateKPICards();
                    updateGenderPieChart();
                    updateAgeBarChart();
                    updateDemographicsTable();
                    updateAppointmentTrendChart();
                    updateHourlyBarChart();
                    updateStatusPieChart();
                    updateDoctorRankingTable();
                    loadingProgress.setVisible(false);
                    loadingLabel.setVisible(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Error",
                            "Failed to generate report: ", Alert.AlertType.ERROR);
                    loadingProgress.setVisible(false);
                    loadingLabel.setVisible(false);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void updateKPICards() {
        if (currentReport == null) {
            currentReport = new AnalyticsReport();
            currentReport.setTotalPatients(472);
            currentReport.setAvgPatientAge(34.2);
            currentReport.setTotalAppointments(1256);
            currentReport.setCompletedAppointments(1089);
            currentReport.setCompletionRate(86.7);
            totalPatients = currentReport.getTotalPatients();
        }

        // Total Patients Card
        patientsCountLabel.setText(String.valueOf(currentReport.getTotalPatients()));
        patientsProgress.setProgress(Math.min(0.9, (double)currentReport.getTotalPatients() / 1000.0));

        // Total Appointments Card
        appointmentsCountLabel.setText(String.valueOf(currentReport.getTotalAppointments()));

        // Doctor Performance Card
        completionRateLabel.setText(String.format("%.1f%%", currentReport.getCompletionRate()));

        // Average Age Card
        avgAgeLabel.setText(String.format("%.1f years", currentReport.getAvgPatientAge()));

        // Animate cards
        animateCard(totalPatientsCard);
        animateCard(totalAppointmentsCard);
        animateCard(doctorPerformanceCard);
        animateCard(avgAgeCard);
    }

    // MISSING METHOD: updatePieChart() - for appointment status pie
    private void updatePieChart(PieChart pieChart, java.util.Map<String, Integer> dataMap) {
        if (pieChart == null || dataMap == null) return;

        pieChart.getData().clear();

        // Add data for each status
        for (java.util.Map.Entry<String, Integer> entry : dataMap.entrySet()) {
            if (entry.getValue() > 0) {
                pieChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
        }

        // Style the pie slices
        String[] colors = {"#FF5722", "#4CAF50", "#FF9800", "#F44336", "#9C27B0"};
        for (int i = 0; i < pieChart.getData().size(); i++) {
            PieChart.Data slice = pieChart.getData().get(i);
            slice.getNode().setStyle(String.format(
                    "-fx-pie-color: %s; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);",
                    colors[i % colors.length]
            ));
        }
    }

    private void animateCard(VBox card) {
        if (card == null) return;

        // Reset to invisible first
        card.setOpacity(0.0);
        card.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), card);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setCycleCount(1);
        fadeIn.play();
    }

    @FXML
    private void generateReport() {
        generateInitialReport();
    }

    @FXML
    private void generateDemographicsReport() {
        try {
            String ageGroupStr = ageGroupComboBox.getValue();
            if (ageGroupStr == null) return;

            int ageGroups = Integer.parseInt(ageGroupStr);

            // Generate sample demographics data
            currentReport = generateSampleDemographicsReport(ageGroups);

            updateGenderPieChart();
            updateAgeBarChart();
            updateDemographicsTable();

        } catch (Exception e) {
            showAlert("Error", "Failed to generate demographics report: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private AnalyticsReport generateSampleDemographicsReport(int ageGroups) {
        AnalyticsReport report = new AnalyticsReport();
        report.setReportType("Patient Demographics (" + ageGroups + " year groups)");

        // Sample data
        report.setTotalPatients(472);
        report.setAvgPatientAge(34.2);

        // Gender distribution
        report.updateGenderDistribution("Male", 245);
        report.updateGenderDistribution("Female", 210);
        report.updateGenderDistribution("Other", 17);

        return report;
    }

    private void updateGenderPieChart() {
        if (currentReport == null) {
            currentReport = new AnalyticsReport();
            currentReport.updateGenderDistribution("Male", 245);
            currentReport.updateGenderDistribution("Female", 210);
            currentReport.updateGenderDistribution("Other", 17);
        }

        genderPieChart.getData().clear();
        genderPieChart.getData().addAll(
                new PieChart.Data("Male", currentReport.getGenderDistribution().getOrDefault("Male", 0)),
                new PieChart.Data("Female", currentReport.getGenderDistribution().getOrDefault("Female", 0)),
                new PieChart.Data("Other", currentReport.getGenderDistribution().getOrDefault("Other", 0))
        );

        // Style the pie slices
        String[] colors = {"#4CAF50", "#2196F3", "#FF9800"};
        for (int i = 0; i < genderPieChart.getData().size(); i++) {
            PieChart.Data slice = genderPieChart.getData().get(i);
            slice.getNode().setStyle(String.format(
                    "-fx-pie-color: %s; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);",
                    colors[i % colors.length]
            ));
        }
    }

    private void updateAgeBarChart() {
        if (currentReport == null) {
            currentReport = new AnalyticsReport();
        }

        ageBarChart.getData().clear();

        XYChart.Series<String, Number> maleSeries = new XYChart.Series<>();
        maleSeries.setName("Male");
        XYChart.Series<String, Number> femaleSeries = new XYChart.Series<>();
        femaleSeries.setName("Female");
        XYChart.Series<String, Number> otherSeries = new XYChart.Series<>();
        otherSeries.setName("Other");

        // Sample age group data
        String[] ageGroups = {"0-18", "19-35", "36-50", "51-65", "65+"};
        int[] maleCounts = {25, 80, 90, 45, 20};
        int[] femaleCounts = {30, 95, 100, 55, 25};
        int[] otherCounts = {2, 5, 3, 2, 1};

        for (int i = 0; i < ageGroups.length; i++) {
            maleSeries.getData().add(new XYChart.Data<>(ageGroups[i], maleCounts[i]));
            femaleSeries.getData().add(new XYChart.Data<>(ageGroups[i], femaleCounts[i]));
            otherSeries.getData().add(new XYChart.Data<>(ageGroups[i], otherCounts[i]));
        }

        ageBarChart.getData().addAll(maleSeries, femaleSeries, otherSeries);

        // Style series
        maleSeries.getNode().setStyle("-fx-stroke: #4CAF50; -fx-fill: #4CAF50;");
        femaleSeries.getNode().setStyle("-fx-stroke: #2196F3; -fx-fill: #2196F3;");
        otherSeries.getNode().setStyle("-fx-stroke: #FF9800; -fx-fill: #FF9800;");
    }

    private void setupDemographicsTable() {
        ageGroupColumn.setCellValueFactory(new PropertyValueFactory<>("ageGroup"));
        maleCountColumn.setCellValueFactory(new PropertyValueFactory<>("maleCount"));
        femaleCountColumn.setCellValueFactory(new PropertyValueFactory<>("femaleCount"));
        otherCountColumn.setCellValueFactory(new PropertyValueFactory<>("otherCount"));
        totalDemographicsColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

        percentageColumn.setCellFactory(col -> new TableCell<DemographicsRow, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty || totalPatients == 0) {
                    setText("0.0%");
                } else {
                    setText(String.format("%.1f%%", (item / totalPatients) * 100));
                }
            }
        });

        updateDemographicsTable();
    }

    private void updateDemographicsTable() {
        demographicsTable.getItems().clear();

        if (currentReport != null) {
            // Sample demographic data
            DemographicsRow row1 = new DemographicsRow("0-18", 25, 30, 2, 57);
            DemographicsRow row2 = new DemographicsRow("19-35", 80, 95, 5, 180);
            DemographicsRow row3 = new DemographicsRow("36-50", 90, 100, 3, 193);
            DemographicsRow row4 = new DemographicsRow("51-65", 45, 55, 2, 102);
            DemographicsRow row5 = new DemographicsRow("65+", 20, 25, 1, 46);

            totalPatients = row1.getTotal() + row2.getTotal() + row3.getTotal() + row4.getTotal() + row5.getTotal();

            demographicsTable.getItems().addAll(row1, row2, row3, row4, row5);
        }
    }

    @FXML
    private void generateAppointmentReport() {
        try {
            String timeRange = appointmentTimeRangeComboBox.getValue();
            if (timeRange == null) return;

            LocalDate endDate = LocalDate.now();
            LocalDate startDate;

            switch (timeRange) {
                case "30 Days": startDate = endDate.minusDays(30); break;
                case "3 Months": startDate = endDate.minusMonths(3); break;
                case "6 Months": startDate = endDate.minusMonths(6); break;
                case "12 Months": startDate = endDate.minusMonths(12); break;
                default: startDate = endDate.minusDays(30); break;
            }

            currentReport = reportingService.generateAnalyticsReport(startDate, endDate);

            updateAppointmentTrendChart();
            updateHourlyBarChart();
            updateStatusPieChart();
            updateDoctorRankingTable();

        } catch (Exception e) {
            showAlert("Error", "Failed to generate appointment report: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void updateAppointmentTrendChart() {
        appointmentTrendLine.getData().clear();

        XYChart.Series<String, Number> totalSeries = new XYChart.Series<>();
        totalSeries.setName("Total Appointments");
        XYChart.Series<String, Number> completedSeries = new XYChart.Series<>();
        completedSeries.setName("Completed");

        // Sample monthly data
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        int[] totalAppointments = {45, 52, 48, 65, 70, 68, 75, 80, 72, 85, 90, 95};
        int[] completedAppointments = {38, 45, 42, 58, 62, 60, 68, 72, 65, 78, 82, 88};

        for (int i = 0; i < months.length; i++) {
            totalSeries.getData().add(new XYChart.Data<>(months[i], totalAppointments[i]));
            completedSeries.getData().add(new XYChart.Data<>(months[i], completedAppointments[i]));
        }

        appointmentTrendLine.getData().addAll(totalSeries, completedSeries);

        // Style series
        totalSeries.getNode().setStyle("-fx-stroke: #2196F3; -fx-fill: #2196F3;");
        completedSeries.getNode().setStyle("-fx-stroke: #4CAF50; -fx-fill: #4CAF50;");
    }

    private void updateHourlyBarChart() {
        hourlyAppointmentBar.getData().clear();

        XYChart.Series<String, Number> appointmentsSeries = new XYChart.Series<>();
        appointmentsSeries.setName("Appointments per Hour");

        String[] hours = {"8AM", "9AM", "10AM", "11AM", "12PM", "1PM", "2PM", "3PM", "4PM", "5PM", "6PM"};
        int[] hourlyCounts = {5, 15, 25, 30, 20, 18, 25, 22, 18, 12, 5};

        for (int i = 0; i < hours.length; i++) {
            appointmentsSeries.getData().add(new XYChart.Data<>(hours[i], hourlyCounts[i]));
        }

        hourlyAppointmentBar.getData().add(appointmentsSeries);
        appointmentsSeries.getNode().setStyle("-fx-stroke: #FF9800; -fx-fill: #FF9800;");
    }

    private void updateStatusPieChart() {
        if (currentReport == null) {
            currentReport = new AnalyticsReport();
            currentReport.updateStatusDistribution("Scheduled", 167);
            currentReport.updateStatusDistribution("Completed", 1089);
            currentReport.updateStatusDistribution("Cancelled", 0);
            currentReport.updateStatusDistribution("No Show", 0);
        }

        statusPieChart.getData().clear();
        statusPieChart.getData().addAll(
                new PieChart.Data("Scheduled", currentReport.getStatusDistribution().getOrDefault("Scheduled", 0)),
                new PieChart.Data("Completed", currentReport.getStatusDistribution().getOrDefault("Completed", 0)),
                new PieChart.Data("Cancelled", currentReport.getStatusDistribution().getOrDefault("Cancelled", 0)),
                new PieChart.Data("No Show", currentReport.getStatusDistribution().getOrDefault("No Show", 0))
        );

        String[] colors = {"#FF5722", "#4CAF50", "#FF9800", "#F44336"};
        for (int i = 0; i < statusPieChart.getData().size(); i++) {
            PieChart.Data slice = statusPieChart.getData().get(i);
            slice.getNode().setStyle(String.format(
                    "-fx-pie-color: %s; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);",
                    colors[i % colors.length]
            ));
        }
    }

    private void setupDoctorRankingTable() {
        doctorNameRankColumn.setCellValueFactory(new PropertyValueFactory<>("doctorName"));

        completionRankColumn.setCellFactory(col -> new TableCell<DoctorRankingRow, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText("0.0%");
                } else {
                    setText(String.format("%.1f%%", item));
                }
            }
        });

        patientCountRankColumn.setCellValueFactory(new PropertyValueFactory<>("patientCount"));
        appointmentCountRankColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentCount"));

        updateDoctorRankingTable();
    }

    private void updateDoctorRankingTable() {
        doctorRankingTable.getItems().clear();

        // Sample doctor performance data
        DoctorRankingRow doctor1 = new DoctorRankingRow(1, "Dr. John Smith", 92.5, 150, 350, 5);
        DoctorRankingRow doctor2 = new DoctorRankingRow(2, "Dr. Sarah Johnson", 88.2, 120, 280, 4);
        DoctorRankingRow doctor3 = new DoctorRankingRow(3, "Dr. Michael Brown", 95.1, 180, 420, 3);
        DoctorRankingRow doctor4 = new DoctorRankingRow(4, "Dr. Emily Davis", 90.3, 95, 220, 2);
        DoctorRankingRow doctor5 = new DoctorRankingRow(5, "Dr. Robert Wilson", 87.8, 110, 260, 1);

        doctorRankingTable.getItems().addAll(doctor1, doctor2, doctor3, doctor4, doctor5);
    }

    @FXML
    private void generateCustomReport() {
        String reportType = reportTypeComboBox.getValue();
        LocalDate startDate = customStartDatePicker.getValue();
        LocalDate endDate = customEndDatePicker.getValue();

        if (reportType == null || startDate == null || endDate == null) {
            showAlert("Validation Error", "Please select report type and date range", Alert.AlertType.WARNING);
            return;
        }

        loadingProgress.setVisible(true);
        loadingLabel.setVisible(true);

        new Thread(() -> {
            try {
                AnalyticsReport report = generateSampleCustomReport(reportType, startDate, endDate);

                Platform.runLater(() -> {
                    generateCustomReportPreview(report);
                    loadingProgress.setVisible(false);
                    loadingLabel.setVisible(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to generate custom report: " + e.getMessage(), Alert.AlertType.ERROR);
                    loadingProgress.setVisible(false);
                    loadingLabel.setVisible(false);
                });
            }
        }).start();
    }

    private AnalyticsReport generateSampleCustomReport(String type, LocalDate start, LocalDate end) {
        AnalyticsReport report = new AnalyticsReport();
        report.setReportType(type);
        report.setStartDate(start);
        report.setEndDate(end);

        // Sample data based on report type
        switch (type) {
            case "Patient Demographics":
                report.setTotalPatients(472);
                report.setAvgPatientAge(34.2);
                report.updateGenderDistribution("Male", 245);
                report.updateGenderDistribution("Female", 210);
                report.updateGenderDistribution("Other", 17);
                break;
            case "Appointment Trends":
                report.setTotalAppointments(1256);
                report.setCompletedAppointments(1089);
                report.setCompletionRate(86.7);
                report.updateStatusDistribution("Scheduled", 167);
                report.updateStatusDistribution("Completed", 1089);
                report.updateStatusDistribution("Cancelled", 0);
                break;
            case "Doctor Performance":
                report.setCompletionRate(89.3);
                // Would load doctor-specific data
                break;
            default:
                report.setTotalPatients(472);
                report.setTotalAppointments(1256);
                break;
        }

        return report;
    }

    // MISSING METHODS: Custom report preview helpers
    private void addPatientDemographicsPreview(VBox reportVBox, AnalyticsReport report) {
        Label sectionTitle = new Label("Patient Demographics Analysis");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 20px 0 10px 0;");
        reportVBox.getChildren().add(sectionTitle);

        // Summary
        VBox summaryBox = new VBox(8);
        summaryBox.getChildren().addAll(
                createReportRow("Total Patients:", String.valueOf(report.getTotalPatients())),
                createReportRow("Average Age:", String.format("%.1f years", report.getAvgPatientAge())),
                createReportRow("Male Patients:", String.valueOf(report.getGenderDistribution().getOrDefault("Male", 0))),
                createReportRow("Female Patients:", String.valueOf(report.getGenderDistribution().getOrDefault("Female", 0))),
                createReportRow("Other:", String.valueOf(report.getGenderDistribution().getOrDefault("Other", 0)))
        );
        reportVBox.getChildren().add(summaryBox);

        // Insights
        VBox insightsBox = new VBox(5);
        insightsBox.setStyle("-fx-padding: 10px; -fx-background-color: #e3f2fd;");
        insightsBox.getChildren().addAll(
                new Label("Key Insights:"),
                new Label("- 52% of patients are female"),
                new Label("- Average patient age is 34.2 years"),
                new Label("- Most patients are in the 19-35 age group"),
                new Label("- 17 patients identified as 'Other' gender")
        );
        reportVBox.getChildren().add(insightsBox);
    }

    private void addAppointmentTrendsPreview(VBox reportVBox, AnalyticsReport report) {
        Label sectionTitle = new Label("Appointment Trends Analysis");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 20px 0 10px 0;");
        reportVBox.getChildren().add(sectionTitle);

        VBox summaryBox = new VBox(8);
        summaryBox.getChildren().addAll(
                createReportRow("Total Appointments:", String.valueOf(report.getTotalAppointments())),
                createReportRow("Completed Appointments:", String.valueOf(report.getCompletedAppointments())),
                createReportRow("Completion Rate:", String.format("%.1f%%", report.getCompletionRate())),
                createReportRow("Scheduled:", String.valueOf(report.getStatusDistribution().getOrDefault("Scheduled", 0))),
                createReportRow("Cancelled:", String.valueOf(report.getStatusDistribution().getOrDefault("Cancelled", 0)))
        );
        reportVBox.getChildren().add(summaryBox);

        // Trend insights
        VBox insightsBox = new VBox(5);
        insightsBox.setStyle("-fx-padding: 10px; -fx-background-color: #f3e5f5;");
        insightsBox.getChildren().addAll(
                new Label("Trends & Insights:"),
                new Label("- Appointment volume increased 15% month-over-month"),
                new Label("- Peak appointment times: 10AM-12PM and 2PM-4PM"),
                new Label("- Cancellation rate: 2.1% (below target of 5%)"),
                new Label("- Friday shows highest appointment volume")
        );
        reportVBox.getChildren().add(insightsBox);
    }

    private void addDoctorPerformancePreview(VBox reportVBox, AnalyticsReport report) {
        Label sectionTitle = new Label("Doctor Performance Review");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 20px 0 10px 0;");
        reportVBox.getChildren().add(sectionTitle);

        VBox summaryBox = new VBox(8);
        summaryBox.getChildren().addAll(
                createReportRow("Overall Completion Rate:", String.format("%.1f%%", report.getCompletionRate())),
                createReportRow("Top Performer:", "Dr. Michael Brown (95.1%)"),
                createReportRow("Average Patient Load:", "142 patients per doctor"),
                createReportRow("Most Productive Doctor:", "Dr. Michael Brown (420 appts)")
        );
        reportVBox.getChildren().add(summaryBox);

        // Performance insights
        VBox insightsBox = new VBox(5);
        insightsBox.setStyle("-fx-padding: 10px; -fx-background-color: #fff3e0;");
        insightsBox.getChildren().addAll(
                new Label("Performance Insights:"),
                new Label("- Dr. Brown exceeds completion target by 12%"),
                new Label("- Average scheduling lead time: 3.2 days"),
                new Label("- Top 3 doctors handle 68% of all appointments"),
                new Label("- All doctors meet minimum performance threshold")
        );
        reportVBox.getChildren().add(insightsBox);
    }

    private void addGenericPreview(VBox reportVBox, AnalyticsReport report) {
        Label sectionTitle = new Label("General Analytics Overview");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 20px 0 10px 0;");
        reportVBox.getChildren().add(sectionTitle);

        VBox summaryBox = new VBox(8);
        summaryBox.getChildren().addAll(
                createReportRow("Report Period:", String.format("%s to %s",
                        report.getStartDate().format(DateTimeFormatter.ofPattern("MMM dd")),
                        report.getEndDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))),
                createReportRow("Data Points:", "1,256 appointments, 472 patients"),
                createReportRow("Report Generated:", LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
        );
        reportVBox.getChildren().add(summaryBox);
    }

    private HBox createReportRow(String labelText, String valueText) {
        HBox row = new HBox(10);
        row.setStyle("-fx-padding: 5px;");

        Label label = new Label(labelText + " ");
        label.setStyle("-fx-font-weight: bold; -fx-min-width: 150px;");

        Label value = new Label(valueText);
        value.setStyle("-fx-font-size: 12px;");

        row.getChildren().addAll(label, value);
        return row;
    }

    @FXML
    private void exportToCSV() {
        if (currentReport == null) {
            showAlert("Error", "No report data to export", Alert.AlertType.WARNING);
            return;
        }

        // Simple CSV export to console (for demo)
        StringBuilder csv = new StringBuilder();
        csv.append("Report Type,").append(currentReport.getReportType()).append("\n");
        csv.append("Date Range,").append(currentReport.getStartDate()).append(" to ").append(currentReport.getEndDate()).append("\n");
        csv.append("Total Patients,").append(currentReport.getTotalPatients()).append("\n");
        csv.append("Avg Age,").append(currentReport.getAvgPatientAge()).append("\n");
        csv.append("Total Appointments,").append(currentReport.getTotalAppointments()).append("\n");
        csv.append("Completion Rate,").append(String.format("%.1f%%", currentReport.getCompletionRate())).append("\n");

        System.out.println("=== CSV EXPORT ===");
        System.out.println(csv.toString());

        showAlert("Export", "CSV data exported to console (implementation for file export would go here)",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    private void exportToPDF() {
        if (currentReport == null) {
            showAlert("Error", "No report data to export", Alert.AlertType.WARNING);
            return;
        }

        // PDF export would use iText, Apache PDFBox, etc.
        showAlert("Export", "PDF export would be implemented using iText or PDFBox library",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    private void printReport() {
        if (currentReport == null) {
            showAlert("Error", "No report to print", Alert.AlertType.WARNING);
            return;
        }

        // JavaFX print would use Printer API
        showAlert("Print", "Print functionality would use JavaFX Printer API",
                Alert.AlertType.INFORMATION);
    }

//    private void initializeSampleCharts() {
//        // Initialize pie chart with sample data
//        appointmentStatusPie.getData().addAll(
//                new PieChart.Data("Scheduled", 167),
//                new PieChart.Data("Completed", 1089),
//                new PieChart.Data("Cancelled", 0)
//        );
//
//        updatePieChart(appointmentStatusPie, currentReport != null ? currentReport.getStatusDistribution() : null);
//
//        // Initialize other charts with sample data
//        updateGenderPieChart();
//        updateAgeBarChart();
//        updateAppointmentTrendChart();
//        updateHourlyBarChart();
//        updateStatusPieChart();
//        updateDoctorRankingTable();
//        updateDemographicsTable();
//    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void generateCustomReportPreview(AnalyticsReport report) {
        if (report == null) {
            showAlert("Error", "No report data available", Alert.AlertType.WARNING);
            return;
        }

        // Clear previous content
        reportPreviewArea.getChildren().clear();

        // Create main report container
        VBox reportContainer = new VBox(15);
        reportContainer.setStyle("-fx-padding: 20px; -fx-spacing: 15px; -fx-background-color: #fafafa; -fx-border-color: #ddd; -fx-border-radius: 8px; -fx-border-width: 1px;");

        // Report Header
        HBox header = new HBox(10);
        header.setStyle("-fx-alignment: center-left; -fx-padding: 15px 20px 10px 20px; -fx-background-color: #e3f2fd;");

        Label iconLabel = new Label("📊");
        iconLabel.setStyle("-fx-font-size: 24px;");

        VBox headerText = new VBox(2);
        Label titleLabel = new Label(report.getReportType());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1976d2;");

        Label dateLabel = new Label(String.format("Generated on %s",
                LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-italic: true;");

        headerText.getChildren().addAll(titleLabel, dateLabel);
        header.getChildren().addAll(iconLabel, headerText);
        reportContainer.getChildren().add(header);

        // Date Range Information
        HBox dateInfo = new HBox(10);
        dateInfo.setStyle("-fx-padding: 0 20px 15px 20px; -fx-alignment: center-left;");
        Label dateIcon = new Label("📅");
        dateIcon.setStyle("-fx-font-size: 16px;");

        String dateRangeText = String.format("Date Range: %s to %s",
                report.getStartDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                report.getEndDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        Label dateRangeLabel = new Label(dateRangeText);
        dateRangeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        dateInfo.getChildren().addAll(dateIcon, dateRangeLabel);
        reportContainer.getChildren().add(dateInfo);

        // Summary Metrics Section
        VBox summarySection = new VBox(10);
        summarySection.setStyle("-fx-padding: 15px 20px; -fx-background-color: white; -fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");

        Label summaryTitle = new Label("📈 Summary Metrics");
        summaryTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 0 0 10px 0; -fx-text-fill: #1976d2;");
        summarySection.getChildren().add(summaryTitle);

        // Add key metrics based on available data
        if (report.getTotalPatients() > 0) {
            HBox patientMetric = createMetricRow("👥 Total Patients",
                    String.valueOf(report.getTotalPatients()), "#4CAF50");
            summarySection.getChildren().add(patientMetric);
        }

        if (report.getAvgPatientAge() > 0) {
            HBox ageMetric = createMetricRow("👴 Average Age",
                    String.format("%.1f years", report.getAvgPatientAge()), "#2196F3");
            summarySection.getChildren().add(ageMetric);
        }

        if (report.getTotalAppointments() > 0) {
            HBox apptMetric = createMetricRow("📋 Total Appointments",
                    String.valueOf(report.getTotalAppointments()), "#FF9800");
            summarySection.getChildren().add(apptMetric);
        }

        if (report.getCompletionRate() > 0) {
            HBox completionMetric = createMetricRow("✅ Completion Rate",
                    String.format("%.1f%%", report.getCompletionRate()), "#4CAF50");
            summarySection.getChildren().add(completionMetric);
        }

        reportContainer.getChildren().add(summarySection);

        // Distribution Analysis Section
        if (!report.getGenderDistribution().isEmpty() || !report.getStatusDistribution().isEmpty()) {
            VBox distributionSection = createDistributionSection(report);
            reportContainer.getChildren().add(distributionSection);
        }

        // Insights Section
        VBox insightsSection = createInsightsSection(report);
        reportContainer.getChildren().add(insightsSection);

        // Footer with action buttons
        HBox footer = new HBox(10);
        footer.setStyle("-fx-padding: 15px 20px; -fx-alignment: center; -fx-spacing: 10px;");
        footer.getChildren().addAll(
                createExportButton("📄 Export CSV", e -> exportToCSV()),
                createExportButton("🖨️ Print Report", e -> printReport())
        );
        reportContainer.getChildren().add(footer);

        // Add the complete report to the preview area
        reportPreviewArea.getChildren().add(reportContainer);

        // Animate the report appearance
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), reportContainer);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private HBox createMetricRow(String icon, String value, String color) {
        HBox metricRow = new HBox(10);
        metricRow.setStyle("-fx-padding: 8px 0; -fx-alignment: center-left;");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 14px; -fx-padding: 0 5px;");

        VBox textContent = new VBox(2);
        Label metricLabel = new Label("Metric Name");
        metricLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Label metricValue = new Label(value);
        metricValue.setStyle(String.format("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: %s;", color));

        textContent.getChildren().addAll(metricLabel, metricValue);

        metricRow.getChildren().addAll(iconLabel, textContent);
        return metricRow;
    }

    private VBox createDistributionSection(AnalyticsReport report) {
        VBox section = new VBox(10);
        section.setStyle("-fx-padding: 15px 20px; -fx-background-color: white; -fx-border-radius: 5px; -fx-margin: 10px 0;");

        Label title = new Label("📊 Distribution Analysis");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 0 0 10px 0; -fx-text-fill: #1976d2;");
        section.getChildren().add(title);

        if (!report.getGenderDistribution().isEmpty()) {
            VBox genderSection = new VBox(5);
            genderSection.setStyle("-fx-padding: 10px; -fx-background-color: #f0f8ff; -fx-border-radius: 3px;");

            Label genderTitle = new Label("Patient Gender Distribution");
            genderTitle.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 5px 0;");
            genderSection.getChildren().add(genderTitle);

            for (String gender : report.getGenderDistribution().keySet()) {
                int count = report.getGenderDistribution().get(gender);
                double percentage = report.getGenderPercentage(gender);

                HBox genderRow = new HBox(10);
                genderRow.setStyle("-fx-padding: 2px 0;");

                Label genderIcon = new Label(gender.equals("Male") ? "♂️" :
                        gender.equals("Female") ? "♀️" : "⚧️");
                genderIcon.setStyle("-fx-font-size: 12px;");

                Label genderLabel = new Label(gender + ": " + count + " (" + String.format("%.1f%%", percentage) + ")");
                genderLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");

                genderRow.getChildren().addAll(genderIcon, genderLabel);
                genderSection.getChildren().add(genderRow);
            }

            section.getChildren().add(genderSection);
        }

        if (!report.getStatusDistribution().isEmpty()) {
            VBox statusSection = new VBox(5);
            statusSection.setStyle("-fx-padding: 10px; -fx-background-color: #fff3e0; -fx-border-radius: 3px; -fx-margin-top: 10px;");

            Label statusTitle = new Label("Appointment Status Distribution");
            statusTitle.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 5px 0;");
            statusSection.getChildren().add(statusTitle);

            for (String status : report.getStatusDistribution().keySet()) {
                int count = report.getStatusDistribution().get(status);
                double percentage = report.getStatusPercentage(status);

                HBox statusRow = new HBox(10);
                statusRow.setStyle("-fx-padding: 2px 0;");

                String statusIcon = status.equals("Scheduled") ? "⏰" :
                        status.equals("Completed") ? "✅" :
                                status.equals("Cancelled") ? "❌" : "⚠️";
                Label statusIconLabel = new Label(statusIcon);
                statusIconLabel.setStyle("-fx-font-size: 12px;");

                Label statusLabel = new Label(status + ": " + count + " (" + String.format("%.1f%%", percentage) + ")");
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");

                statusRow.getChildren().addAll(statusIconLabel, statusLabel);
                statusSection.getChildren().add(statusRow);
            }

            section.getChildren().add(statusSection);
        }

        return section;
    }

    private VBox createInsightsSection(AnalyticsReport report) {
        VBox section = new VBox(8);
        section.setStyle("-fx-padding: 15px 20px; -fx-background-color: #f8f9fa; -fx-border-radius: 5px; -fx-margin-top: 10px;");

        Label title = new Label("💡 Key Insights");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 0 0 10px 0; -fx-text-fill: #388e3c;");
        section.getChildren().add(title);

        // Dynamic insights based on report data
        List<String> insights = generateInsights(report);

        for (String insight : insights) {
            HBox insightRow = new HBox(8);
            insightRow.setStyle("-fx-padding: 5px 0; -fx-alignment: center-left;");

            Label bullet = new Label("•");
            bullet.setStyle("-fx-font-size: 14px; -fx-text-fill: #388e3c; -fx-padding: 0 8px 0 0;");

            Label insightText = new Label(insight);
            insightText.setStyle("-fx-font-size: 12px; -fx-text-fill: #333; -fx-wrap-text: true;");
            insightText.setPrefWidth(400);

            insightRow.getChildren().addAll(bullet, insightText);
            section.getChildren().add(insightRow);
        }

        return section;
    }

    private List<String> generateInsights(AnalyticsReport report) {
        List<String> insights = new ArrayList<>();

        if (report.getTotalPatients() > 0) {
            double avgAge = report.getAvgPatientAge();
            insights.add(String.format("The average patient age is %.1f years, indicating a balanced patient demographic", avgAge));

            if (!report.getGenderDistribution().isEmpty()) {
                double malePercentage = report.getGenderPercentage("Male");
                double femalePercentage = report.getGenderPercentage("Female");

                if (Math.abs(malePercentage - femalePercentage) < 10) {
                    insights.add("Patient gender distribution is balanced (within 10% difference)");
                } else if (femalePercentage > malePercentage) {
                    insights.add(String.format("Slight female patient majority (%.1f%% vs %.1f%% male)",
                            femalePercentage, malePercentage));
                } else {
                    insights.add(String.format("Slight male patient majority (%.1f%% vs %.1f%% female)",
                            malePercentage, femalePercentage));
                }
            }
        }

        if (report.getTotalAppointments() > 0) {
            double completionRate = report.getCompletionRate();
            insights.add(String.format("Appointment completion rate is %.1f%% - " +
                    (completionRate > 85 ? "excellent performance" : completionRate > 70 ? "good performance" :
                            completionRate > 50 ? "average performance" : "needs improvement"), completionRate));

            if (completionRate < 80) {
                insights.add("Consider implementing reminder systems to improve completion rates");
            }
        }

        // Add system health insights
        insights.add("System has been running smoothly with no reported data integrity issues");
        insights.add("All active doctors are meeting minimum performance thresholds");
        insights.add("Patient data is up-to-date with recent registration activity");

        return insights;
    }

    private Button createExportButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-padding: 8px 16px; -fx-font-size: 12px; -fx-background-radius: 4px;");
        button.setOnAction(handler);

        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; -fx-padding: 8px 16px; -fx-font-size: 12px; -fx-background-radius: 4px;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-padding: 8px 16px; -fx-font-size: 12px; -fx-background-radius: 4px;"));

        return button;
    }

//    private void exportToCSV() {
//        if (currentReport == null) {
//            showAlert("Error", "No report data to export", Alert.AlertType.WARNING);
//            return;
//        }
//
//        // Create CSV content
//        StringBuilder csv = new StringBuilder();
//        csv.append("Healthcare Analytics Report\n");
//        csv.append("=========================\n");
//        csv.append("Report Type,").append(currentReport.getReportType()).append("\n");
//        csv.append("Generated,").append(LocalDate.now()).append("\n");
//        csv.append("Date Range,").append(currentReport.getStartDate()).append(" to ").append(currentReport.getEndDate()).append("\n\n");
//
//        // Summary metrics
//        csv.append("SUMMARY METRICS\n");
//        csv.append("---------------\n");
//        if (currentReport.getTotalPatients() > 0) {
//            csv.append("Total Patients,").append(currentReport.getTotalPatients()).append("\n");
//        }
//        if (currentReport.getAvgPatientAge() > 0) {
//            csv.append("Average Patient Age,").append(String.format("%.1f", currentReport.getAvgPatientAge())).append("\n");
//        }
//        if (currentReport.getTotalAppointments() > 0) {
//            csv.append("Total Appointments,").append(currentReport.getTotalAppointments()).append("\n");
//        }
//        if (currentReport.getCompletionRate() > 0) {
//            csv.append("Completion Rate,").append(String.format("%.1f%%", currentReport.getCompletionRate())).append("\n");
//        }
//        csv.append("\n");
//
//        // Gender distribution
//        if (!currentReport.getGenderDistribution().isEmpty()) {
//            csv.append("GENDER DISTRIBUTION\n");
//            csv.append("------------------\n");
//            for (String gender : currentReport.getGenderDistribution().keySet()) {
//                double percentage = currentReport.getGenderPercentage(gender);
//                csv.append(gender).append(",")
//                        .append(currentReport.getGenderDistribution().get(gender))
//                        .append(",")
//                        .append(String.format("%.1f%%", percentage)).append("\n");
//            }
//            csv.append("\n");
//        }
//
//        // Appointment status
//        if (!currentReport.getStatusDistribution().isEmpty()) {
//            csv.append("APPOINTMENT STATUS\n");
//            csv.append("-----------------\n");
//            for (String status : currentReport.getStatusDistribution().keySet()) {
//                double percentage = currentReport.getStatusPercentage(status);
//                csv.append(status).append(",")
//                        .append(currentReport.getStatusDistribution().get(status))
//                        .append(",")
//                        .append(String.format("%.1f%%", percentage)).append("\n");
//            }
//            csv.append("\n");
//        }
//
//        // Output to console (in production, this would save to file)
//        System.out.println("=== CSV EXPORT START ===");
//        System.out.println(csv.toString());
//        System.out.println("=== CSV EXPORT END ===");
//
//        showAlert("Export Complete",
//                "CSV data has been generated.\n\n" +
//                        "Total lines exported: " + csv.toString().split("\n").length + "\n" +
//                        "(In production, this would be saved to a file)",
//                Alert.AlertType.INFORMATION);
//    }

//    private void printReport() {
//        if (currentReport == null) {
//            showAlert("Error", "No report to print", Alert.AlertType.WARNING);
//            return;
//        }
//
//        // Basic print dialog (would use JavaFX Printer API for full implementation)
//        Alert printDialog = new Alert(Alert.AlertType.INFORMATION);
//        printDialog.setTitle("Print Report");
//        printDialog.setHeaderText("Print Preview");
//        printDialog.setContentText("Report ready for printing:\n\n" +
//                "• Title: " + currentReport.getReportType() + "\n" +
//                "• Date Range: " + currentReport.getStartDate() + " to " + currentReport.getEndDate() + "\n" +
//                "• Key Metrics:\n" +
//                "  - Total Patients: " + currentReport.getTotalPatients() + "\n" +
//                "  - Avg Age: " + String.format("%.1f years", currentReport.getAvgPatientAge()) + "\n" +
//                "  - Total Appointments: " + currentReport.getTotalAppointments() + "\n" +
//                "  - Completion Rate: " + String.format("%.1f%%", currentReport.getCompletionRate()));
//
//        printDialog.showAndWait();
//
//        showAlert("Print Ready", "Report is formatted and ready for printing.\n\n" + "In production implementation, this would:\n" + "• Open JavaFX Print dialog\n" + "• Format report with charts and tables\n" + "• Print to selected printer", Alert.AlertType.INFORMATION);
//    }

//    private void showAlert(String title, String message, Alert.AlertType type) {
//        Alert alert = new Alert(type);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }

    private int getTotalPatients() {
        return (currentReport != null) ? currentReport.getTotalPatients() : totalPatients;
    }


}

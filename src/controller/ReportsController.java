package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import service.ReportService;
import util.AlertUtil;
import util.SceneUtil;
import util.Session;

public class ReportsController implements Initializable {

    @FXML
    private ComboBox<String> reportTypeBox;

    @FXML
    private Button generateReportBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Label statusLabel;

    @FXML
    private TextArea reportOutputArea;

    private final ReportService reportService = new ReportService();

    private Task<String> reportTask;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        reportTypeBox.setItems(
                FXCollections.observableArrayList(
                        "Active Orders",
                        "Served Orders",
                        "Menu Item Sales",
                        "Revenue Summary"
                )
        );

        statusLabel.setText("Ready");
        progressIndicator.setVisible(false);
        cancelBtn.setDisable(true);
    }

    @FXML
    private void handleGenerateReport(ActionEvent event) {

        if (reportTask != null && reportTask.isRunning()) {
            AlertUtil.showWarning(
                    "Operation Running",
                    "Please wait for the current report to finish or cancel it."
            );
            return;
        }

        String reportType = reportTypeBox.getValue();

        if (reportType == null) {
            AlertUtil.showError(
                    "Validation Error",
                    "Please select a report type."
            );
            return;
        }

        reportTask = new Task<>() {

            @Override
            protected String call() {

                updateMessage("Generating report...");
                updateProgress(0, 1);

                if (isCancelled()) {
                    updateMessage("Report canceled.");
                    return "";
                }

                String report = reportService.generateReport(
                        reportType,
                        Session.getCurrentUser()
                );

                if (isCancelled()) {
                    updateMessage("Report canceled.");
                    return "";
                }

                updateProgress(1, 1);
                updateMessage("Report generated.");

                return report;
            }
        };

        bindTaskToUi(reportTask);

        reportTask.setOnSucceeded(e -> {

            cleanupTaskBindings();

            if (reportTask.isCancelled()) {
                statusLabel.setText("Canceled");
                return;
            }

            reportOutputArea.setText(reportTask.getValue());
            statusLabel.setText("Ready");
        });

        reportTask.setOnFailed(e -> {

            cleanupTaskBindings();

            Throwable ex = reportTask.getException();

            AlertUtil.showError(
                    "Report Error",
                    ex == null
                    ? "Failed to generate report."
                    : ex.getMessage()
            );

            statusLabel.setText("Failed");
        });

        Thread thread = new Thread(reportTask, "report-generation-task");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleCancelReport(ActionEvent event) {

        if (reportTask != null && reportTask.isRunning()) {
            reportTask.cancel();

            AlertUtil.showInfo(
                    "Canceled",
                    "Report generation canceled."
            );
        }
    }

    @FXML
    private void backToDashboard(ActionEvent event) throws IOException {
        SceneUtil.switchScene(event, "/view/dashboard.fxml");
    }

    private void bindTaskToUi(Task<?> task) {

        progressIndicator.visibleProperty().unbind();
        progressIndicator.visibleProperty().bind(task.runningProperty());

        statusLabel.textProperty().unbind();
        statusLabel.textProperty().bind(task.messageProperty());

        cancelBtn.disableProperty().unbind();
        cancelBtn.disableProperty().bind(task.runningProperty().not());

        generateReportBtn.disableProperty().unbind();
        generateReportBtn.disableProperty().bind(task.runningProperty());
    }

    private void cleanupTaskBindings() {

        progressIndicator.visibleProperty().unbind();
        progressIndicator.setVisible(false);

        statusLabel.textProperty().unbind();

        cancelBtn.disableProperty().unbind();
        cancelBtn.setDisable(true);

        generateReportBtn.disableProperty().unbind();
        generateReportBtn.setDisable(false);
    }
}
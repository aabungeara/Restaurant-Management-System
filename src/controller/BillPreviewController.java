package controller;

import dto.BillItemDTO;
import dto.BillPrintDTO;
import java.io.File;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import util.AlertUtil;
import javafx.print.PrinterJob;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import service.PdfExportService;

public class BillPreviewController {

    @FXML
    private Label billIdLabel;

    @FXML
    private Label tableLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label totalLabel;
    @FXML
    private AnchorPane rootPane;

    @FXML
    private TableView<BillItemDTO> tableView;

    @FXML
    private TableColumn<BillItemDTO, String> itemColumn;

    @FXML
    private TableColumn<BillItemDTO, Integer> qtyColumn;

    @FXML
    private TableColumn<BillItemDTO, Double> priceColumn;

    @FXML
    private TableColumn<BillItemDTO, Double> totalColumn;

    private BillPrintDTO bill;

    @FXML
    public void initialize() {

        itemColumn.setCellValueFactory(data
                -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getItemName()
                )
        );

        qtyColumn.setCellValueFactory(data
                -> new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getQuantity()
                ).asObject()
        );

        priceColumn.setCellValueFactory(data
                -> new javafx.beans.property.SimpleDoubleProperty(
                        data.getValue().getPrice()
                ).asObject()
        );

        totalColumn.setCellValueFactory(data
                -> new javafx.beans.property.SimpleDoubleProperty(
                        data.getValue().getTotal()
                ).asObject()
        );
    }

    public void setBill(BillPrintDTO bill) {

        this.bill = bill;

        // Labels
        billIdLabel.setText("Bill ID: " + bill.getBillId());
        tableLabel.setText("Table: " + bill.getTableNumber());
        statusLabel.setText("Status: " + bill.getStatus());
        totalLabel.setText("TOTAL: $" + String.format("%.2f", bill.getTotal()));

        // Table Items
        tableView.setItems(
                FXCollections.observableArrayList(
                        bill.getItems()
                )
        );
    }

    @FXML
    private void handlePrint() {

        if (bill == null) {
            AlertUtil.showError("Error", "No bill to print");
            return;
        }

        PrinterJob job = PrinterJob.createPrinterJob();

        if (job == null) {
            AlertUtil.showError("Error", "No printer found");
            return;
        }

        boolean proceed = job.showPrintDialog(rootPane.getScene().getWindow());

        if (!proceed) {
            return;
        }

        boolean success = job.printPage(rootPane);

        if (success) {
            job.endJob();
            AlertUtil.showInfo("Print", "Bill printed successfully");
        } else {
            AlertUtil.showError("Print", "Failed to print bill");
        }
    }

    @FXML
private void handleExportPdf() {

    if (bill == null) {
        AlertUtil.showError("Error", "No bill to export");
        return;
    }

    try {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Bill PDF");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(
                billIdLabel.getScene().getWindow()
        );

        if (file == null) return;

        PdfExportService.exportBill(bill, file.getAbsolutePath());

        AlertUtil.showInfo("Success", "PDF exported successfully");

    } catch (Exception e) {
        e.printStackTrace();
        AlertUtil.showError("Error", "Failed to export PDF");
    }
}

    @FXML
    private void handleClose() {
        Stage stage = (Stage) billIdLabel.getScene().getWindow();
        stage.close();
    }
}

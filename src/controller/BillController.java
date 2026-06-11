package controller;

import dto.BillPrintDTO;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javafx.scene.control.cell.PropertyValueFactory;

import model.Bill;
import model.RestaurantTable;

import repositories.OrderRepo;

import service.BillPrintService;
import service.BillService;
import service.OrderService;
import service.TableService;

import util.AlertUtil;
import util.SceneUtil;
import util.Session;

public class BillController implements Initializable {

    @FXML
    private ComboBox<String> tableBox;

    @FXML
    private TextField totalField;

    @FXML
    private ComboBox<String> statusBox;

    @FXML
    private Button addBillBtn;

    @FXML
    private TableView<Bill> tableView;

    @FXML
    private TableColumn<Bill, Integer> idColumn;

    @FXML
    private TableColumn<Bill, Integer> tableNumberColumn;

    @FXML
    private TableColumn<Bill, Double> totalColumn;

    @FXML
    private TableColumn<Bill, String> statusColumn;

    @FXML
    private TextField searchTableField;

    @FXML
    private ComboBox<String> filterStatusBox;

    @FXML
    private ComboBox<String> sortTotalBox;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Label statusLabel;

    @FXML
    private Button cancelBtn;

    private final BillService billService = new BillService();
    private final TableService tableService = new TableService();
    private final OrderService orderService = new OrderService();
    private final OrderRepo orderRepo = new OrderRepo();

    private List<RestaurantTable> tables = new ArrayList<>();

    private final ObservableList<Bill> bills = FXCollections.observableArrayList();

    private Bill selectedBill = null;

    private Task<?> currentTask;
    @FXML
    private TextArea billOutputArea;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        setupStatusBoxes();
        setupTableColumns();
        setupFilters();
        setupTableSelection();

        loadTables();
        loadBills();

        statusLabel.setText("Ready");
        progressIndicator.setVisible(false);
        cancelBtn.setDisable(true);
    }

    private void setupStatusBoxes() {

        statusBox.setItems(
                FXCollections.observableArrayList(
                        "Pending",
                        "Paid"
                )
        );

        filterStatusBox.setItems(
                FXCollections.observableArrayList(
                        "All",
                        "Pending",
                        "Paid"
                )
        );

        filterStatusBox.setValue("All");

        sortTotalBox.setItems(
                FXCollections.observableArrayList(
                        "Total Low to High",
                        "Total High to Low"
                )
        );

        sortTotalBox.setValue("Total Low to High");
    }

    private void setupTableColumns() {

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        tableNumberColumn.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(
                        cellData.getValue()
                                .getTable()
                                .getTableNumber()
                ).asObject()
        );

        totalColumn.setCellValueFactory(
                new PropertyValueFactory<>("totalAmount")
        );

        totalColumn.setCellFactory(column -> new TableCell<Bill, Double>() {

            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);

                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$ %.2f", amount));
                }
            }
        });

        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("paymentStatus")
        );
    }

    private void setupFilters() {

        tableBox.setOnAction(e -> calculateTotal());

        searchTableField.textProperty().addListener(
                (obs, oldVal, newVal) -> applyFilterAndSort()
        );

        filterStatusBox.setOnAction(
                e -> applyFilterAndSort()
        );

        sortTotalBox.setOnAction(
                e -> applyFilterAndSort()
        );
    }

    private void setupTableSelection() {

        tableView.setOnMouseClicked(e -> {

            selectedBill = tableView.getSelectionModel().getSelectedItem();

            if (selectedBill == null) {
                return;
            }

            for (String tableText : tableBox.getItems()) {
                if (extractId(tableText) == selectedBill.getTable().getId()) {
                    tableBox.setValue(tableText);
                    break;
                }
            }

            totalField.setText(
                    String.format("%.2f", selectedBill.getTotalAmount())
            );

            statusBox.setValue(
                    selectedBill.getPaymentStatus()
            );

            addBillBtn.setText("Update Bill");
        });
    }

    private void loadTables() {

        try {
            tables = tableService.getUserTables();

            tableBox.getItems().clear();

            for (RestaurantTable table : tables) {
                tableBox.getItems().add(
                        "Table "
                        + table.getTableNumber()
                        + " (ID: "
                        + table.getId()
                        + ")"
                );
            }

        } catch (Exception e) {
            AlertUtil.showError(
                    "Error",
                    "Failed to load tables: " + e.getMessage()
            );
        }
    }

    private void calculateTotal() {

        if (tableBox.getValue() == null) {
            totalField.clear();
            return;
        }

        int tableId = extractId(tableBox.getValue());

        RestaurantTable table = findTableById(tableId);

        if (table == null) {
            totalField.clear();
            AlertUtil.showError(
                    "Validation Error",
                    "Selected table was not found."
            );
            return;
        }

        try {
            double total = orderRepo.calculateTableTotal(table, Session.getUserId());

            totalField.setText(
                    String.format("%.2f", total)
            );

        } catch (Exception e) {
            totalField.clear();

            AlertUtil.showError(
                    "Error",
                    "Failed to calculate table total: " + e.getMessage()
            );
        }
    }

    @FXML
    private void handleAddBill(ActionEvent event) {

        if (currentTask != null && currentTask.isRunning()) {
            AlertUtil.showWarning(
                    "Operation Running",
                    "Please wait for the current operation to finish or cancel it."
            );
            return;
        }

        if (tableBox.getValue() == null) {
            AlertUtil.showError(
                    "Validation Error",
                    "Select table."
            );
            return;
        }

        String status = statusBox.getValue();

        if (status == null) {
            AlertUtil.showError(
                    "Validation Error",
                    "Select payment status."
            );
            return;
        }

        int tableId = extractId(tableBox.getValue());
        RestaurantTable table = findTableById(tableId);

        if (table == null) {
            AlertUtil.showError(
                    "Validation Error",
                    "Selected table was not found."
            );
            return;
        }

        double total;

        try {
            total = Double.parseDouble(totalField.getText());
        } catch (Exception e) {
            AlertUtil.showError(
                    "Validation Error",
                    "Total amount is invalid."
            );
            return;
        }

        if (total <= 0) {
            AlertUtil.showError(
                    "No Orders",
                    "No orders found for the selected table."
            );
            return;
        }

        /*
         * This check requires this method in OrderService:
         *
         * public boolean hasOrdersForTable(int tableId, User user) {
         *     return OrderRepo.hasOrdersForTable(tableId, user.getId());
         * }
         */
        if (!orderService.hasOrdersForTable(table.getId(), Session.getCurrentUser())) {
            AlertUtil.showError(
                    "No Orders",
                    "No orders found for the selected table."
            );
            return;
        }

        Task<Bill> saveBillTask = new Task<>() {

            @Override
            protected Bill call() {

                updateMessage(
                        selectedBill == null
                                ? "Generating bill..."
                                : "Updating bill..."
                );

                updateProgress(0, 1);

                if (isCancelled()) {
                    updateMessage("Operation canceled.");
                    return null;
                }

                if (selectedBill == null) {

                    if (billService.pendingBillExistsForTable(table.getId(), Session.getUserId())) {
                        throw new IllegalArgumentException(
                                "A pending bill already exists for this table."
                        );
                    }

                    Bill bill = new Bill();

                    bill.setTable(table);
                    bill.setTotalAmount(total);
                    bill.setPaymentStatus(status);

                    billService.addBill(bill);

                    updateProgress(1, 1);
                    updateMessage("Bill generated successfully.");

                    return bill;

                } else {

                    selectedBill.setTable(table);
                    selectedBill.setTotalAmount(total);
                    selectedBill.setPaymentStatus(status);

                    billService.updateBill(selectedBill);

                    updateProgress(1, 1);
                    updateMessage("Bill updated successfully.");

                    return selectedBill;
                }
            }
        };

        currentTask = saveBillTask;

        bindTaskToUi(saveBillTask);

        saveBillTask.setOnSucceeded(e -> {

            cleanupTaskBindings();

            if (saveBillTask.isCancelled()) {
                statusLabel.setText("Canceled");
                return;
            }

            Bill savedBill = saveBillTask.getValue();

            if (savedBill != null) {
                try {
                    BillPrintDTO dto = BillPrintService.buildBill(savedBill);
                    billOutputArea.setText(formatBillText(dto));
                } catch (Exception ex) {
                    billOutputArea.setText("Bill saved, but failed to build bill output.");
                }
            }

            if (selectedBill == null) {
                AlertUtil.showInfo(
                        "Success",
                        "Bill generated successfully."
                );
            } else {
                AlertUtil.showInfo(
                        "Success",
                        "Bill updated successfully."
                );
            }

            selectedBill = null;
            addBillBtn.setText("Generate Bill");

            loadBills();
            clearFields();

            statusLabel.setText("Ready");
        });

        saveBillTask.setOnFailed(e -> {

            cleanupTaskBindings();

            Throwable ex = saveBillTask.getException();

            AlertUtil.showError(
                    "Error",
                    ex == null ? "Failed to save bill." : ex.getMessage()
            );

            statusLabel.setText("Failed");
        });

        Thread thread = new Thread(saveBillTask, "bill-save-task");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleDeleteBill(ActionEvent event) {

        Bill bill = tableView.getSelectionModel().getSelectedItem();

        if (bill == null) {
            AlertUtil.showError(
                    "Delete Error",
                    "Select bill first."
            );
            return;
        }

        try {
            billService.deleteBill(bill.getId());

            loadBills();
            clearFields();

            AlertUtil.showInfo(
                    "Success",
                    "Bill deleted successfully."
            );

        } catch (Exception e) {
            AlertUtil.showError(
                    "Error",
                    "Failed to delete bill: " + e.getMessage()
            );
        }
    }

    private void loadBills() {

        try {
            bills.setAll(
                    billService.getBills()
            );

            applyFilterAndSort();

        } catch (Exception e) {
            AlertUtil.showError(
                    "Error",
                    "Failed to load bills: " + e.getMessage()
            );
        }
    }

    private void clearFields() {

        tableBox.setValue(null);
        totalField.clear();
        statusBox.setValue(null);

        selectedBill = null;

        addBillBtn.setText("Generate Bill");

        tableView.getSelectionModel().clearSelection();
    }

    private void applyFilterAndSort() {

        if (bills == null) {
            return;
        }

        String searchText = searchTableField.getText().trim();
        String status = filterStatusBox.getValue();
        String sortOption = sortTotalBox.getValue();

        List<Bill> result = bills.stream()
                .filter(bill -> {

                    boolean matchesTable = searchText.isEmpty()
                            || String.valueOf(
                                    bill.getTable()
                                            .getTableNumber()
                            ).contains(searchText);

                    boolean matchesStatus = status == null
                            || status.equals("All")
                            || bill.getPaymentStatus()
                                    .equalsIgnoreCase(status);

                    return matchesTable && matchesStatus;
                })
                .sorted((a, b) -> {

                    if (sortOption == null) {
                        return 0;
                    }

                    if (sortOption.equals("Total Low to High")) {
                        return Double.compare(
                                a.getTotalAmount(),
                                b.getTotalAmount()
                        );
                    }

                    return Double.compare(
                            b.getTotalAmount(),
                            a.getTotalAmount()
                    );
                })
                .toList();

        tableView.setItems(
                FXCollections.observableArrayList(result)
        );
    }

    @FXML
    private void handlePrintBill(ActionEvent event) {

        if (currentTask != null && currentTask.isRunning()) {
            AlertUtil.showWarning(
                    "Operation Running",
                    "Please wait for the current operation to finish or cancel it."
            );
            return;
        }

        Bill selected = tableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showError(
                    "Error",
                    "Select a bill first."
            );
            return;
        }

        Task<BillPrintDTO> printTask = new Task<>() {

            @Override
            protected BillPrintDTO call() {

                updateMessage("Building bill preview...");
                updateProgress(0, 1);

                if (isCancelled()) {
                    updateMessage("Bill preview canceled.");
                    return null;
                }

                BillPrintDTO dto = BillPrintService.buildBill(selected);

                if (isCancelled()) {
                    updateMessage("Bill preview canceled.");
                    return null;
                }

                updateProgress(1, 1);
                updateMessage("Bill preview ready.");

                return dto;
            }
        };

        currentTask = printTask;

        bindTaskToUi(printTask);

        printTask.setOnSucceeded(e -> {

            cleanupTaskBindings();

            if (printTask.isCancelled() || printTask.getValue() == null) {
                statusLabel.setText("Canceled");
                return;
            }

            BillPrintDTO dto = printTask.getValue();

            billOutputArea.setText(formatBillText(dto));

            SceneUtil.openWindow(
                    "/view/BillPreview.fxml",
                    "Bill Preview",
                    dto
            );

            statusLabel.setText("Ready");
        });

        printTask.setOnFailed(e -> {

            cleanupTaskBindings();

            Throwable ex = printTask.getException();

            AlertUtil.showError(
                    "Error",
                    ex == null ? "Failed to build bill preview." : ex.getMessage()
            );

            statusLabel.setText("Failed");
        });

        Thread thread = new Thread(printTask, "bill-preview-task");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleCancelTask() {

        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel();
            AlertUtil.showInfo(
                    "Canceled",
                    "Current bill operation canceled."
            );
        }
    }

    @FXML
    private void backToDashboard(ActionEvent event) throws IOException {

        SceneUtil.switchScene(
                event,
                "/view/dashboard.fxml"
        );
    }

    private void bindTaskToUi(Task<?> task) {

        progressIndicator.visibleProperty().unbind();
        progressIndicator.visibleProperty().bind(task.runningProperty());

        statusLabel.textProperty().unbind();
        statusLabel.textProperty().bind(task.messageProperty());

        cancelBtn.disableProperty().unbind();
        cancelBtn.disableProperty().bind(task.runningProperty().not());

        addBillBtn.disableProperty().unbind();
        addBillBtn.disableProperty().bind(task.runningProperty());
    }

    private void cleanupTaskBindings() {

        progressIndicator.visibleProperty().unbind();
        progressIndicator.setVisible(false);

        statusLabel.textProperty().unbind();

        cancelBtn.disableProperty().unbind();
        cancelBtn.setDisable(true);

        addBillBtn.disableProperty().unbind();
        addBillBtn.setDisable(false);
    }

    private RestaurantTable findTableById(int tableId) {

        return tables.stream()
                .filter(t -> t.getId() == tableId)
                .findFirst()
                .orElse(null);
    }

    private int extractId(String text) {

        int start = text.indexOf("ID: ") + 4;
        int end = text.indexOf(")", start);

        return Integer.parseInt(
                text.substring(start, end).trim()
        );
    }

    private String formatBillText(BillPrintDTO bill) {

        StringBuilder sb = new StringBuilder();

        sb.append("RESTAURANT BILL\n");
        sb.append("============================\n");
        sb.append("Bill ID: ").append(bill.getBillId()).append("\n");
        sb.append("Table: ").append(bill.getTableNumber()).append("\n");
        sb.append("Status: ").append(bill.getStatus()).append("\n\n");

        for (var item : bill.getItems()) {
            sb.append(item.getItemName())
                    .append(" x")
                    .append(item.getQuantity())
                    .append(" -- Price: $")
                    .append(String.format("%.2f", item.getPrice()))
                    .append(" -- Total: $")
                    .append(String.format("%.2f", item.getTotal()))
                    .append("\n");
        }

        sb.append("\nTOTAL: $")
                .append(String.format("%.2f", bill.getTotal()));

        return sb.toString();
    }
}

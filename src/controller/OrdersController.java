package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javafx.scene.control.cell.PropertyValueFactory;

import model.MenuItem;
import model.Order;
import model.RestaurantTable;

import service.MenuService;
import service.OrderService;
import service.TableService;

import util.AlertUtil;
import util.SceneUtil;
import util.Session;

public class OrdersController implements Initializable {

    @FXML
    private ComboBox<String> tableBox;

    @FXML
    private ComboBox<String> itemBox;

    @FXML
    private TextField quantityField;

    @FXML
    private ComboBox<String> statusBox;

    @FXML
    private Button addOrderBtn;

    @FXML
    private Button deleteOrderBtn;

    @FXML
    private Button refreshBtn;

    @FXML
    private Button kitchenSummaryBtn;

    @FXML
    private Button cancelTaskBtn;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Label taskStatusLabel;

    @FXML
    private TextArea kitchenSummaryArea;

    @FXML
    private TableView<Order> tableView;

    @FXML
    private TableColumn<Order, Integer> idColumn;

    @FXML
    private TableColumn<Order, Integer> tableNumberColumn;

    @FXML
    private TableColumn<Order, String> itemNameColumn;

    @FXML
    private TableColumn<Order, Integer> quantityColumn;

    @FXML
    private TableColumn<Order, String> statusColumn;

    @FXML
    private ComboBox<String> filterStatusBox;

    @FXML
    private TextField searchTableIdField;

    @FXML
    private ComboBox<String> sortOrderBox;

    private final ObservableList<Order> orders = FXCollections.observableArrayList();

    private List<RestaurantTable> tables = new ArrayList<>();
    private List<MenuItem> menuItems = new ArrayList<>();

    private Order selectedOrder = null;

    private final OrderService orderService = new OrderService();
    private final TableService tableService = new TableService();
    private final MenuService menuItemService = new MenuService();

    private Task<List<Order>> loadOrdersTask;
    private Task<String> kitchenSummaryTask;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        setupComboBoxes();
        setupTableColumns();
        setupFilters();
        setupTableSelection();

        loadTablesAndMenuItems();
        loadOrdersDataInBackground();

        if (tables.isEmpty()) {
            AlertUtil.showWarning("No Tables", "Please add tables first.");
        }

        if (menuItems.isEmpty()) {
            AlertUtil.showWarning("No Menu Items", "Please add menu items first.");
        }
    }

    private void setupComboBoxes() {

        statusBox.setItems(FXCollections.observableArrayList(
                "Pending",
                "Preparing",
                "Served"
        ));

        filterStatusBox.setItems(FXCollections.observableArrayList(
                "All",
                "Pending",
                "Preparing",
                "Served"
        ));

        filterStatusBox.setValue("All");

        sortOrderBox.setItems(FXCollections.observableArrayList(
                "Quantity Low to High",
                "Quantity High to Low",
                "Status A to Z",
                "Status Z to A"
        ));
    }

    private void setupTableColumns() {

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        tableNumberColumn.setCellValueFactory(cellData
                -> new javafx.beans.property.SimpleIntegerProperty(
                        cellData.getValue()
                                .getTable()
                                .getTableNumber()
                ).asObject()
        );

        itemNameColumn.setCellValueFactory(cellData
                -> new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue()
                                .getItem()
                                .getName()
                )
        );

        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusColumn.setCellFactory(column -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(status);

                switch (status) {
                    case "Pending":
                        setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                        break;

                    case "Preparing":
                        setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
                        break;

                    case "Served":
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                        break;

                    default:
                        setStyle("");
                        break;
                }
            }
        });
    }

    private void setupFilters() {

        searchTableIdField.textProperty().addListener(
                (obs, oldVal, newVal) -> applyFilterAndSort()
        );

        filterStatusBox.setOnAction(e -> applyFilterAndSort());

        sortOrderBox.setOnAction(e -> applyFilterAndSort());
    }

    private void setupTableSelection() {

        tableView.setOnMouseClicked(event -> {

            selectedOrder = tableView.getSelectionModel().getSelectedItem();

            if (selectedOrder == null) {
                return;
            }

            for (String tableText : tableBox.getItems()) {
                if (extractId(tableText) == selectedOrder.getTable().getId()) {
                    tableBox.setValue(tableText);
                    break;
                }
            }

            for (String itemText : itemBox.getItems()) {
                if (extractId(itemText) == selectedOrder.getItem().getId()) {
                    itemBox.setValue(itemText);
                    break;
                }
            }

            quantityField.setText(String.valueOf(selectedOrder.getQuantity()));
            statusBox.setValue(selectedOrder.getStatus());

            addOrderBtn.setText("Edit Order");
        });
    }

    private void loadTablesAndMenuItems() {

        try {
            tables = tableService.getUserTables();
            menuItems = menuItemService.getItems();

            tableBox.getItems().clear();
            itemBox.getItems().clear();

            for (RestaurantTable table : tables) {
                tableBox.getItems().add(
                        "Table "
                        + table.getTableNumber()
                        + " (ID: "
                        + table.getId()
                        + ")"
                );
            }

            for (MenuItem item : menuItems) {
                itemBox.getItems().add(
                        item.getName()
                        + " - "
                        + String.format("%.2f", item.getPrice())
                        + " (ID: "
                        + item.getId()
                        + ")"
                );
            }

        } catch (Exception e) {
            AlertUtil.showError(
                    "Database Error",
                    "Failed to load tables or menu items."
            );
        }
    }

    private void loadOrdersDataInBackground() {

        if (loadOrdersTask != null && loadOrdersTask.isRunning()) {
            return;
        }

        loadOrdersTask = new Task<>() {

            @Override
            protected List<Order> call() {

                updateMessage("Loading orders...");
                updateProgress(0, 1);

                List<Order> result = orderService.getOrders(
                        Session.getCurrentUser()
                );

                if (isCancelled()) {
                    updateMessage("Loading canceled.");
                    return new ArrayList<>();
                }

                updateProgress(1, 1);
                updateMessage("Orders loaded.");

                return result;
            }
        };

        bindTaskToUi(loadOrdersTask);

        loadOrdersTask.setOnSucceeded(e -> {

            if (loadOrdersTask.isCancelled()) {
                taskStatusLabel.textProperty().unbind();
                taskStatusLabel.setText("Canceled");
                return;
            }

            orders.setAll(loadOrdersTask.getValue());
            applyFilterAndSort();

            taskStatusLabel.textProperty().unbind();
            taskStatusLabel.setText("Ready");

            progressIndicator.visibleProperty().unbind();
            progressIndicator.setVisible(false);

            cancelTaskBtn.disableProperty().unbind();
            cancelTaskBtn.setDisable(true);
        });

        loadOrdersTask.setOnFailed(e -> {

            taskStatusLabel.textProperty().unbind();
            taskStatusLabel.setText("Failed");

            progressIndicator.visibleProperty().unbind();
            progressIndicator.setVisible(false);

            cancelTaskBtn.disableProperty().unbind();
            cancelTaskBtn.setDisable(true);

            AlertUtil.showError(
                    "Error",
                    "Failed to load orders: "
                    + loadOrdersTask.getException().getMessage()
            );
        });

        Thread thread = new Thread(loadOrdersTask, "orders-load-task");
        thread.setDaemon(true);
        thread.start();
    }

    private void bindTaskToUi(Task<?> task) {

        progressIndicator.visibleProperty().unbind();
        progressIndicator.visibleProperty().bind(task.runningProperty());

        taskStatusLabel.textProperty().unbind();
        taskStatusLabel.textProperty().bind(task.messageProperty());

        cancelTaskBtn.disableProperty().unbind();
        cancelTaskBtn.disableProperty().bind(task.runningProperty().not());

        addOrderBtn.disableProperty().unbind();
        addOrderBtn.disableProperty().bind(task.runningProperty());

        deleteOrderBtn.disableProperty().unbind();
        deleteOrderBtn.disableProperty().bind(task.runningProperty());

        refreshBtn.disableProperty().unbind();
        refreshBtn.disableProperty().bind(task.runningProperty());

        kitchenSummaryBtn.disableProperty().unbind();
        kitchenSummaryBtn.disableProperty().bind(task.runningProperty());
    }

    @FXML
    private void handleRefreshOrders(ActionEvent event) {
        loadOrdersDataInBackground();
    }

    @FXML
    private void handleCancelTask(ActionEvent event) {

        boolean canceled = false;

        if (loadOrdersTask != null && loadOrdersTask.isRunning()) {
            loadOrdersTask.cancel();
            canceled = true;
        }

        if (kitchenSummaryTask != null && kitchenSummaryTask.isRunning()) {
            kitchenSummaryTask.cancel();
            canceled = true;
        }

        if (canceled) {
            AlertUtil.showInfo("Canceled", "Current operation canceled.");
        }
    }

    @FXML
    private void handleGenerateKitchenSummary(ActionEvent event) {

        if (kitchenSummaryTask != null && kitchenSummaryTask.isRunning()) {
            return;
        }

        kitchenSummaryTask = new Task<>() {

            @Override
            protected String call() {

                updateMessage("Generating kitchen summary...");
                updateProgress(0, 1);

                if (isCancelled()) {
                    updateMessage("Kitchen summary canceled.");
                    return "";
                }

                String summary = orderService.generateKitchenSummary(
                        Session.getCurrentUser()
                );

                if (isCancelled()) {
                    updateMessage("Kitchen summary canceled.");
                    return "";
                }

                updateProgress(1, 1);
                updateMessage("Kitchen summary generated.");

                return summary;
            }
        };

        bindTaskToUi(kitchenSummaryTask);

        kitchenSummaryTask.setOnSucceeded(e -> {

            if (!kitchenSummaryTask.isCancelled()) {
                kitchenSummaryArea.setText(kitchenSummaryTask.getValue());
            }

            taskStatusLabel.textProperty().unbind();
            taskStatusLabel.setText("Ready");

            progressIndicator.visibleProperty().unbind();
            progressIndicator.setVisible(false);

            cancelTaskBtn.disableProperty().unbind();
            cancelTaskBtn.setDisable(true);
        });

        kitchenSummaryTask.setOnFailed(e -> {

            taskStatusLabel.textProperty().unbind();
            taskStatusLabel.setText("Failed");

            progressIndicator.visibleProperty().unbind();
            progressIndicator.setVisible(false);

            cancelTaskBtn.disableProperty().unbind();
            cancelTaskBtn.setDisable(true);

            AlertUtil.showError(
                    "Error",
                    "Failed to generate kitchen summary: "
                    + kitchenSummaryTask.getException().getMessage()
            );
        });

        Thread thread = new Thread(kitchenSummaryTask, "kitchen-summary-task");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleAddOrder(ActionEvent event) {

        if (tables.isEmpty() || menuItems.isEmpty()) {
            AlertUtil.showError(
                    "Missing Data",
                    "Please add tables and menu items first."
            );
            return;
        }

        String selectedTable = tableBox.getValue();
        String selectedItem = itemBox.getValue();
        String quantityText = quantityField.getText().trim();
        String status = statusBox.getValue();

        if (selectedTable == null
                || selectedItem == null
                || quantityText.isEmpty()
                || status == null) {

            AlertUtil.showError(
                    "Validation Error",
                    "Please complete all fields."
            );
            return;
        }

        int quantity;

        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            AlertUtil.showError(
                    "Validation Error",
                    "Quantity must be a numeric value."
            );
            return;
        }

        if (quantity <= 0) {
            AlertUtil.showError(
                    "Validation Error",
                    "Quantity must be greater than zero."
            );
            return;
        }

        try {
            int tableId = extractId(selectedTable);
            int itemId = extractId(selectedItem);

            RestaurantTable table = tables.stream()
                    .filter(t -> t.getId() == tableId)
                    .findFirst()
                    .orElse(null);

            MenuItem item = menuItems.stream()
                    .filter(i -> i.getId() == itemId)
                    .findFirst()
                    .orElse(null);

            if (table == null || item == null) {
                AlertUtil.showError(
                        "Validation Error",
                        "Invalid table or menu item selected."
                );
                return;
            }
            if (selectedOrder == null) {

                boolean tableHasActiveOrders = orderService.hasActiveOrdersForTable(
                        tableId,
                        Session.getCurrentUser()
                );

                if (tableHasActiveOrders) {
                    AlertUtil.showInfo(
                            "Table Active",
                            "This table already has active orders. The new item will be added to the same active table."
                    );
                }
            }

            if (selectedOrder == null) {

                Order order = new Order();

                order.setTable(table);
                order.setItem(item);
                order.setQuantity(quantity);
                order.setStatus(status);

                orderService.createOrder(order, Session.getCurrentUser());

                AlertUtil.showInfo(
                        "Success",
                        "Order added successfully."
                );

            } else {

                selectedOrder.setTable(table);
                selectedOrder.setItem(item);
                selectedOrder.setQuantity(quantity);
                selectedOrder.setStatus(status);

                orderService.updateOrderWithValidation(
                        selectedOrder,
                        Session.getCurrentUser()
                );

                selectedOrder = null;
                addOrderBtn.setText("Add Order");

                AlertUtil.showInfo(
                        "Success",
                        "Order updated successfully."
                );
            }

            loadOrdersDataInBackground();
            clearFields();

        } catch (IllegalArgumentException e) {

            AlertUtil.showError(
                    "Invalid Order Update",
                    e.getMessage()
            );

        } catch (SQLException e) {

            AlertUtil.showError(
                    "Database Error",
                    "Failed to save order. Please check table/menu item relationship."
            );
        }
    }

    @FXML
    private void handleDeleteOrder(ActionEvent event) {

        Order selected = tableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showError(
                    "Delete Error",
                    "Please select an order to delete."
            );
            return;
        }

        try {
            orderService.deleteOrder(
                    selected.getId(),
                    Session.getCurrentUser()
            );

            loadOrdersDataInBackground();
            clearFields();

            selectedOrder = null;
            addOrderBtn.setText("Add Order");

            AlertUtil.showInfo(
                    "Success",
                    "Order deleted successfully."
            );

        } catch (SQLException e) {
            AlertUtil.showError(
                    "Database Error",
                    "Failed to delete order from database."
            );
        }
    }

    private void applyFilterAndSort() {

        if (orders == null) {
            return;
        }

        String tableNumberText = searchTableIdField.getText().trim();
        String status = filterStatusBox.getValue();
        String sortOption = sortOrderBox.getValue();

        List<Order> result = orders.stream()
                .filter(order -> {

                    boolean matchesStatus
                            = status == null
                            || status.equals("All")
                            || order.getStatus().equalsIgnoreCase(status);

                    boolean matchesTable
                            = tableNumberText.isEmpty()
                            || String.valueOf(
                                    order.getTable().getTableNumber()
                            ).contains(tableNumberText);

                    return matchesStatus && matchesTable;
                })
                .sorted((a, b) -> {

                    if (sortOption == null) {
                        return 0;
                    }

                    switch (sortOption) {

                        case "Quantity Low to High":
                            return Integer.compare(
                                    a.getQuantity(),
                                    b.getQuantity()
                            );

                        case "Quantity High to Low":
                            return Integer.compare(
                                    b.getQuantity(),
                                    a.getQuantity()
                            );

                        case "Status A to Z":
                            return a.getStatus()
                                    .compareToIgnoreCase(b.getStatus());

                        case "Status Z to A":
                            return b.getStatus()
                                    .compareToIgnoreCase(a.getStatus());

                        default:
                            return 0;
                    }
                })
                .toList();

        tableView.setItems(
                FXCollections.observableArrayList(result)
        );
    }

    private int extractId(String text) {

        int start = text.indexOf("ID: ") + 4;
        int end = text.indexOf(")", start);

        return Integer.parseInt(
                text.substring(start, end).trim()
        );
    }

    private void clearFields() {

        tableBox.setValue(null);
        itemBox.setValue(null);
        quantityField.clear();
        statusBox.setValue(null);

        tableView.getSelectionModel().clearSelection();

        selectedOrder = null;
        addOrderBtn.setText("Add Order");
    }

    @FXML
    private void backToDashboard(ActionEvent event) throws IOException {
        SceneUtil.switchScene(event, "/view/dashboard.fxml");
    }
}

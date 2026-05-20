package controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.MenuItem;
import model.Order;
import model.RestaurantTable;
import javafx.scene.control.TableCell;
import util.AlertUtil;
import java.sql.SQLException;
import javafx.collections.ObservableList;
import service.MenuService;
import service.OrderService;
import service.TableService;
import util.SceneUtil;
import util.Session;

/**
 * FXML Controller class
 *
 * @author hp
 */
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

    private ObservableList<Order> orders = FXCollections.observableArrayList();
    private List<RestaurantTable> tables;
    private List<MenuItem> menuItems;
    @FXML
    private ComboBox<String> filterStatusBox;
    @FXML
    private TextField searchTableIdField;
    @FXML
    private ComboBox<String> sortOrderBox;
    private Order selectedOrder = null;

    private final OrderService orderService = new OrderService();
    private final TableService tableService = new TableService();
    private final MenuService menuItemService = new MenuService();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //Loading tables and items
        try {
            tables = tableService.getUserTables();
            menuItems = menuItemService.getItems();

            for (RestaurantTable table : tables) {
                tableBox.getItems().add("Table " + table.getTableNumber() + " (ID: " + table.getId() + ")");
            }

            for (MenuItem item : menuItems) {
                itemBox.getItems().add(
                        item.getName() + " - " + String.format("%.2f", item.getPrice()) + " (ID: " + item.getId() + ")"
                );
            }

        } catch (Exception e) {
            AlertUtil.showError("Database Error", "Failed to load tables or menu items.");
        }

        //Fill statusBox
        statusBox.setItems(FXCollections.observableArrayList(
                "Pending", "Preparing", "Served"
        ));

        //Fill filterStatusBox
        filterStatusBox.setItems(FXCollections.observableArrayList(
                "All", "Pending", "Preparing", "Served"
        ));
        filterStatusBox.setValue("All");
        //Fill sortOrderBox
        sortOrderBox.setItems(FXCollections.observableArrayList(
                "Quantity Low to High",
                "Quantity High to Low",
                "Status A to Z",
                "Status Z to A"
        ));
        //Action
        searchTableIdField.textProperty().addListener((obs, oldVal, newVal) -> applyFilterAndSort());

        filterStatusBox.setOnAction(e -> applyFilterAndSort());

        sortOrderBox.setOnAction(e -> applyFilterAndSort());

        //Linking table columns to properties
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
//        Color the status column
        statusColumn.setCellFactory(column -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
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
                    }
                }
            }
        });

        loadOrdersData();

        tableView.setOnMouseClicked(event -> {

            selectedOrder = tableView.getSelectionModel().getSelectedItem();

            if (selectedOrder != null) {

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
            }
        });

        if (tables.isEmpty()) {
            AlertUtil.showWarning("No Tables", "Please add tables first.");
        }

        if (menuItems.isEmpty()) {
            AlertUtil.showWarning("No Menu Items", "Please add menu items first.");
        }
    }

    private void loadOrdersData() {
        orders.setAll(
                orderService.getOrders(
                        Session.getCurrentUser()
                )
        );

        applyFilterAndSort();
    }

    @FXML
    private void handleAddOrder(ActionEvent event) {
        if (tables.isEmpty() || menuItems.isEmpty()) {
            AlertUtil.showError("Missing Data", "Please add tables and menu items first.");
            return;
        }
        String selectedTable = tableBox.getValue();
        String selectedItem = itemBox.getValue();
        String quantityText = quantityField.getText().trim();
        String status = statusBox.getValue();

        if (selectedTable == null || selectedItem == null || quantityText.isEmpty() || status == null) {
            AlertUtil.showError("Validation Error", "Please complete all fields.");
            return;
        }
        //Convert quantity to number
        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            AlertUtil.showError("Validation Error", "Quantity must be a numeric value.");
            return;
        }

        if (quantity <= 0) {
            AlertUtil.showError("Validation Error", "Quantity must be greater than zero.");
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

            Order order = new Order();

            order.setTable(table);
            order.setItem(item);
            order.setQuantity(quantity);
            order.setStatus(statusBox.getValue());

            if (selectedOrder == null) {
                orderService.createOrder(order, Session.getCurrentUser());

                AlertUtil.showInfo("Success", "Order added successfully.");
            } else {
                selectedOrder.setTable(table);
                selectedOrder.setItem(item);
                selectedOrder.setQuantity(quantity);
                selectedOrder.setStatus(status);

                orderService.updateOrder(selectedOrder, Session.getCurrentUser());

                selectedOrder = null;
                addOrderBtn.setText("Add Order");

                AlertUtil.showInfo("Success", "Order updated successfully.");
            }

            loadOrdersData();
            clearFields();

        } catch (SQLException e) {
            AlertUtil.showError("Database Error",
                    "Failed to save order. Please check table/menu item relationship.");
        }

    }

    @FXML
    private void backToDashboard(ActionEvent event) throws IOException {
        SceneUtil.switchScene(event, "/view/dashboard.fxml");

    }

    private int extractId(String text) {
        int start = text.indexOf("ID: ") + 4;
        int end = text.indexOf(")", start);
        return Integer.parseInt(text.substring(start, end).trim());
    }

    private void clearFields() {
        tableBox.setValue(null);
        itemBox.setValue(null);
        quantityField.clear();
        statusBox.setValue(null);
    }

    @FXML
    private void handleDeleteOrder(ActionEvent event) {
        Order selected = tableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showError("Delete Error", "Please select an order to delete.");
            return;
        }

        try {
            orderService.deleteOrder(selected.getId(), Session.getCurrentUser());

            loadOrdersData();
            clearFields();
            selectedOrder = null;
            addOrderBtn.setText("Add Order");

            AlertUtil.showInfo("Success", "Order deleted successfully.");

        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Failed to delete order from database.");
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
                //filter
                .filter(order -> {

                    boolean matchesStatus = (status == null || status.equals("All"))
                            || order.getStatus().equalsIgnoreCase(status);

                    boolean matchesTable = tableNumberText.isEmpty()
                            || String.valueOf(order.getTable().getTableNumber()).contains(tableNumberText);

                    return matchesStatus && matchesTable;
                })
                //sort
                .sorted((a, b) -> {
                    if (sortOption == null) {
                        return 0;
                    }

                    switch (sortOption) {
                        case "Quantity Low to High":
                            return Integer.compare(a.getQuantity(), b.getQuantity());
                        case "Quantity High to Low":
                            return Integer.compare(b.getQuantity(), a.getQuantity());
                        case "Status A to Z":
                            return a.getStatus().compareToIgnoreCase(b.getStatus());
                        default:
                            return b.getStatus().compareToIgnoreCase(a.getStatus());
                    }
                })
                .toList();

        tableView.setItems(FXCollections.observableArrayList(result));
    }

}

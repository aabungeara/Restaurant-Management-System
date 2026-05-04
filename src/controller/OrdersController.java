package controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.MenuItem;
import model.Order;
import model.RestaurantTable;
import util.FileUtil;
import javafx.scene.control.TableCell;
import util.AlertUtil;

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
    private TableColumn<Order, Integer> itemNameColumn;
    @FXML
    private TableColumn<Order, Integer> quantityColumn;
    @FXML
    private TableColumn<Order, String> statusColumn;

    private List<Order> orders;
    private List<RestaurantTable> tables;
    private List<MenuItem> menuItems;
    @FXML
    private ComboBox<String> filterStatusBox;
    @FXML
    private TextField searchTableIdField;
    @FXML
    private ComboBox<String> sortOrderBox;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        FileUtil.ensureFiles();

        //Loading tables and items
        tables = FileUtil.loadTables();
        menuItems = FileUtil.loadMenuItems();
        //Fill tableBox
        for (RestaurantTable table : tables) {
            tableBox.getItems().add("Table " + table.getTableNumber() + " (ID: " + table.getId() + ")");
        }
        //Fill itemBox
        for (MenuItem item : menuItems) {
            itemBox.getItems().add(
                    item.getName() + " - " + String.format("%.2f", item.getPrice()) + " (ID: " + item.getId() + ")"
            );
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
        tableNumberColumn.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
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

        if (tables.isEmpty()) {
            AlertUtil.showWarning( "No Tables", "Please add tables first.");
        }

        if (menuItems.isEmpty()) {
            AlertUtil.showWarning( "No Menu Items", "Please add menu items first.");
        }
    }

    private void loadOrdersData() {
        orders = FileUtil.loadOrders();

        for (Order order : orders) {
            //Convert tableId to tableNumber
            for (RestaurantTable table : tables) {
                if (table.getId() == order.getTableId()) {
                    order.setTableNumber(table.getTableNumber());
                    break;
                }
            }
            //Convert itemId to itemName
            for (MenuItem item : menuItems) {
                if (item.getId() == order.getItemId()) {
                    order.setItemName(item.getName());
                    break;
                }
            }
        }

        tableView.setItems(FXCollections.observableArrayList(orders));
    }

    @FXML
    private void handleAddOrder(ActionEvent event) {
        if (tables.isEmpty() || menuItems.isEmpty()) {
            AlertUtil.showError( "Missing Data", "Please add tables and menu items first.");
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
            AlertUtil.showError( "Validation Error", "Quantity must be a numeric value.");
            return;
        }

        if (quantity <= 0) {
            AlertUtil.showError( "Validation Error", "Quantity must be greater than zero.");
            return;
        }

        //Extracting IDs from the displayed text
        int tableId = extractId(selectedTable);
        int itemId = extractId(selectedItem);
        //Generate a new order ID
        int newId = FileUtil.getNextOrderId(orders);
        Order newOrder = new Order(newId, tableId, itemId, quantity, status);

        orders.add(newOrder);
        FileUtil.saveOrders(orders);
        loadOrdersData();
        clearFields();

        AlertUtil.showInfo("Success", "Order added successfully.");

    }

    @FXML
    private void backToDashboard(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();

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
        Order selectedOrder = tableView.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            AlertUtil.showError("Delete Error", "Please select an order to delete.");
            return;
        }

        orders.remove(selectedOrder);
        FileUtil.saveOrders(orders);
        loadOrdersData();
        clearFields();

        AlertUtil.showInfo("Success", "Order deleted successfully.");
    }

    private void applyFilterAndSort() {

        String status = filterStatusBox.getValue();
        String tableIdText = searchTableIdField.getText().trim();
        String sortOption = sortOrderBox.getValue();

        List<Order> result = orders.stream()
                //filter
                .filter(order -> {

                    boolean matchesStatus = (status == null || status.equals("All"))
                            || order.getStatus().equalsIgnoreCase(status);

                    boolean matchesTable = tableIdText.isEmpty()
                            || String.valueOf(order.getTableNumber()).contains(tableIdText);

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
        if (!tableIdText.isEmpty() && result.isEmpty()) {
            AlertUtil.showWarning("No Results", "No Order found with this table id.");
        }

        tableView.setItems(FXCollections.observableArrayList(result));
    }

}

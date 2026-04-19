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
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableCell;

/**
 * FXML Controller class
 *
 * @author hp
 */
public class OrdersController implements Initializable {

    @FXML
    private TextField searchField;
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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        FileUtil.ensureFiles();

        tables = FileUtil.loadTables();
        menuItems = FileUtil.loadMenuItems();

        for (RestaurantTable table : tables) {
            tableBox.getItems().add("Table " + table.getTableNumber() + " (ID: " + table.getId() + ")");
        }

        for (MenuItem item : menuItems) {
            itemBox.getItems().add(
                    item.getName() + " - " + String.format("%.2f", item.getPrice()) + " (ID: " + item.getId() + ")"
            );
        }

        statusBox.setItems(FXCollections.observableArrayList(
                "Pending", "Preparing", "Served"
        ));

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableNumberColumn.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
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
            showAlert(Alert.AlertType.WARNING, "No Tables", "Please add tables first.");
        }

        if (menuItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Menu Items", "Please add menu items first.");
        }
    }

    private void loadOrdersData() {
        orders = FileUtil.loadOrders();

        for (Order order : orders) {
            for (RestaurantTable table : tables) {
                if (table.getId() == order.getTableId()) {
                    order.setTableNumber(table.getTableNumber());
                    break;
                }
            }

            for (MenuItem item : menuItems) {
                if (item.getId() == order.getItemId()) {
                    order.setItemName(item.getName());
                    break;
                }
            }
        }
        FilteredList<Order> filteredData
                = new FilteredList<>(FXCollections.observableArrayList(orders), b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(order -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String key = newValue.toLowerCase();

                if (String.valueOf(order.getTableNumber()).contains(key)) {
                    return true;
                } else if (order.getItemName() != null && order.getItemName().toLowerCase().contains(key)) {
                    return true;
                } else if (order.getStatus().toLowerCase().contains(key)) {
                    return true;
                } else {
                    return String.valueOf(order.getQuantity()).contains(key);
                }
            });
        });

        SortedList<Order> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    @FXML
    private void handleAddOrder(ActionEvent event) {
        if (tables.isEmpty() || menuItems.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Data", "Please add tables and menu items first.");
            return;
        }
        String selectedTable = tableBox.getValue();
        String selectedItem = itemBox.getValue();
        String quantityText = quantityField.getText().trim();
        String status = statusBox.getValue();

        if (selectedTable == null || selectedItem == null || quantityText.isEmpty() || status == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please complete all fields.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Quantity must be a numeric value.");
            return;
        }

        if (quantity <= 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Quantity must be greater than zero.");
            return;
        }

        
        int tableId = extractId(selectedTable);
        int itemId = extractId(selectedItem);

        int newId = FileUtil.getNextOrderId(orders);
        Order newOrder = new Order(newId, tableId, itemId, quantity, status);

        orders.add(newOrder);
        FileUtil.saveOrders(orders);
        loadOrdersData();
        clearFields();

        showAlert(Alert.AlertType.INFORMATION, "Success", "Order added successfully.");

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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleDeleteOrder(ActionEvent event) {
        Order selectedOrder = tableView.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            showAlert(Alert.AlertType.ERROR, "Delete Error", "Please select an order to delete.");
            return;
        }

        orders.remove(selectedOrder);
        FileUtil.saveOrders(orders);
        loadOrdersData();
        clearFields();

        showAlert(Alert.AlertType.INFORMATION, "Success", "Order deleted successfully.");
    }

}

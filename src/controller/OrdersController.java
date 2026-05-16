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

import javafx.scene.control.TableCell;
import util.AlertUtil;
import dao.OrderDAO;
import dao.TableDAO;
import dao.MenuItemDAO;
import java.sql.SQLException;

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
    private Order selectedOrder = null;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //Loading tables and items
        try {
            tables = TableDAO.getAllTables();
            menuItems = MenuItemDAO.getAllMenuItems();

            for (RestaurantTable table : tables) {
                tableBox.getItems().add("Table " + table.getTableNumber() + " (ID: " + table.getId() + ")");
            }

            for (MenuItem item : menuItems) {
                itemBox.getItems().add(
                        item.getName() + " - " + String.format("%.2f", item.getPrice()) + " (ID: " + item.getId() + ")"
                );
            }

        } catch (SQLException e) {
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

        tableView.setOnMouseClicked(event -> {

            selectedOrder = tableView.getSelectionModel().getSelectedItem();

            if (selectedOrder != null) {

                for (String tableText : tableBox.getItems()) {
                    if (extractId(tableText) == selectedOrder.getTableId()) {
                        tableBox.setValue(tableText);
                        break;
                    }
                }

                for (String itemText : itemBox.getItems()) {
                    if (extractId(itemText) == selectedOrder.getItemId()) {
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
        try {
            orders = OrderDAO.getAllOrders();
            tableView.setItems(FXCollections.observableArrayList(orders));
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Failed to load orders from database.");
        }
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

            if (selectedOrder == null) {
                Order newOrder = new Order(0, tableId, itemId, quantity, status);
                OrderDAO.insertOrder(newOrder);

                AlertUtil.showInfo("Success", "Order added successfully.");
            } else {
                selectedOrder.setTableId(tableId);
                selectedOrder.setItemId(itemId);
                selectedOrder.setQuantity(quantity);
                selectedOrder.setStatus(status);

                OrderDAO.updateOrder(selectedOrder);

                selectedOrder = null;
                addOrderBtn.setText("Add Order");

                AlertUtil.showInfo("Success", "Order updated successfully.");
            }

            loadOrdersData();
            clearFields();
            applyFilterAndSort();

        } catch (SQLException e) {
            AlertUtil.showError("Database Error",
                    "Failed to save order. Please check table/menu item relationship.");
        }

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
        Order selected = tableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showError("Delete Error", "Please select an order to delete.");
            return;
        }

        try {
            OrderDAO.deleteOrder(selected.getId());

            loadOrdersData();
            clearFields();
            selected = null;
            addOrderBtn.setText("Add Order");
            applyFilterAndSort();

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
                            || String.valueOf(order.getTableNumber()).contains(tableNumberText);

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
        if (!tableNumberText.isEmpty() && result.isEmpty()) {
            AlertUtil.showWarning("No Results", "No Order found with this table Number.");
        }

        tableView.setItems(FXCollections.observableArrayList(result));
    }

}

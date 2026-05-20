/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
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
import model.RestaurantTable;
import util.AlertUtil;
import java.sql.SQLException;
import java.util.ArrayList;
import javafx.scene.control.Label;
import model.User;
import service.TableService;
import util.SceneUtil;
import util.Session;

/**
 * FXML Controller class
 *
 * @author hp
 */
public class TablesController implements Initializable {

    @FXML
    private TextField tableNumberField;
    @FXML
    private TextField capacityField;
    @FXML
    private Button addEditBtn;
    @FXML
    private TableView<RestaurantTable> tableView;
    @FXML
    private TableColumn<RestaurantTable, Integer> idColumn;
    @FXML
    private TableColumn<RestaurantTable, Integer> tableNumberColumn;
    @FXML
    private TableColumn<RestaurantTable, Integer> capacityColumn;

    private List<RestaurantTable> tables;
    private RestaurantTable selectedTable = null;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> sortByCapacity;
    private final TableService tableService = new TableService();

    /**
     * Initializes the controller class.
     */
    @Override
    //to load data in open table scene 
    public void initialize(URL url, ResourceBundle rb) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableNumberColumn.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));

        tableView.setPlaceholder(new Label("No tables found"));

        loadTableData();

        //fills sortByCapacity item
        sortByCapacity.setItems(FXCollections.observableArrayList(
                "Capacity Low to High",
                "Capacity High to Low"
        ));

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilterAndSort());

        sortByCapacity.setOnAction(e -> applyFilterAndSort());

        tableView.setOnMouseClicked(event -> {
            selectedTable = tableView.getSelectionModel().getSelectedItem();

            if (selectedTable != null) {
                tableNumberField.setText(String.valueOf(selectedTable.getTableNumber()));
                capacityField.setText(String.valueOf(selectedTable.getCapacity()));
                addEditBtn.setText("Edit Table");
            }
        });
    }

    private void loadTableData() {
        try {
            tables = tableService.getUserTables();

            if (tables == null) {
                tables = new ArrayList<>();
            }

            applyFilterAndSort();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to load tables");
        }
    }
    @FXML
    private void handleDeleteMenuItem(ActionEvent event) throws Exception {
        RestaurantTable selected = tableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showError("Delete Error", "Please select a menu item to delete.");
            return;
        }

        try {
            tableService.deleteTable(selected.getId());

            loadTableData();
            clearFields();
            selectedTable = null;
            addEditBtn.setText("Add Menu Item");
            

            AlertUtil.showInfo("Success", "Menu item deleted successfully.");

        } catch (SQLException e) {
            AlertUtil.showError("Database Error",
                    "Failed to delete menu item. It may be used by an order.");
        }
    }

    @FXML
    private void handleAddOrEditTable(ActionEvent event) throws SQLException, Exception {
        String tableNumberText = tableNumberField.getText().trim();
        String capacityText = capacityField.getText().trim();

        if (tableNumberText.isEmpty() || capacityText.isEmpty()) {
            AlertUtil.showError("Validation Error", "Please fill in all fields.");
            return;
        }
        //convert string to integer
        int tableNumber;
        int capacity;

        try {
            tableNumber = Integer.parseInt(tableNumberText);
            capacity = Integer.parseInt(capacityText);
        } catch (NumberFormatException e) {
            AlertUtil.showError("Validation Error", "Table number and capacity must be numeric.");
            return;
        }
        if (tableNumber <= 0 || capacity <= 0) {
            AlertUtil.showError("Validation Error", "Values must be greater than zero.");
            return;
        }

        try {
            // Add new table
            if (selectedTable == null) {

                if (tableService.isDuplicate(tableNumber, 0)) {
                    AlertUtil.showError("Validation Error", "Duplicate table number.");
                    return;
                }
                User user = Session.getCurrentUser();

                RestaurantTable newTable
                        = new RestaurantTable(tableNumber, capacity, user);

                tableService.addTable(newTable);

                AlertUtil.showInfo("Success", "Table added successfully.");

            } // Edit selected table
            else {

                if (tableService.isDuplicate(tableNumber, selectedTable.getId())) {
                    AlertUtil.showError("Validation Error", "Duplicate table number.");
                    return;
                }

                selectedTable.setTableNumber(tableNumber);
                selectedTable.setCapacity(capacity);

                tableService.updateTable(selectedTable);

                selectedTable = null;
                addEditBtn.setText("Add Table");

                AlertUtil.showInfo("Success", "Table updated successfully.");
            }

            // Reload live data from database
            loadTableData();

            // Clear input fields
            clearFields();

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "Failed to save table data.");
        }
    }

    @FXML
    private void backToDashboard(ActionEvent event) throws IOException {
        SceneUtil.switchScene(event, "/view/dashboard.fxml");
    }

    private void clearFields() {
        tableNumberField.clear();
        capacityField.clear();
        tableView.getSelectionModel().clearSelection();
    }

    private void applyFilterAndSort() {
        String searchText = searchField.getText().trim();
        String sortOption = sortByCapacity.getValue();

        List<RestaurantTable> result = tables.stream()
                .filter(table -> {
                    boolean matchesSearch = searchText.isEmpty()
                            || String.valueOf(table.getTableNumber()).contains(searchText);

                    return matchesSearch;
                })
                .sorted((a, b) -> {
                    if (sortOption == null) {
                        return 0;
                    }

                    if (sortOption.equals("Capacity Low to High")) {
                        return Integer.compare(a.getCapacity(), b.getCapacity());

                    } else {
                        return Integer.compare(b.getCapacity(), a.getCapacity());
                    }
                })
                .toList();
        tableView.setItems(FXCollections.observableArrayList(result));

    }

    

}

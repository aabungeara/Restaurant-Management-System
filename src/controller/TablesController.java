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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.RestaurantTable;
import util.FileUtil;

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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        FileUtil.ensureFiles();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableNumberColumn.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));

        loadTableData();

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
        tables = FileUtil.loadTables();
        tableView.setItems(FXCollections.observableArrayList(tables));
    }

    @FXML
    private void handleAddOrEditTable(ActionEvent event) {
        String tableNumberText = tableNumberField.getText().trim();
        String capacityText = capacityField.getText().trim();

        if (tableNumberText.isEmpty() || capacityText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill in all fields.");
            return;
        }

        int tableNumber;
        int capacity;

        try {
            tableNumber = Integer.parseInt(tableNumberText);
            capacity = Integer.parseInt(capacityText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Table number and capacity must be numeric.");
            return;
        }
        if (tableNumber <= 0 || capacity <= 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Values must be greater than zero.");
            return;
        }

        for (RestaurantTable table : tables) {
            if (table.getTableNumber() == tableNumber &&
                (selectedTable == null || table.getId() != selectedTable.getId())) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Duplicate table number.");
                return;
            }
        }

        if (selectedTable == null) {
            int newId = FileUtil.getNextTableId(tables);
            RestaurantTable newTable = new RestaurantTable(newId, tableNumber, capacity);
            tables.add(newTable);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Table added successfully.");
        }else {
            selectedTable.setTableNumber(tableNumber);
            selectedTable.setCapacity(capacity);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Table updated successfully.");
            selectedTable = null;
            addEditBtn.setText("Add Table");
        }

        FileUtil.saveTables(tables);
        loadTableData();
        clearFields();
    


    }

    @FXML
    private void backToDashboard(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
    
    private void clearFields() {
        tableNumberField.clear();
        capacityField.clear();
        tableView.getSelectionModel().clearSelection();
    }

    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
}

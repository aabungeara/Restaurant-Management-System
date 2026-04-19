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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.MenuItem;
import util.FileUtil;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

public class MenuController implements Initializable {

    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private ComboBox<String> categoryBox;
    @FXML
    private Button addEditBtn;
    @FXML
    private TableView<MenuItem> tableView;
    @FXML
    private TableColumn<MenuItem, Integer> idColumn;
    @FXML
    private TableColumn<MenuItem, String> nameColumn;
    @FXML
    private TableColumn<MenuItem, Double> priceColumn;
    @FXML
    private TableColumn<MenuItem, String> categoryColumn;
    @FXML
    private TextField searchField;
    private List<MenuItem> items;
    private MenuItem selectedItem = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        FileUtil.ensureFiles();

        categoryBox.setItems(FXCollections.observableArrayList(
                "Main Course", "Drinks", "Dessert", "Appetizer"
        ));

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setCellFactory(column -> new TableCell<MenuItem, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);

                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$ %.2f", price));
                }
            }
        });
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        
        setupSearchAndTable();

        tableView.setOnMouseClicked(event -> {
            selectedItem = tableView.getSelectionModel().getSelectedItem();

            if (selectedItem != null) {
                nameField.setText(selectedItem.getName());
                priceField.setText(String.valueOf(selectedItem.getPrice()));
                categoryBox.setValue(selectedItem.getCategory());
                addEditBtn.setText("Edit Menu Item");
            }
        });

    }

    private void loadMenuData() {
        items = FileUtil.loadMenuItems();

        FilteredList<MenuItem> filteredData
                = new FilteredList<>(FXCollections.observableArrayList(items), b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(item -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String searchKey = newValue.toLowerCase();

                return item.getName().toLowerCase().contains(searchKey)
                        || item.getCategory().toLowerCase().contains(searchKey)
                        || String.valueOf(item.getPrice()).contains(searchKey);
            });
        });
        SortedList<MenuItem> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());

        tableView.setItems(sortedData);
    }

    @FXML
    private void handleAddOrEditMenuItem(ActionEvent event) {
        String name = nameField.getText().trim();
        String priceText = priceField.getText().trim();
        String category = categoryBox.getValue();

        if (name.isEmpty() || priceText.isEmpty() || category == null || category.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill in all fields.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Price must be a valid number.");
            return;
        }
        if (price <= 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Price must be greater than zero.");
            return;
        }

        for (MenuItem item : items) {
            if (item.getName().equalsIgnoreCase(name)
                    && (selectedItem == null || item.getId() != selectedItem.getId())) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Duplicate item name.");
                return;
            }
        }
        if (selectedItem == null) {
            int newId = FileUtil.getNextMenuItemId(items);
            MenuItem newItem = new MenuItem(newId, name, price, category);
            items.add(newItem);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Menu item added successfully.");
        } else {
            selectedItem.setName(name);
            selectedItem.setPrice(price);
            selectedItem.setCategory(category);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Menu item updated successfully.");
            selectedItem = null;
            addEditBtn.setText("Add Menu Item");
        }
        FileUtil.saveMenuItems(items);
        setupSearchAndTable();
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
        nameField.clear();
        priceField.clear();
        categoryBox.setValue(null);
        tableView.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleDeleteMenuItem(ActionEvent event) {
        MenuItem selected = tableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.ERROR, "Delete Error", "Please select a menu item to delete.");
            return;
        }

        items.remove(selected);
        FileUtil.saveMenuItems(items);
        setupSearchAndTable();
        clearFields();
        selectedItem = null;
        addEditBtn.setText("Add Menu Item");

        showAlert(Alert.AlertType.INFORMATION, "Success", "Menu item deleted successfully.");

    }

    private void setupSearchAndTable() {
        items = FileUtil.loadMenuItems();

        FilteredList<MenuItem> filteredData
                = new FilteredList<>(FXCollections.observableArrayList(items), b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(item -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String searchKey = newValue.toLowerCase();

                if (item.getName().toLowerCase().contains(searchKey)) {
                    return true;
                } else if (item.getCategory().toLowerCase().contains(searchKey)) {
                    return true;
                } else {
                    return String.format("%.2f", item.getPrice()).contains(searchKey);
                }
            });
        });

        SortedList<MenuItem> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

}

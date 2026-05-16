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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.MenuItem;
import util.AlertUtil;
import dao.MenuItemDAO;
import java.sql.SQLException;

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

    @FXML
    private ComboBox<String> categoryFilterBox;
    @FXML
    private ComboBox<String> sortPriceBox;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

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

        loadMenuData();

        // Category option
        categoryFilterBox.setItems(FXCollections.observableArrayList(
                "All", "Main Course", "Drinks", "Dessert", "Appetizer"
        ));
        categoryFilterBox.setValue("All");

        // Sort Options
        sortPriceBox.setItems(FXCollections.observableArrayList(
                "Price Low to High",
                "Price High to Low"
        ));

        // Search 
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilterAndSort());

        categoryFilterBox.setOnAction(e -> applyFilterAndSort());

        sortPriceBox.setOnAction(e -> applyFilterAndSort());

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

    @FXML
    private void handleAddOrEditMenuItem(ActionEvent event) {
        String name = nameField.getText().trim();
        String priceText = priceField.getText().trim();
        String category = categoryBox.getValue();

        if (name.isEmpty() || priceText.isEmpty() || category == null || category.isEmpty()) {
            AlertUtil.showError("Validation Error", "Please fill in all fields.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            AlertUtil.showError("Validation Error", "Price must be a valid number.");
            return;
        }
        if (price <= 0) {
            AlertUtil.showError("Validation Error", "Price must be greater than zero.");
            return;
        }

        try {
            if (selectedItem == null) {

                if (MenuItemDAO.menuItemNameExists(name, 0)) {
                    AlertUtil.showError("Validation Error", "Duplicate item name.");
                    return;
                }

                MenuItem newItem = new MenuItem(0, name, price, category);
                MenuItemDAO.insertMenuItem(newItem);

                AlertUtil.showInfo("Success", "Menu item added successfully.");

            } else {

                if (MenuItemDAO.menuItemNameExists(name, selectedItem.getId())) {
                    AlertUtil.showError("Validation Error", "Duplicate item name.");
                    return;
                }

                selectedItem.setName(name);
                selectedItem.setPrice(price);
                selectedItem.setCategory(category);

                MenuItemDAO.updateMenuItem(selectedItem);

                selectedItem = null;
                addEditBtn.setText("Add Menu Item");

                AlertUtil.showInfo("Success", "Menu item updated successfully.");
            }

            loadMenuData();
            clearFields();
            applyFilterAndSort();

        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Failed to save menu item data.");
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

    private void clearFields() {
        nameField.clear();
        priceField.clear();
        categoryBox.setValue(null);
        tableView.getSelectionModel().clearSelection();
    }

    private void loadMenuData() {
        try {
            items = MenuItemDAO.getAllMenuItems();
            tableView.setItems(FXCollections.observableArrayList(items));
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Failed to load menu items from database.");
        }
    }

    @FXML
    private void handleDeleteMenuItem(ActionEvent event) {
        MenuItem selected = tableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showError("Delete Error", "Please select a menu item to delete.");
            return;
        }

        try {
            MenuItemDAO.deleteMenuItem(selected.getId());

            loadMenuData();
            clearFields();
            selectedItem = null;
            addEditBtn.setText("Add Menu Item");
            applyFilterAndSort();

            AlertUtil.showInfo("Success", "Menu item deleted successfully.");

        } catch (SQLException e) {
            AlertUtil.showError("Database Error",
                    "Failed to delete menu item. It may be used by an order.");
        }

    }

    private void applyFilterAndSort() {
        if (items == null) {
            return;
        }

        String searchText = searchField.getText().toLowerCase().trim();
        String category = categoryFilterBox.getValue();
        String sortOption = sortPriceBox.getValue();

        List<MenuItem> result = items.stream()
                //filter
                .filter(item -> {
                    boolean matchesName = searchText.isEmpty()
                            || item.getName().toLowerCase().contains(searchText);

                    boolean matchesCategory = (category == null || category.equals("All"))
                            || item.getCategory().equalsIgnoreCase(category);

                    return matchesName && matchesCategory;
                })
                //sort
                .sorted((a, b) -> {
                    if (sortOption == null) {
                        return 0;
                    }

                    if (sortOption.equals("Price Low to High")) {
                        return Double.compare(a.getPrice(), b.getPrice());
                    } else {
                        return Double.compare(b.getPrice(), a.getPrice());
                    }
                })
                .toList();
        if (!searchText.isEmpty() && result.isEmpty()) {
            AlertUtil.showWarning("No Results", "No Menu found with this item.");
        }

        tableView.setItems(FXCollections.observableArrayList(result));
    }

}

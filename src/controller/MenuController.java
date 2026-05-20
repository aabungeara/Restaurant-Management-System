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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.MenuItem;
import util.AlertUtil;
import repositories.MenuItemRepo;
import java.sql.SQLException;
import java.util.ArrayList;
import model.User;
import service.MenuService;
import util.SceneUtil;
import util.Session;

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
    private List<MenuItem> items = new ArrayList<>();;
    private MenuItem selectedItem = null;

    @FXML
    private ComboBox<String> categoryFilterBox;
    @FXML
    private ComboBox<String> sortPriceBox;
    
    private final MenuService menuService = new MenuService();

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
    private void handleAddOrEditMenuItem(ActionEvent event) throws Exception {
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

                if (menuService.exists(name, 0)) {
                    AlertUtil.showError("Error", "Duplicate item name");
                    return;
                }
                 User user = Session.getCurrentUser();
                MenuItem item = new MenuItem(name, price, category, user);
                menuService.add(item);


                AlertUtil.showInfo("Success", "Menu item added successfully.");

            } else {

                 if (menuService.exists(name, selectedItem.getId())) {
                    AlertUtil.showError("Error", "Duplicate item name");
                    return;
                }

                selectedItem.setName(name);
                selectedItem.setPrice(price);
                selectedItem.setCategory(category);

                menuService.update(selectedItem);

                selectedItem = null;
                addEditBtn.setText("Add Menu Item");

                AlertUtil.showInfo("Success", "Menu item updated successfully.");
            }

            loadMenuData();
            clearFields();
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "Failed to save menu item data.");
        }
    }

    @FXML
    private void backToDashboard(ActionEvent event) throws IOException {
        SceneUtil.switchScene(event, "/view/dashboard.fxml");
    }

    private void clearFields() {
        nameField.clear();
        priceField.clear();
        categoryBox.setValue(null);
        tableView.getSelectionModel().clearSelection();
    }

    private void loadMenuData() {
         try {
            items = menuService.getItems();
            applyFilterAndSort();
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load menu items");
        }
    }

    @FXML
    private void handleDeleteMenuItem(ActionEvent event) throws Exception {
        MenuItem selected = tableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showError("Delete Error", "Please select a menu item to delete.");
            return;
        }

        try {
            menuService.delete(selected.getId());

            loadMenuData();
            clearFields();
            selectedItem = null;
            addEditBtn.setText("Add Menu Item");
            

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
        
        tableView.setItems(FXCollections.observableArrayList(result));
    }

}

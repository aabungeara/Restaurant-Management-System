/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import util.Session;

/**
 * FXML Controller class
 *
 * @author hp
 */
public class DashboardController implements Initializable {
     private void switchPage(ActionEvent event, String path) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
        
    @FXML
    private Label welcomeLabel;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (Session.getCurrentUser() != null) {
            welcomeLabel.setText("Welcome, " + Session.getCurrentUser().getFirstName() + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }
        
    }    

    @FXML
    private void goToTables(ActionEvent event) throws IOException {
         switchPage(event, "/view/tables.fxml");
    }

    @FXML
    private void goToMenu(ActionEvent event) throws IOException {
        switchPage(event, "/view/menu.fxml");
    }

    @FXML
    private void goToOrders(ActionEvent event) throws IOException {
        switchPage(event, "/view/orders.fxml");
    }

    @FXML
    private void logout(ActionEvent event) throws IOException {
        Session.setCurrentUser(null);
        switchPage(event, "/view/login.fxml");
    }
    
}

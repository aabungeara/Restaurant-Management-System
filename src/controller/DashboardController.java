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
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import util.SceneUtil;
import util.Session;

/**
 * FXML Controller class
 *
 * @author hp
 */
public class DashboardController implements Initializable {
    @FXML
    private Label welcomeLabel;

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
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
        System.out.println(Session.getUserId());
        SceneUtil.switchScene(event, "/view/tables.fxml");
    }

    @FXML
    private void goToMenu(ActionEvent event) throws IOException {
        SceneUtil.switchScene(event, "/view/menu.fxml");
    }

    @FXML
    private void goToOrders(ActionEvent event) throws IOException {
        SceneUtil.switchScene(event, "/view/orders.fxml");
    }

    @FXML
    private void logout(ActionEvent event) throws IOException {
        Session.clear();
        SceneUtil.switchScene(event, "/view/login.fxml");
    }
    
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXML2.java to edit this template
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import model.User;
import util.AlertUtil;
import util.Session;
import service.UserService;
import util.SceneUtil;

/**
 *
 * @author hp
 */
public class loginController implements Initializable {

    private Label label;
    @FXML
    private AnchorPane mainForm;
    @FXML
    private Button loginBut;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button signUpBut;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void loginBut(ActionEvent event) throws IOException {
        //Read email and password
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        
        //Check that the fields are not empty
        if (email.isEmpty() || password.isEmpty()) {
            AlertUtil.showError("Login Error", "Please fill in all fields.");
            return;
        }
        
        //Checks the email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            AlertUtil.showError("Login Error", "Invalid email format.");
            return;
        }

        UserService userService = new UserService();
        User foundUser = userService.login(email, password);

        //If the account does not exist
        if (foundUser == null) {
            AlertUtil.showError("Login Error", "Invalid email or password.");
            return;
        }
        Session.setCurrentUser(foundUser);

        AlertUtil.showInfo("Success", "Login successful.");

        SceneUtil.switchScene(event, "/view/dashboard.fxml");

    }

    //Open a page signUp.fxml
    @FXML
    private void signUpBut(ActionEvent event) throws IOException {
        SceneUtil.switchScene(event, "/view/signUp.fxml");
    }

}

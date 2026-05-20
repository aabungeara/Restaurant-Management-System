/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.io.IOException;
import java.net.URL;
//import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import util.AlertUtil;
import java.sql.SQLException;
import service.UserService;
import util.SceneUtil;

/**
 * FXML Controller class
 *
 * @author hp
 */
  

    public class SignUpController implements Initializable {

        @FXML
        private TextField firstNameField;
        @FXML
        private TextField lastNameField;
        @FXML
        private TextField emailField;
        @FXML
        private PasswordField passwordField;
        @FXML
        private PasswordField confirmPasswordField;

        /**
         * Initializes the controller class.
         */
        @Override
        public void initialize(URL url, ResourceBundle rb) {
            // TODO
        }

        @FXML
        private void handleSignUp(ActionEvent event) throws IOException {
            //Read values ​​from fields
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();
            String confirmPassword = confirmPasswordField.getText().trim();

            //Check for empty fields
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()
                    || password.isEmpty() || confirmPassword.isEmpty()) {
                AlertUtil.showError("Validation Error", "Please fill in all fields.");
                return;
            }
            //Check the email format
            if (!Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", email)) {
                AlertUtil.showError("Validation Error", "Invalid email format.");
                return;
            }
            //Checking the password matches
            if (!password.equals(confirmPassword)) {
                AlertUtil.showError("Validation Error", "Passwords do not match.");
                return;
            }

            UserService userService = new UserService();

            boolean success = userService.register(firstName, lastName, email, password);

            if (!success) {
                AlertUtil.showError("Error", "Email already exists or registration failed.");
                return;
            }

            AlertUtil.showInfo("Success", "Account created successfully.");
            //Open login page
            SceneUtil.switchScene(event, "/view/login.fxml");

        }

    @FXML
    private void goToLogin(ActionEvent event) throws IOException {
        //Open login page
        SceneUtil.switchScene(event, "/view/login.fxml");
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import util.AlertUtil;
import util.FileUtil;
import util.HashUtil;

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
    private void handleSignUp(ActionEvent event) throws IOException{
        FileUtil.ensureFiles();
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
        
        List<User> users = FileUtil.loadUsers();
        //Check that the email is not duplicated.
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                AlertUtil.showError("Validation Error", "Email already exists.");
                return;
            }
        }

        int newId = FileUtil.getNextUserId(users);
        //Password encryption
        String passwordHash = HashUtil.md5(password);
        //Create a User object and add to user and save in user file
        User newUser = new User(newId, firstName, lastName, email, passwordHash);
        users.add(newUser);
        FileUtil.saveUsers(users);

        AlertUtil.showInfo("Success", "Account created successfully.");
        //Open login page
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();

    }

    @FXML
    private void goToLogin(ActionEvent event) throws IOException {
        //Open login page
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
    
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXML2.java to edit this template
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.User;
import util.AlertUtil;
import util.FileUtil;
import util.HashUtil;
import util.Session;

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
        //To read saved users from the data file.
        List<User> users = FileUtil.loadUsers();
        //Searching for a user by email
        User foundUser = null;
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                foundUser = user;
                break;
            }
        }
        //If the account does not exist
        if (foundUser == null) {
            AlertUtil.showError("Login Error", "Account does not exist.");
            return;
        }
        //Encrypting the entered password
        String hashedPassword = HashUtil.md5(password);
        //If the encrypted password does not match the stored value
        if (!foundUser.getPasswordHash().equals(hashedPassword)) {
            AlertUtil.showError("Login Error", "Incorrect password.");
            return;
        }
        //Save the current user in Sessionx 
        Session.setCurrentUser(foundUser);
        AlertUtil.showInfo("Success", "Login successful.");

        //Open a page Dashboard.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();

    }
    //Open a page Dashboard.fxml
    @FXML
    private void signUpBut(ActionEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/signUp.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

}

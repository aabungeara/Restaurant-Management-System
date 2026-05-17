/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author hp
 */
public class SceneUtil {
     public static void switchScene(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneUtil.class.getResource(fxml));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setResizable(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

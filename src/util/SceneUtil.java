/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
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

    public static void openWindow(String fxml, String title, Object data) {

        try {
            FXMLLoader loader = new FXMLLoader(SceneUtil.class.getResource(fxml));
            Parent root = loader.load();

            Object controller = loader.getController();

            if (data != null && controller instanceof controller.BillPreviewController c) {
                c.setBill((dto.BillPrintDTO) data);
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Cannot load window: " + e.getMessage());
        }
    }
}

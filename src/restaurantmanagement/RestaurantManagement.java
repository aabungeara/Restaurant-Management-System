/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXML.java to edit this template
 */
package restaurantmanagement;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.FileUtil;


/**
 *
 * @author hp
 */
/**
 * أسماء الطلاب :
 * 1- أحمد محمد أبو نقيره**120203199
 * 2- عبد الناصر محمد كريم ايوب**120182543
 * 3- صلاح الدين أبو عياده**120231570
 * 4- أحمد السيد أبوجهل **120221272
 */
public class RestaurantManagement extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FileUtil.ensureFiles();
        
        System.out.println(getClass().getResource("/view/login.fxml"));
        Parent root = FXMLLoader.
                load(getClass().getResource("/view/login.fxml"));

        Scene scene = new Scene(root);
        stage.setTitle("Restaurant Management System");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        launch(args);
    }

}

package com.netfilestorage.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainClient extends Application {
    @Override
    public void start(Stage authStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/auth.fxml"));
        Parent root = fxmlLoader.load();
        authStage.setTitle("Network File Storage");
        authStage.setScene(new Scene(root));
        authStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

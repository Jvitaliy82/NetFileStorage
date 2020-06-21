package com.netfilestorage.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;


public class AuthController implements Initializable {

    @FXML
    VBox authorization;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    VBox registration;
    @FXML
    TextField regLoginField;
    @FXML
    PasswordField regPasswordField;
    @FXML
    PasswordField regPasswordField1;
    @FXML
    Button loginButton;

    private Alert alert;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread serverListener = new Thread(() -> {
            while (true){
                Object messageFromServer = null;
                try {
                    messageFromServer = Network.readInObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (Objects.requireNonNull(messageFromServer).toString().startsWith("User checked")) {
                    successfulEnterToStorageScene();

                } else if (messageFromServer.toString().startsWith("Incorrect username or password")) {
                    Platform.runLater(() -> {
                        alert = new Alert(Alert.AlertType.ERROR, "Incorrect username or password!");
                        alert.showAndWait();
                    });

                } else if (messageFromServer.toString().equals("Such user already exists")) {
                    Platform.runLater(() -> {
                        alert = new Alert(Alert.AlertType.ERROR, "Such user already exists!");
                        alert.showAndWait();
                        regLoginField.clear();
                        regPasswordField.clear();
                        regPasswordField1.clear();
                    });

                } else if (messageFromServer.toString().equals("Registration successful")) {
                    Platform.runLater(() -> {
                        alert = new Alert(Alert.AlertType.INFORMATION, "Registration successful!");
                        alert.showAndWait();
                        exitReg();
                    });
                }
            }
        });
        serverListener.setDaemon(true);
        serverListener.start();
    }

    public void showRegForm() {
        authorization.setVisible(false);
        registration.setVisible(true);
    }

    public void exitReg() {
        authorization.setVisible(true);
        registration.setVisible(false);
    }

    public void sendAuthMessage() {
        if (!loginField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
            Network.sendAuthMessage(loginField.getText(), passwordField.getText());
            loginField.clear();
            passwordField.clear();
        }
    }

    public void sendRegMessage() {
        if (!regLoginField.getText().isEmpty() && !regPasswordField.getText().isEmpty() && !regPasswordField1.getText().isEmpty()) {
            if (regPasswordField.getText().equals(regPasswordField1.getText())) {
                Network.sendRegMessage(regLoginField.getText(), regPasswordField.getText());
            } else {
                Platform.runLater(() -> {
                    alert = new Alert(Alert.AlertType.ERROR, "Passwords do not match!");
                    alert.showAndWait();
                    regPasswordField.clear();
                    regPasswordField1.clear();
                });
            }
        }
    }

    private void switchToStorageScene() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        try {
            Parent root1 = FXMLLoader.load(getClass().getResource("/main.fxml"));
            stage.setTitle("Network File Storage");
            Scene scene = new Scene(root1);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void successfulEnterToStorageScene() {
        Platform.runLater(this::switchToStorageScene);
    }
}


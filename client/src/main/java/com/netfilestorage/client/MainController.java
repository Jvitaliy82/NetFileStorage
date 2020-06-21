package com.netfilestorage.client;

import com.netfilestorage.common.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

public class MainController implements Initializable {

    @FXML
    ListView<String> clientFilesList;
    @FXML
    ListView<String> clientFilesSizeList;
    @FXML
    ListView<String> serverFilesList;
    @FXML
    ListView<String> serverFilesSizeList;
    @FXML
    Button exitStorageButton;

    private final String CLIENT_PATH = "client_storage/";
    private Desktop desktop = Desktop.getDesktop();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get(CLIENT_PATH + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
                    }
                    if (am instanceof RefreshServerStorageMessage) {
                        RefreshServerStorageMessage refreshServerStorageMessage = (RefreshServerStorageMessage) am;

                        TreeMap<String, Long> map = refreshServerStorageMessage.getFindFiles();
                        Platform.runLater(() -> {
                            for (Map.Entry<String, Long> entry : map.entrySet()) {
                                serverFilesList.getItems().add(entry.getKey());
                                serverFilesSizeList.getItems().add(entry.getValue().toString() + " Kb");
                            }
                        });
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        refreshLocalFilesList();
        refreshServerFilesList();
    }

    public void pressOnDownloadBtn() {
        Network.sendMsg(new FileRequest(serverFilesList.getSelectionModel().getSelectedItem()));
    }

    public void pressOnUploadBtn() throws IOException {
        Network.sendMsg(new FileMessage(Paths.get(CLIENT_PATH + clientFilesList.getSelectionModel().getSelectedItem())));
        refreshServerFilesList();
    }

    public void pressOnClientDeleteBtn() throws IOException {
        Files.delete(Paths.get(CLIENT_PATH + clientFilesList.getSelectionModel().getSelectedItem()));
        refreshLocalFilesList();
    }

    public void pressOnServerDeleteBtn() {
        Network.sendFileDeleteMessage(serverFilesList.getSelectionModel().getSelectedItem());
        refreshServerFilesList();
    }

    public void pressOnClientOpenBtn() throws IOException {
        File file = Paths.get(CLIENT_PATH + clientFilesList.getSelectionModel().getSelectedItem()).toFile();
        this.desktop.open(file);
    }

    public void pressOnAddFileToLocalList() {
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        try {
            Files.copy(file.toPath(), Paths.get(CLIENT_PATH + file.getName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        refreshLocalFilesList();
    }

    public void pressOnExitButton() {
        Stage stage = (Stage) exitStorageButton.getScene().getWindow();
        try {
            Parent root1 = FXMLLoader.load(getClass().getResource("/auth.fxml"));
            stage.setTitle("Network File Storage");
            Scene scene = new Scene(root1);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshLocalFilesList() {
        updateUI(() -> {
            try {
                clientFilesList.getItems().clear();
                clientFilesSizeList.getItems().clear();
                Files.walkFileTree(Paths.get(CLIENT_PATH), new SimpleFileVisitor<Path>() {
                    public FileVisitResult visitFile (Path file, BasicFileAttributes attrs) {
                        TreeMap<String, Long> findFiles = new TreeMap<>();
                        String fileName = file.getFileName().toString();
                        Long fileSize = (long) Math.ceil(file.toFile().length() / 1024.0);
                        findFiles.put(fileName, fileSize);
                        for (Map.Entry<String, Long> entry : findFiles.entrySet()) {
                            clientFilesList.getItems().add(entry.getKey());
                            clientFilesSizeList.getItems().add(entry.getValue().toString() + " Kb");
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void refreshServerFilesList() {
        serverFilesList.getItems().clear();
        serverFilesSizeList.getItems().clear();
        Network.sendMsg(new RefreshMessage());
    }

    private static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }
}

package org.example.clientsevermsgexample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientView implements Initializable {
    @FXML
    private AnchorPane ap_main;

    @FXML
    private Button button_send;

    @FXML
    private TextField tf_message;

    @FXML
    private ScrollPane sp_main;

    @FXML
    private VBox vbox_messages;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private Thread clientThread;
    private boolean connected = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (vbox_messages != null) {
            vbox_messages.heightProperty().addListener((observableValue, oldValue, newValue) -> {
                sp_main.setVvalue((Double) newValue);
            });
        }

        if (button_send != null) {
            button_send.setOnAction(event -> {
                String messageToSend = tf_message.getText();
                if (!messageToSend.isEmpty() && connected) {
                    sendMessage(messageToSend);
                    tf_message.clear();
                }
            });
        }

        if (tf_message != null) {
            tf_message.setOnAction(event -> {
                if (button_send != null) {
                    button_send.fire();
                }
            });
        }

        if (button_send != null) {
            button_send.setDisable(true);
        }

        if (tf_message != null) {
            tf_message.setDisable(true);
        }

        connectToServer();
    }

    private void connectToServer() {
        displayMessage("Connecting to server...", false);

        clientThread = new Thread(() -> {
            try {
                socket = new Socket("localhost", 6666);
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                connected = true;

                Platform.runLater(() -> {
                    displayMessage("Connected to server!", false);
                    tf_message.setDisable(false);
                    button_send.setDisable(false);
                });

                // Listen for messages from the server
                while (connected) {
                    try {
                        String message = dataInputStream.readUTF();
                        Platform.runLater(() -> {
                            displayMessage(message, true);
                        });
                    } catch (IOException e) {
                        Platform.runLater(() -> {
                            displayMessage("Disconnected from server: " + e.getMessage(), false);
                            tf_message.setDisable(true);
                            button_send.setDisable(true);
                        });
                        closeConnection();
                        break;
                    }
                }

            } catch (IOException e) {
                Platform.runLater(() -> {
                    displayMessage("Failed to connect to server: " + e.getMessage(), false);
                    tf_message.setDisable(true);
                    button_send.setDisable(true);
                });
            }
        });

        clientThread.setDaemon(true);
        clientThread.start();
    }

    private void sendMessage(String message) {
        try {
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
            displayMessage(message, false);
        } catch (IOException e) {
            displayMessage("Failed to send message: " + e.getMessage(), false);
            closeConnection();
        }
    }

    private void displayMessage(String message, boolean fromServer) {
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(5, 5, 5, 10));

        if (fromServer) {
            hBox.setAlignment(Pos.CENTER_LEFT);
        } else {
            hBox.setAlignment(Pos.CENTER_RIGHT);
        }

        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text);

        if (fromServer) {
            textFlow.setStyle(
                    "-fx-background-color: #e0e0e0;" +
                            "-fx-background-radius: 20px;" +
                            "-fx-padding: 8px;"
            );
        } else {
            textFlow.setStyle(
                    "-fx-background-color: #0088ff;" +
                            "-fx-background-radius: 20px;" +
                            "-fx-padding: 8px;"
            );
            text.setFill(Color.WHITE);
        }

        hBox.getChildren().add(textFlow);

        Platform.runLater(() -> {
            vbox_messages.getChildren().add(hBox);
        });
    }

    private void closeConnection() {
        connected = false;
        try {
            if (dataInputStream != null) {
                dataInputStream.close();
            }
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClientClose() {
        closeConnection();
    }
}
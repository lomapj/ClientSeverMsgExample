package org.example.clientsevermsgexample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class ServerView implements Initializable {
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

    private ServerSocket serverSocket;
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private Thread serverThread;
    private boolean isRunning = false;

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
                if (!messageToSend.isEmpty() && socket != null && socket.isConnected()) {
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

        startServer();
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(6666);
            isRunning = true;

            displayServerMessage("Server started on port 6666. Waiting for client...");

            serverThread = new Thread(() -> {
                try {
                    // Wait for a client to connect
                    socket = serverSocket.accept();
                    displayServerMessage("Client connected!");

                    // Set up data streams
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    // Enable message sending
                    Platform.runLater(() -> {
                        tf_message.setDisable(false);
                        button_send.setDisable(false);
                    });

                    // Start receiving messages
                    while (isRunning && socket.isConnected()) {
                        try {
                            String message = dataInputStream.readUTF();
                            Platform.runLater(() -> {
                                displayClientMessage(message);
                            });
                        } catch (IOException e) {
                            Platform.runLater(() -> {
                                displayServerMessage("Client disconnected or error occurred.");
                                tf_message.setDisable(true);
                                button_send.setDisable(true);
                            });
                            closeConnection();
                            // Try to restart server to accept new connections
                            startServer();
                            break;
                        }
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        displayServerMessage("Server error: " + e.getMessage());
                    });
                    closeConnection();
                }
            });

            serverThread.setDaemon(true);
            serverThread.start();

        } catch (IOException e) {
            displayServerMessage("Failed to start server: " + e.getMessage());
        }
    }

    private void sendMessage(String message) {
        try {
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
            displayServerMessage(message);
        } catch (IOException e) {
            displayServerMessage("Failed to send message: " + e.getMessage());
        }
    }

    private void displayServerMessage(String message) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text);

        textFlow.setStyle(
                "-fx-background-color: #0088ff;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 8px;"
        );

        text.setFill(Color.WHITE);

        hBox.getChildren().add(textFlow);

        Platform.runLater(() -> {
            vbox_messages.getChildren().add(hBox);
        });
    }

    private void displayClientMessage(String message) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text);

        textFlow.setStyle(
                "-fx-background-color: #e0e0e0;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 8px;"
        );

        hBox.getChildren().add(textFlow);

        Platform.runLater(() -> {
            vbox_messages.getChildren().add(hBox);
        });
    }

    private void closeConnection() {
        try {
            isRunning = false;

            if (dataInputStream != null) {
                dataInputStream.close();
            }
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onServerClose() {
        closeConnection();
    }
}
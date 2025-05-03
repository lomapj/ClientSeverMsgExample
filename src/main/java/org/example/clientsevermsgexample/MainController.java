package org.example.clientsevermsgexample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private ComboBox dropdownPort;

    @FXML
    private Button clearBtn;

    @FXML
    private TextArea resultArea;

    @FXML
    private Label server_lbl;

    @FXML
    private Label test_lbl;

    @FXML
    private TextField urlName;

    @FXML
    private Button user1_client;

    @FXML
    private Button user2_server;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (dropdownPort != null) {
            dropdownPort.getItems().addAll(
                    "7",     // ping
                    "13",    // daytime
                    "21",    // ftp
                    "23",    // telnet
                    "71",    // finger
                    "80",    // http
                    "119",   // nntp (news)
                    "161"    // snmp
            );

            if (!dropdownPort.getItems().isEmpty()) {
                dropdownPort.getSelectionModel().selectFirst();
            }
        }

        if (user1_client != null) {
            user1_client.setOnAction(this::startClient);
        }

        if (user2_server != null) {
            user2_server.setOnAction(this::startServer);
        }
    }

    @FXML
    void checkConnection(ActionEvent event) {
        if (urlName == null || dropdownPort == null || resultArea == null) {
            System.err.println("UI components not initialized properly");
            return;
        }

        String host = urlName.getText();
        if (host == null || host.trim().isEmpty()) {
            resultArea.appendText("Please enter a hostname\n");
            return;
        }

        if (dropdownPort.getValue() == null) {
            resultArea.appendText("Please select a port\n");
            return;
        }

        try {
            int port = Integer.parseInt(dropdownPort.getValue().toString());

            try {
                Socket sock = new Socket(host, port);
                resultArea.appendText(host + " listening on port " + port + "\n");
                sock.close();
            } catch (UnknownHostException e) {
                resultArea.setText("Unknown host: " + host + "\n");
            } catch (Exception e) {
                resultArea.appendText(host + " not listening on port " + port + "\n");
            }
        } catch (NumberFormatException e) {
            resultArea.appendText("Invalid port number\n");
        }
    }

    @FXML
    void clearBtn(ActionEvent event) {
        if (resultArea != null) {
            resultArea.setText("");
        }

        if (urlName != null) {
            urlName.setText("");
        }
    }

    @FXML
    void startServer(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("server-view.fxml"));
            Parent root = loader.load();

            ServerView serverViewController = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Server Messenger");
            stage.setScene(new Scene(root));
            stage.setOnCloseRequest(e -> {
                try {
                    serverViewController.onServerClose();
                } catch (Exception ex) {
                    System.err.println("Error closing server: " + ex.getMessage());
                }
            });

            stage.show();

            if (resultArea != null) {
                resultArea.appendText("Server started successfully!\n");
            }
        } catch (IOException e) {
            if (resultArea != null) {
                resultArea.appendText("Error starting server: " + e.getMessage() + "\n");
            }
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    @FXML
    void startClient(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("client-view.fxml"));
            Parent root = loader.load();

            ClientView clientViewController = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Client Messenger");
            stage.setScene(new Scene(root));
            stage.setOnCloseRequest(e -> {
                try {
                    clientViewController.onClientClose();
                } catch (Exception ex) {
                    System.err.println("Error closing client: " + ex.getMessage());
                }
            });

            stage.show();

            if (resultArea != null) {
                resultArea.appendText("Client started successfully!\n");
            }
        } catch (IOException e) {
            if (resultArea != null) {
                resultArea.appendText("Error starting client: " + e.getMessage() + "\n");
            }
            System.err.println("Error starting client: " + e.getMessage());
        }
    }
}
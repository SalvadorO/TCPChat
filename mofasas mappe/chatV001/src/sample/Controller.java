package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.BooleanSupplier;

public class Controller implements Initializable{

    ClientFX client;

    @FXML
    public TextField txtField,username,passwd;

    @FXML
    public TextArea txtArea;

    @FXML
    Button loginbutton;

    @FXML
    ListView<String> userlist;


    public void startClient() {


        client = new ClientFX("localhost", 5555,txtField,txtArea);
        client.setClientOnline(true);




    }


    @SuppressWarnings("unchecked")
    @FXML public void loginserver(){





            startClient();
            client.setPassword(passwd.getText());
            client.setUsername(username.getText());
            passwd.clear();
            username.clear();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (client.isLoggedOn()) {

                username.setVisible(false);
                passwd.setVisible(false);
                username.setDisable(true);
                passwd.setDisable(true);
                loginbutton.setDisable(true);
                loginbutton.setVisible(false);

                txtField.setDisable(false);
                txtField.setVisible(true);
                userlist.setDisable(false);
                userlist.setVisible(true);


                userlist.setItems(client.getObservableUsers());


                userlist.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);


                userlist.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>()
                {
                    public void changed(ObservableValue<? extends String> observableval, String oldval, String newval)
                    {

                        String connectToThisUser = userlist.getSelectionModel().getSelectedItem();
                        if (connectToThisUser != null && !connectToThisUser.equals("null") &&!connectToThisUser.equals("")) client.setConnectTo(connectToThisUser);}
                });


    }

    }


    @Override
    public void initialize(URL location, ResourceBundle resources)
    {


        txtArea.setDisable(true);
        txtArea.setVisible(false);
        txtField.setDisable(true);
        txtField.setVisible(false);
        userlist.setDisable(true);
        userlist.setVisible(false);


        Main.getPrimaryStage().setOnCloseRequest(event -> {
            try {
                client.getCliSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });



    }


}

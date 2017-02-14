package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
    ListView userlist;

    public void startClient() {


        client = new ClientFX("localhost", 5555,txtField,txtArea);
        client.setClientOnline(true);




    }


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

                txtArea.setDisable(false);
                txtArea.setVisible(true);
                txtField.setDisable(false);
                txtField.setVisible(true);
                userlist.setDisable(false);
                userlist.setVisible(true);

              //  client.setTextArea(txtArea);
              //  client.setTextField(txtField);

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

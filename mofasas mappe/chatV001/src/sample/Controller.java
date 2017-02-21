package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    ClientFX client;

    @FXML
    public TextField txtField,username,passwd;

    @FXML
    public TextArea txtArea;

    @FXML
    Button loginbutton;

  @FXML  ListView<String> onlinelist,offlinelist,busylist;

    @FXML
    Text startText,busyText,onlineText,offlineText,passwordText,usernameText;






    public void startClient() {


        client = new ClientFX("localhost", 5555,txtField,txtArea,startText);
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

                onlineText.setVisible(true);
                offlineText.setVisible(true);
                busyText.setVisible(true);
                startText.setVisible(true);
                usernameText.setVisible(false);
                passwordText.setVisible(false);


                onlinelist.setDisable(false);
                onlinelist.setVisible(true);
                offlinelist.setVisible(true);
                busylist.setVisible(true);




                onlinelist.setItems(client.getObservableOnline());
                offlinelist.setItems(client.getObservableOffline());
                busylist.setItems(client.getObservableBusy());


                onlinelist.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                offlinelist.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                busylist.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);



                onlinelist.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>()
                {
                    public void changed(ObservableValue<? extends String> observableval, String oldval, String newval)
                    {


                        String connectToThisUser = onlinelist.getSelectionModel().getSelectedItem();



                        if (connectToThisUser != null && !connectToThisUser.equals("null") &&!connectToThisUser.equals("")) {
                            client.setConnectTo(connectToThisUser);
                        }}
                });


    }


    }



    @Override
    public void initialize(URL location, ResourceBundle resources)
    {


        txtArea.setDisable(true);
        txtArea.setVisible(false);
        startText.setVisible(false);
        onlineText.setVisible(false);
        offlineText.setVisible(false);
        busyText.setVisible(false);
        txtField.setDisable(true);
        txtField.setVisible(false);
        onlinelist.setDisable(true);
        onlinelist.setVisible(false);
        offlinelist.setDisable(true);
        offlinelist.setVisible(false);
        busylist.setDisable(true);
        busylist.setVisible(false);





        Main.getPrimaryStage().setOnCloseRequest(event -> {
            try {

               if (client != null) client.getCliSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });



    }


}

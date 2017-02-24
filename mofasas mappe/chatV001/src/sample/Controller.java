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
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    ClientFX client;
    private String serverIp = "localhost";
    private int port = 5555;

    @FXML
    public TextField txtField,username;

    @FXML
    public PasswordField passwd;

    @FXML
    public TextArea txtArea,clientInfo;

    @FXML
    Button loginbutton, buttonCreateUser,editServerButton, sendButton;

    @FXML  ListView<String> onlinelist,offlinelist,busylist;

    @FXML
    Text startText,busyText,onlineText,offlineText,passwordText,usernameText,welcomeText;

    @FXML SplitMenuButton statusButton;

    @FXML MenuItem busyButton,onlineButton;



    public void startClient() {


        client = new ClientFX(serverIp, port,txtField,txtArea,startText,busyButton, onlineButton, sendButton);
        client.setClientOnline(true);

    }

    private void whenSignedIn(boolean isSignedIn){
        if (isSignedIn) {

            Main.getPrimaryStage().setMinWidth(830);
            Main.getPrimaryStage().setMinHeight(600);
            Main.getPrimaryStage().centerOnScreen();

            username.setVisible(false);
            passwd.setVisible(false);
            username.setDisable(true);
            passwd.setDisable(true);
            loginbutton.setDisable(true);
            loginbutton.setVisible(false);
            editServerButton.setDisable(true);
            editServerButton.setVisible(false);
            buttonCreateUser.setDisable(true);
            buttonCreateUser.setVisible(false);

            txtField.setVisible(true);
            sendButton.setVisible(true);

            onlineText.setVisible(true);
            offlineText.setVisible(true);
            busyText.setVisible(true);
            startText.setVisible(true);
            usernameText.setVisible(false);
            passwordText.setVisible(false);
            welcomeText.setText("Welcome, " + client.username + "!");
            welcomeText.setVisible(true);

            clientInfo.setDisable(false);
            clientInfo.setText("IP Address: " + client.getCliSocket().getInetAddress() + "\n"
                + "Port Number: " + client.getCliSocket().getPort());
            clientInfo.setVisible(true);
            clientInfo.setEditable(false);



            statusButton.setDisable(false);
            statusButton.setVisible(true);
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


            onlinelist.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                public void changed(ObservableValue<? extends String> observableval, String oldval, String newval) {


                    String connectToThisUser = onlinelist.getSelectionModel().getSelectedItem();



                    if (connectToThisUser != null && !connectToThisUser.equals("null") && !connectToThisUser.equals("")) {
                        client.setConnectTo(connectToThisUser);
                    }
                }
            });
        }
    }
    /**
     *
     */
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

        whenSignedIn(client.isLoggedOn());

    }

    @FXML public void editServer(){
        TextInputDialog dialog = new TextInputDialog(serverIp);
        dialog.setHeaderText("Server configurations");
        dialog.setContentText("Please enter server IP address");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> serverIp = name);
    }

    @FXML public void createNewUser(){

        startClient();
        client.setPassword(passwd.getText());
        client.setUsername(username.getText());
        passwd.clear();
        username.clear();

        client.setCreatingUser(true);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        whenSignedIn(client.isLoggedOn());


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
        welcomeText.setVisible(false);

        statusButton.setDisable(true);
        statusButton.setVisible(false);
        clientInfo.setDisable(true);
        clientInfo.setVisible(false);


        txtField.setDisable(true);
        txtField.setVisible(false);
        sendButton.setDisable(true);
        sendButton.setVisible(false);
        onlinelist.setDisable(true);
        onlinelist.setVisible(false);
        offlinelist.setDisable(true);
        offlinelist.setVisible(false);
        busylist.setDisable(true);
        busylist.setVisible(false);





        Main.getPrimaryStage().setOnCloseRequest(event -> {
            try {

                if (client != null && client.getCliSocket() != null) client.getCliSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });



    }


}
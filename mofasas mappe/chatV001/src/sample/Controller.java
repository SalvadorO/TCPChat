package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * This class is responsible for controlling the flow of the application.  It sends instances of GUI components to ClientFX, port number
 * and IP Address. This class takes action corresponding to user activities, such as changing GUI visuals and setting variables for ClientFX
 */

public class Controller implements Initializable{

    ClientFX client;
    private String serverIp = "localhost";
    private int port = 5555;

    @FXML public TextField txtField,username;

    @FXML public PasswordField passwd;

    @FXML public TextArea txtArea,clientInfo;

    @FXML Button loginbutton, buttonCreateUser,editServerButton, sendButton;

    @FXML  ListView<String> onlinelist,offlinelist,busylist;

    @FXML Text startText,busyText,onlineText,offlineText,passwordText,usernameText,welcomeText;

    @FXML SplitMenuButton statusButton;

    @FXML MenuItem busyButton,onlineButton;


    /**
     * Start the client by creating a new instance of ClientFX class and sends serverIP, port and GUI components.
     */
    public void startClient() {


        client = new ClientFX(serverIp, port,txtField,txtArea,startText,busyButton, onlineButton, sendButton);
        client.setClientOnline(true);

    }

    /**
     * When the client gets an approval from the server, this method will run and change/toggle the javafx components.
     * @param isSignedIn value that tells if client is allowed to sign in.
     */
    private void whenSignedIn(boolean isSignedIn){
        if (isSignedIn) {

            toggleClientScene();

            // sets arraylists into the listviews
            onlinelist.setItems(client.getObservableOnline());
            offlinelist.setItems(client.getObservableOffline());
            busylist.setItems(client.getObservableBusy());

            // makes sure that it is only able to click on one item at the time
            onlinelist.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            offlinelist.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            busylist.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);


            // listener to the onlinelist when someone clicks on a username.
            onlinelist.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                public void changed(ObservableValue<? extends String> observableval, String oldval, String newval) {


                    String connectToThisUser = onlinelist.getSelectionModel().getSelectedItem();



                    // if the username is not null, ServerFX class will recieve a username. This string will be used when clients wants to create
                    // a new conversation.
                    if (connectToThisUser != null && !connectToThisUser.equals("null") && !connectToThisUser.equals("")) {
                        client.setConnectTo(connectToThisUser);
                    }
                }
            });
        }
    }
    /**
     * This method is connected to the login button, and will run when a user clicks on it. first it sends the password/username inputs
     * to ServerFX and then waits until it gets a response from the server.
     */
    @FXML public void loginserver(){

        if (!passwd.getText().isEmpty() || !username.getText().isEmpty()){
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

    }

    /**
     * This methoed will create a dialogbox where the user can change the server IP address. It has "localhost" as default, which means
     * that the server and client runs on the same machine/host.
     */
    @FXML public void editServerIP(){
        TextInputDialog dialog = new TextInputDialog(serverIp);
        dialog.setHeaderText("Server configurations");
        dialog.setContentText("Please enter server IP address");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> serverIp = name);
    }

    /**
     *  This method is similar to the loginserver() method however the difference is that it will send a true statement to client class
     *  before waiting for server responds.
     */
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

        toggleLoginScene();


        // This makes sure that the socket that belongs to the client, is closed right before signing off (exiting the window).
        Main.getPrimaryStage().setOnCloseRequest(event -> {

            // Create a  logoff popup when clicked on exit.
            try {
                if (client != null && client.getCliSocket() != null) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("Signed Off");
                    alert.setContentText("You are now signed off");

                    alert.showAndWait();

                        // close client socket.
                        client.getCliSocket().close();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        });



    }

    /**
     *  Toggle GUI components by setting visible or disable as long as the user has not logged in.
     */
    private void toggleLoginScene(){

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
    }

    /**
     *  toggle GUI components by setting visible/disable when the user is signed in.
     */
    private void toggleClientScene(){
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
        txtArea.setEditable(false);

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
    }


}
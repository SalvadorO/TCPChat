package sample;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * This class serves client functionality, which consist of sending and receiving a message, changing status, receiving messages.
 * There is also a chatlog variable that saves all conversations in a Hash Map, which enables the functionality of talking to many clients at the same time
 * on the same text area.
 */

public class ClientFX  extends Service<Void> {
    private Socket cliSocket;
    private PrintStream outStream;
    private BufferedReader inStream;
    ArrayList<String> onlineUsers = new ArrayList<>();
    ArrayList<String> offlineUsers = new ArrayList<>();
    ArrayList<String> busyUsers = new ArrayList<>();
    final ObservableList<String> observableOnline = FXCollections.observableArrayList();
    final ObservableList<String> observableOffline = FXCollections.observableArrayList();
    final ObservableList<String> observableBusy = FXCollections.observableArrayList();

    ObservableMap<String,String> chatLog = FXCollections.observableMap(new HashMap<>());
    boolean isClientOnline,loggedOn = false,isCreatingUser = false;
    private MenuItem busyButton,onlineButton;
    String hostName;
    int portNumber;
    TextArea textArea;
    TextField textField;
    Text startText;
    String username,username2,sender,password;
    Button sendButton;


    /**
     *  Constructs a new instance of this class and assigns parameter values to this class values.
     *  The client will try to connect to the server, if not, an IOException/UnknownHostException will be trowed and a alert box will appear.
     * @param ipAdress sets value to the ip address
     * @param port sets value to the port number
     * @param textField gets an javafx instance of a textfield from the controller
     * @param textArea gets an javafx instance of a textArea from the controller
     * @param startText gets an javafx instance of a textArea from the controller
     * @param busyButton gets an javafx instance of a button from the controller
     * @param onlineButton gets an javafx instance of a button from the controller
     * @param sendButton gets an javafx instance of a button from the controller
     */
    public ClientFX(String ipAdress, int port, TextField textField, TextArea textArea, Text startText,
                    MenuItem busyButton, MenuItem onlineButton, Button sendButton) {
        this.hostName = ipAdress;
        this.portNumber = port;
        this.textField = textField;
        this.textArea = textArea;
        this.startText = startText;
        this.busyButton = busyButton;
        this.onlineButton = onlineButton;
        this.sendButton = sendButton;


        try {

            // initializes client socket, output stream and input stream.
            cliSocket = new Socket(hostName, portNumber);
            outStream = new PrintStream(cliSocket.getOutputStream());
            inStream = new BufferedReader(new InputStreamReader(cliSocket.getInputStream()));


        } catch (UnknownHostException e) {
            alertDialogs("Connection Error","Connected host is unknown.");

        } catch (IOException e) {
            alertDialogs("Connection Error","Can not connect to server.");
        }

        // starts this thread
        this.start();


    }

    /**
     * creates a task that this class will run on a separate thread. This will happend once the socket has connected to a server
     * @return returns the task this server is assigned to do.
     */
    @Override
    protected  Task<Void> createTask() {

        Task<Void> task = new Task<Void>() {
            protected Void call() throws InterruptedException {

                String inLine;
                sendButton.setDefaultButton(true);

                try {

                    // while the client has not signed in
                    while (!loggedOn){

                        if (!username.isEmpty() && !password.isEmpty()) {

                            // if creating a new user
                            if (isCreatingUser){
                                if (!username.matches("(\\w+)") || !password.matches("(\\w+)")) {
                                    alertDialogs("Create User", "Username and password must contain only digts or letters.");

                                } else {
                                    outStream.println("[CreateNewUser*OK]");
                                }

                            }
                            if (username.matches("(\\w+)") || password.matches("(\\w+)")) outStream.println(username + ":" + password);
                        }

                        // when a user is signed in.
                        while ((inLine = inStream.readLine()) != null) {

                            if (inLine.equals("[LogInApproved*OK]")){
                                loggedOn = true;
                                break;
                            } else
                              switch (inLine){
                                  case "[LogInNotApproved*OK]" : alertDialogs("Login Error","Wrong password or username.");
                                      break;
                                  case "[CreateNewUser*ERROR]" : alertDialogs("Create User Error","Username already exists.");
                                      break;
                                  case "[UserIsOnline*ERROR]" :  alertDialogs("Login Error ", "You are already logged in");
                                      break;
                                  case "[CreateUsername*ERROR]" : alertDialogs("Create User Error", "Username is too short. Please try again");
                                      break;

                              }
                        }
                        outStream.flush();

                    }

                    // when the user wants to change status to ONLINE
                    onlineButton.setOnAction(event -> {
                        outStream.println("[SetClientOnline*OK]");
                        textArea.setVisible(true);
                        textField.setVisible(true);
                        sendButton.setVisible(true);
                       startText.setText("");
                        startText.setVisible(false);
                    });

                    // when the user wants to change status to BUSY

                    busyButton.setOnAction(event1 ->{
                        outStream.println("[SetClientBusy*OK]");
                        textArea.setVisible(false);
                        startText.setText("Status: BUSY");

                        textField.setVisible(false);
                        sendButton.setVisible(false);

                        startText.setVisible(true);
                    });


                    // when the user wants to send a message
                    sendButton.setOnAction(event1 -> {
                        outStream.println("[SendingAMessage*OK]");
                        outStream.println(textField.getText());
                        if (chatLog.get(username2).equals(""))textArea.setText("You: " + textField.getText());
                        else textArea.setText(textArea.getText() + "\n"  + "You: " + textField.getText());

                        chatLog.put(username2,textArea.getText());


                        textField.clear();
                    });

                    // while receiving messages from server
                    while ((inLine = inStream.readLine()) != null) {

                        //when receiving lists from the server
                        if (inLine.equals("[SendingListOfUsers*OK]")) {
                            onlineUsers.clear();
                            offlineUsers.clear();
                            busyUsers.clear();

                            recieveLists();

                            Platform.runLater(() ->{


                                observableOffline.clear();
                                observableOffline.addAll(offlineUsers);

                                observableOnline.clear();
                                observableOnline.addAll(onlineUsers);

                                observableBusy.clear();
                                observableBusy.addAll(busyUsers);




                                if (observableOnline.contains(username))  observableOnline.remove(username);
                                if (observableBusy.contains(username)) observableBusy.remove(username);


                                areUsersOnline(observableOnline.isEmpty() || (sender != null && !observableOnline.contains(sender))
                                        || (!observableOnline.contains(username2) && !chatLog.get(username2).equals("") ));

                            });


                        }  else {

                            // when the client receives a message from another client.
                            sender = inLine;
                            String message = inStream.readLine();
                            StringBuilder chat = new StringBuilder();



                            // sets the received text into a hash map (chathistory). Enabling the functionality of multiple conversation
                            // on one text area, (Many-to-many chat).

                            if (chatLog.get(sender) == null || chatLog.get(sender).equals("")) chat.append(sender + ": " + message);
                            else chat.append(chatLog.get(sender)+ "\n" + sender + ": " + message);

                            chatLog.put(sender,chat.toString());
                            textArea.setText(chatLog.get(sender));
                            if (!sender.equals(username2)){
                                setConnectTo(sender);

                            }
                        }


                    }

                    cliSocket.close();
                } catch (IOException e) {
                   if (Main.getPrimaryStage().isShowing()) alertDialogs("Connection Error", "Lost connection to server");
                    outStream.close();
                } catch (ConcurrentModificationException cme){
                    alertDialogs("Graphical Display error", "Can not update interface");
                }
                return null;
            }
        };
        return task;
    }


    /**
     * change the state of a client
     * @param clientOnline boolean value that changes if a client becomes online/offline
     */
    public void setClientOnline(boolean clientOnline) {
        isClientOnline = clientOnline;
    }

    /**
     * set method for changing the password variable for this class
     * @param password gets a string as password
     */
    public void setPassword(String password) {
        this.password = password;
    }
    /**
     * set method for changing the username variable for this class
     * @param username gets a string as username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * get-method to see if this client instance (this class) is online/offline
     * @return boolean value
     */
    public boolean isLoggedOn() {
        return loggedOn;
    }

    /**
     * This method checks if there are any users online, and change the GUI components
     * It also changes if the current client is set as busy.
     * @param check boolean value that is used to see if the online list is empty.
     */
    private void areUsersOnline(boolean check){

        if (check){

            textArea.setDisable(true);
            textArea.setVisible(false);
            textField.setDisable(true);
            sendButton.setDisable(true);

            startText.setText("user on the list is not online");
            startText.setVisible(true);
        } else {

            startText.setVisible(false);
            textArea.setVisible(true);
            textArea.setDisable(false);




        }
        if (busyUsers.contains(username)) {
            startText.setText("Status: BUSY");
            startText.setVisible(true);
            textArea.setVisible(false);
        }


        if (sender != null && !observableOnline.contains(sender)) sender = null;

    }

    /**
     * This method sets value to the variable "username2", which is the username that was clicked on, (online user list).
     * The method takes the username that was clicked on, and sends a request for a new chat with this username.
     * Finally, text areas and fields changes when the clients opens up a new chat with username2.
     * @param username2 the username that the client clicked on and wants to talk to.
     */
    public void setConnectTo(String username2) {

        outStream.println("[RequestingChat*OK]");
        startText.setVisible(false);
        textArea.setDisable(false);
        textArea.setVisible(true);
        textField.setDisable(false);
        sendButton.setDisable(false);


        // setting a delay between the request message and username message
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // sends a message of which user this client wants to talk to.
        outStream.println(username2);
        this.username2 = username2;

        textArea.setText(chatLog.get(username2));




    }

    /**
     * If the server sends lists of users, then this method will receive and add all usernames in a observable arraylist.
     * @throws IOException if the socket for inputstream is not connected
     */
    private void recieveLists() throws IOException{


        String line = inStream.readLine();

        if (line.equals("[SendingOnlineList*OK]")) {

            String listOfOnline;
            while ((listOfOnline = inStream.readLine()) != null && !listOfOnline.equals("[SendingOfflineList*OK]")) {
                onlineUsers.add(listOfOnline);

                if (!chatLog.containsKey(username2)) chatLog.put(username2,"");


            }
            line = listOfOnline;

        }  if (line.equals("[SendingOfflineList*OK]")) {

            String listOfOffline;
            while ((listOfOffline = inStream.readLine()) != null && !listOfOffline.equals("[SendingBusyList*OK]")) {
                offlineUsers.add(listOfOffline);

            }
            line = listOfOffline;


        }  if (line.equals("[SendingBusyList*OK]")){
            String listOfBusy;
            while ((listOfBusy = inStream.readLine()) != null && !listOfBusy.equals("[SendingListOfUsers*DONE]")) {
                busyUsers.add(listOfBusy);

            }



        }
    }
    /**
     * When the client revives an error message from server, this method will be called.
     * It will create an alert popup that describes the errors.
     * @param headerText sets text on the header area.
     * @param contentText sets text on the content area.
     */
    protected void alertDialogs(String headerText, String contentText){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            if (alert.getAlertType() == Alert.AlertType.ERROR) alert.setTitle("ERROR");
            alert.setHeaderText(headerText);
            alert.setContentText(contentText);

            alert.showAndWait();
        });
    }

    /**
     * Observablelist that consists of usernames that are currently busy.
     * @return the list of busy user names.
     */
    public ObservableList<String> getObservableOffline() {
        return observableOffline;
    }

    /**
     * Observablelist that consists of usernames that are currently busy.
     * @return the list of busy user names.
     */
    public ObservableList<String> getObservableBusy() {
        return observableBusy;
    }

    /**
     * Observablelist that consists of usernames that are currently online.
     * @return the list of online user names.
     */
    public ObservableList<String> getObservableOnline() {
        return observableOnline;
    }

    /**
     * sets a boolean value true if the client wants to create a new user, by clicking on "Create User".
     * @param creatingUser sets boolean value to the parameter value.
     */
    public void setCreatingUser(boolean creatingUser) {
        isCreatingUser = creatingUser;
    }

    /**
     * Creating an accsess for the Controller class to get the client socket
     * @return the socket of this client that is connected to a server.
     */
    public Socket getCliSocket() {
        return cliSocket;
    }

    public BufferedReader getInStream() {
        return inStream;
    }
}
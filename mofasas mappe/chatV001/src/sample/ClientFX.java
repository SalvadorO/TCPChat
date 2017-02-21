package sample;


/**
 * Created by Mustafe on 07.02.2017.
 */
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.*;
import java.io.*;
import java.util.*;

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

    ObservableMap<String,String> chatHistory = FXCollections.observableMap(new HashMap<>());
    boolean isClientOnline,loggedOn = false;
    String hostName;
    int portNumber;
    TextArea textArea;
    TextField textField;
    Text startText;
    String username,username2,sender,password;



    public ClientFX(String ipAdress, int port, TextField textField, TextArea textArea, Text startText) {
        this.hostName = ipAdress;
        this.portNumber = port;
        this.textField = textField;
        this.textArea = textArea;
        this.startText = startText;

        System.out.println("Client is up!");

        try {
            cliSocket = new Socket(hostName, portNumber);
            outStream = new PrintStream(cliSocket.getOutputStream());
            inStream = new BufferedReader(new InputStreamReader(cliSocket.getInputStream()));


        } catch (UnknownHostException e) {
            System.out.println("Unknown host!");
        } catch (IOException e) {
            System.out.println(e + "nana");
        }



        this.start();


    }

    public Socket getCliSocket() {
        return cliSocket;
    }



    @Override
    protected synchronized Task<Void> createTask() {

        Task<Void> task = new Task<Void>() {
            protected Void call() throws InterruptedException {
                String inLine;
                System.out.println("call");
                try {


                    while (!loggedOn){

                        if (!username.isEmpty() && !password.isEmpty()) {
                            outStream.println(username + ":" + password);
                        }

                        while ((inLine = inStream.readLine()) != null) {
                            if (inLine.equals("[LogInApproved*OK]")){
                                loggedOn = true;
                                break;
                            } else if (inLine.equals("[LogInNotApproved*OK]")){
                                System.out.println("WRONG PASSORD OR USERNAME");
                            }


                        }

                    }



                    textField.setOnAction(event -> {



                        outStream.println("[SendingAMessage*OK]");
                        outStream.println(textField.getText());
                        if (chatHistory.get(username2).equals(""))textArea.setText("You: " + textField.getText());
                        else textArea.setText(textArea.getText() + "\n"  + "You: " + textField.getText());

                        chatHistory.put(username2,textArea.getText());



                        System.out.println("ONLINE: " + onlineUsers.toString() + " OFFLINE: " + offlineUsers.toString() + " BUSY: " + busyUsers.toString());
                        textField.clear();
                    });

                    String listOfUsers;
                    while ((inLine = inStream.readLine()) != null) {

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


                                areUsersOnline(observableOnline.isEmpty() || (sender != null && !observableOnline.contains(sender))
                                        || (!observableOnline.contains(username2) && !chatHistory.get(username2).equals("") ));

                            });


                        } else if (inLine.equals("[RequestingNEWChat*OK]")){


                        } else {
                            sender = inLine;
                            String message = inStream.readLine();
                            StringBuilder chat = new StringBuilder();



                            if (chatHistory.get(sender) == null || chatHistory.get(sender).equals("")) chat.append(sender + ": " + message);
                            else chat.append(chatHistory.get(sender)+ "\n" + sender + ": " + message);

                            chatHistory.put(sender,chat.toString());
                            textArea.setText(chatHistory.get(sender));
                            if (!sender.equals(username2)){
                                setConnectTo(sender);

                            }
                        }


                    }

                    System.out.println("call ferdig");

                    cliSocket.close();
                } catch (IOException e) {
                    System.out.println("IOEx");
                    outStream.close();
                }
                System.out.println("call ferdiglill");
                return null;
            }
        };






        return task;
    }


    public void setClientOnline(boolean clientOnline) {
        isClientOnline = clientOnline;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isLoggedOn() {
        return loggedOn;
    }

    public void areUsersOnline(boolean check){
        if (check) {
            textArea.setVisible(false);
            textArea.setDisable(true);
            startText.setText("user on the list is not available");
            startText.setVisible(true);
        } else {
            startText.setVisible(false);
            textArea.setVisible(true);

        }

        if (sender != null && !observableOnline.contains(sender)) sender = null;

    }

    public void setConnectTo(String username2) {

        outStream.println("[RequestingChat*OK]");
        startText.setVisible(false);
        textArea.setDisable(false);
        textArea.setVisible(true);


        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        outStream.println(username2);
        this.username2 = username2;

        textArea.setText(chatHistory.get(username2));




    }

    public ObservableList<String> getObservableOnline() {
        return observableOnline;
    }

    private void recieveLists() throws IOException{


        String line = inStream.readLine();

        if (line.equals("[SendingOnlineList*OK]")) {

            String listOfOnline;
            while ((listOfOnline = inStream.readLine()) != null && !listOfOnline.equals("[SendingOfflineList*OK]")) {
                onlineUsers.add(listOfOnline);

                if (!chatHistory.containsKey(username2))chatHistory.put(username2,"");


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

    public ObservableList<String> getObservableOffline() {
        return observableOffline;
    }

    public ObservableList<String> getObservableBusy() {
        return observableBusy;
    }
}
package sample;


/**
 * Created by Mustafe on 07.02.2017.
 */
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class ClientFX  extends Service<Void> {
    private Socket cliSocket;
    private PrintStream outStream;
    private BufferedReader inStream;
    ArrayList<String> users = new ArrayList<>();
    final ObservableList<String> observableUsers = FXCollections.observableArrayList();
    boolean isClientOnline,loggedOn = false;
    String hostName;
    int portNumber;
    TextArea textArea;
    TextField textField;
    String username,password;



    public ClientFX(String ipAdress, int port, TextField textField, TextArea textArea) {
        this.hostName = ipAdress;
        this.portNumber = port;
        this.textField = textField;
        this.textArea = textArea;

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


                            outStream.println(textField.getText());
                            if (textArea.getText().isEmpty()) textArea.setText("You: " + textField.getText());
                            else textArea.setText(textArea.getText() + "\n"  + "You: " + textField.getText());


                            textField.clear();
                        });

                    String listOfUsers;
                        while ((inLine = inStream.readLine()) != null) {

                            if (inLine.equals("[SendingListOfUsers*OK]")) {
                                users.clear();

                                while ((listOfUsers = inStream.readLine()) != null && !listOfUsers.equals("[SendingListOfUsers*DONE]")) {
                                    users.add(listOfUsers);


                                }
                           Platform.runLater(() ->{
                                    observableUsers.clear();
                                    observableUsers.addAll(users);

                                });

                            } else{
                                if (textArea.getText().isEmpty()) textArea.setText("Friend: " + inLine);
                                else textArea.setText(textArea.getText() + "\n" + "Friend: " + inLine);
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

    public void setConnectTo(String username2) {

        outStream.println("[RequestingChat*OK]");
        outStream.println(username);
        outStream.println(username2);
    }

    public ObservableList<String> getObservableUsers() {
        return observableUsers;
    }


}

package sample;


/**
 * Created by Mustafe on 07.02.2017.
 */
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

public class ClientFX  extends Service<Void> {
    private Socket cliSocket;
    private PrintStream outStream;
    private BufferedReader inStream;
    ArrayList<String> users = new ArrayList<>();
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
    protected Task<Void> createTask() {

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

                            }

                        }

                    }


                    recieveUserList(cliSocket);
                    System.out.println(users.toString());

                    textField.setOnAction(event -> {


                            outStream.println(textField.getText());
                            if (textArea.getText().isEmpty()) textArea.setText("You: " + textField.getText());
                            else textArea.setText(textArea.getText() + "\n"  + "You: " + textField.getText());


                            textField.clear();
                        });

                        while ((inLine = inStream.readLine()) != null) {

                            if (textArea.getText().isEmpty()) textArea.setText("Friend: " + inLine);
                            else textArea.setText(textArea.getText() + "\n" + "Friend: " + inLine);


                        }

                    System.out.println("call ferdig");

                    cliSocket.close();
                } catch (IOException e) {
                    System.out.println(e);
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

    public void setTextField(TextField textField) {
        this.textField = textField;
    }

    public void setTextArea(TextArea textArea) {
        this.textArea = textArea;
    }

    @SuppressWarnings("unchecked")
    private void recieveUserList(Socket s){
        try {
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            users = (ArrayList<String>) ois.readObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException cf){
            cf.getCause();
        }


    }

}

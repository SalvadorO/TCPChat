package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Mustafe on 20.02.2017.
 */
public class Server  extends Task<Void> {

    int portNumber;
    ThreadServer threadServer;
    ArrayList<Users> userList = new ArrayList<>();
    ArrayList<String> usersOnline = new ArrayList<>();
    ArrayList<String> usersOffline = new ArrayList<>();
    ArrayList<String> usersBusy = new ArrayList<>();
    ObservableList<String> observableOnline = FXCollections.observableArrayList();
    ObservableList<String> observableOffline = FXCollections.observableArrayList();
    ObservableList<String> observableBusy = FXCollections.observableArrayList();




    public Server(int portNumber)
    {
        this.portNumber = portNumber;
    }


    @Override
    public Void call() throws Exception
    {


        try(
                ServerSocket servSock = new ServerSocket(portNumber);
        ){

            System.out.println("server is up");
            listAllUsers();
            while(true){
                threadServer = new ThreadServer(servSock.accept(),usersOnline,usersOffline,usersBusy,userList
                        ,observableOnline,observableOffline,observableBusy);

                threadServer.start();


            }
        }
        catch(IOException e)  {
            System.out.println(e);

        }




        return null;
    }
    public void start()
    {
        Thread t = new Thread(this);
        t.start();
    }

    public void listAllUsers(){

        Pattern comp = Pattern.compile("(\\w+)[:](\\w+)");
        try {
            BufferedReader reader = new BufferedReader(new FileReader("passwd.txt"));
            String line;
            while((line = reader.readLine()) != null){
                Matcher matcher = comp.matcher(line);
                while(matcher.find()) {
                    usersOffline.add(matcher.group(1));

                }
            }
            observableOffline.setAll(usersOffline);

            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ObservableList<String> getObservableOnline() {
        return observableOnline;
    }

    public ObservableList<String> getObservableOffline() {
        return observableOffline;
        }

    public ObservableList<String> getObservableBusy() {
        return observableBusy;
    }

    public ArrayList<Users> getUserList() {
        return userList;
    }
}
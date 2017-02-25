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
 * This class extends Task<Void> which enables the client to connect to a server in a separated thread.
 * This class consists of lists that are passed to each separated thread.
 */

public class Server extends Task<Void> {

    int portNumber;
    ThreadServer threadServer;
    ArrayList<Users> userList = new ArrayList<>();
    ArrayList<String> usersOnline = new ArrayList<>();
    ArrayList<String> usersOffline = new ArrayList<>();
    ArrayList<String> usersBusy = new ArrayList<>();
    ObservableList<String> observableOnline = FXCollections.observableArrayList();
    ObservableList<String> observableOffline = FXCollections.observableArrayList();
    ObservableList<String> observableBusy = FXCollections.observableArrayList();


    /**
     * Construct an instance of this class.
     * @param portNumber sets a port number for the server socket.
     */
    public Server(int portNumber)
    {
        this.portNumber = portNumber;
    }

    /**
     * Creates new instances of ThreadServer each time server sockets connects to a client through the portnumber, and then starts its thread.
     * @return null
     * @throws Exception thrown when serversocket loses connection.
     */
    @Override
    public Void call() throws Exception
    {


        try(
                ServerSocket servSock = new ServerSocket(portNumber);
        ){

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

    /**
     * Creates a new Thread for this class.
     */
    public void start()
    {
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Adds usernames found in passwd.txt to an arraylist. This list consists of all user names that has been registered.
     * If passwd.txt is not found, then this metoden wouls throw and catch FileNotFoundException and print stacktrace.
     */
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

    /**
     * Observablelist that consists of usernames that are currently online.
     * @return the list of online user names.
     */
    public ObservableList<String> getObservableOnline() {
        return observableOnline;
    }
    /**
     * Observablelist that consists of usernames that are currently offline.
     * @return the list of offline user names.
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
     * Arraylist that consists of instances of the class Users. Each has their own username, socket instance ect.
     * @return the list of users.
     */
    public ArrayList<Users> getUserList() {
        return userList;
    }
}
package sample;

import javafx.collections.FXCollections;
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
    ArrayList<Users> userList = new ArrayList<>();
    boolean test[] = new boolean[1];
    ArrayList<String> usersOnline = new ArrayList<>();
    ArrayList<String> usersOffline = new ArrayList<>();
    ArrayList<String> usersBusy = new ArrayList<>();
    private java.net.URL URL = getClass().getResource("ServerFX/../../passwd.txt");




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
            test[0] = true;
            while(true){
                ThreadServer threadServer = new ThreadServer(servSock.accept(),test,usersOnline,usersBusy,usersOffline,userList);
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
                    usersBusy.add(matcher.group(1));
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
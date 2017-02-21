package sample;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Mustafe on 20.02.2017.
 */

public class Users implements Serializable {
    public Socket socket;
    public String username;
    private String status;
    public InetAddress userIP;
    public int userPort;
    public OutputStream userOutStream;
    private java.net.URL URL = getClass().getResource("passwd.txt");
    private String PATH = URL.getPath();
    private ArrayList<String> listOfUsers;



    public Users(Socket socket, String username) throws IOException {
        this.socket = socket;
        this.username = username;
        this.status = "OFFLINE";
        listOfUsers = new ArrayList<>();
        userIP = socket.getInetAddress();
        userPort = socket.getPort();
        userOutStream = socket.getOutputStream();


    }

    public Socket getSocket() {
        return socket;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean manageUser(String info, String key){
        if(key.equals("login")){
            if( checkLogin(info))return true;

        }
        return false;
    }

    private boolean checkLogin(String string){
        Pattern comp = Pattern.compile("(\\w+)[:](\\w+)");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(PATH));
            String line;
            while((line = reader.readLine()) != null){
                Matcher matcher = comp.matcher(line);
                while(matcher.find() && string.equals(line)) {
                    System.out.println("MATCH");
                    return true;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("NO MATCH");

        return false;
    }
    @Override
    public String toString(){
        Pattern comp = Pattern.compile("(\\w+)[:](\\w+)");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(PATH));
            String line;
            while((line = reader.readLine()) != null){
                Matcher matcher = comp.matcher(line);
                while(matcher.find() ) {
                    listOfUsers.add(line);
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return listOfUsers.toString();

    }



}


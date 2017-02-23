package sample;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Users implements Serializable {
    public Socket socket;
    public String username;
    public InetAddress userIP;
    public int userPort;
    public OutputStream userOutStream;
    private java.net.URL URL = getClass().getResource("passwd.txt");
    private String PATH = URL.getPath();
    Pattern comp = Pattern.compile("(\\w+)[:](\\w+)");




    public Users(Socket socket, String username) throws IOException {
        this.socket = socket;
        this.username = username;
        userIP = socket.getInetAddress();
        userPort = socket.getPort();
        userOutStream = socket.getOutputStream();


    }


    public boolean manageUser(String info, String key) throws IOException{
        if(key.equals("login")){
            if( checkLogin(info))return true;

        }else if (key.equals("checkExistingUser")){
            if (usernameExists(info))return true;
        }
        return false;
    }

    private boolean checkLogin(String string){
        try {
            BufferedReader reader = new BufferedReader(new FileReader("passwd.txt"));
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

    boolean usernameExists(String string) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("passwd.txt"));
        String line;
        while((line = reader.readLine()) != null){
            Matcher matcher = comp.matcher(line);
            if(matcher.find()) {
                if(matcher.group(1).equals(string))return true;
            }

        }
        reader.close();
        return false;
    }



    void writeToFile(String string) throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("passwd.txt", true)));
        out.println(string);
        out.close();

    }

    @Override
    public String toString() {
        return "User Info: " +
                "port=" + socket.getLocalPort() +
                " username='" + username + '\'' +
                " userIP=" + userIP +
                " userPort=" + userPort +".";

    }
}

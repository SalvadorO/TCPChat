package soc001;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sindre on 13.02.2017.
 */
public class Users implements Serializable {
    public Socket socket;
    public String username;
    public InetAddress userIP;
    public int userPort;
    public OutputStream userOutStream;
    private URL URL = getClass().getResource("passwd.txt");
    private String PATH = URL.getPath();

    public Users(Socket socket, String username) throws IOException {
        this.socket = socket;
        this.username = username;
        userIP = socket.getInetAddress();
        userPort = socket.getPort();
        userOutStream = socket.getOutputStream();

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



}

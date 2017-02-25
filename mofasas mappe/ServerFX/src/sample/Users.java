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

/**
 * Each time an user attempts to sign in, a new instance of this class is created in order to record their socket
 * and username. This enables the server to connect  clients together and create conversations.  This class has also some
 * methods that is used to check if the user is allowed to sign in.
 */

public class Users  {
    public Socket socket;
    public String username;
    public InetAddress userIP;
    public int userPort;
    public OutputStream userOutStream;
    private java.net.URL URL = getClass().getResource("passwd.txt");
    private String PATH = URL.getPath();
    Pattern comp = Pattern.compile("(\\w+)[:](\\w+)");


    /**
     * Construct a new instance of this class and assigns values to class variables.
     * @param socket the socket that the client is connected to.
     * @param username the username that belongs to the client.
     * @throws IOException IOException thrown if socket connection is lost
     */
    public Users(Socket socket, String username) throws IOException {
        this.socket = socket;
        this.username = username;
        userIP = socket.getInetAddress();
        userPort = socket.getPort();
        userOutStream = socket.getOutputStream();


    }


    /**
     * This method will manage this class when a method is called. Depending on what key parameter it gets, it will run different
     *  methods when called. For example key value "login" will run the checktLogin() method.
     * @param info user linfo that is used for the methods (example user password/username)
     * @param key key that determine which method that will be used
     * @return returns false if key is unrecognizable
     * @throws IOException IOException thrown
     */
    public boolean manageUser(String info, String key) throws IOException{
        if(key.equals("login")){
            if( checkLogin(info))return true;

        }else if (key.equals("checkExistingUser")){
            if (usernameExists(info))return true;
        }
        return false;
    }

    /**
     * This gets a password and username and checks if passwd.txt contains the strings. This method is used to see
     * if someone tries to sign in.
     * @param string username and password
     * @return returns true if there are a match(passwd.txt containing the strings), and returns false if not.
     */
    private boolean checkLogin(String string){
        try {
            BufferedReader reader = new BufferedReader(new FileReader("passwd.txt"));
            String line;
            while((line = reader.readLine()) != null){
                Matcher matcher = comp.matcher(line);
                while(matcher.find() && string.equals(line)) {
                    return true;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This method checks if passwd.txt contains an username
     * @param string username
     * @return return true if the username is found in the text file.
     * @throws IOException IOException thrown.
     */
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


    /**
     * This method appends a new text on a file that consists of a username and password.
     * @param string username and password
     * @throws IOException Exception thrown.
     */
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

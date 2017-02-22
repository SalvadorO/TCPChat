package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.PasswordField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Mustafe on 20.02.2017.
 */
public class ThreadServer extends Service<Void>
{

    Socket sock;
    InetAddress cliAddr;
    int cliPort;
    int servPort;
    ArrayList<Users> listOfOnlineUsers = new ArrayList<>();
    ArrayList<String> onlineUsernames = new ArrayList<>();
    ArrayList<String> offlineUsernames = new ArrayList<>();
    ArrayList<String> busyUsernames = new ArrayList<>();

    boolean[] test;
    Users user;
    String myUsername;
    PrintWriter out;
    BufferedReader in;



    public ThreadServer(Socket sock, boolean[] test, ArrayList<String> onlineUsernames, ArrayList<String> offlineUsernames,
                        ArrayList<String> busyUsernames     ,ArrayList<Users> usersList ) {
        this.sock = sock;
        cliAddr = sock.getInetAddress();
        cliPort = sock.getPort();
        servPort = sock.getLocalPort();

        this.test = test;
        this.onlineUsernames = onlineUsernames;
        this.offlineUsernames = offlineUsernames;
        this.busyUsernames = busyUsernames;
        this.listOfOnlineUsers = usersList;

    }

    @Override
    protected Task<Void> createTask() {
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException {

                ObservableMap<String, Socket> map = FXCollections.observableHashMap();

                System.out.println("port: " + sock.getPort() + " br1: " );


                try {

                    //Creating a writer and reader for the server socket.
                    out = new PrintWriter(sock.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                    //Checking username and password
                    checkUserPassword();

                    //Sending lists of users and their states
                    sendUserList();

                    // recieving messages from clients (chat, change in state etc.)
                    String firstLine = in.readLine();
                    firstLine = setClientState(firstLine);
                    if ((firstLine != null && firstLine.equals("[RequestingChat*OK]"))) manageChat();

                    // clients who are connected but not in a conversation
                    while (isSocketConnected(sock)){

                        Thread.sleep(1);

                    }

                } catch (IOException e) {
                    System.out.println(e + " nei det er en IOEXEPTION");

                } catch (InterruptedException p){

                } catch (ClassNotFoundException cs){
                    cs.getCause();
                } catch(ConcurrentModificationException cm){
                    System.out.println("concurrent modexpetionz");
                }finally{


                    listOfOnlineUsers.remove(user);
                    onlineUsernames.remove(myUsername);
                    if (busyUsernames.contains(myUsername)) busyUsernames.remove(myUsername);
                    offlineUsernames.add(user.username);

                    System.out.println("sock er closed");



                }

                return null;
            }
        };
        return task;
    }
    private void message(Socket sender, Socket reciever)  throws IOException {
        PrintWriter out = new PrintWriter(reciever.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(sender.getInputStream()));


        String recievedText;


        while ((recievedText = in.readLine()) != null) {
            System.out.println("Dette er mld->" + recievedText + "<-");
            System.out.println("my username is: " + myUsername);

            switch (recievedText){
                case "[RequestingChat*OK]" :  manageChat();
                    break;
                case "[SendingAMessage*OK]" : {
                    String message = in.readLine();
                    out.println(myUsername);
                    out.println(message);
                    System.out.println("server sender dette: " + message);
                    break;
                }
                case "[SetClientBusy*OK]" : setClientBusy();
                    break;
                case "[SetClientOnline*OK]" : setClientOnline();
                    break;
            }
        }
        sock.close();
        in.close();


    }


    private String setClientState( String firstLine) throws IOException, InterruptedException{
        while (true){

            if (firstLine != null) System.out.println("g: " + firstLine);
            if (firstLine != null && firstLine.contains("SetClientOnline*OK")) {
                setClientOnline();
                firstLine = "";
            }
            else if (firstLine != null && firstLine.contains("SetClientBusy*OK")){
                setClientBusy();
                firstLine = "";
            } else return firstLine;

            firstLine = in.readLine();
            Thread.sleep(1);


        }
    }

    private void checkUserPassword() throws IOException, ClassNotFoundException{



        String signInLine;

        while ((signInLine = in.readLine()) != null) {

            String username = extractUsername(signInLine);
            user = new Users(sock,username);

            if (signInLine.equals("[CreateNewUser*OK]")){
               String usernamepasswd = in.readLine();

                username = extractUsername(usernamepasswd);
                user.username = username;
                if (user.manageUser(username,"checkExistingUser")){
                    System.out.println("Bruker allrede eksisterer!");
                    out.println("[CreateNewUser*ERROR]");
                } else {
                    System.out.println("EY det funka å laggd en ny bruker: " + usernamepasswd);
                    user.writeToFile(usernamepasswd);

                    listOfOnlineUsers.add(user);
                    myUsername = user.username;
                    onlineUsernames.add(myUsername);
                    offlineUsernames.remove(myUsername);

                    out.println("[LogInApproved*OK]");
                    break;

                }


            }


           else if (user.manageUser(signInLine,"login")){
                


                if (onlineUsernames.contains(user.username)){
                    out.println("[UserIsOnline*ERROR]");
                    System.out.println("aredde logged");
                    break;
                }

                listOfOnlineUsers.add(user);
                myUsername = user.username;
                onlineUsernames.add(myUsername);
                offlineUsernames.remove(myUsername);

                out.println("[LogInApproved*OK]");
                System.out.println(signInLine);
                break;

            } else
                out.println("[LogInNotApproved*OK]");


        }

    }
    private boolean isSocketConnected(Socket s) throws IOException{
        return s.getInputStream().read() != -1;
    }


    private String extractUsername(String string){
        String pattern = "(\\w+)[:](\\w+)";
        Pattern comp = Pattern.compile(pattern);
        Matcher match = comp.matcher(string);
        if(match.find()){
            System.out.println(match.group(1));
            return match.group(1);
        }
        return null;
    }

    private void sendUserList(){

        Timer t = new Timer();



        t.scheduleAtFixedRate(
                new TimerTask()
                {
                    public void run()
                    {
                        out.println("[SendingListOfUsers*OK]");

                        out.println("[SendingOnlineList*OK]");

                        for (String users : onlineUsernames) {

                            out.println(users);
                        }

                        out.println("[SendingOfflineList*OK]");
                        for (String users : offlineUsernames) {

                            out.println(users);
                        }

                        out.println("[SendingBusyList*OK]");
                        for (String users : busyUsernames) {

                            out.println(users);
                        }


                        out.println("[SendingListOfUsers*DONE]");                    }
                },
                0,      // run first occurrence immediately
                1000);  // run every three seconds





    }

    private void manageChat() throws IOException {

        Socket socket2 = null;
        String user2 = in.readLine();

        System.out.println("OFFLINE USERS: " + offlineUsernames.toString()  + " ONLINE USERS: " + onlineUsernames.toString());

        System.out.println("user2: " + user2);

        for (Users user : listOfOnlineUsers) {


            if (user.username.equals(user2)) {
                System.out.println("bruker2: " + user.username);
                socket2 = user.socket;



            }

        }
        if (socket2 != null) {

            message(sock, socket2);


        }



    }

    private void setClientBusy(){
        if (onlineUsernames.contains(myUsername)){
            onlineUsernames.remove(myUsername);
            offlineUsernames.remove(myUsername);
            busyUsernames.add(myUsername);
        }

    }

    private void setClientOnline(){
        if (busyUsernames.contains(myUsername)){
            busyUsernames.remove(myUsername);
            onlineUsernames.add(myUsername);
        }

    }
}
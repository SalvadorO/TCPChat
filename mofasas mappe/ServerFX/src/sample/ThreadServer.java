package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
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
 * This class serves a new thread each time a client is connected. The thread gets an uniqe socket that will be used to indetify client socket.
 * It serves information and functionality when it receives requests from the client, such as creating a conversation, sending lists of usernames
 * and changing the state/status of the client.
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
    ObservableList<String> observableOnline;
    ObservableList<String> observableOffline;
    ObservableList<String> observableBusy;


    Users user;
    String myUsername;
    PrintWriter out;
    BufferedReader in;


    /**
     *  Constructor new instance of this class each time a client connects. Now will this class have access to client socket.
     *  then it will set values to its socket and lists (online, busy and offline).
     * @param sock set value to socket.
     * @param onlineUsernames set value to online username list.
     * @param offlineUsernames  set value to offline username list.
     * @param busyUsernames  set value to busy username list.
     * @param usersList  set value to user list.
     * @param observableOnline  set value to observable online username list,
     * @param observableOffline  set value to observable offline username list
     * @param observableBusy set value to observable busy username list
     */
    public ThreadServer(Socket sock, ArrayList<String> onlineUsernames, ArrayList<String> offlineUsernames,
                        ArrayList<String> busyUsernames ,ArrayList<Users> usersList, ObservableList<String> observableOnline,
                        ObservableList<String> observableOffline, ObservableList<String> observableBusy) {
        this.sock = sock;
        cliAddr = sock.getInetAddress();
        cliPort = sock.getPort();
        servPort = sock.getLocalPort();


        this.onlineUsernames = onlineUsernames;
        this.offlineUsernames = offlineUsernames;
        this.busyUsernames = busyUsernames;
        this.listOfOnlineUsers = usersList;
        this.observableBusy = observableBusy;
        this.observableOffline = observableOffline;
        this.observableOnline = observableOnline;

    }

    /**
     *  Creates the task this class serves to the connected client, (in a separated thread).
     * @return returns the task this class runs on.
     */
    @Override
    protected Task<Void> createTask() {
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException {

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

                } catch (InterruptedException p){
                    alertDialogs("Interrupted Exception",p.getMessage());
                } catch (ClassNotFoundException cs){
                    alertDialogs("ClassNotFoundException Exception",cs.getMessage());

                } catch(ConcurrentModificationException cm){
                    alertDialogs("ConcurrentModificationException",cm.getMessage());
                }finally{


                    // At this time client has stopped sending messages to the server, in which its socket has closed,
                    // and removes/adds the client into the lists.
                    listOfOnlineUsers.remove(user);
                    onlineUsernames.remove(myUsername);
                    if (busyUsernames.contains(myUsername)) busyUsernames.remove(myUsername);
                    offlineUsernames.add(user.username);


                }

                return null;
            }
        };
        return task;
    }

    /**
     * Depending on the inputs, and what the String value receivedText is, this method will create a new chat, set state on a client
     * or send a message to an another client.
     * @param sender  a socket in which a client sends a message though.
     * @param receiver a socket in which a client receives a message from.
     * @throws IOException throws and exception if one of the 2 sockets disconnects from this server
     */
    private void message(Socket sender, Socket receiver)  throws IOException {
        PrintWriter out = new PrintWriter(receiver.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(sender.getInputStream()));


        String recievedText;

        while ((recievedText = in.readLine()) != null) {

            switch (recievedText){
                case "[RequestingChat*OK]" :  manageChat();
                    break;
                case "[SendingAMessage*OK]" : {
                    String message = in.readLine();
                    out.println(myUsername);
                    out.println(message);
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

    /**
     * This method toggles the state of a client depending on what message it gets from the client.
     * However this method will run only if a client has not entered a chat before changing its state
     * @param firstLine the messeage the client sends to this server.
     * @return  if parameter value is not request of changing a client state, it will break out of the while loop
     * and return the newest message from the connected client.
     * @throws IOException throw IOException if server loses connection to the client socket.
     * @throws InterruptedException throws InterruptedException if current thread gets interrupted.
     */
    private String setClientState( String firstLine) throws IOException, InterruptedException{
        while (true){

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

    /**
     * A method that creates a while-loop which waits for client input.
     * The method will run until it sends a login aproval message back to the client
     * @throws IOException throw IOException if server loses connection to the client socket
     * @throws ClassNotFoundException throw ClassNotFoundException method tries to load in class that can not be found.
     */
    private void checkUserPassword() throws IOException, ClassNotFoundException{


        String signInLine;

        while ((signInLine = in.readLine()) != null) {

            // Creates an instance of Users class with its socket instance and username
            String username = extractUsername(signInLine);
            user = new Users(sock,username);

            // if client wants to create a new user.
            if (signInLine.equals("[CreateNewUser*OK]")){
               String usernamepasswd = in.readLine();

                username = extractUsername(usernamepasswd);
                user.username = username;

                // checks if the username already exists.
                if (user.manageUser(username,"checkExistingUser")){
                    out.println("[CreateNewUser*ERROR]");

                // checks if the username has a lenght of 4 or less.
                } else if (username == null || username.length() < 4){
                    out.println("[CreateUsername*ERROR]");
                } else {
                    // finally creates an new user

                    // saves the username and password into a textfile (passwd.txt)
                    user.writeToFile(usernamepasswd);

                    // adds/removes username from the lists (busy, offline, online) before sending an approval message.
                    listOfOnlineUsers.add(user);
                    myUsername = user.username;
                    onlineUsernames.add(myUsername);
                    offlineUsernames.remove(myUsername);


                    out.println("[LogInApproved*OK]");
                    break;

                }

            }


            // If the client wants to sign in
           else if (user.manageUser(signInLine,"login")){
                


                // checks if the user is already signed in.
                if (onlineUsernames.contains(user.username)){
                    out.println("[UserIsOnline*ERROR]");
                    break;
                }
                // the user is allowd to login

                // updates lists beore sending an approval message to the client
                listOfOnlineUsers.add(user);
                myUsername = user.username;
                onlineUsernames.add(myUsername);
                offlineUsernames.remove(myUsername);

                out.println("[LogInApproved*OK]");
                break;

            } else
                out.println("[LogInNotApproved*OK]");


        }

    }

    /**
     * this method checks if the client is connected to this (separated thread) server, if not, an exception is thrown.
     * @param s the socket that belongs to the client.
     * @return returns a true/false statement while reading from a inputstream  to check if it is still connected,
     * @throws IOException throws IOException when socket is disconnected.
     */
    private boolean isSocketConnected(Socket s) throws IOException{
        return s.getInputStream().read() != -1;
    }


    /**
     * This method gets a string and extract the username from it.
     * @param string a string line that consists of password and username
     * @return returns an username
     */
    private String extractUsername(String string){
        String pattern = "(\\w+)[:](\\w+)";
        Pattern comp = Pattern.compile(pattern);
        Matcher match = comp.matcher(string);
        if(match.find()){
            return match.group(1);
        }
        return null;
    }

    /**
     * This method creates a timer that for each second sends lists (offline, online, busy) to all clients.
     */
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
                        out.println("[SendingListOfUsers*DONE]");

                        // clears and adds arraylist to observablelist.
                        Platform.runLater(() ->{
                                    observableOffline.clear();
                                    observableOffline.addAll(offlineUsernames);

                                    observableOnline.clear();
                                    observableOnline.addAll(onlineUsernames);

                                    observableBusy.clear();
                                    observableBusy.addAll(busyUsernames);

                                }
                        );


                    }
                },
                0,      // immediately
                1000);  // run every one second





    }

    /**
     * This methoed iterates the user list that consists of Users instances. First it gets an input from the client (String user2)
     * then compares the username to all users. When it finally finds the User instance, it will extract the socket instance and send it to
     * message() method
     * @throws IOException throws IOException when socket is disconnected.
     */
    private void manageChat() throws IOException {

        Socket socket2 = null;
        String user2 = in.readLine();


        for (Users user : listOfOnlineUsers) {

            if (user.username.equals(user2)) {
                // username match!
                socket2 = user.socket;

            }

        }
        if (socket2 != null) {

            // if socket is not null, create a conversation between sock/sender and socket2/receiver.
            message(sock, socket2);


        }



    }

    /**
     * changing client state by removing/adding user on the lists.
     */
    private void setClientBusy(){
        if (onlineUsernames.contains(myUsername)){
            onlineUsernames.remove(myUsername);
            offlineUsernames.remove(myUsername);
            busyUsernames.add(myUsername);
        }

    }
    /**
     * changing client state by removing/adding user on the lists.
     */
    private void setClientOnline(){
        if (busyUsernames.contains(myUsername)){
            busyUsernames.remove(myUsername);
            onlineUsernames.add(myUsername);
        }

    }

    /**
     * When the compiler throws an exception, this method will be called.
     * It will create an alert popup that describes the exceptions.
     * @param headerText sets text on the header area.
     * @param contentText sets text on the content area.
     */
    private void alertDialogs(String headerText, String contentText){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText(headerText);
            alert.setContentText(contentText);

            alert.showAndWait();
        });
    }


}
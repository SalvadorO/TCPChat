package soc001;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ThreadServer extends Thread {
    Socket sock;
    InetAddress cliAddr;
    int cliPort;
    int servPort;
    ArrayList<Socket> sockets;
    ArrayList<Users> usersList = new ArrayList<>();
    ArrayList<String> usersnames = new ArrayList<>();
    boolean[] test;
    Users user;
    String myUsername;
    PrintWriter out;
    BufferedReader in;



    public ThreadServer(Socket sock, ArrayList<Socket> users, boolean[] test, ArrayList<String> usersnames,ArrayList<Users> usersList) {
        this.sock = sock;
        cliAddr = sock.getInetAddress();
        cliPort = sock.getPort();
        servPort = sock.getLocalPort();
        this.sockets = users;
        this.test = test;
        this.usersnames = usersnames;
        this.usersList = usersList;

    }



    @Override
    public void run() {

        ObservableMap<String, Socket> map = FXCollections.observableHashMap();

        System.out.println("port: " + sock.getPort() + " br1: " + sockets.get(0).getPort());


        try {

             out = new PrintWriter(sock.getOutputStream(), true);
             in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            checkUserPassword();

            sendUserList();

            String g;
            if ((g = in.readLine()) != null && g.equals("[RequestingChat*OK]")) manageChat();


            // or wait ...
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
            sockets.remove(sock);
            usersList.remove(user);
            usersnames.remove(user.username);

            System.out.println(sockets.size() + "sock er closed");



        }
     }

    


    private void message(Socket sender, Socket reciever)  throws IOException {
       PrintWriter out = new PrintWriter(reciever.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(sender.getInputStream()));


        String recievedText;


        while ((recievedText = in.readLine()) != null) {
            System.out.println("Dette er mld->" + recievedText + "<-");
            System.out.println("my username is: " + myUsername);

            if (recievedText.equals("[RequestingChat*OK]")) {

                manageChat();


            } else if (recievedText.equals("[SendingAMessage*OK]")) {
                String message = in.readLine();
                out.println(myUsername);
                out.println(message);
                System.out.println("server sender dette: " + message);
            }

        }
        sock.close();
        in.close();


    }

    private void checkUserPassword() throws IOException, ClassNotFoundException{



        String usernamepassword;

        while ((usernamepassword = in.readLine()) != null) {


            String username = extractUsername(usernamepassword);
            user = new Users(sock,username);

            if (user.manageUser(usernamepassword,"login")){

                usersList.add(user);
                usersnames.add(user.username);
                myUsername = user.username;
                out.println("[LogInApproved*OK]");
                System.out.println(usernamepassword);
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
                        for (String username : usersnames) out.println(username);
                        out.println("[SendingListOfUsers*DONE]");                    }
                },
                0,      // run first occurrence immediately
                1000);  // run every three seconds





    }

    private void manageChat() throws IOException {

        Socket socket2 = null;
        String user2 = in.readLine();


        System.out.println("user2: " + user2);

        for (Users user : usersList) {


            if (user.username.equals(user2)) {
                System.out.println("bruker2: " + user.username);
                socket2 = user.socket;



            }

        }
        if (socket2 != null) {

            message(sock, socket2);


        }

    }

}


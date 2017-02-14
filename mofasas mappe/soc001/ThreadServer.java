package soc001;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
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
    ObjectOutputStream objectOutput;
    Users user;



    public ThreadServer(Socket sock, ArrayList<Socket> users, boolean[] test, ArrayList<Users> usersList) {
        this.sock = sock;
        cliAddr = sock.getInetAddress();
        cliPort = sock.getPort();
        servPort = sock.getLocalPort();
        this.sockets = users;
        this.test = test;
        this.usersList = usersList;

    }

    @Override
    public void run() {

        ObservableMap<String, Socket> map = FXCollections.observableHashMap();

        System.out.println("port: " + sock.getPort() + " br1: " + sockets.get(0).getPort());


        try {
             objectOutput = new ObjectOutputStream(sock.getOutputStream());


            checkUserPassword();
            sendUserList();




            // chat
            chat(0,1);

            // or wait ...
            while (isSocketConnected(sock)){
                Thread.sleep(1);
            }


        } catch (IOException e) {
            System.out.println(e + " nei det er en IOEXEPTION");

        } catch (InterruptedException p){

        } finally {
            sockets.remove(sock);
            usersList.remove(user);
            usersnames.remove(user.username);
            System.out.println(sockets.size() + "sock er closed");



        }
     }

    

    private void chat(int clientNr1, int clientNr2) throws IOException, InterruptedException{

        int firstClient = clientNr1 > clientNr2 ? clientNr2 : clientNr1;
        int secnClient = clientNr2 > clientNr1 ? clientNr2 : clientNr1;


        if (sockets.size() <= secnClient && sockets.size() > firstClient  && sockets.get(firstClient) == sock) {
            test[0] = false;

            while (!test[0] && isSocketConnected(sockets.get(firstClient))){
                Thread.sleep(1);

            }
            if (isSocketConnected(sock))
            message(sock, sockets.get(secnClient));
        }

        else if (sockets.size() > secnClient && sock == sockets.get(secnClient)) {
            test[0] = true;
            message(sock, sockets.get(firstClient));
        }

    }

    private  void message(Socket sender, Socket reciever)  throws IOException {

        PrintWriter out = new PrintWriter(reciever.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(sender.getInputStream()));

        String recievedMsg;

        while ((recievedMsg = in.readLine()) != null) {


            String outMsg = recievedMsg;
            out.println(outMsg);
            System.out.println(recievedMsg);



        }
        System.out.println("lego");
    }

    private void checkUserPassword() throws IOException{
        PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

        String usernamepassword;

        while ((usernamepassword = in.readLine()) != null) {


            String username = extractUsername(usernamepassword);
            user = new Users(sock,username);

            if (user.manageUser(usernamepassword,"login")){

                usersList.add(user);
                usersnames.add(user.username);
                out.println("[LogInApproved*OK]");
                System.out.println(usernamepassword);

                break;
            }


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
        try {
            objectOutput.writeObject(usersnames);
            objectOutput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


package soc001;
import java.io.IOException;
import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class Server {

    public static void main(String[] args) throws IOException {
        int portNumber = 5555;
        ArrayList<Socket> sockets;
        sockets = new ArrayList<>();
        boolean test[] = new boolean[1];
        test[0] = true;
        ArrayList<Users> userList = new ArrayList<>();
        ArrayList<String> usernames = new ArrayList<>();



        if(args.length > 0){
            if(args.length == 1) portNumber = Integer.parseInt(args[0]);
            else{
                System.err.println("Please write a valid port number");
            }
        }
        System.out.println("Your server is up");

        try(
                ServerSocket servSock = new ServerSocket(portNumber);
        ){
            while(true){
                ThreadServer threadServer = new ThreadServer(servSock.accept(), sockets,test,usernames,userList);
                sockets.add(threadServer.sock);
                threadServer.start();

                System.out.println(sockets);
            }
        }
        catch(IOException e)  {
            System.out.println(e);

        }


    }
}

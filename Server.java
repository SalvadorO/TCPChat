package Oblig1.TCPChat;

import java.io.IOException;
import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class Server {

    public static void main(String[] args) throws IOException {
        int portNumber = 5555;
        ArrayList<Socket> users;
        users = new ArrayList<>();
        if(args.length > 0){
            if(args.length == 1) portNumber = Integer.parseInt(args[0]);
            else{
                System.err.println("Please write a valid port number");
            }
        }
        
        try(
            ServerSocket servSock = new ServerSocket(portNumber);
            ){
            System.out.println("Your server is up");
        while(true){
            ThreadServer threadServer = new ThreadServer(servSock.accept(), users);
            users.add(threadServer.sock);
            threadServer.start();
            System.out.println(users);
                   }
             }
        catch(IOException e)  {
              System.out.println(e);
        
        }
        
    
        }
    }


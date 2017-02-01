package Oblig1.TCPChat;

import java.io.IOException;
import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class Server {

    public static void main(String[] args) throws IOException {
        int portNumber = 5555;
        ArrayList<Socket> USERS;
        USERS = new ArrayList<>();
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
        
            String recievedMsg;
        while(true){
            ThreadServer threadServer = new ThreadServer(servSock.accept());
            USERS.add(threadServer.sock);
            threadServer.start();
            System.out.println(USERS);
                   }
             }
        catch(IOException e)  {
              System.out.println(e);
        
        }
        
    
        }
    }


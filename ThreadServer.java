
package Oblig1.TCPChat;

import java.net.*;
import java.io.*;
import java.util.ArrayList;


public class ThreadServer extends Thread {
    Socket sock;
    InetAddress cliAddr;
    int cliPort;
    int servPort;
    ArrayList<Socket> users;
    
    public ThreadServer (Socket sock, ArrayList<Socket> users){
        this.sock = sock;
        cliAddr = sock.getInetAddress();
        cliPort = sock.getPort();
        servPort = sock.getLocalPort();
        this.users = users;
    }
    
    @Override
    public void run(){
        try(
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                ){
                 String recievedMsg;
                 while((recievedMsg = in.readLine()) != null){
                     System.out.println(recievedMsg);
                     String outMsg = recievedMsg.toUpperCase();
                     System.out.println(outMsg);
                     
                     //if(users.get(1).getPort() == cliPort){ 
                         out.println(outMsg);
                     //}
                 }
                 sock.close();
                 }
        catch(IOException e){
            System.out.println(e);
        }
    }
    
}

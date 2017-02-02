
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
    String recivedNum;

    
    
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
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                ){
                 recivedNum = in.readLine();
                 sock.close();
                 System.out.println("ETTER: " + recivedNum);
                 }
        catch(IOException e){
            System.out.println(e);
        }

        try(
            PrintWriter out = new PrintWriter(users.get(Integer.parseInt(recivedNum)).getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                ){
                 String recievedMsg;
                 while((recievedMsg = in.readLine()) != null){
                     System.out.println(recievedMsg);
                     String outMsg = recievedMsg;
                     System.out.println(outMsg);
                     out.println(outMsg);
                     
                 }
                 sock.close();
                 }
        catch(IOException e){
            System.out.println(e);
        }
    }
    
}

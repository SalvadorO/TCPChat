
package Oblig1;

import java.net.*;
import java.io.*;


public class ThreadServer extends Thread {
    
    Socket sock;
    InetAddress cliAddr;
    int cliPort;
    int servPort;
    
    public ThreadServer (Socket sock){
        this.sock = sock;
        cliAddr = sock.getInetAddress();
        cliPort = sock.getPort();
        servPort = sock.getLocalPort();
    }
    
    @Override
    public void run(){
        try(
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                ){
                 String recievedMsg = in.readLine();
                 while(recievedMsg != null){
                     String outMsg = recievedMsg.toUpperCase();
                     out.println(outMsg);
                 }
                 sock.close();
                 }
        catch(IOException e){
            System.out.println(e);
        }
    }
    
}

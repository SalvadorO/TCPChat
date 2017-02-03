
package Oblig1.TCPChat;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ThreadServer extends Thread {
   public Socket sock;
   public InetAddress cliAddr;
   public int cliPort;
   public int servPort;
   public ArrayList<Socket> users;
   public String talkToNum = "0";

    
    
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
      )
      {
                 String receivedMsg;
                 while((receivedMsg = in.readLine()) != null){
                     System.out.println("Motatt melding er : " + receivedMsg + " !");
                     if(getTalkToNum(receivedMsg).matches()) {
                         talkToNum = setTalkToNum(getTalkToNum(receivedMsg));
                         System.out.println(sock.getPort() + " snakker til " + users.get(Integer.parseInt(talkToNum)).getPort());
                     }
                     String outMsg = receivedMsg;
                     PrintWriter out = new PrintWriter(users.get(Integer.parseInt(talkToNum)).getOutputStream(), true);
                     //System.out.println(outMsg);
                     out.println(outMsg);
                     
                 }
                 sock.close();
                 }
        catch(IOException e){
            System.out.println(e);
        }
    }
    
    private Matcher getTalkToNum(String a){
        String p = "(.+)([: \\s /])(\\d+)";
        Pattern pt = Pattern.compile(p);
        Matcher m = pt.matcher(a);
        return m;
    }
    private String setTalkToNum(Matcher m){
        String b = null;
        while(m.find()){
            b = m.group(3);
        }
                   return b;
    }
}

package Oblig1.TCPChat;

import java.io.IOException;
import java.net.*;
import java.io.*;

public class Client implements Runnable {
    private static Socket cliSocket;
    private static PrintStream outStream;
    private static DataInputStream inStream;
    private static BufferedReader keyIn;
    private static boolean closed = false;
    private static String uNum;

    public static void main(String[] args) throws IOException {
        String hostName = "localhost";
        int portNumber = 5555;
        if (args.length > 0)
        {
           hostName = args[0];
           if (args.length > 1)
           {
             portNumber = Integer.parseInt(args[1]);
             if (args.length > 2)
             {
               System.err.println("Usage: java EchoClientTCP [<host name>] [<port number>]");
               System.exit(1);
             }
           }
        }
        System.out.println("Client is up!");
        System.out.println("Enter usernumber to chat with: ");
        
        uNum = userNum();
        try{
              cliSocket = new Socket(hostName, portNumber);
              outStream = new PrintStream(cliSocket.getOutputStream());
              inStream = new DataInputStream(cliSocket.getInputStream());
              keyIn = new BufferedReader(new InputStreamReader(System.in));
            }
                      
        catch(UnknownHostException e){System.out.println("Unknown host!");}
        catch(IOException e){System.out.println(e);}
        
        /*
        * Everything up and running?
        * Lets write to the socket
        * 
        */
        if(cliSocket != null && outStream != null && inStream != null){
            try{
                new Thread(new Client()).start();
                
                while(!closed){
                outStream.print(uNum);
                //System.out.println(uNum);
                outStream.println(keyIn.readLine().trim());
                }
                outStream.close();
                inStream.close();
                cliSocket.close();
            }
            catch(IOException e){
                System.out.println(e);
            }
        }
    }
    

    
    
    /*
    * Creates a thread to read from the server (run)
    *
    */
    @Override
    public void run() {
        String inLine;
        try{
            while((inLine = inStream.readLine()) != null){
                System.out.println(inLine);
            }
            closed = true;
        }catch(IOException e){System.out.println(e);}

}
    /*private void logIn(BufferedReader stdIn, PrintWriter out, BufferedReader in) throws IOException{
        String logInInput;
        String logInAns = in.readLine();
           while ((logInInput = stdIn.readLine()) != null && !logInInput.isEmpty()) {
                out.println(logInInput);
                
           }
    }*/
    /*
    * Which client you connect to
    *
    */
    private static String userNum() throws IOException{
        String num;
        keyIn = new BufferedReader(new InputStreamReader(System.in));
            num = keyIn.readLine();
        return num;
    }
}

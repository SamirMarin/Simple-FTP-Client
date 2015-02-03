import java.io.*;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by rohinpatel on 15-01-24.
 * UserCommands implemented by both Rohin Patel and Samir Marin
 */
public class UserCommands {

    private Socket dataSocket;
    private BufferedReader dataReader;
    private DataOutputStream dataWriter;

    public synchronized void openCmd(ArrayList<String> args) throws IOException {
        if (FTPPanel.getInstance().isOpen()) {
            System.out.println("Already connected to server, please quit before connecting to another server");
            return;
        }
        if (args.size() < 2) {
            System.out.println("801 Incorrect number of arguments");
            return;
        }

        if (!args.get(0).equalsIgnoreCase("open")) {
            return;
        }
        String hostName = args.get(1);

        int port=21;
        if(args.size() == 3){
          try {
               port = Integer.parseInt(args.get(2));
          }
          catch (Exception e) {
              System.out.println("Invalid port, defaulting to 21");
              port = 21;
          }
        }
        else {
            port = 21;
        }

        // setup socket
           if  (FTPPanel.getInstance().setupControlCxn(hostName, port)) {
            System.out.println("Connected to " + args.get(1));
               FTPPanel.getInstance().setOpen(true);
               System.out.println(FTPPanel.getInstance().readLine());
        }
        else {
               return;
           }

        return;
    }
    public synchronized void userCmd(ArrayList<String> args) {
        if (args.size() != 2) {
            System.out.println("Too few arguments");
            return;
        }
        if (!args.get(0).equalsIgnoreCase("user")) {
            return;
        }

        FTPPanel.getInstance().sendInput("USER " + args.get(1));

        return;
    }
    public synchronized void passCmd(ArrayList<String> args) {
        if (args.size() != 2) {
            System.out.println("Too few arguments");
            return;
        }
        if (!args.get(0).equalsIgnoreCase("pass")) {
            return;
        }
        FTPPanel.getInstance().sendInput("PASS " + args.get(1));
    }
    public synchronized void dirCmd() {
        FTPPanel.getInstance().sendInput("PASV");


    }
    public synchronized void createDataConnection(String response, String cmd) {
        int startIndex = response.indexOf("(") + 1;
        int endIndex = response.indexOf(")", startIndex+1);
        String responseIpPort = response.substring(startIndex, endIndex);
        String ip = getIpAdress(responseIpPort);
        int port = getPort(responseIpPort);
        System.out.println(port);
        try {
            dataSocket = new Socket(InetAddress.getByName(ip), port);
            dataReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
            dataWriter = new DataOutputStream(new BufferedOutputStream(dataSocket.getOutputStream()));
            FTPPanel.getInstance().sendInput(cmd);
            // need to create while loop;
            FTPPanel.getInstance().printOutput(dataReader.readLine() + "\n");
            dataSocket.close();
        } catch (Exception e) {
           FTPPanel.getInstance().printOutput(e.getMessage());
        }

    }

    private String getIpAdress(String message){
        StringTokenizer ipString = new StringTokenizer(message, ",");
        return ipString.nextToken() + "." + ipString.nextToken() + "." + ipString.nextToken() + "." + ipString.nextToken();

    }

    private int getPort(String message){
        StringTokenizer portString = new StringTokenizer(message, ",");
        for(int i = 0; i < 4; i++){
            portString.nextToken();
        }

        int hiOrderBit = Integer.parseInt(portString.nextToken());
        int lowOrderBit = Integer.parseInt(portString.nextToken());
        System.out.println(hiOrderBit + " " + lowOrderBit);
        int port = (hiOrderBit * 256) + lowOrderBit;
        return port;
    }


    public synchronized int closeCmd() {

        return 0;
    }
    public synchronized int quitCmd() {

        return 0;
    }


}

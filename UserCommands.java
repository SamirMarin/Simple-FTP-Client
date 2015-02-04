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
    private ByteArrayInputStream byteArrayInputStream;
    private InputStream inputfile;
    private File file;

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

        int port;
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
               FTPPanel.getInstance().readLine();
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
        String response = FTPPanel.getInstance().readLine();
        if (response.contains("331 ")) {
            System.out.print("Please enter a password: ");
            String input = FTPPanel.getInstance().readInput();
            ArrayList<String> args2 = FTPPanel.getInstance().parseInput("PASS " + input);
            passCmd(args2);
            FTPPanel.getInstance().readLine();
        }
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
        String response = FTPPanel.getInstance().readLine();
        if (!response.contains("227 ")) {
            FTPPanel.getInstance().printOutput("899 Processing Error");
            return;
        }
        createDataConnection(response, "LIST");
        FTPPanel.getInstance().readLine();

        try {
            dataReader.close();
            dataWriter.close();
            dataSocket.close();
        }
        catch (IOException e) {

        }
        FTPPanel.getInstance().readLine();
    }

    public synchronized void changeDicCmd(ArrayList<String> args){
        if(args.size() == 2) {
            String directory = args.get(1);
            FTPPanel.getInstance().sendInput("CWD " + directory);
            FTPPanel.getInstance().readLine();
        }
    }


    public synchronized void closeCmd() {
        try {
            FTPPanel.getInstance().sendInput("QUIT");
            FTPPanel.getInstance().readLine();
        }finally {
            FTPPanel.getInstance().setOpen(false);
            FTPPanel.getInstance().setStartProg(false);
        }
    }


    public synchronized void quitCmd() throws IOException {
        //need to fill in

    }
    public synchronized void putCmd(ArrayList<String> args){
        FTPPanel.getInstance().sendInput("PASV");
        String response = FTPPanel.getInstance().readLine();
        if (!response.contains("227 ")) {
            FTPPanel.getInstance().printOutput("899 Processing Error");
            return;
        }
        createDataConnection(response, "STOR", args.get(1));
        FTPPanel.getInstance().readLine();
        try {
            dataReader.close();
            dataWriter.close();
            dataSocket.close();
        }
        catch (IOException e) {
        }
        FTPPanel.getInstance().readLine();
    }
    private  synchronized void createDataConnection(String response, String cmd) {
        createDataConnection(response, cmd, "");
    }

    private synchronized void createDataConnection(String response, String cmd, String userIput) {
        if (userIput != "") {
            userIput = " " + userIput;
        }
        int startIndex = response.indexOf("(") + 1;
        int endIndex = response.indexOf(")", startIndex + 1);
        String responseIpPort = response.substring(startIndex, endIndex);
        String ip = getIpAddress(responseIpPort);
        int port = getPort(responseIpPort);
        System.out.println(port);
            try {
                dataSocket = new Socket(InetAddress.getByName(ip), port);
                dataWriter = new DataOutputStream(new BufferedOutputStream(dataSocket.getOutputStream()));
                if(cmd == "LIST") {
                    dataReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
                    FTPPanel.getInstance().sendInput(cmd + userIput);
                    String output;
                    while ((output = dataReader.readLine()) != null) {
                        FTPPanel.getInstance().printOutput(output);
                    }
                }
                else if(cmd == "STOR"){
                    //byteArrayInputStream = new ByteArrayInputStream(buffer);
                    File fileRead = openFile(userIput.trim());
                    inputfile = new FileInputStream(fileRead);
                    long size = fileRead.length();
                    byte[] buffer = new byte[size];
                    BufferedInputStream input = new BufferedInputStream(fileRead);
                    FTPPanel.getInstance().sendInput(cmd + userIput);
                    FTPPanel.getInstance().readLine();
                    int bytesRead = 0;

                    while((bytesRead = input.read(buffer)) != -1){
                       dataWriter.write(buffer, 0, bytesRead);

                    }
                }


            } catch (Exception e) {
                FTPPanel.getInstance().printOutput(e.getMessage());
            }


    }

    private String getIpAddress(String message){
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

    private File openFile(String fileToPass){
        file = new File(fileToPass);
        return file;
    }

        //
    }







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
    private InputStream file;

    public synchronized void openCmd(ArrayList<String> args) throws IOException {
        if (FTPPanel.getInstance().isOpen()) {
            System.out.println("Already connected to server, please quit before connecting to another server");
            return;
        }
        if (args.size() < 2) {
            FTPPanel.getInstance().printOutput("801 Incorrect number of arguments.");
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
            System.out.println("801 Incorrect number of arguments.");
            return;
        }
        if (!args.get(0).equalsIgnoreCase("user")) {
            return;
        }
        String user = FTPPanel.getInstance().concatWithSpaces(args);

        FTPPanel.getInstance().sendInput("USER " + user);
        String response = FTPPanel.getInstance().readLine();
        if (response.contains("331 ")) {
            System.out.print("Please enter a password: ");
            String input = FTPPanel.getInstance().readInput();
            ArrayList<String> args2 = FTPPanel.getInstance().parseInput("PASS " + input);
            passCmd(args2);
            FTPPanel.getInstance().readLine();
        }
        return;
    }

    public synchronized void passCmd(ArrayList<String> args) {
        if (args.size() != 2) {
            FTPPanel.getInstance().printOutput("801 Incorrect number of arguments.");
            return;
        }
        if (!args.get(0).equalsIgnoreCase("pass")) {
            return;
        }
        FTPPanel.getInstance().sendInput("PASS " + args.get(1));
    }

    public synchronized void dirCmd(ArrayList<String> args) {
        if (args.size() != 1) {
            FTPPanel.getInstance().printOutput("801 Incorrect number of arguments.");
            return;
        }
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
        if (args.size() != 2) {
            FTPPanel.getInstance().printOutput("801 Incorrect number of arguments.");
            return;
        }
            String directory = FTPPanel.getInstance().concatWithSpaces(args);
            FTPPanel.getInstance().sendInput("CWD " + directory);
            FTPPanel.getInstance().readLine();
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
        closeCmd();
        System.exit(0);

    }

    public synchronized void putCmd(ArrayList<String> args){
        if (args.size() < 2) {
            FTPPanel.getInstance().printOutput("801 Incorrect number of arguments.");
            return;
        }
        FTPPanel.getInstance().sendInput("PASV");
        String response = FTPPanel.getInstance().readLine();
        if (!response.contains("227 ")) {
            FTPPanel.getInstance().printOutput("899 Processing Error");
            return;
        }
        String path = FTPPanel.getInstance().concatWithSpaces(args);
        createDataConnection(response, "STOR", path);
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

    private synchronized void createDataConnection(String response, String cmd, String userInput) {
        if (userInput != "") {
            userInput = " " + userInput;
        }
        int startIndex = response.indexOf("(") + 1;
        int endIndex = response.indexOf(")", startIndex + 1);
        String responseIpPort = response.substring(startIndex, endIndex);
        String ip = getIpAddress(responseIpPort);
        int port = getPort(responseIpPort);
        System.out.println(port);
        long start = System.currentTimeMillis();
        long end = start + 30 * 1000; // calculate 30 seconds from system time
        while (System.currentTimeMillis() < end) {  // try for 30 seconds
            try {
                dataSocket = new Socket(InetAddress.getByName(ip), port);
                break;
            } catch (Exception e) {
                FTPPanel.getInstance().printOutput(e.getMessage());
            }
        }
            try {
                dataWriter = new DataOutputStream(new BufferedOutputStream(dataSocket.getOutputStream()));
                if (cmd.equals("LIST")) {
                    dataReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
                    FTPPanel.getInstance().sendInput(cmd + userInput);
                    String output;
                    while ((output = dataReader.readLine()) != null) {
                        FTPPanel.getInstance().printOutput(output);
                    }
                } else if (cmd.equals("STOR")) {
                    byte[] buffer = new byte[4096];
                    //byteArrayInputStream = new ByteArrayInputStream(buffer);
                    InputStream fileRead = openFile(userInput.trim());
                    BufferedInputStream input = new BufferedInputStream(fileRead);
                    FTPPanel.getInstance().sendInput(cmd + userInput);
                    FTPPanel.getInstance().readLine();
                    int bytesRead = 0;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        dataWriter.write(buffer, 0, bytesRead);

                    }
                }
            } catch (Exception e) {

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

    private InputStream openFile(String fileToPass){
        try {
             file = new FileInputStream(fileToPass);
        } catch (FileNotFoundException e) {
            System.out.println("810 Access to local file XXX denied");
        }
        return file;
    }

        //
    }







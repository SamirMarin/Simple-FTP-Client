import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import sun.misc.IOUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.zip.InflaterInputStream;

/**
 * Created by rohinpatel on 15-01-24.
 * UserCommands implemented by both Rohin Patel and Samir Marin
 */
public class UserCommands {

    private Socket dataSocket;
    private BufferedReader dataReader;
    private DataOutputStream dataWriter;
    private InputStream file;
    private BufferedInputStream input;
    private DataInputStream dataReaderRetr;

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

    public synchronized void userCmd(ArrayList<String> args) throws IOException{
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
            if (input == null){
                return;
            }
            ArrayList<String> args2 = FTPPanel.getInstance().parseInput("PASS " + input);
            passCmd(args2);
            FTPPanel.getInstance().readLine();
        }
        return;
    }

    public synchronized void passCmd(ArrayList<String> args)throws IOException {
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
        if (args.size() < 1) {
            FTPPanel.getInstance().printOutput("801 Incorrect number of arguments.");
            return;
        }
        FTPPanel.getInstance().sendInput("TYPE I");
        FTPPanel.getInstance().readLine();
        FTPPanel.getInstance().sendInput("PASV");
        String response = FTPPanel.getInstance().readLine();
        if (!response.contains("227 ")) {
            FTPPanel.getInstance().printOutput("899 Processing Error");
            return;
        }
        String path = FTPPanel.getInstance().concatWithSpaces(args);
        createDataConnection(response);
        try {
            dataWriter = new DataOutputStream(new BufferedOutputStream(dataSocket.getOutputStream()));
            dataReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
            FTPPanel.getInstance().sendInput("LIST");
            String output;
            while ((output = dataReader.readLine()) != null) {
                FTPPanel.getInstance().printOutput(output);
            }
            FTPPanel.getInstance().readLine();
            dataReader.close();
            dataWriter.close();
            dataSocket.close();
        } catch (IOException e) {
        }

        FTPPanel.getInstance().readLine();
    }

    public synchronized void changeDicCmd(ArrayList<String> args)throws IOException{
        if (args.size() != 2) {
            FTPPanel.getInstance().printOutput("801 Incorrect number of arguments.");
            return;
        }
            String directory = FTPPanel.getInstance().concatWithSpaces(args);
            FTPPanel.getInstance().sendInput("CWD " + directory);
            FTPPanel.getInstance().readLine();
    }


    public synchronized void closeCmd()throws IOException {
        try {
            FTPPanel.getInstance().sendInput("QUIT");
            FTPPanel.getInstance().readLine();
            FTPPanel.getInstance().getServerIn().close();
            FTPPanel.getInstance().getServerOut().close();
            FTPPanel.getInstance().getControlCxn().close();
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
        FTPPanel.getInstance().sendInput("TYPE I");
        FTPPanel.getInstance().readLine();
        FTPPanel.getInstance().sendInput("PASV");
        String response = FTPPanel.getInstance().readLine();
        if (!response.contains("227 ")) {
            FTPPanel.getInstance().printOutput("899 Processing Error");
            return;
        }
        String path = FTPPanel.getInstance().concatWithSpaces(args);
        createDataConnection(response);
        try {
        byte[] buffer = new byte[64000];
        //byteArrayInputStream = new ByteArrayInputStream(buffer);
        InputStream file = openFile(path.trim());
        if (file == null) {
            return;
        }
        input = new BufferedInputStream(file);
        FTPPanel.getInstance().sendInput("STOR " + path);
        FTPPanel.getInstance().readLine();
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            dataWriter.write(buffer, 0, bytesRead);
        }
        FTPPanel.getInstance().readLine();
            input.close();
            file.close();
            dataWriter.close();
            dataSocket.close();
        }
        catch (IOException e) {
        }
        FTPPanel.getInstance().readLine();
    }

    public synchronized  void getCmd(ArrayList<String> args){
        if(args.size() < 2){
            FTPPanel.getInstance().printOutput("801 Incorrect number of arguments.");

        }
        FTPPanel.getInstance().sendInput("TYPE I");
        FTPPanel.getInstance().readLine();
        FTPPanel.getInstance().sendInput("PASV");
        String response = FTPPanel.getInstance().readLine();
        if (!response.contains("227 ")) {
            FTPPanel.getInstance().printOutput("899 Processing Error");
            return;
        }
        String path = FTPPanel.getInstance().concatWithSpaces(args);
         createDataConnection(response);

        FTPPanel.getInstance().sendInput("RETR " + path);
        String retrResp = FTPPanel.getInstance().readLine();
        if (retrResp.contains("550")) {
           return;
        }
        try {
        dataReaderRetr = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
        FileOutputStream outputFile= new FileOutputStream(path); // need to change
        byte[] buffer = new byte[64000];
        int bytesRead;
        while((bytesRead = dataReaderRetr.read(buffer)) != -1) {
            outputFile.write(buffer, 0, bytesRead);
        }
        FTPPanel.getInstance().readLine();
            input.close();
            file.close();
            dataWriter.close();
            dataSocket.close();
        }
        catch (IOException e) {
        }
    }




    private synchronized void createDataConnection(String response) {
        int startIndex = response.indexOf("(") + 1;
        int endIndex = response.indexOf(")", startIndex + 1);
        String responseIpPort = response.substring(startIndex, endIndex);
        String ip = getIpAddress(responseIpPort);
        int port = getPort(responseIpPort);
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

        return;
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
        int port = (hiOrderBit * 256) + lowOrderBit;
        return port;
    }

    private InputStream openFile(String fileToPass){
        try {
            file = new FileInputStream(fileToPass);
        } catch (FileNotFoundException e) {
            FTPPanel.getInstance().printOutput("810 Access to local file " + fileToPass  + " denied.");
        }
        return file;
    }


        //
    }







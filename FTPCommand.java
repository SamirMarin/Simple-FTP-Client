import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Created by samirmarin on 15-01-26.
 */
public class FTPCommand {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private boolean loggedIn;

    private boolean isOpen;
    int MAX_LEN = 255;
    Socket dataSocket;
    byte cmdString[] = new byte[MAX_LEN];
    /*
    * add instance of FTP Command
    * */
    public FTPCommand(){

    }
    public synchronized String readUserInput() {
        System.out.print("csftp> ");
        Arrays.fill(cmdString, (byte) 0);
        String cmd;
        try {
            System.in.read(cmdString);
            cmd = new String(cmdString, "UTF-8");
            return cmd;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "800 try again";
    }

    /**
     * Connects to the default port of an FTP server and logs in as
     * anonymous/anonymous.
     */
    public synchronized void open(String host) throws IOException {
        open(host, 21);
    }

    public synchronized void open(String host, int port) throws IOException {
        if(isOpen){
            System.out.println("already connected to server. Please disconnect.");
        }
        else{
            isOpen = true;
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String response = readLine();
            System.out.println(response);
            if (response.contains("220 ")) {
                System.out.println("Enter a user name");
                String cmd = readUserInput();
                user(cmd);

            }

        }
    }

    public synchronized void user(String userName) throws IOException {
        sendLine("USER " + userName);
        String response = readLine();
        System.out.println(response);
        if(response.contains("331 ")){
            //  open up standard input
            System.out.println("Please enter a password");

            String password;
            boolean isRead=true;
            //  read the username from the command-line; need to use try/catch with the
            //  readLine() method
            while(isRead) {
                password = readUserInput();
                passCmd(password);
                isRead=false;
            }

        }

    }
    public synchronized void passCmd(String password) throws IOException {

        sendLine("PASS " + password);
        writeOutput(readLine());
        return;

    }
    public synchronized void quit() throws IOException {
        try {
            sendLine("QUIT");
            isOpen = false;
        }finally {
           socket = null;
        }

    }

    public synchronized void close() throws IOException {
        try {
            sendLine("QUIT");
            writeOutput(readLine());
            socket.close();
            reader.close();
            writer.close();
            isOpen = false;
        }finally {
            socket = null;
        }

    }
    public synchronized void changeDicCmd(String directory) throws IOException {
        /*//get working directory of server.
        String workingDir = null;
        sendLine("PWD");
        String response = readLine();
        if(response.startsWith("257 ")){
            int indexStart = response.indexOf('\"');
            int indexEnd = response.indexOf('\"', indexStart+1) +1;
            if ((indexStart != -1) &&(indexEnd != -1)){
                workingDir = response.substring(indexStart, indexEnd);
            }
        }*/
        sendLine("CWD " + directory);
        String dirResponse = readLine();
        System.out.println(dirResponse);
    }

    public synchronized void dirCmd() throws IOException {
        String response = pasvCmd();
        if (response == null) {
            writeOutput("Failed to issue command");
            return;
        }
        createDataConnection(response, "LIST");
        sendLine("LIST");
        writeOutput(readLine());

    }
    private synchronized String pasvCmd() throws IOException{
        sendLine("PASV");
        String response = readLine();
        writeOutput(response);
        if (!response.contains("227")) {
            System.out.println("Invalid command");
            System.out.println(response);
            return null;
        }
        return response;
    }

    public synchronized String readLine() throws IOException {
        String line = reader.readLine();

        return "--> " + line;
    }

    private synchronized void sendLine(String message) throws IOException {
        if(socket == null){
            //need to do something means not connected
        }
        else{
            try {
                writer.write(message + "\n");
                writer.flush();
                writeOutput("<-- " + message);
            } catch (IOException e) {
                socket= null;
                throw e;
            }
        }

    }
    public void writeOutput(String message) {
        System.out.println(message);

    }

    private String getIpAdress(String message){
        StringTokenizer ipString = new StringTokenizer(message, ",");
        return ipString.nextToken() + "." + ipString.nextToken() + "." + ipString.nextToken() + "." + ipString.nextToken();

    }
    private synchronized void createDataConnection(String response, String cmd) throws IOException{
        System.out.println(response);
        int startIndex = response.indexOf("(") + 1;
        int endIndex = response.indexOf(")", startIndex+1);
        String responseIpPort = response.substring(startIndex, endIndex);
        String ip = getIpAdress(responseIpPort);
        int port = getPort(responseIpPort);
        System.out.println(port);
        try {
            dataSocket = new Socket(InetAddress.getByName(ip), port);
            BufferedReader datareader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
            DataOutputStream datawriter = new DataOutputStream(new BufferedOutputStream(dataSocket.getOutputStream()));
            sendLine(cmd);
            writeOutput(datareader.readLine());
        } catch (Exception e) {
           writeOutput(e.getMessage());
        }
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

    public boolean isOpen() {
        return isOpen;
    }

}

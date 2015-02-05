import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by rohinpatel on 15-01-24.
 * UserCommands implemented by both Rohin Patel and Samir Marin
 *
 * contains all FTP connection methods and helpers
 */
public class UserCommands {

    private Socket dataSocket;
    private BufferedReader dataReader;
    private DataOutputStream dataWriter;
    private InputStream file;
    private BufferedInputStream input;
    private DataInputStream dataReaderRetr;
    /**
    * connect to FTP server on giver specified port
    * if no port is specified connects to port 21
    */
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
              System.out.println("802 Invalid argument. Defaulting to port 21");
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
               if (isTimeOut(FTPPanel.getInstance().readLine())){
                   return;
               }
        }
        else {
               return;
           }

    }
    /**
     * read the user name to log in to FTP server
     * and ask for password to be specified by user if required by server
     * */
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
        if (isTimeOut(response)) {
            return;
        }
        if (response.contains("331 ")) {
            System.out.print("Please enter a password: ");
            String input = FTPPanel.getInstance().readInput();
            if (input == null){
                return;
            }
            ArrayList<String> args2 = FTPPanel.getInstance().parseInput("PASS " + input);
            passCmd(args2);
        }
        else if (response.contains("230 ")) {
            FTPPanel.getInstance().setLoggedIn(true);
        }
    }
    /**
     * reads the user password when require by FTP server
     * */
    public synchronized void passCmd(ArrayList<String> args)throws IOException {
        if (args.size() != 2) {
            FTPPanel.getInstance().printOutput("801 Incorrect number of arguments.");
            return;
        }
        if (!args.get(0).equalsIgnoreCase("pass")) {
            return;
        }
        FTPPanel.getInstance().sendInput("PASS " + args.get(1));

        String response = FTPPanel.getInstance().readLine();
        if (isTimeOut(response)) {
            return;
        }
        if (response.contains("230 ")) {
            FTPPanel.getInstance().setLoggedIn(true);
        }
    }
    /**
     * provides a list of directories in server once logged in
     * */
    public synchronized void dirCmd(ArrayList<String> args) throws IOException {
        if (!FTPPanel.getInstance().isLoggedIn()) {
            FTPPanel.getInstance().printOutput("803 Supplied command not expected at this time");
            return;
        }
        if (args.size() < 1) {
            FTPPanel.getInstance().printOutput("801 Incorrect number of arguments.");
            return;
        }
        FTPPanel.getInstance().sendInput("TYPE I");
        FTPPanel.getInstance().readLine();
        FTPPanel.getInstance().sendInput("PASV");
        String response = FTPPanel.getInstance().readLine();
        if (isTimeOut(response)) {
            return;
        }
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
            isTimeOut(FTPPanel.getInstance().readLine());
            dataReader.close();
            dataWriter.close();
            dataSocket.close();
        } catch (IOException e) {
        }

        isTimeOut(FTPPanel.getInstance().readLine());
    }
    /**
     * changes the current working directory on server
     * */
    public synchronized void changeDicCmd(ArrayList<String> args)throws IOException{
        if (!FTPPanel.getInstance().isLoggedIn()) {
            FTPPanel.getInstance().printOutput("803 Supplied command not expected at this time");
            return;
        }
        if (args.size() != 2) {
            FTPPanel.getInstance().printOutput("801 Incorrect number of arguments.");
            return;
        }
            String directory = FTPPanel.getInstance().concatWithSpaces(args);
            FTPPanel.getInstance().sendInput("CWD " + directory);
            if (isTimeOut(FTPPanel.getInstance().readLine())) {
                return;
            }
    }
    /**
     * closes the established FTP server connection
     * where the next command expected by the FTP client is an open
     * */
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
            FTPPanel.getInstance().setLoggedIn(false);
        }
    }
    /**
     * closes any establish connection and
     * closes the FTP client program
     * */
    public synchronized void quitCmd() throws IOException {
        closeCmd();
        System.exit(0);

    }
    /**
     * establishes data connection with server
     * sends a file specified by the user to the server
     * the file is saved with the same name on the remote machine
     * */
    public synchronized void putCmd(ArrayList<String> args) throws IOException{
        if (!FTPPanel.getInstance().isLoggedIn()) {
            FTPPanel.getInstance().printOutput("803 Supplied command not expected at this time");
            return;
        }
        if (args.size() < 2) {
            FTPPanel.getInstance().printOutput("801 Incorrect number of arguments.");
            return;
        }
        FTPPanel.getInstance().sendInput("TYPE I");
        FTPPanel.getInstance().readLine();
        FTPPanel.getInstance().sendInput("PASV");
        String response = FTPPanel.getInstance().readLine();
        if (isTimeOut(response)) {
            return;
        }
        if (!response.contains("227 ")) {
            FTPPanel.getInstance().printOutput("899 Processing Error");
            return;
        }
        String path = FTPPanel.getInstance().concatWithSpaces(args);
        createDataConnection(response);
        try {
        dataWriter = new DataOutputStream(new BufferedOutputStream(dataSocket.getOutputStream()));
        byte[] buffer = new byte[64000];
        //byteArrayInputStream = new ByteArrayInputStream(buffer);
        InputStream file = openFile(path.trim());
        if (file == null) {
            return;
        }
        input = new BufferedInputStream(file);
        FTPPanel.getInstance().sendInput("STOR " + path);
        String storResp = FTPPanel.getInstance().readLine();
            if (isTimeOut(storResp)) {
                return;
            }
            if (storResp.contains("550")) {
                return;
            }
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            dataWriter.write(buffer, 0, bytesRead);
        }
        isTimeOut(FTPPanel.getInstance().readLine());
            input.close();
            file.close();
            dataWriter.close();
            dataSocket.close();
        }
        catch (IOException e) {
        }
        isTimeOut(FTPPanel.getInstance().readLine());
    }
    /**
     * establishes a data connection with the server
     * retrieves file on remote machine specified by user
     * saves file on local machine with the same name
     * */
    public synchronized  void getCmd(ArrayList<String> args) throws IOException{
        if (!FTPPanel.getInstance().isLoggedIn()) {
            FTPPanel.getInstance().printOutput("803 Supplied command not expected at this time");
            return;
        }
        if(args.size() < 2){
            FTPPanel.getInstance().printOutput("801 Incorrect number of arguments.");

        }
        FTPPanel.getInstance().sendInput("TYPE I");
        if (isTimeOut(FTPPanel.getInstance().readLine())) {
            return;
        }
        FTPPanel.getInstance().sendInput("PASV");
        String response = FTPPanel.getInstance().readLine();
        if (isTimeOut(response)) {
            return;
        }
        if (!response.contains("227 ")) {
            FTPPanel.getInstance().printOutput("899 Processing Error");
            return;
        }
        String path = FTPPanel.getInstance().concatWithSpaces(args);
         createDataConnection(response);
        FTPPanel.getInstance().sendInput("RETR " + path);
        String retrResp = FTPPanel.getInstance().readLine();
        if (isTimeOut(retrResp)) {
            return;
        }
        if (retrResp.contains("550")) {
           return;
        }
        try {
        dataWriter = new DataOutputStream(new BufferedOutputStream(dataSocket.getOutputStream()));
        dataReaderRetr = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
        FileOutputStream outputFile= new FileOutputStream(path); // need to change
        byte[] buffer = new byte[64000];
        int bytesRead;
        while((bytesRead = dataReaderRetr.read(buffer)) != -1) {
            outputFile.write(buffer, 0, bytesRead);
        }
            isTimeOut(FTPPanel.getInstance().readLine());
            input.close();
            file.close();
            dataWriter.close();
            dataSocket.close();
        }
        catch (IOException e) {
        }
    }
    /**
     * helper method used to create a data connection
     * for the putCmd, dirCmd and UserCmd methods
     * */
    private synchronized void createDataConnection(String response) throws IOException {
        int startIndex = response.indexOf("(") + 1;
        int endIndex = response.indexOf(")", startIndex + 1);
        String responseIpPort = response.substring(startIndex, endIndex);
        String ip = getIpAddress(responseIpPort);
        int port = getPort(responseIpPort);
            try {
                dataSocket = new Socket();
                dataSocket.connect(new InetSocketAddress(ip, port), 30000);
            } catch (IOException e) {
                throw new IOException("820 Control Connection to " + ip + " on port " + port + " failed to open");
            }
        }


    /**
     * helper method used to parse the IP address
     * for a data connection specified by remote machine
     * */
    private String getIpAddress(String message){
        StringTokenizer ipString = new StringTokenizer(message, ",");
        return ipString.nextToken() + "." + ipString.nextToken() + "." + ipString.nextToken() + "." + ipString.nextToken();

    }
    /**
     * helper method used to parse the Port number
     * for a data connection specified by remote machine
     * */
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

    /**
     *
     * @param reply - reply string from server
     * @return closes conneciton and returns true if message has "421 timeout" else returns false
     */
    public synchronized boolean isTimeOut(String reply) {
        if (reply.contains("421 ")) {
            try {
                closeCmd();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * helper method used by the put method
     * to open local file as a file input stream
     * for sending to remote machine
     * */
    private InputStream openFile(String fileToPass){
        try {
            file = new FileInputStream(fileToPass);
        } catch (FileNotFoundException e) {
            FTPPanel.getInstance().printOutput("810 Access to local file " + fileToPass  + " denied.");
        }
        return file;
    }
    }







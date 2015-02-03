import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Created by samirmarin on 15-01-26.
 */
public class FTPCommand {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    Socket dataSocket;
    /*
    * add instance of FTP Command
    * */
    public FTPCommand(){

    }

    /**
     * Connects to the default port of an FTP server and logs in as
     * anonymous/anonymous.
     */
    public synchronized void open(String host) throws IOException {
        open(host, 21);
    }

    public synchronized void open(String host, int port) throws IOException {
        if(socket != null){
            System.out.println("already connected to server. Please disconnect.");
        }
        else{
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String response = readLine();
            System.out.println(response);

        }
    }

    public synchronized void user(String userName) throws IOException {
        sendLine("USER " + userName);
        String response = readLine();
        System.out.println(response);
        if(response.startsWith("331 ")){
            //  open up standard input
            System.out.print("csftp> ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            String password = null;
            boolean notRead = true;

            //  read the username from the command-line; need to use try/catch with the
            //  readLine() method
            while(notRead) {
                try {
                    password = br.readLine();
                    notRead = false;
                } catch (IOException ioe) {
                    System.out.println("IO error trying to read your password try again!");
                }
            }
            sendLine("PASS " + password);
            response = readLine();
            System.out.println(response);

        }

    }
    public synchronized void quit() throws IOException {
        try {
            sendLine("QUIT");
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
        sendLine("PASV");
        String response = readLine();
        if (!response.contains("227")) {
            System.out.println("Invalid command");
            System.out.println(response);
            return;
        }
        System.out.println(response);
        int startIndex = response.indexOf("(") + 1;
        int endIndex = response.indexOf(")", startIndex+1);
        String responseIpPort = response.substring(startIndex, endIndex);
        String ip = getIpAdress(responseIpPort);
        int port = getPort(responseIpPort);
        System.out.println(port);
        dataSocket =  new Socket(InetAddress.getByName(ip), port);
        BufferedReader datareader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
        //DataOutputStream datawriter = new DataOutputStream(new BufferedOutputStream(dataSocket.getOutputStream()));
        sendLine("LIST");
        System.out.println(datareader.readLine());
        readLine();
        datareader.close();
        readLine();
        //System.out.println(response);


    }

    private synchronized String readLine() throws IOException {
        String line = reader.readLine();
        System.out.println("--> " + line);
        return line;
    }

    private synchronized void sendLine(String message) throws IOException {
        if(socket == null){
            //need to do something means not connected
        }
        else{
            try {
                writer.write(message+ "\r\n");
                writer.flush();
                System.out.println("<-- " + message);
            } catch (IOException e) {
                socket= null;
                throw e;
            }
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


}

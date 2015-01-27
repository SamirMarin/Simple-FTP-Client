import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by samirmarin on 15-01-26.
 */
public class FTPCommand {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
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
    private synchronized String readLine() throws IOException {
        String line = reader.readLine();
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
            } catch (IOException e) {
                socket= null;
                throw e;
            }
        }

    }


}

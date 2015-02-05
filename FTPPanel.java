
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Rohin Patel and Samir Marin on 15-01-26.
 */
public class FTPPanel {

    int MAX_LEN = 255;


    private UserCommands uc = new UserCommands();
    private static Socket controlCxn;
    private static BufferedWriter serverOut;
    private static BufferedReader serverIn;
    private static FTPPanel ftp;
    private byte cmdString[] = new byte[MAX_LEN];
    private String prompt = "csftp> ";
    int len;
    private boolean isOpen = false;
    private boolean isLoggedIn = false;
    private boolean startProg = false;

    private FTPPanel () {
    }

    /**
     * Singleton method that returns only instance of FTPPanel
     * @return FTPPanel instance
     */
    public synchronized static FTPPanel getInstance() {
       if (ftp == null)  {
           ftp = new FTPPanel();
       }
        return ftp;}

    /**
     * Parses input from user into ArrayList to be checked by handleCommand
     * @param cmd - String returned from System.in
     * @return
     */
    public synchronized ArrayList<String> parseInput(String cmd) {
        ArrayList<String> args = new ArrayList<String>();
        int firstindex = 0;
        for (int i=0; i < cmd.length(); i++) {
            if ((cmd.charAt(i) == ' ') || (cmd.charAt(i) == '\n')) {
                if(!((cmd.substring(firstindex, i).equals(" ")) || (cmd.substring(firstindex, i).equals("")))){
                    args.add(cmd.substring(firstindex,i));
                }
                i = ++i;
                firstindex = i;
            }
        }
        if(args.size() == 0 || args.get(0).trim().substring(0, 1).equals("#")){
            args.clear();
            args.add(0, "#");
        }

        return args;

    }

    /**
     * Concatenates an array of parsed arguments with spaces since ParseInput removes all spaces
     * @param args ArrayList of parsed commands
     * @return
     */
    public synchronized String concatWithSpaces(ArrayList<String> args) {
        String line = "";
        for (int i = 1; i < args.size(); i++) {
            line += args.get(i) + " ";

        }
        return line.trim();
    }

    /**
     * Handles parsed commands and executes appropriate function
     * @param args - arraylist of arguments created by the parser
     * @throws IOException
     */
    public synchronized void handleCommand(ArrayList<String> args) throws IOException {
        try {
            CommandStrings val = CommandStrings.valueOf(args.get(0).toUpperCase());
            switch (val) {
                case OPEN:
                    uc.openCmd(args);
                    break;
                case USER:
                    uc.userCmd(args);
                    break;
                case CLOSE:
                    uc.closeCmd();
                    break;
                case QUIT:
                    uc.quitCmd();
                    break;
                case GET:
                    uc.getCmd(args);
                    break;
                case PUT:
                    uc.putCmd(args);
                    break;
                case CD:
                    uc.changeDicCmd(args);
                    break;
                case DIR:
                    uc.dirCmd(args);
                    break;

            }
        } catch(IllegalArgumentException e){
            System.out.println("800 Invalid Command");
            return;
        }

    }

    /**
     *
     * @param hostname - host to connect to
     * @param port - optional port to connect on
     * @return true if successful
     * @throws IOException
     */
    public synchronized boolean setupControlCxn(String hostname, int port) throws IOException{
        try {
            controlCxn = new Socket();
            controlCxn.connect(new InetSocketAddress(hostname, port), 30000);
            serverIn = new BufferedReader(new InputStreamReader(controlCxn.getInputStream()));
            serverOut = new BufferedWriter(new OutputStreamWriter(controlCxn.getOutputStream()));
            return true;
        }
        catch (IOException e) {
            throw new IOException("820 Control Connection to " + hostname + " on port " + port + " failed to open");
            //printOutput("820 Control Connection to " + hostname + " on port " + port + " failed to open");
        }

    }

    /**
     * Sends command to server
     * @param cmd - command to be sent
     */
    public synchronized void sendInput(String cmd) {
        try {
            printOutput("--> " + cmd);
            serverOut.write(cmd + "\r\n");
            serverOut.flush();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());

        }
    }

    /**
     * Reads lines from server. Checks until code appears with space (indicating last line of server message)
     * @return a String reply of message from server
     */
    public synchronized String readLine() {
        String line = null;
        String code = null;
        StringBuffer buf = new StringBuffer();
        try {
            do {
                line = serverIn.readLine();
                if (code == null)
                    code = line.substring(0, 3);
                printOutput("<-- " + line);
                buf.append(line);
            }
            while(!(line.startsWith(code) && line.charAt(3) == ' '));
            return buf.toString();
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Prints the "csftp> " prompt
     */
    public synchronized void printPrompt() {
            System.out.print(prompt);
    }

    /**
     * Main loop for entire application. Outside loop checks if first command is open.
     * If it is open AND a valid connection is made then the rest of the client functions will be made available
     * Else it will continue to wait for a valid connection
     */
    public void run() {
        while (true) {
            printPrompt();
            String firstInput = readInput();
            ArrayList<String> args = parseInput(firstInput);
            if (args.get(0).equalsIgnoreCase("open")) {
                try {
                    handleCommand(args);
                } catch (IOException e) {
                    printOutput(e.getMessage());
                }
                if (isOpen()) {
                    startProg = true;
                }
            }
            else if(args.get(0).trim().equals("#"))
                continue;
            else {
                printOutput("803 Supplied command not expected at this time.");
                continue;
            }
            while (startProg) {
                synchronized (this) {
                    printPrompt();
                    String cmd = readInput();
                    args = parseInput(cmd);
                    if(args.get(0).trim().equals("#")){
                        continue;
                    }
                    try {
                        handleCommand(args);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
    }
    }

    /**
     * Reads input from System.in
     * @return Returns a string to be handled by the parseInput function
     */
    public String readInput() {
        String cmd;
        Arrays.fill(cmdString, (byte) 0);
        try {
            len = System.in.read(cmdString);

            cmd = new String(cmdString, "UTF-8");
            return cmd;
        } catch (IOException e) {
            printOutput("898 Input error while reading commands, terminating");
            System.exit(-1);
        }
        return null;
    }

    /**
     * Used by all functions to print output. (Easier just to call this function)
     * @param output
     */
    public synchronized void printOutput(String output) {
        System.out.println(output);
    }
    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public void setStartProg(boolean startProg) {
        this.startProg = startProg;
    }

    public static Socket getControlCxn() {
        return controlCxn;
    }

    public static BufferedReader getServerIn() {
        return serverIn;
    }

    public static BufferedWriter getServerOut() {
        return serverOut;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public enum CommandStrings {
        OPEN, USER, CLOSE, QUIT, GET, PUT, CD, DIR

    }

}

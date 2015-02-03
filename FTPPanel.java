
import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by rohinpatel on 15-01-26.
 */
public class FTPPanel{

    int MAX_LEN = 255;


    private UserCommands uc = new UserCommands();
    private ServerMessages sm;
    private static Socket datacxn;
    private static Socket controlCxn;
    private static BufferedWriter serverOut;
    private static BufferedReader serverIn;
    private static FTPPanel ftp;
    private byte cmdString[] = new byte[MAX_LEN];
    private String prompt = "csftp> ";
    int len;
    private boolean isOpen = false;
    private boolean startProg = false;
    private volatile boolean running = true;

    private FTPPanel () {
    }

    public synchronized static FTPPanel getInstance() {
       if (ftp == null)  {
           ftp = new FTPPanel();
       }
        return ftp;}

    public synchronized ArrayList<String> parseInput(String cmd) {
        ArrayList<String> args = new ArrayList<String>();
        int firstindex = 0;
        for (int i=0; i < cmd.length(); i++) {
            if ((cmd.charAt(i) == ' ') || (cmd.charAt(i) == '\n')) {
                args.add(cmd.substring(firstindex,i));
                i = ++i;
                firstindex = i;
            }


        }
        return args;

    }

    public synchronized void handleCommand(ArrayList<String> args) throws IOException {
        try {
            switch (CommandStrings.valueOf(args.get(0).toUpperCase())) {
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
                case PASS:
                    uc.passCmd(args);
                    break;
                case GET:
                    break;
                case PUT:
                    break;
                case CD:
                    uc.changeDicCmd(args);
                    break;
                case DIR:
                    uc.dirCmd();
                    break;

            }
        } catch(Exception e){
            System.out.println("800 Invalid Command");
            return;
        }

        return;

    }

    public synchronized boolean setupControlCxn(String hostname, int port) throws IOException{
        try {
            controlCxn = new Socket(hostname, port);
        }
        catch (IOException e) {
            printOutput("820 Control Connection to " + hostname + " on port " + port + " failed to open");
        }

            serverIn = new BufferedReader(new InputStreamReader(controlCxn.getInputStream()));
            serverOut = new BufferedWriter(new OutputStreamWriter(controlCxn.getOutputStream()));
            return true;

    }

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
    public synchronized String readLine() {
        String line = null;
        try {
//            ArrayList<String> responses = new ArrayList<String>();
//            int i = 0;
            line = serverIn.readLine();
            printOutput("<-- " + line);
//                    responses.add(i, line);
//                    i++;
//            }
//            String listString = "";
//            for (String s: responses) {
//                    listString += s + "\n";
//            }
//            return listString;
//
//            }
            return line;
        }
        catch (IOException e) {
            System.out.println(e.getMessage());

        }
        return null;
    }
    public synchronized void printPrompt() {
            System.out.print(prompt);
    }


    public synchronized void run() {
        while (true) {
            printPrompt();
            String firstInput = readInput();
            ArrayList<String> args = parseInput(firstInput);
            if (args.get(0).equalsIgnoreCase("open")) {
                try {
                    handleCommand(args);
                } catch (Exception e) {
                    printOutput(e.getMessage());
                }
                if (isOpen()) {
                    startProg = true;
                }
            }
            else {
                printOutput("803 Supplied command not expected at this time.");
                continue;
            }
            while (startProg) {
                synchronized (this) {
                    printPrompt();
                    String cmd = readInput();
                    args = parseInput(cmd);
                    try {
                        handleCommand(args);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
    }
    }
    public String readInput() {
        String cmd;
        Arrays.fill(cmdString, (byte) 0);
        try {
            len = System.in.read(cmdString);
            cmd = new String(cmdString, "UTF-8");
            return cmd;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "800 try again";
    }
    public synchronized void printOutput(String output) {
        System.out.println(output);
    }
    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }
    public enum CommandStrings {
        OPEN, USER, CLOSE, QUIT, GET, PUT, CD, DIR, PASS;

    }
    public UserCommands getUc() {
        return uc;
    }
}


import java.io.*;
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
    private String latestRead;

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
                if(!((cmd.substring(firstindex, i).equals(" ")) || (cmd.substring(firstindex, i).equals("")))){
                    args.add(cmd.substring(firstindex,i));
                }
                i = ++i;
                firstindex = i;
            }
        }
        if(args.size() == 0 || args.get(0).trim().substring(0 , 1).equals("#")){
            args.clear();
            args.add(0, "#");
        }

        return args;

    }
    public synchronized String concatWithSpaces(ArrayList<String> args) {
        String line = "";
        for (int i = 1; i < args.size(); i++) {
            line += args.get(i) + " ";

        }
        return line.trim();
    }

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
                case PASS:
                    uc.passCmd(args);
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
    public synchronized boolean setupControlCxn(String hostname, int port) throws IOException{
        try {
            controlCxn = new Socket(hostname, port);
            serverIn = new BufferedReader(new InputStreamReader(controlCxn.getInputStream()));
            serverOut = new BufferedWriter(new OutputStreamWriter(controlCxn.getOutputStream()));
        }
        catch (IOException e) {
            throw new IOException("820 Control Connection to " + hostname + " on port " + port + " failed to open");
            //printOutput("820 Control Connection to " + hostname + " on port " + port + " failed to open");
        }
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
        String line;
        try {
            line = serverIn.readLine();
            printOutput("<-- " + line);
            latestRead = line;
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
    public synchronized void checkIfTimeOut() {
        if (latestRead.contains("421 ")) {
            try {
                controlCxn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            setOpen(false);
            setStartProg(false);
        }
    }

    public synchronized void run() {
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
                        checkIfTimeOut();
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
            printOutput("899 Processing error " + e.getMessage());
        }
        return null;
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
    public UserCommands getUc() {
        return uc;
    }

    public boolean isStartProg() {
        return startProg;
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
        OPEN, USER, CLOSE, QUIT, GET, PUT, CD, DIR, PASS

    }

}


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
public class FTPPanel implements Runnable, Lock{

    int MAX_LEN = 255;

    public UserCommands getUc() {
        return uc;
    }

    private UserCommands uc = new UserCommands();
    private ServerMessages sm;
    private static Socket datacxn;
    private static Socket controlCxn;
    private static PrintWriter serverOut;
    private static BufferedReader serverIn;
    private Thread server;
    private static FTPPanel ftp;
    private byte cmdString[] = new byte[MAX_LEN];
    private String prompt = "csftp> ";
    int len;
    private boolean isOpen = false;
    private boolean startProg = false;


    private final Lock lock = new ReentrantLock();
    private volatile boolean running = true;

    private FTPPanel () {

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



    public synchronized static FTPPanel getInstance() {
       if (ftp == null)  {
           ftp = new FTPPanel();

       }
        return ftp;
    }

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

    public synchronized int handleCommand(ArrayList<String> args) throws IOException {
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
                    break;
                case DIR:
                    uc.dirCmd();
                    break;

            }
        } catch(Exception e){
            System.out.println("800 Invalid Command");
            return -1;
        }

        return 0;

    }
    public void handleMessage(String output) {

                ArrayList<String> args = new ArrayList<String>();
                if (output.contains("227 ")) {
                    uc.createDataConnection(output, "LIST");
                }
                else if (output.contains("220 ")) {
                    System.out.print("Please enter username: ");
                    String user = readInput();
                    args.add(1, user);
                    uc.userCmd(args);
                }

        return;

    }
    public synchronized boolean setupControlCxn(String hostname, int port) throws IOException{
        try {
            controlCxn = new Socket(hostname, port);
        }
        catch (IOException e) {
            throw new IOException("820 Control Connection to " + hostname + " on port " + port + " failed to open");
        }
            serverOut = new PrintWriter(controlCxn.getOutputStream(), true);
            serverIn = new BufferedReader(new InputStreamReader(controlCxn.getInputStream()));
           // sm = new ServerMessages(controlCxn);
            //server = new Thread(sm);
            //server.start();
            return true;

    }

    public synchronized void sendInput(String cmd) {
        try {
            serverOut.write(cmd + "\n");
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
            return line;

            }
        catch (IOException e) {
            System.out.println(e.getMessage());

        }
        return null;
    }
    public synchronized void printPrompt() {
            System.out.println(prompt);
    }

    public Socket getControlCxn() {
        return controlCxn;
    }

    @Override
    public synchronized void run() {
        while (true) {
            printOutput(prompt);
            String firstInput = readInput();
            ArrayList<String> args = parseInput(firstInput);
            if (args.get(0).equalsIgnoreCase("open")) {
                try {
                    handleCommand(args);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                if (isOpen()) {
                    startProg = true;
                }
            }
            else {
                System.out.println("803 Supplied command not expected at this time.");
                continue;
            }
            while (startProg) {
                synchronized (this) {
                    printOutput(prompt);
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
    public synchronized void printOutput(String output) {
        System.out.print(output);
    }
    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }
    @Override
    public void lock() {

    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {

    }

    public Lock getLock() {
        return lock;
    }
    @Override
    public Condition newCondition() {
        return null;
    }

    public enum CommandStrings {
        OPEN, USER, CLOSE, QUIT, GET, PUT, CD, DIR, PASS;

    }
}

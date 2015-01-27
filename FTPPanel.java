
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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
    private UserCommands uc = new UserCommands();
    private ServerMessages sm;
    private static Socket datacxn;
    private static Socket controlCxn;
    private static DataOutputStream serverOut;
    private Thread server;
    private static FTPPanel ftp;
    private byte cmdString[] = new byte[MAX_LEN];
    private String prompt = "csftp> ";
    String cmd;
    int len;
    private Lock lock = new ReentrantLock();

    private FTPPanel () {

    }

    public void readInput() {

            try {
                len = System.in.read(cmdString);
                cmd = new String(cmdString, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }


        parseInput(cmd);

        Arrays.fill(cmdString, (byte) 0);
    }


    public void printPrompt() {
    lock.lock();
        System.out.print(prompt);
        lock.unlock();
    }

    public static FTPPanel getInstance() {
       if (ftp == null)  {
           ftp = new FTPPanel();

       }
        return ftp;
    }

    public int parseInput(String cmd) {
        ArrayList<String> args = new ArrayList<String>();
        int firstindex = 0;
        for (int i=0; i < cmd.length(); i++) {
            if ((cmd.charAt(i) == ' ') || (cmd.charAt(i) == '\n')) {
                args.add(cmd.substring(firstindex,i));
                i = ++i;
                firstindex = i;
            }


        }
        handleCommand(args);
        return 0;

    }

    public int handleCommand(ArrayList<String> args) {
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
                case GET:
                    break;
                case PUT:
                    break;
                case CD:
                    break;
                case DIR:
                    break;

            }
        } catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println("800 Invalid Command");
            return -1;
        }

        return 0;

    }
    public void handleMessage(String output) {
        System.out.println(output);
        if (output.contains("220 ")) {
            String user;
            System.out.println("Please enter user name: ");
             byte cmdstr[] = new byte[MAX_LEN];
                try {
                    System.in.read(cmdstr);

                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    return;
                }

            user = new String(cmdstr);
            ArrayList<String> userargs = new ArrayList<String>();
            userargs.add("USER");
            userargs.add(user);
            uc.userCmd(userargs);
            Arrays.fill(cmdString, (byte) 0);
            return;
        }

    }
    public boolean setupControlCxn(String hostname, int port) {
        try {
            this.controlCxn = new Socket(hostname, port);
            serverOut = new DataOutputStream(controlCxn.getOutputStream());
            sm = new ServerMessages(controlCxn);
            server = new Thread(sm);
            server.start();
            return true;
        } catch (IOException e) {
            e.getMessage();
            return false;
        }

    }

    public static void sendInput(String cmd) {
        try {
            serverOut.writeUTF(cmd);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());

        }
    }

    public Socket getControlCxn() {
        return controlCxn;
    }

    @Override
    public void run() {
        while (true) {
            printPrompt();
            readInput();
        }

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

    @Override
    public Condition newCondition() {
        return null;
    }

    public enum CommandStrings {
        OPEN, USER, CLOSE, QUIT, GET, PUT, CD, DIR;

    }
}

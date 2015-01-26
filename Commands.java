import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by rohinpatel on 15-01-24.
 * Commands implemented by both Rohin Patel and Samir Marin
 */
public final class Commands {

    private static Socket datacxn;
    private static Socket controlCxn;
    private static DataOutputStream serverOut;
    private static Thread receiveserver;

    public static int parseInput(String cmd) {
        ArrayList<String> args = new ArrayList<String>();
        int firstindex = 0;
        for (int i=0; i < cmd.length(); i++) {
           if ((cmd.charAt(i) == ' ') || (cmd.charAt(i) == '\n')) {
               args.add(cmd.substring(firstindex,i));
               i = ++i;
               firstindex = i;
           }


        }
        handleCommand(args, cmd);
        return 0;

    }


    public static int handleCommand(ArrayList<String> args, String cmd) {
        try {
            switch (CommandStrings.valueOf(args.get(0).toUpperCase())) {
                case OPEN:
                    openCmd(args);
                    break;
                case USER:
                    userCmd(args, cmd);
                    break;
                case CLOSE:
                    closeCmd();
                    break;
                case QUIT:
                    quitCmd();
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
                System.out.println("800 Invalid Command");
                return -1;
            }

        return 0;

    }
    public static int openCmd(ArrayList<String> args) {
        if (controlCxn != null) {
            System.out.println("Already connected to server, please quit before connecting to another server");
            return -1; // NOT WORKING YET
        }
        if (args.size() < 2) {
            System.out.println("801 Incorrect number of arguments");
            return -1;
        }

        if (!args.get(0).equalsIgnoreCase("open")) {
            return -1;
        }
        String hostName = args.get(1);

        int port=21;
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
        try {
            controlCxn = new Socket(hostName, port);
            serverOut =
                    new DataOutputStream(controlCxn.getOutputStream());
            ServerMessages sm = new ServerMessages(
                    new BufferedReader(
                            new InputStreamReader(controlCxn.getInputStream())));
        receiveserver = new Thread(sm);
            receiveserver.start();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }
    public static int userCmd(ArrayList<String> args, String cmd) {
        if (args.size() != 2) {
            return -1;
        }
        if (!args.get(0).equalsIgnoreCase("user")) {
            return -1;
        }
        sendInput(cmd);

        return 0;
    }
    public static int closeCmd() {

        return 0;
    }
    public static int quitCmd() {

        return 0;
    }

    public static void sendInput(String cmd) {
        try {
             serverOut.writeUTF(cmd);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());

        }
    }
    public static Socket getControlCxn() {
        return controlCxn;
    }

    public static void setControlCxn(Socket controlCxn) {
        Commands.controlCxn = controlCxn;
    }


    public static DataOutputStream getServerOut() {
        return serverOut;
    }

    public static void setServerOut(DataOutputStream serverOut) {
        Commands.serverOut = serverOut;
    }

    public static Socket getDatacxn() {
        return datacxn;
    }

    public static void setDatacxn(Socket datacxn) {
        Commands.datacxn = datacxn;
    }

    public static enum CommandStrings {
        OPEN, USER, CLOSE, QUIT, GET, PUT, CD, DIR
    }
}

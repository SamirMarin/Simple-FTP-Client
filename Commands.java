import java.io.BufferedReader;
import java.io.IOException;
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
    private static BufferedReader serverIn;
    private static PrintWriter serverOut;
    private static int countRead = 0;

    public static int parseInput(String cmd) {
        ArrayList<String> args = new ArrayList<String>();
        int firstIndex = 0;
        for (int i=0; i < cmd.length(); i++) {
           if ((cmd.charAt(i) == ' ') || (cmd.charAt(i) == '\n')) {
               args.add(cmd.substring(firstIndex,i));
               i = ++i;
               firstIndex = i;
           }


        }
        handleCommand(args);
        return 0;

    }


    public static int handleCommand(ArrayList<String> args) {
        try {
            switch (CommandStrings.valueOf(args.get(0).toUpperCase())) {
                case OPEN:
                    openCmd(args);
                    break;
                case USER:
                    userCmd();
                    break;
                case CLOSE:
                    closeCmd(args);
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
            return -1;
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
        System.out.println("Made it this far");
        try {
            controlCxn = new Socket(hostName, port);
            serverOut =
                    new PrintWriter(controlCxn.getOutputStream(), true);
            serverIn =
                    new BufferedReader(
                            new InputStreamReader(controlCxn.getInputStream()));
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }
    public static int userCmd() {

        return 0;
    }
    public static int closeCmd(ArrayList<String> args) {
        /*if(args.size() > 1){
            return -1;
        }*/
        if(!args.get(0).equalsIgnoreCase("close")){
            return -1;
        }
        else{
            try {
                serverIn.close();
                serverOut.close();
                controlCxn.close();
                countRead = 0;
            } catch (IOException e) {
                System.err.println(e.getCause());
            }
            return 0;
        }
    }
    public static int quitCmd() {

        return 0;
    }
    public static void readInput() {
        if (serverIn == null) {
            return;
        }
        else if(countRead > 0){
            return;
        }
        countRead++;
            try {
                System.out.println(serverIn.readLine());

            } catch (Exception e) {
                System.err.println(e.getCause());
                return;
            }

        return;

    }

    public static enum CommandStrings {
        OPEN, USER, CLOSE, QUIT, GET, PUT, CD, DIR
    }
}

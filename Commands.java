import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by rohinpatel on 15-01-24.
 * Commands implemented by both Rohin Patel and Samir Marin
 */
public final class Commands {

    public static Socket datacxn;


    public static int parseInput(String cmd) {
     System.out.println(cmd);

    handleCommand(cmd.split(" "));
    return 0;

    }


    public static int handleCommand(String [] args){
        switch (CommandStrings.valueOf(args[0].toUpperCase())) {
            case OPEN:
                openCmd(args);
                break;
            case USER:
                userCmd();
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
            default:
                return -1;
        }
        System.out.println(args[0]);
        System.out.println(args[1]);
        return 0;

    }
    public static int openCmd(String[] args) {
        if (!args[0].equalsIgnoreCase("open")) {
            return -1;
        }
        String hostName = args[1];
        int port = Integer.parseInt(args[2]);
        if (port == 0) {
            port = 21;
        }
        try {
            CSftp.controlCxn = new Socket(hostName, port);
            CSftp.pr =
                    new PrintWriter(CSftp.controlCxn.getOutputStream(), true);
            CSftp.br =
                    new BufferedReader(
                            new InputStreamReader(CSftp.controlCxn.getInputStream()));
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }
    public static int userCmd() {

        return 0;
    }
    public static int closeCmd() {

        return 0;
    }
    public static int quitCmd() {

        return 0;
    }

    public static enum CommandStrings {
        OPEN, USER, CLOSE, QUIT, GET, PUT, CD, DIR
    }
}

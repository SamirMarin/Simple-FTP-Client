import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by rohinpatel on 15-01-24.
 * UserCommands implemented by both Rohin Patel and Samir Marin
 */
public class UserCommands {


    public synchronized int openCmd(ArrayList<String> args) {
        if (FTPPanel.getInstance().getControlCxn() != null) {
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

        // setup socket
           if  (FTPPanel.getInstance().setupControlCxn(hostName, port)) {
            System.out.println("Connected to " + args.get(1));
        }
        else {
               return -1;
           }

        return 0;
    }
    public synchronized int userCmd(ArrayList<String> args) {
        if (args.size() != 2) {
            System.out.println("Too few arguments");
            return -1;
        }
        if (!args.get(0).equalsIgnoreCase("user")) {
            return -1;
        }

        FTPPanel.getInstance().sendInput("USER " + args.get(1));
        return 0;
    }

    public synchronized void printOutPut(String output) {
        System.out.print(output);
    }
    public synchronized int closeCmd() {

        return 0;
    }
    public synchronized int quitCmd() {

        return 0;
    }


}

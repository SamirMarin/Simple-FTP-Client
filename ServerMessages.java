import com.sun.corba.se.spi.activation.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by rohinpatel on 15-01-25.
 */
public class ServerMessages implements Runnable{

    private BufferedReader serverIn;


    public ServerMessages(Socket socket) {
        try {
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {

        }
    }
//    public ServerMessages getInstance(Socket socket) {
//        if (sm == null) {
//            sm = new ServerMessages(socket);
//        }
//                    return sm;
//
//    }
    String output;

    public synchronized void readInput() {
        if (getServerIn() == null) {
            return;
        }
        try {

                    while (getServerIn().ready()) {
                        output = null;
                        if ((output = getServerIn().readLine()) != null) {
                            //FTPPanel.getInstance().handleMessage(output);
                        }
                }
        }
        catch (Exception e) {
            System.err.println(e.getCause());
            return;
        }
        return;

    }


    @Override
    public synchronized void run() {
        while (true) {
                readInput();
        }
    }
    public BufferedReader getServerIn() {
        return serverIn;
    }

    public void setServerIn(BufferedReader serverIn) {
        this.serverIn = serverIn;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

}

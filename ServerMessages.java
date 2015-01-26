import java.io.BufferedReader;

/**
 * Created by rohinpatel on 15-01-25.
 */
public class ServerMessages implements Runnable{

    public ServerMessages(BufferedReader in) {
        serverIn = in;
    }
    private BufferedReader serverIn;
    public void readInput() {
        if (getServerIn() == null) {
            return;
        }
        try {
                System.out.println(getServerIn().readLine());


        }
        catch (Exception e) {
            System.err.println(e.getCause());
            return;
        }
        return;

    }

    @Override
    public void run() {
        while (true) {
            readInput();
            System.out.print("csftp> ");
        }
    }
    public BufferedReader getServerIn() {
        return serverIn;
    }

    public void setServerIn(BufferedReader serverIn) {
        this.serverIn = serverIn;
    }
}

/**
 * Created by rohinpatel on 15-01-24.
 */
public final class Commands {

    private Commands() {

    }

    public static int parseInput(String cmd) {
     System.out.println("Inside parse input");

    handleCommand(cmd.split(" "));
return 0;

    }


    public static int handleCommand(String [] args){
        switch (args[0]) {
            case "open":
                openCmd();
                break;

        }
        return 0;

    }
    public static int openCmd() {

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
}

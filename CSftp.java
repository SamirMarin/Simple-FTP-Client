
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.System;
import java.io.IOException;
import java.net.Socket;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;

//
// This is an implementation of a simplified version of a command 
// line ftp client. The program takes no arguments.
//


public class CSftp
{
	static final int MAX_LEN = 255;
	static FTPCommand command;
	public static void main(String [] args) {
		byte cmdString[] = new byte[MAX_LEN];

		int len;
		String cmd;
		command = new FTPCommand();

		try {
			while (true) {

			    cmd = command.readUserInput();
				ArrayList<String> stringList = parseInput(cmd); // returns an Array of strings
				checkCommand(stringList);
				command.writeOutput(command.readLine());
			}
		} catch (IOException exception) {
			System.err.println("898 Input error while reading commands, terminating.");
			command.writeOutput(exception.getMessage());
		}
	}

	public static ArrayList<String> parseInput(String cmd) {
		ArrayList<String> args = new ArrayList<String>();
		int firstIndex = 0;
		for (int i=0; i < cmd.length(); i++) {
			if ((cmd.charAt(i) == ' ') || (cmd.charAt(i) == '\n')) {
				args.add(cmd.substring(firstIndex,i));
				i = ++i;
				firstIndex = i;
			}


		}

		return args;

	}

	public static void checkCommand(ArrayList<String> stringList) throws IOException{

		int length = stringList.size();
		if ((!stringList.get(0).equals("open")) && (!command.isOpen())) {
		command.writeOutput("803 Supplied command not expected at this time.");
			return;
		}
		if (stringList.get(0).equalsIgnoreCase("open")) {
			if (length < 2 || length > 3) {
				command.writeOutput("801 Incorrect number of arguments");
			} else if (length == 2) {
				command.open(stringList.get(1));
			} else {
				command.open(stringList.get(1), Integer.parseInt(stringList.get(2)));
			}
		} else if (stringList.get(0).equalsIgnoreCase("user")) {
			if (length == 2) {
				command.user(stringList.get(1));
			} else {
				command.writeOutput("801 Incorrect number of arguments");
			}
		} else if (stringList.get(0).equalsIgnoreCase("quit")) {
			if (length == 1) {
				command.quit();
			} else {
				command.writeOutput("801 Incorrect number of arguments");
			}
		} else if (stringList.get(0).equalsIgnoreCase("cd")) {
			if (length < 2 || length > 3) {
				command.writeOutput("801 Incorrect number of arguments");
			} else {
				command.changeDicCmd(stringList.get(1));
			}
		} else if (stringList.get(0).equalsIgnoreCase("dir")) {
			if (length == 1) {
			command.dirCmd();
			} else {
				System.out.println("801 Incorrect number of arguments");
			}
		}
		else if (stringList.get(0).equalsIgnoreCase("pass")) {
			if (length < 2 || length > 3) {
				command.writeOutput("801 Incorrect number of arguments");
			} else {
				command.passCmd(stringList.get(1));
			}
		}
		else if (stringList.get(0).equalsIgnoreCase("close")) {
			if (length == 1) {
				command.close();
			} else {
				System.out.println("801 Incorrect number of arguments");
			}
		}
	}

}

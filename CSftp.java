
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
	public static void main(String [] args) {
		byte cmdString[] = new byte[MAX_LEN];

		int len;
		String cmd;
		FTPCommand command = new FTPCommand();

		try {
			while (true) {
				Arrays.fill(cmdString, (byte) 0);
				System.out.print("csftp> ");
				len = System.in.read(cmdString);
				if (len <= 0)
					break;
				cmd = new String(cmdString, "UTF-8");


				ArrayList<String> stringList = parseInput(cmd); // returns an Array of strings

				if(stringList.get(0).equalsIgnoreCase("open")){
					int length = stringList.size();
					if(length < 2 || length > 3){
						System.out.println("801 Incorrect number of arguments");
					}
					else if(length == 2){
						command.open(stringList.get(1));
					}
					else{
						command.open(stringList.get(1), Integer.parseInt(stringList.get(2)));
					}
				}
				else if(stringList.get(0).equalsIgnoreCase("user")){
					int length = stringList.size();
					if(length == 2){
						command.user(stringList.get(1));
					}
					else{
						System.out.println("801 Incorrect number of arguments");
					}
				}
				else if(stringList.get(0).equalsIgnoreCase("quit")){
					int length = stringList.size();
					if(length == 1) {
						command.quit();
					}
					else{
						System.out.println("801 Incorrect number of argumets");
					}
				}

			}
		} catch (IOException exception) {
			System.err.println("898 Input error while reading commands, terminating.");
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




}

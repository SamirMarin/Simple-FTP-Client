
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.System;
import java.io.IOException;
import java.net.Socket;
import java.nio.Buffer;

//
// This is an implementation of a simplified version of a command 
// line ftp client. The program takes no arguments.
//


public class CSftp
{
    static final int MAX_LEN = 255;
	public static Socket controlCxn;
	public static BufferedReader sbr;
	public static PrintWriter spr;
    public static void main(String [] args)
    {
	byte cmdString[] = new byte[MAX_LEN];
		int len;

		String cmd;
	try {
		while (true) {
		System.out.print("csftp> ");
			len = System.in.read(cmdString);
		if (len <= 0)
		    break;
		      cmd = new String(cmdString, "UTF-8");
			Commands.parseInput(cmd); // returns an Array of strings
		// Start processing the command here.
		System.out.println("900 Invalid command.");
	    }
	} catch (IOException exception) {
	    System.err.println("998 Input error while reading commands, terminating.");
	}
    }
}

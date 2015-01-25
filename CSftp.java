
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.System;
import java.io.IOException;

//
// This is an implementation of a simplified version of a command 
// line ftp client. The program takes no arguments.
//


public class CSftp
{
    static final int MAX_LEN = 255;

    public static void main(String [] args)
    {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	byte cmdString[] = new byte[MAX_LEN];
		int len;
	try {
		while (true) {
		System.out.print("csftp> ");
			len = System.in.read(cmdString);
		if (len <= 0)
		    break;

			Commands.parseInput(cmdString.toString()); // returns an Array of strings
		// Start processing the command here.
		System.out.println("900 Invalid command.");
	    }
	} catch (IOException exception) {
	    System.err.println("998 Input error while reading commands, terminating.");
	}
    }
}

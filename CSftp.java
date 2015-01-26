
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.System;
import java.io.IOException;
import java.net.Socket;
import java.nio.Buffer;
import java.util.Arrays;

//
// This is an implementation of a simplified version of a command 
// line ftp client. The program takes no arguments.
//


public class CSftp
{
	static final int MAX_LEN = 255;
	public static void main(String [] args)
	{
		byte cmdString[] = new byte[MAX_LEN];

		int len;
		String cmd;
		try {
			while (true) {
				Arrays.fill(cmdString, (byte) 0);
				System.out.print("csftp> ");
				len = System.in.read(cmdString);
				if (len <= 0)
					break;
				cmd = new String(cmdString, "UTF-8");


				Commands.parseInput(cmd);
				//Arrays.fill(cmdString, (byte) 0);
				// Start processing the command here.
				}
		} catch (IOException exception) {
			System.err.println("898 Input error while reading commands, terminating.");
		}
	}

	}



//
// This is an implementation of a simplified version of a command 
// line ftp client. The program takes no arguments.
//


public class CSftp
{
	static final int MAX_LEN = 255;

	public static void main(String [] args)
	{
		FTPPanel ftp = FTPPanel.getInstance();
		Thread main = new Thread(ftp.getInstance());
		main.start();

				// Start processing the command here.
				}




	}

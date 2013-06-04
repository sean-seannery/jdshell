import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author Sean Maloney
 * 
 * This class serves as a thread that issues user commands and recieves feedback from the
 * individual peers.  One thread is spun up per peer, per command.
 * 
 */
public class JDShellClientThread extends Thread{

	private String input; //the command
	private String currentDir; //working directory of command
	private String peer; //the peer i am applying the command to

	private boolean hasError = false; //whether or not there was an error executing this command
	
	public JDShellClientThread(String peer, String input, String currentDir){
		this.peer = peer;
		this.input = input;
		this.currentDir = currentDir;
	}
	
	/**
	 * This gets executed when the thread starts
	 * @see java.lang.Thread#run()
	 */
	public void run(){
		try {
			//set the handshake time out to 3 seconds 
			Socket sender = new Socket();
			sender.connect(new InetSocketAddress(peer, JDShellServer.SERVER_PORT), 3000);
			PrintWriter out = new PrintWriter(
		               new OutputStreamWriter(sender.getOutputStream()));
			BufferedReader bin = new BufferedReader(
		               new InputStreamReader(sender.getInputStream()));
			//send the peer server 1.)their address from my POV 2.) the command 3.) the working dir.
			//this is processed in JDShellListenerThread
			out.println(peer);
			out.println(input);
			out.println(currentDir);
			out.flush();
			String value;
			while(( value = bin.readLine()) != null){
				//used by JDShellListenerThread.changeDirectory to return a successful cd
				if (value.contains("!CURRENT_DIR: "))
				{
					this.currentDir = value.split(": ")[1];
				//used by JDShellListenerThread to return that something broke
				} else if (value.contains("!ERROR")){
					hasError = true;
				} else
				{
					System.out.println(value);
				}
			}			
			out.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			if (e.getMessage().contains("Connection refused") || e.getMessage().contains("connect timed out") || e.getMessage().contains("No route to host") ) {
				System.out.println("@" + peer);
				System.out.println("  ERROR: unable to connect to peer server");
			}
			else {
				e.printStackTrace();
			}
		}
	}

	public String getCurrentDir() {
		return currentDir;
	}
	public boolean hasError() {
		return hasError;
	}

}

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class JDShellClientThread extends Thread{
	
	private String peer;
	private String input;
	private String currentDir;
	private boolean hasError = false;
	
	public JDShellClientThread(String peer, String input, String currentDir){
		this.peer = peer;
		this.input = input;
		this.currentDir = currentDir;
	}
	
	public void run(){
		try {
			//set the time out to 5 seconds
			Socket sender = new Socket();
			sender.connect(new InetSocketAddress(peer, JDShellServer.SERVER_PORT), 3000);
			PrintWriter out = new PrintWriter(
		               new OutputStreamWriter(sender.getOutputStream()));
			BufferedReader bin = new BufferedReader(
		               new InputStreamReader(sender.getInputStream()));
			out.println(peer);
			out.println(input);
			out.println(currentDir);
			out.flush();
			String value;
			while(( value = bin.readLine()) != null){
				if (value.contains("!CURRENT_DIR: "))
				{
					this.currentDir = value.split(": ")[1];
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
			if (e.getMessage().contains("Connection refused") || e.getMessage().contains("connect timed out") ) {
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

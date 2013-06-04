import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class JDShellListenerThread extends Thread{

	Socket connection;
	private String currentDir;
	private String peer;
	
	public JDShellListenerThread(Socket newConnection){
		this.connection = newConnection;
	}
	

	public void run(){
		BufferedReader in = null;
		String input = null;
		try {
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			peer = in.readLine();
			input = in.readLine();
			currentDir = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (input.toLowerCase().startsWith("cd") )
		{
			if (input.split(" ").length >= 2){
					
				try {
					changeDirectory(input.split(" ")[1]);
				} catch (FileNotFoundException e) {
					send(e.getMessage());				
				}
				
				
			}
			
		} else if (!input.equals("")){
			exec(input, peer);			
		}
		try {
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void changeDirectory(String input) throws FileNotFoundException{

		if (input.equals(".") ) {
			return;
		}
		if (input.equals("..")){
			String[] dirs = currentDir.split("/");
			currentDir = "/";
			//first element is "" and i dont feel like stripping it properly, so i start at index 1
			for (int i = 1; i < dirs.length -1; i++){
				currentDir += dirs[i] + "/";
			}
		} else if (!input.startsWith("/")){
			File test = new File(currentDir  + input);
			if (test.exists()) {
				currentDir = currentDir + input;
			} else {
				throw new FileNotFoundException("@" + peer + "\n" + " ERROR: Directory does not exist: " + currentDir +  input);
			}		
			
		} else {
			File test = new File(input);
			if (test.exists()) {
				currentDir = input;
			} else {
				throw new FileNotFoundException("@" + peer + "\n" + " ERROR: Directory does not exist: " +  input);
			}	
		}
		if (!currentDir.endsWith("/")) {
			currentDir += "/";
		}
		send("!CURRENT_DIR: " + currentDir);

	}

	private void exec(String command, String peer){
		String[] commandArgs = command.split(" ");
		PrintWriter out = null;
		try {
			out = new PrintWriter(
		               new OutputStreamWriter(connection.getOutputStream()));
			
			ProcessBuilder builder = new ProcessBuilder(commandArgs);
			builder.directory(new File(currentDir));
			builder.redirectErrorStream(true);
			Process proc = builder.start();
			
			JDShellCommandReader outputReader = new JDShellCommandReader(proc.getInputStream(), out, peer);			
			outputReader.process();

			//wait until the readers complete
			proc.waitFor();

			
			
		} catch (IOException e) {
			if (e.getMessage().contains("No such file or directory")){
				out.println("@" + peer);
				out.println("   ERROR: That file or command does not exist");
				out.flush();
				out.close();
			}
			else
				e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void send(String value){
		PrintWriter out;
		try {
			out = new PrintWriter(
			           new OutputStreamWriter(connection.getOutputStream()));
			out.println(value);
			out.flush();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
		
}

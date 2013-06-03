import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;


public class JDShellListenerThread {

	Socket connection;
	private String currentDir;
	
	public JDShellListenerThread(Socket newConnection){
		this.connection = newConnection;
	}
	

	public void run(){
		BufferedReader in = null;
		String input = null;
		String peer = null;
		try {
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			peer = in.readLine();
			input = in.readLine();
			currentDir = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Connection Accepted");
		if (input.toLowerCase().startsWith("cd") )
		{
			if (input.split(" ").length >= 2){
				changeDirectory(input.split(" ")[1]);
			}
			
		} else if (!input.equals("")){
			exec(input, peer);			
		}
	}
	
	
	private void changeDirectory(String input) {
		
		 if (!input.startsWith("/")){
			File test = new File(currentDir  + input);
			if (test.exists()) {
				currentDir = currentDir + input;
			} else {
				System.out.println("Directory does not exist: " + currentDir +  input);
			}		
			
		} else {
			File test = new File(input);
			if (test.exists()) {
				currentDir = input;
			} else {
				System.out.println("Directory does not exist: " + currentDir +  input);
			}	
		}
		if (!currentDir.endsWith("/")) {
			currentDir += "/";
		}
		System.out.println("Changed to " + currentDir);
		System.out.print("JDS%: ");
	}

	private void exec(String command, String peer){
		String[] commandArgs = command.split(" ");

		try {
			ProcessBuilder builder = new ProcessBuilder(commandArgs);
			builder.directory(new File(currentDir));
			Process proc = builder.start();
			
			JDShellCommandReader outputReader = new JDShellCommandReader(proc.getInputStream(), connection, peer);
			JDShellCommandReader errorReader = new JDShellCommandReader(proc.getErrorStream(), connection, peer);
			

			errorReader.start();
			outputReader.start();
			proc.waitFor();
			
		} catch (IOException e) {
			System.out.println("That file or command does not exist");
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
		
}

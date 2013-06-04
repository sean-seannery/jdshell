import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Sean Maloney
 * 
 * This is an object/thread that gets created when the JDShellServer recieves a command from the client.
 * Depending on what the command is, it will process appropriately.  If it is executing some other program
 * such as ls, pwd, touch.  It will execute the command locally and return the output.  If it is CD, which
 * is not an external command, it will ensure that the directory exists and return a success or failure
 * which will be used to update the current working directory.
 */
public class JDShellListenerThread extends Thread{

	Socket connection; //connection with the client
	private String currentDir; //current working directory
	private String peer; //who the client thinks I am.

	public JDShellListenerThread(Socket newConnection){
		this.connection = newConnection;
	}
	
	/**
	 * This gets executed when the thread starts
	 * @see java.lang.Thread#run()
	 */
	public void run(){
		BufferedReader in = null;
		String input = null;
		try {
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			//recieve from the client 1.)who they think i am 2.) the command 3.) the working dir.
			//these messages are sent in order by JDShellClientThread.
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
	
	/**
	 * This method attempts to check if the directory provided exists. if it does then it sends
	 * a message back to the client containing !CURRENT_DIR to indicate that it was successful.
	 * @throws FileNotFoundException when you try to change to a directory that doesnt exist
	 * @param input the new directory location (could be relative)
	 */
	private void changeDirectory(String input) throws FileNotFoundException{
		//handle "."
		if (input.equals(".") ) {
			return;
		}
		//handle relative address ".." above vurrent dir.
		if (input.equals("..")){
			String[] dirs = currentDir.split("/");
			currentDir = "/";
			//first element is "" and i dont feel like stripping it properly, so i start at index 1
			for (int i = 1; i < dirs.length -1; i++){
				currentDir += dirs[i] + "/";
			}
		//handle absolute addresses
		} else if (!input.startsWith("/")){
			File test = new File(currentDir  + input);
			if (test.exists()) {
				currentDir = currentDir + input;
			} else {
				throw new FileNotFoundException("@" + peer + "\n" + " ERROR: Directory does not exist: " + currentDir +  input);
			}		
		//handle relative addresses below current dir
		} else {
			File test = new File(input);
			if (test.exists()) {
				currentDir = input;
			} else {
				throw new FileNotFoundException("@" + peer + "\n" + " ERROR: Directory does not exist: " +  input);
			}	
		}
		//ensures that the currentDir always ends with "/" so we dont get weird concatination issues
		if (!currentDir.endsWith("/")) {
			currentDir += "/";
		}
		send("!CURRENT_DIR: " + currentDir);

	}

	/**
	 * This method is in charge of executing all the other programs except cd
	 * unfortunately it does not support xterm applications at the moment.
	 */
	private void exec(String command, String peer){
		String[] commandArgs = command.split(" ");
		PrintWriter out = null;
		try {
			out = new PrintWriter(
		               new OutputStreamWriter(connection.getOutputStream()));
			
			//set up the process to execute
			ProcessBuilder builder = new ProcessBuilder(commandArgs);
			builder.directory(new File(currentDir));
			builder.redirectErrorStream(true); // tell error stream to go to stdout (otherwise we dont see errors)
			Process proc = builder.start();
			
			//process the output and send it to the client
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
	
	/**
	 * Sends a message to the client
	 */
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

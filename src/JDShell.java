import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * 
 */

/**
 * @author sam
 *
 */
public class JDShell {
	
	private static String[] peers = {"127.0.0.1", "192.168.44.178"};
	private static String currentDir;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		currentDir = System.getProperty("user.dir");
		//System.out.println(currentDir);
		String input = null;
		JDShellServer server = new JDShellServer();
		server.start();
		
		Scanner in = new Scanner(System.in);
		System.out.print("JDS%:");
		while (!(input = in.nextLine()).equals("quit") && !input.equals("exit")){
			
			if (input.equals("")) {
				System.out.print("JDS%: ");
			} else {
				if (input.toLowerCase().startsWith("cd") )
				{
					if (input.split(" ").length >= 2){
						//change locally
						changeDirectory(input.split(" ")[1]);

					}
					
				} 
				for (String peer : peers){
					Socket sender;
					try {
						sender = new Socket(peer, JDShellServer.SERVER_PORT);
						PrintWriter out = new PrintWriter(
					               new OutputStreamWriter(sender.getOutputStream()));
						BufferedReader bin = new BufferedReader(
					               new InputStreamReader(sender.getInputStream()));
						out.println(input);
						out.println(currentDir);
						out.flush();
						String test;
						while(( test = bin.readLine()) != null){
							System.out.println(test);
						}				
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}
		server.stopMe();
		
		

	}
	
	private static void changeDirectory(String input) {
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
	



}

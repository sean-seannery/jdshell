import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Scanner;

/**
 * 
 */

/**
 * @author sam
 *
 */
public class JDShell {
	
	private static String currentDir;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		currentDir = System.getProperty("user.dir");
		//System.out.println(currentDir);
		String input = null;
		Scanner in = new Scanner(System.in);
		System.out.print("JDS%:");
		while (!(input = in.nextLine()).equals("quit") && !input.equals("exit")){
			
			if (input.toLowerCase().startsWith("cd") )
			{
				if (input.split(" ").length >= 2){
					changeDirectory(input.split(" ")[1]);
				}
				
			} else if (!input.equals("")){
				exec(input);			
			} else {
				System.out.print("JDS%: ");
			}

		}
		

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

	private static void exec(String command){
		String[] commandArgs = command.split(" ");

		try {
			ProcessBuilder builder = new ProcessBuilder(commandArgs);
			builder.directory(new File(currentDir));
			Process proc = builder.start();
			
			JDShellCommandReader outputReader = new JDShellCommandReader(proc.getInputStream());
			JDShellCommandReader errorReader = new JDShellCommandReader(proc.getErrorStream());
			

			errorReader.start();
			outputReader.start();
			proc.waitFor();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


}

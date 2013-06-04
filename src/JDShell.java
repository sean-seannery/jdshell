

import java.util.ArrayList;
import java.util.Scanner;

/**
 * 
 */

/**
 * @author sam
 *
 */
public class JDShell {
	
	private static String[] peers = {"127.0.0.1","192.168.14.106"};
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
				
				ArrayList<JDShellClientThread> threadList = new ArrayList<JDShellClientThread>();
				for (String peer : peers){
					threadList.add(new JDShellClientThread(peer, input, currentDir));
					threadList.get(threadList.size()-1).start();
				}
				for (JDShellClientThread thread : threadList){
					try {
						thread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (input.toLowerCase().startsWith("cd")) {
			
					boolean hasErrors = false;
					String newDir = "";
					for (JDShellClientThread thread : threadList){
						if (thread.hasError()){
							hasErrors = true;
						}
						newDir = thread.getCurrentDir();
					}
					
					if (hasErrors == false && !newDir.equals(currentDir))
						currentDir = newDir;
				}
				
				System.out.print("\nJDS%:");
			}
			
		}
		server.stopMe();
		
		

	}
	

}

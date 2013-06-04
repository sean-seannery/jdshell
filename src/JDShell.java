import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * @author Sean Maloney
 * This is the Java Distributed Shell Client.  It reads in commands and spins of JDShellClientThreads
 * which send the command to the peers and process the feedback. This is for a Operating Systems class
 * project.  I would have like to have written this shell in C++ but socket/network/multithreading is a
 * pain in the ass in c++ for only 10 weeks.  
 * 
 * CAVEAT!  Unfortunately this shell does not currently support any xterm applications such as vim,
 * top, more, etcetera. 
 * CAVEAT!  Currently only tested on ubuntu linux.  Though it should easily port to other OSs with minor
 * effort because of java-ness.
 */
public class JDShell {
	
	//file that gets read in and loads this computer's peers.  you may need to change this to get it to work
	private static final String PEER_LIST_FILE_LOC = "../peer_list";
	private static ArrayList<String> peers;
	
	//the current directory. cd is not an external program, so this gets changed when the user issues 'cd"
	private static String currentDir;

	/**
	 * Starts the app, starts the server, and waits for user input
	 * @param args not used.
	 */
	public static void main(String[] args) {
		init();
		currentDir = System.getProperty("user.dir");

		//System.out.println(currentDir);
		String input = null;
		JDShellServer server = new JDShellServer();
		server.start();
		
		Scanner in = new Scanner(System.in);
		showPrompt();
		//stop the client if user enters "quit" or "exit"
		while (!(input = in.nextLine()).equals("quit") && !input.equals("exit")){
			
			if (input.equals("")) {
				showPrompt();
			} else {
				
				//handle the ability to target a command to only one server using !@peeraddress
				//example "touch newfile.txt !@127.0.0.1"
				ArrayList<String> tempPeers = peers;
				if (input.contains("!@")){
					String singleton = input.split("!@")[1];
					input = input.split("!@")[0];
					tempPeers = new ArrayList<String>();
					tempPeers.add(singleton);
				}
				
				//loop through all peers and spin off a new thread that handles the input
				ArrayList<JDShellClientThread> threadList = new ArrayList<JDShellClientThread>();
				for (String peer : tempPeers){
					threadList.add(new JDShellClientThread(peer, input, currentDir));
					threadList.get(threadList.size()-1).start();
				}
				//tell the client to block until all threads are completed.
				for (JDShellClientThread thread : threadList){
					try {
						thread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				//if cd was issued and none of the threads had an error. update the current directory
				//if just one of the threads had an error, do not update the directory.
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
				
				showPrompt();
			}
			
		}
		server.stopMe();

	}
	
	/**
	 * Formats the appearance of the shell prompt and displays it.
	 */
	private static void showPrompt(){
		String dir = new File(currentDir).getName();
		SimpleDateFormat sdfDate = new SimpleDateFormat("hh:mm:ss.SSS");
		Date now= new Date();
		System.out.print("["+sdfDate.format(now)+"] "+ dir + " - JDS%: ");
	}
	
	/**
	 * Reads in the Peer List File and stores it to an array to send off to people
	 */
	private static void init() {
		peers = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(PEER_LIST_FILE_LOC)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				//ignore comments and empty lines
				if (!line.startsWith("#") && !line.trim().equals(""))
					peers.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException ie){
			ie.printStackTrace();
		}
		System.out.print(peers.toString());
	}

}

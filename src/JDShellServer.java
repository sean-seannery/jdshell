import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Sean Maloney
 * 
 * This is the shell server that listens for commands on the peers.  It sits listening until
 * the client sends it a command to process.  Once the command is processed, it spins up a new
 * JDShellListenerThread in order to actually run the command.  This must be multi-threaded to
 * handle situations when multiple administrators need to send commands at the same time.
 */
public class JDShellServer extends Thread{

		public static final int SERVER_PORT = 9876;
		private ServerSocket socket;
		private boolean isRunning = true;
		
		/**
		 * This gets executed when the thread starts
		 * @see java.lang.Thread#run()
		 */
		public void run(){
			try {
				socket = new ServerSocket(SERVER_PORT);
				System.out.println("\n Starting Server....");
				System.out.println("=============================================");
				//listen until I tell you to stop.
				while (isRunning) {
							
					Socket connected_socket = socket.accept();
					JDShellListenerThread t = new JDShellListenerThread(connected_socket);
					t.run();
									
				}
			} catch (IOException e) {
				if (isRunning){
					e.printStackTrace();
				}
			}
			
		}
		
		/**
		 * This stops the server and cleans up any open sockets.
		 */
		public void stopMe(){
			System.out.println("=============================================");
			System.out.println("\n Shutting Down Server....");
			
			try {
				isRunning = false;
				this.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(0);
		}
}

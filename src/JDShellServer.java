import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class JDShellServer extends Thread{

		public static final int SERVER_PORT = 9876;
		private ServerSocket socket;
		private boolean isRunning = true;
	
		public void run(){
			try {
				socket = new ServerSocket(SERVER_PORT);
				System.out.println("\n Starting Server....");
				System.out.println("=============================================");
				
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
			
			//stopMe();
		}
		
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

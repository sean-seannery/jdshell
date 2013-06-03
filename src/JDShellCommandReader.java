import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;


public class JDShellCommandReader extends Thread {

    InputStream inStream;
    Socket connection;
    // reads everything from is until empty. 
    JDShellCommandReader(InputStream is, Socket newConnection) {
        this.inStream = is;
        connection = newConnection;
    }

    public void run() {
        try {
        	int lineCounter = 0;
            BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
            PrintWriter out = new PrintWriter(
		               new OutputStreamWriter(connection.getOutputStream()));
            String line=null;
            InetAddress addr = InetAddress.getLocalHost();
        	String hostname = addr.getHostName();
        	out.println("@"+hostname);
            while ( (line = br.readLine()) != null)
            {
            	lineCounter++;
            	
            	out.println(line);
               // System.out.println(line);    
            }

            out.flush();
            	
                if (lineCounter > 0 ){
                	//System.out.print("JDS%:");
                	out.println("JDS%:");
                	out.flush();
                }
            	br.close();
            	out.close();
            
        } catch (IOException e) {
            e.printStackTrace();  
        }
    }
}

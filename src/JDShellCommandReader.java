import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author Sean Maloney
 * 
 * JDShellCommandReader processes the input/output from external programs and returns them to the client
 * This is threaded because some applications require that kinda stuff.  Such as ping. which keeps going
 * and going.
 */
public class JDShellCommandReader extends Thread {

    InputStream inStream;
    PrintWriter out;
    String peer;
    // reads everything from is until empty. 
    JDShellCommandReader(InputStream is, PrintWriter outstream,  String peer) {
    	this.peer = peer;
        this.inStream = is;
        this.out = outstream;
    }
    
    public void run() {
    	process();
    }

    public void process(){
        try {
        	int lineCounter = 0;
        	InputStreamReader ior = new InputStreamReader(inStream);  

        	BufferedReader br = new BufferedReader(ior);
            String line=null;
            while ( (line = br.readLine()) != null)
            {
            	lineCounter++;
            	if (lineCounter == 1 ){;
                	out.println("@"+peer );
                	//out.flush();
                }
            	out.println("  " + line);
            }
            out.flush();

           	out.close();
           	br.close();
            
        } catch (IOException e) {
            e.printStackTrace();  
        }
    }
    
 
}

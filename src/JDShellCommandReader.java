import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class JDShellCommandReader extends Thread {

    InputStream inStream;

    // reads everything from is until empty. 
    JDShellCommandReader(InputStream is) {
        this.inStream = is;
    }

    public void run() {
        try {
        	int lineCounter = 0;
            BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
            String line=null;
            while ( (line = br.readLine()) != null)
            {
            	lineCounter++;
                System.out.println(line);    
            }
            if (lineCounter > 0 ){
            	System.out.print("JDS%:");
            }
        } catch (IOException e) {
            e.printStackTrace();  
        }
    }
}

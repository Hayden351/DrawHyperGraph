package draw;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Callable;

/**
 * @author Hayden Fields
 */
public class DrawHyperGraphCli implements Callable
{
        public static void main (String[] args) throws Exception
    {
        new DrawHyperGraphCli().call();
    }
    
    @Override
    public Object call () throws Exception
    { try (Socket toServer = new Socket("localhost", 65123)) {
        ObjectOutputStream sendToServer = new ObjectOutputStream(toServer.getOutputStream());
        ObjectInputStream recievedFromServer = new ObjectInputStream(toServer.getInputStream());
        ObjectMapper mapper = new ObjectMapper();
        
        Scanner in = new Scanner(System.in);
        
        for (;;)
        {
            // generate message to server from user
            String input  = in.nextLine();
            if ("quit".equals(input)) break;
            // send request to server
            sendToServer.writeUTF(input);
            sendToServer.flush();
            
            // do something with rhe response
            String response = recievedFromServer.readUTF();
            System.out.println(response);
        }
        
    }   return null; }
}

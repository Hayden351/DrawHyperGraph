package deprecated;

import deprecated.DrawGraphAPI;
import deprecated.ApiState;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * @author Hayden Fields
 * 
 * Usage of a cli
 * 
 * if invoked with a command, runs the command
 * 
 * if invoked with no command an only flags then will take 1 or more commands 
 * until the termination command is given
 * 
 */
public class DrawGraphCLI
{
    /** 
     * mutate the graph
     * mutate the window
     * mutate sub elements of the window
     * 
     * 
     * 
     * Add/remove elements from the Graph data structure
     * 
     * Change every command line configurable element
     * 
     * We have a window (width, height, camera location)
     * 
     * The application assigns a location to every vertex
     */
    public static void main (String[] args)
    {
        // addVertex asdf
        if (args.length == 0) // interpreter
        {
            try
            { 
                // Create an object of the interface 
                // implementation class 
                DrawGraphAPI obj = new ApiState(); 

                // rmiregistry within the server JVM with 
                // port number 1900 
                LocateRegistry.createRegistry(1900); 

                Naming.rebind("rmi://localhost:1900/jvmrmi", obj);
            } 
            catch(Exception ae) 
            { 
                System.out.println(ae); 
                return;
            }
        }
        else ; // run 1 command
    }
}

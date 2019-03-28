package deprecated;

import deprecated.Command;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Hayden Fields
 * Ok so heres the plan, any arbitrary application just wants to worry about the
 * application state and not about how someone might want to issue a whole
 * bunch of command line arguments to the application
 * 
 * Also if I had fine grain control of what was displayed then I might start
 * caring about having an abstract application that when triggered drew the
 * graph to the screen. Has a gui or a command line interface
 * 
 * 
 * issue a sequence of cli commands, along with a predifined or supplied initial
 * 
 * cli commands when
 * program is not running
 * program is running
 * not sure if it makes sense to generalize how to design a program and its cli
 * 
 * since the reason for the design here is very clearly the use case that we
 * want to be able to mutate the graph while the program is running and if you
 * can do something with a gui then you should be able to do it via the command 
 * line
 * 
 */
public interface DrawGraphAPI extends Remote
{
    public Command nextCommand() throws RemoteException;
    
    public boolean addVertex(String vertexLabel)throws RemoteException;
    
    public String queryGraphState() throws RemoteException;
}

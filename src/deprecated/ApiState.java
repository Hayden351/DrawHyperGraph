package deprecated;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author Hayden Fields
 */
public class ApiState extends UnicastRemoteObject implements DrawGraphAPI
{
    
    public Queue<Command> commands = new ConcurrentLinkedDeque<>();
    public Queue<String> returnMessage = new ConcurrentLinkedDeque<>();
    
    public ApiState() throws RemoteException
    {
        super();
    }
    
    @Override
    public Command nextCommand() throws RemoteException
    {
        if (commands.isEmpty())
            return null;
        else
            return commands.poll();
    }
    
    
    // should this happen here? Or should all the logic be placed in the draw
    // graph aplication?
    @Override
    public boolean addVertex (String vertexLabel) throws RemoteException
    {
        return true;
    }

    
    // TODO: this is a bad solution
    @Override
    public String queryGraphState () throws RemoteException
    {
        while (returnMessage.isEmpty())
            ;
        
        return returnMessage.poll();
    }
}

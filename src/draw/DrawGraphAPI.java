package draw;

import java.rmi.Remote;

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
 */
public interface DrawGraphAPI extends Remote
{
    public boolean addVertex(String vertexLabel);
}

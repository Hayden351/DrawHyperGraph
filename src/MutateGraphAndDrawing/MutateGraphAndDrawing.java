package MutateGraphAndDrawing;

import definition.Graph;
import definition.GraphProperties;
import draw.DrawHyperGraph;
import java.util.Arrays;

/**
 * @author Hayden Fields
 */
public class MutateGraphAndDrawing
{
    public static void main (String[] args)
    {
        // We can use reflection to invoke menthods to add/remove/modify elemets of the graph
        // a graph just constists of vertices and edges and corresponding miscellaneous data for those edges and vertices
        Graph G = new Graph();
        
//        G.addAllVertices(list)

        // graph properties to ask questions about graphs
        // we want to mutate DrawHyperGraph
        Arrays.asList(GraphProperties.class);
        
        Class<?> cls = DrawHyperGraph.class;
        
        
        
        
    }
}

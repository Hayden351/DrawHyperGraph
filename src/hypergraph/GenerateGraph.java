package hypergraph;

import static java.util.Arrays.asList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Hayden Fields
 */
public class GenerateGraph
{
    public static Graph generate0()
    {
        Graph G = new Graph();

        G.addVertex("0");
        G.addVertex("1");
        G.addVertex("2");
        G.addVertex("3");
        G.addVertex("4");
        G.addVertex("5");
        G.addVertex("6");
        TreeMap<Vertex, Boolean> orientation1 = new TreeMap<>();
        TreeSet<Vertex> subset = new TreeSet(G.vertices.headSet(G.vertices.descendingIterator().next()));
        for (Vertex v : subset) {
            orientation1.put(v, true);
        }
        for (int i = 0; i < 1000; i += 50) {
            G.addEdge(String.format("%03d", i), G.vertices, orientation1);
        }

        TreeMap<Vertex, Boolean> orientation = new TreeMap<>();
        orientation.put(G.vertices.iterator().next(), true);
        G.addEdge("15", new TreeSet<>(asList(G.vertices.iterator().next(), G.vertices.descendingIterator().next())), orientation);
        return G;
    }
    public static Graph randomGraph(int numberOfVertices, int numberOfEdges)
    {
        Graph G = new Graph();
        for (int i = 0; i < numberOfVertices; i++)
            G.addVertex(Integer.toString(i));
         for (int i = 0; i < numberOfEdges; i++)
            G.addEdge("", RandomUtils.randomSubSet(G.vertices), RandomUtils.randomMap(G.vertices));
        
        return G;
    }
    
//    public static Graph randomGraph(int numberOfVertices, int numberOfEdges)
//    {
//        Graph G = new Graph();
//        
//        for (int i = 0; i < numberOfVertices; i++)
//            G.addVertex(Integer.toString(i));
//        for (int i = 0; i < numberOfEdges; i++)
//            G.addEdge("", RandomUtils.randomSubSet(G.vertices), RandomUtils.randomMap(G.vertices));
//        return G;
//    }
}

package hypergraph;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Hayden Fields
 */
public class Graph
{
    public TreeSet<Vertex> vertices;
    public TreeSet<Edge> edges;

    public Graph()
    {
        vertices = new TreeSet<>();
        edges = new TreeSet<>();
    }

    public Vertex addVertex(String label)
    {
        Vertex v = new Vertex(label);
        vertices.add(v);
        return v;
    }

    public Edge addEdge(String label, TreeSet<Vertex> vertices)
    {
        return addEdge(label, vertices, new TreeMap<>());
    }

    public Edge addEdge(String label, Set<Vertex> vertices, Map<Vertex, Boolean> orientation) 
    {
        Edge e = new Edge(label, new TreeSet<>(vertices), orientation);
        edges.add(e);
        return e;
    }

    public Iterable<Vertex> vertices() {
        return vertices;
    }

    public Iterable<Edge> edges() {
        return edges;
    }
}

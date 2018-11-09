package hypergraph;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Hayden Fields
 */
public class Edge implements Comparable<Edge>
{
    private static int edgeId = 0;
    public static class OrientedVertex implements Comparable<OrientedVertex>
    {
        Vertex v;
        boolean orientedToV;

        public OrientedVertex(Vertex v, boolean towards)
        {
            this.v = v;
            this.orientedToV = towards;
        }

        @Override
        public int compareTo(OrientedVertex v)
        {
            return this.v.compareTo(v.v);
        }
    }

    public final int uniqueId;

    public String label;
    public TreeMap<Vertex, Boolean> vertices;

    public Edge()
    {
        this("");
    }

    public Edge(String label)
    {
        this(label, new TreeSet<Vertex>());
    }

    public Edge(String label, Set<Vertex> vertices) 
    {
        this(label, vertices, new TreeMap<Vertex, Boolean>());
    }

    public Edge(String label, Set<Vertex> verticesIn, Map<Vertex, Boolean> orientation)
    {
        uniqueId = edgeId++;

        this.label = label;
        this.vertices = new TreeMap<>();
        Set<Vertex> vertices = new TreeSet<>(verticesIn);
        
        for (Vertex v : vertices)
            if (orientation.containsKey(v))
                this.vertices.put(v, orientation.get(v));
            else
                this.vertices.put(v, false);
    }

    public Iterable<Vertex> vertices()
    {
        return new TreeSet<>(vertices.keySet());
    }

    public boolean sameConnections(Edge e)
    {
        return e.vertices.keySet().equals(vertices.keySet());
    }

    public boolean orientedTowards(Vertex v)
    {
        return vertices.get(v);
    }

    @Override
    public int compareTo(Edge e)
    {
        return this.uniqueId - e.uniqueId;
    }
}


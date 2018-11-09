package hypergraph;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * @author Hayden Fields
 */
public class Vertex implements Comparable<Vertex>
{
    private static int vertexId = 0;
    
    public final int uniqueId;
    public String label;

    public Vertex()
    {
        this("");
    }

    public Vertex(String label)
    {
        uniqueId = vertexId++;
        this.label = label;
    }

    @Override
    public int compareTo(Vertex v)
    {
        return comp.compare(this, v);
    } public static Comparator<Vertex> comp = (u, v) ->
    {
        return u.uniqueId - v.uniqueId;
    };
    
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Vertex))
            return false;
        
        Vertex v = (Vertex)obj;
        return equality.test(this, v);
    } public static BiPredicate<Vertex, Vertex> equality = (u, v) -> 
    {
        return u.uniqueId == v.uniqueId
               && u.label.equals(v.label);
    };
    
    @Override
    public int hashCode()
    {
        return hashCode.apply(this);
    } public static Function<Vertex, Integer> hashCode = v ->
    {
        int hash = 7;
        hash = 31 * hash + v.uniqueId;
        hash = 31 * hash + Objects.hashCode(v.label);
        return hash;
    };
    
}
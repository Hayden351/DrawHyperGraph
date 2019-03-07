package DrawGraph;

import java.util.ArrayList;
import Enumerate.Generate;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;
/**
 *
 * @author Hayden
 */
public class Paths 
{
    public static void main(String[] args)
    {
        
        LinkedList<String> a = new LinkedList<>(null);
        String b = "";
        String d = b;
        
        String c = new String(b);
        
    }
    
    public static void doThings()
    {
        //        Graph G = GenerateGraph.generateGraphFromMatrix(
//              /*  0 1 2 3 4 5 */
//         /* 0 */ "0 1 1 0 0 0\n" +
//         /* 1 */ "0 0 1 1 0 0\n" +      
//         /* 2 */ "0 0 0 1 1 0\n" +      
//         /* 3 */ "0 0 0 0 1 1\n" +      
//         /* 4 */ "0 0 0 0 0 1\n" +      
//         /* 5 */ "0 0 0 0 0 0\n"       
//        );
//        
//        Vertex v = G.vertices.get(0);
//        Vertex u = G.vertices.get(3);
////        System.out.println(G.getIncidentEdges(G.vertices.get(1)));
        
        Graph G = GenerateGraph.dAryNCube(3,3);
        System.out.println(G);
        Vertex v = null;
        Vertex u = null;
        for (Vertex r : G.vertices)
        {
            if (r.message.equals("0 0 0"))
                v = r;
            if (r.message.equals("0 0 0"))
                u = r;
        }
        ArrayList<ArrayList<Vertex>> paths = findAllPaths(G,v,u);
        paths.removeIf(x -> x.size() <= 26);
        for (ArrayList<Vertex> path : paths)
        {
            System.out.println(path);
        }
        //allVertexPaths(G,v,u);
        //allEdgePaths(G,v,u);
    }
    
    public static boolean traverseVertexPath(Graph G, ArrayList<Vertex> path)
    {
        // for each vertex
        for (int i = 0; i < path.size() - 1; i++)
        {
            Vertex v = path.get(i);
            //System.out.println(G.getIncidentEdges(v));
            
            // if we can get to the next vertex from this vertex
            boolean exists = false;
            for (Edge e : G.getIncidentEdges(path.get(i)))
                if (e.contains(path.get(i+1)))
                    exists = true;
            if (!exists)
                return false;
        }
        return true;
    }
    public static void traverseEdgePath(Graph G, ArrayList<Edge> path)
    {
    }
    public static ArrayList<ArrayList<Vertex>> allVertexPaths(Graph G, Vertex v, Vertex u)
    {
        ArrayList<ArrayList<Vertex>> paths = Generate.subLists(G.vertices);
        if (v == u)
            paths.forEach(x -> x.add(v));
        paths.removeIf(x -> !(x.size() >= 2));
        paths.removeIf(x -> !(x.get(0) == v && x.get(x.size() - 1) == u));
        paths.removeIf(x -> (x.size() >= 2 && x.get(1) == x.get(x.size() - 2)));
        paths.removeIf(x -> !(traverseVertexPath(G,x)));
        //paths.removeIf(x -> !new HashSet<>(G.edges).equals(new HashSet<>(getEdgesFromVertexPath(G,x))));
        
        return paths;
    }
    
    public static ArrayList<Edge> allEdgePaths(Graph G, Vertex v,Vertex u)
    {
        ArrayList<ArrayList<Edge>> paths = Generate.powerSet(G.edges);
        for (ArrayList<Edge> path : paths)
            System.out.println(path);
        paths.removeIf(x -> !x.isEmpty() && !(x.get(0).contains(v) && x.get(x.size() - 1).contains(v)));
        for (ArrayList<Edge> path : paths)
            System.out.println(path);
        return null;
    }
    
    public static ArrayList<Edge> getEdgesFromVertexPath(Graph G, ArrayList<Vertex> path)
    {
        ArrayList<Edge> edgePath = new ArrayList<>();
        for (int i = 0; i < path.size()-1;i++)
        {
            Edge e = GraphUtil.findEdge(G,path.get(i),path.get(i+1));
            
            // TODO: maybe this should be caught somewhere else?
            if (e == null)
                return null; // edge doesn't exist is not even a path
            edgePath.add(e);
        }
        return edgePath;
    }

    public static ArrayList<ArrayList<Vertex>> findAllPaths(Graph G, Vertex u, Vertex v)
    {
        ArrayList<ArrayList<Vertex>> paths = new ArrayList<>();
        findAllPathsAux(G,u,v,new ArrayList<>(), new HashSet<>(), paths);
        return paths;
        
    }
    
    private static void findAllPathsAux(Graph G, Vertex u, Vertex v,ArrayList<Vertex> accPath,HashSet<Vertex> visted, ArrayList<ArrayList<Vertex>> paths)
    {
        if (u == v && accPath.size() >= 1) // we done
        {
            accPath.add(v);
            paths.add(accPath);
        }
        else
        {
            // its not the case that we found our node maybe a neighbor leads to it
           for (Vertex r : GraphUtil.getNeighbors(G, u))
           {
               HashSet<Vertex> newVisted = new HashSet<>(visted);
               // if we already visted the node then is a cycle
               if (newVisted.add(u))
               {
                   ArrayList<Vertex> newAccPath = new ArrayList<>(accPath);
                   newAccPath.add(u);
                   findAllPathsAux(G,r,v,newAccPath,newVisted,paths);
               }
           }
        }
    }
}

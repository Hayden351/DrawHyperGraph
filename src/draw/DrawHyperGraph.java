package draw;

import processing.core.*;

import java.util.TreeMap;
import static java.util.Arrays.asList;

import java.util.ArrayList;

import definition.Graph;
import definition.Vertex;
import definition.Edge;
import definition.GenerateGraph;
import definition.GraphProperties;
import java.awt.Color;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class DrawHyperGraph extends PApplet
{
    // track the locations of vertices in the drawing
    public static TreeMap<Vertex, PVector> vertexLocations;
    // track which vertex is currently being moved by the mouse
    public static Vertex held;
    public static List<Graph> graphs = new ArrayList<>();
    public static Graph G;

    // TODO: there is a lookup for global variables for
    @CommandLineConfigurable(description="Will increase the size of the ellipse drawn.")
    private static float vertexRadius = 30;
    
    @CommandLineConfigurable(description="Will increase the width of the triangles that are at the end of a directed edges.")
    private static float headingWidth = 10;
    
    @CommandLineConfigurable(description="Will increase the height of the triangles that are at the end of a directed edges.")
    private static float headingHeight = 15;
    
    @CommandLineConfigurable(description="The width of the window that contains the drawing.")
    private static int initialWidth;
    
    @CommandLineConfigurable(description="The height of the window that contains the drawing.")
    private static int initialHeight;

    @CommandLineConfigurable(description="When edge offset factor when edges are overlapped.")
    public static float edgeOffset = (1 + sqrt(5)) / 2;
    
    public static boolean noOutOfBounds = false;
    
    // TODO: maybe add functionality to command line configurable
    public static boolean fullscreen = false;
    
    private static Color baseVertexColor;
    
    public static boolean tree = false;
    
    public static float PHI = (1 + sqrt(5)) / 2;

// TODO: change font maybe
    PFont font;
    @Override
    public void settings()
    {
        size(displayWidth, displayHeight);
    }   

    @Override
    public void setup ()
    {
        // TODO: don't allow resizing until we can support it
        //surface.setResizable(true);
        if (fullscreen)
        {
            fullScreen();
//            surface.setSize(displayWidth, displayHeight);
//            width = displayWidth;
//            height = displayHeight;
        }
        else
            surface.setSize(initialWidth, initialHeight);
        
        
        // set each edge to not be directed towards any of its vertices
        if (!directed)
            graphs.forEach(graph -> graph.edges().forEach(edge -> edge.vertices.replaceAll((v, b) -> false)));
        
        
        // initially no vertex is being manipulated
        held = null;

        // assign a random location to each vertex in the graph
        vertexLocations = new TreeMap<>();
        
        // draws as a rooted tree
        
        // start at center
        if (tree)
        {
            Set<Vertex> verticesWithNoParents = new HashSet<>(G.vertices);
            for (Iterator<Vertex> it = verticesWithNoParents.iterator(); it.hasNext();)
            {
                Vertex v = it.next();
                for (Edge e : G.edges)
                    if (e.orientedTowards(v))
                    {
                        it.remove();
                        break;
                    }
            }
//            Vertex src = verticesWithNoParents.iterator().next(); // src is root
            
            // initialize visited map
            Map<Vertex, Boolean> visited = new HashMap<>(); // TODO: can replace with vertexLocations
            for (Vertex v : G.vertices)
                visited.put(v, Boolean.FALSE);
            
            Queue<Vertex> vertexQueue = new LinkedList<>();
            
            for (Vertex src : verticesWithNoParents)
            {
                vertexQueue.add(src);
                visited.put(src, Boolean.TRUE);
                vertexLocations.put(src, new PVector(vertexRadius *2, vertexRadius * 2));
            }
            
            while (!vertexQueue.isEmpty())
            {
                Vertex v = vertexQueue.remove();
                System.out.printf("%s :", v);
                
                Collection<Vertex> neighbors = GraphProperties.getNeighbors(G, v);
                
                int j = 0;
                for (Vertex u : neighbors)
                {
                    System.out.printf(" %s", u);
                    
                    if (!visited.get(u))
                    {
                        // want to head 4 vertex radii away
                        // there are n vertcies
                        // r = (4v / n) distance each iteration
                        // average theta = 0; // use average theta for direction afterwards
                        // for v : V(G)
                        //     theta = angle from vector from vertex to initial posiiton
                        //     posiiton += (r * cos(theta), r * sin(theta))
                        // if overlapped travel vertex radius distance in average theta
                        // if hit edge then bounce
                        vertexQueue.add(u);
                        visited.put(u, Boolean.TRUE); // after vertex is visited we place it on the canvas
                        // TODO: check location befoer putting it if overlap 
                        //       find average angle away then move/bounce
                        float rad = 2 * vertexRadius / vertexLocations.keySet().size();
                        
                        // initial position is the parent
                        // for each vertex r that already has a location
                        // move it a little away from r
                        float angleFromParent = 3 * PI / 4 * j++ / neighbors.size();
                        PVector placementLocation = vertexLocations.get(v).copy().
                                                        add(PVector.fromAngle(angleFromParent).
                                                        mult(2 * vertexRadius));
                        
//                        for (Vertex r : vertexLocations.keySet())
//                        {
//                            float angleBetween = angleBetween(vertexLocations.get(r), placementLocation);
////                            System.out.println(angleBetween);
//                            placementLocation.add(PVector.fromAngle(angleBetween).normalize().mult(rad));
//                        }
                        
                        int giveUp = width * height;
                        while (isLocationAwayFrom(placementLocation, vertexLocations, vertexRadius * 3))
                        {
                            PVector newLocation  = placementLocation.copy().add(new PVector(5 * cos(angleFromParent), 5 * sin(angleFromParent)));
  
                            // move or change direction
                            if (!(0 <= newLocation.x && newLocation.x <= width && rad <= newLocation.y && newLocation.y <= height))
                              angleFromParent += (2 * PI / PHI);
                            else
                              placementLocation = newLocation;
                            if (giveUp-- <= 0)
                                break;
                        }
                        vertexLocations.put(u, placementLocation);
                        
                    }
                }
                System.out.println("");
            }
            
            // move back into bounds
            for (Vertex v : G.vertices)
                vertexLocations.put(v, new PVector(
                            constrain(vertexLocations.get(v).x, vertexRadius + 1, width - vertexRadius - 1),
                            constrain(vertexLocations.get(v).y, vertexRadius + 1, height - vertexRadius - 1)));
        }
        else 
            for (Vertex v : G.vertices())
                vertexLocations.put
                (
                    v, // ->
                    new PVector
                    (
                        random(vertexRadius + 1, width - vertexRadius - 1),
                        random(vertexRadius + 1, height - vertexRadius - 1)
                    )
                );
    }
    
    // i don't know maybe this will help
    // https://www.gamedev.net/forums/topic/594055-zooming-onto-an-arbitrary-point/
    public void keyPressed()
    {
        switch (key) // the illusion of zooming in/out
        {
            case 'w': // zoom in
            {
                zoomingIn = true;
            } break;
            case 's': // zoom out
            {
                zoomingOut = true;
            } break;
        }
        switch (keyCode)
        {
            case UP:
            {
                movingUp = true;
            } break;
            case RIGHT:
            {
                movingRight = true;
            } break;
            case DOWN:
            {
                movingDown = true;
            } break;
            case LEFT:
            {
                movingLeft = true;
            } break;
        }
    }
    boolean zoomingIn = false;
    boolean zoomingOut = false;
    boolean movingUp = false;
    boolean movingRight = false;
    boolean movingDown = false;
    boolean movingLeft = false;
    public void keyReleased() 
    {
        switch (key) // the illusion of zooming in/out
        {
            case 'w': // zoom in
            {
                zoomingIn = false;
            } break;
            case 's': // zoom out
            {
                zoomingOut = false;
            } break;
        }
        if (keyCode == UP)
            movingUp = false;
        if (keyCode == RIGHT)
            movingRight = false;
        if (keyCode == DOWN)
             movingDown = false;
        if (keyCode == LEFT)
            movingLeft = false;
    }
    public void handleInput()
    {
        if (zoomingIn)
        {
            vertexRadius -= .125f;
            if (vertexRadius <= 5)
                vertexRadius = 5;
            else
                for (Vertex v : vertexLocations.keySet())
                    vertexLocations.get(v).add(PVector.fromAngle(angleBetween(vertexLocations.get(v), new PVector(width / 2, height / 2))).normalize().mult(.25f));
        }
        else if (zoomingOut)
        {
            vertexRadius += .125f;
            if (vertexRadius >= (width + height) / 8)
                vertexRadius = (width + height) / 8;
            else 
                for (Vertex v : vertexLocations.keySet())
                    vertexLocations.get(v).add(PVector.fromAngle(angleBetween(new PVector(width / 2, height / 2), vertexLocations.get(v))).normalize().mult(.25f));
        }
        
        float movementSpeed = 2;
        if (movingUp)
            for (Vertex v : vertexLocations.keySet())
                vertexLocations.get(v).add(PVector.fromAngle(3 * PI / 2).normalize().mult(movementSpeed));
        if (movingRight)
            for (Vertex v : vertexLocations.keySet())
                vertexLocations.get(v).add(PVector.fromAngle(0).normalize().mult(movementSpeed));
        if (movingDown)
            for (Vertex v : vertexLocations.keySet())
                vertexLocations.get(v).add(PVector.fromAngle(PI / 2).normalize().mult(movementSpeed));
        if (movingLeft)
            for (Vertex v : vertexLocations.keySet())
                vertexLocations.get(v).add(PVector.fromAngle(PI).normalize().mult(movementSpeed));
    }

    @Override
    public void draw ()
    {
        background(255);

        handleInput();
        
        // draw vertices
        for (Vertex v : G.vertices())
        {
            PVector vLoc = vertexLocations.get(v);

            vertexSettings();
            ellipse(vLoc.x, vLoc.y, 2 * vertexRadius, 2 * vertexRadius);

            vertexTextSettings();
            text(v.label, vLoc.x, vLoc.y);
        }

        // find all overlapping edges
        // first separate the graph into distinct classes
        // where each class is of edges that have the same set of vertices they connect
        ArrayList<ArrayList<Edge>> equivalenceClasses = new ArrayList<>();
        
        // membership is transitive so only have to test the first element
        BiPredicate<Edge, ArrayList<Edge>> isIn = (e, edgeClass) -> 
                edgeClass.get(0).sameConnections(e);
        
        G.edges().forEach // matches an edge into a edge class
        ( edge ->   equivalenceClasses.stream().
                    filter(edgeClass -> isIn.test(edge, edgeClass)).
                    findFirst().orElseGet(() -> 
                    {   // create new edge class since one doesn't exist
                        ArrayList<Edge> newEdgeClass = new ArrayList<>();
                        equivalenceClasses.add(newEdgeClass);
                        return newEdgeClass;
                    }).
                    add(edge)
        );
        
        // draw each edge such that they are offset from the previous edge
        for (ArrayList<Edge> edgeClass : equivalenceClasses)
        {
            // draw each edge offset from the other edges in the class
            /* TODO:
            i = 0 the 0th edge is very close to e 1th edge, however
            i = 1 there is no edge that is at the center of the vertices
            */
            int i = edgeClass.size() <= 3 ? 0 : 1; 
            for (Edge e : edgeClass)
                drawEdge(e, i++);
        }

        // move held vertex to the mouse
        if (held != null)
            vertexLocations.put(held, new PVector(mouseX, mouseY));
        if (noOutOfBounds)
            for (Vertex v : G.vertices) // TODO: there is a much nicer way of doing this using map methods
                vertexLocations.put(v, new PVector(
                    constrain(vertexLocations.get(v).x, vertexRadius + 1, width - vertexRadius - 1),
                    constrain(vertexLocations.get(v).y, vertexRadius + 1, height - vertexRadius - 1)
                ));
    }

    public void drawEdge(Edge e, int i)
    {
        // the center of a edge is the average of all the positions of all the
        // positions of the vertices that are connected by this edge
        PVector edgeCenter = e.vertices().stream().
                                map(v -> vertexLocations.get(v)).
                                reduce
                                (
                                    new PVector(0, 0), 
                                    (a,b) -> new PVector(a.x + b.x, a.y + b.y)
                                ).
                                div(e.vertices().size());

        // add an offset to the center so edges that connect the same vertices are not
        // covering eachother
        float angle = (i * 2 * PI / (1 + edgeOffset));
        float dist = i * vertexRadius / 2; // arbitrary distance
        PVector offset = new PVector(cos(angle) * dist, sin(angle) * dist);
        edgeCenter = edgeCenter.add(offset);

        // draw a line from the center of the edge to each vertex that the
        // edge connects
        for (Vertex v : e.vertices())
        {
            PVector vLoc = vertexLocations.get(v).copy();

            // end starts at the edges of the vertex or further if oriented
            // TODO: add generic support for different stroke sizes
            // end + 1 to account for stroke size
            PVector end = edgeCenter.copy().
                          sub(vLoc).
                          normalize().
                          mult((vertexRadius + 1) + (e.orientedTowards(v)?headingHeight:0));

            edgeSettings(); // set settings for drawing an edge line
            line(edgeCenter.x, edgeCenter.y, vLoc.x + end.x, vLoc.y + end.y);

            // draw heading if oriented towards
            if (e.orientedTowards(v))
                drawHeading(vLoc.copy().add(end), atan2(-end.y, -end.x), headingWidth, headingHeight);

            edgeTextSettings(); // set setting for drawing the label of an edge
            text(e.label, edgeCenter.x, edgeCenter.y);
        }
    } // end drawEdge ()

    public void drawHeading(PVector startHeading, float angle, float headingWidth, float headingHeight) {
        PVector leftPoint = startHeading.copy().add(PVector.fromAngle(angle + PI / 2).mult(headingWidth / 2));
        PVector rightPoint = startHeading.copy().add(PVector.fromAngle(angle - PI / 2).mult(headingWidth / 2));
        PVector forwardPoint = startHeading.copy().add(PVector.fromAngle(angle).mult(headingHeight));

        headingSettings();

        line(startHeading.x, startHeading.y, leftPoint.x, leftPoint.y);
        line(startHeading.x, startHeading.y, rightPoint.x, rightPoint.y);
        line(leftPoint.x, leftPoint.y, forwardPoint.x, forwardPoint.y);
        line(rightPoint.x, rightPoint.y, forwardPoint.x, forwardPoint.y);
    }
    
    public float angleBetween (PVector start, PVector end)
    {
        return atan2(end.y - start.y, end.x - start.x);
    }

    @Override
    public void mousePressed ()
    {
        // hold vertex that mouse is hovering over
        for (Vertex v : G.vertices())
        {
            PVector vLoc = vertexLocations.get(v);
            if (Utils.distance(mouseX, mouseY, vLoc.x, vLoc.y) <= vertexRadius)
            {
                held = v;
                return;
            }
        }
    }

    @Override
    public void mouseReleased ()
    {
        held = null;
    }

    public void headingSettings ()
    {
        noFill();
        stroke(0);
    }

    public void vertexSettings ()
    {
        noFill();
        stroke(baseVertexColor.getRed(), baseVertexColor.getGreen(),
            baseVertexColor.getBlue());
    }

    public void vertexTextSettings ()
    {
        fill(0, 0, 255);
        textSize(10);
        textAlign(CENTER);
    }

    public void edgeSettings ()
    {
        noFill();
        stroke(0);
    }

    public void edgeTextSettings ()
    {
        fill(255, 0, 0);
        textSize(14);
        textAlign(CENTER);
    }

    // TODO: this naming convention makes no sense
    // TODO: can change to enums for efficiency
    public static Class translate (String type)
    {
        switch (type)
        {
            case "String": return String.class;
            case "int": case "Integer": return Integer.class;
            case "float": case "Float": return Float.class;
            case "double": case "Double": return Double.class;
            default: return null;
        }
    }
    public static Object stringToType (String type, String value)
    {
        switch (type)
        {
            case "String": return value;
            case "int": case "Integer": return Integer.parseInt(value);
            case "float": case "Float": return Float.parseFloat(value);
            case "double": case "Double": return Double.parseDouble(value);
            default: return null;
        }
    }
    
    private static Object convertArg (Class<?> type, String arg)
    {
        final String a = Integer.class.getCanonicalName();
        switch (type.getCanonicalName())
        {
            case "java.lang.String": return arg;
            case "int": case "java.lang.Integer": return Integer.parseInt(arg);
            case "float": case "java.lang.Float": return Float.parseFloat(arg);
            case "double": case "java.lang.Double": return Double.parseDouble(arg);
        }
        return null;
    }
    
    // TODO: move and maybe 
    public static boolean directed = true;
    public static void main (String[] args)
    {
        baseVertexColor = new Color(0,0,0);
        String[] appletArgs = new String[]{"draw.DrawHyperGraph"};
        
        if (!(args == null || args.length == 0))
        {
            // program is about drawing a graph so the main command line arguments
            // will be a GenerateGraph function and its parameters through reflection
            // TODO: each flag gets the next collection of args as its args
            int i = 0;
            for (; i < args.length && args[i].startsWith("-"); i++)
            {
                String flag = args[i];
                switch (flag)
                {
                    case "-fullscreen":
                    {
                        appletArgs= new String[]{"--present", "--window-color=#666666", "--hide-stop", "draw.DrawHyperGraph"};
                        fullscreen = true;
                    } break;
                    case "-vertexColor":
                    {
                        baseVertexColor = new Color
                            (
                                Integer.parseInt(args[++i]),
                                Integer.parseInt(args[++i]),
                                Integer.parseInt(args[++i])
                            );
                    } break;
                    case "-size":
                    {
                        // TODO: replace parse int with a state machine
                        //       just for funzies
                        // wat do when there are not enough arguments
                        // or arguments are not valid natural numbers
                        initialWidth = Integer.parseInt(args[++i]);
                        initialHeight = Integer.parseInt(args[++i]);
                    } break;
                    case "-undirected":
                    {
                        directed = false;
                    } break;
                    case "-tree":
                    {
                        tree = true;
                    } break;
                    default:
                    {
                        flag = flag.substring(1);
                        boolean flagTripped = false;
                        
                        try
                        {
                            
                        for (Field f : DrawHyperGraph.class.getDeclaredFields())
                            if (flag.equals(f.getName()) && null != f.getDeclaredAnnotation(CommandLineConfigurable.class))
                            {
                                f.set(null, convertArg(f.getType(), args[++i]));
                                flagTripped = true;
                                break;
                            }
                        }
                        catch (IllegalArgumentException | IllegalAccessException ex) 
                        {
                            ex.printStackTrace();
                            // TODO: wat do
                            return;
                        }
                        // TODO: make error handling not garbage
                        if (!flagTripped)
                        {
                            System.out.printf("Unrecognized flag %s\n", flag);
                            // https://stackoverflow.com/questions/11158235/get-name-of-executable-jar-from-within-main-method
                            // get name of executable if i really want it
                            String debugArgs = String.join(" ", args);
                            System.out.println(debugArgs);
                            StringBuilder debugArrow = new StringBuilder();
                            
                            for (int index = 1; index <= i; index++)
                                debugArrow.append(Utils.repititionsOf(" ", args[index - 1].length())).append(" ");
                            debugArrow.append("^");
                            System.out.println(debugArrow);
                            
                            
                            System.out.println("\nValid flags:");
                            for (Field f : DrawHyperGraph.class.getDeclaredFields())
                            {
                                CommandLineConfigurable config = f.getAnnotation(CommandLineConfigurable.class);
                                if (config != null)
                                    System.out.printf("-%s : %s\n", f.getName(), config.description());
                            }
                            throw new IllegalArgumentException("Argument not reconginzed");
                        }
                    } break;
                }
            }
            // there should be no flag errors after this point
            
            // TODO: error message when no GenerateGraph function is specified
            if (i >= args.length)
                return;
            // given n argments the n - 1 - 2k arg will be a k arity function
            // that produces a graph, n - 2k to n is k (type, identifier)
            // parameter pairs
            String methodName = args[i++];
            int numberOfArgs = (args.length - i) / 2;
            Class[] methodArgTypes = new Class[numberOfArgs];
            Object[] methodArgs = new Object[numberOfArgs];
            for (int j = 0; i < args.length - 1; i += 2, j++)
            {
                methodArgTypes[j] = translate(args[i]);
                methodArgs[j] = stringToType(args[i], args[i + 1]);
            }
            
            try
            {
                Method graphGeneratingMethod = GenerateGraph.class.getDeclaredMethod(methodName, methodArgTypes);
                if (graphGeneratingMethod.getReturnType().equals(graphs.getClass()))
                {
                    graphs = (List<Graph>)graphGeneratingMethod.invoke(null, methodArgs);
                    G = graphs.get(0);
                }
                else
                {
                    G = (Graph)graphGeneratingMethod.invoke(null, methodArgs);
                    graphs.add(G);
                }
            }
            catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException ex)
            {
                // if generation of graph fails then printout valid methods
                System.out.printf("Invalid method %s, valid methods:\n", methodName);
                for (Method m : GenerateGraph.class.getDeclaredMethods())
                    // A.containsAll(B) iff B subseteq A
                    if (new HashSet<>(asList(Integer.class, int.class, float.class, Float.class, boolean.class, Boolean.class, String.class)).containsAll(new HashSet<>(asList(m.getParameterTypes()))))
                        if (!m.getName().startsWith("lambda$")) // filter out lambdas
                            System.out.printf("%s.%s : (%s) -> %s\n", GenerateGraph.class.getName(), m.getName(), 
                                String.join(" * ",
                                        new ArrayList<>(
                                        asList(m.getParameterTypes())).
                                        stream().map(x -> x.getTypeName()).
                                        collect(Collectors.toList())),
                                            m.getReturnType().getTypeName());
                return;
            } catch (InvocationTargetException ex)
            {
                ex.getCause().printStackTrace();
                return;
            }
            
        }
        else // no arguments given
        {
            System.out.println("Usage:\n<executable> <config> <generate-graph-function> <function-arguments>");
            return;
        }

        PApplet.main(appletArgs);
       
//        PApplet.main(new String[] { "SaveMenu" });
    } // end main ()

    // is true if the given location is away from the given locations by at least
    // distance amount
    private boolean isLocationAwayFrom (PVector location, Map<Vertex, PVector> locations, float distance)
    {
        for (Vertex u : locations.keySet())
            if (distance(location, locations.get(u)) <= distance)
                return true;
        return false;
    }

    private float distance (PVector vLoc, PVector uLoc)
    {
        return Utils.distance(vLoc.x, vLoc.y, uLoc.x, uLoc.y);
    }
} // end class DrawHyperGraph


// -vertexColor 255 40 60 -fullscreen -vertexRadius 60 -size 400 200 randomGraph int 8 int 4
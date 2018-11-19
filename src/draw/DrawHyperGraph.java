package draw;

import processing.core.*;

import java.util.TreeMap;
import static java.util.Arrays.asList;

import java.util.ArrayList;

import definition.Graph;
import definition.Vertex;
import definition.Edge;
import definition.GenerateGraph;
import java.awt.Color;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DrawHyperGraph extends PApplet
{
    // track the locations of vertices in the drawing
    public static TreeMap<Vertex, PVector> vertexLocations;
    // track which vertex is currently being moved by the mouse
    public static Vertex held;
    List<Graph> graphs = new ArrayList<>();
    public static Graph G;

    // TODO: there is a lookup for global variables for
    @CommandLineConfigurable(description="Will increase the size of the ellipse drawn.")
    private static int vertexRadius = 30;
    
    @CommandLineConfigurable(description="Will increase the triangle width of the arrow at the end of a directed edge.")
    private static int headingWidth = 10;
    
    @CommandLineConfigurable(description="Will increase the triangle height of the arrow at the end of a directed edge. ")
    private static int headingHeight = 15;
    
    @CommandLineConfigurable(description="The width of the windows that contains the drawing.")
    private static int initialWidth = 1000;
    @CommandLineConfigurable(description="The height of the windows that contains the drawing.")
    private static int initialHeight = 1000;

    @CommandLineConfigurable(description="When edges are overlapped, TODO: changing this value does something when edges are overlapped")
    public static float edgeOffset = (1 + sqrt(5)) / 2;
    
    // TODO: maybe add functionality to command line configurable
    public static boolean fullscreen = false;
    
    private static Color baseVertexColor;

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
        surface.setResizable(true);
        if (!fullscreen)
            surface.setSize(initialWidth, initialHeight);
        
        // initially no vertex is being manipulated
        held = null;

        // assign a random location to each vertex in the graph
        vertexLocations = new TreeMap<>();
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

    @Override
    public void draw ()
    {
        background(255);

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
            int i = 1; 
            for (Edge e : edgeClass)
                drawEdge(e, i++);
        }

        // move held vertex to the mouse
        if (held != null)
            vertexLocations.put(held,
                    new PVector(
                            constrain(mouseX, vertexRadius + 1, width - vertexRadius - 1),
                            constrain(mouseY, vertexRadius + 1, height - vertexRadius - 1)));
    }

    public void drawEdge(Edge e, int i)
    {
        // the center of a edge is the average of all the positions of all the
        // positions of the vertices that are connected by this edge
        PVector edgeCenter = e.vertices().stream().map(v -> vertexLocations.get(v)).
            reduce(new PVector(0, 0), (a,b) -> new PVector(a.x + b.x, a.y + b.y)).
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
            PVector end = edgeCenter.copy().
                          sub(vLoc).
                          normalize().
                          mult(vertexRadius + (e.orientedTowards(v)?headingHeight:0));

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
                                debugArrow.append(repititionsOf(" ", args[index - 1].length())).append(" ");
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
                G = (Graph)GenerateGraph.class.getDeclaredMethod(methodName, methodArgTypes).invoke(null, methodArgs);
            }
            catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
            {
                // if generation of graph fails then printout valid methods
                System.out.println("Invalid method as argument, valid methods:");
                for (Method m : GenerateGraph.class.getDeclaredMethods())
                    if (!m.getName().startsWith("lambda$")) // filter out lambdas
                        System.out.printf("%s.%s : (%s) -> %s\n", GenerateGraph.class.getName(), m.getName(), 
                            String.join(" * ",(List<String>)
                                new ArrayList<>(asList(m.getAnnotatedParameterTypes())).
                                    stream().
                                    map(x -> x.getType().getTypeName()).
                                    collect(Collectors.toList())), Graph.class.getName());
                return;
            }
        }
        else
            G = GenerateGraph.generate0(); // default graph since takes 0 args
            
        PApplet.main(appletArgs);
    } // end main ()

    private static String repititionsOf (String __, int numberOfRepititions)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < numberOfRepititions; i++)
            result.append(__);
        return result.toString();
    }
    
    private static String[] subList (String[] args, int start, int endExclusive)
    {
        String[] result = new String[endExclusive - start];
        for (int i = start; i < endExclusive; i++)
            result[i - start] = args[i - start];
        return result;
    }    
    
} // end class DrawHyperGraph


// -vertexColor 255 40 60 -fullscreen -vertexRadius 60 -size 400 200 randomGraph int 8 int 4
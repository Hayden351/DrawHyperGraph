package draw;

import processing.core.*;

import java.util.TreeMap;
import static java.util.Arrays.asList;

import java.util.ArrayList;

import hypergraph.Graph;
import hypergraph.Vertex;
import hypergraph.Edge;
import hypergraph.GenerateGraph;
import java.awt.Color;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    public static Graph G;

    @CommandLineConfigurable private static int vertexRadius = 30;
    @CommandLineConfigurable private static int headingWidth = 10;
    @CommandLineConfigurable private static int headingHeight = 15;
    
    @CommandLineConfigurable private static Color baseVertexColor;
    
    @CommandLineConfigurable private static int initialWidth;
    @CommandLineConfigurable private static int initialHeight;

    public static final float PHI = (1 + sqrt(5)) / 2;

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
        BiPredicate<ArrayList<Edge>, Edge> hasSameConnectionsAsClass = 
            (edgeClass, e) -> 
                edgeClass.get(0).sameConnections(e);
        
        G.edges().forEach // if matches an equivalence class then add edge to class
        ( edge -> equivalenceClasses.stream().
                filter(edgeClass -> hasSameConnectionsAsClass.test(edgeClass, edge)).
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
            int i = 0;
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
        float angle = (i * 2 * PI / (1 + PHI));
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
    public static Class translate (String type)
    {
        switch (type)
        {
            case "String": return String.class;
            case "int": case "Integer": return Integer.class;
            default: return null;
        }
    }
    public static Object stringToType (String type, String value)
    {
        switch (type)
        {
            case "String": return value;
            case "int": case "Integer": return Integer.parseInt(value);
            default: return null;
        }
    }
    
    private static Object convertArg (Class<?> type, String arg)
    {
        final String a = Integer.class.getCanonicalName();
        switch (type.getCanonicalName())
        {
            case "java.lang.String": return arg;
            case "java.lang.Integer": case "int": return Integer.parseInt(arg);
            
        }
        return null;
    }
    
    public static void main (String[] args)
    {
        boolean presenting = false;
        boolean vertexColor = false;
        
        String naturalNumberPattern = "0|[1-9][0-9]*";
        Pattern p = Pattern.compile(String.format("^\\(?(%s),(%s),(%s)\\)?$",
                naturalNumberPattern,
                naturalNumberPattern,
                naturalNumberPattern));
        
        Function<String, Color> parseColorFromArgs = str ->
        {
            
            Matcher m = p.matcher(str);
            m.matches();
            
            return new Color
            (
                Integer.parseInt(m.group(1)),
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3))
            );
        };
        
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
                        presenting = true;
                    } break;
                    case "-vertexColor":
                    {
                        // TODO: make conistent with size thats
                        baseVertexColor = parseColorFromArgs.apply(args[++i]);
                        vertexColor = true;
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
                            throw new IllegalArgumentException("Argument not reconginzed");
                    } break;
                }
            }
            
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
            G = GenerateGraph.generate0();
        
        String[] appletArgs;
        if (presenting)
            appletArgs= new String[]{"--present", "--window-color=#666666", "--hide-stop", "draw.DrawHyperGraph"};
        else 
            appletArgs= new String[]{"draw.DrawHyperGraph"};
        
        if (!vertexColor)
            baseVertexColor = new Color(0,0,0);
        
        if (args != null)
        {
            PApplet.main(appletArgs);
        }
        else 
        {
            PApplet.main(appletArgs);
        }
    } // end main ()
} // end class DrawHyperGraph

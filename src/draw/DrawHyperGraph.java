package draw;

//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import deprecated.DrawGraphAPI;
import deprecated.Command;
import deprecated.ApiState;
import processing.core.*;

import java.util.TreeMap;
import static java.util.Arrays.asList;

import java.util.ArrayList;

import definition.Graph;
import definition.Vertex;
import definition.Edge;
import generate.GenerateGraph;
import definition.GraphProperties;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayDeque;
import java.util.Arrays;
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
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


// fromAST String C:\Users\DarkFight753\OneDrive\NBMisc\DirectedMethodPath\src\directedmethodpath\test_classes\InsideOnly.java
// fromAST String C:\Users\DarkFight753\OneDrive\NBMisc\DirectedMethodPath\src\directedmethodpath\test_classes\TestClass.java
public class DrawHyperGraph extends PApplet
{
    public static class GraphDrawingData
    {
        Graph G;
        // colors for each specific Vertex and Edge in the graph G
        public Map<Vertex, Color> vertexColors = new TreeMap<>();
        public Map<Edge, Color> edgeColors = new TreeMap<>();
        public TreeMap<Vertex, PVector> vertexLocations;
        /*
        in order to get color information i simply have to parse the json a 
        second time while parsing it to get the graph information
        */
        
    }
    // TODO: figure out how to take multiple (graph and graph draw information)
    //       instead of having only one (Although interestingly enough having 
    //       only 1 for colors would work since unique edges)
    public static Map<Edge, Color> edgeColors = new TreeMap<>();
    public static Map<Vertex, Color> vertexColors = new TreeMap<>();
    
    // track the locations of vertices in the drawing
    public static TreeMap<Vertex, PVector> vertexLocations;
    // track which vertex is currently being moved by the mouse
    public static Vertex held;
    public static PVector heldOffset;
    public static List<Graph> graphs = new ArrayList<>();
    public static int currentGraph = 0;
    public static PrintStream out = System.out;
    
    
    public static class ToBoolean implements Converter
    { @Override public Object convert (List<String> args) { return Boolean.valueOf(args.get(0)); } }
    public static class ConvertToColor implements Converter
    { @Override public Object convert (List<String> args) { return new Color(Integer.parseInt(args.get(0)), Integer.parseInt(args.get(1)), Integer.parseInt(args.get(2))); } }
    public static class ConvertTo2DPoint implements Converter
    { @Override public Object convert (List<String> args) { return new PVector(Integer.parseInt(args.get(0)), Integer.parseInt(args.get(1))); } }
    
    // TODO: do we need G at all?
    /*
    can refer to G or graphs.get(currentGraph)
    */
//    public static Graph G;

    // TODO: there is a lookup for global variables for
    @CommandLineConfigurable(description="The radius of the circle the represents a vertex in the graph.")
    private static float vertexRadius = 30;
    
    @CommandLineConfigurable(description="Will increase the width of the triangles that are at the end of a directed edges.")
    private static float headingWidth = 10;
    
    @CommandLineConfigurable(description="Will increase the height of the triangles that are at the end of a directed edges.")
    private static float headingHeight = 15;
    
    @CommandLineConfigurable(description="The width of the window that contains the drawing.")
    private static int initialWidth = 500;
    
    @CommandLineConfigurable(description="The height of the window that contains the drawing.")
    private static int initialHeight = 500;

    @CommandLineConfigurable(description="The edge offset factor when edges are overlapped.")
    public static float edgeOffset = (1 + sqrt(5)) / 2;
    
    // TODO: add an example json into description
    @CommandLineConfigurable(description="Additionally read json information from given graph.", neededParameters=0)
    boolean readColors;
    
    @CommandLineConfigurable(description="Cannot move camera with arrow keys.", neededParameters=0)
    public boolean movementDisabled = false;
    
    @CommandLineConfigurable(description="The given graph is converted to a undirected graph.", neededParameters=0)
    public static boolean undirected = false;
    
    @CommandLineConfigurable(description="The given graph is converted to a undirected graph.", neededParameters=2, function=ConvertTo2DPoint.class)
    public PVector topLeftCameraOffsetFromOrigin = new PVector(0, 0);
    
    @CommandLineConfigurable(description="How quickly the camera will move around when the arrow keys are pressed.")
    public static float panIncrement = 6;
    
    @CommandLineConfigurable(description="Horizontal camera speed.")
    public static float horizantalPanIncrement = -1;
    
    @CommandLineConfigurable(description="Vertical camera speed.")
    public static float verticalPanIncrement = -1;
    
    public static boolean noOutOfBounds = false;
    
    public static boolean fullscreen = false;
    
    @CommandLineConfigurable(description="The default color for a vertex.", function=ConvertToColor.class, neededParameters=3)
    private static Color baseVertexColor = new Color(0,0,0);
    
    @CommandLineConfigurable(description="The default color for a vertex label.", function=ConvertToColor.class, neededParameters=3)
    private static Color baseVertexLabelColor = new Color(0,0,0);
    
    @CommandLineConfigurable(description="The default color for a edges.", function=ConvertToColor.class, neededParameters=3)
    private static Color baseEdgeColor = new Color(0,0,0);
    
    @CommandLineConfigurable(description="The default color for a edge labels.", function=ConvertToColor.class, neededParameters=3)
    private static Color baseEdgeLabelColor = new Color(0,0,0);
    
    @CommandLineConfigurable(description="Vertex and edge color information from a file.")
    public static String readColorInformationFromFile = null;
    
    @CommandLineConfigurable(description="Vertex and edge color information from the input.", neededParameters = 0)
    public static boolean readColorInformationFromInput;
    
    @CommandLineConfigurable(description="Will assume input is a tree and organize the graph under that assumption.", neededParameters = 0)
    public static boolean tree = false;
    
    boolean snapToGrid = false;
    
    @CommandLineConfigurable(description="When snap to grid mode is active (g key) then moving vertices will move according to a grid.", neededParameters = 1)
    int gridSize = 100;
    
    public static final float PHI = (1 + sqrt(5)) / 2;

// TODO: change font maybe
    PFont font;
    @Override
    public void settings()
    {
        size(displayWidth, displayHeight);
    }   

    public DrawGraphAPI apiInput;
    
    @Override
    public void setup ()
    {
        surface.setResizable(true);
        out.println(ManagementFactory.getRuntimeMXBean().getName());
        if (fullscreen)
        {
            surface.setSize(displayWidth, displayHeight);
            width = displayWidth;
            height = displayHeight;
        }
        else
            surface.setSize(initialWidth, initialHeight);
        
        // set each edge to not be directed towards any of its vertices
        // TODO: we shouldn't change the graph, we just want to change how we draw the graph
        if (undirected)
            graphs.forEach(graph -> graph.edges().forEach(edge -> edge.orientations.replaceAll((v, b) -> false)));
        
        
        if (horizantalPanIncrement < 0) horizantalPanIncrement = panIncrement;
        if (verticalPanIncrement < 0) verticalPanIncrement = panIncrement;
        
        
        // initially no vertex is being manipulated
        held = null;
        heldOffset = new PVector(0, 0);

        // assign a random location to each vertex in the graph
        vertexLocations = new TreeMap<>();
        
        // start at center
        // TODO: we don't neccessarily want to apply the same placemnt algorithm 
        //       to all to graphs in the list
        //
        if (tree)
            for (Graph G : graphs)
//                placeVerticesTree(G);
                spreadOutVertices(G);
        else
            for (Graph G : graphs)
                placeVerticesRandom(G);
        
        // TODO:
        if (readColorInformationFromFile != null)
        {
//            try
//            {
//                JsonNode node = new ObjectMapper().readTree(readColorInformationFromFile);
//            }
//            catch (IOException ex)
//            {
//                System.out.println("Couldn't add color information");
//            }
        }
        
        System.out.println(graphs.get(currentGraph));
    }
    
    public int printLocationOccassionally = 0;
    @Override
    public void draw ()
    {
        // logs the current location
        if (printLocationOccassionally < 300)
            printLocationOccassionally++;
        else
        {
            out.printf("[ %s %s ]\n", this.topLeftCameraOffsetFromOrigin.x, this.topLeftCameraOffsetFromOrigin.y);
            printLocationOccassionally = 0;
        }
        
        background(255);

        handleInput();
        
        // draw vertices
        for (Vertex v : graphs.get(currentGraph).vertices())
        {
            PVector vLoc = vertexLocations.get(v);

            vertexSettings(v);
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
        
        graphs.get(currentGraph).edges().forEach // matches an edge into a edge class
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
        {
            if (snapToGrid)
            {
//                PVector absoluteLocationOfGrid = new PVector(mouseX, mouseY).add(topLeftCameraOffsetFromOrigin).div(gridSize);
//                Function<Integer, Function<Float, Function<Integer, Integer>>> findCloser = left -> center -> right -> 
//                {
//                    return abs(left - center) < abs(right - center) ? left : right;
//                };
//                absoluteLocationOfGrid = new PVector(
//                    findCloser.apply(round(floor(absoluteLocationOfGrid.x))).apply(absoluteLocationOfGrid.x).apply(round(ceil(absoluteLocationOfGrid.x))), 
//                    findCloser.apply(round(floor(absoluteLocationOfGrid.y))).apply(absoluteLocationOfGrid.y).apply(round(ceil(absoluteLocationOfGrid.y))));
//                vertexLocations.put(held, absoluteLocationOfGrid.mult(gridSize).sub(topLeftCameraOffsetFromOrigin.copy()));
                
                
                ;
                vertexLocations.put(held, snapRelativeToGrid(topLeftCameraOffsetFromOrigin, new PVector(mouseX, mouseY), gridSize));
            }
            else
                vertexLocations.put(held, new PVector(mouseX, mouseY).add(heldOffset));
        }
        if (noOutOfBounds)
            for (Vertex v : graphs.get(currentGraph).vertices) // TODO: there is a much nicer way of doing this using map methods
                vertexLocations.put(v, new PVector(
                    constrain(vertexLocations.get(v).x, vertexRadius + 1, width - vertexRadius - 1),
                    constrain(vertexLocations.get(v).y, vertexRadius + 1, height - vertexRadius - 1)
                ));
    }
    
    public static PVector snapRelativeToGrid(PVector originRelative, PVector relativeRelative, int size)
    { originRelative = originRelative.copy(); relativeRelative = relativeRelative.copy();
    
        return closestIntegerPoint((relativeRelative.add(originRelative)).div(size)).mult(size).sub(originRelative);
    }
    
    // find closest point that also consists only of integer values
    public static PVector closestIntegerPoint(PVector point)
    {
        return new PVector(
                    returnClosest(round(floor(point.x)), point.x, round(ceil(point.x))), 
                    returnClosest(round(floor(point.y)), point.y, round(ceil(point.y))));
    }
    
    public static int returnClosest(int left, float center, int right)
    {
        return abs(left - center) < abs(right - center) ? left : right;
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
            case 'a':
            {
                currentGraph = constrain(currentGraph - 1, 0, graphs.size() - 1);
            } break;
            case 'd':
            {
                currentGraph = constrain(currentGraph + 1, 0, graphs.size() - 1);
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
            case 'g':
            {
                snapToGrid = !snapToGrid;
                if (snapToGrid)
                    for (Vertex v : vertexLocations.keySet())
                        vertexLocations.put(v, snapRelativeToGrid(topLeftCameraOffsetFromOrigin, vertexLocations.get(v), gridSize));
                    
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
        // TODO: currently zooming in/out is a hack to make vertices smaller/larger
        //       at some point implement an actual zooming system
        // https://www.gamedev.net/forums/topic/594055-zooming-onto-an-arbitrary-point/
        if (zoomingIn)
        {
            vertexRadius -= .125f;
            if (vertexRadius <= 5)
                vertexRadius = 5;
            else ;
//                for (Vertex v : vertexLocations.keySet())
//                    vertexLocations.get(v).add(PVector.fromAngle(angleBetween(vertexLocations.get(v), new PVector(width / 2, height / 2))).normalize().mult(.25f));
        }
        else if (zoomingOut)
        {
            vertexRadius += .125f;
            if (vertexRadius >= 500)
                vertexRadius = 500;
            else ;
//                for (Vertex v : vertexLocations.keySet())
//                    vertexLocations.get(v).add(PVector.fromAngle(angleBetween(new PVector(width / 2, height / 2), vertexLocations.get(v))).normalize().mult(.25f));
        }
        
        PVector movement = new PVector(0,0);
        if (movingUp)
            movement.add(PVector.fromAngle(PI / 2).normalize().mult(verticalPanIncrement));
        if (movingRight)
            movement.add(PVector.fromAngle(PI).normalize().mult(horizantalPanIncrement));
        if (movingDown)
            movement.add(PVector.fromAngle(3 * PI / 2).normalize().mult(verticalPanIncrement));
        if (movingLeft)
            movement.add(PVector.fromAngle(0).normalize().mult(horizantalPanIncrement));
        
        for (Vertex v : vertexLocations.keySet())
            vertexLocations.get(v).add(movement);
        this.topLeftCameraOffsetFromOrigin.sub(movement);
    }

    public static float TAU = PI * 2;
    public void drawEdge (Edge e, int i)
    {
        // the center of a edge is the average of all the positions of all the
        // positions of the vertices that are connected by this edge
        PVector edgeCenter;
        
        // TODO: self loop is too blocky, replace with absolute instead of relative
        if (e.vertices().size() == 1) // is self loop
        {
            PVector vLoc = vertexLocations.get(e.vertices().iterator().next());
            
            boolean isDirected = e.orientations.values().stream().anyMatch(x -> x);
            // TODO: wat do if self loop. right now is just a line above the vertex
//            float theta = (TAU * 8) / 16 + (TAU / 4 - asin(1/sqrt(5)));
            float theta = (12 * TAU - 16 * asin(1/sqrt(5))) / 16;
            
            // adj = vertexRadius / 2
            // opp = vertexRadius
            // hyp = sqrt(4 + 1) * vertexRadius / 2
            // sin (angle) = 1 / sqrt(5)
            
            List<PVector> vectors = new ArrayList<>(asList(
                vLoc.copy().add(PVector.fromAngle(theta).mult(vertexRadius)),
                new PVector(0, -vertexRadius * (i + 1)),
                new PVector(vertexRadius * (i + 1), 0)
            ));
            vectors.add(new PVector(0, vertexRadius - (isDirected?headingHeight:0)));
            
            PVector end = relativeLine(vectors);
            
//            line(vLoc.x - vertexRadius * cos(theta),
//                 vLoc.y - vertexRadius * sin(theta),
//                 vLoc.x - vertexRadius * cos(theta),
//                 vLoc.y - vertexRadius * sin(theta) - vertexRadius);
//
//            // draw line up from edge of vertex on right sight
//            line(
//                vLoc.x + vertexRadius * cos(theta),
//                vLoc.y - (vertexRadius + (headingHeight + 3) * (isDirected ? 1 : 0)) * (float) Math.sin(theta),
//                vLoc.x + vertexRadius * cos(theta),
//                vLoc.y - vertexRadius * sin(theta) - vertexRadius);
//
//            // draw line between them
//            line(vLoc.x - vertexRadius * cos(theta), 
//                 vLoc.y - vertexRadius * sin(theta) - vertexRadius, 
//                 vLoc.x + vertexRadius * cos(theta), 
//                 vLoc.y - vertexRadius * sin(theta) - vertexRadius);

            // add arrow to tip of edge
            if (isDirected)
            {
                strokeWeight(1);
                stroke(0);
                drawHeading(end.x,
                            end.y,
                            PI / 2,
                            headingWidth,
                            headingHeight, e);
            }
            return;
        }
        
            
        edgeCenter = e.vertices().stream().
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

            edgeSettings(e); // set settings for drawing an edge line
            line(edgeCenter.x, edgeCenter.y, vLoc.x + end.x, vLoc.y + end.y);

            // draw heading if oriented towards
            if (e.orientedTowards(v))
                drawHeading(vLoc.copy().add(end), atan2(-end.y, -end.x), headingWidth, headingHeight, e);

            // draw text after line so that text is visible
            edgeTextSettings(); // set setting for drawing the label of an edge
            text(e.label, edgeCenter.x, edgeCenter.y);
        }
    } // end drawEdge ()

    public void drawHeading(float startHeadingX, float startHeadingY, float angle, float headingWidth, float headingHeight, Edge e)
    {
        drawHeading(new PVector(startHeadingX, startHeadingY), angle, headingWidth, headingHeight, e);
    }
    
    public void drawHeading(PVector startHeading, float angle, float headingWidth, float headingHeight, Edge e)
    {
        PVector leftPoint = startHeading.copy().add(PVector.fromAngle(angle + PI / 2).mult(headingWidth / 2));
        PVector rightPoint = startHeading.copy().add(PVector.fromAngle(angle - PI / 2).mult(headingWidth / 2));
        PVector forwardPoint = startHeading.copy().add(PVector.fromAngle(angle).mult(headingHeight));

        headingSettings(e);

        line(startHeading.x, startHeading.y, leftPoint.x, leftPoint.y);
        line(startHeading.x, startHeading.y, rightPoint.x, rightPoint.y);
        line(leftPoint.x, leftPoint.y, forwardPoint.x, forwardPoint.y);
        line(rightPoint.x, rightPoint.y, forwardPoint.x, forwardPoint.y);
    }
    
    public float angleBetween (PVector start, PVector end)
    {
        return atan2(end.y - start.y, end.x - start.x);
    }
    public PVector vectorFromTo(PVector start, PVector end)
    {
        return new PVector(end.x - start.x, end.y - start.y);
    }
    public PVector vectorFromTo(float startx, float starty, float endx, float endy)
    {
        return new PVector(endx - startx, endy - starty);
    }
    
    /*
    
    we start at top left is (0, 0) to (width, height)
    
    */
    public void shiftCamera(PVector amount)
    {
        topLeftCameraOffsetFromOrigin.add(amount);
    }
    
    public void toCenter()
    {
        PVector graphCenter = 
            graphs.get(currentGraph).vertices.stream().
            map(v -> vertexLocations.get(v)).
            reduce
            (
                new PVector(0, 0), 
                (a,b) -> new PVector(a.x + b.x, a.y + b.y)
            ).
            div(graphs.get(currentGraph).vertices.size());
        
        
    }

    @Override
    public void mousePressed ()
    {
        // hold vertex that mouse is hovering over
        for (Vertex v : graphs.get(currentGraph).vertices())
        {
            PVector vLoc = vertexLocations.get(v);
            if (Utils.distance(mouseX, mouseY, vLoc.x, vLoc.y) <= vertexRadius)
            {
                held = v;
                heldOffset = vectorFromTo(mouseX, mouseY, vLoc.x, vLoc.y);
                return;
            }
        }
    }

    @Override
    public void mouseReleased ()
    {
        held = null;
    }

    public void headingSettings (Edge e)
    {
        noFill();
        Color c = edgeColors.getOrDefault(e, baseEdgeColor);
        stroke(c.getRed(), c.getGreen(), c.getBlue());
    }

    public void vertexSettings (Vertex v)
    {
        Color c = vertexColors.getOrDefault(v, baseVertexColor);
        noFill();
        stroke(c.getRed(), c.getGreen(), c.getBlue());
    }

    public void vertexTextSettings ()
    {
        fill(baseVertexLabelColor.getRed(), baseVertexLabelColor.getGreen(),
            baseVertexLabelColor.getBlue());
        textSize(10);
        textAlign(CENTER);
    }

    public void edgeSettings (Edge e)
    {
        noFill();
        Color c = edgeColors.getOrDefault(e, baseEdgeColor);
        stroke(c.getRed(), c.getGreen(), c.getBlue());
        
    }

    public void edgeTextSettings ()
    {
        fill(baseEdgeLabelColor.getRed(), baseEdgeLabelColor.getGreen(),
            baseEdgeLabelColor.getBlue());
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
    public static void main (String[] args)
    {
        System.out.println(Arrays.toString(args));
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
                        appletArgs = new String[]{"--present", "--window-color=#666666", "--hide-stop", "draw.DrawHyperGraph"};
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
                        undirected = false;
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
                            {
                                CommandLineConfigurable var = f.getDeclaredAnnotation(CommandLineConfigurable.class);
                                if (var != null && flag.equals(f.getName()))
                                {
                                    
                                    List<String> parameterArgs = new ArrayList<>();
                                    for (int argCount = 0; argCount < var.neededParameters(); argCount++)
                                    {
                                        i++;
                                        // TODO: i holds the posiiton of the previously used
                                        // probably want to change it so it holds the next usable
                                        String parameterArg = args[i];
                                        Pattern p = Pattern.compile("^--?");
                                        if (p.matcher(parameterArg).matches())
                                            ; // then error
                                        
                                        parameterArgs.add(parameterArg);
                                    }
                                    // TODO: test this i think it works now
                                    f.set(null, ((Converter)var.function().newInstance()).convert(parameterArgs));
                                    flagTripped = true;
                                    break;
                                }
                            }
                        }
                        catch (IllegalArgumentException | IllegalAccessException | InstantiationException ex) 
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
                            printValidFlags();
                            
                            throw new IllegalArgumentException("Argument not recognized");
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
                // seems like this doesn't generalize very well in that
                // The GenerateGraph class will get larger and larger
                // although could have a type hierarchy where each node in the
                // path a different set of graph generating algorithms
                // TODO: implement a way of invoking multiple menthods
                Method graphGeneratingMethod = GenerateGraph.class.getMethod(methodName, methodArgTypes);
                
                
//               
                if (Graph.class.equals(graphGeneratingMethod.getReturnType()))
                    graphs.add((Graph)graphGeneratingMethod.invoke(null, methodArgs));
                else
                    graphs = (List<Graph>)graphGeneratingMethod.invoke(null, methodArgs);
                
                
                if (readColorInformationFromInput)
                {
                    try
                    {
                        JsonNode node = new ObjectMapper().readTree(new File((String)methodArgs[0]));
                        System.out.println("");
                        System.out.println("");
                        System.out.println(node);
                        
                        System.out.println("");
                        System.out.println("");
                        
                        node.get("vertices").elements().forEachRemaining(vertext ->
                        {
                            System.out.println(vertext);
                            
                            JsonNode jsonColor = vertext.get("color");
                            
                            if (jsonColor != null)
                            {
                                Color color = new Color(jsonColor.get("red").asInt(), jsonColor.get("green").asInt(), jsonColor.get("blue").asInt());
                            
                            
                                // TODO: this 
                                vertexColors.put(graphs.get(0).findVertexById(vertext.get("uniqueId").asInt()), color);
                            }
                        });
                        
                        node.get("edges").elements().forEachRemaining(edge ->
                        {
                            System.out.println(edge);
                            
                            JsonNode jsonColor = edge.get("color");
                            
                            if (jsonColor != null)
                            {
                                Color color = new Color(jsonColor.get("red").asInt(), jsonColor.get("green").asInt(), jsonColor.get("blue").asInt());
                            
                            
                                // TODO: this 
                                edgeColors.put(graphs.get(0).findEdgeById(edge.get("uniqueId").asInt()), color);
                            }
                        });
                        
                    }
                    catch (IOException ex)
                    {
                        System.out.println("Couldn't add color information");
                    }
                }
            }
            catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException ex)
            {
                // if generation of graph fails then printout valid methods
                System.out.printf("Invalid method %s, valid methods:\n", methodName);
                
                printUsableMethods();
                
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
            System.out.println("");
            
            System.out.println("Valid <config> flags");
            printValidFlags();
            System.out.println("");
            
            System.out.println("Valid <generate-graph-function> functions");
            printUsableMethods();
            System.out.println("");
            return;
        }

        // we just call a different main method from this main method
        PApplet.main(appletArgs);
       
//        PApplet.main(new String[] { "SaveMenu" });
    } // end main ()
    
    private static void printUsableMethods ()
    {
        for (Method m : GenerateGraph.class.getMethods())
            // A.containsAll(B) iff B subseteq A
            if (new HashSet<>(asList(Integer.class, int.class, float.class, Float.class, boolean.class, Boolean.class, String.class)).
                    containsAll(new HashSet<>(asList(m.getParameterTypes()))))
                if (!m.getName().startsWith("lambda$")) // filter out lambdas
                    if (Graph.class.equals(m.getReturnType())
                        || (m.getGenericReturnType() instanceof ParameterizedType
                            && Graph.class.equals(((ParameterizedType)m.getGenericReturnType()).getActualTypeArguments()[0])))
                        System.out.printf("%s.%s : (%s) -> %s\n", 
                            GenerateGraph.class.getName(), 
                            m.getName(), 
                            String.join(" * ",
                                    new ArrayList<>(asList(m.getParameterTypes())).
                                        stream().
                                            map(x -> x.getTypeName()).
                                            collect(Collectors.toList())),
                            m.getReturnType().getTypeName());
    }
    private static void printValidFlags ()
    {
        for (Field f : DrawHyperGraph.class.getDeclaredFields())
        {
            CommandLineConfigurable config = f.getAnnotation(CommandLineConfigurable.class);
            if (config != null)
                System.out.printf("-%s : %s\n", f.getName(), config.description());
        }
    }
    
    public void spreadOutVertices (Graph G)
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
        
        int depth = 0;
        
        Set<Vertex> visited = new HashSet<>();
        Set<Vertex> currentLayer = new HashSet<>();
        
        currentLayer.addAll(verticesWithNoParents);
        visited.addAll(currentLayer);
        
        while (true)
        {
            int numberInlayer = currentLayer.size();
            int currentInLayer = 0;
            Iterator<Vertex> it = currentLayer.iterator();
            while (it.hasNext())
            {
                Vertex v = it.next();
                vertexLocations.put(v, new PVector(width / 2, height / 2).add(PVector.fromAngle(2 * currentInLayer * PI / numberInlayer).mult(depth * vertexRadius * 3)));
                currentInLayer++;
            }
            
            if (visited.equals(G.vertices))
                break;
            
            Set<Vertex> newLayer = new HashSet<>();
            for (Vertex v : currentLayer)
                for (Vertex u : GraphProperties.getNeighbors(G, v))
                if (visited.add(u))
                    newLayer.add(u);
            currentLayer = newLayer;
            depth++;
        }
        
        for (Vertex v : G.vertices)
            if (!vertexLocations.containsKey(v))
                vertexLocations.put(v, new PVector(0, 0));
    }

    private void placeVerticesTree (Graph G)
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

                    // TODO: this logic is kind of wonky
                    int giveUp = width * height;
                    while (Utils.isLocationAwayFrom(placementLocation, vertexLocations, vertexRadius * 3))
                    {
                        PVector newLocation  = placementLocation.copy().add(new PVector(5 * cos(angleFromParent), 5 * sin(angleFromParent)));

                        // move or change direction
//                        if (!(0 <= newLocation.x && newLocation.x <= width && rad <= newLocation.y && newLocation.y <= height))
//                          angleFromParent += (2 * PI / PHI);
//                        else
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
//        for (Vertex v : G.vertices)
//            vertexLocations.put(v, new PVector(
//                        constrain(vertexLocations.get(v).x, vertexRadius + 1, width - vertexRadius - 1),
//                        constrain(vertexLocations.get(v).y, vertexRadius + 1, height - vertexRadius - 1)));
    }

    private void placeVerticesRandom (Graph G)
    {
        for (Vertex v : G.vertices())
            vertexLocations.put
            (v, new PVector(random(vertexRadius + 1, width - vertexRadius - 1),
                            random(vertexRadius + 1, height - vertexRadius - 1)));
    }

    public PVector relativeLine (List<PVector> vectors)
    {
        PVector acc = vectors.get(0).copy();
        for (int i = 1; i < vectors.size(); acc.add(vectors.get(i)), i++)
            line(acc, acc.copy().add(vectors.get(i)));
        return acc;
    }

    public void line (PVector start, PVector end)
    {
        line(start.x, start.y, end.x, end.y);
    }
} // end class DrawHyperGraph


// -vertexColor 255 40 60 -fullscreen -vertexRadius 60 -size 400 200 randomGraph int 8 int 4
// -baseVertexColor 255 0 0 -baseEdgeColor 0 255 0 -panIncrement 20 -initialWidth 1000 -vertexRadius 20 undirectedUnlabeledGraphFrom String "0 1 1 0 0~0 0 0 1 1~0 0 0 0 0~0 0 0 0 0~0 0 0 0 0"
// java -jar dist\DrawHyperGraph.jar -readColorInformationFromInput -baseVertexColor 0 0 255 -baseVertexLabelColor 0 0 255 -baseEdgeColor 0 0 255 -baseEdgeLabelColor 0 255 0 graphFromJsonFile String src/draw/json
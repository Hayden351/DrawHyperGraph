package draw;

//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
import populate_from_args.Converter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import deprecated.DrawGraphAPI;
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
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import populate_from_args.NamedArgument;


// fromAST String C:\Users\DarkFight753\OneDrive\NBMisc\DirectedMethodPath\src\directedmethodpath\test_classes\InsideOnly.java
// fromAST String C:\Users\DarkFight753\OneDrive\NBMisc\DirectedMethodPath\src\directedmethodpath\test_classes\TestClass.java
public class DrawHyperGraph extends PApplet
{
    // TODO: do i still want to use this?
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
        
        doing 2 passes feels bad though
        
        */
        
    }
    // TODO: figure out how to take multiple (graph and graph draw information)
    //       instead of having only one (Although interestingly enough having 
    //       only 1 for colors would work since unique edges)
    public static Map<Edge, Color> edgeColors = new TreeMap<>();
    public static Map<Vertex, Color> vertexColors = new TreeMap<>();
    
    // track the locations of vertices in the drawing
    public static TreeMap<Vertex, PVector> vertexLocations = new TreeMap<>();
    // track which vertex is currently being moved by the mouse
    public static List<Vertex> held;
    public static PVector heldOffset;
    public static List<Graph> graphs = new ArrayList<>();
    public static int currentGraph = 0;
    public static PrintStream out = System.out;
    
    
//    public static class ToBoolean implements Converter
//    { @Override public Object convert (List<String> args) { return Boolean.valueOf(args.get(0)); } }
    public static class ConvertToColor implements Converter
    {
        @Override public Color convert (List<String> args) { return new Color(Integer.parseInt(args.get(0)), Integer.parseInt(args.get(1)), Integer.parseInt(args.get(2))); }
        @Override public int numberOfArguments () { return 3; }
    }
    public static class ConvertTo2DPoint implements Converter
    {
        @Override public PVector convert (List<String> args) { return new PVector(Integer.parseInt(args.get(0)), Integer.parseInt(args.get(1))); }
        @Override public int numberOfArguments () { return 2; }
    }
    public static class BooleanFlag implements Converter
    {
        @Override public Boolean convert (List<String> args) { return true; }
        @Override public int numberOfArguments () { return 0; }
    }
    
    // TODO: do we need G at all?
    /*
    can refer to G or graphs.get(currentGraph)
    */
//    public static Graph G;

    // TODO: there is a lookup for global variables for
    @NamedArgument(description="The radius of the circle the represents a vertex in the graph.")
    private static float vertexRadius = 30;
    
    @NamedArgument(description="Will increase the width of the triangles that are at the end of a directed edges.")
    private static float headingWidth = 10;
    
    @NamedArgument(description="Will increase the height of the triangles that are at the end of a directed edges.")
    private static float headingHeight = 15;
    
    @NamedArgument(description="The width of the window that contains the drawing.")
    private static int initialWidth = 500;
    
    @NamedArgument(description="The height of the window that contains the drawing.")
    private static int initialHeight = 500;

    @NamedArgument(description="The edge offset factor when edges are overlapped.")
    public static float edgeOffset = (1 + sqrt(5)) / 2;
    
    // TODO: add an example json into description
    @NamedArgument(description="Additionally read json information from given graph.", converter=BooleanFlag.class)
    public static boolean readColors;
    
    @NamedArgument(description="Cannot move camera with arrow keys.", converter=BooleanFlag.class)
    public boolean movementDisabled = false;
    
    @NamedArgument(description="The given graph is converted to a undirected graph.", converter=BooleanFlag.class)
    public static boolean undirected = false;
    
    @NamedArgument(description="The absolute location of the top left corner of the window.", neededParameters=2, converter=ConvertTo2DPoint.class)
    public static PVector topLeftCameraOffsetFromOrigin = new PVector(0, 0);
    
    @NamedArgument(description="How quickly the camera will move around when the arrow keys are pressed.")
    public static float panIncrement = 6;
    
    @NamedArgument(description="Horizontal camera speed.")
    public static float horizantalPanIncrement = -1;
    
    @NamedArgument(description="Vertical camera speed.")
    public static float verticalPanIncrement = -1;
    
    public static boolean noOutOfBounds = false;
    
    public static boolean fullscreen = false;
    
    @NamedArgument(description="The default color for a vertex.", converter=ConvertToColor.class, neededParameters=3)
    private static Color baseVertexColor = new Color(0,0,0);
    
    @NamedArgument(description="The default color for a vertex label.", converter=ConvertToColor.class, neededParameters=3)
    private static Color baseVertexLabelColor = new Color(0,0,0);
    
    @NamedArgument(description="The default color for a edges.", converter=ConvertToColor.class, neededParameters=3)
    private static Color baseEdgeColor = new Color(0,0,0);
    
    @NamedArgument(description="The default color for a edge labels.", converter=ConvertToColor.class, neededParameters=3)
    private static Color baseEdgeLabelColor = new Color(0,0,0);
    
    @NamedArgument(description="Vertex and edge color information from a file.")
    public static String readColorInformationFromFile = null;
    
    @NamedArgument(description="Vertex and edge color information from the input.", converter=BooleanFlag.class)
    public static boolean readColorInformationFromInput;
    
    @NamedArgument(description="Vertex location information from the input.", converter=BooleanFlag.class)
    public static boolean readLocationInformationFromInput;
    
    @NamedArgument(description="Will assume input is a tree and organize the graph under that assumption.", converter=BooleanFlag.class)
    public static boolean tree = false;
    
    public static Class<?> graphGeneratingClass = classForName(GenerateGraph.class.getName());
    
    public static boolean snapToGrid = false;
    
    @NamedArgument(description="When snap to grid mode is active (g key) then moving vertices will move according to a grid.")
    public static int gridSize = 100;
    
    @NamedArgument(description="The title that is displayed on the window bar.")
    public static String title = "DrawHyperGraph";
    
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
        surface.setTitle(title);
        
        // output state of the graph on program shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
        {
            public void run () 
            {
                for (Graph G : graphs)
                {
                    ObjectMapper om = new ObjectMapper();
                    JsonNode jn = om.valueToTree(graphs);
                    jn.forEach(jsonGraph ->
                    {
                        jsonGraph.get("vertices").forEach(jsonVertex ->
                        {
                            // ew
                            Vertex v = G.getVertexById(jsonVertex.get("uniqueId").asInt());
                            PVector location = vertexLocations.get(v);
                            // we got lucky with location but we can't sanely serialize color
                            Color color = vertexColors.get(v);
                            // this is a bit dumb
                            class CColor
                            {
                                public int red;
                                public int blue;
                                public int green;
                                public CColor(int red, int blue, int green)
                                {
                                    this.red = red;
                                    this.blue = blue;
                                    this.green = green;
                                }
                            }
                            if (location != null)
                                ((ObjectNode)jsonVertex).set("location", om.valueToTree(location));
                            if (color != null)
                                ((ObjectNode)jsonVertex).set("color", om.valueToTree(new CColor(color.getRed(), color.getBlue(), color.getGreen())));
                        });
                    });
                    try
                    {
                        System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(jn));
                    } catch (JsonProcessingException ex) { ; }
                }
            }
        }));
        
//        out.println(ManagementFactory.getRuntimeMXBean().getName());
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
//        if (undirected)
//            graphs.forEach(graph -> graph.edges().forEach(edge -> edge.orientations.replaceAll((v, b) -> false)));
        
        
        if (horizantalPanIncrement < 0) horizantalPanIncrement = panIncrement;
        if (verticalPanIncrement < 0) verticalPanIncrement = panIncrement;
        
        
        // initially no vertex is being manipulated
        held = new ArrayList<>();
        heldOffset = new PVector(0, 0);

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
    }
    
    @Override
    public void draw ()
    {
        background(255);

        // we want to have a command promt that takes commands
        handleInput();
        
//        List<PVector> rectangle = getRectangleAroundVertices();
//        for (int i = 0; i < rectangle.size(); i++)
//            line(rectangle.get(i), rectangle.get((i + 1) % rectangle.size()));
        
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
            int i = 0;
            for (Edge e : edgeClass)
                drawEdge(e, i++, edgeClass.size());
        }

        // move held vertex to the mouse
        if (held != null)
            if (snapToGrid)
                for (Vertex h : held)
                    vertexLocations.put(h, snapRelativeToGrid(topLeftCameraOffsetFromOrigin, new PVector(mouseX, mouseY), gridSize));
            else
                for (Vertex h : held)
                    vertexLocations.put(h, new PVector(mouseX, mouseY).add(heldOffset));
        
        if (noOutOfBounds)
            for (Vertex v : graphs.get(currentGraph).vertices) // TODO: there is a much nicer way of doing this using map methods
                vertexLocations.put(v, new PVector(
                    constrain(vertexLocations.get(v).x, vertexRadius + 1, width - vertexRadius - 1),
                    constrain(vertexLocations.get(v).y, vertexRadius + 1, height - vertexRadius - 1)
                ));
    }
    
    public static <T> List<List<T>> partition(List<T> elements, BiPredicate<T, T> equivalenceRelation)
    {
        List<List<T>> partitions = new ArrayList<>();
        elements.forEach // matches an edge into a edge class
        (element ->   partitions.stream().
                    // partition.get(0) : get representative element of partition
                    // all of the elements are equivalent according to the
                    // equivalence relation
                    filter(partition -> equivalenceRelation.test(element, partition.get(0))).
                    findFirst().orElseGet(() -> 
                    {   // create a new partition for the element that doesn't
                        // match any other partition
                        ArrayList<T> newPartition = new ArrayList<>();
                        partitions.add(newPartition);
                        return newPartition;
                    }).
                    add(element));
        return partitions;
    }
    
    public static PVector snapRelativeToGrid(PVector originRelative, PVector relativeRelative, int size)
    {
        return closestIntegerPoint((originRelative.copy().add(relativeRelative)).div(size)).mult(size).sub(originRelative);
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
    public static final char CTRL_S = 19;
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
            case CTRL_S:
            {
//                draw();
                save("SavedGraph.png");
            }
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
            case 'h': // snap held vertex to grid mode
            {
                snapToGrid = !snapToGrid;
            } break;
            case 'g': // snap all vertices to grid
            {
                for (Vertex v : vertexLocations.keySet())
                    vertexLocations.put(v, snapRelativeToGrid(topLeftCameraOffsetFromOrigin, vertexLocations.get(v), gridSize));
            } break;
            case 'p':
            {
                PVector initial = vertexLocations.values().iterator().next();
                float xMin = initial.x;
                float xMax = initial.x;
                float yMin = initial.y;
                float yMax = initial.y;
                for (PVector location : vertexLocations.values())
                {
                    if (location.x < xMin) xMin = location.x;
                    if (location.x > xMax) xMax = location.x;
                    if (location.y < yMin) yMin = location.y;
                    if (location.y > yMax) yMax = location.y;
                }
                xMin -= 2 * vertexRadius;
                xMax += 2 * vertexRadius;
                yMin -= 2 * vertexRadius;
                yMax += 2 * vertexRadius;
                
                System.out.println(topLeftCameraOffsetFromOrigin);
                System.out.println(vertexLocations.keySet().stream().map(v -> String.format("%s %s", v.label, vertexLocations.get(v))).collect(Collectors.toList()));
                System.out.printf("%s %s %s %s\n\n", xMin, xMax, yMin, yMax);
                surface.setSize((int)(xMax - xMin), (int)(yMax - yMin));
                
                
                PVector movement = new PVector(-xMin, -yMin);
                for (Vertex v : vertexLocations.keySet())
                    vertexLocations.get(v).add(movement);
                this.topLeftCameraOffsetFromOrigin.sub(movement);
                
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
        cameraMovement();
    }
    public void handleInput()
    {
        // TODO: currently zooming in/out is a hack to make vertices smaller/larger
        //       at some point implement an actual zooming system
        // https://www.gamedev.net/forums/topic/594055-zooming-onto-an-arbitrary-point/
        float Z = 5;
        if (zoomingIn)
        {
            // https://www.youtube.com/results?search_query=projection+matrices+
//            PVector zoomCenter = new PVector(mouseX, mouseY).add(topLeftCameraOffsetFromOrigin);
//            topLeftCameraOffsetFromOrigin = zoomCenter.copy().add(topLeftCameraOffsetFromOrigin.copy().sub(zoomCenter).mult(Z));
            
            
//            topLeftCameraOffsetFromOrigin.add(zoomCenter);
//            PVector oldViewCenter = new PVector(topLeftCameraOffsetFromOrigin.x, topLeftCameraOffsetFromOrigin.y);
//            PVector newViewCenter = zoomCenter.copy();
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
        cameraMovement();
    }
    
    public void cameraMovement()
    {
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
    
    public static List<PVector> getRectangleAroundVertices()
    {
        float xMin = 0;
        float xMax = 0;
        float yMin = 0;
        float yMax = 0;
        for (PVector location : vertexLocations.values())
        {
            if (location.x < xMin) xMin = location.x;
            if (location.x > xMin) xMax = location.x;
            if (location.y < yMin) yMin = location.y;
            if (location.y > yMin) yMax = location.y;
        }
        return new ArrayList<>(Arrays.asList(new PVector(xMin, yMin), new PVector(xMin, yMax), new PVector(xMax, yMin), new PVector(xMax, yMax)));
    }

    public static float TAU = PI * 2;
    public void drawEdge (Edge e, int i, int classSize)
    {
        // the center of a edge is the average of all the positions of all the
        // positions of the vertices that are connected by this edge
        PVector edgeCenter;
        
        // TODO: self loop is too blocky, replace with absolute instead of relative
        if (e.vertices().size() == 1) // is self loop
        {
            PVector vLoc = vertexLocations.get(e.vertices().iterator().next());
            
            boolean isDirected = !undirected
                                 && e.orientations.values().stream().anyMatch(x -> x);

            // start and ending points of the self loop
            float start = ( 5 * TAU) / 8 + i*(TAU / 16);
            float end = (7 * TAU) / 8 + i*(TAU / 16);
            // distance away from the outer edge of the vertex
            float distance = vertexRadius * (i + 1);
            // angle of the offshoot lines
            float angle = 3 * TAU / 4 + i*(TAU / 16);
            PVector startLoc = vLoc.copy().add(PVector.fromAngle(start).mult(vertexRadius));
            
            PVector topLeft = startLoc.copy().add(PVector.fromAngle(angle).mult(distance));
            PVector endLoc = vLoc.copy().add(PVector.fromAngle(end).mult(vertexRadius));
            PVector topRight = endLoc.copy().add(PVector.fromAngle(angle).mult(distance));
            
            edgeSettings(e);
            
            line(startLoc, topLeft);
            line(topLeft, topRight);
            line(endLoc, topRight);
            if (isDirected)
            {
                endLoc.add(PVector.fromAngle(angle).mult(headingHeight));
                drawHeading(endLoc, angle + TAU / 2, headingWidth, headingHeight,e);
            }
            
            edgeTextSettings(); // set setting for drawing the label of an edge
            
            edgeCenter = topLeft.copy().add(topRight).div(2);
            text(e.label, edgeCenter.x, edgeCenter.y);
            return;
        }
        
        // more aesthetically pleasing if we offset by 1 if overlap is more than 3
        i += classSize <= 3 ? 0 : 1;         
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
            
            boolean isDirected = !undirected && e.orientedTowards(v);

            // end starts at the edges of the vertex or further if oriented
            // TODO: add generic support for different stroke sizes
            // end + 1 to account for stroke size
            PVector end = edgeCenter.copy().
                          sub(vLoc).
                          normalize().
                          mult((vertexRadius + 1) + (isDirected?headingHeight:0));

            edgeSettings(e); // set settings for drawing an edge line
            line(edgeCenter.x, edgeCenter.y, vLoc.x + end.x, vLoc.y + end.y);

            // draw heading if oriented towards
            if (isDirected)
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
        if (mouseButton == RIGHT)
        {
            // hold vertex that mouse is hovering over
            for (Vertex v : graphs.get(currentGraph).vertices())
            {
                PVector vLoc = vertexLocations.get(v);
                if (VectorUtils.distance(mouseX, mouseY, vLoc.x, vLoc.y) <= vertexRadius)
                {
                    held.add(v);
                    heldOffset = vectorFromTo(mouseX, mouseY, vLoc.x, vLoc.y);
                    return;
                }
            }
        }
    }

    @Override
    public void mouseReleased ()
    {
        if (mouseButton == RIGHT)
        {
            held.clear();
        }
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
                    case "-size":
                    {
                        // TODO: replace parse int with a state machine
                        //       just for funzies
                        // wat do when there are not enough arguments
                        // or arguments are not valid natural numbers
                        initialWidth = Integer.parseInt(args[++i]);
                        initialHeight = Integer.parseInt(args[++i]);
                    } break;
                    case "-tree":
                    {
                        tree = true;
                    } break;
                    default:
                    {
                        flag = flag.substring(1); // advance to the next token
                        boolean flagTripped = false;
                        
                        try
                        {
                            
                            for (Field f : DrawHyperGraph.class.getDeclaredFields())
                            {
                                NamedArgument var = f.getDeclaredAnnotation(NamedArgument.class);
                                if (var != null && flag.equals(f.getName()))
                                {
                                    Converter converter = ((Converter)var.converter().newInstance());
                                    
                                    List<String> parameterArgs = new ArrayList<>();
                                    for (int argCount = 0; argCount < converter.numberOfArguments(); argCount++)
                                    {
                                        i++;
                                        // TODO: i holds the posiiton of the previously used
                                        // probably want to change it so it holds the next usable
                                        String parameterArg = args[i]; // increment i to next command line token
                                        Pattern p = Pattern.compile("^--?");
                                        if (p.matcher(parameterArg).matches())
                                            ; // then error, expected arg but recieved a flag
                                        
                                        parameterArgs.add(parameterArg);
                                    }
                                    
                                    f.set(null, converter.convert(parameterArgs));
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
                            System.out.println(Arrays.toString(args));
                            System.out.printf("Unrecognized flag %s\n", flag);
                            // https://stackoverflow.com/questions/11158235/get-name-of-executable-jar-from-within-main-method
                            // get name of executable if i really want it
                            String debugArgs = String.join(" ", args);
                            System.out.println(debugArgs);
                            StringBuilder debugArrow = new StringBuilder();
                            
                            for (int index = 1; index <= i; index++)
                                debugArrow.append(StringUtils.repititionsOf(" ", args[index - 1].length())).append(" ");
                            debugArrow.append("^");
                            System.out.println(debugArrow);
                            
                            
                            System.out.println("\nValid flags:");
                            printValidFlags();
                            
                            throw new IllegalArgumentException("Argument not recognized");
                        }
                    } break;
                } // end switch on flag
            } // end for each arg
            
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
                
                
                Method graphGeneratingMethod = graphGeneratingClass.getMethod(methodName, methodArgTypes);
//                graphGeneratingMethod = graphGeneratingClass.getMethod(methodName, methodArgTypes);
                
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
                        
                        node.get("vertices").elements().forEachRemaining(vertext ->
                        {
                            JsonNode jsonColor = vertext.get("color");
                            
                            if (jsonColor != null)
                                vertexColors.put(
                                    graphs.get(0).findVertexById(vertext.get("uniqueId").asInt()),
                                    new Color(
                                        jsonColor.get("red").asInt(),
                                        jsonColor.get("green").asInt(), 
                                        jsonColor.get("blue").asInt()));
                        });
                        
                        node.get("edges").elements().forEachRemaining(edge ->
                        {
                            JsonNode jsonColor = edge.get("color");
                            
                            if (jsonColor != null)
                                edgeColors.put(
                                    graphs.get(0).findEdgeById(edge.get("uniqueId").asInt()),
                                    new Color(
                                        jsonColor.get("red").asInt(),
                                        jsonColor.get("green").asInt(),
                                        jsonColor.get("blue").asInt()));
                        });
                    }
                    catch (IOException ex)
                    { System.out.println("Couldn't add color information"); }
                }
                if (readLocationInformationFromInput)
                {
                    try
                    {
                        JsonNode node = new ObjectMapper().readTree(new File((String)methodArgs[0]));
                        
                        node.get("vertices").elements().forEachRemaining(vertext ->
                        {
                            JsonNode jsonLocation = vertext.get("location");
                            
                            if (jsonLocation != null)
                                vertexLocations.put(
                                    graphs.get(0).findVertexById(vertext.get("uniqueId").asInt()),
                                    new PVector(
                                        (float)jsonLocation.get("x").asDouble(),
                                        (float)jsonLocation.get("y").asDouble()));
                        });
                    }
                    catch (IOException ex)
                    { System.out.println("Couldn't add color information"); }
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
            System.out.println(Arrays.toString(args));
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
            NamedArgument config = f.getAnnotation(NamedArgument.class);
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

    
    // TODO: still not sure what I want for a tree placing algorithm
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
                    while (VectorUtils.isLocationAwayFrom(placementLocation, vertexLocations, vertexRadius * 3))
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
            vertexLocations.putIfAbsent
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
    
    // this is dumb
    private static Class<?> classForName (String name)
    {
        try                               { return Class.forName(name); }
        catch (ClassNotFoundException ex) { return null; }
    }
} // end class DrawHyperGraph


// -vertexColor 255 40 60 -fullscreen -vertexRadius 60 -size 400 200 randomGraph int 8 int 4
// -baseVertexColor 255 0 0 -baseEdgeColor 0 255 0 -panIncrement 20 -initialWidth 1000 -vertexRadius 20 undirectedUnlabeledGraphFrom String "0 1 1 0 0~0 0 0 1 1~0 0 0 0 0~0 0 0 0 0~0 0 0 0 0"
// java -jar dist\DrawHyperGraph.jar -readColorInformationFromInput -baseVertexColor 0 0 255 -baseVertexLabelColor 0 0 255 -baseEdgeColor 0 0 255 -baseEdgeLabelColor 0 255 0 graphFromJsonFile String src/draw/json
// -undirected -readLocationInformationFromInput -baseVertexColor 123 222 129  -readColorInformationFromInput graphFromJsonFile String src/draw/json
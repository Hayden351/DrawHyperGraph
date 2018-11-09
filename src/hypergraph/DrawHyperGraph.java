package hypergraph;

import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;

import java.util.TreeMap;
import java.util.TreeSet;
import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class DrawHyperGraph extends PApplet
{
    // track the locations of vertices in the drawing
    TreeMap<Vertex, PVector> vertexLocations;
    // track which vertex is currently being moved by the mouse
    Vertex held;
    Graph G;

    private static final int vertexRadius = 60;
    private static final int headingWidth = 10;
    private static final int headingHeight = 15;

    public static final float PHI = (1 + sqrt(5)) / 2;

// TODO: change font maybe
    PFont font;

    @Override
    public void setup()
    {

        vertexLocations = new TreeMap<>();
        
        G = GenerateGraph.randomGraph(7, 3);
        
        // initially no vertex is being manipulated
        held = null;

        for (Vertex v : G.vertices())
        {
            vertexLocations.put(v, new PVector(random(vertexRadius + 1, width - vertexRadius - 1),
                    random(vertexRadius + 1, height - vertexRadius - 1)));
        }
    }

    @Override
    public void draw() {
        background(255);

        // draw vertices
        for (Vertex v : G.vertices()) {
            PVector vLoc = vertexLocations.get(v);

            vertexSettings();
            ellipse(vLoc.x, vLoc.y, 2 * vertexRadius, 2 * vertexRadius);

            vertexTextSettings();
            text(v.label, vLoc.x, vLoc.y);
        }

        // find all overlapping edges
        // first separate the graph into distinct classes
        // where each class is of edges that have the same set of vertices they connect
        ArrayList<ArrayList<Edge>> classes = new ArrayList<ArrayList<Edge>>();
        for (Edge e : G.edges()) {
            boolean placed = false;
            for (int i = 0; i < classes.size(); i++) {
                // why is it hard coded to 0?
                if (classes.get(i).get(0).sameConnections(e)) {
                    classes.get(i).add(e);
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                ArrayList<Edge> newClass = new ArrayList<Edge>();
                newClass.add(e);
                classes.add(newClass);
            }
        }

        // draw each edge such that they are offset from the previous edge
        for (ArrayList<Edge> edgeClass : classes) {
            int i = 0;
            for (Edge e : edgeClass) {
                drawEdge(e, i++);
            }
        }

        // update
        if (held != null) {
            vertexLocations.put(held,
                    new PVector(
                            constrain(mouseX, vertexRadius + 1, width - vertexRadius - 1),
                            constrain(mouseY, vertexRadius + 1, height - vertexRadius - 1)));
        }

//        // draw test heading
//        // heading is a triangle originating from this point in the canvas 
//        PVector startHeading = new PVector(mouseX, mouseY);
//        // angle heading is pointing in
//        float angle = 0;
//        // width of the base of the triangle
//        float headingWidth = 10;
//        // heigh of the triangle
//        float headingHeight = 10;
//
//        drawHeading(startHeading, angle, headingWidth, headingHeight);
    }

    public void drawEdge(Edge e, int i) {
        // the center that is the average of all the positions of all the vertices that
        // are connect by this edge
        PVector center = new PVector(0, 0);
        for (Vertex v : e.vertices()) {
            center = center.add(vertexLocations.get(v).copy());
        }
        center = center.div(e.vertices.size());

        // add an offset to the center so edges that connect the same vertices are not
        // covering eachother
        float angle = (i * 2 * PI / (1 + PHI));
        float dist = i * vertexRadius / 2;
        PVector offset = new PVector(cos(angle) * dist, sin(angle) * dist);
        center = center.add(offset);

        for (Vertex v : e.vertices()) {
            PVector vLoc = vertexLocations.get(v).copy();

            // vector in direction from vertex point to center point and has the magnitude 
            // of the radius of the vertex
            // TODO: refactor at some point since there is probably some way to combine
            //       the logic from directed and undirected edges
            PVector end;
            if (e.orientedTowards(v)) {
                end = center.copy().sub(vLoc).normalize().mult(vertexRadius + headingHeight);
            } else {
                end = center.copy().sub(vLoc).normalize().mult(vertexRadius);
            }

            edgeSettings(); // set settings for drawing an edge line
            line(center.x, center.y, vLoc.x + end.x, vLoc.y + end.y);

            if (e.orientedTowards(v)) {
                PVector startHeading = vLoc.add(end);
                drawHeading(startHeading, atan2(-end.y, -end.x), headingWidth, headingHeight);
            }

            edgeTextSettings(); // set setting for drawing the label of an edge
            text(e.label, center.x, center.y);
        }
    }

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

    public float distance(float x1, float y1, float x2, float y2) {
        return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    @Override
    public void mousePressed() {
        for (Vertex v : G.vertices()) {
            PVector vLoc = vertexLocations.get(v);
            if (distance(mouseX, mouseY, vLoc.x, vLoc.y) <= vertexRadius) {
                held = v;
                break;
            }
        }
    }

    @Override
    public void mouseReleased() {
        held = null;
    }

    public void headingSettings() {
        noFill();
        stroke(0);
    }

    public void vertexSettings() {
        noFill();
        stroke(0);
    }

    public void vertexTextSettings() {

        fill(0, 0, 255);
        textSize(10);
        textAlign(CENTER);
    }

    public void edgeSettings() {
        noFill();
        stroke(0);
    }

    public void edgeTextSettings() {
        fill(255, 0, 0);
        textSize(14);
        textAlign(CENTER);
    }

    
    

    

    public void settings()
    {
        size(1000, 1000);
    }

    public static void main(String[] args)
    {
        String[] appletArgs = new String[]{"hypergraph.DrawHyperGraph"};
        if (args != null)
        {
            PApplet.main(concat(appletArgs, args));
        }
        else 
        {
            PApplet.main(appletArgs);
        }
    }
}

package draw;

import definition.Vertex;
import java.util.Map;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * @author Hayden Fields
 */
public class VectorUtils
{

    // is true if the given location is away from the given locations by at least
    // distance amount
    public static boolean isLocationAwayFrom (PVector location, Map<Vertex, PVector> locations, float distance)
    {
        for (Vertex u : locations.keySet())
            if (distance(location, locations.get(u)) <= distance)
                return true;
        return false;
    }

    public static float distance (float x1, float y1, float x2, float y2)
    {
        return PApplet.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public static float distance (PVector vLoc, PVector uLoc)
    {
        return VectorUtils.distance(vLoc.x, vLoc.y, uLoc.x, uLoc.y);
    }

}

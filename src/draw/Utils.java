package draw;

import processing.core.PApplet;

/**
 * @author Hayden Fields
 */
public class Utils
{
    public static float distance (float x1, float y1, float x2, float y2)
    {
        return PApplet.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public static String repititionsOf (String rep, int numberOfRepititions)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < numberOfRepititions; i++)
            result.append(rep);
        return result.toString();
    }

}

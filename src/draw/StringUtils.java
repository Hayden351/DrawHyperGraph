package draw;

import definition.Vertex;
import java.util.Map;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * @author Hayden Fields
 */
public class StringUtils
{
    public static String repititionsOf (String rep, int numberOfRepititions)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < numberOfRepititions; i++)
            result.append(rep);
        return result.toString();
    }
}

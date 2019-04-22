package draw;

import java.util.List;

/**
 * @author Hayden Fields
 */
public interface Converter
{
    // return an object can cast it to the type of the field that has the
    // command line configurable annotation
    public Object convert(List<String> args);
    
    public int numberOfArguments();
}

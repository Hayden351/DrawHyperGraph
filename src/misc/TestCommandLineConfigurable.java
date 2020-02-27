package misc;

import populate_from_args.Converter;
import java.awt.Color;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.function.Function;
import populate_from_args.NamedArgument;

/**
 * @author Hayden Fields
 */
public class TestCommandLineConfigurable
{
    public static class GenerateColor implements Converter
    {
        @Override public Color convert (List<String> t) { return new Color (Integer.parseInt(t.get(0)), Integer.parseInt(t.get(1)), Integer.parseInt(t.get(2)));}
        @Override public int numberOfArguments () { return 3; }
    }
    
//    @CommandLineConfigurable(neededParameters=1, description="Testing single values")
//    public static float x;
    
    @NamedArgument(converter=GenerateColor.class, neededParameters=3, description="Testing values that are ordered triples")
    public static Color y;
    
    public static void main (String[] args) throws InstantiationException, IllegalAccessException
    {
        for (Field f : TestCommandLineConfigurable.class.getDeclaredFields())
        {
            NamedArgument annotation = f.getAnnotation(NamedArgument.class);
            Function<List<String>, ?> g = (Function<List<String>, ?>)annotation.converter().newInstance();
            f.set(null, g.apply(new ArrayList<>(asList("1", "2", "3"))));
            
        }
        System.out.println(y);
//        CommandLineConfigurable config = y.getClass().getDeclaredAnnotation(CommandLineConfigurable.class);
//        config.description().getClass().newInstance();
    }
}

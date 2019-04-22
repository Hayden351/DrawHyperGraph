package draw;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author Hayden Fields
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandLineConfigurable
{
    int neededParameters() default 1;

    // TODO: remove or support having different flag names
    String[] flagName() default {};
    
    String description() default "Used for something!";
    
    Class<? extends Converter> converter() default DefaultConverter.class;
    
    public class DefaultConverter implements Converter
    {
        @Override
        public Object convert (List<String> args)
        {
            if (args.isEmpty())
                return true;
            String arg = args.get(0);
            
            if (scannerPattern("boolPattern").matcher(arg).matches())
                return Boolean.parseBoolean(arg);
            else if (scannerPattern("integerPattern").matcher(arg).matches())
                return Integer.parseInt(arg);
            else if (scannerPattern("floatPattern").matcher(arg).matches())
                return Float.parseFloat(arg);
            return null;
        }
        
        @Override public int numberOfArguments () { return 1; }
        
        public static Pattern scannerPattern(String pattern)
        {
            Pattern p = null;
            try
            {
                Method m = Scanner.class.getDeclaredMethod(pattern);
                m.setAccessible(true);
                p = (Pattern)m.invoke(new Scanner(""));
            }
            catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
            { ; }
            return p;
        }
    }
}
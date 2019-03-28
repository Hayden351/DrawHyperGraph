package misc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author Hayden Fields
 */
public class ParseTest
{
    public static void main (String[] args)
    {
        Pattern p = scannerPattern("integerPattern");
        System.out.println(Boolean.parseBoolean("123"));
        System.out.println(("123"));
        System.out.println(p.matcher("asdf").matches());
        
        
    }
    
    public static Pattern scannerPattern(String pattern)
    {
        
        Pattern p = null;
        try
        {
            Method m = Scanner.class.getDeclaredMethod(pattern);
            m.setAccessible(true);
            p = (Pattern)m.invoke(new Scanner(""));
        } catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
        { ex.printStackTrace();}
        return p;
    }
}

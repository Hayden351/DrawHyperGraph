package draw;

import java.util.ArrayList;
import static java.util.Arrays.asList;

/**
 * @author Hayden Fields
 */
public class TestClass
{
    int x;
    int y;
    
    public static void f()
    {
    }
    public void g()
    {
    }
    public static void main (String[] args)
    {
        ArrayList<String> strs = new ArrayList<>(asList("1", "2", "3"));
        
        System.out.println(
        
            strs.stream().
                reduce("", (a,b) -> a + " * " + b)
        );
    }
}

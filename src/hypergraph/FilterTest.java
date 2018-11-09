package hypergraph;

import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Hayden Fields
 */
public class FilterTest
{
    public static void main(String[] args)
    {
        Set<Integer> set = new TreeSet<>();
        
        for (int i = 0; i < 100; i++)
            set.add(i);
        Random r = new Random();
        set.stream().filter(x -> r.nextBoolean());
        
        set.forEach(System.out::println);
    }
}

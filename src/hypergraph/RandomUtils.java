package hypergraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Hayden Fields
 */
public class RandomUtils
{
    public static Random r = new Random();
    
    public static <T> Set<T> randomSubSet(Set<T> set)
    {
        return set.stream().filter(x -> r.nextBoolean()).collect(Collectors.toSet());
    }
    public static Set<Vertex> randomSubSet (TreeSet<Vertex> set, double percentage)
    {
        if (!(0 <= percentage && percentage <= 1))
            throw new IllegalArgumentException("Percentage must be a value between 0 and 1");
        return set.stream().filter(x -> r.nextDouble() < percentage).collect(Collectors.toSet());
    }
    public static <T> Map<T, Boolean> randomMap(Set<T> set)
    {
        Map<T, Boolean> result = new HashMap<>();
        for (T element : set)
            result.put(element, r.nextBoolean());
        return result;
    }
}

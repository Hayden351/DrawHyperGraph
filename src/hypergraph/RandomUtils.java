package hypergraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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
    public static <T> Map<T, Boolean> randomMap(Set<T> set)
    {
        Map<T, Boolean> result = new HashMap<>();
        for (T element : set)
            result.put(element, r.nextBoolean());
        return result;
    }
}

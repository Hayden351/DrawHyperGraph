package misc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Hayden Fields
 */
public class LongestCommonSubstring
{
    public static void main (String[] args)
    {
        String s1 = "actgtg";
        String s2 = "ctgt";
        System.out.println(maximumCommonSubstring(s1, s2));   
    }
    public static String maximumCommonSubstring(String s1, String s2)
    {
        List<String> substrings = substrings(s1);
        for (int length = s2.length(); length > 0; length--)
            for (int pos = 0; pos + length <= s2.length(); pos++)
            {
                String str = s2.substring(pos, pos + length);
                for (int i = 0; i < substrings.size(); i++)
                    if (str.equals(substrings.get(i)))
                        return s2.substring(pos, pos + length);
            }
        return null;
    }
    
    public static List<String> substrings(String s)
    {
        List<String> result = new ArrayList<>();
        for (int length = s.length(); length > 0; length--)
            for (int pos = 0; pos + length <= s.length(); pos++)
                result.add(s.substring(pos, pos + length));
        return result;
    }
}

package misc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Hayden Fields
 */
public class GeneratingName
{
    public static void main (String[] args)
    {
        Map<String, String> context = new HashMap<>();
        context.put("fifththird.filename.prefix", "LTCD");
        context.put("fifththird.filename.bank.identifier", "53FD");
        context.put("fifththird.filename.bank.routing", "01234567");
        context.put("fifththird.filename.separators", "_,_,.,.");
//        context.put("fifththird.filename.segments","filename.prefix,filename.bank.identifier,filename.bank.routing,DATE,TIME");
        context.put("fifththird.filename.segments","CONSTANT:LTCD,fifththird.filename.bank.identifier,fifththird.filename.bank.routing,DATE,TIME");
        
        System.out.println(generateName(context));
    }
    
    public static String generateName(Map<String, String> context)
    {
        StringBuilder name = new StringBuilder();
        
        String[] separators = context.get("fifththird.filename.separators").split(",");
        String[] segments = context.get("fifththird.filename.segments").split(",");
        
        for (int i = 0; i < segments.length; i++)
        {
            if (i != 0) name.append(separators[i - 1]);
            name.append(valueOf(context, segments[i]));
        }
        return name.toString();
    }
    
    public static String valueOf(Map<String, String> context, String value)
    {
        if ("DATE".equals(value))
            return LocalDate.now().toString();
        else if ("TIME".equals(value))
            return LocalTime.now().toString();
        else if (value.startsWith("CONSTANT:"))
            return value.substring("CONSTANT:".length());
        
        String result = context.get(value);
        
        return result;
    }
}


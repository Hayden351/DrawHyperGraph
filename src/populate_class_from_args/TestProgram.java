package populate_class_from_args;

import populate_from_args.PopulateClassFromArgs;
import populate_from_args.NamedArgument;

/**
 * @author Hayden Fields
 */
public class TestProgram
{
    @NamedArgument
    public static int x = 3;
    @NamedArgument
    public static String y = "asdf";
    public static void main (String[] args)
    {
        System.out.println(y);
        args = new String[]{"--y", "jkl;", "--x", "23" };
        PopulateClassFromArgs.populate(TestProgram.class, args);
        
        System.out.println(x);
        System.out.println(y);
    }
}

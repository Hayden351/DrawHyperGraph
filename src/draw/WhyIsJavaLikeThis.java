package draw;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * @author Hayden Fields
 */
public class WhyIsJavaLikeThis
{
    public static void main(String[] args)
    {
        for (Method m : WhyIsJavaLikeThis.class.getDeclaredMethods())
            System.out.println(m);
    }
    public void f()
    {
        int x = 3;
        Function f = y -> "" + x;
    }
    public void g()
    {
        Function f = y -> "" + 3;
    }
}

/*
run:
public static void draw.WhyIsJavaLikeThis.main(java.lang.String[])
public void draw.WhyIsJavaLikeThis.f()
public void draw.WhyIsJavaLikeThis.g()
private static java.lang.Object draw.WhyIsJavaLikeThis.lambda$g$1(java.lang.Object)
private static java.lang.Object draw.WhyIsJavaLikeThis.lambda$f$0(int,java.lang.Object)
BUILD SUCCESSFUL (total time: 0 seconds)
*/

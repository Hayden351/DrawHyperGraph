package draw;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Hayden Fields
 */
public class TestAnnotations
{
    static int x;
    public static void main (String[] args)
    {
//        System.out.println(TestAnnotations.class.getDeclaredFields()[0].getDeclaredAnnotations()[0]);
        System.out.println(TestAnnotations.class.getDeclaredFields()[0].getDeclaredAnnotation(TestAnnotation.class));
        System.out.println();
        
    }
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface TestAnnotation
{
}

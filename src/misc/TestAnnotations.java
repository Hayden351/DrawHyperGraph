package misc;

import java.awt.Color;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Hayden Fields
 */


public class TestAnnotations
{
    static class x implements ArgsToValue<Color>
    {
        @Override
        public Color doTheThing(String[] args, int i)
        {
            return new Color(Integer.parseInt(args[++i]),
            Integer.parseInt(args[++i]),
            Integer.parseInt(args[++i]));
        }
    }
    @TestAnnotation(numberOfArgs = 3, getIt=x.class) static Integer x = 5;
    final Class a = String.class;
    
    // "" implies constructor
    // else method name
    @TestAnnotation(getIt=ArgsToValue.class) Integer y = 5;
    public static void main (String[] args) throws NoSuchFieldException
    {
        args = new String[]{"1", "2", "3", "4"};
        System.out.println(invokeMethod(new ArgsToValue<String>() {
            @Override
            public String doTheThing (String[] args, int i)
            {
                return "asdf";
            }
        }.getClass(), new String[]{}, 0));
//        int x = 3;
        Class x = String.class;
        
        ;
        ;
        Color c = (Color)invokeMethod(
        TestAnnotations.class.getDeclaredField("x").getDeclaredAnnotation(TestAnnotation.class).getIt(),
            args, 
            0);
    }

    private static <T> T invokeMethod (Class<? extends ArgsToValue<T>> aClass, String[] args, int i)
    {
        try
        { 
            aClass.newInstance();
            return (T)aClass.getDeclaredMethods()[0].invoke(aClass.newInstance(), args, i);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
}

interface ArgsToValue<T>
{
    default T doTheThing(String[] args, int i)
    {
        return null;
    }
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface TestAnnotation
{
    int numberOfArgs() default 1;
    Class getIt() default ArgsToValue.class;
    String doIt() default "";
}

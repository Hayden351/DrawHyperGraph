package draw;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

/**
 * @author Hayden Fields
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandLineConfigurable
{
    int neededParameters() default 0;

    // TODO: remove or support having different flag names
    String flagName() default "";
    
    String description() default "Does something!";
} 
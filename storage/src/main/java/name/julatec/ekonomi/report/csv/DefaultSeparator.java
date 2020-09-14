package name.julatec.ekonomi.report.csv;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DefaultSeparator {
    char value();
}

package name.julatec.ekonomi.tribunet;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(
        basePackageClasses = DocumentoAdapterService.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Test.*")
)
public class TestApplication {
}

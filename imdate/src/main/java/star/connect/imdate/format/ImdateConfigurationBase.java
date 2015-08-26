package star.connect.imdate.format;

import org.cwatch.imdate.ImdateClassesConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import({
        ImdateClassesConfiguration.class,
		OvrUpdateXmlTools.class
})
@EnableAutoConfiguration(exclude={
		DataSourceAutoConfiguration.class, 
		RepositoryRestMvcAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class
})
@PropertySource("classpath:imdate.properties")
public class ImdateConfigurationBase {

}

package star.connect.imdate.format;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.cwatch.imdate.ImdateProperties;
import org.cwatch.imdate.ImdateServices;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

@Component
@Import(ImdateServices.class)
public class ConfigService<T> {

	final Class<T> configurationClass;

	final ApplicationContext applicationContext;
	
	final List<Environment> environments = Lists.newArrayList();
	final Map<String, Environment> environmentMap = Maps.newHashMap();
	
	public static class Beans {
		
		final DataSource dataSource;
		final Properties initialContext;
		final ImdateProperties.Env environment;
		public Beans(DataSource dataSource, Properties initialContext, ImdateProperties.Env environment) {
			super();
			this.dataSource = dataSource;
			this.initialContext = initialContext;
			this.environment = environment;
		}
		
	}
	public class Environment {
		
		final String name;
		final Beans inputBeans;
		public Environment(String name, Beans inputBeans) {
			super();
			this.name = name;
			this.inputBeans = inputBeans;
		}


//		final Supplier<JdbcTemplate> jdbcTemplate = Suppliers.memoize(new Supplier<JdbcTemplate>() {
//			@Override
//			public JdbcTemplate get() {
//				return new JdbcTemplate(dataSource);
//			}
//		});
		final Supplier<T> context = Suppliers.memoize(new Supplier<T>() {
			@Override
			public T get() {
				@SuppressWarnings("resource")
				AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
				context.setParent(applicationContext);
				context.getBeanFactory().registerSingleton("environment", inputBeans.environment);
				context.getBeanFactory().registerSingleton("dataSource", inputBeans.dataSource);
				context.getBeanFactory().registerSingleton("cdfInitialContextProperties", inputBeans.initialContext);
				context.register(configurationClass);
				context.refresh();
				return context.getBean(configurationClass);
			}
		});

//		public JdbcTemplate getJdbcTemplate() {
//			return jdbcTemplate.get();
//		}
		
		public T getBeans() {
			return context.get();
		}

	}
	
	
	private void addEnvironment(Environment environment) {
		environments.add(environment);
		environmentMap.put(environment.name, environment);
	}
	
	public ConfigService(
            ImdateServices imdateServices,
            Class<T> configurationClass,
            ApplicationContext applicationContext
    ) {
        this.configurationClass = configurationClass;
        this.applicationContext = applicationContext;
        addEnvironment(new Environment(
				"test", 
				new Beans(
					imdateServices.datasourceTest(), 
					imdateServices.initialContextPropertiesTest(),
					imdateServices.getImdateProperties().getTest()
                )
		));
		addEnvironment(new Environment(
				"preprod", 
				new Beans(
					imdateServices.datasourcePreprod(), 
					imdateServices.initialContextPropertiesPreprod(),
					imdateServices.getImdateProperties().getPreprod()
				)
		));
		addEnvironment(new Environment(
				"prod", 
				new Beans(
					imdateServices.datasourceProd(), 
					imdateServices.initialContextPropertiesProd(),
					imdateServices.getImdateProperties().getProd()
				)
		));
	}
	
	public Environment getEnvironment(String name) {
		return environmentMap.get(name);
	}
	
	public Stream<String> getEnvironmentNames() {
		return environments.stream().map(p -> p.name);
	}
	
}

package star.connect.imdate.format;

import org.cwatch.imdate.ImdateServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Created by pappmar on 03/08/2015.
 */
@Component
public class DefaultConfigService extends ConfigService<EnvironmentConfigurationBase> {
    @Autowired
    public DefaultConfigService(ImdateServices imdateServices, ApplicationContext applicationContext) {
        super(imdateServices, EnvironmentConfigurationBase.class, applicationContext);
    }
}

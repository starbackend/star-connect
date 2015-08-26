package star.connect.imdate.format;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by pappmar on 03/08/2015.
 */

@Configuration
@Import(DefaultConfigService.class)
public class DefaultImdateConfiguration extends  ImdateConfigurationBase {



}

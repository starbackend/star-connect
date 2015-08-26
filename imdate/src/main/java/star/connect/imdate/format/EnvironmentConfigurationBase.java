package star.connect.imdate.format;

import org.cwatch.imdate.ImdateJmsConfiguration;
import org.cwatch.imdate.ImdateJpaConfiguration;
import org.cwatch.imdate.jpa.OvrRepository;
import org.cwatch.imdate.jpa.ProjectsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Component
@Import({
        ImdateJpaConfiguration.class,
        ImdateJmsConfiguration.class,
        ImdateRestClient.class,
        OvrService.class,
        ProjectsOvrService.class
})
public class EnvironmentConfigurationBase {

    @Autowired
    public ProjectsRepository projectsRepository;

    @Autowired
    public OvrRepository ovrRepository;

    @Autowired
    public ImdateRestClient imdateRestClient;

    @Autowired
    public OvrService ovrService;

    @Autowired
    public ProjectsOvrService projectsOvrService;

}

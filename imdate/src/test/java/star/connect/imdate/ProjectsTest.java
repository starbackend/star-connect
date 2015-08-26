package star.connect.imdate;

import org.cwatch.imdate.domain.Projects;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import star.connect.imdate.format.ConfigService;
import star.connect.imdate.format.DefaultConfigService;
import star.connect.imdate.format.DefaultImdateConfiguration;
import star.connect.imdate.format.ProjectsOvrSummary;

import java.util.List;

/**
 * Created by pappmar on 03/08/2015.
 */
public class ProjectsTest {
    @Test
    public void testProjects() {


        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(DefaultImdateConfiguration.class);
        ctx.refresh();

        DefaultConfigService cfg = ctx.getBean(DefaultConfigService.class);

        DefaultConfigService.Environment test = cfg.getEnvironment("test");

        List<Projects> projects = test.getBeans().projectsRepository.findAll();

        System.out.println(projects);

        ProjectsOvrSummary poSummary = test.getBeans().projectsOvrService.getProjectsOvrSummary(projects.get(0).name().get());

        System.out.println(poSummary);




    }
}

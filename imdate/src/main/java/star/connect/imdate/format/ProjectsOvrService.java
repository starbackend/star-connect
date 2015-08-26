package star.connect.imdate.format;

import com.google.common.base.Throwables;
import com.google.common.io.ByteSource;
import eu.europa.emsa.schemas.cdf.v_1_0.projectovr.ProjectOvrRootType;
import org.cwatch.imdate.ImdateDomain;
import org.cwatch.imdate.domain.ProjectsOvr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;

/**
 * Created by pappmar on 11/08/2015.
 */
@Component
public class ProjectsOvrService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    ImdateRestClient imdateRestClient;

    public ProjectsOvrSummary getProjectsOvrSummary(String projectName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProjectsOvrSummary> q = cb.createQuery(ProjectsOvrSummary.class);
        Root<ProjectsOvr> c = q.from(ProjectsOvr.class);
        q.select(
                cb.construct(
                        ProjectsOvrSummary.class,
                        cb.count(c),
                        cb.max(c.get(ImdateDomain.projectsOvr.lastUpdate().name()))
                )
        );
        q.where(cb.equal(c.get(ImdateDomain.projectsOvr.project().name()).get(ImdateDomain.projects.name().name()), projectName));
        return entityManager.createQuery(q).getSingleResult();
    }

    public ImdateRestClient.ImdateRestResponse deleteProjectsOvr(String projectName) {
        return imdateRestClient.deleteProjectsOvr(projectName);
    }



    public ImdateRestClient.ImdateRestResponse populateProjectOvr(JAXBElement<ProjectOvrRootType> cdf) {
        return imdateRestClient.populateProjectOvr(new ByteSource() {
            @Override
            public long copyTo(OutputStream output) throws IOException {
                try {
                    JAXBContext jc = JAXBContext.newInstance(ProjectOvrRootType.class);
                    Marshaller m = jc.createMarshaller();
                    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    m.marshal(cdf, output);
                } catch (JAXBException e) {
                    throw Throwables.propagate(e);
                }
                return -1;
            }

            @Override
            public InputStream openStream() throws IOException {
                return null;
            }
        });
    }
}

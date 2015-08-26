package star.connect.imdate;

import org.junit.Test;
import org.springframework.web.client.RestTemplate;
import star.connect.imdate.format.ImdateRestClient;

/**
 * Created by pappmar on 05/08/2015.
 */
public class ImdateRestClientTest {

    @Test(expected = ImdateRestClient.ImdateRestException.class)
    public void testCreateProjects() {
        ImdateRestClient client = new ImdateRestClient(new RestTemplate(), "http://twls55:7020", "CARREPH1");
        client.createProject("test", "test");
    }
}

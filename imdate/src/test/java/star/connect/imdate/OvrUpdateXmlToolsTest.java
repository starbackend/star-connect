package star.connect.imdate;

import hu.mapro.mfw.model.ModelPool;
import org.cwatch.imdate.domain.VesselIds;
import org.junit.Test;
import star.connect.imdate.format.OvrUpdateXmlTools;

/**
 * Created by pappmar on 06/08/2015.
 */
public class OvrUpdateXmlToolsTest {

    @Test
    public void test() {
        OvrUpdateXmlTools tools = new OvrUpdateXmlTools();
        VesselIds ids = ModelPool.newInstance(VesselIdsImpl.class);
        System.out.println(tools.createCdfString(ids));
    }

}

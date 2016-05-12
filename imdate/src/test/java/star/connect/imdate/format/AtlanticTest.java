package star.connect.imdate.format;

import com.google.common.base.Throwables;
import eu.europa.emsa.efca.atlantic.Atlantic;
import eu.europa.emsa.schemas.cdf.v_1_0.projectovr.ProjectOvrRootType;
import org.junit.Test;
import star.connect.imdate.format.format.AtlanticFormat;
import star.connect.imdate.format.format.FormatResult;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by pappmar on 13/08/2015.
 */
public class AtlanticTest {


    @Test
    public void test() throws JAXBException {
        AtlanticFormat format = new AtlanticFormat();

        FormatResult formatResult = new FormatResult() {

            @Override
            public void error(String message, Throwable e) {
                System.out.println(message);
                System.out.println(e);
//                throw Throwables.propagate(e);
            }

            @Override
            public void error(long lineNumber, String field, String message, Throwable e) {
                System.out.println(lineNumber);
                System.out.println(field);
                System.out.println(message);
                System.out.println(e);
//                throw Throwables.propagate(e);
            }
        };

        Atlantic atlantic = format.parseCSV(AtlanticTest.class.getResourceAsStream("/atlantic_schema_invalid.csv"), formatResult);

        JAXBContext jc = JAXBContext.newInstance(Atlantic.class, ProjectOvrRootType.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(atlantic, new File("target/atlantic.xml"));


        format.validate(atlantic, formatResult);

        JAXBElement<ProjectOvrRootType> cdf = format.convertToCDF(atlantic);

        m.marshal(cdf, new File("target/atlantic.cdf.xml"));




    }

    @Test
    public void test2() throws JAXBException, FileNotFoundException {
        AtlanticFormat format = new AtlanticFormat();

        FormatResult formatResult = new FormatResult() {

            @Override
            public void error(String message, Throwable e) {
                System.out.println(message);
                System.out.println(e);
//                throw Throwables.propagate(e);
            }

            @Override
            public void error(long lineNumber, String field, String message, Throwable e) {
                System.out.println(lineNumber);
                System.out.println(field);
                System.out.println(message);
                System.out.println(e);
//                throw Throwables.propagate(e);
            }
        };

//        Atlantic atlantic = format.parseCSV(AtlanticTest.class.getResourceAsStream("/atlantic_valid.csv"), formatResult);
        Atlantic atlantic = format.parseCSV(new FileInputStream("target/atl_input.csv"), formatResult);

        JAXBContext jc = JAXBContext.newInstance(Atlantic.class, ProjectOvrRootType.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(atlantic, new File("target/atlantic2.xml"));


        format.validate(atlantic, formatResult);

        JAXBElement<ProjectOvrRootType> cdf = format.convertToCDF(atlantic);

        m.marshal(cdf, new File("target/atlantic2.cdf.xml"));




    }
}

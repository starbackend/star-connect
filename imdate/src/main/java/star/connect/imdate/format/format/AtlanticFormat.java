package star.connect.imdate.format.format;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import eu.europa.emsa.efca.atlantic.Atlantic;
import eu.europa.emsa.efca.atlantic.ObjectFactory;
import eu.europa.emsa.schemas.cdf.v_1_0.projectovr.ProjectOvrMessageType;
import eu.europa.emsa.schemas.cdf.v_1_0.projectovr.ProjectOvrRootType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by pappmar on 12/08/2015.
 */
@Component
public class AtlanticFormat {

    private static final ObjectFactory ATLANTIC = new ObjectFactory();
    private static final eu.europa.emsa.schemas.cdf.v_1_0.projectovr.ObjectFactory CDF = new eu.europa.emsa.schemas.cdf.v_1_0.projectovr.ObjectFactory();
    private static final String atlanticNamespace = Atlantic.class.getPackage().getAnnotation(XmlSchema.class).namespace();

    public JAXBElement<ProjectOvrRootType> processCSV(
            InputStream csv,
            String targetProject,
            FormatResult result
    ) {
        Atlantic atlantic = parseCSV(csv, result);

        if (atlantic==null) {
            return null;
        }

        validate(atlantic, result);

        return convertToCDF(atlantic, "EFCA", targetProject);
    }

    public Atlantic parseCSV(
            InputStream csv,
            FormatResult result
    ) {
        try {
            CSVParser parser = CSVFormat.EXCEL.withHeader().withSkipHeaderRecord().parse(new InputStreamReader(csv));

            Atlantic root = ATLANTIC.createAtlantic();

            ImmutableBiMap<String, Integer> header = ImmutableBiMap.copyOf(parser.getHeaderMap());


            for (final CSVRecord record : parser) {
                int index = 0;

                Atlantic.Vessel vessel = ATLANTIC.createAtlanticVessel();
                root.getVessel().add(vessel);

                try {

                    vessel.setFlag(string(record, index++));
                    vessel.setEuCode(bool(record, index++));
                    vessel.setIr(string(record, index++));
                    vessel.setVesselName(string(record, index++));
                    vessel.setIrcs(string(record, index++));
                    vessel.setExtMarking(string(record, index++));
                    vessel.setPortName(string(record, index++));
                    vessel.setVmsCode(bool(record, index++));
                    vessel.setMMSI(integer(record, index++));
                    vessel.setType(string(record, index++));
                    vessel.setGear(string(record, index++));
                    vessel.setGT(decimal(record, index++));
                    vessel.setLOA(decimal(record, index++));
                    vessel.setKW(decimal(record, index++));
                    vessel.setCatchPermit(bool(record, index++));
                    vessel.setPermitType(string(record, index++));
                    vessel.setPermitFrom(date(record, index++));
                    vessel.setPermitTo(date(record, index++));
                    vessel.setNafoCode(bool(record, index++));
                    vessel.setGHL(bool(record, index++));
                    vessel.setIccatCode(bool(record, index++));
                    vessel.setIccatId(string(record, index++));
                    vessel.setNEAFCCode(bool(record, index++));

                    if (
                            vessel.getIr()==null
                            &&
                            vessel.getMMSI()==null
                    ) {
                        result.error(record.getRecordNumber()+1, null, "At least one of IR or MMSI must be specified.", null);
                    }

                } catch (Exception e) {
                    result.error(record.getRecordNumber()+1, header.inverse().get(index-1),  e.getMessage(), e);
                }
            }

            return root;


        } catch (IOException e) {
            result.error(e.getMessage(), e);
        }

        return null;
    }

    public JAXBElement<ProjectOvrRootType> convertToCDF(
            Atlantic atlantic
    ) {
        return convertToCDF(atlantic, "EFCA", "ATLANTIC");
    }

    private static final XmlAdapter<String, XMLGregorianCalendar> calendarAdapter = new XmlAdapter<String, XMLGregorianCalendar>() {

        @Override
        public XMLGregorianCalendar unmarshal(String string) throws Exception {
            Date date = dateFormat.parse(string);
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(date);
            XMLGregorianCalendar xmlDate = DTF.newXMLGregorianCalendarDate(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH)+1,
                    cal.get(Calendar.DAY_OF_MONTH),
                    DatatypeConstants.FIELD_UNDEFINED
            );
            return xmlDate;
        }

        @Override
        public String marshal(XMLGregorianCalendar v) throws Exception {
            if (v==null) return null;
            return dateFormat.format(v.toGregorianCalendar().getTime());
        }

    };

    private static final TypeAdapter<XMLGregorianCalendar> jsonCalendarAdapter = new TypeAdapter<XMLGregorianCalendar>() {
        @Override
        public void write(JsonWriter out, XMLGregorianCalendar value) throws IOException {
            try {
                out.value(calendarAdapter.marshal(value));
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }

        @Override
        public XMLGregorianCalendar read(JsonReader in) throws IOException {
            try {
                return calendarAdapter.unmarshal(in.nextString());
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
    };

    public JAXBElement<ProjectOvrRootType> convertToCDF(
            Atlantic atlantic,
            String source,
            String project
    ) {
        ProjectOvrRootType root = CDF.createProjectOvrRootType();
        root.setSource(source);
        root.setTimestamp(DTF.newXMLGregorianCalendar(new GregorianCalendar()));
        root.setId(source + "-" + new Date().toString());

        TypeAdapter<Boolean> booleanAdapter = new TypeAdapter<Boolean>() {
            @Override
            public void write(JsonWriter out, Boolean value) throws IOException {
                if (value == Boolean.FALSE) {
                    out.value("N");
                } else if (value == Boolean.TRUE) {
                    out.value("Y");
                } else {
                    out.nullValue();
                }
            }

            @Override
            public Boolean read(JsonReader in) throws IOException {
                return null;
            }
        }; Gson gson = new GsonBuilder()
                .setFieldNamingStrategy(f ->
                        Optional.ofNullable(f.getAnnotation(XmlElement.class))
                                .map(XmlElement::name)
                                .orElse(f.getName())
                )
                .registerTypeAdapter(XMLGregorianCalendar.class, jsonCalendarAdapter)
                .registerTypeAdapter(boolean.class, booleanAdapter)
                .registerTypeAdapter(Boolean.class, booleanAdapter)
                .create();

        try {
//            Map<String, Object> properties = new HashMap<String, Object>(2);
//            properties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
//            properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);


//            Marshaller m = jc.createMarshaller();
//            m.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");

            for (Atlantic.Vessel vessel : atlantic.getVessel()) {
                ProjectOvrMessageType msg = CDF.createProjectOvrMessageType();
                msg.setCallSign(vessel.getIrcs());
                msg.setIR(vessel.getIr());
                msg.setMMSI(vessel.getMMSI());
                msg.setName(vessel.getVesselName());
                msg.getProject().add(project);

                msg.setJsonData(gson.toJson(vessel));

                root.getProjectOvrMessage().add(msg);
            }

            return CDF.createEMSA(root);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public void validate(
            Atlantic atlantic,
            FormatResult result
    ) {
        try {
            JAXBContext jc = JAXBContext.newInstance(Atlantic.class);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(AtlanticFormat.class.getResource("/atlantic.xsd"));

            AtlanticValidationHandler handler = new AtlanticValidationHandler(result);
            Validator validator = schema.newValidator();
            validator.setErrorHandler(handler);

            Marshaller marshaller = jc.createMarshaller();
            marshaller.setSchema(schema);
            marshaller.setEventHandler(handler);
            marshaller.marshal(atlantic, handler);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }


    private static DatatypeFactory DTF;
    private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    static {
        try {
            DTF = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw Throwables.propagate(e);
        }
    }
    private static String string(CSVRecord record, int index) {
        String value = record.get(index);
        String string = value;
        if (string==null) {
            return null;
        }

        string = string.trim();
        if (string.isEmpty()) {
            return null;
        }

        return string;
    }
    private static XMLGregorianCalendar date(CSVRecord record, int index) {
        String value = record.get(index);
        String string = value;
        if (string==null) {
            return null;
        }

        string = string.trim();
        if (string.isEmpty()) {
            return null;
        }

        try {
            return calendarAdapter.unmarshal(string);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static BigDecimal decimal(CSVRecord record, int index) {
        String value = record.get(index);
        String string = value;
        if (string==null) {
            return null;
        }

        string = string.trim();
        if (string.isEmpty()) {
            return null;
        }

        string = string.replaceAll(",", ".");

        return new BigDecimal(string);
    }

    private static Integer integer(CSVRecord record, int index) {
        String value = record.get(index);
        String string = value;
        if (string==null) {
            return null;
        }

        string = string.trim();
        if (string.isEmpty()) {
            return null;
        }

        return Integer.parseInt(string);
    }

    private static Boolean bool(CSVRecord record, int index) {
        String value = record.get(index);
        String string = value;
        if (string==null) {
            return null;
        }

        string = string.trim();
        if (string.isEmpty()) {
            return null;
        }

        if ("N/A".equals(string)) {
            return null;
        }

        string = string.toUpperCase();
        if ("Y".equals(string)) {
            return true;
        } else if ("N".equals(string) || "F".equals(string)) {
            return false;
        } else {
            throw new RuntimeException("Not a boolean value: " + value);
        }
    }

    private static class AtlanticValidationHandler extends DefaultHandler implements ValidationEventHandler {
        private final FormatResult result;
        int line = 0;
        String element;

        public AtlanticValidationHandler(FormatResult result) {
            this.result = result;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (atlanticNamespace.equals(uri) && localName.equals(Atlantic.Vessel.class.getSimpleName())) {
                line++;
            }

            element = localName;
        }


        @Override
        public boolean handleEvent(ValidationEvent event) {
            result.error(line+1, element, event.getMessage(), null);
            return true;
        }
    }
}

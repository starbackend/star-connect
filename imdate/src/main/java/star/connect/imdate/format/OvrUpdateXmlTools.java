package star.connect.imdate.format;

import eu.europa.emsa.schemas.cdf.v_1_0.shipparticulars.ObjectFactory;
import eu.europa.emsa.schemas.cdf.v_1_0.shipparticulars.ShipParticularsMessageType;
import eu.europa.emsa.schemas.cdf.v_1_0.shipparticulars.ShipParticularsRootType;
import org.cwatch.imdate.cdf.v_1_0.ImdateCdfTools;
import org.cwatch.imdate.domain.VesselIds;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBElement;
import java.util.GregorianCalendar;
import java.util.Objects;

@Component
public class OvrUpdateXmlTools {

	/**
	 * Creates a Ship Particulars CDF JAXB object from a bean containing
	 * certain vesseld identifiers. 
	 * 
	 * @param ids the bean containing the vessel identifiers, probably received
	 * 		from the web layer
	 * @return the jaxb element corresponding the the recevied identifiers
	 */
	public JAXBElement<ShipParticularsRootType> createCdf(VesselIds ids) {
		ObjectFactory of = new ObjectFactory();
		
		ShipParticularsRootType spr = of.createShipParticularsRootType();
		ShipParticularsMessageType spm = of.createShipParticularsMessageType();
		spr.getShipParticularsMessage().add(spm);
		
		spm.setEMSAId(Objects.toString(ids.imdateId().get(), null));
		spm.setMMSI(ids.mmsi().get());
		spm.setIMO(Objects.toString(ids.imo().get(), null));
		spm.setName(ids.shipName().get());
		spm.setCallSign(ids.callSign().get());
		spm.setIR(ids.irNumber().get());
		spm.setFlagState(ids.flagState().get());
		
		spr.setTimestamp(ImdateCdfTools.dtf.newXMLGregorianCalendar(new GregorianCalendar()));
		spr.setSource("OVRUPDATER");
		
		return of.createEMSA(spr);
	}
	
	/**
	 * Creates a string that contains the formatted XML representation
	 * of the Ship particulars CDF document that corresponds to the
	 * received ship identifiers.
	 *  
	 * @param ids the vessel identifiers that the returned cdf document should contain
	 * @return a formatted ship particulars cdf xml document string
	 * @see OvrUpdateXmlTools#createCdf(VesselIds)
	 */
	public String createCdfString(VesselIds ids) {
		JAXBElement<ShipParticularsRootType> cdf = createCdf(ids);
		return ImdateCdfTools.prettyPrintRemoveUnused(cdf);
	}
	
}

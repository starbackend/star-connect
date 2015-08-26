package star.connect.imdate.format;

import com.google.common.collect.Lists;
import hu.mapro.mfw.model.Field;
import hu.mapro.mfw.model.ModelPool;
import org.cwatch.imdate.domain.Ovr;
import org.cwatch.imdate.domain.VesselIds;
import org.cwatch.imdate.jpa.OvrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Component
public class OvrService {

    @Autowired
    OvrUpdateXmlTools ovrUpdateXmlTools;

    @Autowired
    OvrRepository ovrRepository;

    @Autowired
    JmsTemplate ovrJmsTemplate;

    public OvrService() {
        OVR = ModelPool.newInstance(Ovr.class);
    }

    final private Ovr OVR;

    public List<? extends VesselIds> search(final VesselIds vesselIds) {

        List<Ovr> ovrs = ovrRepository.findAll(new Specification<Ovr>() {
            @Override
            public Predicate toPredicate(Root<Ovr> root, CriteriaQuery<?> query,
                                         CriteriaBuilder cb) {
                List<Predicate> preds = Lists.newArrayList();

                root.fetch(OVR.identitySource().name());

                field(root, cb, preds, vesselIds, OVR.imdateId());
                field(root, cb, preds, vesselIds, OVR.imo());
                field(root, cb, preds, vesselIds, OVR.mmsi());
                field(root, cb, preds, vesselIds, OVR.callSign());
                field(root, cb, preds, vesselIds, OVR.irNumber());
                field(root, cb, preds, vesselIds, OVR.shipName());

                return cb.or(preds.toArray(new Predicate[preds.size()]));
            }

            private <E, V, F extends Field<E, V>> void field(Root<Ovr> root, CriteriaBuilder cb, List<Predicate> preds, E source, F field) {
                V value = field.get(source);
                if (value != null) {
                    preds.add(cb.equal(root.get(field.name()), value));
                }
            }
        });

        return ovrs;
    }

    /**
     * Creates a shipparticulars cdf xml messages and sends it to the
     * ovr queue of the cdfWeblogicConnectionFactory imdate jms server.
     *
     * @param ids the vessel identifiers to include in the cdf message
     * @see OvrUpdateXmlTools#createCdfString(VesselIds)
     * @see org.cwatch.imdate.ImdateJmsConfiguration#ovrJmsTemplate(javax.jms.ConnectionFactory, org.springframework.jms.support.destination.JndiDestinationResolver)
     */
    public void send(VesselIds ids) {
        send(ovrUpdateXmlTools.createCdfString(ids));
    }

    public void send(String xml) {
        ovrJmsTemplate.convertAndSend(xml);
    }
}

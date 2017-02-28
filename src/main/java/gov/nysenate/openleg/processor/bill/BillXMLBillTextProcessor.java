package gov.nysenate.openleg.processor.bill;

import gov.nysenate.openleg.model.base.SessionYear;
import gov.nysenate.openleg.model.base.Version;
import gov.nysenate.openleg.model.bill.Bill;
import gov.nysenate.openleg.model.bill.BillAmendment;
import gov.nysenate.openleg.model.bill.BillId;
import gov.nysenate.openleg.model.process.DataProcessUnit;
import gov.nysenate.openleg.model.sobi.SobiFragment;
import gov.nysenate.openleg.model.sobi.SobiFragmentType;
import gov.nysenate.openleg.processor.base.AbstractDataProcessor;
import gov.nysenate.openleg.processor.sobi.SobiProcessor;
import gov.nysenate.openleg.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Created by Chenguang He(gaoyike@gmail.com) on 2016/12/1.
 */
// TODO : figure out how to get the correct published_date_time to be stored in the bill_change_log table
@Service
public class BillXMLBillTextProcessor extends AbstractDataProcessor implements SobiProcessor {
    private static final Logger logger = LoggerFactory.getLogger(BillXMLBillTextProcessor.class);
    @Autowired
    private XmlHelper xmlHelper;

    public BillXMLBillTextProcessor() {}

    @Override
    public void init() {
        initBase();
    }

    @Override
    public SobiFragmentType getSupportedType() {
        return SobiFragmentType.BILLTEXT;
    }

    @Override
    public void process(SobiFragment sobiFragment) {
        LocalDateTime date = sobiFragment.getPublishedDateTime();
        logger.info("Processing " + sobiFragment.getFragmentId() + " (xml file).");
        DataProcessUnit unit = createProcessUnit(sobiFragment);
        try {
            final Document doc = xmlHelper.parse(sobiFragment.getText());
            final Node billTextNode = xmlHelper.getNode("billtext_html",doc);
            final int sessionYear = xmlHelper.getInteger("@sessyr",billTextNode);
            final String senhse = xmlHelper.getString("@senhse",billTextNode).replaceAll("\n","");
            final String senno = xmlHelper.getString("@senno",billTextNode).replaceAll("\n","");
            final String senamd = xmlHelper.getString("@senamd",billTextNode).replaceAll("\n","");
            final String asmhse = xmlHelper.getString("@asmhse",billTextNode).replaceAll("\n","");
            final String asmno = xmlHelper.getString("@asmno",billTextNode).replaceAll("\n","");
            final String asmamd = xmlHelper.getString("@asmamd",billTextNode).replaceAll("\n","");
            final String action = xmlHelper.getString("@action",billTextNode).replaceAll("\n",""); // TODO: implement actions
            final String billText = billTextNode.getTextContent().replaceAll("\n"," ");
            if (!senhse.isEmpty() && !asmhse.isEmpty()){ // uni bill
                //update senate
                final Version version1 = Version.of(senamd);
                final Bill baseBill1 = getOrCreateBaseBill(sobiFragment.getPublishedDateTime(), new BillId(senhse + senno, new SessionYear(sessionYear), version1), sobiFragment);
                baseBill1.getAmendment(version1).setFullText(billText);
                billIngestCache.set(baseBill1.getBaseBillId(), baseBill1, sobiFragment);
                //update assmbly
                final Version version2 = Version.of(asmamd);
                final Bill baseBill2 = getOrCreateBaseBill(sobiFragment.getPublishedDateTime(), new BillId(asmhse+asmno, new SessionYear(sessionYear), version2), sobiFragment);
                baseBill2.getAmendment(version2).setFullText(billText);
                billIngestCache.set(baseBill2.getBaseBillId(), baseBill2, sobiFragment);
            }
            else {
                final Version version = Version.of(senamd.isEmpty() ? asmamd : senamd);
                final Bill baseBill = getOrCreateBaseBill(sobiFragment.getPublishedDateTime(), new BillId(senhse.isEmpty() ? asmhse + asmno : senhse + senno, new SessionYear(sessionYear), version), sobiFragment);
                baseBill.getAmendment(version).setFullText(billText);
                billIngestCache.set(baseBill.getBaseBillId(), baseBill, sobiFragment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postProcess() {
        flushBillUpdates();
    }

}

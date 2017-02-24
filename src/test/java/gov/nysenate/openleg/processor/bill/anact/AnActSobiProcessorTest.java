package gov.nysenate.openleg.processor.bill.anact;

import gov.nysenate.openleg.dao.bill.data.BillDao;
import gov.nysenate.openleg.model.base.Version;
import gov.nysenate.openleg.model.bill.Bill;
import gov.nysenate.openleg.model.bill.BillId;
import gov.nysenate.openleg.processor.BaseXmlProcessorTest;
import gov.nysenate.openleg.processor.sobi.SobiProcessor;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;

/**
 * Created by robert on 2/15/17.
 */
@Transactional
public class AnActSobiProcessorTest extends BaseXmlProcessorTest {

    @Autowired BillDao billDao;
    @Autowired AnActSobiProcessor anActSobiProcessor;

    private static final Logger logger = LoggerFactory.getLogger(AnActSobiProcessorTest.class);

    @Override
    protected SobiProcessor getSobiProcessor() {
        return anActSobiProcessor;
    }

    @Test
    public void processReplaceTest() throws Exception {
        final String anactXmlFilePath = "processor/bill/anact/2016-12-02-09.16.10.220257_ANACT_S08215.XML";

        processXmlFile(anactXmlFilePath);

        Bill b = billDao.getBill(new BillId("S08215", 2015));
        String expectedS = "AN ACT to amend the executive law, in relation to the appointment of\n" +
                "interpreters to be used in parole board proceedings [altered]";
        String actualClause = b.getAmendment(Version.DEFAULT).getActClause();
        assertEquals(expectedS, actualClause);
    }

    @Test
    public void processRemoveTest() throws Exception {
        final String anactXmlFilePath = "processor/bill/anact/2017-02-09-12.36.50.736583_ANACT_A05462.XML";

        processXmlFile(anactXmlFilePath);

        Bill b = billDao.getBill(new BillId("A05462", 2017));
        String expectedS = "";
        String actualClause = b.getAmendment(Version.DEFAULT).getActClause();
        assertEquals(expectedS, actualClause);
    }

}


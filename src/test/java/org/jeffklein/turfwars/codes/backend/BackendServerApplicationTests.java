package org.jeffklein.turfwars.codes.backend;

import junit.framework.Assert;
import org.jeffklein.turfwars.codes.backend.controller.TempCodeRestController;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BackendServerApplication.class)
@WebAppConfiguration
public class BackendServerApplicationTests {

    @Test
    public void testTwUkTempCodeDateManipulation() {
        String dateStr = "May 10";
        String timeStr = "01:44 PM";
        DateTime mergeDateTime = TempCodeRestController.combineDateAndTime(dateStr, timeStr);
        Assert.assertEquals(mergeDateTime.toString(), "2015-05-10T13:44:00.000Z");
    }

}

package org.cru.godtools.article.aem.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AEMDownloadManagerTest {
    @Test
    public void testRoundTimestamp() {
        assertEquals(15000, AEMDownloadManger.roundTimestamp(15234, 1000));
    }
}

package org.keynote.godtools.android.event;

import junit.framework.Assert;

import org.junit.Test;
import org.keynote.godtools.renderer.crureader.bo.GPage.Event.GodToolsEvent;

/**
 * Created by dsgoers on 4/15/16.
 */
public class EventIDTest {

    private final GodToolsEvent.EventID eventID1 = new GodToolsEvent.EventID("FOLLOWup","subSCRIBE");
    private final GodToolsEvent.EventID eventID2 = new GodToolsEvent.EventID("followUP","SUBScribe");

    @Test
    public void hashCodeTest()
    {
        Assert.assertEquals(eventID1.hashCode(), eventID2.hashCode());
    }

    @Test
    public void isEqualTest()
    {
        Assert.assertEquals(eventID1, eventID2);
    }

}

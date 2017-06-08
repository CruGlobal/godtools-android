package org.cru.godtools.base.model;

import org.cru.godtools.base.model.Event.Id;
import org.junit.Test;

import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.cru.godtools.base.model.Event.Id.SUBSCRIBE_EVENT;
import static org.cru.godtools.base.model.Event.Id.parse;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class EventIdTest {
    private static final Id ID1 = new Id("FOLLOWup", "subSCRIBE");
    private static final Id ID2 = new Id("followUP", "SUBScribe");

    @Test
    public void verifyHashCode() {
        assertEquals(ID1.hashCode(), ID2.hashCode());
    }

    @Test
    public void verifyEquals() {
        assertEquals(ID1, ID2);
    }

    @Test
    public void verifyParse() throws Exception {
        final Set<Id> ids = parse("kgp", "FollowUp:subscribe followup:SUBSCRIBE event1 blargh kgp:event1");
        assertThat(ids, containsInAnyOrder(SUBSCRIBE_EVENT, new Id("kgp", "event1"), new Id("kgp", "blargh")));
    }
}

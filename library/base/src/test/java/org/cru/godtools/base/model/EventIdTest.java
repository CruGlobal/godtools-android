package org.cru.godtools.base.model;

import org.cru.godtools.base.model.Event.Id;
import org.junit.Test;

import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.cru.godtools.base.model.Event.Id.FOLLOWUP_EVENT;
import static org.cru.godtools.base.model.Event.Id.parse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

public class EventIdTest {
    private static final Id ID1 = new Id("FOLLOWup", "SEnd");
    private static final Id ID2 = new Id("followUP", "SEnd");

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
        final Set<Id> ids = parse("kgp", "FollowUp:SeNd followup:SEND event1 blargh kgp:event1");
        assertThat(ids, containsInAnyOrder(FOLLOWUP_EVENT, new Id("kgp", "event1"), new Id("kgp", "blargh")));
    }

    @Test
    public void verifyParseEmpty() {
        final Set<Id> ids = parse("kgp", "");
        assertThat(ids, empty());
    }
}

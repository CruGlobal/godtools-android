package org.cru.godtools.base.model

import org.cru.godtools.base.model.Event.Id.Companion.FOLLOWUP_EVENT
import org.cru.godtools.base.model.Event.Id.Companion.parse
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.junit.Assert.assertEquals
import org.junit.Test

private val ID1 = Event.Id("FOLLOWup", "SEnd")
private val ID2 = Event.Id("followUP", "SEnd")

class EventIdTest {
    @Test
    fun verifyHashCode() {
        assertEquals(ID1.hashCode(), ID2.hashCode())
    }

    @Test
    fun verifyEquals() {
        assertEquals(ID1, ID2)
    }

    @Test
    fun verifyParse() {
        assertThat(
            parse("kgp", "FollowUp:SeNd followup:SEND event1 blargh kgp:event1"),
            containsInAnyOrder(FOLLOWUP_EVENT, Event.Id("kgp", "event1"), Event.Id("kgp", "blargh"))
        )
    }

    @Test
    fun verifyParseEmpty() {
        assertThat(parse("kgp", ""), empty())
    }
}

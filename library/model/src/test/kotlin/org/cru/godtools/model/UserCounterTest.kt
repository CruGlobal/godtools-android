package org.cru.godtools.model

import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class UserCounterTest {
    // region jsonapi parsing
    private val jsonApiConverter by lazy {
        JsonApiConverter.Builder()
            .addClasses(UserCounter::class.java)
            .build()
    }

    @Test
    fun testJsonApiParsing() {
        val counter = parseJson("user_counter.json").dataSingle!!
        assertEquals("tool_opens", counter.id)
        assertEquals(41, counter.apiCount)
        assertEquals(26.000768, counter.apiDecayedCount, 0.000001)
    }

    private fun parseJson(file: String) = this::class.java.getResourceAsStream(file)!!.reader()
        .use { jsonApiConverter.fromJson(it.readText(), UserCounter::class.java) }
    // endregion jsonapi parsing
}

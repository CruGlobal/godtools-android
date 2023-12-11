package org.cru.godtools.model

import java.time.Instant
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.converter.InstantConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class UserTest {
    // region jsonapi parsing
    private val jsonApiConverter by lazy {
        JsonApiConverter.Builder()
            .addClasses(User::class.java)
            .addClasses(Tool::class.java)
            .addConverters(InstantConverter())
            .build()
    }

    @Test
    fun testJsonApiParsing() {
        val user = parseJson("user.json").dataSingle!!
        assertEquals("11", user.id)
        assertEquals(Instant.parse("2022-01-28T14:47:48Z"), user.createdAt)
        assertEquals(1, user.apiFavoriteTools.size)
        assertEquals(2L, user.apiFavoriteTools[0].apiId)
    }

    private fun parseJson(file: String) = this::class.java.getResourceAsStream(file)!!.reader()
        .use { jsonApiConverter.fromJson(it.readText(), User::class.java) }
    // endregion jsonapi parsing
}

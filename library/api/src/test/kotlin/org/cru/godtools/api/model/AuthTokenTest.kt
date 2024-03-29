package org.cru.godtools.api.model

import kotlin.test.Test
import kotlin.test.assertEquals
import org.ccci.gto.android.common.jsonapi.JsonApiConverter

class AuthTokenTest {
    // region jsonapi parsing
    private val jsonApiConverter by lazy {
        JsonApiConverter.Builder()
            .addClasses(AuthToken::class.java)
            .build()
    }

    @Test
    fun testJsonApiParsing() {
        val token = parseJson("auth_token.json")

        assertEquals("1", token.userId)
        assertEquals("jwt_auth_token", token.token)
    }

    private fun parseJson(file: String) = this::class.java.getResourceAsStream(file)!!.reader()
        .use { jsonApiConverter.fromJson(it.readText(), AuthToken::class.java).dataSingle!! }
    // endregion jsonapi parsing
}

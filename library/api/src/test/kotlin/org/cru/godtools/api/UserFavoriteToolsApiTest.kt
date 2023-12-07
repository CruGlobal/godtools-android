package org.cru.godtools.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import net.javacrumbs.jsonunit.assertj.assertThatJson
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiConverterFactory
import org.cru.godtools.api.UserFavoriteToolsApi.Companion.PATH_FAVORITE_TOOLS
import org.cru.godtools.model.Tool
import org.junit.Rule
import retrofit2.Retrofit

private const val JSON_RESPONSE_FAVORITES = "{data:[{id:5,type:\"resource\"}]}"

class UserFavoriteToolsApiTest {
    @get:Rule
    val server = MockWebServer()

    private var api = Retrofit.Builder()
        .baseUrl(server.url("/"))
        .addConverterFactory(JsonApiConverterFactory(Tool::class.java))
        .build()
        .create(UserFavoriteToolsApi::class.java)

    @Test
    fun `removeFavoriteTools()`() = runTest {
        server.enqueue(MockResponse().setBody(JSON_RESPONSE_FAVORITES))

        val tool = Tool("en", Tool.Type.TRACT, apiId = 1)
        val resp = api.removeFavoriteTools(tools = listOf(tool)).body()!!.data.single()
        assertNotNull(resp) { assertEquals(5, it.apiId) }

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/$PATH_FAVORITE_TOOLS", request.path)
        assertThatJson(request.body.readUtf8()) {
            isObject
            node("data").isArray.hasSize(1)
            node("data[0].id").isEqualTo(1)
            node("data[0].attributes").isAbsent()
            node("data[0].relationships").isAbsent()
        }
    }
}

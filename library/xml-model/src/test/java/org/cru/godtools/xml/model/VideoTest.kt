package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VideoTest {
    private lateinit var manifest: Manifest

    @Before
    fun setup() {
        manifest = Manifest(TOOL_CODE)
    }

    @Test
    fun testParseVideo() {
        val video = Video(manifest, getXmlParserForResource("video.xml"))
        assertEquals(Video.Provider.YOUTUBE, video.provider)
        assertEquals("test-id", video.videoId)
    }
}

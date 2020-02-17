package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HeroTest {
    @Test
    fun testParseHero() {
        val hero = Hero(Manifest(), getXmlParserForResource("hero.xml"))
        assertThat(hero.analyticsEvents, hasSize(1))
        assertEquals("Heading", hero.heading!!.mText)
        assertThat(
            hero.content,
            contains(instanceOf(Image::class.java), instanceOf(Paragraph::class.java), instanceOf(Tabs::class.java))
        )
    }

    @Test
    fun testParseHeroIgnoredContent() {
        val hero = Hero(Manifest(), getXmlParserForResource("hero_ignored_content.xml"))
        assertThat(hero.content, contains(instanceOf(Paragraph::class.java), instanceOf(Tabs::class.java)))
    }
}

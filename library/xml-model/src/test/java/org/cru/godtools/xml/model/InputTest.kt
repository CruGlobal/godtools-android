package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.R
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InputTest {
    private lateinit var manifest: Manifest

    @Before
    fun setup() {
        manifest = Manifest(TOOL_CODE)
    }

    @Test
    fun testParseInputHidden() {
        val input = Input(manifest, getXmlParserForResource("input_hidden.xml"))
        assertEquals(Input.Type.HIDDEN, input.type)
        assertEquals("destination_id", input.name)
        assertEquals("1", input.value)
        assertFalse(input.required)

        // test validateValue
        assertNull(input.validateValue(null))
        assertNull(input.validateValue(""))
        assertNull(input.validateValue("     "))
    }

    @Test
    fun testParseInputText() {
        val input = Input(manifest, getXmlParserForResource("input_text.xml"))
        assertEquals(Input.Type.TEXT, input.type)
        assertEquals("name", input.name)
        assertEquals("Name", input.label!!.text)
        assertEquals("First Name and Last Name", input.placeholder!!.text)
        assertTrue(input.required)

        // test validateValue
        assertEquals(R.string.tract_content_input_error_required, input.validateValue(null)!!.msgId)
        assertEquals(R.string.tract_content_input_error_required, input.validateValue("")!!.msgId)
        assertEquals(R.string.tract_content_input_error_required, input.validateValue("     ")!!.msgId)
        assertNull(input.validateValue(" a "))
    }

    @Test
    fun testParseInputEmail() {
        val input = Input(manifest, getXmlParserForResource("input_email.xml"))
        assertEquals(Input.Type.EMAIL, input.type)
        assertEquals("email", input.name)
        assertTrue(input.required)

        // test validateValue
        assertEquals(R.string.tract_content_input_error_required, input.validateValue(null)!!.msgId)
        assertEquals(R.string.tract_content_input_error_required, input.validateValue("")!!.msgId)
        assertEquals(R.string.tract_content_input_error_required, input.validateValue("     ")!!.msgId)
        assertEquals(R.string.tract_content_input_error_invalid_email, input.validateValue("a")!!.msgId)
        assertNull(input.validateValue("a@example.com"))
    }
}

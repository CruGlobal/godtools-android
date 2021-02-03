package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.model.tract.Modal
import org.cru.godtools.xml.model.tract.TractPage
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModalTest {
    @Test
    fun testModalButtonsDefaultToOutlined() {
        val modal = Modal(TractPage(Manifest()))
        val button = Button(modal)
        assertEquals(Button.Style.OUTLINED, button.style)
    }
}

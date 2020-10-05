package org.cru.godtools.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AttachmentTest {
    @Test
    fun verifyLocalFilename() {
        val attachment = Attachment()
        attachment.fileName = "file.jpg"
        assertNull(attachment.localFileName)
        attachment.sha256 = "sha256"
        assertEquals("sha256.jpg", attachment.localFileName)
        attachment.fileName = null
        assertEquals("sha256.bin", attachment.localFileName)
        attachment.fileName = "file"
        assertEquals("sha256.bin", attachment.localFileName)
    }
}

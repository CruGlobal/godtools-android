package org.cru.godtools.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AttachmentTest {
    @Test
    fun verifyLocalFilename() {
        val attachment = Attachment()
        attachment.filename = "file.jpg"
        assertNull(attachment.localFilename)
        attachment.sha256 = "sha256"
        assertEquals("sha256.jpg", attachment.localFilename)
        attachment.filename = null
        assertEquals("sha256.bin", attachment.localFilename)
        attachment.filename = "file"
        assertEquals("sha256.bin", attachment.localFilename)
    }
}

package org.cru.godtools.articles.aem.db;

import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;
import org.cru.godtools.articles.aem.service.AEMDownloadManger;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;

public class AEMDownloadMangerTest extends DBBaseTest {

    @Test
    public void verifyAttachmentsAreSaved() {
        for (Article article : mSavedArticles) {
            for (Attachment attachment : mAttachmentDao.getTestableAttachmentsByArticle(article.mkey)) {
                try {
                    AEMDownloadManger
                            .saveAttachmentToStorage(attachment, context);
                } catch (IOException e) {
                    fail("Data was not saved");
                }
            }
        }

        for (Article article : mSavedArticles) {
            for (Attachment attachment : mAttachmentDao.getTestableAttachmentsByArticle(article.mkey)) {
                assertFalse(attachment.mAttachmentFilePath != null);
            }
        }
    }
}

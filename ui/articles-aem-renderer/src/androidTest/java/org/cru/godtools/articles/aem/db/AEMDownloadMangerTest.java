package org.cru.godtools.articles.aem.db;

import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;
import org.cru.godtools.articles.aem.service.AEMDownloadManger;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertFalse;

public class AEMDownloadMangerTest extends DBBaseTest {

    @Test
    public void verifyAttachmentsAreSaved() throws IOException {
        for (Article article : mSavedArticles) {
            for (Attachment attachment : mAttachmentDao.getTestableAttachmentsByArticle(article.mkey)) {
                    AEMDownloadManger
                            .saveAttachmentToStorage(attachment, context);
            }
        }

        for (Article article : mSavedArticles) {
            for (Attachment attachment : mAttachmentDao.getTestableAttachmentsByArticle(article.mkey)) {
                assertFalse(attachment.mAttachmentFilePath != null);
            }
        }
    }
}

package org.cru.godtools.articles.aem.db;

import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;
import org.cru.godtools.articles.aem.service.AEMDownloadManger;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertFalse;

public class AEMDownloadMangerTest extends DBBaseTest {

    @Test
    @Ignore
    public void verifyAttachmentsAreSaved() throws IOException {
        AEMDownloadManger manger = AEMDownloadManger.getInstance(mContext);
        for (Article article : mSavedArticles) {
            for (Attachment attachment : mAttachmentDao.getTestableAttachmentsByArticle(article.mkey)) {
                    manger.saveAttachmentToStorage(attachment);
            }
        }

        for (Article article : mSavedArticles) {
            for (Attachment attachment : mAttachmentDao.getTestableAttachmentsByArticle(article.mkey)) {
                assertFalse(attachment.mAttachmentFilePath != null);
            }
        }
    }
}

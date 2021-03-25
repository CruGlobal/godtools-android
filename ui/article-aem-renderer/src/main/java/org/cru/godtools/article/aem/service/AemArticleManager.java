package org.cru.godtools.article.aem.service;

import org.cru.godtools.article.aem.api.AemApi;
import org.cru.godtools.article.aem.db.ArticleRoomDatabase;
import org.cru.godtools.article.aem.util.AemFileManager;
import org.cru.godtools.base.tool.service.ManifestManager;
import org.keynote.godtools.android.db.GodToolsDao;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * This class hold all the logic for maintaining a local cache of AEM Articles.
 */
@Singleton
public class AemArticleManager extends KotlinAemArticleManager {
    @Inject
    AemArticleManager(final GodToolsDao dao, final AemApi api,
                      final ManifestManager manifestManager, final ArticleRoomDatabase aemDb,
                      final AemFileManager fileManager) {
        super(aemDb, api, dao, fileManager, manifestManager);
    }
}

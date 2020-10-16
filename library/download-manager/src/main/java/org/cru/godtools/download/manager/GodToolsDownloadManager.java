package org.cru.godtools.download.manager;

import android.annotation.SuppressLint;

import org.cru.godtools.api.AttachmentsApi;
import org.cru.godtools.api.TranslationsApi;
import org.cru.godtools.base.FileManager;
import org.cru.godtools.base.Settings;
import org.greenrobot.eventbus.EventBus;
import org.keynote.godtools.android.db.GodToolsDao;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;

@Singleton
@SuppressLint("VisibleForTests")
public final class GodToolsDownloadManager extends KotlinGodToolsDownloadManager {
    @Inject
    GodToolsDownloadManager(@NonNull final AttachmentsApi attachmentsApi,
                            @NonNull final TranslationsApi translationsApi, @NonNull final GodToolsDao dao,
                            @NonNull final EventBus eventBus, @NonNull final FileManager fileManager,
                            @NonNull final Settings settings) {
        super(attachmentsApi, dao, eventBus, fileManager, settings, translationsApi);
    }
}

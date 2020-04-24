package org.cru.godtools.init.content.task;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.common.io.Closer;

import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.model.Language;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.Translation;
import org.keynote.godtools.android.db.GodToolsDao;

import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import timber.log.Timber;

public class InitialContentTasks implements Runnable {
    private final AssetManager mAssets;
    private final GodToolsDao mDao;
    private final GodToolsDownloadManager mDownloadManager;

    public InitialContentTasks(@NonNull final Context context) {
        mDao = GodToolsDao.Companion.getInstance(context);
        mAssets = context.getAssets();
        mDownloadManager = GodToolsDownloadManager.getInstance(context);
    }

    @Override
    @WorkerThread
    public void run() {
        importBundledTranslations();
    }

    private void importBundledTranslations() {
        try {
            for (final String file : mAssets.list("translations")) {
                // short-circuit if this translation doesn't exist, or is already downloaded
                final Translation translation = mDao.find(Translation.class, file.substring(0, file.lastIndexOf('.')));
                if (translation == null || translation.isDownloaded()) {
                    continue;
                }

                // short-circuit if the tool or language are not added to this device
                final Tool tool = mDao.find(Tool.class, translation.getToolCode());
                if (tool == null || !tool.isAdded()) {
                    continue;
                }
                final Language language = mDao.find(Language.class, translation.getLanguageCode());
                if (language == null || !language.isAdded()) {
                    continue;
                }

                // short-circuit if a newer translation is already downloaded
                final Translation latestTranslation =
                        mDao.getLatestTranslation(translation.getToolCode(), translation.getLanguageCode())
                                .orElse(null);
                if (latestTranslation != null && latestTranslation.isDownloaded()) {
                    continue;
                }

                try {
                    final String fileName = "translations/" + file;

                    // open zip file
                    final Closer closer = Closer.create();
                    try {
                        final InputStream in = closer.register(mAssets.open(fileName));
                        mDownloadManager.storeTranslation(translation, in, -1);
                    } catch (final IOException e) {
                        throw closer.rethrow(e);
                    } finally {
                        closer.close();
                    }

                } catch (final Exception e) {
                    Timber.tag("InitialContentTasks")
                            .e(e, "Error importing bundled translation %s-%s-%d (%s)", tool.getCode(),
                               language.getCode(), translation.getVersion(), file);
                }
            }
        } catch (final Exception e) {
            Timber.tag("InitialContentTasks")
                    .e(e, "Error importing bundled translations");
        }
    }
}

package org.cru.godtools.init.content.task;

import android.content.Context;
import android.content.res.AssetManager;

import com.annimon.stream.Stream;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closer;
import com.google.common.util.concurrent.Futures;

import org.ccci.gto.android.common.compat.util.LocaleCompat;
import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.jsonapi.JsonApiConverter;
import org.ccci.gto.android.common.jsonapi.converter.LocaleTypeConverter;
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject;
import org.ccci.gto.android.common.util.IOUtils;
import org.ccci.gto.android.common.util.LocaleUtils;
import org.cru.godtools.base.Settings;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.init.content.BuildConfig;
import org.cru.godtools.model.Attachment;
import org.cru.godtools.model.Language;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.Translation;
import org.cru.godtools.model.event.AttachmentUpdateEvent;
import org.cru.godtools.model.event.LanguageUpdateEvent;
import org.cru.godtools.model.event.ToolUpdateEvent;
import org.cru.godtools.model.event.TranslationUpdateEvent;
import org.cru.godtools.model.jsonapi.ToolTypeConverter;
import org.greenrobot.eventbus.EventBus;
import org.keynote.godtools.android.db.Contract.LanguageTable;
import org.keynote.godtools.android.db.GodToolsDao;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import timber.log.Timber;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;

public class InitialContentTasks implements Runnable {
    private static final String SYNC_TIME_DEFAULT_TOOLS = "last_synced.default_tools";

    private final AssetManager mAssets;
    private final Settings mSettings;
    private final Context mContext;
    private final GodToolsDao mDao;
    private final GodToolsDownloadManager mDownloadManager;
    private final EventBus mEventBus;

    @Nullable
    private JsonApiConverter mJsonApiConverter;

    public InitialContentTasks(@NonNull final Context context) {
        mContext = context.getApplicationContext();
        mDao = GodToolsDao.Companion.getInstance(context);
        mAssets = context.getAssets();
        mSettings = Settings.Companion.getInstance(context);
        mDownloadManager = GodToolsDownloadManager.getInstance(context);
        mEventBus = EventBus.getDefault();
    }

    @Override
    @WorkerThread
    public void run() {
        // languages init
        loadBundledLanguages();
        initSystemLanguages();

        // tools init
        loadBundledTools();
        initDefaultTools();
        importBundledTranslations();
        importBundledAttachments();
    }

    @NonNull
    private JsonApiConverter getJsonApiConverter() {
        if (mJsonApiConverter == null) {
            mJsonApiConverter = new JsonApiConverter.Builder()
                    .addClasses(Language.class)
                    .addClasses(Tool.class, Translation.class, Attachment.class)
                    .addConverters(ToolTypeConverter.INSTANCE)
                    .addConverters(new LocaleTypeConverter())
                    .build();
        }
        return mJsonApiConverter;
    }

    private void loadBundledLanguages() {
        // short-circuit if we already have any languages loaded
        if (!mDao.get(Query.select(Language.class).limit(1)).isEmpty()) {
            return;
        }

        try {
            // read in raw languages json
            final Closer closer = Closer.create();
            final String raw;
            try {
                final InputStream in = closer.register(mAssets.open("languages.json"));
                raw = IOUtils.readString(in);
            } catch (final IOException e) {
                throw closer.rethrow(e);
            } finally {
                closer.close();
            }

            // convert to a usable object
            final JsonApiObject<Language> languages = getJsonApiConverter().fromJson(raw, Language.class);

            // store languages in the database
            final long changed = mDao.inTransaction(() -> {
                return Stream.of(languages.getData())
                        .mapToLong(l -> mDao.insert(l, CONFLICT_IGNORE))
                        .sum();
            });

            // send a broadcast if we inserted any languages
            if (changed > 0) {
                mEventBus.post(LanguageUpdateEvent.INSTANCE);
            }
        } catch (final Exception e) {
            // log exception, but it shouldn't be fatal (for now)
            Timber.tag("InitialContentTasks")
                    .e(e, "Error loading bundled languages");
        }
    }

    private void initSystemLanguages() {
        // check to see if we have any languages currently added
        if (mDao.get(Query.select(Language.class).where(LanguageTable.SQL_WHERE_ADDED).limit(1)).isEmpty()) {
            final Locale[] locales = LocaleUtils.getFallbacks(Locale.getDefault(), Locale.ENGLISH);

            // add any languages active on the device
            Stream.of(locales)
                    .forEach(mDownloadManager::addLanguage);

            // set the primary language to first preferred language available
            Stream.of(locales)
                    .filter(l -> mDao.find(Language.class, l) != null)
                    .findFirst()
                    .ifPresent(mSettings::setPrimaryLanguage);
        }

        // always add english
        mDownloadManager.addLanguage(Locale.ENGLISH);

        // add any bundled languages
        for (final String code : BuildConfig.BUNDLED_LANGUAGES) {
            mDownloadManager.addLanguage(LocaleCompat.forLanguageTag(code));
        }
    }

    private void loadBundledTools() {
        // short-circuit if we already have any tools loaded
        if (!mDao.get(Query.select(Tool.class).limit(1)).isEmpty()) {
            return;
        }

        try {
            // read in raw tools json
            final Closer closer = Closer.create();
            final String raw;
            try {
                final InputStream in = closer.register(mAssets.open("tools.json"));
                raw = IOUtils.readString(in);
            } catch (final IOException e) {
                throw closer.rethrow(e);
            } finally {
                closer.close();
            }

            // convert to a usable object
            final JsonApiObject<Tool> tools = getJsonApiConverter().fromJson(raw, Tool.class);

            // add any tools that we don't already have
            final AtomicBoolean toolsChanged = new AtomicBoolean(false);
            final AtomicBoolean translationsChanged = new AtomicBoolean(false);
            final AtomicBoolean attachmentsChanged = new AtomicBoolean(false);
            mDao.inTransaction(() -> {
                for (final Tool tool : tools.getData()) {
                    if (mDao.refresh(tool) == null) {
                        if (mDao.insert(tool, CONFLICT_IGNORE) > 0) {
                            toolsChanged.set(true);

                            // import all bundled translations
                            final List<Translation> translations = tool.getLatestTranslations();
                            if (translations != null) {
                                final long changes = Stream.of(tool.getLatestTranslations())
                                        .mapToLong(t -> mDao.insert(t, CONFLICT_IGNORE))
                                        .sum();
                                if (changes > 0) {
                                    translationsChanged.set(true);
                                }
                            }

                            // import all bundled attachments
                            final List<Attachment> attachments = tool.getAttachments();
                            if (attachments != null) {
                                final long changes = Stream.of(tool.getAttachments())
                                        .mapToLong(a -> mDao.insert(a, CONFLICT_IGNORE))
                                        .sum();
                                if (changes > 0) {
                                    attachmentsChanged.set(true);
                                }
                            }
                        }
                    }
                }
                return true;
            });

            // send a broadcast if we inserted any tools, translations, or attachments
            if (toolsChanged.get()) {
                mEventBus.post(ToolUpdateEvent.INSTANCE);
            }
            if (translationsChanged.get()) {
                mEventBus.post(TranslationUpdateEvent.INSTANCE);
            }
            if (attachmentsChanged.get()) {
                mEventBus.post(AttachmentUpdateEvent.INSTANCE);
            }
        } catch (final Exception e) {
            // log exception, but it shouldn't be fatal (for now)
            Timber.tag("InitialContentTasks")
                    .e(e, "Error loading bundled tools");
        }
    }

    @WorkerThread
    private void initDefaultTools() {
        // check to see if we have initialized the default tools before
        if (mDao.getLastSyncTime(SYNC_TIME_DEFAULT_TOOLS) > 0) {
            return;
        }

        // add any bundled tools as the default tools
        for (final String code : BuildConfig.BUNDLED_TOOLS) {
            Futures.getUnchecked(mDownloadManager.addTool(code));
        }

        // update the last sync time
        mDao.updateLastSyncTime(SYNC_TIME_DEFAULT_TOOLS);
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

    private void importBundledAttachments() {
        try {
            // bundled attachments
            final Set<String> files = ImmutableSet.copyOf(mAssets.list("attachments"));

            // find any attachments that aren't download, but we came bundled with the resource for
            final List<Attachment> attachments = mDao.streamCompat(Query.select(Attachment.class))
                    .filterNot(Attachment::isDownloaded)
                    .filter(a -> files.contains(a.getLocalFileName()))
                    .toList();

            for (final Attachment attachment : attachments) {
                final Closer closer = Closer.create();
                try {
                    final InputStream in =
                            closer.register(mAssets.open("attachments/" + attachment.getLocalFileName()));

                    mDownloadManager.importAttachment(attachment, in);
                } catch (final Throwable t) {
                    throw closer.rethrow(t);
                } finally {
                    closer.close();
                }
            }
        } catch (final Exception e) {
            Timber.tag("InitialContentTasks")
                    .e(e, "Error importing bundled attachments");
        }
    }
}

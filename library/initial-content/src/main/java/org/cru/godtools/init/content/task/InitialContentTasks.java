package org.cru.godtools.init.content.task;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.annimon.stream.Stream;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closer;

import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.db.Transaction;
import org.ccci.gto.android.common.jsonapi.JsonApiConverter;
import org.ccci.gto.android.common.jsonapi.converter.LocaleTypeConverter;
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject;
import org.ccci.gto.android.common.util.IOUtils;
import org.ccci.gto.android.common.util.LocaleUtils;
import org.cru.godtools.base.Settings;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
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
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;

public class InitialContentTasks implements Runnable {
    private static final ImmutableMap<Long, String> BUNDLED_TRANSLATIONS = ImmutableMap.of(
            388L, "fourlaws-en-38.zip",
            428L, "satisfied-en-50.zip",
            523L, "kgp-en-111.zip"
    );

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
        mDao = GodToolsDao.getInstance(context);
        mAssets = context.getAssets();
        mSettings = Settings.getInstance(context);
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
        importBundledTranslations();
        importBundledAttachments();
    }

    @NonNull
    private JsonApiConverter getJsonApiConverter() {
        if (mJsonApiConverter == null) {
            mJsonApiConverter = new JsonApiConverter.Builder()
                    .addClasses(Language.class)
                    .addClasses(Tool.class, Translation.class, Attachment.class)
                    .addConverters(new ToolTypeConverter())
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
            long changed;
            final Transaction tx = mDao.newTransaction();
            try {
                tx.beginTransaction();
                changed = Stream.of(languages.getData())
                        .mapToLong(l -> mDao.insert(l, CONFLICT_IGNORE))
                        .sum();
                tx.setTransactionSuccessful();
            } finally {
                tx.endTransaction().recycle();
            }

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
    }

    private void loadBundledTools() {
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
            boolean toolsChanged = false;
            boolean translationsChanged = false;
            boolean attachmentsChanged = false;
            final Transaction tx = mDao.newTransaction();
            try {
                tx.beginTransaction();

                for (final Tool tool : tools.getData()) {
                    if (mDao.refresh(tool) == null) {
                        if (mDao.insert(tool, CONFLICT_IGNORE) > 0) {
                            toolsChanged = true;
                            if (tool.getCode() != null) {
                                mDownloadManager.addTool(tool.getCode());
                            }

                            // import all bundled translations
                            final List<Translation> translations = tool.getLatestTranslations();
                            if (translations != null) {
                                translationsChanged = Stream.of(tool.getLatestTranslations())
                                        .mapToLong(t -> mDao.insert(t, CONFLICT_IGNORE))
                                        .sum() > 0 || translationsChanged;
                            }

                            // import all bundled attachments
                            final List<Attachment> attachments = tool.getAttachments();
                            if (attachments != null) {
                                attachmentsChanged = Stream.of(tool.getAttachments())
                                        .mapToLong(a -> mDao.insert(a, CONFLICT_IGNORE))
                                        .sum() > 0 || attachmentsChanged;
                            }
                        }
                    }
                }
                tx.setSuccessful();
            } finally {
                tx.endTransaction().recycle();
            }

            // send a broadcast if we inserted any tools, translations, or attachments
            if (toolsChanged) {
                mEventBus.post(new ToolUpdateEvent());
            }
            if (translationsChanged) {
                mEventBus.post(new TranslationUpdateEvent());
            }
            if (attachmentsChanged) {
                mEventBus.post(new AttachmentUpdateEvent());
            }
        } catch (final Exception e) {
            // log exception, but it shouldn't be fatal (for now)
            Timber.tag("InitialContentTasks")
                    .e(e, "Error loading bundled tools");
        }
    }

    private void importBundledTranslations() {
        for (final Map.Entry<Long, String> bundle : BUNDLED_TRANSLATIONS.entrySet()) {
            // short-circuit if this translation doesn't exist, or is already downloaded
            final Translation translation = mDao.find(Translation.class, bundle.getKey());
            if (translation == null || translation.isDownloaded()) {
                continue;
            }

            try {
                final String fileName = "translations/" + bundle.getValue();

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
                        .e(e, "Error importing bundled translations");
            }
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

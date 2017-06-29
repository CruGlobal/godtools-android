package org.cru.godtools.init.content.task;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.annimon.stream.Stream;
import com.crashlytics.android.Crashlytics;
import com.google.common.io.Closer;

import org.ccci.gto.android.common.compat.util.LocaleCompat;
import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.jsonapi.JsonApiConverter;
import org.ccci.gto.android.common.jsonapi.converter.LocaleTypeConverter;
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject;
import org.ccci.gto.android.common.util.IOUtils;
import org.cru.godtools.base.Settings;
import org.cru.godtools.model.event.LanguageUpdateEvent;
import org.cru.godtools.model.event.ToolUpdateEvent;
import org.cru.godtools.model.event.TranslationUpdateEvent;
import org.cru.godtools.sync.service.GodToolsDownloadManager;
import org.greenrobot.eventbus.EventBus;
import org.keynote.godtools.android.db.Contract.LanguageTable;
import org.keynote.godtools.android.db.GodToolsDao;
import org.keynote.godtools.android.model.Attachment;
import org.keynote.godtools.android.model.Language;
import org.keynote.godtools.android.model.Tool;
import org.keynote.godtools.android.model.Translation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;

public class InitialContentTasks implements Runnable {
    private final AssetManager mAssets;
    private final Settings mSettings;
    private final Context mContext;
    private final GodToolsDao mDao;
    private final GodToolsDownloadManager mDownloadManager;
    private final EventBus mEventBus;

    @Nullable
    JsonApiConverter mJsonApiConverter;

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
    }

    @NonNull
    private JsonApiConverter getJsonApiConverter() {
        if (mJsonApiConverter == null) {
            mJsonApiConverter = new JsonApiConverter.Builder()
                    .addClasses(Language.class)
                    .addClasses(Tool.class, Translation.class, Attachment.class)
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
            final long changed = Stream.of(languages.getData()).mapToLong(l -> mDao.insert(l, CONFLICT_IGNORE)).sum();

            // send a broadcast if we inserted any languages
            if (changed > 0) {
                mEventBus.post(new LanguageUpdateEvent());
            }
        } catch (final Exception e) {
            // log exception, but it shouldn't be fatal (for now)
            Crashlytics.logException(e);
        }
    }

    private void initSystemLanguages() {
        // check to see if we have any languages currently added
        if (mDao.get(Query.select(Language.class).where(LanguageTable.SQL_WHERE_ADDED).limit(1)).isEmpty()) {
            final Locale[] locales = LocaleCompat.getFallbacks(Locale.getDefault(), Locale.ENGLISH);

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
        // short-circuit if we already have any tools
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
            final long toolsChanged = Stream.of(tools.getData())
                    .mapToLong(t -> mDao.insert(t, CONFLICT_IGNORE))
                    .sum();
            final long translationsChanged = Stream.of(tools.getData())
                    .map(Tool::getLatestTranslations)
                    .flatMap(Stream::of)
                    .mapToLong(t -> mDao.insert(t, CONFLICT_IGNORE))
                    .sum();

            // send a broadcast if we inserted any tools or translations
            if (toolsChanged > 0) {
                mEventBus.post(new ToolUpdateEvent());
            }
            if (translationsChanged > 0) {
                mEventBus.post(new TranslationUpdateEvent());
            }

            // add default tools
            // TODO: make actual list of default tools and not just all tools
            mDao.streamCompat(Query.select(Tool.class))
                    .mapToLong(Tool::getId)
                    .forEach(mDownloadManager::addTool);
        } catch (final Exception e) {
            // log exception, but it shouldn't be fatal (for now)
            Crashlytics.logException(e);
        }
    }
}

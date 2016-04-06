package org.keynote.godtools.android.tasks;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.crashlytics.android.Crashlytics;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.io.Closer;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.gson.GsonIgnoreExclusionStrategy;
import org.ccci.gto.android.common.util.IOUtils;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.dao.DBContract.GTLanguageTable;
import org.keynote.godtools.android.model.Followup;
import org.keynote.godtools.android.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class InitialContentTasks {
    final Context mContext;
    final DBAdapter mDao;
    final AssetManager mAssets;

    public InitialContentTasks(@NonNull final Context context) {
        mContext = context.getApplicationContext();
        mDao = DBAdapter.getInstance(mContext);
        mAssets = context.getAssets();
    }

    @NonNull
    public ListenableFuture<Object> loadFollowups() {
        final ListenableFuture<Cursor> followups = mDao.getCursorAsync(Query.select(Followup.class).limit(1));
        return Futures.transform(followups, new InitFollowups());
    }

    @NonNull
    public ListenableFuture<Object> loadDefaultLanguages() {
        final ListenableFuture<Cursor> languages = mDao.getCursorAsync(Query.select(GTLanguage.class).limit(1));
        return Futures.transform(languages, new InitLanguages());
    }

    /**
     * @param languagesTask the {@link InitialContentTasks#loadDefaultLanguages()} task that we wait to complete before
     *                      starting this task
     */
    @NonNull
    public ListenableFuture<Object> loadBundledPackages(@NonNull final ListenableFuture<Object> languagesTask) {
        return Futures.transform(languagesTask, new InitPackages());
    }

    @NonNull
    public ListenableFuture<Long> installEveryStudentPackage() {
        // Add Every Student to database (Asynchronously)
        final GTPackage everyStudent = new GTPackage();
        everyStudent.setCode(GTPackage.EVERYSTUDENT_PACKAGE_CODE);
        everyStudent.setName(mContext.getString(R.string.app_name_everystudent));
        everyStudent.setIcon("homescreen_everystudent_icon_2x.png");
        everyStudent.setStatus("live");
        everyStudent.setLanguage("en");
        everyStudent.setVersion("1.1");
        return mDao.insertAsync(everyStudent, SQLiteDatabase.CONFLICT_IGNORE);
    }

    final class InitFollowups implements Function<Cursor, Object> {
        @Override
        public Object apply(final Cursor c) {
            try {
                if (c != null && c.getCount() == 0) {
                    loadDefaultFollowups();
                }
            } catch (final IOException e) {
                Crashlytics.logException(e);
                throw Throwables.propagate(e);
            } finally {
                if (c != null) {
                    c.close();
                }
            }

            return null;
        }

        @WorkerThread
        private void loadDefaultFollowups() throws IOException {
            // configure Gson
            final Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .setExclusionStrategies(new GsonIgnoreExclusionStrategy())
                    .create();

            // parse current followups out of the json file
            final Closer closer = Closer.create();
            try {
                final InputStream in = closer.register(mContext.getAssets().open("followup.json"));
                final Reader reader = closer.register(new InputStreamReader(in));
                final ArrayList<Followup> followups =
                        gson.fromJson(reader, new TypeToken<ArrayList<Followup>>() {}.getType());

                for (final Followup followup : followups) {
                    mDao.updateOrInsert(followup);
                }
            } catch (final Throwable t) {
                throw closer.rethrow(t);
            } finally {
                closer.close();
            }

        }
    }

    final class InitLanguages implements Function<Cursor, Object> {
        @Override
        public Object apply(final Cursor c) {
            try {
                if (c != null && c.getCount() == 0) {
                    loadDefaultLanguages();
                }
            } catch (final IOException e) {
                Crashlytics.logException(e);
                throw Throwables.propagate(e);
            } finally {
                if (c != null) {
                    c.close();
                }
            }

            return null;
        }

        @WorkerThread
        private void loadDefaultLanguages() throws IOException {
            // meta.xml file contains the list of supported languages
            InputStream metaStream = mAssets.open("meta.xml");
            List<GTLanguage> languageList = GTPackageReader.processMetaResponse(metaStream);
            for (GTLanguage gtl : languageList) {
                mDao.updateOrInsert(gtl, GTLanguageTable.COL_NAME, GTLanguageTable.COL_DRAFT);
            }
        }
    }

    final class InitPackages implements Function<Object, Object> {
        @Nullable
        @Override
        public Object apply(@Nullable Object input) {
            try {
                if (loadMissingPackages()) {
                    copyResources();

                    // mark english resources as downloaded
                    GTLanguage gtlEnglish = new GTLanguage("en");
                    gtlEnglish.setDownloaded(true);
                    mDao.update(gtlEnglish, GTLanguageTable.COL_DOWNLOADED);
                }
            } catch (final IOException e) {
                Crashlytics.logException(e);
                throw Throwables.propagate(e);
            }
            return null;
        }

        @WorkerThread
        private boolean loadMissingPackages() throws IOException {
            boolean loaded = false;
            final Closer closer = Closer.create();
            try {
                // contents.xml file contains information about the bundled english resources
                InputStream in = closer.register(mAssets.open("contents.xml"));
                final List<GTPackage> packages = GTPackageReader.processContentFile(in);
                for (GTPackage gtp : packages) {
                    final GTPackage current = mDao.refresh(gtp);
                    if (current == null || current.compareVersionTo(gtp) < 0) {
                        mDao.updateOrInsert(gtp);
                        loaded = true;
                    }
                }
            } catch (final Throwable t) {
                throw closer.rethrow(t);
            } finally {
                closer.close();
            }

            return loaded;
        }

        @WorkerThread
        private void copyResources() throws IOException {
            // copy the files from assets/english to documents directory
            final File resourcesDir = FileUtils.getResourcesDir(mContext);
            for (String fileName : mAssets.list("english")) {
                final Closer closer = Closer.create();
                try {
                    final InputStream in = closer.register(mAssets.open("english/" + fileName));
                    final OutputStream os = closer.register(new FileOutputStream(new File(resourcesDir, fileName)));

                    IOUtils.copy(in, os);
                } catch (final Throwable t) {
                    throw closer.rethrow(t);
                } finally {
                    closer.close();
                }
            }
        }
    }
}

package org.keynote.godtools.android.tasks;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

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
import org.keynote.godtools.android.model.Followup;

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

    public InitialContentTasks(@NonNull final Context context) {
        mContext = context.getApplicationContext();
        mDao = DBAdapter.getInstance(mContext);
    }

    @NonNull
    public ListenableFuture<Object> loadFollowups() {
        final ListenableFuture<Cursor> followups = mDao.getCursorAsync(Query.select(Followup.class).limit(1));
        return Futures.transform(followups, new InitFollowups());
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
        everyStudent.setVersion(1.1);
        return mDao.insertAsync(everyStudent, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public static void run(Context mContext, File resourcesDir) {
        AssetManager manager = mContext.getAssets();
        final DBAdapter dao = DBAdapter.getInstance(mContext);

        Log.i("resourceDir", resourcesDir.getAbsolutePath());

        try {
            // copy the files from assets/english to documents directory
            String[] files = manager.list("english");
            for (String fileName : files) {
                final Closer closer = Closer.create();
                try {
                    final InputStream in = closer.register(manager.open("english/" + fileName));
                    final OutputStream os = closer.register(new FileOutputStream(new File(resourcesDir, fileName)));

                    IOUtils.copy(in, os);
                } catch (final Throwable t) {
                    throw closer.rethrow(t);
                } finally {
                    closer.close();
                }
            }

            // meta.xml file contains the list of supported languages
            InputStream metaStream = manager.open("meta.xml");
            List<GTLanguage> languageList = GTPackageReader.processMetaResponse(metaStream);
            for (GTLanguage gtl : languageList) {
                gtl.addToDatabase(mContext);
            }

            // contents.xml file contains information about the bundled english resources
            InputStream contentsStream = manager.open("contents.xml");
            List<GTPackage> packageList = GTPackageReader.processContentFile(contentsStream);
            for (GTPackage gtp : packageList) {
                Log.i("addingDB", gtp.getName());
                dao.updateOrInsert(gtp);
            }

            // english resources should be marked as downloaded
            GTLanguage gtlEnglish = new GTLanguage("en");
            gtlEnglish.setDownloaded(true);
            gtlEnglish.update(mContext);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    final class InitFollowups implements Function<Cursor, Object> {
        @Override
        public Object apply(final Cursor c) {
            if (c != null) {
                try {
                    if (c.getCount() == 0) {
                        loadDefaultFollowups();
                    }
                } catch (final IOException e) {
                    Crashlytics.logException(e);
                    throw Throwables.propagate(e);
                } finally {
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

            //
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
}

package org.keynote.godtools.android.service;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Copies the english resources from assets to internal storage,
 * then saves package information on the database.
 *
 * Created by matthewfrederick on 5/11/15.
 */
@SuppressWarnings({"UnusedAssignment", "ResultOfMethodCallIgnored"})
public class PrepareInitialContentTask
{

    public static void run(Context mContext, File documentsDir)
    {
        AssetManager manager = mContext.getAssets();

        File resourcesDir = new File(documentsDir, "resources");
        resourcesDir.mkdir();

        Log.i("resourceDir", resourcesDir.getAbsolutePath());

        try
        {
            // copy the files from assets/english to documents directory
            String[] files = manager.list("english");
            for (String fileName : files)
            {
                InputStream is = manager.open("english/" + fileName);
                File outFile = new File(resourcesDir, fileName);
                OutputStream os = new FileOutputStream(outFile);

                copyFile(is, os);
                is.close();
                is = null;
                os.flush();
                os.close();
                os = null;
            }

            // meta.xml file contains the list of supported languages
            InputStream metaStream = manager.open("meta.xml");
            List<GTLanguage> languageList = GTPackageReader.processMetaResponse(metaStream);
            for (GTLanguage gtl : languageList)
            {
                gtl.addToDatabase(mContext);
            }

            // contents.xml file contains information about the bundled english resources
            InputStream contentsStream = manager.open("contents.xml");
            List<GTPackage> packageList = GTPackageReader.processContentFile(contentsStream);
            for (GTPackage gtp : packageList)
            {
                Log.i("addingDB", gtp.getName());
                gtp.addToDatabase(mContext);
            }

            // Add Every Student to database
            GTPackage everyStudent = new GTPackage();
            everyStudent.setCode(GTPackage.EVERYSTUDENT_PACKAGE_CODE);
            everyStudent.setName(mContext.getString(R.string.app_name_everystudent));
            everyStudent.setIcon("homescreen_everystudent_icon_2x.png");
            everyStudent.setStatus("live");
            everyStudent.setLanguage("en");
            everyStudent.setVersion(1.1);

            everyStudent.addToDatabase(mContext);

            // english resources should be marked as downloaded
            GTLanguage gtlEnglish = new GTLanguage("en");
            gtlEnglish.setDownloaded(true);
            gtlEnglish.update(mContext);

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, read);
        }
    }
}

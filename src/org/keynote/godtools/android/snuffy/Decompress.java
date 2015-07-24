package org.keynote.godtools.android.snuffy;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Decompress
{
    private static final String TAG = "Decompress";

    public void unzip(File zipFile, File destination)
    {
        try
        {
            FileInputStream fin = new FileInputStream(zipFile);
            ZipInputStream zin = new ZipInputStream(new BufferedInputStream(fin));
            ZipEntry ze;
            byte[] buffer = new byte[2048];
            while ((ze = zin.getNextEntry()) != null)
            {
                Log.v(TAG, "Unzipping " + ze.getName());
                String fileName = ze.getName();
                // TODO: sanitize the filenames (and folder names)?
                fileName = destination.getPath() + "/" + fileName;
                dirChecker(fileName);
                FileOutputStream fout = new FileOutputStream(fileName);
                BufferedOutputStream bos = new BufferedOutputStream(fout, buffer.length);

                int size;
                while ((size = zin.read(buffer, 0, buffer.length)) != -1)
                {
                    bos.write(buffer, 0, size);
                }

                bos.flush();
                bos.close();
                fout.close();

                zin.closeEntry();
            }
            zin.close();
            fin.close();
        } catch (Exception e)
        {
            Log.e(TAG, "unzip", e);
        }
    }

    private void dirChecker(String fileName)
    {
        File f = new File(fileName);
        String parent = f.getParent();
        f = new File(parent);
        if (!f.isDirectory())
        {
            boolean success = f.mkdirs();
            if (!success)
                Log.e(TAG, "Cant create dir: " + fileName);
        }
    }
}

package org.keynote.godtools.android.imagemanager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.lang.ref.WeakReference;

public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewReference;
    private SnuffyApplication mApp;
    public String data;

    public BitmapWorkerTask(Activity activity, ImageView imageView) {
        imageViewReference = new WeakReference<ImageView>(imageView);
        mApp = (SnuffyApplication) activity.getApplication();
    }

    @Override
    protected Bitmap doInBackground(String... params) {

        data = params[0];
        String documentsDir = mApp.getDocumentsDir().getAbsolutePath();
        String path = documentsDir + "/resources/" + data;
        return BitmapFactory.decodeFile(path);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null)
                imageView.setImageBitmap(bitmap);
        }
    }
}

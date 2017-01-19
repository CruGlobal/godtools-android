package org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import org.keynote.godtools.renderer.crureader.bo.GPage.Util.FileUtils;

import java.io.File;

public class ImageAsyncTask extends AsyncTask<String, Void, Drawable> {

    public static void setImageView(final String content, final ImageView imageView) {

        new ImageAsyncTask() {
            @Override
            protected void onPostExecute(Drawable bm) {

                if (bm != null && imageView != null) {
                    imageView.setImageDrawable(bm);

                }
            }
        }.start(content);

    }

    @Override
    protected Drawable doInBackground(String... fileLocation) {

        File fileForGDP = new File(FileUtils.getResourcesDir(), fileLocation[0]);
        return Drawable.createFromPath(fileForGDP.getPath());

    }

    public void start(String param) {

        Log.d("Async Task", "Start with Serial Executor");
        if (Build.VERSION.SDK_INT >= 11) {
            // --post GB use serial executor by default --
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, param);
        } else {
            // --GB uses ThreadPoolExecutor by default--
            execute(param);
        }
    }

}

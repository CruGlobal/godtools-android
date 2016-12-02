package com.example.rmatt.crureader.bo.GPage.RenderHelpers;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.IOException;

/**
 * Created by rmatt on 12/1/2016.
 */

public class ImageAsyncTask  extends AsyncTask<String, Void, Drawable> {


    @Override
    protected Drawable doInBackground(String... fileLocation) {
        Drawable d = null;
        try {
            d = Drawable.createFromStream(RenderSingleton.getInstance().getContext().getAssets().open(fileLocation[0]), null);
        } catch (IOException e) {

            //Do nothing
            e.printStackTrace();
        }
        return d;
    }

    public void start(String param)
    {

            Log.d("Async Task", "Start with Serial Executor");
            if (Build.VERSION.SDK_INT >= 11)
            {
                // --post GB use serial executor by default --
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, param);
            }
            else
            {
                // --GB uses ThreadPoolExecutor by default--
                execute(param);
            }
        }


    }

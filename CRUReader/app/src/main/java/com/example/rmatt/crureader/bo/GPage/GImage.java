package com.example.rmatt.crureader.bo.GPage;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.rmatt.crureader.bo.GCoordinator;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.ImageAsyncTask;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by rmatt on 10/31/2016.
 */

@Root(name = "image")
public class GImage extends GCoordinator {


    private static final String TAG = "GImage";

    @Text(required = false)
    public String content;


    @Override
    public int render(LayoutInflater layoutInflater, ViewGroup viewGroup, int position) {
        ImageView imageView = new ImageView(viewGroup.getContext());
        viewGroup.addView(imageView);
        updateBaseAttributes(imageView);
        setImageView(imageView);
        imageView.setId(RenderViewCompat.generateViewId());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        Log.i(TAG, "render in GImage");
        return imageView.getId();
    }

    public void setImageView(final ImageView imageView) {

        new ImageAsyncTask() {
            @Override
            protected void onPostExecute(Drawable drawable) {
                super.onPostExecute(drawable);
                if (drawable != null && imageView != null) {
                    imageView.setImageDrawable(drawable);
                }
            }
        }.start(content);

    }
}

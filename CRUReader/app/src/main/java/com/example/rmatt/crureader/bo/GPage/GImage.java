package com.example.rmatt.crureader.bo.GPage;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.rmatt.crureader.bo.Gtapi;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

import java.io.IOException;

/**
 * Created by rmatt on 10/31/2016.
 */

@Root(name = "image")
public class GImage extends Gtapi<ImageView, ViewGroup> {


    private static final String TAG = "GImage";

    @Text(required = false)
    public String content;


    @Override
    public ImageView render(ViewGroup viewGroup, int position) {
        ImageView imageView = new ImageView(viewGroup.getContext());
        setImageView(imageView);
        Log.i(TAG, "render in GImage");
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        try {
            Drawable d = Drawable.createFromStream(imageView.getContext().getAssets().open(content), null);
            imageView.setImageDrawable(d);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

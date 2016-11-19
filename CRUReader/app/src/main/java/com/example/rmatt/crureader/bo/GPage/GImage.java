package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

import java.io.IOException;

/**
 * Created by rmatt on 10/31/2016.
 */

@Root(name = "image")
public class GImage extends GBaseImageAttributes {


    private static final String TAG = "GImage";

    @Text(required = false)
    public String content;


    @Override
    public ImageView render(ViewGroup viewGroup) {
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

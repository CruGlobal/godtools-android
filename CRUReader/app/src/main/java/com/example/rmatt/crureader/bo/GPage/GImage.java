package com.example.rmatt.crureader.bo.GPage;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.Base.GCoordinator;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

import java.io.IOException;

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
        View view
                = layoutInflater.inflate(R.layout.g_image, viewGroup);
        ImageView gImageView = (ImageView)view.findViewById(R.id.g_image_image_view);


        updateBaseAttributes(gImageView);
        Drawable d = null;
        try {
            d = Drawable.createFromStream(RenderSingleton.getInstance().getContext().getAssets().open(content), null);
            gImageView.setImageDrawable(d);
        } catch (IOException e) {


        }

        //ImageAsyncTask.setImageView(content, gImageView);
        gImageView.setId(RenderViewCompat.generateViewId());
        return gImageView.getId();
    }


}

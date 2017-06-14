package org.keynote.godtools.android.model;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by matthewfrederick on 2/10/15.
 */
public class HomescreenLayout
{
    private LinearLayout layout;
    private TextView textView;
    private ImageView imageView;

    public LinearLayout getLayout()
    {
        return layout;
    }

    public void setLayout(LinearLayout layout)
    {
        this.layout = layout;
    }

    public TextView getTextView()
    {
        return textView;
    }

    public void setTextView(TextView textView)
    {
        this.textView = textView;
    }

    public ImageView getImageView()
    {
        return imageView;
    }

    public void setImageView(ImageView imageView)
    {
        this.imageView = imageView;
    }
}

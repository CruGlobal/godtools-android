package org.keynote.godtools.android;

import android.os.Bundle;

public class Satisfied extends Gallery
{

    public static final String LOGTAG = "Satisfied";

    private static int[] images = {
            R.drawable.sat0, R.drawable.sat1, R.drawable.sat2, R.drawable.sat3,
            R.drawable.sat4, R.drawable.sat5, R.drawable.sat6, R.drawable.sat7,
            R.drawable.sat8, R.drawable.sat9, R.drawable.sat10, R.drawable.sat11,
            R.drawable.sat12, R.drawable.sat13, R.drawable.sat14, R.drawable.sat15
    };

    public void onCreate(Bundle savedInstanceState)
    {
        super.images = images;
        super.curLogTag = LOGTAG;
        super.onCreate(savedInstanceState);
    }

}
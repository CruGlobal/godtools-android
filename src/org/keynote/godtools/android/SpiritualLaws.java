package org.keynote.godtools.android;

import android.os.Bundle;

public class SpiritualLaws extends Gallery
{

    public static final String LOGTAG = "FourSpiritualLaws";

    private static int[] images = {
            R.drawable.sl0, R.drawable.sl1, R.drawable.sl2, R.drawable.sl3,
            R.drawable.sl4, R.drawable.sl5, R.drawable.sl6, R.drawable.sl7,
            R.drawable.sl8, R.drawable.sl9, R.drawable.sl10, R.drawable.sl11,
            R.drawable.sl12, R.drawable.sl13, R.drawable.sl14, R.drawable.sl15
    };

    public void onCreate(Bundle savedInstanceState)
    {
        super.images = images;
        super.curLogTag = LOGTAG;
        super.onCreate(savedInstanceState);
    }

}
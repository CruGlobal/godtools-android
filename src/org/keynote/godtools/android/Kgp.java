package org.keynote.godtools.android;

import org.keynote.godtools.android.R;

import android.os.Bundle;

public class Kgp extends Gallery {

	public static final String LOGTAG = "KnowingGodPersonally";

	private static int[] images = {
    	R.drawable.kgp0, R.drawable.kgp1, R.drawable.kgp2, R.drawable.kgp3,
    	R.drawable.kgp4, R.drawable.kgp5, R.drawable.kgp6, R.drawable.kgp7, 
    	R.drawable.kgp8, R.drawable.kgp9, R.drawable.kgp10, R.drawable.kgp11, 
    	R.drawable.kgp12, R.drawable.kgp13, R.drawable.kgp14, R.drawable.kgp15
    };
    
    public void onCreate(Bundle savedInstanceState) {
    	super.images = images;
    	super.curLogTag = LOGTAG;
        super.onCreate(savedInstanceState);
    }
}
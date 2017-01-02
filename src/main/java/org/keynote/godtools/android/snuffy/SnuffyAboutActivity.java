package org.keynote.godtools.android.snuffy;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import org.keynote.godtools.android.R;
import org.keynote.godtools.renderer.crureader.SlidePageFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SnuffyAboutActivity extends FragmentActivity {

    public static final String FILE_ID_STRING_EXTRA = "fileidstringextra";

    public static String readFileAsString(String filePath) {
        String result = "";
        File file = new File(filePath);
        if (file.exists()) {
            //byte[] buffer = new byte[(int) new File(filePath).length()];
            FileInputStream fis = null;
            try {
                //f = new BufferedInputStream(new FileInputStream(filePath));
                //f.read(buffer);

                fis = new FileInputStream(file);
                char current;
                while (fis.available() > 0) {
                    current = (char) fis.read();
                    result = result + String.valueOf(current);
                }
            } catch (Exception e) {
                Log.d("TourGuide", e.toString());
            } finally {
                if (fis != null)
                    try {
                        fis.close();
                    } catch (IOException ignored) {
                    }
            }
            //result = new String(buffer);
        }
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_snuffy);

        String fileID = getIntent().getExtras().getString(FILE_ID_STRING_EXTRA);
        Log.i("HELP", "Help File: " + readFileAsString(fileID));
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.content_container_real, SlidePageFragment.create(0, fileID));
        fragmentTransaction.commit();

    }

}

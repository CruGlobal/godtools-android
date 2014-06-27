package org.keynote.godtools.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.customactivities.ActionActivity;

import java.util.Locale;

public class SettingsPW extends ActionActivity implements View.OnClickListener {

    private static final String PREFS_NAME = "GodTools";
    private static final String TITLE = "Settings";

    TextView tvMainLanguage, tvParallelLanguage, tvAbout;
    RelativeLayout rlMainLanguage, rlParallelLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        setPageTitle(TITLE);

        tvMainLanguage = (TextView) findViewById(R.id.tvMainLanguage);
        tvParallelLanguage = (TextView) findViewById(R.id.tvParallelLanguage);
        tvAbout = (TextView) findViewById(R.id.tvAbout);
        rlMainLanguage = (RelativeLayout) findViewById(R.id.rlMainLanguage);
        rlParallelLanguage = (RelativeLayout) findViewById(R.id.rlParallelLanguage);

        // set click listeners
        rlParallelLanguage.setOnClickListener(this);
        rlMainLanguage.setOnClickListener(this);
        tvAbout.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String primaryLanguageCode = settings.getString(GTLanguage.KEY_PRIMARY, "en");
        String parallelLanguageCode = settings.getString(GTLanguage.KEY_PARALLEL, "");

        // set up primary language views
        Locale localePrimary = new Locale(primaryLanguageCode);
        tvMainLanguage.setText(localePrimary.getDisplayName());

        // set up parallel language views
        if (!parallelLanguageCode.isEmpty()) {
            Locale localeParallel = new Locale(parallelLanguageCode);
            tvParallelLanguage.setText(localeParallel.getDisplayName());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        switch (resultCode) {
            case 2001: {

            }
        }


    }

    @Override
    public void onClick(View v) {

        Intent intent = new Intent(SettingsPW.this, SelectLanguagePW.class);

        switch (v.getId()) {
            case R.id.rlMainLanguage:
                intent.putExtra("languageType", "Main Language");
                startActivityForResult(intent, 1002);
                break;

            case R.id.rlParallelLanguage:
                intent.putExtra("languageType", "Parallel Language");
                startActivityForResult(intent, 1003);
                break;

            case R.id.tvAbout:
                intent = new Intent(SettingsPW.this, About.class);
                startActivity(intent);
                break;
        }

    }


}

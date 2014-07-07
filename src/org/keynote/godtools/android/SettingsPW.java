package org.keynote.godtools.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.keynote.godtools.android.business.GTLanguage;

import java.util.Locale;

public class SettingsPW extends ActionBarActivity implements View.OnClickListener {

    private static final String PREFS_NAME = "GodTools";

    private static final int REQUEST_PRIMARY = 1002;
    private static final int REQUEST_PARALLEL = 1003;
    public static final int RESULT_DOWNLOAD_PRIMARY = 2001;
    public static final int RESULT_DOWNLOAD_PARALLEL = 2002;
    public static final int RESULT_CHANGED_PRIMARY = 2003;

    TextView tvMainLanguage, tvParallelLanguage, tvAbout;
    RelativeLayout rlMainLanguage, rlParallelLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

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
        if (parallelLanguageCode.isEmpty()) {
            tvParallelLanguage.setText("None");
        } else {
            Locale localeParallel = new Locale(parallelLanguageCode);
            tvParallelLanguage.setText(localeParallel.getDisplayName());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED)
            setResult(resultCode, data);

        if (requestCode == REQUEST_PRIMARY && resultCode == RESULT_DOWNLOAD_PRIMARY) {
            finish();
        } else if (requestCode == REQUEST_PARALLEL && resultCode == RESULT_DOWNLOAD_PARALLEL) {
            finish();
        }

    }

    @Override
    public void onClick(View v) {

        Intent intent = new Intent(SettingsPW.this, SelectLanguagePW.class);

        switch (v.getId()) {
            case R.id.rlMainLanguage:
                intent.putExtra("languageType", "Main Language");
                startActivityForResult(intent, REQUEST_PRIMARY);
                break;

            case R.id.rlParallelLanguage:
                intent.putExtra("languageType", "Parallel Language");
                startActivityForResult(intent, REQUEST_PARALLEL);
                break;

            case R.id.tvAbout:
                intent = new Intent(SettingsPW.this, About.class);
                startActivity(intent);
                break;
        }

    }


}

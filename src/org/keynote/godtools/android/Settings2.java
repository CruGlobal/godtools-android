package org.keynote.godtools.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.keynote.godtools.android.customactivities.ActionActivity;

public class Settings2 extends ActionActivity implements View.OnClickListener {

    public static final String LOGTAG = "Settings";
    public static final String PREFNAME = "GodTools";

    public static final String SELECTED_LANGUAGE_TYPE = "selLangType";
    public static final String PREF_MAIN_LANGUAGE = "prefMLang";
    public static final String PREF_PARALLEL_LANGUAGE = "prefPLang";
    public static final String SEPARATOR = "~";

    public static final String LANGUAGE_TYPE = "langType";
    public static final String LANGUAGE_NAME = "langName";
    public static final String LANGUAGE_CODE = "langCode";
    public static final String LANGUAGE_DOWNLOAD_FLAG = "langDlFlag";

    static final int REQUEST_MAIN_LANGUAGE = 0;
    static final int REQUEST_PARALLEL_LANGUAGE = 1;
    public static final String DEFAULT_LANGUAGE = "en~English";

    final String TITLE = "Settings";

    SharedPreferences pref;

    String currentMainCode;
    String currentParallelCode;
    String currentMainLanguage;
    String currentParallelLanguage;

    TextView tvMainLanguage;
    TextView tvParallelLanguage;
    TextView tvAbout;
    RelativeLayout rlMainLanguage;
    RelativeLayout rlParallelLanguage;

    private ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOGTAG, "setContentView");
        setContentView(R.layout.settings);
        //set the actionbar title
        setPageTitle(TITLE);

        initialize();

    }

    private void initialize() {
        initializePreferences();
        initializeActionbar();
    }

    private void initializePreferences() {
        pref = getSharedPreferences(PREFNAME, MODE_PRIVATE);

        String[] concatMainLanguage = pref.getString(PREF_MAIN_LANGUAGE, DEFAULT_LANGUAGE).split(SEPARATOR);
        String[] concatParallelLanguage = pref.getString(PREF_MAIN_LANGUAGE, DEFAULT_LANGUAGE).split(SEPARATOR);

        currentMainCode = concatMainLanguage[0];
        currentMainLanguage = concatMainLanguage [1];
        currentParallelCode = concatParallelLanguage[0];
        currentParallelLanguage = concatParallelLanguage[1];
    }

    private void initializeActionbar() {
        tvMainLanguage = (TextView) findViewById(R.id.tvMainLanguage);
        tvParallelLanguage = (TextView) findViewById(R.id.tvParallelLanguage);
        tvAbout = (TextView) findViewById(R.id.tvAbout);
        rlMainLanguage = (RelativeLayout) findViewById(R.id.rlMainLanguage);
        rlParallelLanguage = (RelativeLayout) findViewById(R.id.rlParallelLanguage);


        tvMainLanguage.setText(currentMainLanguage);
        tvParallelLanguage.setText(currentParallelLanguage);

        rlMainLanguage.setOnClickListener(this);
        rlParallelLanguage.setOnClickListener(this);
        tvAbout.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View view) {

        Intent intent = null;


        switch (view.getId()) {
            case R.id.rlMainLanguage:
            case R.id.rlParallelLanguage:

                String selectedPref = PREF_MAIN_LANGUAGE;
                int request_code = REQUEST_MAIN_LANGUAGE;

                if (view.getId() == R.id.rlParallelLanguage){
                    request_code = REQUEST_PARALLEL_LANGUAGE;
                    selectedPref = PREF_PARALLEL_LANGUAGE;
                }


                intent = new Intent(this, SelectLanguage.class);
                intent.putExtra(SELECTED_LANGUAGE_TYPE, selectedPref);
                startActivityForResult(intent, request_code);
                break;

            case R.id.tvAbout:
                intent = new Intent(this, About.class);

                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED)
            return;

        data.putExtra(LANGUAGE_TYPE, requestCode);

        //*test
        String languageType = (requestCode == REQUEST_MAIN_LANGUAGE) ? "Main Language" :"Parallel Language";
//        Toast.makeText(this, "type: " + languageType + "\ncode: " + data.getStringExtra(LANGUAGE_CODE) + "\nlanguage: " + data.getStringExtra(LANGUAGE_NAME) + "\ndownloaded? " + data.getBooleanExtra(LANGUAGE_DOWNLOAD_FLAG, false), Toast.LENGTH_LONG).show();

        //pass the data to Main and end the activity.
        //Main will then load the language pack (download if necessary)
        setResult(RESULT_OK, data);
        finish();
    }
}

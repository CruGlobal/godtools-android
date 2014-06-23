package org.keynote.godtools.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import org.keynote.godtools.android.adapters.PackageListAdapter;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.customactivities.ListActionActivity;
import org.keynote.godtools.android.snuffy.SnuffyActivity;

import java.util.ArrayList;
import java.util.List;

public class Main2 extends ListActionActivity implements AdapterView.OnItemClickListener {

    public static final String LOGTAG = "Main2";
    public static final String PREFNAME = "GodTools";

    private int REQUEST_SETTINGS = 1000;

    final String TITLE = "God Tools";

    String currentMainLanguageCode;
    String currentMainLanguageName;
    String currentParallelLanguageCode;
    String currentParallelLanguageName;

    PackageListAdapter adapter;
    List<GTPackage> packages;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main2);
        setPageTitle(TITLE);
        setBackButtonEnabled(false);

        initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        createMenuItems(R.menu.main2_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, Settings2.class);
                startActivityForResult(intent, REQUEST_SETTINGS);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initialize() {

        prepareData();
        getPreferences();
        createAdapter();

        getListView().setOnItemClickListener(this);

    }

    private void prepareData() {
        packages = getPackages("en");

        Toast.makeText(this, "packages: " + packages.size(), Toast.LENGTH_SHORT).show();
    }

    private void getPreferences() {

    }


    private void createAdapter() {
        adapter = new PackageListAdapter(this, R.layout.packages_list_item, packages);

        setListAdapter(adapter);
    }

    private List<GTPackage> getPackages(String langCode) {
        //*test
        packages = new ArrayList<GTPackage>();

        String[] packageArrays = {"kgp~" + getResources().getString(R.string.menu_item_kgp)
                , "satisfied~" + getResources().getString(R.string.menu_item_satisfied)
                , "fourlaws~" + getResources().getString(R.string.menu_item_4laws)
                , "everystudent~" + getResources().getString(R.string.menu_item_cwg)};

        for (int i = 0; i < packageArrays.length; i++) {
            String packageDetails[] = packageArrays[i].split("~");

            GTPackage gtPackage = new GTPackage();
            gtPackage.setName(packageDetails[0]);
            gtPackage.setTitle(packageDetails[1]);
            gtPackage.setCode("en");
            gtPackage.setLanguage("English");

            packages.add(gtPackage);
        }

        return packages;
    }

    private void createAnimations() {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        int mPageWidth = getWindow().getDecorView().getWidth();
        int mPageHeight = getWindow().getDecorView().getHeight();

        Intent intent = new Intent(this, SnuffyActivity.class);
        intent.putExtra("PackageName", packages.get(position).getName());
        intent.putExtra("LanguageCode", packages.get(position).getCode());
        // Also pass in the screen dimensions that we have determined
        intent.putExtra("PageLeft", 0);
        intent.putExtra("PageTop", 0);
        intent.putExtra("PageWidth", mPageWidth);
        intent.putExtra("PageHeight", mPageHeight);
        startActivity(intent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SETTINGS && resultCode == RESULT_OK) {
            int languageType = data.getIntExtra(Settings2.LANGUAGE_TYPE, 1);
            String languageTypeString = (languageType == 0) ? "Main Language" : "Parallel Language";
            String languageCode = data.getStringExtra(Settings2.LANGUAGE_CODE);
            String languageName = data.getStringExtra(Settings2.LANGUAGE_NAME);
            boolean hasBeenDownloaded = data.getBooleanExtra(Settings2.LANGUAGE_DOWNLOAD_FLAG, false);

            Toast.makeText(this, "type: " + languageTypeString + "\ncode: " + languageCode + "\nlanguage: " + languageName + "\ndownloaded? " + hasBeenDownloaded, Toast.LENGTH_LONG).show();
        }


    }
}

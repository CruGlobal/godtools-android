package org.keynote.godtools.android;
 
import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.TextView;

import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.w3c.dom.Text;

public class About extends Activity {
 
	public static final String LOGTAG = "About";
	public static final String FLURRYTAG = "Settings"; // we are now calling this from the Settings cmd and button
	TextView aboutText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// Initialize the layout
		super.onCreate(savedInstanceState);
 
		setContentView(R.layout.about);
		String emailLink = "<font color='blue'><u>support@godtoolsapp.com</u></font>";
		Resources resources = getResources();
		String fullText = String.format(resources.getString(R.string.about_suggestions), emailLink);
		CharSequence styledText = Html.fromHtml(fullText);

		aboutText = (TextView) findViewById(R.id.about_text);
		aboutText.setText(styledText);

		aboutText.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				sendEmail();
			}
		});
 
		// display the application version
		TextView versionVeiw = (TextView) this.findViewById(R.id.app_version);
		try {
			String version = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			versionVeiw.setText(version);
		} catch (NameNotFoundException e) {}
	}

	private void sendEmail()
	{
		String subjectLine = "GodTools Suggestion";
		String message = "";
		SnuffyApplication application = ((SnuffyApplication) getApplication());
		application.sendEmailWithContent(this, subjectLine, message);
	}
	
    @Override
    public void onStart()
    {
       super.onStart();
    }
    
    @Override
    public void onStop()
    {
       super.onStop();
    }
}
package org.keynote.godtools.android;
 
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
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
		String[] sendTo = {"support@godtoolsapp.com"};
		String subjectLine = "GodTools Suggestion";
		String message = "";
		
		try {
			Intent intent = new Intent(Intent.ACTION_SENDTO);
			intent.setData(Uri.parse("mailto:"));
			intent.putExtra(Intent.EXTRA_EMAIL, sendTo);
			intent.putExtra(Intent.EXTRA_SUBJECT, subjectLine);
			intent.putExtra(Intent.EXTRA_TEXT, message);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.startActivity(Intent.createChooser(intent, getApplicationContext().getString(R.string.choose_your_email_provider)));
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.unable_to_send_the_email)
					.setCancelable(false)
					.setPositiveButton(R.string.ok, null);
			AlertDialog alert = builder.create();
			alert.show();
			return;
		}
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
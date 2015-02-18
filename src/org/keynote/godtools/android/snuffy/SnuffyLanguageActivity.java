package org.keynote.godtools.android.snuffy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.keynote.godtools.android.R;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

public class SnuffyLanguageActivity extends ListActivity {
	private static final String TAG = "SnuffyLanguageActivity";
	
	private ArrayList<HashMap<String, Object>> mList = new ArrayList<HashMap<String, Object>>(4);
	private String mPackageName;
	private String mLanguageCode;
    public static final int DIALOG_DOWNLOAD_INDEX_PROGRESS = 0;
    public static final int DIALOG_DOWNLOAD_LANGUAGE_PROGRESS = 1;
    private ProgressDialog mProgressDialog;
    private DownloadFileAsync mDownloadFileAsync;
    private SimpleImageAdapter mAdapter;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.language_menu);
		
		// see also : http://stackoverflow.com/questions/6852876/android-about-listview-and-simpleadapter
		// see also: http://android-developers.blogspot.com.au/2009/02/android-layout-tricks-1.html
		
		// the from array specifies which keys from the map
		// we want to view in our ListView
		String[] from = { "label1", "image", "status" };
		
		// the to array specifies the views from the xml layout
		// on which we want to display the values defined in the from array
		int[] to = { R.id.list2Text1, R.id.list2Image};
		
		mLanguageCode = getIntent().getStringExtra("LanguageCode");
		mPackageName  = getIntent().getStringExtra("PackageName");
		
		// TODO: consider case where device rotated - this code may need to move
		// languages are reloaded but progress dialog is not removed
		
		mAdapter = new SimpleImageAdapter(this, mList, R.layout.list_item_with_icon_text_and_status, from, to);
		setListAdapter(mAdapter);
		
		registerForContextMenu(getListView());

		// fill the list
		mDownloadFileAsync = new DownloadFileAsync("");
		mDownloadFileAsync.execute();
	}
	
	static final int CMD_SWITCH   = Menu.FIRST;
	static final int CMD_DOWNLOAD = Menu.FIRST+1;
	static final int CMD_REMOVE   = Menu.FIRST+2;
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) { 
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo; 

	    String language = (String)(mList.get(info.position).get("label2"));	    
	    String status   = (String)(mList.get(info.position).get("status"));	    
	    menu.setHeaderTitle(language);
	    if (status.equalsIgnoreCase("LOADED")) {
	    	menu.add(Menu.NONE, CMD_REMOVE, Menu.NONE, R.string.remove_language);
	    }
	    if (status.equalsIgnoreCase("UNLOADED")) {
	    	menu.add(Menu.NONE, CMD_DOWNLOAD, Menu.NONE, R.string.download_language);
	    }
	    if (!status.equalsIgnoreCase("UNLOADED")) {
	    	menu.add(Menu.NONE, CMD_SWITCH, Menu.NONE, R.string.switch_to_language);
	    }	    
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) { 
       AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo(); 
       int languageIndex = (int)this.getListView().getItemIdAtPosition(info.position); // wont that be same as position?
	 
	    switch (item.getItemId()) {
	    case CMD_SWITCH: {
	    	doCmd_SwitchLanguage(languageIndex);
	    	break;
	    }
	    case CMD_DOWNLOAD: {
	    	doCmd_DownloadLanguage(languageIndex);
	    	break;
	    }
	    case CMD_REMOVE: { 
	    	doCmd_RemoveLanguage(languageIndex);
	    	break;
		    }
	    default: {	    	
	    	return false; 
	    } 
	    }
	    return true; 
	} 

	
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
		case DIALOG_DOWNLOAD_INDEX_PROGRESS:
		case DIALOG_DOWNLOAD_LANGUAGE_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage(getApplicationContext().getString(
						id == DIALOG_DOWNLOAD_INDEX_PROGRESS 
							? R.string.loading_language_list 
							: R.string.loading_requested_language));
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(true);
			mProgressDialog.setMax(-1);
			mProgressDialog.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					mDownloadFileAsync.cancel(false);					
				}
			});
			mProgressDialog.show();
			return mProgressDialog;
		default:
			return null;
        }
    }
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String status = (String)(mList.get(position).get("status"));
		if (!status.equalsIgnoreCase("UNLOADED")) {
			doCmd_SwitchLanguage(position);
			return;
		}
		if (status.equalsIgnoreCase("UNLOADED")) {
			doCmd_DownloadLanguage(position);
        }
	}
	
	private void doCmd_DownloadLanguage(int languageIndex) {
		String languageCode = (String)(mList.get(languageIndex).get("languagecode"));
		
		mDownloadFileAsync = new DownloadFileAsync(languageCode);
		mDownloadFileAsync.execute();
	}

	private void doCmd_RemoveLanguage(int languageIndex) {
		// Remove a downloaded language and if that was the current language, switch to the built-in language
		final String languageCode = (String)(mList.get(languageIndex).get("languagecode"));
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.remove_this_language)
				.setCancelable(false)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								removeLanguage(languageCode);
								updateLanguageList();
								dialog.dismiss();
							}
						})
				.setNegativeButton(R.string.no, 
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();	
		alert.show();
	}
	
	private void doCmd_SwitchLanguage(int languageIndex) {
		// TODO: Cannot be done by caller.
		// We need to switch to the new language here.
		// So we can also switch languages to the built-in language if user deletes the current language

		Intent intent = new Intent();
		intent.putExtra("LanguageCode", (String)(mList.get(languageIndex).get("languagecode")));
		setResult(RESULT_FIRST_USER + languageIndex, intent);
		finish();
		
	}

	private class SimpleImageAdapter extends SimpleAdapter {

		public SimpleImageAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void setViewImage(ImageView v, String value) {
			
			if (value.equalsIgnoreCase("LOADED" )
			||  value.equalsIgnoreCase("BUILTIN")) {
				v.setImageResource(R.drawable.snuffy_download_assets_tick);
			return;
			}
			if (value.equalsIgnoreCase("UNLOADED")) {
				v.setImageResource(R.drawable.snuffy_download_assets_arrow);
				return;
			}
			
			InputStream isImage;
			try {
				boolean bImageFromAsset = false; // the icons for all languages have been downloaded
				if (!bImageFromAsset) {
					//  need this code when the files have been downloaded
					try {
						Uri uri = Uri.parse("file://" + value);
						isImage = getContentResolver().openInputStream(uri);
					}
					catch (IOException e) {
						// repoFile.xml points to en.png but the icons folder in payload only contains en@2X.png !!
						// so try with that name before complaining
						Uri uri = Uri.parse("file://" + value.replace(".png", "@2x.png"));
						isImage = getContentResolver().openInputStream(uri);						
					}
				}
				else {
					// the code above wont handle assets (perhaps because assets are compressed)
					// So we handle those explicitly
					isImage = getAssets().open(value, AssetManager.ACCESS_BUFFER); // read into memory since it's not very large
				}
	        	Bitmap bm = BitmapFactory.decodeStream(isImage);
				isImage.close();
				
				v.setImageBitmap(bm);
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}		
	}
	
	private void removeLanguage(String languageCode) {
		// first delete the file we use to detect if entire file set is loaded
		File documentsDir = ((SnuffyApplication)getApplication()).getDocumentsDir();
		File f;
		f = new File(documentsDir + "/Packages/" + mPackageName + "/" + languageCode + ".xml");
		f.delete();
		// next delete the entire directory of files belonging to this language + package combo 
		f = new File(documentsDir + "/Packages/" + mPackageName + "/" + languageCode);
		DeleteRecursive(f);		
	}
	
	private void DeleteRecursive(File fileOrDirectory) { 
	    if (fileOrDirectory.isDirectory()) 
	        for (File child : fileOrDirectory.listFiles()) 
	            DeleteRecursive(child); 
	 
	    fileOrDirectory.delete(); 
	} 
	
	private void updateLanguageList() {
		File documentsDir = ((SnuffyApplication)getApplication()).getDocumentsDir();
		Document 			xmlDoc 	= null;
		FileInputStream 	fin 	= null;
		BufferedInputStream	bin 	= null;
		File repoFile = new File(documentsDir + "/repoIndex.xml");
		try {
			fin = new FileInputStream(repoFile);
			bin = new BufferedInputStream(fin);
	       	xmlDoc = DocumentBuilderFactory
    				.newInstance()
    				.newDocumentBuilder()
    				.parse(bin); 
	       	org.w3c.dom.Element root =  xmlDoc.getDocumentElement();
	       	if (root == null)
	       		throw new SAXException("XML Document has no root element");
	       	
	       	// TODO: Could also verify that the package node has id=the package name and status = "live"

	       	NodeList nlLanguages = root.getElementsByTagName("language"); 
	       	int numLanguages = nlLanguages.getLength();
	       	if (numLanguages == 0)
	       		throw new SAXException("Repo document must have at least one language node");
	       	mList.clear();
          	for (int i=0; i < numLanguages; i++) {
            	Element elLanguage = (Element)nlLanguages.item(i);
            	if (elLanguage.getAttribute("status").equalsIgnoreCase("live")) {
            		// TODO: verify version and minimum_interpreter_version too
	            	String localizedPackageName = elLanguage.getAttribute("name");
	            	String iconFileName         = elLanguage.getAttribute("icon");
	            	//String filesPath			= elLanguage.getAttribute("path");
	            	String languageCode			= elLanguage.getAttribute("language_code"); // TODO: split EN_US to EN and US but if 2 letters then 2nd is ""
	            	String languageName			= new Locale(languageCode.substring(0,2), "").getDisplayLanguage();
	            	if (languageCode.equalsIgnoreCase("en_us"))
	            		languageName = "English (United States)";
	            	
	            	// Could add other properties like isAsset/isFile and the filename
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("label1"      , localizedPackageName);
					map.put("label2"      , languageName);
					map.put("languagecode", languageCode);
					map.put("image", new File(documentsDir,  iconFileName).getPath());
					if (((SnuffyApplication)getApplication()).languageExistsAsAsset(mPackageName, languageCode))
						map.put("status"  , "BUILTIN");
					else if (((SnuffyApplication)getApplication()).languageExistsAsFile(mPackageName, languageCode))
						map.put("status"  , "LOADED");
					else
						map.put("status"  , "UNLOADED");
					mList.add(map);			
            	}
          	}
          	mAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			Log.e(TAG, "Cannot open, read or process repo file: " + repoFile.getPath() + " Error=" + e.toString());
		}
		finally {
			if (bin != null) {
				try {bin.close();} catch (Exception e) {}
			}
			if (fin != null) {
				try {fin.close();} catch (Exception e) {}
            }
		}			
	}
	
	
	private class DownloadFileAsync extends AsyncTask<String, Integer, String> {
		private String mLanguageCode;
		private int    mProgressDialogId;
		
		public DownloadFileAsync(String languageCode) {
			//		"" = names, languages and icons for all
			//		"en_US", "ru", "en" etc: full package for a single language
			super();
			mLanguageCode     = languageCode;
			mProgressDialogId = (mLanguageCode.length() == 0) ? DIALOG_DOWNLOAD_INDEX_PROGRESS : DIALOG_DOWNLOAD_LANGUAGE_PROGRESS;
		}
		   
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (mProgressDialog != null) {
				mProgressDialog.setProgress(0);
				mProgressDialog.setMax(0);
			}
			showDialog(mProgressDialogId);
		}

		@Override
		protected String doInBackground(String... notUsed) {
		
			final String repoURL = "http://godtoolsapp.com/SnuffyRepo/payload.php";
			File documentsDir = ((SnuffyApplication)getApplication()).getDocumentsDir();
			String tempFileName = null;
			try {
				
				// 1. Download the repo index or full data for this package (as a ZIP file) 
				//    p = package
				//	  ss = screen resolution (we only want standard, not 2X)
				//	  l = language code
				URL url;
				if (mLanguageCode.length() == 0) {
					// loading languages index only
					url = new URL(repoURL + "?ss=standard&segment=icon&p=" + mPackageName); // TODO: Ought to escape the packageName?
				}
				else {
					// loading all data for a single language
					url = new URL(repoURL + "?ss=standard&l=" + mLanguageCode + "&p=" + mPackageName); // TODO: Ought to escape the packageName?
				}
				Log.d(TAG, "Loading: " + url.toString());
				URLConnection conn = url.openConnection();
				conn.connect();
				int fileLength = conn.getContentLength();
				if (fileLength < 0) {
					// sometimes HTTL does not return a length - then we cannot do proper progress bar!
					publishProgress(0, 1); // 1 to avoid "Nan"
				}
				else {				
					publishProgress(0, fileLength);
				}
				
				tempFileName = getCacheDir() + "/snuffyrepo.zip"; // Ought to use external folders - IF EXT STORAGE EXISTS
				InputStream  input  = new BufferedInputStream(url.openStream());
				OutputStream output = new FileOutputStream(tempFileName); 
				byte data[] = new byte[1024];
	
				int total = 0;
				int count;
	
				while ((!isCancelled()) && ((count = input.read(data)) != -1)) {
					total += count;
					if (fileLength < 0) {
						publishProgress(total, total);						
					}
					else {
						publishProgress(total, fileLength);
					}
					output.write(data, 0, count);
				}
	
				output.flush();
				output.close();
				input.close();
				
				// 2. Unzip the file into our persistent storage area 
				if (!isCancelled()) {
					new Decompress().unzip(new File(tempFileName), documentsDir);
				}
				
				// 3. Process the downloaded files
				if (!isCancelled()) {
					if (mLanguageCode.length() == 0) {
						String downloadedIndex = documentsDir.getPath() + "/repoFile.xml";
						String renamedIndex    = documentsDir.getPath() + "/repoIndex.xml"; // TODO: could build packagename into this filename
						
						// rename the repo index so it wont get overwritten by subsequent download of whole package
						// which includes another file of the same name but different contents.
						File repoFile = new File(downloadedIndex);
						repoFile.renameTo(new File(renamedIndex));						
					}
				}
			}
			catch (Exception e) {
				Log.e(TAG, "retrieveRepoForPackage failed: " + e.toString());        	
				
			}
			finally {
				if (tempFileName != null) {
					File f = new File(tempFileName);
					f.delete();					
				}				
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... progress) {
			mProgressDialog.setMax     (progress[1]);
			mProgressDialog.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(String unused) {
			dismissDialog(mProgressDialogId);
			// Force list to refresh
			updateLanguageList();
		}		
	}	
}

package org.keynote.godtools.android.snuffy;

// TODO: We can load our own fonts:
// 		http://www.barebonescoder.com/2010/05/android-development-using-custom-fonts/

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.text.ClipboardManager;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.keynote.godtools.android.R;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class PackageReader
{
    private static final String TAG = "PackageReader";

    // Note that these coords are those used in the Package .XML files.
    // They are based on an original iPhone (480x320), with the status bar removed
    // The similar specs in Main.java and the HomeScreen layout in the GodTools app are based on
    // an iPhone4S with retina display (960x640), with the status bar removed.

    //public static final int		REFERENCE_DEVICE_HEIGHT = 460;	// pixels on iPhone - not including status bar
    public static final int REFERENCE_DEVICE_HEIGHT = 480;    // pixels on iPhone - including status bar
    public static final int REFERENCE_DEVICE_WIDTH = 320;    // pixels on iPhone - full width

    private static final float DEFAULT_TEXT_SIZE = 17.0f;
    private static final float DEFAULT_BUTTON_TEXT_SIZE = 20.0f;
    private static final float DEFAULT_QUESTION_TEXT_SIZE = 20.0f;
    private static final float DEFAULT_STRAIGHT_QUESTION_TEXT_SIZE = 17.0f;
    private static final String DEFAULT_BACKGROUND_COLOR = "#ffff00";    // yellow so we will see if it is not set in the XML
    private static final int TEXT_MARGINX = 10;
    private static final int BUTTON_MARGINX = 10;    // within the page
    private static final int BUTTON_HR_MARGINX = 0;    // within the button container
    private static final int BUTTON_TEXT_MARGINX = 10;    // within the button container
    private static final int BUTTON_MARGIN_BOTTOM = 5;
    private static final int QUESTION_MARGIN_BOTTOM = 20;
    private static final int DEFAULT_YOFFSET = 25;
    private static final int MIN_MARGIN_ABOVE_FOOTER = 5;
    private static final int MAX_YOFFSET = 100;
    private static final float HR_ALPHA = 0.25f;

    private WeakReference<SnuffyApplication>
            mAppRef;

    private Context mContext;
    private int mPageWidth;
    private int mPageHeight;
    private String mPackageName;
    private String mLanguageCode;
    private Vector<SnuffyPage> mPages;
    private String mPackageTitle;
    private Typeface mAlternateTypeface;

    private String mImageFolderName;
    private String mThumbsFolderName;
    private String mSharedFolderName;
    private int mBackgroundColor;
    private int mYOffsetPerItem;
    private int mYOffset;
    private int mYOffsetInPanel;
    private int mYOffsetMaxInPanel;
    private int mYOffsetInQuestion;
    private int mYFooterTop;
    private int mNumOffsetItems;
    private int mTotalBitmapSpace;
    private boolean mFromAssets;
    private ProgressCallback mProgressCallback;

    public String getPackageTitle()
    {
        return mPackageTitle;
    }

    public interface ProgressCallback
    {
        void updateProgress(int curr, int max);
    }

    public boolean processPackagePW(SnuffyApplication app,
                                    int pageWidth,
                                    int pageHeight,
                                    String packageConfigName,
                                    Vector<SnuffyPage> pages,
                                    ProgressCallback progressCallback,
                                    Typeface alternateTypeface)
    {

        mAppRef = new WeakReference<SnuffyApplication>(app);
        mContext = app.getApplicationContext();
        mPageWidth = pageWidth;
        mPageHeight = pageHeight;
        mPages = pages;
        mTotalBitmapSpace = 0;
        mImageFolderName = "resources/";
        mThumbsFolderName = "resources/";
        mSharedFolderName = "shared/";
        mFromAssets = false;
        mProgressCallback = progressCallback;
        mAlternateTypeface = alternateTypeface;

        // In the case where this package is replacing the previous package - release the memory occupied by the original
        bitmapCache.clear();
        mPages.clear();

        String mainPackagefileName = "resources/" + packageConfigName;
        boolean bSuccess;
        InputStream isMain = null;
        try
        {
            isMain = new BufferedInputStream(new FileInputStream(app.getDocumentsDir().getPath() + "/" + mainPackagefileName));
            bSuccess = processMainPackageFilePW(isMain);

        } catch (IOException e)
        {
            Log.e(TAG, "Cannot open or read main package file: " + mainPackagefileName);
            return false;
        } finally
        {
            if (isMain != null)
            {
                try
                {
                    isMain.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return bSuccess;
    }

    public boolean processMainPackageFilePW(InputStream isMain)
    {
        Log.d(TAG, ">>> processMainPackageFile starts");

        boolean bSuccess = false;

        Document xmlDoc;
        try
        {
            xmlDoc = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(isMain);
            org.w3c.dom.Element root = xmlDoc.getDocumentElement();
            if (root == null)
                throw new SAXException("XML Document has no root element");

            NodeList nlPages = root.getElementsByTagName("page");
            int numPages = nlPages.getLength();
            if (numPages == 0)
                throw new SAXException("Main Package file document must have at least one page node");
            NodeList nlAbout = root.getElementsByTagName("about");
            int numAbouts = nlAbout.getLength();
            if (numAbouts != 1)
                throw new SAXException("Main Package file document must have exactly one about node");
            NodeList nlPackageName = root.getElementsByTagName("packagename"); // packagename seems to have superseded displayname as the name for this element
            int numPackageNames = nlPackageName.getLength();
            NodeList nlDisplayName = root.getElementsByTagName("displayname"); // but some files (e.g. 4Laws/ru.xml) still have displayname ?
            int numDisplayNames = nlDisplayName.getLength();
            if ((numPackageNames + numDisplayNames) != 1)
                throw new SAXException("Main Package file document must have exactly one packagename or displayname node");
            if (numPackageNames == 1)
            {
                Element elPackage = (Element) nlPackageName.item(0);
                mPackageTitle = elPackage.getTextContent();
            }
            else
            {
                Element elPackage = (Element) nlDisplayName.item(0);
                mPackageTitle = elPackage.getTextContent();
            }

            Element elAbout = (Element) nlAbout.item(0);
            if (!processPagePW(elAbout))    // about will be page 0
                return false;

            mProgressCallback.updateProgress(0, numPages);
            for (int i = 0; i < numPages; i++)
            {
                Element elPage = (Element) nlPages.item(i);
                if (!processPagePW(elPage))
                    return false;
                mProgressCallback.updateProgress(i + 1, numPages);
            }

            bSuccess = true;
        } catch (IOException e)
        {
            Log.e(TAG, "processMainPackageFile failed: " + e.toString());
        } catch (ParserConfigurationException e)
        {
            Log.e(TAG, "processMainPackageFile failed: " + e.toString());
        } catch (SAXException e)
        {
            Log.e(TAG, "processMainPackageFile failed: " + e.toString());
        }
        Log.d(TAG, ">>> processMainPackageFile ends");
        return bSuccess;
    }

    private boolean processPagePW(Element pageElement)
    {
        String thumbFileName = pageElement.getAttribute("thumb");
        String pageFileName = pageElement.getAttribute("filename");
        String description = pageElement.getTextContent();
        Log.d(TAG, ">>> processPage: " + pageFileName);

        pageFileName = "resources/" + pageFileName;

        SnuffyPage currPage;
        InputStream pageInputStream = null;
        try
        {
            pageInputStream = new BufferedInputStream(new FileInputStream(mAppRef.get().getDocumentsDir().getPath() + "/" + pageFileName));
            currPage = processPageFilePW(pageInputStream, pageFileName);
            currPage.mDescription = description;
            //currPage.mThumbnail   = Uri.parse("file:///android_asset/" + mThumbsFolderName + thumbFileName).toString();
            currPage.mThumbnail = thumbFileName;
            mPages.add(currPage);
        } catch (IOException e)
        {
            Log.e(TAG, "Cannot open or read page file: " + pageFileName);
            return false;
        } finally
        {
            if (pageInputStream != null)
            {
                try
                {
                    pageInputStream.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    private SnuffyPage processPageFilePW(InputStream isPage, String pageFileName)
    {
        Log.d(TAG, ">>> processPageFile starts");

        SnuffyPage snuffyPage = null;

        Document xmlDoc;
        try
        {
            xmlDoc = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(isPage);
            Element root = xmlDoc.getDocumentElement();
            if (root == null)
                throw new SAXException("XML Document has no root element");

            mYOffsetPerItem = getScaledYValue(DEFAULT_YOFFSET);
            int SAVE = mTotalBitmapSpace;
            for (int iPass = 1; iPass <= 2; iPass++)
            {
                snuffyPage = new SnuffyPage(mContext);

                mYOffset = 0;
                mYFooterTop = mPageHeight;
                mNumOffsetItems = 0;
                addCover(snuffyPage);
                processBackgroundPW(root, snuffyPage);
                processPageElements(root, snuffyPage);

                snuffyPage.setPageIdFromFilename(pageFileName);

                switch (iPass)
                {
                    case 1:
                    {
                        if (mNumOffsetItems < 1)
                        {
                            // no second pass required
                            iPass = 2; // force exit from loop
                        }
                        else
                        {
                            mYOffsetPerItem = (int) Math.round(
                                    (double) ((mYFooterTop - getScaledYValue(MIN_MARGIN_ABOVE_FOOTER)) - (mYOffset - (mNumOffsetItems * getScaledYValue(DEFAULT_YOFFSET)))) /
                                            (double) (mNumOffsetItems + 1));
                            mYOffsetPerItem = Math.min(mYOffsetPerItem, getScaledYValue(MAX_YOFFSET));
                            mYOffsetPerItem = Math.max(mYOffsetPerItem, 0);
                            mTotalBitmapSpace = SAVE;
                        }
                        break;
                    }
                    case 2:
                    {
                        break;
                    }
                }
            }
        } catch (IOException e)
        {
            Log.e(TAG, "processPageFile failed: " + e.toString());
            snuffyPage = null;
        } catch (ParserConfigurationException e)
        {
            Log.e(TAG, "processPageFile failed: " + e.toString());
            snuffyPage = null;
        } catch (SAXException e)
        {
            Log.e(TAG, "processPageFile failed: " + e.toString());
            snuffyPage = null;
        }
        Log.d(TAG, ">>> processPageFile ends");
        return snuffyPage;
    }

    private void processBackgroundPW(Element root, SnuffyPage currPage)
    {
        String watermark = root.getAttribute("watermark");        // 	either this
        String backgroundImage = root.getAttribute("backgroundimage");    // 	or this
        String shadows = getStringAttributeValue(root, "shadows", "yes");

        mBackgroundColor = Color.parseColor(getStringAttributeValue(root, "color", DEFAULT_BACKGROUND_COLOR));
        currPage.setBackgroundColor(mBackgroundColor);
        currPage.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        if (backgroundImage.length() > 0)
        {
            Bitmap bm = getBitmapFromAssetOrFile(mContext, backgroundImage);
            if (bm != null)
            {
                ImageView iv = new ImageView(mContext);
                iv.setLayoutParams(new SnuffyLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0));
                iv.setImageBitmap(bm);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                currPage.addView(iv);
            }
        }
        if (watermark.length() > 0)
        {
            Bitmap bm = getBitmapFromAssetOrFile(mContext, watermark);
            if (bm != null)
            {
                ImageView iv = new ImageView(mContext);
                iv.setLayoutParams(new SnuffyLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0));
                iv.setImageBitmap(bm);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                currPage.addView(iv);
            }
        }
        if (shadows.equalsIgnoreCase("yes"))
        {
            Bitmap bmTop = getBitmapFromAssetOrFile(mContext, "grad_shad_top.png");
            if (bmTop != null)
            {
                ImageView iv = new ImageView(mContext);
                iv.setLayoutParams(new SnuffyLayoutParams(LayoutParams.MATCH_PARENT, getScaledYValue(bmTop.getHeight()), 0, 0));
                iv.setImageBitmap(bmTop);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                currPage.addView(iv);

            }
            Bitmap bmBot = getBitmapFromAssetOrFile(mContext, "grad_shad_bot.png");
            if (bmBot != null)
            {
                ImageView iv = new ImageView(mContext);
                iv.setLayoutParams(new SnuffyLayoutParams(LayoutParams.MATCH_PARENT, getScaledYValue(bmBot.getHeight()), 0, mPageHeight - getScaledYValue(bmBot.getHeight())));
                iv.setImageBitmap(bmBot);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                currPage.addView(iv);
            }
        }
    }

    public boolean processPackage(SnuffyApplication app,
                                  int pageWidth,
                                  int pageHeight,
                                  String packageName,
                                  String languageCode,
                                  Vector<SnuffyPage> pages,
                                  ProgressCallback progressCallback,
                                  Typeface alternateTypeface)
    {
        mAppRef = new WeakReference<SnuffyApplication>(app);
        mContext = app.getApplicationContext();
        mPageWidth = pageWidth;
        mPageHeight = pageHeight;
        mPackageName = packageName;
        mLanguageCode = languageCode;
        mPages = pages;
        mTotalBitmapSpace = 0;
        mImageFolderName = "Packages/" + mPackageName + "/shared/images/";
        mThumbsFolderName = "Packages/" + mPackageName + "/shared/thumbs/";
        mSharedFolderName = "Packages/shared/";
        mFromAssets = app.languageExistsAsAsset(mPackageName, mLanguageCode);
        mProgressCallback = progressCallback;
        mAlternateTypeface = alternateTypeface;

        // In the case where this package is replacing the previous package - release the memory occupied by the original
        bitmapCache.clear();
        mPages.clear();

        // Now process the package files and build the views
        String mainPackagefileName = "Packages/" + packageName + "/" + languageCode + ".xml";
        boolean bSuccess;
        InputStream isMain = null;
        try
        {
            if (mFromAssets)
                isMain = app.getAssets().open(mainPackagefileName, AssetManager.ACCESS_BUFFER); // read into memory since it's not very large
            else
                isMain = new BufferedInputStream(new FileInputStream(app.getDocumentsDir().getPath() + "/" + mainPackagefileName));
            bSuccess = processMainPackageFile(isMain);

        } catch (IOException e)
        {
            Log.e(TAG, "Cannot open or read main package file: " + mainPackagefileName);
            return false;
        } finally
        {
            if (isMain != null)
            {
                try
                {
                    isMain.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return bSuccess;
    }


    private boolean processMainPackageFile(InputStream isMain)
    {
        Log.d(TAG, ">>> processMainPackageFile starts");

        boolean bSuccess = false;

        Document xmlDoc;
        try
        {
            xmlDoc = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(isMain);
            org.w3c.dom.Element root = xmlDoc.getDocumentElement();
            if (root == null)
                throw new SAXException("XML Document has no root element");

            NodeList nlPages = root.getElementsByTagName("page");
            int numPages = nlPages.getLength();
            if (numPages == 0)
                throw new SAXException("Main Package file document must have at least one page node");
            NodeList nlAbout = root.getElementsByTagName("about");
            int numAbouts = nlAbout.getLength();
            if (numAbouts != 1)
                throw new SAXException("Main Package file document must have exactly one about node");
            NodeList nlPackageName = root.getElementsByTagName("packagename"); // packagename seems to have superseded displayname as the name for this element
            int numPackageNames = nlPackageName.getLength();
            NodeList nlDisplayName = root.getElementsByTagName("displayname"); // but some files (e.g. 4Laws/ru.xml) still have displayname ?
            int numDisplayNames = nlDisplayName.getLength();
            if ((numPackageNames + numDisplayNames) != 1)
                throw new SAXException("Main Package file document must have exactly one packagename or displayname node");
            if (numPackageNames == 1)
            {
                Element elPackage = (Element) nlPackageName.item(0);
                mPackageTitle = elPackage.getTextContent();
            }
            else
            {
                Element elPackage = (Element) nlDisplayName.item(0);
                mPackageTitle = elPackage.getTextContent();
            }

            Element elAbout = (Element) nlAbout.item(0);
            if (!processPage(elAbout))    // about will be page 0
                return false;

            mProgressCallback.updateProgress(0, numPages);
            for (int i = 0; i < numPages; i++)
            {
                Element elPage = (Element) nlPages.item(i);
                if (!processPage(elPage))
                    return false;
                mProgressCallback.updateProgress(i + 1, numPages);
            }

            bSuccess = true;
        } catch (IOException e)
        {
            Log.e(TAG, "processMainPackageFile failed: " + e.toString());
        } catch (ParserConfigurationException e)
        {
            Log.e(TAG, "processMainPackageFile failed: " + e.toString());
        } catch (SAXException e)
        {
            Log.e(TAG, "processMainPackageFile failed: " + e.toString());
        }
        Log.d(TAG, ">>> processMainPackageFile ends");
        return bSuccess;
    }

    private boolean processPage(Element elPage)
    {
        String thumbFileName = elPage.getAttribute("thumb");
        String pageFileName = elPage.getAttribute("filename");
        String description = elPage.getTextContent();
        Log.d(TAG, ">>> processPage: " + pageFileName);

        pageFileName = "Packages/" + mPackageName + "/" + mLanguageCode + "/" + pageFileName;

        SnuffyPage currPage;
        InputStream isPage = null;
        try
        {
            if (mFromAssets)
            {
                isPage = mContext.getAssets().open(pageFileName, AssetManager.ACCESS_BUFFER); // read into memory since it's not very large
            }
            else
            {
                isPage = new BufferedInputStream(new FileInputStream(mAppRef.get().getDocumentsDir().getPath() + "/" + pageFileName));
            }
            currPage = processPageFile(isPage);
            currPage.mDescription = description;
            //currPage.mThumbnail   = Uri.parse("file:///android_asset/" + mThumbsFolderName + thumbFileName).toString();
            currPage.mThumbnail = mThumbsFolderName + thumbFileName;
            mPages.add(currPage);
        } catch (IOException e)
        {
            Log.e(TAG, "Cannot open or read page file: " + pageFileName);
            return false;
        } finally
        {
            if (isPage != null)
            {
                try
                {
                    isPage.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }


    private SnuffyPage processPageFile(InputStream isPage)
    {
        Log.d(TAG, ">>> processPageFile starts");

        SnuffyPage currPage = null;

        Document xmlDoc;
        try
        {
            xmlDoc = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(isPage);
            Element root = xmlDoc.getDocumentElement();
            if (root == null)
                throw new SAXException("XML Document has no root element");

            mYOffsetPerItem = getScaledYValue(DEFAULT_YOFFSET);
            int SAVE = mTotalBitmapSpace;
            for (int iPass = 1; iPass <= 2; iPass++)
            {
                currPage = new SnuffyPage(mContext);

                mYOffset = 0;
                mYFooterTop = mPageHeight;
                mNumOffsetItems = 0;
                addCover(currPage);
                processBackground(root, currPage);
                processPageElements(root, currPage);
                switch (iPass)
                {
                    case 1:
                    {
                        if (mNumOffsetItems < 1)
                        {
                            // no second pass required
                            iPass = 2; // force exit from loop
                        }
                        else
                        {
                            mYOffsetPerItem = (int) Math.round(
                                    (double) ((mYFooterTop - getScaledYValue(MIN_MARGIN_ABOVE_FOOTER)) - (mYOffset - (mNumOffsetItems * getScaledYValue(DEFAULT_YOFFSET)))) /
                                            (double) (mNumOffsetItems + 1));
                            mYOffsetPerItem = Math.min(mYOffsetPerItem, getScaledYValue(MAX_YOFFSET));
                            mYOffsetPerItem = Math.max(mYOffsetPerItem, 0);
                            mTotalBitmapSpace = SAVE;
                        }
                        break;
                    }
                    case 2:
                    {
                        break;
                    }
                }
            }
        } catch (IOException e)
        {
            Log.e(TAG, "processPageFile failed: " + e.toString());
            currPage = null;
        } catch (ParserConfigurationException e)
        {
            Log.e(TAG, "processPageFile failed: " + e.toString());
            currPage = null;
        } catch (SAXException e)
        {
            Log.e(TAG, "processPageFile failed: " + e.toString());
            currPage = null;
        }
        Log.d(TAG, ">>> processPageFile ends");
        return currPage;

    }

    private void addCover(SnuffyPage currPage)
    {
        SnuffyLayout theCover = new SnuffyLayout(mContext);
        theCover.setLayoutParams(new SnuffyLayoutParams(mPageWidth, mPageHeight, 0, 0));
        theCover.setBackgroundColor(Color.TRANSPARENT); // overwritten in SnuffyPage.setCover
        theCover.setVisibility(View.GONE);
        currPage.addView(theCover);
        currPage.setCover(theCover); // save obj refs into page for hideActivePanel() method
    }

    private void processBackground(Element root, SnuffyPage currPage)
    {
        String watermark = root.getAttribute("watermark");        // 	either this
        String backgroundImage = root.getAttribute("backgroundimage");    // 	or this
        //int numButtons = Integer.valueOf(root.getAttribute("buttons"), 10); // but redundant since we get it from the DOM below
        String shadows = getStringAttributeValue(root, "shadows", "yes");

        mBackgroundColor = Color.parseColor(getStringAttributeValue(root, "color", DEFAULT_BACKGROUND_COLOR));
        currPage.setBackgroundColor(mBackgroundColor);
        currPage.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        //currPage.setOrientation(LinearLayout.VERTICAL); // only used with LinearLayout

        if (backgroundImage.length() > 0)
        {
            Bitmap bm = getBitmapFromAssetOrFile(mContext, backgroundImage);
            if (bm != null)
            {
                ImageView iv = new ImageView(mContext);
                iv.setLayoutParams(new SnuffyLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0));
                iv.setImageBitmap(bm);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                currPage.addView(iv);
            }
        }
        if (watermark.length() > 0)
        {
            Bitmap bm = getBitmapFromAssetOrFile(mContext, watermark);
            if (bm != null)
            {
                ImageView iv = new ImageView(mContext);
                iv.setLayoutParams(new SnuffyLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0));
                iv.setImageBitmap(bm);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                currPage.addView(iv);
            }
        }
        if (shadows.equalsIgnoreCase("yes"))
        {
            Bitmap bmTop = getBitmapFromAssetOrFile(mContext, "grad_shad_top.png");
            if (bmTop != null)
            {
                ImageView iv = new ImageView(mContext);
                iv.setLayoutParams(new SnuffyLayoutParams(LayoutParams.MATCH_PARENT, getScaledYValue(bmTop.getHeight()), 0, 0));
                iv.setImageBitmap(bmTop);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                currPage.addView(iv);

            }
            Bitmap bmBot = getBitmapFromAssetOrFile(mContext, "grad_shad_bot.png");
            if (bmBot != null)
            {
                ImageView iv = new ImageView(mContext);
                iv.setLayoutParams(new SnuffyLayoutParams(LayoutParams.MATCH_PARENT, getScaledYValue(bmBot.getHeight()), 0, mPageHeight - getScaledYValue(bmBot.getHeight())));
                iv.setImageBitmap(bmBot);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                currPage.addView(iv);
            }
        }
    }

    private void processPageElements(Element root, SnuffyPage currPage)
    {
        int numButtons = 0; // buttons are numbered from 1 to 9 and used as tag ranges: 1-9, 11-19 etc
        Vector<String> urlsOnpage = new Vector<String>(0);
        Node node = root.getFirstChild();
        while (node != null)
        {
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                Element el = (Element) node;
                if (el.getTagName().equalsIgnoreCase("button"))
                    processButton(currPage, el, ++numButtons, urlsOnpage);
                if (el.getTagName().equalsIgnoreCase("question"))
                    processQuestion(currPage, el);
                if (el.getTagName().equalsIgnoreCase("text"))
                    processText(currPage, el);
                if (el.getTagName().equalsIgnoreCase("title"))
                    processTitle(currPage, el);
            }
            node = node.getNextSibling();
        }
    }

    private void processButton(SnuffyPage currPage, Element elButton, int iButton, Vector<String> urlsOnpage)
    {
        int iTagButtonContainer = iButton + 100;
        int iTagButtonPanel = iButton + 1000;
        String mode = getStringAttributeValue(elButton, "mode", "");
        final boolean bUrlMode = mode.equalsIgnoreCase("url");
        final boolean bAllUrlMode = mode.equalsIgnoreCase("allurl");
        final boolean bPhoneMode = mode.equalsIgnoreCase("phone");
        final boolean bEmailMode = mode.equalsIgnoreCase("email");
        int yPos = getIntegerAttributeValue(elButton, "y", 0);
        int size = getIntegerAttributeValue(elButton, "size", 100);
        int yOffset = getIntegerAttributeValue(elButton, "yoffset", 0);
        String label = getStringAttributeValue(elButton, "label", "");


        yPos = getScaledYValue(yPos);
        yOffset = getScaledYValue(yOffset);

        if (yPos == 0)
        {
            yPos = mYOffset + yOffset;
            // add the default offset too
            if (!(bUrlMode || bPhoneMode || bEmailMode))
            {        // url and like  buttons are not offset from text above
                yPos += mYOffsetPerItem;
                mNumOffsetItems++;
            }
        }

        if (bUrlMode || bPhoneMode || bEmailMode)
        {
            // Could share code with processPuttonPanelButton?
            // That is where the following code came from
            final float DEFAULT_BUTTON_TEXT_SIZE = 19.0f;
            final int BUTTON_MARGIN = 20;
            final int BUTTON_YOFFSET_TOP = 16; // was 30 but not so good on en_US, final page
            final int BUTTON_YOFFSET_BOTTOM = 5;

            final String content = getTextContentImmediate(elButton);
            int margin = getScaledXValue(BUTTON_MARGIN);
            if (elButton.getAttribute("y").length() == 0)
                mYOffset += getScaledYValue(BUTTON_YOFFSET_TOP);
            Button button = new Button(mContext);
            button.setText(label.length() > 0 ? label : content);
            button.setSingleLine();
            button.setEllipsize(TruncateAt.END);
            button.setTextSize(getScaledTextSize(DEFAULT_BUTTON_TEXT_SIZE * size / 100.0f));
            button.setTextColor(mBackgroundColor);
            button.setLayoutParams(new SnuffyLayoutParams(mPageWidth - 2 * margin, LayoutParams.WRAP_CONTENT, margin, mYOffset));


            currPage.addView(new SnuffyAlternateTypefaceTextView(button)
                    .setAlternateTypeface(mAlternateTypeface)
                    .get());

            button.measure(
                    MeasureSpec.makeMeasureSpec(mPageWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.UNSPECIFIED);
            int height = button.getMeasuredHeight();
            mYOffset += height;
            mYOffset += getScaledYValue(BUTTON_YOFFSET_BOTTOM);
            if (bUrlMode)
            {
                // save urls for the addurl button - if any - that must be final button on page
                urlsOnpage.add(content);
            }

            setupUrlButtonHandler(currPage, button, mode, content);
        }
        else
        {
            // Button consists of container with:
            //		HR above and below
            //		Disclosure indicator
            //		button text (and image if its a "big" button)
            SnuffyLayout theContainer = new SnuffyLayout(mContext);
            int yPosInContainer = 0;
            int buttonWidth = (mPageWidth - BUTTON_MARGINX - BUTTON_MARGINX);

            // add 2 pixel thick horizontal rule above the button
            View hr;
            hr = getHRView(Color.parseColor("#4c4c4c"), buttonWidth, BUTTON_HR_MARGINX, yPosInContainer, 1);
            theContainer.addView(hr);
            yPosInContainer += 1;
            hr = getHRView(Color.parseColor("#ffffff"), buttonWidth, BUTTON_HR_MARGINX, yPosInContainer, 1);
            theContainer.addView(hr);
            yPosInContainer += 1;

            theContainer.setLayoutParams(new SnuffyLayoutParams(buttonWidth,
                    yPosInContainer, BUTTON_MARGINX, yPos));
            processButtonButton(elButton, 1000 + iButton, false, theContainer);
            SnuffyLayoutParams lpContainer = (SnuffyLayoutParams) (theContainer.getLayoutParams());
            yPosInContainer = lpContainer.height; // we add to bottom of the container

            // add 2 pixel thick horizontal rule below the button
            hr = getHRView(Color.parseColor("#4c4c4c"), buttonWidth, BUTTON_HR_MARGINX, yPosInContainer, 1);
            theContainer.addView(hr);
            yPosInContainer += 1;
            hr = getHRView(Color.parseColor("#ffffff"), buttonWidth, BUTTON_HR_MARGINX, yPosInContainer, 1);
            theContainer.addView(hr);
            yPosInContainer += 1;

            int buttonHeight = yPosInContainer;
            theContainer.setLayoutParams(new SnuffyLayoutParams(buttonWidth, buttonHeight, BUTTON_MARGINX, yPos));
            theContainer.setBackgroundColor(Color.TRANSPARENT);
            theContainer.setTag(iTagButtonContainer);
            currPage.addView(theContainer);
            mYOffset = yPos + yPosInContainer;

            processButtonPanel(currPage, elButton, iButton, urlsOnpage);

            final SnuffyLayout panel = (SnuffyLayout) (currPage.findViewWithTag(iTagButtonPanel));
            if (bAllUrlMode)
            {

                String content = "";
                for (String aFinalUrlsOnPage : urlsOnpage)
                {
                    content += "http://" + aFinalUrlsOnPage + "\n";
                }
                setupUrlButtonHandler(currPage, theContainer, mode, content);
            }
            else if (panel != null)
            { // not all buttons have panels (e.g. url buttons)
                final int EXPANDED_PANEL_OFFSET_FROM_BOTTOM_OF_PAGE = 10;

                final SnuffyPage thePage = currPage;
                final SnuffyLayout theButton = theContainer;
                final SnuffyLayout thePanelContent = (SnuffyLayout) (panel.getChildAt(1)); // 0 = the button, 1= container for panel content (Could use a tag instead?)

                // Determine size and position:
                // a) the panel to be shown
                SnuffyLayoutParams lp = (SnuffyLayoutParams) (panel.getLayoutParams());
                int panelHeight = lp.height;
                // b) the button view that was clicked at will appear to transform into the panel
                final int yBefore = yPos;
                // c) the bottom of the page which (less a 10 pixel buffer) determines the lowest allowable point in the panel
                // and therefore whether the panel needs to animate up from the button's position.
                int yLowest = mPageHeight - getScaledYValue(EXPANDED_PANEL_OFFSET_FROM_BOTTOM_OF_PAGE);

                final int yAfter =
                        ((yBefore + panelHeight) > yLowest)
                                ? yLowest - panelHeight
                                : yBefore;

                // Place panel over button
                final SnuffyLayoutParams lpBefore = new SnuffyLayoutParams(lp.width, buttonHeight, lp.x, yBefore);  // button height, button locn
                final SnuffyLayoutParams lpAfter = new SnuffyLayoutParams(lp.width, lp.height, lp.x, yAfter); // full height, final locn

                // Create the shadow view with its 5 images
                // IOS Equivalent of this is in standardViewController.m: showShadow

                final int DROPSHADOW_X = getScaledXValue(10);    // The 30 pixel wide image will be shrunk to this size
                final int DROPSHADOW_Y = getScaledYValue(10);    // The 30 pixel wide image will be shrunk to this size
                //final int TAG_SHAD_NE = 549;
                //final int TAG_SHAD_E  = 546;
                //final int TAG_SHAD_SE = 543;
                //final int TAG_SHAD_S  = 542;
                //final int TAG_SHAD_SW = 541;
                //final int TAG_SHADOW  = 2000;	// + iButton

                final SnuffyLayout shadView = new SnuffyLayout(mContext);
                //shadView.setTag(new Integer(TAG_SHADOW+iButton));
                shadView.setBackgroundColor(Color.TRANSPARENT);
                final SnuffyLayoutParams lpBeforeShad = new SnuffyLayoutParams(lp.width + DROPSHADOW_X, lp.height + DROPSHADOW_Y, lp.x, yBefore);
                final SnuffyLayoutParams lpAfterShad = new SnuffyLayoutParams(lp.width + DROPSHADOW_X, lp.height + DROPSHADOW_Y, lp.x, yAfter);
                //shadView.setLayoutParams(lpBeforeShad);
                shadView.setVisibility(View.GONE);
                currPage.addView(shadView);

                // grad_shad_NE
                Bitmap bmNE = getBitmapFromAssetOrFile(mContext, "grad_shad_NE.png");
                if (bmNE != null)
                {
                    ImageView iv = new ImageView(mContext);
                    //iv.setTag(new Integer(TAG_SHAD_NE));
                    iv.setLayoutParams(new SnuffyLayoutParams(
                            DROPSHADOW_X,
                            DROPSHADOW_Y,
                            lp.width,
                            0));
                    iv.setImageBitmap(bmNE);
                    iv.setScaleType(ImageView.ScaleType.FIT_XY);
                    //iv.setBackgroundColor(Color.WHITE); // debug
                    shadView.addView(iv);
                }

                // grad_shad_E
                Bitmap bmE = getBitmapFromAssetOrFile(mContext, "grad_shad_E.png");
                if (bmE != null)
                {
                    ImageView iv = new ImageView(mContext);
                    //iv.setTag(new Integer(TAG_SHAD_E));
                    iv.setLayoutParams(new SnuffyLayoutParams(
                            DROPSHADOW_X,
                            lp.height - DROPSHADOW_Y,
                            lp.width,
                            DROPSHADOW_Y));
                    iv.setImageBitmap(bmE);
                    iv.setScaleType(ImageView.ScaleType.FIT_XY);
                    //iv.setBackgroundColor(Color.WHITE); // debug
                    shadView.addView(iv);
                }

                // grad_shad_SE
                Bitmap bmSE = getBitmapFromAssetOrFile(mContext, "grad_shad_SE.png");
                if (bmSE != null)
                {
                    ImageView iv = new ImageView(mContext);
                    //iv.setTag(new Integer(TAG_SHAD_SE));
                    iv.setLayoutParams(new SnuffyLayoutParams(
                            DROPSHADOW_X,
                            DROPSHADOW_Y,
                            lp.width,
                            lp.height));
                    iv.setImageBitmap(bmSE);
                    iv.setScaleType(ImageView.ScaleType.FIT_XY);
                    //iv.setBackgroundColor(Color.WHITE); // debug
                    shadView.addView(iv);
                }

                // grad_shad_S
                Bitmap bmS = getBitmapFromAssetOrFile(mContext, "grad_shad_S.png");
                if (bmS != null)
                {
                    ImageView iv = new ImageView(mContext);
                    //iv.setTag(new Integer(TAG_SHAD_S));
                    iv.setLayoutParams(new SnuffyLayoutParams(
                            lp.width - DROPSHADOW_X,
                            DROPSHADOW_Y,
                            DROPSHADOW_X,
                            lp.height));
                    iv.setImageBitmap(bmS);
                    iv.setScaleType(ImageView.ScaleType.FIT_XY);
                    //iv.setBackgroundColor(Color.WHITE); // debug
                    shadView.addView(iv);
                }

                // grad_shad_SW
                Bitmap bmSW = getBitmapFromAssetOrFile(mContext, "grad_shad_SW.png");
                if (bmSW != null)
                {
                    ImageView iv = new ImageView(mContext);
                    //iv.setTag(new Integer(TAG_SHAD_SW));
                    iv.setLayoutParams(new SnuffyLayoutParams(
                            DROPSHADOW_X,
                            DROPSHADOW_Y,
                            0,
                            lp.height));
                    iv.setImageBitmap(bmSW);
                    iv.setScaleType(ImageView.ScaleType.FIT_XY);
                    //iv.setBackgroundColor(Color.WHITE); // debug
                    shadView.addView(iv);
                }

                theContainer.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View v)
                    {
                        // Show the panel when user clicks on the button's container

                        final Runnable animIn3 = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                thePage.requestLayout();
                                thePage.forceLayout();
                                thePage.invalidate();
                            }
                        };

                        final Runnable animIn2 = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                shadView.setVisibility(View.GONE);
                                panel.setVisibility(View.GONE);
                                theButton.setVisibility(View.VISIBLE);
                                thePage.requestLayout();
                                thePage.forceLayout();
                                thePage.invalidate();
                                // schedule a redraw again - on some slower devices, after the animation in animIn1,
                                //  the background doesn't seem to get drawn?
                                // (unfortunately the result is a black flash)
                                new Handler().postDelayed(animIn3, 250); // Needs to be this long or it still doesn't take

                            }
                        };

                        final Runnable animIn1 = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                final int THIS_ANIMATION_DURATION = 250;

                                TranslateAnimation translateAnimation = new TranslateAnimation(
                                        Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                                        Animation.ABSOLUTE, 0, Animation.ABSOLUTE, yBefore - yAfter);
                                //translateAnimation.setFillAfter(true); // eliminates flicker but results in next layout being mispositioned.
                                translateAnimation.setDuration(THIS_ANIMATION_DURATION);
                                AnimationListener listener = new SimpleAnimationListener(animIn2);
                                translateAnimation.setAnimationListener(listener);
                                //translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator()); // isn't this the default anyway?
                                panel.startAnimation(translateAnimation);

                                TranslateAnimation taShad = new TranslateAnimation(
                                        Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                                        Animation.ABSOLUTE, 0, Animation.ABSOLUTE, yBefore - yAfter);
                                taShad.setDuration(THIS_ANIMATION_DURATION);
                                shadView.startAnimation(taShad);
                            }
                        };

                        final Runnable animOut4 = new Runnable()
                        {
                            /*
                             * animationCleanup:finished:context
                             *	if was contracting then hide maskview and shadow view
                            */
                            @Override
                            public void run()
                            {

                            }
                        };

                        final Runnable animOut3 = new Runnable()
                        {
                            // IOS version: method = animateContentsIn
                            @Override
                            public void run()
                            {
                                final int THIS_ANIMATION_DURATION = 250;

                                // Fade in the panel content
                                AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
                                animation.setDuration(THIS_ANIMATION_DURATION);
                                animation.setFillAfter(true); // Tell it to persist after the animation ends
                                AnimationListener listener = new SimpleAnimationListener(animOut4);
                                animation.setAnimationListener(listener);
                                thePanelContent.startAnimation(animation);

                                panel.setLayoutParams(lpAfter);
                                shadView.setLayoutParams(lpAfterShad);
                                thePage.requestLayout();
                                thePage.forceLayout();
                                thePage.invalidate();
                            }
                        };

                        final Runnable animOut1 = new Runnable()
                        {
                            /*
                             * IOS version: method = showShadow
                             * 		set shadow frames small around button
                             * 		set mask view alpha to 0 and not hidden
                             * 		tell page that expand is active (activeViewMasked)
                             * 		bring to front: mask, shad, panel, button, arrow
                             * 		set shadow view alpha to 0 and not hidden
                             * 		set panel view alpha to 0 and not hidden
                             * 		init panel from to be identical to button
                             * 		BEGIN ANIMATION (0.25 sec)
                             * 			set button frame 2 wider
                             * 			scale button 0.09% larger?
                             * 			set shadow frames 10 pixels to right
                             * 			set mask alpha to 0.2
                             * 			set shad alpha to 1.0
                             * 			set panel alpha to 1.0
                             * 			set panel frame to locn and size of "button large" (what is that?)
                             * 			rotate arrow anti-clockwise 90 deg
                             * 			move arrow to new loc (arrow view large)
                             * 		COMMIT ANIMATION
                             *
                            */
                            @Override
                            public void run()
                            {
                                final int THIS_ANIMATION_DURATION = 500;

                                panel.setLayoutParams(lpBefore);
                                shadView.setLayoutParams(lpBeforeShad);

                                theButton.setVisibility(View.INVISIBLE);
                                shadView.setVisibility(View.VISIBLE);
                                panel.setVisibility(View.VISIBLE);

                                thePage.requestLayout();
                                thePage.forceLayout();
                                thePage.invalidate();

                                thePage.showCover(animIn1, false); // must be after we have brought any other views to front since cover must end up on top
                                shadView.bringToFront();
                                panel.bringToFront();

                                AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 0.0f); // start with it transparent, will fade it in once panel is expanded.
                                alphaAnimation.setDuration(0); // Make animation instant. setAlpha not available before API 11! yet AlphaAnimation does exist from API1
                                alphaAnimation.setFillAfter(true); // Tell it to persist after the animation ends
                                thePanelContent.startAnimation(alphaAnimation);

                                TranslateAnimation translateAnimation = new TranslateAnimation(
                                        Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                                        Animation.ABSOLUTE, 0, Animation.ABSOLUTE, yAfter - yBefore);
                                //translateAnimation.setFillAfter(true); // eliminates flicker but results in next layout being mispositioned.
                                int duration;
                                if (yAfter != yBefore)
                                    duration = THIS_ANIMATION_DURATION;
                                else
                                    duration = 1;
                                translateAnimation.setDuration(duration);
                                //float yMult = (float) lp.height / (float)v.getHeight();
                                //ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.0f, 1.0f, yMult, 0.0f, 0.0f);
                                //scaleAnimation.setDuration(translateAnimation.getDuration());
                                AnimationListener listener = new SimpleAnimationListener(animOut3);
                                translateAnimation.setAnimationListener(listener);
                                //translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator()); // isn't this the default anyway?
                                panel.startAnimation(translateAnimation);

                                TranslateAnimation taShad = new TranslateAnimation(
                                        Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                                        Animation.ABSOLUTE, 0, Animation.ABSOLUTE, yAfter - yBefore);
                                taShad.setDuration(duration);
                                shadView.startAnimation(taShad);
                            }
                        };
                        // launch the first animation in the chain
                        new Handler().post(animOut1);
                    }
                });
            }
        }
    }

    private void processButtonButton(Element elButton, int iButton, Boolean bExpanded,
                                     SnuffyLayout theContainer)
    {
        // Add the button to its container.
        // Two button views are created per button on the page.
        // This function is called once to add the button to the page
        // And it       is called again to add the button to its panel

        // Note that mode can include phone, email allurl and and url although the main use for these in the About pages

        SnuffyLayoutParams lpContainer = (SnuffyLayoutParams) (theContainer.getLayoutParams());
        int yPosInContainer = lpContainer.height; // we add to bottom of the container
        int containerWidth = lpContainer.width;
        int marginX = BUTTON_TEXT_MARGINX; // ?? 22Jan2012 - (REFERENCE_DEVICE_WIDTH-lpContainer.width)/2;

        Element elButtonText = getChildElementNamed(elButton, "buttontext");
        String content;
        if (elButtonText == null)
            content = getTextContentImmediate(elButton);        // used when mode=url
        else
            content = getTextContentImmediate(elButtonText);    // used with paneltext
        Element elImage = getChildElementNamed(elButton, "image");
        String mode = getStringAttributeValue(elButton, "mode", "");
        boolean bAllUrlMode = mode.equalsIgnoreCase("allurl");
        boolean bUrlMode = mode.equalsIgnoreCase("url");
        boolean bBigMode = mode.equalsIgnoreCase("big");
        int xPos = getIntegerAttributeValue(elButton, "x", marginX);
        int width = getIntegerAttributeValue(elButton, "w", Math.min(containerWidth, 280));    // 280 is precise limit set in iPhone version
        int size = getIntegerAttributeValue(elButton, "size", 100);
        if (elButtonText != null)
            size = getIntegerAttributeValue(elButtonText, "size", 100);
        String align = getStringAttributeValue(elButton, "textalign", (bUrlMode || bBigMode || bAllUrlMode) ? "center" : "left");
        String modifier = getStringAttributeValue(elButton, "modifier", "");
        float alpha = getFloatAttributeValue(elButton, "alpha", 1.0f);
        int color = getColorAttributeValue(elButton, "color", Color.WHITE);

        // TODO: bUrlMode - WHITE BGD, backgroundColor for text, NO HR, BUTTON CLICK Run the URL
        // Can we make the object a Button class?

        xPos = getScaledXValue(xPos);
        width = getScaledXValue(width);
        color = setColorAlphaVal(color, alpha);
        if (align.equalsIgnoreCase("center"))
        {
            xPos = 0;
            width = containerWidth;
        }
        if ((xPos > 0) && (elButton.getAttribute("w").length() == 0))
            width = containerWidth - xPos - getScaledXValue(marginX);

        if (bBigMode)
        {
            // image goes above the text and centered ( + some padding?)
            if (elImage != null)
            {
                String imageFileName = getTextContentImmediate(elImage);
                if (imageFileName.length() > 0)
                {
                    Bitmap bm = getBitmapFromAssetOrFile(mContext, imageFileName);
                    if (bm != null)
                    {
                        int imageHeight = getScaledYValue(bm.getHeight());
                        ImageView iv = new ImageView(mContext);
                        iv.setLayoutParams(new SnuffyLayoutParams(LayoutParams.MATCH_PARENT, imageHeight, getScaledXValue(xPos), yPosInContainer));
                        iv.setImageBitmap(bm);
                        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        theContainer.addView(iv);
                        yPosInContainer += getScaledYValue(104);    // a fixed height used in the iPhone code/
                        // sometimes, by design, the image extends below the button text
                    }
                }
            }
        }

        // The button text
        // centered vertically inside a fixed height
        int buttonHeight = getScaledYValue(36);

        TextView tv = new TextView(mContext);
        tv.setLayoutParams(new SnuffyLayoutParams(
                width == 0 ? LayoutParams.WRAP_CONTENT : width,
                buttonHeight,
                xPos, yPosInContainer));
        tv.setText(content);
        tv.setSingleLine();
        tv.setGravity(getGravityFromAlign(align) + Gravity.CENTER_VERTICAL);
        tv.setTypeface(null, getTypefaceFromModifier(modifier));
        tv.setTextColor(color);
        tv.setTextSize(getScaledTextSize(DEFAULT_BUTTON_TEXT_SIZE * size / 100.0f));
        theContainer.addView(new SnuffyAlternateTypefaceTextView(tv)
                .setAlternateTypeface(mAlternateTypeface)
                .get());


        SnuffyLayoutParams lp = (SnuffyLayoutParams) tv.getLayoutParams();
        tv.measure(
                MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY),
                MeasureSpec.UNSPECIFIED);
        lp = (SnuffyLayoutParams) tv.getLayoutParams();
        yPosInContainer += buttonHeight;

        // add the disclosure indicator at the right of the button

        if (!bAllUrlMode)
        {
            ImageView iv = new ImageView(mContext);
            //SnuffyDisclosureIndicator di = new SnuffyDisclosureIndicator(mContext);
            //iv.setImageDrawable(di);
            if (bExpanded)
            {
                iv.setImageResource(R.drawable.disclosure_indicator_maximized);
                iv.setLayoutParams(new SnuffyLayoutParams(buttonHeight, buttonHeight, containerWidth - buttonHeight - getScaledXValue(0), lp.y + 2));
            }
            else
            {
                iv.setImageResource(R.drawable.disclosure_indicator_minimized);
                iv.setLayoutParams(new SnuffyLayoutParams(buttonHeight, buttonHeight, containerWidth - buttonHeight + getScaledXValue(5), lp.y + 2));
            }
            theContainer.addView(iv);
        }

        // add margin below the button
        yPosInContainer += BUTTON_MARGIN_BOTTOM;

        theContainer.setLayoutParams(new SnuffyLayoutParams(lpContainer.width, lpContainer.height + yPosInContainer, lpContainer.x, lpContainer.y));

        theContainer.setTag(iButton);
    }

    private void processButtonPanel(SnuffyPage currPage, Element elButton, int iButton, Vector<String> urlsOnPage)
    {
        final int PANEL_XMARGIN = 5;        // from edge of page to panel
        final int PANEL_YMARGIN = 5;        // from edge of page to panel
        final int PANEL_ITEM_DEFAULT_YOFFSET = 10;

        int panelWidth = mPageWidth - PANEL_XMARGIN - PANEL_XMARGIN;

        Element elPanel = getChildElementNamed(elButton, "panel");
        if (elPanel == null)
            return;

        SnuffyLayout thePanel = new SnuffyLayout(mContext); // contains...
        SnuffyLayout theButton = new SnuffyLayout(mContext); // this above...
        SnuffyLayout theContainer = new SnuffyLayout(mContext); // this

        mYOffsetInPanel = 0;
        mYOffsetMaxInPanel = 0;

        Node node = elPanel.getFirstChild();
        while (node != null)
        {
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                Element el = (Element) node;
                mYOffsetInPanel += getScaledYValue(PANEL_ITEM_DEFAULT_YOFFSET);
                if (el.getTagName().equalsIgnoreCase("text"))
                    processButtonPanelText(elPanel, el, theContainer, panelWidth);
                if (el.getTagName().equalsIgnoreCase("image"))
                    processButtonPanelImage(elPanel, el, theContainer, panelWidth);
                if (el.getTagName().equalsIgnoreCase("button"))
                    processButtonPanelButton(el, currPage, theContainer, panelWidth, urlsOnPage);
            }
            node = node.getNextSibling();
        }

        theButton.setLayoutParams(new SnuffyLayoutParams(panelWidth, 0, 0, 2)); // yPos=2 to allow for the HR drawn when not in panel
        processButtonButton(elButton, iButton, true, theButton);
        thePanel.addView(theButton);
        SnuffyLayoutParams lpButton = (SnuffyLayoutParams) (theButton.getLayoutParams());
        int buttonHeight = lpButton.height + lpButton.y;

        // TODO: limit container width to 280 to match iPhone code
        int containerHeight = mYOffsetMaxInPanel;
        theContainer.setLayoutParams(new SnuffyLayoutParams(panelWidth, containerHeight, 0, buttonHeight));
        theContainer.setBackgroundColor(Color.TRANSPARENT);
        thePanel.addView(theContainer);

        // TODO: add a frame around all 4 sides of the container: 1 pixel, black, Alpha 0.6
        // composed of 4 views as in title (VR) and button (hr)

        // panel Y coord will be relocated at runtime to cover its associated button and then animated, with button,
        // so that bottom of panel is at bottom of screen (if whole panel was not already able to fit on-screen)
        int yPanelTop = 50;
        thePanel.setLayoutParams(new SnuffyLayoutParams(panelWidth, PANEL_YMARGIN + buttonHeight + containerHeight + PANEL_YMARGIN, PANEL_XMARGIN, yPanelTop));
        thePanel.setBackgroundColor(mBackgroundColor);
        thePanel.setTag(1000 + iButton);
        thePanel.setVisibility(View.INVISIBLE); // hide it initially - will be animated into position on click on its owner button.
        // CANT DO THIS? thePanel.setAlpha(0.0f);				// also hides it

        currPage.addView(thePanel);
    }

    private void processButtonPanelButton(Element elButton, SnuffyPage currPage, SnuffyLayout theContainer, int panelWidth, Vector<String> urlsOnPage)
    {

        final float DEFAULT_BUTTON_TEXT_SIZE = 19.0f;
        final int BUTTON_MARGIN = 20;

        String mode = getStringAttributeValue(elButton, "mode", "url");
        int size = getIntegerAttributeValue(elButton, "size", 100);

        final String content = getTextContentImmediate(elButton);
        int margin = getScaledXValue(BUTTON_MARGIN);

        Button button = new Button(mContext);
        button.setText(content);
        button.setTextSize(getScaledTextSize(DEFAULT_BUTTON_TEXT_SIZE * size / 100.0f));
        button.setTextColor(mBackgroundColor);
        button.setLayoutParams(new SnuffyLayoutParams(panelWidth - 2 * margin, LayoutParams.WRAP_CONTENT, margin, mYOffsetInPanel));


        theContainer.addView(new SnuffyAlternateTypefaceTextView(button)
                .setAlternateTypeface(mAlternateTypeface)
                .get());

        button.measure(
                MeasureSpec.makeMeasureSpec(panelWidth, MeasureSpec.EXACTLY),
                MeasureSpec.UNSPECIFIED);
        int height = button.getMeasuredHeight();

        mYOffsetInPanel += height;
        if (mYOffsetInPanel > mYOffsetMaxInPanel)
            mYOffsetMaxInPanel = mYOffsetInPanel;

        urlsOnPage.add(content);

        setupUrlButtonHandler(currPage, button, mode, content);
    }

    private void processButtonPanelImage(Element elPanel, Element elImage, SnuffyLayout theContainer, int panelWidth)
    {
        // inherited value
        int yBase = getIntegerAttributeValue(elPanel, "y", mYOffsetInPanel);
        // own values
        int xPos = getIntegerAttributeValue(elImage, "x", 0);
        int yPos = getIntegerAttributeValue(elImage, "y", 0);
        int width = getIntegerAttributeValue(elImage, "w", 0);
        int height = getIntegerAttributeValue(elImage, "h", 0);
        int yOffset = getIntegerAttributeValue(elImage, "yoffset", 0);
        String align = getStringAttributeValue(elImage, "align", getStringAttributeValue(elPanel, "align", "left")); // v2 align or textalign here?
        xPos = getScaledXValue(xPos);
        yPos = getScaledYValue(yPos);
        width = getScaledXValue(width);
        height = getScaledYValue(height);
        yOffset = getScaledYValue(yOffset);
        if (align.equalsIgnoreCase("center"))
        {
            xPos = (panelWidth - width) / 2;
        }
        if (yPos == 0)
            yPos = yBase;
        yPos += yOffset;

        String imageFileName = getTextContentImmediate(elImage);
        if (imageFileName.length() > 0)
        {
            Bitmap bm = getBitmapFromAssetOrFile(mContext, imageFileName);
            if (bm != null)
            {
                ImageView iv = new ImageView(mContext);
                iv.setLayoutParams(new SnuffyLayoutParams(width, height, xPos, yPos));
                iv.setImageBitmap(bm);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                theContainer.addView(iv);

                mYOffsetInPanel = yPos + height;
                if (mYOffsetInPanel > mYOffsetMaxInPanel)
                    mYOffsetMaxInPanel = mYOffsetInPanel;
            }
        }
    }

    private void processButtonPanelText(Element elPanel, Element elText, SnuffyLayout theContainer, int panelWidth)
    {
        final int PANEL_TEXT_LEFT_MARGIN = 10;    // from panel edge to text contained in it.
        final int PANEL_TEXT_RIGHT_MARGIN = 15;    // from panel edge to text contained in it.

        // inherited value
        int yBase = getIntegerAttributeValue(elPanel, "y", mYOffsetInPanel);
        // own values
        String content = elText.getTextContent();
        int xPos = getIntegerAttributeValue(elText, "x", 0);
        int yPos = getIntegerAttributeValue(elText, "y", 0);
        int width = getIntegerAttributeValue(elText, "w", 0);
        int height = getIntegerAttributeValue(elText, "h", 0);
        int xOffset = getIntegerAttributeValue(elText, "xoffset", 0);
        int yOffset = getIntegerAttributeValue(elText, "yoffset", 0);
        int size = getIntegerAttributeValue(elText, "size", 100);
        String align = getStringAttributeValue(elText, "textalign", getStringAttributeValue(elPanel, "textalign", "left"));
        String modifier = getStringAttributeValue(elText, "modifier", "");
        float alpha = getFloatAttributeValue(elText, "alpha", 1.0f);
        int color = getColorAttributeValue(elText, "color", Color.WHITE);

        xPos = getScaledXValue(xPos);
        yPos = getScaledYValue(yPos);
        width = getScaledXValue(width);
        height = getScaledYValue(height);
        xOffset = getScaledXValue(xOffset);
        yOffset = getScaledYValue(yOffset);
        color = setColorAlphaVal(color, alpha);
        if (width == 0)
            width = panelWidth;

        if (align.equalsIgnoreCase("center"))
        {
            if (xPos > 0)
                width = panelWidth - 2 * xPos;
            else
                xPos = (panelWidth - width) / 2;
        }
        if (yPos == 0)
            yPos = yBase;
        yPos += yOffset;

        content = content.trim(); // strip leading and trailing white space including CRLF, even though internal spaces and CRLF are retained.
        content = content.replace("\t", " ");    // replace tabs with spaces to match iPhone (Android treats as > 1 space). (see 4Laws/en/11.xml)

        TextView tv = new TextView(mContext);
        tv.setLayoutParams(new SnuffyLayoutParams(
                width,
                height == 0 ? LayoutParams.WRAP_CONTENT : height,
                xPos, yPos));
        tv.setText(content);
        tv.setGravity(getGravityFromAlign(align) + Gravity.TOP);
        tv.setTypeface(null, getTypefaceFromModifier(modifier));
        tv.setTextColor(color);
        tv.setTextSize(getScaledTextSize(DEFAULT_TEXT_SIZE * size / 100.0f));

        if (xPos == 0)
        {
            int marginLeft = getScaledXValue(PANEL_TEXT_LEFT_MARGIN) + xOffset;
            int marginRight = getScaledXValue(PANEL_TEXT_RIGHT_MARGIN);
            tv.setPadding(marginLeft, 0, marginRight, 0);
        }
        theContainer.addView(new SnuffyAlternateTypefaceTextView(tv)
                        .setAlternateTypeface(mAlternateTypeface)
                        .get()
        );

        if (height == 0)
        {
            tv.measure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.UNSPECIFIED);
            height = tv.getMeasuredHeight();
        }
        mYOffsetInPanel = yPos + height;
        if (mYOffsetInPanel > mYOffsetMaxInPanel)
            mYOffsetMaxInPanel = mYOffsetInPanel;
    }

    private void processQuestion(SnuffyPage currPage, Element elQuestion)
    {
        Vector<Element> vTexts = getChildrenWithName(elQuestion, "text");
        int numTexts = vTexts.size();
        if (numTexts > 0)
        {
            // create a container layout to hold the question texts
            SnuffyLayout questionContainer = new SnuffyLayout(mContext);

            int yBase = getIntegerAttributeValue(elQuestion, "y", 0);
            yBase = getScaledYValue(yBase);

            mYOffsetInQuestion = 0;
            for (int i = 0; i < numTexts; i++)
            {
                Element elText = vTexts.elementAt(i);
                processQuestionText(elText, questionContainer);
            }
            // place at bottom of page with a margin below
            int yOffset = yBase;
            if (yOffset == 0)
            {
                yOffset = mPageHeight - mYOffsetInQuestion - getScaledYValue(QUESTION_MARGIN_BOTTOM);
            }
            SnuffyLayoutParams lp = new SnuffyLayoutParams(mPageWidth, mYOffsetInQuestion, 0, yOffset); // TODO: Why is mPageWidth 317?

            questionContainer.setLayoutParams(lp);
            mYFooterTop = lp.y;
            currPage.addView(questionContainer);

        }
        else
        {
            // The question defines a single text in its content
            String content = getTextContentImmediate(elQuestion);
            if (content.length() > 0)
            {
                content = content.trim(); // eliminate trailing newlines, e.g. 4Laws/en/02.xml
                String mode = getStringAttributeValue(elQuestion, "mode", "");
                boolean bStraightMode = mode.equalsIgnoreCase("straight");
                //int h			= getIntegerAttributeValue(elQuestion, "h"      , 0);
                int xPos = getIntegerAttributeValue(elQuestion, "x", 0);
                int yPos = getIntegerAttributeValue(elQuestion, "y", 0);
                int width = getIntegerAttributeValue(elQuestion, "w", REFERENCE_DEVICE_WIDTH);
                int size = getIntegerAttributeValue(elQuestion, "size", 100);
                String align = getStringAttributeValue(elQuestion, "textalign", "right");
                String modifier = getStringAttributeValue(elQuestion, "modifier", bStraightMode ? "bold" : "bold-italics");
                float alpha = getFloatAttributeValue(elQuestion, "alpha", 1.0f);
                int color = getColorAttributeValue(elQuestion, "color", bStraightMode ? mBackgroundColor : Color.WHITE);

                //int yOffset		= getIntegerAttributeValue(elQuestion, "yoffset", 0);

                xPos = getScaledXValue(xPos);
                yPos = getScaledYValue(yPos);
                width = getScaledXValue(width);
                color = setColorAlphaVal(color, alpha);
                if (align.equalsIgnoreCase("center"))
                {
                    xPos = 0;
                    width = mPageWidth;
                }

                TextView tv = new TextView(mContext);
                if (mode.equalsIgnoreCase("straight"))
                {
                    tv.setBackgroundColor(Color.WHITE);
                    width = mPageWidth;
                    align = "center";
                    xPos = 0;
                }

                tv.setLayoutParams(new SnuffyLayoutParams(
                        width == 0 ? LayoutParams.WRAP_CONTENT : width,
                        LayoutParams.WRAP_CONTENT,
                        xPos, yPos));
                tv.setText(content);
                tv.setGravity(getGravityFromAlign(align) + Gravity.TOP);
                if (mAlternateTypeface != null)
                {
                    tv.setTypeface(mAlternateTypeface);
                }
                else
                {
                    tv.setTypeface(null, getTypefaceFromModifier(modifier));
                }
                tv.setTextColor(color);
                tv.setTextSize(getScaledTextSize((bStraightMode ? DEFAULT_STRAIGHT_QUESTION_TEXT_SIZE : DEFAULT_QUESTION_TEXT_SIZE) * size / 100.0f));
                if (xPos == 0)
                {
                    int marginX = getScaledXValue(TEXT_MARGINX);
                    tv.setPadding(marginX, 0, marginX, 0); // margin on left and right
                }

                if (yPos == 0)
                {
                    // place at bottom of page with a margin below
                    SnuffyLayoutParams lp = (SnuffyLayoutParams) tv.getLayoutParams();
                    tv.measure(
                            MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY),
                            MeasureSpec.UNSPECIFIED);
                    lp = (SnuffyLayoutParams) tv.getLayoutParams();
                    int mh = tv.getMeasuredHeight();
                    lp.y = mPageHeight - mh - getScaledYValue(QUESTION_MARGIN_BOTTOM);
                    // lp.y -= yOffset; iPhone version seems not to do this?
                    tv.setLayoutParams(lp);
                    mYFooterTop = lp.y;
                }
                currPage.addView(tv);
            }
        }
    }

    private void processQuestionText(Element elText, SnuffyLayout container)
    {
        String content = elText.getTextContent();
        int xPos = getIntegerAttributeValue(elText, "x", 0);
        int yPos = getIntegerAttributeValue(elText, "y", 0);
        int width = getIntegerAttributeValue(elText, "w", REFERENCE_DEVICE_WIDTH);
        int size = getIntegerAttributeValue(elText, "size", 100);
        String align = getStringAttributeValue(elText, "textalign", "right");
        String modifier = getStringAttributeValue(elText, "modifier", "bold-italics");
        float alpha = getFloatAttributeValue(elText, "alpha", 1.0f);
        int color = getColorAttributeValue(elText, "color", Color.WHITE);

        xPos = getScaledXValue(xPos);
        color = setColorAlphaVal(color, alpha);
        if (align.equalsIgnoreCase("center"))
        {
            xPos = 0;
            width = mPageWidth;
        }
        if (yPos == 0)
            yPos = mYOffsetInQuestion;

        TextView tv = new TextView(mContext);
        tv.setLayoutParams(new SnuffyLayoutParams(
                width == 0 ? LayoutParams.WRAP_CONTENT : width,
                LayoutParams.WRAP_CONTENT,
                xPos, yPos));
        tv.setText(content);
        tv.setGravity(getGravityFromAlign(align) + Gravity.TOP);
        if (mAlternateTypeface != null)
        {
            tv.setTypeface(mAlternateTypeface);
        }
        else
        {
            tv.setTypeface(null, getTypefaceFromModifier(modifier));
        }
        tv.setTextColor(color);
        tv.setTextSize(getScaledTextSize(DEFAULT_QUESTION_TEXT_SIZE * size / 100.0f));
        if (xPos == 0)
        {
            int marginX = getScaledXValue(TEXT_MARGINX);
            marginX = getScaledXValue(marginX);
            tv.setPadding(marginX, 0, marginX, 0); // margin on left and right
        }

        // Determine height required for all texts in this question so caller can set yFooterTop
        SnuffyLayoutParams lp = (SnuffyLayoutParams) tv.getLayoutParams();
        tv.measure(
                MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY),
                MeasureSpec.UNSPECIFIED);
        mYOffsetInQuestion = yPos + tv.getMeasuredHeight();


        container.addView(tv);
    }

    private void processText(SnuffyPage currPage, Element elText)
    {

        String content = elText.getTextContent(); // note that CRLF in textContent is significant
        int xPos = getIntegerAttributeValue(elText, "x", 0);
        int yPos = getIntegerAttributeValue(elText, "y", 0);
        int xOffset = getIntegerAttributeValue(elText, "xoffset", 0);
        int yOffset = getIntegerAttributeValue(elText, "yoffset", 0);
        int width = getIntegerAttributeValue(elText, "w", REFERENCE_DEVICE_WIDTH);
        int size = getIntegerAttributeValue(elText, "size", 100);
        String align = getStringAttributeValue(elText, "textalign", "left");
        String modifier = getStringAttributeValue(elText, "modifier", "");
        float alpha = getFloatAttributeValue(elText, "alpha", 1.0f);
        int color = getColorAttributeValue(elText, "color", Color.WHITE);

        xPos = getScaledXValue(xPos);
        yPos = getScaledYValue(yPos);
        xOffset = getScaledXValue(xOffset);
        yOffset = getScaledYValue(yOffset);
        width = getScaledXValue(width);
        color = setColorAlphaVal(color, alpha);

        if (elText.getAttribute("x").length() == 0)
            xPos = getScaledXValue(TEXT_MARGINX);

        if (align.equalsIgnoreCase("center"))
            width = mPageWidth - 2 * xPos;
        else if (elText.getAttribute("w").length() == 0)
            width = mPageWidth - 2 * xPos;
        xPos += xOffset;

        if (yPos == 0)
        {
            yPos = mYOffset + mYOffsetPerItem;
            yPos += yOffset;
            mNumOffsetItems++;
        }

        TextView tv = new TextView(mContext);
        tv.setLayoutParams(new SnuffyLayoutParams(
                width == 0 ? LayoutParams.WRAP_CONTENT : width,
                LayoutParams.WRAP_CONTENT,
                xPos, yPos));
        tv.setText(content);
        tv.setGravity(getGravityFromAlign(align));
        if (mAlternateTypeface != null)
        {
            tv.setTypeface(mAlternateTypeface);
        }
        else
        {
            tv.setTypeface(null, getTypefaceFromModifier(modifier));
        }
        tv.setTextColor(color);
        tv.setTextSize(getScaledTextSize(DEFAULT_TEXT_SIZE * size / 100.0f));
        currPage.addView(tv);

        SnuffyLayoutParams lp = (SnuffyLayoutParams) tv.getLayoutParams();
        tv.measure(
                MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY),
                MeasureSpec.UNSPECIFIED);
        mYOffset = yPos + tv.getMeasuredHeight();
    }

    private void processTitle(SnuffyPage currPage, Element elTitle)
    {
        String titleMode = getStringAttributeValue(elTitle, "mode", "");    // plain/clear/straight/peek (default is plain)
        int titleHeight = getIntegerAttributeValue(elTitle, "h", 0);
        boolean bPeekMode = titleMode.equalsIgnoreCase("peek");
        boolean bStraightMode = titleMode.equalsIgnoreCase("straight");
        boolean bClearMode = titleMode.equalsIgnoreCase("clear");
        boolean bPlainMode = titleMode.equalsIgnoreCase("plain") || titleMode.equalsIgnoreCase("");

        // In peek mode there is white background, heading is on left, subheading is on right and peek panel drops down below (0 margin at top), round corner at BR
        // In plain mode (used in KGP and about.xml) there is white background, flush with left margin, 20 pixels from top, round corners at TR, BR
        // In clear mode there is no background and the text is always positioned (but with 20 pixel margin at top)
        // In straight mode there is white background, full width  (but with 20 pixel margin at top)      

        Element elNumber = getChildElementNamed(elTitle, "number");
        Element elHeading = getChildElementNamed(elTitle, "heading");
        Element elSubHeading = getChildElementNamed(elTitle, "subheading");
        Element elPeekPanel = getChildElementNamed(elTitle, "peekpanel");

        boolean bHasPeekPanel = (elPeekPanel != null);         // we only expect <peekpanel> with bPlainMode or bPeekMode

        TextView tvNumber = null;
        TextView tvHeading = null;
        TextView tvSubHeading = null;
        TextView tvSubTitle = null;
        View vLine = null;

        if (elNumber != null) tvNumber = createTitleNumberFromElement(elNumber);
        if (elHeading != null) tvHeading = createTitleHeadingFromElement(elHeading, titleMode);
        if (elSubHeading != null)
            tvSubHeading = createTitleSubHeadingFromElement(elSubHeading, titleMode);
        if (elPeekPanel != null) tvSubTitle = createSubTitleFromElement(elPeekPanel);

        final SnuffyLayout titleContainer = new SnuffyLayout(mContext);
        titleContainer.setTag(560);

        final SnuffyLayout titleClippingContainer = new SnuffyLayout(mContext);
        // contains titleContainer, subtitleContainer and their shadows
        titleClippingContainer.setBackgroundColor(Color.TRANSPARENT);

        final int TITLE_FRAME_XMARGIN = 20;    // on right only in peek mode
        final int TITLE_FRAME_YMARGIN = 20;    // on top only if not in peek mode
        final int SUBTITLE_FRAME_MARGIN = 30;    // on right of peekpanel
        final int HEADING_BOTTOM_PADDING = 5;
        final int SUBTITLE_BOTTOM_PADDING = 5;
        final int PEEK_HEADING_XSEPARATION = 20;
        final int PEEK_VLINE_YMARGIN = 5;
        final int SUBTITLE_PEEK_OFFSET = 10;    // this amount of the subtitle (aka peek panel) hangs out from underneath the title

        int titleMarginX = getScaledXValue(TITLE_FRAME_XMARGIN);
        int titleMarginY = getScaledYValue(bPeekMode ? 0 : TITLE_FRAME_YMARGIN);
        int subTitleMarginX = getScaledXValue(SUBTITLE_FRAME_MARGIN);
        int subTitlePeekOffset = getScaledYValue(SUBTITLE_PEEK_OFFSET);
        int titleWidth;
        int subTitleWidth;
        if (bClearMode)
        {
            titleContainer.setBackgroundColor(Color.TRANSPARENT);
            titleWidth = mPageWidth - titleMarginX; // TODO: Why not 2x margin (left and right?)
        }
        else if (bStraightMode)
        {
            titleContainer.setBackgroundColor(Color.WHITE);
            titleWidth = mPageWidth;
        }
        else if (bPeekMode)
        {
            titleContainer.setBackgroundResource(R.drawable.round_botright_box);
            titleWidth = mPageWidth - titleMarginX;
        }
        else
        { // bPlainMode
            titleContainer.setBackgroundResource(R.drawable.round_right_box);
            titleWidth = mPageWidth - titleMarginX;
        }

        if ((bPlainMode || bPeekMode) && (tvHeading != null) && (tvSubHeading != null))
        {
            // layout title container with heading on left, subheading on right and line between them
            // Note: IOS version ONLY does this if bPeekMode - why
            adjustHeadingFont(tvHeading);
            SnuffyLayoutParams lpHeading = (SnuffyLayoutParams) tvHeading.getLayoutParams();
            SnuffyLayoutParams lpSubHeading = (SnuffyLayoutParams) tvSubHeading.getLayoutParams();
            int newX = lpHeading.x + lpHeading.width + PEEK_HEADING_XSEPARATION;
            lpSubHeading.width -= (newX - lpSubHeading.x);
            lpSubHeading.x += (newX - lpSubHeading.x);
            tvSubHeading.setLayoutParams(lpSubHeading);

            tvHeading.measure(
                    MeasureSpec.makeMeasureSpec(lpHeading.width, MeasureSpec.EXACTLY),
                    MeasureSpec.UNSPECIFIED);
            lpHeading = (SnuffyLayoutParams) tvHeading.getLayoutParams();
            tvSubHeading.measure(
                    MeasureSpec.makeMeasureSpec(lpSubHeading.width, MeasureSpec.EXACTLY),
                    MeasureSpec.UNSPECIFIED);
            lpSubHeading = (SnuffyLayoutParams) tvSubHeading.getLayoutParams();

            int xPosVLine = lpSubHeading.x - (PEEK_HEADING_XSEPARATION / 2);
            int y2PosVLine = Math.max(lpHeading.y + tvHeading.getMeasuredHeight(), lpSubHeading.y + tvSubHeading.getMeasuredHeight()) - (2 * PEEK_VLINE_YMARGIN) + HEADING_BOTTOM_PADDING;
            vLine = getVRView(Color.BLACK, xPosVLine, PEEK_VLINE_YMARGIN, y2PosVLine, 1);
        }

        titleHeight = getScaledYValue(titleHeight);
        if (titleHeight == 0)
        {
            // get it from the 3 children and their children
            if (tvNumber != null)
            {
                SnuffyLayoutParams lp = (SnuffyLayoutParams) tvNumber.getLayoutParams();
                tvNumber.measure(
                        MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY),
                        MeasureSpec.UNSPECIFIED);
                int h = lp.y + tvNumber.getMeasuredHeight();
                if (h > titleHeight)
                    titleHeight = h;
            }
            if (tvHeading != null)
            {
                SnuffyLayoutParams lp = (SnuffyLayoutParams) tvHeading.getLayoutParams();
                tvHeading.measure(
                        MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY),
                        MeasureSpec.UNSPECIFIED);
                int h = lp.y + tvHeading.getMeasuredHeight();
                if (h > titleHeight)
                    titleHeight = h;
            }
            if (tvSubHeading != null)
            {
                SnuffyLayoutParams lp = (SnuffyLayoutParams) tvSubHeading.getLayoutParams();
                tvSubHeading.measure(
                        MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY),
                        MeasureSpec.UNSPECIFIED);
                int h = lp.y + tvSubHeading.getMeasuredHeight();
                if (h > titleHeight)
                    titleHeight = h;
            }
            titleHeight += HEADING_BOTTOM_PADDING;
        }

        titleContainer.setLayoutParams(new SnuffyLayoutParams(
                titleWidth, titleHeight,
                0, 0));

        titleClippingContainer.addView(titleContainer);

        if (elTitle.getAttribute("h").length() > 0)
        {
            // center heading vertically

            // h specified. assume there is a heading and only a heading.
            assert tvHeading != null;
            SnuffyLayoutParams lp = (SnuffyLayoutParams) tvHeading.getLayoutParams();
            tvHeading.measure(
                    MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY),
                    MeasureSpec.UNSPECIFIED);
            lp = (SnuffyLayoutParams) tvHeading.getLayoutParams();
            lp.y = (int) (0.5 * (titleHeight - tvHeading.getMeasuredHeight()));
            tvHeading.setLayoutParams(lp);
        }
        else if ((bPlainMode || bPeekMode) && (tvHeading != null) && (tvSubHeading != null))
        {
            // center heading and subheading vertically
            SnuffyLayoutParams lp = (SnuffyLayoutParams) tvHeading.getLayoutParams();
            tvHeading.measure(
                    MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY),
                    MeasureSpec.UNSPECIFIED);
            lp = (SnuffyLayoutParams) tvHeading.getLayoutParams();
            lp.y = (int) (0.5 * (titleHeight - tvHeading.getMeasuredHeight()));
            tvHeading.setLayoutParams(lp);

            lp = (SnuffyLayoutParams) tvSubHeading.getLayoutParams();
            tvSubHeading.measure(
                    MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY),
                    MeasureSpec.UNSPECIFIED);
            lp = (SnuffyLayoutParams) tvSubHeading.getLayoutParams();
            lp.y = (int) (0.5 * (titleHeight - tvSubHeading.getMeasuredHeight()));
            tvSubHeading.setLayoutParams(lp);
        }

        titleContainer.setPadding(0, 0, 0, 0);


        if (tvNumber != null) titleContainer.addView(tvNumber);
        if (tvHeading != null) titleContainer.addView(tvHeading);
        if (tvSubHeading != null) titleContainer.addView(tvSubHeading);
        if (vLine != null) titleContainer.addView(vLine);

        // add shadows for titleContainer to the page (Tags 56x)
        final int DROPSHADOW_INSETX = getScaledXValue(10); // inset from image size 30
        final int DROPSHADOW_INSETY = getScaledYValue(10); // inset from image size 30
        final int DROPSHADOW_INSETTOP = getScaledYValue(20); // inset from image size 40
        final int DROPSHADOW_INSETBOT = getScaledYValue(20); // inset from image size 40
        final int DROPSHADOW_LENGTHX = getScaledXValue(30);
        final int DROPSHADOW_LENGTHY = getScaledYValue(30);
        final int DROPSHADOW_SUBLENGTHX = getScaledXValue(20);
        final int DROPSHADOW_SUBLENGTHY = getScaledYValue(20);

        SnuffyLayoutParams lpTitleContainer = (SnuffyLayoutParams) titleContainer.getLayoutParams();
        if (bStraightMode)
        {
            Bitmap bmTop = getBitmapFromAssetOrFile(mContext, "grad_shad_bot.png");
            if (bmTop != null)
            {
                ImageView iv = new ImageView(mContext);
                iv.setTag(561);
                iv.setLayoutParams(new SnuffyLayoutParams(lpTitleContainer.width, getScaledYValue(bmTop.getHeight()),
                        lpTitleContainer.x, lpTitleContainer.y - bmTop.getHeight() + getScaledYValue(DROPSHADOW_INSETTOP)));
                iv.setImageBitmap(bmTop);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                titleClippingContainer.addView(iv);
            }
            Bitmap bmBot = getBitmapFromAssetOrFile(mContext, "grad_shad_top.png");
            if (bmBot != null)
            {
                ImageView iv = new ImageView(mContext);
                iv.setTag(562);
                iv.setLayoutParams(new SnuffyLayoutParams(lpTitleContainer.width, getScaledYValue(bmBot.getHeight()),
                        lpTitleContainer.x, lpTitleContainer.y + lpTitleContainer.height - DROPSHADOW_INSETBOT));
                iv.setImageBitmap(bmBot);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                titleClippingContainer.addView(iv);
            }
        }
        else if (bPlainMode)
        {
            Bitmap bmNE = getBitmapFromAssetOrFile(mContext, "grad_shad_NE.png");
            if (bmNE != null)
            {
                // grad_shad_NE_title:...
                ImageView iv = new ImageView(mContext);
                iv.setTag(561);
                iv.setLayoutParams(new SnuffyLayoutParams(
                        getScaledXValue(bmNE.getWidth()),
                        getScaledYValue(bmNE.getHeight()),
                        lpTitleContainer.x + lpTitleContainer.width - DROPSHADOW_INSETX,
                        lpTitleContainer.y
                ));
                iv.setImageBitmap(bmNE);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                titleClippingContainer.addView(iv);
            }
            Bitmap bmE = getBitmapFromAssetOrFile(mContext, "grad_shad_E.png");
            if (bmE != null)
            {
                // grad_shad_E_title:...
                ImageView iv = new ImageView(mContext);
                iv.setTag(566);
                assert bmNE != null;
                iv.setLayoutParams(new SnuffyLayoutParams(
                        getScaledXValue(bmE.getWidth()),
                        lpTitleContainer.height - getScaledYValue(bmNE.getHeight()) - DROPSHADOW_INSETY,
                        lpTitleContainer.x + lpTitleContainer.width - DROPSHADOW_INSETX,
                        lpTitleContainer.y + getScaledYValue(bmNE.getHeight())
                ));
                iv.setImageBitmap(bmE);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                titleClippingContainer.addView(iv);
            }
            Bitmap bmSE = getBitmapFromAssetOrFile(mContext, "grad_shad_SE.png");
            if (bmSE != null)
            {
                // grad_shad_SE_title:...
                ImageView iv = new ImageView(mContext);
                iv.setTag(563);
                iv.setLayoutParams(new SnuffyLayoutParams(
                        getScaledXValue(bmSE.getWidth()),
                        getScaledYValue(bmSE.getHeight()),
                        lpTitleContainer.x + lpTitleContainer.width - DROPSHADOW_INSETX,
                        lpTitleContainer.y + lpTitleContainer.height - DROPSHADOW_INSETY
                ));
                iv.setImageBitmap(bmSE);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                titleClippingContainer.addView(iv);
            }
            Bitmap bmS = getBitmapFromAssetOrFile(mContext, "grad_shad_S.png");
            if (bmS != null)
            {
                // grad_shad_S_title:...
                ImageView iv = new ImageView(mContext);
                iv.setTag(562);
                iv.setLayoutParams(new SnuffyLayoutParams(
                        lpTitleContainer.width - DROPSHADOW_INSETX,
                        getScaledYValue(bmS.getHeight()),
                        lpTitleContainer.x,
                        lpTitleContainer.y + lpTitleContainer.height - DROPSHADOW_INSETY
                ));
                iv.setImageBitmap(bmS);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                titleClippingContainer.addView(iv);
            }
        }
        else if (bPeekMode)
        {
            Bitmap bmE = getBitmapFromAssetOrFile(mContext, "grad_shad_E.png");
            if (bmE != null)
            {
                // grad_shad_E_title:...
                ImageView iv = new ImageView(mContext);
                iv.setTag(566);
                iv.setLayoutParams(new SnuffyLayoutParams(
                        DROPSHADOW_LENGTHX,
                        lpTitleContainer.height - DROPSHADOW_INSETY,
                        lpTitleContainer.width - DROPSHADOW_INSETX,
                        lpTitleContainer.y));
                iv.setImageBitmap(bmE);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                titleClippingContainer.addView(iv);
            }
            Bitmap bmS = getBitmapFromAssetOrFile(mContext, "grad_shad_S.png");
            if (bmS != null)
            {
                // grad_shad_S_title:...
                ImageView iv = new ImageView(mContext);
                iv.setTag(562);
                iv.setLayoutParams(new SnuffyLayoutParams(
                        lpTitleContainer.width - DROPSHADOW_INSETX,
                        DROPSHADOW_LENGTHY,
                        lpTitleContainer.x,
                        lpTitleContainer.y + lpTitleContainer.height - DROPSHADOW_INSETY));
                iv.setImageBitmap(bmS);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                titleClippingContainer.addView(iv);
            }
            Bitmap bmSE = getBitmapFromAssetOrFile(mContext, "grad_shad_SE.png");
            if (bmSE != null)
            {
                // grad_shad_SE_title:...
                ImageView iv = new ImageView(mContext);
                iv.setTag(563);
                iv.setLayoutParams(new SnuffyLayoutParams(
                        DROPSHADOW_LENGTHX,
                        DROPSHADOW_LENGTHY,
                        lpTitleContainer.x + lpTitleContainer.width - DROPSHADOW_INSETX,
                        lpTitleContainer.y + lpTitleContainer.height - DROPSHADOW_INSETY));
                iv.setImageBitmap(bmSE);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                titleClippingContainer.addView(iv);
            }
        }
        titleContainer.bringToFront(); // so it is in front of the shadows

        titleClippingContainer.setLayoutParams(new SnuffyLayoutParams(
                mPageWidth, mPageHeight - titleMarginY,
                0, titleMarginY));

        currPage.addView(titleClippingContainer);

        // create and fill subtitle container and hook up to click on title
        if (bHasPeekPanel)
        { //i.e. (tvSubTitle != null)

            final SnuffyLayout subTitleContainer = new SnuffyLayout(mContext);
            subTitleContainer.setTag(550);

            // layout subtitle container with textview and arrow
            subTitleContainer.setBackgroundResource(R.drawable.round_botright_box);
            subTitleWidth = mPageWidth - subTitleMarginX;

            subTitleContainer.setPadding(0, 0, 0, 0);
            if (tvSubTitle != null) subTitleContainer.addView(tvSubTitle);

            // Add "arrow" - actually appears as a dash in IOS version (probably because the Unicode downarrow char = U+25BC ) is not found in many fonts)
            // and therefore a dash (a "Macron" = U+00AF) is what we use here
            TextView tvArrow = new TextView(mContext);
            tvArrow.setText("¯");
            tvArrow.setGravity(Gravity.CENTER_HORIZONTAL);
            tvArrow.setTextColor(Color.BLACK);
            tvArrow.setBackgroundColor(Color.TRANSPARENT);
            tvArrow.setTextSize(10);
            tvArrow.setPadding(0, 3, 0, 0);
            subTitleContainer.addView(tvArrow);

            assert tvSubTitle != null;
            SnuffyLayoutParams lp = (SnuffyLayoutParams) tvSubTitle.getLayoutParams();
            tvSubTitle.measure(
                    MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY),
                    MeasureSpec.UNSPECIFIED);
            lp = (SnuffyLayoutParams) tvSubTitle.getLayoutParams();
            tvArrow.setLayoutParams(new SnuffyLayoutParams(
                    LayoutParams.MATCH_PARENT,
                    getScaledYValue(SUBTITLE_PEEK_OFFSET),
                    0, lp.y + tvSubTitle.getMeasuredHeight() + getScaledYValue(SUBTITLE_BOTTOM_PADDING)));

            int subTitleHeight = lp.y + tvSubTitle.getMeasuredHeight() + getScaledYValue(SUBTITLE_BOTTOM_PADDING) + getScaledYValue(SUBTITLE_PEEK_OFFSET);
            final int yBefore = titleHeight - subTitleHeight + subTitlePeekOffset;
            final int yAfter = titleHeight - getScaledYValue(20); // adjusted since there is too much white space at top
            final SnuffyLayoutParams lpBefore = new SnuffyLayoutParams(subTitleWidth, subTitleHeight, 0, yBefore);
            final SnuffyLayoutParams lpAfter = new SnuffyLayoutParams(subTitleWidth, subTitleHeight, 0, yAfter);

            subTitleContainer.setLayoutParams(lpBefore);

            titleClippingContainer.addView(subTitleContainer);

            SnuffyLayoutParams lpSubTitleContainer = new SnuffyLayoutParams(subTitleWidth, subTitleHeight, 0, yBefore);
            // grad_shad_E_subtitle:...
            SnuffyLayoutParams lpTemp = new SnuffyLayoutParams(
                    DROPSHADOW_SUBLENGTHX,
                    lpSubTitleContainer.height - DROPSHADOW_INSETY,
                    lpSubTitleContainer.width - DROPSHADOW_INSETX,
                    yBefore);
            Bitmap bmE = getBitmapFromAssetOrFile(mContext, "grad_shad_E.png");
            if (bmE != null)
            {
                ImageView iv = new ImageView(mContext);
                iv.setTag(556);
                iv.setLayoutParams(lpTemp);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                iv.setImageBitmap(bmE);
                titleClippingContainer.addView(iv);
            }
            final SnuffyLayoutParams lpBeforeE = new SnuffyLayoutParams(lpTemp.width, lpTemp.height, lpTemp.x, yBefore);
            final SnuffyLayoutParams lpAfterE = new SnuffyLayoutParams(lpTemp.width, lpTemp.height, lpTemp.x, yAfter);

            // grad_shad_S_subtitle:...
            final int yBeforeS = yBefore + lpSubTitleContainer.height - DROPSHADOW_INSETY;
            final int yAfterS = yAfter + lpSubTitleContainer.height - DROPSHADOW_INSETY;
            lpTemp = new SnuffyLayoutParams(
                    lpSubTitleContainer.width - DROPSHADOW_INSETX,
                    DROPSHADOW_SUBLENGTHY,
                    lpSubTitleContainer.x,
                    yBeforeS);
            Bitmap bmS = getBitmapFromAssetOrFile(mContext, "grad_shad_S.png");
            if (bmS != null)
            {
                ImageView iv = new ImageView(mContext);
                iv.setTag(552);
                iv.setLayoutParams(lpTemp);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                iv.setImageBitmap(bmS);
                titleClippingContainer.addView(iv);
            }
            final SnuffyLayoutParams lpBeforeS = new SnuffyLayoutParams(lpTemp.width, lpTemp.height, lpTemp.x, yBeforeS);
            final SnuffyLayoutParams lpAfterS = new SnuffyLayoutParams(lpTemp.width, lpTemp.height, lpTemp.x, yAfterS);

            // grad_shad_SE_subtitle:...
            final int yBeforeSE = yBefore + lpSubTitleContainer.height - DROPSHADOW_INSETY;
            final int yAfterSE = yAfter + lpSubTitleContainer.height - DROPSHADOW_INSETY;
            lpTemp = new SnuffyLayoutParams(
                    DROPSHADOW_SUBLENGTHX,
                    DROPSHADOW_SUBLENGTHY,
                    lpSubTitleContainer.x + lpSubTitleContainer.width - DROPSHADOW_INSETX,
                    yBeforeSE);
            Bitmap bmSE = getBitmapFromAssetOrFile(mContext, "grad_shad_SE.png");
            if (bmSE != null)
            {
                ImageView iv = new ImageView(mContext);
                iv.setTag(553);
                iv.setLayoutParams(lpTemp);
                iv.setImageBitmap(bmSE);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                titleClippingContainer.addView(iv);
            }
            final SnuffyLayoutParams lpBeforeSE = new SnuffyLayoutParams(lpTemp.width, lpTemp.height, lpTemp.x, yBeforeSE);
            final SnuffyLayoutParams lpAfterSE = new SnuffyLayoutParams(lpTemp.width, lpTemp.height, lpTemp.x, yAfterSE);

            orderTitleViews(titleClippingContainer);
            final SnuffyPage thePage = currPage;
            titleContainer.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    // drop the subtitle panel down from orig posn until its top is flush with bottom of title.

                    final Runnable animIn2 = new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            subTitleContainer.setLayoutParams(lpBefore);
                            setLayoutParamsOfViewWithTag(titleClippingContainer, 556, lpBeforeE);
                            setLayoutParamsOfViewWithTag(titleClippingContainer, 552, lpBeforeS);
                            setLayoutParamsOfViewWithTag(titleClippingContainer, 553, lpBeforeSE);
                            thePage.requestLayout();
                            thePage.forceLayout();
                            thePage.invalidate();
                        }
                    };

                    final Runnable animIn1 = new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            final int THIS_ANIMATION_DURATION = 400;

                            TranslateAnimation translateAnimation = new TranslateAnimation(
                                    Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                                    Animation.ABSOLUTE, 0, Animation.ABSOLUTE, yBefore - yAfter);
                            //translateAnimation.setFillAfter(true); // eliminates flicker but results in next layout being mispositioned.
                            translateAnimation.setDuration(THIS_ANIMATION_DURATION);
                            AnimationListener listener = new SimpleAnimationListener(animIn2);
                            translateAnimation.setAnimationListener(listener);
                            //translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator()); // isn't this the default anyway?
                            subTitleContainer.startAnimation(translateAnimation);

                            TranslateAnimation taE = new TranslateAnimation(
                                    Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                                    Animation.ABSOLUTE, 0, Animation.ABSOLUTE, yBefore - yAfter);
                            taE.setDuration(THIS_ANIMATION_DURATION);
                            startAnimationOnViewWithTag(titleClippingContainer, 556, taE);

                            TranslateAnimation taS = new TranslateAnimation(
                                    Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                                    Animation.ABSOLUTE, 0, Animation.ABSOLUTE, yBeforeS - yAfterS);
                            taS.setDuration(THIS_ANIMATION_DURATION);
                            startAnimationOnViewWithTag(titleClippingContainer, 552, taS);
                            TranslateAnimation taSE = new TranslateAnimation(
                                    Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                                    Animation.ABSOLUTE, 0, Animation.ABSOLUTE, yBeforeSE - yAfterSE);
                            taSE.setDuration(THIS_ANIMATION_DURATION);
                            startAnimationOnViewWithTag(titleClippingContainer, 553, taSE);
                        }
                    };

                    final Runnable animOut2 = new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            subTitleContainer.setLayoutParams(lpAfter);
                            setLayoutParamsOfViewWithTag(titleClippingContainer, 556, lpAfterE);
                            setLayoutParamsOfViewWithTag(titleClippingContainer, 552, lpAfterS);
                            setLayoutParamsOfViewWithTag(titleClippingContainer, 553, lpAfterSE);
                            //thePage.requestLayout();
                            //thePage.forceLayout();
                            //thePage.invalidate();
                        }
                    };

                    final Runnable animOut1 = new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            final int THIS_ANIMATION_DURATION = 500;

                            subTitleContainer.setLayoutParams(lpBefore);
                            setLayoutParamsOfViewWithTag(titleClippingContainer, 556, lpBeforeE);
                            setLayoutParamsOfViewWithTag(titleClippingContainer, 552, lpBeforeS);
                            setLayoutParamsOfViewWithTag(titleClippingContainer, 553, lpBeforeSE);
                            orderTitleViews(titleClippingContainer);
                            thePage.requestLayout();
                            thePage.forceLayout();
                            thePage.invalidate();
                            thePage.showCover(animIn1, true);

                            TranslateAnimation translateAnimation = new TranslateAnimation(
                                    Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                                    Animation.ABSOLUTE, 0, Animation.ABSOLUTE, yAfter - yBefore);
                            //translateAnimation.setFillAfter(true); // eliminates flicker but results in next layout being mispositioned.
                            translateAnimation.setDuration(THIS_ANIMATION_DURATION);
                            AnimationListener listener = new SimpleAnimationListener(animOut2);
                            translateAnimation.setAnimationListener(listener);
                            //translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator()); // isn't this the default anyway?
                            subTitleContainer.startAnimation(translateAnimation);


                            TranslateAnimation taE = new TranslateAnimation(
                                    Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                                    Animation.ABSOLUTE, 0, Animation.ABSOLUTE, yAfter - yBefore);
                            taE.setDuration(THIS_ANIMATION_DURATION);
                            startAnimationOnViewWithTag(titleClippingContainer, 556, taE);

                            TranslateAnimation taS = new TranslateAnimation(
                                    Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                                    Animation.ABSOLUTE, 0, Animation.ABSOLUTE, yAfterS - yBeforeS);
                            taS.setDuration(THIS_ANIMATION_DURATION);
                            startAnimationOnViewWithTag(titleClippingContainer, 552, taS);
                            TranslateAnimation taSE = new TranslateAnimation(
                                    Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0,
                                    Animation.ABSOLUTE, 0, Animation.ABSOLUTE, yAfterSE - yBeforeSE);
                            taSE.setDuration(THIS_ANIMATION_DURATION);
                            startAnimationOnViewWithTag(titleClippingContainer, 553, taSE);

                        }
                    };

                    new Handler().post(animOut1);


                }
            });
        }

        mYOffset += titleMarginY + titleHeight + (bHasPeekPanel ? subTitlePeekOffset : 0);
    }

    private TextView createTitleNumberFromElement(Element el)
    {
        // Note: the x,y positioning here is relative to the "title" frame that encloses them
        //boolean bPeekMode 	 	= (titleMode.equalsIgnoreCase("peek"));
        //boolean bStraightMode 	= (titleMode.equalsIgnoreCase("straight"));
        float textSize = 4 * DEFAULT_TEXT_SIZE;
        String content = el.getTextContent();
        int color = getColorAttributeValue(el, "color", mBackgroundColor);
        float labelAlpha = getFloatAttributeValue(el, "alpha", 1.0f);
        String align = "right";
        int size = getIntegerAttributeValue(el, "size", 100);
        int x = getIntegerAttributeValue(el, "x", 10);
        int y = getIntegerAttributeValue(el, "y", 5);
        int w = getIntegerAttributeValue(el, "w", 40);
        String modifier = getStringAttributeValue(el, "modifier", "");

        textSize = textSize * size / 100.0f;
        color = setColorAlphaVal(color, labelAlpha);
        x = getScaledXValue(x);
        y = getScaledYValue(y);
        w = getScaledXValue(w);

        y = y - (int) (0.35 * textSize); // eliminate the leading TODO: this is not accurate enough - can we get the metrics and do it that way?

        TextView tv = new TextView(mContext);
        tv.setLayoutParams(new SnuffyLayoutParams(
                w == 0 ? LayoutParams.WRAP_CONTENT : w,
                LayoutParams.WRAP_CONTENT,
                x, y));
        tv.setText(content);
        tv.setGravity(getGravityFromAlign(align) + Gravity.TOP);
        tv.setTypeface(null, getTypefaceFromModifier(modifier));
        tv.setTextColor(color);
        tv.setTextSize(getScaledTextSize(textSize));
        tv.setPadding(0, 0, 0, 0);

        return tv;
    }

    private TextView createTitleHeadingFromElement(Element el, String titleMode)
    {
        // Note: the x,y positioning here is relative to the "title" frame that encloses them
        boolean bPeekMode = (titleMode.equalsIgnoreCase("peek"));
        boolean bStraightMode = (titleMode.equalsIgnoreCase("straight"));
        float textSize = bPeekMode ? 30 : DEFAULT_TEXT_SIZE;
        String content = el.getTextContent();
        int color = getColorAttributeValue(el, "color", mBackgroundColor);
        float labelAlpha = getFloatAttributeValue(el, "alpha", 1.0f);
        String alignment = getStringAttributeValue(el, "alignment", bPeekMode ? "right" : "left");            // both supported
        String align = getStringAttributeValue(el, "textalign", alignment);                                // both supported
        int size = getIntegerAttributeValue(el, "size", 100);
        int x = getIntegerAttributeValue(el, "x", bPeekMode ? 5 : 55);
        int y = getIntegerAttributeValue(el, "y", bPeekMode ? 10 : 5);
        int w = getIntegerAttributeValue(el, "w", bPeekMode ? 95 : 240);
        int h = getIntegerAttributeValue(el, "h", bPeekMode ? 100 : 150);
        String modifier = getStringAttributeValue(el, "modifier", bPeekMode ? "" : "bold");

        if (bStraightMode)
        {
            align = "center";
            x = 10;            // IOS version has 0 but that is a bit ugly
            w = REFERENCE_DEVICE_WIDTH - x - x;
            // Strip leading and trailing newline chars from text
            content = content.trim();
        }
        Log.d(TAG, content);
        Log.d(TAG, "x=" + Integer.toString(x));
        Log.d(TAG, "w=" + Integer.toString(w));
        boolean bResize = (!((el.getAttribute("w").length() > 0) && (el.getAttribute("h").length() > 0)));
        textSize = textSize * size / 100.0f;
        color = setColorAlphaVal(color, labelAlpha);
        x = getScaledXValue(x);
        y = getScaledYValue(y);
        w = getScaledXValue(w);
        h = getScaledYValue(h);

        TextView tv = new TextView(mContext);
        tv.setLayoutParams(new SnuffyLayoutParams(
                w == 0 ? LayoutParams.WRAP_CONTENT : w,
                bResize ? LayoutParams.WRAP_CONTENT : h,
                x, y));
        tv.setText(content);
        tv.setGravity(getGravityFromAlign(align));
        tv.setTypeface(null, getTypefaceFromModifier(modifier));
        tv.setTextColor(color);
        tv.setTextSize(getScaledTextSize(textSize));
        tv.setPadding(0, 0, 0, 0);

        return new SnuffyAlternateTypefaceTextView(tv)
                .setAlternateTypeface(mAlternateTypeface)
                .get();
    }

    private TextView createTitleSubHeadingFromElement(Element el, String titleMode)
    {
        // Note: the x,y positioning here is relative to the "title" frame that encloses them
        boolean bPeekMode = (titleMode.equalsIgnoreCase("peek"));
        boolean bStraightMode = (titleMode.equalsIgnoreCase("straight"));
        boolean bPlainMode = (titleMode.equalsIgnoreCase("plain"));
        float textSize = DEFAULT_TEXT_SIZE;
        String content = el.getTextContent();
        int color = getColorAttributeValue(el, "color", mBackgroundColor);
        float labelAlpha = getFloatAttributeValue(el, "alpha", 1.0f);
        String alignment = getStringAttributeValue(el, "alignment", bPeekMode ? "left" : "center");        // both supported
        String align = getStringAttributeValue(el, "textalign", alignment);                            // both supported
        int size = getIntegerAttributeValue(el, "size", 100);
        int x = getIntegerAttributeValue(el, "x", bPeekMode ? 116 : (bPlainMode ? 10 : 0));
        int y = getIntegerAttributeValue(el, "y", bPeekMode ? 0 : (bPlainMode ? 0 : 82));
        int w = getIntegerAttributeValue(el, "w", bPeekMode ? 175 : (bPlainMode ? 290 : 320));
        int h = getIntegerAttributeValue(el, "h", bPeekMode ? 120 : (bPlainMode ? 0 : 23));
        String modifier = getStringAttributeValue(el, "modifier", "");

        if (bStraightMode)
        {
            align = "center";
            x = 0;
            w = REFERENCE_DEVICE_WIDTH;
        }
        boolean bResize = (!((el.getAttribute("w").length() > 0) && (el.getAttribute("h").length() > 0)));
        textSize = textSize * size / 100.0f;
        color = setColorAlphaVal(color, labelAlpha);
        x = getScaledXValue(x);
        y = getScaledYValue(y);
        w = getScaledXValue(w);
        h = getScaledYValue(h);

        TextView tv = new TextView(mContext);
        tv.setLayoutParams(new SnuffyLayoutParams(
                w == 0 ? LayoutParams.WRAP_CONTENT : w,
                bResize ? LayoutParams.WRAP_CONTENT : h,
                x, y));
        tv.setText(content);
        tv.setGravity(getGravityFromAlign(align));
        tv.setTypeface(null, getTypefaceFromModifier(modifier));
        tv.setTextColor(color);
        tv.setTextSize(getScaledTextSize(textSize));
        tv.setPadding(0, 0, 0, 0);

        return new SnuffyAlternateTypefaceTextView(tv)
                .setAlternateTypeface(mAlternateTypeface)
                .get();
    }

    private TextView createSubTitleFromElement(Element el)
    {

        // Note: the x,y positioning here is relative to the subtitle container frame that encloses them
        float textSize = DEFAULT_TEXT_SIZE;
        String content = el.getTextContent();
        int color = getColorAttributeValue(el, "color", mBackgroundColor);
        int bgColor = Color.TRANSPARENT;
        float labelAlpha = getFloatAttributeValue(el, "alpha", 1.0f);
        String alignment = getStringAttributeValue(el, "alignment", "left");    // both supported
        String align = getStringAttributeValue(el, "textalign", alignment);    // both supported
        int size = getIntegerAttributeValue(el, "size", 100);
        int x = getIntegerAttributeValue(el, "x", 10);
        int y = getIntegerAttributeValue(el, "y", 30);
        int w = getIntegerAttributeValue(el, "w", REFERENCE_DEVICE_WIDTH - 40);
        int h = getIntegerAttributeValue(el, "h", 80);
        String modifier = getStringAttributeValue(el, "modifier", "bold-italics");

        boolean bResize = (!((el.getAttribute("w").length() > 0) && (el.getAttribute("h").length() > 0)));
        textSize = textSize * size / 100.0f;
        color = setColorAlphaVal(color, labelAlpha);
        x = getScaledXValue(x);
        y = getScaledYValue(y);
        w = getScaledXValue(w);
        h = getScaledYValue(h);
        content = content.trim(); // some pages have trailing CRLF which we do not want

        TextView tv = new TextView(mContext);
        tv.setLayoutParams(new SnuffyLayoutParams(
                w == 0 ? LayoutParams.WRAP_CONTENT : w,
                bResize ? LayoutParams.WRAP_CONTENT : h,
                x, y));
        tv.setText(content);
        tv.setGravity(getGravityFromAlign(align));
        tv.setTypeface(null, getTypefaceFromModifier(modifier));
        tv.setTextColor(color);
        tv.setBackgroundColor(bgColor);
        tv.setTextSize(getScaledTextSize(textSize));
        tv.setPadding(0, 0, 0, 0);

        return new SnuffyAlternateTypefaceTextView(tv)
                .setAlternateTypeface(mAlternateTypeface)
                .get();
    }

    static Hashtable<String, Bitmap> bitmapCache = new Hashtable<String, Bitmap>();

//	private Bitmap getBitmap(Context context, String imageFileName) {
//		if (mFromAssets)
//			return getBitmapFromAsset(context, imageFileName);
//		else
//			return getBitmapFromFile(context, imageFileName);
//	}
//	
//	private Bitmap getBitmapFromAsset(Context context, String imageFileName) {
//		if (bitmapCache.containsKey(imageFileName)) {
//			return bitmapCache.get(imageFileName);
//		}
//		String path = mImageFolderName + imageFileName;
//		try {
//			InputStream isImage = context.getAssets().open(path, AssetManager.ACCESS_BUFFER); // read into memory since it's not very large
//        	//return BitmapFactory.decodeStream(isImage);			
//        	Bitmap b = BitmapFactory.decodeStream(isImage);
//        	mTotalBitmapSpace += b.getRowBytes() * b.getHeight();
//        	Log.d("BITMAPS", imageFileName + ": " + b.getRowBytes() * b.getHeight());
//        	bitmapCache.put(imageFileName, b);
//        	return b;
//			
//		} catch (IOException e) {
//			// try the next path instead
//		}		
//		path = mSharedFolderName + imageFileName;
//		try {
//			InputStream isImage = context.getAssets().open(path, AssetManager.ACCESS_BUFFER); // read into memory since it's not very large
//        	//return BitmapFactory.decodeStream(isImage);			
//        	Bitmap b = BitmapFactory.decodeStream(isImage);
//        	mTotalBitmapSpace += b.getRowBytes() * b.getHeight();
//        	Log.d("BITMAPS", imageFileName + ": " + b.getRowBytes() * b.getHeight());
//        	bitmapCache.put(imageFileName, b);
//        	return b;
//			
//		} catch (IOException e) {
//			Log.e(TAG, "Cannot open or read bitmap file: " + imageFileName);
//			return null;
//		}		
//	}

    private Bitmap getBitmapFromAssetOrFile(Context context, String imageFileName)
    {
        // 1. first try the cache
        if (bitmapCache.containsKey(imageFileName))
        {
            return bitmapCache.get(imageFileName);
        }

        // 2a.  next the package-specific folder
        String path = mImageFolderName + imageFileName;
        InputStream isImage = null;
        try
        {
            if (mFromAssets)
                isImage = context.getAssets().open(path, AssetManager.ACCESS_BUFFER); // read into memory since it's not very large
            else
            {
                isImage = new BufferedInputStream(new FileInputStream(mAppRef.get().getDocumentsDir().getPath() + "/" + path));
            }
            return getBitmapFromStream(imageFileName, isImage);

        } catch (IOException e)
        {
            // try the next path instead
        } finally
        {
            if (isImage != null)
            {
                try
                {
                    isImage.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        // 2b.  next the package-specific folder with a @2x
        path = mImageFolderName + imageFileName.replace(".png", "@2x.png");
        isImage = null;
        try
        {
            if (mFromAssets)
                isImage = context.getAssets().open(path, AssetManager.ACCESS_BUFFER); // read into memory since it's not very large
            else
            {
                Log.d(TAG, "getBitmapFromAssetOrFile:" + mAppRef.get().getDocumentsDir().getPath() + "/" + path);
                isImage = new BufferedInputStream(new FileInputStream(mAppRef.get().getDocumentsDir().getPath() + "/" + path));
            }
            return getBitmapFromStream(imageFileName, isImage);

        } catch (IOException e)
        {
            // try the next path instead
        } finally
        {
            if (isImage != null)
            {
                try
                {
                    isImage.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        // 3. next the folder that is shared by all packages
        path = mSharedFolderName + imageFileName;
        isImage = null;
        try
        {
            isImage = context.getAssets().open(path, AssetManager.ACCESS_BUFFER); // read into memory since it's not very large
            return getBitmapFromStream(imageFileName, isImage);

        } catch (IOException e)
        {
            Log.e(TAG, "Cannot open or read bitmap file: " + imageFileName);
            return null;
        } finally
        {
            if (isImage != null)
            {
                try
                {
                    isImage.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private Bitmap getBitmapFromStream(String imageFileName, InputStream isImage)
    {
        Bitmap b = BitmapFactory.decodeStream(isImage);
        mTotalBitmapSpace += b.getRowBytes() * b.getHeight();
        Log.d("BITMAPS", imageFileName + ": " + b.getRowBytes() * b.getHeight());
        bitmapCache.put(imageFileName, b);
        return b;
    }

    private int getScaledXValue(int x)
    {
        return (int) Math.round((double) (x * mPageWidth) / (double) REFERENCE_DEVICE_WIDTH);
    }

    private int getScaledYValue(int y)
    {
        return (int) Math.round((double) (y * mPageHeight) / (double) REFERENCE_DEVICE_HEIGHT);
    }

    private float getScaledTextSize(float textSize)
    {
        // textSize is supplied is SP units.
        // return a value in DP units.
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (textSize * (float) mPageHeight / (float) REFERENCE_DEVICE_HEIGHT) / scale;
    }

    private int getColorAttributeValue(Element el, String attrName, int defaultValue)
    {
        String s = el.getAttribute(attrName).trim();
        if (s.length() == 0)
            return defaultValue;
        else
            return Color.parseColor(s);
    }

    private float getFloatAttributeValue(Element el, String attrName, float defaultValue)
    {
        String s = el.getAttribute(attrName).trim();
        if (s.length() == 0)
            return defaultValue;
        else
            return Float.valueOf(s);
    }

    private int getIntegerAttributeValue(Element el, String attrName, int defaultValue)
    {
        String s = el.getAttribute(attrName).trim();
        if (s.length() == 0)
            return defaultValue;
        else
            return Integer.valueOf(s, 10);
    }

    private String getStringAttributeValue(Element el, String attrName, String defaultValue)
    {
        String s = el.getAttribute(attrName).trim();
        if (s.length() == 0)
            return defaultValue;
        else
            return s;
    }

    private int setColorAlphaVal(int color, float alpha)
    {
        return Color.argb((int) (255.0f * alpha), Color.red(color), Color.green(color), Color.blue(color));
    }

    private Element getChildElementNamed(Element elParent, String elementName)
    {
        Node node = elParent.getFirstChild();
        while (node != null)
        {
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                Element el = (Element) node;
                if (el.getTagName().equalsIgnoreCase(elementName))
                    return el;
            }
            node = node.getNextSibling();
        }
        return null;
    }

    private String getTextContentImmediate(Element elParent)
    {
        Node node = elParent.getFirstChild();
        while (node != null)
        {
            if (node.getNodeType() == Node.TEXT_NODE)
                return node.getNodeValue();
            node = node.getNextSibling();
        }
        return "";
    }

    private Vector<Element> getChildrenWithName(Element elParent, String elementName)
    {
        Vector<Element> v = new Vector<Element>();
        Node node = elParent.getFirstChild();
        while (node != null)
        {
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                Element el = (Element) node;
                if (el.getTagName().equalsIgnoreCase(elementName))
                    v.add(el);
            }
            node = node.getNextSibling();
        }
        return v;
    }

    private int getTypefaceFromModifier(String modifier)
    {
        if (modifier.equalsIgnoreCase("italics"))
            return Typeface.ITALIC;
        if (modifier.equalsIgnoreCase("bold"))
            return Typeface.BOLD;
        if (modifier.equalsIgnoreCase("bold-italics"))
            return Typeface.BOLD_ITALIC;
        return Typeface.NORMAL;
    }

    private int getGravityFromAlign(String align)
    {
        if (align.equalsIgnoreCase("right"))
            return Gravity.RIGHT;
        else if (align.equalsIgnoreCase("left"))
            return Gravity.LEFT;
        else
            return Gravity.CENTER_HORIZONTAL;

    }

    private View getHRView(int color, int pageWidth, int margin, int yPos, int thickness)
    {
        View hr = new View(mContext);
        hr.setBackgroundColor(setColorAlphaVal(color, HR_ALPHA));
        hr.setLayoutParams(new SnuffyLayoutParams(
                pageWidth - 2 * margin,
                thickness,
                margin, yPos));
        return hr;
    }

    private View getVRView(int color, int xPos, int y1Pos, int y2Pos, int thickness)
    {
        View vr = new View(mContext);
        vr.setBackgroundColor(setColorAlphaVal(color, HR_ALPHA));
        vr.setLayoutParams(new SnuffyLayoutParams(
                thickness,
                y2Pos - y1Pos,
                xPos, y1Pos));
        return vr;
    }

    private void adjustHeadingFont(TextView tv)
    {
        String theText = tv.getText().toString();
        theText = theText.replace(" ", "\n");
        tv.setText(theText);
        SnuffyLayoutParams lpOrig = (SnuffyLayoutParams) tv.getLayoutParams(); // save it
        float textSize = tv.getTextSize();
        float minTextSize = 6.0f; // try down to 6px text
        float incr = 1.0f;
        int w;
        do
        {
            tv.setLayoutParams(new SnuffyLayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT,
                    0, 0));
            tv.setTextSize(getScaledTextSize(textSize));
            tv.measure(
                    MeasureSpec.UNSPECIFIED,
                    MeasureSpec.UNSPECIFIED);
            w = tv.getMeasuredWidth();
            if (w <= lpOrig.width)
                break;
            textSize -= incr;
        } while (textSize >= minTextSize);
        lpOrig.width = w;
        tv.setLayoutParams(lpOrig);
    }

    private void setLayoutParamsOfViewWithTag(View parentView, int tagValue, SnuffyLayoutParams lp)
    {
        View v = parentView.findViewWithTag(tagValue);
        if (v != null)
            v.setLayoutParams(lp);
    }

    private void startAnimationOnViewWithTag(View parentView, int tagValue, Animation a)
    {
        View v = parentView.findViewWithTag(tagValue);
        if (v != null)
            v.startAnimation(a);
    }

    private void bringSubViewWithTagToFront(View parentView, int tagValue)
    {
        View v = parentView.findViewWithTag(tagValue);
        if (v != null)
            v.bringToFront(); // should that be v.bringSubViewToFront() ?
    }

    private void orderTitleViews(View titleClippingContainer)
    {
        // IOS version calls this orderShadows

        // subtitle drop shadows
        bringSubViewWithTagToFront(titleClippingContainer, 556); // E
        bringSubViewWithTagToFront(titleClippingContainer, 552); // S
        bringSubViewWithTagToFront(titleClippingContainer, 553); // SE

        //bringSubViewWithTagToFront(titleClippingContainer, 572); // S static

        // subtitle
        bringSubViewWithTagToFront(titleClippingContainer, 550); // subtitle

        // title drop shadows
        bringSubViewWithTagToFront(titleClippingContainer, 561); // N/NE (my invention)
        bringSubViewWithTagToFront(titleClippingContainer, 566); // E
        bringSubViewWithTagToFront(titleClippingContainer, 562); // S
        bringSubViewWithTagToFront(titleClippingContainer, 563); // SE

        //bringSubViewWithTagToFront(page, 573); // SE static

        // title
        bringSubViewWithTagToFront(titleClippingContainer, 560); // title

        titleClippingContainer.bringToFront();
    }

    private class SimpleAnimationListener implements Animation.AnimationListener
    {

        private Runnable mToRunOnEnd;
        private long mDelay;

        public SimpleAnimationListener(Runnable toRunOnEnd)
        {
            mToRunOnEnd = toRunOnEnd;
            mDelay = 0;
        }

        @Override
        public void onAnimationStart(Animation animation)
        {
        }

        @Override
        public void onAnimationRepeat(Animation animation)
        {
        }

        @Override
        public void onAnimationEnd(Animation animation)
        {
            if (mDelay == 0)
                new Handler().post(mToRunOnEnd);
            else
                new Handler().postDelayed(mToRunOnEnd, mDelay);
        }

    }

    private void setupUrlButtonHandler(SnuffyPage currPage, View button, String mode, String content)
    {
        final boolean bUrlMode = mode.equalsIgnoreCase("url");
        final boolean bAllUrlMode = mode.equalsIgnoreCase("allurl");
        final boolean bPhoneMode = mode.equalsIgnoreCase("phone");
        final boolean bEmailMode = mode.equalsIgnoreCase("email");
        final String finalContent = content;
        final SnuffyPage finalCurrPage = currPage;

        if (bPhoneMode || bEmailMode)
        {
            button.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    try
                    {
                        String urlScheme = "http://"; // bUrlMode
                        if (bPhoneMode) urlScheme = "tel:";
                        if (bEmailMode) urlScheme = "mailto:";
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlScheme + finalContent));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                        if (bPhoneMode)
                            Toast.makeText(mContext, "Cannot launch dialler", Toast.LENGTH_SHORT).show();
                        if (bEmailMode)
                            Toast.makeText(mContext, "Cannot launch email app", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else
        {    // url or allurl
            button.setOnClickListener(new View.OnClickListener()
            {
                // See urlClick in standardViewController.m

                @Override
                public void onClick(View v)
                {
                    // Display a dialog with:
                    //	Title: the url
                    //	Button: Open
                    //  Button: Email
                    //	Button: Copy
                    //	Cancelable
                    AlertDialog.Builder builder = new AlertDialog.Builder(finalCurrPage.mCallingActivity);
                    String dialogTitle = bAllUrlMode ? mContext.getString(R.string.all_websites) : finalContent;
                    builder.setMessage(dialogTitle);
                    builder.setCancelable(true);
                    if (bUrlMode)
                    {
                        builder.setPositiveButton("Open",
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        try
                                        {
                                            String urlScheme = "http://"; // bUrlMode
                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlScheme + finalContent));
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            mContext.startActivity(intent);
                                        } catch (Exception e)
                                        {
                                            e.printStackTrace();
                                            Toast.makeText(mContext, "Cannot launch browser", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                    builder.setNeutralButton(R.string.email,
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    // This code - to send an email with subject and body - is similar
                                    // to code in SnuffyActivity: doCmdShare. Can we abstract to a common function?

                                    String subjectLine = mPackageTitle + (bAllUrlMode ? " - websites to assist you" : " - a website to assist you");
                                    // stick to plain text - Android cannot reliably send HTML email and anyway
                                    // most receivers will turn the link into a hyperlink automatically

                                    String msgBody = "http://" + finalContent;

                                    SnuffyApplication app = ((SnuffyApplication) finalCurrPage.mCallingActivity.getApplication());
                                    app.sendEmailWithContent(finalCurrPage.mCallingActivity, subjectLine, msgBody);
                                }
                            });
                    builder.setNegativeButton(R.string.copy,
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                    clipboard.setText("http://" + finalContent);
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });

        }
    }
}

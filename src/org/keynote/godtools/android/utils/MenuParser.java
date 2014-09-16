package org.keynote.godtools.android.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.InflateException;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import org.keynote.godtools.android.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by john.jarder on 6/23/14.
 */
public class MenuParser
{
    private final String LOGTAG = "Menu Parser";

    private static final String XML_ITEM = "item";
    private static final String XML_MENU = "menu";
    private static final int NO_ID = 0;

    protected Context context;
    protected Menu menu;
    protected ArrayList<ActionMenu> actionMenuList;
    protected ArrayList<MenuItem> menuItems;

    public MenuParser(Context context)
    {
        this.context = context;
    }

    public void createMenuItems(int menuResourceId, Menu menu)
    {
        Log.d(LOGTAG, "super createMenuItems");
        actionMenuList = new ArrayList<ActionMenu>();
        menuItems = new ArrayList<MenuItem>();

        this.menu = menu;
        inflate(menuResourceId, menu);
    }

    public ArrayList<ActionMenu> getActionMenuList()
    {
        return actionMenuList;
    }

    public ArrayList<MenuItem> getMenuItems()
    {
        return menuItems;
    }

    private void inflate(int menuRes, Menu menu)
    {
        XmlResourceParser parser = null;
        try
        {
            parser = context.getResources().getLayout(menuRes);
            AttributeSet attrs = Xml.asAttributeSet(parser);

            Log.d(LOGTAG, "attrs: " + attrs);

            parseMenu(parser, attrs, menu);
        } catch (XmlPullParserException e)
        {
            throw new InflateException("Error inflating menu XML", e);
        } catch (IOException e)
        {
            throw new InflateException("Error inflating menu XML", e);
        } catch (RuntimeException e)
        {
            parser.close();
            parser = null;
        } finally
        {
            if (parser != null) parser.close();
        }
    }

    /**
     * Called internally to fill the given menu. If a sub menu is seen, it will
     * call this recursively.
     */
    private void parseMenu(XmlPullParser parser, AttributeSet attrs, Menu menu)
            throws XmlPullParserException, IOException
    {

        int eventType = parser.getEventType();
        String tagName;
        boolean lookingForEndOfUnknownTag = false;
        String unknownTagName = null;

        int index = 0;

        boolean reachedEndOfMenu = false;
        while (!reachedEndOfMenu)
        {

            switch (eventType)
            {
                case XmlPullParser.START_TAG:
                    Log.d(LOGTAG, "parser: eventType.START_TAG");
                    if (lookingForEndOfUnknownTag)
                    {
                        break;
                    }

                    tagName = parser.getName();

                    Log.d(LOGTAG, "parser.getName(): " + tagName);

                    if (tagName.equals(XML_ITEM))
                    {
                        readMenuItem(attrs, index);
                        index++;
                    }
                    else if (tagName.equals(XML_MENU))
                    {
//                        // A menu start tag denotes a submenu for an item
//                        SubMenu subMenu = addSubMenuItem(attrs);
//
//                        // Parse the submenu into returned SubMenu
//                        parseMenu(parser, attrs, subMenu);
                    }
                    else
                    {
                        lookingForEndOfUnknownTag = true;
                    }

                    break;

                case XmlPullParser.END_DOCUMENT:
                    Log.d(LOGTAG, "END OF DOC");
                    throw new RuntimeException("Unexpected end of document");
            }

            eventType = parser.next();
        }
    }

    private void readMenuItem(AttributeSet attrs, int index)
    {
        final int defaultItemId = NO_ID;
        final int defaultGroupid = NO_ID;
        final int defaultCategoryOrder = 0;

        ActionMenu actionMenu = new ActionMenu();

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.MenuItem);

        actionMenu.itemId = a.getResourceId(R.styleable.MenuItem_android_id, defaultItemId);
        actionMenu.itemTitle = a.getText(R.styleable.MenuItem_android_title).toString();
        actionMenu.itemIconResId = a.getResourceId(R.styleable.MenuItem_android_icon, 0);
        actionMenu.itemIndex = index;

        Log.d(LOGTAG, "itemId: " + actionMenu.itemId);
        Log.d(LOGTAG, "itemTitle: " + actionMenu.itemTitle);
        Log.d(LOGTAG, "itemIconResId: " + actionMenu.itemIconResId);

        menuItems.add(menu.add(defaultGroupid, actionMenu.itemId, defaultCategoryOrder, actionMenu.itemTitle));

        Log.d(LOGTAG, "menu size: " + menu.size());
        Log.d(LOGTAG, "menuItems size: " + menuItems.size());

        actionMenuList.add(actionMenu);
    }


    public class ActionMenu
    {
        public int itemIndex;
        public int itemId;
        public int itemIconResId;
        public String itemTitle;
        public ImageView menuItemView;
    }
}

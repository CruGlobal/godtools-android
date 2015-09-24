package org.keynote.godtools.android.everystudent;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.keynote.godtools.android.utils.GoogleAnalytics;

/**
 * Provides access to the EveryStudent database.
 */
public class EveryStudentProvider extends ContentProvider
{

    private static final String AUTHORITY = "org.keynote.godtools.android.everystudent.EveryStudentProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/everystudent");

    private static final String BASE_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.org.keynote.godtools.android.everystudent";
    private static final String SEARCH_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.org.keynote.godtools.android.everystudent";
    private static final String TITLE_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.org.keynote.godtools.android.everystudent.item";
    private static final String CONTENT_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.org.keynote.godtools.android.everystudent.item";
    private static final int SEARCH_EVERYSTUDENT = 0;
    private static final int SEARCH_SUGGEST = 1;
    private static final int REFRESH_SHORTCUT = 2;
    private static final int GET_BASE = 3;
    private static final int GET_TITLES = 4;
    private static final int GET_CONTENT = 5;
    private static final int GET_CONTENT_ROWID = 6;
    private static final UriMatcher sURIMatcher = buildUriMatcher();
    private EveryStudentDatabase mEveryStudentDatabase;

    private static UriMatcher buildUriMatcher()
    {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, "everystudent", SEARCH_EVERYSTUDENT);
        matcher.addURI(AUTHORITY, "everystudent/base", GET_BASE);
        matcher.addURI(AUTHORITY, "everystudent/category/*/title", GET_TITLES);
        matcher.addURI(AUTHORITY, "everystudent/category/*/title/*/content", GET_CONTENT);
        matcher.addURI(AUTHORITY, "everystudent/content/#", GET_CONTENT_ROWID);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", REFRESH_SHORTCUT);
        return matcher;
    }

    @Override
    public boolean onCreate()
    {
        mEveryStudentDatabase = new EveryStudentDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder)
    {

        switch (sURIMatcher.match(uri))
        {
            case SEARCH_EVERYSTUDENT:
                if (selectionArgs == null)
                {
                    throw new IllegalArgumentException(
                            "selectionArgs must be provided for the Uri: " + uri);
                }
                return search(selectionArgs[0]);
            case SEARCH_SUGGEST:
                return getSuggestions(uri.getLastPathSegment());
            case GET_BASE:
                return getBase();
            case GET_TITLES:
                return getTitles(uri.getPathSegments().get(2));
            case GET_CONTENT:
                return getContent(uri.getPathSegments().get(2), uri.getPathSegments().get(4));
            case GET_CONTENT_ROWID:
                return getContent(uri.getPathSegments().get(2));
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    private Cursor getBase()
    {
        return mEveryStudentDatabase.getBase();
    }

    private Cursor getTitles(String category)
    {
        return mEveryStudentDatabase.getTitles(category);
    }

    private Cursor getContent(String rowid)
    {
        return mEveryStudentDatabase.getContent(rowid, null);
    }

    private Cursor getContent(String category, String title)
    {
        return mEveryStudentDatabase.getContent(category, title, null);
    }

    private Cursor getSuggestions(String query)
    {
        query = query.toLowerCase();
        return mEveryStudentDatabase.getSuggestions(query);
    }

    private Cursor search(String query)
    {
        if (!query.equalsIgnoreCase("search_suggest_query"))
        {
            Tracker tracker = GoogleAnalytics.getTracker(getContext());
            tracker.setScreenName("everystudent-search");
            tracker.send(new HitBuilders.EventBuilder()
                    .setCustomDimension(1, "everystudent")
                    .setCustomDimension(2, "en_classic")
                    .setCustomDimension(3, "en_classic-everystudent-1")
                    .setCategory("searchbar")
                    .setAction("tap")
                    .setLabel(query)
                    .build());
        }

        query = query.toLowerCase();
        return mEveryStudentDatabase.getSearch(query);
    }

    @Override
    public String getType(@NonNull Uri uri)
    {
        switch (sURIMatcher.match(uri))
        {
            case SEARCH_EVERYSTUDENT:
                return SEARCH_MIME_TYPE;
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            case GET_BASE:
                return BASE_MIME_TYPE;
            case GET_TITLES:
                return TITLE_MIME_TYPE;
            case GET_CONTENT:
            case GET_CONTENT_ROWID:
                return CONTENT_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        throw new UnsupportedOperationException();
    }

}

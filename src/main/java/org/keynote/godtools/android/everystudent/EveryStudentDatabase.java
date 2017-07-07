package org.keynote.godtools.android.everystudent;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

import org.keynote.godtools.android.R;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

class EveryStudentDatabase
{

    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    private static final int DATABASE_VERSION = 4; // Change this number when EveryStudent.xml is updated/changed.
    private static final String DATABASE_NAME = "godtools";
    private static final String TABLE_NAME = "everystudent";
    private static final String ROWID = "rowid";
    private static final String CATEGORY = "category";
    private static final HashMap<String, String> mColumnMap = buildColumnMap();
    private final EveryStudentOpenHelper mDatabaseOpenHelper;

    public EveryStudentDatabase(Context context)
    {
        mDatabaseOpenHelper = new EveryStudentOpenHelper(context);
    }

    private static HashMap<String, String> buildColumnMap()
    {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(EveryStudentDatabase.CATEGORY, EveryStudentDatabase.CATEGORY);
        map.put(EveryStudentDatabase.TITLE, EveryStudentDatabase.TITLE);
        map.put(EveryStudentDatabase.CONTENT, EveryStudentDatabase.CONTENT);
        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return map;
    }

    private Cursor query(String selection, String[] selectionArgs, String[] columns)
    {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_NAME);
        builder.setProjectionMap(mColumnMap);

        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null)
        {
            return null;
        }
        else if (!cursor.moveToFirst())
        {
            cursor.close();
            return null;
        }
        return cursor;
    }

    private Cursor rawQuery(String sql, String[] selectionArgs)
    {
        return mDatabaseOpenHelper.getReadableDatabase().rawQuery(sql, selectionArgs);
    }

    public Cursor getBase()
    {
        String sql = "SELECT " + ROWID + ", " + CATEGORY + ", " + TITLE + " FROM " + TABLE_NAME;
        return rawQuery(sql, null);
    }

    public Cursor getTitles(String category)
    {
        String selection = CATEGORY + "=?";
        String[] selectionArgs = new String[]{category};
        return query(selection, selectionArgs, new String[]{ROWID, TITLE});
    }

    public Cursor getContent(String category, String title, String[] columns)
    {
        String selection = CATEGORY + "=? AND " + TITLE + "=?";
        String[] selectionArgs = new String[]{category, title};
        return query(selection, selectionArgs, columns);
    }

    public Cursor getContent(String rowid, String[] columns)
    {
        String selection = ROWID + "=?";
        String[] selectionArgs = new String[]{rowid};
        return query(selection, selectionArgs, columns);
    }

    public Cursor getSearch(String query)
    {
        ArrayList<Result> results = searchCompile(query);
        return buildSuggestionCursor(results, results.size());
    }

    public Cursor getSuggestions(String query)
    {
        return buildSuggestionCursor(searchCompile(query), 10);
    }

    private Cursor getRawResults(String query)
    {
        String sql = "SELECT " + ROWID + ", offsets(" + TABLE_NAME + "), snippet(" + TABLE_NAME + ",\"\",\"\",\"...\"), " + TITLE + " FROM " + TABLE_NAME +
                " WHERE " + TABLE_NAME + " MATCH ?";
        String[] selectionArgs = new String[]{query.trim().replace(" ", "* ") + "*"};
        return rawQuery(sql, selectionArgs);
    }

    private ArrayList<Result> searchCompile(String query)
    {
        Cursor rawData = getRawResults(query);
        ArrayList<Result> resultList = new ArrayList<Result>();
        if (rawData.moveToFirst())
        {
            do
            {
                int rowid = rawData.getInt(0);
                String offsets = rawData.getString(1);
                String snippet = rawData.getString(2);
                String title = rawData.getString(3);
                double rank = rankRow(offsets);
                resultList.add(new Result(rowid, title, snippet, rank));
            } while (rawData.moveToNext());
        }
        rawData.close();
        return resultList;
    }

    private Cursor buildSuggestionCursor(ArrayList<Result> s, int num)
    {
        MatrixCursor suggestions = new MatrixCursor(new String[]{BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_DATA});
        Collections.sort(s, new Comparator<Object>()
        {
            public int compare(Object o1, Object o2)
            {
                Result s1 = (Result) o1;
                Result s2 = (Result) o2;
                return s2.rank.compareTo(s1.rank);
            }
        });

        int i = 0;
        Iterator<Result> itr = s.iterator();
        while (i < num && itr.hasNext())
        {
            Result r = itr.next();
            Uri contentUri = Uri.withAppendedPath(EveryStudentProvider.CONTENT_URI, "content");
            Uri contentUriRow = Uri.withAppendedPath(contentUri, String.valueOf(r.rowid));
            suggestions.addRow(new Object[]{r.rowid, r.row1, r.row2, contentUriRow});
            i++;
        }
        return suggestions;
    }

    private double rankRow(String offsets)
    {
        double rank = 0.0;
        double[] colWeight = {2, 6, 1};

        String[] offsetArray = offsets.split(" ");
        ArrayList<String> o = new ArrayList<String>();
        Collections.addAll(o, offsetArray);

        while (o.size() > 0)
        {
            int col = Integer.valueOf(o.get(0));
            o.remove(0);
            int offset = Integer.valueOf(o.get(0));
            o.remove(0);
            rank += colWeight[col] * 1 / (Math.pow(offset, 0.333) + 1);
        }
        return rank;
    }

    /**
     * EveryStudent DB OpenHelper
     */
    private static class EveryStudentOpenHelper extends SQLiteOpenHelper
    {

        private static final String CREATE_STMT = "CREATE VIRTUAL TABLE " + TABLE_NAME + " USING fts3(" + CATEGORY + ", " + TITLE + ", " + CONTENT + ");";
        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;
        private EveryStudentHandler handler = null;

        EveryStudentOpenHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            mDatabase = db;
            db.execSQL(CREATE_STMT);
            //loadEveryStudent();
            try
            {
                loadXMLContent();
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

        public void loadXMLContent() throws IOException, ParserConfigurationException, SAXException
        {
            Resources resources = mHelperContext.getResources();
            InputStream inputStream = resources.openRawResource(R.raw.everystudent);
            Reader reader = new InputStreamReader(inputStream, "UTF-8");
            InputSource is = new InputSource(reader);
            is.setEncoding("UTF-8");

            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser parser = spf.newSAXParser();
            XMLReader xr = parser.getXMLReader();
            handler = new EveryStudentHandler();
            xr.setContentHandler(handler);
            xr.parse(is);

            List<Map<String, String>> categories = handler.getCategories();
            List<List<Map<String, String>>> topics = handler.getTopics();

            for (int i = 0; i < categories.size(); i++)
            {
                String catName = categories.get(i).get(Constants.NAME);
                List<Map<String, String>> catTopics = topics.get(i);
                for (int j = 0; j < catTopics.size(); j++)
                {
                    insertArticle(catName, catTopics.get(j).get(Constants.NAME), catTopics.get(j).get(Constants.CONTENT));
                }
            }
        }

        public long insertArticle(String category, String title, String content)
        {
            ContentValues initialValues = new ContentValues();
            initialValues.put(CATEGORY, category);
            initialValues.put(TITLE, title);
            initialValues.put(CONTENT, content);
            return mDatabase.insert(TABLE_NAME, null, initialValues);
        }
    }

    /**
     * Result Item
     */
    private class Result
    {
        public final int rowid;
        public final String row1;
        public final String row2;
        public final Double rank;

        public Result(int rowid, String row1, String row2, double rank)
        {
            this.rowid = rowid;
            this.row1 = row1;
            this.row2 = row2;
            this.rank = rank;
        }
    }
}
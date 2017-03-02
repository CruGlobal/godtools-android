package godtools.keynote.org.gttestui.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class BasePrefs {

    public static final String USER_PREFS_LOCATION = "USER_PREFS";
    public static final String APP_PREFS_LOCATION = "APP_PREFS";

    private static final String IS_USER_CONFIGURED = "IS_USER_CONFIGURED";

    private static final String TAG = "BasePrefs";

    public static boolean isUserLanguageConfigured(Context context) {
        return getUserPrefs(context).getBoolean(IS_USER_CONFIGURED, false);
    }

    public static void setUserConfigured(Context context, boolean userConfigured) {
        getUserPrefs(context).edit().putBoolean(IS_USER_CONFIGURED, userConfigured).apply();
    }

    protected static void setStringAsync(Context context, String key, String value) {
        SharedPreferences prefs = get(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    protected static void setUserStringAsync(Context context, String key, String value) {
        SharedPreferences prefs = getUserPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    protected static void setString(Context context, String key, String value) {
        SharedPreferences prefs = get(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    protected static void setUserString(Context context, String key, String value) {
        SharedPreferences prefs = getUserPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private static void setInt(Context context, String key, int value) {
        SharedPreferences prefs = get(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private static void setBooleanInternal(SharedPreferences prefs, String key, boolean value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private static void setUserBoolean(Context context, String key, boolean value) {
        setBooleanInternal(getUserPrefs(context), key, value);
    }

    private static void setBoolean(Context context, String key, boolean value) {
        setBooleanInternal(get(context), key, value);
    }

    private static void setSerializableInternal(Context context, boolean isUser, String key, Serializable serializable) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(serializable);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String serialized = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
        if (isUser) {
            setUserString(context, key, serialized);
        } else {
            setString(context, key, serialized);
        }
    }

    private static void setUserSerializable(Context context, String key, Serializable serializable) {
        setSerializableInternal(context, true, key, serializable);
    }

    private static void setSerializable(Context context, String key, Serializable serializable) {
        setSerializableInternal(context, false, key, serializable);
    }

    private static Object getSerializableInternal(SharedPreferences prefs, String key) {
        String serialized = prefs.getString(key, null);
        if (serialized == null) {
            return null;
        }
        byte[] data;
        try {
            data = Base64.decode(serialized, Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Unable to deserialize object for key (" + key + ")", e);
            return null;
        }
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(data));
            Object result = ois.readObject();
            ois.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static Object getUserSerializable(Context context, String key) {
        return getSerializableInternal(getUserPrefs(context), key);
    }

    private static Object getSerializable(Context context, String key) {
        return getSerializableInternal(get(context), key);
    }

    public static SharedPreferences get(Context context) {
        return context.getSharedPreferences(APP_PREFS_LOCATION, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getUserPrefs(Context context) {
        return context.getSharedPreferences(USER_PREFS_LOCATION, Context.MODE_PRIVATE);
    }

    public static void clearAppPrefs(Context context) {
        get(context).edit().clear().commit();
    }

    public static void clearUserPrefs(Context context) {
        getUserPrefs(context).edit().clear().commit();
    }
}

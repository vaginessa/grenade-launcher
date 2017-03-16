package bitshift.grenadelauncher;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.util.ArrayList;

/**
 * Created by Bronson on 3/06/13.
 * store app settings here so we can access them quickly without constantly loading them from xml
 */
public class SettingsMgr implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private ArrayList<SettingsMgrListener> mListeners = new ArrayList<SettingsMgrListener> ();

    static private SettingsMgr mSingleInstance;
    static private SharedPreferences mPreferences;
    private DisplayMetrics mDisplayMetrics;

    SettingsMgr(Context context)
    {
        mSingleInstance = this;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        mDisplayMetrics = context.getResources().getDisplayMetrics();
    }

    static SettingsMgr instance()
    {
        return mSingleInstance;
    }

    // custom listener interface (interface is a group of related methods with empty bodies)
    public interface SettingsMgrListener
    {
        void onSettingsChanged(SharedPreferences sharedPreferences, String key);
    }

    public void addSettingsChangeListener(SettingsMgrListener listener)
    {
        for(int i = 0; i < mListeners.size(); ++i)
            if (mListeners.get(i).equals(listener));
        mListeners.remove(listener);

        mListeners.add(listener);
    }

    public void removeSettingsChangeListener(SettingsMgrListener listener)
    {
        for(int i = 0; i < mListeners.size(); ++i)
            if (mListeners.get(i).equals(listener));
        mListeners.remove(listener);
    }

    // shared pref listener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        // notify listeners
        for (SettingsMgrListener listener : mListeners)
            listener.onSettingsChanged(sharedPreferences, key);
    }

    DisplayMetrics displayMetrics()
    {
        return mDisplayMetrics;
    }

    // gets the int & checks its ok :) else return and set default value :D
    int parseInt(String key, int defaultValue, int min, int max)
    {
        int value = min - 1;
        try
        {
            value = Integer.parseInt(getString(key, Integer.toString(defaultValue)));
        }
        catch(Exception e) {}

        if (value > max || value < min)
        {
            putString(key, Integer.toString(defaultValue));
            value = defaultValue;
        }

        return value;
    }

    void putBoolean(String key, boolean val)
    {
        SharedPreferences.Editor editor = SettingsMgr.instance().preferences().edit();
        editor.putBoolean(key, val);
        editor.commit();
    }

    boolean getBool(String key, boolean def)
    {
        return preferences().getBoolean(key, def);
    }

    void putString(String key, String val)
    {
        SharedPreferences.Editor editor = SettingsMgr.instance().preferences().edit();
        editor.putString(key, val);
        editor.commit();
    }

    String getString(String key, String def)
    {
        return preferences().getString(key, def);
    }

    void putInt(String key, int val)
    {
        SharedPreferences.Editor editor = SettingsMgr.instance().preferences().edit();
        editor.putInt(key, val);
        editor.commit();
    }

    int getInt(String key, int def)
    {
        return preferences().getInt(key, def);
    }

    void putFloat(String key, float val)
    {
        SharedPreferences.Editor editor = SettingsMgr.instance().preferences().edit();
        editor.putFloat(key, val);
        editor.commit();
    }

    float getFloat(String key, float def)
    {
        return preferences().getFloat(key, def);
    }

    void putLong(String key, long val)
    {
        SharedPreferences.Editor editor = SettingsMgr.instance().preferences().edit();
        editor.putLong(key, val);
        editor.commit();
    }

    long getLong(String key, long def)
    {
        return preferences().getLong(key, def);
    }

    public SharedPreferences preferences()
    {
        return mPreferences;
    }

    float dpToPixels(float dp)
    {
        float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics());
        return pixels;
    }

    // WORDPAD SPECIFIC
    boolean securityLock()
    {
        return preferences().getBoolean("key_pin_lock", false);
    }

    boolean proximityLock()
    {
        return preferences().getBoolean("key_proximity_lock", false);
    }

    int gridWidth()
    {
        int gridWidth = parseInt("key_grid_width", 4, 1, 100);
        return gridWidth;
    }

    int gridHeight()
    {
        int gridHeight = parseInt("key_grid_height", 6, 1, 100);
        return gridHeight;
    }

    int tileLayout()
    {
        int layout = Integer.parseInt(preferences().getString("key_layout", "0"));
        int tile;

        switch (layout)
        {
        case 0:
            tile = R.layout.app_layout_tile;
            break;

        case 1:
            tile = R.layout.app_layout_list;
            break;

        case 2:
            tile = R.layout.app_layout_center;
            break;

        default:
            tile = R.layout.app_layout_tile;
            break;
        }

        return tile;
    }

    boolean iconEnabled()
    {
        return preferences().getBoolean("key_icon_enabled", true);
    }

    int iconSize()
    {
        int iconSize = parseInt("key_icon_size", 40, 0, 100);
        return (int) dpToPixels(iconSize);
    }

    int iconAlpha()
    {
        int iconAlpha = parseInt("key_icon_alpha", 100, 0, 100);
        iconAlpha *= 2.55;
        return iconAlpha;
    }

    boolean fontEnabled()
    {
        return preferences().getBoolean("key_font_enabled", true);
    }

    int fontSize()
    {
        int fontSize = parseInt("key_font_size", 14, 0, 100);
        return fontSize;
    }

    int fontColor()
    {
        int fontColor = Integer.parseInt(preferences().getString("key_font_color", "0"));
        int color;

        switch (fontColor)
        {
        case 0:
            color = Color.WHITE;
            break;

        case 1:
            color = Color.BLACK;
            break;

        case 2:
            color = Color.GRAY;
            break;

        default:
            color = Color.WHITE; //Color.parseColor(fontColor);
            break;
        }

        return color;
    }

    Typeface fontTypeface()
    {
        int face = Integer.parseInt(preferences().getString("key_font_typeface", "0"));
        Typeface fontface = Typeface.create(Typeface.DEFAULT, 0);
        switch (face)
        {
        case 0:
            fontface = Typeface.create(Typeface.DEFAULT, 0);
            break;

        case 1:
            fontface = Typeface.create(Typeface.SANS_SERIF, 0);
            break;

        case 2:
            fontface = Typeface.create(Typeface.SERIF, 0);
            break;

        case 3:
            fontface = Typeface.create(Typeface.MONOSPACE, 0);
            break;
        }

        return fontface;
    }

    boolean fontShadowEnabled()
    {
        return preferences().getBoolean("key_font_shadow_enabled", false);
    }



    int wallpaperAlpha()
    {
        int alpha = parseInt("key_wallpaper_alpha", 0, 0, 100);
        alpha *= 2.55;
        return alpha;
    }


    String displayTabStrip()
    {
        return preferences().getString("key_display_tabs", "0"); // 0 - always, -1 never, 1 landscape only, 2 portrait only
    }

    boolean restartLauncher()
    {
        return preferences().getBoolean("key_restart_launcher", false);
    }

    boolean displayNotificationBar()
    {
        return preferences().getBoolean("key_notification_bar", true) ;
    }

    String screenOrientation()
    {
        return preferences().getString("key_orientation", "0");
    }

}

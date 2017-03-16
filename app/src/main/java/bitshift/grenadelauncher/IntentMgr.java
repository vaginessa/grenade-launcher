package bitshift.grenadelauncher;

import java.io.IOException;
import java.util.ArrayList;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class IntentMgr extends BroadcastReceiver
{
    static private ArrayList<PackageListener> mPackageChangeListeners = new ArrayList<PackageListener>();
    static private ArrayList<ShortcutListener> mShortcutListeners = new ArrayList<ShortcutListener>();
    static private ArrayList<GroupSelectionListener> mGroupSelectionListeners = new ArrayList<GroupSelectionListener>();

    public static final String ACTION_VIEW_GROUP = "bitshift.grenadelauncher.action.VIEW_GROUP";
    public static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    public static final String ACTION_LOCK_DEVICE = "bitshift.grenadelauncher.action.LOCK_DEVICE";

    static private IntentMgr mSingleInstance;

    // need to keep this empty for android
    public IntentMgr()
    {
        mSingleInstance = this;
    }

    public IntentMgr(Context context)
    {
        mSingleInstance = this;
/*        // intenets to monitor
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_VIEW_GROUP);
       // intentFilter.addAction(Intent.ACTION_SCREEN_ON);
       // intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        context.registerReceiver(this, intentFilter);
*/
    }

    static IntentMgr instance()
    {
        return mSingleInstance;
    }

	@Override
	public void onReceive(Context context, Intent intent) 
	{
        String intentAction = intent.getAction();
        Log.i("GrenadeLauncher", intentAction);

        if (intentAction.equals(Intent.ACTION_PACKAGE_ADDED) || intentAction.equals(Intent.ACTION_PACKAGE_REMOVED))
            notifyPackageListener(intent);
        else if (intentAction.equals(ACTION_INSTALL_SHORTCUT))
            notifyShortcutListener(intent);
        else if (intentAction.equals(Intent.ACTION_SET_WALLPAPER))
            notifyWallPapaper(context);
        else if (intentAction.equals(ACTION_VIEW_GROUP))
            notifyViewGroup();
        else if (intentAction.equals(ACTION_LOCK_DEVICE))
            notifyLockDevice();
	}


    // custom listener interface (interface is a group of related methods with empty bodies)
    public interface PackageListener
    {
        void onPackageChangeListener(Intent intent);
    }

    public void addPackageListener(PackageListener listener)
    {
        mPackageChangeListeners.add(listener);
    }

    public void notifyPackageListener(Intent intent)
    {
        for (PackageListener listener : mPackageChangeListeners)
            listener.onPackageChangeListener(intent);
    }

    // custom listener interface (interface is a group of related methods with empty bodies)
    public interface ShortcutListener
    {
        void onShortcutListener(Intent intent);
    }

    public void addShortcutListener(ShortcutListener listener)
    {
        mShortcutListeners.add(listener);
    }

    public void notifyShortcutListener(Intent intent)
    {
        for (ShortcutListener listener : mShortcutListeners)
            listener.onShortcutListener(intent);
    }

    // custom listener interface (interface is a group of related methods with empty bodies)
    public interface GroupSelectionListener
    {
        void onGroupSelectionListener(Intent intent);
    }

    public void addGroupSelectionListener(GroupSelectionListener listener)
    {
        mGroupSelectionListeners.add(listener);
    }

    public void notifyGroupSelectionListener(Intent intent)
    {
        for (GroupSelectionListener listener : mGroupSelectionListeners)
            listener.onGroupSelectionListener(intent);
    }

    void notifyWallPapaper(Context context)
    {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        try
        {
            wallpaperManager.setResource(R.drawable.black);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    void notifyLockDevice()
    {
        DeviceAdminMgr.instance().lockDevice();
    }

    void notifyViewGroup()
    {

    }
}

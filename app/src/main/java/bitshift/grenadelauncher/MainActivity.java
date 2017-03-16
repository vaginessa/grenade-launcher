
/*
TODO
next version:
- clean up interfaces like in wordpad
- Slide navigation (recent apps)
- double tap to lock screen


- GC_FOR_ALLOC with bitmaps - http://stackoverflow.com/questions/12716574/bitmap-recycle-with-largeheap-enabled
- Checkout this for async loading apps! http://arnab.ch/blog/2013/08/how-to-write-custom-launcher-app-in-android/

Shortcut + custom icons to SD unscaled, then we can load from there use uuid
- if bitmap override, do not scale bitmaps(or some other work around?) mainly for shortcuts & user icons OR save to SD?

- shortcuts
- Check proximity sensors not in use when screen is off (we need to do this as it keeps pinging)
- make grenade launcher open the settings and put group editor etc.. in the settings
- Custom icons (image picker intenet and store the path, should be easy)
- Folders(groups)

-- Fullversion --
- shortcuts
- folders
- wallpaper settings
- font settings
- group editor
- option to launch an app on startup (ie media player etc)



CHANGELOG
Build 9
- Kitkat optimised (android 4.4), works with ART
- Added more debug info to package manager
- Changed page transitions
- Fixed crash when application changed icon
- Add start-up check to see if packages added or removed
- Fix bug with installing/updating app causing icon to change groups
- Added side navigation drawer to access launcher options
- Improved smoothness of scrolling
- Disabled font shadows by default (it improves performance on low end devices)
- Done more work on shortcuts, these should be working correctly now
- API minimum is now android 2.3 Gingerbread
- Added lock device shortcut and device admin manager
- General cleanup and optimisations

Build 8
- data files now stored on internal sd so backup apps should work
- removed permission to write to external sd
- changes to screen on/off code
- changed home button to not return to home screen unless launcher has focus
- fix for app view displaying incorrectly
- reduced garbage collection and update code
- rewrote code which checks added and removed apps, fixing duplicate apps

Build 7
- Default icon size to 40
- Added fade to page transition animations
- Changed the group editor dialog
- Icons are automagically scaled to screen size
- Added background opacity option
- Changed storing of apps from xml to json (massive overhaul)
- Changed the way settings, broadcast and sensor managers work
- Changed path of cache and data to /Android/data/
- Added more font and icon customisation options in preferences (font face, drop shadow, color etc)
- Added No Wallpaper option to wallpaper picker
- Newly installed apps will now appear in the app group instead of the first screen
- Hidden groups will stay hidden after updating
- When apps update they will now remember what group they were in
- Made hidden items a hidden group
- Removed unused settings
- Removed Grenade Launcher icon from showing as an application
- Proximity sensor lock now functional
- Hidden application icons no longer stored in memory
- More work on shortcuts(currently dont save, and may be broken)


Build 6
- Fixed adding and removing apps not updating
- Home button now takes back to home screen
- Removed beta tag (however still in beta)
- Some more work done on the shortcut dialog(displays but does not work yet!)
- Apps should sort correctly now
- Prevented view resizing after exiting apps

Build 5
- Fixed check boxes on dialogs displaying incorrectly
- Added group editor
- General cleanup of code
- Added check before running app to check it exists

Build 4
- Works on android 2.0+

Build 3
- Added sorting in the hidden apps
- Menu button should now display
- Reduced API to support honeycomb (untested)
- Possible fix for boot crash on some devices

Build 2
- Fixed bug with loading duplicate apps if data was missing for the app
- Fixed bug when reinstalling rom and packagename changing
- Fixed a bug when displaying hidden apps
- Fixed bug when updating app its settings got reset
- Reduced needed permissions


 */

package bitshift.grenadelauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.List;



public class MainActivity extends FragmentActivity implements Group.GroupChangeListener, SettingsMgr.SettingsMgrListener, IntentMgr.ShortcutListener
{
    // views
    private static DrawerLayout mDrawerLayout;
    private static ListView mDrawerList;
	private static SectionsPagerAdapter mSectionsPagerAdapter;
    private static ViewPager mPager;
    private static PagerTabStrip mPagerTabStrip;
    private static ImageView mBackground;

    public static String BASEDIR; // store the INTERNAL files dir here
    private boolean mInFocus = true;
	private boolean mHidden = false;

    // managers
    private static IntentMgr mIntentMgr;
    private static SensorMgr mSensorMgr;
    private static PackageMgr mPackageMgr;
    private static SettingsMgr mSettingsMgr;
    private static GroupItemMgr mGroupItemMgr;
    private static JsonMgr mJsonMgr;
    private static NavigationMgr mNavigationMgr;
    private static DeviceAdminMgr mDeviceAdminMgr;

    final public static int REQUEST_CODE_SHORTCUT = 0;
    final public static int REQUEST_CODE_IMAGE = 1;

    // onCreate called by android, thread the loading of create() via delay load handler
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
 		super.onCreate(savedInstanceState);
        Log.i("GrenadeLauncher", "onCreate()");

        // load in views
        setContentView(R.layout.activity_main);
        mPagerTabStrip = (PagerTabStrip) findViewById(R.id.pagerTabStrip);
        mPager = (ViewPager) findViewById(R.id.pager);
        mBackground = (ImageView) findViewById(R.id.background);
        mPagerTabStrip.setVisibility(View.GONE);
        mBackground.setVisibility(View.GONE);

        // navigation views
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        BASEDIR = getApplicationContext().getFilesDir().toString() + "/";

        // create folders
        try
        {
            File f = new File(BASEDIR + "data/application/");
            f.mkdirs();

            f = new File(BASEDIR + "data/group/");
            f.mkdirs();

            f = new File(BASEDIR + "data/shortcut/");
            f.mkdirs();

            f = new File(BASEDIR + "res/");
            f.mkdirs();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // setup our helper singletons
        // NOTE: storing context can be bad, make sure we try to eliminate the need for them in these!!
        mDeviceAdminMgr = new DeviceAdminMgr(getApplicationContext());
        mIntentMgr = new IntentMgr();
        mSensorMgr = new SensorMgr(getApplicationContext());
        mSettingsMgr = new SettingsMgr(getApplicationContext());
        mGroupItemMgr = new GroupItemMgr();
        mPackageMgr = new PackageMgr(getPackageManager());
        mJsonMgr = new JsonMgr(getApplicationContext());
        mNavigationMgr = new NavigationMgr();

        createNavigation();
        drawBackground();
        update();
        goHomeScreen();

        // listeners
        mIntentMgr.addPackageListener(mPackageMgr); // package manager to be notified
        mIntentMgr.addShortcutListener(this);
        mSettingsMgr.addSettingsChangeListener(this);
        mSettingsMgr.addSettingsChangeListener(mPackageMgr);
        mPackageMgr.rootGroup().addGroupChangeListener(this); // root group change listener

        // update app list after complete reboot
        mPackageMgr.updatePackageList();
	}


    private void createNavigation()
    {
        // setup the navigation drawer
        NavigationItemAdapter adapter = new NavigationItemAdapter(getApplicationContext());
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                NavigationItem item = (NavigationItem) adapterView.getItemAtPosition(position);
                SelectNavigationItem(item);
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });
    }


    void SelectNavigationItem(NavigationItem item)
    {
        switch (item.Id())
        {
            case R.id.menu_add_shortcut:
                addShortcut();
                break;

            case R.id.menu_group_editor:
                groupEditor();
                break;

            case R.id.menu_toggle_hidden:
                toggleHidden();
                break;

            case R.id.menu_select_wallpaper:
                selectWallpaper();
                break;

            case R.id.menu_launcher_settings:
                launcherSettings();
                break;
        }
    }

    void selectWallpaper()
    {
        Intent wallpaperIntent = new Intent(Intent.ACTION_SET_WALLPAPER);
        String select = getResources().getString(R.string.dialog_title_select_wallpaper);
        startActivity(Intent.createChooser(wallpaperIntent, select));
    }

    void groupEditor()
    {
        Intent groupIntent = new Intent(this, GroupEditorActivity.class);
        startActivity(groupIntent);
    }

    void launcherSettings()
    {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

	void addShortcut()
	{
		// inflate the builder
		LayoutInflater inflater = LayoutInflater.from(this);
        final View dialogView = inflater.inflate(R.layout.dialog_shortcut_add, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setNegativeButton(R.string.cancel, null);

        final AlertDialog dialog = builder.create(); // store dialog so we can close it after!
        final List<ResolveInfo> items = getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_CREATE_SHORTCUT), 0);
		ListView listShortcut = (ListView) dialogView.findViewById(R.id.list_shortcut);
		ShortcutAddAdapter adapter = new ShortcutAddAdapter(this, items);
		listShortcut.setAdapter(adapter);
        listShortcut.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) 
			{
                dialog.dismiss();
                //dialog.cancel();

				//unregisterReceiver(mBroadcastMgr);
				ComponentName componentName = new ComponentName(items.get(position).activityInfo.packageName, items.get(position).activityInfo.name);
				try 
				{
					Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					intent.setComponent(componentName);
					((Activity) adapterView.getContext()).startActivityForResult(intent, REQUEST_CODE_SHORTCUT);
				} 
				catch (Exception e)
                {
                    e.printStackTrace();
                }
			}

        });

        dialog.show();
	}


    @Override
    public void onShortcutListener(Intent intent)
    {
        onActivityResult(REQUEST_CODE_SHORTCUT, Activity.RESULT_OK, intent);
    }

	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{     
		super.onActivityResult(requestCode, resultCode, intent);
	
		if (resultCode != Activity.RESULT_OK) // check we have picked something
			return;
		
		switch(requestCode) 
		{ 
			case REQUEST_CODE_SHORTCUT:
			{
                int GroupId = mPager.getCurrentItem();
                Group parent = (Group) PackageMgr.instance().rootGroup().childList(GroupItem.FLAG_VISIBLE).get(GroupId);

                Shortcut shortcut = new Shortcut(intent);
                if (!shortcut.isCategoryLauncher())
                {
                    parent.add(shortcut);
                    parent.update();
                    shortcut.writeToFile();
                }
                else
                    Log.i("GrenadeLauncher", "Ignore: Android market shortcut");
				break; 
			}
		} 
	
	}	

	void toggleHidden()
	{
        mHidden = !mHidden;
        update();
        goHomeScreen();

        // clear icons from hidden objects to save memory
        if (!mHidden)
            PackageMgr.instance().rootGroup().acceptVisitor(new GroupItemMgr.DeleteHiddenIconVisitor());
	}

	void update()
	{
        Log.i("GrenadeLauncher", "update() in MainActivity");

        int visibleFlag = GroupItem.FLAG_VISIBLE;
		if (mHidden)
		{
            visibleFlag = GroupItem.FLAG_HIDDEN;
		}

        int page = 0;
        if (mPager != null)
            page = mPager.getCurrentItem();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getApplicationContext(), PackageMgr.instance().rootGroup().childList(visibleFlag));
        mPager.setAdapter(mSectionsPagerAdapter);
        mPager.setCurrentItem(page); // set home

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) // cant do this on old android
            mPager.setPageTransformer(true, new PageTransformerFade());

        Configuration config = getResources().getConfiguration();
        int orientation = config.orientation;
        String strTabDisplay = SettingsMgr.instance().displayTabStrip(); // 0 - always, -1 never, 1 landscape only, 2 portrait only

        switch (orientation)
        {
            case Configuration.ORIENTATION_LANDSCAPE:
            {
                if (strTabDisplay.equals("0") || strTabDisplay.equals("1"))
                    drawTabStrip();
                else
                    hideTabStrip();
                break;
            }
            case Configuration.ORIENTATION_PORTRAIT:
            {
                if (strTabDisplay.equals("0") || strTabDisplay.equals("2"))
                    drawTabStrip();
                else
                    hideTabStrip();
                break;
            }
        }
	}

    void hideTabStrip()
    {
        mPagerTabStrip.setVisibility(PagerTabStrip.GONE);
    }

    void drawTabStrip()
    {
        int color = Color.parseColor("#99000000"); // 25%
        mPagerTabStrip.setBackgroundColor(color);
        mPagerTabStrip.setDrawFullUnderline(true);
        mPagerTabStrip.setTabIndicatorColor(getResources().getColor(android.R.color.holo_blue_dark));

        for (int i = 0; i < mPagerTabStrip.getChildCount(); ++i)  // loop over children and give a font
        {
            View nextChild = mPagerTabStrip.getChildAt(i);
            if (nextChild instanceof TextView)
            {
                TextView label = (TextView) nextChild;
                label.setTypeface(SettingsMgr.instance().fontTypeface(), 0);
                if (SettingsMgr.instance().fontShadowEnabled())
                    label.setShadowLayer(1, 3, 3, Color.BLACK);
            }
        }

        mPagerTabStrip.setVisibility(PagerTabStrip.VISIBLE);
    }

    void drawBackground()
    {
        if (SettingsMgr.instance().wallpaperAlpha() == 0)
            mBackground.setVisibility(View.GONE);
        else
        {
            mBackground.setVisibility(View.VISIBLE);
            Drawable background = mBackground.getDrawable();
            background.setAlpha(SettingsMgr.instance().wallpaperAlpha());
        }
    }

	// Activity config change in manifest: android:configChanges="orientation|screenSize"
	@Override
    public void onConfigurationChanged (Configuration newConfig) 
	{
		super.onConfigurationChanged(newConfig);
		update();
    }	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
        //getMenuInflater().inflate(R.menu.menu_main, menu); // Inflate the menu; this adds items to the action bar if it is present.
        return true;
	}

	@Override
	public void onGroupChangeListener() 
	{
		update();
	}

    // triggered on screen on
    @Override
    public void onResume()
    {
        super.onResume();
        update();
    }

    // triggered on screen off
    @Override
    public void onPause()
    {
        super.onPause();
        System.gc();
    }

    // triggered on refocus from app
    @Override
    public void onRestart()
    {
        super.onResume();
        mInFocus = true;
    }

    // triggered when other app takes focus
    @Override
    public void onStop()
    {
        super.onStop();
        mInFocus = false;
    }

    @Override
    public void onSettingsChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals("key_wallpaper_alpha"))
        {
            drawBackground();
        }

        // restart
        if (SettingsMgr.instance().restartLauncher())
        {
            SettingsMgr.instance().putBoolean("key_restart_launcher", false);
            System.exit(0);
        }

        // full screen - notification bar
        if (SettingsMgr.instance().displayNotificationBar())
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        // force orientation
        String strOrientation = SettingsMgr.instance().screenOrientation();
        if (strOrientation.equals("0"))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        else if (strOrientation.equals("1"))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else if (strOrientation.equals("2"))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        update();
    }

    void openDrawer(View view)
    {
        mDrawerLayout.openDrawer(view);
    }

    void closeDrawer(View view)
    {
        mDrawerLayout.closeDrawer(view);
    }

    boolean isDrawOpen(View view)
    {
        return mDrawerLayout.isDrawerOpen(view);
    }

    // Key Presses
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (!mSensorMgr.checkProximity())
            return true;

        switch(keyCode) // change from false to not allow android to continue doing what it does
        {
            case KeyEvent.KEYCODE_BACK:
            {
                if (isDrawOpen(mDrawerList))
                {
                    closeDrawer(mDrawerList);
                    return true;
                }
                if (mHidden)
                {
                    toggleHidden();
                    return true;
                }
                else
                {
                    goHomeScreen();
                    return true;
                }
            }
            case KeyEvent.KEYCODE_MENU:
            {
                if (isDrawOpen(mDrawerList))
                    closeDrawer(mDrawerList);
                else
                    openDrawer(mDrawerList);

                //return mSensorMgr.checkSecurity();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }


    // detect home press in here, and point to our broadcast manager
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        if (mInFocus)
            goHomeScreen();
    }

    void goHomeScreen()
    {
        if (mPager.getCurrentItem() == homeScreen()) //TODO: some way to expand notification bar here?
        {}
        else
            mPager.setCurrentItem(homeScreen());
    }

    int homeScreen()
    {
        if (mHidden)
        {
            return 0;
        }

        List<Integer> homeGroup = PackageMgr.instance().rootGroup().childIndexList(GroupItem.FLAG_HOMESCREENGROUP);
        return homeGroup.get(0);
    }


}

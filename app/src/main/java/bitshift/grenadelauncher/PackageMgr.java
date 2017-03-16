package bitshift.grenadelauncher;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Bronson on 1/06/13.
 * Fabs gone pro!
 * Custom package manager and database interface
 */
public class PackageMgr implements IntentMgr.PackageListener, SettingsMgr.SettingsMgrListener
{
    static private PackageMgr mSingleInstance;
    static private PackageManager mPackageManager;

    private Group mRootGroup;

    PackageMgr(PackageManager pkm)
    {
        mSingleInstance = this;
        mPackageManager = pkm;

        // setup root group
        setRootGroup(new Group());
        rootGroup().setLabel("_root_");
        rootGroup().setFlags(GroupItem.FLAG_ROOTGROUP);
    }

    public PackageManager packageManager()
    {
        return mPackageManager;
    }

    static PackageMgr instance()
    {
        return mSingleInstance;
    }

    public void createDefaultGroups()
    {
        Group groupA = new Group();
        groupA.setLabel("Apps");
        groupA.setOverridePosition(0);
        groupA.setFlag(GroupItem.FLAG_APPGROUP, true);
        rootGroup().add(groupA);

        Group groupB = new Group();
        groupB.setLabel("Home");
        groupB.setOverridePosition(1);
        groupB.setFlag(GroupItem.FLAG_HOMESCREENGROUP, true);
        rootGroup().add(groupB);

        Group groupC = new Group();
        groupC.setLabel("Games");
        groupC.setOverridePosition(2);
        groupC.setFlag(GroupItem.FLAG_GAMEGROUP, true);
        rootGroup().add(groupC);

        Group groupD = new Group();
        groupD.setLabel("Hidden");
        groupD.setOverridePosition(999999999);
        groupD.setFlag(GroupItem.FLAG_HIDDEN, true);
        rootGroup().add(groupD);
    }

    public void setRootGroup(Group root)
    {
        mRootGroup = root;
    }

    public Group rootGroup()
    {
        return mRootGroup;
    }

    void createPackageList()
    {
        Group appGroup = rootGroup().appGroup();

        List<ResolveInfo> activities = launchActivities();
        for (int i = 0; i < activities.size(); ++i)
        {
            ActivityInfo myActivity = activities.get(i).activityInfo;
            Application app = new Application(myActivity);
            appGroup.add(app);
        }

        appGroup.update();
    }

    ActivityInfo activityInfo(ComponentName component, int flags)
    {
        ActivityInfo activityInfo = new ActivityInfo();
        try
        {
            return packageManager().getActivityInfo(component, flags);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return activityInfo;
    }

    String applicationLabel(ComponentName component)
    {
        ActivityInfo activityInfo = activityInfo(component, 0);
        if (activityInfo.loadLabel(packageManager()) != null)
            return activityInfo.loadLabel(packageManager()).toString();

        return "";
    }

    Drawable applicationIcon(ComponentName component)
    {
        Drawable icon = null;
        try
        {
            icon = packageManager().getActivityIcon(component);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return icon;
    }

    Resources resourcesForApplication(ComponentName component)
    {
        Resources res = null;
        try
        {
            res = packageManager().getResourcesForActivity(component);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return res;
    }

    Resources resourcesForApplication(String packageName)
    {
        Resources res = null;
        try
        {

            res = packageManager().getResourcesForApplication(packageName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return res;
    }

    List<ResolveInfo> launchActivities()
    {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return packageManager().queryIntentActivities(mainIntent, 0);
    }

    // get a list of launchable activityies/classes from a package
    public List<ResolveInfo> launchActivities(String packageName) // NOTE: a package may have multiple launch able class names within it
    {
        List<ResolveInfo> activities = new ArrayList<ResolveInfo>();
        List<ResolveInfo> launchables = launchActivities();

        // loop over all launchable packages
        for (ResolveInfo activity : launchables)
        {
            if (packageName.equalsIgnoreCase(activity.activityInfo.packageName))
            {
                activities.add(activity);
            }
        }

        return activities;
    }

    /*
    public void addPackage(String packageName)
    {
        Log.i("GrenadeLauncher", "Installing " + packageName);
        // list of launchables from this package (we may have multiple)
        List<ResolveInfo> launchActivities = PackageMgr.instance().launchActivities(packageName);

        // list of entries form the database which has this package name (we may have multiple)
        List<GroupItem> groupItems = JsonMgr.instance().groupItemsFromFile(packageName);

        for (int i = 0; i < launchActivities.size(); ++i)
        {
            ActivityInfo myActivity = launchActivities.get(i).activityInfo;
            boolean found = false;

            for (int x = 0; x < groupItems.size(); ++x)
            {
                GroupItem item = groupItems.get(x);

                // match class names and package names, we have a match, assign that item, if not put it in the apps group!
                if (item.className().equals(myActivity.name))
                {
                    // MATCH!
                    // remove uninstalled flag and add back in to parent
                    item.setFlag(GroupItem.FLAG_UNINSTALLED, false);
                    item.parent().add(item);

                    item.writeToFile();
                    item.parent().update();

                    found = true;
                    break;
                }
            }

            // myActivity has no entry in the database, so make a new application for it
            // add it to the app group and save
            if (!found)
            {
                Group appGroup = PackageMgr.instance().rootGroup().appGroup();
                Application app = new Application(myActivity);

                appGroup.add(app);
                app.writeToFile();
                appGroup.update();
            }
        }
    }

    public void removePackage(String packageName)
    {
        Log.i("GrenadeLauncher", "Uninstalling " + packageName);
        rootGroup().acceptVisitor(new GroupItemMgr.RemovePackageVisitor(packageName));
    }
*/

    public void updatePackageList()
    {
        Log.i("GrenadeLauncher", "Updating package list");

        // list of all launchables from android
        List<ResolveInfo> launchActivities = launchActivities();

        // List of Application groupItems (in memory)
        GroupItemMgr.FindApplications find = new GroupItemMgr.FindApplications();
        rootGroup().acceptVisitor(find);
        List<Application> applicationItems = find.foundGroupItems();

        // this compares the 2 lists
        // it will remove common matches from both lists
        // leaving only the unique entries in both lists
        Log.i("GrenadeLauncher", "Android Packages: " + launchActivities.size() + " | Launcher Packages: " + applicationItems.size());

        // something in this loop is failing to cause double apps
        for (int i = 0; i < launchActivities.size(); ++i)
        {
            ActivityInfo myActivity = launchActivities.get(i).activityInfo; // the activity from android

            for (int x = 0; x < applicationItems.size(); ++x) // loop over the json files
            {
                Application app = applicationItems.get(x);

                // match class names and package names, we have a match, assign that item, if not put it in the apps group!
                // MATCH! found, we need to check the package name too! Here is our bug!
                if (app.className().equalsIgnoreCase(myActivity.name) && app.packageName().equalsIgnoreCase(myActivity.packageName))
                {
                    //Log.i("GrenadeLauncher", "Found: " + app.label() + " | " + app.packageName() + " | " + app.className() + " | " + app.id());
                    applicationItems.remove(x);
                    launchActivities.remove(i);
                    --i;
                    --x;
                    break;
                }
            }
        }

        // remove old apps
        for (int i = 0; i < applicationItems.size(); ++i)
        {
            Application item = applicationItems.get(i);
            Log.i("GrenadeLauncher", "Removing: " + item.label() + " | " + item.packageName()+ " | " + item.className()+ " | " + item.id());
            item.setFlag(GroupItem.FLAG_UNINSTALLED, true);
            item.writeToFile();
            Group parent = item.parent();
            parent.remove(item);
            parent.update();
        }

        if (launchActivities.size() == 0)
            return;

        // list all uninstalled apps from json files
        List<Application> uninstalledApplicationItems = JsonMgr.instance().UninstalledApplicationsFromFile();

        // install new apps
        for (int i = 0; i < launchActivities.size(); ++i)
        {
            // create a new application from the android activity
            Application app = new Application(launchActivities.get(i).activityInfo);
            boolean added = false;

            for (int x = 0; x < uninstalledApplicationItems.size(); ++x)
            {
                Application uninstalledApp = uninstalledApplicationItems.get(x);
                if (app.className().equals(uninstalledApp.className()) && app.packageName().equals(uninstalledApp.packageName()))
                {
                    // we have found a previously installed app
                    Log.i("GrenadeLauncher", "Re-Adding: " + app.label() + " | " + app.packageName() + " | " + app.className()+ " | " + app.id());
                    uninstalledApp.setFlag(GroupItem.FLAG_UNINSTALLED, false);
                    uninstalledApp.parent().add(uninstalledApp);
                    uninstalledApp.writeToFile();
                    uninstalledApp.update();
                    added = true;
                    break;
                }
            }

            // if we dont have matching classname and packagename in any json files put in the app group
            if (!added)
            {
                Log.i("GrenadeLauncher", "Adding: " + app.label() + " | " + app.packageName()+ " | " + app.className());
                rootGroup().appGroup().add(app);
                app.writeToFile();
                rootGroup().appGroup().update();
            }
        }

        Log.i("GrenadeLauncher", "Finished updating package list");
    }

    // this gets called on package removed and on adding, so updating an app calls this twice
    @Override
    public void onPackageChangeListener(Intent intent)
    {
        Log.i("GrenadeLauncher", "PackageChangeListener Intent Action:" + intent.getAction());
        // change this to just scan for all changes
        updatePackageList();


/*
        if (intentAction.equals(Intent.ACTION_PACKAGE_ADDED)) // new package
        {
            String intentData = intent.getData().getSchemeSpecificPart();
            //addPackage(intentData);
        }
        else if (intentAction.equals(Intent.ACTION_PACKAGE_REMOVED)) // removed
        {
            //String intentData = intent.getData().getSchemeSpecificPart();
            //removePackage(intentData);
        }
        else if (intentAction.equals(Intent.ACTION_PACKAGE_CHANGED) || intentAction.equals(Intent.ACTION_PACKAGE_REPLACED))
        {
            String intentData = intent.getData().getSchemeSpecificPart();
            Log.i("GrenadeLauncher", "onPackageChangeListener Error:" + intentData);
        }
*/
    }

    @Override
    public void onSettingsChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals("key_icon_size")) // icon has changed, we need to update the icon cache (or we should do this on the fly??)
        {
            rootGroup().acceptVisitor(new GroupItemMgr.DeleteIconVisitor());
        }
    }
/*
    public String marketCategory(String packageName)
    {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String line = "";
        String url = "https://play.google.com/store/apps/details?id=" + packageName;

        try
        {
            HttpGet method = new HttpGet(new URI(url));
            HttpResponse response = httpClient.execute(method); // crapping out here

            HttpEntity ht = response.getEntity();
            BufferedHttpEntity buf = new BufferedHttpEntity(ht);
            InputStream is = buf.getContent();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            StringBuilder total = new StringBuilder();
            while ((line = r.readLine()) != null)
            {
                total.append(line + "\n");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return line;
    }
*/
}

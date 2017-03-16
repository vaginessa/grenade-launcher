package bitshift.grenadelauncher;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Bronson on 24/06/13.
 */
public class JsonMgr
{
    private static JsonMgr mSingleInstance;

    public static String BASEDIR;

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PACKAGENAME = "packageName";
    public static final String COLUMN_CLASSNAME = "className";
    public static final String COLUMN_ICONOVERRIDE = "iconOverride";
    public static final String COLUMN_ICON = "icon";
    public static final String COLUMN_LABEL = "label";
    public static final String COLUMN_OVERRIDEPOSITION = "overridePosition";
    public static final String COLUMN_FLAGS = "flags";
    public static final String COLUMN_PARENT = "parentId";

    public static final String COLUMN_INTENTFLAGS = "intentFlags";
    public static final String COLUMN_INTENTACTION = "intentAction";
    public static final String COLUMN_INTENTDATA = "intentData";
    public static final String COLUMN_INTENTTYPE = "intentType";
    public static final String COLUMN_INTENTCATEGORIES = "intentCategories";
    public static final String COLUMN_INTENTEXTRABUNDLE = "intentExtraBundle";


    JsonMgr(Context context)
    {
        mSingleInstance = this;
        BASEDIR = context.getFilesDir().toString() + "/";

        boolean rootExists = loadRoot();

        if (!rootExists)
        {
            Log.i("GrenadeLauncher", "No previous groups found");
            PackageMgr.instance().createDefaultGroups();
            PackageMgr.instance().createPackageList();
            PackageMgr.instance().rootGroup().acceptVisitor(new GroupItemMgr.WriteToFileClassVisitor());
        }
        else
        {
            Log.i("GrenadeLauncher", "Loading groups");
            loadGroups();
            loadApplications();
            loadShortcuts();
        }

        PackageMgr.instance().rootGroup().acceptVisitor(new GroupItemMgr.UpdateVisitor());
    }

    public static JsonMgr instance()
    {
        return mSingleInstance;
    }

    public void writeJson(JSONObject obj, String path)
    {
        FileWriter file = null;
        try
        {
            file = new FileWriter(path);
            file.write(obj.toString());
            file.flush();
            file.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void writeGroup(Group item)
    {
        try
        {
            JSONObject object = new JSONObject();
            if (item.parent() != null)
                object.put(COLUMN_PARENT, String.valueOf(item.parent().id()));
            object.put(COLUMN_LABEL, item.label());
            object.put(COLUMN_FLAGS, Long.toString(item.flags()));
            object.put(COLUMN_OVERRIDEPOSITION, Long.toString(item.overridePosition()));
            object.put(COLUMN_ICONOVERRIDE, Boolean.toString(item.iconOverride()));
            String path = BASEDIR + "data/group/" + String.valueOf(item.id());
            writeJson(object, path);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void writeApplication(Application item)
    {
        try
        {
            JSONObject object = new JSONObject();
            object.put(COLUMN_PARENT, String.valueOf(item.parent().id()));
            object.put(COLUMN_LABEL, item.label());
            object.put(COLUMN_FLAGS, Long.toString(item.flags()));
            object.put(COLUMN_OVERRIDEPOSITION, Long.toString(item.overridePosition()));
            object.put(COLUMN_ICONOVERRIDE, Boolean.toString(item.iconOverride()));
            object.put(COLUMN_PACKAGENAME, item.packageName());
            object.put(COLUMN_CLASSNAME, item.className());
            String path = BASEDIR + "data/application/" + String.valueOf(item.id());
            writeJson(object, path);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void writeShortcut(Shortcut item)
    {
        try
        {
            JSONObject object = new JSONObject();

            object.put(COLUMN_PARENT, String.valueOf(item.parent().id()));
            object.put(COLUMN_LABEL, item.label());
            object.put(COLUMN_FLAGS, Long.toString(item.flags()));
            object.put(COLUMN_OVERRIDEPOSITION, Long.toString(item.overridePosition()));
            object.put(COLUMN_ICONOVERRIDE, Boolean.toString(item.iconOverride()));
            object.put(COLUMN_PACKAGENAME, item.packageName());
            object.put(COLUMN_CLASSNAME, item.className());
            object.put(COLUMN_INTENTFLAGS, String.valueOf(item.intentFlags()));
            object.put(COLUMN_INTENTACTION, item.intentAction());
            object.put(COLUMN_INTENTDATA, item.intentDataString());
            object.put(COLUMN_INTENTTYPE, item.intentType());
            object.put(COLUMN_INTENTCATEGORIES, item.intentCategoriesString());
            object.put(COLUMN_INTENTEXTRABUNDLE, item.extraBundleString());

            String path = BASEDIR + "data/shortcut/" + String.valueOf(item.id());
            writeJson(object, path);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void deleteItemFile(GroupItem item)
    {
        String id = String.valueOf(item.id());
        String path = "";

        if (item instanceof Group)
            path = BASEDIR + "data/group/" + id;

        if (item instanceof Application)
            path = BASEDIR + "data/application/" + id;

        if (item instanceof Shortcut)
            path = BASEDIR + "data/shortcut/" + id;

        File f = new File(path);

        long age = System.currentTimeMillis() - f.lastModified();
        long week = 604800000; // 7 days in ms
        if (age < week)
            return;

        Log.i("GrenadeLauncher", "Delete JSON:" + item.packageName());
        f.delete();

        // remove override icon if one exists
        item.deleteIcon();
    }

    public List<File> getFileList(String path)
    {
        List<File> list = new ArrayList<File>();

        File[] files = new File(path).listFiles();
        for (File f : files)
        {
            if (!f.isDirectory() && f.exists())
                list.add(f);
        }
        return list;
    }

    private void loadApplications()
    {
        List<File> appList = getFileList(BASEDIR + "data/application/");
        for (File f : appList)
        {
            Application item = readApplication(UUID.fromString(f.getName()));

            if (item.testFlag(GroupItem.FLAG_UNINSTALLED))
            {
                deleteItemFile(item);
            }
            else
                item.parent().add(item);
        }
    }

    public Application readApplication(UUID id)
    {
        Application item = new Application(id);
        String json = loadFile(BASEDIR + "data/application/" + String.valueOf(id));
        JSONObject object = new JSONObject();
        try
        {
            object = new JSONObject(json);

            item.setPackageName(object.getString(COLUMN_PACKAGENAME));
            item.setClassName(object.getString(COLUMN_CLASSNAME));
            item.setLabel(object.getString(COLUMN_LABEL));
            item.setFlags(object.getLong(COLUMN_FLAGS));
            item.setOverridePosition(object.getLong(COLUMN_OVERRIDEPOSITION));
            item.setIconOverride(object.getBoolean(COLUMN_ICONOVERRIDE));

            GroupItemMgr.FindGroupByIdVisitor find = new GroupItemMgr.FindGroupByIdVisitor(UUID.fromString(object.getString(COLUMN_PARENT)));
            PackageMgr.instance().rootGroup().acceptVisitor(find);
            Group parentGroup = find.foundGroup();
            item.setParent(parentGroup);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return item;
    }

    private void loadShortcuts()
    {
        List<File> shortcutList = getFileList(BASEDIR + "data/shortcut/");
        for (File f : shortcutList)
        {
            Shortcut item = readShortcut(UUID.fromString(f.getName()));

            if (item.testFlag(GroupItem.FLAG_UNINSTALLED))
            {
                deleteItemFile(item);
            }
            else
            {
                // Crash here... why?!
                item.parent().add(item);
            }
        }
    }


    public Shortcut readShortcut(UUID id)
    {
        Shortcut item = new Shortcut(id);
        String json = loadFile(BASEDIR + "data/shortcut/" + String.valueOf(id));
        JSONObject object = new JSONObject();
        try
        {
            object = new JSONObject(json);

            item.setLabel(object.getString(COLUMN_LABEL));
            item.setFlags(object.getLong(COLUMN_FLAGS));
            item.setOverridePosition(object.getLong(COLUMN_OVERRIDEPOSITION));
            item.setIconOverride(object.getBoolean(COLUMN_ICONOVERRIDE));

            GroupItemMgr.FindGroupByIdVisitor find = new GroupItemMgr.FindGroupByIdVisitor(UUID.fromString(object.getString(COLUMN_PARENT)));
            PackageMgr.instance().rootGroup().acceptVisitor(find);
            Group parentGroup = find.foundGroup();
            item.setParent(parentGroup);

            // extra stuff
            if (object.has(COLUMN_INTENTFLAGS))
                item.setIntentFlags(object.getInt(COLUMN_INTENTFLAGS));

            if (object.has(COLUMN_INTENTACTION))
                item.setIntentAction(object.getString(COLUMN_INTENTACTION));

            if (object.has(COLUMN_INTENTDATA))
                item.setIntentData(Uri.parse(object.getString(COLUMN_INTENTDATA)));

            if (object.has(COLUMN_INTENTTYPE))
                item.setIntentType(object.getString(COLUMN_INTENTTYPE));

            if (object.has(COLUMN_INTENTCATEGORIES))
                item.setIntentCategories(object.getString(COLUMN_INTENTCATEGORIES));

            if (object.has(COLUMN_INTENTEXTRABUNDLE))
                item.setExtraBundle(object.getString(COLUMN_INTENTEXTRABUNDLE));

            if (object.has(COLUMN_PACKAGENAME))
                item.setPackageName(object.getString(COLUMN_PACKAGENAME));

            if (object.has(COLUMN_CLASSNAME))
                item.setClassName(object.getString(COLUMN_CLASSNAME));


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return item;
    }

    private boolean loadRoot()
    {
        String path = BASEDIR + "data/group/";

        List<File> groupList = getFileList(path);
        for (File f : groupList)
        {
            Group item = readGroup(UUID.fromString(f.getName()));
            if (item.testFlag(GroupItem.FLAG_ROOTGROUP))
            {
                PackageMgr.instance().setRootGroup(item);
                return true;
            }
        }

        return false;
    }

    private void loadGroups()
    {
        List<File> groupList = getFileList(BASEDIR + "data/group/");
        for (File f : groupList)
        {
            Group item = readGroup(UUID.fromString(f.getName()));
            if (!item.testFlag(GroupItem.FLAG_ROOTGROUP)) // dont load root here
            {
                item.parent().add(item); // add to its parent here
            }
        }
    }

    public Group readGroup(UUID id)
    {
        Group item = new Group(id);
        String json = loadFile(BASEDIR + "data/group/" + String.valueOf(id));
        JSONObject object = new JSONObject();
        try
        {
            object = new JSONObject(json);

            item.setLabel(object.getString(COLUMN_LABEL));
            item.setFlags(object.getLong(COLUMN_FLAGS));
            item.setOverridePosition(object.getLong(COLUMN_OVERRIDEPOSITION));
            item.setIconOverride(object.getBoolean(COLUMN_ICONOVERRIDE));

            if (object.has(COLUMN_PARENT) && !object.getString(COLUMN_PARENT).equals("")) // root has no parent
            {
                GroupItemMgr.FindGroupByIdVisitor find = new GroupItemMgr.FindGroupByIdVisitor(UUID.fromString(object.getString(COLUMN_PARENT)));
                PackageMgr.instance().rootGroup().acceptVisitor(find);
                Group parentGroup = find.foundGroup();
                item.setParent(parentGroup);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return item;
    }

    public String loadFile(String path)
    {
        String json = null;
        try
        {
            InputStream is = new BufferedInputStream(new FileInputStream(path));
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    // get only the apps that are flagged as uninstalled
    public List<Application> UninstalledApplicationsFromFile()
    {
        List<Application> items = new ArrayList<Application>();

        List<File> appList = getFileList(BASEDIR + "data/application/");
        for (File f : appList)
        {
            Application item = readApplication(UUID.fromString(f.getName()));
            if (item.testFlag(GroupItem.FLAG_UNINSTALLED))
                items.add(item);
        }

        return items;
    }

    /*
    // return a list of apps and shortcuts which have this packagename in them
    public List<GroupItem> groupItemsFromFile(String packageName)
    {
        List<GroupItem> groupItems = new ArrayList<GroupItem>();

        List<File> appList = getFileList(BASEDIR + "data/application/");
        for (File f : appList)
        {
            Application item = readApplication(UUID.fromString(f.getName()));
            String itemPackageName = item.packageName();
            if (packageName.equals(itemPackageName))
            {
                groupItems.add(item);
            }
        }

        // TODO: shortcut loading, finish this...
        List<File> shortcutList = getFileList(BASEDIR + "data/shortcut/");
        for (File f : shortcutList)
        {/*
            Shortcut item = readShortcut(UUID.fromString(f.getName()));
            String itemPackageName = item.packageName();
            if (packageName.equals(itemPackageName))
            {
                groupItems.add(item);
            }* /
        }

        return groupItems;
    }
*/

}

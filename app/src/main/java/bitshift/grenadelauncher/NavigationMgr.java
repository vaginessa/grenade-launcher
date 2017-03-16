package bitshift.grenadelauncher;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bronson on 14/11/13.
 */
public class NavigationMgr
{
    static private NavigationMgr mSingleInstance;

    public NavigationMgr()
    {
        mSingleInstance = this;
        createMenuList();
    }

    public static NavigationMgr instance()
    {
        return mSingleInstance;
    }

    List<NavigationItem> mMenuList = new ArrayList<NavigationItem>();

    public List<NavigationItem> menuList()
    {
        return mMenuList;
    }

    public void createMenuList()
    {
        // add id's to values/ids.xml
        addMenuItem(R.id.menu_add_shortcut, R.string.menu_add_shortcut, R.drawable.ic_launcher);
        addMenuItem(R.id.menu_group_editor, R.string.menu_group_editor, R.drawable.ic_launcher);
        addMenuItem(R.id.menu_toggle_hidden, R.string.menu_toggle_hidden, R.drawable.ic_launcher);
        addMenuItem(R.id.menu_select_wallpaper, R.string.menu_select_wallpaper, R.drawable.ic_launcher);
        addMenuItem(R.id.menu_launcher_settings, R.string.menu_launcher_settings, R.drawable.ic_launcher);
    }

    // history here
    public void createAltMenuList()
    {

    }

    public void addMenuItem(int id, int label, int icon)
    {
        NavigationItem item = new NavigationItem(id, label, icon);
        mMenuList.add(item);
    }

    public void addMenuItem(int id, int label, int icon, NavigationItem.ItemType type)
    {
        NavigationItem item = new NavigationItem(id, label, icon, type);
        mMenuList.add(item);
    }

    public void addMenuItem()
    {
        NavigationItem item = new NavigationItem();
        mMenuList.add(item);
    }
}

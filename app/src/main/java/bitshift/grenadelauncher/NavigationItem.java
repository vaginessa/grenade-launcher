package bitshift.grenadelauncher;


/**
 * Created by Bronson on 14/11/13.
 * contains the menu item, icon, and intent
 * read this from the menu/activity_main.xml??
 */
public class NavigationItem
{
    public int mId;
    public int mLabel;
    public int mIcon;
    public ItemType mItemType = ItemType.Item;
    //public String mIntent;
    enum ItemType
    {
        Item, // clickable
        Heading,
        Blank,
    }

    NavigationItem(int id, int label, int icon, ItemType type)
    {
        mId = id;
        mLabel = label;
        mIcon = icon;
        mItemType = type;
        //mIntent = intent;
    }

    NavigationItem(int id, int label, int icon)
    {
        mId = id;
        mLabel = label;
        mIcon = icon;
        //mIntent = intent;
    }

    // blank item
    NavigationItem()
    {
        mItemType = ItemType.Blank;
    }

    public int Label()
    {
        return mLabel;
    }

    public int Id()
    {
        return mId;
    }

    public int Icon()
    {
        return mIcon;
    }
}

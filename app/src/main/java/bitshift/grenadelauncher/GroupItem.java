package bitshift.grenadelauncher;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

public class GroupItem //an empty shell, like fab - he says no code is good code!
{
    public static interface Visitor
    {
        public void visit(GroupItem item);
    }

    public enum Type
    {
        Group,
        Application,
        Shortcut,
        Widget
    }

    public static final int FLAG_NONE = 0;
    public static final int FLAG_HIDDEN = 0x1 << 0;
    public static final int FLAG_VISIBLE = 0x1 << 1;
    public static final int FLAG_ALL = GroupItem.FLAG_VISIBLE | GroupItem.FLAG_HIDDEN;
    public static final int FLAG_UNINSTALLED = 0x1 << 2;
    public static final int FLAG_APPGROUP = 0x1 << 3;
    public static final int FLAG_GAMEGROUP = 0x1 << 4;
    public static final int FLAG_HOMESCREENGROUP = 0x1 << 5;
    public static final int FLAG_ROOTGROUP = 0x1 << 6;

    private UUID mId;
	private String mLabel = "";
	private Group mParent;
	private Bitmap mIcon;
    private boolean mIconOverride = false; // use this if we have an icon in the database!
	private long mOverridePosition = -1;
    private long mFlags = FLAG_VISIBLE;
	private String mClassName;
	private String mPackageName;


    public GroupItem()
    {
        setId();
    }

    public GroupItem(UUID id)
    {
        setId(id);
    }

	public GroupItem(String packageName, String className)
	{
        setId();
        setPackageName(packageName);
        setClassName(className);
	}

    public GroupItem(String packageName, String className, String label)
    {
        setId();
        setPackageName(packageName);
        setClassName(className);
        setLabel(label);
    }

    public boolean testFlag(int flag)
    {
        return (mFlags & flag) != 0;
    }

    public long flags()
    {
        return mFlags;
    }

    public void setFlags(long flags)
    {
        mFlags = flags;
    }

    public void setFlag(int flag, boolean enable)
    {
        // visible hack - as these are mutually exclusive
        if ((flag & FLAG_HIDDEN) != 0)
        {
            mFlags &= ~FLAG_VISIBLE;
        }
        else if ((flag & FLAG_VISIBLE) != 0)
        {
            mFlags &= ~FLAG_HIDDEN;
        }

        if (enable)
            mFlags |= flag;
        else
            mFlags &= ~flag;
    }

    protected void setId(UUID id)
    {
        mId = id;
    }

    protected void setPackageName(String packageName)
    {
        if (packageName == null || mPackageName != null)
            return;

        mPackageName = packageName;
    }

    protected void setClassName(String className)
    {
        mClassName = className;
    }

    void setLabel()
    {
        if (label() == null || label().equals(""))
        {
            String label = PackageMgr.instance().applicationLabel(componentName());
            setLabel(label);
        }
    }

    ComponentName componentName()
    {
        ComponentName component = null;

        if (packageName() != null && className() != null)
            component = new ComponentName(packageName(), className());

        return component;
    }

	void setLabel(String string)
	{
		mLabel = string;
	}

    public boolean iconOverride()
    {
        return mIconOverride;
    }

    public void setIconOverride(boolean bool)
    {
        mIconOverride = bool;
    }

    // reads icon from file
    public Bitmap overrideIcon()
    {
        String fName = MainActivity.BASEDIR + "res/" + id().toString() + ".png";
        return BitmapFactory.decodeFile(fName);
    }

    // saves a bitmap
    public void outputIconOverride(Bitmap bitmap)
    {
        String fName = MainActivity.BASEDIR + "res/" + id().toString() + ".png";

        try
        {
            FileOutputStream output = new FileOutputStream(fName);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // as above, but converts drawable to bitmap first
    public void outputIconOverride(Drawable drawable)
    {
        if (drawable == null)
            return;

        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        outputIconOverride(bitmap);
    }

    Bitmap icon()
    {
        return mIcon;
    }

    Bitmap createIcon()
    {
        return icon();
    }

    void deleteIcon()
    {
        if (mIcon == null)
            return;

        mIcon.recycle();
        mIcon = null;

        String iconPath = MainActivity.BASEDIR + "data/res/" + id().toString();
        File f = new File(iconPath);
        if (f.exists())
            f.delete();
    }

    void setIcon(Bitmap bitmap)
    {
        if (bitmap == null)
            return;

        mIcon = bitmap;
    }

	void setIcon(Drawable drawable)
	{
        if (drawable == null)
            return;

    	Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        setIcon(bitmap);
	}

    void setParent(Group group)
    {
        mParent = group;
    }

	String label()
	{
		return mLabel;
	}
	
	String className()
	{
		return mClassName;
	}

	Group parent()
	{
		return mParent;
	}
	
	String packageName()
	{
		return mPackageName;
	}	

	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
	{
	}
	
	public void onItemLongClick(AdapterView<?> adapterView, View view, int position, long id)	
	{
	}
	
	public void update()
	{
		if (mParent != null)
			mParent.update();
	}

    public UUID id()
    {
        return mId;
    }

    public void setId()
    {
        UUID uid = UUID.randomUUID();
        setId(uid);
    }

	void setOverridePosition(long id)
	{
		mOverridePosition = id;
	}
	
	Long overridePosition()
	{
		return mOverridePosition;
	}
	
	public Group appGroup()
	{
        Group root = PackageMgr.instance().rootGroup();
        return (Group) root.childList(GroupItem.FLAG_APPGROUP).get(0);
	}

    public Group gameGroup()
    {
        Group root = PackageMgr.instance().rootGroup();
        return (Group) root.childList(GroupItem.FLAG_GAMEGROUP).get(0);
    }

    public Group homeGroup()
    {
        Group root = PackageMgr.instance().rootGroup();
        return (Group) root.childList(GroupItem.FLAG_HOMESCREENGROUP).get(0);
    }

    public void acceptVisitor(Visitor visitor)
    {
        visitor.visit(this);
    }

    public void writeToFile()
    {
    }

    public void deleteFile()
    {
        JsonMgr.instance().deleteItemFile(this);
    }

    public void delete()
    {
        setFlag(GroupItem.FLAG_UNINSTALLED, true);
        writeToFile();
        Group parent = parent();
        parent().remove(this);
        parent.update();
    }


}

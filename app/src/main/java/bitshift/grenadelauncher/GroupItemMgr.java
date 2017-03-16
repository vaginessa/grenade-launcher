package bitshift.grenadelauncher;

import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Bronson on 10/06/13.
 */
public class GroupItemMgr
{
    static private GroupItemMgr mSingleInstance;

    GroupItemMgr()
    {
        mSingleInstance = this;
    }

    static GroupItemMgr instance()
    {
        return mSingleInstance;
    }

    public static class UpdateVisitor implements GroupItem.Visitor
    {
        public void visit(GroupItem item)
        {
            if (item instanceof Group)
                item.update();
        }
    }

    public static class DeleteIconVisitor implements GroupItem.Visitor
    {
        public void visit(GroupItem item)
        {
            item.deleteIcon();
        }
    }

    public static class DeleteHiddenIconVisitor implements GroupItem.Visitor
    {
        public void visit(GroupItem item)
        {
            if (item.parent() != null && item.parent().testFlag(GroupItem.FLAG_HIDDEN))
                item.deleteIcon();
        }
    }

    public static class FindGroupByIdVisitor implements GroupItem.Visitor
    {
        private UUID mId;
        private Group mFoundGroup;

        FindGroupByIdVisitor(UUID id)
        {
            mId = id;
        }

        public void visit(GroupItem item)
        {
            if (item instanceof Group)
            {
                if (item.id().equals(mId))
                    mFoundGroup = (Group) item;
            }
        }

        public Group foundGroup()
        {
            return mFoundGroup;
        }
    }

    public static class FindApplications implements GroupItem.Visitor
    {
        private List<Application> mFoundList = new ArrayList<Application>();

        public void visit(GroupItem item)
        {
            if (item instanceof Application)
                mFoundList.add((Application)item);
        }

        public List<Application> foundGroupItems()
        {
            return mFoundList;
        }
    }

    public static class RemovePackageVisitor implements GroupItem.Visitor
    {
        String mPackageName;

        RemovePackageVisitor(String packageName)
        {
            mPackageName = packageName;
        }

        public void visit(GroupItem item) // the item we are visiting calls this
        {
            if (mPackageName.equals(item.packageName()))
            {
                // set uninstalled
                item.setFlag(GroupItem.FLAG_UNINSTALLED, true);
                item.writeToFile();
                item.deleteIcon();

                Group parent = item.parent();
                parent.remove(item);
                parent.update();
            }
        }
    }

    public static class WriteToFileClassVisitor implements GroupItem.Visitor
    {
        public void visit(GroupItem item)
        {
            item.writeToFile();
        }
    }
}

package bitshift.grenadelauncher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class Group extends GroupItem
{
	private List<GroupItem> mPackageList = new ArrayList<GroupItem>();
	private ArrayList<GroupChangeListener> mListeners = new ArrayList<GroupChangeListener> ();

    enum GroupVisible
    {
        All,
        Visible,
        Hidden,
    }

    public Group(UUID id)
    {
        super(id);
    }

    public Group()
    {
        super();
    }

	// custom listener interface (interface is a group of related methods with empty bodies)
	public interface GroupChangeListener
	{
		void onGroupChangeListener(); // empty shell, this gets overriden by mainactivity
	}
	
	public void addGroupChangeListener(GroupChangeListener listener)
	{
		for(int i = 0; i < mListeners.size(); ++i)
			if (mListeners.get(i).equals(listener));
				mListeners.remove(listener);
				
		mListeners.add(listener);
	}
	
	public void removeGroupChangeListener(GroupChangeListener listener)
	{
		for(int i = 0; i < mListeners.size(); ++i)
			if (mListeners.get(i).equals(listener));
				mListeners.remove(listener);
	}	
	

	void sortByLabel()
	{
		Comparator<GroupItem> sort = new Comparator<GroupItem>() 
		{
			public int compare(GroupItem u1, GroupItem u2) 
			{
				return u1.label().toLowerCase().compareTo(u2.label().toLowerCase());
			}
	    } ;
	    
        Collections.sort(mPackageList, sort);
	}
	
	void sortBySortOrder()
	{
		Comparator<GroupItem> sort = new Comparator<GroupItem>() 
		{
			public int compare(GroupItem u1, GroupItem u2) 
			{
				return (int) Math.signum(u1.overridePosition() - u2.overridePosition());
			}
        } ;
	    
        Collections.sort(mPackageList, sort);

        List<GroupItem> childList = childList(GroupItem.FLAG_VISIBLE);
	}

	public void update()
	{
        // DONT SORT DA ROOT
		if (id() != PackageMgr.instance().rootGroup().id())
		{
			sortByLabel();
			super.update();
		}
		else // root group - update listeners
		{
            sortBySortOrder();
			for (GroupChangeListener listener : mListeners)
			    listener.onGroupChangeListener();
		}
	}

	public void add(GroupItem item)
	{
		item.setParent(this);
		mPackageList.add(item);
    }
	
	public void remove(GroupItem item)
	{
		mPackageList.remove(item);
	}


    public List<GroupItem> childList(int flags)
	{
        List<GroupItem> list = new ArrayList<GroupItem>();
        for (GroupItem child : mPackageList)
        {
            if (child.testFlag(flags))
            {
                list.add(child);
            }
        }

        return list;
	}

    public List<Integer> childIndexList(int flags)
    {
        int i = 0;
        List<Integer> list = new ArrayList<Integer>();
        for (GroupItem child : mPackageList)
        {
            if (child.testFlag(flags))
            {
                list.add(i);
            }

            ++i;
        }

        return list;
    }

    public GroupItem set(int position, GroupItem item)
	{
		return mPackageList.set(position, item);
	}			

	public void removeListener(GroupEditorActivity groupActivity)
	{
		
	}

    public void acceptVisitor(GroupItem.Visitor visitor)
    {
        super.acceptVisitor(visitor);

        List<GroupItem> childList = childList(GroupItem.FLAG_VISIBLE | GroupItem.FLAG_HIDDEN);

        for (int i = 0; i < childList.size(); ++i)
        {
            childList.get(i).acceptVisitor(visitor);
        }
    }

    @Override
    public void writeToFile()
    {
        JsonMgr.instance().writeGroup(this);
    }


}

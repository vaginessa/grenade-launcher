package bitshift.grenadelauncher;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

import java.util.List;

public class SectionsPagerAdapter extends PagerAdapter implements OnItemClickListener, OnItemLongClickListener
{
	private List<GroupItem> mGroup;
	private SharedPreferences mPreferences; // for reading settings
    private Configuration mConfiguration;

	public SectionsPagerAdapter(Context context, List<GroupItem> group)
	{
		mGroup = group;
        mConfiguration = context.getResources().getConfiguration();
	}

	public List<GroupItem> group()
	{
		return mGroup;
	}
	
	@Override
	public int getCount() 
	{
		return mGroup.size();
	}

	@Override
	public CharSequence getPageTitle(int position) 
	{
		return mGroup.get(position).label();
	}

	// Determines whether a page View is associated with a specific key object as returned by instantiateItem(ViewGroup, int).
	@Override
	public boolean isViewFromObject(View view, Object object) 
	{
		return view.equals(object);
	}
	
    @Override
    public Object instantiateItem(ViewGroup collection, int position) 
    {
    	LayoutInflater inflater = (LayoutInflater) collection.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	View view = inflater.inflate(R.layout.pager_view, null);

    	int orientation = configuration().orientation;
		
		// grid view
    	GridView grid = (GridView)view.findViewById(R.id.gridView);
    	Group group = (Group) group().get(position);
		switch (orientation)
		{
        case Configuration.ORIENTATION_LANDSCAPE:
            grid.setNumColumns(SettingsMgr.instance().gridHeight());
            break;

        case Configuration.ORIENTATION_PORTRAIT:
            grid.setNumColumns(SettingsMgr.instance().gridWidth());
            break;
		}

		// set adapter & properties
        List<GroupItem> childList = group.childList(GroupItem.FLAG_VISIBLE);
    	GroupItemAdapter gridAdapter = new GroupItemAdapter(collection.getContext(), childList);

    	grid.setAdapter(gridAdapter);
    	grid.setOnItemClickListener(this); //click listener	
    	grid.setOnItemLongClickListener(this); //click listener	

    	collection.addView(view, 0);
        return view;
    }
    
	// Remove a page for the given position.
    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) 
    {
    	((ViewGroup) collection).removeView((View) view);
    	//mViews.remove(position);
    }	
    
    @Override
    public void finishUpdate(View arg0) {}


    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {}

    @Override
    public Parcelable saveState() 
    {
        return null;
    }

    @Override
    public void startUpdate(ViewGroup container) {}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) 
	{
        // proximity check
        if (!SensorMgr.instance().checkProximity())
            return;

        // load app
		GridView mGrid = (GridView) arg0.findViewById(R.id.gridView);
		GroupItem item = (GroupItem) mGrid.getAdapter().getItem(position);
		item.onItemClick(arg0, view, position, id);

		//setCurrentItem(0);	// reset to home?! ask fab how to get this.setCurItem
	}	
	
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long id) 
	{
        // proximity check
         if (!SensorMgr.instance().checkProximity())
            return true;

        // locked check
		if (SensorMgr.instance().checkLock())
			return true;

        // load
		GridView mGrid = (GridView) arg0.findViewById(R.id.gridView);
		GroupItem item = (GroupItem) mGrid.getAdapter().getItem(position);
		item.onItemLongClick(arg0, view, position, id);		
		return true;			
	}

    Configuration configuration()
    {
        return mConfiguration;
    }

}

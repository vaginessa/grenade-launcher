package bitshift.grenadelauncher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by Bronson on 14/11/13.
 */
public class NavigationItemAdapter extends ArrayAdapter<NavigationItem>
{

    public NavigationItemAdapter(Context context)
    {
        super(context, R.layout.navigation_item, NavigationMgr.instance().menuList()); // assigned are context, the resource id, and a list<resolveinfo>
    }

    @Override
    public int getCount()
    {
        return NavigationMgr.instance().menuList().size();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        NavigationItem item = getItem(position);
        if (item.mItemType == NavigationItem.ItemType.Heading)
        {
            view = inflater.inflate(R.layout.navigation_heading, null);

            TextView label = (TextView) view.findViewById(R.id.tv_menu_label);
            int labelId = item.Label();
            label.setText(getContext().getResources().getString(labelId));
        }
        else if (item.mItemType == NavigationItem.ItemType.Blank)
        {
            view = inflater.inflate(R.layout.navigation_blank, null);
        }
        else
        {
            view = inflater.inflate(R.layout.navigation_item, null);

            TextView label = (TextView) view.findViewById(R.id.tv_menu_label);
            int labelId = item.Label();
            label.setText(getContext().getResources().getString(labelId));

            //ImageView icon = (ImageView) view.findViewById(R.id.iv_menu_icon);
            //icon.setImageResource(item.Icon());
        }
        return view;
    }

    @Override
    public NavigationItem getItem(int position)
    {
        NavigationItem item = NavigationMgr.instance().menuList().get(position);
        return item;
    }

    @Override
    public long getItemId(int position)
    {
        long id = NavigationMgr.instance().menuList().get(position).Id();
        return id;
    }

}

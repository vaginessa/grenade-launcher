package bitshift.grenadelauncher;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GroupEditorAdapter extends ArrayAdapter<GroupItem>
{
    List<GroupItem> mGroupChildren;

	public GroupEditorAdapter(Context context, List<GroupItem> items) 
	{
		super(context, R.layout.group_row, items);
        mGroupChildren = items;
		// TODO Auto-generated constructor stub
	}
	
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) //pos final so we can access it
    {
		LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		convertView = inflater.inflate(R.layout.group_row, null);

  		final Group group = (Group)super.getItem(position); 
  		final Group root = PackageMgr.instance().rootGroup();
  		
	    TextView label = (TextView)convertView.findViewById(R.id.textLabel);
	    label.setText(group.label());

        ImageView imageHome = (ImageView)convertView.findViewById(R.id.image_home);
        ImageView imageApp = (ImageView)convertView.findViewById(R.id.image_app);
        ImageView imageGame = (ImageView)convertView.findViewById(R.id.image_game);

        if (!group.testFlag(GroupItem.FLAG_HOMESCREENGROUP))
        {
            imageHome.getLayoutParams().width = 0;
            imageHome.setVisibility(ImageView.INVISIBLE);
        }

        if (!group.testFlag(GroupItem.FLAG_APPGROUP))
        {
            imageApp.getLayoutParams().width = 0;
            imageApp.setVisibility(ImageView.INVISIBLE);
        }

        if (!group.testFlag(GroupItem.FLAG_GAMEGROUP))
        {
            imageGame.getLayoutParams().width = 0;
            imageGame.setVisibility(ImageView.INVISIBLE);
        }


        // put this in here so we can scab the position :)
		OnClickListener mClickListener = new OnClickListener() 
		{ 
			@Override
			public void onClick(View v) 
			{
				switch(v.getId())
				{
					case R.id.button_group_up:
					{
						if (position != 0)
						{
							Group groupA = (Group) mGroupChildren.get(position);
							Group groupB = (Group) mGroupChildren.get(position - 1);
							Long a = mGroupChildren.get(position).overridePosition();
							Long b = mGroupChildren.get(position - 1).overridePosition();
							groupA.setOverridePosition(b);
							groupB.setOverridePosition(a);

                            // write group changes
                            groupA.writeToFile();
                            groupB.writeToFile();
                            root.writeToFile();

                            root.update();
						}
						break;
					}
						
					case R.id.button_group_down:
					{
						int size = mGroupChildren.size() - 1;
						if (position < size)
						{
							Group groupA = (Group) mGroupChildren.get(position);
							Group groupB = (Group) mGroupChildren.get(position + 1);
							Long a = mGroupChildren.get(position).overridePosition();
							Long b = mGroupChildren.get(position + 1).overridePosition();
							groupA.setOverridePosition(b);
							groupB.setOverridePosition(a);

                            // write group changes
                            groupA.writeToFile();
                            groupB.writeToFile();
                            root.writeToFile();

                            root.update();
						}
						break;
					}

					default:
						throw new RuntimeException("Unknow button ID");
				}
			} 
		}; 
		
		// assign button listeners
		convertView.findViewById(R.id.button_group_up).setOnClickListener(mClickListener);
		convertView.findViewById(R.id.button_group_down).setOnClickListener(mClickListener);

	    return(convertView);
    }	
    
    

}

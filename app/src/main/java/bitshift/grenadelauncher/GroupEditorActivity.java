package bitshift.grenadelauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

public class GroupEditorActivity extends Activity implements Group.GroupChangeListener, OnItemClickListener
{
	ListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group);
		mListView = (ListView) findViewById(R.id.listView);

		PackageMgr.instance().rootGroup().addGroupChangeListener(this);

        setAdapter();
	}

    public void setAdapter()
    {
        GroupEditorAdapter adapter = new GroupEditorAdapter(getApplicationContext(), PackageMgr.instance().rootGroup().childList(GroupItem.FLAG_VISIBLE));
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this); // needs to be after the adapter is set
    }

	public void newGroup() 
	{
		Group newGroup = new Group();
		newGroup.setLabel("New");
        int op = (PackageMgr.instance().rootGroup().childList(GroupItem.FLAG_VISIBLE).size() + 1);
		newGroup.setOverridePosition(Long.valueOf(op));
        PackageMgr.instance().rootGroup().add(newGroup);

        // write changes groups
        newGroup.writeToFile();
        PackageMgr.instance().rootGroup().writeToFile();

        PackageMgr.instance().rootGroup().update();
	}

	@Override
	public void onPause()
	{
        PackageMgr.instance().rootGroup().removeGroupChangeListener(this);
		super.onPause();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
        PackageMgr.instance().rootGroup().addGroupChangeListener(this);
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_group, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) 
		{
			case R.id.menu_add_group:
				newGroup();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onGroupChangeListener() 
	{
        setAdapter();
	}

	// edit group
	@Override
	public void onItemClick(final AdapterView<?> adapterView, View view, int position, long id)
	{
		// inflate the builder
		final LayoutInflater inflater = LayoutInflater.from(adapterView.getContext());
		AlertDialog.Builder builder = new AlertDialog.Builder(adapterView.getContext()); // this has to be adapterview context dunno why?
		View dialogView = inflater.inflate(R.layout.dialog_group_edit, null);
		builder.setView(dialogView);
	
		// now populate the builder(final allows us to use it in the onclick methods
		final Group thisGroup = (Group) PackageMgr.instance().rootGroup().childList(GroupItem.FLAG_VISIBLE).get(position);
        final List<GroupItem> thisGroupChildren = ((Group) PackageMgr.instance().rootGroup().childList(GroupItem.FLAG_VISIBLE).get(position)).childList(GroupItem.FLAG_VISIBLE);
		final Group rootGroup = PackageMgr.instance().rootGroup();
				
		final EditText groupLabel = (EditText) dialogView.findViewById(R.id.text_group_label);
		groupLabel.setText(thisGroup.label());
		
		final CheckBox checkDelete = (CheckBox) dialogView.findViewById(R.id.check_delete);
        final CheckBox checkHome = (CheckBox) dialogView.findViewById(R.id.check_flag_home);
        final CheckBox checkApp = (CheckBox) dialogView.findViewById(R.id.check_flag_app);
        final CheckBox checkGame = (CheckBox) dialogView.findViewById(R.id.check_flag_game);

        if (thisGroup.testFlag(GroupItem.FLAG_HOMESCREENGROUP))
        {
            checkHome.setChecked(true);
            checkHome.setEnabled(false);
        }

        if (thisGroup.testFlag(GroupItem.FLAG_APPGROUP))
        {
            checkApp.setChecked(true);
            checkApp.setEnabled(false);
        }

        if (thisGroup.testFlag(GroupItem.FLAG_GAMEGROUP))
        {
            checkGame.setChecked(true);
            checkGame.setEnabled(false);
        }

	    builder.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() 
	    {
	        @Override
	        public void onClick(DialogInterface dialog, int id)
	        {
                boolean homeGroup = thisGroup.testFlag(GroupItem.FLAG_HOMESCREENGROUP);
                boolean appGroup = thisGroup.testFlag(GroupItem.FLAG_APPGROUP);
                boolean gameGroup = thisGroup.testFlag(GroupItem.FLAG_GAMEGROUP);
                Group parent = thisGroup.parent();

                Group currentHomeGroup = rootGroup.homeGroup();
                Group currentAppGroup = rootGroup.appGroup();
                Group currentGameGroup = rootGroup.gameGroup();


	        	if (checkDelete.isChecked() && !homeGroup && !appGroup && !gameGroup) // make sure it has no flags
	        	{
                    // move all items out of this group, and put them in the app group
                    for (GroupItem item : thisGroupChildren)
                    {
                        currentAppGroup.add(item);
                    }

                    // delete the group
                    parent.remove(thisGroup);
                    thisGroup.deleteFile();
	        	}
                else if (checkDelete.isChecked() && (!homeGroup || !appGroup || !gameGroup))  // has flags, warning box
                {
                    AlertDialog.Builder warningBuilder = new AlertDialog.Builder(adapterView.getContext());
                    warningBuilder.setMessage(R.string.dialog_delete_warning);
                    warningBuilder.setTitle(R.string.dialog_title_delete_warning);
                    warningBuilder.show();
                }
	        	else // update this group
	        	{
                     // check flags have been assigned ( remove old flag, set on new group)
                    if (checkHome.isChecked())
                    {
                        currentHomeGroup.setFlag(GroupItem.FLAG_HOMESCREENGROUP, false);
                        thisGroup.setFlag(GroupItem.FLAG_HOMESCREENGROUP, true);
                    }

                    if (checkApp.isChecked())
                    {
                        currentAppGroup.setFlag(GroupItem.FLAG_APPGROUP, false);
                        thisGroup.setFlag(GroupItem.FLAG_APPGROUP, true);
                    }

                    if (checkGame.isChecked())
                    {
                        currentGameGroup.setFlag(GroupItem.FLAG_GAMEGROUP, false);
                        thisGroup.setFlag(GroupItem.FLAG_GAMEGROUP, true);
                    }

                    thisGroup.setLabel(groupLabel.getText().toString());

                    // write potentially changed groups
                    thisGroup.writeToFile();
                    currentHomeGroup.writeToFile();
                    currentAppGroup.writeToFile();
                    currentGameGroup.writeToFile();
	        	}
                parent.update();
	        }
	    });
	    builder.setNegativeButton(R.string.cancel, null);
		builder.show();		
	}


}

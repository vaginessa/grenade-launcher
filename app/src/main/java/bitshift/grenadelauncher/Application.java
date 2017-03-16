package bitshift.grenadelauncher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

public class Application extends GroupItem
{
    public Application(UUID id)
    {
        super(id);
    }

    public Application(ActivityInfo activity)
    {
        super(activity.packageName, activity.name, activity.loadLabel(PackageMgr.instance().packageManager()).toString());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
    {
        try
        {
            ActivityInfo info = PackageMgr.instance().packageManager().getActivityInfo(componentName(), 0); // check we exist
            if (info != null)
            {
                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                //intent.addCategory(Intent.CATEGORY_LAUNCHER);
                Log.i("GrenadeLauncher", "RunningApp: " + componentName().toString());
                intent.setComponent(componentName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                adapterView.getContext().startActivity(intent);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

	@Override
	public void onItemLongClick(AdapterView<?> adapterView, View view, int position, long id)
	{
        final View adapView = adapterView;

		// infalte the builder
		LayoutInflater inflater = LayoutInflater.from(adapterView.getContext());
		AlertDialog.Builder builder = new AlertDialog.Builder(adapterView.getContext()); // this has to be adapterview context dunno why?
		View dialogView = inflater.inflate(R.layout.dialog_application_edit, null);
		builder.setView(dialogView);

		// now populate the builder(final allows us to use it in the onclick methods
		final EditText appLabel = (EditText) dialogView.findViewById(R.id.text_app_label);
		appLabel.setText(label());

		final ImageView imgIcon = (ImageView) dialogView.findViewById(R.id.image_app_icon);
        imgIcon.setImageBitmap(icon());
        imgIcon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String select = adapView.getContext().getResources().getString(R.string.dialog_title_select_image);

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                adapView.getContext().startActivity(Intent.createChooser(intent, select));

                /*
                Intent imageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                adapView.getContext().startActivity(imageIntent);
                */
            }
        });
        {

        };
		
		final CheckBox checkUnintstall = (CheckBox) dialogView.findViewById(R.id.check_uninstall);
		final String packageName = packageName();
		final Application thisApp = this;

		// setp the spinner box
		final Spinner spinGroup = (Spinner) dialogView.findViewById(R.id.spinner_app_group);
		final Group rootGroup = PackageMgr.instance().rootGroup();

		List<String> spinList = new ArrayList<String>(); //make list of groups
        final List<GroupItem> childList = rootGroup.childList(GroupItem.FLAG_ALL);

		for (GroupItem item : rootGroup.childList(GroupItem.FLAG_ALL))
			spinList.add(item.label());

		ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(adapterView.getContext(), android.R.layout.simple_list_item_1, spinList);
		spinGroup.setAdapter(spinAdapter);
		
		for (int i = 0; i < childList.size(); ++i) // set our current group as selection
			if (parent().equals(childList.get(i)))
			{
				spinGroup.setSelection(i); 
				break;
			}

	    builder.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() 
	    {
            @Override
            public void onClick(DialogInterface dialog, int id) 
            {
            	if (checkUnintstall.isChecked()) // UNINSTALL
            	{
            		Uri uninsUri = Uri.parse(String.format("package:%s", packageName));
            		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, uninsUri);
            		uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            		adapView.getContext().startActivity(uninstallIntent);
            		// the receiver will then determine if this package was removed :)
            	}
            	else // update this package
            	{
                    thisApp.setLabel(appLabel.getText().toString());
                    thisApp.setLabel(); // make sure its not blank
            		
            		int pos = spinGroup.getSelectedItemPosition();

            		Group oldParent = (Group) thisApp.parent();
                    oldParent.remove(thisApp);

            		Group newParent = (Group) childList.get(pos);
                    newParent.add(thisApp);

            		thisApp.update(); // update newParent
            	}

                // write changes for this app to file
                writeToFile();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
		builder.show();			
	}

    @Override
    Bitmap createIcon()
    {
       // if (iconOverride())
        //    return icon();

        if (icon() != null)
            return icon();

        Drawable drawable = PackageMgr.instance().applicationIcon(componentName()); // check if we exist first

        if (drawable != null)
        {
            // old way causes GC_FOR_ALLOC
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            int iconPixelSize = SettingsMgr.instance().iconSize(); // this comes in as pixels!
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, iconPixelSize, iconPixelSize, true);
            return scaled;
        }
        return null;
    }

    @Override
    public void writeToFile()
    {
        JsonMgr.instance().writeApplication(this);
    }


}

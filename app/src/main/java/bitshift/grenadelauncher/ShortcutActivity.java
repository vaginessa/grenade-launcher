package bitshift.grenadelauncher;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bronson on 22/05/13.
 * http://developer.android.com/reference/android/app/admin/DevicePolicyManager.html#lockNow%28%29
 */
public class ShortcutActivity extends Activity
{

    Intent mIntent;
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shortcut);

        // get the original intent
        // we return this later with our extra shortcut info!
        mIntent = getIntent();
        String action = mIntent.getAction();
        if (!action.equals(Intent.ACTION_CREATE_SHORTCUT))
            return;

        // create a list of shortcut intents
        final List<ShortcutIntent> shortcutIntentList = getShortcutIntentList();

        //display a list with choices of shortcut
        ListView listView = (ListView) findViewById(R.id.lv_list);
        ShortcutAdapter adapter = new ShortcutAdapter(getApplicationContext(), shortcutIntentList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {

                selectIntent(shortcutIntentList.get(i));
            }
        });
    }

    // code here to return an intenet!
    void selectIntent(ShortcutIntent item)
    {
        if (item.mAdmin)
            promptAdmin();

        mIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, item.mIntent); // this is out new shortcut intent (component, categories, action etc)
        mIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, item.mLabel); // name of the intent
        mIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, item.mIcon);
        setResult(RESULT_OK, mIntent);
        finish();
    }


    void promptAdmin()
    {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, DeviceAdminMgr.instance().deviceAdmin());
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getApplicationContext().getResources().getString(R.string.app_name));
        startActivity(intent);
    }


    List<ShortcutIntent> getShortcutIntentList()
    {
        List<ShortcutIntent> items = new ArrayList<ShortcutIntent>();

        Intent shortcutIntent = new Intent();
        shortcutIntent.addCategory(Intent.CATEGORY_DEFAULT);

        // first we need the custom shortcuts
        shortcutIntent.setAction(IntentMgr.ACTION_LOCK_DEVICE);
        items.add(new ShortcutIntent("Lock Device", BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher), shortcutIntent, true)); // needs admin access

        /*

        // next we need a shorcut to each group
        Group root = PackageMgr.instance().rootGroup();
        for (GroupItem g : root.childList(GroupItem.FLAG_ALL))
        {
            shortcutIntent.setAction(IntentMgr.ACTION_VIEW_GROUP);
            //shortcutIntent.setType();
            //shortcutIntent.setData();  // URI: Scheme:Scheme-specific part // put our data in here i guess, or in an extra bundle

            items.add(new ShortcutIntent(g.label(), BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher), shortcutIntent));
        }

        // maybe later a shortcut for each application/class?
*/
        return items;
    }


    // need an object which has an label, icon, intenet
    class ShortcutIntent
    {
        public String mLabel;
        public Bitmap mIcon;
        public Intent mIntent;
        public Boolean mAdmin = false; // ask for admin access
        ShortcutIntent(String label, Bitmap icon, Intent intent)
        {
            mLabel = label;
            mIcon = icon;
            mIntent = intent;
        }
        ShortcutIntent(String label, Bitmap icon, Intent intent, Boolean admin)
        {
            mLabel = label;
            mIcon = icon;
            mIntent = intent;
            mAdmin = admin;
        }
    }

}
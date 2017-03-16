package bitshift.grenadelauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by Bronson on 10/06/13.
 * Intent contains the following via extras
 *  icon or resource
 *  name of shortcut
 *  intent to store
 */
public class Shortcut extends GroupItem
{
    Intent temp;
    int mIntentFlags;
    String mIntentAction;
    String mIntentData;
    String mIntentType;
    Set<String> mIntentCategories;
    Bundle mExtraBundle;

    public Shortcut(UUID id)
    {
        super(id);
        setIconOverride(true); // icon override true, bitmap is dumped to file
    }

    public Shortcut(Intent intent)
    {
        setId();
        setIconOverride(true);

        // no name or no intent, malformed shortcut intent
        if (!intent.hasExtra(Intent.EXTRA_SHORTCUT_INTENT) || !intent.hasExtra(Intent.EXTRA_SHORTCUT_NAME))
            return;

        // set the label from the name field
        setLabel(intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME));

        // grab the icon (createIcon bellow is used to generate the scaled icon when viewd)
        Intent.ShortcutIconResource iconRes = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
        if (iconRes != null)
        {
            Resources resources = PackageMgr.instance().resourcesForApplication(iconRes.packageName);
            if (resources != null)
                outputIconOverride(resources.getDrawable(resources.getIdentifier(iconRes.resourceName, null, null)));
        }
        else
        {
            Bitmap bitmap = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
            if (bitmap != null)
                outputIconOverride(bitmap);
        }

        // this contains the info for our intenet
        Intent extraIntent = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        setComponentName(extraIntent.getComponent());
        setPackageName(extraIntent.getPackage());
        setIntentFlags(extraIntent.getFlags());
        setIntentAction(extraIntent.getAction());
        setIntentData(extraIntent.getData());
        setIntentCategories(extraIntent.getCategories());
        setIntentType(extraIntent.getType());
        setExtraBundle(extraIntent.getExtras());
    }

    private void setComponentName(ComponentName component)
    {
        if (component != null)
        {
            setClassName(component.getClassName());
            setPackageName(component.getPackageName());
        }
    }

    public void setExtraBundle(String string)
    {
        if (string == null || string.isEmpty())
            return;

        Bundle bundle = new Bundle();
        Pattern p = Pattern.compile("\\|");
        Pattern p2 = Pattern.compile("\\=");

        // remove first and last chars as theyre usless to us
        string = string.substring(1, string.length() - 1);
        String[] splitStrings = TextUtils.split(string, p);

        for (String s : splitStrings)
        {
            String[] stringItems = TextUtils.split(s, p2);

            String strClass = stringItems[0];
            String key = stringItems[1];
            String data = stringItems[2];

            if (strClass.equalsIgnoreCase("String"))
                bundle.putString(key, data);

            if (strClass.equalsIgnoreCase("Boolean"))
                bundle.putBoolean(key, Boolean.valueOf(data));

            if (strClass.equalsIgnoreCase("Integer"))
                bundle.putInt(key, Integer.valueOf(data));
        }

        mExtraBundle = bundle;
    }

    public void setExtraBundle(Bundle bundle)
    {
        if (bundle == null || bundle.isEmpty() || bundle.keySet().isEmpty())
            return;

        mExtraBundle = bundle;
    }

    public Bundle extraBundle()
    {
        return mExtraBundle;
    }

    public String extraBundleString()
    {
        if (extraBundle() == null)
            return null;

        Set<String> keys = extraBundle().keySet();

        StringBuilder string = new StringBuilder();
        string.append("|"); // seperator

        for (String s : keys)
        {
            if (extraBundle().get(s) != null)
            {
                string.append(extraBundle().get(s).getClass().getSimpleName()); // key class (eg, boolean, int)
                string.append("=");
                string.append(s); // key name (string)
                string.append("=");
                string.append(extraBundle().get(s)); // keys value/data
                string.append("|"); // seperator
            }
        }
        //Log.i("bundle", string.toString());
        return string.toString();
    }

    public void setIntentCategories(Set<String> strings)
    {
        if (strings == null || strings.isEmpty())
            return;
        mIntentCategories = strings;
    }

    public Set<String> intentCategories()
    {
        return mIntentCategories;
    }

    public String intentCategoriesString()
    {
        if (intentCategories() == null || intentCategories().isEmpty())
            return null;

        StringBuilder string = new StringBuilder();
        string.append("|"); // seperator

        for (String s : intentCategories())
        {
            string.append(s);
            string.append("|"); // seperator
        }

        return string.toString();
    }

    public void setIntentCategories(String string)
    {
        if (string == null || string.isEmpty())
            return;

        Pattern p = Pattern.compile("\\|");
        Set<String> set = new HashSet<String>();

        // remove first and last chars as theyre usless to us
        string = string.substring(1, string.length() - 1);
        String[] splitStrings = TextUtils.split(string, p);

        for (String s : splitStrings)
        {
            set.add(s);
        }

        setIntentCategories(set);
    }

    public void setIntentFlags(int flags)
    {
        mIntentFlags = flags;
    }

    public int intentFlags()
    {
        return mIntentFlags;
    }

    public void setIntentAction(String intentAction)
    {
        if (intentAction == null || intentAction.isEmpty())
            return;

        mIntentAction = intentAction;
    }

    public String intentAction()
    {
        return mIntentAction;
    }

    public void setIntentType(String type)
    {
        if (type == null || type.isEmpty())
            return;

        mIntentType = type;
    }

    public String intentType()
    {
        return mIntentType;
    }

    public Uri intentData()
    {
        if (mIntentData == null)
            return null;

        return Uri.parse(mIntentData);
    }

    public String intentDataString()
    {
        if (mIntentData == null)
            return null;

        return mIntentData.toString();
    }

    public void setIntentData(Uri intentData)
    {
        if (intentData == null || intentData.toString().isEmpty())
            return;

        mIntentData = intentData.toString();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
    {
        try
        {
            Intent intent = new Intent();

            if (intentCategories() != null)
                for (String category : intentCategories())
                    intent.addCategory(category);

            if (intentAction() != null)
                intent.setAction(intentAction());

            if (componentName() != null)
                intent.setComponent(componentName());

            if (packageName() != null)
                intent.setPackage(packageName());

            // these wipe each other, stooopido!
            if (intentType() != null && intentData() == null)
                intent.setType(intentType());
            else if (intentData() != null && intentType() == null)
                intent.setData(intentData());
            else if (intentType() != null && intentData() != null)
                intent.setDataAndType(intentData(), intentType());

            intent.setFlags(intentFlags());

            if (extraBundle() != null)
                intent.putExtras(extraBundle());

            adapterView.getContext().startActivity(intent);
            //adapterView.getContext().startActivity(temp);
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
        View dialogView = inflater.inflate(R.layout.dialog_shortcut_edit, null);
        builder.setView(dialogView);

        // now populate the builder(final allows us to use it in the onclick methods
        final EditText itemLabel = (EditText) dialogView.findViewById(R.id.et_label);
        itemLabel.setText(label());

        final ImageView imgIcon = (ImageView) dialogView.findViewById(R.id.iv_icon);
        imgIcon.setImageBitmap(icon());
        imgIcon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String select = adapView.getContext().getResources().getString(R.string.dialog_title_select_image);

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");

                //adapView.getContext().startActivity(Intent.createChooser(intent, select));
                ((Activity) adapView.getContext()).startActivityForResult(intent, MainActivity.REQUEST_CODE_IMAGE);

                /*
                Intent imageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                adapView.getContext().startActivity(imageIntent);
                */
            }
        });
        {

        };

        final CheckBox cbRemove = (CheckBox) dialogView.findViewById(R.id.cb_remove);
        final Shortcut item = this;

        // setp the spinner box
        final Spinner sGroup = (Spinner) dialogView.findViewById(R.id.s_group);
        final Group rootGroup = PackageMgr.instance().rootGroup();

        List<String> spinList = new ArrayList<String>(); //make list of groups
        final List<GroupItem> childList = rootGroup.childList(GroupItem.FLAG_ALL);

        for (GroupItem group : rootGroup.childList(GroupItem.FLAG_ALL))
            spinList.add(group.label());

        ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(adapterView.getContext(), android.R.layout.simple_list_item_1, spinList);
        sGroup.setAdapter(spinAdapter);

        for (int i = 0; i < childList.size(); ++i) // set our current group as selection
            if (parent().equals(childList.get(i)))
            {
                sGroup.setSelection(i);
                break;
            }

        builder.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                if (cbRemove.isChecked()) // delete shortcut
                {
                    item.setFlag(GroupItem.FLAG_UNINSTALLED, true);
                    item.writeToFile();
                    Group parent = item.parent();
                    parent.remove(item);
                    parent.update();
                }
                else // update this package
                {
                    item.setLabel(itemLabel.getText().toString());
                    item.setLabel(); // make sure its not blank

                    int pos = sGroup.getSelectedItemPosition();

                    Group oldParent = (Group) item.parent();
                    oldParent.remove(item);

                    Group newParent = (Group) childList.get(pos);
                    newParent.add(item);

                    item.update(); // update newParent

                    writeToFile();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    @Override
    public void writeToFile()
    {
        JsonMgr.instance().writeShortcut(this);
    }

    @Override
    Bitmap createIcon()
    {
        if (icon() != null)
            return icon();

        //load bitmap from file
        // old way causes GC_FOR_ALLOC
        Bitmap bitmap = overrideIcon();
        if (bitmap == null)
            return null;

        int iconPixelSize = SettingsMgr.instance().iconSize(); // this comes in as pixels!
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, iconPixelSize, iconPixelSize, true);
        return scaled;
    }

    // return true if this shortcut contains the category from launcher
    // that means it has come from the play market, so we should ignore this shortcut!
    public boolean isCategoryLauncher()
    {
        if (intentCategories() == null)
            return false;

        for (String s : intentCategories())
        {
            if (s.equalsIgnoreCase(Intent.CATEGORY_LAUNCHER))
                return true;
        }


        return false;
    }
}

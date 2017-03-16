package bitshift.grenadelauncher;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GroupItemAdapter extends ArrayAdapter<GroupItem>
{

    List<GroupItem> groupItems;

	public GroupItemAdapter(Context context, List<GroupItem> items)
	{
        super(context, R.layout.app_layout_tile, items); // assigned are context, the resource id, and a list<resolveinfo>
        groupItems = items;
	}
     
    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        ViewHolder viewHolder; // class bellow

        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(SettingsMgr.instance().tileLayout(), null); // mLayout = R.layout.app_tile

            // setups the view holder
            viewHolder = new ViewHolder();

            viewHolder.label = (TextView) convertView.findViewById(R.id.appLabel);
            viewHolder.icon = (ImageView)convertView.findViewById(R.id.appIcon);

            // store stuff in the view, this stuff only needs to be done once
            // FONT Layout
            if (SettingsMgr.instance().fontEnabled())
            {
                viewHolder.label.setTextSize(SettingsMgr.instance().fontSize());
                viewHolder.label.setTypeface(SettingsMgr.instance().fontTypeface(), 0);
                viewHolder.label.setTextColor(SettingsMgr.instance().fontColor());

                if (SettingsMgr.instance().fontShadowEnabled())
                    viewHolder.label.setShadowLayer(1, 3, 3, Color.BLACK);
                else
                    viewHolder.label.setShadowLayer(0, 0, 0, Color.WHITE);
            }
            else
                viewHolder.label.setVisibility(TextView.GONE);

            // ICON Layout
            if (SettingsMgr.instance().iconEnabled())
            {
                ViewGroup.LayoutParams params = viewHolder.icon.getLayoutParams();
                params.width = SettingsMgr.instance().iconSize();
                params.height = SettingsMgr.instance().iconSize();
                viewHolder.icon.setLayoutParams(params);
                viewHolder.icon.setAlpha(SettingsMgr.instance().iconAlpha());
            }
            else
                viewHolder.icon.setVisibility(ImageView.GONE);

            // store the viewholder with the view
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        GroupItem item = groupItems.get(position);
        viewHolder.label.setText(item.label());

        if (item.icon() == null)
        {
            BitmapDownloaderTask task = new BitmapDownloaderTask(viewHolder.icon, item);
            task.execute();
        }
        viewHolder.icon.setImageBitmap(item.icon());

	    return(convertView);
    }



    // a view holder which holds the view information
    // this is an optimisation to speed the scrolling in the list view
    // http://developer.android.com/training/improving-layouts/smooth-scrolling.html
    static class ViewHolder
    {
        //GroupItem groupItem;
        TextView label;
        ImageView icon;
    }


    // bitmap downloader task
    // this loads the bitmaps on a separate thread
    // http://developer.android.com/training/improving-layouts/smooth-scrolling.html
    static class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap>
    {
        // TODO: remove weak references, see note here http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<GroupItem> groupItemReference;

        public BitmapDownloaderTask(ImageView imageView, GroupItem item)
        {
            imageViewReference = new WeakReference<ImageView>(imageView);
            groupItemReference = new WeakReference<GroupItem>(item);
        }

        @Override
        // Actual download method, run in the task thread
        protected Bitmap doInBackground(String... params)
        {
            int i = 0;
            if (groupItemReference != null)
            {
                GroupItem item = groupItemReference.get();
                if (item != null)
                {
                    Bitmap bitmap = item.createIcon();
                    return bitmap;
                }
            }
            return null;
        }

        @Override
        // Once the image is downloaded, associates it to the imageView
        protected void onPostExecute(Bitmap bitmap)
        {
            if (isCancelled())
            {
                bitmap = null;
            }

            if (imageViewReference != null)
            {
                ImageView imageView = imageViewReference.get();
                if (imageView != null)
                {
                    if (bitmap != null)
                    {
                        //imageView.setAlpha(SettingsMgr.instance().iconAlpha());
                        imageView.setImageBitmap(bitmap);
                        groupItemReference.get().setIcon(bitmap);
                    }
                }
            }
        }
    }


    // bitmap cache
    // uses more memory, but will smooth the scrolling
    // http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html


}



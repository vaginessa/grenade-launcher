package bitshift.grenadelauncher;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Bronson on 22/05/13.
 */
public class IntentActivity extends Activity
{
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        IntentMgr.instance().onReceive(getApplicationContext(), intent);
        this.finish();
    }
}
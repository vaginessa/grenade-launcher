package bitshift.grenadelauncher;

import java.util.ArrayList;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class DeviceAdminMgr extends DeviceAdminReceiver
{
    static private DeviceAdminMgr mSingleInstance;
    static DevicePolicyManager mDPM;
    static ComponentName mDeviceAdmin;
    static Context mContext;

    // needs blank for android
    public DeviceAdminMgr()
    {
        mSingleInstance = this;
    }

    public DeviceAdminMgr(Context context)
    {
        mSingleInstance = this;
        mDPM = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdmin = new ComponentName(context, DeviceAdminMgr.class);
        mContext = context;
    }

    static DeviceAdminMgr instance()
    {
        return mSingleInstance;
    }

    boolean isActiveAdmin()
    {
        return mDPM.isAdminActive(mDeviceAdmin);
    }

    void lockDevice()
    {
        if (isActiveAdmin())
            mDPM.lockNow();
    }

    ComponentName deviceAdmin()
    {
        return mDeviceAdmin;
    }

}

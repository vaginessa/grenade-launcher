package bitshift.grenadelauncher;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by Bronson on 1/06/13.
 * Sensor & security checks go in here
 */
public class SensorMgr implements SensorEventListener
{
    static private SensorMgr mSingleInstance;
    static private SensorManager mSensorManager;

    private Sensor mProximity;

    private float mProximityValue = 1.0f;

    SensorMgr(Context context)
    {
        mSingleInstance = this;
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    static SensorMgr instance()
    {
        return mSingleInstance;
    }

    public boolean checkProximity()
    {
        if (SettingsMgr.instance().proximityLock())
        {
            if (mProximityValue == 0)
                return false;
            else
                return true;
        }
        else
            return true;
    }

    public boolean checkSecurity() // for the menu button, asks for pin
    {
        if (checkLock())
        {
            // PUT PIN DIALOG HERE!! and return true when done!
            return false;
        }
        else
            return false;
    }

    public boolean checkLock() // check if locked
    {
        if (SettingsMgr.instance().securityLock())
        {
            return true;
        }
        else
            return false;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        mProximityValue = sensorEvent.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {

    }
}

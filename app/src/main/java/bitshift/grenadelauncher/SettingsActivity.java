package bitshift.grenadelauncher;

import android.os.Bundle;
import android.preference.PreferenceActivity;


// settings activity
public class SettingsActivity extends PreferenceActivity 
{
	// ON CREATE
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}


}

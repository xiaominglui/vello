package com.mili.xiaominglui.app.vello.ui;

import com.mili.xiaominglui.app.vello.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static final String KEY_PREF_SYNC_FREQ = "pref_sync_frequency";
	
	private ListPreference mListPreference;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// setup the initial value
		mListPreference.setSummary(mListPreference.getEntry());
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setupSimplePreferencesScreen();
		mListPreference = (ListPreference) getPreferenceScreen().findPreference(KEY_PREF_SYNC_FREQ);
	}
	
	private void setupSimplePreferencesScreen() {
        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.preferences);
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(KEY_PREF_SYNC_FREQ)) {
			ListPreference syncFreqPref = (ListPreference) findPreference(key);
			syncFreqPref.setSummary(syncFreqPref.getEntry());
		}
	}
}

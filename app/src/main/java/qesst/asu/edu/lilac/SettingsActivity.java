package qesst.asu.edu.lilac;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Application-wide settings
 */
public class SettingsActivity extends PreferenceActivity
{
	private static final String PREF_NAME = "pref_custom_name_string";
	private static final String PREF_MAC = "pref_custom_mac_string";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
	}

	public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences);

			// Change the summary text to any saved values if we have them
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
			onSharedPreferenceChanged(sharedPreferences, PREF_NAME);
			onSharedPreferenceChanged(sharedPreferences, PREF_MAC);
		}

		@Override
		public void onResume()
		{
			super.onResume();
			// Set up a listener whenever a key changes
			getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause()
		{
			super.onPause();
			// Set up a listener whenever a key changes
			getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
		{
			if (key.equals(PREF_NAME) || key.equals(PREF_MAC))
			{
				EditTextPreference editTextPreference = (EditTextPreference)findPreference(key);
				String value = sharedPreferences.getString(key, "");
				if (value.trim().isEmpty())
				{
					if (key.equals(PREF_NAME))
					{
						editTextPreference.setText(getString(R.string.pref_custom_name_hint));
						editTextPreference.setSummary(getString(R.string.pref_custom_name_hint));
					}
					else if (key.equals(PREF_MAC))
					{
						editTextPreference.setText(getString(R.string.pref_custom_mac_hint));
						editTextPreference.setSummary(getString(R.string.pref_custom_mac_hint));
					}
				}
				else
				{
					editTextPreference.setText(value);
					editTextPreference.setSummary(value);
				}
			}
		}
	}
}

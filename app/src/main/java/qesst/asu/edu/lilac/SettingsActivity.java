package qesst.asu.edu.lilac;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
	// Keep in sync with preferences.xml
	private static final String PREF_NAME = "pref_custom_name_string";
	private static final String PREF_MAC = "pref_custom_mac_string";
	private static final String PREF_RESET = "pref_key_reset";

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
			final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
			onSharedPreferenceChanged(sharedPreferences, PREF_NAME);
			onSharedPreferenceChanged(sharedPreferences, PREF_MAC);

			// Reset all preferences option
			Preference prefReset = (Preference) findPreference(PREF_RESET);
			prefReset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				public boolean onPreferenceClick(Preference preference)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle(R.string.confirm_title);
					builder.setMessage(R.string.confirm_message);
					builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							// Reset all preferences
							sharedPreferences.edit().clear().commit();
							PreferenceManager.setDefaultValues(getActivity().getBaseContext(), R.xml.preferences, true);

							// Refresh the activity without any window animation
							Intent intent = getActivity().getIntent();
							intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
							getActivity().overridePendingTransition(0, 0);
							getActivity().finish();
							getActivity().overridePendingTransition(0, 0);
							startActivity(intent);

							dialog.dismiss();
						}
					});
					builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					builder.create().show();
					return true;
				}
			});
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
		public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key)
		{
			if (key.equals(PREF_NAME) || key.equals(PREF_MAC))
			{
				EditTextPreference editTextPreference = (EditTextPreference)findPreference(key);
				String value = sharedPreferences.getString(key, "");
				if (value.trim().isEmpty())
				{
					if (key.equals(PREF_NAME))
					{
						editTextPreference.setText("");
						editTextPreference.setSummary(getString(R.string.pref_custom_name_hint));
					}
					else if (key.equals(PREF_MAC))
					{
						editTextPreference.setText("");
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

package qesst.asu.edu.lilac;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Global application context for managing state and string resources.
 *
 * This feels dirty, but avoids a ton of ugliness later
 */
public class Lilac extends Application
{
	private static Context mContext;
	private static SharedPreferences mPreferences;

	@Override
	public void onCreate()
	{
		super.onCreate();
		mContext = this;

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
	}

	/**
	 * Returns a reference to a global application context when needed
	 * @return
	 */
	public static Context getContext()
	{
		return mContext;
	}

	/**
	 * Returns a string from anywhere in the application
	 *
	 * Why is this needed? Because getString() requires a Context
	 * which is null until the Activity has been initialized.
	 * This avoids having to create string variables in onCreate().
	 * @param resId Resource ID
	 * @return
	 */
	public static String getStringGlobal(int resId)
	{
		return mContext.getString(resId);
	}

	/**
	 * Returns a reference to the global preferences object
	 * @return
	 */
	public static SharedPreferences getPreferences()
	{
		return mPreferences;
	}
}

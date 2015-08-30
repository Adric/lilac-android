package qesst.asu.edu.lilac;

//import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.app.FragmentTransaction;
//import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity //extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		setContentView(R.layout.activity_main);

		/*
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_iv);

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.replace(R.id.content_fragment, fragment);
		transaction.commit();
		*/
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			//Intent i = new Intent(this, SettingsActivity.class);
			//startActivityForResult(i, 1);

			// Display the fragment as the main content.
			/*
			getFragmentManager().beginTransaction()
			                    .replace(R.id.content_fragment, new SettingsFragment())
			                    .commit();
			*/

			/*
			FragmentTransaction transaction = getFragmentManager().beginTransaction();

			// Replace whatever is in the fragment_container view with this fragment,
			// and add the transaction to the back stack so the user can navigate back
			transaction.replace(R.id.content_fragment, new SettingsFragment());
			transaction.addToBackStack(null);

			// Commit the transaction
			transaction.commit();
			*/

			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);

			return true;
		}
		else if (id == R.id.about)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}

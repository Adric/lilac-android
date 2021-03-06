package qesst.asu.edu.lilac;

//import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.app.FragmentTransaction;
//import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity implements IMessageCallback //extends AppCompatActivity
{
	private final static String TAG = "MainActivity";

	/**
	 * Measurement
	 */
	private boolean mUseDummyData = false;
	private boolean mMeasuring;
	private DummyData mDummyData = new DummyData();
	private ModuleDataSet mDataSet;
	private String mFilenameBase;

	/**
	 * Communication
	 */
	private EnumSet<EFlag> mFlags;
	// Number of times to attempt to retry sending flag updates
	private int mRetryCount = 0;
	private final int RETRY_COUNT_MAX = 2; // three retries
	private BluetoothActivity mBluetooth;
	private int mCallCount;

	/**
	 * Interface
	 */
	private Button btnConnect = null;
	private Button btnMeasure = null;
	private TextView txtReceived = null;
	private Button btnClear = null;
	private Button btnFlags = null;
	private TextView lblVoc = null;
	private TextView lblIsc = null;
	private Button btnWriteToFile = null;
	private Button btnEmail = null;
	private Button btnScreenshot = null;
	private ArrayList<MenuItem> mFlagMenuItems;
	private GraphView mGraph;
	SharedPreferences mPreferences;

	public MainActivity()
	{
		mDataSet = new ModuleDataSet();
		mCallCount = 0;
		mFlags = EnumSet.noneOf(EFlag.class);
		mFlagMenuItems = new ArrayList<MenuItem>();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null)
		{
			// Init services
		}
//	}

	//@Override
	//public View onCreateView(String name, Context context, AttributeSet attrs)
	//{

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		mPreferences = //this.getSharedPreferences("preferences", Context.MODE_PRIVATE);
						//PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        Lilac.getPreferences();

		mBluetooth = new BluetoothActivity(this, mPreferences);

		// Call after initializing mPreferences
		initFlags();

		// Set up the UI
		btnConnect = (Button) findViewById(R.id.btn_connect);
		btnWriteToFile = (Button) findViewById(R.id.btn_to_file);
		btnEmail = (Button) findViewById(R.id.btn_email);
		btnScreenshot = (Button) findViewById(R.id.btn_screenshot);
		btnMeasure = (Button) findViewById(R.id.btn_measure);
		txtReceived = (TextView) findViewById(R.id.txt_received);
		btnClear = (Button) findViewById(R.id.btn_clear);
		btnFlags = (Button) findViewById(R.id.btn_flags);
		lblVoc = (TextView) findViewById(R.id.lbl_voc);
		lblIsc = (TextView) findViewById(R.id.lbl_isc);

		btnMeasure.setEnabled(false);
		btnConnect.setText(getString(R.string.connect));

		// TODO: templatize
		lblVoc.setText("Voc: ");
		lblIsc.setText("Isc: ");

		// Toggle the measure button differently than these
		toggleBTUI(false);

		// Make the textview non-clickable in code since this doesn't work in xml
		final boolean readOnly = true;
		txtReceived.setFocusable(!readOnly);
		txtReceived.setFocusableInTouchMode(!readOnly);
		txtReceived.setClickable(!readOnly);
		txtReceived.setLongClickable(!readOnly);
		txtReceived.setCursorVisible(!readOnly);


		btnConnect.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (!mBluetooth.isConnected())
				{
					connect();
				}
				else
				{
					disconnect();
				}

				// Toggle menu items based on connection state
				updateMenuItems();
			}
		});


		btnMeasure.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (!mMeasuring)
				{
					startMeasurement();
				}
				else
				{
					stopMeasurement();
				}
			}
		});

		btnFlags.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				mFlags.add(EFlag.GET_FLAGS);
				mBluetooth.write(EFlag.toJsonString(mFlags));
				mFlags.remove(EFlag.GET_FLAGS);
			}
		});

		btnClear.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				clearUI();
			}
		});

		btnWriteToFile.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				saveFile();
			}
		});

		btnEmail.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				emailData();
			}
		});

		btnScreenshot.setOnClickListener(new View.OnClickListener()
		{

			//TODO: change library to use Lilac subfolder
			public void onClick(View v)
			{
				saveImage();
			}
		});

		// Get the view without the status bar
		final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);

		// Create the chart
		mGraph = new GraphView((LineChart) findViewById(R.id.iv_curve), viewGroup.getRootView(), this);
		mGraph.setIsSweep(mFlags.contains(EFlag.SWEEP));

		// We have menu options
		//setHasOptionsMenu(true);





		/*
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_iv);

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.replace(R.id.content_fragment, fragment);
		transaction.commit();
		*/

		mFilenameBase = getResources().getString(R.string.file_name_base);

		//return super.onCreateView(name, context, attrs);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);

		for (int i = 0; i < menu.size(); ++i)
		{
			MenuItem item = menu.getItem(i);
			switch (item.getItemId())
			{
				case R.id.menu_item_voltage:
				case R.id.menu_item_current:
				case R.id.menu_item_temp:
				case R.id.menu_item_sweep:
				case R.id.menu_item_continuous:
				case R.id.menu_item_average:
				case R.id.menu_item_debug:
					mFlagMenuItems.add(item);
					break;
				default:
					continue;
			}

			// Should be disabled at the start
			updateMenuItems();
		}

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
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			return true;
		}
		else if (id == R.id.about)
		{
			Intent i = new Intent(this, AboutActivity.class);
			startActivity(i);
			return true;
		}

		if (item.isCheckable())
		{
			if (mBluetooth == null || !mBluetooth.isConnected())
			{
				Toast.makeText(this, "Cannot change until connected!", Toast.LENGTH_SHORT).show();
				return false;
			}

			/*
			 * From: http://developer.android.com/guide/topics/ui/menus.html#checkable
			 *
			 * When a checkable item is selected, the system calls your respective
			 * item-selected callback method (such as onOptionsItemSelected()).
			 * It is here that you must set the state of the checkbox, because a
			 * checkbox or radio button does not change its state automatically.
			 * You can query the current state of the item
			 * (as it was before the user selected it) with isChecked() and then
			 * set the checked state with setChecked()
			 */
			int old_flag_count = mFlags.size();

			updateCheckedAndFlags(item, getEFlagFromMenuItem(item));

			// If anything was changed, send the change
			if (old_flag_count != mFlags.size())
			{
				// Update the Arduino
				mBluetooth.write(EFlag.toJsonString(mFlags));

				// Update the preference
				// The strange usage is to work around a bug in Android:
				// https://code.google.com/p/android/issues/detail?id=27801
				Set<String> prefFlags = mPreferences.getStringSet("pref_key_flag_list", null);
				if (prefFlags != null)
				{
					HashSet<String> prefFlagsCopy = new HashSet<String>(prefFlags);
					for (EFlag flag : mFlags)
					{
						prefFlagsCopy.add(""+flag.toChar());
						Log.d(TAG, "Adding pref flag from menu: " + flag.toChar());
					}
					mPreferences.edit().putStringSet("pref_key_flag_list", prefFlagsCopy).commit();
				}

				// Update the graph
				mGraph.setIsSweep(mFlags.contains(EFlag.SWEEP));

				return super.onOptionsItemSelected(item);
			}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume()
	{
		if (mBluetooth != null && !mBluetooth.isConnected())
		{
			// TODO: causes a crash
			//connect();
		}

		// Menu options may have changed if reset prefs in a different activity
		initFlags();
		if (mGraph != null) mGraph.setIsSweep(mFlags.contains(EFlag.SWEEP));
		invalidateOptionsMenu();

		super.onResume();
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}


	@Override
	public void call(ArrayList<String> arr)
	{
		mCallCount++;
		Log.d("MessageData", "Call count: " + mCallCount);
		String output = "";
		for (String str : arr)
		{
			// Any kind of string operation here bogs down our threads
			// Comment this out for release!
			output += str;// + "\n";
			txtReceived.append(output + "\n");

			final String TAG = "JsonParsing";

			try
			{
				// TODO: Ugly parsing, refactor!
				JSONObject jObj = new JSONObject(str);
				ModuleData md = new ModuleData();
				boolean add = false;
				if (jObj.has("V"))
				{
					Log.d(TAG, "V found");
					add = true;
					md.setVoltage(jObj.getDouble("V"));
				}
				if (jObj.has("I"))
				{
					Log.d(TAG, "I found");
					add = true;
					md.setCurrent(jObj.getDouble("I"));
				}
				if (jObj.has("T"))
				{
					Log.d(TAG, "T found");
					add = true;
					md.setTemp(jObj.getDouble("T"));
				}
				if (jObj.has("t"))
				{
					add = true;
					Log.d(TAG, "t found");
					md.setTime(jObj.getLong("t"));
				}
				if (jObj.has("G"))
				{
					Log.d(TAG, "G found");
					Log.d(TAG, "Current flags (int): " + jObj.getInt("G"));
					EnumSet<EFlag> flags = EFlag.getEFlags(jObj.getInt("G"));
					for (EFlag flag : flags)
					{
						Log.d(TAG, "Current flags (string): " + flag.toString());
					}

					if (flags.containsAll(mFlags))
					{
						Log.d(TAG, "All flags are set on the Arduino");
					}

					// Update the menu
					invalidateOptionsMenu();
				}
				if (jObj.has("F"))
				{
					Log.d(TAG, "F found");
					if (jObj.getBoolean("F"))
					{
						mRetryCount = 0;
						Log.d(TAG, "Flag update successful!");
					}
					else
					{
						Log.d(TAG, "Flag update failed!");
						if (jObj.has("G"))
						{
							Log.d(TAG, "Failed to update flags (int): " + jObj.getInt("G"));
							EnumSet<EFlag> flags = EFlag.getEFlags(jObj.getInt("G"));
							for (EFlag flag : flags)
							{
								Log.d(TAG, "Failed to update flags (string): " + flag.toString());
							}
							Log.d(TAG, "Retry count: " + mRetryCount);
						}

						if (mRetryCount < RETRY_COUNT_MAX)
						{
							mRetryCount++;
							mBluetooth.write(EFlag.toJsonString(mFlags));
						}
						else
						{
							Toast.makeText(getBaseContext(), getString(R.string.flag_update_failed), Toast.LENGTH_SHORT);
							mRetryCount = 0;
						}
					}
				}
				if (jObj.has("VOC"))
				{
					Log.d(TAG, "VOC found");
					lblVoc.setEnabled(true);
					lblVoc.setText("Voc: " + Double.toString(jObj.getDouble("VOC")));
					Log.d(TAG, "VOC: " + Double.toString(jObj.getDouble("VOC")));
					md.setVoc(jObj.getDouble("VOC"));
					add = true;
				}
				if (jObj.has("ISC"))
				{
					Log.d(TAG, "ISC found");
					lblIsc.setEnabled(true);
					lblIsc.setText("Isc: " + Double.toString(jObj.getDouble("ISC")));
					Log.d(TAG, "ISC: " + Double.toString(jObj.getDouble("ISC")));
					md.setIsc(jObj.getDouble("ISC"));
					add = true;
				}
				if (jObj.has("E"))
				{
					Log.d(TAG, "E found");
					Log.e(TAG, "ERROR: " + jObj.getString("E"));
				}
				if (add)
				{
					Log.d(TAG, "Adding md: " + md.toString());
					mDataSet.add(md);
				}
			}
			catch (Exception e)
			{
				Log.d(TAG, e.getMessage());
			}
		}
		Log.e(TAG, output);

		// Clear out what we get
		arr.clear();

		updateGraph();
	}

	public void updateGraph()
	{
		ModuleData data = null;
		if (mUseDummyData)
		{
			data = mDummyData.getNextEntry();
			mDataSet.add(data); // For UI enabling, etc
		}
		else
		{
			if (mDataSet.isEmpty())
			{
				Log.e(TAG, "Trying to update graph with an empty dataset! Returning!");
				return;
			}
			data = mDataSet.get(mDataSet.size()-1);
		}
		mGraph.updateGraph(data);

		// Make sure all our UI data buttons are enabled
		// if we're going to be continuously updating
		// or just have a single data point
		toggleBTUI(!mDataSet.isEmpty());
	}

	public void initFlags()
	{
		mFlags.clear();
		boolean default_flags = (mPreferences == null);
		if (!default_flags)
		{
			Set<String> prefFlags = mPreferences.getStringSet("pref_key_flag_list", null);
			if (prefFlags != null)
			{
				if (!prefFlags.isEmpty())
				{
					/*
						These are the values in arrays.xml
					 */
					for (String s : prefFlags)
					{
						Log.d(TAG, "Found pref flag: " + s);
						EFlag flag = EFlag.fromChar(s.charAt(0));
						if (flag != EFlag.NONE)
						{
							Log.d(TAG, "Adding pref flag: " + flag.toString());
							mFlags.add(flag);
						}
					}
				}
				else
				{
					Log.e(TAG, "pref flag is empty!");
				}
			}
		}
		else
		{
			Log.e(TAG, "pref flag is null! Using hardcoded defaults");
			default_flags = true;
		}

		if (default_flags)
		{
			mFlags.add(EFlag.CONTINUOUS);
			mFlags.add(EFlag.VOLTAGE);
			mFlags.add(EFlag.CURRENT);
			mFlags.add(EFlag.AVERAGE);
			mFlags.add(EFlag.SWEEP);
		}
	}

	public void connect()
	{
		mBluetooth.connect();
		mCallCount = 0;
		if (mBluetooth.isConnected())
		{
			btnConnect.setText(getString(R.string.disconnect));
			// Get the flags back to set the menu entries
			mFlags.add(EFlag.GET_FLAGS);
			String flag_str = EFlag.toJsonString(mFlags);
			//txtReceived.append("Sending flags: " + flag_str);
			mBluetooth.write(flag_str);
			mFlags.remove(EFlag.GET_FLAGS);

			// TODO: make this less fragile!
			btnMeasure.setEnabled(true);
			btnMeasure.setText(getString(R.string.begin_measurement));
		}
	}

	/**
	 * Reset input and output streams and make sure socket is closed.
	 * This method will be used during shutdown() to ensure that the
	 * connection is properly closed during a shutdown.
	 */
	public void disconnect()
	{
		mFlags.remove(EFlag.RUN);
		mBluetooth.write(EFlag.toJsonString(mFlags));
		toggleBTUI(!mDataSet.isEmpty());
		btnConnect.setText(getString(R.string.connect));

		// Also disconnects the message system
		mBluetooth.disconnect();

		// TODO: make this less fragile!
		btnMeasure.setEnabled(false);
		btnMeasure.setText(getResources().getString(R.string.begin_measurement));

		txtReceived.append("\nBluetooth Disconnected!\n");
	}

	private void toggleBTUI(boolean enable)
	{
		if (btnWriteToFile != null) btnWriteToFile.setEnabled(enable);
		if (btnEmail != null) btnEmail.setEnabled(enable);
		if (btnScreenshot != null) btnScreenshot.setEnabled(enable);
	}



	private void updateMenuItems()
	{
		for (MenuItem item : mFlagMenuItems)
		{
			item.setEnabled(mBluetooth.isConnected());
			item.setChecked(mFlags.contains(getEFlagFromMenuItem(item)));
		}
	}

	// Returns the flag associated with a menu item
	private EFlag getEFlagFromMenuItem(MenuItem item)
	{
		if (item == null) return EFlag.NONE;

		switch (item.getItemId())
		{
			case R.id.menu_item_voltage:
				return EFlag.VOLTAGE;

			case R.id.menu_item_current:
				return EFlag.CURRENT;

			case R.id.menu_item_temp:
				return EFlag.TEMP;

			case R.id.menu_item_sweep:
				return EFlag.SWEEP;

			case R.id.menu_item_continuous:
				return EFlag.CONTINUOUS;

			case R.id.menu_item_average:
				return EFlag.AVERAGE;

			case R.id.menu_item_debug:
				return EFlag.DEBUG_OUTPUT;

			default:
				return EFlag.NONE;
		}
	}

	private void updateCheckedAndFlags(MenuItem item, EFlag flag)
	{
		if (item == null || flag == null || flag == EFlag.NONE) return;

		if (item.isChecked())
		{
			mFlags.remove(flag);
			item.setChecked(false);
		}
		else
		{
			mFlags.add(flag);
			item.setChecked(true);
		}
	}


	private void saveFile()
	{
		// Create filename
		EDataSeparator separator = getSeparator(false);
		String filename = (separator == EDataSeparator.COMMA) ?
		                  mFilenameBase + ".csv" :
		                  mFilenameBase + ".txt";

		// Try to write to SDcard
		if (canWriteOnExternalStorage())
		{
			File file = getStorageDir("Lilac");
			if (file == null)
			{
				Log.e(TAG, "Could not create lilac folder to write in");
				return;
			}

			ArrayList<String> data = mDataSet.getStringsForFile(separator);
			try
			{
				FileOutputStream out = new FileOutputStream(file + File.separator + filename);
				for (String s : data)
				{
					// Add newlines here
					out.write((s + "\n").getBytes());
				}
				out.close();

				Log.d(TAG, filename + " written successfully");
				Toast.makeText(this, filename + " saving successful!",
				               Toast.LENGTH_SHORT).show();
			}
			catch (IOException e)
			{
				Log.e(TAG, "Could not write file: " + filename + ", trying alternate way");
				try
				{
					FileOutputStream fout = this.getApplicationContext().openFileOutput(filename, Context.MODE_APPEND);
					OutputStreamWriter osw = new OutputStreamWriter(fout);

					// Write the string to the file
					for (String s : data)
					{
						// Add newlines here
						osw.write(s + "\n");
					}
					osw.close();
				}
				catch (IOException ioe)
				{
					Log.e(TAG, "OutputStreamWriter could not write " + filename);
					try
					{
						File root = new File(Environment.DIRECTORY_DOWNLOADS);
						File gpxfile = new File(root, filename);
						FileWriter fw = new FileWriter(gpxfile);
						for (String s : data)
						{
							// Add newlines here
							fw.append(s + "\n");
						}
						fw.flush();
						fw.close();
					}
					catch (IOException ioe2)
					{
						Log.e(TAG, "FileWriter failed to write " + filename + " too!");
						Toast.makeText(this, filename + " saving failed!",
						               Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
		else
		{
			Log.e(TAG, "Could not write to external storage!");
			Toast.makeText(this, filename + " saving failed!",
			               Toast.LENGTH_SHORT).show();
		}
	}

	public void saveImage()
	{
		String filename = mFilenameBase + ".jpg";
		String state = (mGraph.saveToGallery(filename, 100)) ?
		               "successful!" : "failed!";
		Toast.makeText(this, "Saving " + filename + " " + state, Toast.LENGTH_SHORT).show();
	}

	public void emailData()
	{
		if (mDataSet.isEmpty())
		{
			Toast.makeText(this, "No data to email!", Toast.LENGTH_SHORT).show();
			return;
		}

		Intent email = new Intent(Intent.ACTION_SEND);
		email.putExtra(Intent.EXTRA_EMAIL, "Receiver Email Address");
		email.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		String subject = mFilenameBase;
		email.putExtra(Intent.EXTRA_SUBJECT, subject);

		// TODO: add preference for either emailing the data as an attachment or as the email body
		String text = "";
		ArrayList<String> data = mDataSet.getStringsForFile(getSeparator(true));
		for (String s: data)
		{
			text += s + "\n";
		}
		email.putExtra(Intent.EXTRA_TEXT, text);

		//Mime type of the attachment (or) u can use sendIntent.setType("*/*")
		email.setType("text/plain");
		//email.setType("application/YourMimeType");
		//email.setType("*/*");

		// TODO: be able to email a screenshot as well
		//Full Path to the attachment
				/*
				if (!mFilenames.isEmpty())
				{
					try
					{
						// Could also use URIs in the form:
						//Uri u1 = Uri.fromFile(file);
						FileInputStream fin = this.getApplicationContext().openFileInput(mFilenames.get(mFilenames.size() - 1));
						email.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + fin.toString()));
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				*/

		try
		{
			startActivity(Intent.createChooser(email, "Send Message..."));
		}
		catch (android.content.ActivityNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}

	private EDataSeparator getSeparator(boolean email)
	{
		if (mPreferences != null)
		{
			String sep = (email) ? mPreferences.getString("pref_key_email_data_separator", null) :
			             mPreferences.getString("pref_key_file_data_separator", null);
			if ((sep != null) && sep.contains("Comma Value"))
			{
				return EDataSeparator.COMMA;
			}
		}
		return EDataSeparator.TAB;
	}

	private void clearUI()
	{
		if (txtReceived != null) txtReceived.setText("");
		if (lblVoc != null) lblVoc.setText("Voc: ");
		if (lblIsc != null) lblIsc.setText("Isc: ");
		if (mGraph != null) mGraph.reset();
		if (mDataSet != null) mDataSet.clear();
	}

	public void startMeasurement()
	{
		// Generate a new filename for this data
		SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy_HHmmss", Locale.getDefault());
		mFilenameBase = "IV-data-" + df.format(new Date());

		mGraph.resetEndMeasurement(); // resets the flag that triggers from current stop
		mGraph.setAllowNegativeCurrents(mPreferences.getBoolean("pref_key_negative_current_check", false));

		mMeasuring = true;
		if (mFlags.contains(EFlag.CONTINUOUS))
		{
			btnMeasure.setText(getResources().getString(R.string.end_measurement));
		}
		mFlags.add(EFlag.RUN);
		mBluetooth.write(EFlag.toJsonString(mFlags));
	}

	public void stopMeasurement()
	{
		mMeasuring = false;
		if (mFlags.contains(EFlag.CONTINUOUS))
		{
			btnMeasure.setText(getResources().getString(R.string.begin_measurement));
		}
		mFlags.remove(EFlag.RUN);
		mBluetooth.write(EFlag.toJsonString(mFlags));

		// Make sure all our UI data buttons are enabled
		toggleBTUI(!mDataSet.isEmpty());

		if (mPreferences.getBoolean("pref_key_auto_save_files_check", false))
		{
			saveFile();
			saveImage();
		}
	}







	public File getStorageDir(String filename)
	{
		// Get the directory for the user's public documents directory.
		try
		{
			File file = new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_DOWNLOADS), filename);
			if (!file.mkdirs())
			{
				Log.e(TAG, "Directory not created");
			}
			return file;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public File getScreenshotDir(String filename)
	{
		// Get the directory for the user's public pictures directory.
		try
		{
			File file = new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_DCIM), filename);
			if (!file.mkdirs())
			{
				Log.e(TAG, "Directory not created");
			}
			return file;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static boolean canWriteOnExternalStorage()
	{
		// get the state of your external storage
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state))
		{
			// if storage is mounted return true
			Log.v(TAG, "Cannot write to external storage.");
			return false;
		}
		return true;
	}

}

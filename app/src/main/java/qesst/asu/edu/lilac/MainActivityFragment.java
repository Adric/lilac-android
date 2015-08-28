package qesst.asu.edu.lilac;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import java.util.Locale;
import java.util.Set;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements IMessageCallback
{
	private final static String TAG = "MainActivityFragment";

	private boolean mUseDummyData = false;

	private boolean mMeasuring;

	SharedPreferences mPreferences;

	private BluetoothActivity mBluetooth;

	private int mCallCount;

	private GraphView mGraph;

	private DummyData mDummyData = new DummyData();

	private ModuleDataSet mDataSet;

	/**
	 * Class variables
	 */
	// List of files
	private ArrayList<String> mFilenames;

	private ArrayList<MenuItem> mFlagMenuItems;

	// UI
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

	private float mVoc;

	private EnumSet<EFlag> mFlags;

	public MainActivityFragment()
	{
		mBluetooth = new BluetoothActivity(this);
		mFilenames = new ArrayList<String>();
		mDataSet = new ModuleDataSet();
		mVoc = 0.f;
		mCallCount = 0;
		mFlags = EnumSet.noneOf(EFlag.class);
		mFlagMenuItems = new ArrayList<MenuItem>();

		// Default flags we want
		/*
		mFlags.add(EFlag.VOLTAGE);
		mFlags.add(EFlag.CURRENT);
		mFlags.add(EFlag.AVERAGE);
		mFlags.add(EFlag.CONTINUOUS); // DEBUG REMOVE ME
		*/
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_iv_main, container, false);

		mPreferences = //getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
						PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

		// Set up the flags
		Set<String> prefFlags = mPreferences.getStringSet("pref_default_flag_list", null);
		if (prefFlags != null)
		{
			for (String s: prefFlags)
			{
				EFlag flag = EFlag.toType(s.charAt(0));
				if (flag != EFlag.NONE)
				{
					mFlags.add(flag);
				}
			}
		}

			// Set up the UI
		btnConnect = (Button) view.findViewById(R.id.btn_connect);
		btnWriteToFile = (Button) view.findViewById(R.id.btn_to_file);
		btnEmail = (Button) view.findViewById(R.id.btn_email);
		btnScreenshot = (Button) view.findViewById(R.id.btn_screenshot);
		btnMeasure = (Button) view.findViewById(R.id.btn_measure);
		txtReceived = (TextView) view.findViewById(R.id.txt_received);
		btnClear = (Button) view.findViewById(R.id.btn_clear);
		btnFlags = (Button) view.findViewById(R.id.btn_flags);
		lblVoc = (TextView) view.findViewById(R.id.lbl_voc);
		lblIsc = (TextView) view.findViewById(R.id.lbl_isc);

		btnMeasure.setEnabled(false);
		btnConnect.setText("Connect");
		lblVoc.setText("");
		lblIsc.setText("");

		toggleBTUI(false);

		// set non clickable in code since xml flags don't work
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
					mBluetooth.connect();
					mCallCount = 0;
					if (mBluetooth.isConnected())
					{
						btnConnect.setText("Disconnect");
						// Get the flags back to set the menu entries
						mFlags.add(EFlag.GET_FLAGS);
						String flag_str = EFlag.toJsonString(mFlags);
						//txtReceived.append("Sending flags: " + flag_str);
						mBluetooth.write(flag_str);
						mFlags.remove(EFlag.GET_FLAGS);

						// TODO: make this less fragile!
						btnMeasure.setEnabled(true);
						btnMeasure.setText(getResources().getString(R.string.begin_measurement));
					}
				}
				else
				{
					mFlags.remove(EFlag.RUN);
					mBluetooth.write(EFlag.toJsonString(mFlags));
					disconnect();
					toggleBTUI(!mDataSet.isEmpty());
					btnConnect.setText("Connect");

					// TODO: make this less fragile!
					btnMeasure.setEnabled(false);
					btnMeasure.setText(getResources().getString(R.string.begin_measurement));
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
					mMeasuring = true;
					if (mFlags.contains(EFlag.CONTINUOUS)) 
					{
						btnMeasure.setText(getResources().getString(R.string.end_measurement));	
					}
					mFlags.add(EFlag.RUN);
					mBluetooth.write(EFlag.toJsonString(mFlags));
				}
				else
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

					if (mPreferences.getBoolean("pref_auto_save_files_check", false))
					{
						saveFile();
						saveImage();
					}
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
				txtReceived.setText("");
				mGraph.reset();
				mDataSet.clear();
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
				// Email
				Intent email = new Intent(Intent.ACTION_SEND);
				email.putExtra(Intent.EXTRA_EMAIL, "Receiver Email Address");
				email.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy_HHmmss", Locale.getDefault());
				String subject = "IV-data-" + df.format(new Date());
				email.putExtra(Intent.EXTRA_SUBJECT, subject);

				String text = "";
				ArrayList<String> data = mDataSet.getStringsForFile(ModuleDataSet.EDataSeparator.COMMA);
				for (String s: data)
				{
					text += s + "\n";
				}
				email.putExtra(Intent.EXTRA_TEXT, text);

				//Mime type of the attachment (or) u can use sendIntent.setType("*/*")
				email.setType("text/plain");
				//email.setType("application/YourMimeType");
				//email.setType("*/*");

				//Full Path to the attachment
				/*
				if (!mFilenames.isEmpty())
				{
					try
					{
						// Could also use URIs in the form:
						//Uri u1 = Uri.fromFile(file);
						FileInputStream fin = getActivity().getApplicationContext().openFileInput(mFilenames.get(mFilenames.size() - 1));
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
		});

		btnScreenshot.setOnClickListener(new View.OnClickListener()
		{

			//TODO: change library to use Lilac subfolder
			public void onClick(View v)
			{
				saveImage();
			}
		});

		// Create the chart
		mGraph = new GraphView((LineChart) view.findViewById(R.id.iv_curve), view);

		// We have menu options
		setHasOptionsMenu(true);

		return view;
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


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.menu_fragment_iv, menu);
		super.onCreateOptionsMenu(menu, inflater);

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
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.isCheckable())
		{
			if (mBluetooth == null || !mBluetooth.isConnected())
			{
				Toast.makeText(this.getActivity(), "Cannot change until connected!", Toast.LENGTH_SHORT).show();
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
			int id = item.getItemId();
			int old_flag_count = mFlags.size();

			updateCheckedAndFlags(item, getEFlagFromMenuItem(item));

			if (old_flag_count != mFlags.size())
			{
				mBluetooth.write(EFlag.toJsonString(mFlags));
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
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
				ModuleDataEntry mde = new ModuleDataEntry();
				boolean add = false;
				if (jObj.has("V"))
				{
					Log.d(TAG, "V found");
					add = true;
					mde.setVoltage(jObj.getDouble("V"));
				}
				if (jObj.has("I"))
				{
					Log.d(TAG, "I found");
					add = true;
					mde.setCurrent(jObj.getDouble("I"));
				}
				if (jObj.has("T"))
				{
					Log.d(TAG, "T found");
					add = true;
					mde.setTemp(jObj.getDouble("T"));
				}
				if (jObj.has("t"))
				{
					add = true;
					Log.d(TAG, "t found");
					mde.setTime(jObj.getLong("t"));
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
					getActivity().invalidateOptionsMenu();
				}
				if (jObj.has("F"))
				{
					Log.d(TAG, "F found");
					if (jObj.getBoolean("F"))
					{
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
						}
					}
				}
				if (jObj.has("E"))
				{
					Log.d(TAG, "E found");
					Log.e(TAG, "ERROR: " + jObj.getString("E"));
				}
				if (add)
				{
					Log.d(TAG, "Adding mde: " + mde.toString());
					mDataSet.add(mde);
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
		ModuleDataEntry entry = null;
		if (mUseDummyData)
		{
			entry = mDummyData.getNextEntry();
			mDataSet.add(entry); // For UI enabling, etc
		}
		else
		{
			if (mDataSet.isEmpty())
			{
				Log.e(TAG, "Trying to update graph with an empty dataset! Returning!");
				return;
			}
			entry = mDataSet.get(mDataSet.size()-1);
		}
		mGraph.updateGraph(entry);

		// Make sure all our UI data buttons are enabled
		// if we're going to be continuously updating
		// or just have a single data point
		toggleBTUI(!mDataSet.isEmpty());
	}

	/**
	 * Reset input and output streams and make sure socket is closed.
	 * This method will be used during shutdown() to ensure that the
	 * connection is properly closed during a shutdown.
	 */
	private void disconnect()
	{
		// Also disconnects the message system
		mBluetooth.disconnect();

		txtReceived.append("\nBluetooth Disconnected!\n");
	}

	private void toggleBTUI(boolean enable)
	{
		if (btnWriteToFile != null) btnWriteToFile.setEnabled(enable);
		if (btnEmail != null) btnEmail.setEnabled(enable);
		if (btnScreenshot != null) btnScreenshot.setEnabled(enable);
	}

	private void saveFile()
	{
		// Create filename
		SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy_HHmmss", Locale.getDefault());

		// TODO: add preferences to check for local enum between .txt and .csv
		String filename = "IV-data-" + df.format(new Date()) + ".txt";

		// Try to write to SDcard
		if (canWriteOnExternalStorage())
		{
			// TODO: move this list into a method that rebuilds it from the file directory on request
			if (!mFilenames.contains(filename))
			{
				mFilenames.add(filename);
			}

			File file = getStorageDir("Lilac");
			if (file == null)
			{
				Log.e(TAG, "Could not create lilac folder to write in");
				return;
			}

			ArrayList<String> data = mDataSet.getStringsForFile(ModuleDataSet.EDataSeparator.TAB);
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
				Toast.makeText(getActivity(), filename + " saving successful!",
				               Toast.LENGTH_SHORT).show();
			}
			catch (IOException e)
			{
				Log.e(TAG, "Could not write file: " + filename + ", trying alternate way");
				try
				{
					FileOutputStream fout = getActivity().getApplicationContext().openFileOutput(filename, Context.MODE_APPEND);
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
						Toast.makeText(getActivity(), filename + " saving failed!",
						               Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
		else
		{
			Log.e(TAG, "Could not write to external storage!");
			Toast.makeText(getActivity(), filename + " saving failed!",
			               Toast.LENGTH_SHORT).show();
		}
	}
	
	public void saveImage()
	{
		SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy_HHmmss", Locale.getDefault());
		String filename = "IV-data-" + df.format(new Date()) + ".jpg";
		if (mGraph.saveToGallery(filename, 100))
		{
			Toast.makeText(getActivity(), "Saving " + filename + " successful!", Toast.LENGTH_SHORT).show();
		}
		else
		{
			Toast.makeText(getActivity(), "Saving " + filename + " failed!", Toast.LENGTH_SHORT).show();
		}
	}
}


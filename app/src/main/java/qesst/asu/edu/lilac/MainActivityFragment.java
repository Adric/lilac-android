package qesst.asu.edu.lilac;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.JsonReader;
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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ValueFormatter;

import org.json.JSONObject;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements IMessageCallback
{
	private final static String TAG = "MainActivityFragment";

	//private View mView;

	private boolean mUseDummyData = false;

	private boolean mEndMeasurement = false;

	private int mSingleEntryIndex;

	private boolean mMeasuring;

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


	// UI
	private Button btnConnect = null;
	private Button btnMeasure = null;
	private TextView txtReceived = null;
	private Button btnClear = null;
	private Button btnFlags = null;
	private TextView lblVoc = null;
	private TextView lblIsc = null;
	private Menu mMenu = null;
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
		mSingleEntryIndex = 0;
		mVoc = 0.f;
		mCallCount = 0;
		mFlags = EnumSet.noneOf(EFlag.class);

		// Default flags we want
		mFlags.add(EFlag.VOLTAGE);
		mFlags.add(EFlag.CURRENT);
		mFlags.add(EFlag.AVERAGE);
		mFlags.add(EFlag.CONTINUOUS); // DEBUG REMOVE ME
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_main, container, false);
		//mView = view;

		// set up the buttons
		//btnVoltage = (Button) view.findViewById(R.id.btn_voltage);
		//btnCurrent = (Button) view.findViewById(R.id.btn_current);
		//btnSweep = (Button) view.findViewById(R.id.btn_sweep);
		btnConnect = (Button) view.findViewById(R.id.btn_connect);
		btnWriteToFile = (Button) view.findViewById(R.id.btn_to_file);
		btnEmail = (Button) view.findViewById(R.id.btn_email);
		btnScreenshot = (Button) view.findViewById(R.id.btn_screenshot);
		//btnAverage = (Button) view.findViewById(R.id.btn_average);
		//btnDebug = (Button) view.findViewById(R.id.btn_debug_output);
		btnMeasure = (Button) view.findViewById(R.id.btn_measure);
		//btnContinuous = (Button) view.findViewById(R.id.btn_continuous);
		//btnTemp = (Button) view.findViewById(R.id.btn_temp);
		txtReceived = (TextView) view.findViewById(R.id.txt_received);
		btnClear = (Button) view.findViewById(R.id.btn_clear);
		btnFlags = (Button) view.findViewById(R.id.btn_flags);
		lblVoc = (TextView) view.findViewById(R.id.lbl_voc);
		lblIsc = (TextView) view.findViewById(R.id.lbl_isc);

		btnMeasure.setEnabled(false);

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


		btnConnect.setText("Connect");

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
			}
		});

		btnWriteToFile.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				// Generate a file
				/*String[] output = txtInfo.getText().toString().split(Character.toString('\n'));
				String text = "";
				for (int i = 0; i < output.length; ++i)
				{
					text += output[i];
					if (i < output.length - 1)
					{
						text += "\n";
					}
				}
				*/

				String text = "";
				String[] output2 = txtReceived.getText().toString().split(Character.toString('\n'));
				for (int i = 0; i < output2.length; ++i)
				{
					text += output2[i];
					if (i < output2.length - 1)
					{
						text += "\n";
					}
				}


				//String text = txtInfo.getText() + "\n" + txtRawData.getText();

				// Create filename
				SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy_HHmmss", Locale.getDefault());
				String filename = "IV-data-" + df.format(new Date()) + ".txt";
				//String filename = "data.csv";
				// Add it to the list
				// TODO: build this array from the directory structure and prompt the user
				// to select a file

				// new way:
				if (canWriteOnExternalStorage())
				{
					// new new way
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

					/*
					if (!file.exists())
					{
						try
						{
							file.createNewFile();
						}
						catch (Exception eee)
						{
							Log.e(TAG, "File doesn't exist but can't create a new one!");
						}
					}*/

					try
					{
						/*
						FileWriter fw = new FileWriter(file.getAbsoluteFile());
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(text);
						bw.close();*/

						FileOutputStream out = new FileOutputStream(file + File.separator + filename);
						ArrayList<String> data = mDataSet.getStringsForFile(ModuleDataSet.EDataSeparator.TAB);
						for (String s : data)
						{
							out.write((s + "\n").getBytes());
						}
						//out.write(text.getBytes());
						out.close();
						Log.e(TAG, filename + " written");
						Toast.makeText(getActivity(), filename + " saved successfully!",
						               Toast.LENGTH_SHORT).show();
					}
					catch (IOException e)
					{
						Log.e(TAG, "Could not write file: " + filename + ", trying alterate way");
						try
						{
							FileOutputStream fout = getActivity().getApplicationContext().openFileOutput(filename, Context.MODE_APPEND);
							OutputStreamWriter osw = new OutputStreamWriter(fout);

							// Write the string to the file
							osw.write(text);
							// ensure that everything is really written out and close
							//osw.flush();// ensure that everything is really written out and close
							osw.close();
						}
						catch (IOException ioe)
						{
							Log.e(TAG, "OutputStreamWriter could not write " + filename);
							//e.printStackTrace();
							try
							{
								File root = new File(Environment.DIRECTORY_DOWNLOADS);
								File gpxfile = new File(root, filename);
								FileWriter writer2 = new FileWriter(gpxfile);
								writer2.append(text);
								writer2.flush();
								writer2.close();
							}
							catch (IOException ioe2)
							{
								Log.e(TAG, "FileWriter failed to write " + filename + " too!");
							}
						}
					}

					/*
					// get the path to sdcard
					File sdcard = Environment.getExternalStorageDirectory(); // to this path add a new directory path
					File dir = new File(sdcard.getAbsolutePath() + "Lilac"); // create this directory if not already created
					dir.mkdir();// create the file in which we will write the contents
					File file = new File(dir, filename);
					try
					{
						FileOutputStream os = new FileOutputStream(file);
						//String data = “This is the content of my file”;
						os.write(text.getBytes());
						os.flush();// ensure that everything is really written out and close
						os.close();
					}
					catch (IOException ioe)
					{
						ioe.printStackTrace();
					}*/
				}
				else
				{
					Log.e(TAG, "Could not write to external storage!");
				}


				// old way
				/*
				if (!mFilenames.contains(filename))
				{
					mFilenames.add(filename);
				}

				File file = new File(getFilesDir() + File.separator + filename);
				try
				{
					FileOutputStream out = new FileOutputStream(file);
					out.write(text.getBytes());
					out.close();
					Log.d(TAG, "Wrote output to: " + getFilesDir() + File.separator + filename);
				}
				catch (IOException e)
				{
					Log.e(TAG, "Could not write file: " + e.getLocalizedMessage());
				}


				try
				{
					// Alternate deprecated file output
					// TODO: add in if/else
					/*FileOutputStream fout = openFileOutput(filename, MODE_WORLD_READABLE);
					OutputStreamWriter osw = new OutputStreamWriter(fout);

					// Write the string to the file
					osw.write(text);
					// ensure that everything is really written out and close
					osw.flush();// ensure that everything is really written out and close
					osw.flush();
					osw.close();*/

					/*Log.d(TAG, "text: " + text);

					// Verify we wrote the file correctly
					FileInputStream fin = openFileInput(filename);
					InputStreamReader isr = new InputStreamReader(fin);
					// Prepare a char-Array that will hold the chars we read back in
					char[] input_buffer = new char[text.length()];
					// Fill the Buffer with data from the file
					isr.read(input_buffer);
					// Transform the chars to a String
					String input_str = new String(input_buffer);

					// Check if we read back the same chars that we had written out
					boolean is_same = text.equals(input_str);

					Log.i(TAG, "Write success: " + is_same);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}*/
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

			//TODO: pass this the View of the graphing Fragment
			public void onClick(View v)
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
				/*if (canWriteOnExternalStorage())
				{
					File file = getScreenshotDir("Lilac");
					if (file == null)
					{
						Log.e(TAG, "Could not create lilac folder to write in");
						return;
					}
					final View rootView = view.findViewById(android.R.id.content).getRootView();
					rootView.setDrawingCacheEnabled(true);
					Bitmap bitmap = rootView.getDrawingCache();

					SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy_HHmmss");
					String filename = "IV-data-" + df.format(new Date()) + ".png";
					File imagePath = new File(file + File.separator + filename);
					FileOutputStream fos;
					try
					{
						fos = new FileOutputStream(imagePath);
						bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
						fos.flush();
						fos.close();
						Toast.makeText(view.getBaseContext(), filename + " saved successfully!",
						               Toast.LENGTH_SHORT).show();
					}
					catch (FileNotFoundException e)
					{
						Log.e(TAG, e.getMessage(), e);
					}
					catch (IOException e)
					{
						Log.e(TAG, e.getMessage(), e);
					}
					bitmap = null;
				}
				/*
				// Create the bitmap
				final Bitmap bitmap = Bitmap.createBitmap(v.getWidth(),
				                                    v.getHeight(), Bitmap.Config.ARGB_8888);
				final Canvas canvas = new Canvas(bitmap);

				// Get current theme to know which background to use
				final Resources.Theme theme = getTheme();
				final TypedArray ta = theme
						.obtainStyledAttributes(new int[] { android.R.attr.windowBackground });
				final int res = ta.getResourceId(0, 0);
				final Drawable background = getResources().getDrawable(res);

				// Draw background
				background.draw(canvas);

				// Draw view
				v.draw(canvas);

				// Save to file
				FileOutputStream fos = null;
				try
				{
					final String screenshot_folder = getFilesDir() + File.separator;
					final File sddir = new File(screenshot_folder);
					final String filename = "Graph_" + System.currentTimeMillis() + ".jpg";
					if (!sddir.exists())
					{
						sddir.mkdirs();
					}
					fos = new FileOutputStream(screenshot_folder + filename);
					if (fos != null)
					{
						if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos))
						{
							Log.d(TAG, "Compress/Write failed");
						}
						else
						{
							Log.d(TAG, "Saved screenshot: " + screenshot_folder + filename);
						}
						fos.flush();
						fos.close();

						// Temporary code to open the image file for viewing:
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_VIEW);
						// TODO "imagine/jpeg" for Android 2.3
						intent.setDataAndType(Uri.parse("file://" + screenshot_folder + filename), "image/*");
						try
						{
							startActivity(intent);
						}
						catch(ActivityNotFoundException e)
						{
							e.printStackTrace();
						}
					}
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}*/
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
		inflater.inflate(R.menu.menu_fragment, menu);
		super.onCreateOptionsMenu(menu, inflater);

		for (int i = 0; i < menu.size(); ++i)
		{
			MenuItem item = menu.getItem(i);
			if (item.isCheckable())
			{
				item.setEnabled(mBluetooth.isConnected());
			}
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

	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		if (menu != null && mBluetooth.isConnected())
		{
			// Update checked-ness based on flags we've received back
			for (int i = 0; i < menu.size(); ++i)
			{
				MenuItem item = menu.getItem(i);
				//item.setEnabled(true);
				item.setChecked(mFlags.contains(getEFlagFromMenuItem(item)));
			}
		}
		else
		{
			Log.d(TAG, "onPrepareOptionsMenu when not connected!");
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
			output += str;// + "\n";
			txtReceived.append(output + "\n");

			final String TAG = "JsonParsing";

			try
			{
				// Ugly parsing, refactor!
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
		//mDataSet.add(MessageData.stringToModuleData(output));
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
}


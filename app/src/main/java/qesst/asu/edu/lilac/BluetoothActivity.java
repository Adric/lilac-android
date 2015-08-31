package qesst.asu.edu.lilac;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Controls the Bluetooth connection
 */
public class BluetoothActivity extends Activity
{
	private final static String TAG = "BluetoothActivity";

	private Activity mParentActivity = null;
	private Fragment mParentFragment = null;

	// MAC-address of Bluetooth module (default)
	private String mAddress = "30:14:09:03:01:86";
	// Device name
	private String mDeviceName = Constants.DEVICE_NAME;
	// Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Bluetooth socket
	private BluetoothSocket mBluetoothSocket = null;
	// Bluetooth device used for connections
	private BluetoothDevice mDevice = null;
	// List of possible devices
	ArrayList<String> mArrayAdapter = new ArrayList<String>();

	// Threaded message system
	private MessageSystem mMessageSystem = null;

	public BluetoothActivity(Activity activity)
	{
		mParentActivity = activity;
	}

	public BluetoothActivity(Fragment fragment)
	{
		mParentFragment = fragment;
	}

	/*
	 * Create the actual Bluetooth connection for the application
	 */
	public void connect()
	{
		Log.d(TAG, "connect");
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();		// get Bluetooth adapter
		if (mBluetoothAdapter == null)
		{
			// Device does not support Bluetooth
			Log.e(TAG, getString(R.string.bluetooth_not_supported));
			Toast.makeText(getBaseContext(), getString(R.string.bluetooth_not_supported), Toast.LENGTH_LONG).show();
			return;
		}

		/*
			If this method returns false, then Bluetooth is disabled.
			To request that Bluetooth be enabled, call startActivityForResult()
			with the ACTION_REQUEST_ENABLE action Intent.
			This will issue a request to enable Bluetooth through the system settings
			(without stopping your application)
		 */
		if (!mBluetoothAdapter.isEnabled())
		{
			// Bluetooth is not enabled, request enabling from the user
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
		}
		else
		{
			// Bluetooth is enabled, attempt pairing
			pairBTDevices();
		}

		/*
		Block the main thread until a connection is established
		TODO: move into its own thread
		 */
	}

	public void disconnect()
	{
		mBluetoothAdapter = null;

		// Bluetooth socket
		try
		{

			mBluetoothSocket.close();
			mBluetoothSocket = null;
		}
		catch (Exception e)
		{

		}

		// Bluetooth device used for connections
		mDevice = null;

		// Threaded message system
		mMessageSystem = null;
	}

	public boolean isConnected()
	{
		return mBluetoothSocket != null && mBluetoothSocket.isConnected();
	}

	public void write(String message)
	{
		if (mMessageSystem != null)
		{
			mMessageSystem.write(message);
		}
	}

	public void write(char message)
	{
		if (mMessageSystem != null)
		{
			mMessageSystem.write(message);
		}
	}

	/**
	 * Callback for enabling Bluetooth is disabled when trying to connnect
	 * @param requestCode   Code to request Bluetooth enabling
	 * @param resultCode    Code for user's selection
	 * @param data          Optional data
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.d(TAG, "onActivityResult");
		// Process the user's choice
		if (requestCode == Constants.REQUEST_ENABLE_BT)
		{
			switch(resultCode)
			{
				case Activity.RESULT_CANCELED:
					mBluetoothAdapter.cancelDiscovery();
					break;
				case Activity.RESULT_OK:
					pairBTDevices();
					break;
				default:
					// TODO: add logic for here
					break;
			}
		}
	}

	/**
	 * Attempt to pair with the DogBlue device by finding its MAC address
	 */
	protected void pairBTDevices()
	{
		// Attempt to pair a device first
		Log.d(TAG, "pairBTDevices");

		/*
		try
		{
			Log.d("pairDevice()", "Start Pairing...");
			Method m = mBluetoothAdapter.getClass().getMethod("createBond", (Class[]) null);
			m.invoke(mBluetoothAdapter, (Object[]) null);
			Log.d("pairDevice()", "Pairing finished.");
		}
		catch (Exception e)
		{
			Log.e("pairDevice()", e.getMessage());
		}*/

		Set<BluetoothDevice> paired_devices = mBluetoothAdapter.getBondedDevices();
		Log.d(TAG, paired_devices.size() + " paired devices found");

		// Try to find the mac address for the Arduino's Bluetooth adapter
		final String[] mac_address = { null };

		// If there are paired devices
		if (paired_devices.size() > 0)
		{
			// Loop through paired devices
			for (BluetoothDevice device : paired_devices)
			{
				mac_address[0] = device.getAddress();
				// Add the name and address to an array adapter to show in a ListView
				mArrayAdapter.add(device.getName() + "\n" + mac_address[0]);

				// if the name matches the one we're looking for, store the mac address
				// TODO: set default name in preferences
				if (device.getName().toLowerCase().contains(mDeviceName))
				{
					Log.e(TAG, mDeviceName + ", address: " + mac_address[0]);
				}
				else
				{
					// List other known devices
					Log.d(TAG, "Device: " + device.getName() + ", address: " + mac_address[0]);
				}
			}
		}

		// The Arduino wasn't found anywhere, try to pair by name
		if (mac_address[0] == null)
		{
			Log.d(TAG, "Arduino not found, trying to pair by name");
			mArrayAdapter.clear();

			// Create a BroadcastReceiver for ACTION_FOUND
			final BroadcastReceiver mReceiver = new BroadcastReceiver()
			{
				public void onReceive(Context context, Intent intent)
				{
					String action = intent.getAction();
					// When discovery finds a device
					if (BluetoothDevice.ACTION_FOUND.equals(action))
					{
						// Get the BluetoothDevice object from the Intent
						BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

						mac_address[0] = device.getAddress();
						// Add the name and address to an array adapter to show in a ListView
						mArrayAdapter.add(device.getName() + "\n" + mac_address[0]);

						if (device.getName().toLowerCase().contains(mDeviceName))
						{
							Log.e(TAG, mDeviceName + ", address: " + mac_address[0]);
						}
						else
						{
							// List other known devices
							Log.d(TAG, "Device: " + device.getName() + ", address: " + mac_address[0]);
						}
					}
				}
			};

			// Register the BroadcastReceiver
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			this.registerReceiver(mReceiver, filter); // TODO: Don't forget to unregister during onDestroy
		}

		if (mac_address[0] == null)
		{
			// use the default
			// TODO: set in the preferences
			Log.e(TAG, "Device address not found, using default: " + mAddress);
			mac_address[0] = mAddress;
		}

		/*
		* Two things are needed to make a connection:
		*
		*   A MAC address, which we got above.
		*   A Service ID or UUID.  In this case we are using the UUID for SPP.
		*/
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac_address[0]);

		try
		{
			mBluetoothSocket = device.createRfcommSocketToServiceRecord(Constants.MY_UUID);
		}
		catch (IOException ioe)
		{
			Log.e(TAG, getString(R.string.bluetooth_rf_comm_create_failed) + ": " + ioe.getLocalizedMessage());
			Toast.makeText(getBaseContext(), getString(R.string.bluetooth_rf_comm_create_failed) + ": " + ioe.getLocalizedMessage(), Toast.LENGTH_LONG).show();

			// If unable to create an RFComm socket, try the more generic way
			try
			{
				mBluetoothSocket = createBluetoothSocket(device);
			}
			catch (IOException ioe2)
			{
				Log.e(TAG, getString(R.string.bluetooth_socket_create_failed) + ": " + ioe2.getLocalizedMessage());
				Toast.makeText(getBaseContext(), getString(R.string.bluetooth_socket_create_failed) + ": " + ioe2.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			}
		}

		// Discovery consumes a lot of resources, so we can kill it now
		mBluetoothAdapter.cancelDiscovery();

		// Try to establish our connection
		// TODO: this currently blocks the main thread, put in its own threaded system
		Log.d(TAG, "Connecting");

		try
		{
			mBluetoothSocket.connect();
			Log.d(TAG, "Connected!");
		}
		catch (IOException e)
		{
			// Connection failed, send a message to the user and try to close the socket
			try
			{
				Log.d(TAG, "Connection failure, closing BluetoothSocket");
				mBluetoothSocket.close();
			}
			catch (IOException e2)
			{
				Log.e(TAG, getString(R.string.bluetooth_socket_close_failed) + ": " + e2.getLocalizedMessage());
				Toast.makeText(getBaseContext(), getString(R.string.bluetooth_socket_close_failed) + ": " + e2.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			}
		}

		// If we connected, initialize the messaging system
		if (mBluetoothSocket.isConnected())
		{
			if (mParentActivity != null)
			{
				mMessageSystem = new MessageSystem(mParentActivity, mBluetoothSocket);
				mMessageSystem.start();
			}
			else if (mParentFragment != null)
			{
				mMessageSystem = new MessageSystem(mParentFragment, mBluetoothSocket);
				mMessageSystem.start();
			}
			else
			{
				Log.e(TAG, "No parent to initialize message system with!");
			}
		}
	}

	/**
	 * Create the BluetoothSocket from a BluetoothDevice
	 * @param device Device to create a socket for
	 * @return returns a socket
	 * @throws IOException
	 */
	private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
	{
		Log.d(TAG, "createBluetoothSocket");
		if (Build.VERSION.SDK_INT >= 10)
		{
			try
			{
				final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
				return (BluetoothSocket) m.invoke(device, Constants.MY_UUID);
			}
			catch (Exception e)
			{
				Log.e(TAG, "Could not create Insecure RFComm Connection", e);
			}
		}
		return  device.createRfcommSocketToServiceRecord(Constants.MY_UUID);
	}

}

package qesst.asu.edu.lilac;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * JSON-based message system
 */
public class MessageSystem3 extends Thread
{
	// Input and output streams
	private InputStream mInputStream;
	private OutputStream mOutputStream;

	// StringBuilder used to process info
	private StringBuilder mStringBuilder = new StringBuilder();

	// Bluetooth socket and event handler
	private BluetoothSocket mSocket;
	private static Handler mHandler;

	// Status for Handler
	private static final int RECEIVE_MESSAGE = 1;

	// Message system tag
	private static final String TAG = "MessageSystem";

	// Activity callback
	IMessageCallback mActivityCallback;

	public MessageSystem3(Fragment fragment, BluetoothSocket socket)
	{
		// Assign callback fragment
		if (fragment != null)
		{
			mActivityCallback = (IMessageCallback) fragment;
		}
		else
		{
			Log.d(TAG, "\tAcvitiy is null!");
		}

		init(socket);
	}

	public MessageSystem3(Activity activity, BluetoothSocket socket)
	{
		// Assign callback activity
		if (activity != null)
		{
			mActivityCallback = (IMessageCallback) activity;
		}
		else
		{
			Log.d(TAG, "\tAcvitiy is null!");
		}
		init(socket);
	}

	private void init(BluetoothSocket socket)
	{
		if (mActivityCallback == null) return;

		// Pause this thread so that UI thread get some time to breathe
		try
		{
			Thread.sleep(150);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		// Assign socket
		if (socket != null)
		{
			mSocket = socket;
		}
		else
		{
			Log.d(TAG, "\tBluetoothSocket is null!");
		}

		// Create the handler
		mHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
					case RECEIVE_MESSAGE:
						byte[] buffer = (byte[]) msg.obj;
						String message = new String(buffer, 0, msg.arg1);
						//String message = msg.obj.toString();

						// Fix any formatting errors
						//message.trim();
						// Remove any Windows-only linefeeds before appending
						//message = message.replace(Character.toString('\r'), "");
						mStringBuilder.append(message);

						// Strip out any leading whitespace chars
						while (mStringBuilder.indexOf("\n") == 0 ||
						       mStringBuilder.indexOf("\r") == 0 ||
						       mStringBuilder.indexOf("\t") == 0 ||
						       mStringBuilder.indexOf(" ") == 0)
						{
							Log.d(TAG, "Deleting whitespace char at index 0");
							Log.d(TAG, "Old mStringBuilder: " + mStringBuilder);
							mStringBuilder.delete(0, 1);
							Log.d(TAG, "New mStringBuilder: " + mStringBuilder);
						}

						// Find the end of line
						//int eol = mStringBuilder.indexOf(Character.toString('\n'));
						int eol = mStringBuilder.indexOf("\n");
						Log.d(TAG, "EOL index: " + eol);
						while (eol > 0)
						{
							Log.d(TAG, "EOL found at index: " + eol);
							// If reached end of line, extract the string
							String str = mStringBuilder.substring(0, eol);
							//mStringBuilder.delete(0, mStringBuilder.length());
							mStringBuilder.delete(0, eol);
							Log.d(TAG, "About to call call() with: " + str);

							// TODO: remove UI updating from here!!!
							ArrayList arr = new ArrayList<String>();
							arr.add(str);
							mActivityCallback.call(arr);
							arr = null;

							//eol = mStringBuilder.indexOf(Character.toString('\n'));
							eol = mStringBuilder.indexOf("\n");
						}
						Log.d(TAG, "\tReceived: " + mStringBuilder.toString() + ", byte count:" + msg.arg1);
						break;

					default:
						Log.d(TAG, "Unknown message case: " + msg.what);
						break;
				}
			}
		};

		// Streams are final, so use temporary variables for assignment
		InputStream in = null;
		OutputStream out = null;

		try
		{
			in = socket.getInputStream();
			out = socket.getOutputStream();
		}
		catch (IOException e)
		{
			Log.d(TAG, e.getLocalizedMessage());
		}
		finally
		{
			mInputStream = in;
			mOutputStream = out;
		}
	}

	/**
	 * Read input from an InputStream
	 */
	public void run()
	{
		if (mHandler == null)
		{
			Log.d(TAG, "\tCalling run() with null Handler!");
			return;
		}

		// Store the stream in a buffer for reading
		byte[] buffer = new byte[256];
		// Number of bytes read() returns
		int bytes;

		// Loop through the InputStream
		while (true)
		{
			try
			{
				// Get the number of bytes and the message in buffer
				bytes = mInputStream.read(buffer);
				// Send the message to the queue Handler
				mHandler.obtainMessage(RECEIVE_MESSAGE, bytes, -1, buffer).sendToTarget();
			}
			catch (IOException e)
			{
				// Done reading
				break;
			}
		}
	}

	/**
	 * Write data to OutputStream
	 * Sends data to a remote device
	 */
	public void write(String message)
	{
		Log.d(TAG, "\tWriting: " + message);
		// Convert into a buffer to write to the output stream
		// Need the newline so the arduino knows when the end of the input is
		byte[] buffer = (message /*+ '\0'*/ + '\n').getBytes();
		try
		{
			mOutputStream.write(buffer);
		}
		catch (IOException e)
		{
			Log.d(TAG, "\tWrite error: " + e.getLocalizedMessage());
		}
	}

	public void write(char msg)
	{
		Log.d(TAG, "\tWriting: " + msg);
		try
		{
			mOutputStream.write(msg);
			mOutputStream.write('\0');
			mOutputStream.write('\n');
		}
		catch (IOException e)
		{
			Log.d(TAG, "\tWrite error: " + e.getLocalizedMessage());
		}
	}
}
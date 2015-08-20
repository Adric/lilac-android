package qesst.asu.edu.lilac;

import android.util.Log;

import java.util.ArrayList;

/**
 * This class converts a string from the arduino to a set of data objects
 */
public class MessageData
{
	private final static String TAG = "MessageData";
	private static StringBuffer mBuffer = new StringBuffer("");

	public static ArrayList<ModuleDataEntry> stringToModuleData(String data)
	{
		// Keep appending string data to the buffer until we get a terminating char
		mBuffer.append(data);
		int terminatingIndex = data.indexOf('L');
		if (terminatingIndex == -1)
		{
			Log.d(TAG, "Terminating index not found. Current buffer: \"" + mBuffer + "\"");
			return null;
		}
		else
		{
			Log.d(TAG, "Terminating index found in data: \"" + data + "\"");
		}

		// We have a terminating char sent, get the buffer's index
		terminatingIndex = mBuffer.indexOf("L");
		Log.d(TAG, "Terminating index: " + terminatingIndex);

		String dataLine = mBuffer.substring(terminatingIndex).trim();
		Log.d(TAG, "dataLine: \"" + dataLine + "\"");

		// Clear out the old data
		Log.d(TAG, "Old buffer: \"" + mBuffer + "\"");
		mBuffer.delete(0, terminatingIndex + 1);
		Log.d(TAG, "New buffer: \"" + mBuffer + "\"");

		// Create the array list
		ArrayList<ModuleDataEntry> dataEntries = new ArrayList<ModuleDataEntry>();

		// Parse our data based on whitespace
		String[] entries = mBuffer.toString().trim().split("\\s+");
		for (int i = 0; i < entries.length; ++i)
		{
			Log.d(TAG, "Received entry #" + i + ": " + entries[i]);

			// If we have a flag, make sure the menu reflects this
		}
		/*
		 Data should look like this (three parts):
		    TYPE CHAR
		    DATA
		    DATA LENGTH
		 */
		if (entries.length % 3 != 0)
		{
			Log.e(TAG, "Invalid data length: " + entries.length + ", skipping");
			return null;
		}

		for (int i = 0; i < entries.length-2; i += 3)
		{
			// Make sure we're starting with a variable type
			String type = entries[i].trim();
			if (type.length() != 1)
			{
				Log.e(TAG, "Invalid type length: " + type.length() + ", skipping " + type);
				continue;
			}

			// Check for errors
			if (type.equals("!"))
			{
				Log.e(TAG, "Error flag set: " + entries[i+1]);
				continue;
			}

			// No errors, so take the value
			double value = 0;
			try
			{
				value = Double.valueOf(entries[i+1]);
			}
			catch(NumberFormatException nfe)
			{
				Log.e(TAG, "Invalid data (expected a double): " + entries[i+1] + ", skipping");
				continue;
			}

			// If the value's length doesn't match what we expect, discard
			try
			{
				// Check for length mismatch
				if (entries[i+1].length() != Integer.valueOf(entries[i+2]))
				{
					throw new Exception();
				}
			}
			catch(Exception e)
			{
				Log.e(TAG, "Invalid data length (mismatch). Expected " + entries[i+2] + " but " + entries[i+1] + " has a different length!");
				continue;
			}

			ModuleDataEntry mde = new ModuleDataEntry(EFlag.toType(type.charAt(0)), value);
			Log.d(TAG, "New ModuleDataEntry created: " + mde.toString());
			dataEntries.add(mde);
		}

		return dataEntries;
	}

}

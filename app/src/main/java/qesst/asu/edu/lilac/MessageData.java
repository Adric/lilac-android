package qesst.asu.edu.lilac;

import android.util.Log;

import java.util.ArrayList;

/**
 * This class converts a string from the arduino to a set of data objects
 */
public class MessageData
{
	private final static String TAG = "MessageData";

	public static ArrayList<ModuleDataEntry> stringToModuleData(String data)
	{
		ArrayList<ModuleDataEntry> dataEntries = new ArrayList<ModuleDataEntry>();
		data = data.trim();
		String[] entries = data.split("\\s+"); // are these spaces or tabs?
		for (int i = 0; i < entries.length; ++i)
		{
			Log.e(TAG, "Received entry #" + i + ": " + entries[i]);
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
				if (Integer.valueOf(entries[i + 2]) != entries[i + 1].length())
				{
					throw new Exception();
				}
			}
			catch(Exception e)
			{
				Log.e(TAG, "Invalid data length (mismatch). Expected " + entries[i+2] + " but " + entries[i+3] + " has a different length!");
				continue;
			}

			dataEntries.add(new ModuleDataEntry(EMeasurementType.toType(type.charAt(0)), value));
		}
		return dataEntries;
	}

}

package qesst.asu.edu.lilac;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;


/**
 * Data set for storing module data entries
 */
public class ModuleDataSet
{
	private ArrayList<ModuleData> mModuleData = new ArrayList<ModuleData>();

	public ModuleDataSet()
	{
	}

	public ModuleDataSet(ArrayList<ModuleData> data)
	{
		add(data);
	}

	public ModuleDataSet(ModuleData moduleData)
	{
		add(moduleData);
	}

	public void add(ModuleData moduleData)
	{
		mModuleData.add(moduleData);
	}

	public void add(ArrayList<ModuleData> data)
	{
		if (data != null)
		{
			for (int i = 0; i < data.size(); ++i)
			{
				if (data.get(i) != null)
				{
					mModuleData.add(data.get(i));
				}
			}
		}
	}

	// doesn't work
	public void remove(ModuleData moduleData)
	{
		mModuleData.remove(moduleData);
	}

	public void remove(double value, EFlag type)
	{
		search: for (ModuleData entry : mModuleData)
		{
			switch(type)
			{
				case VOLTAGE:
					if (entry.hasVoltage() && entry.getVoltage() == value)
					{
						entry.clearVoltage();
						break search;
					}
					break;
				case CURRENT:
					if (entry.hasCurrent() && entry.getCurrent() == value)
					{
						entry.clearCurrent();
						break search;
					}
					break;
				case TEMP:
					if (entry.hasTemp() && entry.getTemp() == value)
					{
						entry.clearTemp();
						break search;
					}
					break;
				default:
					continue;
			}
		}
	}

	public int size()
	{
		return mModuleData.size();
	}

	public ModuleData get(int index)
	{
		if (index < 0 || index >= mModuleData.size())
		{
			// TODO: throw an exception
			return null;
		}
		return mModuleData.get(index);
	}

	double getStdev(EFlag type)
	{
		// Get the mean
		double mean = getMean(type);

		// Get the variance
		int count = 0;
		double variance = 0;
		for (ModuleData entry : mModuleData)
		{
			switch(type)
			{
				case VOLTAGE:
					if (entry.hasVoltage())
					{
						count++;
						variance += Math.pow(mean - entry.getVoltage(), 2);
					}
					break;
				case CURRENT:
					if (entry.hasCurrent())
					{
						count++;
						variance += Math.pow(mean - entry.getCurrent(), 2);
					}
					break;
				case TEMP:
					if (entry.hasTemp())
					{
						count++;
						variance += Math.pow(mean - entry.getTemp(), 2);
					}
					break;
				default:
					continue;
			}
		}

		if (count <= 0)
		{
			return 0;
		}

		variance /= count;

		return Math.sqrt(variance);
	}

	double getMean(EFlag type)
	{
		int count = 0;
		double sum = 0;
		for (ModuleData entry : mModuleData)
		{
			switch(type)
			{
				case VOLTAGE:
					if (entry.hasVoltage())
					{
						count++;
						sum += entry.getVoltage();
					}
					break;
				case CURRENT:
					if (entry.hasCurrent())
					{
						count++;
						sum += entry.getCurrent();
					}
					break;
				case TEMP:
					if (entry.hasTemp())
					{
						count++;
						sum += entry.getTemp();
					}
					break;
				default:
					continue;
			}
		}

		if (count <= 0) return sum;
		return (sum/count);
	}

	public ArrayList<Double> getData(EFlag type)
	{
		ArrayList<Double> data = new ArrayList<Double>();
		for (ModuleData entry : mModuleData)
		{
			switch(type)
			{
				case VOLTAGE:
					if (entry.hasVoltage())
					{
						data.add(entry.getVoltage());
					}
					break;
				case CURRENT:
					if (entry.hasCurrent())
					{
						data.add(entry.getCurrent());
					}
					break;
				case TEMP:
					if (entry.hasTemp())
					{
						data.add(entry.getTemp());
					}
					break;
				default:
					continue;
			}
		}
		return data;
	}

	public Entry getEntry(int index, EFlag type)
	{
		try
		{
			ModuleData entry = get(index);
			if (entry == null) return null;

			switch(type)
			{
				case VOLTAGE:
					if (entry.hasVoltage())
					{
						return (new Entry((float)entry.getVoltage(), index));
					}
					break;
				case CURRENT:
					if (entry.hasCurrent())
					{
						return (new Entry((float)entry.getCurrent(), index));
					}
					break;
				case TEMP:
					if (entry.hasTemp())
					{
						return (new Entry((float)entry.getCurrent(), index));
					}
					break;
				default:
					return null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<Entry> getEntries(EFlag type)
	{
		ArrayList<Entry> entries = new ArrayList<Entry>();

		int count = 0;
		for (ModuleData entry : mModuleData)
		{
			switch(type)
			{
				case VOLTAGE:
					if (entry.hasVoltage())
					{
						count++;
						entries.add(new Entry((float)entry.getVoltage(), count));
					}
					break;
				case CURRENT:
					if (entry.hasCurrent())
					{
						count++;
						entries.add(new Entry((float)entry.getCurrent(), count));
					}
					break;
				case TEMP:
					if (entry.hasTemp())
					{
						count++;
						entries.add(new Entry((float)entry.getCurrent(), count));
					}
					break;
				default:
					continue;
			}
		}

		return entries;
	}

	public ArrayList<String> getStrings(EFlag type)
	{
		ArrayList<String> strings = new ArrayList<String>();

		int count = 0;
		for (ModuleData entry : mModuleData)
		{
			switch(type)
			{
				case VOLTAGE:
					if (entry.hasVoltage())
					{
						strings.add(Double.toString(entry.getVoltage()));
					}
					break;
				case CURRENT:
					if (entry.hasCurrent())
					{
						strings.add(Double.toString(entry.getCurrent()));
					}
					break;
				case TEMP:
					if (entry.hasTemp())
					{
						strings.add(Double.toString(entry.getTemp()));
					}
					break;
				default:
					continue;
			}
		}

		return strings;
	}

	/*
	 * Returns everything as an array of separated strings
	 * suitable for writing to file later on
	 */
	public ArrayList<String> getStringsForFile(EDataSeparator data_separator)
	{
		ArrayList<String> strings = new ArrayList<String>();

		// TODO: make this less messy
		boolean has_time = false;
		boolean has_voltage = false;
		boolean has_current = false;
		boolean has_temp = false;
		boolean has_voc = false;
		boolean has_isc = false;
		for (ModuleData entry : mModuleData)
		{
			String str = "";
			if (entry.hasTime())
			{
				str += Long.toString(entry.getTime());
				has_time = true;
			}
			str += data_separator.get();

			if (entry.hasVoltage())
			{
				str += Double.toString(entry.getVoltage());
				has_voltage = true;
			}
			str += data_separator.get();

			if (entry.hasCurrent())
			{
				str += Double.toString(entry.getCurrent());
				has_current = true;
			}
			str += data_separator.get();

			if (entry.hasTemp())
			{
				str += Double.toString(entry.getTemp());
				has_temp = true;
			}

			if (entry.hasVoc())
			{
				str += Double.toString(entry.getVoc());
				has_voc = true;
			}

			if (entry.hasIsc())
			{
				str += Double.toString(entry.getIsc());
				has_isc = true;
			}

			if (str.charAt(0) == data_separator.get().charAt(0))
			{
				continue;
			}
			else if (!str.isEmpty() &&
			         str.charAt(str.length()-1) == data_separator.get().charAt(0))
			{
				str = str.substring(0, str.length()-1);
			}

			strings.add(str);
		}

		String header = "";
		if (has_time)
		{
			header += "TIME" + data_separator.get();
		}
		if (has_voltage)
		{
			header += "VOLTAGE" + data_separator.get();
		}
		if (has_current)
		{
			header += "CURRENT" + data_separator.get();
		}
		if (has_temp)
		{
			header += "TEMPERATURE" + data_separator.get();
		}
		if (has_voc)
		{
			header += "VOC" + data_separator.get();
		}
		if (has_isc)
		{
			header += "ISC" + data_separator.get();
		}
		if (!header.isEmpty() &&
		    header.charAt(header.length()-1) == data_separator.get().charAt(0))
		{
			header = header.substring(0, header.length()-1);
		}

		if (!header.isEmpty())
		{
			strings.add(0, header);
		}

		return strings;
	}

	public boolean isEmpty()
	{
		return mModuleData.isEmpty();
	}

	public void clear()
	{
		mModuleData.clear();
	}
}

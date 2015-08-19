package qesst.asu.edu.lilac;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;


/**
 * Data set for storing module data entries
 */
public class ModuleDataSet
{
	private ArrayList<ModuleDataEntry> mModuleDataEntries = new ArrayList<ModuleDataEntry>();

	public ModuleDataSet()
	{
	}

	public ModuleDataSet(ArrayList<ModuleDataEntry> dataEntries)
	{
		add(dataEntries);
	}

	public ModuleDataSet(ModuleDataEntry moduleData)
	{
		add(moduleData);
	}

	public void add(ModuleDataEntry moduleData)
	{
		mModuleDataEntries.add(moduleData);
	}

	public void add(ArrayList<ModuleDataEntry> dataEntries)
	{
		if (dataEntries != null)
		{
			for (int i = 0; i < dataEntries.size(); ++i)
			{
				if (dataEntries.get(i) != null)
				{
					mModuleDataEntries.add(dataEntries.get(i));
				}
			}
		}
	}

	// doesn't work
	public void remove(ModuleDataEntry moduleData)
	{
		mModuleDataEntries.remove(moduleData);
	}

	public void remove(double value, EFlag type)
	{
		search: for (ModuleDataEntry entry : mModuleDataEntries)
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
		return mModuleDataEntries.size();
	}

	public ModuleDataEntry get(int index)
	{
		if (index < 0 || index >= mModuleDataEntries.size())
		{
			// TODO: throw an exception
			return null;
		}
		return mModuleDataEntries.get(index);
	}

	double getStdev(EFlag type)
	{
		// Get the mean
		double mean = getMean(type);

		// Get the variance
		int count = 0;
		double variance = 0;
		for (ModuleDataEntry entry : mModuleDataEntries)
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
		for (ModuleDataEntry entry : mModuleDataEntries)
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
		for (ModuleDataEntry entry : mModuleDataEntries)
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
			ModuleDataEntry entry = get(index);
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
		for (ModuleDataEntry entry : mModuleDataEntries)
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
		for (ModuleDataEntry entry : mModuleDataEntries)
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

	public boolean isEmpty()
	{
		return mModuleDataEntries.isEmpty();
	}
}

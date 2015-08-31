package qesst.asu.edu.lilac;

/**
 * Wrapper for each piece of data
 */
class ModuleDataEntry
{
	private double mValue;
	public ModuleDataEntry(double value)
	{
		mValue = value;
	}

	double get()
	{
		return mValue;
	}
}

/**
 * Class for storing module data
 */
public class ModuleData
{
	private ModuleDataEntry mVoltage;
	private ModuleDataEntry mCurrent;
	private ModuleDataEntry mTemp;
	private long mTime;

	public ModuleData()
	{
		mVoltage = null;
		mCurrent = null;
		mTemp = null;
		mTime = 0;
	}

	public ModuleData(EFlag type, double data)
	{
		mVoltage = null;
		mCurrent = null;
		mTemp = null;
		mTime = 0;
		switch(type)
		{
			case VOLTAGE:
				mVoltage = new ModuleDataEntry(data);
				break;
			case CURRENT:
				mVoltage = new ModuleDataEntry(data);
				break;
			case TEMP:
				mTemp = new ModuleDataEntry(data);
				break;
			default:
				break;
		}
	}

	public ModuleData(double voltage)
	{
		mVoltage = new ModuleDataEntry(voltage);
		mCurrent = null;
		mTemp = null;
		mTime = 0;
	}

	public ModuleData(double voltage, double current)
	{
		mVoltage = new ModuleDataEntry(voltage);
		mCurrent = new ModuleDataEntry(current);
		mTemp = null;
		mTime = 0;
	}

	public ModuleData(double voltage, double current, double temp)
	{
		mVoltage = new ModuleDataEntry(voltage);
		mCurrent = new ModuleDataEntry(current);
		mTemp = new ModuleDataEntry(temp);
		mTime = 0;
	}

	public ModuleData(long time)
	{
		mVoltage = null;
		mCurrent = null;
		mTemp = null;
		mTime = time;
	}

	public ModuleData(double voltage, double current, double temp, long time)
	{
		mVoltage = new ModuleDataEntry(voltage);
		mCurrent = new ModuleDataEntry(current);
		mTemp = new ModuleDataEntry(temp);
		mTime = time;
	}

	public boolean hasTime()
	{
		return mTime != 0;
	}

	public boolean hasVoltage()
	{
		return mVoltage != null;
	}

	public boolean hasCurrent()
	{
		return mCurrent != null;
	}

	public boolean hasTemp()
	{
		return mTemp != null;
	}

	public void clearVoltage()
	{
		mVoltage = null;
	}

	public void clearCurrent()
	{
		mCurrent = null;
	}

	public void clearTemp()
	{
		mTemp = null;
	}

	public double getVoltage()
	{
		if (!hasVoltage()) return 0;
		return mVoltage.get();
	}

	public void setVoltage(double voltage)
	{
		mVoltage = new ModuleDataEntry(voltage);
	}

	public double getCurrent()
	{
		if (!hasCurrent()) return 0;
		return mCurrent.get();
	}

	public void setCurrent(double current)
	{
		mCurrent = new ModuleDataEntry(current);
	}

	public double getTemp()
	{
		if (!hasTemp()) return 0;
		return mTemp.get();
	}

	public void setTemp(double temp)
	{
		mTemp = new ModuleDataEntry(temp);
	}

	public long getTime()
	{
		return mTime;
	}

	public void setTime(long mTime)
	{
		this.mTime = mTime;
	}

	@Override
	public String toString()
	{
		String str = "";
		if (hasTime())      str += "time: "    + getTime() + " ";
		if (hasVoltage())   str += "voltage: " + getVoltage() + " ";
		if (hasCurrent())   str += "current: " + getCurrent() + " ";
		if (hasTemp())      str += "temperature: " + getTemp() + " ";
		return str;
	}
}


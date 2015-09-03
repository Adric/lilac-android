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
public class ModuleData implements Cloneable, Comparable<ModuleData>
{
	private ModuleDataEntry mVoltage = null;
	private ModuleDataEntry mCurrent = null;
	private ModuleDataEntry mTemp = null;
	private ModuleDataEntry mVoc = null;
	private ModuleDataEntry mIsc = null;
	private long mTime = 0;

	public ModuleData()
	{
	}

	public ModuleData(ModuleData md)
	{
		if (md != null)
		{
			if (md.hasVoltage()) mVoltage = new ModuleDataEntry(md.getVoltage());
			if (md.hasCurrent()) mCurrent = new ModuleDataEntry(md.getCurrent());
			if (md.hasTemp()) mTemp = new ModuleDataEntry(md.getTemp());
			if (md.hasVoc()) mVoc = new ModuleDataEntry(md.getVoc());
			if (md.hasIsc()) mIsc = new ModuleDataEntry(md.getIsc());
			if (md.hasTime()) mTime = md.getTime();
		}
	}

	public ModuleData(EFlag type, double data)
	{
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
	}

	public ModuleData(double voltage, double current)
	{
		mVoltage = new ModuleDataEntry(voltage);
		mCurrent = new ModuleDataEntry(current);
	}

	public ModuleData(double voltage, double current, double temp)
	{
		mVoltage = new ModuleDataEntry(voltage);
		mCurrent = new ModuleDataEntry(current);
		mTemp = new ModuleDataEntry(temp);
	}

	public ModuleData(double voltage, double current, double temp, double voc)
	{
		mVoltage = new ModuleDataEntry(voltage);
		mCurrent = new ModuleDataEntry(current);
		mTemp = new ModuleDataEntry(temp);
		mVoc = new ModuleDataEntry(voc);
	}

	public ModuleData(double voltage, double current, double temp, double voc, double isc)
	{
		mVoltage = new ModuleDataEntry(voltage);
		mCurrent = new ModuleDataEntry(current);
		mTemp = new ModuleDataEntry(temp);
		mVoc = new ModuleDataEntry(voc);
		mIsc = new ModuleDataEntry(isc);
	}

	public ModuleData(long time)
	{
		mTime = time;
	}

	public ModuleData(long time, double voltage, double current, double temp)
	{
		mVoltage = new ModuleDataEntry(voltage);
		mCurrent = new ModuleDataEntry(current);
		mTemp = new ModuleDataEntry(temp);
		mTime = time;
	}

	public ModuleData(long time, double voltage, double current, double temp, double voc)
	{
		mVoltage = new ModuleDataEntry(voltage);
		mCurrent = new ModuleDataEntry(current);
		mTemp = new ModuleDataEntry(temp);
		mVoc = new ModuleDataEntry(voc);
		mTime = time;
	}

	public ModuleData(long time, double voltage, double current, double temp, double voc, double isc)
	{
		mVoltage = new ModuleDataEntry(voltage);
		mCurrent = new ModuleDataEntry(current);
		mTemp = new ModuleDataEntry(temp);
		mVoc = new ModuleDataEntry(voc);
		mIsc = new ModuleDataEntry(isc);
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

	public boolean hasVoc()
	{
		return mVoc != null;
	}

	public boolean hasIsc()
	{
		return mIsc != null;
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

	public void clearVoc()
	{
		mVoc = null;
	}

	public void clearIsc()
	{
		mIsc = null;
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

	public double getVoc()
	{
		return mVoc.get();
	}

	public void setVoc(double voc)
	{
		mVoc = new ModuleDataEntry(voc);
	}

	public double getIsc()
	{
		return mIsc.get();
	}

	public void setIsc(double isc)
	{
		mIsc = new ModuleDataEntry(isc);
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

	@Override
	public ModuleData clone()
	{
		try
		{
			return (ModuleData) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public int compareTo(ModuleData md)
	{
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;

		if (this.equals(md)) return EQUAL;

		// Compare times
		if (this.getTime() < md.getTime()) return BEFORE;
		if (this.getTime() > md.getTime()) return AFTER;

		// Compare voltages
		if (this.hasVoltage() && md.hasVoltage())
		{
			if (this.getVoltage() < md.getVoltage()) return BEFORE;
			if (this.getVoltage() > md.getVoltage()) return AFTER;
		}
		else if (!this.hasVoltage() && md.hasVoltage())
		{
			return BEFORE;
		}
		else if (this.hasVoltage() && !md.hasVoltage())
		{
			return AFTER;
		}

		// Compare currents
		if (this.hasCurrent() && md.hasCurrent())
		{
			if (this.getCurrent() < md.getCurrent()) return BEFORE;
			if (this.getCurrent() > md.getCurrent()) return AFTER;
		}
		else if (!this.hasCurrent() && md.hasCurrent())
		{
			return BEFORE;
		}
		else if (this.hasCurrent() && !md.hasCurrent())
		{
			return AFTER;
		}

		// Compare temperature
		if (this.hasTemp() && md.hasTemp())
		{
			if (this.getTemp() < md.getTemp()) return BEFORE;
			if (this.getTemp() > md.getTemp()) return AFTER;
		}
		else if (!this.hasTemp() && md.hasTemp())
		{
			return BEFORE;
		}
		else if (this.hasTemp() && !md.hasTemp())
		{
			return AFTER;
		}

		// Compare Voc
		if (this.hasVoc() && md.hasVoc())
		{
			if (this.getVoc() < md.getVoc()) return BEFORE;
			if (this.getVoc() > md.getVoc()) return AFTER;
		}
		else if (!this.hasVoc() && md.hasVoc())
		{
			return BEFORE;
		}
		else if (this.hasVoc() && !md.hasVoc())
		{
			return AFTER;
		}

		// Compare Isc
		if (this.hasIsc() && md.hasIsc())
		{
			if (this.getIsc() < md.getIsc()) return BEFORE;
			if (this.getIsc() > md.getIsc()) return AFTER;
		}
		else if (!this.hasIsc() && md.hasIsc())
		{
			return BEFORE;
		}
		else if (this.hasIsc() && !md.hasIsc())
		{
			return AFTER;
		}

		return EQUAL;
	}
}


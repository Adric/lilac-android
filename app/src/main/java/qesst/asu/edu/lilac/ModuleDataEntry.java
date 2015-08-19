package qesst.asu.edu.lilac;

/**
 * Created by amovi_000 on 8/11/2015.
 */
/**
 * Class for storing module data
 */
public class ModuleDataEntry
{
	private ModuleData mVoltage;
	private ModuleData mCurrent;
	private ModuleData mTemp;

	public ModuleDataEntry()
	{
		mVoltage = null;
		mCurrent = null;
		mTemp = null;
	}

	public ModuleDataEntry(EMeasurementType type, double data)
	{
		mVoltage = null;
		mCurrent = null;
		mTemp = null;
		switch(type)
		{
			case VOLTAGE:
				mVoltage = new ModuleData(data);
				break;
			case CURRENT:
				mVoltage = new ModuleData(data);
				break;
			case TEMP:
				mTemp = new ModuleData(data);
				break;
			default:
				break;
		}
	}

	public ModuleDataEntry(ModuleData voltage)
	{
		mVoltage = voltage;
		mCurrent = null;
		mTemp = null;
	}

	public ModuleDataEntry(ModuleData voltage, ModuleData current)
	{
		mVoltage = voltage;
		mCurrent = current;
		mTemp = null;
	}

	public ModuleDataEntry(ModuleData voltage, ModuleData current, ModuleData temp)
	{
		mVoltage = voltage;
		mCurrent = current;
		mTemp = temp;
	}

	public ModuleDataEntry(double voltage)
	{
		mVoltage = new ModuleData(voltage);
		mCurrent = null;
		mTemp = null;
	}

	public ModuleDataEntry(double voltage, double current)
	{
		mVoltage = new ModuleData(voltage);
		mCurrent = new ModuleData(current);
		mTemp = null;
	}

	public ModuleDataEntry(double voltage, double current, double temp)
	{
		mVoltage = new ModuleData(voltage);
		mCurrent = new ModuleData(current);
		mTemp = new ModuleData(temp);
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
		mVoltage = new ModuleData(voltage);
	}

	public double getCurrent()
	{
		if (!hasCurrent()) return 0;
		return mCurrent.get();
	}

	public void setCurrent(double current)
	{
		mCurrent = new ModuleData(current);
	}

	public double getTemp()
	{
		if (!hasTemp()) return 0;
		return mTemp.get();
	}

	public void setTemp(double temp)
	{
		mTemp = new ModuleData(temp);
	}
}


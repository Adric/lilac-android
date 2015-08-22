package qesst.asu.edu.lilac;

import android.util.JsonWriter;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.EnumSet;

/**
 * Enum for each measurement type
 */
public enum EFlag
{
	NONE(1 << 0),
	ERROR(1 << 1),
	VOLTAGE(1 << 2),
	CURRENT(1 << 3),
	TEMP(1 << 4),
	CONTINUOUS(1 << 5),
	AVERAGE(1 << 6),
	DEBUG_OUTPUT(1 << 7),
	RUN(1 << 8),
	GET_FLAGS(1 << 9),
	SET_FLAGS(1 << 10),
	SWEEP(VOLTAGE.val | CURRENT.val | CONTINUOUS.val),
	;

	private final static String TAG = "EFlag";
	private int val = 0;

	private EFlag(int val)
	{
		this.val = val;
	}

	public int getValue()
	{
		return val;
	}

	public static EnumSet<EFlag> getEFlags(int flags_as_int)
	{
		EnumSet flags = EnumSet.noneOf(EFlag.class);
		for (EFlag flag : EFlag.values())
		{
			if ((flags_as_int & flag.getValue()) == flag.getValue())
			{
				flags.add(flag);
			}
		}
		return flags;
	}

	public static String toJsonString(int flags_as_int)
	{
		return toJsonString(getEFlags(flags_as_int));
	}

	public static String toJsonString(EFlag flag)
	{
		EnumSet flags = EnumSet.of(flag);
		return toJsonString(flags);
	}

	public static String toJsonString(EnumSet<EFlag> flags)
	{
		StringWriter out = new StringWriter();
		JsonWriter writer = new JsonWriter(out);
		if (writer == null || flags == null || flags.isEmpty())
		{
			return "";
		}

		writer.setIndent("");
		try
		{
			writeEFlags(writer, flags);
		}
		catch(IOException ioe)
		{
			Log.d(TAG, "Exception when writing JSON flags: " + ioe);
		}

		String str = out.toString();
		Log.d(TAG, "Sending flags: " + str);

		return str;
	}

	private static void writeEFlag(JsonWriter writer, EFlag flag) throws IOException
	{
		//writer.beginArray();
		writer.beginObject();
		writer.name("F").value(flag.getValue());
		writer.endObject();
		//writer.endArray();
		writer.close();
	}

	private static void writeEFlags(JsonWriter writer, EnumSet<EFlag> flags) throws IOException
	{
		int flagToSend = 0x000;
		for (EFlag flag : flags)
		{
			flagToSend |= flag.getValue();
		}
		//writer.beginArray();
		writer.beginObject();
		writer.name("F").value(flagToSend);
		writer.endObject();
		//writer.endArray();
		writer.close();
	}

	public static EFlag toType(char ch)
	{
		switch (ch)
		{
			case 'V':
				return EFlag.VOLTAGE;
			case 'I':
				return EFlag.CURRENT;
			case 'T':
				return EFlag.TEMP;
			case 'C':
				return EFlag.CURRENT;
			case 'A':
				return EFlag.AVERAGE;
			case 'W':
				return EFlag.SWEEP;
			case 'D':
				return EFlag.DEBUG_OUTPUT;
			case 'R':
				return EFlag.RUN;
			case 'G':
				return EFlag.GET_FLAGS;
			case 'E':
				return EFlag.ERROR;
			case 'F':
				return EFlag.SET_FLAGS;
			default:
				return EFlag.NONE;
		}
	}

	/**
	 * Returns the first flag in a string. If no flag is found, returns NONE
	 */
	/*
	public static EFlag getFirstFlag(String str)
	{
		if (str != null && !str.isEmpty())
		{
			for (int i = 0; i < str.length(); ++i)
			{
				EFlag flag = toType(str.charAt(i));
				if (flag != EFlag.NONE)
				{
					return flag;
				}
			}
		}
		return EFlag.NONE;
	}*/

	public char toChar()
	{
		switch (val)
		{
			case 1:
				return 'V';
			case 2:
				return 'I';
			case 3:
				return 'T';
			case 4:
				return 'C';
			case 5:
				return 'A';
			case 6:
				return 'W';
			case 7:
				return 'D';
			case 8:
				return 'B';
			case 9:
				return 'E';
			case 10:
				return '!';
			case 11:
				return 'F';
			case 0:
			default:
				return '\0';
		}
	}

	public String toString()
	{
		switch (this)
		{
			case NONE:
				return "None";
			case VOLTAGE:
				return "Voltage";
			case CURRENT:
				return "Current";
			case TEMP:
				return "Temperature";
			case CONTINUOUS:
				return "Continuous";
			case AVERAGE:
				return "Average";
			case SWEEP:
				return "Sweep";
			case DEBUG_OUTPUT:
				return "Debug output";
			case RUN:
				return "Run";
			case ERROR:
				return "Error";
			case GET_FLAGS:
				return "Get flags";
			case SET_FLAGS:
				return "Set flags";
			default:
				return "";
		}
	}
}

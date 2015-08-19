package qesst.asu.edu.lilac;

/**
 * Enum for each measurement type
 */
public enum EFlag
{
	NONE(0),
	VOLTAGE(1),
	CURRENT(2),
	TEMP(3),
	CONTINUOUS(4),
	AVERAGE(5),
	SWEEP(6),
	DEBUG_OUTPUT(7),
	BEGIN(8),
	END(9),
	ERROR(10),
	FLAGS(11)
	;

	private int val = 0;

	private EFlag(int val)
	{
		this.val = val;
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
			case 'B':
				return EFlag.BEGIN;
			case 'E':
				return EFlag.END;
			case '!':
				return EFlag.ERROR;
			case 'F':
				return EFlag.FLAGS;
			default:
				return EFlag.NONE;
		}
	}

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
		switch (val)
		{
			case 0:
				return "None";
			case 1:
				return "Voltage";
			case 2:
				return "Current";
			case 3:
				return "Temperature";
			case 4:
				return "Continuous";
			case 5:
				return "Average";
			case 6:
				return "Sweep";
			case 7:
				return "Debug output";
			case 8:
				return "Begin";
			case 9:
				return "End";
			case 10:
				return "Error";
			case 11:
				return "Flag list";
			default:
				return "";
		}
	}
}

package qesst.asu.edu.lilac;

/**
 * Enum for different data separators
 */
public enum EDataSeparator
{
	COMMA(","),
	TAB("\t")
	;

	private String val;
	EDataSeparator(String val)
	{
		this.val = val;
	}

	public String get()
	{
		return val;
	}
}
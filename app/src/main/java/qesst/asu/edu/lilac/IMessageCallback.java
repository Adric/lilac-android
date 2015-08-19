package qesst.asu.edu.lilac;

import java.util.ArrayList;

/**
 * Callback for sending messages back to the main activity
 */
public interface IMessageCallback
{
	public void call(ArrayList<String> arr);
}

package qesst.asu.edu.lilac;

import java.util.ArrayList;

/**
 * Callback for sending messages back to an activity
 */
public interface IMessageCallback
{
	public void call(ArrayList<String> arr);
}

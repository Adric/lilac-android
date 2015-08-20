package qesst.asu.edu.lilac;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MessageSystem2 extends Activity 
{
	private static final int MSG_STOP_THREAD = 0x0001;
	private static final int MSG_STRING_OPERATION = 0x0002;
	private static final int MSG_LONG_OP_START = 0x0003;

	private static final int RESP_THREAD_READY = 0x0100;
	private static final int RESP_THREAD_FINISH = 0x0101;
	private static final int RESP_STRING_OPERATION = 0x0102;
	private static final int RESP_LONG_OP_STARTED = 0x0103;
	private static final int RESP_LONG_OP_FINISHED = 0x0104;

	private static final String RESP_KEY_1 = "RESP_KEY_1";

	private Handler uiThreadHandler;
	private Handler childThreadHandler;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		uiThreadHandler = new UIThreadHandler();
		new SampleThread().start();
		OnClickListener listener = new OnClickListener() {

			public void onClick(View v) {
				if (null != childThreadHandler) {
					switch (v.getId()) {
						case R.id.btn_begin:
							childThreadHandler
									.sendEmptyMessage(MSG_STRING_OPERATION);
							break;
						case R.id.btn_end:

							childThreadHandler
									.sendEmptyMessage(MSG_LONG_OP_START);

							break;
					}

				}
			}
		};
		findViewById(R.id.btn_begin).setOnClickListener(listener);
		findViewById(R.id.btn_end).setOnClickListener(listener);
	}

	private class SampleThread extends Thread {

		@Override
		public void run() {

			// Pause this thread so that UI thread get some time to breathe.
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// Start the looper
			Looper.prepare();
			// create instance of child thread handler
			childThreadHandler = new ChildThreadHandler();
			// intimate ui thread that child thread is ready
			uiThreadHandler.sendEmptyMessage(RESP_THREAD_READY);
			Looper.loop();
		}

	}

	// extending handler class to handle messages sent to UI thread
	private class UIThreadHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case RESP_THREAD_READY:
					Toast.makeText(MessageSystem2.this, "Child thread ready",
					               Toast.LENGTH_SHORT).show();
					break;
				case RESP_THREAD_FINISH:
					Toast.makeText(MessageSystem2.this, "Child thread finished",
					               Toast.LENGTH_SHORT).show();
					break;
				case RESP_STRING_OPERATION:
					Toast.makeText(MessageSystem2.this, msg.obj.toString(),
					               Toast.LENGTH_SHORT).show();
					break;
				case RESP_LONG_OP_STARTED:
					((Button) findViewById(R.id.btn_begin)).setVisibility(View.GONE);
					Toast.makeText(MessageSystem2.this, "Long operation started",
					               Toast.LENGTH_SHORT).show();
					break;
				case RESP_LONG_OP_FINISHED:
					((Button) findViewById(R.id.btn_end))
							.setVisibility(View.VISIBLE);
					Toast.makeText(
							MessageSystem2.this,
							"After long consideration, thread thinks that you are "
							+ msg.getData().getString(RESP_KEY_1),
							Toast.LENGTH_SHORT).show();
					break;
			}
			super.handleMessage(msg);
		}
	}

	// extending handler class to handle messages sent to child thread
	private class ChildThreadHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_STOP_THREAD:
					// end looper
					Looper.myLooper().quit();
					// send message to ui thread
					uiThreadHandler.sendEmptyMessage(RESP_THREAD_FINISH);
					break;
				case MSG_STRING_OPERATION:
					Message message = obtainMessage(RESP_STRING_OPERATION);
					message.obj = "Hello from child thread.";
					uiThreadHandler.sendMessage(message);
					break;
				case MSG_LONG_OP_START:
					uiThreadHandler.sendEmptyMessage(RESP_LONG_OP_STARTED);
					// perform long operation
					try {
						Thread.sleep(2000);
						// if operation done send back success
						// response
						Message successMessage = obtainMessage(RESP_LONG_OP_FINISHED);
						Bundle bundle = new Bundle();
						bundle.putString(RESP_KEY_1,
						                 System.currentTimeMillis() % 2 == 1 ? "smart"
						                                                     : "idiot");
						successMessage.setData(bundle);
						uiThreadHandler.sendMessage(successMessage);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					break;
			}
			super.handleMessage(msg);
		}
	}

	@Override
	protected void onStop() {
		childThreadHandler.sendEmptyMessage(MSG_STOP_THREAD);
		super.onStop();
	}
}
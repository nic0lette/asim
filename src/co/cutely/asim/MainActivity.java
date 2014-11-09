package co.cutely.asim;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import co.cutely.asim.service.XmppService;

public class MainActivity extends Activity {
	private XmppService xmppService;
	private static final String TAG = MainActivity.class.getSimpleName();

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	protected void onStart() {
		super.onStart();

		Log.i(TAG, "Binding xmppService");
		Intent intent = new Intent(this, XmppService.class);
		bindService(intent, xmppServiceConncetion, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(xmppServiceConncetion);
	}

	private ServiceConnection xmppServiceConncetion = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			XmppService.XmppBinder b = (XmppService.XmppBinder) service;
			xmppService = b.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			xmppService = null;
		}
	};
}

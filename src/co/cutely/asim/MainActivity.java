package co.cutely.asim;

import android.animation.*;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import co.cutely.asim.registration.RegistrationActivity;
import co.cutely.asim.service.XmppService;

import java.util.List;

public class MainActivity extends Activity {
	private XmppService xmppService;
	private static final String TAG = MainActivity.class.getSimpleName();

	private List<XmppAccount> accounts;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Get the list of accounts that could be active
		final AsimApplication application = (AsimApplication) getApplication();
		accounts = application.getAccountManager().getAccounts();

		// Are there any accounts?
		if (accounts.isEmpty()) {
			// Nope! Better add one
			startActivity(new Intent(this, RegistrationActivity.class));

			// Nothing to do until there are accounts
			Log.d("nicole", "No accounts - starting registration");
			finish();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		Log.i(TAG, "Binding xmppService");
		Intent intent = new Intent(this, XmppService.class);
		bindService(intent, xmppServiceConncetion, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();
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

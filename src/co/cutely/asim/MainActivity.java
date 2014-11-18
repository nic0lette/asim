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
import co.cutely.asim.service.XmppService;

public class MainActivity extends Activity {
	private XmppService xmppService;
	private static final String TAG = MainActivity.class.getSimpleName();

	private RelativeLayout root;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		root = (RelativeLayout) findViewById(R.id.root);
	}

	@Override
	protected void onStart() {
		super.onStart();

		Log.i(TAG, "Binding xmppService");
		Intent intent = new Intent(this, XmppService.class);
		bindService(intent, xmppServiceConncetion, Context.BIND_AUTO_CREATE);

		root.removeAllViews();

		final AddAccountStart start = new AddAccountStart();
		final FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.add(R.id.root, start);
		transaction.commit();
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

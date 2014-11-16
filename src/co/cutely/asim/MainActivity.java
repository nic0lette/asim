package co.cutely.asim;

import android.animation.*;
import android.app.Activity;
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
import co.cutely.asim.service.XmppService;

public class MainActivity extends Activity {
	private XmppService xmppService;
	private static final String TAG = MainActivity.class.getSimpleName();

	private ImageView addButton;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		addButton = (ImageView) findViewById(R.id.add_button);

		ViewGroup root = (ViewGroup) findViewById(R.id.root);
		LayoutTransition lt = root.getLayoutTransition();

		// Setup the cool animation
		Animator slideIn = AnimatorInflater.loadAnimator(this, R.animator.right_slide_in);
		lt.setAnimator(LayoutTransition.APPEARING, slideIn);
		lt.setDuration(LayoutTransition.APPEARING, 200);
		lt.setStartDelay(LayoutTransition.APPEARING, 0);

		Animator slideOut = AnimatorInflater.loadAnimator(this, R.animator.right_slide_out);
		lt.setAnimator(LayoutTransition.DISAPPEARING, slideOut);
		lt.setDuration(LayoutTransition.DISAPPEARING, 200);
		lt.setStartDelay(LayoutTransition.DISAPPEARING, 0);
		root.setLayoutTransition(lt);
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

		final Handler handler = new Handler(getMainLooper());
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (addButton.getVisibility() == View.VISIBLE) {
					addButton.setVisibility(View.GONE);
				} else {
					addButton.setVisibility(View.VISIBLE);
				}
				handler.postDelayed(this, 1000);
			}
		}, 1000);
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

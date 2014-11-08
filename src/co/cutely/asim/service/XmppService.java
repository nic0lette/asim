package co.cutely.asim.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.nick.packet.Nick;

/**
 * Created by nicole on 11/4/14.
 */
public class XmppService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		Log.i("XmppService", "Started with start id " + startId + ": " + intent);


		// continue running until stopped
		return START_STICKY;

		//return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}

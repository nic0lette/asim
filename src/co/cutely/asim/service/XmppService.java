package co.cutely.asim.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.nick.packet.Nick;

import java.io.IOException;

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
		connect();

		// continue running until stopped
		return START_STICKY;

		//return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void connect() {
		new AsyncTask<Void, Void, AbstractXMPPConnection>() {
			@Override
			protected AbstractXMPPConnection doInBackground(final Void... params) {
				AbstractXMPPConnection conn = new XMPPTCPConnection("0xxon.net");

				Log.i("XmppService", "trying to connect...");
				try {
					conn.connect();
					Log.i("XmppService", "trying to login...");
					conn.login("smacktest", "thisIsThePasswordForSmacktest");
					return conn;
				} catch (SmackException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (XMPPException e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(final AbstractXMPPConnection connection) {
				onConnected(connection);
			}
		}.execute((Void) null);

	}

	private void onConnected(AbstractXMPPConnection connection) {

	}
}

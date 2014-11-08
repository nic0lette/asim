package co.cutely.asim.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.nick.packet.Nick;

import java.io.IOException;

/**
 * Service handling all XMPP connection logic
 */
public class XmppService extends Service {
	private static final String TAG = XmppService.class.getSimpleName();

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
		Log.i(TAG, "Started with start id " + startId + ": " + intent);
		connect();

		// continue running until stopped
		return START_STICKY;
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

				Log.i(TAG, "trying to connect...");
				try {
					conn.connect();
					Log.i(TAG, "trying to login...");
					conn.login("smacktest", "thisIsThePasswordForSmacktest");
					return conn;
				} catch (SmackException e) {
					Log.e(TAG, "Error connecting", e);
				} catch (IOException e) {
					Log.e(TAG, "Error connecting", e);
				} catch (XMPPException e) {
					Log.e(TAG, "Error connecting", e);
				}

				return null;
			}

			@Override
			protected void onPostExecute(final AbstractXMPPConnection connection) {
				onConnected(connection);
			}
		}.execute((Void) null);

	}

	private void onConnected(AbstractXMPPConnection conn) {
		if (conn == null) {
			Log.i(TAG, "Didn't connect =(");
			return;
		}

		Log.i(TAG, "Connected to " + conn.getServiceName() + " with " + conn.getUser() );

		Roster roster = conn.getRoster();

		Log.i(TAG, "Duming roster");
		for ( RosterEntry e : roster.getEntries() ) {
			Log.i(TAG, "User " + e.getName() + "(" + e.getUser() + ")" + e.getGroups());
		}
	}
}

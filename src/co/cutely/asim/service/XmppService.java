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
		connect(new XmppServiceConnectionConfiguration("0xxon.net", "smacktest", "thisIsThePasswordForSmacktest"));

		// continue running until stopped
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void connect(final XmppServiceConnectionConfiguration conf) {
		new AsyncTask<XmppServiceConnectionConfiguration, Void, AbstractXMPPConnection>() {
			@Override
			protected AbstractXMPPConnection doInBackground(final XmppServiceConnectionConfiguration... configs) {
				Log.i(TAG, "In doInBackground");

				if ( configs.length != 1 ) {
					Log.e(TAG, "Wrong number of connection configurations in connect");
					assert(false);
				}

				XmppServiceConnectionConfiguration config = configs[0];

				Log.i(TAG, "trying to connect to " + config.service);
                AbstractXMPPConnection conn = new XMPPTCPConnection(config.service);

                try {
                    conn.connect();
                    Log.i(TAG, "trying to login...");
                    conn.login(config.user, config.password);
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
		}.execute(conf);

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

	public static class XmppServiceConnectionConfiguration {
		public final String service;
		public final String user;
		public final String password;

		public XmppServiceConnectionConfiguration(String service, String user, String password) {
			this.service = service;
			this.user = user;
			this.password = password;
		}
	}
}

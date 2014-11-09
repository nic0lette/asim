package co.cutely.asim.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import co.cutely.asim.XmppAccount;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service handling all XMPP connection logic
 */
public class XmppService extends Service {
	private static final String TAG = XmppService.class.getSimpleName();
	private final IBinder xmppBinder = new XmppBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return xmppBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		Log.i(TAG, "Started with start id " + startId + ": " + intent);
		connect(new XmppAccount("smacktest@0xxon.net", "thisIsThePasswordForSmacktest"));

		// continue running until stopped
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Connects a single account
	 *
	 * @param conf The account to connect
	 */
	private void connect(final XmppAccount conf) {
		final List<XmppAccount> confs = new ArrayList<XmppAccount>(1);
		confs.add(conf);
		connect(confs);
	}

	/**
	 * Connects a list of accounts
	 *
	 * @param confs List of accounts to connect
	 */
	private void connect(final List<XmppAccount> confs) {
		final XmppAccount[] confsArray = new XmppAccount[confs != null ? confs.size() : 0];
		if (confs != null && !confs.isEmpty()) {
			confs.toArray(confsArray);
		}

		new XmppConnectTask().execute(confsArray);
	}

	private void onConnected(XmppAccount account, AbstractXMPPConnection conn) {
		Log.i(TAG, "Connected to " + conn.getServiceName() + " with " + conn.getUser());

		Roster roster = conn.getRoster();

		Log.i(TAG, "Roster for " + account.xmppId + ":");
		for (RosterEntry e : roster.getEntries()) {
			Log.i(TAG, "User " + e.getName() + "(" + e.getUser() + ")" + e.getGroups());
		}
	}

	private void onConnectFail(XmppAccount account) {
		Log.w(TAG, "Failed to connect as " + account.xmppId);
	}

	public class XmppBinder extends Binder {
		public XmppService getService() {
			return XmppService.this;
		}
	}

	private class XmppConnectTask extends AsyncTask<XmppAccount, Object, Void> {
		@Override
		protected Void doInBackground(final XmppAccount... configs) {
			Log.i(TAG, "In doInBackground");

			ConnectionTuple[] connection = new ConnectionTuple[1];

			for (final XmppAccount config : configs) {
				Log.i(TAG, "trying to connect to " + config.host);

				// To easily check for failures
				connection[0] = new ConnectionTuple(config, null);

				AbstractXMPPConnection conn = new XMPPTCPConnection(
						new ConnectionConfiguration(config.host, config.port));

				try {
					conn.connect();
					Log.i(TAG, "trying to login for " + config.user);
					conn.login(config.user, config.password, config.resource);
					connection[0] = new ConnectionTuple(config, conn);
				} catch (SmackException e) {
					Log.e(TAG, "Error connecting", e);
				} catch (IOException e) {
					Log.e(TAG, "Error connecting", e);
				} catch (XMPPException e) {
					Log.e(TAG, "Error connecting", e);
				}

				// Since there may be multiple accounts, report each connect attempt  as it finishes
				publishProgress(connection);
			}

			// Return the connections (successful or otherwise)
			return null;
		}

		@Override
		protected void onProgressUpdate(final Object... values) {
			super.onProgressUpdate(values);

			if (values == null || values.length != 1) {
				throw new IllegalStateException("Somehow progress was published without a connection tuple");
			}
			if (values[0] instanceof ConnectionTuple) {
				ConnectionTuple tuple = (ConnectionTuple) values[0];
				if (tuple.connection != null) {
					onConnected(tuple.account, tuple.connection);
				} else {
					onConnectFail(tuple.account);
				}
			} else {
				throw new IllegalStateException("Somehow progress was published but was not a connection tuple");
			}
		}

		/**
		 * Internal helper class
		 */
		private class ConnectionTuple {
			public final XmppAccount account;
			public final AbstractXMPPConnection connection;

			public ConnectionTuple(XmppAccount account, AbstractXMPPConnection connection) {
				this.account = account;
				this.connection = connection;
			}
		}
	}
}

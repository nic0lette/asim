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
import java.util.*;

/**
 * Service handling all XMPP connection logic
 */
public class XmppService extends Service {
	private static final String TAG = XmppService.class.getSimpleName();
	private final IBinder xmppBinder = new XmppBinder();

	/**
	 * The Xmpp service needs to be aware of the currently active connections
	 * and all currently active users. All this is tracked through the connection
	 * map, which is indexed by the account name used for the connection.
	 */
	private Map<String, XmppConnection> connectionMap = new HashMap<String, XmppConnection>();

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
		if ( connectionMap.containsKey(conf.xmppId) ) {
			Log.w(TAG, "There already is a connection for xmpp ID " + conf.xmppId + "; ignoring");
			return;
		}

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

	private void onConnected(XmppConnection connection) {
		Log.i(TAG, "Connected to " + connection.conn.getServiceName() + " with " + connection.account.xmppId);

		if ( connectionMap.containsKey(connection.account.xmppId) ) {
			Log.e(TAG, "Duplicate connection was established for " + connection.account.xmppId + "; aborting new connection");
			try {
				connection.conn.disconnect();
			} catch (SmackException.NotConnectedException e) {
				Log.e(TAG, "second connection was not established after all?", e);
			}
			return;
		}

		final AbstractXMPPConnection conn = connection.conn;
		Roster roster = conn.getRoster();
		for (RosterEntry e : roster.getEntries()) {
			Set<String> groups = new HashSet<String>();

			for ( RosterGroup groupEntry : e.getGroups() )
				groups.add(groupEntry.getName());

			XmppUser u = new XmppUser(e.getUser(), e.getName(), groups);
			connection.userMap.put(e.getUser(), u);
		}

		connectionMap.put(connection.account.xmppId, connection);
	}

	private void onConnectFail(XmppAccount account) {
		Log.w(TAG, "Failed to connect as " + account.xmppId);
	}

	public class XmppBinder extends Binder {
		public XmppService getService() {
			return XmppService.this;
		}
	}

	public class XmppUser {
		public final String id;
		public String name;
		// the groups the user is a member of
		public Set<String> groups;

		public XmppUser(String id, String name, Set<String> groups) {
			this.id = id;

			if ( name == null )
				this.name = id;
			else
				this.name = name;

			this.groups = groups;
		}

		// TODO: we are missing a few important parts here. E.g., at the  moment we have
		// no way to determine if a user is busy, etc. But - that will hopefully come :)
	}

	private class XmppConnection {
		public final XmppAccount account;
		public final AbstractXMPPConnection conn;
		// the Roster information for the current account
		public Map<String, XmppUser> userMap = new HashMap<String, XmppUser>();

		private XmppConnection(XmppAccount account, AbstractXMPPConnection conn) {
			this.account = account;
			this.conn = conn;
		}
	}

	private class XmppConnectTask extends AsyncTask<XmppAccount, XmppConnection, Void> {
		@Override
		protected Void doInBackground(final XmppAccount... configs) {

			for (final XmppAccount config : configs) {
				Log.i(TAG, "Trying to connect to " + config.host);

				if ( connectionMap.containsKey(config.xmppId) ) {
					Log.w(TAG, "Connection to " + config.xmppId + " was already established in the past, ignoring");
					continue;
				}

				// To easily check for failures
				XmppConnection connection = new XmppConnection(config, null);

				AbstractXMPPConnection conn = new XMPPTCPConnection(
						new ConnectionConfiguration(config.host, config.port));

				try {
					conn.connect();
					Log.i(TAG, "Trying to login for " + config.user + " at " + config.host + " with " + config.resource);
					conn.login(config.user, config.password, config.resource);
					connection = new XmppConnection(config, conn);
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
		protected void onProgressUpdate(final XmppConnection... values) {
			super.onProgressUpdate(values);

			if (values == null || values.length != 1) {
				throw new IllegalStateException("Somehow progress was published without a connection tuple");
			}
			XmppConnection connection = (XmppConnection) values[0];
			if (connection.conn != null) {
				onConnected(connection);
			} else {
				onConnectFail(connection.account);
			}
		}

	}
}

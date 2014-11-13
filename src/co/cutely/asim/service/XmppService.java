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
	 * The Xmpp service needs to be aware of the currently active users because the rest
	 * of the application might be suspended or whatever. To be able to update the list (e.g.
	 * status, groups) due to incoming messages, we keep it indexed by account ID.
	 */
	private Map<String, XmppUser> userList = new HashMap<String, XmppUser>();

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
		for (RosterEntry e : roster.getEntries()) {
			// first - check if the user is already online on another account. If yes, just
			// add the current account to it and merge the list of groups. Otherwise - add user
			// to list.

			XmppUser u = userList.get(e.getUser());
			if ( u == null ) {
				Set<String> accounts = new HashSet<String>();
				accounts.add(conn.getUser());
				Set<String> groups = new HashSet<String>();
				for ( RosterGroup groupEntry : e.getGroups() )
					groups.add(groupEntry.getName());
				u = new XmppUser(e.getUser(), e.getName(), groups, accounts);
			} else {
				u.accounts.add(conn.getUser());
				for ( RosterGroup groupEntry : e.getGroups() )
					u.groups.add(groupEntry.getName());
			}
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

	public class XmppUser {
		public final String id;
		public String name;
		// the groups the user is a member of
		public Set<String> groups;
		// the accounts a user is available on. This might be several, if the same user is
		// added to several xmpp IDs that we are online with.
		public Set<String> accounts;

		public XmppUser(String id, String name, Set<String> groups, Set<String> accounts) {
			this.id = id;

			if ( name == null )
				this.name = id;
			else
				this.name = name;

			this.groups = groups;
			this.accounts = accounts;
		}

		// TODO: we are missing a few important parts here. E.g., at the  moment we have
		// no way to determine if a user is busy, etc. But - that will hopefully come :)
	}

	private class XmppConnectTask extends AsyncTask<XmppAccount, Object, Void> {
		@Override
		protected Void doInBackground(final XmppAccount... configs) {

			for (final XmppAccount config : configs) {
				Log.i(TAG, "Trying to connect to " + config.host);

				// To easily check for failures
				ConnectionTuple connection = new ConnectionTuple(config, null);

				AbstractXMPPConnection conn = new XMPPTCPConnection(
						new ConnectionConfiguration(config.host, config.port));

				try {
					conn.connect();
					Log.i(TAG, "Trying to login for " + config.user + " at " + config.host + " with " + config.resource);
					conn.login(config.user, config.password, config.resource);
					connection = new ConnectionTuple(config, conn);
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

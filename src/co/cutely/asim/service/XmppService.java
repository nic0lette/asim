package co.cutely.asim.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import co.cutely.asim.Database;
import co.cutely.asim.XmppAccount;
import co.cutely.asim.messages.ChatMessage;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.xhtmlim.XHTMLManager;

import java.io.IOException;
import java.util.*;

/**
 * Service handling all XMPP connection logic
 */
public class XmppService extends Service {
	private static final String TAG = XmppService.class.getSimpleName();
	private final IBinder xmppBinder = new XmppBinder();

	private Database db;

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

		db = new Database(this);
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
	public void connect(final XmppAccount conf) {
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

	/**
	 * Disconnects a single, currently connected account
	 *
	 * @param account the account to disconnect
	 */
	public void disconnect(final String account) {
		XmppConnection conn = connectionMap.get(account);

		if ( conn == null ) {
			Log.w(TAG, "Trying to disconnect nonexistant connection: "+account);
			return;
		}

		try {
			conn.conn.disconnect();
		} catch (SmackException.NotConnectedException e) {
			Log.e(TAG, "Disconnected already disconnected connection for "+account, e);
		}
		connectionMap.remove(account);
	}

	/**
	 * Returns the list of currently connected accounts
	 *
	 * @return list of currently connected accounts
	 */
	public XmppAccount[] getConnectedAccounts() {
		final XmppAccount[] accounts = new XmppAccount[connectionMap.size()];
		int i = 0;
		for ( Map.Entry<String, XmppConnection> e : connectionMap.entrySet() ) {
			accounts[i++] = e.getValue().account;
		}

		return accounts;
	}

	/**
	 * Returns the xmpp IDs of the currently connected accounts
	 *
	 * @return xmpp IDs of currently connected accounts
	 */
	public Set<String> getConnectedAccountNames() {
		return connectionMap.keySet();
	}

	/**
	 * Get the account roster for a specified xmpp ID.
	 *
	 * @param xmppId the xmpp account to get the roster for. Has to be connected.
	 * @return list of users currently on the account roster.
	 * @throws AccountNotConnectedException
	 */
	public Set<XmppUser> getAccountRoster(final String xmppId) throws AccountNotConnectedException  {
		final XmppConnection conn = getConnection(xmppId);

		final Set<XmppUser> userList = new HashSet<XmppUser>();
		userList.addAll(conn.userMap.values());
		return userList;
	}

	/**
	 * Sends an xmpp message from a specific account to a specific user
	 *
	 * @param xmppId the account to send the message from. Has to be connected at the Moment.
	 * @param target the account to send the message to.
	 * @param message the message to send.
	 * @throws AccountNotConnectedException
	 */
	public void sendMessage(final String xmppId, String target, final String message) throws AccountNotConnectedException {
		final XmppConnection conn = getConnection(xmppId);

		target = stripResource(target);
		Chat c = conn.chatMap.get(target);

		if (c == null)
			c = ChatManager.getInstanceFor(conn.conn).createChat(target, new XmppChatMessageListener(conn));

		final ChatMessage cm = db.createChatMessage(conn.account.xmppId, target, true, false, message, false, false);

		try {
			c.sendMessage(message);
		} catch (XMPPException e) {
			// FIXME: this will be another exception
			Log.e(TAG, "Sending message failed", e);
		} catch (SmackException.NotConnectedException e) {
			throw new AccountNotConnectedException("Account "+xmppId+" in connection Map, but not currently connected", e);
		}

		db.setProcessed(cm.id);
	}

	private XmppConnection getConnection(final String xmppId) throws AccountNotConnectedException {
		final XmppConnection conn = connectionMap.get(xmppId);
		if (conn == null)
			throw new AccountNotConnectedException("Account "+xmppId+" is not currently connected");

		return conn;
	}

	/**
	 * Remove the resource information from an account, if present.
	 *
	 * e.g. testuser@testdomain.org/asmin -> testuser@testdomain.org
	 *
	 * This might belong into a different class
	 *
	 * @param account the account name to strip the roster from
	 * @return account name without roster
	 */
	private static String stripResource(final String account) {
		final int separator = account.indexOf('/');

		return account.substring(0, separator);
	}

	private void onConnected(final XmppConnection connection) {
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
		final Roster roster = conn.getRoster();
		for (RosterEntry e : roster.getEntries())
			connection.userMap.put(e.getUser(), rosterEntryToUser(e));

		roster.addRosterListener(new XmppRosterListener(connection, roster));

		ChatManager.getInstanceFor(conn).addChatListener(new XmppChatManagerListener(connection));

		connectionMap.put(connection.account.xmppId, connection);
	}

	private XmppUser rosterEntryToUser(final RosterEntry entry) {
		final Set<String> groups = new HashSet<String>();

		for ( RosterGroup groupEntry : entry.getGroups() )
			groups.add(groupEntry.getName());

		final XmppUser u = new XmppUser(entry.getUser(), entry.getName(), groups);
		return u;
	}

	private void onConnectFail(XmppAccount account) {
		Log.w(TAG, "Failed to connect as " + account.xmppId);
	}

	public class XmppBinder extends Binder {
		public XmppService getService() {
			return XmppService.this;
		}

		// FIXME: not quite sure if we want to expose the DB directly or have functions
		// to do whatever the front end wants directly.
		// I am tending towards exposing it though...
		/* public Database getDb() {
			return db;
		} */
	}

	/**
	 * This class should potentially go into global scope instead of being
	 * a subclass here.
	 */
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

	private static class XmppConnection {
		public final XmppAccount account;
		public final AbstractXMPPConnection conn;
		// the Roster information for the current account
		public final Map<String, XmppUser> userMap = new HashMap<String, XmppUser>();

		// the currently active Chats for the account
		// maps xmpp ID of the chat partner to Chat instances
		public final Map<String, Chat> chatMap = new HashMap<String, Chat>();

		private XmppConnection(XmppAccount account, AbstractXMPPConnection conn) {
			this.account = account;
			this.conn = conn;
		}
	}

	private final class XmppConnectTask extends AsyncTask<XmppAccount, XmppConnection, Void> {
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
			final XmppConnection connection = (XmppConnection) values[0];
			if (connection.conn != null) {
				onConnected(connection);
			} else {
				onConnectFail(connection.account);
			}
		}

	}

	private final class XmppRosterListener implements RosterListener {
		private final XmppConnection connection;
		private final Roster roster;

		private XmppRosterListener(XmppConnection connection, Roster roster) {
			this.connection = connection;
			this.roster = roster;
		}

		@Override
		public void entriesAdded(Collection<String> addresses) {
			for ( String address : addresses )
				connection.userMap.put(address, rosterEntryToUser(roster.getEntry(address)));
		}

		@Override
		public void entriesUpdated(Collection<String> addresses) {
			for ( String address : addresses )
				connection.userMap.put(address, rosterEntryToUser(roster.getEntry(address)));
		}

		@Override
		public void entriesDeleted(Collection<String> addresses) {
			for ( String address : addresses )
				connection.userMap.remove(address);
		}

		@Override
		public void presenceChanged(Presence presence) {
			// TODO: we do not store presence information yet
		}
	}

	private final class XmppChatManagerListener implements ChatManagerListener {
		private final XmppConnection connection;

		private XmppChatManagerListener(XmppConnection connection) {
			this.connection = connection;
		}

		@Override
		public void chatCreated(Chat chat, boolean createdLocally) {

			connection.chatMap.put(stripResource(chat.getParticipant()), chat);

			if ( !createdLocally )
				chat.addMessageListener(new XmppChatMessageListener(connection));
		}
	}

	private final class XmppChatMessageListener implements ChatMessageListener {
		private final XmppConnection connection;

		private XmppChatMessageListener(XmppConnection connection) {
			this.connection = connection;
		}

		@Override
		public void processMessage(Chat chat, Message message) {
			Log.i(TAG, "Incoming raw message: "+message);
			Log.i(TAG, "Incoming " + message.getType().name() + " message from "+chat.getParticipant()+": "+message.getBody());
			if ( message.getBody() == null )
				// we simply ignore null messages (e.g. thread establishment) for the moment
				return;

			if (XHTMLManager.isXHTMLMessage(message)) {
				List<CharSequence> bodies = XHTMLManager.getBodies(message);
				for (CharSequence body : bodies) {
					db.createChatMessage(connection.account.xmppId, chat.getParticipant(), false, false, body.toString(), false, false);
				}
			} else {
				db.createChatMessage(connection.account.xmppId, chat.getParticipant(), false, false, message.getBody(), false, false);
			}
		}
	}
}

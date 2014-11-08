package co.cutely.asim;

/**
 * A user's XMPP account
 */
public class Account {
	public final String xmppId;
	public final String password;
	public final String host;
	public final int port;
	public final String resource;

	public Account(String xmppId, String password) {
		this(xmppId, password, null, DEFAULT_PORT);
	}

	public Account(String xmppId, String password, String host, int port) {
		this(xmppId, password, host, port, "");
	}

	public Account(String xmppId, String password, String host, int port, String resource) {
		if (xmppId == null || password == null) {
			throw new IllegalArgumentException("Account must include an id and password");
		}

		this.xmppId = xmppId;
		this.password = password;

		/*
		 * If the hostname is null pull it from the ID
		 */
		if (host == null || host.isEmpty()) {
			int hostSeparator = xmppId.indexOf('@');
			if (hostSeparator == -1) {
				throw new IllegalArgumentException("XMPP ID must contain an '@'");
			}
			this.host = xmppId.substring(hostSeparator + 1);
		} else {
			this.host = host;
		}

		this.port = port;
		this.resource = (resource != null && !resource.isEmpty()) ? resource : DEFAULT_RESOURCE;
	}

	private static final int DEFAULT_PORT = 5222;
	private static final String DEFAULT_RESOURCE = "asim";
}

package co.cutely.asim;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A user's XMPP account
 */
public class XmppAccount implements Parcelable {
	public final String xmppId;
	public final String user;
	public final String password;
	public final String host;
	public final int port;
	public final String resource;

	public XmppAccount(String xmppId, String password) {
		this(xmppId, password, null, DEFAULT_PORT);
	}

	public XmppAccount(String xmppId, String password, String host, int port) {
		this(xmppId, password, host, port, null);
	}

	public XmppAccount(String xmppId, String password, String host, int port, String resource) {
		if (xmppId == null || password == null) {
			throw new IllegalArgumentException("Account must include an id and password");
		}

		this.xmppId = xmppId;
		this.password = password;

		int hostSeparator = xmppId.indexOf('@');
		if (hostSeparator == -1) {
			throw new IllegalArgumentException("XMPP ID must contain an '@'");
		}

		/*
		 * If the hostname is null pull it from the ID
		 */
		if (host == null || host.isEmpty()) {
			this.host = xmppId.substring(hostSeparator + 1);
		} else {
			this.host = host;
		}

		this.user = xmppId.substring(0, hostSeparator);

		this.port = port;
		this.resource = (resource != null && !resource.isEmpty()) ? resource : DEFAULT_RESOURCE;
	}

	private static final int DEFAULT_PORT = 5222;
	private static final String DEFAULT_RESOURCE = "asim";

	/*
	 * JSON
	 */
	public XmppAccount(final String json) throws JSONException {
		final JSONObject o = new JSONObject(json);
		xmppId = o.getString("xmppId");
		user = o.getString("user");
		password = o.getString("password");
		host = o.getString("host");
		port = o.getInt("port");
		resource = o.getString("resource");
	}

	public String toJson() throws JSONException {
		final JSONObject o = new JSONObject();
		o.put("xmppId", xmppId);
		o.put("user", user);
		o.put("password", password);
		o.put("host", host);
		o.put("port", port);
		o.put("resource", resource);
		return o.toString();
	}

	/*
	 * Parcelable implementation
	 */

	public static final Parcelable.Creator<XmppAccount> CREATOR
			= new Parcelable.Creator<XmppAccount>() {
		public XmppAccount createFromParcel(Parcel in) {
			return new XmppAccount(in);
		}

		public XmppAccount[] newArray(int size) {
			return new XmppAccount[size];
		}
	};

	private XmppAccount(Parcel in) {
		xmppId = in.readString();
		user = in.readString();
		password = in.readString();
		host = in.readString();
		port = in.readInt();
		resource = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel out, final int flags) {
		out.writeString(xmppId);
		out.writeString(user);
		out.writeString(password);
		out.writeString(host);
		out.writeInt(port);
		out.writeString(resource);
	}
}

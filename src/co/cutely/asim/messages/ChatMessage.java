package co.cutely.asim.messages;

/**
 * Database layout plan (tenative):
 *
 * messages:
 * id               serial
 * timestamp        integer, default = now
 * xmpp_id          text identify the xmpp account sending the message
 * xmpp_target      the xmpp account we interact with
 * sender           bool, T if we sent the message
 * processed        bool, T if we read the message (received) or sent it successfully (sent)
 * message          text
 * xhtml            bool, T if xhtml formatted message
 * otr              bool, T is message was otr encrypted
 *
 * xmpp_ids:
 * id               integer (?)
 * email            text
 *
 */
public class ChatMessage {
	public final int id;
	public final int ts;
	public final int xmpp_id; // we probably need to do this differently later...
	public final int xmpp_target;
	public final boolean sender;
	public final boolean processed;
	public final String message;
	public final boolean xhtml;
	public final boolean otr;


	public ChatMessage(int id, int ts, int xmpp_id, int xmpp_target, boolean sender,
			boolean processed,
			String message, boolean xhtml, boolean otr) {
		this.id = id;
		this.ts = ts;
		this.xmpp_id = xmpp_id;
		this.xmpp_target = xmpp_target;
		this.sender = sender;
		this.processed = processed;
		this.message = message;
		this.xhtml = xhtml;
		this.otr = otr;
	}
}

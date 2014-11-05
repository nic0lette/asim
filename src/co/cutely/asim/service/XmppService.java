package co.cutely.asim.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.nick.packet.Nick;

/**
 * Created by nicole on 11/4/14.
 */
public class XmppService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}

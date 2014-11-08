package co.cutely.asim;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import co.cutely.asim.service.XmppService;

/**
 * Application level data and methods
 */
public class AsimApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		Log.i("Asim", "starting XMPP service...");
		Intent xmppservice = new Intent(getApplicationContext(), XmppService.class);
		getApplicationContext().startService(xmppservice);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
}
package co.cutely.asim;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import co.cutely.asim.service.AccountManager;
import co.cutely.asim.service.XmppService;

/**
 * Application level data and methods
 */
public class AsimApplication extends Application {
	private static AsimApplication sApplication;

	private AccountManager accountManager;

	@Override
	public void onCreate() {
		super.onCreate();

		sApplication = this;
		accountManager = AccountManager.create(this);

		Intent xmppservice = new Intent(getApplicationContext(), XmppService.class);
		getApplicationContext().startService(xmppservice);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	public AccountManager getAccountManager() {
		return accountManager;
	}

	public static AsimApplication get() {
		return sApplication;
	}
}

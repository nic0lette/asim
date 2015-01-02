package co.cutely.asim.registration;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import co.cutely.asim.AsimApplication;
import co.cutely.asim.MainActivity;
import co.cutely.asim.R;
import co.cutely.asim.XmppAccount;
import co.cutely.asim.service.AccountManager;

/**
 * Created by nicole on 11/22/14.
 */
public class RegistrationActivity extends Activity {
	private static final String TAG = RegistrationActivity.class.getSimpleName();

    private XmppAccount account;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration_activity);
	}

	@Override
	protected void onStart() {
		super.onStart();

		final Fragment start = new RegistrationIdFragment();
		final FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.add(R.id.root, start);
		transaction.commit();
	}

    /* package */ void next(final String login, final String password) {
        account = new XmppAccount(login, password);

        final FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.root, RegistrationDetails.create(account));
        transaction.commit();
    }

    /* package */ void complete(final String handle, final String host, final int port) {
        account = new XmppAccount(handle, account.xmppId, account.password, host, port);

        // Create the account and return to the main activity
        final AccountManager accountManager = AsimApplication.get().getAccountManager();
        accountManager.addAccount(account);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}

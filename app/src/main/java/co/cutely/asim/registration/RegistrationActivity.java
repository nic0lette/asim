package co.cutely.asim.registration;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import co.cutely.asim.R;

/**
 * Created by nicole on 11/22/14.
 */
public class RegistrationActivity extends Activity {
	private static final String TAG = RegistrationActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration_activity);
	}

	@Override
	protected void onStart() {
		super.onStart();

//		final BasicRegFragment start = new BasicRegFragment();
		final Fragment start = new RegistrationIdFragment();
		final FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.add(R.id.root, start);
		transaction.commit();
	}
}

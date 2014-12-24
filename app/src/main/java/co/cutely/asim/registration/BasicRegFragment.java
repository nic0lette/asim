package co.cutely.asim.registration;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import co.cutely.asim.AsimApplication;
import co.cutely.asim.MainActivity;
import co.cutely.asim.R;
import co.cutely.asim.XmppAccount;
import co.cutely.asim.service.AccountManager;
import co.cutely.asim.view.CheckedEditText;

import java.util.regex.Pattern;

/**
 * This fragment represents the first (of two) parts to creating
 * an account.
 *
 * This part, specifically, handles the basic case - login/password
 * where the server, port, and resource are default values.
 * i.e.: This is all the required info; the next set is optional
 */
public class BasicRegFragment extends Fragment {
	public static final String TAG = BasicRegFragment.class.getSimpleName();

	private CheckedEditText loginInput;
	private CheckedEditText passwordInput;
	private View next;

	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container,
			Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.reg_basic, container, false);

		// Find our views
		loginInput = (CheckedEditText) root.findViewById(R.id.login);
		passwordInput = (CheckedEditText) root.findViewById(R.id.password);
		next = root.findViewById(R.id.next);

		// Connect actions
		passwordInput.setOnEditorActionListener(imeNext);
		next.setOnClickListener(nextOnClick);

		// And set up the validators
		loginInput.setValidator(new CheckedEditText.Validator() {
			@Override
			public boolean isValid(final String content) {
				final String email = "\\S+@\\S+.\\S+";
				return Pattern.matches(email, content);
			}
		});
		passwordInput.setValidator(new CheckedEditText.Validator() {
			@Override
			public boolean isValid(final String content) {
				return !content.isEmpty();
			}
		});

		// Done
		return root;
	}

	private TextView.OnEditorActionListener imeNext = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				// Act as though the user pressed the button
				nextOnClick.onClick(next);
				return true;
			}
			return false;
		}
	};

	private View.OnClickListener nextOnClick = new View.OnClickListener() {
		@Override
		public void onClick(final View v) {
            if (!loginInput.isValid() || !passwordInput.isValid()) {
                // Show errors - check in reverse order
                // (ie: if login isn't valid that's where focus should go)
                if (!passwordInput.isValid()) {
                    passwordInput.requestFocus();
                }

                if (!loginInput.isValid()) {
                    loginInput.requestFocus();
                }

                // Don't continue (because of errors)
                return;
            }

			// Create a basic account
			final String user = loginInput.getValue();
			final String password = passwordInput.getValue();
			final XmppAccount account = new XmppAccount(user, password);

			// Add and done!
			final AccountManager manager = AsimApplication.get().getAccountManager();
			manager.addAccount(account);

			// Return to the main activity
			final Activity activity = getActivity();
			if (activity != null) {
				startActivity(new Intent(activity, MainActivity.class));
			}
		}
	};
}

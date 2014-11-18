package co.cutely.asim;

import android.app.Fragment;
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

import java.util.regex.Pattern;

/**
 * This fragment represents the first (of two) parts to creating
 * an account.
 *
 * This part, specifically, handles the basic case - login/password
 * where the server, port, and resource are default values.
 * i.e.: This is all the required info; the next set is optional
 */
public class AddAccountStart extends Fragment {
	public static final String TAG = AddAccountStart.class.getSimpleName();

	private EditText loginInput;
	private EditText passwordInput;
	private View next;

	private Drawable warning;
	private Drawable error;

	private int defaultHintColor;
	private int defaultColor;
	private int alertColor;

	private boolean validLogin = false;
	private boolean validPassword = false;

	// Basically, the first time the user enters potentially wrong information
	// we show a "warning" icon, but if they try to proceed it becomes an error
	// so we shouldn't then downgrade it to "warning"
	private boolean showError = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.add_account_1, container, false);

		// Find our views
		loginInput = (EditText) root.findViewById(R.id.login);
		passwordInput = (EditText) root.findViewById(R.id.password);
		next = root.findViewById(R.id.next);

		// Connect actions
		loginInput.setOnFocusChangeListener(validate);
		passwordInput.setOnFocusChangeListener(validate);
		passwordInput.setOnEditorActionListener(imeNext);
		next.setOnClickListener(nextOnClick);

		// Setup some colors...
		defaultHintColor = getResources().getColor(R.color.text_hint);
		defaultColor = getResources().getColor(R.color.text_input);
		alertColor = getResources().getColor(R.color.text_error);

		// And drawables
		warning = getResources().getDrawable(R.drawable.ic_warning_grey600_18dp);
		error = getResources().getDrawable(R.drawable.ic_error_red_18dp);

		// Done
		return root;
	}

	private View.OnFocusChangeListener validate = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(final View v, final boolean hasFocus) {
			// Sanity check
			if (!(v instanceof EditText)) {
				Log.w(TAG, "Somehow the EditText isn't? (" + v.getClass().getName() + ")");
				return;
			}
			final EditText input = (EditText) v;

			// Only check when we lose focus, but when we gain focus we can assume the user
			// wants to fix any warnings/errors, if there are any, so let's make the
			// EditText pretty again
			if (hasFocus) {
				input.setTextColor(defaultColor);
				input.setHintTextColor(defaultHintColor);
				input.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
				return;
			}

			// Did we validate whatever we're checking?
			boolean valid;

			if (v.getId() == R.id.login) {
				// We'll be very lenient about what a valid login looks like
				// Essentially it's just:
				// [At least one non-whitespace char]@[At least another].[One More]
				// So a@b.c would match...
				final String email = "\\S+@\\S+.\\S+";
				final String login = loginInput.getText().toString();

				// Check
				valid = validLogin = Pattern.matches(email, login);
			} else {
				// Password is even less strict... have one?
				valid = validPassword = passwordInput.getText().length() > 0;
			}

			// Set the color according to the result
			if (valid) {
				input.setTextColor(defaultColor);
				input.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			} else {
				input.setTextColor(alertColor);
				final Drawable alert = (showError) ? error : warning;
				input.setCompoundDrawablesWithIntrinsicBounds(null, null, alert, null);
			}
		}
	};

	private TextView.OnEditorActionListener imeNext = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				// Act as though the user pressed the button
				validate.onFocusChange(v, false);
				nextOnClick.onClick(next);
				return true;
			}
			return false;
		}
	};

	private View.OnClickListener nextOnClick = new View.OnClickListener() {
		@Override
		public void onClick(final View v) {
			// Validating, so warnings are now errors
			showError = true;

			// Show errors if they're needed
			if (!validLogin) {
				loginInput.setCompoundDrawablesWithIntrinsicBounds(null, null, error, null);
			}
			if (!validPassword) {
				passwordInput.setCompoundDrawablesWithIntrinsicBounds(null, null, error, null);
			}

			// Now, figure out where to put focus
			if (!validLogin) {
				loginInput.requestFocus();
				return;
			}

			// Since the password check is so basic, do it here
			if (!validPassword) {
				passwordInput.requestFocus();
				passwordInput.setHintTextColor(alertColor);
				return;
			}

			Log.d("nicole", "Yay!");
		}
	};
}

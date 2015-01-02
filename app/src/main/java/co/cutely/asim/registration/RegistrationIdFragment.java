package co.cutely.asim.registration;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import java.util.regex.Pattern;

import co.cutely.asim.R;
import co.cutely.asim.view.CheckedEditText;

/**
 * Created by nicole on 12/23/14.
 */
public class RegistrationIdFragment extends Fragment {
    private CheckedEditText loginInput;
    private CheckedEditText passwordInput;
    private View next;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.reg_xmpp_id, container, false);

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

            final Activity activity = getActivity();
            if (activity instanceof RegistrationActivity) {
                // Create a basic account
                final String login = loginInput.getValue();
                final String password = passwordInput.getValue();
                ((RegistrationActivity) activity).next(login, password);
            }
        }
    };
}

package co.cutely.asim.registration;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Pattern;

import co.cutely.asim.R;
import co.cutely.asim.XmppAccount;
import co.cutely.asim.view.CheckedEditText;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegistrationDetails extends Fragment {
    private CheckedEditText handleInput;
    private CheckedEditText hostInput;
    private CheckedEditText portInput;

    private View next;

    public static RegistrationDetails create(final XmppAccount account) {
        final RegistrationDetails fragment = new RegistrationDetails();
        fragment.handleInput.setText(account.handle);
        fragment.hostInput.setText(account.host);
        fragment.portInput.setText(account.port);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.reg_details, container, false);

        // Find our views
        handleInput = (CheckedEditText) root.findViewById(R.id.handle);
        hostInput = (CheckedEditText) root.findViewById(R.id.host);
        portInput = (CheckedEditText) root.findViewById(R.id.port);
        next = root.findViewById(R.id.next);

        // Connect actions
        hostInput.setOnEditorActionListener(imeNext);
        next.setOnClickListener(nextOnClick);

        // And set up the validators
        hostInput.setValidator(new CheckedEditText.Validator() {
            @Override
            public boolean isValid(final String content) {
                return Pattern.matches("S+.\\S+", content);
            }
        });

        // Done
        return root;
    }

    private TextView.OnEditorActionListener imeNext = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Act as though the handle pressed the button
                nextOnClick.onClick(next);
                return true;
            }
            return false;
        }
    };

    private View.OnClickListener nextOnClick = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (!hostInput.isValid()) {
                hostInput.requestFocus();
                return;
            }

            final Activity activity = getActivity();
            if (activity instanceof RegistrationActivity) {
                final String handle = handleInput.getValue();
                final String host = hostInput.getValue();
                final int port = Integer.getInteger(portInput.getValue());
                ((RegistrationActivity) activity).complete(handle, host, port);
            }
        }
    };
}

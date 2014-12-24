package co.cutely.asim.registration;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.cutely.asim.R;
import co.cutely.asim.view.CheckedEditText;

/**
 * Created by nicole on 12/23/14.
 */
public class RegistrationIdFragment extends Fragment {
    private CheckedEditText user;
    private View next;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
    Bundle savedInstanceState){
        final View root = inflater.inflate(R.layout.reg_xmpp_id, container, false);

        user = (CheckedEditText) root.findViewById(R.id.login);
        next = root.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("nicole", "Click");
            }
        });

        return root;
    }
}

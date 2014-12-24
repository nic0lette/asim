package co.cutely.asim.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import co.cutely.asim.XmppAccount;
import org.json.JSONException;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Simple class to save accounts
 * (Just in SharedPreferences for now)
 */
@SuppressWarnings("unused")
public final class AccountManager {
    public static final String TAG = AccountManager.class.getName();
    private static final String ACCOUNTS_KEY = "accounts";

    private static AccountManager instance;
    private final Context context;

    private Set<AccountsChangedListener> listeners = new HashSet<>();

    /**
     * Creates the account manager
     *
     * Do not instantiate this class directly.
     * @param context Context to interface with
     */
    private AccountManager(final Context context) {
        this.context = context;
    }

    public static AccountManager create(final Context context) {
        if (instance == null) {
            instance = new AccountManager(context);
        }
        return instance;
    }

    public void addListener(final AccountsChangedListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final AccountsChangedListener listener) {
        listeners.remove(listener);
    }

    public List<XmppAccount> getAccounts() {
        final List<XmppAccount> accounts = new LinkedList<>();
        final SharedPreferences preferences = context.getSharedPreferences(
                AccountManager.class.getName(), Context.MODE_PRIVATE);

        final Set<String> jsonStrings = preferences.getStringSet(ACCOUNTS_KEY, null);
        if (jsonStrings != null) {
            for (final String json : jsonStrings) {
                try {
                    accounts.add(new XmppAccount(json));
                } catch (JSONException e) {
                    Log.w(TAG, "Could not deserialize account json '" + json + "'");
                }
            }
        }

        return accounts;
    }

    public boolean addAccount(final XmppAccount account) {
        try {
            final String json = account.toJson();

            // Get the current list of accounts and add it to the set
            final SharedPreferences preferences = context.getSharedPreferences(
                    AccountManager.class.getName(), Context.MODE_PRIVATE);
            final Set<String> jsonStrings = preferences.getStringSet(ACCOUNTS_KEY, new HashSet<String>());
            jsonStrings.add(json);

            // add it and apply
            preferences.edit().putStringSet(ACCOUNTS_KEY, jsonStrings).apply();

            // Notify listeners
            for (final AccountsChangedListener listener : listeners) {
                listener.accountAdded(account);
            }

            return true;
        } catch (JSONException e) {
            Log.w(TAG, "Could not serialize account json '" + account.xmppId + "'");
            return false;
        }
    }

    /**
     * Interface for listening to account changes
     */
    public static interface AccountsChangedListener {
        /**
         * Called when a new account is added
         *
         * @param account The added account
         */
        void accountAdded(XmppAccount account);

        /**
         * Called when an account is removed
         *
         * @param account The account that was removed
         */
        void accountRemoved(XmppAccount account);
    }
}

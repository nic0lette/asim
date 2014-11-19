package co.cutely.asim.service;

import org.jivesoftware.smack.SmackException;

/**
 * An exception class which is thrown when trying to use an xmpp account that is not connected to a server
 * at the Moment.
 */
public class AccountNotConnectedException extends Exception {
    public AccountNotConnectedException(String s) {
        super(s);
    }

    public AccountNotConnectedException(String s, Exception e) {
        super(s,e);
    }
}

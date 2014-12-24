package co.cutely.asim.service;

import android.util.Log;
import net.java.otr4j.OtrEngineHost;
import net.java.otr4j.OtrException;
import net.java.otr4j.OtrPolicy;
import net.java.otr4j.crypto.OtrCryptoEngineImpl;
import net.java.otr4j.session.FragmenterInstructions;
import net.java.otr4j.session.InstanceTag;
import net.java.otr4j.session.SessionID;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
* This is the main OTR implementation where we get all the callbacks
 * from otr4j.
*/
/* package */ final class XmppOtrEngineHost implements OtrEngineHost {
    private static final String TAG = XmppOtrEngineHost.class.getSimpleName();
    private XmppService xmppService;
    private final OtrPolicy policy;

    XmppOtrEngineHost(XmppService xmppService, OtrPolicy policy) {
        this.xmppService = xmppService;
        Log.i(TAG, "OTR engine host instantiated");
        this.policy = policy;
    }

    @Override
    public void injectMessage(SessionID sessionID, String msg) throws OtrException {
        Log.w(TAG, "OTR inject message from "+ sessionID.getAccountID() + " to "+sessionID.getUserID()+": "+msg);
        try {
            xmppService.sendMessageRaw(sessionID.getAccountID(), sessionID.getUserID(), msg);
        } catch (AccountNotConnectedException e) {
            throw new OtrException(e);
        }
    }

    @Override
    public void unreadableMessageReceived(SessionID sessionID) throws OtrException {
        Log.w(TAG, "OTR received unreadable message");
    }

    @Override
    public void unencryptedMessageReceived(SessionID sessionID, String msg) throws OtrException {
        Log.i(TAG, "OTR received unencrypted message "+msg);
    }

    @Override
    public void showError(SessionID sessionID, String error) throws OtrException {
        Log.e(TAG, "OTR error: "+error);
    }

    @Override
    public void smpError(SessionID sessionID, int tlvType, boolean cheated) throws OtrException {
        Log.e(TAG, "OTR smp Error: "+tlvType);
    }

    @Override
    public void smpAborted(SessionID sessionID) throws OtrException {
        Log.w(TAG, "OTR smp Aborted");
    }

    @Override
    public void finishedSessionMessage(SessionID sessionID, String msgText) throws OtrException {
        Log.i(TAG, "OTR finished session message: "+msgText);
    }

    @Override
    public void requireEncryptedMessage(SessionID sessionID, String msgText) throws OtrException {
        Log.i(TAG, "OTR require encrypted message: "+msgText);
    }

    @Override
    public OtrPolicy getSessionPolicy(SessionID sessionID) {
        return policy;
    }

    @Override
    public FragmenterInstructions getFragmenterInstructions(SessionID sessionID) {
        return new FragmenterInstructions(FragmenterInstructions.UNLIMITED, FragmenterInstructions.UNLIMITED);
    }

    @Override
    public KeyPair getLocalKeyPair(SessionID sessionID) throws OtrException {
        KeyPairGenerator kg;
        try {
            kg = KeyPairGenerator.getInstance("DSA");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "No such algorithm when trying to create key pair", e);
            throw new OtrException(e);
        }
        return kg.genKeyPair();
    }

    @Override
    public byte[] getLocalFingerprintRaw(SessionID sessionID) {
        try {
            return new OtrCryptoEngineImpl().getFingerprintRaw(getLocalKeyPair(sessionID).getPublic());
        } catch (OtrException e) {
            Log.e(TAG, "Could not get key fingerprint", e);
        }
        return null;
    }

    @Override
    public void askForSecret(SessionID sessionID, InstanceTag receiverTag, String question) {
        Log.i(TAG, "OTR ask for secret: "+	question);
    }

    @Override
    public void verify(SessionID sessionID, String fingerprint, boolean approved) {
        Log.i(TAG, "OTR verify: "+fingerprint);
    }

    @Override
    public void unverify(SessionID sessionID, String fingerprint) {
        Log.i(TAG, "OTR unverify: "+fingerprint);
    }

    @Override
    public String getReplyForUnreadableMessage(SessionID sessionID) {
        Log.i(TAG, "OTR get reply for unreadable message");
        return null;
    }

    @Override
    public String getFallbackMessage(SessionID sessionID) {
        return "An off-the-record private conversation has be requested which your client does not support";
    }

    @Override
    public void messageFromAnotherInstanceReceived(SessionID sessionID) {
        Log.i(TAG, "message from another instance received");
    }

    @Override
    public void multipleInstancesDetected(SessionID sessionID) {
        Log.i(TAG, "multiple instances detected");
    }
}

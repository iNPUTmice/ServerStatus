package im.conversations.status.xmpp;

import im.conversations.status.pojo.Credentials;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;

import java.time.Duration;

public class CredentialsVerifier {
    public static boolean verifyCredentials(Credentials credentials) {
        XmppSessionConfiguration xmppSessionConfiguration = XmppSessionConfiguration.builder()
                .defaultResponseTimeout(Duration.ofSeconds(10))
                .build();
        Jid jid = credentials.getJid();
        String password = credentials.getPassword();
        try (XmppClient xmppClient = XmppClient.create(jid.getDomain(), xmppSessionConfiguration)) {
            xmppClient.connect();
            xmppClient.login(jid.getLocal(), password);
        } catch (XmppException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

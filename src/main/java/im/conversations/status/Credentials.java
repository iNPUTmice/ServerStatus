package im.conversations.status;

import rocks.xmpp.addr.Jid;

import java.util.Arrays;
import java.util.List;

public class Credentials {

    public static final List<Credentials> LIST = Arrays.asList(
            new Credentials(Jid.of("dummy@example.com"), "password")
    );

    private final Jid jid;
    private final String password;

    private Credentials(Jid jid, String password) {
        this.jid = jid;
        this.password = password;
    }

    public Jid getJid() {
        return jid;
    }

    public String getPassword() {
        return password;
    }
}
